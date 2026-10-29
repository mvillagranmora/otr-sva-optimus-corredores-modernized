package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.ModificarRelacionLaboral;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.modificarrelacionlaboral.ModificarRelacionLaboralEntrada;
import cl.bcs.apv.api.dto.modificarrelacionlaboral.ModificarRelacionLaboralResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.contratos.RelacionLaboralRepositorio;

/**
 * Use case {@code ModificarRelacionLaboral}: legacy {@code cl.bcs.APV.ModificarRelacionLaboral}. The legacy {@code call()} never sets the error counter:
 * the outcome carries no counter ({@code P_OUT_ERRORESCONTADOR} absent from the JSON).
 */
@Transactional(rollbackFor = Exception.class)
public class ModificarRelacionLaboralServicio implements ModificarRelacionLaboral {

    private final Colaboradores colaboradores;
    private final RelacionLaboralRepositorio repositorio;

    public ModificarRelacionLaboralServicio(Colaboradores colaboradores, RelacionLaboralRepositorio repositorio) {
        this.colaboradores = colaboradores;
        this.repositorio = repositorio;
    }

    @Override
    public ModificarRelacionLaboralResultado ejecutar(ModificarRelacionLaboralEntrada entrada) {
        List<Mensaje> mensajes = new ArrayList<>();
        // VALIDACIONES INICIALES
        JsonObject basicos = colaboradores.invocar("RevisarRequisitosBasicos", Json.objeto()
                .poner("p_IdEmpresa", entrada.idEmpresa())
                .poner("p_IdUsuario", entrada.idUsuario())
                .poner("p_IdModulo", entrada.idModulo())
                .construir());
        List<Mensaje> mensajesBasicos = Json.mensajes(basicos, "p_out_Mensajes");
        if (Mensajes.numeroErrores(mensajesBasicos) > 0) {
            mensajes.addAll(mensajesBasicos);
        }
        if (mensajes.size() > 0) {
            return new ModificarRelacionLaboralResultado(mensajes, null);
        }
        // LOGICA
        repositorio.insertar(entrada.relacionLaboral());
        return new ModificarRelacionLaboralResultado(mensajes, null);
    }
}
