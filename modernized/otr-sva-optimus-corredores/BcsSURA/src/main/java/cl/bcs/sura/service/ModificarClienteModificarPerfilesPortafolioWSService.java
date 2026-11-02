package cl.bcs.sura.service;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.port.out.SuraDataPort.Variante;
import cl.bcs.sura.service.PerfilesPortafolioUtilsService.FormaEntidad;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cl.bcs.sura.service.JsonCampos.entero;
import static cl.bcs.sura.service.JsonCampos.enteroONulo;
import static cl.bcs.sura.service.JsonCampos.lista;
import static cl.bcs.sura.service.JsonCampos.objetos;
import static cl.bcs.sura.service.JsonCampos.texto;
import static cl.bcs.sura.service.PerfilesPortafolioUtilsService.perfilesDe;
import static cl.bcs.sura.service.PerfilesPortafolioUtilsService.valorCobrocust;

/**
 * Replacement for legacy ModificarClienteModificarPerfilesPortafolioWS. B-SURA-14 flag defaults to "S" here,
 * B-SURA-15 DatosPortafolio loop quirks, B-SURA-17 output of exactly three fields, B-SURA-18 merge/NoSuchElement,
 * B-SURA-19 indicators are written before the missing-profile check, B-SURA-10 EntidadManipulada with raw messages.
 */
@Service
public class ModificarClienteModificarPerfilesPortafolioWSService {

    private final PerfilesPortafolioUtilsService utils;

    public ModificarClienteModificarPerfilesPortafolioWSService(PerfilesPortafolioUtilsService utils) {
        this.utils = utils;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        Long idEmpresa = enteroONulo(params, "p_IdEmpresa");
        boolean neutralizarApv = "N".equals(texto(params, "p_ActualizarIndicadorFormaCobro", "S"));
        List<JsonObject> socios = objetos(params, "p_SocioNegocio");

        long errores = entero(params, "P_OUT_ERRORESCONTADOR", 0L);
        long advertencias = entero(params, "P_OUT_ADVERTENCIASCONTADOR", 0L);
        JsonArray entidades = lista(params, "P_OUT_ENTIDADES_MANIPULADAS").deepCopy();

        List<String> documentos = socios.stream().map(s -> texto(s, "DocumentoIdentidad")).toList();
        List<IdsEntidadYPerfiles> ids = utils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(documentos);
        ids = ingresarIndicadores(socios, ids);
        if (neutralizarApv) ids = utils.validarIndicadorFormaCobroPortafolioApv(ids);
        utils.modificarIndicadoresCobroCustodia(ids, Variante.WS);

        if (!ids.isEmpty()) {
            ids = utils.obtenerEntidadesYPerfilesCompletas(ids);
            errores += utils.registrarPerfilesIncompletos(ids, idEmpresa, FormaEntidad.MANIPULADA, entidades);
            if (errores == 0) utils.modificarPerfilesPortafolio(ids);
        }

        JsonObject salida = new JsonObject();
        salida.addProperty("P_OUT_ERRORESCONTADOR", errores);
        salida.addProperty("P_OUT_ADVERTENCIASCONTADOR", advertencias);
        salida.add("P_OUT_ENTIDADES_MANIPULADAS", entidades);
        return salida;
    }

    /**
     * Legacy quirks kept on purpose (B-SURA-15): the matches are accumulated INSIDE the DatosPortafolio loop, so a socio
     * without DatosPortafolio contributes nothing and one with N entries contributes its portfolios N times; every entry
     * with a COBROCUST re-sets the IdFormaCobro and inserts again with its own value.
     */
    private List<IdsEntidadYPerfiles> ingresarIndicadores(List<JsonObject> socios, List<IdsEntidadYPerfiles> ids) {
        List<IdsEntidadYPerfiles> resultado = new ArrayList<>();
        for (JsonObject socio : socios) {
            List<IdsEntidadYPerfiles> perfiles = perfilesDe(texto(socio, "DocumentoIdentidad"), ids);
            for (JsonElement el : lista(socio, "DatosPortafolio")) {
                Optional<String> valor = valorCobrocust(lista(el.getAsJsonObject(), "Indicadores"));
                if (valor.isPresent() && !perfiles.isEmpty()) {
                    perfiles.forEach(p -> p.idFormaCobro = valor.get());
                    utils.ingresarIndicadoresCobroCustodia(perfiles, valor.get(), Variante.WS);
                }
                resultado.addAll(perfiles);
            }
        }
        return resultado;
    }
}
