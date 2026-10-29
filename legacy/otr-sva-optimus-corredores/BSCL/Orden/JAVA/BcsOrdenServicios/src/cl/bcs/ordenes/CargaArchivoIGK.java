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

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES;
import cl.bcs.plataforma.evt.AgregarEvento.ServiceEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
/*import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;*/
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoIGK implements JavaCall{
	
	private final Log log =new Log("CargaArchivoIGK");
	
	private final String SQL_INSERT_CARGA_IGK = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoIGK_InsertCargaArchivoIGK.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoIGK_SelectInstrumentos.sql");
	private final String SQL_SELECT_TRANSACCIONES =  loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoIGK_SelectTransacciones.sql");
	private final String SQL_SELECT_ID_PERSONA = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoIGK_SelectPersonasRutCavali.sql");
	private final String SQL_UPDATE_TRANSACCIONES =  loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoIGK_UpdateTransacciones.sql");
	private final String SQL_SELECT_DATOS_EMPRESA = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoIGK_SelectDatosEmpresa.sql");
	
	
	public static class IGK{
		public Long			IdInterno;
		public String 		CodParticipante;
		public String 		FechaOperacion;
		public String 		Modalidad;
		public String 		CodMoneda;
		public String 		TipoComp;
		public String 		TipoPart;    // Tipo Participacion (1: Compra   2: Venta)
		public String		TipoOper;    // Tipo Operacion (1: Contrado  2: Plazo)
		public String		Nemonico;
		public Long			NroOper;
		public String		CodTitular;
		public BigDecimal	Cantidad;
		public BigDecimal	Precio;
		public String		ParticipanteAfectado;
		public BigDecimal	MontoEfectivo;
		public BigDecimal	IGK;   //Interes de garancia capital (Monto Retención)		
		public String		CuotaCavali;
		public String		IgvCavali;
		public String		EncargadoValores;
		public String		NroEnvio;
		public String		IdEstado;
		public String		Resultado;
		
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
	
	public static class Transacciones{
		public Long				IdMovimientoComprobante;
		public Long 			IdTransaccion;
		public Long 			IdCliente;
		public Long 			IdPortafolio;
		public String 			DocumentoIdentidad;
		public String			AccionOrden;
		public String			UsaCuentaInversion;
		public String 			Folio;
		public String 			Nemotecnico;
		public java.sql.Date 	FechaSistemaCreacion;
		public String 			IdClasificacionTipoInstrumento;
		public String			IdMoneda;
		public java.sql.Date	FechaLiquidacion;
		public Long				IdPersona;
		public BigDecimal       MontoRetencion; 
		public BigDecimal		Cantidad;
		public BigDecimal		Monto;
		public String 			DescripcionMoneda;
		public String			NombreCliente;
		public Long				IdDireccionPrincipal;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Personas{
		public Long IdPersona;
		public String DocumentoIdentidad;
	}
	
	public static class CargoIGK{
		public Long			IdInterno;
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public String 		Folio;
		public Long 		IdMovimientoComprobante;
		public Long			IdTransaccion;
		public Long			IdInstrumento;
		public BigDecimal 	Precio;
		public BigDecimal 	Cantidad;
		public BigDecimal	MontoEfectivo;
		public String		UsaCuentaInversion;
		public String		IdMoneda;
		public String		FechaLiquidacion;
		public Long			IdPersona;		
		public BigDecimal	MontoRetencion;
		public String       MarcaIGK;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class MarcaIGK
	{	
		public Long			IdTransaccion;				
		public BigDecimal	MontoRetencion;   //Monto total retención por transacción
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Empresa{
		public String NombreEmpresa;
		public String CiudadEmpresa;
		public String DistritoEmpresa;
		public String PaisEmpresa;
		public String DireccionEmpresa;
	}
	
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCargaArchivoIGK{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoIGK{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoIGK in=gson.fromJson(jin.toString() ,InCargaArchivoIGK.class);
		OutCargaArchivoIGK out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoIGK logica(Connection con, InCargaArchivoIGK entrada) throws Exception {
		OutCargaArchivoIGK out = new OutCargaArchivoIGK();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_NombreArchivoLog="ord/CARGA_ARCHIVO_IGK_"+fecha+".log";
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
			SimpleDateFormat formatoArchivo = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat formatoSP = new SimpleDateFormat("yyyy-MM-dd");
			JsonParser parser     = new JsonParser();
	        JsonObject dataJsonIn  = parser.parse(archivoDecoded).getAsJsonObject();
			
			List<IGK> operaciones = new ArrayList<IGK>();
			
			JsonArray registros= dataJsonIn.getAsJsonArray("p_contenido");
			Long i = 0L;
			List<String> nemotecnicos = new ArrayList<String>();
			List<String> folioOperaciones = new ArrayList<String>();
			List<String> rutCavalis = new ArrayList<String>();
			

			// ---------------------------------------------------------------------------------------------
			// RECORRE REGISTROS DEL ARCHIVO Y METE DATA EN LISTA ("operaciones") PARA SU PROCESAMIENTO
			
			for(JsonElement registro : registros){
				numeroRegistros++;
				IGK operacion = new IGK();
				JsonObject elemento = registro.getAsJsonObject();
				String validacion = elemento.get("MENSAJEVALIDACION").getAsString();
				if("OK".equalsIgnoreCase(validacion)){
					try{
						Date fechaOperacion = formatoArchivo.parse(elemento.get("CIGK_FECHAOPERACION").getAsString());
						operacion.CodParticipante = elemento.get("CIGK_CODPARTICIPANTE").getAsString();
						operacion.FechaOperacion = formatoSP.format(fechaOperacion);
						operacion.Modalidad = elemento.get("CIGK_MODALIDAD").getAsString();
						operacion.CodMoneda = elemento.get("CIGK_CODMONEDA").getAsString();
						operacion.TipoComp = elemento.get("CIGK_TIPOCOMP").getAsString();						
						operacion.TipoPart = elemento.get("CIGK_TIPOPART").getAsString();	// Tipo Participacion (1: Compra   2: Venta)
						operacion.TipoOper = elemento.get("CIGK_TIPOOPER").getAsString();	// Tipo Operacion (1: Contrado  2: Plazo)						
						operacion.Nemonico = elemento.get("CIGK_NEMONICO").getAsString().trim();
						operacion.NroOper = elemento.get("CIGK_NROOPER").getAsLong();
						operacion.CodTitular = elemento.get("CIGK_CODTITULAR").getAsString();
						operacion.Cantidad = elemento.get("CIGK_CANTIDAD").getAsBigDecimal();
						operacion.Precio = elemento.get("CIGK_PRECIO").getAsBigDecimal();
						operacion.ParticipanteAfectado = elemento.get("CIGK_PARTICIPANTEAFECTADO").getAsString();
						operacion.MontoEfectivo = elemento.get("CIGK_MONTOEFECTIVO").getAsBigDecimal();
						operacion.IGK = elemento.get("CIGK_IGK").getAsBigDecimal();
						operacion.CuotaCavali = elemento.get("CIGK_CUOTACAVALI").getAsString();
						operacion.IgvCavali = elemento.get("CIGK_IGVCAVALI").getAsString();
						operacion.EncargadoValores = elemento.get("CIGK_ENCARGADOVALORES").getAsString();
						operacion.NroEnvio = elemento.get("CIGK_NROENVIO").getAsString();
						operacion.IdInterno = i;
						operacion.IdEstado = "PEN";
						operacion.Resultado = "";
						i++;
						if(!nemotecnicos.contains(operacion.Nemonico)) nemotecnicos.add(operacion.Nemonico);
						if(!folioOperaciones.contains(String.valueOf(operacion.NroOper))) folioOperaciones.add(String.valueOf(operacion.NroOper));
						if(!rutCavalis.contains(operacion.CodTitular)) rutCavalis.add(operacion.CodTitular);
						operaciones.add(operacion);
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
			
			
			
			// ---------------------------------------------------------------------------------------------
			// BUSCA INFORMACION RELACIONADA AL REGISTRO
			
			HashMap<String,Object> params = new HashMap<>();
			params.put("Nemotecnicos", nemotecnicos);
			List<Instrumentos> idInstrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumentos.class);
			
			params.clear();
			params.put("foliosTransaccion", folioOperaciones);
			List<Transacciones> transacciones = sql.query(con, SQL_SELECT_TRANSACCIONES, params, Transacciones.class);
			
			params.clear();
			params.put("DocumentoIdentidad", rutCavalis);
			List<Personas> personas = sql.query(con, SQL_SELECT_ID_PERSONA, params, Personas.class);
			
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<Empresa> empresas = sql.query(con, SQL_SELECT_DATOS_EMPRESA, params, Empresa.class);
			
			
			
			// ---------------------------------------------------------------------------------------------
			// PREPARA DATA PARA GENERAR CARGOS Y ACTUALIZAR TRANSACIONES
			
			List<CargoIGK> cargosIGK = new ArrayList<CargoIGK>();			
			
			
			for(IGK operacion : operaciones){
				try{
					CargoIGK cargoIGK = new CargoIGK();
					cargoIGK.IdInterno = operacion.IdInterno;
					cargoIGK.Cantidad = operacion.Cantidad;
					cargoIGK.Precio = operacion.Precio;
					cargoIGK.MontoEfectivo = operacion.MontoEfectivo;					
					if(cargoIGK.MontoEfectivo.compareTo(BigDecimal.ZERO) == 0) throw new Exception("Monto cero no se incluira en el cargo, en operacion::" + operacion.NroOper);
					cargoIGK.Folio = String.valueOf(operacion.NroOper);
					for(Instrumentos instrumento : idInstrumentos){
						if(instrumento.Nemotecnico.equalsIgnoreCase(operacion.Nemonico)){
							cargoIGK.IdInstrumento = instrumento.IdInstrumento;
						}
					}
					for(Personas persona : personas){
						if(persona.DocumentoIdentidad.equalsIgnoreCase(operacion.CodTitular)){
							cargoIGK.IdPersona = persona.IdPersona;
						}
					}
					if(cargoIGK.IdPersona == null) throw new Exception("Persona con RUT Cavali::" + operacion.CodTitular + ", no existe");
					if(cargoIGK.IdInstrumento == null) throw new Exception("Instrumento:" + operacion.Nemonico + ",no existe");
					
					String tipoParticipacion = "01".equalsIgnoreCase(operacion.TipoPart) ? "C" : "02".equalsIgnoreCase(operacion.TipoPart) ? "V" : null;

					
					
					for(Transacciones transaccion : transacciones)
					{
						if (transaccion.Folio.equalsIgnoreCase(String.valueOf(operacion.NroOper)) && Fecha.toStringDate(transaccion.FechaSistemaCreacion).equals(operacion.FechaOperacion) 
								&& transaccion.Nemotecnico.equalsIgnoreCase(operacion.Nemonico) && cargoIGK.IdPersona.longValue() == transaccion.IdPersona.longValue()){
							if (("COMPRA".equalsIgnoreCase(transaccion.AccionOrden) && "C".equalsIgnoreCase(tipoParticipacion)) || 
									("VENTA".equalsIgnoreCase(transaccion.AccionOrden) && "V".equalsIgnoreCase(tipoParticipacion))) {
								cargoIGK.IdCliente = transaccion.IdCliente;
								cargoIGK.IdPortafolio = transaccion.IdPortafolio;
								cargoIGK.IdMovimientoComprobante = transaccion.IdMovimientoComprobante;
								cargoIGK.UsaCuentaInversion = transaccion.UsaCuentaInversion;
								cargoIGK.IdTransaccion = transaccion.IdTransaccion;
								cargoIGK.IdMoneda = transaccion.IdMoneda;
								cargoIGK.FechaLiquidacion = Fecha.toStringDate(transaccion.FechaLiquidacion);								
								cargoIGK.MarcaIGK = "S";
								cargoIGK.MontoRetencion = operacion.IGK;  // Monto retención de ventas
							}
						}
					}
					if(cargoIGK.IdTransaccion == null) throw new Exception("Transaccion:" + operacion.NroOper + ",no existe");
					
					
					cargosIGK.add(cargoIGK);
					
					
				}catch(Exception e){
					numeroRegistrosErroneos++;
					operacion.Resultado = e.getMessage();
					operacion.IdEstado = "ERR";
					bwArchivoLog.write("Registro con Error:" + e.getMessage());
					bwArchivoLog.newLine();
				}
			}
			
			
			
			// ---------------------------------------------------------------------------------------------
			// ACTUALIZA TRANSACCIONES (MarcaIGK y MontoRetencion) PARA SU LIQUIDACION MULTILATERAL(Clearing Financiero)
			 
			List<MarcaIGK> marcaIGK = new ArrayList<MarcaIGK>();
						
			for (CargoIGK _cargosIGK : cargosIGK)
			{	
				if (_cargosIGK.MarcaIGK.equalsIgnoreCase("S"))
				{
					Boolean nuevoElemento = true;
					for (MarcaIGK _marcaIGK : marcaIGK)
					{	
						if (_cargosIGK.IdTransaccion.compareTo(_marcaIGK.IdTransaccion) == 0)
						{
							_marcaIGK.MontoRetencion = _marcaIGK.MontoRetencion.add(_cargosIGK.MontoRetencion);
							nuevoElemento = false;
						}
					}
					
					if (nuevoElemento)
					{
						MarcaIGK objMarcaIGK = new MarcaIGK();
						Utils.copyPojo(_cargosIGK, objMarcaIGK);
						marcaIGK.add(objMarcaIGK);
					}
					
				}
			}
			
			if(marcaIGK.size() > 0)
			{
				sql.batch(con, SQL_UPDATE_TRANSACCIONES, marcaIGK, MarcaIGK.class);
			}
			
			
			

			
			// ---------------------------------------------------------------------------------------------
			// GENERA "CARGOS" EN LA CUENTA INVERSION
			
			//List<IngresoEgreso_Entrada> ingresos = new ArrayList<IngresoEgreso_Entrada>();
			//List<FormaDePago_Entrada> formasPago = new ArrayList<FormaDePago_Entrada>();
			List<ObjCargoAbonoAGenerar> cargosGenerar = new ArrayList<ObjCargoAbonoAGenerar>();
			for(CargoIGK cargo : cargosIGK){
			
				/*if("N".equalsIgnoreCase(cargo.UsaCuentaInversion)){
					IngresoEgreso_Entrada ingreso = new IngresoEgreso_Entrada();
					ingreso.NumeroReferencia = cargo.IdInterno;
					ingreso.IdTipoMovimiento = "MOVING";
					ingreso.IdTipoPersona = "CLI";
					ingreso.IdPersona = cargo.IdPersona;
					ingreso.IdPortafolio = cargo.IdPortafolio;
					ingreso.IdMoneda = cargo.IdMoneda;
					ingreso.FechaLiquidacion = cargo.FechaLiquidacion;
					ingreso.IdConcepto = "CARIGK";
					ingreso.IdTipoPersonaTitularPago = "CLI";
					ingreso.IdPersonaTitularPago = cargo.IdPersona;
					ingreso.IdPortafolioTitularPago = cargo.IdPortafolio;
					ingreso.IdCaja = 0L;
					ingreso.Monto = cargo.MontoEfectivo;
					ingreso.Glosa = "Abono por IGK Folio:" + cargo.Folio;//TODO:Dejar dinamico para sacarlo del diccionario
					ingreso.TitularPago = "N";
					ingreso.IdComprobante = 0L;
					ingreso.IdTipoComprobante = "";
					ingreso.IdTipoProducto = "";
					ingreso.IdTipoOperacion = "";
					ingreso.IdBolsa = "";
					ingreso.IdMonedaOperacion = "";
					ingreso.MontoOperacion = BigDecimal.ZERO;
					ingreso.IdTipoReajuste = "";
					ingreso.ValorParidad = BigDecimal.ZERO;
					ingreso.FolioCartera = 0L;
					ingreso.IdInstrumento = 0L;
					ingreso.IdTipoInstrumento = "";
					ingreso.IdClasificacionTipoInstrumento = "";
					ingreso.MueveCuentaInversion = "";
					ingresos.add(ingreso);
					FormaDePago_Entrada formaPago = new FormaDePago_Entrada();
					formaPago.NumeroReferencia = cargo.IdInterno;
					formaPago.Monto = cargo.MontoEfectivo;
					formaPago.FechaDocumentoPagoCorredora = null;
					formaPago.FechaDocumentoPagoSocio = null;
					formaPago.IdMoneda = cargo.IdMoneda;
					formaPago.IdMedioDePago = "CIN";
					formaPago.IdCuentaBancariaSocio = 0L;
					formaPago.IdCuentaBancariaCorredora = 0L;
					formaPago.IdTarjetaBancariaCorredora = 0L;
					formaPago.IdTarjetaBancariaSocio = 0L;
					formaPago.IdChequera = 0L;
					formaPago.NumeroDocumentoCorredora = 0L;
					formaPago.NumeroSerieSocio = "";
					formaPago.NumeroDocumentoSocio = 0L;
					formasPago.add(formaPago);
				}else{*/
				
					ObjCargoAbonoAGenerar cargoGenerar = new ObjCargoAbonoAGenerar();
					cargoGenerar.NumeroCargoAbono = cargo.IdInterno;
					cargoGenerar.IdPortafolio = cargo.IdPortafolio;
					cargoGenerar.IdMoneda = cargo.IdMoneda;
					cargoGenerar.IdTipoMovimiento = "MOVCAR";
					cargoGenerar.IdConcepto = "CARIGK";
					cargoGenerar.Descripcion =  "Abono por IGK Folio:" + cargo.Folio;//TODO:Dejar dinamico para sacarlo del diccionario
					cargoGenerar.IdCondicionDeLiquidacion = "T0";
					cargoGenerar.FechaCondicionDeLiquidacion = cargo.FechaLiquidacion;
					cargoGenerar.Monto = cargo.MontoEfectivo;
					cargoGenerar.FechaLiquidacionOperacion = cargo.FechaLiquidacion;
					
					cargosGenerar.add(cargoGenerar);
				
				//}
				
			}
			/*
			if(ingresos.size() > 0){
				CrearIngresoEgreso_Entrada crearIngresoEgresoEntrada = new CrearIngresoEgreso_Entrada();
				crearIngresoEgresoEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				crearIngresoEgresoEntrada.p_IdUsuario = entrada.p_IdUsuario;
				crearIngresoEgresoEntrada.p_IdModulo = entrada.p_IdModulo;
				crearIngresoEgresoEntrada.p_SimularOperacion = "N";
				crearIngresoEgresoEntrada.p_UsaReglasNegocio = "S";
				crearIngresoEgresoEntrada.p_EsIngresoAutomatico = "S";
				crearIngresoEgresoEntrada.p_IngresoEgresoGenerar = ingresos;
				crearIngresoEgresoEntrada.p_FormaDePago = formasPago;
				
				CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
				
				try{
					CrearIngresoEgreso_Salida crearIngresoEgresoSalida = crearIngresoEgreso.logica(con, crearIngresoEgresoEntrada);
					
					List<MensajeSalida> mensajesSalida = crearIngresoEgresoSalida.P_OUT_MENSAJES;
					List<IngresoEgresoGenerado_Salida> resultados = crearIngresoEgresoSalida.P_OUT_INGRESOEGRESOGENERADO;
					
					for(MensajeSalida mensajeSalida : mensajesSalida){
						if("ERROR".equalsIgnoreCase(mensajeSalida.tipo)){
							for(IGK operacion : operaciones){
								if(operacion.IdInterno.longValue() == mensajeSalida.id.longValue() && "PEN".equalsIgnoreCase(operacion.IdEstado)){
									numeroRegistrosErroneos++;
									bwArchivoLog.write(mensajeSalida.descripcion);
									bwArchivoLog.newLine();
									operacion.IdEstado = "ERR";
									operacion.Resultado = mensajeSalida.descripcion;
								}
							}
						}
					}
					
					for(IngresoEgresoGenerado_Salida resultado : resultados){
						for(CargoIGK cargo : cargosIGK){
							if(cargo.IdInterno.longValue() == resultado.NumeroReferencia.longValue()){
								ObjCargoAbonoAGenerar cargoGenerar = new ObjCargoAbonoAGenerar();
								cargoGenerar.NumeroCargoAbono = cargo.IdInterno;
								cargoGenerar.IdPortafolio = cargo.IdPortafolio;
								cargoGenerar.IdMoneda = cargo.IdMoneda;
								cargoGenerar.IdTipoMovimiento = "MOVCAR";
								cargoGenerar.IdConcepto = "CARIGK";
								cargoGenerar.Descripcion =  "Abono por IGK Folio:" + cargo.Folio;//TODO:Dejar dinamico para sacarlo del diccionario
								cargoGenerar.IdCondicionDeLiquidacion = "T0";
								cargoGenerar.FechaCondicionDeLiquidacion = cargo.FechaLiquidacion;
								cargoGenerar.Monto = cargo.MontoEfectivo;
								cargoGenerar.FechaLiquidacionOperacion = cargo.FechaLiquidacion;
								
								cargosGenerar.add(cargoGenerar);
							}
						}
					}
				}catch(Exception e){
					for(IGK operacion : operaciones){
						if("PEN".equalsIgnoreCase(operacion.IdEstado)){
							for(CargoIGK cargo : cargosIGK){
								if(cargo.IdInterno.longValue() == operacion.IdInterno.longValue() && "S".equalsIgnoreCase(cargo.UsaCuentaInversion)){
									operacion.IdEstado = "ERR";
									operacion.Resultado = "Error servicio ingreso egreso";
									numeroRegistrosErroneos++;
								}
							}
						}
					}
					bwArchivoLog.write("Error servicio ingreso egreso:" + e.getMessage());
					bwArchivoLog.newLine();
					con.rollback();
				}
				
				
			}
			*/
			
			if(cargosGenerar.size() > 0){
				CrearCargoAbonoEntrada crearCargoAbonoEntrada = new CrearCargoAbonoEntrada();
				crearCargoAbonoEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				crearCargoAbonoEntrada.p_IdModulo = entrada.p_IdModulo;
				crearCargoAbonoEntrada.p_IdUsuario = entrada.p_IdUsuario;
				crearCargoAbonoEntrada.p_simularOperacion = "N";
				crearCargoAbonoEntrada.p_UsaReglasNegocio = "S";
				crearCargoAbonoEntrada.p_EsIngresoManual = "N";
				crearCargoAbonoEntrada.p_FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				crearCargoAbonoEntrada.p_CargoAbonoAGenerar = cargosGenerar;
				
				CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
				
				try{
					CrearCargoAbonoSalida crearCargoAbonoSalida = crearCargoAbono.logica(con, crearCargoAbonoEntrada);
					
					List<MensajeSalida> mensajesSalida = crearCargoAbonoSalida.P_OUT_MENSAJES;
					List<ObjCargoAbonoGenerado> resultados = crearCargoAbonoSalida.P_OUT_CARGOABONOGENERADO;
					
					for(MensajeSalida mensajeSalida : mensajesSalida){
						if("ERROR".equalsIgnoreCase(mensajeSalida.tipo)){
							for(IGK operacion : operaciones){
								if(operacion.IdInterno.longValue() == mensajeSalida.id.longValue() && "PEN".equalsIgnoreCase(operacion.IdEstado)){
									numeroRegistrosErroneos++;
									bwArchivoLog.write(mensajeSalida.descripcion);
									bwArchivoLog.newLine();
									operacion.IdEstado = "ERR";
									operacion.Resultado = mensajeSalida.descripcion;
								}
							}
						}
					}
					
					for(ObjCargoAbonoGenerado resultado : resultados){
						for(IGK operacion : operaciones){
							if(operacion.IdInterno.longValue() == resultado.NumeroCargoAbono.longValue()){
								numeroRegistrosCorrecto++;
								operacion.IdEstado = "OK";
								operacion.Resultado = "OK";
							}
						}
					}
				}catch(Exception e){
					for(IGK operacion : operaciones){
						if("PEN".equalsIgnoreCase(operacion.IdEstado)){
							operacion.IdEstado = "ERR";
							operacion.Resultado = "Error servicio cargo abono";
							numeroRegistrosErroneos++;
						}
					}
					bwArchivoLog.write("Error servicio cargo abono:" + e.getMessage());
					bwArchivoLog.newLine();
					con.rollback();
				}
			}
			
			
			
			for(IGK operacion : operaciones){
				if("PEN".equalsIgnoreCase(operacion.IdEstado)) operacion.IdEstado = "ERR";
			}
			
			sql.batch(con, SQL_INSERT_CARGA_IGK, operaciones, IGK.class);
			
			ServiceEntrada eventoEntrada = new ServiceEntrada();
			eventoEntrada.P_IDEMPRESA = entrada.p_IdEmpresa;
			eventoEntrada.P_IDMODULO = entrada.p_IdModulo;
			eventoEntrada.P_IDUSUARIO = entrada.p_IdUsuario;
			Empresa empresa = empresas.get(0);
			for(IGK operacion : operaciones){
				if("OK".equalsIgnoreCase(operacion.IdEstado)){
					ObjEVENTOS evento = new ObjEVENTOS();
					evento.IdSecuencia = operacion.IdInterno;
					evento.CodigoTipoEvento = "CARGO_IGK";
					evento.FechaCreacionSistema = operacion.FechaOperacion;
					evento.IdPersonaRemitente = 0L;
					String nombreCompleto = "";
					String documentoIdentidad = "";
					BigDecimal cantidad = BigDecimal.ZERO;
					BigDecimal monto = BigDecimal.ZERO;
					String descripcionMoneda = "";
					Long idDireccionElectronicaPrincipal = 0L;
					String idMoneda = "";
					 
					String tipoParticipacion = "01".equalsIgnoreCase(operacion.TipoPart) ? "C" : "02".equalsIgnoreCase(operacion.TipoPart) ? "V" : null;
					
					for(Personas persona : personas){
						if(persona.DocumentoIdentidad.equalsIgnoreCase(operacion.CodTitular)){
							evento.IdPersonaDestinatario = persona.IdPersona;
						}
					}
					for(Transacciones transaccion : transacciones)
					{
						if (transaccion.Folio.equalsIgnoreCase(String.valueOf(operacion.NroOper)) && Fecha.toStringDate(transaccion.FechaSistemaCreacion).equals(operacion.FechaOperacion) 
								&& transaccion.Nemotecnico.equalsIgnoreCase(operacion.Nemonico) && evento.IdPersonaDestinatario.longValue() == transaccion.IdPersona.longValue()){
							if (("COMPRA".equalsIgnoreCase(transaccion.AccionOrden) && "C".equalsIgnoreCase(tipoParticipacion)) || 
									("VENTA".equalsIgnoreCase(transaccion.AccionOrden) && "V".equalsIgnoreCase(tipoParticipacion))) {
								evento.IdPortafolio = transaccion.IdPortafolio;
								cantidad = transaccion.Cantidad;
								monto = transaccion.Monto;
								descripcionMoneda = transaccion.DescripcionMoneda;
								nombreCompleto = transaccion.NombreCliente;
								documentoIdentidad = transaccion.DocumentoIdentidad;
								idDireccionElectronicaPrincipal = transaccion.IdDireccionPrincipal;
								idMoneda = transaccion.IdMoneda;
							}
						}
					}
					
					eventoEntrada.P_EVENTOS.add(evento);
					eventoEntrada.addValor(operacion.IdInterno, "DIRECCION_EMPRESA", empresa.DireccionEmpresa)
								 .addValor(operacion.IdInterno, "CIUDAD_EMPRESA", empresa.CiudadEmpresa)
								 .addValor(operacion.IdInterno, "DISTRITO_EMPRESA", empresa.DistritoEmpresa)
								 .addValor(operacion.IdInterno, "PAIS_EMPRESA", empresa.PaisEmpresa)
								 .addValor(operacion.IdInterno, "FECHA", operacion.FechaOperacion)
								 .addValor(operacion.IdInterno, "NOMBRE_CLIENTE", nombreCompleto)
								 .addValor(operacion.IdInterno, "DOCUMENTO_IDENTIDAD", documentoIdentidad)
								 .addValor(operacion.IdInterno, "MONTO_IGK", MonedaFormato.formatear(con, operacion.MontoEfectivo, "MONTO", idMoneda, null, null).valorConFormato)
								 .addValor(operacion.IdInterno, "DESCRIPCION_MONEDA", descripcionMoneda)
								 .addValor(operacion.IdInterno, "FOLIO_POLIZA", operacion.NroOper+"")
								 .addValor(operacion.IdInterno, "FECHA_POLIZA", operacion.FechaOperacion)
								 .addValor(operacion.IdInterno, "CANTIDAD", MonedaFormato.formatear(con, cantidad, "CANTIDAD", idMoneda, null, null).valorConFormato)
								 .addValor(operacion.IdInterno, "NEMOTECNICO", operacion.Nemonico)
								 .addValor(operacion.IdInterno, "MONEDA", operacion.CodMoneda)
								 .addValor(operacion.IdInterno, "MONTO", MonedaFormato.formatear(con, monto, "MONTO", idMoneda, null, null).valorConFormato)
								 .addValor(operacion.IdInterno, "DESCRIPCION_MONEDA", descripcionMoneda)
								 .addValor(operacion.IdInterno, "NOMBRE_EMPRESA", empresa.NombreEmpresa);
					
					ObjIDDIRECCIONES dir = new ObjIDDIRECCIONES();
					dir.IdDireccion = idDireccionElectronicaPrincipal;
					dir.IdSecuencia = operacion.IdInterno;
					dir.TipoDestinatario = "TO";
					eventoEntrada.P_IDDIRECCIONES.add(dir);
				}
			}
			
			if (eventoEntrada.P_EVENTOS.size() > 0) {
			    AgregarEvento notif = new AgregarEvento();
			    notif.logica(con, eventoEntrada);
			}
			
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
		/*********		
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";
 		**********/        
        
        
        String dbURL  = "jdbc:sap://161.238.34.163:30015/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "desarrollo";
        String pass   = "Bolsa123";
        
        
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
  			////Connection con = ds.getConnection();
  			Connection con = PlataformaUtils.getMBPEConnection();
  			
  			try{
  				
  				InCargaArchivoIGK entrada= new InCargaArchivoIGK();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				///entrada.p_Archivo		= "eyJwX2NvbnRlbmlkbyI6W3siQ0lHS19DT0RQQVJUSUNJUEFOVEUiOiIxNjg5NTA0NS00IiwiQ0lHS19GRUNIQU9QRVJBQ0lPTiI6IjIwMTcxMDIzIiwiQ0lHS19NT0RBTElEQUQiOiIwMSIsIkNJR0tfQ09ETU9ORURBIjoiMDEiLCJDSUdLX1RJUE9DT01QIjoiMDEiLCJDSUdLX1RJUE9QQVJUIjoiMDEiLCJDSUdLX1RJUE9PUEVSIjoiMDEiLCJDSUdLX05FTU9OSUNPIjoiQ09QRUMgICIsIkNJR0tfTlJPT1BFUiI6MTcsIkNJR0tfQ09EVElUVUxBUiI6MTE3MS4zOCwiQ0lHS19DQU5USURBRCI6NTksIkNJR0tfUFJFQ0lPIjo3ODUwMDAwLCJDSUdLX1BBUlRJQ0lQQU5URUFGRUNUQURPIjoiMDAwMSIsIkNJR0tfTU9OVE9FRkVDVElWTyI6NDYzMTUsIkNJR0tfSUdLIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0NVT1RBQ0FWQUxJIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0lHVkNBVkFMSSI6IjAwMDAwMDAwMDAwMDAwIiwiQ0lHS19FTkNBUkdBRE9WQUxPUkVTIjoiMDAwMCIsIkNJR0tfTlJPRU5WSU8iOiIwMSIsIk1FTlNBSkVWQUxJREFDSU9OIjoiT0sifSx7IkNJR0tfQ09EUEFSVElDSVBBTlRFIjoiMTY4OTUwNDUtNCIsIkNJR0tfRkVDSEFPUEVSQUNJT04iOiIyMDE3MTAyMyIsIkNJR0tfTU9EQUxJREFEIjoiMDEiLCJDSUdLX0NPRE1PTkVEQSI6IjAxIiwiQ0lHS19USVBPQ09NUCI6IjAxIiwiQ0lHS19USVBPUEFSVCI6IjAxIiwiQ0lHS19USVBPT1BFUiI6IjAxIiwiQ0lHS19ORU1PTklDTyI6IkNPUEVDICAiLCJDSUdLX05ST09QRVIiOjIwLCJDSUdLX0NPRFRJVFVMQVIiOjExNzEuMzgsIkNJR0tfQ0FOVElEQUQiOjYsIkNJR0tfUFJFQ0lPIjo3ODUwMDAwLCJDSUdLX1BBUlRJQ0lQQU5URUFGRUNUQURPIjoiMDAwMSIsIkNJR0tfTU9OVE9FRkVDVElWTyI6NDcxMCwiQ0lHS19JR0siOiIwMDAwMDAwMDAwMDAwMCIsIkNJR0tfQ1VPVEFDQVZBTEkiOiIwMDAwMDAwMDAwMDAwMCIsIkNJR0tfSUdWQ0FWQUxJIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0VOQ0FSR0FET1ZBTE9SRVMiOiIwMDAwIiwiQ0lHS19OUk9FTlZJTyI6IjAxIiwiTUVOU0FKRVZBTElEQUNJT04iOiJPSyJ9LHsiQ0lHS19DT0RQQVJUSUNJUEFOVEUiOiIxNjg5NTA0NS00IiwiQ0lHS19GRUNIQU9QRVJBQ0lPTiI6IjIwMTcxMDIzIiwiQ0lHS19NT0RBTElEQUQiOiIwMSIsIkNJR0tfQ09ETU9ORURBIjoiMDEiLCJDSUdLX1RJUE9DT01QIjoiMDEiLCJDSUdLX1RJUE9QQVJUIjoiMDEiLCJDSUdLX1RJUE9PUEVSIjoiMDEiLCJDSUdLX05FTU9OSUNPIjoiQ09QRUMgICIsIkNJR0tfTlJPT1BFUiI6NTEsIkNJR0tfQ09EVElUVUxBUiI6MTE3MS4zOCwiQ0lHS19DQU5USURBRCI6MzUsIkNJR0tfUFJFQ0lPIjo3ODUwMDAwLCJDSUdLX1BBUlRJQ0lQQU5URUFGRUNUQURPIjoiMDAwMSIsIkNJR0tfTU9OVE9FRkVDVElWTyI6Mjc0NzUsIkNJR0tfSUdLIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0NVT1RBQ0FWQUxJIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0lHVkNBVkFMSSI6IjAwMDAwMDAwMDAwMDAwIiwiQ0lHS19FTkNBUkdBRE9WQUxPUkVTIjoiMDAwMCIsIkNJR0tfTlJPRU5WSU8iOiIwMSIsIk1FTlNBSkVWQUxJREFDSU9OIjoiT0sifV19";
  				entrada.p_Archivo		= "eyJwX2NvbnRlbmlkbyI6W3siQ0lHS19DT0RQQVJUSUNJUEFOVEUiOiIwMDAxIiwiQ0lHS19GRUNIQU9QRVJBQ0lPTiI6IjIwMTgwNDIzIiwiQ0lHS19NT0RBTElEQUQiOiIwMSIsIkNJR0tfQ09ETU9ORURBIjoiMDEiLCJDSUdLX1RJUE9DT01QIjoiMDEiLCJDSUdLX1RJUE9QQVJUIjoiMDIiLCJDSUdLX1RJUE9PUEVSIjoiMDEiLCJDSUdLX05FTU9OSUNPIjoiQ09DRVNVQzEgICIsIkNJR0tfTlJPT1BFUiI6MjMwNCwiQ0lHS19DT0RUSVRVTEFSIjoiMDAzODQxOTkiLCJDSUdLX0NBTlRJREFEIjo0Mi4wMywiQ0lHS19QUkVDSU8iOjcyNTAwMDAsIkNJR0tfUEFSVElDSVBBTlRFQUZFQ1RBRE8iOiIwMDAxIiwiQ0lHS19NT05UT0VGRUNUSVZPIjozMDQ3MS43NSwiQ0lHS19JR0siOjE1MjMuNTgsIkNJR0tfQ1VPVEFDQVZBTEkiOiIwMDAwMDAwMDAwMDAwMCIsIkNJR0tfSUdWQ0FWQUxJIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0VOQ0FSR0FET1ZBTE9SRVMiOiIwMDAwIiwiQ0lHS19OUk9FTlZJTyI6IjAxIiwiTUVOU0FKRVZBTElEQUNJT04iOiJPSyJ9LHsiQ0lHS19DT0RQQVJUSUNJUEFOVEUiOiIwMDAxIiwiQ0lHS19GRUNIQU9QRVJBQ0lPTiI6IjIwMTgwNDIzIiwiQ0lHS19NT0RBTElEQUQiOiIwMSIsIkNJR0tfQ09ETU9ORURBIjoiMDEiLCJDSUdLX1RJUE9DT01QIjoiMDEiLCJDSUdLX1RJUE9QQVJUIjoiMDIiLCJDSUdLX1RJUE9PUEVSIjoiMDEiLCJDSUdLX05FTU9OSUNPIjoiQ09DRVNVQzEgICIsIkNJR0tfTlJPT1BFUiI6MjMwNCwiQ0lHS19DT0RUSVRVTEFSIjoiMDEyNTQ5NTgiLCJDSUdLX0NBTlRJREFEIjoxNy4xNSwiQ0lHS19QUkVDSU8iOjcyNTAwMDAsIkNJR0tfUEFSVElDSVBBTlRFQUZFQ1RBRE8iOiIwMDAxIiwiQ0lHS19NT05UT0VGRUNUSVZPIjoxMjQzMy43NSwiQ0lHS19JR0siOjU0NC41MSwiQ0lHS19DVU9UQUNBVkFMSSI6IjAwMDAwMDAwMDAwMDAwIiwiQ0lHS19JR1ZDQVZBTEkiOiIwMDAwMDAwMDAwMDAwMCIsIkNJR0tfRU5DQVJHQURPVkFMT1JFUyI6IjAwMDAiLCJDSUdLX05ST0VOVklPIjoiMDEiLCJNRU5TQUpFVkFMSURBQ0lPTiI6Ik9LIn1dfQ==";   
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoIGK demo= new CargaArchivoIGK();
  				System.out.println(demo.call(con, dataJsonIn).toString());
  				///con.commit();
  				
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
