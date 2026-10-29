package cl.bcs.normativa.cartolas.procesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import cl.bcs.normativa.cartolas.dal.ConfigService;

public final class Configs {

    private static final int DEFAULT_CALLER_BATCH_SIZE = 100;
    private static final int DEFAULT_DELETE_BATCH_SIZE = 100;
    private static final int DEFAULT_INSERT_BATCH_SIZE = 1000;

    private final int callerBatchSize;
    private final Map<String, String> configs;
    private final String formatoNombreArchivoCartola;
    private final String formatoNombreArchivoNomina;
    private final String formatoNombreArchivoZip;
    private final int insertBatchSize;
    private final String rutaRaizArchivos;
    private final int threadPoolSize;
    private final int deleteBatchSize;

    public Configs(
            final Connection connection,
            final String schema,
            long idEmpresa) throws SQLException {
        this.configs = ConfigService.lookupByIdEmpresa(connection, schema, idEmpresa);
        this.callerBatchSize = getInt("CallerBatchSize", DEFAULT_CALLER_BATCH_SIZE);
        this.insertBatchSize = getInt("InsertBatchSize", DEFAULT_INSERT_BATCH_SIZE);
        this.deleteBatchSize = getInt("DeleteBatchSize", DEFAULT_DELETE_BATCH_SIZE);
        this.threadPoolSize = getInt("ThreadPoolSize", 1);
        this.rutaRaizArchivos = configs.get("RutaRaizArchivos");
        this.formatoNombreArchivoCartola = configs.get("FormatoNombreArchivoCartola");
        this.formatoNombreArchivoNomina = configs.get("FormatoNombreArchivoNomina");
        this.formatoNombreArchivoZip = configs.get("FormatoNombreArchivoZip");
    }

    public int getCallerBatchSize() {
        return callerBatchSize;
    }

    public int getDeleteBatchSize() {
        return deleteBatchSize;
    }

    public String getFormatoNombreArchivoCartola() {
        return formatoNombreArchivoCartola;
    }

    public String getFormatoNombreArchivoNomina() {
        return formatoNombreArchivoNomina;
    }

    public String getFormatoNombreArchivoZip() {
        return formatoNombreArchivoZip;
    }

    public int getInsertBatchSize() {
        return insertBatchSize;
    }

    private int getInt(
            final String key,
            final int defaultValue) {
        if (configs.containsKey(key)) {
            final String valor = configs.get(key);
            return Integer.valueOf(valor);
        } else {
            return defaultValue;
        }
    }

    public String getRutaRaizArchivos() {
        return rutaRaizArchivos;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

}
