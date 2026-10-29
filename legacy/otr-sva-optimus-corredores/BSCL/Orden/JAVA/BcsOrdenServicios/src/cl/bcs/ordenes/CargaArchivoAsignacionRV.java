package cl.bcs.ordenes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoAsignacionRV implements JavaCall{
	
	private final Log log =new Log("CargaArchivoAsignacionRV");
	
	private final String SQL_INSERT_CARGA_ASIGNACION_RV = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoAsignacionRV_InsertCargaArchivoAsignacionRV.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoAsignacionRV_SelectInstrumentos.sql");
	private final String SQL_SELECT_PORTAFOLIOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoAsignacionRV_SelectPortafolios.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoAsignacionRV_SelectIndicadoresPortafolio.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoAsignacionRV_SelectTransacciones.sql");
	private final String SQL_SELECT_CUENTA_CUSTODIO = loadResourceToString("cl/bcs/ordenes/sql/GloblalesCargaArchivo_SelectCuentaCustodio.sql");
	
	public static class AsignacionRV{
		public Long			IdInterno;
		public String 		CodigoLibro;
		public String 		FechaSubasta;
		public String 		FechaEmision;
		public String 		FechaPago;
		public String 		RutCavali;
		public Integer 		CodigoPortafolio;
		public Integer		Mercado;
		public Integer		Producto;
		public String		Bolsa;
		public String		TipoOperacion;
		public String		Instrumento;
		public BigDecimal	Cantidad;
		public BigDecimal	Precio;
		public String		Moneda;
		public BigDecimal	Nominal;
		public String		FolioTransaccion;
		public BigDecimal	BLV;
		public BigDecimal	Cavali;
		public BigDecimal	Smv;
		public BigDecimal	FondoLiquidacion;
		public BigDecimal	FondoGarantias;
		public BigDecimal	ComisionSab;
		public String		Boveda;
		public String		Cuenta;
		public String		IdEstado;
		public String		Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolios{
		public Long IdPortafolio;
		public Long IdCliente;
		public String CodigoPortafolio;
		public String DocumentoIdentidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Instrumentos{
		public Long		IdInstrumento;
		public String	IdMoneda;
		public String	Nemotecnico;
		public String   IdClasificacionTipoInstrumento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorPortafolio{
		public Long IdPortafolio;
		public String OperaCustodiaRV;
		public String OperaCustodiaRFIF;
		public String OperaCuentaInversion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Transaccion {
		public String EsFungible;
		public Long IdTransaccion;
		public String AccionOrden;
		public String Folio;
		public String Nemotecnico;
		public java.sql.Date FechaSistemaCreacion;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentasCustodioPortafolio{
		public Long IdCuentaCustodioPortafolio;
		public String Codigo;
		public String IdBoveda;
		public Long IdPortafolio;
	}
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCargaArchivoAsignacionRV{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoAsignacionRV{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoAsignacionRV in=gson.fromJson(jin.toString() ,InCargaArchivoAsignacionRV.class);
		OutCargaArchivoAsignacionRV out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoAsignacionRV logica(Connection con, InCargaArchivoAsignacionRV entrada) throws Exception {
		OutCargaArchivoAsignacionRV out = new OutCargaArchivoAsignacionRV();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_NombreArchivoLog="ord/CARGA_ASIGNACION_RV_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		File folder = new File(entrada.p_Ruta + "/ord");
		if(!folder.exists()){
			folder.mkdirs();
		}
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			
			String archivoDecoded = new String(Base64.getDecoder().decode(entrada.p_Archivo));
			System.out.println(archivoDecoded);
			SimpleDateFormat formatoArchivo = new SimpleDateFormat("dd-MM-yy");
			SimpleDateFormat formatoSP = new SimpleDateFormat("yyyy-MM-dd");
			JsonParser parser     = new JsonParser();
	        JsonObject dataJsonIn  = parser.parse(archivoDecoded).getAsJsonObject();
			
			List<AsignacionRV> asignaciones = new ArrayList<AsignacionRV>();
			
			JsonArray registros= dataJsonIn.getAsJsonArray("p_contenido");
			Long i = 0L;
			List<String> nemotecnicos = new ArrayList<String>();
			List<String> documentosIdentidad = new ArrayList<String>();
			List<String> portafolios = new ArrayList<String>();
			List<Long> idsPortafolio = new ArrayList<Long>();
			List<String> foliosTransacciones = new ArrayList<String>();
			List<String> cuentaCustodioPortafolio = new ArrayList<String>();
			List<String> bovedas = new ArrayList<String>();
			for(JsonElement registro : registros){
				numeroRegistros++;
				AsignacionRV asignacion = new AsignacionRV();
				JsonObject elemento = registro.getAsJsonObject();
				String validacion = elemento.get("MENSAJEVALIDACION").getAsString();
				if("OK".equalsIgnoreCase(validacion)){
					try{
						Date fechaSubasta = formatoArchivo.parse(elemento.get("CARV_FECHASUBASTA").getAsString());
						Date fechaEmision = formatoArchivo.parse(elemento.get("CARV_FECHAEMISION").getAsString());
						Date fechaPago = formatoArchivo.parse(elemento.get("CARV_FECHAPAGO").getAsString());
						asignacion.CodigoLibro = elemento.get("CARV_CODIGOLIBRO").getAsString();
						asignacion.FechaSubasta = formatoSP.format(fechaSubasta);
						asignacion.FechaEmision = formatoSP.format(fechaEmision);
						asignacion.FechaPago = formatoSP.format(fechaPago);
						asignacion.RutCavali = elemento.get("CARV_RUTCAVALI").getAsString();
						asignacion.CodigoPortafolio = elemento.get("CARV_CODIGOPORTAFOLIO").getAsInt();
						asignacion.Mercado = elemento.get("CARV_MERCADO").getAsInt();
						asignacion.Producto = elemento.get("CARV_PRODUCTO").getAsInt();
						asignacion.Bolsa = elemento.get("CARV_BOLSA").getAsString();
						asignacion.TipoOperacion = elemento.get("CARV_TIPOOPERACION").getAsString();
						asignacion.Instrumento = elemento.get("CARV_INSTRUMENTO").getAsString();
						asignacion.Cantidad = elemento.get("CARV_CANTIDAD").getAsBigDecimal();
						asignacion.Precio = elemento.get("CARV_PRECIO").getAsBigDecimal();
						asignacion.Moneda = elemento.get("CARV_MONEDA").getAsString();
						asignacion.Nominal = elemento.get("CARV_NOMINAL").getAsBigDecimal();
						asignacion.FolioTransaccion = elemento.get("CARV_FOLIOTRANSACCION").getAsString();
						asignacion.BLV = elemento.get("CARV_BLV").getAsBigDecimal();
						asignacion.Cavali = elemento.get("CARV_CAVALI").getAsBigDecimal();
						asignacion.Smv = elemento.get("CARV_SMV").getAsBigDecimal();
						asignacion.FondoLiquidacion = elemento.get("CARV_FONDOLIQUIDACION").getAsBigDecimal();
						asignacion.FondoGarantias = elemento.get("CARV_FONDOGARANTIAS").getAsBigDecimal();
						asignacion.ComisionSab = elemento.get("CARV_COMISIONSAB").getAsBigDecimal();
						asignacion.Boveda = elemento.get("CARV_BOVEDA").getAsString();
						asignacion.Cuenta = elemento.get("CARV_CUENTA").getAsString();
						asignacion.IdInterno = i;
						asignacion.IdEstado = "PEN";
						asignacion.Resultado = "";
						i++;
						if(!nemotecnicos.contains(asignacion.Instrumento)) nemotecnicos.add(asignacion.Instrumento);
						if(!documentosIdentidad.contains(asignacion.RutCavali)) documentosIdentidad.add(asignacion.RutCavali);
						if(!portafolios.contains(String.valueOf(asignacion.CodigoPortafolio))) portafolios.add(String.valueOf(asignacion.CodigoPortafolio));
						if(!foliosTransacciones.contains(asignacion.FolioTransaccion)) foliosTransacciones.add(asignacion.FolioTransaccion);
						if(!cuentaCustodioPortafolio.contains(asignacion.Cuenta)) cuentaCustodioPortafolio.add(asignacion.Cuenta);
						if(!bovedas.contains(asignacion.Boveda)) bovedas.add(asignacion.Boveda);
						asignaciones.add(asignacion);
					 }catch(Exception e){
						numeroRegistrosErroneos++;
						bwArchivoLog.write("Error al parsear el registro:" + elemento);
						bwArchivoLog.newLine();
					}
				}else{
					numeroRegistrosErroneos++;
					bwArchivoLog.write("Registro con Error:" + validacion + ",registro:" + elemento);
					bwArchivoLog.newLine();
				}
			}
			
			HashMap<String,Object> params = new HashMap<>();
			params.put("Nemotecnicos", nemotecnicos);
			List<Instrumentos> idInstrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumentos.class);
			
			params.clear();
			params.put("DocumentoIdentidad", documentosIdentidad);
			params.put("CodigoPortafolios", portafolios);
			List<Portafolios> idPortafolios = sql.query(con, SQL_SELECT_PORTAFOLIOS, params, Portafolios.class);
			for(Portafolios portafolio : idPortafolios){
				if(!idsPortafolio.contains(portafolio.IdPortafolio)) idsPortafolio.add(portafolio.IdPortafolio);
			}
			
			params.clear();
			params.put("foliosTransaccion", foliosTransacciones);
			List<Transaccion> transacciones = sql.query(con, SQL_SELECT_TRANSACCIONES, params, Transaccion.class);
			
			HashMap<Long,IndicadorPortafolio> indicadores = new HashMap<>();
			if(idsPortafolio.size() > 0){
				params.clear();
				params.put("idsPortafolio", idsPortafolio);
				List<IndicadorPortafolio> _indicadores = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIOS, params, IndicadorPortafolio.class);
				for (IndicadorPortafolio ind : _indicadores) {
					indicadores.put(ind.IdPortafolio, ind);
				}
			}
			List<CuentasCustodioPortafolio> cuentasCustodioPortafolio = new ArrayList<CuentasCustodioPortafolio>();
			
			params.clear();
			params.put("IdBoveda", bovedas);
			params.put("Codigo", cuentaCustodioPortafolio);
			params.put("IdPortafolio", idsPortafolio);
			cuentasCustodioPortafolio = sql.query(con, SQL_SELECT_CUENTA_CUSTODIO, params, CuentasCustodioPortafolio.class);
			
			
			String idBovedaDefecto = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "ORDENES", "IdBovedaDefecto");
			
			List<OrdenPostrera_Entrada> ordenes = new ArrayList<OrdenPostrera_Entrada>();
			
			for(AsignacionRV asig : asignaciones){
				try{
					Instrumentos instrumentoArchivo = null;
					OrdenPostrera_Entrada orden = new OrdenPostrera_Entrada();
					orden.IdInterno = asig.IdInterno;
					for(Portafolios portafolio : idPortafolios){
						if(portafolio.CodigoPortafolio.equals(String.valueOf(asig.CodigoPortafolio)) &&
						   portafolio.DocumentoIdentidad.equalsIgnoreCase(asig.RutCavali)){
						   orden.IdPortafolio = portafolio.IdPortafolio;
						}
					}
					if(orden.IdPortafolio == null) 
						throw new Exception("Portafolio no existe para cliente RUT Cavali: " + asig.RutCavali + ",Codigo Portafolio: " + asig.CodigoPortafolio);
					for(Instrumentos instrumento : idInstrumentos){
						if(instrumento.Nemotecnico.equalsIgnoreCase(asig.Instrumento)){
							instrumentoArchivo = instrumento; 
						}
					}
					if(instrumentoArchivo == null) throw new Exception("Instrumento:" + asig.Instrumento + ",no existe");
					if(!"RV".equalsIgnoreCase(instrumentoArchivo.IdClasificacionTipoInstrumento)) throw new Exception("Instrumento:" + asig.Instrumento + ",no es RV");
					for (Transaccion tran : transacciones) {
						if (tran.Folio.equalsIgnoreCase(asig.FolioTransaccion) && Fecha.toStringDate(tran.FechaSistemaCreacion).equals(asig.FechaEmision) && tran.Nemotecnico.equalsIgnoreCase(asig.Instrumento)) {
							if (("COMPRA".equalsIgnoreCase(tran.AccionOrden) && "C".equalsIgnoreCase(asig.TipoOperacion)) || 
									("VENTA".equalsIgnoreCase(tran.AccionOrden) && "V".equalsIgnoreCase(asig.TipoOperacion))) {
								orden.IdTransaccion = tran.IdTransaccion;
							}
						}
					}
					for(CuentasCustodioPortafolio cuenta : cuentasCustodioPortafolio){
						if(asig.Cuenta.equalsIgnoreCase(cuenta.Codigo) && asig.Boveda.equalsIgnoreCase(cuenta.IdBoveda) && orden.IdPortafolio.longValue() == cuenta.IdPortafolio.longValue()){
							orden.IdCuentaCustodioPortafolio = cuenta.IdCuentaCustodioPortafolio;
							break;
						}
					}
					if(orden.IdCuentaCustodioPortafolio == null) throw new Exception("Cuenta: " + asig.Cuenta + ",no existe para cliente RUT Cavali: " + asig.RutCavali + ",Codigo Portafolio: " + asig.CodigoPortafolio + " Boveda:" + asig.Boveda);
					
					if(orden.IdTransaccion == null) throw new Exception("Folio Transaccion:" + asig.FolioTransaccion + ",no existe");
					orden.Cantidad = asig.Cantidad;
					orden.ValorRescate = BigDecimal.ZERO;
					orden.EsFacturaInmediata = "N";
					orden.FechaRecepcion = FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa);
					orden.FolioCartera = 0L;
					orden.Glosa = "";
					orden.IdBoveda = idBovedaDefecto;
					if("S".equalsIgnoreCase(indicadores.get(orden.IdPortafolio).OperaCustodiaRV)){
						orden.IdFormaCustodia = "CUS";
					}else{
						orden.IdFormaCustodia = "NOM";
					}
					
					orden.IdMonedaLiquidacionOrden = asig.Moneda;
					if ("S".equalsIgnoreCase(indicadores.get(orden.IdPortafolio).OperaCuentaInversion)) {
						orden.PagoCobroCuentaInversion = "S";
					}
					else {
						orden.PagoCobroCuentaInversion = "N";
					}
					orden.IdPreferenciaPagoPortafolio = null;
					orden.PlazoVentaCorta = 0L;
					
					orden.TasaVentaCorta = BigDecimal.ZERO;
					orden.TipoCambio = null;
					orden.IdBoveda = asig.Boveda;
					ordenes.add(orden);
				}catch(Exception e){
					numeroRegistrosErroneos++;
					asig.Resultado = e.getMessage();
					asig.IdEstado = "ERR";
					bwArchivoLog.write("Registro con Error:" + e.getMessage());
					bwArchivoLog.newLine();
				}
				
			}
			
			if(ordenes.size() > 0){
				CrearOrdenesPostreras crearOrdenesPostreras = new CrearOrdenesPostreras();
				CrearOrdenesPostreras_Entrada crearOrdenesPostrerasEntrada = new CrearOrdenesPostreras_Entrada();
				crearOrdenesPostrerasEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				crearOrdenesPostrerasEntrada.p_IdModulo = entrada.p_IdModulo;
				crearOrdenesPostrerasEntrada.p_IdUsuario = entrada.p_IdUsuario;
				crearOrdenesPostrerasEntrada.p_simularOperacion = "N";
				crearOrdenesPostrerasEntrada.p_UsaReglasNegocio = "S";
				
				crearOrdenesPostrerasEntrada.p_Ordenes = ordenes;
				try{
					CrearOrdenesPostreras_Salida salida = crearOrdenesPostreras.logica(con, crearOrdenesPostrerasEntrada);
					
					for(Resultado_Salida resultado : salida.P_OUT_RESULTADO){
						for(AsignacionRV asignacion : asignaciones){
							if(resultado.IdInterno.longValue() == asignacion.IdInterno.longValue()){
								numeroRegistrosCorrecto++;
								asignacion.IdEstado = "OK";
								asignacion.Resultado = "OK";
							}
						}
						
					}
					
					for(MensajeSalida mensaje : salida.P_OUT_MENSAJES){
						for(AsignacionRV asignacion : asignaciones){
							if(asignacion.IdInterno.longValue() == mensaje.id.longValue() && "ERROR".equalsIgnoreCase(mensaje.tipo) && "PEN".equalsIgnoreCase(asignacion.IdEstado)){
								asignacion.IdEstado = "ERR";
								asignacion.Resultado = mensaje.descripcion;
								numeroRegistrosErroneos++;
								bwArchivoLog.write("Error a crear Asignacion:" + mensaje.descripcion);
								bwArchivoLog.newLine();
							}
						}
					}
				}catch(Exception e){
					for(AsignacionRV asignacion : asignaciones){
						if("PEN".equalsIgnoreCase(asignacion.IdEstado)){
							asignacion.IdEstado = "ERR";
							asignacion.Resultado = "Error servicio crear postrera";
							numeroRegistrosErroneos++;
						}
					}
					bwArchivoLog.write("Error servicio crear postrera:" + e.getMessage());
					bwArchivoLog.newLine();
					con.rollback();
				}
				
				
			}
			
			for(AsignacionRV asignacion : asignaciones){
				if("PEN".equalsIgnoreCase(asignacion.IdEstado)) asignacion.IdEstado = "ERR";
			}
			
			sql.batch(con, SQL_INSERT_CARGA_ASIGNACION_RV, asignaciones, AsignacionRV.class);
			//evaluar salida
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new Date());
			bwArchivoLog.write("Fecha Hora Inicio:" + fecha);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Fecha Hora Termino:" + fechaFin);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Tiempo de ejecución:" + (endTime - startTime));
			bwArchivoLog.newLine();
			bwArchivoLog.write("Total Registros:" + numeroRegistros);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Correctos:" + numeroRegistrosCorrecto);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Erroneos:" + numeroRegistrosErroneos);
			out.p_out_ErroresContador = new Long(numeroRegistrosErroneos);
			
		}catch(Exception e){
			log.write(e);
		}finally{
			log.writeAsJson("SALIDA",out);
			if(bwArchivoLog != null){
				bwArchivoLog.close();
			}
		}
		
		return out;
		
	}
	
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
  				
  				InCargaArchivoAsignacionRV entrada= new InCargaArchivoAsignacionRV();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Archivo		= "eyJwX2NvbnRlbmlkbyI6W3siQ0FSVl9DT0RJR09MSUJSTyI6IkNPRElHTyIsIkNBUlZfRkVDSEFTVUJBU1RBIjoiMTYtMTAtMTciLCJDQVJWX0ZFQ0hBRU1JU0lPTiI6IjE2LTEwLTE3IiwiQ0FSVl9GRUNIQVBBR08iOiIxNi0xMC0xNyIsIkNBUlZfUlVUQ0FWQUxJIjoiNTU1NTU1NSIsIkNBUlZfQ09ESUdPUE9SVEFGT0xJTyI6MjAxLCJDQVJWX01FUkNBRE8iOjcsIkNBUlZfUFJPRFVDVE8iOjIyLCJDQVJWX0JPTFNBIjoiT1RDIiwiQ0FSVl9USVBPT1BFUkFDSU9OIjoiQyIsIkNBUlZfSU5TVFJVTUVOVE8iOiJDT1BFQyIsIkNBUlZfQ0FOVElEQUQiOiIzMCIsIkNBUlZfUFJFQ0lPIjoiNC4zNjAwIiwiQ0FSVl9NT05FREEiOiJDTFAiLCJDQVJWX05PTUlOQUwiOjAsIkNBUlZfRk9MSU9UUkFOU0FDQ0lPTiI6IjE2ODAwMDEiLCJDQVJWX0JMViI6MCwiQ0FSVl9DQVZBTEkiOjAsIkNBUlZfU01WIjowLCJDQVJWX0ZPTkRPTElRVUlEQUNJT04iOjAsIkNBUlZfRk9ORE9HQVJBTlRJQVMiOjAsIkNBUlZfQ09NSVNJT05TQUIiOiIwIiwiTUVOU0FKRVZBTElEQUNJT04iOiJPSyJ9XX0NCg==";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoAsignacionRV demo= new CargaArchivoAsignacionRV();
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
