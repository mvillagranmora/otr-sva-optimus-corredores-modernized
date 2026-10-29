package cl.bcs.fondomutuo.tesoreria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * C2 / C1b / C4 of PLAN_FONDOMUTUO_F2 4.1 in the REAL context of the module: exactly the imported Tesoreria beans are present (no scan of cl.bcs.tesoreria:
 * no controller, filter, dispatcher, SOAP or other service), the entry point is a transactional proxy, the only transaction manager is the module's one
 * with globalRollbackOnParticipationFailure = false, Tesoreria's Colaboradores is this module's adapter, and Normal's Tesoreria port is the ported helper.
 */
@SpringBootTest
@ActiveProfiles("test")
class TesoreriaEnProcesoContextoTest {

    static final Set<String> ESPERADOS = Set.of("cl.bcs.tesoreria.javacall.CrearIngresoEgresoService", "cl.bcs.tesoreria.javacall.CrearComprobanteService",
            "cl.bcs.tesoreria.javacall.CrearComprobanteProcedimiento", "cl.bcs.tesoreria.javacall.CrearComprobanteJavaService",
            "cl.bcs.tesoreria.javacall.LiquidarComprobanteService", "cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService",
            "cl.bcs.tesoreria.infrastructure.adapter.ConfiguracionSistemaJdbc", "cl.bcs.tesoreria.infrastructure.adapter.DiccionarioJdbc",
            "cl.bcs.tesoreria.infrastructure.adapter.SecuenciasJdbc", "cl.bcs.tesoreria.infrastructure.adapter.ProcedimientoAlmacenado");

    @Autowired ApplicationContext ctx;

    @Test
    void soloLosBeansImportadosDeTesoreria_sinEscaneo() {
        Set<String> deTesoreria = new TreeSet<>();
        for (String n : ctx.getBeanDefinitionNames()) {
            Class<?> t = ctx.getType(n);
            if (t == null) continue;
            String clase = org.springframework.util.ClassUtils.getUserClass(t).getName();
            if (clase.startsWith("cl.bcs.tesoreria.")) deTesoreria.add(clase);
        }
        assertThat(deTesoreria).containsExactlyInAnyOrderElementsOf(ESPERADOS);
    }

    @Test
    void elPuntoDeEntradaEsTransaccional_yHayUnSoloGestorConC1b() {
        assertThat(AopUtils.isAopProxy(ctx.getBean(CrearIngresoEgresoService.class))).isTrue();
        assertThat(ctx.getBeansOfType(PlatformTransactionManager.class)).hasSize(1);
        DataSourceTransactionManager tm = (DataSourceTransactionManager) ctx.getBean(PlatformTransactionManager.class);
        assertThat(tm.isGlobalRollbackOnParticipationFailure()).isFalse();
        assertThat(tm.getDataSource()).isSameAs(ctx.getBean(javax.sql.DataSource.class));
    }

    @Test
    void colaboradoresDeTesoreria_yPuertoDeNormal() {
        assertThat(ctx.getBean(Colaboradores.class)).isInstanceOf(ColaboradoresTesoreria.class);
        assertThat(ctx.getBean(ProcesosExternos.TesoreriaPort.class)).isInstanceOf(ProcesarIngresoEgresoTesoreriaService.class);
    }
}
