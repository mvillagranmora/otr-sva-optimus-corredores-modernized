package cl.bcs.loaderapp.servicios;

/*
 * Clase generada automaticamente por Sp2Java
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearRelacionPersona implements JavaCall {

    /*
     * El log
     */
    private final Log log = new Log("MIG.CrearRelacionPersona");

    /*
     * 
     * Queries
     * 
     */
    // private final String SQL_XXX = loadResourceToString(".....");

    /*
     * 
     * Clases utilitarias que representan objetos IN/OUT
     */
    public static class ServiceEntrada {

        public Long P_IDEMPRESA;
        public Long P_IDUSUARIO;
        public String P_IDMODULO;
        public String P_USAREGLASNEGOCIO;
        public String P_IDTIPORELACION;
        public Long P_IDPERSONAORIGEN;
        public Long P_IDPERSONADESTINO;
        public String P_IDESTADO;
        public String P_FECHAINICIORELACION;
        public String P_FECHAFINRELACION;
        public Long P_PORCENTAJEPARTICIPACION;
        public String P_RETIRADOCUMENTOS;
        public String P_FINALIDADRELACION;
        public String P_CONDICIONESRELACION;
        public Long P_IDGRADOVINCULACIONPARENTAL;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ServiceSalida {

        public Long P_OUT_REFID;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public List<ObjADVERTENCIASDETALLE> P_OUT_ADVERTENCIASDETALLE;
        public Long P_OUT_ERRORESCONTADOR;
        public List<ObjERRORESDETALLE> P_OUT_ERRORESDETALLE;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    /*
     * 
     * Clases utilitarias que representan objetos de procesamiento
     * 
     */

    public static class ObjADVERTENCIASDETALLE {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class ObjERRORESDETALLE {
        public String Codigo;
        public String Descripcion;
    }

    /*
     * Objeto de transporte
     */
    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        Gson gson = getGsonBuilder();
        ServiceEntrada in = gson.fromJson(jin.toString(), ServiceEntrada.class);
        ServiceSalida out = logica(con, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    /*
     * Logica Servicio
     * 
     */
    public ServiceSalida logica(Connection con, ServiceEntrada in) throws Exception {
        SQL sql = new SQL();
        ServiceSalida out = new ServiceSalida();
        log.write("INICIO");
        try {

            // ------------------------------
            // Crea las tablas temporales

            // ------------------------------
            // Inserta tablas temporales globales

            // ------------------------------
            // Ejecuta el SP
            HashMap<String, Object> parmsIN = new HashMap<String, Object>();

            parmsIN.put("P_IDEMPRESA", in.P_IDEMPRESA);
            parmsIN.put("P_IDUSUARIO", in.P_IDUSUARIO);
            parmsIN.put("P_IDMODULO", in.P_IDMODULO);
            parmsIN.put("P_USAREGLASNEGOCIO", in.P_USAREGLASNEGOCIO);
            parmsIN.put("P_IDTIPORELACION", in.P_IDTIPORELACION);
            parmsIN.put("P_IDPERSONAORIGEN", in.P_IDPERSONAORIGEN);
            parmsIN.put("P_IDPERSONADESTINO", in.P_IDPERSONADESTINO);
            parmsIN.put("P_IDESTADO", in.P_IDESTADO);
            parmsIN.put("P_FECHAINICIORELACION", in.P_FECHAINICIORELACION);
            parmsIN.put("P_FECHAFINRELACION", in.P_FECHAFINRELACION);
            parmsIN.put("P_PORCENTAJEPARTICIPACION", in.P_PORCENTAJEPARTICIPACION);
            parmsIN.put("P_RETIRADOCUMENTOS", in.P_RETIRADOCUMENTOS);
            parmsIN.put("P_FINALIDADRELACION", in.P_FINALIDADRELACION);
            parmsIN.put("P_CONDICIONESRELACION", in.P_CONDICIONESRELACION);
            parmsIN.put("P_IDGRADOVINCULACIONPARENTAL", in.P_IDGRADOVINCULACIONPARENTAL);

            HashMap<String, Class> parmsOUT = new HashMap<String, Class>();

            parmsOUT.put("P_OUT_REFID", Long.class);
            parmsOUT.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
            parmsOUT.put("P_OUT_ADVERTENCIASDETALLE", ObjADVERTENCIASDETALLE.class);
            parmsOUT.put("P_OUT_ERRORESCONTADOR", Long.class);
            parmsOUT.put("P_OUT_ERRORESDETALLE", ObjERRORESDETALLE.class);

            String queryCall = "call \"BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,:P_USAREGLASNEGOCIO,:P_IDTIPORELACION,:P_IDPERSONAORIGEN,:P_IDPERSONADESTINO,:P_IDESTADO,:P_FECHAINICIORELACION,:P_FECHAFINRELACION,:P_PORCENTAJEPARTICIPACION,:P_RETIRADOCUMENTOS,:P_FINALIDADRELACION,:P_CONDICIONESRELACION,:P_IDGRADOVINCULACIONPARENTAL,@P_OUT_REFID,@P_OUT_ADVERTENCIASCONTADOR,$P_OUT_ADVERTENCIASDETALLE,@P_OUT_ERRORESCONTADOR,$P_OUT_ERRORESDETALLE)";
            HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIN, parmsOUT);

            // ------------------------------
            // Lee scalar

            out.P_OUT_REFID = (Long) rpt.get("P_OUT_REFID");
            out.P_OUT_ADVERTENCIASCONTADOR = (Long) rpt.get("P_OUT_ADVERTENCIASCONTADOR");
            out.P_OUT_ERRORESCONTADOR = (Long) rpt.get("P_OUT_ERRORESCONTADOR");

            // ------------------------------

            out.P_OUT_ADVERTENCIASDETALLE = (List<ObjADVERTENCIASDETALLE>) rpt.get("P_OUT_ADVERTENCIASDETALLE");
            out.P_OUT_ERRORESDETALLE = (List<ObjERRORESDETALLE>) rpt.get("P_OUT_ERRORESDETALLE");

            // ------------------------------
            // Lee tablas temporales globales

        } finally {
            // Elimina tablas temporales locales

        }
        log.write("TERMINO");
        return out;

    }

}
