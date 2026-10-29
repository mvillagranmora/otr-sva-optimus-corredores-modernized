package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.util.HashMap;

import cl.bcs.utils.Utils;

/** ORACLE SHIM: MensajeSalida as in legacy PlataformaUtils.java:598-642 (Diccionario is the shim). */
public class PlataformaUtils {
    public static class MensajeSalida {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
        public static final String MENSAJE_OK = "OK";
        public static final String MENSAJE_ERROR = "ERROR";
        public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
        public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
        public static final String MENSAJE_INFORMACION = "INFORMACION";

        public MensajeSalida() { }

        public MensajeSalida(Long id, String tipo, String codigo, String descripcion) {
            this.id = id;
            this.tipo = tipo;
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public MensajeSalida(Long id, String tipo, String codigo, HashMap<String, String> valores, Connection con, Long idEmpresa) throws Exception {
            this.id = id;
            this.tipo = tipo;
            this.codigo = codigo;
            this.descripcion = Diccionario.getMensaje(con, idEmpresa, codigo, valores);
        }

        @Override
        public String toString() { return Utils.printJson(this, false); }
    }

    public static class AdvertenciaDetalle { public String Codigo; public String Descripcion; }

    public static class ErrorDetalle { public String Codigo; public String Descripcion; }

    public static Connection getDESAConnection() { throw new UnsupportedOperationException("oracle shim"); }

    public static Connection getPropertiesConnection(String a, String b) { throw new UnsupportedOperationException("oracle shim"); }
}
