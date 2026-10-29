package cl.bcs.tesoreria.javacall.ban;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.tesoreria.javacall.kit.SqlNombrado;

/**
 * Rounds amounts and exchange rates with the decimals and truncate/round rule configured per currency in {@code PLA.MonedaFormato} (use GEN):
 * the part of the legacy platform helper {@code MonedaFormato} that {@code ConsultarLibretaBanco} uses (MONTO and TIPOCAMBIO). The format table
 * is read once and kept, as the legacy static did.
 */
@Component
public class FormatoMoneda {

    public static final String MONTO = "MONTO";
    public static final String TIPOCAMBIO = "TIPOCAMBIO";

    private static final String SQL_FORMATOS = SqlNombrado.recurso("Formato_ConsultarMonedaFormato");
    private static final String USO = "GEN";

    /** Row of PLA.MonedaFormato (only the columns of the two supported formats). */
    public record Formato(String IdMoneda, String IdUsoMonedaFormato, String IdTipoInstrumento, String IdTipoOperacion, Integer MontoDecimales,
            String MontoTrucaRedondea, Integer TipoCambioDecimales, String TipoCambioTrucaRedondea) {
    }

    private final SqlNombrado sql;
    private volatile List<Formato> formatos;

    public FormatoMoneda(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** The rounded value (legacy {@code MonedaFormato.formatear(con, valor, tipo, moneda, tipoInstrumento, tipoOperacion).valor}). */
    public BigDecimal formatear(BigDecimal valor, String tipoFormato, String idMoneda, String idTipoInstrumento, String idTipoOperacion)
            throws Exception {
        if (idMoneda == null) {
            throw new Exception("debe especificar moneda");
        }
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        Formato formato = buscar(idMoneda, idTipoInstrumento, idTipoOperacion);
        if (formato == null) {
            throw new Exception("formato no encontrado moneda " + idMoneda + " uso " + USO + " tipo instrumento " + idTipoInstrumento
                    + " tipo operacion " + idTipoOperacion);
        }
        String truncaRedondea;
        Integer decimales;
        if (tipoFormato.equalsIgnoreCase(MONTO)) {
            truncaRedondea = formato.MontoTrucaRedondea();
            decimales = formato.MontoDecimales();
        } else if (tipoFormato.equalsIgnoreCase(TIPOCAMBIO)) {
            truncaRedondea = formato.TipoCambioTrucaRedondea();
            decimales = formato.TipoCambioDecimales();
        } else {
            throw new Exception("tipoFormato " + tipoFormato + " no valido");
        }
        if (truncaRedondea.equalsIgnoreCase("R")) {
            return v.setScale(decimales, RoundingMode.HALF_UP);
        }
        if (truncaRedondea.equalsIgnoreCase("T")) {
            return v.setScale(decimales, RoundingMode.FLOOR);
        }
        throw new Exception("trunca o redondea invalido " + truncaRedondea + ".");
    }

    /** Most specific format first: instrument type + operation type, then instrument type, then the currency alone. */
    private Formato buscar(String idMoneda, String idTipoInstrumento, String idTipoOperacion) throws SQLException {
        List<Formato> todos = formatos();
        for (Formato f : todos) {
            if (f.IdUsoMonedaFormato().equalsIgnoreCase(USO) && f.IdMoneda().equalsIgnoreCase(idMoneda)
                    && f.IdTipoInstrumento().equalsIgnoreCase(idTipoInstrumento) && f.IdTipoOperacion().equalsIgnoreCase(idTipoOperacion)) {
                return f;
            }
        }
        for (Formato f : todos) {
            if (f.IdUsoMonedaFormato().equalsIgnoreCase(USO) && f.IdMoneda().equalsIgnoreCase(idMoneda)
                    && f.IdTipoInstrumento().equalsIgnoreCase(idTipoInstrumento)) {
                return f;
            }
        }
        for (Formato f : todos) {
            if (f.IdUsoMonedaFormato().equalsIgnoreCase(USO) && f.IdMoneda().equalsIgnoreCase(idMoneda)) {
                return f;
            }
        }
        return null;
    }

    private List<Formato> formatos() throws SQLException {
        List<Formato> cache = formatos;
        if (cache == null) {
            cache = sql.consultar(SQL_FORMATOS, Map.of(), Formato.class);
            formatos = cache;
        }
        return cache;
    }
}
