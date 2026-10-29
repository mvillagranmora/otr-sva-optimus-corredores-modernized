package cl.bcs.tesoreria.javacall.dap;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Semicolon separated text without quote character, the output of opencsv 3.x/4.x {@code new CSVWriter(writer, ';', NO_QUOTE_CHARACTER)} that the
 * legacy used (that constructor is gone in opencsv 5, which is why this is plain Java): fields separated by {@code ;}, a double quote inside a
 * field is doubled, a NUL character is preceded by a double quote, a null field is empty, every line ends with {@code \n}. The legacy wrote a
 * UTF-8 byte order mark first (TES-B-178).
 */
public final class CsvPlano {

    private CsvPlano() {
    }

    public static byte[] aBytes(List<String[]> lineas) {
        StringBuilder sb = new StringBuilder("﻿");
        for (String[] linea : lineas) {
            for (int i = 0; i < linea.length; i++) {
                if (i != 0) {
                    sb.append(';');
                }
                String campo = linea[i];
                if (campo == null) {
                    continue;
                }
                for (int j = 0; j < campo.length(); j++) {
                    char c = campo.charAt(j);
                    if (c == '"') {
                        sb.append("\"\"");
                    } else if (c == '\u0000') {
                        sb.append('"').append(c);
                    } else {
                        sb.append(c);
                    }
                }
            }
            sb.append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
