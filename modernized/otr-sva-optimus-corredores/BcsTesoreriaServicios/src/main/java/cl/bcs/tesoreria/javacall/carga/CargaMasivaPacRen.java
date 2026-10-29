package cl.bcs.tesoreria.javacall.carga;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;

/** JSON contract of legacy {@code CrearCargaMasivaPacRen} (field names are the contract). */
public final class CargaMasivaPacRen {

    private CargaMasivaPacRen() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaCarga;
        public String p_simular;
        public List<CargaMasivaPac.DetalleCargaMasiva> p_CargaMasiva;
        public List<DetalleRendicion> p_CargaMasivaDetalle;
    }

    /** One settlement line; also the parameter object of the INSERT (names of the SQL parameters). */
    public static class DetalleRendicion {
        public Long IdCargaMasivaDetalle;
        public Long IdCargaMasivaPac;
        public String CodigoBanco;
        public String CodigoEmpresa;
        public String CodigoConvenio;
        public String TipoRegistro;
        public String IdentServicio;
        public String InfAdicional;
        public String MontoCargo;
        public String FechaVencimiento;
        public String FechaCargo;
        public String EstadoCargo;
        public String CodigoRechazo;
        public String EstadoCSC;
        public String EstadoSAAPV;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        /** Registers of the pre-settlement validation, as the JSON the validator produced (RegistroRecaudacion). */
        public JsonArray P_OUT_SOLICITUDES = new JsonArray();
    }
}
