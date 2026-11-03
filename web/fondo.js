// Fondo animado con WebGL: un shader GLSL dibuja "vapor" suave con ruido fractal
// en los colores de la marca. Si no hay WebGL, el canvas queda transparente y se ve el fondo CSS.
"use strict";

const VERTICES = `
attribute vec2 p;
void main() { gl_Position = vec4(p, 0.0, 1.0); }`;

const FRAGMENTOS = `
precision mediump float;
uniform vec2 res;
uniform float t;
uniform vec3 c1;
uniform vec3 c2;
uniform vec3 c3;
uniform float grano;

float azar(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
float ruido(vec2 p) {
  vec2 i = floor(p), f = fract(p);
  vec2 u = f * f * (3.0 - 2.0 * f);
  return mix(mix(azar(i), azar(i + vec2(1, 0)), u.x), mix(azar(i + vec2(0, 1)), azar(i + vec2(1, 1)), u.x), u.y);
}
float fbm(vec2 p) {
  float v = 0.0, a = 0.5;
  for (int k = 0; k < 5; k++) { v += a * ruido(p); p = p * 2.03 + vec2(1.7, 9.2); a *= 0.5; }
  return v;
}
void main() {
  vec2 uv = gl_FragCoord.xy / res;
  vec2 q = uv * vec2(res.x / res.y, 1.0) * 1.6;
  // Deformación del dominio: el vapor sube despacio y se retuerce
  vec2 w = vec2(fbm(q + vec2(0.0, -t * 0.05)), fbm(q + vec2(5.2, 1.3) - t * 0.04));
  float n = fbm(q + 2.2 * w);
  vec3 col = mix(c1, c2, smoothstep(0.25, 0.85, n));
  col = mix(col, c3, smoothstep(0.55, 1.0, n * w.y) * 0.9);
  col += (azar(gl_FragCoord.xy + t) - 0.5) * grano; // grano de papel
  gl_FragColor = vec4(col, 1.0);
}`;

function hexARgb(hex) {
  const h = hex.trim().replace("#", "");
  return [0, 2, 4].map(i => parseInt(h.slice(i, i + 2), 16) / 255);
}

function compilar(gl, tipo, codigo) {
  const s = gl.createShader(tipo);
  gl.shaderSource(s, codigo);
  gl.compileShader(s);
  return gl.getShaderParameter(s, gl.COMPILE_STATUS) ? s : null;
}

// Arranca el shader en un canvas. colores: tres hex "#rrggbb". Devuelve una función para pararlo.
window.iniciarFondo = function (canvas, colores, grano) {
  const gl = canvas.getContext("webgl", { premultipliedAlpha: false, antialias: false });
  if (!gl) return () => {};
  const vs = compilar(gl, gl.VERTEX_SHADER, VERTICES), fs = compilar(gl, gl.FRAGMENT_SHADER, FRAGMENTOS);
  if (!vs || !fs) return () => {};
  const prog = gl.createProgram();
  gl.attachShader(prog, vs); gl.attachShader(prog, fs); gl.linkProgram(prog); gl.useProgram(prog);
  gl.bindBuffer(gl.ARRAY_BUFFER, gl.createBuffer());
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 3, -1, -1, 3]), gl.STATIC_DRAW);
  const p = gl.getAttribLocation(prog, "p");
  gl.enableVertexAttribArray(p); gl.vertexAttribPointer(p, 2, gl.FLOAT, false, 0, 0);
  const u = n => gl.getUniformLocation(prog, n);
  colores.forEach((c, i) => gl.uniform3fv(u("c" + (i + 1)), hexARgb(c)));
  gl.uniform1f(u("grano"), grano || 0.025);

  const quieto = matchMedia("(prefers-reduced-motion: reduce)").matches;
  let vivo = true, inicio = performance.now();
  function cuadro(ahora) {
    if (!vivo || !canvas.isConnected) return;
    const escala = Math.min(devicePixelRatio, 1.5) * 0.5; // media resolución: es un fondo borroso
    const w = Math.round(canvas.clientWidth * escala), h = Math.round(canvas.clientHeight * escala);
    if (canvas.width !== w || canvas.height !== h) { canvas.width = w; canvas.height = h; gl.viewport(0, 0, w, h); }
    gl.uniform2f(u("res"), w, h);
    gl.uniform1f(u("t"), (ahora - inicio) / 1000 + 20);
    gl.drawArrays(gl.TRIANGLES, 0, 3);
    if (!quieto) requestAnimationFrame(cuadro);
  }
  requestAnimationFrame(cuadro);
  return () => { vivo = false; };
};
