package cl.bcs.fondomutuo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;

public class BcsJobBloqueoCierreSeries extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "BLOQUEOCIERRESERIESFFMM";

    public boolean transaction(Connection dbConnection) throws InterruptedException, Throwable {
        System.out.println("*BLOQUEOCIERRESERIESFFMM* Inicio de demonio");
        running.set(true);
        PreparedStatement pstmt = null;
        CallableStatement sp = null;
        ResultSet rs = null;
        final String callSpFFMM = "CALL \"BSCL.FondoMutuo.BD.Servicios::CierreBloqueoAutomaticoFondos\"(?, ?, ?, ?, ?, ?, ?)";

        try {
            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String modulo = "FMM";
            String trans = "";

            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));

            pstmt = dbConnection.prepareStatement("set schema BSCL");
            pstmt.execute();
            pstmt.close();

            sp = dbConnection.prepareCall(callSpFFMM);

            sp.setLong(1, idEmpresa);
            sp.setLong(2, idUsuario);

            sp.registerOutParameter(3, Types.INTEGER); // advertencias contador
            sp.registerOutParameter(4, Types.INTEGER); // errores contador

            boolean hasResults = sp.execute();

            rs = sp.getResultSet(); // p_out_Resultado

            // advertencias
            hasResults = sp.getMoreResults();
            rs = sp.getResultSet();

            // errores
            hasResults = sp.getMoreResults();
            rs = sp.getResultSet();

            dbConnection.commit();
            System.out.println("*BLOQUEOCIERRESERIESFFMM* Fin de demonio");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new Throwable(e);
            }
        }

        running.set(false);
        return true;
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(":ProcesadorTransacciones:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    public static void main(String[] args) {

        BcsJobBloqueoCierreSeries p = new BcsJobBloqueoCierreSeries();
        try {
            p.transaction(null);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
