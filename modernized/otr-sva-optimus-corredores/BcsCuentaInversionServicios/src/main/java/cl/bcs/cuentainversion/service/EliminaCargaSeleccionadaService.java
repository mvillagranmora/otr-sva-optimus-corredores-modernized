package cl.bcs.cuentainversion.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Utils;

/**
 * Deletes the selected bulk loads, detail first and then header (README CTA-A-109..112). Unknown ids and an empty list are
 * not errors: the answer is always "process completed".
 */
@Service
public class EliminaCargaSeleccionadaService {

    private static final String SQL_ELIMINA_DETALLE = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargaMasiva_EliminaDetalle.sql");
    private static final String SQL_ELIMINA_CABECERA = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargaMasiva_EliminaCabecera.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<Carga> p_IdCargaMasiva;
    }

    public static class Carga {
        public Long p_IdCarga;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;

    public EliminaCargaSeleccionadaService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        Entrada entrada = Utils.getGsonBuilder().fromJson(params, Entrada.class);
        SqlParameterSource[] cargas = entrada.p_IdCargaMasiva.stream()
                .map(c -> new MapSqlParameterSource("p_IdCarga", c.p_IdCarga))
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(SQL_ELIMINA_DETALLE, cargas);
        jdbc.batchUpdate(SQL_ELIMINA_CABECERA, cargas);

        Salida salida = new Salida();
        salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.OK, "UI_PROCESO_COMPLETADO",
                diccionario.traducir(entrada.p_IdEmpresa, "UI_PROCESO_COMPLETADO", DiccionarioMensajes.valores())));
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(salida.P_OUT_MENSAJES);
        return Utils.getGsonBuilder().toJsonTree(salida).getAsJsonObject();
    }
}
