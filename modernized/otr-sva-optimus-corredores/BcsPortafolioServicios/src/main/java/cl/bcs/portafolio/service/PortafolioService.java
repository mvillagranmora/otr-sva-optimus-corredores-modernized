package cl.bcs.portafolio.service;

import cl.bcs.portafolio.domain.portafolio.*;
import cl.bcs.portafolio.port.out.PortafolioCRUDPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PortafolioService {

    private final PortafolioCRUDPort port;

    public PortafolioService(PortafolioCRUDPort port) {
        this.port = port;
    }

    public CrearPortafolioResultado crearPortafolio(CrearPortafolioEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.propositoPortafolio(), "propositoPortafolio");
        String validacionCodigo = validateCodigoPortafolio(
            entrada.propositoPortafolio(),
            entrada.maximoCodigoPortafolio(),
            entrada.codigoPortafolio());
        if (!"OK".equals(validacionCodigo)) {
            return new CrearPortafolioResultado(null, null, null, validacionCodigo);
        }
        String validacionApv = validateApvPerfilCobros(entrada.propositoPortafolio(), entrada.idPerfilCobros());
        if (!"OK".equals(validacionApv)) {
            return new CrearPortafolioResultado(null, null, null, validacionApv);
        }
        return port.crearPortafolio(entrada);
    }

    public CrearPortafolioConReplicaResultado crearPortafolioConReplica(CrearPortafolioConReplicaEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.base(), "base");
        return port.crearPortafolioConReplica(entrada);
    }

    public PortafolioResultado eliminarSinTransacciones(EliminarPortafolioEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        return port.eliminarSinTransacciones(entrada);
    }

    public PortafolioResultado modificarPortafolio(ModificarPortafolioEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        return port.modificarPortafolio(entrada);
    }

    public PortafolioResultado modificarEstadoPortafolio(ModificarEstadoPortafolioEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        Objects.requireNonNull(entrada.estado(), "estado");
        return port.modificarEstadoPortafolio(entrada);
    }

    // -------------------------------------------------------------------------
    // Static helpers — extracted from legacy control flow; pinned by oracle tests
    // -------------------------------------------------------------------------

    /** B-POR-01: uses .find() (partial match), not .matches() */
    public static String validateCodigoPortafolio(String propositoPortafolio,
                                                   String maximoCodigoPortafolio,
                                                   String codigoPortafolio) {
        if ("PROPOSITOAPV".equalsIgnoreCase(propositoPortafolio)) {
            return "OK";
        }
        if (maximoCodigoPortafolio == null || maximoCodigoPortafolio.isEmpty()) {
            return "OK";
        }
        if (codigoPortafolio == null || codigoPortafolio.isEmpty()) {
            return "CODIGO_PORTAFOLIO_NO_VALIDO";
        }
        Pattern regex = Pattern.compile(maximoCodigoPortafolio);
        Matcher m = regex.matcher(codigoPortafolio);
        if (!m.find()) {
            return "CODIGO_PORTAFOLIO_NO_VALIDO";
        }
        return "OK";
    }

    /** B-POR-02: APV_EXENTO exact match (case-insensitive) */
    public static String validateApvPerfilCobros(String propositoPortafolio, String idPerfilCobros) {
        if (!"PROPOSITOAPV".equalsIgnoreCase(propositoPortafolio)) {
            return "OK";
        }
        String trimmed = (idPerfilCobros != null) ? idPerfilCobros.trim() : "";
        if (!"APV_EXENTO".equalsIgnoreCase(trimmed)) {
            return "PERFIL_COMERCIAL_APV_EXENTO_INVALIDO";
        }
        return "OK";
    }

    public static class AuthFlags {
        public String RequiereAutOrigen;
        public String RequiereAutorizacion;
        public AuthFlags(String initial) {
            this.RequiereAutOrigen = initial;
            this.RequiereAutorizacion = initial;
        }
    }

    /**
     * B-POR-03: lines 965-967 are TWO separate if-statements, not if-else.
     * When matrizAgrupadorSize > 0, BOTH RequiereAutorizacion="N" (line 965)
     * AND RequiereAutOrigen="N" (else of line 966) fire.
     */
    public static AuthFlags deriveAuthFlags(String initialValue,
                                             int matrizAgrupadorSize,
                                             String usaReglasNegocio,
                                             String propositoPortafolio) {
        AuthFlags f = new AuthFlags(initialValue);
        if (matrizAgrupadorSize > 0) {
            f.RequiereAutorizacion = "N";
        }
        if (usaReglasNegocio.compareTo("N") != 0
                && matrizAgrupadorSize == 0
                && propositoPortafolio.compareTo("PROPOSITOAPV") == 0) {
            f.RequiereAutorizacion = "S";
        } else {
            f.RequiereAutOrigen = "N";
        }
        return f;
    }

    /** B-POR-04: glosa construction */
    public static String buildGlosa(String tipo,
                                     String docId,
                                     String razonSocial,
                                     String nombres,
                                     String apPat,
                                     String apMat,
                                     String codigoPortafolio) {
        if (tipo.compareToIgnoreCase("J") == 0) {
            return docId.concat(" / ").concat(razonSocial).concat(" / CodPort:").concat(codigoPortafolio);
        } else {
            return docId.concat(" / ")
                        .concat(nombres).concat(" ")
                        .concat(apPat).concat(" ")
                        .concat(apMat)
                        .concat(" / CodPort:").concat(codigoPortafolio);
        }
    }

    /** B-POR-05: case-sensitive "S" comparison — lowercase "s" does NOT add EUR */
    public static List<String> buildCurrencyList(String monedaSistema, String crearCuentaInversionEUR) {
        List<String> lst = new ArrayList<>();
        lst.add(monedaSistema);
        lst.add("USD");
        if ("S".equals(crearCuentaInversionEUR)) {
            lst.add("EUR");
        }
        return lst;
    }

    /** B-POR-06: explicit param wins over perfil comercial fallback */
    public static String resolvePlantillaCobro(String pIdPlantillaCobro, String perfilComercialCobro) {
        if (pIdPlantillaCobro != null && !pIdPlantillaCobro.trim().isEmpty()) {
            return pIdPlantillaCobro;
        }
        return perfilComercialCobro;
    }

    public static String resolvePlantillaProducto(String pIdPlantillaProducto, String perfilComercialProducto) {
        if (pIdPlantillaProducto != null && !pIdPlantillaProducto.trim().isEmpty()) {
            return pIdPlantillaProducto;
        }
        return perfilComercialProducto;
    }

    /** B-POR-07: null → 0L */
    public static long resolveIdPersonaBrokerExtranjero(Long value) {
        if (value == null) return 0L;
        return value;
    }

    /** B-POR-08: null/empty → "ACT" */
    public static String resolveEstado(String pEstado) {
        if (pEstado == null || pEstado.isEmpty()) return "ACT";
        return pEstado;
    }

    // ---- B-POR-12/13/14: CrearPortafolioConReplica helpers ----

    /** Carrier for 3 CategoriaInversionista values to match */
    public static class CategoriaMatch {
        public final String CategoriaInvCliente;
        public final String CategoriaInvComercial;
        public final String CategoriaInvProducto;
        public CategoriaMatch(String cliente, String comercial, String producto) {
            this.CategoriaInvCliente = cliente;
            this.CategoriaInvComercial = comercial;
            this.CategoriaInvProducto = producto;
        }
        public CategoriaMatch(String cliente, String comercial) {
            this(cliente, comercial, null);
        }
    }

    /** B-POR-12: 3-way match (CrearPortafolioConReplica.java lines 327-332) */
    public static String validate3wayCategoria(CategoriaMatch c) {
        if (c != null && !(c.CategoriaInvCliente.equals(c.CategoriaInvComercial)
                           && c.CategoriaInvComercial.equals(c.CategoriaInvProducto))) {
            return "CATEGORIAS_INVERSIONISTA_REPLICA_NO_COINCIDEN";
        }
        return null;
    }

    /** B-POR-13: 2-way match (CrearPortafolioConReplica.java lines 339-344) */
    public static String validate2wayCategoria(CategoriaMatch c) {
        if (c != null && !(c.CategoriaInvCliente.equals(c.CategoriaInvComercial))) {
            return "CATEGORIAS_INVERSIONISTA_REPLICA_NO_COINCIDEN";
        }
        return null;
    }

    /** B-POR-14: blank replicaPortafolio → early return (CrearPortafolioConReplica.java lines 232-236) */
    public static boolean isReplicaPortafolioEmpty(String replicaPortafolio) {
        return replicaPortafolio == null || replicaPortafolio.trim().isEmpty();
    }

    // ---- B-POR-15: EliminarSinTransacciones helpers ----

    /** Transaction row carrier for portfolio deletion guard */
    public static class TransaccionPortafolio {
        public final Long Cantidad;
        public final String Tabla;
        public TransaccionPortafolio(long cantidad, String tabla) {
            this.Cantidad = cantidad;
            this.Tabla = tabla;
        }
    }

    /** B-POR-15: transaction presence blocks portfolio deletion (EliminarSinTransacciones.java lines 189-218) */
    public static boolean tieneTransacciones(List<TransaccionPortafolio> listadoTransacciones) {
        Map<String, Long> cantidadPorTabla = new HashMap<>();
        listadoTransacciones.forEach(t -> {
            if (t.Cantidad == 0) return;
            if (cantidadPorTabla.containsKey(t.Tabla)) {
                cantidadPorTabla.put(t.Tabla, cantidadPorTabla.get(t.Tabla) + 1);
            } else {
                cantidadPorTabla.put(t.Tabla, 1L);
            }
        });
        for (String tabla : cantidadPorTabla.keySet()) {
            if (cantidadPorTabla.get(tabla) > 0) return true;
        }
        return false;
    }
}
