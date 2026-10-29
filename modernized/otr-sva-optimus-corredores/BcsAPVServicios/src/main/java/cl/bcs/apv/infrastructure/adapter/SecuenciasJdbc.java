package cl.bcs.apv.infrastructure.adapter;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * SAP HANA implementation of {@link Secuencias}: the next value of a named database sequence, e.g.
 * {@code BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV}.
 *
 * <p>HANA SYNTAX UNVERIFIED: the legacy {@code Utils.getSecuencia(con, name, cantidad)} drew {@code cantidad} values with
 * {@code SELECT "<name>".NEXTVAL FROM SERIES_GENERATE_BIGINT(1, 0, cantidad)}; one value is drawn here with {@code SELECT "<name>".NEXTVAL FROM DUMMY}
 * (HANA sequence access, {@code DUMMY} is the one-row system table). It has not been run against a HANA instance (the characterization tests
 * use a recording double of the port). The sequence name is an identifier, not a bind variable: it is validated against a whitelist of identifier
 * characters before it is quoted into the statement.
 */
@Component
public class SecuenciasJdbc implements Secuencias {

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("[A-Za-z0-9_.:]+");

    private final NamedParameterJdbcTemplate jdbc;

    public SecuenciasJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long siguiente(String nombre) {
        Long valor = jdbc.queryForObject(sentencia(nombre), Map.of(), Long.class);
        if (valor == null) {
            throw new IllegalStateException("The sequence " + nombre + " returned no value");
        }
        return valor;
    }

    /** The statement for one sequence; rejects names that are not plain identifiers (no quotes, spaces or statement separators). */
    static String sentencia(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new IllegalArgumentException("Invalid sequence name: " + nombre);
        }
        return "SELECT \"" + nombre + "\".NEXTVAL FROM DUMMY";
    }
}
