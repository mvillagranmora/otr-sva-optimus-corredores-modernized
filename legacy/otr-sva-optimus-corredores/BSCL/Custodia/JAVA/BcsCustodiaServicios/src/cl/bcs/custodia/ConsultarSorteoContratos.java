package cl.bcs.custodia;

/**
 * Objetivo: Servicio que recibe el usuario, el terminal y una lista de solicitudes de ejercicio, las cuales se van a validar y, en caso de estar correctas, 
 * actualizan las opciones de ejercicio del dia.   
 */
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarEjercicioBloqueo.Empresa;
import cl.bcs.custodia.utils.ClientWS.sorteoContratos.PtdeDopcCuentaDerivadosOpcResponse;
import cl.bcs.custodia.utils.ClientWS.sorteoContratos.PtdeDopcCuentaDerivadosOpcService;
import cl.bcs.custodia.utils.ClientWS.sorteoContratos.PtdeDopcCuentaDerivadosOpcServiceService;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarSorteoContratos implements JavaCall {

	private final Log log = new Log("CUS.ConsultarSorteoContratos");
	private final String SQL_SELECT_DATOS_CORREDORA = loadResourceToString("cl/bcs/custodia/sql/ConsultarSorteoContratos_SelectDatosCorredora.sql");	
	private final String SQL_SELECT_SORTEO_CONTRATOS = loadResourceToString("cl/bcs/custodia/sql/ConsultarSorteoContratos_SelectSorteoContratos.sql");
	private final String SQL_INSERT_SORTEO_CONTRATOS = loadResourceToString("cl/bcs/custodia/sql/ConsultarSorteoContratos_InsertSorteoContratos.sql");
	private final String SQL_DELETE_SORTEO_CONTRATOS = loadResourceToString("cl/bcs/custodia/sql/ConsultarSorteoContratos_DeleteSorteoContratos.sql");
	private final String SQL_SELECT_INDICADORES_CLIENTE = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectIndicadoresCliente.sql");
	private final String SQL_SELECT_IDCLIENTE_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/ConsultarSorteoContratos_SelectDatosClientes.sql");
	// --------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------
	// clases que son utilizadas unicamente en este java deben ser private y no estaticas
	public static class EntradaConsultarSorteoContratos{
		public Long 	p_IdEmpresa;
		public Integer 	p_IdUsuario;
		public String 	p_IdModulo;		
		public String 	p_FechaConsulta;
		public String 	p_IdClasificacionTipoInstrumento;
		public String 	p_IdTipoInstrumento;
		public Integer 	p_IdInstrumento;		
		public Integer 	p_IdCliente;
		public String 	p_TipoDerivado;
		public Long 	p_IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	
	// --------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------
	
	public static class SalidaConsultarSorteoContratos {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR=0L;
		public List<Contratos>				P_OUT_CONTRATOS;
		
		
		public SalidaConsultarSorteoContratos()
		{	
			this.P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
			this.P_OUT_CONTRATOS		 	= new ArrayList<Contratos>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// --------------------------------------------------------
	// CLASES DE PROCESO
	// --------------------------------------------------------

	private class ConexionWSEjercicioBloqueo{
		private String 	p_wsdl;
		private String 	p_nameSpace;
		private String 	p_localPart;
		private String 	p_usuario;
		private String 	p_pass;
		private String  p_fechaSistema;

		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	
	public static class Contratos{		
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String 		DocumentoIdentidad;
		public String 		PortafolioDerivados;
		public String 		IdTipoOperacion;
		public String 		Serie;
		public String 		EsPutEsCall;
		public String 		CodigoCorredora;
		public String 		CodigoClasificacion;
		public String 		IdTipoInstrumento;
		public String 		ModalidadLiquidacion;
		public String 		ModalidadEjercicio;
		public String 		EstiloOpcion;
		public Date 		Fecha;
		public Date 		FechaVencimiento;
		public String 		Subyacente;
		public BigDecimal 	Strike;
		public BigDecimal 	PrecioSubyacente;
		public BigDecimal 	PerdidaGanancia;
		public BigDecimal 	SaldoEjercicio;
		public BigDecimal 	SaldoNeto;
		public BigDecimal 	SaldoPrima;
		public BigDecimal 	SaldoSorteo;
		public String 		IdFondo;
		public String 		IdFondoCliente;
		public String 		LadoDiaAnterior;
		public String 		LadoDia;
		public String 		LadoNeto;
		public String 		LadoTr;
		public BigDecimal	LotePadron;
		public BigDecimal 	ContratosAbiertosNeto;
		public BigDecimal 	ContratosAbiertosDiaAnterior;
		public BigDecimal 	ContratosAbiertosDia;
		public BigDecimal 	ContratosEjercidos;
		public BigDecimal 	ContratosSorteados;
		public BigDecimal 	CantidadContratosTr;
		public String 		Observacion;
	}
	

	public static class Clientes{		
		public Long			IdCliente;
		public String 		DocumentoIdentidad;
		public String  		PortafolioDerivados;
	}
	

	private static final Logger logger = LogManager.getLogger(ConsultarSorteoContratos.class);	
	
	// --------------------------------------------------------
	// LOGICA
	// --------------------------------------------------------
	//usar prefijo entrada y salida
	public SalidaConsultarSorteoContratos logica(Connection con, EntradaConsultarSorteoContratos in) throws Exception {
		logger.info("INICIO CONSULTAR SORTEO CONTRATOS");
		
		log.write("ENTRADA > " + in.toString());
		SalidaConsultarSorteoContratos out = sendConsultarSorteoContratos(con, in);
		logger.info("TERMINO CONSULTAR SORTEO CONTRATOS");	
		return out;
	}
	
	// --------------------------------------------------------
	// METODOS
	// --------------------------------------------------------
	public SalidaConsultarSorteoContratos sendConsultarSorteoContratos(Connection con, EntradaConsultarSorteoContratos entrada) throws Exception{

		HashMap<String, String> mapMensageSalida 	= new HashMap<>();		
		SQL _sql = new SQL();
	    
		SalidaConsultarSorteoContratos salida = new SalidaConsultarSorteoContratos();		
		// ----------------------------------------------------------
		// Obtiene parametros de conexion a WS HT
		ConexionWSEjercicioBloqueo conexion = new ConexionWSEjercicioBloqueo();
//		conexion.p_usuario 		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "DER.EnvioEjercicioDerivados.Usuario");
//		conexion.p_pass 		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "DER.EnvioEjercicioDerivados.Password");
		
		// Conexion WS para consultar contratos		
		conexion.p_wsdl 		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "DER.SorteoContratosDerivados.Wsdl"); 
		conexion.p_nameSpace 	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "DER.SorteoContratosDerivados.NameSpace");
		conexion.p_localPart 	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "DER.SorteoContratosDerivados.LocalPart");		
		conexion.p_fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdBolsa", "BCS");
		List<Empresa> empresa = sql.query(con, SQL_SELECT_DATOS_CORREDORA, params, Empresa.class);
		String codigoCorredora = "";

		if (!empresa.isEmpty()) {
			codigoCorredora = empresa.get(0).CodigoCorredora;//35
		}
		else
		{
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "SRV_DER_NO_EXISTE_CODIGO_CORREDOR", mapMensageSalida, con, entrada.p_IdEmpresa));
		}
		

		// Conexion WS Consultar contratos
    	// ----------------------------------------------------------
		
		try {

			if (conexion.p_fechaSistema.contentEquals(entrada.p_FechaConsulta)) {			

				//Elimina registros del dia
				List<Contratos> paramContratos = new ArrayList<>();
			    Contratos fechaContrato = new Contratos();
			    fechaContrato.Fecha = Fecha.toDate(conexion.p_fechaSistema);
			    paramContratos.add(fechaContrato);				
				sql.batch(con, SQL_DELETE_SORTEO_CONTRATOS,paramContratos, Contratos.class);

				//Llamada a WS de contratos
				PtdeDopcCuentaDerivadosOpcServiceService wsClienteSorteoContrato = new PtdeDopcCuentaDerivadosOpcServiceService(new URL(conexion.p_wsdl), new QName(conexion.p_nameSpace, conexion.p_localPart));
				PtdeDopcCuentaDerivadosOpcService  clienteSorteoContrato = wsClienteSorteoContrato.getPtdeDopcCuentaDerivadosOpcServicePort();
				
				((BindingProvider) clienteSorteoContrato).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, conexion.p_usuario);			
		        ((BindingProvider) clienteSorteoContrato).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, conexion.p_pass);
		        
		        PtdeDopcCuentaDerivadosOpcResponse listaSorteoContrato = clienteSorteoContrato.ptdeDopcCuentaDerivadosOpcExecute(entrada.p_FechaConsulta, codigoCorredora, "", "", "", "", "", "", "");//fondo, rutClie, fondoClie, mercado, familia, subyacente, fecVcto)

				//Obtiene contratos sorteados desde WS
			    List<Contratos> lisContratos = new ArrayList<Contratos>();				    
			    for (int i=0; i < listaSorteoContrato.getLogResult().size();i++) {	
			    
			    	Contratos typeContratos= new Contratos();
			    	typeContratos.DocumentoIdentidad			= listaSorteoContrato.getLogResult().get(i).getRUTCLIE();//"333-6";
			    	typeContratos.IdCliente						= 0L;
			    	typeContratos.PortafolioDerivados			= "0";
	    			typeContratos.IdTipoOperacion				= "VENTA";	//No existe
	    			typeContratos.Serie							= listaSorteoContrato.getLogResult().get(i).getSERIE();//"COP20B8220";
	    			typeContratos.EsPutEsCall					= listaSorteoContrato.getLogResult().get(i).getTIPOOP().trim();
	    			typeContratos.CodigoCorredora				= listaSorteoContrato.getLogResult().get(i).getCODCORR();
	    			typeContratos.CodigoClasificacion			= listaSorteoContrato.getLogResult().get(i).getMERCADO().trim();
	    			typeContratos.Fecha							= Fecha.toDate(listaSorteoContrato.getLogResult().get(i).getFECHA());
	    			typeContratos.FechaVencimiento				= Fecha.toDate(listaSorteoContrato.getLogResult().get(i).getFECVCTO());
	    			typeContratos.Subyacente					= listaSorteoContrato.getLogResult().get(i).getSUBYACENTE().trim();
	    			typeContratos.Strike						= listaSorteoContrato.getLogResult().get(i).getSTRIKE();
	    			typeContratos.PrecioSubyacente				= listaSorteoContrato.getLogResult().get(i).getPRESUBYACENTE();
	    			typeContratos.PerdidaGanancia				= listaSorteoContrato.getLogResult().get(i).getSALDOSORTEO();
	    			typeContratos.SaldoEjercicio				= listaSorteoContrato.getLogResult().get(i).getSALDOEJERCICIO();
	    			typeContratos.SaldoNeto						= listaSorteoContrato.getLogResult().get(i).getSALDONETO();
	    			typeContratos.SaldoPrima					= listaSorteoContrato.getLogResult().get(i).getSALDOPRIMA();
	    			typeContratos.SaldoSorteo					= listaSorteoContrato.getLogResult().get(i).getSALDOSORTEO();
	    			typeContratos.IdFondo						= listaSorteoContrato.getLogResult().get(i).getFONDO();
	    			typeContratos.IdFondoCliente				= listaSorteoContrato.getLogResult().get(i).getFONDOCLIE();
	    			typeContratos.LadoDiaAnterior				= listaSorteoContrato.getLogResult().get(i).getLADODA();
	    			typeContratos.LadoDia						= listaSorteoContrato.getLogResult().get(i).getLADODIA();
	    			typeContratos.LadoNeto						= listaSorteoContrato.getLogResult().get(i).getLADONETO();
	    			typeContratos.LadoTr						= listaSorteoContrato.getLogResult().get(i).getLADOTR();
	    			typeContratos.LotePadron					= listaSorteoContrato.getLogResult().get(i).getLOTEPADRON();
	    			typeContratos.ContratosAbiertosNeto			= listaSorteoContrato.getLogResult().get(i).getCONTABIERTOSNETO();
	    			typeContratos.ContratosAbiertosDiaAnterior	= listaSorteoContrato.getLogResult().get(i).getCONTABIERTODA();
	    			typeContratos.ContratosAbiertosDia			= listaSorteoContrato.getLogResult().get(i).getCONTABIERTODIA();
	    			typeContratos.ContratosEjercidos			= listaSorteoContrato.getLogResult().get(i).getCONTEJERCIDOS();
	    			typeContratos.ContratosSorteados			= listaSorteoContrato.getLogResult().get(i).getCONTSORTEADOS();
	    			typeContratos.CantidadContratosTr			= listaSorteoContrato.getLogResult().get(i).getCANTCONTRATOSTR();	
					lisContratos.add(typeContratos);
						
			    }


			    //Selecciona Documento Identidad para posteriormente obtener el Portafolio de cada cliente
				ArrayList<String> DocumentoIdentidad = new ArrayList<>();								
				for (int i=0; i < lisContratos.size();i++) {					    
					DocumentoIdentidad.add(lisContratos.get(i).DocumentoIdentidad);
				};   
			    
			    
				SQL sqlClientes=new SQL();
				HashMap<String,Object> paramClientes = new HashMap<>();
				paramClientes.put("DocumentoIdentidad",DocumentoIdentidad);
			    List<Clientes> salidaClientes = sql.query(con, SQL_SELECT_IDCLIENTE_CLIENTE, paramClientes, Clientes.class);	
			    
			    //Actualiza IdCliente en lisContratos
			    for (int i=0; i < lisContratos.size();i++) {
			    	String vDocumentoIdentidad = lisContratos.get(i).DocumentoIdentidad;
			    	List<Clientes> cliente = salidaClientes.parallelStream().filter(t-> t.DocumentoIdentidad.equalsIgnoreCase(vDocumentoIdentidad)).collect(Collectors.toList());
			    	
			    	if (!cliente.isEmpty()) {
				    	for (Clientes c :cliente) {
				    		if (lisContratos.get(i).DocumentoIdentidad.trim().equals(c.DocumentoIdentidad.trim())) {
				    			lisContratos.get(i).IdCliente = c.IdCliente; 
				    		}			    		
				    	}
			    	}
			    	else
			    	{
			    		lisContratos.get(i).Observacion = "NO EXISTE CLIENTE ";
			    	}
			    }
			    
			    //Obtiene indicador de clientes 
				HashMap<Long, Clientes> indicadoresCliente = new HashMap<>();
				{
					HashMap<String,Object> paramsInIndicadorCliente = new HashMap<>();
					ArrayList<Long> idsCliente = new ArrayList<>();
					
					for (Clientes cli : salidaClientes) {
						idsCliente.add(cli.IdCliente);
					}
					
					paramsInIndicadorCliente.put("IdsCliente", idsCliente);
					List<Clientes> _indicadoresCliente = _sql.query(con, SQL_SELECT_INDICADORES_CLIENTE, paramsInIndicadorCliente, Clientes.class);
					
					HashMap<Object, Object> __indicadoresCliente = Utils.List2Hash(_indicadoresCliente, "IdCliente");
					for(Object key: __indicadoresCliente.keySet()) {
						indicadoresCliente.put((Long)key,(Clientes) __indicadoresCliente.get(key));
					}
				}			    
			    
			    //Actualiza Portafolio derivados en lisContratos
				for (Contratos cnt: lisContratos) {
					if (indicadoresCliente.get(cnt.IdCliente) != null && !indicadoresCliente.get(cnt.IdCliente).PortafolioDerivados.equals("0")) {
						cnt.PortafolioDerivados = indicadoresCliente.get(cnt.IdCliente).PortafolioDerivados;
						cnt.Observacion	=  "CLIENTE INGRESADO CORRECTAMENTE";
					}
					else if (cnt.Observacion == null ) {
							cnt.Observacion	= "NO EXISTE PORTAFOLIO CLIENTE";
							
						}
						else
						{	
						cnt.Observacion	=  cnt.Observacion + "- " +"NO EXISTE PORTAFOLIO CLIENTE";
						}
			    }			    
				
				//Realiza Insert de contratos    
			    sql.batch(con, SQL_INSERT_SORTEO_CONTRATOS, lisContratos, Contratos.class);
				    
				    
			}

			HashMap<String,Object> paramFinal = new HashMap<>();
		    paramFinal.put("p_Fecha",entrada.p_FechaConsulta);
		    List<Contratos> salidaSorteoContratos = sql.query(con, SQL_SELECT_SORTEO_CONTRATOS, paramFinal, Contratos.class);
			    
		    if (!salidaSorteoContratos.isEmpty()) {				    	
				    
			    List<Contratos> lisSalida = new ArrayList<Contratos>();						    
			    for(Contratos contratoOpciones :  salidaSorteoContratos) {
			    	Contratos dContrato = new Contratos();
			    	dContrato.IdCliente = contratoOpciones.IdCliente;
	    			dContrato.IdPortafolio = contratoOpciones.IdPortafolio;
			    	dContrato.DocumentoIdentidad = contratoOpciones.DocumentoIdentidad;				    	
			    	dContrato.PortafolioDerivados = contratoOpciones.PortafolioDerivados;
			    	dContrato.IdTipoOperacion = contratoOpciones.IdTipoOperacion;
			    	dContrato.Serie = contratoOpciones.Serie;
			    	dContrato.EsPutEsCall = contratoOpciones.EsPutEsCall;
			    	dContrato.CodigoCorredora = contratoOpciones.CodigoCorredora;
			    	dContrato.CodigoClasificacion = contratoOpciones.CodigoClasificacion;
			    	dContrato.IdTipoInstrumento = contratoOpciones.IdTipoInstrumento;
			    	dContrato.ModalidadLiquidacion = contratoOpciones.ModalidadLiquidacion;
			    	dContrato.ModalidadEjercicio = contratoOpciones.ModalidadEjercicio;
			    	dContrato.EstiloOpcion = contratoOpciones.EstiloOpcion;
			    	dContrato.Fecha = contratoOpciones.Fecha;
			    	dContrato.FechaVencimiento = contratoOpciones.FechaVencimiento;
			    	dContrato.Subyacente = contratoOpciones.Subyacente;
			    	dContrato.Strike = contratoOpciones.Strike;
			    	dContrato.PrecioSubyacente = contratoOpciones.PrecioSubyacente;
			    	dContrato.PerdidaGanancia = contratoOpciones.PerdidaGanancia;
			    	dContrato.SaldoEjercicio = contratoOpciones.SaldoEjercicio;
			    	dContrato.SaldoNeto= contratoOpciones.SaldoNeto;
			    	dContrato.SaldoPrima = contratoOpciones.SaldoPrima;
			    	dContrato.SaldoSorteo = contratoOpciones.SaldoSorteo; 
			    	dContrato.IdFondo = contratoOpciones.IdFondo;
			    	dContrato.IdFondoCliente = contratoOpciones.IdFondoCliente;
			    	dContrato.LadoDiaAnterior = contratoOpciones.LadoDiaAnterior;
			    	dContrato.LadoDia = contratoOpciones.LadoDia;
			    	dContrato.LadoNeto = contratoOpciones.LadoNeto;
			    	dContrato.LadoTr = contratoOpciones.LadoTr;
			    	dContrato.LotePadron = contratoOpciones.LotePadron;
			    	dContrato.ContratosAbiertosNeto = contratoOpciones.ContratosAbiertosNeto;
			    	dContrato.ContratosAbiertosDiaAnterior = contratoOpciones.ContratosAbiertosDiaAnterior;
			    	dContrato.ContratosAbiertosDia = contratoOpciones.ContratosAbiertosDia;
			    	dContrato.ContratosEjercidos = contratoOpciones.ContratosEjercidos;
			    	dContrato.ContratosSorteados = contratoOpciones.ContratosSorteados ;
			    	dContrato.CantidadContratosTr = contratoOpciones.CantidadContratosTr;
			    	dContrato.Observacion = contratoOpciones.Observacion;
			    	lisSalida.add(dContrato);
					salida.P_OUT_CONTRATOS.add(dContrato);				    	
				}				    

			    	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "SRV_DER_OK_AL_CONSULTAR_CONTRATOS", mapMensageSalida, con, entrada.p_IdEmpresa));
				    
		    }
		    else
		    {
		    	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "SRV_DER_NO_EXISTE_INFORMACION_PARA_FECHA", mapMensageSalida, con, entrada.p_IdEmpresa));
		    }
		}    
		
    	catch (Exception e){
    		e.printStackTrace();
    		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "SRV_DER_ERROR_AL_CONSULTAR_CONTRATOS", mapMensageSalida, con, entrada.p_IdEmpresa));
    		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
    	}

		return salida;
    	
	};
    	
	
	
	// --------------------------------------------------------
	// CALL
	// --------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarSorteoContratos in=gson.fromJson(jin.toString() ,EntradaConsultarSorteoContratos.class);
				
		SalidaConsultarSorteoContratos out=logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	

	
	
	// --------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------------
	
	public static void main(String[] args) {		
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try {
			Connection con = PlataformaUtils.getDESAConnection();			
			
			Gson gson = Utils.getGsonBuilder();	
			String json = "{\r\n" + 
					"  \"p_IdEmpresa\": 1,\r\n" + 
					"  \"p_IdUsuario\": 21,\r\n" + 
					"  \"p_IdModulo\": \"CUS\",\r\n" + 					
					"  \"p_FechaConsulta\": \"2020-03-19\",\r\n" + 
					"  \"p_IdClasificacionTipoInstrumento\": \"\",\r\n" + 
					"  \"p_IdTipoInstrumento\": \"\",\r\n" + 
					"  \"p_IdInstrumento\": 0,\r\n" + 					
					"  \"p_IdCliente\": 123569,\r\n" +
					"  \"p_TipoDerivado\": \"OP\",\r\n" +
					"  \"p_IdPortafolio\": 27499\r\n" +
					"}";			
			
			EntradaConsultarSorteoContratos Objin = gson.fromJson(json, EntradaConsultarSorteoContratos.class);
			ConsultarSorteoContratos demo=new ConsultarSorteoContratos();
			
			try {
				SalidaConsultarSorteoContratos ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.rollback();
 				con.commit();
 				
			}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
    

};
