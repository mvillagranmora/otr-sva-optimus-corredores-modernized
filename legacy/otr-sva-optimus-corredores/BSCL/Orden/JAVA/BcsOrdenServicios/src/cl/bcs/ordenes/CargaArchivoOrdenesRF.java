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

import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.CrearOrdenes.ObjCobrosModificados;
import cl.bcs.ordenes.CrearOrdenes.ObjGarantiasIn;
import cl.bcs.ordenes.CrearOrdenes.ObjOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoOrdenesRF implements JavaCall{
	
	private final Log log =new Log("CargaArchivoOrdenesRF");
	
	private final String SQL_INSERT_CARGA_ORDEN_RF = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_InsertCargaOrdenRF.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectInstrumentos.sql");
	private final String SQL_SELECT_PORTAFOLIOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectPortafolios.sql");
	private final String SQL_SELECT_LIBRO_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectLibroOrdenes.sql");
	private final String SQL_SELECT_PARAMETRO_VIGENCIA = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectParametroVigencia.sql");
	private final String SQL_SELECT_AUTORIZADOR = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectAutorizador.sql");
	private final String SQL_SELECT_BANCO_CUSTODIO_LIQUIDADOR = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectBancoLiquidador.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectPreferenciaPago.sql");
	private final String SQL_SELECT_CUENTA_CUSTODIO = loadResourceToString("cl/bcs/ordenes/sql/GloblalesCargaArchivo_SelectCuentaCustodio.sql");
	
	public static class OrdenRF{
		public Long			IdInterno;
		public String 		CodigoLibro;
		public String 		RutCavali;
		public Integer  	CodigoPortafolio;
		public Integer  	Vigencia;
		public String		CondicionLiquidacion;
		public String		MonedaLiquidacion;
		public String		TipoInstrumento;
		public String		Bolsa;
		public String		FormaNegociacion;
		public String		CodigoProducto;
		public String		TipoOperacion;
		public String		Instrumento;
		public BigDecimal 	Cantidad;
		public BigDecimal 	Nominal;
		public String		TipoPrecio;
		public BigDecimal 	Precio;
		public BigDecimal	Comision;
		public String		FormaPago;
		public String		TipoCustodia;
		public String		BancoCustodio;
		public String		BancoLiquida;
		public String		MedioCaptacion;
		public String		Representante;
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
	
	public static class LibroOrdenes{
		public Long IdPortafolio;
		public Long IdLibroOrdenes;
		public String NombreLibroOrdenes;
		public String IdTipoLibroOrden;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Vigencia{
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Autorizador{
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		public Long IdAutorizado;
		public Long IdPersona;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentasCustodio{
		public Long IdCuentaCustodioPortafolio;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreferenciaPago{
		public Long IdPreferenciaPagoPortafolio;
		public Long IdPortafolio;
		public String IdMedioDePago;
		public String IdTipoOperacion;
		
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
	
	public static class InCargarArchivoOrdenesRF{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargarArchivoOrdenesRF{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargarArchivoOrdenesRF in=gson.fromJson(jin.toString() ,InCargarArchivoOrdenesRF.class);
		OutCargarArchivoOrdenesRF out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargarArchivoOrdenesRF logica(Connection con, InCargarArchivoOrdenesRF entrada) throws Exception {
		OutCargarArchivoOrdenesRF out = new OutCargarArchivoOrdenesRF();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_NombreArchivoLog="ord/CARGA_ARCHIVO_ORDEN_RF_"+fecha+".log";
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
			JsonParser parser     = new JsonParser();
	        JsonObject dataJsonIn  = parser.parse(archivoDecoded).getAsJsonObject();
			
			List<OrdenRF> ordenes = new ArrayList<OrdenRF>();
			
			JsonArray registros= dataJsonIn.getAsJsonArray("p_contenido");
			
			List<String> nemotecnicos = new ArrayList<String>();
			List<String> documentosIdentidad = new ArrayList<String>();
			List<String> documentosIdentidadRepresentante = new ArrayList<String>();
			List<String> portafolios = new ArrayList<String>();
			List<Long> idsPortafolio = new ArrayList<Long>();
			List<String> bancosCustodio = new ArrayList<String>();
			List<String> bancoLiquidador = new ArrayList<String>();
			List<String> mediosPago = new ArrayList<String>();
			List<String> codigosLibros = new ArrayList<String>();
			List<String> cuentaCustodioPortafolio = new ArrayList<String>();
			List<String> bovedas = new ArrayList<String>();
			Long i = 0L;
			for(JsonElement registro : registros){
				numeroRegistros++;
				OrdenRF orden = new OrdenRF();
				JsonObject elemento = registro.getAsJsonObject();
				String validacion = elemento.get("MENSAJEVALIDACION").getAsString();
				if("OK".equalsIgnoreCase(validacion)){
					try{
						orden.CodigoLibro = elemento.get("CORF_CODIGOLIBRO").getAsString();
						orden.RutCavali = elemento.get("CORF_RUTCAVALI").getAsString();
						orden.CodigoPortafolio = !"".equalsIgnoreCase(elemento.get("CORF_CODIGOPORTAFOLIO").getAsString()) ? elemento.get("CORF_CODIGOPORTAFOLIO").getAsInt() : 0;
						orden.Vigencia = !"".equalsIgnoreCase(elemento.get("CORF_VIGENCIA").getAsString()) ? elemento.get("CORF_VIGENCIA").getAsInt() : 0;
						orden.CondicionLiquidacion = elemento.get("CORF_CONDICIONLIQUIDACION").getAsString();
						orden.MonedaLiquidacion = elemento.get("CORF_MONEDALIQUIDACION").getAsString();
						orden.TipoInstrumento = elemento.get("CORF_TIPOINSTRUMENTO").getAsString();
						orden.Bolsa = elemento.get("CORF_BOLSA").getAsString();
						orden.FormaNegociacion = elemento.get("CORF_FORMANEGOCIACION").getAsString();
						orden.CodigoProducto = elemento.get("CORF_CODIGOPRODUCTO").getAsString();
						orden.TipoOperacion = elemento.get("CORF_TIPOOPERACION").getAsString();
						orden.Instrumento = elemento.get("CORF_INSTRUMENTO").getAsString();
						orden.Cantidad = !"".equalsIgnoreCase(elemento.get("CORF_CANTIDAD").getAsString()) ?  elemento.get("CORF_CANTIDAD").getAsBigDecimal() : BigDecimal.ZERO;
						orden.Nominal = !"".equalsIgnoreCase(elemento.get("CORF_NOMINAL").getAsString()) ? elemento.get("CORF_NOMINAL").getAsBigDecimal() : BigDecimal.ZERO;
						orden.TipoPrecio = elemento.get("CORF_TIPOPRECIO").getAsString();
						orden.Precio = !"".equalsIgnoreCase(elemento.get("CORF_PRECIO").getAsString()) ? elemento.get("CORF_PRECIO").getAsBigDecimal() : BigDecimal.ZERO;
						orden.Comision = !"".equalsIgnoreCase(elemento.get("CORF_COMISION").getAsString()) ? elemento.get("CORF_COMISION").getAsBigDecimal() : BigDecimal.ZERO;
						orden.FormaPago = elemento.get("CORF_FORMAPAGO").getAsString();
						orden.TipoCustodia = elemento.get("CORF_TIPOCUSTODIA").getAsString();
						orden.BancoCustodio = elemento.get("CORF_BANCOCUSTODIO").getAsString();
						orden.BancoLiquida = elemento.get("CORF_BANCOLIQUIDA").getAsString();
						orden.MedioCaptacion = elemento.get("CORF_MEDIOCAPTACION").getAsString();
						orden.Representante = !"0".equalsIgnoreCase(elemento.get("CORF_REPRESENTANTE").getAsString()) ? elemento.get("CORF_REPRESENTANTE").getAsString() : "";
						orden.Boveda = elemento.get("CORF_BOVEDA").getAsString();
						orden.Cuenta = elemento.get("CORF_CUENTA").getAsString();
						orden.IdInterno = i;
						orden.IdEstado = "PEN";
						orden.Resultado = "";
						i++;
						if(!nemotecnicos.contains(orden.Instrumento)) nemotecnicos.add(orden.Instrumento);
						if(!documentosIdentidad.contains(orden.RutCavali)) documentosIdentidad.add(orden.RutCavali);
						if(!documentosIdentidadRepresentante.contains(orden.Representante)) documentosIdentidadRepresentante.add(orden.Representante);
						if(!portafolios.contains(String.valueOf(orden.CodigoPortafolio))) portafolios.add(String.valueOf(orden.CodigoPortafolio));
						if(!bancosCustodio.contains(orden.BancoCustodio)) bancosCustodio.add(orden.BancoCustodio);
						if(!bancoLiquidador.contains(orden.BancoLiquida)) bancoLiquidador.add(orden.BancoLiquida);
						if(!mediosPago.contains(orden.FormaPago)) mediosPago.add(orden.FormaPago);
						if(!codigosLibros.contains(orden.CodigoLibro)) codigosLibros.add(orden.CodigoLibro);
						if(!cuentaCustodioPortafolio.contains(orden.Cuenta)) cuentaCustodioPortafolio.add(orden.Cuenta);
						if(!bovedas.contains(orden.Boveda)) bovedas.add(orden.Boveda);
						ordenes.add(orden);
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
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<Vigencia> vigencias = sql.query(con, SQL_SELECT_PARAMETRO_VIGENCIA, params, Vigencia.class);
			Long vigencia = Long.parseLong(vigencias.get(0).Valor);
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
			InformacionEmpresa informacionEmpresa = consultarEmpresa.logica(con, entrada.p_IdEmpresa);
			List<LibroOrdenes> libros = new ArrayList<LibroOrdenes>();
			List<Autorizador> representantes = new ArrayList<Autorizador>();
			List<CuentasCustodio> cuentasCustodio = new ArrayList<CuentasCustodio>();
			List<PreferenciaPago> preferenciaPago = new ArrayList<PreferenciaPago>();
			List<CuentasCustodioPortafolio> cuentasCustodioPortafolio = new ArrayList<CuentasCustodioPortafolio>();
			if(idsPortafolio.size() > 0){
				params.clear();
				params.put("NombreLibroOrdenes",codigosLibros);
				params.put("IdPortafolio", idsPortafolio);
				libros = sql.query(con, SQL_SELECT_LIBRO_ORDENES, params, LibroOrdenes.class);
				
				params.clear();
				params.put("DocumentoIdentidad", documentosIdentidadRepresentante);
				params.put("IdPortafolio", idsPortafolio);
				representantes = sql.query(con, SQL_SELECT_AUTORIZADOR, params, Autorizador.class);
				
				params.clear();
				params.put("BancoCustodio", bancosCustodio);
				params.put("BancoLiquida", bancoLiquidador);
				params.put("IdPortafolio", idsPortafolio);
				cuentasCustodio = sql.query(con, SQL_SELECT_BANCO_CUSTODIO_LIQUIDADOR, params, CuentasCustodio.class);
				
				params.clear();
				params.put("IdMedioDePago", mediosPago);
				params.put("IdPortafolio", idsPortafolio);
				preferenciaPago = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO, params, PreferenciaPago.class);
				
				params.clear();
				params.put("IdBoveda", bovedas);
				params.put("Codigo", cuentaCustodioPortafolio);
				params.put("IdPortafolio", idsPortafolio);
				cuentasCustodioPortafolio = sql.query(con, SQL_SELECT_CUENTA_CUSTODIO, params, CuentasCustodioPortafolio.class);
			}
			
			List<ObjOrdenesEntrada> p_Ordenes = new ArrayList<ObjOrdenesEntrada>();
			
			for(OrdenRF orden : ordenes){
				try{
					ObjOrdenesEntrada p_orden = new ObjOrdenesEntrada();
					
					//Rut Cavali y Portafolio
					for(Portafolios portafolio : idPortafolios){
						if(portafolio.CodigoPortafolio.equals(String.valueOf(orden.CodigoPortafolio)) &&
						   portafolio.DocumentoIdentidad.equalsIgnoreCase(orden.RutCavali)){
							p_orden.IdPortafolio = portafolio.IdPortafolio;
						}
					}
					if(p_orden.IdPortafolio == null) 
						throw new Exception("Portafolio no existe para cliente RUT Cavali: " + orden.RutCavali + ",Codigo Portafolio: " + orden.CodigoPortafolio);
					
					//Libro de ordenes
					p_orden.IdLibroOrdenes = 0L;
					p_orden.IdTipoLibroOrden = "NORMAL";
					if(!"".equalsIgnoreCase(orden.CodigoLibro)){
						for(LibroOrdenes libro : libros){
							if(libro.NombreLibroOrdenes.equalsIgnoreCase(orden.CodigoLibro) 
							   && libro.IdPortafolio.longValue() == p_orden.IdPortafolio){
								p_orden.IdLibroOrdenes = libro.IdLibroOrdenes;
								p_orden.IdTipoLibroOrden = libro.IdTipoLibroOrden;
							}
						}
						if(p_orden.IdLibroOrdenes == null) 
							throw new Exception("Libro: " + orden.CodigoLibro + ",no existe para cliente RUT Cavali: " + orden.RutCavali + ",Codigo Portafolio: " + orden.CodigoPortafolio);
						
					}
					//Vigencia
					p_orden.IdTipoVigencia = "FF";
					List<FechasIn> fechas = new ArrayList<FechasIn>();
					FechasIn fechaOrden = new FechasIn();
					fechaOrden.fechaInicial = fechaSistema;
					fechaOrden.numeroDias = orden.Vigencia.intValue() == 0 ? vigencia : orden.Vigencia;
					fechas.add(fechaOrden);
					InObtenerDiaSiguiente inDiaSiguiente = new InObtenerDiaSiguiente();
					inDiaSiguiente.p_diaHabil = "S";
					inDiaSiguiente.p_fechas = fechas;
					inDiaSiguiente.p_idPais = informacionEmpresa.IdPais;
					ObtenerDiaSiguiente o = new ObtenerDiaSiguiente();
					List<FechasOut> fechaVigencia = o.logica(con,inDiaSiguiente).p_out_fechas;
					p_orden.FechaVigencia = fechaVigencia.get(0).fechaCalculada;
					
					//CodidicionLiquidacion
					if(!"".equalsIgnoreCase(orden.CondicionLiquidacion)) p_orden.IdCondicionDeLiquidacion = orden.CondicionLiquidacion;
					//MonedaLiquidacion
					if(!"".equalsIgnoreCase(orden.MonedaLiquidacion)) p_orden.IdMonedaLiquidacionOrden = orden.MonedaLiquidacion;
					//Bolsa
					 p_orden.IdBolsa = !"".equalsIgnoreCase(orden.Bolsa) ? orden.Bolsa : "BVL";
					//FormaNegociacion
					 if(!"".equalsIgnoreCase(orden.FormaNegociacion)) p_orden.IdSistemaBursatil = orden.FormaNegociacion;
					//TipoOperacion
					String tipoOperacion = "C".equalsIgnoreCase(orden.TipoOperacion) ? "COMRF" : "VENRF";
					p_orden.IdTipoOperacion = tipoOperacion;
					//Nemotecnico
					for(Instrumentos instrumento : idInstrumentos){
						if(instrumento.Nemotecnico.equalsIgnoreCase(orden.Instrumento)){
							p_orden.IdInstrumento = instrumento.IdInstrumento;
							p_orden.IdMonedaLiquidacionOrden = p_orden.IdMonedaLiquidacionOrden == null ? instrumento.IdMoneda : p_orden.IdMonedaLiquidacionOrden;
							if(!"RF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento)) throw new Exception("Instrumento: " + orden.Instrumento + ",no es RF.");
						}
					}
					if(p_orden.IdInstrumento == null){
						throw new Exception("Instrumento: " + orden.Instrumento + ",no existe.");
					}
					for(CuentasCustodioPortafolio cuenta : cuentasCustodioPortafolio){
						if(orden.Cuenta.equalsIgnoreCase(cuenta.Codigo) && orden.Boveda.equalsIgnoreCase(cuenta.IdBoveda) && p_orden.IdPortafolio.longValue() == cuenta.IdPortafolio.longValue()){
							p_orden.IdCuentaCustodioPortafolio = cuenta.IdCuentaCustodioPortafolio;
							break;
						}
					}
					if(p_orden.IdCuentaCustodioPortafolio == null) throw new Exception("Cuenta: " + orden.Cuenta + ",no existe para cliente RUT Cavali: " + orden.RutCavali + ",Codigo Portafolio: " + orden.CodigoPortafolio + " Boveda:" + orden.Boveda);
					//Cantidad
					p_orden.Cantidad = orden.Cantidad;
					//Nominal
					//p_orden.ValorRescate = orden.Nominal;
					//TipoPrecio
					p_orden.IdTipoPrecio = orden.TipoPrecio;
					//Precio
					p_orden.PrecioLimite = orden.Precio;
					if("L".equalsIgnoreCase(p_orden.IdTipoPrecio) && orden.Precio.intValue() == 0) throw new Exception("Precio es 0, para operacion Precio Limite, para cliente RUT Cavali: " 
							+ orden.RutCavali + ",Codigo Portafolio: " + orden.CodigoPortafolio + 
							",Instrumento: " + orden.Instrumento);
					
					//SistemaBursatil
					p_orden.IdSistemaBursatil = orden.FormaNegociacion;
					//Forma Pago
					if(!"".equalsIgnoreCase(orden.FormaPago)){
						p_orden.PagoCobroCuentaInversion = "N";
						for(PreferenciaPago preferencia : preferenciaPago){
							if(preferencia.IdPortafolio.longValue() == p_orden.IdPortafolio.longValue() &&
							   preferencia.IdMedioDePago.equalsIgnoreCase(orden.FormaPago) &&
							   preferencia.IdTipoOperacion.equalsIgnoreCase(tipoOperacion)){
								p_orden.IdPreferenciaPagoPortafolio = preferencia.IdPreferenciaPagoPortafolio;
							}
							if(p_orden.IdPreferenciaPagoPortafolio == null) throw new Exception("Preferencia de pago: " + orden.FormaPago + "no existe configurada para cliente RUT Cavali:"
																								+ orden.RutCavali + ",Codigo Portafolio: " + orden.CodigoPortafolio);
						}
					}else{
						p_orden.PagoCobroCuentaInversion = "S";
					}
					//FormaCustodia
					p_orden.IdFormaCustodia = "C".equalsIgnoreCase(orden.TipoCustodia) ? "CUS" : "NOM";
					//Cuentas Custodio y Liquidador
					if(!"".equalsIgnoreCase(orden.BancoCustodio) || !"".equalsIgnoreCase(orden.BancoLiquida)){
						for(CuentasCustodio cuentaCustodio : cuentasCustodio){
							if(cuentaCustodio.CodigoBancoCustodio.equalsIgnoreCase(orden.BancoCustodio) &&
							   cuentaCustodio.CodigoBancoLiquidador.equalsIgnoreCase(orden.BancoLiquida) &&
							   cuentaCustodio.IdPortafolio.longValue() == p_orden.IdPortafolio.longValue()){
								p_orden.IdCuentaCustodioPortafolio = cuentaCustodio.IdCuentaCustodioPortafolio;
							}
						}
						if(p_orden.IdCuentaCustodioPortafolio == null) throw new Exception("Cuenta Banco Custodio: " + orden.BancoCustodio + "y banco Liquida: " + orden.BancoLiquida 
																							+ "no existe para Rut Cavali: " + orden.RutCavali + ",Codigo Portafolio: " + orden.CodigoPortafolio);
					}
					//Mecanismo Captacion
					p_orden.IdMecanismoCaptacion = orden.MedioCaptacion;
					//Representante
					if(!"".equalsIgnoreCase(orden.Representante)){
						for(Autorizador representante : representantes){
							if(representante.IdPortafolio.longValue() == p_orden.IdPortafolio 
							   && representante.DocumentoIdentidad.equalsIgnoreCase(orden.Representante)){
								p_orden.IdPersonaAutorizadoDarOrdenes = representante.IdPersona;
							}
						}
						if(p_orden.IdPersonaAutorizadoDarOrdenes == null) throw new Exception("Representante: " + orden.Representante + " no existe,para cliente RUT Cavali: "
																							 + orden.RutCavali + ",Codigo Portafolio: " + orden.CodigoPortafolio);
					}
					
					p_orden.IdBoveda = orden.Boveda;
					p_orden.PorcentajeAllIn = orden.Comision;
					p_orden.IdInterno = orden.IdInterno;
					p_orden.IdTipoCanalIngreso =  "AGE";
					p_orden.EspecificaBolsa = "S";
					p_orden.IdTipoCanalDistribucion = "MAN";
					p_orden.IdPorcentajeVisibilidad = 1L;
					p_orden.IdInstrumentoSubyacente = 0L;
					p_orden.BaseTasa = 0L;
					p_orden.TipoInteres = "";
					p_Ordenes.add(p_orden);
					i++;
					
				}catch(Exception e){
					numeroRegistrosErroneos++;
					orden.Resultado = e.getMessage();
					orden.IdEstado = "ERR";
					bwArchivoLog.write("Registro con Error:" + e.getMessage());
					bwArchivoLog.newLine();
				}
			}
			
			if(p_Ordenes.size() > 0){
				try{
					CrearOrdenes crearOrdenes = new CrearOrdenes();
					CrearOrdenesEntrada crearOrdenesEntrada = new CrearOrdenesEntrada();
					crearOrdenesEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
					crearOrdenesEntrada.p_IdUsuario = entrada.p_IdUsuario;
					crearOrdenesEntrada.p_IdModulo = entrada.p_IdModulo;
					crearOrdenesEntrada.p_simularOperacion = "N";
					crearOrdenesEntrada.p_UsaReglasNegocio = "S";
					crearOrdenesEntrada.p_CobrosModificados = new ArrayList<ObjCobrosModificados>();
					crearOrdenesEntrada.p_Garantias = new ArrayList<ObjGarantiasIn>();
					crearOrdenesEntrada.p_Ordenes = p_Ordenes;
					
					CrearOrdenesSalida crearOrdenesSalida = crearOrdenes.logica(con, crearOrdenesEntrada);
					
					for(ObjRESULTADO resultado : crearOrdenesSalida.P_OUT_RESULTADO){
						for(OrdenRF orden : ordenes){
							if(orden.IdInterno.longValue() == resultado.IdInterno.longValue()){
								numeroRegistrosCorrecto++;
								orden.IdEstado = "OK";
								orden.Resultado = "OK";
							}
						}
					}
					
					for(MensajeSalida mensaje : crearOrdenesSalida.P_OUT_MENSAJES){
						for(OrdenRF orden : ordenes){
							if(orden.IdInterno.longValue() == mensaje.id.longValue() && "ERROR".equalsIgnoreCase(mensaje.tipo) && "PEN".equalsIgnoreCase(orden.IdEstado)){
								orden.IdEstado = "ERR";
								orden.Resultado = mensaje.descripcion;
								numeroRegistrosErroneos++;
								bwArchivoLog.write("Error a crear Orden:" + mensaje.descripcion);
								bwArchivoLog.newLine();
							}
						}
					}
				}catch(Exception e){
					for(OrdenRF orden : ordenes){
						if("PEN".equalsIgnoreCase(orden.IdEstado)){
							orden.IdEstado = "ERR";
							orden.Resultado = "Error servicio Ordenes";
							numeroRegistrosErroneos++;
						}
					}
					bwArchivoLog.write("Error servicio Ordenes:" + e.getMessage());
					bwArchivoLog.newLine();
					con.rollback();
				}
				
			}
			
			for(OrdenRF orden : ordenes){
				if("PEN".equalsIgnoreCase(orden.IdEstado)) orden.IdEstado = "ERR";
			}
			
			sql.batch(con, SQL_INSERT_CARGA_ORDEN_RF, ordenes, OrdenRF.class);
			
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
  				
  				InCargarArchivoOrdenesRF entrada= new InCargarArchivoOrdenesRF();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Archivo		= "eyJwX2NvbnRlbmlkbyI6W3siQ09SVl9DT0RJR09MSUJSTyI6IiIsIkNPUlZfUlVUQ0FWQUxJIjoiNTU1NTU1NSIsIkNPUlZfQ09ESUdPUE9SVEFGT0xJTyI6MjAxLCJDT1JWX1ZJR0VOQ0lBIjowLCJDT1JWX0NPTkRJQ0lPTkxJUVVJREFDSU9OIjoiVDAiLCJDT1JWX01PTkVEQUxJUVVJREFDSU9OIjoiQ0xQIiwiQ09SVl9USVBPSU5TVFJVTUVOVE8iOiIiLCJDT1JWX0JPTFNBIjoiQkNTIiwiQ09SVl9GT1JNQU5FR09DSUFDSU9OIjoiTkVHT0NJQUNJT05DT05USU5VQSIsIkNPUlZfQ09ESUdPUFJPRFVDVE8iOiIiLCJDT1JWX1RJUE9PUEVSQUNJT04iOiJDIiwiQ09SVl9JTlNUUlVNRU5UTyI6IkFCTkExNTA1MTMiLCJDT1JWX0NBTlRJREFEIjoxMCwiQ09SVl9OT01JTkFMIjoiIiwiQ09SVl9USVBPUFJFQ0lPIjoiTSIsIkNPUlZfUFJFQ0lPIjoiMCIsIkNPUlZfQ09NSVNJT04iOiIwIiwiQ09SVl9GT1JNQVBBR08iOiIiLCJDT1JWX1RJUE9DVVNUT0RJQSI6IkMiLCJDT1JWX0JBTkNPQ1VTVE9ESU8iOiIiLCJDT1JWX0JBTkNPTElRVUlEQSI6IiIsIkNPUlZfTUVESU9DQVBUQUNJT04iOiJPRCIsIkNPUlZfUkVQUkVTRU5UQU5URSI6IjAiLCJNRU5TQUpFVkFMSURBQ0lPTiI6Ik9LIn1dfQ0K";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoOrdenesRF demo= new CargaArchivoOrdenesRF();
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
