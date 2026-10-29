package cl.bcs.socionegocio.cli;


import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;


import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarReporteConsultaAvanzadaCliente implements JavaCall {
	private final Log log = new Log("CLI.GenerarReporteConsultaAvanzadaCliente");
	private final String SQL_CONSULTA_AVANZADA_CLIENTE_CALL = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaCliente_Call.sql");
	
	
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InConsultaSocioNegocio {
		
		public Long     p_IdEmpresa;
		public Long     p_IdUsuario;
		public String   p_IdModulo;
		public Long     p_IdEmpleadoConsulta;
		public String	p_Nombre;
		public String	p_DocumentoIdentidad;
		public String   p_IdTipoPersona;
		public String   p_IdTipoCliente;
		public Long		p_IdSegmentoCliente;
		public String	p_IdEstadoCliente;
		public String	p_IdTipoBloqueoCliente;
		public String	p_IdTipoBloqueoPortafolio;
		public Long		p_IdEmpleadoFiltro;
		public String	p_IdCategoriaInversionista;
		public String	p_IdPropositoPortafolio;
		public String	p_IdEstadoPortafolio;
		public String	p_IdPerfilComercial;
		public String	p_IdPerfilProductos;
		public String	p_IdPerfilCobros;
		public String	p_FechaNacimientoDesde;
		public String	p_FechaNacimientoHasta;
		public String	p_FechaCreacionDesde;
		public String	p_FechaCreacionHasta;
		public String	p_FechaDefuncionDesde;
		public String	p_FechaDefuncionHasta;
		public String	p_AliasPortafolio;
		public Long		p_IdPersonaBancoCustodio;
		public Long		p_IdPersonaBrokerExtranjero;
		public String	p_BuscaPorFlujoCaja;
		public String	p_UsoFlujoCaja;
		public Long		p_SaldoFlujoCaja;
		public String	p_IdMonedaFlujoCaja	;
		public String	p_CriterioFlujoCaja	;
		public String	p_BuscaPorFlujoCustodia;
		public String	p_TipoOperacionCustodia;
		public Long		p_SaldoFlujoCustodia;
		public String	p_CriterioFlujoCustodia	;
		public Long		p_IdInstrumento;
		
		public String p_Ruta;
		
		
	}
	
	public static class OutConsultaSocioNegocio {
		
		public String p_out_RutaSalida;
		public String p_out_NombreArchivo;
		public Long p_out_AdvertenciasContador;
		public Long p_out_ErroresContador;
		public List<InformacionAdvertenciasDetalle> p_out_AdvertenciasDetalle;
		public List<InformacionErroresDetalle> p_out_ErroresDetalle;
		
		
	}
	
	public static class Cliente{
		
		 public String DocumentoIdentidad;
		 public String NombreCompleto;
		 public Date   FechaNacimiento;
		 public Date   FechaCreacion;
		 public Date   FechaDefuncion;
		 public String IdTipoCliente;
		 public Long   IdCliente;
		 public Long   IdPortafolio;
		 public String NombreTipoCliente;
		 public String IdTipoPersona;
		 public String NombreTipoPersona;
		 public String IdSegmentoCliente;
		 public String NombreSegmentoCliente;
		 public String DescripcionSegmentoCliente;
		 public Long   IdEmpleadoEjecutivo;
		 public String CodigoEmpleadoEjecutivo;
		 public String NombreEmpleadoEjecutivo;
		 public String IdCategoriaInversionista;
		 public String NombreCategoriaInversionista;
		 public String IdEstadoCliente;
		 public String NombreEstadoCliente;
		 public String IdEstadoPortafolio;
		 public String NombreEstadoPortafolio;
		 public String CodigoPortafolio;
		 public String DescripcionPortafolio;
		 public String PropositoPortafolio;
		 public String IdTipoBloqueoCliente;
		 public String NombreTipoBloqueoCliente;
		 public String IdTipoBloqueoPortafolio;
		 public String NombreTipoBloqueoPortafolio;
		 public String IdPerfilComercial;
		 public String IdPerfilProductos;
		 public String IdPerfilCobros;
		 public String IdPerfilLimites;
		 public BigDecimal SaldoFlujoCaja;
		 public String IdMonedaFlujoCaja;
		 public BigDecimal SaldoFlujoCustodia;
		 public Long IdInstrumento;
		 public String IdMonedaInstrumento;
		 public String Nemotecnico;
	}
	
	public static class InformacionErroresDetalle {
		
		public String Codigo;
		public String Descripcion;
		
	}
	
	public static class InformacionAdvertenciasDetalle{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	
	
	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		InConsultaSocioNegocio in =gson.fromJson(jin.toString() ,InConsultaSocioNegocio.class);
		OutConsultaSocioNegocio out = logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutConsultaSocioNegocio logica(Connection con, InConsultaSocioNegocio entrada) throws Exception {
		
		OutConsultaSocioNegocio salida = new OutConsultaSocioNegocio();
		
		
		SQL sql = new SQL();
		
		//Entrada HashMap
		HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
		paramsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
		paramsIn.put("p_IdUsuario", entrada.p_IdUsuario);
		paramsIn.put("p_IdModulo", entrada.p_IdModulo);
		paramsIn.put("p_IdEmpleadoConsulta", entrada.p_IdEmpleadoConsulta);
		
		paramsIn.put("p_Nombre", entrada.p_Nombre);
		paramsIn.put("p_DocumentoIdentidad", entrada.p_DocumentoIdentidad);
		paramsIn.put("p_IdTipoPersona", entrada.p_IdTipoPersona);
		paramsIn.put("p_IdTipoCliente", entrada.p_IdTipoCliente);
		paramsIn.put("p_IdSegmentoCliente", entrada.p_IdSegmentoCliente);
		paramsIn.put("p_IdEstadoCliente", entrada.p_IdEstadoCliente);
		paramsIn.put("p_IdTipoBloqueoCliente", entrada.p_IdTipoBloqueoCliente);
		paramsIn.put("p_IdTipoBloqueoPortafolio", entrada.p_IdTipoBloqueoPortafolio);
		paramsIn.put("p_IdEmpleadoFiltro", entrada.p_IdEmpleadoFiltro);
		paramsIn.put("p_IdCategoriaInversionista", entrada.p_IdCategoriaInversionista);
		paramsIn.put("p_IdPropositoPortafolio", entrada.p_IdPropositoPortafolio);
		paramsIn.put("p_IdEstadoPortafolio", entrada.p_IdEstadoPortafolio);
		paramsIn.put("p_IdPerfilComercial", entrada.p_IdPerfilComercial);
		paramsIn.put("p_IdPerfilProductos", entrada.p_IdPerfilProductos);
		paramsIn.put("p_IdPerfilCobros", entrada.p_IdPerfilCobros);
		paramsIn.put("p_IdPerfilCobros", entrada.p_IdPerfilCobros);
		paramsIn.put("p_FechaNacimientoDesde", entrada.p_FechaNacimientoDesde != null ? entrada.p_FechaNacimientoDesde : "0001-01-01");
		paramsIn.put("p_FechaNacimientoHasta", entrada.p_FechaNacimientoHasta != null ? entrada.p_FechaNacimientoHasta : "0001-01-01");
		paramsIn.put("p_FechaCreacionDesde", entrada.p_FechaCreacionDesde != null ? entrada.p_FechaCreacionDesde : "0001-01-01 00:00:00");
		paramsIn.put("p_FechaCreacionHasta", entrada.p_FechaCreacionHasta != null ? entrada.p_FechaCreacionHasta : "0001-01-01 00:00:00");
		paramsIn.put("p_FechaDefuncionDesde", entrada.p_FechaDefuncionDesde != null ? entrada.p_FechaDefuncionDesde : "0001-01-01 00:00:00");
		paramsIn.put("p_FechaDefuncionHasta", entrada.p_FechaDefuncionHasta != null ? entrada.p_FechaDefuncionHasta : "0001-01-01 00:00:00");
		paramsIn.put("p_AliasPortafolio", entrada.p_AliasPortafolio);
		
		paramsIn.put("p_IdPersonaBancoCustodio", entrada.p_IdPersonaBancoCustodio);
		paramsIn.put("p_IdPersonaBrokerExtranjero", entrada.p_IdPersonaBrokerExtranjero);
		
		paramsIn.put("p_BuscaPorFlujoCaja", entrada.p_BuscaPorFlujoCaja != null ? entrada.p_BuscaPorFlujoCaja : "N");
		paramsIn.put("p_UsoFlujoCaja", entrada.p_UsoFlujoCaja);
		paramsIn.put("p_SaldoFlujoCaja", entrada.p_SaldoFlujoCaja);
		paramsIn.put("p_IdMonedaFlujoCaja", entrada.p_IdMonedaFlujoCaja);
		paramsIn.put("p_CriterioFlujoCaja", entrada.p_CriterioFlujoCaja);
		
		paramsIn.put("p_BuscaPorFlujoCustodia", entrada.p_BuscaPorFlujoCustodia != null ? entrada.p_BuscaPorFlujoCustodia : "N");
		paramsIn.put("p_TipoOperacionCustodia", entrada.p_TipoOperacionCustodia);
		paramsIn.put("p_SaldoFlujoCustodia", entrada.p_SaldoFlujoCustodia);
		paramsIn.put("p_CriterioFlujoCustodia", entrada.p_CriterioFlujoCustodia);
		paramsIn.put("p_IdInstrumento", entrada.p_IdInstrumento);
		
		//Salida HashMap
		HashMap<String,Class>  paramsOut = new HashMap<String,Class>();		
		paramsOut.put("p_out_AdvertenciasContador", Long.class);
		paramsOut.put("p_out_ErroresContador", Long.class);
		
		paramsOut.put("p_out_Clientes", Cliente.class);
		paramsOut.put("p_out_ErroresDetalle", InformacionErroresDetalle.class);
		paramsOut.put("p_out_AdvertenciasDetalle", InformacionAdvertenciasDetalle.class);
		HashMap<String,Object> resultSP= sql.call(con, SQL_CONSULTA_AVANZADA_CLIENTE_CALL, paramsIn, paramsOut);
		
		List<Cliente> clientes = new ArrayList<Cliente>();
		clientes.addAll((List<Cliente>) resultSP.get("p_out_Clientes"));
		
		salida.p_out_ErroresContador = (Long)resultSP.get("p_out_ErroresContador");
		salida.p_out_AdvertenciasContador = (Long)resultSP.get("p_out_AdvertenciasContador");
		salida.p_out_ErroresDetalle = (List<InformacionErroresDetalle>) resultSP.get("p_out_ErroresDetalle");
		salida.p_out_AdvertenciasDetalle = (List<InformacionAdvertenciasDetalle>) resultSP.get("p_out_AdvertenciasDetalle");
		
		salida.p_out_NombreArchivo= "SOC/ConsultaAvanzadaCliente" +Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyyMMddHHmmss") + ".csv";
		salida.p_out_RutaSalida =entrada.p_Ruta+salida.p_out_NombreArchivo;
		
		
		//se crea carpeta 
		File folder = new File(entrada.p_Ruta+"/SOC");
		if(!folder.exists()){
			folder.mkdirs();
		}
		
		FileOutputStream os = new FileOutputStream(salida.p_out_RutaSalida);
		os.write(0xef);
		os.write(0xbb);
		os.write(0xbf);
		CSVWriter EscribeCSV=new CSVWriter(new OutputStreamWriter(os,"UTF-8"),';');
		
		
		
		try{
			
			
			String [] FilasConsultaAvanzadaClientes= new String[]{
					"Documento Identidad",
					"Nombre Completo",
					"Fecha Nacimiento",
					"Fecha Creación",
					"Fecha Defunción",
					"Tipo Persona",
					"Segmento",
					"Codigo Ejecutivo",
					"Nombre Ejecutivo",
					"Tipo Cliente",
					"Categoría Inversionista",
					"Estado Cliente",
					"Tipo de Bloqueo Cliente",
					"Tipo de Bloqueo Portafolio",
					"Código Portafolio",
					"Propósito Portafolio",
					"Descripción Portafolio",
					"Estado Portafolio",
					"Modelo Comercial",
					"Perfil de Productos",
					"Perfil de Cobros",
					"Perfil de Límites",
					"Codigó Ejecutivo Anterior",
					"Nombre Ejecutivo Anterior",
					"Moneda Saldo FLujo de Caja",
					"Saldo FLujo de Caja",
					"Nemotécnico Flujo de Custodia",
					"Saldo Flujo de Custodia"
					
					};
			EscribeCSV.writeNext(FilasConsultaAvanzadaClientes);
			log.write("Numero clientes ",clientes.size());

			
			for(Cliente cliente : clientes){
				
				
				FilasConsultaAvanzadaClientes= new String[]{
						
						cliente.DocumentoIdentidad,
						cliente.NombreCompleto,
						cliente.FechaNacimiento != null ? Fecha.toStringDate(cliente.FechaNacimiento) : "",
						cliente.FechaCreacion != null ? Fecha.toStringDate(cliente.FechaCreacion)    :"",
						cliente.FechaDefuncion != null ? Fecha.toStringDate(cliente.FechaDefuncion)  :"",
						cliente.NombreTipoPersona,
						cliente.DescripcionSegmentoCliente,
						cliente.CodigoEmpleadoEjecutivo != null ? cliente.CodigoEmpleadoEjecutivo: ""  ,
						cliente.NombreEmpleadoEjecutivo != null ? cliente.NombreEmpleadoEjecutivo: "" ,
						cliente.NombreTipoCliente != null ? cliente.NombreTipoCliente: "",
						cliente.NombreCategoriaInversionista != null ? cliente.NombreCategoriaInversionista: "",
						cliente.NombreEstadoCliente != null ? cliente.NombreEstadoCliente: "",
						cliente.NombreTipoBloqueoCliente != null ? cliente.NombreTipoBloqueoCliente: "",
						cliente.NombreTipoBloqueoPortafolio != null ? cliente.NombreTipoBloqueoPortafolio: "",
						cliente.CodigoPortafolio != null ? cliente.CodigoPortafolio: "",
						cliente.PropositoPortafolio != null ? cliente.PropositoPortafolio: "",
						cliente.DescripcionPortafolio != null ? cliente.DescripcionPortafolio: "",
						cliente.IdEstadoPortafolio != null ? cliente.IdEstadoPortafolio: "",
						cliente.IdPerfilComercial != null ? cliente.IdPerfilComercial: "",
						cliente.IdPerfilCobros  != null ? cliente.IdPerfilCobros: "",
						cliente.IdPerfilLimites != null ? cliente.IdPerfilLimites: "",
						cliente.IdPerfilProductos  != null ? cliente.IdPerfilProductos: "",
						 "",//cliente.CodigoEmpleadoEjecutivoAnterior
						 "",//cliente.NombreEmpleadoEjecutivoAnterior
						cliente.IdMonedaFlujoCaja != null ? cliente.IdMonedaFlujoCaja: "",
						cliente.SaldoFlujoCaja != null ? cliente.SaldoFlujoCaja.toPlainString() : "",
						cliente.Nemotecnico != null ? cliente.Nemotecnico : "",
						cliente.SaldoFlujoCustodia != null ? cliente.SaldoFlujoCustodia.toPlainString() : ""       

						
				};
				
				EscribeCSV.writeNext(FilasConsultaAvanzadaClientes);
				
				
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try{
				EscribeCSV.close();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			
		}
		
		log.writeAsJson("SALIDA:",salida);
		return salida;
	}
	
	
	

	
	/**********************
	 * Main 
	 **********************/
	public static void main (String [] args) {
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        //prueba de la clase
		try {
			Connection con = ds.getConnection();
			try{
				InConsultaSocioNegocio entrada = new InConsultaSocioNegocio();
				entrada.p_Ruta = "C:\\";
				entrada.p_IdEmpresa = new Long(1);
				entrada.p_IdUsuario = new Long(21);
				entrada.p_IdModulo = "SOC";
				entrada.p_IdEmpleadoConsulta =  new Long(7889);
				
				/*
				entrada.p_Nombre = "SOC";
				entrada.p_DocumentoIdentidad = "SOC";
				entrada.p_IdTipoPersona = "SOC";
				entrada.p_IdTipoCliente = "SOC";
				entrada.p_IdSegmentoCliente = "SOC";
				entrada.p_IdEstadoCliente = "SOC";
				entrada.p_IdTipoBloqueoCliente = "SOC";
				entrada.p_IdTipoBloqueoPortafolio = "SOC";
				entrada.p_IdEmpleadoFiltro = "SOC";
				entrada.p_IdCategoriaInversionista = "SOC";
				entrada.p_IdPropositoPortafolio = "SOC";
				entrada.p_IdEstadoPortafolio = "SOC";
				entrada.p_IdPerfilComercial = "SOC";
				entrada.p_IdPerfilProductos = "SOC";
				entrada.p_IdPerfilCobros = "SOC";
				entrada.p_FechaNacimientoDesde = "SOC";
				entrada.p_FechaNacimientoHasta = "SOC";
				entrada.p_FechaCreacionDesde = "SOC";
				entrada.p_FechaCreacionHasta = "SOC";
				entrada.p_FechaDefuncionDesde = "SOC";
				entrada.p_FechaDefuncionHasta = "SOC";
				entrada.p_AliasPortafolio = "SOC";
				
				entrada.p_IdPersonaBancoCustodio = "SOC";
				entrada.p_IdPersonaBrokerExtranjero = "SOC";
				
				entrada.p_BuscaPorFlujoCaja = "SOC";
				entrada.p_UsoFlujoCaja = "SOC";
				entrada.p_SaldoFlujoCaja = "SOC";
				entrada.p_IdMonedaFlujoCaja = "SOC";
				entrada.p_CriterioFlujoCaja = "SOC";
				
				entrada.p_BuscaPorFlujoCustodia = "SOC";
				entrada.p_TipoOperacionCustodia = "SOC";
				entrada.p_SaldoFlujoCustodia = "SOC";0
				entrada.p_CriterioFlujoCustodia = "SOC";
				entrada.p_IdInstrumento = "SOC";				 
				 */
				
				
				
	 			JsonParser parser 		= new JsonParser();
		        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
		        GenerarReporteConsultaAvanzadaCliente demo=new GenerarReporteConsultaAvanzadaCliente();		
		    	System.out.println(demo.call(con, dataJsonIn).toString());	
		    	con.commit();
			}catch(Exception e){
				con.rollback();
				e.printStackTrace();
			}finally{
				con.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
}