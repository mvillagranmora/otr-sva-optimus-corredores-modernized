package cl.bcs.sura.service;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.service.PerfilesPortafolioUtilsService.FormaEntidad;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cl.bcs.sura.service.JsonCampos.entero;
import static cl.bcs.sura.service.JsonCampos.enteroONulo;
import static cl.bcs.sura.service.JsonCampos.listaDeSalida;
import static cl.bcs.sura.service.JsonCampos.objetos;
import static cl.bcs.sura.service.JsonCampos.texto;

/**
 * Replacement for legacy ModificarSocioNegocioModificarPerfilesPortafolio (B-SURA-07 every socio contributes its
 * document, B-SURA-14 flag p_ActualizarIndicadorFormaCobro defaults to "N" and only "N" neutralises APV, B-SURA-19
 * all writes blocked by errors, B-SURA-13 order, B-SURA-10 EntidadModificada entry).
 */
@Service
public class ModificarSocioNegocioModificarPerfilesPortafolioService {

    private final PerfilesPortafolioUtilsService utils;

    public ModificarSocioNegocioModificarPerfilesPortafolioService(PerfilesPortafolioUtilsService utils) {
        this.utils = utils;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        JsonObject salida = params.deepCopy();
        Long idEmpresa = enteroONulo(params, "p_IdEmpresa");
        boolean neutralizarApv = "N".equals(texto(params, "p_ActualizarIndicadorFormaCobro", "N"));
        List<JsonObject> socios = objetos(params, "p_SocioNegocio");

        List<String> documentos = socios.stream().map(s -> texto(s, "DocumentoIdentidad")).toList();

        List<IdsEntidadYPerfiles> ids = utils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(documentos);
        ids = utils.obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(socios, ids);

        long errores = entero(params, "P_OUT_ERRORESCONTADOR", 0L);
        JsonArray entidades = listaDeSalida(salida, "p_Out_EntidadModificada");

        if (!ids.isEmpty()) {
            errores += utils.registrarPerfilesIncompletos(ids, idEmpresa, FormaEntidad.MODIFICADA, entidades);
            if (errores == 0) {
                ids = utils.ingresarIndicadoresDeSociosNegocio(socios, ids);
                if (neutralizarApv) ids = utils.validarIndicadorFormaCobroPortafolioApv(ids);
                utils.modificarIndicadoresCobroCustodia(ids);
                utils.modificarPerfilesPortafolio(ids);
            }
        }
        salida.addProperty("P_OUT_ERRORESCONTADOR", errores);
        return salida;
    }
}
