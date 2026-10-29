package cl.bcs.normativa.jobs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.scheduler.BcsJob;

public class BcsJobGrabarCertificadoCliente extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void onError(
            final Throwable t) {
        System.out.println(":BcsJobGrabarCertificadoCliente:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    @Override
    public boolean transaction(
            final Connection conn)
                    throws InterruptedException,
                    Throwable {
        running.set(true);
        final DataSource datasource = super.getDataSource();

        final PreparedStatement pstmt = null;
        CallableStatement sp = null;
        CallableStatement spexe = null;
        CallableStatement spgrab = null;

        final String callSp = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios.CER::ObtenerListaClientesCertificado\"(?, ?, ?)";
        final String callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodia\"(?, ?, ?, ?, ?)";
        final String callSpgrab = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios.CER::CrearCertificadoCliente\"(?, ?, ?, ?, ?, ?)";

        try {

            final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            final String periodo = this.getParam("Periodo");
            final String strFechaConsulta = this.getParam("UltimoDiaMes");

            final String idTipoDocumento = this.getParam("idTipoDocumento");
            final long IdSolicitudCircular = Long.parseLong(this.getParam("IdSolicitudCircular"));
            Integer numColumnas = 0;
            final long total = System.currentTimeMillis();
            try {
                sp = conn.prepareCall(callSp);
                sp.setLong(1, idEmpresa);
                sp.setString(2, idTipoDocumento);// idusuario
                sp.setString(3, periodo);
                final long milis = System.currentTimeMillis();
                sp.execute();
                //conn.commit();
                // Lectura de Clientes
                final ResultSet rs = sp.getResultSet();
                long contadorClientes = 0;

                while (rs.next()) {
                    contadorClientes++;

                    /// EJECUTAR CERTIFICADO POR CLIENTE

                    spexe = conn.prepareCall(callSpexe);
                    /*
                     * comun spexe.setLong(1, idEmpresa); spexe.setLong(2,
                     * IdSolicitudCircular); spexe.setString(3,
                     * idTipoDocumento); spexe.setString(4, periodo);
                     * spexe.setLong(5, rs.getLong("IdCliente"));
                     * spexe.setLong(6, rs.getLong("IdPortafolio"));
                     */
                    spexe.setLong(1, idEmpresa);
                    spexe.setLong(2, rs.getLong("IdCliente"));// 395174);//
                    spexe.setLong(3, rs.getLong("IdPortafolio"));// 343710);//
                    spexe.setString(4, strFechaConsulta);// "2016-11-30");
                    spexe.setString(5, "CARTA");

                    // Ejecutar certificado
                    spexe.execute();
                   // conn.commit();

                    /// RECORRER DATOS :DETALLE DEL CERTIFICADO

                    final ResultSet rsExe = spexe.getResultSet();

                    // armar JsonData
                    final JsonArray jsonObj = new JsonArray();

                    numColumnas = rsExe.getMetaData().getColumnCount();
                    /// RECORRER COLUMNAS
                    while (rsExe.next()) { // Recorrer salida del certificado
                        final JsonObject registro = new JsonObject();
                        for (int i = 1; i <= numColumnas; i++) {
                        	String colName = rsExe.getMetaData().getColumnName(i);
                        	int colType = rsExe.getMetaData().getColumnType(i);
							switch (colType) {
							case Types.INTEGER:
								registro.addProperty(colName, rsExe.getInt(i));
								break;
							case Types.BIGINT:
								registro.addProperty(colName, rsExe.getLong(i));
								break;
							case Types.FLOAT:
								registro.addProperty(colName, rsExe.getFloat(i));
								break;
							case Types.DOUBLE:
								registro.addProperty(colName, rsExe.getDouble(i));
								break;
							case Types.DECIMAL:
								registro.addProperty(colName, rsExe.getDouble(i));
								break;
							case Types.TIMESTAMP:
								registro.addProperty(colName, rsExe.getTimestamp(i).toString());
								break;
							case Types.DATE:
								registro.addProperty(colName, rsExe.getDate(i).toString());
								break;
							case Types.VARCHAR:
								registro.addProperty(colName, rsExe.getString(i));
								break;
							case Types.NVARCHAR:
								registro.addProperty(colName, rsExe.getString(i));
								break;
							case Types.BLOB:
								byte[] encodedBytes = Base64.getEncoder().encode(rsExe.getBytes(i));
                        		registro.addProperty(colName, new String(encodedBytes));
								break;
							default:
								registro.addProperty(colName, rsExe.getString(i));
								break;
							}
                        }

                        jsonObj.add(registro);
                    } /// while

                    /// GRABAR CERTIFICADO POR CLIENTE
                    spgrab = conn.prepareCall(callSpgrab);

                    spgrab.setLong(1, idEmpresa);
                    spgrab.setString(2, idTipoDocumento);
                    spgrab.setString(3, periodo);
                    spgrab.setLong(4, rs.getLong("IdCliente"));
                    spgrab.setLong(5, rs.getLong("IdPortafolio"));
                    spgrab.setString(6, jsonObj.toString());
                    // grabar certificado
                    spgrab.execute();
                    //conn.commit();

                }
                ;

            } catch (final Exception ex) {
                System.out.println("BcsJobGrabarCertificadoCliente: EXCEPTION AL OBTENER CLIENTES");
                ex.printStackTrace();
            }

        } catch (final Exception e) {
            System.out.println("BcsJobGrabarCertificadoCliente(): ERROR AL OBTENER DB CONNECTION");
            System.out.println(e.getMessage());
        } finally {
        	/*
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
            */
        }

        running.set(false);

        return true;
    }
}
