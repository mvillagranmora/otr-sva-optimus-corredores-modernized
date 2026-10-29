package cl.bcs.facturacion.jobs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sql2o.Sql2o;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.jobs.pojo.ResumenFacturacionCliente;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ResumenFacturasClienteJob extends BcsJob implements JavaCall{

    AtomicBoolean running = new AtomicBoolean(false);

    final String callGetConfiguracion = "CALL " + Utils.getSchema() + ".\"BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion\"(?,?,?);";
    final String insertTemporalTable = "INSERT INTO " + Utils.getSchema()
            + ".\"BSCL.Facturacion.BD::FAC.NotificacionResumenFacturacionTemporal\"(\"IdCliente\",\"IdPortafolio\",\"IdPersonaRemitente\",\"IdPersonaDestinatario\",\"CLIENTE\",\"RUT\",\"CUENTA\",\"AGENTE\",\"TELEFONO\",\"CORREO\",\"FECHA\",\"HORA\",\"CORREDORA\",\"TABLA\",\"ATENCIONCLIENTE\", \"IdEmpresa\", \"TABLACOMPRA\", \"TABLAVENTA\") VALUES (?,?,?,?,?,?,?,?,?,?,TO_VARCHAR(CURRENT_UTCTIMESTAMP,'DD-MM-YYYY') ,TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'HH:mm:ss'),?,?,?,?,?,?);";
    final String callResumenFacturacion = "CALL " + Utils.getSchema() + ".\"BSCL.Facturacion.BD.Servicios::FAC.NotificarResumenFacturasCliente\"(?, ?, ?, ?,?,?,?,?)";
    final String selectResumenFacturasCliente = loadResourceToString("cl/bcs/facturacion/jobs/sql/ResumenFacturacionCliente_Select.sql");
    
    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
        Sql2o sql2o = super.getSql2o();
        try {
            go(sql2o, false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void go(Sql2o sql2ox, boolean isDevelopment) throws InterruptedException, Throwable {
        running.set(true);
        
        try {
            long idEmpresa;
            if (isDevelopment) {
                idEmpresa = 1L;
            } else {
                idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            }

            Map<String, String> tabla = new HashMap<String, String>();
            try (org.sql2o.Connection sql2oConn = sql2ox.beginTransaction()) {
            	
                try (java.sql.Connection jdbcConn = sql2oConn.getJdbcConnection()) {

                    try (CallableStatement cstm = jdbcConn.prepareCall(callGetConfiguracion)) {
                        cstm.setLong(1, idEmpresa);
                        cstm.setString(2, "PLA.FechaSistema");
                        cstm.registerOutParameter(3, Types.VARCHAR);
                        cstm.execute();
                        String fechaSistema = cstm.getString(3);

                        // Obtener los comprobantes de facturacion agrupados.
                        List<ResumenFacturacionCliente> r = sql2oConn.createQuery(selectResumenFacturasCliente, false).addParameter("IdEmpresa", idEmpresa).addParameter("FechaSistema", fechaSistema).executeAndFetch(ResumenFacturacionCliente.class);
                        for (ResumenFacturacionCliente res : r) {
                            // generar una fila en HTML para cada cliente
                            // agrupado.
                            String key = res.getDocumentoIdentidad() + "_" + res.getAccionTipoOperacion();
                            String cantidad = MonedaFormato.formatear(jdbcConn, res.getCantidadTotal(), MonedaFormato.CANTIDAD, res.getIdMoneda()).valorConFormato;
                            String precio = MonedaFormato.formatear(jdbcConn, res.getPrecioPromedio(), MonedaFormato.PRECIO, res.getIdMoneda()).valorConFormato;
                            String monto = MonedaFormato.formatear(jdbcConn, res.getTotal() , MonedaFormato.MONTO, res.getIdMoneda()).valorConFormato;
                            
                            res.setRow("<tr class=\"detalle\"><td>"+res.getIdClasificacionTipoInstrumento()+"</td><td>"+res.getAccionTipoOperacion()+"</td><td>"+res.getNombreTipoInstrumento()+"</td><td>"+res.getNemotecnicoInstrumento()+"</td><td>"+ cantidad +"</td><td>"+precio+"</td><td>"+monto+"</td></tr>");
                            // agrupar por cliente todas las filas de
                            // agrupamiento
                            if (tabla.get(key) != null) {
                                tabla.put(key, tabla.get(key) + res.getRow());
                            } else {
                                tabla.put(key, res.getRow());
                            }
                        }

                        // agregar inicio y cierre de tablas html
                        for (String mapKey : tabla.keySet()) {
                            if (mapKey.contains("COMPRA")) {
                                tabla.put(mapKey, tabla.get(mapKey));//tr y td de compra
                            } else if (mapKey.contains("VENTA")) {
                                tabla.put(mapKey, tabla.get(mapKey));//tr y td de ventas
                            }
                        }

                        // Agrupar tablas de compra y venta.
                        HashMap<String, ResumenFacturacionCliente> clientesAgrupados = new HashMap<String, ResumenFacturacionCliente>();

                        for (ResumenFacturacionCliente iterable : r) {
                            if (clientesAgrupados.get(iterable.getDocumentoIdentidad()) == null) {
                                String tablaCompra = (tabla.get(iterable.getDocumentoIdentidad() + "_COMPRA") == null) ? "" : tabla.get(iterable.getDocumentoIdentidad() + "_COMPRA");
                                String tablaVenta = (tabla.get(iterable.getDocumentoIdentidad() + "_VENTA") == null) ? "" : tabla.get(iterable.getDocumentoIdentidad() + "_VENTA");
                                
                                iterable.setTabla(tablaCompra + tablaVenta);
                                iterable.setTablaCompra(tablaCompra);
                                iterable.setTablaVenta(tablaVenta);
                                clientesAgrupados.put(iterable.getDocumentoIdentidad(), iterable);
                            }
                        }

                        try (PreparedStatement pstm = jdbcConn.prepareStatement(insertTemporalTable)) {
                            // agregar registros a la tabla temporal de
                            // resumenes para generar los eventos de
                            // notificacion.
                            for (String key : clientesAgrupados.keySet()) {
                                ResumenFacturacionCliente res = clientesAgrupados.get(key);

                                pstm.setLong(1, res.getIdCliente());
                                pstm.setLong(2, res.getIdPortafolio());
                                pstm.setLong(3, res.getIdPersonaRemitente());
                                pstm.setLong(4, res.getIdPersonaDestinatario());

                                pstm.setString(5, res.getNombreLargoCliente());// CLIENTE
                                pstm.setString(6, res.getDocumentoIdentidad());// RUT
                                pstm.setString(7, res.getCodigoPortafolio());// CUENTA
                                pstm.setString(8, res.getNombreCompleto());// AGENTE
                                pstm.setString(9, res.getNumero());// TELEFONO
                                pstm.setString(10, res.getDireccion());// CORREO
                                pstm.setString(11, res.getNombre());// CORREDORA
                                pstm.setString(12, res.getTabla());// TABLA
                                pstm.setString(13, res.getTelefonoPrincipal());// ATENCIONCLIENTE
                                pstm.setLong(14, idEmpresa);// ATENCIONCLIENTE
                                pstm.setString(15, res.getTablaCompra());// TABLACOMRA
                                pstm.setString(16, res.getTablaVenta());// TABLAVENTA
                                
                                if(res.getTablaCompra().length()>=5000 || res.getTablaVenta().length()>=5000) {
                                	System.out.println("ALERTA "+res.getTabla().length()+": " + res.getTabla());
                                	System.out.println(res);
                                }else {
                                	pstm.addBatch();
                                }
                            }
                            pstm.executeBatch();

                            try (CallableStatement cstm2 = jdbcConn.prepareCall(callResumenFacturacion)) {
                                cstm2.setLong(1, idEmpresa);
                                cstm2.setLong(2, (long) 0);
                                cstm2.setString(3, "FAC");
                                cstm2.registerOutParameter(4, Types.BIGINT);
                                cstm2.execute();
                                System.out.println("{Eventos ingresados : " + cstm2.getInt(4) + "}");
                            }
                            
                        }
                    }
                    jdbcConn.commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ResumenFacturasClienteJob.main(): ERROR AL OBTENER DB CONNECTION");
            System.out.println(e.getMessage());
        }
        running.set(false);
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(":ResumenFacturasClienteJob:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }
  //solo para test
  	public static void main(String[] args) {
  		// Example
  		System.out.println("== START ==");
  		try (Connection con = PlataformaUtils.getDESAConnection()) {
  			con.setAutoCommit(false);
  			SQL.exec(con, "set schema BSCL");
  			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
  			// ---------------------------------
  			try {
  				  		        
  				ResumenFacturasClienteJob apf = new ResumenFacturasClienteJob();
  				 Sql2o sql2o = new Sql2o(PlataformaUtils.getDESADatasource());
  		        try {
  		        	
  		        	apf.go(sql2o,true);

  		        } catch (Exception e) {
  		        	e.printStackTrace();
  		        }
  				
  				con.rollback();
  			} catch (Exception er) {
  				er.printStackTrace();
  				con.rollback();
  			} catch (Throwable e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		System.out.println("== END ==");
  	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
