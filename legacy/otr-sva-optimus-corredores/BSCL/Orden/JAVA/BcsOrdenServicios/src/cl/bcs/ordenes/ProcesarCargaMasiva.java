package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.ordenes.CrearOrdenesMasiva.DetalleOrden;
import cl.bcs.ordenes.CrearOrdenesMasiva.EntradaCrearOrdenesMasiva;
import cl.bcs.ordenes.CrearOrdenesMasiva.SalidaCrearOrdenesMasiva;
import cl.bcs.ordenes.ProcesarCargaMasivaInstruccion.CargaInstruccion;
import cl.bcs.ordenes.ProcesarCargaMasivaInstruccion.EntradaProcesarCargaMasivaInstruccion;
import cl.bcs.ordenes.ProcesarCargaMasivaInstruccion.SalidaProcesarCargaMasivaInstruccion;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;



public class ProcesarCargaMasiva implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ProcesarCargaMasiva.class);

	private final String SQL_SELECT_CARGA_MASIVA = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasiva_SelectCargaMasiva.sql");
	private final String SQL_SELECT_CARGA_MASIVA_DETALLE = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasiva_SelectCargaMasivaDetalle.sql");	
	private final String SQL_UPDATE_CARGA_MASIVA = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasiva_UpdateCargaMasiva.sql");
	private final String SQL_UPDATE_CARGA_MASIVA_DETALLE = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasiva_UpdateCargaMasivaDetalle.sql");
	
	
	// ----------------------------------------------------------------------------	
	// CLASES DE ENTRADA
	// ----------------------------------------------------------------------------	
	public static class EntradaProcesarCargaMasiva{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_SimularOperacion;
		public String 		p_UsaReglasNegocio;
		public String 		p_FechaProceso;		
		public List<CargaMasivaProcesar> 	p_CargaMasivaProcesar;
		
		public EntradaProcesarCargaMasiva() {
			this.p_CargaMasivaProcesar = new ArrayList<CargaMasivaProcesar>();
		}
	}
	
	
	public static class CargaMasivaProcesar {		
		public Long 		IdCargaMasiva;
		public String 		CodigoTipoCargaMasiva;
		
		// Para tipo Carga: "CARGA MASIVA DE ORDENES"
		public String 		CanalDistribucion;
		public String 		Negociacion;
		public String 		UsarPlantillaCobros;
		
		
		public String 		FechaOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
	
	// ----------------------------------------------------------------------------	
	// CLASES DE SALIDA
	// ----------------------------------------------------------------------------	
	public static class SalidaProcesarCargaMasiva{
		public Long 					P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> 	P_OUT_MENSAJES;


		public SalidaProcesarCargaMasiva() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	// ----------------------------------------------------------------------------	
	// CLASES COMPLEMENTARIAS
	// ----------------------------------------------------------------------------	


	public static class CargaMasiva {
		public Long 		IdCargaMasiva;
		public String 		FechaProceso;
		public String 		IdEstado;

		
		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
	public static class CargaMasivaDetalle {		

		public Long 		IdCargaMasivaDetalle;
		public Long 		IdCargaMasiva;
		public String 		DocumentoIdentidad;
		public String 		CodigoPortafolio;
		public String 		TipoOperacion;
		public String 		IdMecanismoCaptacion;
		public BigDecimal 	Cantidad;
		public String 		Nemotecnico;
		public BigDecimal 	Precio;
		public String 		IdCondicionDeLiquidacion;
		public String 		IdTipoVigenciaOrden;
		public String 		FechaVigencia;
		public String 		IdFormaCustodia;
		public String 		IdBolsa;
		public String 		IdBoveda;
		public String 		IdTipoPrecioOrden;
		public Long 		IdPersona;
		public Long 		IdPortafolio;
		public String		TipoInstruccion;
		public String		Emisor;
		public String 		FechaVencimiento;
		public BigDecimal	ValorRescate;
		public String		FechaTrade;
		public String		Intermediario;
		public String		FolioFactura;
		public BigDecimal	MontoLiquidacion;
		public BigDecimal	Tasa;
		public String		IdTipoReajuste;
		public BigDecimal	TotalCobros;
		public String		CodigoTipoCargaMasiva;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	// Se usa para obtener la salida del procesamiento de carga masiva y trazabilidad
	public static class ResultadoProceso {
		public Long 		IdInterno;
		public Long 		IdOperacionGenerada;   //Ej: IdOrden
		public String 		Mensaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// ----------------------------------------------------------------------------	
	// CALL
	// ----------------------------------------------------------------------------	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaProcesarCargaMasiva jin = gson.fromJson(in.toString(), EntradaProcesarCargaMasiva.class);
		SalidaProcesarCargaMasiva out = logica(conn,jin);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	// ----------------------------------------------------------------------------	
	// LOGICA
	// ----------------------------------------------------------------------------	
	public SalidaProcesarCargaMasiva logica(Connection con, EntradaProcesarCargaMasiva entrada) throws Exception {
		logger.info("INICIO::ProcesarCargaMasiva");
		SalidaProcesarCargaMasiva out = new SalidaProcesarCargaMasiva();
		
		SQL sql = new SQL();
		HashMap<String, Object> param = new HashMap<>();		
		List<CargaMasiva> lstCargaMasiva = new ArrayList<CargaMasiva>();
		List<CargaMasiva> lstCargaMasivaConErrores = new ArrayList<CargaMasiva>();
		List<ResultadoProceso> lstResultadoProceso = new ArrayList<ResultadoProceso>();
		
		HashMap<Long, Object> hashCargaMasivaProcesar = new HashMap<Long, Object>();
		hashCargaMasivaProcesar = Utils.List2Hash(entrada.p_CargaMasivaProcesar, "IdCargaMasiva");
		
		//---------------------------------------------------------------------------------------------
		//VALIDACIONES
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			logger.debug("NO CUMPLE REQUISITOS BASICOS");
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		

		List<Long> idsCargasMasivas = entrada.p_CargaMasivaProcesar.stream().map(linea -> linea.IdCargaMasiva).collect(Collectors.toList());
		

		param.clear();		
		param.put("IdsCargasMasivas", idsCargasMasivas);
		param.put("FechaProceso", entrada.p_FechaProceso);		
		lstCargaMasiva = sql.query(con, SQL_SELECT_CARGA_MASIVA, param, CargaMasiva.class);
		
		
		
		for (CargaMasiva carga: lstCargaMasiva)	
		{
			ProcesaRequisito(con, entrada.p_IdEmpresa, (carga.IdEstado.equalsIgnoreCase("PRO")),"CARGA_MASIVA_PROCESADA",MensajeSalida.MENSAJE_ERROR,carga, out.P_OUT_MENSAJES);
			ProcesaRequisito(con, entrada.p_IdEmpresa, (carga.IdEstado.equalsIgnoreCase("INV")),"CARGA_MASIVA_INVALIDA",MensajeSalida.MENSAJE_ERROR,carga, out.P_OUT_MENSAJES);
			
		}
	
		limpiarCargasMasiva(lstCargaMasiva,lstCargaMasivaConErrores);
		
		if (lstCargaMasiva.size() == 0) 
		{
			logger.debug("CARGA MASIVA CON ERRORES DE VALIDACION ");
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}

		

	
		List<Long> idsCargasMasivasValidas = lstCargaMasiva.stream().map(linea -> linea.IdCargaMasiva).collect(Collectors.toList());
		
		param.clear();		
		param.put("IdsCargasMasivas", idsCargasMasivasValidas);
		List<CargaMasivaDetalle> lstCargaMasivaDetalle = sql.query(con, SQL_SELECT_CARGA_MASIVA_DETALLE, param, CargaMasivaDetalle.class);
		
		
		// -------------------------------------------------------------
		// PROCESAR CARGA "INGRESO MASIVO DE ORDENES"
		CrearOrdenesMasiva servicioCrearOrdenesMasiva = new CrearOrdenesMasiva();
		EntradaCrearOrdenesMasiva entradaCrearOrdenesMasiva = new EntradaCrearOrdenesMasiva();
		
		
		List<DetalleOrden> lstDetalleOrden = new ArrayList<DetalleOrden>();
		DetalleOrden objDetalleOrden;
		
		List<CargaMasivaDetalle> lstCargaMasivaDetalleOrdenes = lstCargaMasivaDetalle
				.stream()
				.filter(detalle->"CARGA_INGRESO_ORDEN_MASIVO".equals(detalle.CodigoTipoCargaMasiva))
				.collect(Collectors.toList());
		
		List<CargaMasivaDetalle> lstCargaMasivaDetalleInstruccciones = lstCargaMasivaDetalle
				.stream()
				.filter(detalle->"CARGA_INGRESO_INSTRUCCIONES_MASIVA".equals(detalle.CodigoTipoCargaMasiva))
				.collect(Collectors.toList());
		
		for (CargaMasivaDetalle detCar : lstCargaMasivaDetalleOrdenes)
		{
			objDetalleOrden = new DetalleOrden();
			
			CargaMasivaProcesar infoCargaMasivaProcesar =  (CargaMasivaProcesar) hashCargaMasivaProcesar.get(detCar.IdCargaMasiva);
			
			objDetalleOrden.IdInterno = detCar.IdCargaMasivaDetalle;	// Este Id debe salir en la respuesta del servicio para trazabilidad		
			objDetalleOrden.DocumentoIdentidad = detCar.DocumentoIdentidad;
			objDetalleOrden.CodigoPortafolio = detCar.CodigoPortafolio;
			objDetalleOrden.TipoOperacion = detCar.TipoOperacion;
			objDetalleOrden.IdMecanismoCaptacion = detCar.IdMecanismoCaptacion;
			objDetalleOrden.Cantidad = detCar.Cantidad;
			objDetalleOrden.Nemotecnico = detCar.Nemotecnico;
			objDetalleOrden.Precio = detCar.Precio;
			objDetalleOrden.IdCondicionDeLiquidacion = detCar.IdCondicionDeLiquidacion;
			objDetalleOrden.IdTipoVigenciaOrden = detCar.IdTipoVigenciaOrden;
			objDetalleOrden.FechaVigencia = detCar.FechaVigencia;
			objDetalleOrden.IdFormaCustodia = detCar.IdFormaCustodia;			
			objDetalleOrden.IdBolsa = detCar.IdBolsa;
			objDetalleOrden.IdBoveda = detCar.IdBoveda;
			objDetalleOrden.IdTipoPrecioOrden = detCar.IdTipoPrecioOrden;
			objDetalleOrden.IdPersona = detCar.IdPersona;
			objDetalleOrden.IdPortafolio = detCar.IdPortafolio;			
			objDetalleOrden.IdTipoCanalDistribucion = infoCargaMasivaProcesar.CanalDistribucion;
			objDetalleOrden.Negociacion = infoCargaMasivaProcesar.Negociacion;
			objDetalleOrden.UsarPlantillaCobros = infoCargaMasivaProcesar.UsarPlantillaCobros;
			objDetalleOrden.FechaOperacion = infoCargaMasivaProcesar.FechaOperacion;			
			
			lstDetalleOrden.add(objDetalleOrden);
		}

		if (lstDetalleOrden.size() > 0)
		{
			entradaCrearOrdenesMasiva.p_IdEmpresa =  entrada.p_IdEmpresa;
			entradaCrearOrdenesMasiva.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearOrdenesMasiva.p_IdModulo =  entrada.p_IdModulo;
			entradaCrearOrdenesMasiva.p_SimularOperacion = entrada.p_SimularOperacion;
			entradaCrearOrdenesMasiva.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;		
			entradaCrearOrdenesMasiva.p_DetalleOrden = lstDetalleOrden;
			
			SalidaCrearOrdenesMasiva salidaCrearOrdenesMasiva = servicioCrearOrdenesMasiva.logica(con, entradaCrearOrdenesMasiva);
			
			
			out.P_OUT_ERRORESCONTADOR 	= salidaCrearOrdenesMasiva.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES 			= salidaCrearOrdenesMasiva.P_OUT_MENSAJES;
			
			
			if(out.P_OUT_ERRORESCONTADOR == 0) {
				for(ObjRESULTADO resultado : salidaCrearOrdenesMasiva.P_OUT_RESULTADO) {
					String msg=Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_INGRESO_ORDEN").add("IdOrden", resultado.IdOrden).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(resultado.IdOrden, MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_ORDEN", msg));
					
					//Resultado proceso para trazabilidad
					ResultadoProceso objResultadoProceso = new ResultadoProceso();
					objResultadoProceso.IdInterno = resultado.IdInterno;
					objResultadoProceso.IdOperacionGenerada = resultado.IdOrden;
					objResultadoProceso.Mensaje = msg;
					
					lstResultadoProceso.add(objResultadoProceso);
				}
			}

			
		}
		
		List<CargaInstruccion> lstDetalleInstruccion = new ArrayList<>();
		
		for (CargaMasivaDetalle detCar : lstCargaMasivaDetalleInstruccciones)
		{
			CargaInstruccion cargaInstruccion = new CargaInstruccion();
			
			cargaInstruccion.IdInterno = detCar.IdCargaMasivaDetalle;	// Este Id debe salir en la respuesta del servicio para trazabilidad
			cargaInstruccion.TipoInstruccion = detCar.TipoInstruccion;
			cargaInstruccion.Operacion = detCar.TipoOperacion;
			cargaInstruccion.Nemotecnico = detCar.Nemotecnico;
			cargaInstruccion.IdEmisor = detCar.Emisor;
			cargaInstruccion.FechaVencimiento = detCar.FechaVencimiento;
			cargaInstruccion.Tasa = detCar.Tasa;
			cargaInstruccion.ValorRescate = detCar.ValorRescate;
			cargaInstruccion.FechaTrade = detCar.FechaTrade;
			cargaInstruccion.CondicionLiquidacion = detCar.IdCondicionDeLiquidacion;
			cargaInstruccion.Intermediario = Long.valueOf(detCar.Intermediario);
			cargaInstruccion.NumeroFactura = detCar.FolioFactura;
			cargaInstruccion.Cantidad = detCar.Cantidad;
			cargaInstruccion.MontoTotal = detCar.MontoLiquidacion;
			cargaInstruccion.TotalCobros = detCar.TotalCobros;
			cargaInstruccion.IdTipoReajuste = detCar.IdTipoReajuste;
			cargaInstruccion.Precio = detCar.Precio;
			cargaInstruccion.DocumentoIdentidad = detCar.DocumentoIdentidad;
			cargaInstruccion.CodigoPortafolio = detCar.CodigoPortafolio;
						
			lstDetalleInstruccion.add(cargaInstruccion);
		}
		
		if(lstDetalleInstruccion.size() > 0) {
			EntradaProcesarCargaMasivaInstruccion entradaProcesarCargaMasivaInstruccion = new EntradaProcesarCargaMasivaInstruccion();
			entradaProcesarCargaMasivaInstruccion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaProcesarCargaMasivaInstruccion.p_IdUsuario = entrada.p_IdUsuario;
			entradaProcesarCargaMasivaInstruccion.p_IdModulo = entrada.p_IdModulo;
			entradaProcesarCargaMasivaInstruccion.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaProcesarCargaMasivaInstruccion.p_IdTipoCanalIngreso = "AGE";
			entradaProcesarCargaMasivaInstruccion.p_Instrucciones = lstDetalleInstruccion;
			
			ProcesarCargaMasivaInstruccion procesarCargaMasivaInstruccion = new ProcesarCargaMasivaInstruccion();
			SalidaProcesarCargaMasivaInstruccion salidaProcesarCargaMasivaInstruccion = procesarCargaMasivaInstruccion.logica(con, entradaProcesarCargaMasivaInstruccion);
			out.P_OUT_ERRORESCONTADOR = salidaProcesarCargaMasivaInstruccion.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES 	= salidaProcesarCargaMasivaInstruccion.P_OUT_MENSAJES;
			
			if(out.P_OUT_ERRORESCONTADOR == 0) {
				for(Entidad resultado : salidaProcesarCargaMasivaInstruccion.P_OUT_REFID) {
					String msg=Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_INGRESO_INSTRUCCION").add("IdInstruccion", resultado.IdEntidad).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(resultado.IdEntidad, MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_ORDEN", msg));
					
					//Resultado proceso para trazabilidad
					ResultadoProceso objResultadoProceso = new ResultadoProceso();
					objResultadoProceso.IdInterno = resultado.IdInterno;
					objResultadoProceso.IdOperacionGenerada = resultado.IdEntidad;
					objResultadoProceso.Mensaje = msg;
					
					lstResultadoProceso.add(objResultadoProceso);
				}
			}
		}
		
		
		
		// -----------------------------------------------------   
		// ACTUALIZA ESTADOS DE CARGAS MASIVAS PROCESADAS
		// -----------------------------------------------------
		sql.batch(con, SQL_UPDATE_CARGA_MASIVA, lstCargaMasiva, CargaMasiva.class);		
		sql.batch(con, SQL_UPDATE_CARGA_MASIVA_DETALLE, lstResultadoProceso, ResultadoProceso.class);

		logger.info("FIN::ProcesarCargaMasiva");
		return out;
		
	}
	



	
	//Procesa salida de validaciones
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, CargaMasiva carga, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdCargMasiva", carga.IdCargaMasiva)
				    .msg();			
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(carga.IdCargaMasiva,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				carga.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(carga.IdCargaMasiva,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				carga.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(carga.IdCargaMasiva,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				carga.advertencias++;
			}
			else 
			{
				carga.ok++;
			}
		}
		else 
		{
			carga.ok++;
		}		
	}
	
	
	//Remueve las cargas masivas invalidas
	public void limpiarCargasMasiva(List<CargaMasiva> cargaMasiva, List<CargaMasiva> cargaMasivaConErrores) {		
		for (CargaMasiva carga : cargaMasiva) {
			if (carga.errores > 0) {
				cargaMasivaConErrores.add(carga);
			}
		}
		
		for (CargaMasiva cargaErr : cargaMasivaConErrores) {		
			if (cargaMasiva.contains(cargaErr)) {
				cargaMasiva.remove(cargaErr);
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------	
	// METODO PARA PRUEBAS LOCALES
	// ----------------------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("Desa02", "connections.properties")) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"ORD\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":188,\"CodigoTipoCargaMasiva\":\"CARGA_INGRESO_INSTRUCCIONES_MASIVA\",\"CanalDistribucion\":null,\"Negociacion\":\"OFE\",\"FechaOperacion\":\"0001-01-01\",\"UsarPlantillaCobros\":\"S\"}],\"p_FechaProceso\":\"2021-12-13\"}";
		//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"ORD\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaProceso\":\"2020-06-08\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":60, \"CodigoTipoCargaMasiva\":\"CARGA_INGRESO_ORDEN_MASIVO\", \"CanalDistribucion\":\"MAN\", \"Negociacion\":\"CTD\"},{\"IdCargaMasiva\":61, \"CodigoTipoCargaMasiva\":\"CARGA_INGRESO_ORDEN_MASIVO\", \"CanalDistribucion\":\"MAN\", \"Negociacion\":\"POF\"}]}";

		EntradaProcesarCargaMasiva Objin = gson.fromJson(json, EntradaProcesarCargaMasiva.class);
		ProcesarCargaMasiva demo = new ProcesarCargaMasiva();				
		try {
			SalidaProcesarCargaMasiva ObjOut=demo.logica(con, Objin);
			//con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			//con.rollback(); 			
    	}
		System.out.println("== FIN ==");
	}

}
