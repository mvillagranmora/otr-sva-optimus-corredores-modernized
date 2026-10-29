package cl.bcs.valo2.data.cargadores;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.sql.datasource.DataSourceProvider;
import cl.bcs.sql.datasource.DataSourceSettings;
import cl.bcs.utils.PropertiesUtil;
import cl.bcs.valo2.data.archivos.ArchivoBonos;
import cl.bcs.valo2.entidades.Bono;

public final class Cargador {

    public static void main(
            final String[] args)
                    throws IOException,
                    SQLException {
        final ArchivoBonos archivoBonos = new ArchivoBonos("bonos.csv");
        final Collection<Bono> bonos = archivoBonos.getBonos().values();
        final Properties props = PropertiesUtil.newProperties("valorizador.properties");
        final DataSourceSettings settings = new DataSourceSettings(props);
        final DataSource dataSource = DataSourceProvider.getInstance(settings);
        try (final Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            final CargadorInstrumentos cargadorInstrumentos = new CargadorInstrumentos(conn, 100);
            cargadorInstrumentos.cargar(bonos, 100000);
            final CargadorBonos cargadorBonos = new CargadorBonos(conn, 100);
            cargadorBonos.cargar(bonos, 100000);
        }
    }

    private Cargador() {
    }

}
