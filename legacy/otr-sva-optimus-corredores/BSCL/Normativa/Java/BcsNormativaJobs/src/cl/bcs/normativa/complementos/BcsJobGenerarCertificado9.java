package cl.bcs.normativa.complementos;

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

public class BcsJobGenerarCertificado9 extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "GenerarCertificado9";

    public boolean transaction(Connection dbConnection) throws InterruptedException, Throwable {
        System.out.println("*GenerarCertificado9* Inicio de proceso");
        String dropTempTable = "drop table #clientes";
        running.set(true);
        PreparedStatement pstmt = null;
        CallableStatement sp = null;
        ResultSet rs = null;
        final String callSp1899 = "CALL \"BSCL.Normativa.BD.Servicios::TRI.DJ1889.GenerarCertificado9\"(?, ?, ?, ?, ?, #clientes, ?, ?, ?, ?)";

        try {
            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String idModulo = "TRI";
            String simularOperacion = "";
            String usaReglasNegocio = "";
            String clientes = "";
            String esquema = "";
            String version = "";
            String todosLosClientes = "";
            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));
            idModulo = this.getParam("IdModulo");
            simularOperacion = this.getParam("SimularOperacion");
            usaReglasNegocio = this.getParam("UsaReglasNegocio");
            esquema = this.getParam("Esquema");
            version = this.getParam("Version");
            clientes = this.getParam("Clientes");
            todosLosClientes = this.getParam("TodosLosClientes");
            
            pstmt = dbConnection.prepareStatement("set schema BSCL");
            pstmt.execute();
            pstmt.close();

            try {
                pstmt = dbConnection.prepareStatement(dropTempTable);
                pstmt.execute();
            } catch (Exception ex) {
                System.out.println( "Tabla #clientes no existe");
            }
            
            String[] cliente = clientes.split(";");
            
            String createTempTable = "Create local temporary table #clientes (\"IdCliente\" BIGINT NOT NULL)";
            
            pstmt = dbConnection.prepareStatement(createTempTable);
            pstmt.execute();
            pstmt.close();
            if (todosLosClientes.equalsIgnoreCase("N")){
            	String insert = "INSERT INTO #clientes(\"IdCliente\") SELECT ? FROM DUMMY;";
                pstmt = dbConnection.prepareStatement(insert);
                for (int i = 0; i < cliente.length; i++) {
                    pstmt.setLong(1, Long.parseLong(cliente[i]));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                pstmt.close();
            }
            try (Statement st = dbConnection.createStatement();) {
                st.executeUpdate("SET TRANSACTION AUTOCOMMIT DDL OFF");
            }
           
            sp = dbConnection.prepareCall(callSp1899);

            sp.setLong(1, idEmpresa);
            sp.setLong(2, idUsuario);
            sp.setString(3, idModulo);
            sp.setString(4, version);
            sp.setString(5, todosLosClientes);
            
            sp.registerOutParameter(6, Types.INTEGER); // advertencias contador
            sp.registerOutParameter(7, Types.INTEGER); // errores contador

            boolean hasResults = sp.execute();
            
            // advertencias
            rs = sp.getResultSet(); // p_out_Resultado
            
            // errores
            hasResults = sp.getMoreResults();
            rs = sp.getResultSet();
            /*
            if (Integer.parseInt(sp.getObject(7).toString()) > 0) {
                System.out.println( "*GenerarCertificado9* Error controlado al intentar de generar el certificado 9");
                while (rs.next()) {
                    System.out.println( "*GenerarCertificado9 " + rs.getString("Codigo") + " " + rs.getString("Descripcion"));
                }
                dbConnection.rollback();
                throw new Throwable(new Exception( "*GenerarCertificado9* Error al momento de generar certificado 9:"));
            } else {
                dbConnection.commit();
            }
            */
            dbConnection.commit();
            System.out.println("*GenerarCertificado9* Fin de proceso");

        } catch (Exception e) {
            System.out.println("*GenerarCertificado9* Error al momento de generar certificado 9: " + e.getMessage());
            throw new Throwable(e);
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

    	BcsJobGenerarCertificado9 p = new BcsJobGenerarCertificado9();
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
