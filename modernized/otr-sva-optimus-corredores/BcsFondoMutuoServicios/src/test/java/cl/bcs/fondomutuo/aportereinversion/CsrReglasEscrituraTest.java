package cl.bcs.fondomutuo.aportereinversion;

import static cl.bcs.fondomutuo.aportereinversion.CorridaCsr.correr;
import static cl.bcs.fondomutuo.aportereinversion.CorridaCsr.n;
import static cl.bcs.fondomutuo.aportereinversion.CorridaCsr.s;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The writes and the collaborators behind ports of {@code CrearSolicitudAporteReinversion}: Tesoreria (gate G1), Spot (gate G2), the INSERT, {@code FechaFinalReinversion}, AGF and the log
 * (cards FM-REI-61..65 and 68; defects D-F2-4, D-F2-11, D-F2-12, D-F2-19, D-TES-1/2, D-NOR-5/12) and the lots of 2.000 (FM-REI-43), plus the contract that Certificar uses
 * ({@code crearAporteReinversion}). See {@link CsrReglasTest} for the method and for the RED-before-code rule.
 */
class CsrReglasEscrituraTest {

    private static String formas(JsonObject spot, int i, String clave) {
        return spot.getAsJsonArray("p_DatosFormaPago").get(i).getAsJsonObject().get(clave).getAsString();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-61
    @Test
    @DisplayName("FM-REI-61 (C53), D-F2-11: medium TRA: ProcesarIngresoEgresoTesoreria receives the whole pending request (p_EsIngresoAutomatico N, the simulation flag and the rules flag of the input); no balance is asked")
    void fmRei61_tesoreriaRecibeLaSolicitudCompleta() throws Exception {
        CorridaCsr r = correr("tesoreria-medio-tra");
        assertThat(r.decision()).isEqualTo("COMMIT");
        JsonObject a = r.arg("ProcesarIngresoEgresoTesoreria", 0);
        assertThat(a.get("p_IdEmpresa").getAsLong()).isEqualTo(1);
        assertThat(a.get("p_IdUsuario").getAsLong()).isEqualTo(7);
        assertThat(a.get("p_IdModulo").getAsString()).isEqualTo("FMM");
        assertThat(a.get("p_SimularOperacion").getAsString()).isEqualTo("N");
        assertThat(a.get("p_UsaReglasNegocio").getAsString()).isEqualTo("S");
        assertThat(a.get("p_EsIngresoAutomatico").getAsString()).isEqualTo("N");
        JsonObject sol = a.getAsJsonArray("p_Solicitudes").get(0).getAsJsonObject();
        assertThat(sol.get("IdMedioDePago").getAsString()).isEqualTo("TRA");
        assertThat(sol.get("IdTipoMovimientoFondoMutuo").getAsString()).as("D-TES-2: sent as APOREINV (Tesoreria maps the concept APORFM)").isEqualTo("APOREINV");
        assertThat(sol.get("EstadoProceso").getAsString()).isEqualTo("PEN");
        assertThat(sol.get("IdSolicitudAporteRescate").getAsLong()).isEqualTo(11413788L);
        assertThat(sol.get("IdInterno").getAsLong()).isEqualTo(1);
        assertThat(r.llamo("SaldoFlujoCaja")).isFalse();
        assertThat(n(r.fila(), "IdIngresoEgreso")).isZero();
        assertThat(r.orden().indexOf("ProcesarIngresoEgresoTesoreria")).isLessThan(r.orden().indexOf("AgregarLog"));
    }

    @Test
    @DisplayName("FM-REI-61: simulation does not call Tesoreria; medium 'cin' in lower case is CIN (no Tesoreria, balance asked); a mixed lot sends only the TRA aporte; a request already rejected is not sent")
    void fmRei61_quienVaATesoreria() throws Exception {
        CorridaCsr sim = correr("tesoreria-simulacion-no-llama");
        assertThat(sim.llamo("ProcesarIngresoEgresoTesoreria")).isFalse();
        assertThat(sim.resultados()).hasSize(1);
        CorridaCsr cin = correr("tesoreria-medio-cin-minuscula-no-llama");
        assertThat(cin.llamo("ProcesarIngresoEgresoTesoreria")).isFalse();
        assertThat(s(cin.fila(), "Visado")).isEqualTo("N");
        CorridaCsr mixto = correr("tesoreria-lote-mixto-cin-y-tra");
        JsonArray enviadas = mixto.arg("ProcesarIngresoEgresoTesoreria", 0).getAsJsonArray("p_Solicitudes");
        assertThat(enviadas).hasSize(1);
        assertThat(enviadas.get(0).getAsJsonObject().get("IdInterno").getAsLong()).isEqualTo(2);
        assertThat(mixto.arg("SaldoFlujoCaja", 0).getAsJsonArray("P_PORTAFOLIOACONSULTAR")).hasSize(1);
        assertThat(correr("tesoreria-rechazada-por-validacion-no-se-envia").llamo("ProcesarIngresoEgresoTesoreria")).isFalse();
    }

    @Test
    @DisplayName("FM-REI-61 (C54), D-F2-11: ONLY Resultado RECH with Campo NumeroReferencia rejects (the first request with that IdInterno): it is not inserted, not in the result and the counter stays 0")
    void fmRei61_soloRechConNumeroReferenciaRechaza() throws Exception {
        CorridaCsr r = correr("tesoreria-rech-numeroreferencia-rechaza");
        assertThat(r.decision()).as("COMMIT of nothing: the rejected request vanishes without a message").isEqualTo("COMMIT");
        assertThat(r.contador()).isZero();
        assertThat(r.mensajes()).isEmpty();
        assertThat(r.resultados()).isEmpty();
        assertThat(r.filas()).isEmpty();
        assertThat(r.llamo("AgregarLog")).isFalse();
        for (String slug : List.of("tesoreria-rech-otro-campo-ignora", "tesoreria-resultado-ok-ignora")) {
            assertThat(correr(slug).filas()).as(slug).hasSize(1);
        }
        CorridaCsr dos = correr("tesoreria-dos-aportes-uno-rech");
        assertThat(dos.filas()).hasSize(1);
        assertThat(n(dos.fila(), "IdPortafolio")).isEqualByComparingTo("500");
        assertThat(dos.resultados()).hasSize(1);
        assertThat(n(dos.resultado(), "IdInterno")).isEqualByComparingTo("1");
    }

    @Test
    @DisplayName("FM-REI-61 (C54), D-F2-11, D-TES-1: Tesoreria's counter, its messages and its generated ids are IGNORED: counter 0, no message, IdIngresoEgreso stays 0")
    void fmRei61_contadorMensajesYGeneradosSeIgnoran() throws Exception {
        CorridaCsr err = correr("tesoreria-error-sin-rech-ignora");
        assertThat(err.contador()).isZero();
        assertThat(err.mensajes()).isEmpty();
        assertThat(err.decision()).isEqualTo("COMMIT");
        assertThat(n(err.fila(), "IdIngresoEgreso")).isZero();
        assertThat(n(correr("tesoreria-generado-ignora").fila(), "IdIngresoEgreso")).isZero();
    }

    @Test
    @DisplayName("FM-REI-61/67/68: an exception of Tesoreria (or of Spot, AgregarLog) comes out untouched (no catch in the AR); nothing was written by the AR before it; Visado of the input is respected for a non CIN medium")
    void fmRei61_excepcionesDeColaboradores() throws Exception {
        CorridaCsr t = correr("tesoreria-excepcion-del-colaborador");
        assertThat(t.decision()).isEqualTo("ROLLBACK_HTTP500");
        assertThat(t.excepcion()).endsWith(": falla de tesoreria");
        assertThat(t.filas()).isEmpty();
        assertThat(correr("spot-excepcion-del-colaborador").excepcion()).endsWith(": falla de mesa");
        CorridaCsr log = correr("log-excepcion-tras-insertar");
        assertThat(log.excepcion()).endsWith(": falla log");
        assertThat(log.llamadas("AgregarLog")).hasSize(1);
        assertThat(s(correr("tesoreria-visado-respeta-entrada").fila(), "Visado")).isEqualTo("S");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-51 / 63
    @Test
    @DisplayName("FM-REI-51/63 (C55), D-F2-4: parameter S and CLP aporte to a USD series: GeneraSpot S, IdMonedaPago CLP, IdMoneda USD, cost = vc.Monto, state PEN, IdOperacionSpot of the answer; forms COMSPT_CIN_CLP_0 (MOVING, MontoDestino, R) and VENSPT_CIN_USD_0 (MOVEGR, Monto, P)")
    void fmRei63_spotDeClpAUsd() throws Exception {
        CorridaCsr r = correr("spot-clp-a-usd");
        JsonObject f = r.fila();
        assertThat(s(f, "IdMoneda")).isEqualTo("USD");
        assertThat(s(f, "IdMonedaPago")).isEqualTo("CLP");
        assertThat(s(f, "Estado")).isEqualTo("PEN");
        assertThat(n(f, "IdOperacionSpot")).isEqualByComparingTo("777");
        assertThat(n(f, "Monto")).isEqualByComparingTo("111.11");
        assertThat(n(f, "CostoMonedaLocal")).as("generaSpot: vc.Monto and not vc.MontoDestino").isEqualByComparingTo("111.11");
        assertThat(n(f, "MontoPagoLocal")).isEqualByComparingTo("111.11");
        JsonObject a = r.arg("ProcesarIngresoOperacionSpot", 0);
        assertThat(a.get("p_CalculaMargen").getAsString()).isEqualTo("S");
        assertThat(a.get("p_UsaReglasNegocio").getAsString()).isEqualTo("S");
        assertThat(a.get("p_PermiteOperarProspectos").getAsString()).isEqualTo("N");
        assertThat(a.getAsJsonArray("p_DatosOperacion")).hasSize(1);
        assertThat(a.getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject().get("GeneraSpot").getAsString()).isEqualTo("S");
        assertThat(a.getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject().get("Estado").getAsString()).isEqualTo("PEN");
        assertThat(a.getAsJsonArray("p_DatosOrigenInversion")).as("the INPUT aporte (all its 38 fields, TipoReinversion as classified)").hasSize(1);
        JsonObject origen = a.getAsJsonArray("p_DatosOrigenInversion").get(0).getAsJsonObject();
        assertThat(origen.keySet()).hasSize(38);
        assertThat(origen.get("TipoReinversion").getAsString()).isEqualTo("NACIONAL");
        assertThat(origen.get("IdMoneda").getAsString()).isEqualTo("CLP");
        assertThat(a.getAsJsonArray("p_DatosFormaPago")).hasSize(2);
        assertThat(formas(a, 0, "IdFormaDePago")).isEqualTo("COMSPT_CIN_CLP_0");
        assertThat(formas(a, 0, "IdTipoMovimiento")).isEqualTo("MOVING");
        assertThat(formas(a, 0, "RecibePagaPortafolio")).isEqualTo("R");
        assertThat(a.getAsJsonArray("p_DatosFormaPago").get(0).getAsJsonObject().get("MontoFormaDePago").getAsBigDecimal()).isEqualByComparingTo("100000");
        assertThat(formas(a, 0, "IdInternoPagoOperacion")).isEqualTo("0");
        assertThat(formas(a, 1, "IdFormaDePago")).isEqualTo("VENSPT_CIN_USD_0");
        assertThat(formas(a, 1, "IdTipoMovimiento")).isEqualTo("MOVEGR");
        assertThat(formas(a, 1, "RecibePagaPortafolio")).isEqualTo("P");
        assertThat(a.getAsJsonArray("p_DatosFormaPago").get(1).getAsJsonObject().get("MontoFormaDePago").getAsBigDecimal()).isEqualByComparingTo("111.11");
        assertThat(formas(a, 1, "IdInternoPagoOperacion")).isEqualTo("1");
        assertThat(formas(a, 0, "AfectarTesoreria")).isEqualTo("N");
        assertThat(formas(a, 0, "FechaLiquidacion")).as("FechaAceptacion").startsWith("2026-03-02");
    }

    @Test
    @DisplayName("FM-REI-63 (C56), D-F2-4: USD aporte to a CLP series: forms COMSPT_CIN_USD_0 (MOVEGR) and VENSPT_CIN_CLP_0 (MOVING); with FechaLiquidaSpotCompra both forms settle that day and the operation carries a COPY with FechaCertificacion = FechaCertificaSpotCompra")
    void fmRei63_spotDeUsdAClpYFechasDelRescate() throws Exception {
        JsonObject base = correr("spot-usd-a-clp").arg("ProcesarIngresoOperacionSpot", 0);
        assertThat(formas(base, 0, "IdFormaDePago")).isEqualTo("COMSPT_CIN_USD_0");
        assertThat(formas(base, 0, "IdTipoMovimiento")).isEqualTo("MOVEGR");
        assertThat(formas(base, 1, "IdFormaDePago")).isEqualTo("VENSPT_CIN_CLP_0");
        assertThat(formas(base, 1, "IdTipoMovimiento")).isEqualTo("MOVING");
        assertThat(formas(base, 0, "FechaLiquidacion")).startsWith("2026-03-02");
        assertThat(base.getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject().get("FechaCertificacion").getAsString()).startsWith("2026-03-03");
        CorridaCsr con = correr("spot-usd-a-clp-con-fechas-del-rescate");
        JsonObject a = con.arg("ProcesarIngresoOperacionSpot", 0);
        assertThat(formas(a, 0, "FechaLiquidacion")).startsWith("2026-03-11");
        assertThat(formas(a, 1, "FechaLiquidacion")).startsWith("2026-03-11");
        assertThat(a.getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject().get("FechaCertificacion").getAsString()).startsWith("2026-03-10");
        assertThat(s(con.fila(), "FechaCertificacion")).as("the INSERTED request keeps its own date (the copy only goes to Spot)").isEqualTo("2026-03-03");
        JsonObject sola = correr("spot-solo-fecha-liquida").arg("ProcesarIngresoOperacionSpot", 0);
        assertThat(sola.getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject().get("FechaCertificacion").getAsString()).as("no copy without FechaCertificaSpotCompra").startsWith("2026-03-03");
        assertThat(formas(sola, 0, "FechaLiquidacion")).startsWith("2026-03-11");
    }

    @Test
    @DisplayName("FM-REI-63: CLP -> USD with the Spot dates of the rescue: the copy takes FechaCertificacion, the forms keep FechaAceptacion (only the USD -> CLP branch reads FechaLiquidaSpotCompra)")
    void fmRei63_clpAUsdConFechasDelRescate() throws Exception {
        JsonObject a = correr("spot-clp-a-usd-con-fechas-del-rescate").arg("ProcesarIngresoOperacionSpot", 0);
        assertThat(a.getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject().get("FechaCertificacion").getAsString()).startsWith("2026-03-10");
        assertThat(formas(a, 0, "FechaLiquidacion")).startsWith("2026-03-02");
        assertThat(formas(a, 1, "FechaLiquidacion")).startsWith("2026-03-02");
    }

    @Test
    @DisplayName("FM-REI-63 (C58/C57), D-F2-12, D-NOR-5/12: RECH without error message: the request is RZD but INSERTED anyway (state PEN of the row); an ERROR message: counter +1, message 'SPT : ...' (id of the message), the FIRST insertable rejected and the lot is cut (return true: nothing inserted)")
    void fmRei63_rechazosDeSpot() throws Exception {
        CorridaCsr rech = correr("spot-rech-sin-mensaje-se-inserta-rzd");
        assertThat(rech.decision()).isEqualTo("COMMIT");
        assertThat(rech.filas()).hasSize(1);
        assertThat(n(rech.fila(), "IdOperacionSpot")).isZero();
        CorridaCsr err = correr("spot-mensaje-error-corta-el-lote");
        assertThat(err.decision()).isEqualTo("ROLLBACK");
        assertThat(err.contador()).isEqualTo(1);
        assertThat(err.mensajesIdCodigo()).containsExactly("0:UI_ERROR_SPOT");
        assertThat(err.mensajes().get(0).get("descripcion").getAsString()).isEqualTo("SPT : sin liquidez");
        assertThat(err.mensajes().get(0).get("tipo").getAsString()).isEqualTo("ERROR");
        assertThat(err.filas()).isEmpty();
        assertThat(err.llamo("AgregarLog")).isFalse();
        assertThat(err.resultados()).as("the result already had the pending request").hasSize(1);
        assertThat(correr("spot-mensaje-error-en-minuscula-corta").mensajes().get(0).get("tipo").getAsString()).isEqualTo("error");
        assertThat(correr("spot-mensaje-error-en-minuscula-corta").contador()).isEqualTo(1);
    }

    @Test
    @DisplayName("FM-REI-63, D-F2-12: a Spot message of another type (ADVERTENCIA) or a non zero Spot counter without ERROR message does not cut; two aportes with Spot and an error reject only the FIRST and cut the lot; without error both get their IdOperacionSpot")
    void fmRei63_variosAportesConSpot() throws Exception {
        assertThat(correr("spot-mensaje-advertencia-no-corta").filas()).hasSize(1);
        assertThat(correr("spot-contador-de-spot-no-se-lee").filas()).hasSize(1);
        CorridaCsr err = correr("spot-dos-aportes-error-rechaza-solo-el-primero");
        assertThat(err.decision()).isEqualTo("ROLLBACK");
        assertThat(err.filas()).isEmpty();
        assertThat(err.resultados()).hasSize(2);
        CorridaCsr ok = correr("spot-dos-aportes-ok");
        assertThat(ok.filas()).hasSize(2);
        assertThat(n(ok.filas().get(0), "IdOperacionSpot")).isEqualByComparingTo("11");
        assertThat(n(ok.filas().get(1), "IdOperacionSpot")).isEqualByComparingTo("12");
        JsonObject a = ok.arg("ProcesarIngresoOperacionSpot", 0);
        assertThat(a.getAsJsonArray("p_DatosFormaPago")).hasSize(4);
        assertThat(formas(a, 2, "IdInternoPagoOperacion")).isEqualTo("2");
        assertThat(formas(a, 3, "IdInternoPagoOperacion")).isEqualTo("3");
        assertThat(formas(a, 2, "IdOperacion")).isEqualTo("2");
    }

    @Test
    @DisplayName("FM-REI-51/63 (C59): only the requests that cross CLP/USD go to Spot; parameter N, 's' (exact S needed), same currency, or the GeneraSpot of the input do NOT generate Spot; a Spot answer for an unknown id is ignored")
    void fmRei51_decisionDeSpot() throws Exception {
        CorridaCsr mixto = correr("spot-solo-las-que-generan-spot");
        assertThat(mixto.arg("ProcesarIngresoOperacionSpot", 0).getAsJsonArray("p_DatosOperacion")).hasSize(1);
        assertThat(mixto.filas()).hasSize(2);
        for (String slug : List.of("spot-parametro-n-no-genera", "spot-parametro-minuscula-no-genera", "spot-parametro-minuscula-usd-a-clp", "spot-parametro-s-sin-solicitudes-spot", "spot-parametro-s-moneda-igual-no-genera",
                "spot-generaspot-de-la-entrada-se-ignora")) {
            CorridaCsr r = correr(slug);
            assertThat(r.llamo("ProcesarIngresoOperacionSpot")).as(slug).isFalse();
            assertThat(r.filas()).as(slug).hasSize(1);
        }
        assertThat(s(correr("spot-parametro-n-no-genera").fila(), "IdMonedaPago")).isEqualTo("USD");
        CorridaCsr sim = correr("spot-simulacion-no-llama");
        assertThat(sim.llamo("ProcesarIngresoOperacionSpot")).isFalse();
        assertThat(sim.resultados()).hasSize(1);
        assertThat(n(correr("spot-operacion-con-id-ajeno-se-ignora").fila(), "IdOperacionSpot")).isZero();
        assertThat(correr("spot-idinterno-no-numerico").claseExcepcion()).isEqualTo("NumberFormatException");
    }

    @Test
    @DisplayName("FM-REI-61/63/68: Tesoreria runs before Spot and before the INSERT; a request rejected by Tesoreria never reaches Spot")
    void fmRei68_ordenDeLosEfectos() throws Exception {
        CorridaCsr juntos = correr("spot-y-tesoreria-juntos");
        List<String> orden = juntos.orden();
        assertThat(orden.indexOf("ProcesarIngresoEgresoTesoreria")).isLessThan(orden.indexOf("ProcesarIngresoOperacionSpot"));
        assertThat(orden.indexOf("ProcesarIngresoOperacionSpot")).isLessThan(orden.indexOf("AgregarLog"));
        CorridaCsr rech = correr("spot-con-tesoreria-rech-no-llega-a-spot");
        assertThat(rech.llamo("ProcesarIngresoEgresoTesoreria")).isTrue();
        assertThat(rech.llamo("ProcesarIngresoOperacionSpot")).isFalse();
        assertThat(rech.filas()).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-62 / 64 / 65
    @Test
    @DisplayName("FM-REI-52/64: INSERT: Visado N when the medium is CIN even if the input says S; a null Visado with a non CIN medium and a duplicated primary key make the INSERT fail (SQLException) after Tesoreria")
    void fmRei64_insert() throws Exception {
        assertThat(s(correr("insert-visado-con-cin-es-n").fila(), "Visado")).isEqualTo("N");
        CorridaCsr nulo = correr("insert-visado-nulo-no-cin");
        assertThat(nulo.claseExcepcion()).isEqualTo("SQLException");
        assertThat(nulo.llamo("ProcesarIngresoEgresoTesoreria")).isTrue();
        CorridaCsr pk = correr("insert-pk-duplicada");
        assertThat(pk.claseExcepcion()).isEqualTo("SQLException");
        assertThat(pk.llamo("AgregarLog")).isFalse();
    }

    @Test
    @DisplayName("FM-REI-52: two aportes, one with an informed folio (900) and one from the sequence: the sequence is asked for both and taken by POSITION of the aporte in the list")
    void fmRei52_foliosPorPosicion() throws Exception {
        CorridaCsr r = correr("insert-folios-por-posicion");
        assertThat(r.secuencias()).containsExactly("SolicitudAporteRescate:2", "FolioContrato:2");
        assertThat(n(r.filas().get(0), "FolioContrato")).isEqualByComparingTo("100049222");
        assertThat(n(r.filas().get(1), "FolioContrato")).isEqualByComparingTo("900");
        assertThat(n(r.filas().get(0), "IdSolicitudAporteRescate")).isEqualByComparingTo("11413788");
        assertThat(n(r.filas().get(1), "IdSolicitudAporteRescate")).isEqualByComparingTo("11413789");
    }

    @Test
    @DisplayName("FM-REI-64 (C77), D-F2-4: FechaFinalReinversion of the destination row (related rescue, series, regime) = FechaCertificacion of the aporte; unchanged rows and rescues without rows are not touched; the last of several aportes wins")
    void fmRei64_fechaFinalReinversion() throws Exception {
        assertThat(correr("fechafinal-actualiza-la-fila-de-reinversion").fechasFinales()).containsExactly("2026-03-03", "2026-03-02", "2026-03-02");
        assertThat(correr("fechafinal-sin-cambio-no-actualiza").fechasFinales()).containsExactly("2026-03-03", "2026-03-02", "2026-03-02");
        assertThat(correr("fechafinal-relacionado-sin-filas").fechasFinales()).containsExactly("2026-03-02", "2026-03-02", "2026-03-02");
        CorridaCsr dos = correr("fechafinal-varios-aportes-misma-fila-gana-el-ultimo");
        assertThat(dos.filas().stream().map(f -> s(f, "FechaCertificacion")).toList()).containsExactly("2026-03-03", "2026-03-09");
        assertThat(dos.fechasFinales().get(0)).isEqualTo("2026-03-09");
        CorridaCsr filas = correr("fechafinal-dos-filas-del-mismo-rescate");
        assertThat(filas.fechasFinales()).containsExactly("2026-03-03", "2026-03-04", "2026-03-02");
        CorridaCsr batch = correr("fechafinal-lote-batch-sin-agf");
        assertThat(batch.fechasFinales()).as("batch certification = today = the stored date: nothing to update").containsExactly("2026-03-02", "2026-03-02", "2026-03-02");
        assertThat(correr("costouf-mlt-s-todo-18q-hereda").fechasFinales()).as("the row of the rescue 9002").containsExactly("2026-03-02", "2026-03-02", "2026-03-03");
    }

    @Test
    @DisplayName("FM-REI-65 (C76): AgregarLog: ONE call with one entry per inserted request: IdTipoRegistro SOLICITUD_APORTE_REINVERSION, IdTipoLog INGRESO, EstadoRegistro = the state of the request, TipoCambio = ParidadFechaInversion.doubleValue()")
    void fmRei65_log() throws Exception {
        CorridaCsr r = correr("real-un-aporte-nacional-cin");
        JsonObject l = r.arg("AgregarLog", 0).getAsJsonArray("p_Log").get(0).getAsJsonObject();
        assertThat(l.get("IdCabeceraLog").getAsLong()).isEqualTo(11413788L);
        assertThat(l.get("IdTipoLog").getAsString()).isEqualTo("INGRESO");
        assertThat(l.get("IdModulo").getAsString()).isEqualTo("FMM");
        assertThat(l.get("IdTipoRegistro").getAsString()).isEqualTo("SOLICITUD_APORTE_REINVERSION");
        assertThat(l.get("EstadoRegistro").getAsString()).isEqualTo("ING");
        assertThat(l.get("IdTipoOperacion").getAsString()).isEqualTo("APOREINV");
        assertThat(l.get("TipoCambio").getAsString()).isEqualTo("1.0");
        assertThat(l.get("Nemo").getAsString()).isEqualTo("FM002A");
        assertThat(l.get("Folio").getAsLong()).isEqualTo(11413788L);
        assertThat(l.get("IdTipoDocumentoIdentidad").getAsString()).isEqualTo("RUT");
        assertThat(l.get("IdDocumentoIdentidad").getAsString()).isEqualTo("11111111-1");
        assertThat(l.get("NombreCliente").getAsString()).isEqualTo("Cliente Ficticio Uno");
        assertThat(l.get("FechaSistemaLog").getAsString()).isEqualTo("2026-03-02");
        assertThat(l.get("IdProceso").getAsLong()).isZero();
        assertThat(correr("log-con-cliente-y-portafolio").arg("AgregarLog", 0).getAsJsonArray("p_Log").get(0).getAsJsonObject().get("IdMonedaOrigen").getAsString()).isEqualTo("USD");
        CorridaCsr dos = correr("log-dos-solicitudes-en-un-solo-llamado");
        assertThat(dos.llamadas("AgregarLog")).hasSize(1);
        assertThat(dos.arg("AgregarLog", 0).getAsJsonArray("p_Log")).hasSize(2);
        assertThat(correr("log-no-se-llama-sin-insertables").llamo("AgregarLog")).isFalse();
        assertThat(correr("spot-clp-a-usd").arg("AgregarLog", 0).getAsJsonArray("p_Log").get(0).getAsJsonObject().get("EstadoRegistro").getAsString()).as("PEN after Spot").isEqualTo("PEN");
    }

    @Test
    @DisplayName("FM-REI-62/65 (C76): AGF only for SolicitudBatch N (equalsIgnoreCase) and with the replica active: the beneficiary and the request rows of InterfazAgf; batch rows never")
    void fmRei65_agf() throws Exception {
        CorridaCsr agf = correr("agf-con-fila-en-la-vista");
        List<JsonObject> interfaz = agf.tabla("BSCL.FondoMutuo.BD::FMM.InterfazAgf");
        assertThat(interfaz).hasSize(2);
        assertThat(s(interfaz.get(0), "TipoRegistro")).isEqualTo("PA");
        assertThat(s(interfaz.get(1), "TipoRegistro")).isEqualTo("AR");
        assertThat(n(interfaz.get(1), "IdSolicitudAporteRescate")).isEqualByComparingTo("11413788");
        assertThat(correr("agf-batch-no-entra").tabla("BSCL.FondoMutuo.BD::FMM.InterfazAgf")).isEmpty();
        CorridaCsr estricto = correr("agf-h2-standin-estricto-falla");
        assertThat(estricto.claseExcepcion()).as("H2 limit: the step table InterfazAgf has the NOT NULL columns of both inserts").isEqualTo("SQLException");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-43 / 63 / 67 / 68 lots
    @Test
    @DisplayName("FM-REI-43 (C63), D-F2-19: 2.001 aportes: two lots (2.000 and 1): the sequences, reads, SaldoFlujoCaja and AgregarLog are done per lot; 2.001 rows inserted; both lots answer in one output")
    void fmRei43_dosLotes() throws Exception {
        CorridaCsr r = correr("lote-2001-aportes-dos-lotes");
        assertThat(r.decision()).isEqualTo("COMMIT");
        assertThat(r.secuencias()).containsExactly("SolicitudAporteRescate:2000", "FolioContrato:2000", "SolicitudAporteRescate:1", "FolioContrato:1");
        assertThat(r.resultados()).hasSize(2001);
        assertThat(r.llamadas("SaldoFlujoCaja")).hasSize(2);
        assertThat(r.llamadas("AgregarLog")).hasSize(2);
        assertThat(r.arg("AgregarLog", 0).getAsJsonArray("p_Log")).hasSize(2000);
        assertThat(r.arg("AgregarLog", 1).getAsJsonArray("p_Log")).hasSize(1);
        JsonObject conteo = r.tabla("CSR.ConteoSolicitudes").get(0);
        assertThat(n(conteo, "Filas")).isEqualByComparingTo("2001");
        assertThat(n(conteo, "PrimerId")).isEqualByComparingTo("11413788");
        assertThat(r.fechasFinales().get(0)).isEqualTo("2026-03-03");
    }

    @Test
    @DisplayName("FM-REI-43/63 (C57/C63), D-F2-12, D-F2-19: an error of Spot in the first lot cuts the process (return true): the second lot is NOT processed (no sequences, no reads, no message) and nothing is inserted")
    void fmRei43_corteDelLote() throws Exception {
        CorridaCsr r = correr("lote-2001-corte-por-error-de-spot");
        assertThat(r.decision()).isEqualTo("ROLLBACK");
        assertThat(r.secuencias()).containsExactly("SolicitudAporteRescate:2000", "FolioContrato:2000");
        assertThat(r.llamadas("CalcularParidad")).as("only the lot 1 reads: the parity of the aportes, of the quota calculation and of the Spot pass").hasSize(3);
        assertThat(r.llamadas("SaldoFlujoCaja")).hasSize(1);
        assertThat(r.llamadas("ProcesarIngresoOperacionSpot")).hasSize(1);
        assertThat(r.llamo("AgregarLog")).isFalse();
        assertThat(r.mensajesIdCodigo()).containsExactly("2:REGIMEN_NO_PERMITE_APORTE", "0:UI_ERROR_SPOT");
        assertThat(r.contador()).isEqualTo(2);
        assertThat(n(r.tabla("CSR.ConteoSolicitudes").get(0), "Filas")).isZero();
    }

    @Test
    @DisplayName("FM-REI-43/55/56/61/64/65: a lot of 30 aportes of six portfolios, CIN and TRA, CLP and USD series, some rejected: one coherent output (counter, order of the messages and of the results, sequences once for 30)")
    void fmRei43_loteMixto30() throws Exception {
        CorridaCsr r = correr("lote-mixto-30-aportes");
        assertThat(r.secuencias()).containsExactly("SolicitudAporteRescate:30", "FolioContrato:30");
        assertThat(r.mensajesIdCodigo()).containsExactly("7:REGIMEN_NO_PERMITE_APORTE");
        assertThat(r.resultados()).hasSize(29);
        assertThat(r.arg("ProcesarIngresoEgresoTesoreria", 0).getAsJsonArray("p_Solicitudes")).as("TRA: ids 5, 10, 15, 20, 25, 30").hasSize(6);
        assertThat(r.llamadas("ProcesarIngresoEgresoTesoreria")).hasSize(1);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- what Certificar uses
    @Test
    @DisplayName("FM-CER crearAporteReinversion (C:2385-2442, 762-787): the aporte exactly as Certificar builds it; the output row has what Certificar reads: IdInterno, IdSolicitudAporteRescate, IdTipoCanalIngreso and a non null Switch; the folio of the rescue is kept")
    void certificar_unAporte() throws Exception {
        CorridaCsr r = correr("certificar-un-aporte");
        assertThat(r.decision()).isEqualTo("COMMIT");
        JsonObject res = r.resultado();
        assertThat(n(res, "IdInterno")).isEqualByComparingTo("1");
        assertThat(n(res, "IdSolicitudAporteRescate")).isEqualByComparingTo("11413788");
        assertThat(s(res, "IdTipoCanalIngreso")).isEqualTo("AGE");
        assertThat(s(res, "Switch")).isEqualTo("N");
        assertThat(n(r.fila(), "FolioContrato")).isEqualByComparingTo("7000");
        assertThat(s(r.fila(), "FechaOriginalInversion")).isEqualTo("2026-02-27");
        assertThat(s(r.fila(), "Glosa")).isEmpty();
        assertThat(n(r.fila(), "IdSolicitudAporteRescateRelacionado")).isEqualByComparingTo("9001");
    }

    @Test
    @DisplayName("FM-CER (C:780-787): a switch aporte comes back with Switch S (Certificar adds it to aportesSwitch with its IdSolicitudAporteRescate and channel); the dates are those of the switch")
    void certificar_aporteSwitch() throws Exception {
        CorridaCsr r = correr("certificar-aporte-switch");
        assertThat(s(r.resultado(), "Switch")).isEqualTo("S");
        assertThat(n(r.resultado(), "IdSolicitudAporteRescate")).isEqualByComparingTo("11413788");
        assertThat(s(r.resultado(), "IdTipoCanalIngreso")).isEqualTo("AGE");
        assertThat(s(r.fila(), "EstadoLiquidacion")).isEqualTo("LIQ");
    }

    @Test
    @DisplayName("FM-CER (C:2436-2447) / FM-REI-63: the Spot dates Certificar sends (USD: FechaCertificacion and FechaLiquidacion of the rescue; CLP: FechaAceptacion twice) reach Spot as the legacy reads them")
    void certificar_fechasDeSpot() throws Exception {
        JsonObject usd = correr("certificar-aporte-usd-a-serie-clp-con-spot").arg("ProcesarIngresoOperacionSpot", 0);
        assertThat(formas(usd, 0, "FechaLiquidacion")).startsWith("2026-03-04");
        assertThat(usd.getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject().get("FechaCertificacion").getAsString()).startsWith("2026-03-03");
        JsonObject clp = correr("certificar-aporte-clp-a-serie-usd-con-spot").arg("ProcesarIngresoOperacionSpot", 0);
        assertThat(formas(clp, 0, "FechaLiquidacion")).startsWith("2026-03-02");
        assertThat(clp.getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject().get("FechaCertificacion").getAsString()).startsWith("2026-03-02");
    }

    @Test
    @DisplayName("FM-CER (C:2452-2460, 2539-2565): three aportes of one rescue: three results with the ids Certificar maps back, folio 7000 kept, one lot; a rejection comes back with the IdInterno of the aporte and the other aporte is still answered")
    void certificar_variosAportesYRechazos() throws Exception {
        CorridaCsr tres = correr("certificar-tres-aportes-de-un-rescate");
        assertThat(tres.resultados().stream().map(x -> n(x, "IdInterno").intValue()).toList()).containsExactly(1, 2, 3);
        assertThat(tres.secuencias()).containsExactly("SolicitudAporteRescate:3", "FolioContrato:3");
        CorridaCsr rechazo = correr("certificar-rechazo-por-requisitos-vuelve-con-su-idinterno");
        assertThat(rechazo.mensajesIdCodigo()).containsExactly("1:REGIMEN_NO_PERMITE_APORTE");
        assertThat(rechazo.resultados()).isEmpty();
        CorridaCsr uno = correr("certificar-lote-con-un-rechazo");
        assertThat(uno.mensajesIdCodigo()).containsExactly("2:REGIMEN_NO_PERMITE_APORTE");
        assertThat(uno.resultados()).hasSize(1);
        assertThat(n(uno.resultado(), "IdInterno")).isEqualByComparingTo("1");
        assertThat(uno.decision()).isEqualTo("ROLLBACK");
    }

    @Test
    @DisplayName("FM-CER (C:2429-2434, 2543-2545): the channel of the rescue wins over AGE, IngresoAutomatico is copied, MontoTotalInversion drives the parity, the cost UF of Certificar is inherited by APV-A and by MLT/18Q; p_UsaReglasNegocio reaches Tesoreria")
    void certificar_camposDelAporte() throws Exception {
        assertThat(s(correr("certificar-canal-del-rescate-gana").fila(), "IdTipoCanalIngreso")).isEqualTo("OPT");
        assertThat(s(correr("certificar-ingreso-automatico-s").fila(), "IngresoAutomatico")).isEqualTo("S");
        CorridaCsr total = correr("certificar-monto-total-de-inversion-distinto");
        assertThat(total.arg("CalcularParidad", 0).getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject().get("MontoOrigen").getAsBigDecimal()).isEqualByComparingTo("100000");
        assertThat(n(total.fila(), "Monto")).isEqualByComparingTo("50000");
        assertThat(n(correr("certificar-portafolio-apv-hereda-costouf").fila(), "CostoUF")).isEqualByComparingTo("0.12345679");
        assertThat(n(correr("certificar-mlt-18q-hereda").fila(), "CostoUF")).isEqualByComparingTo("0.25");
        CorridaCsr reglas = correr("certificar-usa-reglas-de-negocio-n");
        assertThat(reglas.arg("ProcesarIngresoEgresoTesoreria", 0).get("p_UsaReglasNegocio").getAsString()).isEqualTo("N");
        assertThat(s(correr("certificar-fecha-emision-fecha-certificacion-del-rescate").fila(), "FechaCertificacion")).as("FechaEmision Friday 2026-03-06").isEqualTo("2026-03-09");
        CorridaCsr sim = correr("certificar-simulacion-siempre-n");
        assertThat(sim.filas()).isEmpty();
    }

    @Test
    @DisplayName("FM-REI-61/63/67: malformed answers of the collaborators: a RECH of Tesoreria without Campo is a NullPointerException; a Spot operation without IdInterno a NumberFormatException; a repeated Spot id applies both answers (the last IdOperacionSpot stays); two ERROR messages count twice and cut")
    void fmRei63_respuestasMalFormadasYRepetidas() throws Exception {
        assertThat(correr("tesoreria-rech-sin-campo-npe").claseExcepcion()).isEqualTo("NullPointerException");
        CorridaCsr sinId = correr("spot-operacion-sin-idinterno");
        assertThat(sinId.claseExcepcion()).isEqualTo("NumberFormatException");
        assertThat(sinId.filas()).isEmpty();
        CorridaCsr repetida = correr("spot-operacion-repetida-gana-la-ultima");
        assertThat(n(repetida.fila(), "IdOperacionSpot")).isEqualByComparingTo("32");
        CorridaCsr dos = correr("spot-dos-mensajes-error");
        assertThat(dos.contador()).isEqualTo(2);
        assertThat(dos.mensajesIdCodigo()).containsExactly("0:E1", "0:E2");
        assertThat(dos.mensajes().get(1).get("descripcion").getAsString()).isEqualTo("SPT : dos");
        assertThat(dos.filas()).isEmpty();
    }
}
