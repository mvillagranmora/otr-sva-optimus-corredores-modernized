package com.opencsv;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * ORACLE SHIM of com.opencsv.CSVWriter (opencsv 3.x/4.x API used by the legacy: {@code new CSVWriter(writer, ';', CSVWriter.NO_QUOTE_CHARACTER)},
 * {@code writeAll(List<String[]>)}, {@code close()}). The 3-argument constructor does not exist in the opencsv 5.9 available in the local maven
 * repository, so the semantics of the 4.x implementation are reproduced here: separator between fields, no quoting with NO_QUOTE_CHARACTER,
 * a field containing the escape character is escaped by doubling it, lines end with "\n". ASSUMPTION: the legacy runtime used opencsv 3.x/4.x.
 */
public class CSVWriter implements Closeable {
    public static final char DEFAULT_ESCAPE_CHARACTER = '"';
    public static final char NO_QUOTE_CHARACTER = '\u0000';
    public static final char NO_ESCAPE_CHARACTER = '\u0000';
    public static final String DEFAULT_LINE_END = "\n";

    private final Writer rawWriter;
    private final char separator;
    private final char quotechar;
    private final char escapechar;

    public CSVWriter(Writer writer, char separator, char quotechar) {
        this(writer, separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
    }

    public CSVWriter(Writer writer, char separator, char quotechar, char escapechar) {
        this.rawWriter = writer;
        this.separator = separator;
        this.quotechar = quotechar;
        this.escapechar = escapechar;
    }

    public void writeAll(List<String[]> allLines) throws IOException {
        for (String[] line : allLines) writeNext(line);
    }

    public void writeNext(String[] nextLine) throws IOException {
        if (nextLine == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nextLine.length; i++) {
            if (i != 0) sb.append(separator);
            String next = nextLine[i];
            if (next == null) continue;
            boolean special = next.indexOf(quotechar) != -1 || next.indexOf(escapechar) != -1 || next.contains("\n") || next.contains("\r");
            if (special && quotechar != NO_QUOTE_CHARACTER) sb.append(quotechar);
            if (special) {
                for (int j = 0; j < next.length(); j++) {
                    char c = next.charAt(j);
                    if (escapechar != NO_ESCAPE_CHARACTER && c == quotechar) sb.append(escapechar).append(c);
                    else if (escapechar != NO_ESCAPE_CHARACTER && c == escapechar) sb.append(escapechar).append(c);
                    else sb.append(c);
                }
            } else {
                sb.append(next);
            }
            if (special && quotechar != NO_QUOTE_CHARACTER) sb.append(quotechar);
        }
        sb.append(DEFAULT_LINE_END);
        rawWriter.write(sb.toString());
    }

    @Override
    public void close() throws IOException {
        rawWriter.flush();
        rawWriter.close();
    }
}
