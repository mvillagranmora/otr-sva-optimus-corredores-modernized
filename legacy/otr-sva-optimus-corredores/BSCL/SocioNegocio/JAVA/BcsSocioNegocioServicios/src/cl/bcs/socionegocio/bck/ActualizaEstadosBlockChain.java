package cl.bcs.socionegocio.bck;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.FolioPrestamoBlockChain;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizaEstadosBlockChain implements JavaCall 
{
	
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_CONSULTA_PRESTAMOS					= loadResourceToString("cl/bcs/socionegocio/bck/sql/ActualizaEstadosBlockChain_SelectPrestamosVig.sql");
	private final String SQL_UPDATE_ESTADO_PRESTAMO                     = loadResourceToString("cl/bcs/socionegocio/bck/sql/ActualizaEstadosBlockChain_UpdateEstadoBCPrestamo.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ActualizaEstadosBlockChain_Entrada
	{
		public Long 				p_IdEmpresa;
		public Long					p_IdCliente;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ActualizaEstadosBlockChain_Salida
	{
		public List<Prestamo>		results;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Prestamo
	{
		public long		    FolioPrestamo;
		public String		FolioSistemaExterno;
		public String		EstadoAnticipo;
		public String		CodigoCondicionLiquidacion;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	public static class Folio {

		public long	  FolioPrestamo;
		public String Estado;
		public String EstadoLiquidado;
        public List<FolioPrestamoBlockChain>	p_FoliosPrestamoBlockChain;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectTipoPersona
	{
        public String Tipo = "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	public static class SelectNombreEstado
	{
		public String IdEstado = "";
        public String Nombre = "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ActualizaEstadosBlockChain_Salida logica(Connection conn, ActualizaEstadosBlockChain_Entrada in) throws Exception
	{
		SQL sql = new SQL();
		
		ActualizaEstadosBlockChain_Salida salida = new ActualizaEstadosBlockChain_Salida();
		
		HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
		List<Prestamo> prestamos = new ArrayList<Prestamo>();
		parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
		List<Prestamo> SelectPrestamos = sql.query(conn, SQL_SELECT_CONSULTA_PRESTAMOS, parametroEntrada, Prestamo.class);
		
		for(Prestamo selectPrestamos : SelectPrestamos)   
		{
			
				if(selectPrestamos.FolioSistemaExterno!= null){
					ConsultaEstadoAnticipoBlockChain consultarEstadoAntBlockchain = new ConsultaEstadoAnticipoBlockChain();
					ConsultaEstadoAnticipoBlockChain.ConsultaEstadoAnticipoBlockChain_Entrada entradaConsultaEstadoAnticipoBlockChain = new ConsultaEstadoAnticipoBlockChain.ConsultaEstadoAnticipoBlockChain_Entrada();
					entradaConsultaEstadoAnticipoBlockChain.p_IdEmpresa = in.p_IdEmpresa;
					entradaConsultaEstadoAnticipoBlockChain.p_FolioExterno = selectPrestamos.FolioSistemaExterno;
					ConsultaEstadoAnticipoBlockChain.ConsultaEstadoAnticipoBlockChain_Salida salidaConsultaEstadoAnticipoBlockChain = consultarEstadoAntBlockchain.logica(conn, entradaConsultaEstadoAnticipoBlockChain);
				
					selectPrestamos.EstadoAnticipo = salidaConsultaEstadoAnticipoBlockChain.p_Out_Status;
				
						List<Folio> parametrosPrestamo 	= 	new ArrayList<Folio>();
						Folio vPrestamo 				= 	new Folio();
						vPrestamo.FolioPrestamo 		= 	selectPrestamos.FolioPrestamo;
						vPrestamo.Estado 		        = 	selectPrestamos.EstadoAnticipo; 
						vPrestamo.EstadoLiquidado		= 	selectPrestamos.EstadoAnticipo;

						switch (vPrestamo.Estado) {
						  case "MATURED":
							vPrestamo.Estado = " ";
							vPrestamo.EstadoLiquidado = "LIQUIDACION AL VENCIMIENTO";
						    break;
						  case "PENDING_TERMINATION":
							vPrestamo.Estado = "PENDIENTE";
							vPrestamo.EstadoLiquidado = " ";
						    break;
						  default:
							vPrestamo.Estado = " "; 
							vPrestamo.EstadoLiquidado = " ";	  
						}

						System.out.println("F ==>"+vPrestamo.FolioPrestamo+"-->"+selectPrestamos.FolioSistemaExterno+"-->est "+vPrestamo.Estado);
						parametrosPrestamo.add(vPrestamo);
						sql.batch(conn, SQL_UPDATE_ESTADO_PRESTAMO, parametrosPrestamo, Folio.class);
				}

			prestamos.add(selectPrestamos);
		}
		
		salida.results = prestamos;
		
		return salida;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ActualizaEstadosBlockChain_Entrada in = gson.fromJson(jin.toString(), ActualizaEstadosBlockChain_Entrada.class);
		ActualizaEstadosBlockChain_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String jsonCL = "{\"p_IdEmpresa\": 1,\"p_IdCliente\": 1423496}";
			
			ActualizaEstadosBlockChain_Entrada Objin = gson.fromJson(jsonCL, ActualizaEstadosBlockChain_Entrada.class);

			ActualizaEstadosBlockChain demo = new ActualizaEstadosBlockChain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ActualizaEstadosBlockChain_Salida ObjOut = demo.logica(con, Objin); 
				con.commit();
				//con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}