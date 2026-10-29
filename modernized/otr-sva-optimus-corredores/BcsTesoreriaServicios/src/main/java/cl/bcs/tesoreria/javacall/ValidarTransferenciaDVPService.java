package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.ValidarTransferenciaDVP} (service 700.146), behaviours TES-A-01..17: validates the nominas of transfer whose payment
 * medium is TDP (securities transfer) against the DCV key held by custody. Read only.
 *
 * <p>Two entry points, as in the legacy: {@link #logica} (typed) and {@link #execute} (the JSON {@code call}, which recounts the errors).
 */
@Service
public class ValidarTransferenciaDVPService {

    static final String MEDIO_TDP = "TDP";
    static final String ESTADO_INGRESADA = "ING";
    static final String ESTADO_RECEPCIONADA = "RCP";
    static final String ESTADO_ANULADA = "ANU";

    private static final String SQL_NOMINA = loadSql("sql/ValidarNominaTransferenciaDVP.sql");
    private static final String SQL_TRANSFERENCIA = loadSql("sql/ValidarTransferenciaDVP.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        /** Business date (the system date). */
        public String p_Fecha;
        public List<NominaTransferencia> p_NominaTransferencia;
    }

    public static class NominaTransferencia {
        public Long IdNominaTransferencia;
        public String IdMedioDePago;
        public BigDecimal Monto;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public String P_OUT_VALIDO;
    }

    public static class NominaTransferenciaDCV {
        public String ClaveDCV;
        public String IdTipoOperacion;
        public String IdEstado;
    }

    public static class TransferenciaDCV {
        public String IdTipoOperacion;
        public String Folio;
        public BigDecimal MontoNominaTransferencia;
        public BigDecimal MontoCustodia;
        public String ClaveDCV;
        public String ClaveCustodia;
        public String IdEstado;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;

    public ValidarTransferenciaDVPService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
    }

    /** Legacy {@code call}: as {@link #logica} but {@code P_OUT_ERRORESCONTADOR} is recounted from the messages (TES-A-17). */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) {
        Entrada in = JsonLegacy.ENTRADA_SALIDA.fromJson(entrada, Entrada.class);
        Salida salida = logica(in);
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(salida.P_OUT_MENSAJES);
        return JsonLegacy.ENTRADA_SALIDA.toJsonTree(salida).getAsJsonObject();
    }

    /**
     * Legacy {@code logica}. The result is per item and the LAST item wins on {@code P_OUT_VALIDO} (TES-A-12); the error counter is 1 whatever
     * the number of errors (TES-A-16); an annulled nomina ends the loop (TES-A-11).
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Salida logica(Entrada in) {
        Salida salida = new Salida();
        salida.P_OUT_VALIDO = "S";
        for (NominaTransferencia n : in.p_NominaTransferencia) {
            if (!n.IdMedioDePago.equals(MEDIO_TDP)) {
                // LEGACY DEFECT kept (see README TES-A-12): a non-TDP item resets P_OUT_VALIDO to S even after an error
                salida.P_OUT_VALIDO = "S";
                continue;
            }
            NominaTransferenciaDCV nomina = jdbc.query(SQL_NOMINA, Map.of("IdNominaTransferencia", n.IdNominaTransferencia),
                    Filas.de(NominaTransferenciaDCV.class)).get(0);
            switch (nomina.IdEstado) {
                case ESTADO_INGRESADA -> validarClave(in, n, nomina, salida);
                case ESTADO_RECEPCIONADA -> error(in, salida, "NOMINA_TRANSFERENCIA_RECEPCIONADO");
                case ESTADO_ANULADA -> {
                    error(in, salida, "NOMINA_TRANSFERENCIA_ANULADO");
                    return salida;
                }
                default -> {
                    // another state matches no rule: nothing changes (TES-A-13)
                }
            }
        }
        return salida;
    }

    private void validarClave(Entrada in, NominaTransferencia n, NominaTransferenciaDCV nomina, Salida salida) {
        if (nomina.IdTipoOperacion == null || nomina.IdTipoOperacion.trim().isEmpty()) {
            salida.P_OUT_VALIDO = "S"; // the nomina comes from a manual egreso: the validation does not apply
        } else if (nomina.ClaveDCV == null || nomina.ClaveDCV.trim().isEmpty()) {
            error(in, salida, "UI_DEBE_AGREGAR_CLAVE_DCV");
        } else {
            List<TransferenciaDCV> coincidentes = jdbc.query(SQL_TRANSFERENCIA,
                    Map.of("IdNominaTransferencia", n.IdNominaTransferencia, "ClaveDCV", nomina.ClaveDCV), Filas.de(TransferenciaDCV.class));
            if (coincidentes.isEmpty()) {
                error(in, salida, "UI_CLAVE_DCV_NO_COINCIDE_CON_CLAVE_TRANSFERENCIA_TITULOS_DEPOSITO_VALORES");
            } else {
                salida.P_OUT_VALIDO = "S";
            }
        }
    }

    private void error(Entrada in, Salida salida, String codigo) {
        salida.P_OUT_VALIDO = "N";
        salida.P_OUT_ERRORESCONTADOR = 1L; // LEGACY DEFECT kept (see README TES-A-16): logica never counts above 1
        salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, codigo, diccionario.mensaje(in.p_IdEmpresa, codigo, Map.of())));
    }
}
