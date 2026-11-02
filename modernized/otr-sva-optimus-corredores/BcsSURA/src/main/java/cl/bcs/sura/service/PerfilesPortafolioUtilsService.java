package cl.bcs.sura.service;

import cl.bcs.sura.adapter.out.LegacyHanaSuraPort;
import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.port.out.SuraDataPort;
import cl.bcs.sura.port.out.SuraDataPort.Variante;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cl.bcs.sura.service.JsonCampos.lista;
import static cl.bcs.sura.service.JsonCampos.texto;

/**
 * Replacement for legacy ModificarPerfilesPortafolioUtils. Behaviors B-SURA-07 (documents), B-SURA-08 (profile
 * writes), B-SURA-09/10 (missing-profile entries), B-SURA-11 (temp-table seed), B-SURA-12 (COBROCUST),
 * B-SURA-18 (merge of IdFormaCobro). All persistence goes through {@link SuraDataPort}.
 */
@Component
public class PerfilesPortafolioUtilsService {

    public static final String CODIGO_PERFILES_NO_ENCONTRADOS = "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS";
    private static final String COBROCUST = "COBROCUST";

    /** The four shapes legacy uses for the "profile not found" entity (agregarMensajeError*), B-SURA-10. */
    public enum FormaEntidad {
        /** CrearSocioNegocio.EntidadCreada: Estado/Nuevo empty, messages Codigo/Mensaje/TipoMensaje. */
        CREADA,
        /** ModificarSocioNegocio.EntidadModificada: no Estado/Nuevo. */
        MODIFICADA,
        /** WebServiceUtils.EntidadManipulada: messages are raw MensajeSalida (codigo/descripcion/tipo). */
        MANIPULADA,
        /** CrearClienteFullWS.EntidadCreada: like CREADA plus IdInterno, without Estado/Nuevo. */
        CREADA_WS
    }

    private final SuraDataPort port;

    @Autowired
    public PerfilesPortafolioUtilsService(SuraDataPort port) {
        this.port = port;
    }

    // ------------------------------------------------------------------ queries

    public List<IdsEntidadYPerfiles> consultarIdsPortafolioIdClientePorDocumentoIdentidad(List<String> documentosIdentidad) {
        return port.consultarIdsPortafolioIdCliente(documentosIdentidad);
    }

    /**
     * Seeds the COBROCUST value on the portfolios of each socio (in input order, duplicates kept), asks the catalogue
     * join and drops rows that still have no IdFormaCobro (B-SURA-11).
     */
    public List<IdsEntidadYPerfiles> obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(
            List<JsonObject> sociosNegocio, List<IdsEntidadYPerfiles> ids) {
        List<IdsEntidadYPerfiles> semilla = new ArrayList<>();
        for (JsonObject socio : sociosNegocio) {
            List<IdsEntidadYPerfiles> perfiles = perfilesDe(texto(socio, "DocumentoIdentidad"), ids);
            valorCobrocust(lista(socio, "IndicadorPortafolio")).ifPresent(valor -> {
                if (!perfiles.isEmpty()) perfiles.forEach(p -> p.idFormaCobro = valor);
            });
            semilla.addAll(perfiles);
        }
        return port.consultarPerfilesFiltrados(semilla).stream()
                .filter(p -> !p.idFormaCobro.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * One document per id (duplicates kept); every returned row must have its in-memory counterpart, otherwise
     * NoSuchElementException (legacy findFirst().get()), and an empty IdFormaCobro is filled from it (B-SURA-18).
     */
    public List<IdsEntidadYPerfiles> obtenerEntidadesYPerfilesCompletas(List<IdsEntidadYPerfiles> ids) {
        List<String> documentos = ids.stream().map(id -> id.documentoIdentidad).collect(Collectors.toList());
        List<IdsEntidadYPerfiles> entidades = port.consultarPerfilesComerciales(documentos);
        for (IdsEntidadYPerfiles entidad : entidades) {
            String formaEnMemoria = ids.stream()
                    .filter(id -> id.idPortafolio.equals(entidad.idPortafolio))
                    .findFirst()
                    .orElseThrow()
                    .idFormaCobro;
            if (entidad.idFormaCobro.isEmpty()) entidad.idFormaCobro = formaEnMemoria;
        }
        return entidades;
    }

    // ------------------------------------------------------------------ writes

    public void modificarPerfilesPortafolio(List<IdsEntidadYPerfiles> perfiles) {
        port.modificarPerfilesPortafolio(perfiles);
    }

    public void ingresarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> ids, String valorIndicador) {
        port.ingresarIndicadoresCobroCustodia(ids, valorIndicador);
    }

    public void ingresarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> ids, String valorIndicador, Variante variante) {
        port.ingresarIndicadoresCobroCustodia(ids, valorIndicador, variante);
    }

    public void modificarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> ids) {
        port.modificarIndicadoresCobroCustodia(ids);
    }

    public void modificarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> ids, Variante variante) {
        port.modificarIndicadoresCobroCustodia(ids, variante);
    }

    public List<IdsEntidadYPerfiles> validarIndicadorFormaCobroPortafolioApv(List<IdsEntidadYPerfiles> ids) {
        return PerfilesPortafolioService.validarIndicadorFormaCobroPortafolioApv(ids);
    }

    /**
     * Shared by CrearSocioNegocio and ModificarSocioNegocio (legacy: the latter calls the former). For each socio, the
     * first COBROCUST of IndicadorPortafolio (even if empty) becomes the IdFormaCobro of its portfolios and is inserted
     * for those without indicator; the returned list is the concatenation of the per-socio matches (B-SURA-12).
     */
    public List<IdsEntidadYPerfiles> ingresarIndicadoresDeSociosNegocio(
            List<JsonObject> sociosNegocio, List<IdsEntidadYPerfiles> ids) {
        List<IdsEntidadYPerfiles> resultado = new ArrayList<>();
        for (JsonObject socio : sociosNegocio) {
            List<IdsEntidadYPerfiles> perfiles = perfilesDe(texto(socio, "DocumentoIdentidad"), ids);
            Optional<String> valor = valorCobrocust(lista(socio, "IndicadorPortafolio"));
            if (valor.isPresent() && !perfiles.isEmpty()) {
                perfiles.forEach(p -> p.idFormaCobro = valor.get());
                ingresarIndicadoresCobroCustodia(perfiles, valor.get());
            }
            resultado.addAll(perfiles);
        }
        return resultado;
    }

    // ------------------------------------------------------------------ missing-profile entries (B-SURA-09/10)

    /**
     * Appends one entry to {@code destino} for every row lacking IdPerfilComercial, IdPerfilProductos or IdPerfilCobros
     * and returns how many were appended (the caller adds them to its error counter).
     */
    public int registrarPerfilesIncompletos(List<IdsEntidadYPerfiles> ids, Long idEmpresa, FormaEntidad forma, JsonArray destino) {
        int errores = 0;
        for (IdsEntidadYPerfiles id : ids) {
            if (id.idPerfilComercial.isEmpty() || id.idPerfilProductos.isEmpty() || id.idPerfilCobros.isEmpty()) {
                destino.add(entidadConError(id, idEmpresa, forma));
                errores++;
            }
        }
        return errores;
    }

    private JsonObject entidadConError(IdsEntidadYPerfiles id, Long idEmpresa, FormaEntidad forma) {
        Map<String, String> parametros = new HashMap<>();
        parametros.put("IdFormaCobro", id.idFormaCobro);
        parametros.put("TipoEntidad", "PORTAFOLIO");
        parametros.put("IdEntidad", id.idPortafolio.toString());
        Map<String, String> ms = port.resolverMensajeError(id.idCliente, idEmpresa, CODIGO_PERFILES_NO_ENCONTRADOS, parametros);

        JsonObject mensaje = new JsonObject();
        if (forma == FormaEntidad.MANIPULADA) {
            mensaje.addProperty("codigo", ms.get("codigo"));
            mensaje.addProperty("descripcion", ms.get("descripcion"));
            mensaje.addProperty("tipo", ms.get("tipo"));
        } else {
            mensaje.addProperty("IdInterno", 0L);
            mensaje.addProperty("Codigo", ms.get("codigo"));
            mensaje.addProperty("Mensaje", ms.get("descripcion"));
            mensaje.addProperty("TipoMensaje", ms.get("tipo"));
        }
        JsonArray mensajes = new JsonArray();
        mensajes.add(mensaje);

        JsonObject entidad = new JsonObject();
        entidad.addProperty("TipoEntidad", parametros.get("TipoEntidad"));
        entidad.addProperty("IdEntidad", id.idPortafolio);
        if (forma == FormaEntidad.CREADA) {
            entidad.addProperty("Estado", "");
            entidad.addProperty("Nuevo", "");
        }
        entidad.add("Mensajes", mensajes);
        if (forma == FormaEntidad.CREADA_WS) entidad.addProperty("IdInterno", 0L);
        return entidad;
    }

    // ------------------------------------------------------------------ helpers shared by the four flows

    /** Portfolios whose document equals {@code documento}; a fresh list over the same (mutable) row objects. */
    static List<IdsEntidadYPerfiles> perfilesDe(String documento, List<IdsEntidadYPerfiles> ids) {
        return ids.stream().filter(id -> documento.equals(id.documentoIdentidad)).collect(Collectors.toList());
    }

    /** Value of the FIRST COBROCUST indicator, even when empty (legacy findFirst() without emptiness check). */
    static Optional<String> valorCobrocust(JsonArray indicadores) {
        for (JsonElement e : indicadores) {
            JsonObject indicador = e.getAsJsonObject();
            if (COBROCUST.equals(texto(indicador, "CodigoTipoIndicador"))) {
                return Optional.of(texto(indicador, "ValorIndicador"));
            }
        }
        return Optional.empty();
    }
}
