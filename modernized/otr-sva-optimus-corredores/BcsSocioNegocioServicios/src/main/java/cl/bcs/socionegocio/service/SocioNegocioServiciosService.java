package cl.bcs.socionegocio.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Pure-logic service: all methods are public static — no HANA dependency.
 * Extracted from:
 *   - cl.bcs.socionegocio.utils.IUP
 *   - cl.bcs.socionegocio.utils.OfuscarDocumentoIdentidad
 *   - cl.bcs.socionegocio.soc.SocioNegocioUtils
 */
@Service
public class SocioNegocioServiciosService {

    // -------------------------------------------------------------------------
    // IUP — Identity Unique Prefix generation
    // -------------------------------------------------------------------------

    private static final int LARGO_IUP = 14;

    private enum ConfiguracionDocumentoIdentidad {
        DNI("02", 8), RUC("03", 11), PAS("20", 11), CDI("00", 8),
        FIC_N("20", 8), FIC_M("22", 8), FIC_J("22", 8), PAI("22", 11),
        RUT("20", 8), CEX("20", 8), CE("20", 8), ERROR("EE", 0);

        final String codigoDocumento;
        final int largo;

        ConfiguracionDocumentoIdentidad(String codigoDocumento, int largo) {
            this.codigoDocumento = codigoDocumento;
            this.largo = largo;
        }
    }

    private static ConfiguracionDocumentoIdentidad getConfiguracionDocumento(
            String tipoDocumentoIdentidad, String tipoPersona) {
        if (tipoDocumentoIdentidad == null) {
            return ConfiguracionDocumentoIdentidad.ERROR;
        }
        switch (tipoDocumentoIdentidad.toUpperCase()) {
            case "DNI": return ConfiguracionDocumentoIdentidad.DNI;
            case "RUC": return ConfiguracionDocumentoIdentidad.RUC;
            case "PAS": return ConfiguracionDocumentoIdentidad.PAS;
            case "CDI": return ConfiguracionDocumentoIdentidad.CDI;
            case "FIC":
                if ("J".equalsIgnoreCase(tipoPersona)) return ConfiguracionDocumentoIdentidad.FIC_J;
                if ("M".equalsIgnoreCase(tipoPersona)) return ConfiguracionDocumentoIdentidad.FIC_M;
                return ConfiguracionDocumentoIdentidad.FIC_N;
            case "PAI": return ConfiguracionDocumentoIdentidad.PAI;
            case "RUT": case "CEX": case "CE": return ConfiguracionDocumentoIdentidad.RUT;
            default: return ConfiguracionDocumentoIdentidad.ERROR;
        }
    }

    public static String iupGenerar(String documentoIdentidad, String tipoDocumentoIdentidad, String tipoPersona) {
        ConfiguracionDocumentoIdentidad conf = getConfiguracionDocumento(tipoDocumentoIdentidad, tipoPersona);
        return StringUtils.rightPad(
                conf.codigoDocumento + StringUtils.leftPad(documentoIdentidad, conf.largo, "0"),
                LARGO_IUP, "0");
    }

    public static String iupObtenerDocumentoAsociado(String documentoIdentidad, String tipoDocumentoIdentidad, String tipoPersona) {
        ConfiguracionDocumentoIdentidad conf = getConfiguracionDocumento(tipoDocumentoIdentidad, tipoPersona);
        return documentoIdentidad.substring(2, conf.largo + 2);
    }

    // -------------------------------------------------------------------------
    // OfuscarDocumentoIdentidad — document obfuscation
    // -------------------------------------------------------------------------

    private static final String LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String ofuscarDocumentoIdentidad(String valor) {
        if (valor == null || valor.isEmpty()) return valor;
        String v = valor.replace(".", "");
        if (v.matches("^\\d{7,8}-[0-9kK]{1}$")) return generarRut(v);
        return generarPasaporte(v);
    }

    private static String generarRut(String valor) {
        long rut = obtenerNumeroDeterministico(valor);
        String dv = calcularDV(rut);
        String dvInvalido = generarDVInvalido(dv);
        return rut + "-" + dvInvalido;
    }

    private static String generarPasaporte(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(LETRAS.charAt(Math.abs(b) % LETRAS.length()));
            }
            return sb.substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String calcularDV(long rut) {
        int[] multiplicadores = {2, 3, 4, 5, 6, 7};
        String rutStr = String.valueOf(rut);
        int suma = 0;
        int idx = 0;
        for (int i = rutStr.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(rutStr.charAt(i)) * multiplicadores[idx % 6];
            idx++;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return "0";
        if (resto == 10) return "K";
        return String.valueOf(resto);
    }

    private static String generarDVInvalido(String dvValido) {
        if ("K".equals(dvValido)) return "0";
        if ("0".equals(dvValido)) return "1";
        return String.valueOf((Integer.parseInt(dvValido) + 1) % 10);
    }

    private static long obtenerNumeroDeterministico(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            BigInteger bi = new BigInteger(1, hash);
            return bi.mod(BigInteger.valueOf(99_999_999L)).longValue() + 1;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    // -------------------------------------------------------------------------
    // SocioNegocioUtils — general-purpose utilities
    // -------------------------------------------------------------------------

    public static <T> T obtenerValorNoNulo(T valor, T valorPorDefecto) {
        return valor == null ? valorPorDefecto : valor;
    }

    public static String obtenerIdEstadoAutorizacion(Boolean autorizado) {
        return Boolean.TRUE.equals(autorizado) ? "ING" : "PEN";
    }

    public static String obtenerCampoRegistroTruncado(String campoRegistro) {
        if (campoRegistro == null) return null;
        return campoRegistro.length() > 40 ? campoRegistro.substring(0, 40) : campoRegistro;
    }
}
