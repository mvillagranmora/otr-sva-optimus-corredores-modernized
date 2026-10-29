package cl.bcs.apv.infrastructure.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;

/**
 * Production {@link DiccionarioMensajes}: loads the message dictionary of the language configured for the company
 * ({@code PLA.LocalizacionSistema}) once and keeps it in memory, as the legacy {@code Diccionario} did with a static map.
 *
 * <p>Legacy rules kept: an unknown code, or a {@code null} value map, gives {@code "<CODE> sin traduccion"}; a {@code null} value is
 * replaced by an empty text; placeholders are written {@code ::Key::}.
 */
@Component
public class DiccionarioJdbc implements DiccionarioMensajes {

    private static final String SQL = SqlNombrado.recurso("DiccionarioMensajes_Consultar");
    private static final String CODIGO_LOCALIZACION = "PLA.LocalizacionSistema";
    private static final String SIN_TRADUCCION = "sin traduccion";
    private static final String SEPARADOR = "::";

    private final NamedParameterJdbcTemplate jdbc;
    private final Map<Long, Map<String, String>> porEmpresa = new ConcurrentHashMap<>();

    public DiccionarioJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String mensaje(Long idEmpresa, String codigo, Map<String, String> valores) {
        String texto = porEmpresa.computeIfAbsent(idEmpresa, this::cargar).get(codigo);
        if (texto == null || valores == null) {
            return codigo + " " + SIN_TRADUCCION;
        }
        for (Map.Entry<String, String> v : valores.entrySet()) {
            texto = texto.replace(SEPARADOR + v.getKey() + SEPARADOR, v.getValue() == null ? "" : v.getValue());
        }
        return texto;
    }

    private Map<String, String> cargar(Long idEmpresa) {
        Map<String, String> textos = new HashMap<>();
        jdbc.query(SQL, new MapSqlParameterSource("IdEmpresa", idEmpresa).addValue("CodigoParametro", CODIGO_LOCALIZACION),
                rs -> {
                    textos.put(rs.getString("CodDiccionario"), rs.getString("Descripcion"));
                });
        return textos;
    }
}
