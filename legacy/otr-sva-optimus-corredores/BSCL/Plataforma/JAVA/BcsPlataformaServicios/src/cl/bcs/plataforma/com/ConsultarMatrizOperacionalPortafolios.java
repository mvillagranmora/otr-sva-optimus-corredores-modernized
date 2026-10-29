package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.JsonObject;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.ErrorDetalle;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarMatrizOperacionalPortafolios implements JavaCall {

	private final String SQL_CREAR_ENTRADA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalPortafolios_CrearEntrada.sql");
	private final String SQL_INSERT_ENTRADA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalPortafolios_InsertEntrada.sql");
	private final String SQL_CALL_SP = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalPortafolios_Call_Sp.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public ConsultarMatrizOperacionalPortafoliosSalida logica(Connection conn,ConsultarMatrizOperacionalPortafoliosEntrada entrada) {
		SQL sql = new SQL();
		ConsultarMatrizOperacionalPortafoliosSalida salida = new ConsultarMatrizOperacionalPortafoliosSalida();
		
		String tabla = "CMOP_ENTRADA" + Math.round(Math.random()*1000);
		String sqlCreateTable = SQL_CREAR_ENTRADA.replace("::NAME::", tabla);
		String sqlInsertTable = SQL_INSERT_ENTRADA.replace("::NAME::", tabla);
		String sqlCallSp = SQL_CALL_SP.replace("::NAME::", tabla);
		try {
			SQL.exec(conn, sqlCreateTable);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			sql.batch(conn, sqlInsertTable, entrada.p_Portafolios, PortafolioEntrada.class);
			
			HashMap<String,Object> paramsIN=new HashMap<String,Object>();
			paramsIN.put("p_IdEmpresa", entrada.p_IdEmpresa);	
			paramsIN.put("p_IdUsuario", entrada.p_IdUsuario);
			paramsIN.put("p_IdModulo", entrada.p_IdModulo);
			
			HashMap<String,Class> paramsOUT=new HashMap<String,Class>();
			paramsOUT.put("p_out_MatrizOperacional", LineaMatriz.class);
			paramsOUT.put("p_out_AdvertenciasContador", Long.class);
			paramsOUT.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
			paramsOUT.put("p_out_ErroresContador", Long.class);
			paramsOUT.put("p_out_ErroresDetalle", ErrorDetalle.class);
			
			HashMap<String,Object> respuesta = sql.call(conn, sqlCallSp, paramsIN, paramsOUT);
			
			salida.p_out_AdvertenciasDetalle = (List<AdvertenciaDetalle>) respuesta.get(("p_out_AdvertenciasDetalle"));
			salida.p_out_ErroresDetalle = (List<ErrorDetalle>) respuesta.get(("p_out_ErroresDetalle"));
			
			salida.p_out_AdvertenciasContador = (Long) respuesta.get(("p_out_AdvertenciasContador"));
			salida.P_OUT_ERRORESCONTADOR = (Long) respuesta.get(("p_out_ErroresContador"));
			salida.p_out_MatrizOperacional = (List<LineaMatriz>) respuesta.get(("p_out_MatrizOperacional"));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				SQL.exec(conn, "DROP TABLE #" + tabla);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return salida;
	}
	
	public static void main(String[] args) throws SQLException {
		BasicDataSource ds = PlataformaUtils.getTestDataSource();
		try (Connection conn = ds.getConnection()){
			
			ConsultarMatrizOperacionalPortafolios cm = new ConsultarMatrizOperacionalPortafolios();
			ConsultarMatrizOperacionalPortafoliosEntrada entrada = new ConsultarMatrizOperacionalPortafoliosEntrada();
			
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 4L;
			entrada.p_IdModulo = "ORD";
			
			entrada.p_Portafolios.add(new PortafolioEntrada(27500L,"INAC","COMACN","AGE","BCS"));
			
			
			
			
			ConsultarMatrizOperacionalPortafoliosSalida salida = cm.logica(conn, entrada);
			System.out.println("listo");

		}
	}
	
	public static class ConsultarMatrizOperacionalPortafoliosSalida {
		public List<LineaMatriz> p_out_MatrizOperacional;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public List<ErrorDetalle> p_out_ErroresDetalle;
		public Long p_out_AdvertenciasContador;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ConsultarMatrizOperacionalPortafoliosSalida() {
			p_out_MatrizOperacional = new ArrayList();
			p_out_AdvertenciasDetalle = new ArrayList();
			p_out_ErroresDetalle = new ArrayList();
		}
	}
	
	public static class LineaMatriz {
		public Long IdPortafolio;
		public String IdBolsa;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdCanalIngreso;
		public String IdCobro;
		public BigDecimal ValorCobro;
		public String FormaCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;
		public Long Prioridad;
		public Long DiasPlazo;
		public String NombreCobro;
		public Long IdCobroClasificacion;
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor;
		public Long IdEmpresa;
	}
	
	public static class ConsultarMatrizOperacionalPortafoliosEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<PortafolioEntrada> p_Portafolios;
		
		public ConsultarMatrizOperacionalPortafoliosEntrada() {
			p_Portafolios = new ArrayList<PortafolioEntrada>();
		}
	}
	
	public static class PortafolioEntrada {
		public Long IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdCanalIngreso;
		public String IdBolsa;
		
		public PortafolioEntrada(Long IdPortafolio,String IdTipoProducto,String IdTipoOperacion,String IdCanalIngreso,String IdBolsa) {
			this.IdPortafolio = IdPortafolio;
			this.IdTipoProducto = IdTipoProducto;
			this.IdTipoOperacion = IdTipoOperacion;
			this.IdCanalIngreso = IdCanalIngreso;
			this.IdBolsa = IdBolsa;
		}
		
	}

}
