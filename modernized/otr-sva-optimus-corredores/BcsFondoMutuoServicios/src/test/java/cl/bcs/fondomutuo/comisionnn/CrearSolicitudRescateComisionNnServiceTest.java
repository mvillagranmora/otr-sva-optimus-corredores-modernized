package cl.bcs.fondomutuo.comisionnn;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.cierre3b.Caso3B;
import cl.bcs.fondomutuo.cierre3b.PuertosGuionados;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.requisitos.RequisitosJdbc;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;

/** FM-COM / FM-NOR-46: CrearSolicitudRescateComisionNN against the goldens recorded from the legacy class (cases3b/COM-NN-nn); 3A collaborators scripted. */
class CrearSolicitudRescateComisionNnServiceTest {

    static List<String> casos() {
        return Caso3B.casos("CrearSolicitudRescateComisionNN");
    }

    static JsonObject sujeto(Caso3B.Contexto ctx, JsonObject entrada) throws Exception {
        ComisionNnContrato.Entrada in = JsonLegacy.plano().fromJson(entrada.toString(), ComisionNnContrato.Entrada.class);
        RequisitosJdbc requisitosDatos = new RequisitosJdbc(ctx.conexion);
        ServicioCumpleRequisitosService cumple = new ServicioCumpleRequisitosService(requisitosDatos, ctx.diccionario, ctx.configuracion,
                e -> ctx.guion.responder("ConsultarProductosPortafolio", e));
        CrearSolicitudRescateComisionNnService s = new CrearSolicitudRescateComisionNnService(ctx.revisar, ctx.configuracion,
                id -> JsonLegacy.plano().fromJson(ctx.guion.responder("ConsultarEmpresa", new JsonPrimitive(id)), InformacionEmpresa.class),
                ctx.diccionario, new ComisionNnJdbc(ctx.conexion), requisitosDatos, PuertosGuionados.fechas(ctx.guion), PuertosGuionados.cuotas(ctx.guion),
                new SecuenciaH2(ctx.con, ctx.secuencias), cumple, ctx.calendario, e -> ctx.guion.responder("AgregarLog", e));
        return JsonParser.parseString(JsonLegacy.plano().toJson(s.logica(in))).getAsJsonObject();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FM-COM / FM-NOR-46 equivalence with the recorded legacy")
    void equivalencia(String idCaso) throws Exception {
        Caso3B.verificar(idCaso, CrearSolicitudRescateComisionNnServiceTest::sujeto);
    }
}
