package cl.bcs.normativa.cartolas.procesos;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelper;
import com.opencsv.ResultSetHelperService;

public abstract class CargadorDatos {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    private static final String NOMBRE_COLUMNA_ID_PORTAFOLIO = "IdPortafolio";
    private static final String TIMESTAMP_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    public static String serialize(
            final List<String[]> allLines)
                    throws IOException {
        final StringWriter writer = new StringWriter();
        try (final CSVWriter csvWriter = new CSVWriter(writer)) {
            for (final String[] line : allLines) {
                csvWriter.writeNext(line, false);
            }
        }

        return writer.toString();
    }

    private String[] columnNames;
    private final ResultSetHelper resultService;

    public CargadorDatos() {
        this.resultService = new ResultSetHelperService();
    }

    protected Map<Long, List<String[]>> buildMap(
            final ResultSet resultSet)
                    throws SQLException,
                    IOException {
        columnNames = resultService.getColumnNames(resultSet);

        final Map<Long, List<String[]>> map = new ConcurrentHashMap<Long, List<String[]>>();
        while (resultSet.next()) {
            final Long idPortafolio = resultSet.getLong(NOMBRE_COLUMNA_ID_PORTAFOLIO);
            final String[] entries = resultService.getColumnValues(resultSet, true,
                    DATE_FORMAT_PATTERN, TIMESTAMP_FORMAT_PATTERN);
            final List<String[]> list = getList(map, idPortafolio);
            list.add(entries);
        }

        return map;
    }

    private List<String[]> getList(
            final Map<Long, List<String[]>> map,
            final Long idPortafolio)
                    throws SQLException {
        List<String[]> list;
        if (map.containsKey(idPortafolio)) {
            list = map.get(idPortafolio);
        } else {
            list = new ArrayList<String[]>();
            list.add(columnNames);
            map.put(idPortafolio, list);
        }

        return list;
    }

    public abstract void load(
            final String nombre,
            final ResultSet resultSet)
                    throws SQLException,
                    IOException;

}
