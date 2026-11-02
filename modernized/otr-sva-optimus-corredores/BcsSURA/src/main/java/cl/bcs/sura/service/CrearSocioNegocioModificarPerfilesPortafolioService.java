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
 * Replacement for legacy CrearSocioNegocioModificarPerfilesPortafolio (B-SURA-07 only CLIENTE documents, B-SURA-08/19
 * writes only when the error counter is 0, B-SURA-09/10 error entry, B-SURA-11/12 seed and COBROCUST, B-SURA-13 order).
 * Never neutralises APV (no B-SURA-14 flag here).
 */
@Service
public class CrearSocioNegocioModificarPerfilesPortafolioService {

    private final PerfilesPortafolioUtilsService utils;

    public CrearSocioNegocioModificarPerfilesPortafolioService(PerfilesPortafolioUtilsService utils) {
        this.utils = utils;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        JsonObject salida = params.deepCopy();
        Long idEmpresa = enteroONulo(params, "p_IdEmpresa");
        List<JsonObject> socios = objetos(params, "p_SocioNegocio");

        List<String> documentos = socios.stream()
                .filter(s -> "CLIENTE".equals(texto(s, "TipoEntidad")))
                .map(s -> texto(s, "DocumentoIdentidad"))
                .toList();

        List<IdsEntidadYPerfiles> ids = utils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(documentos);
        ids = utils.obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(socios, ids);

        long errores = entero(params, "P_OUT_ERRORESCONTADOR", 0L);
        JsonArray entidades = listaDeSalida(salida, "p_Out_EntidadCreada");

        if (!ids.isEmpty()) {
            errores += utils.registrarPerfilesIncompletos(ids, idEmpresa, FormaEntidad.CREADA, entidades);
            if (errores == 0) {
                ids = utils.ingresarIndicadoresDeSociosNegocio(socios, ids);
                utils.modificarIndicadoresCobroCustodia(ids);
                utils.modificarPerfilesPortafolio(ids);
            }
        }
        salida.addProperty("P_OUT_ERRORESCONTADOR", errores);
        return salida;
    }
}
