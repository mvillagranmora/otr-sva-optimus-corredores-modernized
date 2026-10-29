package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.LiquidarComprobante} (entry point call(conn, json)) as {@link LiquidarComprobanteService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-100 .. TES-A-164} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class LiquidarComprobanteServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-100_liq-ingreso-simple-liquida-comprobante-detalle-y-forma-de-pago",
            "TES-A-101_liq-egreso-simple-liquida",
            "TES-A-102_liq-comprobante-inexistente-es-error",
            "TES-A-103_liq-comprobante-bloqueado-es-error",
            "TES-A-104_liq-comprobante-anulado-es-error",
            "TES-A-105_liq-comprobante-modificado-es-error",
            "TES-A-106_liq-comprobante-ya-liquidado-es-error",
            "TES-A-107_liq-comprobante-sin-forma-de-pago-es-error",
            "TES-A-108_liq-forma-de-pago-menor-al-monto-del-comprobante-es-error",
            "TES-A-109_liq-forma-de-pago-mayor-al-monto-del-comprobante-es-error",
            "TES-A-110_liq-comprobante-anulado-con-forma-de-pago-viva-reporta-ambos-errores",
            "TES-A-111_liq-forma-de-pago-en-nomina-de-deposito-sin-liquidar-es-error",
            "TES-A-112_liq-forma-de-pago-en-nomina-con-deposito-liquidado-se-liquida",
            "TES-A-113_liq-transferencia-sin-cuenta-bancaria-de-la-corredora-es-error",
            "TES-A-114_liq-efectivo-no-requiere-cuenta-bancaria-de-la-corredora",
            "TES-A-115_liq-egreso-por-deposito-sin-banco-del-socio-es-error-si-el-parametro-lo-exige",
            "TES-A-116_liq-egreso-por-deposito-con-banco-del-socio-se-liquida",
            "TES-A-117_liq-partida-de-egreso-no-exige-banco-del-socio",
            "TES-A-118_liq-cheque-sin-depositar-ni-certificar-es-error",
            "TES-A-119_liq-cheque-depositado-en-nomina-y-deposito-liquidado-se-liquida",
            "TES-A-120_liq-cheque-certificado-pero-no-depositado-es-error",
            "TES-A-121_liq-recaudacion-APV-por-caja-PAC-no-valida-deposito-ni-certificacion",
            "TES-A-122_liq-requisitos-basicos-con-error-corta-antes-de-validar",
            "TES-A-123_liq-sin-reglas-de-negocio-no-valida-y-liquida-un-comprobante-bloqueado",
            "TES-A-124_liq-egreso-con-ingreso-relacionado-pendiente-es-error",
            "TES-A-125_liq-un-comprobante-invalido-y-otro-valido-liquida-solo-el-valido",
            "TES-A-126_liq-requisito-LIQFECHADIS-AUTORIZACION-crea-evento-y-deja-el-comprobante-en-PDA",
            "TES-A-127_liq-requisito-LIQFECHADIS-ADVERTENCIA-liquida-con-advertencia",
            "TES-A-128_liq-requisito-LIQFECHADIS-RECHAZO-es-error",
            "TES-A-129_liq-requisito-LIQFECHADIS-inactivo-se-ignora",
            "TES-A-130_liq-requisito-LIQFECHADIS-no-aplica-a-ingresos",
            "TES-A-131_liq-requisito-LIQFECHADIS-no-aplica-a-egresos-ya-en-PDA",
            "TES-A-132_liq-requisito-LIQFECHADIS-con-fecha-de-sistema-no-exige-nada",
            "TES-A-133_liq-requisito-LIQSALDONEG-AUTORIZACION-con-saldo-negativo-crea-evento",
            "TES-A-134_liq-requisito-LIQSALDONEG-ADVERTENCIA-con-saldo-negativo-liquida",
            "TES-A-135_liq-requisito-LIQSALDONEG-RECHAZO-con-saldo-negativo-es-error",
            "TES-A-136_liq-requisito-LIQSALDONEG-con-saldo-positivo-no-exige-nada",
            "TES-A-137_liq-requisito-LIQSALDONEG-sin-saldo-del-portafolio-lanza-NPE",
            "TES-A-138_liq-requisito-LIQSALDONEG-solo-aplica-a-los-conceptos-CIECDV-y-REPDIN",
            "TES-A-139_liq-requisito-LIQSALDONEG-portafolio-sin-cuenta-de-inversion-no-exige-nada",
            "TES-A-140_liq-requisito-LIQSALDONEG-sin-indicadores-del-portafolio-lanza-NPE",
            "TES-A-141_liq-forma-de-pago-indicada-liquida-parcialmente-y-deja-el-comprobante-en-LIP",
            "TES-A-142_liq-liquidacion-parcial-con-envio-a-cuenta-inversion-genera-cargo-abono",
            "TES-A-143_liq-sin-forma-de-pago-indicada-liquida-todas-las-formas-de-pago",
            "TES-A-144_liq-segunda-liquidacion-parcial-completa-el-comprobante",
            "TES-A-145_liq-cuenta-inversion-del-cliente-y-de-la-corredora-genera-dos-cargos-abonos",
            "TES-A-146_liq-solo-cuenta-inversion-de-la-corredora",
            "TES-A-147_liq-solo-cuenta-inversion-del-cliente",
            "TES-A-148_liq-cargo-abono-con-error-corta-despues-de-liquidar",
            "TES-A-149_liq-error-al-actualizar-saldo-de-banco-corta-despues-de-liquidar",
            "TES-A-150_liq-simulacion-por-call-hace-rollback-de-lo-liquidado",
            "TES-A-151_liq-simulacion-por-logica-no-hace-rollback-y-pasa-la-simulacion-a-cuenta-inversion",
            "TES-A-152_liq-folio-CSC-se-asigna-al-primer-cargo-abono",
            "TES-A-153_liq-concepto-INGBON-se-informa-a-cuenta-inversion-como-ABOBON",
            "TES-A-154_liq-parametro-LIQUIDACIONPARCIALENVIAACTA-ausente-lanza-excepcion",
            "TES-A-155_liq-flujo-de-caja-insuficiente-de-la-corredora-es-error",
            "TES-A-156_liq-flujo-de-caja-suficiente-continua-a-cuenta-inversion",
            "TES-A-157_liq-concepto-que-no-valida-saldo-de-caja-no-bloquea",
            "TES-A-158_liq-concepto-configurado-sin-validacion-de-flujo-no-consulta-flujo-de-caja",
            "TES-A-159_liq-validacion-de-flujo-de-caja-desactivada-no-consulta-flujo",
            "TES-A-160_liq-egreso-con-saldo-de-flujo-solo-de-la-corredora-lanza-NPE-para-el-portafolio-del-cliente",
            "TES-A-161_liq-partida-de-ingreso-informa-a-cuenta-inversion-desde-el-movimiento-origen",
            "TES-A-162_liq-egreso-de-tesoreria-a-otro-portafolio-usa-el-portafolio-del-movimiento",
            "TES-A-163_liq-egreso-del-cliente-con-flujo-de-caja-insuficiente-es-error",
            "TES-A-164_liq-egreso-del-cliente-con-flujo-de-caja-suficiente-continua");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        LiquidarComprobanteService service = new LiquidarComprobanteService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        // the recorded entry point is the legacy typed logica (TES-A-151) or the JSON call (everything else)
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> "logica".equals(caso.metodo())
                ? JsonLegacy.PLATAFORMA.toJsonTree(service.logica(JsonLegacy.PLATAFORMA.fromJson(caso.entrada().deepCopy(),
                        LiquidarComprobanteService.LiquidarComprobante_Entrada.class)))
                : service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("LiquidarComprobante");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-100 liq ingreso simple liquida comprobante detalle y forma de pago")
    void tesA100_liqIngresoSimpleLiquidaComprobanteDetalleYFormaDePago() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-101 liq egreso simple liquida")
    void tesA101_liqEgresoSimpleLiquida() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-102 liq comprobante inexistente es error")
    void tesA102_liqComprobanteInexistenteEsError() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-103 liq comprobante bloqueado es error")
    void tesA103_liqComprobanteBloqueadoEsError() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-104 liq comprobante anulado es error")
    void tesA104_liqComprobanteAnuladoEsError() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-105 liq comprobante modificado es error")
    void tesA105_liqComprobanteModificadoEsError() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-106 liq comprobante ya liquidado es error")
    void tesA106_liqComprobanteYaLiquidadoEsError() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-107 liq comprobante sin forma de pago es error")
    void tesA107_liqComprobanteSinFormaDePagoEsError() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-108 liq forma de pago menor al monto del comprobante es error")
    void tesA108_liqFormaDePagoMenorAlMontoDelComprobanteEsError() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-109 liq forma de pago mayor al monto del comprobante es error")
    void tesA109_liqFormaDePagoMayorAlMontoDelComprobanteEsError() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-110 liq comprobante anulado con forma de pago viva reporta ambos errores")
    void tesA110_liqComprobanteAnuladoConFormaDePagoVivaReportaAmbosErrores() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-111 liq forma de pago en nomina de deposito sin liquidar es error")
    void tesA111_liqFormaDePagoEnNominaDeDepositoSinLiquidarEsError() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-112 liq forma de pago en nomina con deposito liquidado se liquida")
    void tesA112_liqFormaDePagoEnNominaConDepositoLiquidadoSeLiquida() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-113 liq transferencia sin cuenta bancaria de la corredora es error")
    void tesA113_liqTransferenciaSinCuentaBancariaDeLaCorredoraEsError() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-114 liq efectivo no requiere cuenta bancaria de la corredora")
    void tesA114_liqEfectivoNoRequiereCuentaBancariaDeLaCorredora() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-115 liq egreso por deposito sin banco del socio es error si el parametro lo exige")
    void tesA115_liqEgresoPorDepositoSinBancoDelSocioEsErrorSiElParametroLoExige() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-116 liq egreso por deposito con banco del socio se liquida")
    void tesA116_liqEgresoPorDepositoConBancoDelSocioSeLiquida() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-117 liq partida de egreso no exige banco del socio")
    void tesA117_liqPartidaDeEgresoNoExigeBancoDelSocio() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-118 liq cheque sin depositar ni certificar es error")
    void tesA118_liqChequeSinDepositarNiCertificarEsError() throws Exception {
        verificar(CASOS.get(18));
    }

    @Test
    @DisplayName("TES-A-119 liq cheque depositado en nomina y deposito liquidado se liquida")
    void tesA119_liqChequeDepositadoEnNominaYDepositoLiquidadoSeLiquida() throws Exception {
        verificar(CASOS.get(19));
    }

    @Test
    @DisplayName("TES-A-120 liq cheque certificado pero no depositado es error")
    void tesA120_liqChequeCertificadoPeroNoDepositadoEsError() throws Exception {
        verificar(CASOS.get(20));
    }

    @Test
    @DisplayName("TES-A-121 liq recaudacion APV por caja PAC no valida deposito ni certificacion")
    void tesA121_liqRecaudacionAPVPorCajaPACNoValidaDepositoNiCertificacion() throws Exception {
        verificar(CASOS.get(21));
    }

    @Test
    @DisplayName("TES-A-122 liq requisitos basicos con error corta antes de validar")
    void tesA122_liqRequisitosBasicosConErrorCortaAntesDeValidar() throws Exception {
        verificar(CASOS.get(22));
    }

    @Test
    @DisplayName("TES-A-123 liq sin reglas de negocio no valida y liquida un comprobante bloqueado")
    void tesA123_liqSinReglasDeNegocioNoValidaYLiquidaUnComprobanteBloqueado() throws Exception {
        verificar(CASOS.get(23));
    }

    @Test
    @DisplayName("TES-A-124 liq egreso con ingreso relacionado pendiente es error")
    void tesA124_liqEgresoConIngresoRelacionadoPendienteEsError() throws Exception {
        verificar(CASOS.get(24));
    }

    @Test
    @DisplayName("TES-A-125 liq un comprobante invalido y otro valido liquida solo el valido")
    void tesA125_liqUnComprobanteInvalidoYOtroValidoLiquidaSoloElValido() throws Exception {
        verificar(CASOS.get(25));
    }

    @Test
    @DisplayName("TES-A-126 liq requisito LIQFECHADIS AUTORIZACION crea evento y deja el comprobante en PDA")
    void tesA126_liqRequisitoLIQFECHADISAUTORIZACIONCreaEventoYDejaElComprobanteEnPDA() throws Exception {
        verificar(CASOS.get(26));
    }

    @Test
    @DisplayName("TES-A-127 liq requisito LIQFECHADIS ADVERTENCIA liquida con advertencia")
    void tesA127_liqRequisitoLIQFECHADISADVERTENCIALiquidaConAdvertencia() throws Exception {
        verificar(CASOS.get(27));
    }

    @Test
    @DisplayName("TES-A-128 liq requisito LIQFECHADIS RECHAZO es error")
    void tesA128_liqRequisitoLIQFECHADISRECHAZOEsError() throws Exception {
        verificar(CASOS.get(28));
    }

    @Test
    @DisplayName("TES-A-129 liq requisito LIQFECHADIS inactivo se ignora")
    void tesA129_liqRequisitoLIQFECHADISInactivoSeIgnora() throws Exception {
        verificar(CASOS.get(29));
    }

    @Test
    @DisplayName("TES-A-130 liq requisito LIQFECHADIS no aplica a ingresos")
    void tesA130_liqRequisitoLIQFECHADISNoAplicaAIngresos() throws Exception {
        verificar(CASOS.get(30));
    }

    @Test
    @DisplayName("TES-A-131 liq requisito LIQFECHADIS no aplica a egresos ya en PDA")
    void tesA131_liqRequisitoLIQFECHADISNoAplicaAEgresosYaEnPDA() throws Exception {
        verificar(CASOS.get(31));
    }

    @Test
    @DisplayName("TES-A-132 liq requisito LIQFECHADIS con fecha de sistema no exige nada")
    void tesA132_liqRequisitoLIQFECHADISConFechaDeSistemaNoExigeNada() throws Exception {
        verificar(CASOS.get(32));
    }

    @Test
    @DisplayName("TES-A-133 liq requisito LIQSALDONEG AUTORIZACION con saldo negativo crea evento")
    void tesA133_liqRequisitoLIQSALDONEGAUTORIZACIONConSaldoNegativoCreaEvento() throws Exception {
        verificar(CASOS.get(33));
    }

    @Test
    @DisplayName("TES-A-134 liq requisito LIQSALDONEG ADVERTENCIA con saldo negativo liquida")
    void tesA134_liqRequisitoLIQSALDONEGADVERTENCIAConSaldoNegativoLiquida() throws Exception {
        verificar(CASOS.get(34));
    }

    @Test
    @DisplayName("TES-A-135 liq requisito LIQSALDONEG RECHAZO con saldo negativo es error")
    void tesA135_liqRequisitoLIQSALDONEGRECHAZOConSaldoNegativoEsError() throws Exception {
        verificar(CASOS.get(35));
    }

    @Test
    @DisplayName("TES-A-136 liq requisito LIQSALDONEG con saldo positivo no exige nada")
    void tesA136_liqRequisitoLIQSALDONEGConSaldoPositivoNoExigeNada() throws Exception {
        verificar(CASOS.get(36));
    }

    @Test
    @DisplayName("TES-A-137 liq requisito LIQSALDONEG sin saldo del portafolio lanza NPE")
    void tesA137_liqRequisitoLIQSALDONEGSinSaldoDelPortafolioLanzaNPE() throws Exception {
        verificar(CASOS.get(37));
    }

    @Test
    @DisplayName("TES-A-138 liq requisito LIQSALDONEG solo aplica a los conceptos CIECDV y REPDIN")
    void tesA138_liqRequisitoLIQSALDONEGSoloAplicaALosConceptosCIECDVYREPDIN() throws Exception {
        verificar(CASOS.get(38));
    }

    @Test
    @DisplayName("TES-A-139 liq requisito LIQSALDONEG portafolio sin cuenta de inversion no exige nada")
    void tesA139_liqRequisitoLIQSALDONEGPortafolioSinCuentaDeInversionNoExigeNada() throws Exception {
        verificar(CASOS.get(39));
    }

    @Test
    @DisplayName("TES-A-140 liq requisito LIQSALDONEG sin indicadores del portafolio lanza NPE")
    void tesA140_liqRequisitoLIQSALDONEGSinIndicadoresDelPortafolioLanzaNPE() throws Exception {
        verificar(CASOS.get(40));
    }

    @Test
    @DisplayName("TES-A-141 liq forma de pago indicada liquida parcialmente y deja el comprobante en LIP")
    void tesA141_liqFormaDePagoIndicadaLiquidaParcialmenteYDejaElComprobanteEnLIP() throws Exception {
        verificar(CASOS.get(41));
    }

    @Test
    @DisplayName("TES-A-142 liq liquidacion parcial con envio a cuenta inversion genera cargo abono")
    void tesA142_liqLiquidacionParcialConEnvioACuentaInversionGeneraCargoAbono() throws Exception {
        verificar(CASOS.get(42));
    }

    @Test
    @DisplayName("TES-A-143 liq sin forma de pago indicada liquida todas las formas de pago")
    void tesA143_liqSinFormaDePagoIndicadaLiquidaTodasLasFormasDePago() throws Exception {
        verificar(CASOS.get(43));
    }

    @Test
    @DisplayName("TES-A-144 liq segunda liquidacion parcial completa el comprobante")
    void tesA144_liqSegundaLiquidacionParcialCompletaElComprobante() throws Exception {
        verificar(CASOS.get(44));
    }

    @Test
    @DisplayName("TES-A-145 liq cuenta inversion del cliente y de la corredora genera dos cargos abonos")
    void tesA145_liqCuentaInversionDelClienteYDeLaCorredoraGeneraDosCargosAbonos() throws Exception {
        verificar(CASOS.get(45));
    }

    @Test
    @DisplayName("TES-A-146 liq solo cuenta inversion de la corredora")
    void tesA146_liqSoloCuentaInversionDeLaCorredora() throws Exception {
        verificar(CASOS.get(46));
    }

    @Test
    @DisplayName("TES-A-147 liq solo cuenta inversion del cliente")
    void tesA147_liqSoloCuentaInversionDelCliente() throws Exception {
        verificar(CASOS.get(47));
    }

    @Test
    @DisplayName("TES-A-148 liq cargo abono con error corta despues de liquidar")
    void tesA148_liqCargoAbonoConErrorCortaDespuesDeLiquidar() throws Exception {
        verificar(CASOS.get(48));
    }

    @Test
    @DisplayName("TES-A-149 liq error al actualizar saldo de banco corta despues de liquidar")
    void tesA149_liqErrorAlActualizarSaldoDeBancoCortaDespuesDeLiquidar() throws Exception {
        verificar(CASOS.get(49));
    }

    @Test
    @DisplayName("TES-A-150 liq simulacion por call hace rollback de lo liquidado")
    void tesA150_liqSimulacionPorCallHaceRollbackDeLoLiquidado() throws Exception {
        verificar(CASOS.get(50));
    }

    @Test
    @DisplayName("TES-A-151 liq simulacion por logica no hace rollback y pasa la simulacion a cuenta inversion")
    void tesA151_liqSimulacionPorLogicaNoHaceRollbackYPasaLaSimulacionACuentaInversion() throws Exception {
        verificar(CASOS.get(51));
    }

    @Test
    @DisplayName("TES-A-152 liq folio CSC se asigna al primer cargo abono")
    void tesA152_liqFolioCSCSeAsignaAlPrimerCargoAbono() throws Exception {
        verificar(CASOS.get(52));
    }

    @Test
    @DisplayName("TES-A-153 liq concepto INGBON se informa a cuenta inversion como ABOBON")
    void tesA153_liqConceptoINGBONSeInformaACuentaInversionComoABOBON() throws Exception {
        verificar(CASOS.get(53));
    }

    @Test
    @DisplayName("TES-A-154 liq parametro LIQUIDACIONPARCIALENVIAACTA ausente lanza excepcion")
    void tesA154_liqParametroLIQUIDACIONPARCIALENVIAACTAAusenteLanzaExcepcion() throws Exception {
        verificar(CASOS.get(54));
    }

    @Test
    @DisplayName("TES-A-155 liq flujo de caja insuficiente de la corredora es error")
    void tesA155_liqFlujoDeCajaInsuficienteDeLaCorredoraEsError() throws Exception {
        verificar(CASOS.get(55));
    }

    @Test
    @DisplayName("TES-A-156 liq flujo de caja suficiente continua a cuenta inversion")
    void tesA156_liqFlujoDeCajaSuficienteContinuaACuentaInversion() throws Exception {
        verificar(CASOS.get(56));
    }

    @Test
    @DisplayName("TES-A-157 liq concepto que no valida saldo de caja no bloquea")
    void tesA157_liqConceptoQueNoValidaSaldoDeCajaNoBloquea() throws Exception {
        verificar(CASOS.get(57));
    }

    @Test
    @DisplayName("TES-A-158 liq concepto configurado sin validacion de flujo no consulta flujo de caja")
    void tesA158_liqConceptoConfiguradoSinValidacionDeFlujoNoConsultaFlujoDeCaja() throws Exception {
        verificar(CASOS.get(58));
    }

    @Test
    @DisplayName("TES-A-159 liq validacion de flujo de caja desactivada no consulta flujo")
    void tesA159_liqValidacionDeFlujoDeCajaDesactivadaNoConsultaFlujo() throws Exception {
        verificar(CASOS.get(59));
    }

    @Test
    @DisplayName("TES-A-160 liq egreso con saldo de flujo solo de la corredora lanza NPE para el portafolio del cliente")
    void tesA160_liqEgresoConSaldoDeFlujoSoloDeLaCorredoraLanzaNPEParaElPortafolioDelCliente() throws Exception {
        verificar(CASOS.get(60));
    }

    @Test
    @DisplayName("TES-A-161 liq partida de ingreso informa a cuenta inversion desde el movimiento origen")
    void tesA161_liqPartidaDeIngresoInformaACuentaInversionDesdeElMovimientoOrigen() throws Exception {
        verificar(CASOS.get(61));
    }

    @Test
    @DisplayName("TES-A-162 liq egreso de tesoreria a otro portafolio usa el portafolio del movimiento")
    void tesA162_liqEgresoDeTesoreriaAOtroPortafolioUsaElPortafolioDelMovimiento() throws Exception {
        verificar(CASOS.get(62));
    }

    @Test
    @DisplayName("TES-A-163 liq egreso del cliente con flujo de caja insuficiente es error")
    void tesA163_liqEgresoDelClienteConFlujoDeCajaInsuficienteEsError() throws Exception {
        verificar(CASOS.get(63));
    }

    @Test
    @DisplayName("TES-A-164 liq egreso del cliente con flujo de caja suficiente continua")
    void tesA164_liqEgresoDelClienteConFlujoDeCajaSuficienteContinua() throws Exception {
        verificar(CASOS.get(64));
    }
}
