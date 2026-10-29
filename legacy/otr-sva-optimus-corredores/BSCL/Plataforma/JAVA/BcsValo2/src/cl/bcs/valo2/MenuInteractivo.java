package cl.bcs.valo2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public class MenuInteractivo {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    private static void printOption(
            final String option,
            final String title) {
        System.out.printf("%s:\t%s\n", option, title);
    }

    public static BigDecimal readBigDecimal(
            final String prompt,
            final BigDecimal defaultValue) {
        while (true) {
            try {
                final String s = readString(prompt, defaultValue.toString());
                return new BigDecimal(s);
            } catch (final Exception ex) {
                System.out.println("Por favor, ingrese un numero!");
            }
        }
    }

    public static boolean readBoolean(
            final String prompt) {
        while (true) {
            try {
                final String s = readString(prompt, "N");
                if (s.compareToIgnoreCase("S") == 0) {
                    return true;
                }

                if (s.compareToIgnoreCase("N") == 0) {
                    return false;
                }
            } catch (final IOException ex) {
                // Nada a proposito.
            }

            System.out.println("Por favor, ingrese S o N!");
        }
    }

    public static Date readDate(
            final String prompt,
            final Date defaultValue) {
        while (true) {
            try {
                final String s = readString(prompt, DATE_FORMAT.format(defaultValue));
                return DATE_FORMAT.parse(s);
            } catch (final Exception ex) {
                System.out.println("Por favor, ingrese una fecha!");
            }
        }
    }

    public static Integer readInteger(
            final String prompt,
            final Integer defaultValue) {
        while (true) {
            try {
                final String s = readString(prompt, defaultValue.toString());
                return Integer.valueOf(s);
            } catch (final Exception ex) {
                System.out.println("Por favor, ingrese un numero entero!");
            }
        }
    }

    public static String readString(
            final String prompt)
                    throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.print(prompt + ": ");

        final String ret = in.readLine();
        if (ret == null) {
            return null;
        }

        return ret.toUpperCase();
    }

    public static String readString(
            final String prompt,
            final String defaultValue)
                    throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.print(prompt + " [" + defaultValue + "]: ");

        final String ret = in.readLine();
        if (ret == null) {
            return null;
        }

        if (ret.length() == 0) {
            return defaultValue;
        }

        return ret;
    }

    private final Map<String, String> options;
    private final String title;

    public MenuInteractivo(
            final String title,
            final Map<String, String> options) {
        this.title = title;
        this.options = options;
    }

    private void printMenu() {
        printTitle();

        for (final Entry<String, String> e : options.entrySet()) {
            printOption(e.getKey(), e.getValue());
        }
    }

    private void printTitle() {
        System.out.println();
        System.out.println(title);
    }

    public String selectOption() {
        while (true) {
            printMenu();

            String option;
            try {
                option = readString("Ingrese opcion (<enter> para salir)");
            } catch (final IOException e) {
                return "";
            }

            if (option == null || option.isEmpty() || options.keySet().contains(option)) {
                return option;
            }

            System.out.println("Por favor, seleccione una opcion de la lista!");
        }
    }
}
