package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.ConsultarTotalesOrigenPorDeposito;
import cl.bcs.apv.api.dto.consultartotalesorigenpordeposito.ConsultarTotalesOrigenPorDepositoEntrada;
import cl.bcs.apv.api.dto.consultartotalesorigenpordeposito.ConsultarTotalesOrigenPorDepositoResultado;
import cl.bcs.apv.api.dto.consultartotalesorigenpordeposito.TotalesOrigen;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ConsultarTotalesOrigenPorDeposito} (family C): totals per origin of a deposit, read through the stored procedure
 * {@code BSCL.APV.BD.Servicios::APV.ConsultarTotalesOrigenPorDeposito} (service {@code StoredProcedure} of the Colaboradores port).
 * Legacy {@code cl.bcs.APV.ConsultarTotalesOrigenPorDeposito} (call and logica).
 */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class ConsultarTotalesOrigenPorDepositoServicio implements ConsultarTotalesOrigenPorDeposito {

    private static final String SQL_CONSULTAR_TOTALES_ORIGEN_POR_DEPOSITO = SqlLegacy.recurso("Consultar_Totales_Origen_Call");

    private final Colaboradores colaboradores;

    public ConsultarTotalesOrigenPorDepositoServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion,
            DiccionarioMensajes diccionario, Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.colaboradores = colaboradores;
    }

    /** Output row of the procedure (legacy {@code TotalesOrigen}). */
    public static class TotalesOrigenFila {
        public String Origen;
        public BigDecimal Total;
    }

    /** Legacy {@code ErrorDetalleCall} / {@code AdvertenciaDetalleCall}: code and description of one procedure message. */
    public static class DetalleSp {
        public String Codigo;
        public String Descripcion;
    }

    @Override
    public ConsultarTotalesOrigenPorDepositoResultado ejecutar(ConsultarTotalesOrigenPorDepositoEntrada in) {
        try {
            List<MensajeLegacy> mensajes = new ArrayList<>();
            List<TotalesOrigen> totales = new ArrayList<>();

            HashMap<String, Object> paramsIn = new HashMap<>();
            paramsIn.put("IdEmpresa", in.idEmpresa());
            paramsIn.put("IdUsuario", in.idUsuario());
            paramsIn.put("IdModulo", in.idModulo());
            paramsIn.put("IdRecaudacion", in.idRecaudacion());

            JsonObject resultSP = Colab.procedimiento(colaboradores, SQL_CONSULTAR_TOTALES_ORIGEN_POR_DEPOSITO, paramsIn);

            // a scalar OUT parameter the procedure does not answer is null (the legacy SQL.call map), and the unboxing below then throws NullPointerException
            Long erroresContador = resultSP.has("p_out_ErroresContador") && !resultSP.get("p_out_ErroresContador").isJsonNull()
                    ? resultSP.get("p_out_ErroresContador").getAsLong() : null;
            if (erroresContador.equals(Long.valueOf(0))) {
                List<TotalesOrigenFila> filas = tabla(resultSP, "p_out_TotalesOrigen", TotalesOrigenFila.class);
                totales = null;
                if (filas != null) {
                    totales = new ArrayList<>();
                    for (TotalesOrigenFila f : filas) {
                        totales.add(new TotalesOrigen(f.Origen, f.Total));
                    }
                }
            } else {
                // LEGACY DEFECT kept (see README APV-C-474..479): one MensajeSalida instance is reused for every error and warning (all messages end up identical)
                MensajeLegacy mensajeSalida = new MensajeLegacy();
                for (DetalleSp errorDetalle : tabla(resultSP, "p_out_ErroresDetalle", DetalleSp.class)) {
                    mensajeSalida.codigo = errorDetalle.Codigo;
                    mensajeSalida.descripcion = errorDetalle.Descripcion;
                    mensajes.add(mensajeSalida);
                }
                for (DetalleSp advertenciaDetalle : tabla(resultSP, "p_out_AdvertenciasDetalle", DetalleSp.class)) {
                    mensajeSalida.codigo = advertenciaDetalle.Codigo;
                    mensajeSalida.descripcion = advertenciaDetalle.Descripcion;
                    mensajes.add(mensajeSalida);
                }
            }
            return new ConsultarTotalesOrigenPorDepositoResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes), totales);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    /** A table OUT parameter: present only when the procedure answers it (otherwise null, as in the legacy SQL.call map). */
    private static <T> List<T> tabla(JsonObject resultSP, String nombre, Class<T> tipo) {
        JsonElement v = resultSP.get(nombre);
        if (v == null || v.isJsonNull()) {
            return null;
        }
        List<T> filas = new ArrayList<>();
        JsonArray a = v.getAsJsonArray();
        for (JsonElement r : a) {
            filas.add(Colab.de(r.getAsJsonObject(), tipo));
        }
        return filas;
    }
}
