package com.opencsv;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * ORACLE SHIM of com.opencsv.CSVWriter, reproducing opencsv 3.x / 4.x (the API the legacy uses: the constructors
 * {@code (Writer, char)} and {@code (Writer, char, char)}, {@code writeNext(String[])}, {@code writeNext(String[], boolean)},
 * {@code writeAll}, {@code close()}). Those constructors do not exist in the opencsv 5.9 of the local maven repository.
 *
 * <p>ASSUMPTION (README "Assumptions", open question for the owner): the legacy runtime shipped opencsv 3.x / 4.x, whose
 * observable behavior is reproduced here:
 * <ul>
 *   <li>a field is quoted when {@code applyQuotesToAll} (the default of writeNext(String[])) or when it contains the quote
 *       character, the escape character or a line break; a field that only contains the separator is NOT quoted (5.x quotes it);</li>
 *   <li>inside a quoted/special field the quote and escape characters are doubled (escape character '"');</li>
 *   <li>a null element writes nothing (the separators stay); lines end with "\n";</li>
 *   <li>writes go through a {@link PrintWriter}: an IOException is swallowed, so {@code close()} on a writer whose underlying
 *       FileWriter was already closed does NOT throw (5.x throws "Stream closed").</li>
 * </ul>
 */
public class CSVWriter implements Closeable {
    public static final char DEFAULT_ESCAPE_CHARACTER = '"';
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    public static final char DEFAULT_SEPARATOR = ',';
    public static final char NO_QUOTE_CHARACTER = '\u0000';
    public static final char NO_ESCAPE_CHARACTER = '\u0000';
    public static final String DEFAULT_LINE_END = "\n";

    private final Writer rawWriter;
    private final PrintWriter pw;
    private final char separator;
    private final char quotechar;
    private final char escapechar;

    public CSVWriter(Writer writer, char separator) {
        this(writer, separator, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }

    public CSVWriter(Writer writer, char separator, char quotechar) {
        this(writer, separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
    }

    public CSVWriter(Writer writer, char separator, char quotechar, char escapechar) {
        this.rawWriter = writer;
        this.pw = new PrintWriter(writer);
        this.separator = separator;
        this.quotechar = quotechar;
        this.escapechar = escapechar;
    }

    public void writeAll(List<String[]> allLines) {
        for (String[] line : allLines) writeNext(line);
    }

    public void writeNext(String[] nextLine) {
        writeNext(nextLine, true);
    }

    public void writeNext(String[] nextLine, boolean applyQuotesToAll) {
        if (nextLine == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nextLine.length; i++) {
            if (i != 0) sb.append(separator);
            String next = nextLine[i];
            if (next == null) continue;
            boolean special = next.indexOf(quotechar) != -1 || next.indexOf(escapechar) != -1 || next.contains("\n") || next.contains("\r");
            if ((applyQuotesToAll || special) && quotechar != NO_QUOTE_CHARACTER) sb.append(quotechar);
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
            if ((applyQuotesToAll || special) && quotechar != NO_QUOTE_CHARACTER) sb.append(quotechar);
        }
        sb.append(DEFAULT_LINE_END);
        pw.write(sb.toString());
    }

    public void flush() throws IOException {
        pw.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        pw.close();
        rawWriter.close();
    }
}
