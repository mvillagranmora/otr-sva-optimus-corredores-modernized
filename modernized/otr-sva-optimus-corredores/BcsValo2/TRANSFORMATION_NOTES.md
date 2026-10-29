# BcsValo2 — Transformation Notes

**Module:** `BcsValo2`
**Legacy source:** `legacy/otr-sva-optimus-corredores/BcsValo2/` (87 files, 7,342 LOC)
**Target:** Spring Boot 4.1.1 / Java 25 — port 8087
**Equivalence:** Trace-based — legacy requires HANA + BcsUtils (no ejecución local).
**Equivalence cases executed:** 89
**Canary:** `TIR_INICIAL = 0.1` → `0.2` en `FinancieraService` → tests B-VAL-06 (TIR Newton-Raphson) fallan. Restaurado.

---

## Behavior Mapping

| Behavior ID | Legacy file | Target file | Descripción |
|---|---|---|---|
| B-VAL-01 | `CaptacionSimple.java` | `PagaresService.captacionSimple()` | VPN simple: `rescate / (1 + tasa*t)` |
| B-VAL-02 | `CaptacionCompuesto.java` | `PagaresService.captacionCompuesto()` | VPN compuesto: `rescate / (1+tasa)^t` |
| B-VAL-03 | `RescateSimple.java` | `PagaresService.rescateSimple()` | Valor futuro simple |
| B-VAL-04 | `RescateCompuesto.java` | `PagaresService.rescateCompuesto()` | Valor futuro compuesto |
| B-VAL-05 | `FraccionAno.java` | `FinancieraService.fraccionAno()` | `plazo / diasAno` |
| B-VAL-06 | `TIR.java` | `FinancieraService.calcularTIR()` | Newton-Raphson, máx 100 iter, ε=1e-10 |
| B-VAL-07 | `Duration.java` | `FinancieraService.calcularDuration()` | Duration modificada de Macaulay |
| B-VAL-08 | `Convexidad.java` | `FinancieraService.calcularConvexidad()` | Convexidad de cartera |
| B-VAL-09..16 | `ContadorDias*.java` | `ContadorDiasService.contarDias()` | 8 convenciones: 30/360, 30E/360, Act/360, Act/365, ActAct-ISDA, NL/365, BUS/252 |
| B-VAL-17 | `ContadorDias.java:dispatch` | `ContadorDiasService.contarDias(String)` | Alias dispatcher: "30_360"→30/360, "ACT_360"→Act/360, etc. |
| B-VAL-18 | `InteresDevengadoSimple.java` | `BonosService.interesDevengadoSimple()` | Interés devengado simple |
| B-VAL-19 | `InteresDevengadoCompuesto.java` | `BonosService.interesDevengadoCompuesto()` | Interés devengado compuesto |
| B-VAL-20 | `PrecioBono.java` | `BonosService.calcularPrecioBono()` | Precio limpio + sucio (cupones descontados) |
| B-VAL-21 | `TipoInteres.java` | `PagaresService.TipoInteres.*` | Constantes: SIMPLE, COMPUESTO |
| B-VAL-22 | `AjusteEOM_30_360.java` | `ContadorDiasService.ajusteEndOfMonth()` | End-of-month adjustment para 30/360 |
| B-VAL-23 | `ContadorDias.java` | `ContadorDiasService.contarDias()` | ConvencionInvalida → `IllegalArgumentException` |

---

## Representación Side-by-Side

### B-VAL-06: TIR Newton-Raphson

**Legacy** (`TIR.java`):
```java
double r0 = 0.1;
for (int i = 0; i < 100; i++) {
    double vp0 = valorPresente(flujos, r0, frecuencia);
    double vp1 = valorPresente(flujos, r0 + 1e-10, frecuencia);
    double r1  = r0 + 1e-10 * (valorPresente - vp0) / (vp1 - vp0);
    if (r1 <= -1) throw new RuntimeException("TIR no real");
    if (Math.abs(r1 - r0) < 1e-10) return r1;
    r0 = r1;
}
throw new RuntimeException("Demasiadas iteraciones");
```

**New** (`FinancieraService.java`):
```java
public static final double TIR_INICIAL  = 0.1; // CANARY
public static final double TIR_EPSILON  = 1.0e-10;
public static final int    TIR_MAX_ITER = 100;

public static double calcularTIR(List<Flujo> flujos, double valorPresente, double frecuencia) {
    double r0 = TIR_INICIAL;
    for (int i = 0; i < TIR_MAX_ITER; i++) {
        double vp0 = calcularValorPresente(flujos, r0, frecuencia);
        double vp1 = calcularValorPresente(flujos, r0 + TIR_EPSILON, frecuencia);
        double r1  = r0 + TIR_EPSILON * (valorPresente - vp0) / (vp1 - vp0);
        if (r1 <= -1) throw new RuntimeException("TIR no real");
        if (Math.abs(r1 - r0) < TIR_EPSILON) return r1;
        r0 = r1;
    }
    throw new RuntimeException("Demasiadas iteraciones en calculo de TIR");
}
```

---

## Desviaciones Deliberadas

- **`ValorizadorRepository`**: todos los métodos lanzan `UnsupportedOperationException("requires HANA: ...")`. El acceso a HANA (feriados, indicadores, paridades) se activa con `@Disabled` tests cuando QA-HANA esté disponible.
- **`ValorizadorConfig`**: configuración Spring con `@Bean` para `ContadorDiasService`, `BonosService`, `PagaresService`, `FinancieraService`. En el legacy eran clases estáticas sin inyección — se mantienen los métodos `static` para permitir testing sin Spring context.
- **`GlobalExceptionHandler`**: NPE→400, `IllegalArgumentException`→400 (convencion inválida), `UnsupportedOperationException`→503.

## No Migrado

- Acceso HANA (`BSCL.Valo2.BD::*`): deferred hasta QA-HANA.
- Crystal Reports de valorización: fuera de alcance (Fase 3 exit criterion con JasperReports).

## Follow-ups

- Activar tests `@Disabled` para feriados BUS/252 cuando HANA QA disponible.
- Validar curvas de descuento con datos reales de HANA en staging.
