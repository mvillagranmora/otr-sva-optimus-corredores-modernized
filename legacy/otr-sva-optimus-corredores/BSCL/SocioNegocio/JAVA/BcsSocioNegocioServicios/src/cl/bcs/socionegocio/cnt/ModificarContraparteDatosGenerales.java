package cl.bcs.socionegocio.cnt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarContraparteDatosGenerales implements JavaCall {

    static final Logger logger = LogManager.getLogger(ModificarContraparteDatosGenerales.class);

    private final String SQL_COUNT_EXISTE = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/cnt/sql/ModificarContraparteDatosGenerales_Count_Existe.sql");
    private final String SQL_SELECT_GLOSA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/cnt/sql/ModificarContraparteDatosGenerales_Select_Glosa.sql");
    private final String SQL_SELECT_VALOR_ANTIGUO_TEMPLATE = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/cnt/sql/ModificarContraparteDatosGenerales_Select_ValorAntiguo.sql");

    private final String SQL_UPDATE_CONTRAPARTE =
    PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/cnt/sql/ModificarContraparteDatosGenerales_Update_Contraparte.sql"
    );

    private final String SQL_UPDATE_ESTADO_SOLICITUD =
    PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/cnt/sql/ModificarContraparteDatosGenerales_Update_EstadoSolicitud.sql"
    );

    public static class RegistroCambio {
        public String CampoModificado;
        public String ValorNuevo;
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdContraparte;
        public List<RegistroCambio> p_RegistroCambio;

        public Entrada() {
            p_RegistroCambio = new ArrayList<>();
        }
    }

    public static class ErrorDetalle {
        public String Codigo;
        public String Descripcion;

        public ErrorDetalle() {}
        public ErrorDetalle(String c, String d){ Codigo=c; Descripcion=d; }
    }

    public static class AdvertenciaDetalle {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class Contador {
        public Long Contador;
    }

    public static class GlosaObj {
        public String DocumentoIdentidad;
        public String NombreCompleto;
    }

    public static class ValorAntiguoObj {
        public String ValorAntiguo;
        public String TipoDato;
    } 
    public static class ParamConfirmarModificacion {
        public Long IdSolicitudCambio;
        public Long IdContraparte;
        public String IdEstadoAutorizacion;
    }

    public static class Salida {
        public Long P_OUT_REFID;
        public long P_OUT_ADVERTENCIASCONTADOR;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
        public long P_OUT_ERRORESCONTADOR;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

        public Salida() {
            P_OUT_REFID = 0L;
            P_OUT_ADVERTENCIASCONTADOR = 0L;
            P_OUT_ERRORESCONTADOR = 0L;
            P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            P_OUT_ERRORESDETALLE = new ArrayList<>();
        }
    }

    public Salida logica(Connection conn, Entrada in) throws Exception {
        logger.info("INICIO::ModificarContraparteDatosGenerales");
        Salida salida = new Salida();
        SQL sql = new SQL(); // única instancia reutilizable

        // 1) Validar existencia de contraparte
        HashMap<String,Object> params = new HashMap<>();
        params.put("IdContraparte", in.p_IdContraparte);
        List<Contador> cnt = sql.query(conn, SQL_COUNT_EXISTE, params, Contador.class);
        Long existe = (cnt!=null && !cnt.isEmpty())? cnt.get(0).Contador : 0L;
        if (existe == 0L) {
            String msg = Diccionario.getMensaje(conn, in.p_IdEmpresa, "CONTRAPARTE_NO_EXISTE").msg();
            salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("CONTRAPARTE_NO_EXISTE", msg));
            salida.P_OUT_ERRORESCONTADOR = 1;
            return salida;
        }

        // 2) Preparar registros de solicitud de cambio
        Long idSolicitudCambio = CrearRegistroSolicitudCambio.obtenerIdSolicitudCambio(conn);
        String schemaName = conn.getSchema();
        
        List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> registros = new ArrayList<>();

        for (RegistroCambio rc : in.p_RegistroCambio) {
            CrearRegistroSolicitudCambio.RegistroSolicitudCambio r =
                new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();

            r.IdSolicitudCambio = idSolicitudCambio;
            r.IdTipoEntidad = "CONTRAPARTE";
            r.IdEntidadOrigen = String.valueOf(in.p_IdContraparte);
            r.CampoModificado = rc.CampoModificado;

            String valorAntiguo = "";
            String tipoDato = "";

            HashMap<String, Object> paramsValorAntiguo = new HashMap<>();
            paramsValorAntiguo.put("IdContraparte", in.p_IdContraparte);
            paramsValorAntiguo.put("CampoModificado", rc.CampoModificado);
            paramsValorAntiguo.put("SchemaName", schemaName);

            List<ValorAntiguoObj> valores = sql.query(
                conn,
                SQL_SELECT_VALOR_ANTIGUO_TEMPLATE,
                paramsValorAntiguo,
                ValorAntiguoObj.class
            );

            if (valores == null || valores.isEmpty()) {
                String msg = Diccionario.getMensaje(
                    conn,
                    in.p_IdEmpresa,
                    "FALTA_ESPECIFICAR_REGISTRO_CAMBIO"
                ).msg();

                salida.P_OUT_ERRORESDETALLE.add(
                    new ErrorDetalle("FALTA_ESPECIFICAR_REGISTRO_CAMBIO", msg)
                );

                salida.P_OUT_ERRORESCONTADOR = 1;
                return salida;
            }

            ValorAntiguoObj valor = valores.get(0);
            valorAntiguo = valor.ValorAntiguo != null ? valor.ValorAntiguo : "";
            tipoDato = valor.TipoDato != null ? valor.TipoDato : "";

            r.TipoDato = tipoDato;
            r.ValorAntiguo = valorAntiguo;
            r.ValorNuevo = rc.ValorNuevo;
            r.IdEstadoAutorizacion = "PEN";

            registros.add(r);
        }

        if (registros.isEmpty()) {
            String msg = Diccionario.getMensaje(conn, in.p_IdEmpresa, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO").msg();
            salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("FALTA_ESPECIFICAR_REGISTRO_CAMBIO", msg));
            salida.P_OUT_ERRORESCONTADOR = 1;
            return salida;
        }

        // 3) Crear registro(s) de solicitud cambio usando servicio Java existente
        CrearRegistroSolicitudCambio servicioCrear = new CrearRegistroSolicitudCambio();
        CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio inCrear = new CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio();
        inCrear.p_RegistroSolicitudCambio = registros;
        servicioCrear.logica(conn, inCrear);

        // 4) Verificar si requiere autorizacion
        final String ID_CONCEPTO = "MODCNT";
        Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(
                conn,
                in.p_IdEmpresa,
                ID_CONCEPTO,
                in.p_UsaReglasNegocio
        );

        if (Boolean.TRUE.equals(esOperacionConAutorizacion)) {
            String glosa = "";
            HashMap<String,Object> p = new HashMap<>();
            p.put("IdContraparte", in.p_IdContraparte);
            List<GlosaObj> gl = sql.query(conn, SQL_SELECT_GLOSA, p, GlosaObj.class);
            if (gl!=null && !gl.isEmpty()) {
                GlosaObj obj = gl.get(0);
                String documento = (obj.DocumentoIdentidad==null)? "" : obj.DocumentoIdentidad;
                String nombre = (obj.NombreCompleto==null)? "" : obj.NombreCompleto;
                glosa = documento + " / " + nombre;
            }

            String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(
                    conn,
                    in.p_IdEmpresa,
                    "PLATAFORMA",
                    "PLA.FechaSistema"
            );

            CrearEventoAutorizacion crearEvt = new CrearEventoAutorizacion();
            CrearEventoAutorizacion.ServiceEntrada inEvt = new CrearEventoAutorizacion.ServiceEntrada();
            inEvt.p_IdEmpresa = in.p_IdEmpresa;
            inEvt.p_IdUsuario = in.p_IdUsuario;
            inEvt.p_IdModulo = in.p_IdModulo;
            inEvt.p_UsaReglasNegocio = "N";
            inEvt.p_IdTipoEvento = ID_CONCEPTO;
            inEvt.p_IdModuloSolicita = in.p_IdModulo;
            inEvt.p_IdUsuarioSolicita = in.p_IdUsuario;
            inEvt.p_Fecha = fechaSistema;
            inEvt.p_IdConcepto = ID_CONCEPTO;
            inEvt.p_NumeroOperacionConcepto = String.valueOf(in.p_IdContraparte);
            inEvt.p_FechaOperacionConcepto = fechaSistema;
            inEvt.p_IdCliente = in.p_IdContraparte;
            inEvt.p_Glosa = glosa;

            CrearEventoAutorizacion.ServiceSalida outEvt = crearEvt.logica(conn, inEvt);

            if (outEvt != null) {
                salida.P_OUT_ADVERTENCIASCONTADOR += (outEvt.p_out_AdvertenciasContador!=null)? outEvt.p_out_AdvertenciasContador.intValue() : 0;
                if (outEvt.p_out_AdvertenciasDetalle!=null) {
                    for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : outEvt.p_out_AdvertenciasDetalle) {
                        AdvertenciaDetalle a = new AdvertenciaDetalle();
                        a.Codigo = adv.Codigo; a.RequiereAutorizacion = adv.RequiereAutorizacion; a.Descripcion = adv.Descripcion;
                        salida.P_OUT_ADVERTENCIASDETALLE.add(a);
                    }
                }

                salida.P_OUT_ERRORESCONTADOR += (outEvt.p_out_ErroresContador!=null)? outEvt.p_out_ErroresContador.intValue() : 0;
                if (outEvt.p_out_ErroresDetalle!=null) {
                    for (CrearEventoAutorizacion.ObjERRORESDETALLE err : outEvt.p_out_ErroresDetalle) {
                        salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
                    }
                }
            }

            if (salida.P_OUT_ERRORESCONTADOR > 0) {
                return salida;
            } else {
                String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION").msg();
                AdvertenciaDetalle ad = new AdvertenciaDetalle();
                ad.Codigo = "REQUIERE_AUTORIZACION"; ad.RequiereAutorizacion = "S"; ad.Descripcion = descripcion;
                salida.P_OUT_ADVERTENCIASDETALLE.add(0, ad);
                salida.P_OUT_ADVERTENCIASCONTADOR += 1;
                salida.P_OUT_REFID = idSolicitudCambio;
                return salida;
            }
        } else {
            confirmarModificacionSinAutorizacion(
                conn,
                sql,
                in,
                salida,
                idSolicitudCambio
            );

            return salida;
        }
    }

    private void confirmarModificacionSinAutorizacion(
            Connection conn,
            SQL sql,
            Entrada in,
            Salida salida,
            Long idSolicitudCambio
    ) throws Exception {

    	ParamConfirmarModificacion params = new ParamConfirmarModificacion();
        params.IdSolicitudCambio = idSolicitudCambio;
        params.IdContraparte = in.p_IdContraparte;
        params.IdEstadoAutorizacion = "ING";

        List<ParamConfirmarModificacion> listaParams = new ArrayList<>();
        listaParams.add(params);

        logger.info("INICIO::ModificarContraparteDatosGenerales_Update_Contraparte");
        int[] resUpdateContraparte = sql.batch(
            conn,
            SQL_UPDATE_CONTRAPARTE,
            listaParams,
            ParamConfirmarModificacion.class
        );
        logger.info("FIN::ModificarContraparteDatosGenerales_Update_Contraparte");

        if (resUpdateContraparte == null || resUpdateContraparte.length == 0 || resUpdateContraparte[0] == 0) {

            String msg = Diccionario.getMensaje(
                conn,
                in.p_IdEmpresa,
                "CONTRAPARTE_NO_ACTUALIZADA"
            ).msg();

            salida.P_OUT_ERRORESDETALLE.add(
                new ErrorDetalle("CONTRAPARTE_NO_ACTUALIZADA", msg)
            );
            salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
            return;
        }

        logger.info("INICIO::ModificarContraparteDatosGenerales_Update_EstadoSolicitud");
        int[] resUpdateSolicitud = sql.batch(
            conn,
            SQL_UPDATE_ESTADO_SOLICITUD,
            listaParams,
            ParamConfirmarModificacion.class
        );
        logger.info("FIN::ModificarContraparteDatosGenerales_Update_EstadoSolicitud");

        if (resUpdateSolicitud == null || resUpdateSolicitud.length == 0 || resUpdateSolicitud[0] == 0) {

            String msg = Diccionario.getMensaje(
                conn,
                in.p_IdEmpresa,
                "SOLICITUD_NO_ACTUALIZADA"
            ).msg();

            salida.P_OUT_ERRORESDETALLE.add(
                new ErrorDetalle("SOLICITUD_NO_ACTUALIZADA", msg)
            );
            salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
            return;
        }

        salida.P_OUT_REFID = idSolicitudCambio;
    }
    
    @Override
    public JsonObject call(Connection conn, JsonObject jin) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(conn, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            ModificarContraparteDatosGenerales svc = new ModificarContraparteDatosGenerales();
            String sample = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"CNT\",\"p_UsaReglasNegocio\":\"N\",\"p_IdContraparte\":2,\"p_RegistroCambio\":[] }";
            Gson g = Utils.getGsonBuilder();
            JsonObject jin = g.fromJson(sample, JsonObject.class);
            JsonObject out = svc.call(conn, jin);
            System.out.println(out.toString());
            conn.rollback();
        }
    } 
}
