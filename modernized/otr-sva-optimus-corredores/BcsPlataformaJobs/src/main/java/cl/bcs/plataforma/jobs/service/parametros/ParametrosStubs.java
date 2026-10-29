package cl.bcs.plataforma.jobs.service.parametros;

import org.springframework.stereotype.Service;

/**
 * Stub service for the remaining parameter-update jobs that require
 * a live HANA connection or external REST services.
 *
 * Legacy classes stubbed:
 *   - ActualizarParametrosFinancierosUF
 *   - ActualizarParametrosFinancierosBancoCentral
 *   - ActualizarParametrosFinancierosBancoCentralUF
 *   - OrquestadorActualizarParametrosFinancieros
 *   - OrquestadorActualizarParametrosFinancierosUF
 *   - ActualizaPreciosHistoricosJob
 *   - ActualizarPreciosVar
 *   - ActualizaInstrumentosJob
 *   - ActualizaInstrumentosNegociadosJob
 *   - ActualizaContratosFirmadosJob
 *   - EstandarizarCorredoresBolsaJob
 *   - VencimientoAutorizaciones
 *   - VencimientoClavesJob
 *   - VencimientoDocumentos
 *   - ValidaSaldosMovimientos
 *   - ValidacionPreventivaJob
 *   - ValorizaJob
 *   - EjecucionContinuaSQL
 *   - LimpiarDataHistoricoJob
 *   - MigrarDataHistoricoJob
 *   - JobAperturaMesasModulos
 *   - ProgramarPautaJob
 *   - TestAgendaJob
 *   - BcsJobEjecutarTareaJenkins
 *   - BcsJobInterfazLiquidezIntradia
 *   - BcsJobMonitor
 *   - BcsJobPautaGenerarInterfaces
 *   - MonitorPlataformaJob
 *   - NotificarOwpJob
 *   - DespachadorEmailsJob
 *   - MailChecker
 */
@Service
public class ParametrosStubs {

    private static final String MSG =
            " requires a live HANA connection or external service";

    /** legacy: ActualizarParametrosFinancierosUF */
    public void actualizarParametrosFinancierosUF(Long idEmpresa) {
        throw new UnsupportedOperationException("ActualizarParametrosFinancierosUF" + MSG);
    }

    /** legacy: ActualizarParametrosFinancierosBancoCentral */
    public void actualizarParametrosFinancierosBancoCentral(Long idEmpresa) {
        throw new UnsupportedOperationException("ActualizarParametrosFinancierosBancoCentral" + MSG);
    }

    /** legacy: ActualizarParametrosFinancierosBancoCentralUF */
    public void actualizarParametrosFinancierosBancoCentralUF(Long idEmpresa) {
        throw new UnsupportedOperationException("ActualizarParametrosFinancierosBancoCentralUF" + MSG);
    }

    /** legacy: OrquestadorActualizarParametrosFinancieros */
    public void orquestadorActualizarParametrosFinancieros(Long idEmpresa) {
        throw new UnsupportedOperationException("OrquestadorActualizarParametrosFinancieros" + MSG);
    }

    /** legacy: OrquestadorActualizarParametrosFinancierosUF */
    public void orquestadorActualizarParametrosFinancierosUF(Long idEmpresa) {
        throw new UnsupportedOperationException("OrquestadorActualizarParametrosFinancierosUF" + MSG);
    }

    /** legacy: ActualizaPreciosHistoricosJob */
    public void actualizaPreciosHistoricos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("ActualizaPreciosHistoricosJob" + MSG);
    }

    /** legacy: ActualizarPreciosVar */
    public void actualizarPreciosVar(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("ActualizarPreciosVar" + MSG);
    }

    /** legacy: ActualizaInstrumentosJob */
    public void actualizaInstrumentos(Long idEmpresa) {
        throw new UnsupportedOperationException("ActualizaInstrumentosJob" + MSG);
    }

    /** legacy: ActualizaInstrumentosNegociadosJob */
    public void actualizaInstrumentosNegociados(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("ActualizaInstrumentosNegociadosJob" + MSG);
    }

    /** legacy: ActualizaContratosFirmadosJob */
    public void actualizaContratosFirmados(Long idEmpresa) {
        throw new UnsupportedOperationException("ActualizaContratosFirmadosJob" + MSG);
    }

    /** legacy: EstandarizarCorredoresBolsaJob */
    public void estandarizarCorredoresBolsa(Long idEmpresa) {
        throw new UnsupportedOperationException("EstandarizarCorredoresBolsaJob" + MSG);
    }

    /** legacy: VencimientoAutorizaciones */
    public void vencimientoAutorizaciones(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("VencimientoAutorizaciones" + MSG);
    }

    /** legacy: VencimientoClavesJob */
    public void vencimientoClaves(Long idEmpresa) {
        throw new UnsupportedOperationException("VencimientoClavesJob" + MSG);
    }

    /** legacy: VencimientoDocumentos */
    public void vencimientoDocumentos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("VencimientoDocumentos" + MSG);
    }

    /** legacy: ValidaSaldosMovimientos */
    public void validaSaldosMovimientos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("ValidaSaldosMovimientos" + MSG);
    }

    /** legacy: ValidacionPreventivaJob */
    public void validacionPreventiva(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("ValidacionPreventivaJob" + MSG);
    }

    /** legacy: ValorizaJob */
    public void valoriza(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("ValorizaJob" + MSG);
    }

    /** legacy: EjecucionContinuaSQL */
    public void ejecucionContinuaSQL(Long idEmpresa) {
        throw new UnsupportedOperationException("EjecucionContinuaSQL" + MSG);
    }

    /** legacy: LimpiarDataHistoricoJob */
    public void limpiarDataHistorico(Long idEmpresa, String fechaHasta) {
        throw new UnsupportedOperationException("LimpiarDataHistoricoJob" + MSG);
    }

    /** legacy: MigrarDataHistoricoJob */
    public void migrarDataHistorico(Long idEmpresa, String fechaDesde, String fechaHasta) {
        throw new UnsupportedOperationException("MigrarDataHistoricoJob" + MSG);
    }

    /** legacy: JobAperturaMesasModulos */
    public void aperturaMesasModulos(Long idEmpresa) {
        throw new UnsupportedOperationException("JobAperturaMesasModulos" + MSG);
    }

    /** legacy: ProgramarPautaJob */
    public void programarPauta(Long idEmpresa) {
        throw new UnsupportedOperationException("ProgramarPautaJob" + MSG);
    }

    /** legacy: TestAgendaJob */
    public void testAgenda(Long idEmpresa) {
        throw new UnsupportedOperationException("TestAgendaJob" + MSG);
    }

    /** legacy: BcsJobEjecutarTareaJenkins */
    public void ejecutarTareaJenkins(String nombreTarea) {
        throw new UnsupportedOperationException("BcsJobEjecutarTareaJenkins" + MSG);
    }

    /** legacy: BcsJobInterfazLiquidezIntradia */
    public void interfazLiquidezIntradia(Long idEmpresa) {
        throw new UnsupportedOperationException("BcsJobInterfazLiquidezIntradia" + MSG);
    }

    /** legacy: BcsJobMonitor */
    public void jobMonitor(Long idEmpresa) {
        throw new UnsupportedOperationException("BcsJobMonitor" + MSG);
    }

    /** legacy: BcsJobPautaGenerarInterfaces */
    public void pautaGenerarInterfaces(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("BcsJobPautaGenerarInterfaces" + MSG);
    }

    /** legacy: MonitorPlataformaJob */
    public void monitorPlataforma(Long idEmpresa) {
        throw new UnsupportedOperationException("MonitorPlataformaJob" + MSG);
    }

    /** legacy: NotificarOwpJob */
    public void notificarOwp(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("NotificarOwpJob" + MSG);
    }

    /** legacy: DespachadorEmailsJob */
    public void despachadorEmails(Long idEmpresa) {
        throw new UnsupportedOperationException("DespachadorEmailsJob" + MSG);
    }

    /** legacy: MailChecker */
    public void mailChecker(Long idEmpresa) {
        throw new UnsupportedOperationException("MailChecker" + MSG);
    }
}
