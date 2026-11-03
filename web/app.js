// PickEats web: misma idea y mismos datos que la app Android, para abrirla en el navegador del ordenador.
"use strict";

// ===== Datos =====
const PRODUCTOS = new Map(window.DATOS.productos.map(p => [p.id, p]));
const RECETAS = window.DATOS.recetas.map(r => ({ ...r, ingredientes: r.ingredientes.map(([id, c]) => ({ p: PRODUCTOS.get(id), c })) }));
const RECETA_POR_ID = new Map(RECETAS.map(r => [r.id, r]));

const SUPERS = [
  { id: "MERCADONA", nombre: "Mercadona", color: "#0A8A3E" },
  { id: "DIA", nombre: "Dia", color: "#E30613" },
  { id: "LIDL", nombre: "Lidl", color: "#0050AA" },
  { id: "ALDI", nombre: "Aldi", color: "#1E3A6E" },
  { id: "ALCAMPO", nombre: "Alcampo", color: "#E2001A" },
  { id: "CARREFOUR", nombre: "Carrefour", color: "#1E5BB8" },
];
const SUPER = Object.fromEntries(SUPERS.map(s => [s.id, s]));

const sinCarne = p => !["CARNE", "PESCADO", "MARISCO"].includes(p.origen);
const RESTRICCIONES = [
  { id: "SIN_GLUTEN", nombre: "Sin gluten (celiaquía)", grupo: "alergias", permite: p => !p.alergenos.includes("GLUTEN") },
  { id: "SIN_LACTOSA", nombre: "Sin lactosa", grupo: "alergias", permite: p => !p.alergenos.includes("LACTOSA") },
  { id: "SIN_HUEVO", nombre: "Sin huevo", grupo: "alergias", permite: p => !p.alergenos.includes("HUEVO") },
  { id: "SIN_FRUTOS_SECOS", nombre: "Sin frutos secos", grupo: "alergias", permite: p => !p.alergenos.includes("FRUTOS_SECOS") },
  { id: "SIN_PESCADO", nombre: "Sin pescado", grupo: "alergias", permite: p => !p.alergenos.includes("PESCADO") },
  { id: "SIN_MARISCO", nombre: "Sin marisco", grupo: "alergias", permite: p => !p.alergenos.includes("MARISCO") },
  { id: "SIN_SOJA", nombre: "Sin soja", grupo: "alergias", permite: p => !p.alergenos.includes("SOJA") },
  { id: "VEGETARIANO", nombre: "Vegetariana", grupo: "dieta", permite: sinCarne },
  { id: "VEGANO", nombre: "Vegana", grupo: "dieta", permite: p => p.origen === "VEGETAL" },
  { id: "DIETA_BLANDA", nombre: "Dieta blanda (estómago delicado)", grupo: "salud", maxGrasas: 20,
    permite: p => !["IRRITANTE", "INDIGESTO", "GRASO"].some(e => p.etiquetas.includes(e)) },
  { id: "BAJA_EN_SAL", nombre: "Baja en sal (tensión alta)", grupo: "salud", permite: p => !p.etiquetas.includes("SALADO") },
  { id: "CONTROL_AZUCAR", nombre: "Control de azúcar (diabetes)", grupo: "salud", maxHidratos: 65, permite: () => true },
  { id: "BAJA_EN_GRASA", nombre: "Baja en grasa (colesterol)", grupo: "salud", maxGrasas: 20, permite: p => !p.etiquetas.includes("GRASO") },
];
const RESTRICCION = Object.fromEntries(RESTRICCIONES.map(r => [r.id, r]));

const ILUSTRACIONES = [["hamburguesa", "🍔"], ["burrito", "🌯"], ["tortilla", "🍳"], ["revuelto", "🍳"], ["ensalada", "🥗"],
  ["crema", "🥣"], ["sopa", "🍲"], ["puré", "🥣"], ["gambas", "🍤"], ["merluza", "🐟"], ["atún", "🐟"], ["pollo", "🍗"],
  ["lomo", "🥩"], ["albóndigas", "🥩"], ["berenjena", "🍆"], ["espaguetis", "🍝"], ["macarrones", "🍝"], ["pasta", "🍝"],
  ["arroz", "🍚"], ["lentejas", "🍲"], ["alubias", "🫘"], ["garbanzos", "🫘"], ["brócoli", "🥦"], ["champiñones", "🍄"],
  ["tofu", "🥢"], ["quinoa", "🥗"], ["cuscús", "🥘"], ["pisto", "🥘"]];
const ilustracion = r => (ILUSTRACIONES.find(([clave]) => r.nombre.toLowerCase().includes(clave)) || [0, "🍽️"])[1];

// ===== Formato =====
const euros = n => n.toLocaleString("es-ES", { style: "currency", currency: "EUR" });
const cantidad = (v, u) => u === "ud" ? `${+v.toFixed(1)} ud.` : v >= 1000 ? `${+(v / 1000).toFixed(2)} ${u === "g" ? "kg" : "l"}` : `${Math.round(v)} ${u}`;
const esc = t => String(t).replace(/[&<>"]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
const personasTexto = n => `${n} ${n === 1 ? "persona" : "personas"}`;

// ===== Estado guardado en el navegador =====
const CLAVE = "pickeats-web-v1";
let estado = { perfil: null, plan: null, comprados: [] };
try { estado = { ...estado, ...JSON.parse(localStorage.getItem(CLAVE) || "{}") }; } catch (e) { /* sin almacenamiento: empezamos de cero */ }
function guardar() { try { localStorage.setItem(CLAVE, JSON.stringify(estado)); } catch (e) { /* modo privado */ } }

let borrador = estado.perfil ? JSON.parse(JSON.stringify(estado.perfil)) : {
  supers: [], restricciones: [], presupuesto: 150, personas: 1, comida: true, cena: true, basicos: false, unSolo: true,
};

// ===== Lógica (port de logica/ en Java) =====
function nutricion(r) {
  const n = { kcal: 0, prot: 0, hid: 0, gra: 0, peso: 0 };
  for (const { p, c } of r.ingredientes) {
    const f = p.unidad === "ud" ? c : c / 100;
    n.kcal += p.nutrientes[0] * f; n.prot += p.nutrientes[1] * f; n.hid += p.nutrientes[2] * f; n.gra += p.nutrientes[3] * f;
    if (p.unidad !== "ud") n.peso += c;
  }
  return n;
}

const seVende = (p, s) => p.precios[s] != null;
function mejorSuper(p, supers) {
  let mejor = null;
  for (const s of supers) if (seVende(p, s) && (mejor === null || p.precios[s] < p.precios[mejor])) mejor = s;
  return mejor;
}

function recetaValida(r, perfil, supers) {
  const rs = perfil.restricciones.map(id => RESTRICCION[id]);
  if (!r.ingredientes.every(({ p }) => rs.every(x => x.permite(p)) && supers.some(s => seVende(p, s)))) return false;
  const n = nutricion(r);
  return rs.every(x => (!x.maxGrasas || n.gra <= x.maxGrasas) && (!x.maxHidratos || n.hid <= x.maxHidratos));
}

// Coste de una cesta comprando envases enteros, cada producto donde salga más barato
function costeCesta(cesta, perfil, supers) {
  let total = 0;
  for (const [id, q] of cesta) {
    const p = PRODUCTOS.get(id);
    if (perfil.basicos && p.categoria === "Básicos") continue;
    const s = mejorSuper(p, supers);
    if (!s) return Infinity;
    total += Math.ceil(q / p.envase - 1e-9) * p.precios[s];
  }
  return total;
}
function anadir(cesta, r, personas) {
  const nueva = new Map(cesta);
  for (const { p, c } of r.ingredientes) nueva.set(p.id, (nueva.get(p.id) || 0) + c * personas);
  return nueva;
}
const costeRacion = (r, perfil, supers) => {
  let t = 0;
  for (const { p, c } of r.ingredientes) {
    if (perfil.basicos && p.categoria === "Básicos") continue;
    const s = mejorSuper(p, supers);
    if (s) t += p.precios[s] * c / p.envase;
  }
  return t;
};

function azar(semilla) { let x = semilla || 1; return () => (x = (x * 16807) % 2147483647) / 2147483647; }

// Plan voraz: en cada hueco elige la receta que menos sube la cuesta, castigando las repetidas
function construir(perfil, supers, semilla, presupuesto) {
  const momentos = [perfil.comida && "COMIDA", perfil.cena && "CENA"].filter(Boolean);
  const validas = RECETAS.filter(r => recetaValida(r, perfil, supers));
  const rnd = azar(semilla);
  const usos = new Map();
  const dias = [];
  let cesta = new Map(), coste = 0, sinDinero = false;
  for (let d = 0; d < 30; d++) {
    const dia = {};
    for (const m of momentos) {
      const candidatas = validas.filter(r => r.tipo === m || r.tipo === "AMBAS");
      let mejor = null, mejorNota = Infinity, mejorCoste = 0;
      for (const r of candidatas) {
        const reciente = dias.slice(-3).some(x => Object.values(x).includes(r.id)) || Object.values(dia).includes(r.id);
        const extra = costeCesta(anadir(cesta, r, perfil.personas), perfil, supers) - coste;
        const nota = extra + (usos.get(r.id) || 0) * 1.1 * perfil.personas + (reciente ? 50 : 0) + rnd() * 1.4;
        if (nota < mejorNota) { mejor = r; mejorNota = nota; mejorCoste = extra; }
      }
      if (!mejor || sinDinero || coste + mejorCoste > presupuesto) { sinDinero = sinDinero || !!mejor; dia[m] = null; continue; }
      dia[m] = mejor.id;
      cesta = anadir(cesta, mejor, perfil.personas);
      coste += mejorCoste;
      usos.set(mejor.id, (usos.get(mejor.id) || 0) + 1);
    }
    dias.push(dia);
  }
  return { dias, coste, supers, sinDinero, validas: validas.length };
}

function cestaDe(dias, personas) {
  let cesta = new Map();
  for (const dia of dias) for (const id of Object.values(dia)) if (id) cesta = anadir(cesta, RECETA_POR_ID.get(id), personas);
  return cesta;
}

function generarPlan(perfil, semilla) {
  const opciones = perfil.unSolo ? perfil.supers.map(s => [s]) : [perfil.supers];
  let mejor = null;
  for (const supers of opciones) {
    const plan = construir(perfil, supers, semilla, perfil.presupuesto);
    const llenos = plan.dias.reduce((n, d) => n + Object.values(d).filter(Boolean).length, 0);
    plan.llenos = llenos;
    if (!mejor || llenos > mejor.llenos || (llenos === mejor.llenos && plan.coste < mejor.coste)) mejor = plan;
  }
  // Si el presupuesto no llega, calcular cuánto haría falta para el mes completo
  if (mejor.sinDinero) mejor.necesario = construir(perfil, mejor.supers, semilla, Infinity).coste;
  return { dias: mejor.dias, supers: mejor.supers, semilla, necesario: mejor.necesario || null };
}

function minimoEstimado(perfil) {
  if (!perfil.supers.length || !(perfil.comida || perfil.cena)) return null;
  const opciones = perfil.unSolo ? perfil.supers.map(s => [s]) : [perfil.supers];
  let min = Infinity;
  for (const supers of opciones) {
    const p = construir({ ...perfil }, supers, 7, Infinity);
    if (p.validas > 0 && p.dias.every(d => Object.values(d).every(Boolean))) min = Math.min(min, p.coste);
  }
  return min === Infinity ? null : min;
}

function listaCompra(plan, perfil) {
  const cesta = cestaDe(plan.dias, perfil.personas);
  const lineas = [];
  for (const [id, q] of cesta) {
    const p = PRODUCTOS.get(id);
    if (perfil.basicos && p.categoria === "Básicos") continue;
    const s = mejorSuper(p, plan.supers);
    const envases = Math.ceil(q / p.envase - 1e-9);
    lineas.push({ p, s, q, envases, total: envases * p.precios[s] });
  }
  return lineas.sort((a, b) => a.s.localeCompare(b.s) || a.p.categoria.localeCompare(b.p.categoria) || a.p.nombre.localeCompare(b.p.nombre));
}

// Mismo plan en cada supermercado del usuario (solo los que venden todo)
function compararSupers(plan, perfil) {
  const cesta = cestaDe(plan.dias, perfil.personas);
  return perfil.supers.map(s => ({ s, coste: costeCesta(cesta, perfil, [s]) }))
    .filter(x => x.coste !== Infinity).sort((a, b) => a.coste - b.coste);
}

// Tiempo más largo dentro del texto de un paso, en segundos (igual que Temporizadores.java)
function segundosDe(paso) {
  let mayor = 0;
  for (const m of paso.matchAll(/(\d+)\s*(?:(?:-|–|a)\s*(\d+)\s*)?(segundos?|seg\b|minutos?|min\b|horas?)/gi)) {
    const u = m[3].toLowerCase();
    mayor = Math.max(mayor, +m[1] * (u.startsWith("seg") ? 1 : u.startsWith("hora") ? 3600 : 60));
  }
  return mayor;
}
const reloj = s => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;

// ===== Interfaz =====
const app = document.getElementById("app");
function avisar(texto) {
  const a = document.getElementById("aviso");
  a.textContent = texto; a.classList.add("visible");
  clearTimeout(avisar.t); avisar.t = setTimeout(() => a.classList.remove("visible"), 2800);
}

function pintarMenu(ruta) {
  const enlaces = [["inicio", "Inicio"], ["plan", "Mi plan"], ["recetario", "Recetas"], ["lista", "Compra"]];
  document.getElementById("menu").innerHTML = enlaces
    .filter(([r]) => estado.plan || r === "inicio" || r === "recetario")
    .map(([r, t]) => `<a href="#${r}" ${ruta === r ? 'aria-current="page"' : ""}>${t}</a>`).join("");
}

function pantallaInicio() {
  const tienePlan = !!estado.plan;
  return `<section class="pantalla">
    <div class="portada">
      <div>
        <p class="etiqueta">Tu mes de comidas, resuelto</p>
        <h1 style="margin-top:12px">30 días de comida casera con <em>${euros(estado.perfil ? estado.perfil.presupuesto : 150).replace(",00", "")}</em></h1>
        <p class="entradilla">Dinos dónde compras, qué no puedes comer y cuánto quieres gastar. Te damos el menú del mes, cada receta paso a paso y la lista de la compra.</p>
        <div class="fila" style="flex-wrap:wrap">
          ${tienePlan ? `<a class="boton" href="#plan">Ver mi plan</a><a class="boton secundario" href="#paso1">Cambiar mis datos</a>`
                      : `<a class="boton" href="#paso1">Empezar ahora</a><a class="boton secundario" href="#recetario">Ver recetas</a>`}
        </div>
      </div>
      <div class="plato" aria-hidden="true">
        <span class="comida-plato">🥗</span>
        <div class="nota n1">${RECETAS.length} recetas<small>pensadas para principiantes</small></div>
        <div class="nota n2">6 supermercados<small>con precios reales</small></div>
      </div>
    </div>
    <ol class="pasos-inicio">
      <li class="tarjeta"><h3>Elige tus supermercados</h3><p class="suave">Mercadona, Dia, Lidl, Aldi, Alcampo o Carrefour.</p></li>
      <li class="tarjeta"><h3>Marca lo que no comes</h3><p class="suave">Alergias, dieta vegana o vegetariana y dietas por salud.</p></li>
      <li class="tarjeta"><h3>Pon tu presupuesto</h3><p class="suave">Si no llega, te decimos cuánto haría falta.</p></li>
    </ol>
  </section>`;
}

function cabeceraPaso(n, titulo, ayuda) {
  return `<p class="etiqueta">Paso ${n} de 3</p>
    <div class="progreso"><span style="width:${n * 33.4}%"></span></div>
    <h2>${titulo}</h2><p class="suave" style="margin-top:8px">${ayuda}</p>`;
}
const casilla = (nombre, valor, marcado, texto, extra = "") =>
  `<label class="opcion"><input type="checkbox" name="${nombre}" value="${valor}" ${marcado ? "checked" : ""}>${extra}<span>${texto}</span><span class="tic"></span></label>`;

function pantallaPaso1() {
  return `<section class="pantalla asistente">
    ${cabeceraPaso(1, "¿Dónde puedes comprar?", "Marca todos los que te vengan bien.")}
    <div class="opciones">${SUPERS.map(s => casilla("super", s.id, borrador.supers.includes(s.id), s.nombre,
      `<span class="marca-super" style="background:${s.color}"></span>`)).join("")}</div>
    <div class="acciones"><a class="boton secundario" href="#inicio">Volver</a><button class="boton" data-ir="paso2">Siguiente</button></div>
  </section>`;
}

function pantallaPaso2() {
  const grupo = (g, titulo) => `<h3 class="grupo-titulo">${titulo}</h3><div class="opciones">${RESTRICCIONES.filter(r => r.grupo === g)
    .map(r => casilla("restriccion", r.id, borrador.restricciones.includes(r.id), r.nombre)).join("")}</div>`;
  return `<section class="pantalla asistente">
    ${cabeceraPaso(2, "¿Hay algo que no puedas comer?", "Si no tienes ninguna restricción, deja todo sin marcar.")}
    ${grupo("alergias", "Alergias e intolerancias")}${grupo("dieta", "Dieta")}${grupo("salud", "Dietas por salud")}
    <div class="acciones"><a class="boton secundario" href="#paso1">Atrás</a><button class="boton" data-ir="paso3">Siguiente</button></div>
  </section>`;
}

function pantallaPaso3() {
  return `<section class="pantalla asistente">
    ${cabeceraPaso(3, "Tu presupuesto", "Solo lo que gastas en el supermercado para comer.")}
    <label class="campo"><span>Presupuesto al mes</span>
      <div class="importe"><output id="vPresu">${borrador.presupuesto}</output> € <small>al mes</small></div>
      <input type="range" id="presu" min="30" max="600" step="5" value="${borrador.presupuesto}">
    </label>
    <div class="campo"><span>Personas que coméis</span>
      <div class="contador"><button type="button" data-personas="-1" aria-label="Una persona menos">−</button><output id="vPersonas">${borrador.personas}</output><button type="button" data-personas="1" aria-label="Una persona más">+</button></div>
    </div>
    <div class="opciones">
      ${casilla("comida", 1, borrador.comida, "Planificar comidas")}
      ${casilla("cena", 1, borrador.cena, "Planificar cenas")}
      ${casilla("basicos", 1, borrador.basicos, "Ya tengo aceite, sal y pimentón")}
      ${casilla("unSolo", 1, borrador.unSolo, "Toda la compra en un solo súper")}
    </div>
    <div id="estimacion" class="estimacion"></div>
    <div class="acciones"><a class="boton secundario" href="#paso2">Atrás</a><button class="boton" id="generar">Generar mi plan</button></div>
  </section>`;
}

function actualizarEstimacion() {
  const caja = document.getElementById("estimacion");
  if (!caja) return;
  const min = minimoEstimado(borrador);
  caja.classList.toggle("mal", min === null || min > borrador.presupuesto);
  caja.textContent = min === null ? "No hay recetas para todo lo que has marcado. Prueba a elegir más supermercados o quitar alguna restricción."
    : min > borrador.presupuesto ? `El mínimo estimado es ${euros(min)}. Con menos solo podremos planificar parte del mes.`
    : `Tu presupuesto cubre el mínimo estimado (${euros(min)}).`;
}

function pantallaPlan() {
  const plan = estado.plan, perfil = estado.perfil;
  if (!plan) { location.hash = "#inicio"; return ""; }
  const coste = costeCesta(cestaDe(plan.dias, perfil.personas), perfil, plan.supers);
  const distintas = new Set(plan.dias.flatMap(d => Object.values(d)).filter(Boolean)).size;
  const pct = Math.min(100, Math.round(coste * 100 / perfil.presupuesto));
  const semana = pantallaPlan.semana || 0;
  const desde = semana * 7, hasta = Math.min(30, desde + 7);
  const sobra = perfil.presupuesto - coste;
  return `<section class="pantalla">
    <div class="tarjeta cabecera-plan">
      <div class="anillo" style="--p:${pct}"><div><div><strong>${euros(coste)}</strong><small>de ${euros(perfil.presupuesto)}</small></div></div></div>
      <div>
        <p class="etiqueta">Mi plan del mes</p>
        <h2 style="margin-top:6px">${plan.supers.length === 1 ? `Toda la compra en ${SUPER[plan.supers[0]].nombre}` : "Cada producto donde sale más barato"}</h2>
        <p class="suave" style="margin-top:6px">${distintas} recetas distintas · ${personasTexto(perfil.personas)}${sobra > 0 ? ` · te sobran ${euros(sobra)} para desayunos y fruta` : ""}</p>
      </div>
      <div class="acciones-plan fila" style="flex-wrap:wrap">
        <a class="boton" href="#lista">Lista de la compra</a>
        <button class="boton secundario" id="otroPlan">Otro plan</button>
      </div>
    </div>
    ${plan.necesario ? `<div class="alerta">Con ${euros(perfil.presupuesto)} no llega para todo el mes. Necesitarías unos ${euros(plan.necesario)}.</div>` : ""}
    <div class="pestanas" role="tablist">${[0, 1, 2, 3, 4].map(i => `<button role="tab" data-semana="${i}" aria-selected="${i === semana}">${i < 4 ? `Semana ${i + 1}` : "Días 29 y 30"}</button>`).join("")}</div>
    <div class="dias">${plan.dias.slice(desde, semana === 4 ? 30 : Math.min(hasta, 28)).map((dia, k) => {
      const n = desde + k + 1;
      return `<article class="tarjeta dia"><div class="dia-titulo"><h3>Día ${n}</h3></div>
        ${Object.entries(dia).map(([m, id]) => platoDia(m, id, perfil, plan)).join("")}</article>`;
    }).join("")}</div>
  </section>`;
}

function platoDia(momento, id, perfil, plan) {
  const etiqueta = `<span class="pildora ${momento === "COMIDA" ? "comida" : "cena"}">${momento === "COMIDA" ? "Comida" : "Cena"}</span>`;
  if (!id) return `<div class="plato-dia vacio">${etiqueta}<span>Sin presupuesto para este hueco</span></div>`;
  const r = RECETA_POR_ID.get(id);
  return `<button class="plato-dia" data-receta="${id}">
    <span class="ilustracion" aria-hidden="true">${ilustracion(r)}</span>
    <span><span class="nombre">${esc(r.nombre)}</span><br><span class="datos">${etiqueta} ${r.minutos} min · ${euros(costeRacion(r, perfil, plan.supers))} ración</span></span>
  </button>`;
}

function pantallaRecetario() {
  const filtro = pantallaRecetario.filtro || "TODAS";
  const lista = RECETAS.filter(r => filtro === "TODAS" || r.tipo === filtro || r.tipo === "AMBAS");
  return `<section class="pantalla">
    <p class="etiqueta">Recetario</p><h2 style="margin-top:6px">${RECETAS.length} recetas explicadas para tu primera vez</h2>
    <div class="pestanas">${[["TODAS", "Todas"], ["COMIDA", "Comidas"], ["CENA", "Cenas"]].map(([f, t]) =>
      `<button data-filtro="${f}" aria-selected="${f === filtro}">${t}</button>`).join("")}</div>
    <div class="dias">${lista.map(r => {
      const n = nutricion(r);
      return `<article class="tarjeta dia"><button class="plato-dia" data-receta="${r.id}">
        <span class="ilustracion" aria-hidden="true">${ilustracion(r)}</span>
        <span><span class="nombre">${esc(r.nombre)}</span><br><span class="datos">${r.minutos} min · ${Math.round(n.kcal)} kcal</span></span>
      </button></article>`;
    }).join("")}</div>
  </section>`;
}

function pantallaReceta(id) {
  const r = RECETA_POR_ID.get(+id);
  if (!r) return "<p>Receta no encontrada.</p>";
  const perfil = estado.perfil || borrador;
  const supers = estado.plan ? estado.plan.supers : (perfil.supers.length ? perfil.supers : SUPERS.map(s => s.id));
  const personas = perfil.personas || 1;
  const n = nutricion(r);
  return `<section class="pantalla">
    <div class="receta-cabecera">
      <span class="ilustracion" aria-hidden="true">${ilustracion(r)}</span>
      <div>
        <p class="etiqueta">${r.tipo === "AMBAS" ? "Comida o cena" : r.tipo === "COMIDA" ? "Comida" : "Cena"}</p>
        <h1 style="font-size:clamp(2rem,4.5vw,3.2rem);margin-top:8px">${esc(r.nombre)}</h1>
        <div class="chips">
          <span class="pildora">${r.minutos} min</span><span class="pildora">${personasTexto(personas)}</span>
          <span class="pildora">${euros(costeRacion(r, perfil, supers))} por ración</span><span class="pildora">${r.pasos.length} pasos</span>
        </div>
        <button class="boton" style="margin-top:24px" data-cocinar="${r.id}">Empezar a cocinar</button>
      </div>
    </div>
    <div class="columnas">
      <div class="pila">
        <div class="tarjeta">
          <h3>Por ración</h3>
          <div class="nutricion">
            <div><strong>${Math.round(n.kcal)}</strong><small>kcal</small></div>
            <div><strong>${Math.round(n.prot)} g</strong><small>proteínas</small></div>
            <div><strong>${Math.round(n.hid)} g</strong><small>hidratos</small></div>
            <div><strong>${Math.round(n.gra)} g</strong><small>grasas</small></div>
          </div>
          <p class="suave" style="margin-top:12px;font-size:.9rem">Unos ${Math.round(n.peso)} g en crudo, el ${Math.round(n.kcal / 20)} % de las calorías de un día. Valores aproximados.</p>
        </div>
        <div class="tarjeta">
          <h3>Ingredientes</h3>
          ${r.ingredientes.map(({ p, c }) => {
            const s = mejorSuper(p, supers);
            const donde = perfil.basicos && p.categoria === "Básicos" ? "Ya lo tienes en casa"
              : s ? `${SUPER[s].nombre} · ${euros(p.precios[s])} el envase` : "";
            return `<label class="ingrediente"><input type="checkbox"><span class="info"><span class="nombre">${esc(p.nombre)}</span><br><span class="donde">${donde}</span></span><span class="cantidad">${cantidad(c * personas, p.unidad)}</span></label>`;
          }).join("")}
        </div>
      </div>
      <div class="tarjeta"><h3>Preparación</h3><ol class="lista-pasos">${r.pasos.map(p => `<li><span>${esc(p)}</span></li>`).join("")}</ol></div>
    </div>
  </section>`;
}

function pantallaLista() {
  const plan = estado.plan, perfil = estado.perfil;
  if (!plan) { location.hash = "#inicio"; return ""; }
  const lineas = listaCompra(plan, perfil);
  const total = lineas.reduce((t, l) => t + l.total, 0);
  const hechos = lineas.filter(l => estado.comprados.includes(l.p.id)).length;
  const comparacion = compararSupers(plan, perfil);
  const caro = comparacion.length ? comparacion[comparacion.length - 1].coste : 1;
  const grupos = [...new Set(lineas.map(l => l.s))];
  return `<section class="pantalla">
    <div class="tarjeta">
      <div class="fila entre"><div><p class="etiqueta">Lista de la compra</p><h2 style="margin-top:6px">${euros(total)} en total</h2></div>
      <button class="boton secundario" id="copiar">Copiar lista</button></div>
      <p class="suave" style="margin-top:8px" id="contador">${hechos} de ${lineas.length} productos comprados</p>
      <div class="progreso" style="margin-bottom:0"><span id="barraCompra" style="width:${lineas.length ? hechos * 100 / lineas.length : 0}%"></span></div>
    </div>
    ${comparacion.length > 1 ? `<div class="tarjeta comparacion" style="margin-top:16px"><h3>Este mismo plan en tus supermercados</h3>
      ${comparacion.map((x, i) => `<div class="barra-super ${i === 0 ? "ganador" : ""}"><span>${SUPER[x.s].nombre}</span>
        <span class="pista"><span data-ancho="${Math.round(x.coste * 100 / caro)}"></span></span>
        <span>${i === 0 ? euros(x.coste) : "+" + euros(x.coste - comparacion[0].coste)}</span></div>`).join("")}</div>` : ""}
    ${grupos.map(s => `<div class="tarjeta grupo-super">
      <div class="fila entre"><h3><span class="punto" style="background:${SUPER[s].color}"></span>${SUPER[s].nombre}</h3>
      <strong>${euros(lineas.filter(l => l.s === s).reduce((t, l) => t + l.total, 0))}</strong></div>
      ${lineas.filter(l => l.s === s).map(l => `<label class="producto"><input type="checkbox" data-comprado="${l.p.id}" ${estado.comprados.includes(l.p.id) ? "checked" : ""}>
        <span><span class="nombre">${esc(l.p.nombre)}</span><small>${l.envases} × ${cantidad(l.p.envase, l.p.unidad)} · necesitas ${cantidad(l.q, l.p.unidad)}${l.p.verificado[s] ? ' · <span class="verificado">precio verificado</span>' : " · precio estimado"}</small></span>
        <span class="precio">${euros(l.total)}</span></label>`).join("")}
    </div>`).join("")}
    ${perfil.basicos ? '<p class="suave" style="margin-top:16px">Sin aceite, sal ni pimentón: ya los tienes en casa.</p>' : ""}
  </section>`;
}

// ===== Modo cocina =====
let cocina = null;
function abrirCocina(id) {
  const r = RECETA_POR_ID.get(+id);
  cocina = { r, paso: 0, fin: false, restante: 0, total: 0, t: null };
  const capa = document.getElementById("cocina");
  capa.hidden = false;
  document.body.style.overflow = "hidden";
  pintarCocina();
  if (navigator.wakeLock) navigator.wakeLock.request("screen").then(w => { cocina.bloqueo = w; }).catch(() => {});
}
function cerrarCocina() {
  if (!cocina) return;
  clearInterval(cocina.t);
  if (cocina.bloqueo) cocina.bloqueo.release();
  cocina = null;
  document.getElementById("cocina").hidden = true;
  document.body.style.overflow = "";
}
function pintarCocina() {
  const c = cocina, capa = document.getElementById("cocina");
  const n = c.r.pasos.length;
  clearInterval(c.t); c.t = null;
  if (!c.fin) { c.total = segundosDe(c.r.pasos[c.paso]); c.restante = c.total; }
  capa.innerHTML = `<div class="arriba"><div><p class="etiqueta" style="color:#F4C9A8">${c.fin ? "Listo" : `Paso ${c.paso + 1} de ${n}`}</p>
      <h3 style="margin-top:4px">${esc(c.r.nombre)}</h3></div><button class="cerrar" data-cocina="cerrar" aria-label="Cerrar modo cocina">✕</button></div>
    <div class="barra-pasos"><span style="width:${c.fin ? 100 : (c.paso + 1) * 100 / n}%"></span></div>
    <div class="paso" key="${c.paso}"><div class="numero">${c.fin ? "✓" : c.paso + 1}</div>
      ${c.fin ? "Plato terminado. Sírvelo recién hecho y que aproveche." : esc(c.r.pasos[c.paso])}</div>
    ${!c.fin && c.total ? `<div class="reloj" id="reloj">
      <svg viewBox="0 0 104 104"><circle cx="52" cy="52" r="46" fill="none" stroke="rgba(255,255,255,.15)" stroke-width="8"/>
      <circle id="arco" cx="52" cy="52" r="46" fill="none" stroke="#F4C9A8" stroke-width="8" stroke-linecap="round" stroke-dasharray="289" stroke-dashoffset="0"/></svg>
      <div><div class="tiempo" id="tiempo">${reloj(c.total)}</div><p style="opacity:.85">Este paso lleva su tiempo. Pon el temporizador y te avisamos.</p>
      <button class="boton claro" style="margin-top:8px" data-cocina="reloj">Iniciar ${reloj(c.total)}</button></div></div>` : ""}
    <div class="abajo"><button class="boton contorno" data-cocina="atras" ${c.paso === 0 && !c.fin ? "disabled" : ""}>Anterior</button>
      <button class="boton claro" data-cocina="${c.fin ? "cerrar" : "siguiente"}">${c.fin ? "Volver a la receta" : c.paso === n - 1 ? "¡Terminado!" : "Siguiente"}</button></div>`;
}
function alternarReloj(boton) {
  const c = cocina;
  if (c.t) { clearInterval(c.t); c.t = null; boton.textContent = "Seguir"; return; }
  if (c.restante <= 0) c.restante = c.total;
  boton.textContent = "Pausar";
  let ultimo = Date.now(), acumulado = 0;
  c.t = setInterval(() => {
    const ahora = Date.now(); acumulado += ahora - ultimo; ultimo = ahora;
    if (acumulado >= 1000) { c.restante -= Math.floor(acumulado / 1000); acumulado %= 1000; }
    document.getElementById("tiempo").textContent = reloj(Math.max(0, c.restante));
    document.getElementById("arco").setAttribute("stroke-dashoffset", String(289 * (1 - Math.max(0, c.restante) / c.total)));
    if (c.restante <= 0) { clearInterval(c.t); c.t = null; boton.textContent = `Iniciar ${reloj(c.total)}`; finTiempo(); }
  }, 200);
}
function finTiempo() {
  document.getElementById("reloj").classList.add("sonando");
  if (navigator.vibrate) navigator.vibrate([400, 200, 400, 200, 600]);
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    [0, .35, .7].forEach(t => {
      const o = ctx.createOscillator(), g = ctx.createGain();
      o.frequency.value = 880; o.connect(g); g.connect(ctx.destination);
      g.gain.setValueAtTime(.25, ctx.currentTime + t); g.gain.exponentialRampToValueAtTime(.001, ctx.currentTime + t + .3);
      o.start(ctx.currentTime + t); o.stop(ctx.currentTime + t + .3);
    });
  } catch (e) { /* sin sonido */ }
  avisar("¡Tiempo! Mira cómo va antes de seguir.");
}

function confeti() {
  const colores = ["#3E6A4F", "#A94E27", "#F4E3B5", "#F7E2D5", "#9FC9A9"];
  for (let i = 0; i < 60; i++) {
    const c = document.createElement("span");
    c.className = "confeti";
    c.style.left = Math.random() * 100 + "vw";
    c.style.background = colores[i % colores.length];
    c.style.animationDuration = 1.8 + Math.random() * 1.6 + "s";
    c.style.animationDelay = Math.random() * .5 + "s";
    document.body.appendChild(c);
    setTimeout(() => c.remove(), 4200);
  }
  avisar("¡Compra completada! Ya tienes todo lo del mes.");
}

// ===== Rutas y eventos =====
function pintar() {
  const [ruta, arg] = (location.hash.slice(1) || "inicio").split("/");
  const pantallas = { inicio: pantallaInicio, paso1: pantallaPaso1, paso2: pantallaPaso2, paso3: pantallaPaso3,
    plan: pantallaPlan, recetario: pantallaRecetario, receta: () => pantallaReceta(arg), lista: pantallaLista };
  app.innerHTML = (pantallas[ruta] || pantallaInicio)();
  pintarMenu(ruta);
  if (ruta === "paso3") actualizarEstimacion();
  if (ruta === "lista") requestAnimationFrame(() => document.querySelectorAll("[data-ancho]").forEach(b => { b.style.width = b.dataset.ancho + "%"; }));
  window.scrollTo(0, 0);
}
window.addEventListener("hashchange", () => { cerrarCocina(); pintar(); });

function leerFormulario() {
  const marcados = n => [...document.querySelectorAll(`input[name=${n}]:checked`)].map(i => i.value);
  if (document.querySelector("input[name=super]")) borrador.supers = marcados("super");
  if (document.querySelector("input[name=restriccion]")) borrador.restricciones = marcados("restriccion");
  ["comida", "cena", "basicos", "unSolo"].forEach(n => {
    const i = document.querySelector(`input[name=${n}]`);
    if (i) borrador[n] = i.checked;
  });
}

app.addEventListener("change", e => {
  leerFormulario();
  const id = e.target.dataset.comprado;
  if (id) {
    estado.comprados = e.target.checked ? [...estado.comprados, +id] : estado.comprados.filter(x => x !== +id);
    guardar();
    const total = listaCompra(estado.plan, estado.perfil).length;
    const hechos = estado.comprados.length;
    document.getElementById("contador").textContent = `${hechos} de ${total} productos comprados`;
    document.getElementById("barraCompra").style.width = hechos * 100 / total + "%";
    if (e.target.checked && hechos >= total) confeti();
  }
  actualizarEstimacion();
});

app.addEventListener("input", e => {
  if (e.target.id === "presu") {
    borrador.presupuesto = +e.target.value;
    document.getElementById("vPresu").textContent = borrador.presupuesto;
    clearTimeout(app.t); app.t = setTimeout(actualizarEstimacion, 120);
  }
});

app.addEventListener("click", e => {
  const b = e.target.closest("button, [data-receta]");
  if (!b) return;
  if (b.dataset.ir === "paso2" && !borrador.supers.length) return avisar("Elige al menos un supermercado.");
  if (b.dataset.ir) location.hash = "#" + b.dataset.ir;
  if (b.dataset.personas) {
    borrador.personas = Math.min(10, Math.max(1, borrador.personas + +b.dataset.personas));
    document.getElementById("vPersonas").textContent = borrador.personas;
    actualizarEstimacion();
  }
  if (b.id === "generar") {
    if (!borrador.comida && !borrador.cena) return avisar("Marca al menos comida o cena.");
    estado.perfil = JSON.parse(JSON.stringify(borrador));
    estado.plan = generarPlan(estado.perfil, Date.now() % 100000);
    estado.comprados = [];
    guardar(); pantallaPlan.semana = 0;
    location.hash = "#plan";
  }
  if (b.id === "otroPlan") {
    estado.plan = generarPlan(estado.perfil, estado.plan.semilla + 7919);
    estado.comprados = [];
    guardar(); pintar(); avisar("Nuevo plan generado.");
  }
  if (b.dataset.semana) { pantallaPlan.semana = +b.dataset.semana; pintar(); }
  if (b.dataset.filtro) { pantallaRecetario.filtro = b.dataset.filtro; pintar(); }
  if (b.dataset.receta) location.hash = "#receta/" + b.dataset.receta;
  if (b.dataset.cocinar) abrirCocina(b.dataset.cocinar);
  if (b.id === "copiar") {
    const lineas = listaCompra(estado.plan, estado.perfil);
    const texto = "Lista de la compra (PickEats)\n" + lineas.map(l => `- ${l.p.nombre}: ${l.envases} × ${cantidad(l.p.envase, l.p.unidad)} en ${SUPER[l.s].nombre} (${euros(l.total)})`).join("\n");
    navigator.clipboard.writeText(texto).then(() => avisar("Lista copiada."), () => avisar("No se pudo copiar."));
  }
});

document.getElementById("cocina").addEventListener("click", e => {
  const b = e.target.closest("[data-cocina]");
  if (!b || !cocina) return;
  const accion = b.dataset.cocina;
  if (accion === "cerrar") cerrarCocina();
  if (accion === "reloj") alternarReloj(b);
  if (accion === "siguiente") { if (cocina.paso < cocina.r.pasos.length - 1) cocina.paso++; else cocina.fin = true; pintarCocina(); }
  if (accion === "atras") { if (cocina.fin) cocina.fin = false; else cocina.paso--; pintarCocina(); }
});
document.addEventListener("keydown", e => {
  if (!cocina) return;
  if (e.key === "Escape") cerrarCocina();
  if (e.key === "ArrowRight") document.querySelector('[data-cocina="siguiente"]')?.click();
  if (e.key === "ArrowLeft") document.querySelector('[data-cocina="atras"]:not([disabled])')?.click();
});

pintar();
