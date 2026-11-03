"""Convierte los CSV de app/src/main/assets en web/datos.js para la versión web.

Uso: python3 tools/generar_datos_web.py
Aplica los precios reales verificados igual que la app Android (CargadorDatos).
"""
import json
import os

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(RAIZ, "app", "src", "main", "assets")
SUPERS = ["MERCADONA", "DIA", "LIDL", "ALDI", "ALCAMPO", "CARREFOUR"]


def filas(nombre):
    with open(os.path.join(ASSETS, nombre), encoding="utf-8") as f:
        lineas = [l.rstrip("\n") for l in f if l.strip() and not l.startswith("#")]
    return [l.split(";") for l in lineas[1:]]


def num(texto):
    return float(texto) if texto.strip() else None


productos = {}
for c in filas("productos.csv"):
    productos[c[0]] = {
        "id": int(c[0]), "nombre": c[1], "categoria": c[2], "unidad": c[3], "envase": float(c[4]),
        "precios": {s: num(c[5 + i]) for i, s in enumerate(SUPERS)},
        "verificado": {},
        "alergenos": [a for a in c[11].split("|") if a],
        "origen": c[12],
        "nutrientes": [num(x) or 0 for x in c[13:17]],
        "etiquetas": [e for e in (c[17] if len(c) > 17 else "").split("|") if e],
    }

# Precios reales: euros por kg, litro o unidad -> precio del envase
for c in filas("precios_reales.csv"):
    p = productos.get(c[0])
    if p is None or c[1] not in SUPERS:
        continue
    cantidad = p["envase"] if p["unidad"] == "ud" else p["envase"] / 1000
    p["precios"][c[1]] = round(float(c[2]) * cantidad, 2)
    p["verificado"][c[1]] = True

recetas = {}
for c in filas("recetas.csv"):
    recetas[c[0]] = {"id": int(c[0]), "nombre": c[1], "tipo": c[2], "minutos": int(c[3]),
                     "pasos": c[4].split("|"), "ingredientes": []}
for c in filas("ingredientes.csv"):
    if c[0] in recetas and c[1] in productos:
        recetas[c[0]]["ingredientes"].append([int(c[1]), float(c[2])])

datos = {"productos": list(productos.values()), "recetas": list(recetas.values())}
with open(os.path.join(RAIZ, "web", "datos.js"), "w", encoding="utf-8") as f:
    f.write("// Generado con tools/generar_datos_web.py a partir de los CSV de la app. No editar a mano.\n")
    f.write("window.DATOS = " + json.dumps(datos, ensure_ascii=False, separators=(",", ":")) + ";\n")
print(len(productos), "productos,", len(recetas), "recetas")
