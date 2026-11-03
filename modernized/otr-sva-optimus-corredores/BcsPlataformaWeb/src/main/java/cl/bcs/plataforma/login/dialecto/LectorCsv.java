package cl.bcs.plataforma.login.dialecto;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the two equivalence CSV files: UTF-8, comma separated, a header row, columns found by NAME (extra columns are allowed), quoted cells with
 * doubled quotes, LF or CRLF line ends and an optional BOM.
 */
final class LectorCsv {

    private LectorCsv() {
    }

    /** The rows as maps from header name to cell; the header must contain every one of {@code obligatorias}. */
    static List<Map<String, String>> filas(Reader origen, List<String> obligatorias) {
        List<List<String>> tabla = tabla(leer(origen));
        if (tabla.isEmpty()) {
            throw new IllegalArgumentException("empty CSV");
        }
        List<String> cabecera = tabla.get(0);
        List<String> faltan = new ArrayList<>(obligatorias);
        faltan.removeAll(cabecera);
        if (!faltan.isEmpty()) {
            throw new IllegalArgumentException("CSV header lacks column(s): " + String.join(", ", faltan));
        }
        List<Map<String, String>> filas = new ArrayList<>();
        for (List<String> celdas : tabla.subList(1, tabla.size())) {
            Map<String, String> fila = new LinkedHashMap<>();
            for (int i = 0; i < cabecera.size(); i++) {
                fila.put(cabecera.get(i), i < celdas.size() ? celdas.get(i) : "");
            }
            filas.add(fila);
        }
        return filas;
    }

    private static String leer(Reader origen) {
        try (origen) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int n;
            while ((n = origen.read(buffer)) > 0) {
                sb.append(buffer, 0, n);
            }
            return sb.length() > 0 && sb.charAt(0) == '﻿' ? sb.substring(1) : sb.toString();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read the equivalence CSV", e);
        }
    }

    static List<List<String>> tabla(String texto) {
        List<List<String>> filas = new ArrayList<>();
        List<String> fila = new ArrayList<>();
        StringBuilder celda = new StringBuilder();
        boolean entreComillas = false;
        boolean hayContenido = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (entreComillas) {
                if (c != '"') {
                    celda.append(c);
                } else if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                    celda.append('"');
                    i++;
                } else {
                    entreComillas = false;
                }
            } else if (c == '"') {
                entreComillas = true;
                hayContenido = true;
            } else if (c == ',') {
                fila.add(celda.toString());
                celda.setLength(0);
                hayContenido = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') {
                    i++;
                }
                if (hayContenido || celda.length() > 0) {
                    fila.add(celda.toString());
                    filas.add(fila);
                }
                fila = new ArrayList<>();
                celda.setLength(0);
                hayContenido = false;
            } else {
                celda.append(c);
                hayContenido = true;
            }
        }
        if (hayContenido || celda.length() > 0) {
            fila.add(celda.toString());
            filas.add(fila);
        }
        return filas;
    }
}
