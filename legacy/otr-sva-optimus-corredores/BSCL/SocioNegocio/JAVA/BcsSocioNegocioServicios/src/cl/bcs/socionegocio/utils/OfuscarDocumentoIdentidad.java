package cl.bcs.socionegocio.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OfuscarDocumentoIdentidad {

    static final Logger logger = LogManager.getLogger(OfuscarDocumentoIdentidad.class);
    static final String LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String ofuscar(String valor) {
        logger.trace("INICIO::DocumentoIdentidadGenerico.ofuscar");

        if (valor == null || valor.trim().isEmpty()) {
            return valor;
        }

        valor = valor.replaceAll("\\.", "");

        Pattern pattern = Pattern.compile("^\\d{7,8}-[0-9kK]{1}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(valor);

        if (matcher.matches()) {
            valor = generarRut(valor);
        } else {
            valor = generarPasaporte(valor);
        }

        logger.trace("FIN::DocumentoIdentidadGenerico.ofuscar");

        return valor;
    }

    private static String generarRut(String valor) {
        logger.trace("INICIO::DocumentoIdentidadGenerico.generarRut");
        String rutOriginal = valor.replace(".", "").toUpperCase();

        long numeroRut = obtenerNumeroDeterministico(rutOriginal);

        String rut = String.valueOf(numeroRut);
        String dvValido = calcularDV(rut);
        String dvInvalido = generarDVInvalido(dvValido);

        logger.trace("FIN::DocumentoIdentidadGenerico.generarRut");
        return rut + "-" + dvInvalido;
    }

    private static String generarPasaporte(String valor) {
        logger.trace("INICIO::DocumentoIdentidadGenerico.generarPasaporte");

        try {
            String valorNormalizado = valor.toUpperCase().trim();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(valorNormalizado.getBytes(StandardCharsets.UTF_8));

            StringBuilder resultado = new StringBuilder();

            for (byte b : hash) {
                int indice = (b & 0xFF) % LETRAS.length();
                resultado.append(LETRAS.charAt(indice));
            }

            logger.trace("FIN::DocumentoIdentidadGenerico.generarPasaporte");

            return resultado.substring(0, 10);

        } catch (Exception e) {
            logger.error("Error generando pasaporte determinístico", e);
            throw new RuntimeException(e);
        }
    }
    
    private static String generarDVInvalido(String dvValido) {

        if ("K".equals(dvValido)) {
            return "0";
        }

        if ("0".equals(dvValido)) {
            return "1";
        }

        int dv = Integer.parseInt(dvValido);
        return String.valueOf((dv + 1) % 10);
    }

    private static long obtenerNumeroDeterministico(String valor) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(valor.getBytes(StandardCharsets.UTF_8));

            BigInteger numero = new BigInteger(1, hash);

            return numero.mod(BigInteger.valueOf(99999999L)).add(BigInteger.ONE).longValue();

        } catch (Exception e) {
            logger.error("Error generando valor determinístico", e);
            throw new RuntimeException(e);
        }
    }


    private static String calcularDV(String rut) {
        int suma = 0;
        int multiplicador = 2;

        for (int i = rut.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(rut.charAt(i)) * multiplicador;

            multiplicador++;

            if (multiplicador > 7) {
                multiplicador = 2;
            }
        }

        int resto = suma % 11;
        int dv = 11 - resto;

        if (dv == 11) {
            return "0";
        }

        if (dv == 10) {
            return "K";
        }

        return String.valueOf(dv);
    }

    public static void main(String[] args) {
        String rutPrueba = "16.338.637-2";

        System.out.println("RUT 1: " + OfuscarDocumentoIdentidad.ofuscar(rutPrueba));
        System.out.println("RUT 2: " + OfuscarDocumentoIdentidad.ofuscar(rutPrueba));
        System.out.println("RUT 3: " + OfuscarDocumentoIdentidad.ofuscar("16338637-2"));

        String pasaportePrueba = "AF130000";

        System.out.println("PASAPORTE 1: " + OfuscarDocumentoIdentidad.ofuscar(pasaportePrueba));
        System.out.println("PASAPORTE 2: " + OfuscarDocumentoIdentidad.ofuscar(pasaportePrueba));
    }
}