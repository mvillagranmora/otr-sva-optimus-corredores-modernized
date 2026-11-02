package cl.bcs.sura.service;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
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
import static cl.bcs.sura.service.PerfilesPortafolioUtilsService.valorCobrocust;

/**
 * Replacement for legacy CrearClienteFullWSModificarPerfilesPortafolio. B-SURA-07 only the first NumeroDocumento of
 * each socio is queried, B-SURA-12 the match uses ALL documents of the socio and the first COBROCUST of
 * DatosCliente.DatosPortafolio.PreferenciasNegocio wins, B-SURA-16 warnings are recounted and the output has exactly
 * three fields, B-SURA-19 indicators are written before the missing-profile check, B-SURA-13 order.
 */
@Service
public class CrearClienteFullWSModificarPerfilesPortafolioService {

    private static final String ADVERTENCIA = "ADVERTENCIA";

    private final PerfilesPortafolioUtilsService utils;

    public CrearClienteFullWSModificarPerfilesPortafolioService(PerfilesPortafolioUtilsService utils) {
        this.utils = utils;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        Long idEmpresa = enteroONulo(params, "p_IdEmpresa");
        List<JsonObject> socios = objetos(params, "p_SocioNegocio");

        long errores = entero(params, "P_OUT_ERRORESCONTADOR", 0L);
        JsonArray entidades = lista(params, "P_OUT_ENTIDADCREADA").deepCopy();
        long advertencias = contarAdvertencias(entidades);   // counted before any error entity is appended

        List<String> documentos = new ArrayList<>();
        for (JsonObject socio : socios) {
            JsonArray tipos = lista(socio, "TiposDocumento");
            if (!tipos.isEmpty()) documentos.add(texto(tipos.get(0).getAsJsonObject(), "NumeroDocumento"));
        }

        List<IdsEntidadYPerfiles> ids = utils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(documentos);
        ids = ingresarIndicadores(socios, ids);
        utils.modificarIndicadoresCobroCustodia(ids);

        if (!ids.isEmpty()) {
            ids = utils.obtenerEntidadesYPerfilesCompletas(ids);
            errores += utils.registrarPerfilesIncompletos(ids, idEmpresa, FormaEntidad.CREADA_WS, entidades);
            if (errores == 0) utils.modificarPerfilesPortafolio(ids);
        }

        JsonObject salida = new JsonObject();
        salida.addProperty("P_OUT_ERRORESCONTADOR", errores);
        salida.addProperty("P_OUT_ADVERTENCIASCONTADOR", advertencias);
        salida.add("P_OUT_ENTIDADCREADA", entidades);
        return salida;
    }

    private List<IdsEntidadYPerfiles> ingresarIndicadores(List<JsonObject> socios, List<IdsEntidadYPerfiles> ids) {
        List<IdsEntidadYPerfiles> resultado = new ArrayList<>();
        for (JsonObject socio : socios) {
            List<String> documentos = new ArrayList<>();
            for (JsonElement t : lista(socio, "TiposDocumento")) documentos.add(texto(t.getAsJsonObject(), "NumeroDocumento"));
            List<IdsEntidadYPerfiles> perfiles = ids.stream()
                    .filter(id -> documentos.contains(id.documentoIdentidad))
                    .collect(java.util.stream.Collectors.toList());

            Optional<String> valor = valorCobrocust(preferenciasNegocio(socio));
            if (valor.isPresent() && !perfiles.isEmpty()) {
                perfiles.forEach(p -> p.idFormaCobro = valor.get());
                utils.ingresarIndicadoresCobroCustodia(perfiles, valor.get());
            }
            resultado.addAll(perfiles);
        }
        return resultado;
    }

    private static JsonArray preferenciasNegocio(JsonObject socio) {
        JsonElement cliente = socio.get("DatosCliente");
        if (cliente == null || !cliente.isJsonObject()) return new JsonArray();
        JsonElement portafolio = cliente.getAsJsonObject().get("DatosPortafolio");
        if (portafolio == null || !portafolio.isJsonObject()) return new JsonArray();
        return lista(portafolio.getAsJsonObject(), "PreferenciasNegocio");
    }

    private static long contarAdvertencias(JsonArray entidades) {
        long total = 0;
        for (JsonElement e : entidades) {
            for (JsonElement m : lista(e.getAsJsonObject(), "Mensajes")) {
                if (ADVERTENCIA.equals(texto(m.getAsJsonObject(), "TipoMensaje"))) total++;
            }
        }
        return total;
    }
}
