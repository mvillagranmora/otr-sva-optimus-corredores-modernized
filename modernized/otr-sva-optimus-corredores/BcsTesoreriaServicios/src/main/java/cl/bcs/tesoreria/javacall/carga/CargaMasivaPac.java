package cl.bcs.tesoreria.javacall.carga;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;

/** JSON contract of legacy {@code CrearCargaMasivaPac} (field names are the contract). */
public final class CargaMasivaPac {

    private CargaMasivaPac() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaCarga;
        public List<DetalleCargaMasiva> p_CargaMasiva;
        public List<DetalleUniverso> p_CargaMasivaDetalle;
    }

    public static class DetalleCargaMasiva {
        public Long IdInterno;
        public String NombreArchivo;
        public String CodigoTipoCargaMasiva;
    }

    /** One universe line; also the parameter object of the INSERT (names of the SQL parameters). */
    public static class DetalleUniverso {
        public Long IdCargaMasivaDetalle;
        public Long IdCargaMasivaPac;
        public String CodigoBanco;
        public String CodigoEmpresa;
        public String CodigoConvenio;
        public String TipoRegistro;
        public String IdentServicio;
        public String FechaGeneracion;
        public String FechaInicioServ;
    }

    /** Parameter / row object of the small statements ({@code :tipo}, {@code :IdentServicio}). */
    public static class ClaveUniverso {
        public String IdentServicio;
        public String tipo;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }
}
