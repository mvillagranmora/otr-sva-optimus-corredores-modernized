package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.adapter.in.InternalJavaCallController;
import cl.bcs.contabilidad.service.*;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Dispatch of the legacy J-type JavaCall class names (cl.bcs.contabilidad.&lt;Name&gt;, one per legacy file
 * legacy/.../BcsContabilidadServicios/src/cl/bcs/contabilidad/&lt;Name&gt;.java, each "implements JavaCall") to the
 * modern service. Legacy JavaCall.call(con, jin) receives the p_* parameters at the top level of jin
 * (e.g. AnularCentralizacion.java:80-82), so the params object must reach the service unchanged and the
 * service JSON must come back unchanged.
 */
class InternalJavaCallControllerDispatchTest {

    final Map<Class<?>, Object> mocks = new LinkedHashMap<>();

    InternalJavaCallController controller() {
        Class<?>[] types = InternalJavaCallController.class.getConstructors()[0].getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Object m = mock(types[i]);
            mocks.put(types[i], m);
            args[i] = m;
        }
        try {
            return (InternalJavaCallController) InternalJavaCallController.class.getConstructors()[0].newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @ParameterizedTest(name = "cl.bcs.contabilidad.{0}")
    @ValueSource(strings = {"AgregarCuentasPorMetadata", "AgregarMetadata", "AnularCentralizacion", "CargarAsientoComprobante",
            "CargarCabeceraComprobante", "CargarDeterminanteComprobante", "CentralizaGeneraCabecera", "CentralizaMultiSeleccion",
            "ConfigurarConceptoNormativo", "ConsultarConceptoNormativo", "ConsultarConfiguracionesConceptoNormativo",
            "CuadraturaCierreDiario", "EditarCuentaBasilea", "EditarMetadata", "EliminarMetadata", "EnviarPreComprobante",
            "FiltrarCuentas", "GeneraPreComprobanteMultiSeleccion", "HomologarCuentasContables"})
    @DisplayName("CONT-G-480 legacy JavaCall class name routes to its service with params unchanged; service JSON returned")
    void contG480_dispatch(String legacyName) throws Exception {
        InternalJavaCallController c = controller();
        Class<?> svcType = Class.forName("cl.bcs.contabilidad.service." + legacyName + "Service");
        Object svc = mocks.get(svcType);
        assertThat(svc).as("controller has a dependency on %s", svcType.getSimpleName()).isNotNull();
        Method execute = svcType.getMethod("execute", JsonObject.class);
        JsonObject result = new JsonObject();
        result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        result.addProperty("marker", legacyName);
        when(execute.invoke(svc, any(JsonObject.class))).thenReturn(result);

        String body = "{\"class\":\"cl.bcs.contabilidad." + legacyName + "\",\"params\":{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\"}}";
        ResponseEntity<String> resp = c.dispatch(body, null);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(Out.json(resp.getBody())).isEqualTo(result);
        JsonObject expectedParams = Out.json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\"}");
        execute.invoke(verify(svc), expectedParams);
        for (Object other : mocks.values()) if (other != svc) verifyNoInteractions(other);
    }
}
