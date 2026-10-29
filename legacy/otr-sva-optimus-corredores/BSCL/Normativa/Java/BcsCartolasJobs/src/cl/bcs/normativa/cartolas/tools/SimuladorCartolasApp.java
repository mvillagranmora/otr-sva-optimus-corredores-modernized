package cl.bcs.normativa.cartolas.tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Properties;

import cl.bcs.normativa.cartolas.dal.NominaService;
import cl.bcs.normativa.cartolas.entidades.EstadoProceso;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.entidades.Opcion;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.utils.SqlConnectionBuilder;
import cl.bcs.sql.utils.SqlConnectionSettings;
import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.utils.PropertiesUtil;

public final class SimuladorCartolasApp {

    private static final String PERIODO = "2016-08";
    private static final Date FECHA_DESDE = SqlUtil.getDate(2016, Calendar.MARCH, 1);
    private static final Date FECHA_HASTA = SqlUtil.getDate(2016, Calendar.MARCH, 31);

    private static Nomina crearNomina(
            final ContextoCartolas contexto)
                    throws SQLException,
                    ParseException {
        final Nomina nomina = new Nomina();
        nomina.setCertificada(Opcion.NO);
        nomina.setEstadoProceso(EstadoProceso.INGRESADO);
        nomina.setPeriodo(PERIODO);
        nomina.setFechaDesde(FECHA_DESDE);
        nomina.setFechaHasta(FECHA_HASTA);
        nomina.setIdEmpresa(contexto.getEmpresa().getId());
        nomina.setIdUsuario(contexto.getIdUsuario());
        nomina.setTipoProceso(EstadoProceso.NORMAL);
        NominaService.save(contexto, nomina);
        return nomina;
    }

    public static void main(
            final String[] args)
                    throws SQLException,
                    IOException,
                    ParseException {
        final Properties properties = PropertiesUtil.newProperties("cartolas.properties");
        final Properties connProps = PropertiesUtil.newProperties("connection.properties");
        final SqlConnectionSettings connSettings = new SqlConnectionSettings(connProps, "source");

        try (final Connection conn = SqlConnectionBuilder.getConnection(connSettings)) {
            final ContextoCartolas contexto = new ContextoCartolas(conn, properties, 0);

            try {
                final Nomina nomina = crearNomina(contexto);
                // NominaService.poblar(contexto, nomina.getId());
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private SimuladorCartolasApp() {
    }

}
