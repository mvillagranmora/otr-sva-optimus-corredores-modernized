package cl.bcs.apv.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.adapter.javacall.ApvJavaCallPipelineEnProceso;
import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro;
import cl.bcs.apv.adapter.javacall.ValidarMandatosPACLogicaMapeo;
import cl.bcs.apv.api.logica.ValidarMandatosPACLogica;
import cl.bcs.apv.application.recaudacion.ValidarMandatosPACLogicaServicio;

/**
 * The typed entry {@code ValidarMandatosPAC.logica} runs on the connection and in the transaction of the CALLER (the Tesoreria job): what it writes is
 * visible inside the caller's transaction, is undone when the caller rolls back, is kept when the caller commits, and, through a real
 * {@code @Transactional} proxy (REQUIRED), an exception of the service marks the SHARED transaction rollback-only. Same in-process pipeline as the job uses.
 */
class ValidarMandatosPACLogicaTransaccionTest {

    private static final String LOG = "\"BSCL.Tesoreria.BD::PAC.LogMandatoPAC\"";
    private static final String BUENA = "{\"entrada\":{\"P_OUT_SOLICITUDES_A_PROCESAR\":[{\"IdMandatoPAC\":1,\"IdentServicio\":\"S1\"}],"
            + "\"P_OUT_SOLICITUDES_NO_PROCESADAS\":[{\"IdMandatoPAC\":2,\"EstadoRendicion\":\"SIN FONDOS\"}]},\"idEmpresa\":1,\"idUsuario\":21}";
    private static final String SIN_USUARIO = "{\"entrada\":{\"P_OUT_SOLICITUDES_A_PROCESAR\":[{\"IdMandatoPAC\":1}]},\"idEmpresa\":1,\"idUsuario\":null}";

    private NamedParameterJdbcTemplate jdbc;
    private DataSourceTransactionManager tm;
    private TransactionTemplate tx;
    private ApvJavaCallPipelineEnProceso pipeline;

    @BeforeEach
    void preparar() {
        DriverManagerDataSource ds = new DriverManagerDataSource("jdbc:h2:mem:logica_" + UUID.randomUUID() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new NamedParameterJdbcTemplate(ds);
        jdbc.getJdbcOperations().execute("CREATE TABLE " + LOG + " (\"FechaProceso\" DATE, \"FechaSistema\" DATE, \"Usuario\" NVARCHAR(500), "
                + "\"EstadoProcesamiento\" NVARCHAR(500), \"PreRendicion\" NVARCHAR(500), \"Glosa\" NVARCHAR(500), \"IdMandatoPAC\" BIGINT)");
        for (int id = 1; id <= 2; id++) {
            jdbc.getJdbcOperations().update("INSERT INTO " + LOG + " VALUES (DATE '2024-01-01', DATE '2025-04-01', '9', 'ORI', 'N', 'previo', ?)", id);
        }
        tm = new DataSourceTransactionManager(ds);
        tx = new TransactionTemplate(tm);
        ValidarMandatosPACLogicaServicio servicio = new ValidarMandatosPACLogicaServicio(jdbc, (empresa, modulo, nombre) -> "2025-04-01");
        ProxyFactory fabrica = new ProxyFactory(servicio);
        fabrica.addAdvice(new TransactionInterceptor(tm, new AnnotationTransactionAttributeSource()));
        pipeline = new ApvJavaCallPipelineEnProceso(new ApvJavaCallRegistro(), Map.of(ValidarMandatosPACLogica.class, fabrica.getProxy()));
    }

    private void invocar(String json) throws Exception {
        JsonObject r = pipeline.invocar(ValidarMandatosPACLogicaMapeo.OPERACION, JsonParser.parseString(json).getAsJsonObject());
        assertThat(r).as("the typed method is void").isNull();
    }

    private String estado(int id) {
        return jdbc.getJdbcOperations().queryForObject("SELECT \"EstadoProcesamiento\" FROM " + LOG + " WHERE \"IdMandatoPAC\" = ?", String.class, id);
    }

    @Test
    void loEscritoEsVisibleEnLaTransaccionDelLlamadorYSeDeshaceConSuRollback() {
        tx.executeWithoutResult(status -> {
            try {
                invocar(BUENA);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
            assertThat(estado(1)).isEqualTo("PRO");
            assertThat(estado(2)).isEqualTo("PEN");
            status.setRollbackOnly();
        });
        assertThat(estado(1)).as("rolled back with the caller").isEqualTo("ORI");
        assertThat(estado(2)).isEqualTo("ORI");
    }

    @Test
    void conCommitDelLlamadorSeConserva() {
        tx.executeWithoutResult(status -> {
            try {
                invocar(BUENA);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
        assertThat(estado(1)).isEqualTo("PRO");
        assertThat(estado(2)).isEqualTo("PEN");
    }

    @Test
    void unaExcepcionDelServicioMarcaLaTransaccionCompartidaYSeDeshaceTodoLoDelLlamador() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            try {
                invocar(BUENA);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
            // the second call fails (IdUsuario null with a request to write: NullPointerException, the very exception of the legacy) and the caller swallows it
            assertThatThrownBy(() -> invocar(SIN_USUARIO)).isInstanceOf(NullPointerException.class);
        })).isInstanceOf(UnexpectedRollbackException.class);
        assertThat(estado(1)).as("the first call was part of the same transaction: undone").isEqualTo("ORI");
        assertThat(estado(2)).isEqualTo("ORI");
    }

    @Test
    void unaExcepcionChequeadaLlegaSinEnvolverAunqueElServicioEsteProxificado() {
        // PLA.FechaSistema missing is a java.lang.Exception in the legacy: through the proxy it arrives wrapped, the pipeline must hand the original one
        ValidarMandatosPACLogicaServicio sinFecha = new ValidarMandatosPACLogicaServicio(jdbc, (empresa, modulo, nombre) -> null);
        ProxyFactory fabrica = new ProxyFactory(sinFecha);
        fabrica.addAdvice(new TransactionInterceptor(tm, new AnnotationTransactionAttributeSource()));
        ApvJavaCallPipelineEnProceso otro = new ApvJavaCallPipelineEnProceso(new ApvJavaCallRegistro(),
                Map.of(ValidarMandatosPACLogica.class, fabrica.getProxy()));
        assertThatThrownBy(() -> otro.invocar(ValidarMandatosPACLogicaMapeo.OPERACION, JsonParser.parseString(BUENA).getAsJsonObject()))
                .isExactlyInstanceOf(Exception.class).hasMessageContaining("PLA.FechaSistema");
        assertThat(estado(1)).isEqualTo("ORI");
    }
}
