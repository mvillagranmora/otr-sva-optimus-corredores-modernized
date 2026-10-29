package cl.bcs.cuentainversion.infrastructure.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra;

/**
 * Production {@link DiccionarioMensajes}: loads the dictionary of the company language once and keeps it in memory
 * (legacy {@code Diccionario} cached it in a static map for the life of the JVM).
 */
@Component
public class DiccionarioJdbc implements DiccionarioMensajes {

    private static final String SQL = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/Diccionario_ConsultarDiccionario.sql");
    private static final String SIN_TRADUCCION = "sin traduccion";

    private final NamedParameterJdbcTemplate jdbc;
    private final Map<Long, Map<String, String>> porEmpresa = new ConcurrentHashMap<>();

    public DiccionarioJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String traducir(Long idEmpresa, String codigo, Map<String, String> valores) {
        String texto = porEmpresa.computeIfAbsent(idEmpresa, this::cargar).get(codigo);
        if (texto == null || valores == null) {
            return codigo + " " + SIN_TRADUCCION;
        }
        for (Map.Entry<String, String> v : valores.entrySet()) {
            texto = texto.replace("::" + v.getKey() + "::", v.getValue() == null ? "" : v.getValue());
        }
        return texto;
    }

    private Map<String, String> cargar(Long idEmpresa) {
        Map<String, String> textos = new HashMap<>();
        jdbc.query(SQL, new MapSqlParameterSource("IdEmpresa", idEmpresa).addValue("CodigoParametro", "PLA.LocalizacionSistema"),
                rs -> {
                    textos.put(rs.getString("CodDiccionario"), rs.getString("Descripcion"));
                });
        return textos;
    }
}
