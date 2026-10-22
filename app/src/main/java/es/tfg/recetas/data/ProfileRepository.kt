package es.tfg.recetas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import es.tfg.recetas.core.model.Allergen
import es.tfg.recetas.core.model.Diet
import es.tfg.recetas.core.model.DietaryProfile
import es.tfg.recetas.core.model.MealType
import es.tfg.recetas.core.model.Supermarket
import es.tfg.recetas.core.model.UserProfile
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "perfil_usuario")

/** Perfil guardado junto con la semilla del plan actual (para que el menú no cambie al reabrir). */
data class StoredProfile(val profile: UserProfile, val planSeed: Long)

/** Guarda y recupera el perfil del usuario en local usando Jetpack DataStore. */
class ProfileRepository(private val context: Context) {

    private object Keys {
        val supermarkets = stringSetPreferencesKey("supermercados")
        val diet = stringPreferencesKey("dieta")
        val allergens = stringSetPreferencesKey("alergenos")
        val lactose = booleanPreferencesKey("intolerancia_lactosa")
        val noPork = booleanPreferencesKey("sin_cerdo")
        val budgetCents = intPreferencesKey("presupuesto_centimos")
        val people = intPreferencesKey("personas")
        val meals = stringSetPreferencesKey("comidas")
        val seed = longPreferencesKey("semilla_plan")
    }

    suspend fun load(): StoredProfile? {
        val prefs = context.dataStore.data.first()
        val budget = prefs[Keys.budgetCents] ?: return null
        val supermarkets = prefs[Keys.supermarkets].orEmpty().mapNotNull { enumOrNull<Supermarket>(it) }.toSet()
        val meals = prefs[Keys.meals].orEmpty().mapNotNull { enumOrNull<MealType>(it) }.toSet()
        if (supermarkets.isEmpty() || meals.isEmpty()) return null
        val profile = UserProfile(
            supermarkets = supermarkets,
            dietary = DietaryProfile(
                diet = prefs[Keys.diet]?.let { enumOrNull<Diet>(it) } ?: Diet.OMNIVORE,
                allergens = prefs[Keys.allergens].orEmpty().mapNotNull { enumOrNull<Allergen>(it) }.toSet(),
                lactoseIntolerant = prefs[Keys.lactose] ?: false,
                avoidPork = prefs[Keys.noPork] ?: false,
            ),
            monthlyBudgetCents = budget,
            people = (prefs[Keys.people] ?: 1).coerceAtLeast(1),
            meals = meals,
        )
        return StoredProfile(profile, prefs[Keys.seed] ?: 0L)
    }

    suspend fun save(stored: StoredProfile) {
        val p = stored.profile
        context.dataStore.edit { prefs ->
            prefs[Keys.supermarkets] = p.supermarkets.map { it.name }.toSet()
            prefs[Keys.diet] = p.dietary.diet.name
            prefs[Keys.allergens] = p.dietary.allergens.map { it.name }.toSet()
            prefs[Keys.lactose] = p.dietary.lactoseIntolerant
            prefs[Keys.noPork] = p.dietary.avoidPork
            prefs[Keys.budgetCents] = p.monthlyBudgetCents
            prefs[Keys.people] = p.people
            prefs[Keys.meals] = p.meals.map { it.name }.toSet()
            prefs[Keys.seed] = stored.planSeed
        }
    }

    private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? =
        enumValues<T>().firstOrNull { it.name == name }
}
