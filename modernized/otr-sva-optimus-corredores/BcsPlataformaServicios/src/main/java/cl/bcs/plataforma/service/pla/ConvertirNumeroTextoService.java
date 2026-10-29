package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

@Service
public class ConvertirNumeroTextoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConvertirNumeroTextoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private static final String[] DECENAS = {
        "", " diez", " veinte", " treinta", " cuarenta",
        " cincuenta", " sesenta", " setenta", " ochenta", " noventa"
    };
    private static final String[] CENTENAS = {
        "", " ciento", " doscientos", " trescientos", " cuatrocientos",
        " quinientos", " seiscientos", " setecientos", " ochocientos", " novecientos"
    };
    private static final String[] NUMEROS = {
        "", " uno", " dos", " tres", " cuatro", " cinco", " seis", " siete", " ocho", " nueve",
        " diez", " once", " doce", " trece", " catorce", " quince",
        " dieciseis", " diecisiete", " dieciocho", " diecinueve",
        " veinte", " veintiuno", " veintidos", " veintitres", " veinticuatro",
        " veinticinco", " veintiseis", " veintisiete", " veintiocho", " veintinueve"
    };

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ConvertirNumeroTexto"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ConvertirNumeroTexto");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            BigDecimal numero = in.numero != null ? in.numero : BigDecimal.ZERO;
            boolean decimalesEnFraccion = Boolean.TRUE.equals(in.decimalesEnFraccion);
            String textoMoneda = in.textoMoneda != null ? in.textoMoneda : "";
            out.resultado = convert(numero, decimalesEnFraccion, textoMoneda);
        } catch (Exception e) {
            log.error("Error en ConvertirNumeroTexto: {}", e.getMessage(), e);
            out.resultado = "";
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private String convert(BigDecimal numeroCompleto, boolean decimalesEnFraccion, String textoMoneda) {
        long number = numeroCompleto.longValue();
        BigDecimal decimales = numeroCompleto.setScale(2, RoundingMode.FLOOR)
                .subtract(numeroCompleto.setScale(0, RoundingMode.FLOOR));
        decimales = decimales.movePointRight(decimales.scale());

        if (number == 0) return "cero";

        String snumber = new DecimalFormat("000000000000").format(number);
        int billions = Integer.parseInt(snumber.substring(0, 3));
        int millions = Integer.parseInt(snumber.substring(3, 6));
        int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
        int thousands = Integer.parseInt(snumber.substring(9, 12));

        String tradBillions = switch (billions) {
            case 0 -> "";
            case 1 -> "un billon ";
            default -> convertLessThanOneThousand(billions) + " billones ";
        };
        String tradMillions = switch (millions) {
            case 0 -> "";
            case 1 -> "un millon ";
            default -> convertLessThanOneThousand(millions) + " millones ";
        };
        String tradHundredThousands = switch (hundredThousands) {
            case 0 -> "";
            case 1 -> "mil ";
            default -> convertLessThanOneThousand(hundredThousands) + " mil ";
        };
        String result = (tradBillions + tradMillions + tradHundredThousands
                + convertLessThanOneThousand(thousands))
                .replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");

        if (decimalesEnFraccion) {
            return result + " con " + decimales + "/100 " + textoMoneda;
        }
        return result + " " + textoMoneda;
    }

    private String convertLessThanOneThousand(int number) {
        String soFar;
        if (number == 100) return "cien";
        else if (number % 100 < 30) {
            soFar = NUMEROS[number % 100];
            number /= 100;
        } else {
            soFar = NUMEROS[number % 10];
            number /= 10;
            if (soFar.isEmpty()) {
                soFar = DECENAS[number % 10];
                number /= 10;
            } else {
                soFar = DECENAS[number % 10] + " y" + soFar;
                number /= 10;
            }
        }
        if (number == 0) return soFar;
        return CENTENAS[number] + soFar;
    }

    public static class Entrada {
        public BigDecimal numero;
        public Boolean decimalesEnFraccion;
        public String textoMoneda;
    }

    public static class Salida {
        public String resultado;
    }
}
