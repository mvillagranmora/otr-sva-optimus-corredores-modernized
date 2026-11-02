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
 * The rule cards FM-REI-41..68 (RULES_FONDOMUTUO_F2_REINVERSION.md 8.2) and the defects of {@code CrearSolicitudAporteReinversion} as readable assertions: each test runs ONE case of the
 * matrix (the new code, every gate open) and states what the legacy did, in the name and in the assertions. The golden is the authority (CsrEquivalenciaTest compares everything byte by
 * byte); these tests are the index: they name the rule or defect that each case pins and they are the tests that read well when a case turns red. Every assertion was checked against the
 * RECORDED golden (run the class with {@code -Dcsr.autocomprobacion=true}: it reads the goldens instead of the production class) before any production code existed.
 *
 * <p>RED BEFORE CODE: every test fails with "CSR not implemented: the production class ... does not exist yet" until {@code cl.bcs.fondomutuo.aportereinversion} is written.
 */
class CsrReglasTest {

    private static void sinEscrituras(CorridaCsr r) {
        assertThat(r.filas()).as("no row APOREINV written").isEmpty();
        assertThat(r.fechasFinales()).as("FechaFinalReinversion untouched").containsExactly("2026-03-02", "2026-03-02", "2026-03-02");
        assertThat(r.llamadas("AgregarLog")).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-41
    @Test
    @DisplayName("FM-REI-41/52/62/64/65 (C52): real, one CIN aporte: INSERT APOREINV, FechaFinalReinversion, one AgregarLog, sequences asked once each")
    void fmRei41_realUnAporteCin() throws Exception {
        CorridaCsr r = correr("real-un-aporte-nacional-cin");
        assertThat(r.decision()).isEqualTo("COMMIT");
        assertThat(r.contador()).isZero();
        assertThat(r.mensajes()).isEmpty();
        assertThat(r.resultados()).hasSize(1);
        JsonObject f = r.fila();
        assertThat(s(f, "TipoSolicitud")).isEqualTo("APO");
        assertThat(s(f, "IdTipoMovimientoFondoMutuo")).isEqualTo("APOREINV");
        assertThat(s(f, "Estado")).isEqualTo("ING");
        assertThat(s(f, "EstadoLiquidacion")).isEqualTo("ING");
        assertThat(s(f, "Overnight")).isEqualTo("N");
        assertThat(s(f, "CalculoPorMontoCuota")).isEqualTo("M");
        assertThat(s(f, "EsPorMontoLiquido")).isEqualTo("N");
        assertThat(n(f, "IdSolicitudAporteRescate")).isEqualByComparingTo("11413788");
        assertThat(n(f, "FolioContrato")).isEqualByComparingTo("100049222");
        assertThat(s(f, "Visado")).isEqualTo("N");
        assertThat(n(f, "Monto")).isEqualByComparingTo("100000");
        assertThat(n(f, "NumeroCuotas")).isEqualByComparingTo("83.2986");
        assertThat(n(f, "ValorCuota")).isEqualByComparingTo("1200.5");
        assertThat(s(f, "IdMonedaPago")).isEqualTo("CLP");
        assertThat(s(f, "FechaEmision")).isEqualTo("2026-03-02");
        assertThat(s(f, "FechaOriginalInversion")).isEqualTo("2026-01-15");
        assertThat(s(f, "FechaAceptacion")).isEqualTo("2026-03-02");
        assertThat(s(f, "FechaCertificacion")).isEqualTo("2026-03-03");
        assertThat(s(f, "FechaLiquidacion")).isEqualTo("2026-03-03");
        assertThat(s(f, "FechaValorCuota")).isEqualTo("2026-03-04");
        assertThat(n(f, "IdIngresoEgreso")).isZero();
        assertThat(r.fechasFinales()).containsExactly("2026-03-03", "2026-03-02", "2026-03-02");
        assertThat(r.secuencias()).containsExactly("SolicitudAporteRescate:1", "FolioContrato:1");
        assertThat(r.llamadas("AgregarLog")).hasSize(1);
        assertThat(r.llamo("ProcesarIngresoEgresoTesoreria")).isFalse();
        assertThat(r.llamo("ProcesarIngresoOperacionSpot")).isFalse();
    }

    @Test
    @DisplayName("FM-REI-41/52 (C52): the output row (ResultadoSolicitud) carries the series, the client and the origin: what Certificar and the screens read")
    void fmRei41_resultadoCompleto() throws Exception {
        JsonObject res = correr("real-un-aporte-nacional-cin").resultado();
        assertThat(n(res, "IdInterno")).isEqualByComparingTo("1");
        assertThat(n(res, "IdSolicitudAporteRescate")).isEqualByComparingTo("11413788");
        assertThat(s(res, "IdSerieFondo")).isEqualTo("FM002A");
        assertThat(s(res, "IdFondo")).isEqualTo("11");
        assertThat(s(res, "Nemotecnico")).isEqualTo("FM002");
        assertThat(s(res, "NombreFondo")).isEqualTo("Fondo FM002A");
        assertThat(s(res, "NombreSerieFondo")).isEqualTo("Serie FM002A");
        assertThat(s(res, "CodigoSerie")).isEqualTo("A");
        assertThat(s(res, "IdEmisorDestino")).isEqualTo("AGF2");
        assertThat(s(res, "IdEmisorOrigen")).isEqualTo("AGF1");
        assertThat(s(res, "IdSerieFondoOrigen")).isEqualTo("FM001A");
        assertThat(s(res, "DocumentoIdentidad")).isEqualTo("11111111-1");
        assertThat(s(res, "CodigoPortafolio")).isEqualTo("PF500");
        assertThat(s(res, "NombreCliente")).isEqualTo("Cliente Ficticio Uno");
        assertThat(n(res, "MontoTransferencia")).isEqualByComparingTo("100000");
        assertThat(n(res, "NumeroCuotaTransferencia")).isEqualByComparingTo("83.2986");
        assertThat(s(res, "FechaIngresoRegistro")).isEqualTo("2026-03-02 14:30:00.000");
        assertThat(s(res, "Switch")).isEqualTo("N");
        assertThat(s(res, "IdTipoCanalIngreso")).isEqualTo("AGE");
    }

    @Test
    @DisplayName("FM-REI-66 (C51): simulation: no INSERT, no UPDATE, no AGF/log/Tesoreria/Spot; SaldoFlujoCaja is called anyway (D-F2-13); the result has the pending request")
    void fmRei66_simulacion() throws Exception {
        CorridaCsr r = correr("simulacion-un-aporte");
        assertThat(r.decision()).isEqualTo("COMMIT");
        assertThat(r.resultados()).hasSize(1);
        sinEscrituras(r);
        assertThat(r.llamo("SaldoFlujoCaja")).isTrue();
        assertThat(r.secuencias()).as("the sequences are still consumed").containsExactly("SolicitudAporteRescate:1", "FolioContrato:1");
    }

    @Test
    @DisplayName("FM-REI-66: p_SimularOperacion null or absent is a REAL pass; lower case 's' simulates (equalsIgnoreCase)")
    void fmRei66_simularNuloEsRealYMinusculaSimula() throws Exception {
        assertThat(correr("simulacion-nula-es-real").filas()).hasSize(1);
        assertThat(correr("simulacion-ausente-es-real").filas()).hasSize(1);
        sinEscrituras(correr("simulacion-minuscula-simula"));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-42
    @Test
    @DisplayName("FM-REI-42 (C60): a missing APORTE_GENERA_COMPRA_SPOT_DOLAR (or PLA.MonedaSistema, PLA.FechaSistema) raises the parameter exception; the first two BEFORE any collaborator or read")
    void fmRei42_parametrosFaltantes() throws Exception {
        CorridaCsr spot = correr("falta-parametro-spot");
        assertThat(spot.decision()).isEqualTo("ROLLBACK_HTTP500");
        assertThat(spot.excepcion()).as("a plain Exception in the legacy: the message is what counts").contains("APORTE_GENERA_COMPRA_SPOT_DOLAR").contains("FONDOMUTUO");
        assertThat(spot.orden()).isEmpty();
        CorridaCsr moneda = correr("falta-moneda-sistema");
        assertThat(moneda.excepcion()).contains("PLA.MonedaSistema");
        assertThat(moneda.orden()).isEmpty();
        CorridaCsr fecha = correr("falta-fecha-sistema");
        assertThat(fecha.excepcion()).contains("PLA.FechaSistema");
        assertThat(fecha.orden()).as("the basic requirements and the IdInterno checks ran first, no collaborator was reached").isEmpty();
    }

    @Test
    @DisplayName("FM-REI-42 (C61): RevisarRequisitosBasicos with an error: counter 1, the messages of the kit, nothing else read")
    void fmRei42_requisitosBasicos() throws Exception {
        CorridaCsr u = correr("requisitos-basicos-usuario-inexistente");
        assertThat(u.decision()).isEqualTo("ROLLBACK");
        assertThat(u.contador()).isEqualTo(1);
        assertThat(u.mensajesIdCodigo()).containsExactly("0:USUARIO_NO_EXISTE");
        assertThat(u.orden()).isEmpty();
        assertThat(correr("requisitos-basicos-modulo-inexistente").mensajesIdCodigo()).containsExactly("0:MODULO_NO_EXISTE");
    }

    @Test
    @DisplayName("FM-REI-42 (C61): empty list, IdInterno 0 and repeated IdInterno: counter 1, one message of id 0, return; null IdInterno: NullPointerException")
    void fmRei42_validacionesDeIdInterno() throws Exception {
        assertThat(correr("lista-vacia").mensajesIdCodigo()).containsExactly("0:NO_SE_INGRESARON_SOLICITUDES_APORTE");
        assertThat(correr("idinterno-cero").mensajesIdCodigo()).containsExactly("0:ID_INTERNO_NO_CERO");
        assertThat(correr("idinterno-repetido").mensajesIdCodigo()).containsExactly("0:CORRELATIVO_INCORRECTO");
        for (String slug : List.of("lista-vacia", "idinterno-cero", "idinterno-repetido")) {
            CorridaCsr r = correr(slug);
            assertThat(r.contador()).as(slug).isEqualTo(1);
            assertThat(r.decision()).as(slug).isEqualTo("ROLLBACK");
            assertThat(r.orden()).as(slug).isEmpty();
            assertThat(r.secuencias()).as(slug).isEmpty();
        }
        CorridaCsr nulo = correr("idinterno-nulo-npe");
        assertThat(nulo.decision()).isEqualTo("ROLLBACK_HTTP500");
        assertThat(nulo.claseExcepcion()).isEqualTo("NullPointerException");
    }

    @Test
    @DisplayName("FM-REI-42/48: RespetaValutasCargaMasiva absent = N (ADVERTENCIA only): a batch aporte takes the dates of the batch load (all today, no valutas); S follows the normal valutas")
    void fmRei42_respetaValutasCargaMasiva() throws Exception {
        for (String slug : List.of("respeta-valutas-ausente-es-n", "batch-valutas-cero")) {
            JsonObject f = correr(slug).fila();
            assertThat(s(f, "FechaCertificacion")).as(slug).isEqualTo("2026-03-02");
            assertThat(s(f, "FechaLiquidacion")).as(slug).isEqualTo("2026-03-02");
            assertThat(s(f, "FechaValorCuota")).as(slug).isEqualTo("2026-03-02");
            assertThat(s(f, "SolicitudBatch")).as(slug).isEqualTo("S");
        }
        JsonObject s = correr("batch-respeta-valutas").fila();
        assertThat(s(s, "FechaCertificacion")).isEqualTo("2026-03-03");
        assertThat(s(s, "FechaValorCuota")).isEqualTo("2026-03-04");
    }

    @Test
    @DisplayName("FM-REI-48/62: SolicitudBatch 's' lower case: counted as batch by equalsIgnoreCase but NOT by the exact comparison of the dates (the normal valutas are the batch ones here)")
    void fmRei48_batchMinuscula() throws Exception {
        CorridaCsr r = correr("batch-minuscula-no-es-batch");
        assertThat(s(r.fila(), "SolicitudBatch")).isEqualTo("s");
        assertThat(s(r.fila(), "FechaCertificacion")).isEqualTo("2026-03-02");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-44
    @Test
    @DisplayName("FM-REI-44 (C62), D-F2-19: a user without profile with a FechaEmision in the first row: PERFIL_INCORRECTO, counter 1 (fixed), return true; nothing read afterwards")
    void fmRei44_perfilAusenteRechaza() throws Exception {
        CorridaCsr r = correr("perfil-ausente-con-fechaemision");
        assertThat(r.decision()).isEqualTo("ROLLBACK");
        assertThat(r.contador()).isEqualTo(1);
        assertThat(r.mensajesIdCodigo()).containsExactly("0:PERFIL_INCORRECTO");
        assertThat(r.orden()).as("only the reads before the profile: no parity, no prices").containsExactly("ConsultarEmpresa");
    }

    @Test
    @DisplayName("FM-REI-44, D-F2-19: the profile is NOT demanded when the first row has no FechaEmision, or when every row is batch")
    void fmRei44_perfilAusenteNoRechazaSinFechaEmisionOTodoBatch() throws Exception {
        CorridaCsr sinFecha = correr("perfil-ausente-sin-fechaemision");
        assertThat(sinFecha.decision()).isEqualTo("COMMIT");
        assertThat(n(sinFecha.fila(), "IdUsuario")).isEqualByComparingTo("9");
        CorridaCsr batch = correr("perfil-ausente-todo-batch");
        assertThat(batch.decision()).isEqualTo("COMMIT");
        assertThat(s(batch.fila(), "SolicitudBatch")).isEqualTo("S");
    }

    @Test
    @DisplayName("FM-REI-44/48/53 (D-F2-21): the state of the fund follows the profile of the user: closed for the commercial user warns FONDO_CERRADO and (Overnight S) FECHA_LIQUIDEZ_CERRADO; closed only for back office does not warn a commercial user")
    void fmRei44_estadoDelFondoPorPerfil() throws Exception {
        assertThat(correr("perfil-comercial-fondo-cerrado").mensajesIdCodigo()).containsExactly("1:FONDO_CERRADO_REQ_APO_RES", "1:FECHA_LIQUIDEZ_CERRADO");
        assertThat(correr("perfil-comercial-ignora-backoffice").mensajes()).isEmpty();
        CorridaCsr back = correr("perfil-backoffice");
        assertThat(back.mensajesIdCodigo()).containsExactly("1:FONDO_CERRADO_REQ_APO_RES", "1:FECHA_LIQUIDEZ_CERRADO");
        assertThat(n(back.fila(), "IdUsuario")).isEqualByComparingTo("8");
        assertThat(back.contador()).as("warnings do not count").isZero();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-52
    @Test
    @DisplayName("FM-REI-52 (C83): the channel is the Canal of the aporte, else p_IdTipoCanalIngreso (no default AGE); an empty Canal is used as it is")
    void fmRei52_canal() throws Exception {
        assertThat(s(correr("canal-de-la-peticion").fila(), "IdTipoCanalIngreso")).isEqualTo("OPT");
        assertThat(s(correr("canal-del-aporte").fila(), "IdTipoCanalIngreso")).isEqualTo("WEB");
        assertThat(s(correr("canal-vacio-del-aporte").fila(), "IdTipoCanalIngreso")).isEmpty();
        assertThat(s(correr("canal-del-aporte").resultado(), "IdTipoCanalIngreso")).isEqualTo("WEB");
    }

    @Test
    @DisplayName("FM-REI-52 (C83): no channel at all: the INSERT fails (NOT NULL) AFTER the sequences and the parity reads; in simulation the result just has a null channel")
    void fmRei52_canalNuloSinDefecto() throws Exception {
        CorridaCsr r = correr("canal-nulo-sin-defecto");
        assertThat(r.decision()).isEqualTo("ROLLBACK_HTTP500");
        assertThat(r.claseExcepcion()).isEqualTo("SQLException");
        assertThat(r.llamo("SaldoFlujoCaja")).isTrue();
        assertThat(r.llamo("AgregarLog")).isFalse();
        CorridaCsr sim = correr("canal-nulo-simulacion");
        assertThat(sim.decision()).isEqualTo("COMMIT");
        assertThat(s(sim.resultado(), "IdTipoCanalIngreso")).isNull();
    }

    @Test
    @DisplayName("FM-REI-41/52: only the mandatory fields: the defaults of the contract (Switch N, IdAgrupador 0, Porcentaje 100 ...) and FechaEmision null -> the FechaFondo of the series")
    void fmRei41_defaultsDelContrato() throws Exception {
        CorridaCsr r = correr("defaults-del-contrato");
        assertThat(r.decision()).isEqualTo("COMMIT");
        assertThat(s(r.fila(), "Switch")).isEqualTo("N");
        assertThat(n(r.fila(), "IdAgrupador")).isZero();
        assertThat(s(r.fila(), "IngresoAutomatico")).isEqualTo("N");
        assertThat(s(r.fila(), "FechaEmision")).isEqualTo("2026-03-02");
        assertThat(n(r.fila(), "CostoUF")).as("computed (CostoUF 0 of the input is only used when it is inherited)").isEqualByComparingTo("38.5");
    }

    @Test
    @DisplayName("FM-REI-52/67: FolioContrato absent -> NullPointerException after the sequences were asked; a folio > 0 is kept (the sequence is asked for the whole lot anyway)")
    void fmRei52_folio() throws Exception {
        CorridaCsr nulo = correr("folio-nulo-npe");
        assertThat(nulo.claseExcepcion()).isEqualTo("NullPointerException");
        assertThat(nulo.secuencias()).containsExactly("SolicitudAporteRescate:1", "FolioContrato:1");
        CorridaCsr dado = correr("folio-informado");
        assertThat(n(dado.fila(), "FolioContrato")).isEqualByComparingTo("555");
        assertThat(dado.secuencias()).containsExactly("SolicitudAporteRescate:1", "FolioContrato:1");
    }

    @Test
    @DisplayName("FM-REI-49/52/64 (D-F2-13): IdSolicitudAporteRescateRelacionado null: the request keeps 0, the CIN balance IS validated (related 0) and an empty balance rejects")
    void fmRei52_relacionadoNulo() throws Exception {
        CorridaCsr r = correr("relacionado-nulo");
        assertThat(n(r.fila(), "IdSolicitudAporteRescateRelacionado")).isZero();
        assertThat(r.decision()).isEqualTo("COMMIT");
    }

    @Test
    @DisplayName("FM-REI-52/67: FechaOrigenInversion absent or badly formed: an exception while building the request (NullPointerException / DateTimeParseException), nothing written")
    void fmRei52_fechaOrigenInvalida() throws Exception {
        assertThat(correr("fechaorigen-nula-npe").claseExcepcion()).isEqualTo("NullPointerException");
        assertThat(correr("fechaorigen-mal-formada").claseExcepcion()).isEqualTo("DateTimeParseException");
        sinEscrituras(correr("fechaorigen-mal-formada"));
    }

    @Test
    @DisplayName("FM-REI-41/49 (C82): FechaValorCuota as java.sql.Date in the plain Gson format 'Mar 9, 2026' is used for the price and kept; the ISO text is a JsonSyntaxException before the logic")
    void fmRei41_fechaValorCuotaDeLaEntrada() throws Exception {
        CorridaCsr ok = correr("fechavalorcuota-de-la-entrada");
        assertThat(s(ok.fila(), "FechaValorCuota")).as("the input date, not the calculated one (2026-03-04)").isEqualTo("2026-03-09");
        assertThat(ok.arg("EntregarPreciosTasas", 0).getAsJsonArray("p_DatosInstrumentos").get(0).getAsJsonObject().get("FechaConsulta").getAsString()).startsWith("2026-03-09");
        CorridaCsr iso = correr("fechavalorcuota-iso-falla-gson");
        assertThat(iso.claseExcepcion()).isEqualTo("JsonSyntaxException");
        assertThat(iso.orden()).isEmpty();
    }

    @Test
    @DisplayName("FM-REI-41/42/48: ConsultarEmpresa answers country 840: the custody flow and the dates use the country of the company; FechaEmision null uses the FechaFondo")
    void fmRei42_paisDeLaEmpresa() throws Exception {
        CorridaCsr r = correr("empresa-otro-pais");
        assertThat(r.decision()).isEqualTo("COMMIT");
        assertThat(correr("fechaemision-nula-usa-fechafondo").decision()).isEqualTo("COMMIT");
    }

    @Test
    @DisplayName("FM-REI-41/51/67 (D-NOR-9): absent IdMoneda / Monto: NullPointerException after the first parity read; absent medio de pago = not CIN (goes to Tesoreria, the INSERT then fails on the NOT NULL column)")
    void fmRei41_camposNulos() throws Exception {
        assertThat(correr("moneda-nula-npe").claseExcepcion()).isEqualTo("NullPointerException");
        assertThat(correr("monto-nulo").claseExcepcion()).isEqualTo("NullPointerException");
        CorridaCsr medio = correr("medio-de-pago-nulo-es-no-cin");
        assertThat(medio.llamo("ProcesarIngresoEgresoTesoreria")).isTrue();
        assertThat(medio.llamo("SaldoFlujoCaja")).isFalse();
        assertThat(medio.claseExcepcion()).isEqualTo("SQLException");
    }

    @Test
    @DisplayName("FM-REI-45/53/62/52: absent IdRegimenTributario (no regime row for the series: two errors), absent SolicitudBatch (not batch, not AGF: the INSERT fails on the NOT NULL column), null IdAgrupador (INSERT fails)")
    void fmRei45_regimenYBatchNulos() throws Exception {
        CorridaCsr regimen = correr("regimen-nulo");
        assertThat(regimen.mensajesIdCodigo()).containsExactly("1:PORTAFOLIO_REGIMEN_INVALIDO", "1:REGIMEN_NO_EXISTE_REQ_APO_RES");
        assertThat(regimen.contador()).isEqualTo(2);
        assertThat(correr("solicitudbatch-nulo").claseExcepcion()).isEqualTo("SQLException");
        assertThat(correr("agrupador-nulo-falla-insert").claseExcepcion()).isEqualTo("SQLException");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-46/47/48
    @Test
    @DisplayName("FM-REI-46: CalcularParidad (trabajo 1) with IdTrabajo 1, one line per aporte, origin currency of the aporte, destination the system currency, the MontoTotalInversion when it is not 0 (else Monto)")
    void fmRei46_paridadAlMontoTotalDeInversion() throws Exception {
        JsonObject a = correr("real-un-aporte-nacional-cin").arg("CalcularParidad", 0);
        assertThat(a.get("p_IdEmpresa").getAsLong()).isEqualTo(1);
        assertThat(a.get("p_IdTrabajo").getAsLong()).isEqualTo(1);
        JsonObject l = a.getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject();
        assertThat(l.get("IdTrabajo").getAsLong()).isEqualTo(1);
        assertThat(l.get("IdInterno").getAsLong()).isEqualTo(1);
        assertThat(l.get("Fecha").getAsString()).isEqualTo("2026-03-02");
        assertThat(l.get("IdMonedaOrigen").getAsString()).isEqualTo("CLP");
        assertThat(l.get("IdMonedaDestino").getAsString()).isEqualTo("CLP");
        assertThat(l.get("ValorCambioLocalModificado").getAsBigDecimal()).isEqualByComparingTo("0");
        assertThat(l.get("MontoOrigen").getAsBigDecimal()).isEqualByComparingTo("100000");
        assertThat(l.get("IdUso").getAsString()).isEmpty();
        JsonObject total = correr("paridad-monto-total-de-inversion").arg("CalcularParidad", 0).getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject();
        assertThat(total.get("MontoOrigen").getAsBigDecimal()).isEqualByComparingTo("250000");
        JsonObject usd = correr("paridad-moneda-usd").arg("CalcularParidad", 0).getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject();
        assertThat(usd.get("IdMonedaOrigen").getAsString()).isEqualTo("USD");
        assertThat(usd.get("IdMonedaDestino").getAsString()).isEqualTo("CLP");
        assertThat(correr("paridad-trabajo-1-vacia").decision()).as("a parity without line for the id: ValidaMontoInversion gets Monto 0").isEqualTo("COMMIT");
    }

    @Test
    @DisplayName("FM-REI-47/48 (C66): SWITCH (foreign, same issuer): retention 2, valuta cuota 1, certification 2, curse 0; INTERNACIONAL (other issuer): retention 4; NACIONAL: the days of the destination series")
    void fmRei47_tipoDeReinversionYValutas() throws Exception {
        JsonObject sw = correr("tipo-switch-mismo-emisor").fila();
        assertThat(s(sw, "FechaValorCuota")).isEqualTo("2026-03-03");
        assertThat(s(sw, "FechaCertificacion")).isEqualTo("2026-03-04");
        assertThat(s(sw, "FechaLiquidacion")).isEqualTo("2026-03-04");
        JsonObject in = correr("tipo-internacional-otro-emisor").fila();
        assertThat(s(in, "FechaCertificacion")).isEqualTo("2026-03-04");
        assertThat(s(in, "FechaLiquidacion")).isEqualTo("2026-03-06");
        JsonObject nacExt = correr("tipo-nacional-extranjero-a-nacional").fila();
        assertThat(s(nacExt, "FechaValorCuota")).isEqualTo("2026-03-04");
        assertThat(s(nacExt, "FechaCertificacion")).isEqualTo("2026-03-03");
        JsonObject nacNac = correr("tipo-nacional-nacional-a-extranjero").fila();
        assertThat(s(nacNac, "FechaValorCuota")).isEqualTo("2026-03-03");
        assertThat(s(nacNac, "FechaCertificacion")).isEqualTo("2026-03-04");
        assertThat(s(nacNac, "FechaLiquidacion")).isEqualTo("2026-03-03");
    }

    @Test
    @DisplayName("FM-REI-47 (C65), D-SQL-1: VALUTASNACUSD only by the third rule (foreign dollar issuer -> national dollar issuer: curse 0 days); the two dead rules (EmisorFondoNacionalPesos is always N) leave NACIONAL")
    void fmRei47_valutasNacUsd() throws Exception {
        JsonObject viva = correr("tipo-valutasnacusd-tercera-rama").fila();
        assertThat(s(viva, "FechaCertificacion")).isEqualTo("2026-03-02");
        assertThat(s(viva, "FechaLiquidacion")).isEqualTo("2026-03-02");
        JsonObject muertaPesosADolar = correr("tipo-valutasnacusd-rama-muerta-pesos-a-dolar").fila();
        assertThat(s(muertaPesosADolar, "FechaCertificacion")).isEqualTo("2026-03-02");
        JsonObject muertaDolarAPesos = correr("tipo-valutasnacusd-rama-muerta-dolar-a-pesos").fila();
        assertThat(s(muertaDolarAPesos, "FechaCertificacion")).isEqualTo("2026-03-03");
        assertThat(s(muertaDolarAPesos, "FechaValorCuota")).isEqualTo("2026-03-04");
        JsonObject dolarADolar = correr("tipo-nacional-dolar-a-dolar").fila();
        assertThat(s(dolarADolar, "FechaCertificacion")).isEqualTo("2026-03-02");
        JsonObject conCurse = correr("tipo-valutasnacusd-con-dias-de-curse").fila();
        assertThat(s(conCurse, "FechaAceptacion")).as("VALUTASNACUSD takes the curse days of the destination series (2), SWITCH / INTERNACIONAL take 0").isEqualTo("2026-03-04");
        assertThat(s(conCurse, "FechaValorCuota")).isEqualTo("2026-03-06");
        assertThat(s(conCurse, "FechaCertificacion")).isEqualTo("2026-03-04");
    }

    @Test
    @DisplayName("FM-REI-47/67 (D-NOR-9): origin series without row, unknown or destination unknown: NullPointerException when classifying (before the sequences for the destination, after for ... see golden); nothing written")
    void fmRei47_seriesInexistentes() throws Exception {
        for (String slug : List.of("origen-sin-serie-npe", "origen-desconocido-npe", "destino-sin-serie-npe")) {
            CorridaCsr r = correr(slug);
            assertThat(r.claseExcepcion()).as(slug).isEqualTo("NullPointerException");
            assertThat(r.decision()).as(slug).isEqualTo("ROLLBACK_HTTP500");
            assertThat(r.filas()).as(slug).isEmpty();
        }
        assertThat(correr("origen-sin-serie-npe").orden()).as("only the first parity read happened").containsExactly("ConsultarEmpresa", "CalcularParidad");
    }

    @Test
    @DisplayName("FM-REI-43/47/48: three aportes (SWITCH, INTERNACIONAL, NACIONAL) in one lot: three results, the sequences asked ONCE for three, three rows with their own valutas")
    void fmRei43_loteMixtoDeTipos() throws Exception {
        CorridaCsr r = correr("lote-mixto-de-tipos");
        assertThat(r.resultados()).hasSize(3);
        assertThat(r.filas()).hasSize(3);
        assertThat(r.secuencias()).containsExactly("SolicitudAporteRescate:3", "FolioContrato:3");
        assertThat(s(r.filas().get(0), "FechaLiquidacion")).isEqualTo("2026-03-04");
        assertThat(s(r.filas().get(1), "FechaLiquidacion")).isEqualTo("2026-03-06");
        assertThat(s(r.filas().get(2), "FechaLiquidacion")).isEqualTo("2026-03-03");
        assertThat(r.llamadas("AgregarLog")).hasSize(1);
        assertThat(r.arg("AgregarLog", 0).getAsJsonArray("p_Log")).hasSize(3);
    }

    @Test
    @DisplayName("FM-REI-48/52 (C84): Switch S with the origin dates of the rescue: the dates are those of the switch calculation, EstadoLiquidacion LIQ; Switch N: ING")
    void fmRei48_switchConFechasDeOrigen() throws Exception {
        JsonObject sw = correr("switch-s-con-fechas-de-origen").fila();
        assertThat(s(sw, "EstadoLiquidacion")).isEqualTo("LIQ");
        assertThat(s(sw, "Switch")).isEqualTo("S");
        assertThat(s(sw, "FechaAceptacion")).isEqualTo("2026-03-04");
        assertThat(s(sw, "FechaCertificacion")).isEqualTo("2026-03-04");
        assertThat(s(sw, "FechaValorCuota")).isEqualTo("2026-03-03");
        assertThat(s(correr("switch-n-explicito").fila(), "EstadoLiquidacion")).isEqualTo("ING");
    }

    @Test
    @DisplayName("FM-REI-48/52, H2 limit: Switch S (or 's') with the origin dates at their default 0001-01-01: the output carries 0001-01-01 and the table reads them back as 0000-12-30 (H2 vs HANA, documented)")
    void fmRei48_switchSinFechasDeOrigenDevuelveElCentinela() throws Exception {
        for (String slug : List.of("switch-s-sin-fechas-de-origen", "switch-minuscula")) {
            CorridaCsr r = correr(slug);
            assertThat(s(r.resultado(), "FechaCertificacion")).as(slug).isEqualTo("0001-01-01");
            assertThat(s(r.resultado(), "FechaAceptacion")).as(slug).isEqualTo("0001-01-01");
            assertThat(s(r.fila(), "EstadoLiquidacion")).as(slug).isEqualTo("LIQ");
        }
        assertThat(s(correr("switch-minuscula").fila(), "Switch")).isEqualTo("s");
    }

    @Test
    @DisplayName("FM-REI-52/67: Switch null (explicit) -> NullPointerException (solicitud.Switch.equalsIgnoreCase); Switch S in a batch row: LIQ and the batch dates")
    void fmRei52_switchNuloYEnBatch() throws Exception {
        assertThat(correr("switch-nulo-npe").claseExcepcion()).isEqualTo("NullPointerException");
        JsonObject b = correr("switch-s-en-batch").fila();
        assertThat(s(b, "EstadoLiquidacion")).isEqualTo("LIQ");
        assertThat(s(b, "FechaCertificacion")).isEqualTo("2026-03-02");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-49
    @Test
    @DisplayName("FM-REI-49 (D-CUO): amounts and quotas: Monto 123456.789 formats to 123457 and 102.8378 quotas; Monto 0: NUMERO_CUOTA_VALIDO and MONTO_VALIDO errors; informed NumeroCuotas is ignored (always by amount)")
    void fmRei49_montoYCuotas() throws Exception {
        JsonObject d = correr("monto-con-decimales").fila();
        assertThat(n(d, "Monto")).isEqualByComparingTo("123457");
        assertThat(n(d, "NumeroCuotas")).isEqualByComparingTo("102.8378");
        assertThat(n(d, "MontoOrigen")).isEqualByComparingTo("123456.789");
        CorridaCsr cero = correr("monto-cero");
        assertThat(cero.mensajesIdCodigo()).containsExactly("1:NUMERO_CUOTA_VALIDO_REQ_APO_RES", "1:MONTO_VALIDO");
        assertThat(n(correr("numerocuotas-informado").fila(), "NumeroCuotas")).isEqualByComparingTo("83.2986");
    }

    @Test
    @DisplayName("FM-REI-49, D-CUO: no price for the series (or EntregarPreciosTasas with an error): the AR ignores the messages of the calculation: amounts 0 and the validator rejects (VALOR_CUOTA_INCORRECTO)")
    void fmRei49_sinValorCuota() throws Exception {
        for (String slug : List.of("sin-valor-cuota", "precios-con-error")) {
            CorridaCsr r = correr(slug);
            assertThat(r.mensajesIdCodigo()).as(slug).containsExactly("1:NUMERO_CUOTA_VALIDO_REQ_APO_RES", "1:VALOR_CUOTA_INCORRECTO", "1:MONTO_VALIDO");
            assertThat(r.contador()).as(slug).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("FM-REI-49: the series of CalcularMontoNumeroCuota: date = FechaValorCuota calculated, amount mode M, UF adjustment, the currency of the fund and of the investment")
    void fmRei49_entradaDelCalculoDeCuota() throws Exception {
        CorridaCsr r = correr("real-un-aporte-nacional-cin");
        JsonObject p = r.arg("EntregarPreciosTasas", 0).getAsJsonArray("p_DatosInstrumentos").get(0).getAsJsonObject();
        assertThat(p.get("IdInstrumento").getAsLong()).isEqualTo(901);
        assertThat(p.get("Nemotecnico").getAsString()).isEqualTo("FM002A");
        assertThat(p.get("FechaConsulta").getAsString()).startsWith("2026-03-04");
        JsonObject uf = r.arg("CalcularParidad", 1);
        assertThat(uf.get("p_IdTrabajo").getAsLong()).isEqualTo(2);
        assertThat(uf.getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject().get("IdMonedaDestino").getAsString()).isEqualTo("UF");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- FM-REI-50
    @Test
    @DisplayName("FM-REI-50 (C64), D-F2-20: a normal portfolio CALCULATES the cost UF (vc.CostoEnFactorAjuste = 38.5)")
    void fmRei50_portafolioNormalCalcula() throws Exception {
        assertThat(n(correr("costouf-portafolio-normal-calcula").fila(), "CostoUF")).isEqualByComparingTo("38.5");
    }

    @Test
    @DisplayName("FM-REI-50 (C64), D-F2-20: portfolios APV-A, BF, DC-DL, DC-AL, DC-R (and apv-a in lower case: equalsIgnoreCase) INHERIT the CostoUF of the input (0.5); ValorUFFechaInversion is the factor in both cases")
    void fmRei50_portafoliosQueHeredan() throws Exception {
        for (String slug : List.of("costouf-portafolio-apv-a-hereda", "costouf-portafolio-bf-hereda", "costouf-portafolio-dc-dl-hereda", "costouf-portafolio-dc-al-hereda",
                "costouf-portafolio-dc-r-hereda", "costouf-portafolio-apv-a-minuscula-hereda")) {
            JsonObject f = correr(slug).fila();
            assertThat(n(f, "CostoUF")).as(slug).isEqualByComparingTo("0.5");
            assertThat(n(f, "ValorUFFechaInversion")).as(slug).isEqualByComparingTo("39000");
        }
    }

    @Test
    @DisplayName("FM-REI-50 (C64), D-F2-20: MLT S inherits only when EVERY regime of the lot and EVERY regime of the related rescues is 18Q (allMatch of an empty list is true: related 0 inherits); otherwise it calculates")
    void fmRei50_mltYRegimen18Q() throws Exception {
        assertThat(n(correr("costouf-mlt-s-todo-18q-hereda").fila(), "CostoUF")).isEqualByComparingTo("0.5");
        assertThat(n(correr("costouf-mlt-minuscula").fila(), "CostoUF")).as("equalsIgnoreCase").isEqualByComparingTo("0.5");
        assertThat(n(correr("costouf-mlt-s-sin-rescate-hereda-vacio").fila(), "CostoUF")).as("no related rescue row: allMatch(empty) = true").isEqualByComparingTo("0.5");
        assertThat(n(correr("costouf-mlt-s-rescate-gen-calcula").fila(), "CostoUF")).as("the related rescue is GEN").isEqualByComparingTo("38.5");
        assertThat(n(correr("costouf-mlt-n-18q-calcula").fila(), "CostoUF")).as("MLT N").isEqualByComparingTo("38.5");
        CorridaCsr lote = correr("costouf-mlt-lote-con-regimen-distinto-calcula");
        for (JsonObject f : lote.filas()) assertThat(n(f, "CostoUF")).as("the regime list is the one of the whole lot").isEqualByComparingTo("38.5");
    }

    @Test
    @DisplayName("FM-REI-50/53/67: portfolio without client row (999): NullPointerException on cliente.CodigoPortafolio; no value of the quota (vc null): the aporte is rejected before any inheritance")
    void fmRei50_clienteNuloYSinValorCuota() throws Exception {
        CorridaCsr nulo = correr("costouf-cliente-nulo-npe");
        assertThat(nulo.claseExcepcion()).isEqualTo("NullPointerException");
        assertThat(nulo.orden()).containsExactly("ConsultarEmpresa", "CalcularParidad", "ConsultarEmpresa", "EntregarPreciosTasas", "CalcularParidad");
        assertThat(correr("costouf-sin-calculo-vc-nulo").contador()).isEqualTo(3);
    }

    @Test
    @DisplayName("FM-REI-47: origin = destination series (no change-of-series check in the AR): NACIONAL, the request is created")
    void fmRei47_origenIgualADestino() throws Exception {
        CorridaCsr r = correr("serie-origen-igual-a-destino");
        assertThat(r.decision()).isEqualTo("COMMIT");
        assertThat(s(r.fila(), "IdSerieFondo")).isEqualTo("FM001A");
        assertThat(s(r.fila(), "IdSerieFondoOrigen")).isEqualTo("FM001A");
    }

    @Test
    @DisplayName("FM-REI-48: a non working day (Saturday 2026-03-07) or the eve of a holiday (2026-03-05) moves the valuta dates to the next working days; FechaFinalReinversion follows the certification date")
    void fmRei48_diasInhabilesYFeriados() throws Exception {
        CorridaCsr sab = correr("fechaemision-sabado");
        assertThat(s(sab.fila(), "FechaAceptacion")).isEqualTo("2026-03-07");
        assertThat(s(sab.fila(), "FechaCertificacion")).isEqualTo("2026-03-09");
        assertThat(s(sab.fila(), "FechaLiquidacion")).isEqualTo("2026-03-09");
        assertThat(s(sab.fila(), "FechaValorCuota")).isEqualTo("2026-03-10");
        assertThat(sab.fechasFinales().get(0)).isEqualTo("2026-03-09");
        CorridaCsr fer = correr("fechaemision-vispera-de-feriado");
        assertThat(s(fer.fila(), "FechaAceptacion")).isEqualTo("2026-03-04");
        assertThat(s(fer.fila(), "FechaCertificacion")).isEqualTo("2026-03-06");
        assertThat(s(fer.fila(), "FechaValorCuota")).isEqualTo("2026-03-09");
    }

    @Test
    @DisplayName("FM-REI-52/54: a client without executive: IdEmpleadoEjecutivo 0 in the request (null executive is not an error)")
    void fmRei52_clienteSinEjecutivo() throws Exception {
        CorridaCsr r = correr("cliente-sin-ejecutivo");
        assertThat(r.decision()).isEqualTo("COMMIT");
        assertThat(n(r.fila(), "IdEmpleadoEjecutivo")).isZero();
    }
}
