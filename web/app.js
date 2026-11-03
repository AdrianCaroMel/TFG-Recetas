// PickEats web: misma lógica y mismos datos que la app Android, con una interfaz propia para el navegador.
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
  // Con un solo súper, el mismo carro puede salir más barato en otra cadena: entonces se compra allí
  if (perfil.unSolo && perfil.supers.length > 1) {
    const cesta = cestaDe(mejor.dias, perfil.personas);
    const actual = costeCesta(cesta, perfil, mejor.supers);
    for (const s of perfil.supers) if (costeCesta(cesta, perfil, [s]) < actual - 0.005 && costeCesta(cesta, perfil, [s]) <= costeCesta(cesta, perfil, mejor.supers)) mejor.supers = [s];
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


// ===== Platos dibujados: cada ingrediente es un sector del color de su categoría =====
const GRUPOS = {
  "Cereales y pasta": ["cereal", "Cereales"], "Verduras": ["verdura", "Verdura"], "Congelados": ["verdura", "Verdura"],
  "Legumbres": ["legumbre", "Legumbres"], "Proteína vegetal": ["legumbre", "Legumbres"], "Carne": ["carne", "Carne"],
  "Pescado": ["pescado", "Pescado"], "Marisco": ["pescado", "Pescado"], "Lácteos": ["lacteo", "Lácteos"],
  "Huevos": ["huevo", "Huevo"], "Básicos": ["basico", "Condimentos"], "Salsas": ["basico", "Condimentos"], "Conservas": ["otro", "Despensa"],
};
const grupoDe = p => GRUPOS[p.categoria] || ["otro", "Despensa"];

function composicion(r) {
  // Peso aproximado de cada ingrediente; las unidades cuentan como 50 g
  const partes = r.ingredientes.map(({ p, c }) => ({ g: grupoDe(p), peso: p.unidad === "ud" ? c * 50 : c }))
    .filter(x => x.peso >= 3);
  const total = partes.reduce((t, x) => t + x.peso, 0) || 1;
  return partes.map(x => ({ ...x, frac: x.peso / total }));
}

function sector(cx, cy, r1, r2, a0, a1) {
  const p = (r, a) => `${(cx + r * Math.cos(a)).toFixed(2)} ${(cy + r * Math.sin(a)).toFixed(2)}`;
  const grande = a1 - a0 > Math.PI ? 1 : 0;
  return `M${p(r2, a0)}A${r2} ${r2} 0 ${grande} 1 ${p(r2, a1)}L${p(r1, a1)}A${r1} ${r1} 0 ${grande} 0 ${p(r1, a0)}Z`;
}

function platoSVG(r, tam, nombreTransicion) {
  let a = -Math.PI / 2;
  const sectores = composicion(r).map(x => {
    const a1 = a + x.frac * Math.PI * 2;
    const hueco = Math.min(0.04, (a1 - a) / 4);
    const d = sector(50, 50, 13, 35, a + hueco, a1 - hueco);
    a = a1;
    return `<path d="${d}" fill="var(--c-${x.g[0]})"/>`;
  }).join("");
  const estilo = nombreTransicion ? ` style="view-transition-name:${nombreTransicion}"` : "";
  return `<svg class="plato-svg" width="${tam}" height="${tam}" viewBox="0 0 100 100" aria-hidden="true"${estilo}>
    <circle class="borde-plato" cx="50" cy="50" r="47" stroke-width="1"/>
    <circle cx="50" cy="50" r="40" fill="none" stroke="var(--linea)" stroke-width="1"/>
    <g class="giro">${sectores}</g>
    <circle cx="50" cy="50" r="9" fill="var(--superficie)"/>
  </svg>`;
}

function leyendaPlato(r) {
  const vistos = new Map();
  composicion(r).forEach(x => vistos.set(x.g[1], (vistos.get(x.g[1]) || 0) + x.frac));
  return `<div class="leyenda">${[...vistos].sort((a, b) => b[1] - a[1]).map(([nombre, f]) => {
    const g = Object.values(GRUPOS).find(v => v[1] === nombre) || ["otro"];
    return `<span><i style="background:var(--c-${g[0]})"></i>${nombre} <span class="num">${Math.round(f * 100)} %</span></span>`;
  }).join("")}</div>`;
}

// Resalta los tiempos y temperaturas dentro del texto de un paso
const resaltarPaso = t => esc(t).replace(/(\d+(?:[-–]\d+)?\s*(?:segundos?|minutos?|min|horas?|°C|ºC|grados))/gi, "<mark>$1</mark>");

// ===== Interfaz =====
const app = document.getElementById("app");
function avisar(texto) {
  const a = document.getElementById("aviso");
  a.textContent = texto; a.classList.add("visible");
  clearTimeout(avisar.t); avisar.t = setTimeout(() => a.classList.remove("visible"), 2800);
}

// Cuenta hasta un número con una curva suave (para los importes)
function contar(el, hasta, formato) {
  const desde = 0, ms = 900, t0 = performance.now();
  if (matchMedia("(prefers-reduced-motion: reduce)").matches) { el.textContent = formato(hasta); return; }
  (function paso(ahora) {
    const k = Math.min(1, (ahora - t0) / ms), e = 1 - Math.pow(1 - k, 3);
    el.textContent = formato(desde + (hasta - desde) * e);
    if (k < 1) requestAnimationFrame(paso);
  })(t0);
}

function pintarMenu(ruta) {
  const enlaces = [["inicio", "Inicio"], ["plan", "Mi plan"], ["recetario", "Recetas"], ["lista", "Compra"]];
  document.getElementById("menu").innerHTML = enlaces
    .filter(([r]) => estado.plan || r === "inicio" || r === "recetario")
    .map(([r, t]) => `<a href="#${r}" ${ruta === r || (ruta === "receta" && r === "recetario") ? 'aria-current="page"' : ""}>${t}</a>`).join("");
}

// Coste del mes más barato en cada súper (1 persona, comida y cena), calculado una vez
let referencia = null;
function calcularReferencia() {
  if (referencia) return referencia;
  const base = { supers: SUPERS.map(s => s.id), restricciones: [], presupuesto: Infinity, personas: 1, comida: true, cena: true, basicos: false };
  referencia = SUPERS.map(s => ({ s: s.id, coste: construir(base, [s.id], 7, Infinity).coste })).sort((a, b) => a.coste - b.coste);
  return referencia;
}

function pantallaInicio() {
  const ref = calcularReferencia();
  const presu = estado.perfil ? estado.perfil.presupuesto : 120;
  return `<section>
    <div class="portada">
      <div>
        <p class="antetitulo">Planificador de comidas · ${SUPERS.length} supermercados</p>
        <h1 style="margin-top:16px">Come casero todo el mes por <span class="resalte">${Math.ceil(ref[0].coste)} €</span></h1>
        <p class="lema">Dinos dónde compras, qué no puedes comer y cuánto quieres gastar. Te damos el menú de 30 días, cada receta explicada paso a paso y la lista de la compra con precios reales.</p>
        <div class="fila">
          ${estado.plan ? `<a class="boton marca-b" href="#plan">Ver mi plan <span class="flecha">→</span></a><a class="boton fantasma" href="#paso1">Cambiar mis datos</a>`
                        : `<a class="boton marca-b" href="#paso1">Crear mi plan <span class="flecha">→</span></a><a class="boton fantasma" href="#recetario">Ver las recetas</a>`}
        </div>
        <div class="sellos">
          <div><strong>${RECETAS.length}</strong><span>recetas para principiantes</span></div>
          <div><strong>${window.DATOS.productos.reduce((n, p) => n + Object.keys(p.verificado).length, 0)}</strong><span>precios verificados</span></div>
          <div><strong>0 €</strong><span>sin registro ni anuncios</span></div>
        </div>
      </div>
      <div class="elevada simulador">
        <canvas class="sombreado" data-colores="claro"></canvas>
        <p class="antetitulo">Pruébalo · 1 persona, comida y cena</p>
        <label for="simPresu" class="fila entre" style="margin-top:14px;align-items:baseline">
          <span class="cifra num" id="simCifra">${presu} €</span><span class="suave">al mes</span>
        </label>
        <input type="range" id="simPresu" min="40" max="300" step="5" value="${presu}">
        <p id="simTexto" class="suave" style="min-height:3em"></p>
        <div class="semana-muestra" id="simSemana"></div>
        <div class="comparador">${ref.map(x => `<div class="linea"><span>${SUPER[x.s].nombre}</span>
          <span class="pista"><i style="width:${Math.round(x.coste * 100 / ref[ref.length - 1].coste)}%"></i></span><span class="num">${euros(x.coste)}</span></div>`).join("")}</div>
        <p class="suave" style="font-size:.8rem;margin-top:12px">Lo más barato que cuesta un mes completo en cada cadena, con precios de octubre de 2026.</p>
      </div>
    </div>
    ${cintaPrecios()}
    <div class="como">
      <div><span class="paso-n">01</span><h3>Elige tus supermercados</h3><p class="suave">Calculamos dónde te sale más barato el mes entero, no producto a producto.</p></div>
      <div><span class="paso-n">02</span><h3>Marca lo que no comes</h3><p class="suave">Alergias, dieta vegetariana o vegana y dietas por salud como la blanda o la baja en sal.</p></div>
      <div><span class="paso-n">03</span><h3>Pon tu presupuesto</h3><p class="suave">Si no llega para todo el mes, te decimos cuánto haría falta.</p></div>
    </div>
    ${franjaMes(ref[0])}
  </section>`;
}

function simular() {
  const entrada = document.getElementById("simPresu");
  if (!entrada) return;
  const presu = +entrada.value;
  entrada.style.setProperty("--lleno", ((presu - 40) * 100 / 260) + "%");
  document.getElementById("simCifra").textContent = presu + " €";
  const ref = calcularReferencia();
  const perfil = { supers: [ref[0].s], restricciones: [], presupuesto: presu, personas: 1, comida: true, cena: true, basicos: false };
  const plan = construir(perfil, [ref[0].s], 11, presu);
  const huecos = plan.dias.reduce((n, d) => n + Object.values(d).filter(Boolean).length, 0);
  document.getElementById("simTexto").innerHTML = huecos === 60
    ? `Te da para <strong>las 60 comidas</strong> del mes en ${SUPER[ref[0].s].nombre} y te sobran <strong class="num">${euros(presu - plan.coste)}</strong> para desayunos y fruta.`
    : `Llegas a <strong>${huecos} de 60</strong> comidas. Para el mes entero harían falta unos <strong class="num">${euros(ref[0].coste)}</strong>.`;
  document.getElementById("simSemana").innerHTML = plan.dias.slice(0, 7).map((d, i) => {
    const id = d.COMIDA || d.CENA;
    return `<div>${id ? platoSVG(RECETA_POR_ID.get(id), 38) : '<svg width="38" height="38" viewBox="0 0 100 100"><circle cx="50" cy="50" r="46" fill="none" stroke="var(--linea-fuerte)" stroke-dasharray="6 6"/></svg>'}${["L", "M", "X", "J", "V", "S", "D"][i]}</div>`;
  }).join("");
}

// Cinta con precios reales verificados que se desplaza sin parar (se para al pasar el ratón)
function cintaPrecios() {
  const items = [];
  for (const p of window.DATOS.productos) {
    for (const s of Object.keys(p.verificado)) {
      const unidad = p.unidad === "ud" ? "ud" : p.unidad === "ml" ? "l" : "kg";
      const porUnidad = p.unidad === "ud" ? p.precios[s] / p.envase : p.precios[s] * 1000 / p.envase;
      items.push(`<span class="item"><b>${esc(p.nombre)}</b><span class="num">${euros(porUnidad)}/${unidad}</span><span class="super" style="--c:${SUPER[s].color}">${SUPER[s].nombre}</span></span>`);
    }
  }
  const mitad = items.filter((_, i) => i % 3 === 0).slice(0, 30).join("");
  return `<div class="cinta" aria-label="Precios reales verificados en octubre de 2026"><div class="pista-cinta">${mitad}${mitad}</div></div>`;
}

// Franja oscura: un mes completo dibujado en platos, con las cifras grandes al lado
function franjaMes(mejor) {
  const perfil = { supers: [mejor.s], restricciones: [], presupuesto: Infinity, personas: 1, comida: true, cena: true, basicos: false };
  const plan = construir(perfil, [mejor.s], 11, Infinity);
  const distintas = new Set(plan.dias.flatMap(d => Object.values(d))).size;
  return `<section class="franja">
    <div class="franja-texto">
      <p class="antetitulo">Un mes de ejemplo en ${SUPER[mejor.s].nombre}</p>
      <h2>60 platos, ${distintas} recetas distintas y ni una repetida dos días seguidos</h2>
      <div class="franja-cifras">
        <div><strong class="num">${euros(plan.coste)}</strong><span>el mes entero</span></div>
        <div><strong class="num">${euros(plan.coste / 60)}</strong><span>por plato</span></div>
      </div>
      <a class="boton claro-b" href="#paso1">Hacer el mío <span class="flecha">→</span></a>
    </div>
    <div class="calendario">${["L", "M", "X", "J", "V", "S", "D"].map(d => `<span class="dia-sem">${d}</span>`).join("")}
      ${plan.dias.map((d, i) => `<div class="celda" style="--i:${i}" title="Día ${i + 1}: ${esc(RECETA_POR_ID.get(d.COMIDA).nombre)}">${platoSVG(RECETA_POR_ID.get(d.COMIDA), 40)}<span class="num">${i + 1}</span></div>`).join("")}
    </div>
  </section>`;
}

// Gasto de cada día del plan como mapa de calor; pulsar un día abre su semana
function calorMes(plan, perfil, semana) {
  const gastos = plan.dias.map(d => Object.values(d).filter(Boolean).reduce((t, id) => t + costeRacion(RECETA_POR_ID.get(id), perfil, plan.supers) * perfil.personas, 0));
  const max = Math.max(...gastos), min = Math.min(...gastos.filter(g => g > 0));
  return `<div class="superficie calor"><div class="fila entre"><h3>Gasto por día</h3>
    <span class="leyenda-calor"><span class="num">${euros(min)}</span><i></i><span class="num">${euros(max)}</span></span></div>
    <div class="celdas-calor">${gastos.map((g, i) => {
      const k = max > min ? (g - min) / (max - min) : 0;
      return `<button class="c-calor ${Math.floor(i / 7) === semana || (semana === 4 && i >= 28) ? "activa" : ""}" data-semana="${Math.min(4, Math.floor(i / 7))}" style="--k:${k.toFixed(2)};color:${k > .4 ? "var(--sobre-albahaca)" : "var(--tinta)"}" title="Día ${i + 1}: ${euros(g)}" aria-label="Día ${i + 1}, ${euros(g)}"><span>${i + 1}</span></button>`;
    }).join("")}</div></div>`;
}

const pasosBarra = n => `<div class="pasos-barra">${[1, 2, 3].map(i => `<i class="${i <= n ? "hecho" : ""}"></i>`).join("")}</div>`;
const CHECK = '<svg viewBox="0 0 12 12"><path d="M2 6.5l2.5 2.5L10 3"/></svg>';
const casilla = (nombre, valor, marcado, texto, delante = "", i = 0) =>
  `<label class="opcion" style="--i:${i}"><input type="checkbox" name="${nombre}" value="${valor}" ${marcado ? "checked" : ""}>${delante}<span>${texto}</span><span class="caja">${CHECK}</span></label>`;

function pantallaPaso1() {
  return `<section class="asistente">
    <p class="antetitulo">Paso 1 de 3</p>${pasosBarra(1)}
    <h2>¿Dónde puedes comprar?</h2><p class="suave" style="margin-top:8px">Marca todos los que te queden cerca. Elegimos el que salga más barato para tu plan.</p>
    <div class="opciones cascada">${SUPERS.map((s, i) => casilla("super", s.id, borrador.supers.includes(s.id), s.nombre,
      `<span class="logo" style="background:${s.color}">${s.nombre.slice(0, 2).toUpperCase()}</span>`, i)).join("")}</div>
    <div class="acciones"><a class="boton fantasma" href="#inicio">Volver</a><button class="boton marca-b" data-ir="paso2">Siguiente <span class="flecha">→</span></button></div>
  </section>`;
}

function pantallaPaso2() {
  let i = 0;
  const grupo = (g, titulo) => `<h3 class="grupo-titulo">${titulo}</h3><div class="opciones cascada">${RESTRICCIONES.filter(r => r.grupo === g)
    .map(r => casilla("restriccion", r.id, borrador.restricciones.includes(r.id), r.nombre, "", i++)).join("")}</div>`;
  return `<section class="asistente">
    <p class="antetitulo">Paso 2 de 3</p>${pasosBarra(2)}
    <h2>¿Hay algo que no puedas comer?</h2><p class="suave" style="margin-top:8px">Si no tienes ninguna restricción, deja todo sin marcar.</p>
    ${grupo("alergias", "Alergias e intolerancias")}${grupo("dieta", "Dieta")}${grupo("salud", "Dietas por salud")}
    <div class="acciones"><a class="boton fantasma" href="#paso1">Atrás</a><button class="boton marca-b" data-ir="paso3">Siguiente <span class="flecha">→</span></button></div>
  </section>`;
}

function pantallaPaso3() {
  return `<section class="asistente">
    <p class="antetitulo">Paso 3 de 3</p>${pasosBarra(3)}
    <h2>Tu presupuesto</h2><p class="suave" style="margin-top:8px">Solo lo que gastas en el supermercado para comer.</p>
    <label class="campo" for="presu"><span class="etq">Presupuesto al mes</span>
      <span class="importe"><output id="vPresu">${borrador.presupuesto}</output> € <small>al mes</small></span>
      <input type="range" id="presu" min="30" max="600" step="5" value="${borrador.presupuesto}" style="--lleno:${(borrador.presupuesto - 30) * 100 / 570}%">
    </label>
    <div class="campo"><span class="etq">Personas que coméis</span>
      <div class="contador"><button type="button" data-personas="-1" aria-label="Una persona menos">−</button><output id="vPersonas">${borrador.personas}</output><button type="button" data-personas="1" aria-label="Una persona más">+</button></div>
    </div>
    <div class="opciones">
      ${casilla("comida", 1, borrador.comida, "Planificar comidas")}
      ${casilla("cena", 1, borrador.cena, "Planificar cenas")}
      ${casilla("basicos", 1, borrador.basicos, "Ya tengo aceite, sal y pimentón")}
      ${casilla("unSolo", 1, borrador.unSolo, "Toda la compra en un solo súper")}
    </div>
    <div id="estimacion" class="estimacion" aria-live="polite"></div>
    <div class="acciones"><a class="boton fantasma" href="#paso2">Atrás</a><button class="boton marca-b" id="generar">Generar mi plan <span class="flecha">→</span></button></div>
  </section>`;
}

function actualizarEstimacion() {
  const caja = document.getElementById("estimacion");
  if (!caja) return;
  const min = minimoEstimado(borrador);
  caja.classList.toggle("mal", min === null || min > borrador.presupuesto);
  caja.textContent = min === null ? "No hay recetas para todo lo que has marcado. Prueba a elegir más supermercados o quitar alguna restricción."
    : min > borrador.presupuesto ? `El mínimo estimado es ${euros(min)}. Con menos solo podremos planificar parte del mes.`
    : `Tu presupuesto cubre el mínimo estimado, ${euros(min)}.`;
}

function pantallaPlan() {
  const plan = estado.plan, perfil = estado.perfil;
  if (!plan) { location.hash = "#inicio"; return ""; }
  const coste = costeCesta(cestaDe(plan.dias, perfil.personas), perfil, plan.supers);
  const distintas = new Set(plan.dias.flatMap(d => Object.values(d)).filter(Boolean)).size;
  const raciones = plan.dias.reduce((n, d) => n + Object.values(d).filter(Boolean).length, 0) * perfil.personas;
  const frac = Math.min(1, coste / perfil.presupuesto);
  const semana = pantallaPlan.semana || 0;
  const desde = semana * 7, hasta = semana === 4 ? 30 : desde + 7;
  const L = 2 * Math.PI * 52;
  return `<section>
    <div class="elevada resumen">
      <div class="medidor">
        <svg width="124" height="124" viewBox="0 0 124 124"><circle class="fondo-arco" cx="62" cy="62" r="52" fill="none" stroke-width="10"/>
        <circle class="arco-gasto" cx="62" cy="62" r="52" fill="none" stroke-width="10" stroke-dasharray="${L}" stroke-dashoffset="${L}" data-final="${L * (1 - frac)}"/></svg>
        <div class="centro"><strong data-contar="${coste}">0 €</strong><small>de ${euros(perfil.presupuesto)}</small></div>
      </div>
      <div>
        <p class="antetitulo">Plan de 30 días · ${personasTexto(perfil.personas)}</p>
        <h2 style="margin-top:8px">${plan.supers.length === 1 ? `Toda la compra en ${SUPER[plan.supers[0]].nombre}` : "Cada producto donde sale más barato"}</h2>
        <div class="cifras">
          <div><strong>${distintas}</strong><span>recetas distintas</span></div>
          <div><strong>${euros(raciones ? coste / raciones : 0)}</strong><span>por ración</span></div>
          <div><strong>${euros(Math.max(0, perfil.presupuesto - coste))}</strong><span>te sobran</span></div>
        </div>
      </div>
      <div class="acciones-plan fila">
        <a class="boton marca-b" href="#lista">Lista de la compra</a>
        <button class="boton fantasma" id="otroPlan">Otro plan</button>
      </div>
    </div>
    ${plan.necesario ? `<div class="alerta">Con ${euros(perfil.presupuesto)} no llega para todo el mes. Necesitarías unos ${euros(plan.necesario)}.</div>` : ""}
    ${calorMes(plan, perfil, semana)}
    <div class="pestanas" role="tablist">${[0, 1, 2, 3, 4].map(i => `<button role="tab" data-semana="${i}" aria-selected="${i === semana}">${i < 4 ? `Semana ${i + 1}` : "Días 29 y 30"}</button>`).join("")}</div>
    <div class="dias cascada">${plan.dias.slice(desde, hasta).map((dia, k) => `<article class="superficie dia" style="--i:${k}">
      <div class="dia-cab"><h3>Día ${desde + k + 1}</h3><span class="num">${euros(Object.values(dia).filter(Boolean).reduce((t, id) => t + costeRacion(RECETA_POR_ID.get(id), perfil, plan.supers) * perfil.personas, 0))}</span></div>
      ${Object.entries(dia).map(([m, id]) => platoDia(m, id, perfil, plan)).join("")}</article>`).join("")}</div>
  </section>`;
}

function platoDia(momento, id, perfil, plan) {
  const etiqueta = `<span class="dato ${momento === "COMIDA" ? "comida" : "cena"}">${momento === "COMIDA" ? "Comida" : "Cena"}</span>`;
  if (!id) return `<div class="hueco-vacio">${etiqueta} Sin presupuesto para este hueco</div>`;
  const r = RECETA_POR_ID.get(id);
  return `<button class="tarjeta-receta" data-receta="${id}">${platoSVG(r, 52)}
    <span class="texto"><span class="nombre">${esc(r.nombre)}</span><span class="meta">${etiqueta}<span>${r.minutos} min</span></span></span>
    <span class="precio">${euros(costeRacion(r, perfil, plan.supers))}</span></button>`;
}

function pantallaRecetario() {
  const filtro = pantallaRecetario.filtro || "TODAS";
  const lista = RECETAS.filter(r => filtro === "TODAS" || r.tipo === filtro || r.tipo === "AMBAS");
  return `<section>
    <p class="antetitulo">Recetario</p><h2 style="margin-top:8px">${RECETAS.length} recetas explicadas para tu primera vez</h2>
    <p class="suave" style="margin-top:8px;max-width:60ch">Cada plato se dibuja con sus ingredientes: el color indica de qué está hecho y el tamaño, cuánto lleva.</p>
    <div class="pestanas">${[["TODAS", "Todas"], ["COMIDA", "Comidas"], ["CENA", "Cenas"]].map(([f, t]) =>
      `<button data-filtro="${f}" aria-selected="${f === filtro}">${t}</button>`).join("")}</div>
    <div class="rejilla-recetas cascada">${lista.map((r, i) => {
      const n = nutricion(r);
      return `<button class="superficie ficha" data-receta="${r.id}" style="--i:${Math.min(i, 16)}">${platoSVG(r, 88)}
        <span class="nombre">${esc(r.nombre)}</span>
        <span class="meta"><span>${r.minutos} min</span><span class="num">${Math.round(n.kcal)} kcal</span></span></button>`;
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
  const macro = (nombre, g, ref, color) => `<div class="macro"><span>${nombre}</span><span class="pista"><i style="width:${Math.min(100, g * 100 / ref)}%;background:${color}"></i></span><span class="num">${Math.round(g)} g</span></div>`;
  return `<section>
    <div class="receta-cab">
      ${platoSVG(r, 260, "plato-activo")}
      <div>
        <p class="antetitulo">${r.tipo === "AMBAS" ? "Comida o cena" : r.tipo === "COMIDA" ? "Comida" : "Cena"} · ${r.minutos} min · ${r.pasos.length} pasos</p>
        <h1 style="font-size:clamp(2.1rem,4.6vw,3.6rem);margin-top:12px">${esc(r.nombre)}</h1>
        ${leyendaPlato(r)}
        <div class="fila" style="margin-top:26px">
          <button class="boton marca-b" data-cocinar="${r.id}">Empezar a cocinar <span class="flecha">→</span></button>
          <span class="suave"><span class="num" style="color:var(--tinta);font-weight:700">${euros(costeRacion(r, perfil, supers))}</span> por ración · ${personasTexto(personas)}</span>
        </div>
      </div>
    </div>
    <div class="columnas">
      <div class="pila">
        <div class="superficie bloque">
          <h3>Por ración</h3>
          <div class="kcal"><strong>${Math.round(n.kcal)}</strong><span class="suave">kcal · el ${Math.round(n.kcal / 20)} % de un día</span></div>
          <div class="macros">
            ${macro("Proteínas", n.prot, 50, "var(--c-carne)")}
            ${macro("Hidratos", n.hid, 130, "var(--c-cereal)")}
            ${macro("Grasas", n.gra, 35, "var(--c-huevo)")}
          </div>
          <p class="suave" style="margin-top:14px;font-size:.84rem">Barras sobre una referencia de comida principal. Valores aproximados, unos ${Math.round(n.peso)} g en crudo.</p>
        </div>
        <div class="superficie bloque">
          <h3>Ingredientes</h3>
          ${r.ingredientes.map(({ p, c }) => {
            const s = mejorSuper(p, supers);
            const donde = perfil.basicos && p.categoria === "Básicos" ? "Ya lo tienes en casa"
              : s ? `${SUPER[s].nombre} · ${euros(p.precios[s])} el envase` : "";
            return `<label class="ingrediente"><input type="checkbox"><span><span class="nombre">${esc(p.nombre)}</span><span class="donde">${donde}</span></span><span class="cantidad">${cantidad(c * personas, p.unidad)}</span></label>`;
          }).join("")}
        </div>
      </div>
      <div class="superficie bloque"><h3>Preparación</h3><ol class="pasos">${r.pasos.map(p => `<li><span>${resaltarPaso(p)}</span></li>`).join("")}</ol></div>
    </div>
  </section>`;
}

// Código de barras de adorno a partir de un número (como el del ticket)
function codigoBarras(semilla) {
  const rnd = azar(semilla || 7);
  let x = 0, barras = "";
  while (x < 200) { const w = 1 + Math.floor(rnd() * 3); if (rnd() > .35) barras += `<rect x="${x}" y="0" width="${w}" height="44"/>`; x += w + 1; }
  return `<svg class="codigo" viewBox="0 0 200 44" preserveAspectRatio="none" aria-hidden="true">${barras}</svg>`;
}

function pantallaLista() {
  const plan = estado.plan, perfil = estado.perfil;
  if (!plan) { location.hash = "#inicio"; return ""; }
  const lineas = listaCompra(plan, perfil);
  const total = lineas.reduce((t, l) => t + l.total, 0);
  const hechos = lineas.filter(l => estado.comprados.includes(l.p.id)).length;
  const comparacion = compararSupers(plan, perfil);
  const grupos = [...new Set(lineas.map(l => l.s))];
  const verificados = lineas.filter(l => l.p.verificado[l.s]).length;
  const hoy = new Date().toLocaleDateString("es-ES", { day: "2-digit", month: "2-digit", year: "numeric" });
  return `<section>
    <div class="fila entre" style="align-items:end">
      <div><p class="antetitulo">Lista de la compra</p><h2 style="margin-top:8px">${lineas.length} productos para 30 días</h2></div>
      <button class="boton fantasma" id="copiar">Copiar lista</button>
    </div>
    <p class="suave" style="margin-top:10px" id="contador">${hechos} de ${lineas.length} comprados</p>
    <div class="progreso-compra"><i id="barraCompra" style="width:${lineas.length ? hechos * 100 / lineas.length : 0}%"></i></div>
    <div class="compra" style="margin-top:24px">
      <div>${grupos.map(s => `<div class="superficie grupo-super">
        <div class="cab"><h3><span class="logo" style="width:26px;height:26px;border-radius:6px;display:grid;place-items:center;color:#fff;font:800 .72rem var(--f-titulo);background:${SUPER[s].color}">${SUPER[s].nombre.slice(0, 2).toUpperCase()}</span>${SUPER[s].nombre}</h3>
        <span class="precio">${euros(lineas.filter(l => l.s === s).reduce((t, l) => t + l.total, 0))}</span></div>
        ${lineas.filter(l => l.s === s).map(l => `<label class="producto"><input type="checkbox" data-comprado="${l.p.id}" ${estado.comprados.includes(l.p.id) ? "checked" : ""}>
          <span><span class="nombre">${esc(l.p.nombre)}</span><small>${l.envases} × ${cantidad(l.p.envase, l.p.unidad)} · necesitas ${cantidad(l.q, l.p.unidad)}${l.p.verificado[s] ? ' · <span class="verificado">precio verificado</span>' : ""}</small></span>
          <span class="precio">${euros(l.total)}</span></label>`).join("")}
      </div>`).join("")}</div>
      <aside class="ticket" aria-label="Resumen de la compra">
        <div class="ticket-papel"></div>
        <div class="centro"><div class="tienda">PickEats</div><div class="suave">Compra del mes · ${hoy}</div></div>
        <hr>
        ${grupos.map(s => `<div class="renglon"><span>${SUPER[s].nombre.toUpperCase()}</span><span class="relleno"></span><span>${euros(lineas.filter(l => l.s === s).reduce((t, l) => t + l.total, 0))}</span></div>`).join("")}
        ${perfil.basicos ? '<div class="renglon"><span>BÁSICOS EN CASA</span><span class="relleno"></span><span>0,00 €</span></div>' : ""}
        <hr>
        <div class="renglon total"><span>TOTAL</span><span class="relleno"></span><span>${euros(total)}</span></div>
        <div class="renglon"><span>PRESUPUESTO</span><span class="relleno"></span><span>${euros(perfil.presupuesto)}</span></div>
        <div class="renglon"><span>TE SOBRA</span><span class="relleno"></span><span>${euros(Math.max(0, perfil.presupuesto - total))}</span></div>
        ${comparacion.length > 1 ? `<div class="seccion">Mismo carro en otros súper</div>
          ${comparacion.map(x => `<div class="renglon"><span>${SUPER[x.s].nombre}</span><span class="relleno"></span><span>${x === comparacion[0] ? euros(x.coste) : "+" + euros(x.coste - comparacion[0].coste)}</span></div>`).join("")}` : ""}
        <hr>
        <div class="centro suave">${verificados} de ${lineas.length} precios verificados<br>en webs oficiales y RadarSuper</div>
        ${codigoBarras(plan.semilla)}
        <div class="centro suave" style="letter-spacing:.3em">${String(plan.semilla).padStart(8, "0")}</div>
      </aside>
    </div>
  </section>`;
}

// ===== Modo cocina =====
let cocina = null;
function abrirCocina(id) {
  cocina = { r: RECETA_POR_ID.get(+id), paso: 0, fin: false, restante: 0, total: 0, t: null };
  const capa = document.getElementById("cocina");
  capa.hidden = false;
  document.body.style.overflow = "hidden";
  pintarCocina();
  if (navigator.wakeLock) navigator.wakeLock.request("screen").then(w => { if (cocina) cocina.bloqueo = w; }).catch(() => {});
}
function cerrarCocina() {
  if (!cocina) return;
  clearInterval(cocina.t);
  if (cocina.bloqueo) cocina.bloqueo.release().catch(() => {});
  if (cocina.pararFondo) cocina.pararFondo();
  cocina = null;
  document.getElementById("cocina").hidden = true;
  document.body.style.overflow = "";
}
function pintarCocina() {
  const c = cocina, capa = document.getElementById("cocina");
  const n = c.r.pasos.length;
  clearInterval(c.t); c.t = null;
  if (!c.fin) { c.total = segundosDe(c.r.pasos[c.paso]); c.restante = c.total; }
  const L = 2 * Math.PI * 40;
  if (!capa.querySelector("canvas")) {
    capa.innerHTML = '<canvas class="sombreado"></canvas><div class="contenido" style="display:flex;flex-direction:column;flex:1;min-height:0"></div>';
    c.pararFondo = window.iniciarFondo ? window.iniciarFondo(capa.querySelector("canvas"), ["#0E1A14", "#15382A", "#3A2A10"], .02) : null;
  } else if (!c.pararFondo && window.iniciarFondo) {
    c.pararFondo = window.iniciarFondo(capa.querySelector("canvas"), ["#0E1A14", "#15382A", "#3A2A10"], .02);
  }
  capa.querySelector(".contenido").innerHTML = `<div class="arriba"><div><p class="antetitulo">${c.fin ? "Listo" : `Paso ${c.paso + 1} de ${n}`}</p>
      <h3 style="margin-top:4px">${esc(c.r.nombre)}</h3></div><button class="cerrar" data-cocina="cerrar" aria-label="Cerrar modo cocina">✕</button></div>
    <div class="segmentos">${c.r.pasos.map((_, i) => `<i class="${c.fin || i <= c.paso ? "hecho" : ""}"></i>`).join("")}</div>
    <div class="paso"><span class="n">${c.fin ? "✓" : String(c.paso + 1).padStart(2, "0")}</span>
      <p>${c.fin ? "Plato terminado. Sírvelo recién hecho y que aproveche." : esc(c.r.pasos[c.paso])}</p>
      ${!c.fin && c.total ? `<div class="reloj" id="reloj">
        <svg viewBox="0 0 92 92"><circle cx="46" cy="46" r="40" fill="none" stroke="rgba(255,255,255,.12)" stroke-width="6"/>
        <circle id="arco" cx="46" cy="46" r="40" fill="none" stroke="#F2B55A" stroke-width="6" stroke-linecap="round" stroke-dasharray="${L}" stroke-dashoffset="0"/></svg>
        <div><div class="tiempo num" id="tiempo">${reloj(c.total)}</div><p style="opacity:.75;font-size:.95rem">Pon el temporizador y te avisamos con un sonido.</p>
        <button class="boton claro" style="margin-top:10px;min-height:42px" data-cocina="reloj">Iniciar ${reloj(c.total)}</button></div></div>` : ""}
    </div>
    <div class="abajo"><button class="boton contorno" data-cocina="atras" ${c.paso === 0 && !c.fin ? "disabled" : ""}>Anterior</button>
      <button class="boton claro" data-cocina="${c.fin ? "cerrar" : "siguiente"}">${c.fin ? "Volver a la receta" : c.paso === n - 1 ? "Terminar" : "Siguiente"}</button></div>`;
  if (c.fin) confeti();
}
function alternarReloj(boton) {
  const c = cocina, L = 2 * Math.PI * 40;
  if (c.t) { clearInterval(c.t); c.t = null; boton.textContent = "Seguir"; return; }
  if (c.restante <= 0) c.restante = c.total;
  boton.textContent = "Pausar";
  let fin = Date.now() + c.restante * 1000;
  c.t = setInterval(() => {
    c.restante = Math.max(0, Math.ceil((fin - Date.now()) / 1000));
    document.getElementById("tiempo").textContent = reloj(c.restante);
    document.getElementById("arco").setAttribute("stroke-dashoffset", String(L * (1 - (fin - Date.now()) / (c.total * 1000))));
    if (c.restante <= 0) { clearInterval(c.t); c.t = null; boton.textContent = `Iniciar ${reloj(c.total)}`; finTiempo(); }
  }, 100);
}
function finTiempo() {
  document.getElementById("reloj").classList.add("sonando");
  if (navigator.vibrate) navigator.vibrate([400, 200, 400, 200, 600]);
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    [0, .35, .7].forEach(t => {
      const o = ctx.createOscillator(), g = ctx.createGain();
      o.type = "sine"; o.frequency.value = 880; o.connect(g); g.connect(ctx.destination);
      g.gain.setValueAtTime(.0001, ctx.currentTime + t); g.gain.exponentialRampToValueAtTime(.25, ctx.currentTime + t + .02);
      g.gain.exponentialRampToValueAtTime(.0001, ctx.currentTime + t + .3);
      o.start(ctx.currentTime + t); o.stop(ctx.currentTime + t + .32);
    });
  } catch (e) { /* sin sonido */ }
  avisar("¡Tiempo! Mira cómo va antes de seguir.");
}

// Confeti con física simple en un canvas (papelitos que giran y caen)
function confeti() {
  if (matchMedia("(prefers-reduced-motion: reduce)").matches) return;
  const cv = document.createElement("canvas");
  cv.id = "confeti"; document.body.appendChild(cv);
  const ctx = cv.getContext("2d"), dpr = devicePixelRatio || 1;
  cv.width = innerWidth * dpr; cv.height = innerHeight * dpr; ctx.scale(dpr, dpr);
  const colores = ["#1D5B44", "#E9A23B", "#C8462E", "#5E9654", "#6F98AE", "#F0C64B"];
  const trozos = Array.from({ length: 140 }, () => ({
    x: innerWidth / 2 + (Math.random() - .5) * 200, y: innerHeight * .35,
    vx: (Math.random() - .5) * 14, vy: -Math.random() * 14 - 4, g: .32 + Math.random() * .1,
    w: 6 + Math.random() * 6, h: 8 + Math.random() * 8, a: Math.random() * 6, va: (Math.random() - .5) * .3,
    c: colores[Math.floor(Math.random() * colores.length)],
  }));
  const t0 = performance.now();
  (function cuadro(ahora) {
    ctx.clearRect(0, 0, innerWidth, innerHeight);
    for (const t of trozos) {
      t.vx *= .985; t.vy = t.vy * .985 + t.g; t.x += t.vx; t.y += t.vy; t.a += t.va;
      ctx.save(); ctx.translate(t.x, t.y); ctx.rotate(t.a); ctx.scale(1, Math.cos(t.a * 2));
      ctx.fillStyle = t.c; ctx.fillRect(-t.w / 2, -t.h / 2, t.w, t.h); ctx.restore();
    }
    if (ahora - t0 < 3200) requestAnimationFrame(cuadro); else cv.remove();
  })(t0);
}

// ===== Rutas y eventos =====
let pararFondoPortada = null;
function pintar() {
  const [ruta, arg] = (location.hash.slice(1) || "inicio").split("/");
  const pantallas = { inicio: pantallaInicio, paso1: pantallaPaso1, paso2: pantallaPaso2, paso3: pantallaPaso3,
    plan: pantallaPlan, recetario: pantallaRecetario, receta: () => pantallaReceta(arg), lista: pantallaLista };
  if (pararFondoPortada) { pararFondoPortada(); pararFondoPortada = null; }
  app.innerHTML = (pantallas[ruta] || pantallaInicio)();
  pintarMenu(ruta);
  if (ruta === "paso3") actualizarEstimacion();
  if (ruta === "inicio" || !pantallas[ruta]) {
    simular();
    const cv = app.querySelector("canvas.sombreado");
    const oscuro = getComputedStyle(document.documentElement).getPropertyValue("--superficie").trim().toLowerCase() !== "#ffffff";
    if (cv && window.iniciarFondo) pararFondoPortada = window.iniciarFondo(cv, oscuro ? ["#1A201C", "#173126", "#2E2410"] : ["#FFFFFF", "#EAF2EC", "#FBEFD9"], .015);
  }
  app.querySelectorAll("[data-contar]").forEach(el => contar(el, +el.dataset.contar, euros));
  requestAnimationFrame(() => app.querySelectorAll("[data-final]").forEach(a => a.setAttribute("stroke-dashoffset", a.dataset.final)));
}

// Cambio de pantalla con View Transitions cuando el navegador las tiene
function conTransicion(fn) {
  if (document.startViewTransition && !matchMedia("(prefers-reduced-motion: reduce)").matches) {
    document.startViewTransition(() => { fn(); window.scrollTo(0, 0); });
  } else { fn(); window.scrollTo(0, 0); }
}
window.addEventListener("hashchange", () => { cerrarCocina(); conTransicion(pintar); });

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
    document.getElementById("contador").textContent = `${hechos} de ${total} comprados`;
    document.getElementById("barraCompra").style.width = hechos * 100 / total + "%";
    if (e.target.checked && hechos >= total) { confeti(); avisar("Compra completada. Ya tienes todo lo del mes."); }
  }
  actualizarEstimacion();
});

app.addEventListener("input", e => {
  if (e.target.id === "presu") {
    borrador.presupuesto = +e.target.value;
    e.target.style.setProperty("--lleno", (borrador.presupuesto - 30) * 100 / 570 + "%");
    document.getElementById("vPresu").textContent = borrador.presupuesto;
    clearTimeout(app.t); app.t = setTimeout(actualizarEstimacion, 120);
  }
  if (e.target.id === "simPresu") { cancelAnimationFrame(app.sim); app.sim = requestAnimationFrame(simular); }
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
    guardar(); conTransicion(pintar); avisar("Nuevo plan generado.");
  }
  if (b.dataset.semana) { pantallaPlan.semana = +b.dataset.semana; conTransicion(pintar); }
  if (b.dataset.filtro) { pantallaRecetario.filtro = b.dataset.filtro; conTransicion(pintar); }
  if (b.dataset.receta) {
    // El plato pulsado "vuela" hasta la cabecera de la receta
    const svg = b.querySelector(".plato-svg");
    if (svg) svg.style.viewTransitionName = "plato-activo";
    location.hash = "#receta/" + b.dataset.receta;
  }
  if (b.dataset.cocinar) abrirCocina(b.dataset.cocinar);
  if (b.id === "copiar") {
    const lineas = listaCompra(estado.plan, estado.perfil);
    const texto = "Lista de la compra (PickEats)\n" + lineas.map(l => `- ${l.p.nombre}: ${l.envases} × ${cantidad(l.p.envase, l.p.unidad)} en ${SUPER[l.s].nombre} (${euros(l.total)})`).join("\n");
    navigator.clipboard.writeText(texto).then(() => avisar("Lista copiada."), () => avisar("No se pudo copiar. Selecciona el texto a mano."));
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
