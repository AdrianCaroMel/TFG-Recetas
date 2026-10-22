package es.tfg.recetas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.tfg.recetas.core.data.SampleCatalog
import es.tfg.recetas.core.model.Allergen
import es.tfg.recetas.core.model.Catalog
import es.tfg.recetas.core.model.Diet
import es.tfg.recetas.core.model.DietaryProfile
import es.tfg.recetas.core.model.MealType
import es.tfg.recetas.core.model.Supermarket
import es.tfg.recetas.core.model.UserProfile
import es.tfg.recetas.core.planner.MealPlanner
import es.tfg.recetas.core.planner.PlanResult
import es.tfg.recetas.data.ProfileRepository
import es.tfg.recetas.data.StoredProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/** Datos que el usuario va rellenando en el asistente inicial. */
data class ProfileDraft(
    val supermarkets: Set<Supermarket> = emptySet(),
    val diet: Diet = Diet.OMNIVORE,
    val allergens: Set<Allergen> = emptySet(),
    val lactoseIntolerant: Boolean = false,
    val avoidPork: Boolean = false,
    val budgetText: String = "",
    val people: Int = 1,
    val meals: Set<MealType> = setOf(MealType.LUNCH, MealType.DINNER),
) {
    val budgetCents: Int?
        get() = budgetText.replace(',', '.').toDoubleOrNull()
            ?.takeIf { it > 0 && it < 100_000 }
            ?.let { (it * 100).toInt() }

    val dietary: DietaryProfile
        get() = DietaryProfile(diet, allergens, lactoseIntolerant, avoidPork)

    fun toProfile(budget: Int? = budgetCents): UserProfile? {
        if (supermarkets.isEmpty() || meals.isEmpty() || budget == null) return null
        return UserProfile(supermarkets, dietary, budget, people, meals)
    }

    companion object {
        fun from(profile: UserProfile) = ProfileDraft(
            supermarkets = profile.supermarkets,
            diet = profile.dietary.diet,
            allergens = profile.dietary.allergens,
            lactoseIntolerant = profile.dietary.lactoseIntolerant,
            avoidPork = profile.dietary.avoidPork,
            budgetText = (profile.monthlyBudgetCents / 100.0).let {
                if (it % 1.0 == 0.0) it.toInt().toString() else "%.2f".format(it)
            },
            people = profile.people,
            meals = profile.meals,
        )
    }
}

sealed interface StartState {
    data object Loading : StartState
    data object NeedsOnboarding : StartState
    data object HasPlan : StartState
}

/** Estimación del gasto mínimo mensual mostrada en la pantalla de presupuesto. */
sealed interface MinimumEstimate {
    data object Calculating : MinimumEstimate
    data object NoRecipes : MinimumEstimate
    data class Value(val cents: Int) : MinimumEstimate
}

sealed interface PlanUiState {
    data object Empty : PlanUiState
    data object Generating : PlanUiState
    data class Ready(val result: PlanResult) : PlanUiState
}

@OptIn(FlowPreview::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {

    val catalog: Catalog = SampleCatalog.catalog
    private val planner = MealPlanner(catalog)
    private val repository = ProfileRepository(application)

    private val _startState = MutableStateFlow<StartState>(StartState.Loading)
    val startState: StateFlow<StartState> = _startState.asStateFlow()

    private val _draft = MutableStateFlow(ProfileDraft())
    val draft: StateFlow<ProfileDraft> = _draft.asStateFlow()

    private val _planState = MutableStateFlow<PlanUiState>(PlanUiState.Empty)
    val planState: StateFlow<PlanUiState> = _planState.asStateFlow()

    /** Ingredientes marcados como comprados en la lista de la compra (solo en memoria). */
    private val _boughtIngredients = MutableStateFlow<Set<String>>(emptySet())
    val boughtIngredients: StateFlow<Set<String>> = _boughtIngredients.asStateFlow()

    private var currentSeed: Long = 0L

    /**
     * Gasto mínimo estimado con las preferencias actuales del borrador. Se recalcula al cambiar
     * supermercados, restricciones, personas o comidas, y se muestra en la pantalla de presupuesto.
     */
    val minimumEstimate: StateFlow<MinimumEstimate> = _draft
        .map { it.copy(budgetText = "") }
        .distinctUntilChanged()
        .debounce(250)
        .map { draft ->
            val profile = draft.toProfile(budget = 0) ?: return@map MinimumEstimate.NoRecipes
            val cents = withContext(Dispatchers.Default) { planner.estimateMinimumCostCents(profile) }
            if (cents == null) MinimumEstimate.NoRecipes else MinimumEstimate.Value(cents)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MinimumEstimate.Calculating)

    init {
        viewModelScope.launch {
            val stored = repository.load()
            if (stored == null) {
                _startState.value = StartState.NeedsOnboarding
            } else {
                _draft.value = ProfileDraft.from(stored.profile)
                currentSeed = stored.planSeed
                _startState.value = StartState.HasPlan
                generate(stored.profile, stored.planSeed)
            }
        }
    }

    fun updateDraft(transform: (ProfileDraft) -> ProfileDraft) = _draft.update(transform)

    /** Guarda el perfil del borrador y genera un menú nuevo. Devuelve false si faltan datos. */
    fun confirmProfile(): Boolean {
        val profile = _draft.value.toProfile() ?: return false
        createPlan(profile, Random.nextLong())
        return true
    }

    /** Genera otro menú distinto con las mismas preferencias. */
    fun regenerate() {
        val profile = _draft.value.toProfile() ?: return
        createPlan(profile, Random.nextLong())
    }

    fun toggleBought(ingredientId: String) = _boughtIngredients.update {
        if (ingredientId in it) it - ingredientId else it + ingredientId
    }

    fun currentProfile(): UserProfile? = (planState.value as? PlanUiState.Ready)?.result?.let {
        when (it) {
            is PlanResult.Success -> it.plan.profile
            is PlanResult.OverBudget -> it.cheapestPlan.profile
            is PlanResult.NoRecipes -> null
        }
    } ?: _draft.value.toProfile()

    private fun createPlan(profile: UserProfile, seed: Long) {
        currentSeed = seed
        _boughtIngredients.value = emptySet()
        viewModelScope.launch { repository.save(StoredProfile(profile, seed)) }
        generate(profile, seed)
    }

    private fun generate(profile: UserProfile, seed: Long) {
        _planState.value = PlanUiState.Generating
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) { planner.plan(profile, seed) }
            if (seed == currentSeed) _planState.value = PlanUiState.Ready(result)
        }
    }
}
