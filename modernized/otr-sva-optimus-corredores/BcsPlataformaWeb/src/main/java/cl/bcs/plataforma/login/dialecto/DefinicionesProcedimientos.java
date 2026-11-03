package cl.bcs.plataforma.login.dialecto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.plataforma.login.dialecto.DefinicionProcedimiento.Atributo;
import cl.bcs.plataforma.login.dialecto.DefinicionProcedimiento.Parametro;

/**
 * The eight procedures the login calls in Postgres, written in Java (no file): the signature of each (position, mode, legacy HANA name, Postgres name, type),
 * the labels HANA gave to the columns of the formats function and the attributes of the four composite types of the event procedure. Checked against the
 * Postgres catalog by the development tests, not at run time.
 */
final class DefinicionesProcedimientos {

    private static final Map<String, DefinicionProcedimiento> TODAS = new LinkedHashMap<>();

    static {
        TODAS.put("BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin", new DefinicionProcedimiento("BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin", "sp_pla_acc_consultar_usuario_admin", false,
                List.of(
                        new Parametro(1, "IN", "p_id_empresa", "p_id_empresa", "bigint"),
                        new Parametro(2, "IN", "p_id_usuario", "p_id_usuario", "bigint"),
                        new Parametro(3, "OUT", "P_OUT_ESADMIN", "p_out_es_admin", "character varying")),
                Map.of(),
                Map.of(),
                null));
        TODAS.put("BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion", new DefinicionProcedimiento("BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion", "sp_pla_obtener_configuracion", false,
                List.of(
                        new Parametro(1, "IN", "p_id_empresa", "p_id_empresa", "bigint"),
                        new Parametro(2, "IN", "p_codigo_parametro", "p_codigo_parametro", "character varying"),
                        new Parametro(3, "OUT", "P_OUT_VALOR", "p_out_valor", "character varying")),
                Map.of(),
                Map.of(),
                null));
        TODAS.put("BSCL.Plataforma.BD.Servicios::ConsultarFormatos", new DefinicionProcedimiento("BSCL.Plataforma.BD.Servicios::ConsultarFormatos", "fn_pla_consultar_formatos", true,
                List.of(),
                Map.ofEntries(
                        Map.entry("id_uso_moneda_formato", "IdUsoMonedaFormato"),
                        Map.entry("id_moneda", "IdMoneda"),
                        Map.entry("id_tipo_instrumento", "IdTipoInstrumento"),
                        Map.entry("id_tipo_operacion", "IdTipoOperacion"),
                        Map.entry("precio_decimales", "PrecioDecimales"),
                        Map.entry("precio_truca_redondea", "PrecioTrucaRedondea"),
                        Map.entry("tasa_decimales", "TasaDecimales"),
                        Map.entry("tasa_truca_redondea", "TasaTrucaRedondea"),
                        Map.entry("cantidad_decimales", "CantidadDecimales"),
                        Map.entry("cantidad_truca_redondea", "CantidadTrucaRedondea"),
                        Map.entry("monto_decimales", "MontoDecimales"),
                        Map.entry("monto_truca_redondea", "MontoTrucaRedondea"),
                        Map.entry("tipo_cambio_decimales", "TipoCambioDecimales"),
                        Map.entry("tipo_cambio_truca_redondea", "TipoCambioTrucaRedondea"),
                        Map.entry("paridad_decimales", "ParidadDecimales"),
                        Map.entry("paridad_truca_redondea", "ParidadTrucaRedondea"),
                        Map.entry("puntos_decimales", "PuntosDecimales"),
                        Map.entry("puntos_truca_redondea", "PuntosTrucaRedondea")),
                Map.of(),
                "P_OUT_MONEDAFORMATO"));
        TODAS.put("BSCL.Plataforma.BD.Utilitarios::ObtenerFechas", new DefinicionProcedimiento("BSCL.Plataforma.BD.Utilitarios::ObtenerFechas", "sp_pla_obtener_fechas", false,
                List.of(
                        new Parametro(1, "IN", "p_id_empresa", "p_id_empresa", "bigint"),
                        new Parametro(2, "OUT", "P_OUT_FECHASISTEMAUTC", "p_out_fecha_sistema_utc", "character varying"),
                        new Parametro(3, "OUT", "P_OUT_FECHASISTEMALOCAL", "p_out_fecha_sistema_local", "character varying"),
                        new Parametro(4, "OUT", "P_OUT_FECHAHORASISTEMAUTC", "p_out_fecha_hora_sistema_utc", "character varying"),
                        new Parametro(5, "OUT", "P_OUT_FECHAHORASISTEMALOCAL", "p_out_fecha_hora_sistema_local", "character varying"),
                        new Parametro(6, "OUT", "P_OUT_FECHAHORAUTC", "p_out_fecha_hora_utc", "character varying"),
                        new Parametro(7, "OUT", "P_OUT_FECHAHORALOCAL", "p_out_fecha_hora_local", "character varying")),
                Map.of(),
                Map.of(),
                null));
        TODAS.put("BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario", new DefinicionProcedimiento("BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario", "sp_pla_acc_recuperar_clave_usuario", false,
                List.of(
                        new Parametro(1, "IN", "p_id_empresa", "p_id_empresa", "bigint"),
                        new Parametro(2, "IN", "p_username", "p_username", "character varying"),
                        new Parametro(3, "IN", "p_token", "p_token", "character varying"),
                        new Parametro(4, "OUT", "P_OUT_MAIL", "p_out_mail", "character varying"),
                        new Parametro(5, "OUT", "P_OUT_ADVERTENCIASCONTADOR", "p_out_advertencias_contador", "integer"),
                        new Parametro(6, "INOUT", "P_OUT_ADVERTENCIASDETALLE", "p_out_advertencias_detalle", "refcursor"),
                        new Parametro(7, "OUT", "P_OUT_ERRORESCONTADOR", "p_out_errores_contador", "integer"),
                        new Parametro(8, "INOUT", "P_OUT_ERRORESDETALLE", "p_out_errores_detalle", "refcursor")),
                Map.of(),
                Map.of(),
                null));
        TODAS.put("BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario", new DefinicionProcedimiento("BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario", "sp_pla_acc_confirmar_cambio_clave_usuario", false,
                List.of(
                        new Parametro(1, "IN", "p_id_empresa", "p_id_empresa", "bigint"),
                        new Parametro(2, "IN", "p_username", "p_username", "character varying"),
                        new Parametro(3, "IN", "p_password", "p_password", "character varying"),
                        new Parametro(4, "IN", "p_token", "p_token", "character varying"),
                        new Parametro(5, "OUT", "P_STATUSCODE", "p_status_code", "integer"),
                        new Parametro(6, "OUT", "P_OUT_ADVERTENCIASCONTADOR", "p_out_advertencias_contador", "integer"),
                        new Parametro(7, "INOUT", "P_OUT_ADVERTENCIASDETALLE", "p_out_advertencias_detalle", "refcursor"),
                        new Parametro(8, "OUT", "P_OUT_ERRORESCONTADOR", "p_out_errores_contador", "integer"),
                        new Parametro(9, "INOUT", "P_OUT_ERRORESDETALLE", "p_out_errores_detalle", "refcursor")),
                Map.of(),
                Map.of(),
                null));
        TODAS.put("BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento", new DefinicionProcedimiento("BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento", "sp_pla_evt_agregar_evento", false,
                List.of(
                        new Parametro(1, "IN", "p_id_empresa", "p_id_empresa", "bigint"),
                        new Parametro(2, "IN", "p_id_usuario", "p_id_usuario", "bigint"),
                        new Parametro(3, "IN", "p_id_modulo", "p_id_modulo", "character varying"),
                        new Parametro(4, "IN", "p_eventos", "p_eventos", "ty_pla_evt_agregar_evento_eventos[]"),
                        new Parametro(5, "IN", "p_valores", "p_valores", "ty_pla_evt_agregar_evento_valores[]"),
                        new Parametro(6, "IN", "p_archivos_adjuntos", "p_archivos_adjuntos", "ty_pla_evt_agregar_evento_archivos_adjuntos[]"),
                        new Parametro(7, "IN", "p_id_direcciones", "p_id_direcciones", "ty_pla_evt_agregar_evento_id_direcciones[]"),
                        new Parametro(8, "IN", "p_aplica_origen_evento", "p_aplica_origen_evento", "character varying"),
                        new Parametro(9, "INOUT", "P_OUT_IDEVENTO", "p_out_id_evento", "refcursor")),
                Map.of(),
                Map.of(
                        "p_eventos", List.of(
                                new Atributo(1, "codigo_tipo_evento", "character varying(40)", "CodigoTipoEvento"),
                                new Atributo(2, "id_secuencia", "bigint", "IdSecuencia"),
                                new Atributo(3, "fecha_creacion_sistema", "timestamp(0) without time zone", "FechaCreacionSistema"),
                                new Atributo(4, "id_persona_remitente", "bigint", "IdPersonaRemitente"),
                                new Atributo(5, "id_persona_destinatario", "bigint", "IdPersonaDestinatario"),
                                new Atributo(6, "id_portafolio", "bigint", "IdPortafolio")),
                        "p_valores", List.of(
                                new Atributo(1, "id_secuencia", "bigint", "IdSecuencia"),
                                new Atributo(2, "llave", "character varying(40)", "Llave"),
                                new Atributo(3, "valor", "character varying(5000)", "Valor")),
                        "p_archivos_adjuntos", List.of(
                                new Atributo(1, "id_secuencia", "bigint", "IdSecuencia"),
                                new Atributo(2, "archivo_adjunto", "bytea", "ArchivoAdjunto"),
                                new Atributo(3, "nombre", "character varying(520)", "Nombre"),
                                new Atributo(4, "contrasenia", "character varying(520)", "Contrasenia")),
                        "p_id_direcciones", List.of(
                                new Atributo(1, "id_secuencia", "bigint", "IdSecuencia"),
                                new Atributo(2, "id_direccion", "bigint", "IdDireccion"),
                                new Atributo(3, "tipo_destinatario", "character varying(6)", "TipoDestinatario"))),
                null));
        TODAS.put("BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema", new DefinicionProcedimiento("BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema", "sp_pla_obtener_fecha_hora_sistema", false,
                List.of(
                        new Parametro(1, "IN", "p_id_empresa", "p_id_empresa", "bigint"),
                        new Parametro(2, "OUT", "P_OUT_FECHAHORASISTEMA", "p_out_fecha_hora_sistema", "character varying")),
                Map.of(),
                Map.of(),
                null));
    }

    private DefinicionesProcedimientos() {
    }

    /** The definition of a login procedure by its full HANA name, or null when the login does not call it. */
    static DefinicionProcedimiento de(String nombreHana) {
        return nombreHana == null ? null : TODAS.get(nombreHana);
    }
}
