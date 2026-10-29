package cl.bcs.facturacion.jobs.service;

import cl.bcs.facturacion.jobs.domain.CampoFormato;
import cl.bcs.facturacion.jobs.domain.ConfiguracionFormato;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Pure-logic service for fixed-width file field formatting.
 *
 * <p>All methods are {@code public static} so they can be called directly in tests
 * without Spring context. The {@code @Service} annotation allows Spring to manage
 * this bean for injection into controllers or other components.</p>
 *
 * <p>Implements the legacy {@code FormatoArchivo.formatear} rules exactly:
 * CHAR, NUM, DEC, DATE — with padding, default values, and decimal splitting.</p>
 *
 * <p>DEC separator note: {@code separadorDecimales} is taken from the config-level setting
 * and is NOT counted toward {@code largo}. The integer portion width is
 * {@code largo - decimales}.</p>
 */
@Service
public class FormatoArchivoService {

    /**
     * Formats a raw string value for a named field within a record type, using the
     * rules defined in {@code config}.
     *
     * @param config        format configuration (padding flag, decimal separator, field list)
     * @param tipoRegistro  record type identifier (e.g. 1=header, 2=detail)
     * @param nombreCampo   field name to look up in the configuration
     * @param valor         raw input value
     * @return formatted string, or {@code ""} if no matching field definition exists
     */
    public static String formatear(ConfiguracionFormato config,
                                   int tipoRegistro,
                                   String nombreCampo,
                                   String valor) {

        // Step 1: find the field definition
        CampoFormato campo = null;
        if (config.getCampos() != null) {
            for (CampoFormato c : config.getCampos()) {
                if (c.tipoRegistro() == tipoRegistro && nombreCampo.equals(c.campo())) {
                    campo = c;
                    break;
                }
            }
        }
        if (campo == null) {
            return "";
        }

        // Step 2: trim the value
        if (valor == null) {
            valor = "";
        }
        valor = valor.trim();

        // Step 3: apply default when value is empty and a default exists
        String valorDefecto = campo.valorDefecto();
        if (valor.isEmpty() && valorDefecto != null && !valorDefecto.isEmpty()) {
            valor = valorDefecto;
        }

        boolean rellenar = "S".equalsIgnoreCase(config.getRellenarCampo());
        String separadorDecimales = config.getSeparadorDecimales();
        int largo = campo.largo();
        int decimales = campo.decimales();
        String tipoDato = campo.tipoDato();
        String formato = campo.formato();

        return switch (tipoDato) {
            case "CHAR" -> formatearChar(valor, largo, rellenar);
            case "NUM"  -> formatearNum(valor, largo, rellenar);
            case "DEC"  -> formatearDec(valor, largo, decimales, rellenar, separadorDecimales);
            case "DATE" -> formatearDate(valor, largo, formato);
            default     -> valor;
        };
    }

    /**
     * Formats a CHAR field.
     *
     * <ul>
     *   <li>Truncates to {@code largo} if too long.</li>
     *   <li>If {@code rellenar} is true, left-aligns within {@code largo} (trailing spaces).</li>
     *   <li>Otherwise returns the value as-is.</li>
     * </ul>
     */
    public static String formatearChar(String valor, int largo, boolean rellenar) {
        if (valor.length() > largo) {
            valor = valor.substring(0, largo);
        }
        if (rellenar) {
            return String.format("%-" + largo + "s", valor);
        }
        return valor;
    }

    /**
     * Formats a NUM field.
     *
     * <ul>
     *   <li>If {@code rellenar} is true, right-aligns within {@code largo} then replaces
     *       spaces with zeros (leading zeros).</li>
     *   <li>Otherwise replaces any spaces in the value with zeros.</li>
     * </ul>
     */
    public static String formatearNum(String valor, int largo, boolean rellenar) {
        if (rellenar) {
            return String.format("%" + largo + "s", valor).replace(' ', '0');
        }
        return valor.replace(' ', '0');
    }

    /**
     * Formats a DEC field.
     *
     * <p>The separator character is extra — not counted in {@code largoTotal}.
     * Integer width = {@code largoTotal - decimales}.</p>
     *
     * <ul>
     *   <li>If {@code valor} contains no ".", treat entire value as "0".</li>
     *   <li>Split on ".": parts[0] = integer part, parts[1] = decimal part (or "0").</li>
     *   <li>Decimal part is truncated if longer than {@code decimales}.</li>
     *   <li>If {@code rellenar}: integer part is right-aligned with leading zeros;
     *       decimal part is left-aligned with trailing zeros.</li>
     *   <li>Otherwise: just replace any spaces with zeros in each part.</li>
     * </ul>
     */
    public static String formatearDec(String valor, int largoTotal, int decimales,
                                      boolean rellenar, String separadorDecimales) {
        int largoParteEntera = largoTotal - decimales;
        int largoParteDecimal = decimales;

        // If no decimal point found, treat the entire value as "0"
        if (!valor.contains(".")) {
            valor = "0";
        }

        String[] parts = valor.split("\\.", 2);
        String parteEntera = parts[0];
        String parteDecimal = (parts.length > 1) ? parts[1] : "0";

        // Truncate decimal if too long
        if (parteDecimal.length() > largoParteDecimal) {
            parteDecimal = parteDecimal.substring(0, largoParteDecimal);
        }

        String valorParteEntera;
        String valorParteDecimal;

        if (rellenar) {
            // Integer part: right-aligned → leading zeros
            valorParteEntera = String.format("%" + largoParteEntera + "s", parteEntera)
                    .replace(' ', '0');
            // Decimal part: left-aligned → trailing zeros
            valorParteDecimal = String.format("%-" + largoParteDecimal + "s", parteDecimal)
                    .replace(' ', '0');
        } else {
            valorParteEntera = parteEntera.replace(' ', '0');
            valorParteDecimal = parteDecimal.replace(' ', '0');
        }

        return valorParteEntera + separadorDecimales + valorParteDecimal;
    }

    /**
     * Formats a DATE field.
     *
     * <ul>
     *   <li>If a non-empty {@code formato} is provided and {@code valor} is non-empty,
     *       parse the value as "yyyy-MM-dd" and reformat with {@code formato}.</li>
     *   <li>Result (or raw value if no format) is left-aligned within {@code largo}.</li>
     * </ul>
     */
    public static String formatearDate(String valor, int largo, String formato) {
        if (formato != null && !formato.isEmpty() && valor != null && !valor.isEmpty()) {
            LocalDate fecha = LocalDate.parse(valor, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String formateada = fecha.format(DateTimeFormatter.ofPattern(formato));
            return String.format("%-" + largo + "s", formateada);
        }
        return String.format("%-" + largo + "s", valor != null ? valor : "");
    }
}
