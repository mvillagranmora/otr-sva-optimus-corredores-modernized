//GCMBIPAPAS-1 Filtro de clientes por Perfiles
package cl.bcs.custodia;


import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarEventoCapitalCliente implements JavaCall{

	
	private final String SQL_SELECT_DATOS_CUS_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/ConsultarEventoCapitalCliente_Select.sql");
	private final String SQL_SELECT_CLIENTES_EVENTO_CAPITAL_SUSCOMPLEMENTARIA = loadResourceToString("cl/bcs/custodia/sql/ConsultarEventoCapitalCliente_SelectSuscripcionComplementaria.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		Parametros_Entrada in = gson.fromJson(jin.toString(), Parametros_Entrada.class);
		ArrSalidaGrilla out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;		
	}	
	
	public static class Parametros_Entrada {
		//Parametros Fijos
		public long		p_IdEmpresa;
		public long		p_IdUsuario;
		public String	p_IdModulo;
		public long 	p_IdEmpleadoConsulta;
		
		//Parametros de ventana		
		public long p_IdCliente;
		public String p_TipoEvento;
		public String p_Instrumento;
		public String p_Nemo;
		public String p_Estado;
		public String p_TipoSocioNegocio;
		public long p_NumeroDividendo;
		public String p_FechaPagoDesde;
		public String p_FechaPagoHasta;
		public long p_Secuencia;
		
		// GCBSXNEC-1::jmedina_ext Parametros 2da vuelta
		public String p_SecuenciaRelacionada = "";
		public String p_SuscripcionComplementaria = "N";
		
		public Parametros_Entrada() {
			this.p_Nemo = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	//Fijo Estructura salida datos para Grilla
	public static class Parametros_Salida{
		
		public long IdEventoCapitalCliente;             
		public long IdEventoCapital;
		public String IdTipoEventoCapital;              
		public String TipoEvento;
		public long NumeroDividendo;                    
		public long Secuencia;                          
		public long IdSocioNegocio;
		public long IdPortafolio;                       
		public long IdCliente;
		public String CodigoPortafolio;                 
		public String Portafolio;
		public String RUTCavali;                        
		public String IdClasificacionTipoInstrumento;   
		public String TipoSocioNegocio;                 
		public String Nemo;                             
		public String IdTipoInstrumento;
		public String TipoInstrumento;
		public long IdInstrumento;
		public String ISIN;                             
		public long IdInstrumentoNuevo;
		public String NemoNuevo;                        
		public String IdConceptoGarantia;
		public String TipoCartera;                      
		public String IdTipoCartera;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;               
		public String NombreCliente;                    
		public String ConfiguracionNotificacion;        
		public String TienePreferenciaDePago;           
		public long IdEmpleadoEjecutivoPortafolio;      
		public String CodigoUsuarioAgente;              
		public String NombreAgente;                     
		public String CodigoSucursal;                   
		public java.sql.Timestamp FechaHoraIngresoRegistro;
		public java.sql.Timestamp FechaHoraUltimaActualizacion;
		public BigDecimal MontoCapital;                 
		public BigDecimal Factor;                       
		public BigDecimal Valor;
		public String DescripcionEvento;
		public BigDecimal Precio;                       
		public BigDecimal PrecioNuevo;                  
		public BigDecimal CantidadNueva;                
		public BigDecimal MontoInteres;                 
		public BigDecimal MontoReajuste;                
		public String FechaPago;                        
		public String FechaCierre;
		public String IdMoneda;                         
		public String NombreMoneda;
		public long SecuenciaCupon;
		public BigDecimal CantidadDerecho;              
		public long PrecioAnterior;                     
		public BigDecimal Total;                        
		public BigDecimal MontoRetencion;               
		public BigDecimal CantidadDerechoArchivo;             
		public BigDecimal AccionesDisponiblesArchivo;         
		public BigDecimal DiferenciaCantidadDerecho;          
		public BigDecimal DiferenciaAccionesCantidadNueva;    
		public BigDecimal MontoBeneficioArchivo;              
		public BigDecimal DiferenciaMontoBeneficio;           
		public BigDecimal MontoInteresArchivo;                
		public BigDecimal MontoCapitalArchivo;                
		public BigDecimal DiferenciaMontoInteres;             
		public BigDecimal DiferenciaMontoCapital;             
		public BigDecimal MontoRetencionArchivo;        
		public BigDecimal DiferenciaMontoRetencion;     
		public String IdBoveda;                         
		public long IdEmpresa;
		public String Origen;		
		public long IdUsuario;
		public String IdEstado;                         
		public String EstadoNotificacion;               
		public String NombreMedioDePago;                
		public String TipoCuentaBoveda;                 
		public String NumeroCuentaBoveda;               
		public String PagaClienteCorredor;              
		public String IdTipoReajuste;                   
		public BigDecimal INGMontoInteresCorteCupon;    
		public BigDecimal Amortizacion;                 
		public String AfectoaClearing;                  
		public String ProcesadoClearing;		        
		public long IdCorredorContraparte;
		public String CodigoCorredorContraparte;
		public long IdComprobanteTesoreria;             
		public String TipoOperacion;                    
		public String IdBolsa;
		public String IdRegimenFondoMutuo;
		public String OperacionesVigentes; 
		public String ISFUT;
		public String RetencionISFUT;
		public BigDecimal MontoRetencionISFUT;
		public String ISFUTEventoCapital;
		public BigDecimal TasaEfectiva;
		public String Modalidad;
		public String MedioDePago;
		public String IndicadorCuentaInversion;
		public String SecuenciaRelacionada;
		public String SuscripcionComplementaria;
		public String HoraIngresoFechaPago;
		public BigDecimal ImpuestoDivExtranjero;
		public BigDecimal MontoImpuestoExtranjero;
		public BigDecimal GastoTransferencia;
		public BigDecimal FactorNuevo; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	//Logica para obtencion de data
	public ArrSalidaGrilla logica(Connection conn, Parametros_Entrada entrada)throws Exception{

		int FiltroTipoEvento = 0;
		int FiltroInstrumento = 0;
		int FiltroNemo = 0;
		int FiltroEstado = 0;
		int FiltroTipoSocioNegocio = 0;
		int FiltroNumeroDividendo = 0;
		int FiltroCliente = 0;
		int FiltroSecuencia = 0;

		//Obtenemos valores de los parametros de entrada		
		Long idCliente = entrada.p_IdCliente;
		String varTipoEvento = entrada.p_TipoEvento;		
		String varInstrumento = entrada.p_Instrumento;
		String varNemo = entrada.p_Nemo;
		String varEstado = entrada.p_Estado;
		String varTipoSocioNegocio = entrada.p_TipoSocioNegocio;
		String varNumeroDividendo = String.valueOf(entrada.p_NumeroDividendo);
		String varSecuencia = String.valueOf(entrada.p_Secuencia);
				
		if (varTipoEvento.equalsIgnoreCase("0")) {
			varTipoEvento = "0";
			FiltroTipoEvento = 1;
		}

		if (varInstrumento.equalsIgnoreCase("0")) {
			varInstrumento = "0";
			FiltroInstrumento = 1;
		}
		
		if (varNemo.isEmpty()) {
			varNemo = "";
			FiltroNemo = 1;
		}
		
		if (varEstado.equalsIgnoreCase("0")) {
			varEstado = "0";
			FiltroEstado = 1;
		}
		
		if (varTipoSocioNegocio.equalsIgnoreCase("0")) {
			varTipoSocioNegocio = "0";
			FiltroTipoSocioNegocio = 1;
		}
		
		if (varNumeroDividendo.equalsIgnoreCase("0")) {
			varNumeroDividendo = "0";
			FiltroNumeroDividendo = 1;
		}		
		
		if (varSecuencia.equalsIgnoreCase("0")) {
			varSecuencia = "0";
			FiltroSecuencia = 1;
		}	
				
		SQL _sql = new SQL();
		
		List<Long> listIdCliente = new ArrayList<Long>();
		HashMap<String,Object> params = null;
		
		//Fijo Si no se selecciona Cliente se obtienen todos los clientes segun sea el perfil del Empleado que consulta
		
		/*if (idCliente == 0) {
			
			//Obtenemos los clientes del portafolio
			ObtenerClientesPortafolio clientesPortafolio = new ObtenerClientesPortafolio();
			
			ConsultarClientesPortafolio_entrada entradaConsulta = new ConsultarClientesPortafolio_entrada();
			
			entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsulta.p_IdModulo = entrada.p_IdModulo;
			entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
			
			listIdCliente = clientesPortafolio.getClientesPortafolio(conn, entradaConsulta);
			
			
		}else {*/
		if (idCliente != 0) {
			listIdCliente.add(idCliente);
			FiltroCliente = 0;
		}
		else {
			FiltroCliente = 1;
		}
		
		//Logica propia de la ventana
		
		//Obtenemos consulta de las operaciones Cliente
		ArrSalidaGrilla salida = new ArrSalidaGrilla();
		params = new HashMap<>();
		params.put("p_listaClientes", listIdCliente); 
		params.put("p_TipoEvento", varTipoEvento);		
		params.put("p_FiltroTipoEvento", FiltroTipoEvento);
		params.put("p_Instrumento", varInstrumento);
		params.put("p_FiltroInstrumento", FiltroInstrumento);
		params.put("p_Nemo", varNemo);
		params.put("p_FiltroNemo", FiltroNemo);
		params.put("p_Estado", varEstado);
		params.put("p_FiltroEstado", FiltroEstado);
		params.put("p_TipoSocioNegocio", varTipoSocioNegocio);
		params.put("p_FiltroTipoSocioNegocio", FiltroTipoSocioNegocio);
		params.put("p_NumeroDividendo", varNumeroDividendo);	
		params.put("p_FiltroNumeroDividendo", FiltroNumeroDividendo);
		params.put("p_FechaPagoDesde", entrada.p_FechaPagoDesde);
		params.put("p_FechaPagoHasta", entrada.p_FechaPagoHasta);	
		params.put("p_FiltroCliente", FiltroCliente);
		params.put("p_Secuencia", varSecuencia);
		params.put("p_FiltroSecuencia", FiltroSecuencia);

		List<Parametros_Salida> operaciones = null;
		
		if(entrada.p_SuscripcionComplementaria.equalsIgnoreCase("S")) {
			params = new HashMap<>();; 
			params.put("p_TipoEvento", varTipoEvento);
			params.put("p_Instrumento", varInstrumento);
			params.put("p_Nemo", varNemo);
			params.put("p_Secuencia", varSecuencia);
			
			operaciones = _sql.query(conn, SQL_SELECT_CLIENTES_EVENTO_CAPITAL_SUSCOMPLEMENTARIA, params, Parametros_Salida.class);
			
		} else {
			operaciones = _sql.query(conn, SQL_SELECT_DATOS_CUS_EVENTO_CAPITAL, params, Parametros_Salida.class);
		}
		
		for(Parametros_Salida notif : operaciones) {
			if(notif.Modalidad == null && (notif.IdTipoEventoCapital.equals("DV") || notif.IdTipoEventoCapital.equals("RC") ) ) {
				if (notif.IndicadorCuentaInversion.equals("S")){
					notif.Modalidad = "CUENTA INVERSION";
					notif.MedioDePago = "CUENTA INVERSION";
				}else {
					notif.Modalidad = "";
					notif.MedioDePago = "";
				}
			}
			
			salida.P_OUT_CUSTODIA_EVENTO_CAPITAL.add(notif);
		}
		
		return salida;		
	}
	
	
	//Fijo
	//Arreglo de salida para la grilla
	public static class ArrSalidaGrilla {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public long						P_OUT_ERRORESCONTADOR;
		
		public List<Parametros_Salida> P_OUT_CUSTODIA_EVENTO_CAPITAL;
		
		public ArrSalidaGrilla() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_CUSTODIA_EVENTO_CAPITAL = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
		
	}
	
	//Fijo
	//Main para prueba
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties"))
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			//Datos simulados, obtenidos desde ventana			
			String jsonEP ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoConsulta\":108,\"p_IdCliente\":0,\"p_TipoEvento\":\"EP\",\"p_Instrumento\":560,\"p_Estado\":0,\"p_TipoSocioNegocio\":0,\"p_NumeroDividendo\":0,\"p_FechaPagoDesde\":\"2024-09-11T06:00:00.000Z\",\"p_FechaPagoHasta\":\"2024-09-18T05:59:59.000Z\",\"p_Secuencia\":100000506}";
			String jsonSU ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoConsulta\":839,\"p_IdCliente\":0,\"p_TipoEvento\":\"DF\",\"p_Instrumento\":0,\"p_Estado\":0,\"p_TipoSocioNegocio\":0,\"p_NumeroDividendo\":3300701,\"p_FechaPagoDesde\":\"2024-12-23T06:00:00.000Z\",\"p_FechaPagoHasta\":\"2024-12-28T05:59:59.000Z\",\"p_Secuencia\":100002831}";
			Parametros_Entrada Objin = gson.fromJson(jsonSU, Parametros_Entrada.class);
			ConsultarEventoCapitalCliente demo = new ConsultarEventoCapitalCliente();
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			try 
			{
				ArrSalidaGrilla ObjOut= demo.logica(con, Objin);
				System.out.println("SALIDA:");
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }	
}