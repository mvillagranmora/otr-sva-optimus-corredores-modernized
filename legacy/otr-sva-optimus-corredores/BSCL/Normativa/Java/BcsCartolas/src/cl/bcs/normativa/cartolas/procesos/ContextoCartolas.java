package cl.bcs.normativa.cartolas.procesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import cl.bcs.normativa.cartolas.dal.CartolaService;
import cl.bcs.normativa.cartolas.dal.NominaService;
import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.entidades.NominaException;
import cl.bcs.normativa.cartolas.entidades.TipoProceso;
import cl.bcs.normativa.dal.EmpresaDAO;
import cl.bcs.normativa.esquema.Empresa;
import cl.bcs.utils.PropertiesUtil;

public class ContextoCartolas {

    public static final String MODULO_CARTOLAS = "CAR";

    private List<Cartola> cartolas;
    private final Configs configs;
    private final Connection connection;
    private final Empresa empresa;
    private final long idCartola;
    private final long idNomina;
    private final long idProceso;
    private final long idUsuario;
    private final String modulo;
    /**
     * Para control de concurrencia.
     */
    private final Object mutexCartolas = new Object();
    private final Object mutexNomina = new Object();
    private Nomina nomina;
    private final String outputPath;
    private final Properties properties;
    private final String schema;
    private final String tipoProceso;

    public ContextoCartolas(
            final Connection connection,
            final Properties properties,
            final long idProceso) throws SQLException {
        this.connection = connection;
        this.properties = properties;
        this.idProceso = idProceso;

        this.schema = PropertiesUtil.getRequiredString(properties, "schema");
        this.idUsuario = PropertiesUtil.getRequiredLong(properties, "idUsuario");
        this.modulo = MODULO_CARTOLAS;

        final long idEmpresa = PropertiesUtil.getRequiredLong(properties, "idEmpresa");
        this.empresa = EmpresaDAO.buscarPorId(connection, schema, idEmpresa);
        this.configs = new Configs(connection, schema, idEmpresa);

        this.idCartola = PropertiesUtil.getOptional(properties, "idCartola", 0);
        this.idNomina = PropertiesUtil.getOptional(properties, "idNomina", 0);

        this.outputPath = PropertiesUtil.getOptional(properties, "outputPath", "");
        this.tipoProceso = PropertiesUtil.getOptional(properties, "tipoProceso",
                TipoProceso.NORMAL);
    }

    public List<Cartola> getCartolas()
            throws SQLException {
        synchronized (mutexCartolas) {
            if (cartolas == null) {
                final boolean reproceso = !tipoProceso.equals(TipoProceso.NORMAL);
                this.cartolas = CartolaService.lookupByIdNomina(this, idNomina, reproceso);
                if (this.cartolas.isEmpty()) {
                    throw new NominaException(
                            "No hay cartolas para proceso en la nomina: id=" + idNomina);
                }
            }
        }

        return cartolas;
    }

    public Configs getConfigs() {
        return configs;
    }

    public Connection getConnection() {
        return connection;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public long getIdCartola() {
        return idCartola;
    }

    public long getIdNomina() {
        return idNomina;
    }

    public long getIdProceso() {
        return idProceso;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public String getModulo() {
        return modulo;
    }

    public Nomina getNomina()
            throws SQLException {
        synchronized (mutexNomina) {
            if (nomina == null) {
                this.nomina = NominaService.lookupByIdNomina(connection, schema, idNomina);
            }

            return nomina;
        }
    }

    public String getOutputPath() {
        return outputPath;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getSchema() {
        return schema;
    }

    public String getTipoProceso() {
        return tipoProceso;
    }

}
