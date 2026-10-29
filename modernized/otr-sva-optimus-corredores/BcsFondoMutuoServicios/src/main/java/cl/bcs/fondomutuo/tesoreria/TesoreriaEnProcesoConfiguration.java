package cl.bcs.fondomutuo.tesoreria;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import cl.bcs.fondomutuo.adapter.out.http.RouterClient;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.tesoreria.infrastructure.adapter.ConfiguracionSistemaJdbc;
import cl.bcs.tesoreria.infrastructure.adapter.DiccionarioJdbc;
import cl.bcs.tesoreria.infrastructure.adapter.ProcedimientoAlmacenado;
import cl.bcs.tesoreria.infrastructure.adapter.SecuenciasJdbc;
import cl.bcs.tesoreria.javacall.CrearComprobanteJavaService;
import cl.bcs.tesoreria.javacall.CrearComprobanteProcedimiento;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;

/**
 * C2 of PLAN_FONDOMUTUO_F2 4.1: BcsTesoreriaServicios as an IN-PROCESS library, importing ONLY the beans that {@code CrearIngresoEgresoService} needs
 * (its constructor and the constructors below it). {@code cl.bcs.tesoreria} is never component-scanned: its controller, {@code TokenInternoFilter}, SOAP
 * skeleton, {@code JavaCallDispatcher}, {@code ColaboradoresJavaCall} (its own router client) and the rest of its services stay out of this context
 * ({@code TesoreriaEnProcesoContextoTest} pins the list). Imported classes get their fully qualified class name as bean name, so they never clash with the
 * beans of this module that have the same simple name ({@code ConfiguracionSistemaJdbc}, {@code DiccionarioJdbc}).
 *
 * <ul>
 *   <li>services: {@code CrearIngresoEgresoService} (the entry point, {@code logica(CrearIngresoEgreso_Entrada)}, {@code @Transactional} REQUIRED),
 *       {@code CrearComprobanteService} + {@code CrearComprobanteProcedimiento} + {@code CrearComprobanteJavaService} (the movements are created with
 *       {@code GeneraComprobante = "S"}), {@code LiquidarComprobanteService} (constructor of the entry point; the helper never asks for liquidation) and
 *       {@code ActualizarSaldosCuentaBancoService} (Tesoreria's own write reached by the Java voucher path; Tesoreria runs it in process too);</li>
 *   <li>ports of Tesoreria with Tesoreria's own JDBC adapters ({@code ConfiguracionSistema}, {@code DiccionarioMensajes}, {@code Secuencias}) and
 *       {@code ProcedimientoAlmacenado} (the existing procedure {@code CrearComprobante}, same SQL as the legacy, on the connection of the unit of work);</li>
 *   <li>its {@code Colaboradores} port: {@link ColaboradoresTesoreria} (C4: the Plataforma collaborators over this module's {@link RouterClient}).</li>
 * </ul>
 *
 * <p>C1 / C1b: every bean here works on the module {@code DataSource} through Spring's {@code NamedParameterJdbcTemplate} (auto-configured), so it joins
 * the Spring transaction that {@code JavaCallUnitOfWorkFilter} opened with the module {@code DataSourceTransactionManager}
 * ({@code globalRollbackOnParticipationFailure = false}, {@code config.TransaccionConfig}); the Tesoreria rows are committed or rolled back with the request.
 */
@Configuration(proxyBeanMethods = false)
@Import({ CrearIngresoEgresoService.class, CrearComprobanteService.class, CrearComprobanteProcedimiento.class, CrearComprobanteJavaService.class,
        LiquidarComprobanteService.class, ActualizarSaldosCuentaBancoService.class, ConfiguracionSistemaJdbc.class, DiccionarioJdbc.class,
        SecuenciasJdbc.class, ProcedimientoAlmacenado.class })
public class TesoreriaEnProcesoConfiguration {

    @Bean
    public ColaboradoresTesoreria colaboradoresTesoreria(RouterClient router, Compuertas compuertas, ProcedimientoAlmacenado procedimientos,
            ActualizarSaldosCuentaBancoService saldosCuentaBanco) {
        return new ColaboradoresTesoreria(router, compuertas, procedimientos, saldosCuentaBanco::execute);
    }

    @Bean
    public TesoreriaDatos tesoreriaDatos(ConexionUnidadDeTrabajo conexion) {
        return new TesoreriaJdbc(conexion);
    }

    /** The port Normal calls (legacy {@code ProcesarIngresoEgresoTesoreria.logica}); replaces the F1 "not served" default of {@code NormalConfiguracion}. */
    @Bean
    public ProcesosExternos.TesoreriaPort procesarIngresoEgresoTesoreria(TesoreriaDatos datos, CrearIngresoEgresoService crearIngresoEgreso) {
        return new ProcesarIngresoEgresoTesoreriaService(datos, crearIngresoEgreso::logica);
    }
}
