package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.Utils;

/**
 * Contract of the legacy service {@code cl.bcs.cuentaInversion.AnularCargoAbono} (annuls investment-account movements) as the Tesoreria family-B
 * services use it through the {@link Colaboradores} port. The nested classes keep the legacy names and field names (they ARE the JSON contract).
 */
public final class AnularCargoAbono {

    private AnularCargoAbono() {
    }

    public static class CargoAbono_Entrada {
        public Long IdCargoAbonoMovimiento;
        public String GlosaAnulacion;

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class AnularCargoAbono_Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_EjecutaAutorizacion = "S";
        public String p_OmiteRegistroSinAutorizar = "S";
        public String p_FechaMovimiento;
        public String p_EsAnulacionManual = "N";
        public List<CargoAbono_Entrada> p_CargoAbonoAAnular = new ArrayList<>();

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class AnularCargoAbono_Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static AnularCargoAbono_Salida invocar(Colaboradores colaboradores, AnularCargoAbono_Entrada entrada) {
        JsonObject r = colaboradores.invocar("AnularCargoAbono", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, AnularCargoAbono_Salida.class);
    }
}
