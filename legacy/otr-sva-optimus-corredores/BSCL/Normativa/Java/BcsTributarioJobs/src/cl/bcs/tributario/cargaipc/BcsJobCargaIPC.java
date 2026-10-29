package cl.bcs.tributario.cargaipc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;

public class BcsJobCargaIPC extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "CargaIPC";

    public boolean transaction(Connection dbConnection) throws InterruptedException, Throwable {
        System.out.println("*CargaIPC* Inicio de proceso");
        running.set(true);
        PreparedStatement pstmt = null;
        CallableStatement sp = null;
        ResultSet rs = null;
        final String callSp = "CALL \"BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc\"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String IdModulo = "";
            String SimularOperacion = "";
            String UsaReglasNegocio = "";
            String modulo = "TRI";
            String trans = "";

            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));
            IdModulo = this.getParam("IdModulo");
            SimularOperacion = this.getParam("SimularOperacion");
            UsaReglasNegocio = this.getParam("UsaReglasNegocio");
            pstmt = dbConnection.prepareStatement("set schema BSCL");
            pstmt.execute();
            pstmt.close();

            try (Statement st = dbConnection.createStatement();) {
                st.executeUpdate("SET TRANSACTION AUTOCOMMIT DDL OFF");
            }
            
            sp = dbConnection.prepareCall(callSp);

            sp.setLong(1, idEmpresa);
            sp.setLong(2, idUsuario);
            sp.setString(3, IdModulo);
            sp.setString(4, SimularOperacion);
            sp.setString(5, UsaReglasNegocio);
            
            sp.registerOutParameter(6, Types.INTEGER); // advertencias contador
            sp.registerOutParameter(7, Types.INTEGER); // errores contador

            boolean hasResults = sp.execute();

            rs = sp.getResultSet(); // p_out_Resultado
            
            // advertencias
            hasResults = sp.getMoreResults();
            rs = sp.getResultSet();

            // errores
            hasResults = sp.getMoreResults();
            rs = sp.getResultSet();

            if (Integer.parseInt(sp.getObject(4).toString()) > 0) {
                System.out.println( "*CargaIPC* Error controlado al intentar cargar IPC");
                while (rs.next()) {
                    System.out.println( "*CargaIPC " + rs.getString("Codigo") + " " + rs.getString("Descripcion"));
                }
                dbConnection.rollback();
                throw new Throwable(new Exception( "*CargaIPC* Error al procesar carga de ipc"));
            } else {
                dbConnection.commit();
            }
            
            dbConnection.commit();
            System.out.println("*CargaIPC* Fin de proceso");

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

    	BcsJobCargaIPC p = new BcsJobCargaIPC();
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
