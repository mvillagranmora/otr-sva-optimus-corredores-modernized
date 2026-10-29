package cl.bcs.apv.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

import cl.bcs.apv.api.casouso.GrabarContratos;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargaArchivo;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargarContratosMasivoEntrada;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargarContratosMasivoResultado;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.ParametrosContrato;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosEntrada;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosResultado;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.application.contratos.CargarContratosMasivoServicio;
import cl.bcs.apv.domain.port.out.contratos.CargaMasivaRepositorio;

/**
 * H1: a checked exception thrown by a peer use case through a REAL Spring AOP proxy arrives wrapped in {@link UndeclaredThrowableException}
 * (the typed interface declares no checked exception). The caller must see the very same exception as with the unproxied peer: same class, same message.
 */
class ExcepcionChequeadaTrasProxyTest {

    private static final String MENSAJE = "fallo del par: tabla inexistente";

    private static GrabarContratos parQueLanza() {
        return new GrabarContratos() {
            @Override
            public GrabarContratosResultado ejecutar(GrabarContratosEntrada in) {
                throw Sneaky.lanzar(new SQLException(MENSAJE));
            }
        };
    }

    private static GrabarContratos conProxy(GrabarContratos objetivo) {
        ProxyFactory fabrica = new ProxyFactory(objetivo);
        fabrica.addAdvice((org.aopalliance.intercept.MethodInterceptor) invocacion -> invocacion.proceed());
        return (GrabarContratos) fabrica.getProxy();
    }

    private static String mensajeDeLaCarga(GrabarContratos par) {
        CargaMasivaRepositorio repositorio = mock(CargaMasivaRepositorio.class);
        when(repositorio.insertarArchivos(any())).thenReturn(new int[] { 1 });
        CargarContratosMasivoServicio servicio = new CargarContratosMasivoServicio(repositorio, (empresa, modulo, nombre) -> "2026-01-01", nombre -> 7L, par);
        List<ParametrosContrato> contratos = new ArrayList<>();
        contratos.add(ParametrosContrato.porDefecto());
        List<CargaArchivo> archivo = new ArrayList<>();
        archivo.add(CargaArchivo.porDefecto());
        CargarContratosMasivoEntrada entrada = new CargarContratosMasivoEntrada(1L, 1L, "APV", archivo, contratos, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
        CargarContratosMasivoResultado salida = servicio.ejecutar(entrada);
        assertThat(salida.mensajes()).hasSize(1);
        assertThat(salida.mensajes().get(0).codigo()).isEqualTo("CARGA_CONTRATOS_ERROR");
        return salida.mensajes().get(0).descripcion();
    }

    @Test
    void elProxyEnvuelveLaExcepcionChequeadaDelPar() {
        GrabarContratos proxy = conProxy(parQueLanza());
        try {
            proxy.ejecutar(null);
            throw new AssertionError("debia lanzar");
        } catch (UndeclaredThrowableException e) {
            assertThat(e.getUndeclaredThrowable()).isInstanceOf(SQLException.class).hasMessage(MENSAJE);
            assertThat(Sneaky.causa(e)).isInstanceOf(SQLException.class);
        }
    }

    @Test
    void elLlamadorVeLaMismaExcepcionConProxyQueSinProxy() {
        assertThat(mensajeDeLaCarga(parQueLanza())).isEqualTo(MENSAJE);
        assertThat(mensajeDeLaCarga(conProxy(parQueLanza()))).isEqualTo(MENSAJE);
    }
}
