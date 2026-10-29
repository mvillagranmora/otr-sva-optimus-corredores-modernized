package cl.bcs.facturacion.jobs.domain;

/**
 * Immutable descriptor for a single field within a file-format definition.
 *
 * <ul>
 *   <li>{@code tipoRegistro} — record type (e.g. 1=header, 2=detail)</li>
 *   <li>{@code campo} — field name key used for lookup</li>
 *   <li>{@code tipoDato} — one of CHAR, NUM, DEC, DATE</li>
 *   <li>{@code largo} — total field width in characters</li>
 *   <li>{@code decimales} — decimal places (relevant for DEC type)</li>
 *   <li>{@code valorDefecto} — default value applied when input is blank</li>
 *   <li>{@code formato} — date pattern (e.g. "ddMMyyyy") used for DATE type</li>
 * </ul>
 */
public record CampoFormato(
        int tipoRegistro,
        String campo,
        String tipoDato,
        int largo,
        int decimales,
        String valorDefecto,
        String formato
) {}
