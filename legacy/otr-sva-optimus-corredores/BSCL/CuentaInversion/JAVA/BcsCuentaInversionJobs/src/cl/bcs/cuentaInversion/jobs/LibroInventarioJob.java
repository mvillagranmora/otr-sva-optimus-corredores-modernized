package cl.bcs.cuentaInversion.jobs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.scheduler.BcsJob;

public class LibroInventarioJob extends BcsJob {
    AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        running.set(true);

        CallableStatement SP = null;
        String FechaMovimiento = null;
        String IdEjecucion = null;
        String callSP = "CALL \"BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoLibroInventario\"(?,?,?);";

        try {
            // FechaMovimiento = "2016-01-01";
            // IdEjecucion = "272";
            FechaMovimiento = this.getParam("FechaMovimiento");
            IdEjecucion = this.getParam("IdEjecucion");
        } catch (Exception e) {
            System.out.println("LibroInventarioJob error lectura parametros: " + e.getMessage());
        }

        // try(Connection con = getDBConnection();){ --Prueba debug local
        try {
            SP = con.prepareCall(callSP);
            SP.setString(1, FechaMovimiento);
            SP.setLong(2, Long.parseLong(IdEjecucion));
            SP.registerOutParameter(3, Types.BIGINT);
            SP.execute();
            System.out.println("LibroInventarioJob Cuentas afectadas=" + SP.getInt(3));

            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LibroInventarioJob error de ejecución: " + e.getMessage());
            return false;
        } finally {
            running.set(false);
        }
        return true;
    }

    private static final String DB_DRIVER = "com.sap.db.jdbc.Driver";
    private static final String DB_CONNECTION = "jdbc:sap://161.238.34.246:30215/?autocommit=false&currentschema=BSCL";
    private static final String DB_USER = "APPJAVA";
    private static final String DB_PASSWORD = "Bolsa123";

    private static Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }

    public static void main(String[] args) {

        LibroInventarioJob p = new LibroInventarioJob();
        try {
            p.transaction(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable t) {
        // TODO Auto-generated method stub
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

}