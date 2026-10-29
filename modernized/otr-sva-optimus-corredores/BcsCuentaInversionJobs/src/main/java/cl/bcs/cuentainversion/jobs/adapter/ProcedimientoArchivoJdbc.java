package cl.bcs.cuentainversion.jobs.adapter;

import cl.bcs.cuentainversion.jobs.job.port.ProcedimientoArchivo;
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
 * Production {@link ProcedimientoArchivo}: the HANA procedure that feeds a generated file, as legacy {@code GenerarArchivo.logica} ran it
 * (GenerarArchivo.java:139-205): {@code prepareCall}, the four fixed parameters bound BY NAME ({@code p_IdEmpresa}, {@code p_IdUsuario},
 * {@code p_CodigoFormatoArchivo}, {@code p_IdPrograma}), {@code execute()}, then every result set with {@code getColumnName} and
 * {@code getString}. It runs on the connection of the job transaction and lets the {@link java.sql.SQLException} through unchanged.
 */
@Component
public class ProcedimientoArchivoJdbc implements ProcedimientoArchivo {

    static final String[] NOMBRES = {"p_IdEmpresa", "p_IdUsuario", "p_CodigoFormatoArchivo", "p_IdPrograma"};

    private final DataSource dataSource;

    public ProcedimientoArchivoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ResultadoProcedimiento> ejecutar(String llamada, List<Object> parametros) throws Exception {
        Connection con = DataSourceUtils.getConnection(dataSource);
        try (CallableStatement cs = con.prepareCall(llamada)) {
            for (int i = 0; i < parametros.size(); i++) {
                Object parametro = parametros.get(i);
                if (parametro instanceof Long valor) {
                    cs.setLong(NOMBRES[i], valor);
                } else {
                    cs.setString(NOMBRES[i], (String) parametro);
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
