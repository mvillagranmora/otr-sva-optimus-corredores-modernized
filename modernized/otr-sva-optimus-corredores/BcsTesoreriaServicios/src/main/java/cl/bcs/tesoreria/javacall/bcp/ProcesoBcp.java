package cl.bcs.tesoreria.javacall.bcp;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * The BCP depositor (client) of one inquiry / payment line, resolved against the client tables, and the validation that both
 * ConsultaBCP and RecaudacionBCP apply to it (legacy {@code Consulta_Proceso} / {@code Operacion_Proceso} and their two copies of
 * {@code ProcesaRequisito...BCP}). Used by TES-B-64..96.
 */
public class ProcesoBcp {

    /** A client as the person/client tables describe it (SQL RecaudacionBCP_SelectCliente). */
    public static class Cliente {
        public String LlaveCliente;            // IdEmpresa + document type + document ('1DNI12969673', '1RUC20101031340')
        public Long IdPersona;
        public Long IdCliente;
        public String NombreCompleto;
        public String IdEstadoPersona;
        public String IdEstadoCliente;
        public String IdTipoDocumentoIdentidad;
        public String DocumentoIdentidad;
        public Long IdPortafolioDefecto;
        public String IdTipoSocioDeNegocio;
    }

    public String LlaveCliente;
    public Long IdMensajeTransaccion;
    public Long IdPersona;
    public Long IdCliente;
    public String IdServicioBcp;
    public String IdClienteBcp;
    public String NombreCliente;
    public String IdTipoDocumentoIdentidad;
    public String DocumentoIdentidad;
    public String IdEstadoPersona;
    public Long IdPortafolioDefecto;
    public String IdEstadoCliente;
    public String IdTipoSocioDeNegocio;
    public String IdMoneda;
    public BigDecimal Monto;
    public long errores;

    private static final String SQL_SELECT_CLIENTE = SqlNombrado.recurso("RecaudacionBCP_SelectCliente");

    /** Legacy {@code isNumeric}: true when {@code Double.parseDouble} accepts it (so "12345678d" is numeric; null throws NullPointerException). */
    public static boolean esNumerico(String texto) {
        try {
            Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Removes the left zeros of a numeric BCP depositor id ("0000012969" gives "12969"); a non numeric id is kept as is.
     * "12345678d" is numeric for {@code esNumerico} and then fails in {@code Long.parseLong}: NumberFormatException, as in the legacy.
     */
    public static String sinCerosIzquierda(String idClienteBcp) {
        return esNumerico(idClienteBcp) ? Long.valueOf(idClienteBcp).toString() : idClienteBcp;
    }

    /** Client key: 8 characters is a natural person (DNI), 11 a legal person (RUC), anything else has no key (blank, rejected by the validation). */
    public static String llaveCliente(Long idEmpresa, String idClienteBcp) {
        if (idClienteBcp.length() == 8) {
            return idEmpresa + "DNI" + idClienteBcp;
        }
        if (idClienteBcp.length() == 11) {
            return idEmpresa + "RUC" + idClienteBcp;
        }
        return "";
    }

    /**
     * The clients of the given depositor ids by client key. Ids that have no key are not queried (the legacy added a blank or stale key to the
     * IN list; it matches nothing and the rows are the same). Two rows with the same key throw {@code Exception("key field is not unique")}.
     */
    public static Map<String, Cliente> consultarClientes(SqlNombrado sql, Long idEmpresa, List<String> idsClienteBcp) throws Exception {
        List<String> llaves = idsClienteBcp.stream().map(id -> llaveCliente(idEmpresa, id)).filter(l -> !l.isEmpty()).toList();
        List<Cliente> filas = sql.consultar(SQL_SELECT_CLIENTE, Map.of("llavesClientes", llaves), Cliente.class);
        Map<String, Cliente> porLlave = new HashMap<>();
        for (Cliente c : filas) {
            if (porLlave.put(c.LlaveCliente, c) != null) {
                throw new Exception("key field is not unique"); // Utils.List2Hash of the legacy
            }
        }
        return porLlave;
    }

    /** Fills the client data of this line; without client every field is blank / zero and the validation rejects it. */
    public void asignarCliente(String llave, Cliente c, Long idMensajeTransaccion) {
        LlaveCliente = llave;
        IdPersona = c == null ? 0 : c.IdPersona;
        IdCliente = c == null ? 0 : c.IdCliente;
        IdPortafolioDefecto = c == null ? 0 : c.IdPortafolioDefecto;
        IdTipoSocioDeNegocio = c == null ? "" : c.IdTipoSocioDeNegocio;
        IdEstadoPersona = c == null ? "" : c.IdEstadoPersona;
        IdEstadoCliente = c == null ? "" : c.IdEstadoCliente;
        NombreCliente = c == null ? "" : c.NombreCompleto;
        IdTipoDocumentoIdentidad = c == null ? "" : c.IdTipoDocumentoIdentidad;
        DocumentoIdentidad = c == null ? "" : c.DocumentoIdentidad;
        IdMensajeTransaccion = idMensajeTransaccion;
    }

    /** The six client checks, in the legacy order; every failure adds an ERROR message with the given BCP code. */
    public void validar(DiccionarioMensajes diccionario, Long idEmpresa, String codigoBcp, boolean conNombrePersona, List<MensajeSalida> mensajes) {
        requisito(diccionario, idEmpresa, LlaveCliente == null || LlaveCliente.isEmpty(), "TIPO_DOCUMENTO_IDENTIDAD_DESCONOCIDO", codigoBcp, conNombrePersona, mensajes);
        requisito(diccionario, idEmpresa, IdPersona == null || IdPersona == 0, "PERSONA_NO_EXISTE", codigoBcp, conNombrePersona, mensajes);
        requisito(diccionario, idEmpresa, (IdCliente == null || IdCliente == 0) && IdPersona != 0, "CLIENTE_NO_EXISTE", codigoBcp, conNombrePersona, mensajes);
        requisito(diccionario, idEmpresa, !IdEstadoPersona.equalsIgnoreCase("ACT") && IdPersona != 0, "PERSONA_NO_ACTIVA", codigoBcp, conNombrePersona, mensajes);
        requisito(diccionario, idEmpresa, !IdEstadoCliente.equalsIgnoreCase("ACT") && IdPersona != 0 && IdCliente != 0, "CLIENTE_NO_ACTIVO", codigoBcp, conNombrePersona, mensajes);
        requisito(diccionario, idEmpresa, IdPortafolioDefecto == 0 && IdPersona != 0 && IdCliente != 0, "PORTAFOLIO_DEFECTO_NO_EXISTE", codigoBcp, conNombrePersona, mensajes);
    }

    /** Adds the ERROR message of a failed requirement and counts it. LEGACY DEFECT kept (see README TES-B-72): the placeholders IdCliente, IdPersona and TipoDocumentoIdentidad all carry the BCP depositor id. */
    public void requisito(DiccionarioMensajes diccionario, Long idEmpresa, boolean falla, String codigoMensaje, String codigoBcp, boolean conNombrePersona,
            List<MensajeSalida> mensajes) {
        if (!falla) {
            return;
        }
        Plataforma.Valores valores = Plataforma.valores()
                .con("IdCliente", IdClienteBcp)
                .con("IdPersona", IdClienteBcp)
                .con("TipoDocumentoIdentidad", IdClienteBcp);
        if (conNombrePersona) { // only ConsultaBCP passed NombrePersona
            valores.con("NombrePersona", NombreCliente);
        }
        // the message code selects the dictionary text, the BCP code is what the answer carries
        mensajes.add(new MensajeSalida(IdMensajeTransaccion, MensajeSalida.MENSAJE_ERROR, codigoBcp, diccionario.mensaje(idEmpresa, codigoMensaje, valores)));
        errores++;
    }
}
