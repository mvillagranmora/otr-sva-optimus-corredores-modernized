package cl.bcs.cuentaInversion.webservices;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearCargoAbonoWS implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(CrearCargoAbonoWS.class);

	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_CLIENTE_PORTAFOLIO = loadResourceToString("cl/bcs/cuentaInversion/webservices/sql/CrearCargoAbonoWS_SelectClientePortafolio.sql");
	
	
	// ------------------------------------------------------------
	// CLASES DE ENTRADA
	// ------------------------------------------------------------

	public static class EntradaCrearCargoAbonoWS{
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public List<CargoAbonoAGenerar>	p_CargoAbonoAGenerar;
		public String 					p_SimularOperacion;
		
		public EntradaCrearCargoAbonoWS (){			
			this.p_CargoAbonoAGenerar	= new ArrayList<CargoAbonoAGenerar>();
			this.p_SimularOperacion = "N";
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	


	public static class CargoAbonoAGenerar {
		public Long					NumeroCargoAbono;
		public String 				DocumentoIdentidad;   
		public String 				CodigoPortafolio;
		public String				IdMoneda;
		public String				IdTipoMovimiento;
		public String				IdConcepto;
		public String				Descripcion;
		public String				FechaCondicionDeLiquidacion;
		public BigDecimal			Monto;
		public String 				DocumentoIdentidadTraspaso;   
		public String 				CodigoPortafolioTraspaso;
		public String				CertificadoAutomatico;
		
		//Datos referenciales (opcionales)
		public String				IdTipoOperacion = "";		   
		public Long					IdOperacion 	= 0L;
		public String				FechaOperacion 	= "0001-01-01";
		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		
	};

	
	
	// ------------------------------------------------------------
	// CLASES DE SALIDA
	// ------------------------------------------------------------
	
	public static class SalidaCrearCargoAbonoWS{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<ObjCargoAbonoGenerado>	P_OUT_CARGOABONOGENERADO;

		public SalidaCrearCargoAbonoWS (){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_CARGOABONOGENERADO	= new ArrayList<ObjCargoAbonoGenerado>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	
	// ------------------------------------------------------------
	// CLASES DE PROCESO
	// ------------------------------------------------------------
	public static class  ClientePortafolio {		
		public String 	DocumentoIdentidad;
		public String 	CodigoPortafolio;
		public Long 	IdPortafolio;
		public String 	NombreCompletoPortafolio;
		public String 	IdEstado;        // Estado del portafolio
		public String 	IdEstadoCliente;
		public String	EsClienteNuevoProspecto = "N";
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		
	};
	
	
	// ---------------------------------------------------------------------
	// CALL
	// ---------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearCargoAbonoWS jin = gson.fromJson(in.toString(), EntradaCrearCargoAbonoWS.class);
		SalidaCrearCargoAbonoWS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	
	
	// ---------------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------------
	public SalidaCrearCargoAbonoWS logica(Connection con, EntradaCrearCargoAbonoWS entrada) throws Exception {
		logger.info("INICIO::CrearCargoAbonoWS");
		
		SalidaCrearCargoAbonoWS out = new SalidaCrearCargoAbonoWS();		
		SQL sql = new SQL();		
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		HashMap<String, String> paramsMensaje = new HashMap<String, String>();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		
		
		// ---------------------------------------------------------------------------------
		// OBTIENE IdPortafolio a partir de DocumentoIdentidad y CodigoPortafolio		
		logger.debug("INICIO OBTENCION DE PORTAFOLIOS");
		
		List<String> lstDocumentoIdentidad = new ArrayList<String>();
		List<String> lstCodigoPortafolio = new ArrayList<String>();
		
		
		for (CargoAbonoAGenerar mov : entrada.p_CargoAbonoAGenerar)
		{
			if (!lstDocumentoIdentidad.contains(mov.DocumentoIdentidad))
				lstDocumentoIdentidad.add(mov.DocumentoIdentidad);
			
			if (!lstCodigoPortafolio.contains(mov.CodigoPortafolio))
				lstCodigoPortafolio.add(mov.CodigoPortafolio);
			
			//Si es una transferencia 
			if (mov.DocumentoIdentidadTraspaso != null && !mov.DocumentoIdentidadTraspaso.equals(""))
			{	
				if (!lstDocumentoIdentidad.contains(mov.DocumentoIdentidadTraspaso))
					lstDocumentoIdentidad.add(mov.DocumentoIdentidadTraspaso);
			}
			
			if (mov.CodigoPortafolioTraspaso != null && !mov.CodigoPortafolioTraspaso.equals(""))
			{
				if (!lstCodigoPortafolio.contains(mov.CodigoPortafolioTraspaso))
					lstCodigoPortafolio.add(mov.CodigoPortafolioTraspaso);
			}			
		}
		
		params.clear();
		params.put("DocumentoIdentidad", lstDocumentoIdentidad);
		params.put("CodigoPortafolio", lstCodigoPortafolio);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
 		
		List<ClientePortafolio> lstClientePortafolio = sql.query(con, SQL_SELECT_CLIENTE_PORTAFOLIO, params, ClientePortafolio.class);

		
		MultiKeyMap mapClientePortafolio = new MultiKeyMap();
		
		//Agregar elementos al mapa con llave multiple
		for (ClientePortafolio por: lstClientePortafolio)
		{	
			ClientePortafolio objClientePortafolio = (ClientePortafolio) mapClientePortafolio.get(por.DocumentoIdentidad, por.CodigoPortafolio);
			
			if (objClientePortafolio == null)			
				mapClientePortafolio.put(por.DocumentoIdentidad, por.CodigoPortafolio, por);
		}

		
		// ---------------------------------------------------------------------------------
		// VALIDACIONES BASICAS
		logger.debug("INICIO VALIDACIONES");
		
		List<Long> lstIdInterno = new ArrayList<Long>();
		
		for (CargoAbonoAGenerar mov : entrada.p_CargoAbonoAGenerar)
		{
			ClientePortafolio objClientePortafolio = (ClientePortafolio) mapClientePortafolio.get(mov.DocumentoIdentidad, mov.CodigoPortafolio);
			
			if (!lstIdInterno.contains(mov.NumeroCargoAbono))
			{
				lstIdInterno.add(mov.NumeroCargoAbono);
			}
			else
			{
				out.P_OUT_MENSAJES.add(new MensajeSalida(mov.NumeroCargoAbono, MensajeSalida.MENSAJE_ERROR, "ID_INTERNO_DUPLICADO",null, con, entrada.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;				
			}
			
			
			if (objClientePortafolio == null)
			{	
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CLIENTE_PORTAFOLIO_NO_EXISTE",null, con, entrada.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;
			}
			
			if (!objClientePortafolio.IdEstadoCliente.equalsIgnoreCase("ACT") && _c.NO.equals(objClientePortafolio.EsClienteNuevoProspecto) )
			{
				paramsMensaje.put("NombreCliente", objClientePortafolio.NombreCompletoPortafolio);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CLIENTE_NO_SE_ENCUENTRA_ACTIVO",paramsMensaje, con, entrada.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;
			}
			
			if (!objClientePortafolio.IdEstado.equalsIgnoreCase("ACT"))
			{
				paramsMensaje.put("NombreCliente", objClientePortafolio.NombreCompletoPortafolio);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PORTAFOLIO_NO_SE_ENCUENTRA_ACTIVO",paramsMensaje, con, entrada.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;
			}
		}


		
		// ---------------------------------------------------------------------------------
		// LLAMADA SERVICIO CrearCargoAbono 
		logger.debug("INICIO LLAMADA SERVICIO CTA.CrearCargoAbono");
		
		CrearCargoAbono servicioCrearCargoAbono = new CrearCargoAbono();		
		CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
		CrearCargoAbonoSalida salidaCrearCargoAbono = new CrearCargoAbonoSalida();
		
		entradaCrearCargoAbono.p_IdEmpresa 			= entrada.p_IdEmpresa;
		entradaCrearCargoAbono.p_IdUsuario 			= entrada.p_IdUsuario;		
		entradaCrearCargoAbono.p_IdModulo 			= "CTA";		
		entradaCrearCargoAbono.p_simularOperacion 	= entrada.p_SimularOperacion;
		entradaCrearCargoAbono.p_UsaReglasNegocio 	= "S";		
		entradaCrearCargoAbono.p_FechaSistema 		= fechaSistema;
		entradaCrearCargoAbono.p_EsIngresoManual	= "N";
		
		
		
		for (CargoAbonoAGenerar mov : entrada.p_CargoAbonoAGenerar)
		{
			ObjCargoAbonoAGenerar objCargoAbono = new ObjCargoAbonoAGenerar();
			
			
			objCargoAbono.NumeroCargoAbono 			= mov.NumeroCargoAbono;
			objCargoAbono.IdMoneda 					= mov.IdMoneda;			
			objCargoAbono.IdTipoMovimiento 			= mov.IdTipoMovimiento.equalsIgnoreCase("MOVTRN") ? "MOVCAR" : mov.IdTipoMovimiento;
			objCargoAbono.IdConcepto 				= mov.IdConcepto;
			objCargoAbono.Descripcion 				= mov.Descripcion;
			objCargoAbono.IdCondicionDeLiquidacion 	= "T0";		// La FechaCondicionDeLiquidacion es la que manda
			objCargoAbono.FechaCondicionDeLiquidacion = mov.FechaCondicionDeLiquidacion;
			objCargoAbono.Monto 					= mov.Monto;
			objCargoAbono.IdEjecutivo 				= 0L; 
			objCargoAbono.IdSucursal 				= 0L;
			objCargoAbono.EsCargoAbonoPorTraspaso 	= (mov.IdTipoMovimiento.equalsIgnoreCase("MOVTRN"))  ? "S" : "N";
			objCargoAbono.GeneroContraparteEnTesoreria = "N";  // campo deprecado, no tiene ningun efecto
			objCargoAbono.CertificadoAutomatico 	= mov.CertificadoAutomatico;

			if ((ClientePortafolio) mapClientePortafolio.get(mov.DocumentoIdentidad, mov.CodigoPortafolio) != null)
				objCargoAbono.IdPortafolio = ((ClientePortafolio) mapClientePortafolio.get(mov.DocumentoIdentidad, mov.CodigoPortafolio)).IdPortafolio;
			else
				objCargoAbono.IdPortafolio = 0L;
			
			if ((ClientePortafolio) mapClientePortafolio.get(mov.DocumentoIdentidadTraspaso, mov.CodigoPortafolioTraspaso) != null)
				objCargoAbono.IdPortafolioTraspaso = ((ClientePortafolio) mapClientePortafolio.get(mov.DocumentoIdentidadTraspaso, mov.CodigoPortafolioTraspaso)).IdPortafolio;
			else
				objCargoAbono.IdPortafolioTraspaso = 0L;
			
			//Datos Referenciales (opcionales)
			objCargoAbono.IdTipoOperacion 			= mov.IdTipoOperacion;
			objCargoAbono.IdOperacion 				= mov.IdOperacion;
			objCargoAbono.FechaOperacion 			= mov.FechaOperacion;
			objCargoAbono.FechaVencimientoOperacion = null; 
			objCargoAbono.MontoOperacion 			= null;
			objCargoAbono.IdContraparte 			= null;
			objCargoAbono.IdTipoComprobante 		= null; 
			objCargoAbono.IdComprobante 			= null;
			objCargoAbono.FolioComprobante 			= null; 
			objCargoAbono.IdComprobanteDetalle 		= null; 
			objCargoAbono.IdComprobanteFormaDePago 	= null;
			objCargoAbono.IdTipoMovimientoAsociado 	= null; 
			objCargoAbono.IdMovimientoAsociado 		= null; 
			objCargoAbono.FechaComprobante 			= null;
			objCargoAbono.IdPersonaPagoCobro 		= null; 
			objCargoAbono.IdInstrumento 			= null; 
			objCargoAbono.IdTipoInstrumento 		= null;
			objCargoAbono.Cantidad 					= null; 
			objCargoAbono.Nemo 						= null; 
			objCargoAbono.EsIndicadorBilateral 		= "N";
			
			entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(objCargoAbono);	
			
		}				
		
		salidaCrearCargoAbono = servicioCrearCargoAbono.logica(con, entradaCrearCargoAbono);
		
		
		//Retornar salida		
		out.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
		out.P_OUT_CARGOABONOGENERADO.addAll(salidaCrearCargoAbono.P_OUT_CARGOABONOGENERADO);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		
		logger.info("FIN::CrearCargoAbonoWS");
		return out;
	}
	

	// ------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();		

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_CargoAbonoAGenerar\":[{\"NumeroCargoAbono\": 1, \"DocumentoIdentidad\":\"12969673-7\", \"CodigoPortafolio\":\"1\",	\"IdMoneda\":\"CLP\", \"IdTipoMovimiento\":\"MOVABN\", \"IdConcepto\":\"AAAAAA\", \"Descripcion\":\"prueba WS\", \"FechaCondicionDeLiquidacion\":\"2021-09-14\", \"Monto\":\"1000001\", \"DocumentoIdentidadTraspaso\":\"\", \"CodigoPortafolioTraspaso\":\"\", \"CertificadoAutomatico\":\"S\",	\"IdTipoOperacion\":\"\", \"IdOperacion\":0, \"FechaOperacion\":\"0001-01-01\"}]}";

			EntradaCrearCargoAbonoWS Objin = gson.fromJson(json, EntradaCrearCargoAbonoWS.class);
			CrearCargoAbonoWS demo = new CrearCargoAbonoWS();
			try {
				System.out.println(demo.logica(con, Objin));
			}
			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				//con.rollback();
				con.commit();
				con.close();
			}
    	}
		System.out.println("== END ==");
	}
}

