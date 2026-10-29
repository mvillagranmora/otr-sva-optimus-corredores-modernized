package cl.bcs.tesoreria.jobs.adapter;

import cl.bcs.tesoreria.jobs.job.port.ProcedimientoArchivo;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * Production {@link ProcedimientoArchivo}: the HANA stored procedure that feeds a bank file, as the legacy
 * {@code GenerarArchivoCargos.getService} and {@code GenerarArchivoPago.logica} ran it: {@code prepareCall}, {@code setLong} /
 * {@code setString} in order, {@code execute()}, then every result set with {@code getColumnName} and {@code getString}. It runs on
 * the connection of the job transaction (the same one {@code BcsJob} handed to the legacy job) and lets the {@link java.sql.SQLException}
 * through unchanged, as the legacy did.
 */
@Component
public class ProcedimientoArchivoJdbc implements ProcedimientoArchivo {

    private final DataSource dataSource;

    public ProcedimientoArchivoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ResultadoProcedimiento> ejecutar(String llamada, List<Object> parametros) throws Exception {
        Connection con = DataSourceUtils.getConnection(dataSource);
        try (CallableStatement cs = con.prepareCall(llamada)) {
            int indice = 1;
            for (Object parametro : parametros) {
                if (parametro instanceof Long valor) {
                    cs.setLong(indice++, valor);
                } else {
                    cs.setString(indice++, (String) parametro);
                }
            }
            List<ResultadoProcedimiento> resultados = new ArrayList<>();
            boolean hayResultado = cs.execute();
            while (hayResultado) {
                try (ResultSet rs = cs.getResultSet()) {
                    resultados.add(leer(rs));
                }
                hayResultado = cs.getMoreResults();
            }
            return resultados;
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private static ResultadoProcedimiento leer(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        List<String> columnas = new ArrayList<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            columnas.add(meta.getColumnName(i));
        }
        List<List<String>> filas = new ArrayList<>();
        while (rs.next()) {
            List<String> fila = new ArrayList<>();
            for (int i = 1; i <= columnas.size(); i++) {
                fila.add(rs.getString(i));
            }
            filas.add(fila);
        }
        return new ResultadoProcedimiento(columnas, filas);
    }
}
