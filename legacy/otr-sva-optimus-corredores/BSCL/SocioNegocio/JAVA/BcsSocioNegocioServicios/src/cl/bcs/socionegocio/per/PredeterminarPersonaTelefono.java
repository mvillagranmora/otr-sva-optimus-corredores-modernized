package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cl.bcs.plataforma.pla.utils.PlataformaUtils.loadResourceToString;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio que actualiza el telefono por defecto de la persona,
 * para ser creado como una solicitud de Cambio.
 * 
 * Migrado desde:
 * BSCL.SocioNegocio.BD.Servicios::PER.PredeterminarPersonaTelefono
 * Autor Original: CHERNANDEZ - BCS
 * Fecha Creacion Original: 22-02-2017
 * Fecha Migracion: Mayo 2026
 */
public class PredeterminarPersonaTelefono implements JavaCall {

    private static final Logger logger = LogManager.getLogger(PredeterminarPersonaTelefono.class);

    private final SQL sql = new SQL();

    // Cargar archivos SQL
    private final String SQL_SELECT_EXISTE_TELEFONO = loadResourceToString(
            "cl/bcs/socionegocio/per/sql/PredeterminarPersonaTelefono_Select_ExisteTelefono.sql");
    private final String SQL_SELECT_ID_PERSONA = loadResourceToString(
            "cl/bcs/socionegocio/per/sql/PredeterminarPersonaTelefono_Select_IdPersona.sql");
    private final String SQL_SELECT_DATOS_PERSONA_GLOSA = loadResourceToString(
            "cl/bcs/socionegocio/per/sql/PredeterminarPersonaTelefono_Select_DatosPersonaGlosa.sql");

    // ═══════════════════════════════════════════════════════
    // CLASES DE ENTRADA
    // ═══════════════════════════════════════════════════════

    public static class EntradaPredeterminarPersonaTelefono {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdTelefono;
    }

    // ═══════════════════════════════════════════════════════
    // CLASES DE SALIDA
    // ═══════════════════════════════════════════════════════

    public static class SalidaPredeterminarPersonaTelefono {
        public Long P_OUT_REFID;
        public int P_OUT_ADVERTENCIASCONTADOR;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
        public int P_OUT_ERRORESCONTADOR;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

        public SalidaPredeterminarPersonaTelefono() {
            this.P_OUT_REFID = 0L;
            this.P_OUT_ADVERTENCIASCONTADOR = 0;
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0;
            this.P_OUT_ERRORESDETALLE = new ArrayList<>();
        }
    }

    // ═══════════════════════════════════════════════════════
    // CLASES AUXILIARES
    // ═══════════════════════════════════════════════════════

    public static class ErrorDetalle {
        public String Codigo;
        public String Descripcion;

        public ErrorDetalle(String codigo, String descripcion) {
            this.Codigo = codigo;
            this.Descripcion = descripcion;
        }
    }

    public static class AdvertenciaDetalle {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;

        public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
            this.Codigo = codigo;
            this.RequiereAutorizacion = requiereAutorizacion;
            this.Descripcion = descripcion;
        }
    }

    public static class Contador {
        public Long Contador;
    }

    public static class TelefonoPersona {
        public Long IdPersona;
    }

    public static class DatosPersonaGlosa {
        public String DocumentoIdentidad;
        public String NombreCompleto;
    }

    // ═══════════════════════════════════════════════════════
    // LÓGICA PRINCIPAL
    // ═══════════════════════════════════════════════════════

    public SalidaPredeterminarPersonaTelefono logica(Connection conn, EntradaPredeterminarPersonaTelefono entrada)
            throws Exception {
        logger.info("INICIO::PredeterminarPersonaTelefono.logica");
        long inicio = System.currentTimeMillis();

        SalidaPredeterminarPersonaTelefono salida = new SalidaPredeterminarPersonaTelefono();

        try {
            // ═══════════════════════════════════════════════════════
            // VARIABLES DE CONTROL
            // ═══════════════════════════════════════════════════════

            String v_tipoEntidad = "TELEFONO-PREDETERMINAR";
            String v_IdConcepto = "PRDTEL";
            String v_estadoAutorizacion = "PEN";
            Long v_secuencia_IdSolicitudCambio = -1L;

            // Obtener fecha del sistema
            String FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());

            // ═══════════════════════════════════════════════════════
            // 1. VALIDAR EXISTENCIA DEL TELÉFONO
            // ═══════════════════════════════════════════════════════

            logger.debug("Validar existencia del teléfono");
            HashMap<String, Object> paramsExiste = new HashMap<>();
            paramsExiste.put("IdTelefono", entrada.p_IdTelefono);

            List<Contador> countList = sql.query(conn, SQL_SELECT_EXISTE_TELEFONO, paramsExiste, Contador.class);
            Long existe = (countList != null && !countList.isEmpty()) ? countList.get(0).Contador : 0L;

            if (existe == 0) {
                salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ENTIDAD_NO_EXISTE", "El teléfono no existe"));
                salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
                return salida;
            }

            // Obtener IdPersona del teléfono
            HashMap<String, Object> paramsPersona = new HashMap<>();
            paramsPersona.put("IdTelefono", entrada.p_IdTelefono);

            List<TelefonoPersona> personaList = sql.query(conn, SQL_SELECT_ID_PERSONA, paramsPersona,
                    TelefonoPersona.class);
            if (personaList == null || personaList.isEmpty()) {
                salida.P_OUT_ERRORESDETALLE
                        .add(new ErrorDetalle("ERROR_OBTENER_PERSONA", "No se pudo obtener la persona del teléfono"));
                salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
                return salida;
            }

            Long v_IdEntidadBase = personaList.get(0).IdPersona;
            String v_IdEntidadOrigen = String.valueOf(entrada.p_IdTelefono);

            // ═══════════════════════════════════════════════════════
            // 2. CREAR SOLICITUD DE CAMBIO
            // ═══════════════════════════════════════════════════════

            logger.debug("Crear solicitud de cambio");

            // Obtener siguiente ID de secuencia
            List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
            v_secuencia_IdSolicitudCambio = secuencia.get(0);

            // Crear registro de solicitud de cambio
            CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio solicitudIn = new CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio();

            CrearRegistroSolicitudCambio.RegistroSolicitudCambio registro = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();

            registro.IdSolicitudCambio = v_secuencia_IdSolicitudCambio;
            registro.IdTipoEntidad = v_tipoEntidad;
            registro.IdEntidadOrigen = v_IdEntidadOrigen;
            registro.CampoModificado = "Predeterminado";
            registro.TipoDato = "NVARCHAR";
            registro.ValorAntiguo = "N";
            registro.ValorNuevo = "S";
            registro.IdEstadoAutorizacion = v_estadoAutorizacion;

            solicitudIn.p_RegistroSolicitudCambio.add(registro);

            CrearRegistroSolicitudCambio solicitudService = new CrearRegistroSolicitudCambio();
            solicitudService.logica(conn, solicitudIn);

            salida.P_OUT_REFID = v_secuencia_IdSolicitudCambio;

            // ═══════════════════════════════════════════════════════
            // 3. EVALUAR SI REQUIERE AUTORIZACIÓN
            // ═══════════════════════════════════════════════════════

            logger.debug("Consultar si requiere autorización");
            String RequiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, entrada.p_IdEmpresa, v_IdConcepto);

            if ("S".equals(RequiereAutorizacion) && !"N".equals(entrada.p_UsaReglasNegocio)) {
                // ───────────────────────────────────────────────────
                // 3A. REQUIERE AUTORIZACIÓN → Crear evento
                // ───────────────────────────────────────────────────

                logger.debug("Crear evento de autorización");

                // Obtener datos de la persona para la glosa
                HashMap<String, Object> paramsGlosa = new HashMap<>();
                paramsGlosa.put("IdPersona", v_IdEntidadBase);

                List<DatosPersonaGlosa> datosGlosaList = sql.query(conn, SQL_SELECT_DATOS_PERSONA_GLOSA, paramsGlosa,
                        DatosPersonaGlosa.class);

                String v_Glosa = "";
                if (datosGlosaList != null && !datosGlosaList.isEmpty()) {
                    DatosPersonaGlosa datos = datosGlosaList.get(0);
                    v_Glosa = datos.DocumentoIdentidad + " / " + datos.NombreCompleto +
                            " / IdTelefono: " + entrada.p_IdTelefono + " / PREDETERMINAR";
                }

                // Llamar wrapper de CrearEventoAutorizacion
                CrearEventoAutorizacion wrapper = new CrearEventoAutorizacion();
                CrearEventoAutorizacion.ServiceEntrada evtIn = new CrearEventoAutorizacion.ServiceEntrada();

                evtIn.p_IdEmpresa = entrada.p_IdEmpresa;
                evtIn.p_IdUsuario = entrada.p_IdUsuario;
                evtIn.p_IdModulo = entrada.p_IdModulo;
                evtIn.p_UsaReglasNegocio = "N";
                evtIn.p_IdTipoEvento = v_IdConcepto;
                evtIn.p_IdModuloSolicita = entrada.p_IdModulo;
                evtIn.p_IdUsuarioSolicita = entrada.p_IdUsuario;

                // Fecha en formato YYYY-MM-DD
                String fechaStr = FechaHoraSistema.substring(0, 10);
                evtIn.p_Fecha = fechaStr;
                evtIn.p_IdConcepto = v_IdConcepto;
                evtIn.p_NumeroOperacionConcepto = v_IdEntidadOrigen;
                evtIn.p_FechaOperacionConcepto = fechaStr;
                evtIn.p_IdCliente = v_IdEntidadBase;
                evtIn.p_Glosa = v_Glosa;
                evtIn.p_IdCanal = "OPT";

                CrearEventoAutorizacion.ServiceSalida evtOut = wrapper.logica(conn, evtIn);

                // Propagar advertencias
                if (evtOut.p_out_AdvertenciasDetalle != null) {
                    for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : evtOut.p_out_AdvertenciasDetalle) {
                        salida.P_OUT_ADVERTENCIASDETALLE.add(
                                new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
                    }
                }

                // Propagar errores
                if (evtOut.p_out_ErroresDetalle != null) {
                    for (CrearEventoAutorizacion.ObjERRORESDETALLE err : evtOut.p_out_ErroresDetalle) {
                        salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
                    }
                }

                // Actualizar contadores
                salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();
                salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();

                // Si NO hay errores, agregar mensaje REQUIERE_AUTORIZACION desde BD
                if (salida.P_OUT_ERRORESCONTADOR == 0) {
                    String mensajeAutorizacion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,
                            "REQUIERE_AUTORIZACION", new HashMap<String, String>());
                    salida.P_OUT_ADVERTENCIASDETALLE.add(
                            new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", mensajeAutorizacion));
                    salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();
                }

                // Si hay errores, retornar
                if (salida.P_OUT_ERRORESCONTADOR > 0) {
                    return salida;
                }

            } else {
                // ───────────────────────────────────────────────────
                // 3B. NO REQUIERE AUTORIZACIÓN → Ejecutar directamente
                // ───────────────────────────────────────────────────

                logger.debug("Confirmar predeterminar teléfono directamente");

                // Llamar al servicio de autorización existente
                AutorizarPredeterminarPersonaTelefono servicioAutorizar = new AutorizarPredeterminarPersonaTelefono();

                // Construir entrada para autorización
                EntradaAutorizacion entradaAuth = new EntradaAutorizacion();
                entradaAuth.p_IdEmpresa = entrada.p_IdEmpresa;
                entradaAuth.p_IdUsuario = entrada.p_IdUsuario;
                entradaAuth.p_IdModulo = entrada.p_IdModulo;
                entradaAuth.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;

                Evento evento = new Evento();
                evento.NumeroOperacionConcepto = String.valueOf(entrada.p_IdTelefono);
                evento.IdConcepto = v_IdConcepto;
                entradaAuth.p_Lista.add(evento);

                // Ejecutar autorización directamente
                SalidaAutorizacion salidaAuth = servicioAutorizar.autorizarEvento(conn, entradaAuth);

                // Propagar resultados
                if (salidaAuth.P_OUT_ERRORESCONTADOR != null && salidaAuth.P_OUT_ERRORESCONTADOR > 0) {
                    salida.P_OUT_ERRORESCONTADOR += salidaAuth.P_OUT_ERRORESCONTADOR.intValue();

                    if (salidaAuth.P_OUT_MENSAJES != null) {
                        for (Object msg : salidaAuth.P_OUT_MENSAJES) {
                            // Convertir mensajes de autorización a errores
                            salida.P_OUT_ERRORESDETALLE.add(
                                    new ErrorDetalle("ERROR_AUTORIZACION", msg.toString()));
                        }
                    }

                    return salida;
                }
            }

        } catch (Exception e) {
            logger.error("Error en PredeterminarPersonaTelefono: " + e.getMessage(), e);
            salida.P_OUT_ERRORESDETALLE.add(
                    new ErrorDetalle("ERROR_PREDETERMINAR_TELEFONO", e.getMessage()));
            salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
        }

        long fin = System.currentTimeMillis();
        logger.info("FIN::PredeterminarPersonaTelefono.logica Tiempo: {} ms", fin - inicio);

        return salida;
    }

    // ═══════════════════════════════════════════════════════
    // MÉTODO CALL - JAVACALL INTERFACE
    // ═══════════════════════════════════════════════════════

    @Override
    public JsonObject call(Connection conn, JsonObject jin) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaPredeterminarPersonaTelefono in = gson.fromJson(jin.toString(),
                EntradaPredeterminarPersonaTelefono.class);
        SalidaPredeterminarPersonaTelefono out = logica(conn, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }
}
