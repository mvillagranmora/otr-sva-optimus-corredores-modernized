package cl.bcs.fondomutuo.aportereinversion;

import static cl.bcs.fondomutuo.aportereinversion.CorridaCsr.correr;
import static cl.bcs.fondomutuo.aportereinversion.CorridaCsr.n;
import static cl.bcs.fondomutuo.aportereinversion.CorridaCsr.s;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * The validations of {@code CrearSolicitudAporteReinversion} (cards FM-REI-53..60; defects D-F2-8, D-F2-13, D-F2-18, D-F2-21, D-REQ-6/7/9, D-NOR-9/16) as readable assertions over the
 * cases of the matrix. See {@link CsrReglasTest} for the method and for the RED-before-code rule.
 */
class CsrReglasValidacionTest {

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-53
    @Test
    @DisplayName("FM-REI-53 (C67), D-F2-18: EsMontoMaximo also needs MontoMaximo < MontoTotalInversion (default 0): with the default the maximum never rejects; with a MontoTotalInversion above it does")
    void fmRei53_montoMaximoYDefecto() throws Exception {
        assertThat(correr("montomax-con-default-no-rechaza").decision()).isEqualTo("COMMIT");
        CorridaCsr r = correr("montomax-con-montototal-rechaza");
        assertThat(r.mensajesIdCodigo()).containsExactly("1:MONTO_MAXIMO_REQ_APO_RES");
        assertThat(r.decision()).isEqualTo("ROLLBACK");
        assertThat(r.filas()).isEmpty();
        assertThat(correr("montomax-cero-no-valida").decision()).isEqualTo("COMMIT");
        assertThat(correr("montomax-monto-bajo-el-maximo").decision()).isEqualTo("COMMIT");
    }

    @Test
    @DisplayName("FM-REI-53 (C68), D-F2-18: EsMontoMinimo also needs MontoMinimo > MontoTotalInversion: with the default 0 a minimum above the amount rejects (MONTO_MINIMO); with a larger MontoTotalInversion it passes; BF is exempt; a regime that does not validate it passes")
    void fmRei53_montoMinimo() throws Exception {
        assertThat(correr("montomin-violado-con-default-rechaza").mensajesIdCodigo()).containsExactly("1:MONTO_MINIMO");
        assertThat(correr("montomin-con-montototal-mayor-pasa").decision()).isEqualTo("COMMIT");
        assertThat(correr("montomin-bf-exento").decision()).isEqualTo("COMMIT");
        assertThat(correr("montomin-otro-portafolio-no-exento").mensajesIdCodigo()).containsExactly("1:MONTO_MINIMO");
        assertThat(correr("montomin-regimen-no-valida").decision()).isEqualTo("COMMIT");
    }

    @Test
    @DisplayName("FM-REI-53: EsDiferido: an aporte in another currency than the fund and a fund that is not deferred is rejected (FONDO_NO_DIFERIDO), regardless of the Spot parameter; a deferred fund passes and pays in the currency of the fund")
    void fmRei53_diferido() throws Exception {
        CorridaCsr n = correr("diferido-n-moneda-distinta-rechaza");
        assertThat(n.mensajesIdCodigo()).containsExactly("1:FONDO_NO_DIFERIDO_REQ_APO_RES");
        CorridaCsr s = correr("diferido-s-moneda-distinta-pasa");
        assertThat(s.decision()).isEqualTo("COMMIT");
        assertThat(s(s.fila(), "IdMonedaPago")).isEqualTo("USD");
        assertThat(s(s.fila(), "IdMoneda")).isEqualTo("CLP");
        assertThat(n(s.fila(), "Monto")).isEqualByComparingTo("111.11");
        assertThat(s.llamo("ProcesarIngresoOperacionSpot")).isFalse();
    }

    @Test
    @DisplayName("FM-REI-53/52 (C78): a regime without row for the series: EsRegimenValido N and EsPortafolioRegimenValido N (two errors); a regime that does not allow contributions (NOAP): REGIMEN_NO_PERMITE_APORTE")
    void fmRei53_regimen() throws Exception {
        CorridaCsr sin = correr("regimen-sin-fila-para-la-serie");
        assertThat(sin.mensajesIdCodigo()).containsExactly("1:PORTAFOLIO_REGIMEN_INVALIDO", "1:REGIMEN_NO_EXISTE_REQ_APO_RES");
        assertThat(correr("regimen-no-permite-aporte").mensajesIdCodigo()).containsExactly("1:REGIMEN_NO_PERMITE_APORTE");
        assertThat(correr("portafolio-sin-proposito-regimen").mensajesIdCodigo()).containsExactly("1:PORTAFOLIO_REGIMEN_INVALIDO");
    }

    @Test
    @DisplayName("FM-REI-52: Tributacion by GrupoRegimen: APV -> ModeloRetiroAPV, ART108 -> MayorMenorValor, ART107 -> Beneficio107LIR, 57BIS -> Beneficio57BIS, any other group -> MayorMenorValor")
    void fmRei52_tributacion() throws Exception {
        assertThat(s(correr("tributacion-apv").fila(), "Tributacion")).isEqualTo("ModeloRetiroAPV");
        assertThat(s(correr("tributacion-art108").fila(), "Tributacion")).isEqualTo("MayorMenorValor");
        assertThat(s(correr("tributacion-art107").fila(), "Tributacion")).isEqualTo("Beneficio107LIR");
        assertThat(s(correr("tributacion-57bis").fila(), "Tributacion")).isEqualTo("Beneficio57BIS");
        assertThat(s(correr("tributacion-defecto").fila(), "Tributacion")).isEqualTo("MayorMenorValor");
    }

    @Test
    @DisplayName("FM-REI-53 (C69/C70): CIN without portfolio indicator GEN_OCCINV: NullPointerException; indicator N: PORTAFOLIO_NO_OPERA_CUENTA_INVERSION_FFMM; a non CIN medium never reads the indicator; Overnight S warns on closed liquidity (D-F2-21)")
    void fmRei53_cuentaDeInversion() throws Exception {
        assertThat(correr("cin-sin-indicador-npe").claseExcepcion()).isEqualTo("NullPointerException");
        assertThat(correr("cin-indicador-n-rechaza").mensajesIdCodigo()).containsExactly("1:PORTAFOLIO_NO_OPERA_CUENTA_INVERSION_FFMM");
        CorridaCsr tra = correr("medio-no-cin-sin-indicador-no-falla");
        assertThat(tra.decision()).isEqualTo("COMMIT");
        assertThat(s(tra.fila(), "IdMedioDePago")).isEqualTo("TRA");
        assertThat(s(tra.fila(), "Visado")).as("only CIN forces Visado N").isEqualTo("S");
        CorridaCsr liq = correr("overnight-liquidez-cerrada-advierte");
        assertThat(liq.mensajesIdCodigo()).containsExactly("1:FECHA_LIQUIDEZ_CERRADO");
        assertThat(liq.mensajes().get(0).get("tipo").getAsString()).isEqualTo("ADVERTENCIA");
        assertThat(liq.contador()).isZero();
    }

    @Test
    @DisplayName("FM-REI-53/54 (D-REQ-6): blocked fund, inactive series, inactive portfolio, inactive client: each rejects with its code; an IdCliente that is not the one of the portfolio is NOT rejected")
    void fmRei53_estadosYBloqueos() throws Exception {
        assertThat(correr("fondo-bloqueado-para-aporte").mensajesIdCodigo()).containsExactly("1:FONDO_BLOQUEADO_REQ_APO_RES");
        assertThat(correr("serie-inactiva").mensajesIdCodigo()).containsExactly("1:INSTRUMENTO_NO_VIGENTE_RUT_CLIENTE");
        assertThat(correr("portafolio-inactivo").mensajesIdCodigo()).containsExactly("1:PORTAFOLIO_NO_HABILITADO");
        assertThat(correr("cliente-inactivo").mensajesIdCodigo()).containsExactly("1:CLIENTE_BLOQUEADO");
        CorridaCsr otro = correr("cliente-no-es-el-del-portafolio");
        assertThat(otro.decision()).isEqualTo("COMMIT");
        assertThat(n(otro.fila(), "IdCliente")).isEqualByComparingTo("777");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-54 / 57
    @Test
    @DisplayName("FM-REI-54/57 (C72), D-REQ-7: PatrimonioAltos S: the candidate adds the accumulated amount of the lot (MontoMonedaSistema of every request of the client and issuer) to its MontoPatrimonio; below the issuer minimum: UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE")
    void fmRei54_patrimonioDelEmisor() throws Exception {
        CorridaCsr falla = correr("patrimonio-emisor-insuficiente");
        assertThat(falla.mensajesIdCodigo()).containsExactly("1:UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE");
        assertThat(falla.filas()).isEmpty();
        assertThat(correr("patrimonio-emisor-suficiente-con-monto-patrimonio").decision()).isEqualTo("COMMIT");
        assertThat(correr("patrimonio-emisor-serie-sin-patrimonio-altos").decision()).as("PatrimonioAltos N: neither accumulated nor validated").isEqualTo("COMMIT");
        CorridaCsr lote = correr("patrimonio-emisor-acumulado-del-lote");
        assertThat(lote.mensajesIdCodigo()).as("each candidate adds the amount of the WHOLE lot: the first (350000+200000) passes, the second (0+200000) fails").containsExactly("2:UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE");
        assertThat(lote.resultados()).hasSize(1);
        assertThat(lote.llamadas("CalcularParidad")).as("parity of the aportes, of the quota calculation and ONE call of the patrimony check").hasSize(3);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-55 / 56
    @Test
    @DisplayName("FM-REI-56 (C71), D-REQ-6: ValidaMontoInversion rejects with the MontoDestino of the parity (trabajo 1): minimum total, maximum total and high-contribution limits of the series")
    void fmRei56_montoDeInversion() throws Exception {
        assertThat(correr("monto-inversion-minimo-total").mensajesIdCodigo()).containsExactly("1:UI_ERROR_MONTO_TOTAL_INVERSION_FFMM");
        assertThat(correr("monto-inversion-maximo-total").mensajesIdCodigo()).containsExactly("1:UI_ERROR_MONTO_TOTAL_INVERSION_FFMM");
        assertThat(correr("monto-inversion-aportes-altos").mensajesIdCodigo()).containsExactly("1:UI_ERROR_ALTOS_APORTE_FFMM");
    }

    @Test
    @DisplayName("FM-REI-55/56 (C71): ValidaMontoInversion also receives the requests already rejected by the requirements (all of the lot): the rejected one gets the amount error too; each error adds 1 to the counter")
    void fmRei56_incluyeLasRechazadas() throws Exception {
        CorridaCsr r = correr("monto-inversion-incluye-rechazadas");
        assertThat(r.mensajesIdCodigo()).containsExactly("2:REGIMEN_NO_PERMITE_APORTE", "1:UI_ERROR_MONTO_TOTAL_INVERSION_FFMM", "2:UI_ERROR_MONTO_TOTAL_INVERSION_FFMM");
        assertThat(r.contador()).isEqualTo(3);
        assertThat(r.resultados()).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-58 / 60
    @Test
    @DisplayName("FM-REI-60 (C74), D-F2-13: related rescue 0, OperaLineaSobregiro absent or N, balance below the cost: UI_SALDO_INSUFICIENTE_FLUJO_CAJA (no parameters); the balance row is the one of FechaCertificacion")
    void fmRei60_saldoInsuficiente() throws Exception {
        for (String slug : List.of("saldo-insuficiente-relacionado-cero", "saldo-fecha-distinta-es-cero", "saldo-sobregiro-n-explicito-valida", "saldo-usa-el-costo-en-moneda-local")) {
            CorridaCsr r = correr(slug);
            assertThat(r.mensajesIdCodigo()).as(slug).containsExactly("1:UI_SALDO_INSUFICIENTE_FLUJO_CAJA");
            assertThat(r.filas()).as(slug).isEmpty();
            assertThat(r.llamadas("SaldoFlujoCaja")).as(slug).hasSize(1);
            assertThat(r.llamo("AgregarLog")).as(slug).isFalse();
        }
    }

    @Test
    @DisplayName("FM-REI-60 (C74), D-F2-13: a balance equal to the cost passes (total > saldo); sobregiro S, or a related rescue different from 0, skip the check (SaldoFlujoCaja is called anyway)")
    void fmRei60_saldoSuficienteOExento() throws Exception {
        assertThat(correr("saldo-suficiente-relacionado-cero").decision()).isEqualTo("COMMIT");
        assertThat(correr("saldo-sobregiro-s-no-valida").decision()).isEqualTo("COMMIT");
        CorridaCsr rel = correr("saldo-relacionado-distinto-de-cero-no-valida");
        assertThat(rel.decision()).isEqualTo("COMMIT");
        assertThat(rel.llamadas("SaldoFlujoCaja")).hasSize(1);
    }

    @Test
    @DisplayName("FM-REI-58/60 (D-NOR-16 analogue): the cost accumulates per portfolio and currency (two aportes of 60000 against 100000 reject both); the (portfolio, currency) pairs skip a pair when only one of them is new")
    void fmRei58_acumuladoYParesOmitidos() throws Exception {
        CorridaCsr acumula = correr("saldo-acumula-por-portafolio-y-moneda");
        assertThat(acumula.mensajesIdCodigo()).containsExactly("1:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", "2:UI_SALDO_INSUFICIENTE_FLUJO_CAJA");
        assertThat(acumula.arg("SaldoFlujoCaja", 0).getAsJsonArray("P_PORTAFOLIOACONSULTAR")).hasSize(1);
        CorridaCsr pares = correr("saldo-pares-portafolio-moneda-omitidos");
        assertThat(pares.arg("SaldoFlujoCaja", 0).getAsJsonArray("P_PORTAFOLIOACONSULTAR")).as("(500,CLP) (501,USD); the third (500,USD) is omitted: both of its parts are already listed").hasSize(2);
        assertThat(pares.filas()).hasSize(3);
        assertThat(correr("saldo-no-se-llama-si-todos-son-no-cin").llamo("SaldoFlujoCaja")).isFalse();
    }

    @Test
    @DisplayName("FM-REI-58: SaldoFlujoCaja is asked with INGRESO_APORTE_FMM from the system date to the maximum FechaCertificacion, without DESPLEGARENUI / AGRUPARPORORIGEN")
    void fmRei58_entradaDeSaldoFlujoCaja() throws Exception {
        JsonObject a = correr("real-un-aporte-nacional-cin").arg("SaldoFlujoCaja", 0);
        assertThat(a.get("P_IDEMPRESA").getAsLong()).isEqualTo(1);
        assertThat(a.get("P_FECHADESDE").getAsString()).isEqualTo("2026-03-02");
        assertThat(a.get("P_FECHAHASTA").getAsString()).isEqualTo("2026-03-03");
        assertThat(a.get("P_USO").getAsString()).isEqualTo("INGRESO_APORTE_FMM");
        assertThat(a.get("P_DESPLEGARENUI").getAsString()).isEqualTo("N");
        assertThat(a.get("P_AGRUPARPORORIGEN").getAsString()).isEqualTo("N");
        JsonObject par = a.getAsJsonArray("P_PORTAFOLIOACONSULTAR").get(0).getAsJsonObject();
        assertThat(par.get("IdPortafolio").getAsLong()).isEqualTo(500);
        assertThat(par.get("IdMoneda").getAsString()).isEqualTo("CLP");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-59
    @Test
    @DisplayName("FM-REI-59 (C75): PatrimonioMinimo > 0: the custody flow is asked (one cartera per portfolio, one instrument per IdInstrumento with the regime of the first); Monto + custody x quota >= minimum passes")
    void fmRei59_custodiaSuficiente() throws Exception {
        CorridaCsr r = correr("patrimonio-minimo-con-custodia-suficiente");
        assertThat(r.decision()).isEqualTo("COMMIT");
        JsonObject a = r.arg("ConsultarFlujoCustodia", 0);
        assertThat(a.get("p_FechaSaldo").getAsString()).isEqualTo("2026-03-02");
        assertThat(a.get("p_IdPais").getAsLong()).isEqualTo(56);
        assertThat(a.get("p_DiasProyeccion").getAsLong()).isZero();
        assertThat(a.getAsJsonArray("p_Uso")).hasSize(1);
        assertThat(a.getAsJsonArray("p_FiltrosBusqueda").get(0).getAsJsonObject().get("IdInstrumento").getAsLong()).isEqualTo(901);
        assertThat(a.getAsJsonArray("p_FiltrosBusqueda").get(0).getAsJsonObject().get("IdRegimenFondoMutuo").getAsString()).isEqualTo("GEN");
        assertThat(correr("patrimonio-minimo-cero-no-consulta-custodia").llamo("ConsultarFlujoCustodia")).isFalse();
    }

    @Test
    @DisplayName("FM-REI-59 (C75), D-F2-8: without custody (or with the first flow too small: the FIRST matching flow is taken, break): UI_PATRIMONIO_MINIMO_INSUFICIENTE with no parameters")
    void fmRei59_custodiaInsuficiente() throws Exception {
        assertThat(correr("patrimonio-minimo-sin-custodia-rechaza").mensajesIdCodigo()).containsExactly("1:UI_PATRIMONIO_MINIMO_INSUFICIENTE");
        CorridaCsr primero = correr("patrimonio-minimo-primer-flujo-gana");
        assertThat(primero.mensajesIdCodigo()).containsExactly("1:UI_PATRIMONIO_MINIMO_INSUFICIENTE");
        assertThat(primero.filas()).isEmpty();
    }

    @Test
    @DisplayName("FM-REI-59: two portfolios with PatrimonioMinimo: one call to the custody flow with two carteras; the one without custody (501) is rejected")
    void fmRei59_unaCarteraPorPortafolio() throws Exception {
        CorridaCsr r = correr("custodia-una-cartera-por-portafolio");
        assertThat(r.llamadas("ConsultarFlujoCustodia")).hasSize(1);
        assertThat(r.mensajesIdCodigo()).containsExactly("2:UI_PATRIMONIO_MINIMO_INSUFICIENTE");
        assertThat(r.resultados()).hasSize(1);
    }

    @Test
    @DisplayName("FM-REI-59, D-REQ-9: the custody flow must be of the instrument AND of the regime of the request (esFlujoCustodiaParaSolicitudProceso): a flow of another regime does not count; with a non CIN medium the custody is read BEFORE Tesoreria is called")
    void fmRei59_regimenDelFlujoYOrdenDeLasLecturas() throws Exception {
        assertThat(correr("patrimonio-minimo-flujo-de-otro-regimen-no-coincide").mensajesIdCodigo()).containsExactly("1:UI_PATRIMONIO_MINIMO_INSUFICIENTE");
        CorridaCsr tra = correr("patrimonio-minimo-medio-tra");
        assertThat(tra.decision()).isEqualTo("COMMIT");
        assertThat(tra.llamo("SaldoFlujoCaja")).isFalse();
        assertThat(tra.orden().indexOf("ConsultarFlujoCustodia")).isLessThan(tra.orden().indexOf("ProcesarIngresoEgresoTesoreria"));
    }

    @Test
    @DisplayName("FM-REI-55 (D-REQ-1, D-NOR-7): three aportes with the SAME requirement error: the closure reports ONE message (id of the first) and the AR rejects only the first request with that id: the other two stay pending, are inserted (until the host rolls back for the counter) and logged")
    void fmRei55_unSoloMensajeRechazaSoloLaPrimera() throws Exception {
        CorridaCsr r = correr("tres-rechazos-iguales-un-solo-mensaje");
        assertThat(r.mensajesIdCodigo()).containsExactly("1:REGIMEN_NO_PERMITE_APORTE");
        assertThat(r.contador()).isEqualTo(1);
        assertThat(r.decision()).as("the counter is > 0: the host rolls back what was written").isEqualTo("ROLLBACK");
        assertThat(r.resultados().stream().map(x -> n(x, "IdInterno").intValue()).toList()).containsExactly(2, 3);
        assertThat(r.llamadas("AgregarLog")).hasSize(1);
        assertThat(r.arg("AgregarLog", 0).getAsJsonArray("p_Log")).hasSize(2);
        assertThat(r.secuencias()).containsExactly("SolicitudAporteRescate:3", "FolioContrato:3");
    }
}
