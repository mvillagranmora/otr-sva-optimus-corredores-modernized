# Estado de la modernizacion y pendientes (traspaso de sesion, 2026-10-22)

Documento para retomar en una sesion nueva. Complementa `AVANCE_MIGRACION.md` (cifras, se regenera con `tools/avance_migracion.py`) y la memoria del proyecto. Repo de codigo: `modernized/otr-sva-optimus-corredores`, rama `main`, limpio, ultimo commit `f43d20c9`. `analysis/` no es repo git.

## 1. Donde estamos
- **Verificacion firmada (21-10):** 10 modulos PROVEN (BcsUtils, Contabilidad Servicios/Jobs, CuentaInversion Servicios/Jobs, Tesoreria Servicios/Jobs, APV Servicios/Jobs, BcsCrystalReports). **Desactualizada** para BcsAPVServicios y los 4 Jobs (cambiaron despues). Re-verificar y volver a firmar se hace en bloque al llegar al 80 %.
- **Avance medido (AVANCE_MIGRACION.md):** 12,5 % del codigo migrado y probado (95.650 de 765.652 lineas Java legacy, 9 de 42 modulos), 14,6 % construido; 144.411 lineas de codigo nuevo y 104.297 de pruebas. Meta 80 %: faltan ~516.900 lineas.
- **Construido despues de la firma:**
  - Jobs (4 modulos): pasos 5, 6 y 6b (colaboradores migrados en proceso, guarda TomEE fail-closed, escaneo de paralelismo). Inertes hasta el corte global.
  - `BcsPlataformaWeb`: router `POST /internal/java-call` (commit b9cb0b0a).
  - `BcsPlataformaServicios`: solo `ConsultarEstadoModulo` (600017) migrada (71fb8b0d, 113 pruebas); 326 stubs fuera del contexto.
  - `BcsFondoMutuoServicios` **F1 completa** (pasos 1-7, 742 pruebas, commits f15c4acc..f43d20c9): `CrearSolicitudAporteRescateNormal` (357) y `ManejadorSolicitudAporteRescate` (601190, compuerta G0) + cierre puro de 10 clases. Falta pack de verificacion y firma.
- **Documentos clave:** `PRIORIDAD_POR_USO.md` (orden aprobado), `PLAN_BLOQUE1_PLATAFORMA_OLA_A.md`, `PLAN_FONDOMUTUO_SOLICITUD_APORTE_RESCATE.md`, `PLAN_FONDOMUTUO_F2.md`, `RULES_FONDOMUTUO_SOLICITUD.md`, `RULES_FONDOMUTUO_F2_REINVERSION.md`, `RULES_PLATAFORMA_ESM.md`, `SECURITY_REVIEW_FONDOMUTUO_F1.md`, `HANA_DDL_COMMIT_HALLAZGO.md`, oraculos en `equivalence/BcsPlataformaServicios/` y `equivalence/BcsFondoMutuoServicios/`.

## 2. Orden de ejecucion vigente (override del brief, aprobado por el usuario)
1. `ConsultarEstadoModulo` (HECHO)
2. FondoMutuo `ManejadorSolicitudAporteRescate` + `CrearSolicitudAporteRescateNormal` = F1 (HECHO)
3. **FondoMutuo F2 (SIGUIENTE):** W1 = libreria de Tesoreria en proceso + `ProcesarIngresoEgresoTesoreria` (levanta G1) y `RescateReinversion`; luego W2 (Spot con G2 cerrada, etc.). Plan y decisiones en `PLAN_FONDOMUTUO_F2.md` (secciones 12-16). `AporteReinversion` pospuesta (depende de `Certificar...`, F3). Programado/Overnight/CambioSerie/Transferencia fuera de alcance (0 filas). Custodia espera su libreria.
4. `ConsultarCustodiaPortafolio` (Custodia)
5. `CrearOrdenes` (Ordenes; RULE-068 inyeccion SQL se corrige aqui; lizard >=80 % ramas como entrada de la fase 3)
6. Despues: volver al plan original del brief (CalcularParidad, ConsultarFlujoCaja, ConsultarMatrizOperacional diferidas; Portafolio, Mesa, etc. segun `PRIORIDAD_POR_USO.md`).
- Regla del usuario: no correr el core modernizado en paralelo al legado todavia; pruebas de conjunto, runbook de corte, re-verificacion y firmas al llegar al 80 %.

## 3. Decisiones del usuario que rigen (ademas de la memoria)
- Replicar el legacy siempre (bug-for-bug, defectos listados para el experto). Sin cambios de BD de ningun tipo; HANA solo SELECT de solo lectura (excepcion autorizada: DDL de sesion del bloqueo, y una prueba con temporales de sesion). Commits sin `Co-Authored-By`.
- DDL de sesion `#p_Bloqueo...` aceptado. **Medido en HANA QA: CREATE LOCAL TEMPORARY TABLE y DROP TABLE confirman implicitamente**; opcion A elegida (replicar legacy, misma conexion).
- DEV-F1..F7 aprobadas. Zona horaria: `JAVA_TOOL_OPTIONS=-Duser.timezone=UTC` por compose (sobrescribible por pais). SEC-001: servicios internos con `expose` (solo gateway publicado), acceso por nombre de servicio. SEC-002: identidad como viene del legacy. SEC-010: se deja.
- `optimus-servicios` (`modernized/otr-sva-optimus-servicios`) es un consumidor (API LoopBack): llama `/BSCL/Plataforma/GWJAVA/PLA/CrearSesion` y `/call?ch=WS&t={servicio}` con `X-SESSION-TOKEN`, y `ProcesarIdTransaccion`/`AuditoriaWS`. No es parte de la migracion.

## 4. Pendientes y preguntas abiertas
**Decisiones del usuario sin responder:**
- Verificar FondoMutuo ahora como "tajada F1" (con la limitacion en la firma) o esperar a migrar todo el modulo (recomendado: esperar; el avance solo cuenta lo migrado).
- Confirmar DEV-6-H1 (golden `FMM-NOR-37`: el legacy deja viva la tabla temporal local; el comparador ignora `tablasLocalesSinDrop` en ese caso) y si G2 se ata a la necesidad real de Spot (no a las filas de `p_FormaPagoSpot`).
- Autorizar borrar los tres delegadores de FondoMutuo y el arbol viejo de stubs; unificar la zona horaria en los servicios no probados; prueba de tipos y escalas contra HANA real (pendiente de autorizacion).
- D-FEC-11 (bucle infinito inalcanzable en `ObtenerDiaHabilMultiplesPaises`): se dejo fiel.
**Datos de terceros / dominio:** experto de dominio por modulo y quien asigna los RULE-nnn (Q-01); `p_FormaPagoSpot` siempre en la UI y ruta UI del 601190 (Q-03); QD-02/03/04/05/12/13 y las preguntas FM-REI/QD de FondoMutuo; concepto Tesoreria `APORFM` vs `APOREI` (replicado APORFM); formato de fechas en respuestas HTTP de Plataforma/Custodia; forma del OWP `{"p_Solicitudes":[...]}` (supuesta); BECB/SURA sin credenciales sandbox (omitidas por ahora).
**Seguridad / corte global (requisitos):** idempotency key para operaciones de dinero (SEC-005), token por servicio, digest de imagen base, `read_only` en contenedores, `ch=WS` y rutas `CrearSesion`/`ProcesarIdTransaccion`/`AuditoriaWS` en Plataforma, brecha R-05 (el despachador de `BcsPlataformaWeb` salta la validacion de sesion si `RegistraUso='N'`; el legacy la exige) antes de habilitar `/call`, residual: la simulacion del Manejador escribe NN y el DDL del bloqueo la confirma antes de G1/G2/G4.
**Modulos ya PROVEN con el mismo DDL de tablas temporales:** su equivalencia no cambia, pero el rollback tras contador>0 probado en H2 no vale tal cual en HANA (probar en staging).
**Sin ejercitar:** HANA real (todo H2); tráfico real; APV sin firma del experto regulatorio; BcsCrystalReports: ~29 llamadores en proceso por redirigir, `/reports/generate` publico, 6 archivos no Crystal sin decidir.

## 5. Como retomar
1. `/code-modernization:modernize-status otr-sva-optimus-corredores` (el usuario lo ejecutara; este documento y `AVANCE_MIGRACION.md` explican lo que verá desactualizado: VERIFICATION.md es del 21-10).
2. Regenerar avance: `py -3 -I analysis/otr-sva-optimus-corredores/tools/avance_migracion.py` desde la raiz del workspace (actualizar antes `avance_estado.json`: estado + fila de historial por cada modulo que cambie).
3. Siguiente trabajo: lanzar F2 W1 de FondoMutuo (agentes de a lo mas 2 en paralelo; ver `PLAN_FONDOMUTUO_F2.md` seccion de olas). Requiere VPN para HANA QA solo si hace falta una consulta de catalogo (programas de solo lectura en `analysis/.../tools/`; credenciales de `.env` local, nunca imprimir).
4. Higiene de agentes: los agentes suelen usar heredocs y `Write` de >150 lineas contra las reglas, y se atascan con pasos largos sin salida: dar briefs con comandos largos en segundo plano, `grep -a` sobre logs, nunca `clean` concurrente en el mismo modulo. Siempre verificar la suite completa desde limpio antes de commitear.
5. Maven: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"`; `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :<artifactId> clean test -Dfile.encoding=UTF-8` (nunca `-am`, salvo BcsContabilidadJobs). artifactIds: `bcs-fondo-mutuo-servicios`, `plataforma-service`, `tesoreria-jobs`, `apv-jobs`, `bcs-cuenta-inversion-jobs`, `bcs-plataforma-web`.
