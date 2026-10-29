package cl.bcs.cuentainversion.jobs.wiring;

import cl.bcs.cuentainversion.infrastructure.adapter.AuditoriaReportesAdapter;
import cl.bcs.cuentainversion.infrastructure.adapter.ColaboradoresReportesAdapter;
import cl.bcs.cuentainversion.infrastructure.adapter.DiccionarioJdbc;
import cl.bcs.cuentainversion.infrastructure.adapter.GeneradorPdfNoDisponible;
import cl.bcs.cuentainversion.infrastructure.adapter.HanaRepository;
import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.ActualizarSaldosReporteAntiguedadService;
import cl.bcs.cuentainversion.service.CargaInicialSaldosReporteAntiguedadService;
import cl.bcs.cuentainversion.service.ConsultaComparacionSaldoService;
import cl.bcs.cuentainversion.service.ConsultarSaldoCuentaInversionService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.service.GenerarArchivoCargoAbonoService;
import cl.bcs.cuentainversion.service.GenerarArchivoClientesPorPagarYCobrarService;
import cl.bcs.cuentainversion.service.GenerarArchivoCuentasPorCobrarAnexo16Service;
import cl.bcs.cuentainversion.service.GenerarArchivoSaldoCuentasService;
import cl.bcs.cuentainversion.service.GenerarInformePatrimonioDiarioService;
import cl.bcs.cuentainversion.service.GenerarInformeResumenCobroService;
import cl.bcs.cuentainversion.service.GenerarReporteAcreedoresService;
import cl.bcs.cuentainversion.service.GenerarReporteAntiguedadDeudaBolsaCTAService;
import cl.bcs.cuentainversion.service.GenerarReporteAntiguedadDeudaCTAService;
import cl.bcs.cuentainversion.service.GuardarPatrimonioCajaService;
import cl.bcs.cuentainversion.service.GuardarSaldosReporteAntiguedadService;
import cl.bcs.cuentainversion.service.JavaCallDispatcher;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * BcsCuentaInversionServicios as an in-process library (decision 2026-10-15): ONLY the services the jobs call (the 15 of the legacy jobs)
 * and what they need, WITHOUT the web layer ({@code controller}, {@code infrastructure.web}: this module has its own controller and filter)
 * and without the services of the transactional family (which would need the REST clients of authorization, events, ...).
 *
 * <p>{@link ConsultarSaldoCuentaInversionService} and {@link SaldoCuentaInversionService} are included because the report services reach
 * {@code ConsultarSaldoCuentaInversion} through the {@code Colaboradores} port, which runs it in process ({@link JavaCallDispatcher}).
 * That path needs the configuration service of the platform ({@code services.configuracion.url}), reached through
 * {@code ConfiguracionRestAcotado} of this module (the library's {@code ConfiguracionRestClient} has no timeouts and is NOT scanned).
 * Everything runs on the HANA {@code DataSource} that Spring Boot configures and in the transaction the {@code JobRunner} opens per run.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
        basePackages = "cl.bcs.cuentainversion",
        useDefaultFilters = false,
        includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                ActualizarSaldoCargoAbonoService.class, ActualizarSaldosReporteAntiguedadService.class,
                CargaInicialSaldosReporteAntiguedadService.class, ConsultaComparacionSaldoService.class,
                GenerarArchivoCargoAbonoService.class, GenerarArchivoClientesPorPagarYCobrarService.class,
                GenerarArchivoCuentasPorCobrarAnexo16Service.class, GenerarArchivoSaldoCuentasService.class,
                GenerarInformePatrimonioDiarioService.class, GenerarInformeResumenCobroService.class,
                GenerarReporteAcreedoresService.class, GenerarReporteAntiguedadDeudaBolsaCTAService.class,
                GenerarReporteAntiguedadDeudaCTAService.class, GuardarPatrimonioCajaService.class,
                GuardarSaldosReporteAntiguedadService.class,
                ConsultarSaldoCuentaInversionService.class, SaldoCuentaInversionService.class, FormatoMoneda.class,
                JavaCallDispatcher.class, ColaboradoresReportesAdapter.class, AuditoriaReportesAdapter.class,
                DiccionarioJdbc.class, GeneradorPdfNoDisponible.class, HanaRepository.class}))
public class ServiciosCuentaInversionConfiguration {
}
