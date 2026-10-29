package cl.bcs.custodia.evc;

import java.io.IOException;
import java.lang.reflect.Field;

//***************************
// MIGRACIÓN SIN TERMINAR!!!
//***************************

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarSorteoContratos.Clientes;
import cl.bcs.custodia.evc.CrearEventoCapital.Evento;
import cl.bcs.custodia.evc.CrearEventoCapital.EventoCapital_IN;
import cl.bcs.custodia.evc.CrearEventoCapital.EventoCapital_OUT;
import cl.bcs.custodia.evc.CrearEventoCapitalCliente.EventoCL;
import cl.bcs.custodia.evc.CrearEventoCapitalCliente.EventoCapitalCliente_IN;
import cl.bcs.custodia.evc.CrearEventoCapitalCliente.EventoCapitalCliente_OUT;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapitalCliente_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.ordenes.NotificarEstadoOrden;
import cl.bcs.ordenes.CrearOrdenes.InformacionOrden;
import cl.bcs.ordenes.CrearOrdenes.OrdenProceso;
import cl.bcs.ordenes.CrearOrdenes.RequisitoOperativoCrearOrden;
import cl.bcs.ordenes.NotificarEstadoOrden.NotificarEstadoOrden_Entrada;
import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Entrada;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class SuscripcionEmisionPago implements JavaCall{
	private final Log log = new Log("CUS.SuscipcionEmisionPago");
	
	private final String SQL_SELECT_EVENTO_CAPITAL			= loadResourceToString("cl/bcs/custodia/sql/SuscipcionEmisionPago_SelectEventoCapital.sql");
	private final String SQL_SELECT_DATOS_VALIDACION		= loadResourceToString("cl/bcs/custodia/sql/SuscipcionEmisionPago_SelectDatosValidacion.sql"); // trae los datos necesarios para realizar validaciones
	private final String SQL_SELECT_INSTRUMENTO_RELACIONADO	= loadResourceToString("cl/bcs/custodia/sql/SuscipcionEmisionPago_SelectInstrumentoRelacionado.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL			= loadResourceToString("cl/bcs/custodia/sql/SuscipcionEmisionPago_InsertEventoCapital.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE	= loadResourceToString("cl/bcs/custodia/sql/SuscipcionEmisionPago_InsertEventoCapitalCliente.sql");
	private final String SQL_SELECT_REQUISITOS_OPERATIVOS 	= loadResourceToString("cl/bcs/custodia/sql/SuscipcionEmisionPago_SelectRequisitosOperativos.sql");
	
	public static class SuscripcionEmisionPago_IN {
		public Long 					p_IdEmpresa;
	    public Long						p_IdUsuario;
	    public String 					p_IdModulo;
	    public List<SuscripcionEmiPago> p_Suscripcion;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SuscripcionEmiPago {
		public Long 			p_IdInterno;
		public Long 			p_IdEventoCapital;
		public String 			p_IdTipoEventoCapital;
		public Long 			p_Secuencia;
		public Long				p_IdSocioNegocio;
		public String			p_TipoSocioNegocio;
		public Long				p_IdPortafolio;
		public String			p_CodigoPortafolio;
//		public String			p_IdTipoDocumentoIdentidad;		
		public String			p_DocumentoIdentidad;			
//		public String			p_NombreCliente;				
//		public String			p_IdClasificacionTipoInstrumento;
//		public String			p_IdTipoInstrumento;			
//		public Long				p_IdInstrumento;				
//		public String			p_Nemo;							
//		public Long				p_IdInstrumentoNuevo;			
//		public String			p_NemoNuevo;					
		public String			p_FechaSu;					
		public BigDecimal		p_CantidadDerecho;    			
//		public String			p_TipoCartera;					
		public BigDecimal		p_CantidadNueva;				
//		public BigDecimal		p_MontoCapital;					
//		public BigDecimal		p_MontoInteres;					
//		public BigDecimal		p_Total;						
		public String			p_IdMoneda;						
//		public Long				p_SecuenciaCupon;				
//		public Long				p_IdEmpleadoEjecutivoPortafolio;
//		public String			p_CodigoUsuarioAgente;			
//		public String			p_NombreAgente;					
//		public String			p_CodigoSucursal;				
		public String			p_IdBoveda;						
//		public String			p_UsaReglasNegocio; 			
//		public String			p_IdBolsa;						
		public Long				p_NumeroDividendo;
		public Long				p_IdCliente;
		public BigDecimal		p_Precio;	    
		public String		    IdTipoCanalIngreso;
		public String 			p_SecuenciaRelacionada;
		public String 			p_SuscripcionComplementaria;

		//para saldo caja
		public BigDecimal		TotalMontoPortafolio = BigDecimal.ZERO;
		public BigDecimal 		SaldoCajaPortafolio = BigDecimal.ZERO;
		public BigDecimal 		LineaSobregiroPortafolio = BigDecimal.ZERO;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public SuscripcionEmiPago(){
			this.p_IdCliente=0L;
			this.IdTipoCanalIngreso="0";
		}
	}
	public static class SuscripcionEmisionPago_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public SuscripcionEmisionPago_OUT() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Instrumento {
		public Long 		IdInstrumento;
		public String		Nemotecnico;
		public String		IdTipoInstrumento;
		public String		IdClasificacionTipoInstrumento;
		public String		IdEstado;
		public String		CallConfirmado;
		public String		CuponeraVariable;
		public Date			FechaPagoCupon;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoRelacionado {
		public Long 		IdInstrumento;
		public Long 		IdInstrumentoRelacionado;
		public String		NemoRelacionado;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosValidacion {
		public Long 		IdCliente;
		public String 		IdEstadoCliente;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SuscripcionEmisionPago_OUT logica (Connection conn, SuscripcionEmisionPago_IN in) throws Exception {
		log.write("INICIO::CUS.SuscipcionEmisionPago");
		SuscripcionEmisionPago_OUT out = new SuscripcionEmisionPago_OUT();
		SQL _sql = new SQL();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();

		HashMap<String, Object> params = new HashMap<>();
		Map<String, Object> instrumentosMap = new HashMap<>();
		Map<String, Object> datosValidacionMap = new HashMap<>();
		Map<String, Object> suscripcionMap = new HashMap<>();
		Map<String, Object> emisionPagadaMap = new HashMap<>();
		List<Long> IdInstrumentoNuevo= new ArrayList<>();
		List<InstrumentoRelacionado> instrumentoRelacionado = new ArrayList<>();
		List<DatosValidacion> datosValidacion = new ArrayList<>();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String fechaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String fechaHoraSistema 	= FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
		Long vIdEventoCapital = 0L;
		Long vSecuenciaIdInterno = 0L;
		
		params = new HashMap<>();

		List<Long> emisionesPagadas = new ArrayList<>();
		List<Long> emisionesPagadasSec = new ArrayList<>();
		List<Long> idsCliente = new ArrayList<>();
		HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();
		
		SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
		
		for (SuscripcionEmiPago recorreEmisiones: in.p_Suscripcion){
			vSecuenciaIdInterno++;
			recorreEmisiones.p_IdInterno = vSecuenciaIdInterno; //no recibe idInterno asi que se agrega para mensajes solamente
			emisionesPagadas.add(recorreEmisiones.p_NumeroDividendo);
			emisionesPagadasSec.add(recorreEmisiones.p_Secuencia);
			idsCliente.add(recorreEmisiones.p_IdCliente);

			//port.IdMoneda = recorreEmisiones.p_IdMoneda;
			port.IdPortafolio = recorreEmisiones.p_IdPortafolio;
			portafoliosFlujoCaja.add(port);
		}
		
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("NumeroDividendo", emisionesPagadas);
		params.put("IdTipoEventoCapital",  "EP");
		params.put("Secuencia",emisionesPagadasSec);
		List<EventoCapital> eventoEmisionPagada = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);
		emisionPagadaMap = Utils.List2Hash(eventoEmisionPagada, "NumeroDividendo");
		
		//Obtiene solo IdMoneda de proceso
		if (eventoEmisionPagada.size() > 0){
			for (EventoCapital data:eventoEmisionPagada){
				port.IdMoneda = data.IdMonedaLiquidacion;
			}
		}
		
		if (eventoEmisionPagada.size() > 0){
			params = new HashMap<>();
			for (EventoCapital data:eventoEmisionPagada){
				IdInstrumentoNuevo.add(data.IdInstrumento);
			}
			params.put("p_IdInstrumento",IdInstrumentoNuevo);
			instrumentoRelacionado = _sql.query(conn, SQL_SELECT_INSTRUMENTO_RELACIONADO, params, InstrumentoRelacionado.class);
			instrumentosMap = Utils.List2Hash(instrumentoRelacionado, "IdInstrumento");
		}else{
			//TODO: error
		};
		
		if (instrumentoRelacionado.size() == 0){
			//error, no existe el nemo relacionado.
			mapMsg.clear();
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_INSTRUMENTO_RELACIONADO",mapMsg,conn,in.p_IdEmpresa));
			Id++;
		};

		if (idsCliente.size() > 0){
			params = new HashMap<>();
			params.put("IdsCliente", idsCliente);
			datosValidacion = _sql.query(conn, SQL_SELECT_DATOS_VALIDACION, params, DatosValidacion.class);
			datosValidacionMap = Utils.List2Hash(datosValidacion, "IdCliente");
		}else{
			//TODO: error
		};
		

		SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
    	entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
    	entradaFlujoCaja.P_FECHADESDE = fechaSistema;
		entradaFlujoCaja.P_FECHAHASTA = fechaSistema;
		entradaFlujoCaja.P_USO = "CUENTA_INVERSION";
		entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(portafoliosFlujoCaja);
		
		SaldoFlujoCaja saldoFlujoCajaService = new SaldoFlujoCaja();
		SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = saldoFlujoCajaService.logica(conn,entradaFlujoCaja);

		//TODO MANEJO DE ERRORES
		List<ObjFLUJOCAJA> asdf = salidaFlujoCaja.P_OUT_FLUJOCAJA;
		for (ObjFLUJOCAJA flj : asdf) {
			for (SuscripcionEmiPago sus : in.p_Suscripcion) {
	        	if (sus.p_IdPortafolio.equals(flj.IdPortafolio)) {
	        		sus.SaldoCajaPortafolio = flj.Saldo;
	        	}
	        }
		}
		//requisitos
		HashMap<String, RequisitoOperativoCrearOrden> requisitos = new HashMap<>();
		{
			HashMap<String,Object> paramsInRequisitosOperativos = new HashMap<>();
			paramsInRequisitosOperativos.put("IdEmpresa", in.p_IdEmpresa);
			List<RequisitoOperativoCrearOrden> _requisitos = _sql.query(conn, SQL_SELECT_REQUISITOS_OPERATIVOS,paramsInRequisitosOperativos,RequisitoOperativoCrearOrden.class);
			HashMap<Object,Object> __requisitos = Utils.List2Hash(_requisitos, "IdTipoCanalIngreso");
			for(Object key: __requisitos.keySet()) {
				requisitos.put((String)key,(RequisitoOperativoCrearOrden) __requisitos.get(key));
			}
		}
		
		//VALIDACIONES BASICAS
		{
			List<CalculoEntrada> calculosEntrada = new ArrayList<>();
			
			for (SuscripcionEmiPago sus: in.p_Suscripcion) {
				// CHEQUEAR VALOR DE LA MONEDA SEGUN MONEDA LIQUIDACION
				
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.Fecha = fechaSistema;
				calculoEntrada.IdInterno = sus.p_IdCliente;
				calculoEntrada.IdMonedaOrigen = eventoEmisionPagada.get(0).IdMoneda;
				calculoEntrada.IdMonedaDestino = eventoEmisionPagada.get(0).IdMonedaLiquidacion;
				calculoEntrada.IdTrabajo = 0L;
				calculoEntrada.IdUso = "";
				calculoEntrada.MontoOrigen = sus.p_CantidadNueva.multiply(sus.p_Precio);
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				
				calculosEntrada.add(calculoEntrada);
			}
			
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 0L;
			calcularParidadEntrada.p_CalculosEntrada = calculosEntrada;
			
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(conn, calcularParidadEntrada);
			
			for (int i = 0; i < in.p_Suscripcion.size(); i++) {
			    SuscripcionEmiPago sus = in.p_Suscripcion.get(i);
			    CalculoSalida calculoSalida = calcularParidadSalida.p_out_CalculosSalida.get(i);
			    
			    BigDecimal montoDestino = calculoSalida.MontoDestino;
			    BigDecimal montoOrigen = calculoSalida.MontoOrigen;
			    sus.p_Precio = (montoDestino.divide(montoOrigen)).multiply(sus.p_Precio); // Precio según moneda de liquidación

			    DatosValidacion dato = (DatosValidacion) datosValidacionMap.get(sus.p_IdCliente);
			    ProcesaRequisito(conn, in.p_IdEmpresa, (!dato.IdEstadoCliente.equals("ACT")), "CLIENTE_BLOQUEADO", "CO_ClienteBloqueado", sus, requisitos, out.P_OUT_MENSAJES);
			    ProcesaRequisito(conn, in.p_IdEmpresa, (montoDestino.doubleValue() > sus.SaldoCajaPortafolio.doubleValue()), "FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO", "CO_OperacionSinFlujoCaja", sus, requisitos, out.P_OUT_MENSAJES);
			}		
		}
		
		if(!out.P_OUT_MENSAJES.isEmpty()) {
			return out;
		}
		params = new HashMap<>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("NumeroDividendo", emisionesPagadas);
		params.put("IdTipoEventoCapital","SU");
		params.put("Secuencia", emisionesPagadasSec);
		List<EventoCapital> eventoEmisionSuscripcion = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);
		suscripcionMap = Utils.List2Hash(eventoEmisionSuscripcion, "NumeroDividendo");
		List<Long> insertEventoCapCli = new ArrayList<>();
		for (SuscripcionEmiPago recorreEntrada: in.p_Suscripcion){
			
			if (eventoEmisionSuscripcion.size() == 0 ){
				List<Evento> listaEventos = new ArrayList<>();
				Long vIdInstrumentoRelacionado = 0L;
				String vNemoRelacionado = new String();
				for (EventoCapital recorreEmPag : eventoEmisionPagada){
					Evento regEvento = new Evento();
					regEvento.p_IdTipoEventoCapital					= "SU";
					regEvento.p_Secuencia							= recorreEmPag.Secuencia;
					regEvento.p_Descripcion							= recorreEmPag.Descripcion;
					regEvento.p_EsInscrito							= recorreEmPag.EsInscrito;
					regEvento.p_IdClasificacionTipoInstrumento		= recorreEmPag.IdClasificacionTipoInstrumento;
					regEvento.p_IdTipoInstrumento					= recorreEmPag.IdTipoInstrumento;
					regEvento.p_IdInstrumento						= recorreEmPag.IdInstrumento;
					regEvento.p_Nemo								= recorreEmPag.Nemo;
					
					vIdInstrumentoRelacionado						= ((InstrumentoRelacionado) instrumentosMap.get(recorreEmPag.IdInstrumento)).IdInstrumentoRelacionado;
					vNemoRelacionado								= ((InstrumentoRelacionado) instrumentosMap.get(recorreEmPag.IdInstrumento)).NemoRelacionado;
					
					regEvento.p_Factor								= recorreEmPag.Factor;
					regEvento.p_Impuesto							= recorreEmPag.Impuesto;
					regEvento.p_IdMoneda							= recorreEmPag.IdMoneda;
					regEvento.p_IdMonedaLiquidacion					= recorreEmPag.IdMonedaLiquidacion;
					regEvento.p_Paridad								= recorreEmPag.Paridad;
					regEvento.p_IdInstrumentoNuevo					= ((InstrumentoRelacionado) instrumentosMap.get(recorreEmPag.IdInstrumento)).IdInstrumentoRelacionado;//recorreEmPag.IdInstrumentoNuevo;
					regEvento.p_NemoNuevo							= ((InstrumentoRelacionado) instrumentosMap.get(recorreEmPag.IdInstrumento)).NemoRelacionado;//recorreEmPag.NemoNuevo;
					regEvento.p_FactorNuevo							= recorreEmPag.FactorNuevo;
					regEvento.p_FechaActualizacion					= recorreEmPag.FechaActualizacion.toString();
					regEvento.p_FechaLimite							= recorreEmPag.FechaLimite.toString();
					regEvento.p_FechaPago							= recorreEmPag.FechaPago.toString();
					regEvento.p_NumeroDividendo						= recorreEmPag.NumeroDividendo;
					regEvento.p_IdTipoDividendo						= recorreEmPag.IdTipoDividendo;
					regEvento.p_PrecioAnterior						= recorreEmPag.PrecioAnterior;
					regEvento.p_FechaHoraIngresoRegistro			= fechaSistema;
					regEvento.p_FechaHoraUltimaActualizacion		= fechaSistema;
					regEvento.p_TipoIngreso							= recorreEmPag.TipoIngreso;
					regEvento.p_IdEstado							= "VIG";
					regEvento.p_IdEmisor							= recorreEmPag.IdEmisor;
					regEvento.p_IdEmisorNuevo						= recorreEmPag.IdEmisorNuevo;
					regEvento.p_Monto								= recorreEmPag.Monto;
					regEvento.p_Cuotas								= recorreEmPag.CantidadCuotas;
					regEvento.p_Origen								= recorreEmPag.Origen;
					regEvento.p_IdEmpresa							= recorreEmPag.IdEmpresa;
					regEvento.p_Valor								= recorreEmPag.Valor;
					regEvento.p_IdUsuario							= recorreEmPag.IdUsuario;
					regEvento.p_Indice								= recorreEmPag.Indice;
					regEvento.p_SuscripcionComplementaria 			= recorreEmPag.SuscripcionComplementaria;
					regEvento.p_SecuenciaRelacionada 				= recorreEmPag.SecuenciaRelacionada;
					regEvento.p_EsEP 								= true;
					
					listaEventos.add(regEvento);
				}
				
				CrearEventoCapital crearEvCap = new CrearEventoCapital();
				EventoCapital_IN paramCrearEvCap = new EventoCapital_IN();
				
				paramCrearEvCap.p_IdEmpresa			= in.p_IdEmpresa;
				paramCrearEvCap.p_IdUsuario			= in.p_IdUsuario;
				paramCrearEvCap.p_IdModulo			= in.p_IdModulo;
				paramCrearEvCap.p_UsaReglasNegocio	= "S";
				paramCrearEvCap.p_EventoCapital		= listaEventos; 

				EventoCapital_OUT salidaCreaEventoCapital	= crearEvCap.logica(conn, paramCrearEvCap);
				
				//TODO: llamamos al crear Clientes
				EventoCapitalCliente_OUT salidaEvCapCliente = InsertarRegistroCliente (	conn, 
																in.p_IdEmpresa, 
																in.p_IdModulo, 
																in.p_IdUsuario, 
																vIdInstrumentoRelacionado,
																vNemoRelacionado,
																recorreEntrada, 
																((EventoCapital) emisionPagadaMap.get(recorreEntrada.p_NumeroDividendo)), 
																salidaCreaEventoCapital.P_OUT_SECUENCIA);
				
			    out.P_OUT_MENSAJES.addAll(salidaCreaEventoCapital.P_OUT_MENSAJES);
			    out.P_OUT_SECUENCIA.addAll(salidaCreaEventoCapital.P_OUT_SECUENCIA);
			    out.P_OUT_MENSAJES.addAll(salidaEvCapCliente.P_OUT_MENSAJES);
			    out.P_OUT_SECUENCIA.addAll(salidaEvCapCliente.P_OUT_SECUENCIA);
			    insertEventoCapCli.addAll(salidaEvCapCliente.P_OUT_SECUENCIA);
			}else{
				List<Long> listaIdEventos = new ArrayList<>();
				//obtenemos el id del evento para poder insertar a los clientes suscritos.
				listaIdEventos.add(((EventoCapital) suscripcionMap.get(recorreEntrada.p_NumeroDividendo)).IdEventoCapital);
				//vIdEventoCapital					= ((EventoCapital) suscripcionMap.get(recorreEntrada.p_NumeroDividendo)).IdEventoCapital;
				//TODO: llamamos al crear Clientes
				List<Evento> listaEventos = new ArrayList<>();
				Long vIdInstrumentoRelacionado = 0L;
				String vNemoRelacionado = new String();
				for (EventoCapital recorreEmPag : eventoEmisionPagada){
					Evento regEvento = new Evento();
					vIdInstrumentoRelacionado						= ((InstrumentoRelacionado) instrumentosMap.get(recorreEmPag.IdInstrumento)).IdInstrumentoRelacionado;
					vNemoRelacionado								= ((InstrumentoRelacionado) instrumentosMap.get(recorreEmPag.IdInstrumento)).NemoRelacionado;
					
					listaEventos.add(regEvento);
				}
				//TODO: llamamos al crear Clientes
				EventoCapitalCliente_OUT salidaEvCapCliente = InsertarRegistroCliente (	conn, 
																in.p_IdEmpresa, 
																in.p_IdModulo, 
																in.p_IdUsuario, 
																vIdInstrumentoRelacionado,
																vNemoRelacionado,
																recorreEntrada, 
																((EventoCapital) emisionPagadaMap.get(recorreEntrada.p_NumeroDividendo)), 
																listaIdEventos);
				
				
			    out.P_OUT_MENSAJES.addAll(salidaEvCapCliente.P_OUT_MENSAJES);
			    out.P_OUT_SECUENCIA.addAll(salidaEvCapCliente.P_OUT_SECUENCIA);
			    insertEventoCapCli.addAll(salidaEvCapCliente.P_OUT_SECUENCIA);
			}
			
		}
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
				
				
		log.write("FIN::CUS.SuscipcionEmisionPagoManual");
			
		return out;
				
	};
	
	private EventoCapitalCliente_OUT InsertarRegistroCliente(	Connection con, 
											Long p_IdEmpresa, 
											String p_IdModulo, 
											Long p_IdUsuario,
											Long IdInstrumentoRelacionado,
											String NemoRelacionado,
											SuscripcionEmiPago recorreEntrada , 
											EventoCapital emisionPagada, 
											List<Long> idEventoCapital) throws Exception{
		Long idEventoCliente = (0L);
		List<EventoCL> eventosCL = new ArrayList<>(); 
		EventoCL eventoCL = new EventoCL();
		
		eventoCL.p_IdEventoCapital					= idEventoCapital.size()>0?idEventoCapital.get(0):0L;
		eventoCL.p_IdTipoEventoCapital				= "SU";//recorreEntrada.p_IdTipoEventoCapital;
		eventoCL.p_NumeroDividendo					= emisionPagada.NumeroDividendo;
		eventoCL.p_Secuencia						= emisionPagada.Secuencia;
		eventoCL.p_IdSocioNegocio					= recorreEntrada.p_IdSocioNegocio;
		eventoCL.p_TipoSocioNegocio					= recorreEntrada.p_TipoSocioNegocio;
		eventoCL.p_IdPortafolio						= recorreEntrada.p_IdPortafolio;
		eventoCL.p_IdTipoDocumentoIdentidad			= "";
		eventoCL.p_IdCliente						= recorreEntrada.p_IdCliente;
		eventoCL.p_IdMoneda 						= emisionPagada.IdMonedaLiquidacion;
		eventoCL.p_MontoCapital						= recorreEntrada.p_CantidadNueva.multiply(recorreEntrada.p_Precio);
		eventoCL.p_CantidadDerecho					= BigDecimal.ZERO;
		eventoCL.p_CantidadNueva					= recorreEntrada.p_CantidadNueva;
		eventoCL.p_IdClasificacionTipoInstrumento	= emisionPagada.IdClasificacionTipoInstrumento;
		eventoCL.p_IdTipoInstrumento				= emisionPagada.IdTipoInstrumento;
		eventoCL.p_NemoNuevo						= NemoRelacionado;
		eventoCL.p_FechaPago						= emisionPagada.FechaPago.toString();
		eventoCL.p_Factor							= emisionPagada.Factor;
		eventoCL.p_Descripcion						= emisionPagada.Descripcion;
		eventoCL.p_IdInstrumento					= emisionPagada.IdInstrumento;
		eventoCL.p_IdInstrumentoNuevo				= IdInstrumentoRelacionado;
		eventoCL.p_Nemo								= emisionPagada.Nemo;
		eventoCL.p_IdEstado							= "VIG";
		eventoCL.p_TipoCartera						= "CUS";
		eventoCL.p_IdBoveda							= recorreEntrada.p_IdBoveda;
		eventoCL.p_Precio							= recorreEntrada.p_Precio;
		eventoCL.p_SecuenciaRelacionada 			= emisionPagada.SecuenciaRelacionada;
		eventoCL.p_SuscripcionComplementaria 		= emisionPagada.SuscripcionComplementaria;
		
	
		
		eventosCL.add(eventoCL);
		
		CrearEventoCapitalCliente insertaEveCapCli = new CrearEventoCapitalCliente();
		EventoCapitalCliente_IN paramEntradaEvCapCli = new EventoCapitalCliente_IN();
		
		paramEntradaEvCapCli.p_IdEmpresa		= p_IdEmpresa;
		paramEntradaEvCapCli.p_IdUsuario		= p_IdUsuario;
		paramEntradaEvCapCli.p_IdModulo			= p_IdModulo;
		paramEntradaEvCapCli.p_UsaReglasNegocio	= "S";
		paramEntradaEvCapCli.p_EventoCapital	= eventosCL; 
		
		
		EventoCapitalCliente_OUT salidaInserEvCapCli = insertaEveCapCli.logica(con, paramEntradaEvCapCli);
		
		return salidaInserEvCapCli; 
	}

//	private EventoCapital_OUT SuscipcionEmisionPagoManual (Connection conn, EventoCapital_IN in)  throws Exception {}

	public String obtenerValorRequisitoOperativo(HashMap<String, RequisitoOperativoCrearOrden> requisitos,String IdTipoCanalIngreso,String requisitoOperativo) throws IllegalArgumentException, IllegalAccessException {
		if (requisitos.get(IdTipoCanalIngreso) == null &&  requisitos.get("0") == null) return "OK";
		RequisitoOperativoCrearOrden reqCanal = requisitos.get(IdTipoCanalIngreso);
		RequisitoOperativoCrearOrden reqTodos = requisitos.get("0");
		Field[] fields = reqCanal.getClass().getDeclaredFields();
		for (Field f: fields) {
			if (f.getName().equalsIgnoreCase(requisitoOperativo)) {
				String requisitoCanal = (String) f.get(reqCanal);
				String requisitoTodos = null;
				if (f.get(reqTodos)!= null) {
					requisitoTodos = (String) f.get(reqTodos);
				} else {
					requisitoTodos = null;
				}
				if (requisitoCanal != null){
					return requisitoCanal;
				}
				if (requisitoTodos != null){
					return requisitoTodos;
				}
				else {
					return "OK";
				}
			}
		}
		return "";
	}
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String CodRequisito,SuscripcionEmiPago sus,HashMap<String, RequisitoOperativoCrearOrden> requisitos,List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		MensajeSalida objMensaje = new MensajeSalida(); //JALA para manejar mejora proceso de Autorizaciones
		if (esError) {
			HashMap<String,String> valores = new HashMap<String,String>();
			valores.put("DocumentoIdentidad", sus.p_DocumentoIdentidad);
			valores.put("CodigoPortafolio", sus.p_CodigoPortafolio);
		
			
			//valores.put("IdPreComprobante", String.valueOf(precomprobante.IdPreComprobante));
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
					  .add("CodigoPortafolio", sus.p_CodigoPortafolio)
					  .add("DocumentoIdentidad", sus.p_DocumentoIdentidad).msg();
			
				//si el comportamiento viene de requisitos operativos
				String requisito=obtenerValorRequisitoOperativo(requisitos, sus.IdTipoCanalIngreso, CodRequisito);
				if (requisito.equals(MensajeSalida.MENSAJE_ERROR) || requisito.equals(MensajeSalida.MENSAJE_RECHAZO)) { //TODO ELIMINAR MENSAJE RECHAZO
					objMensaje = new MensajeSalida(sus.p_IdInterno,MensajeSalida.MENSAJE_ERROR,codMsg,valores,con,idEmpresa);
					P_OUT_MENSAJES.add(objMensaje);
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
					objMensaje = new MensajeSalida(sus.p_IdInterno,requisito,codMsg,msg);
					P_OUT_MENSAJES.add(objMensaje);
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
					objMensaje = new MensajeSalida(sus.p_IdInterno,requisito,codMsg,valores,con,idEmpresa);
					P_OUT_MENSAJES.add(objMensaje);
				}
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		SuscripcionEmisionPago_IN jin = gson.fromJson(in.toString(), SuscripcionEmisionPago_IN.class);
		SuscripcionEmisionPago_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("qa02","connections.properties");
			//datos entrada
			
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 97,\r\n"
					+ "  \"p_IdModulo\": \"CUS\",\r\n"
					+ "  \"p_Suscripcion\": [\r\n"
					+ "    {\r\n"
					+ "      \"p_IdEmpresa\": 1,\r\n"
					+ "      \"p_IdUsuario\": 97,\r\n"
					+ "      \"p_IdModulo\": \"CUS\",\r\n"
					+ "      \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "      \"p_Precio\": 1,\r\n"
					+ "      \"p_IdEventoCapital\": 40649,\r\n"
					+ "      \"p_IdTipoEventoCapital\": \"EP\",\r\n"
					+ "      \"p_Secuencia\": 100000503,\r\n"
					+ "      \"p_IdCliente\": 2742,\r\n"
					+ "      \"p_DocumentoIdentidad\": \"15652777-7\",\r\n"
					+ "      \"p_IdSocioNegocio\": 25046,\r\n"
					+ "      \"p_TipoSocioNegocio\": \"CLI\",\r\n"
					+ "      \"p_IdPortafolio\": 7509,\r\n"
					+ "      \"p_CodigoPortafolio\": \"0\",\r\n"
					+ "      \"p_IdTipoDocumentoIdentidad\": \"R\",\r\n"
					+ "      \"p_NombreCliente\": \"PABLO ESTEBAN FERNANDEZ CACERES\",\r\n"
					+ "      \"p_IdClasificacionTipoInstrumento\": \"RV\",\r\n"
					+ "      \"p_IdTipoInstrumento\": \"ACN\",\r\n"
					+ "      \"p_IdInstrumento\": 560,\r\n"
					+ "      \"p_Nemo\": \"CRUZADOS\",\r\n"
					+ "      \"p_IdInstrumentoNuevo\": 0,\r\n"
					+ "      \"p_NemoNuevo\": \"\",\r\n"
					+ "      \"p_FechaCierre\": \"2024-07-22\",\r\n"
					+ "      \"p_FechaPago\": \"2024-07-26\",\r\n"
					+ "      \"p_CantidadDerecho\": 0,\r\n"
					+ "      \"p_TipoCartera\": \"CUS\",\r\n"
					+ "      \"p_CantidadNueva\": 200,\r\n"
					+ "      \"p_MontoCapital\": 0,\r\n"
					+ "      \"p_MontoInteres\": 0,\r\n"
					+ "      \"p_Total\": 0,\r\n"
					+ "      \"p_IdMoneda\": \"CLP\",\r\n"
					+ "      \"p_SecuenciaCupon\": 0,\r\n"
					+ "      \"p_IdEmpleadoEjecutivoPortafolio\": 89,\r\n"
					+ "      \"p_CodigoUsuarioAgente\": \"FMH\",\r\n"
					+ "      \"p_NombreAgente\": \"FRANCISCO METTROZ HOLLEY\",\r\n"
					+ "      \"p_CodigoSucursal\": \"SU01\",\r\n"
					+ "      \"p_IdBoveda\": \"DCV\",\r\n"
					+ "      \"p_IdBolsa\": \"BCS\",\r\n"
					+ "      \"p_NumeroDividendo\": 10\r\n"
					+ "    }\r\n"
					+ "  ]\r\n"
					+ "}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"CUS\",\"p_Suscripcion\":[{\"p_IdEventoCapital\":1117,\"p_IdTipoEventoCapital\":\"EP\",\"p_IdCliente\":100059,\"p_CantidadDerecho\":117615,\"p_IdSocioNegocio\":100198,\"p_TipoSocioNegocio\":\"CLI\",\"p_IdPortafolio\":100069,\"p_FechaSu\":\"2019-06-25\",\"p_CantidadNueva\":123,\"p_NumeroDividendo\":169,\"p_Secuencia\":169,\"p_Precio\":5800,\"p_IdBoveda\":\"DCV\"}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_Suscripcion\":[{\"p_IdEventoCapital\":366477,\"p_IdTipoEventoCapital\":\"EP\",\"p_IdCliente\":249199,\"p_CantidadDerecho\":164000,\"p_IdSocioNegocio\":245889,\"p_TipoSocioNegocio\":\"CLI\",\"p_IdPortafolio\":153970,\"p_FechaSu\":\"2019-06-24\",\"p_CantidadNueva\":120,\"p_NumeroDividendo\":127,\"p_Precio\":1,\"p_IdBoveda\":\"DCV\",\"p_Secuencia\":127}]}";
			
			SuscripcionEmisionPago_IN objIn = gson.fromJson(json,SuscripcionEmisionPago_IN.class);
			SuscripcionEmisionPago demo = new SuscripcionEmisionPago();
			SuscripcionEmisionPago_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
