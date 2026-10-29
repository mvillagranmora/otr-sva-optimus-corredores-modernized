package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.custodia.AnularMovimientosEnTransito;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjEstadoOperacionOUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_IN;
import cl.bcs.mesa.MRF.pojo.CacheMRF;
import cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.CacheServicio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnularRegistros;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Entrada;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Salida;
import cl.bcs.tesoreria.AnularRegistros.Registro_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularOperacionMesa implements JavaCall{
	private final Log log = new Log("MRF.AnularOperacionMesa");
	private static final Logger logger = LogManager.getLogger(AnularOperacionMesa.class);
	
	private final String SQL_SELECT_CONCEPTOS_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarConceptosMesa.sql");
	
	private final String SQL_SELECT_DETALLE_OPERACION = loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectOperacion.sql");
	
	private final String SQL_SELECT_DETALLE_RETIRO_CARTERA 			= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraRetiro.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_CARTERA_ORIGEN 	= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraRetiroOrigen.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_PACTO 			= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectPactoRetiro.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_PACTO_SUBYACENTE = loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectPactoSubyacenteRetiro.sql");
	private final String SQL_SELECT_DETALLE_CUENTA_INVERSION 	= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectDetalleCuentaInversion.sql");
	private final String SQL_SELECT_DETALLE_TESORERIA 			= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectDetalleTesoreria.sql");
	private final String SQL_SELECT_DETALLE_MOVIMIENTO_TRANSITO = loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectDetalleMovimientoEntransito.sql");
	private final String SQL_SELECT_DETALLE_CARTERA_RESERVADA	= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraReservada.sql");
	private final String SQL_SELECT_DETALLE_CARTERA_COMPROMETIDA	= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraComprometida.sql");
	
	private final String SQL_SELECT_DETALLE_RETIRO_CARTERA_TERMINO = loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectDetalleCarteraTermino.sql");  //cambiar
	private final String SQL_SELECT_DETALLE_CARTERAS_ORIGEN = loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectDetalleCarteraOrigen.sql");
	private final String SQL_SELECT_DETALLE_AFECTA_CUSTODIA = loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectDetalleAfectaCustodia.sql");
	
	private final String SQL_SELECT_ID_CARTERA_RESERVA = loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectcarteraReserva.sql");
	
	
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	
	
	public static class AnularOperacionMesa_IN extends BcsPojo {
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String 				p_IdModulo; 
		public String				p_UsaReglasNegocio;
		public List<Operacion> 		p_Operaciones;
		
		public AnularOperacionMesa_IN() {
			this.p_Operaciones 		= new ArrayList<Operacion>();
		}

	}
	
	public static class Operacion {
		public Long		IdOperacion;
		public String	IdEstadoSiguiente;
		public String 	EstadoConfirmacion = "NCF";
	}
		
	public static class AnularOperacionMesa_OUT extends BcsPojo{

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ObjEstadoOperacionOUT>	P_OUT_OPERACIONES;
		
		public AnularOperacionMesa_OUT() {
			this.P_OUT_OPERACIONES 		= new ArrayList<ObjEstadoOperacionOUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}

	}
	

	public static class DetalleValidacion extends BcsPojo{
		public Long				IdOperacion;
		public Long 			FolioCarteraCP;
		public String 			Nemotecnico;
		public String 			Emisor;		

	    public BigDecimal 		CantidadCP;
	    public BigDecimal 		CantidadCartera;
	    public BigDecimal 		CantidadReservada;

	}
	
	
	
	public static class DetalleOperacion extends BcsPojo {
		public Long				IdOperacion;
		public String			IdTipoProducto;
		public Long 			IdCliente;
		public Long 			IdPortafolio;
		public Long 			IdClienteCorredor;
		public Long 			IdPortafolioCorredor;
		public String 			IdTipoOperacion;
		public Long				IdInstrumentoPacto;
		public String			IdTipoCanalIngreso;
		public Long 			FolioOperacion;
		public Long				FolioCarteraPacto;
		public Long				IdCarteraPacto;
		public BigDecimal		ValorRescatePacto;
		
		public Long				IdReserva;
		public Long				IdInstrumentoDetalle;
	    public BigDecimal 		CantidadDetalle;
	    public BigDecimal 		ValorRescateDetalle;
	    public BigDecimal		MontoOperacion;
	    public BigDecimal 		MontoValorizadoDetalle;
	    public Long 			IdCarteraOrigenDetalle;
	    public Long 			FolioCarteraOrigenDetalle;	    
	    public Long 			IdCarteraDestinoDetalle;
	    public Long 			FolioCarteraDestinoDetalle;
	    public Long 			IdCarteraSubyacenteOrigenDetalle;
	    public BigDecimal 		CantidadCarteraDestinoDetalle;
	    public BigDecimal 		ValorRescateDestinoDetalle;
	    public Date				FechaLiquidacion;
	    public Date 			FechaVencimientoInstrumento;
	    public java.sql.Timestamp FechaVencimientoInstrumentoLimite;
	    public Long				PlazoEnDias;
	    public String			EsFungible;
	    public String			IdEmisor;
	    public BigDecimal		PrecioCompra;
	    public BigDecimal		Tasa;
	    public BigDecimal		TasaCompra;
	    
	    public String			IdTipoReajuste;
	    public String			IdMonedaEmision;
	    public String			IdMonedaInstrumento;
	    public String			IdEstadoCartera;
	    
	    public String 			EstadoConfirmacion;
	    public String			IdClasificacionTipoInstrumento = "";
	    
	    public Long				IdInterno;
	    public String			Glosa = "";


	}
	public static class EntidadTesoreria_Proceso extends BcsPojo{
		
		public Long IdComprobanteOrigen;
		public Long Id;
		public String Entidad;
		public String NombreTipoMovimiento;
		public String IdEstado; 
		
	}
	public static class CargoAbono_Proceso extends BcsPojo {
		public Long IdComprobante;
		public String IdTipoComprobante;
		public Long IdCargoAbonoMovimiento;
		public String NombreTipoMovimiento;
		public String IdEstado;

		
	}
	public static class Concepto extends BcsPojo{
		public String IdConcepto;
	    public String NombreConcepto;
	    public String IdTipoProducto;
	    public String IdTipoOperacion;
	    public String Uso;
	    public String IdConceptoRelacionado;
	    public String NombreConceptoRelacionado;

	}
	
	public static class ObjCarteraOrigen extends BcsPojo{
		public Long FolioCartera;
	    public Long IdCartera;
	    public BigDecimal INGValorRescate;
	    public BigDecimal INGCantidad;
	}
	
	public static class DetalleAfectaCustodia extends BcsPojo{
		public String AfectaCustodia;
		public String IdTipoOperacion;
	    public Long FolioCarteraOrigenDetalle;
	    public Long IdCarteraOrigenDetalle;
	}
	
	public static class MensajeDinamico extends BcsPojo{
		
	    public Long IdOperacion;
	    public String IdEstado;

	}
	

	public static class VerificarCartera  extends BcsPojo  {
		public Long FolioCartera;
		public Long IdCartera;
		public String IdEstado;
		
	}
	
	public AnularOperacionMesa_OUT logica (Connection conn, AnularOperacionMesa_IN in) throws Exception {
		SQL _sql = new SQL();
		log.write("INICIO::MRF.AnularOperacionMesa");
		
		boolean esRechazo = false;
		
		HashMap<String,String> mapMsg = new HashMap<>();
		AnularOperacionMesa_OUT out = new AnularOperacionMesa_OUT();
		Long Id = 0L;
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
	    Long idPaisSistema = empresa.IdPais;
	    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	    HashMap<String, Object> paramsDetOpIn = new HashMap<String, Object>();
	    List<Object> par = new ArrayList<>();
	    Long TicketAtencion = 0L;
	    
	    List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
	    List<IngresoCustodia.ObjDatosEntrada> p_IngresoCartera = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
	    //List<RetiroCustodia.ObjDatosEntrada> p_RetiroCarteraSubyacentePacto = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
	    
	    
		//busqueda de conceptos usado en mesa
		Concepto conceptoAnulacion = null;
		HashMap<String,Object> paramConcep = new HashMap<>();
		paramConcep.put("IdTipoProducto", "MERF");
		List<Concepto> selectConcepto = _sql.query(conn, SQL_SELECT_CONCEPTOS_MESA, paramConcep, Concepto.class);
		for (Concepto con: selectConcepto){
			if (con.Uso.equals("ANULACION")){
				conceptoAnulacion = con;
			}
		}
	    
	    for (Operacion op: in.p_Operaciones){
	    	par.add(op.IdOperacion);
	    	if (op.EstadoConfirmacion.equalsIgnoreCase("RZD")) {
	    		esRechazo= true;
	    	}	
	    		
	    }
	    Id = 0L;
	    paramsDetOpIn.put("IdOperacion", par);
	    List<DetalleOperacion> detOpe = _sql.query(conn, SQL_SELECT_DETALLE_OPERACION, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpeCarteraRetiro = _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_CARTERA, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpeCarteraOrigenRetiro = _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_CARTERA_ORIGEN, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpePactoRetiro = _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_PACTO, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpePactoSubyacenteRetiro = _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_PACTO_SUBYACENTE, paramsDetOpIn, DetalleOperacion.class);	   
	    List<AnularMovimientosEnTransito.ObjMovimientos> detOpeTransito = _sql.query(conn, SQL_SELECT_DETALLE_MOVIMIENTO_TRANSITO,paramsDetOpIn, AnularMovimientosEnTransito.ObjMovimientos.class);
	    List<DetalleValidacion> detOpeReservada = _sql.query(conn, SQL_SELECT_DETALLE_CARTERA_RESERVADA, paramsDetOpIn, DetalleValidacion.class);
	    List<DetalleValidacion> detOpeComprometida = _sql.query(conn, SQL_SELECT_DETALLE_CARTERA_COMPROMETIDA, paramsDetOpIn, DetalleValidacion.class);
	    List<DetalleAfectaCustodia> detAfectaCustodia = _sql.query(conn, SQL_SELECT_DETALLE_AFECTA_CUSTODIA, paramsDetOpIn, DetalleAfectaCustodia.class);
	   
	    // SE COMPRUEBA QUE LA OPERACION NO SEA INCOMPLETA:
	    Boolean OpeIncompleta = false;
	    if (detOpeCarteraRetiro.isEmpty() &&
	    	detOpeCarteraOrigenRetiro.isEmpty() &&
	    	detOpePactoRetiro.isEmpty() &&
	    	detOpePactoSubyacenteRetiro.isEmpty() &&
	    	detOpeTransito.isEmpty() &&
	    	detOpeReservada.isEmpty() &&
	    	detOpeComprometida.isEmpty() &&
	    	detAfectaCustodia.isEmpty()) {
	    	    OpeIncompleta = true;
	    }
	    
	    HashMap<Long, Object> inOperacionesHash = Utils.List2Hash(in.p_Operaciones, "IdOperacion");
	    List<EntidadTesoreria_Proceso> entidadesTesoreria= _sql.query(conn, SQL_SELECT_DETALLE_TESORERIA, paramsDetOpIn, EntidadTesoreria_Proceso.class);
	    
	    out = this.validacionesBasicas(conn,entidadesTesoreria,detOpe,detOpeReservada,detOpeComprometida,detOpeCarteraRetiro,conceptoAnulacion,in,out);
	    
	    //Valido Estado Comprobante Liquidacion
	    
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!out.P_OUT_ERRORESCONTADOR.equals(0L) ){
			return out;
		}
		
		//RETIRO CARTERA CABECERA		
		p_RetiroCartera.addAll(this.obtenerCabecera(detOpePactoRetiro,inOperacionesHash,fechaSistema,conceptoAnulacion));
		
	   
  		Long idInternoLimite = 0L;
  		for(DetalleOperacion det: detOpe) {
  			det.IdInterno = idInternoLimite++;
  		}
  		
  		if(!OpeIncompleta) {
  			List<MensajeSalida> mensajesValidarLimitesCuentaPropia = obtenerMensajeAdvertenciaLimites(conn,in,detOpe);
  		    if(mensajesValidarLimitesCuentaPropia.size() > 0) {
  		    	out.P_OUT_MENSAJES.addAll(mensajesValidarLimitesCuentaPropia);
  		    }
  		} 		  		
	    
	    //REVISAR ID CARTERAS
	    List<DetalleOperacion> detOpeCarteraOrigenRetiroVerificado = this.verificarIdCarteraVigente(conn, detOpeCarteraOrigenRetiro);
	    //RETIRO CARTERA PAPEL  
	    p_RetiroCartera.addAll(this.obtenerDetalle(detOpeCarteraOrigenRetiroVerificado,inOperacionesHash,fechaSistema,conceptoAnulacion));
	    
	    //RETIRO SUBYACENTE	    
	    p_RetiroCartera.addAll(this.obtenerSubyacente(detOpePactoSubyacenteRetiro,inOperacionesHash,fechaSistema,conceptoAnulacion));
	    
	    List<Long> ListaFolioOrigen = new ArrayList<>();
	    List<Long> ListaIdOrigen = new ArrayList<>();
	   
	    //RETIRO DE COMPRAS Y VENTAS A TERMINO  -- DESTINO
	    for (DetalleOperacion det: detOpeCarteraRetiro){
	    	if (inOperacionesHash.get(det.IdOperacion) != null  && (det.IdTipoOperacion.equals("COMTER") || det.IdTipoOperacion.equals("VENTER") ) ){
	    		RetiroCustodia.ObjDatosEntrada	ObjRetiro = new RetiroCustodia.ObjDatosEntrada();
	    		//reriro de cartera op mesa
	    		ObjRetiro.IdGenerarMovimientoCustodia			= det.IdReserva; //det.IdCarteraDestinoDetalle;//
	    		ObjRetiro.TablaAfectada 						= "CARTERA";
				ObjRetiro.AccionOperacion						= "ANULACION";		
				ObjRetiro.IdCliente								= det.IdTipoOperacion.contains("COM")?det.IdClienteCorredor:det.IdCliente;
				ObjRetiro.IdPortafolio							= det.IdTipoOperacion.contains("COM")?det.IdPortafolioCorredor:det.IdPortafolio;
				ObjRetiro.FolioCustodia							= det.FolioCarteraDestinoDetalle;//det.FolioCarteraOrigenDetalle;
				ObjRetiro.IdCustodia							= det.IdCarteraDestinoDetalle;//det.IdCarteraOrigenDetalle;
				ObjRetiro.IdConcepto							= conceptoAnulacion.IdConcepto;
				ObjRetiro.Cantidad								= det.CantidadDetalle;				
				ObjRetiro.ValorRescate							= det.ValorRescateDetalle;
				ObjRetiro.IdInstrumento							= det.IdInstrumentoDetalle;
				ObjRetiro.FechaLiquidacion						= det.FechaLiquidacion;
				ObjRetiro.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjRetiro.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
				ObjRetiro.EsFungible							= det.EsFungible;
				ObjRetiro.IdTipoProducto						= det.IdTipoProducto;
				ObjRetiro.IdTipoOperacion						= ("COMTER").equals(det.IdTipoOperacion)?"ANUCTE":"ANUVTE";
				ObjRetiro.IdEmisor								= det.IdEmisor;
				// MSH
				ObjRetiro.IdMonedaEmision						= det.IdTipoReajuste;
				ObjRetiro.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjRetiro.IdTipoReajusteInstrumento				= det.IdTipoReajuste;
				ObjRetiro.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
				p_RetiroCartera.add(ObjRetiro);	
				
				ListaFolioOrigen.add(det.FolioCarteraOrigenDetalle);
				ListaIdOrigen.add(det.IdCarteraOrigenDetalle);
				
				
	    	}
	    }
	    
	  //RETIRO DE COMPRAS Y VENTAS A TERMINO DESTINO SI NO AFECTA A CUSTODIA
	    for (DetalleAfectaCustodia det: detAfectaCustodia){
	    	if (det.AfectaCustodia.equals("N") && (det.IdTipoOperacion.equals("COMTER") || det.IdTipoOperacion.equals("VENTER"))){
	    		ListaFolioOrigen.add(det.FolioCarteraOrigenDetalle);
				ListaIdOrigen.add(det.IdCarteraOrigenDetalle);
	    	}
	    }
	    
	    
	    //SE BUSCAN TODAS LAS CARTERAS DE ORIGEN , SE REVISA EL ESTADO PARA VER SI SON PARCIALES O TOTALES
	    HashMap<String,Object> paramsOrigen = new HashMap<String,Object>(); 
	    paramsOrigen.put("FolioCartera", ListaFolioOrigen);
	    paramsOrigen.put("IdCartera"   , ListaIdOrigen);
	    List<DetalleOperacion> ListaOrigenTermino= _sql.query(conn, SQL_SELECT_DETALLE_CARTERAS_ORIGEN, paramsOrigen, DetalleOperacion.class);
	    List<Long> ListaOrigenRPA = new ArrayList<>();
	    
	    for (DetalleOperacion org: ListaOrigenTermino){	    	
	    	if(org.IdTipoOperacion.equals("COMTER") || org.IdTipoOperacion.equals("VENTER") ) {
	    		IngresoCustodia.ObjDatosEntrada	ObjIngreso = new IngresoCustodia.ObjDatosEntrada(); 
		    	
		    	if(org.IdEstadoCartera.equals("LIQ")) {
		    		ObjIngreso.IdInstrumento = org.IdInstrumentoDetalle;
		    		ObjIngreso.IdGenerarMovimientoCustodia	= org.IdReserva;
		    		ObjIngreso.EsRegistroNuevo 				= "N";
					ObjIngreso.GenerarNuevoFolioCustodia	= "N";
					ObjIngreso.TablaAfectada 				= "CARTERA";
					ObjIngreso.AccionOperacion				= "INGRESO";		
					ObjIngreso.IdCliente					= org.IdTipoOperacion.contains("COM")?org.IdCliente:org.IdClienteCorredor;
					ObjIngreso.IdPortafolio					= org.IdTipoOperacion.contains("COM")?org.IdPortafolio:org.IdPortafolioCorredor;
					ObjIngreso.FolioCustodia				= org.FolioCarteraDestinoDetalle;
					ObjIngreso.IdCustodia					= org.IdCarteraDestinoDetalle;
					ObjIngreso.Cantidad						= org.CantidadDetalle;// ingreso del vigente mas el retorno 
					ObjIngreso.ValorRescate					= org.ValorRescateDetalle;// ingreso del vigente mas el retorno 
					ObjIngreso.PlazoEnDiasOperacion			= org.PlazoEnDias;
					ObjIngreso.EsFungible					= org.EsFungible;
					ObjIngreso.IdTipoProducto				= org.IdTipoProducto;
					ObjIngreso.IdTipoOperacion				= ("COMTER").equals(org.IdTipoOperacion)?"ANUCTE":"ANUVTE";
					ObjIngreso.IdEmisor						= org.IdEmisor;
					ObjIngreso.FechaLiquidacion				= org.FechaLiquidacion;
					ObjIngreso.FechaOperacion				= Fecha.toDate(fechaSistema);
					ObjIngreso.FechaVencimientoInstrumento	= org.FechaVencimientoInstrumento;
					// MSH
					ObjIngreso.IdMonedaEmision				= org.IdTipoReajuste;
					ObjIngreso.IdMonedaInstrumento			= org.IdMonedaInstrumento;	
					ObjIngreso.IdTipoReajusteInstrumento	= org.IdTipoReajuste;
					//EME
					ObjIngreso.PrecioCompra					= org.PrecioCompra;
					ObjIngreso.TasaCompra					= org.TasaCompra;
					p_IngresoCartera.add(ObjIngreso);
		    	}
		    	else {
		    		ListaOrigenRPA.add(org.FolioCarteraOrigenDetalle);
		    	}
	    		
	    	}
	    	
	    	
	    }
	    
	    if(ListaOrigenRPA.size()>0) {
	    	HashMap<String,Object> paramsOrigenRpa = new HashMap<String,Object>();
	    	paramsOrigenRpa.put("FolioCartera", ListaOrigenRPA);
	    	List<DetalleOperacion> detOpeCarteraRetiroTermino = _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_CARTERA_TERMINO, paramsOrigenRpa, DetalleOperacion.class);
	    	
	    	 //retiro e ingreso de cartera original - restitucion de cartera
		    for (DetalleOperacion det: detOpeCarteraRetiroTermino){
		    	if (inOperacionesHash.get(det.IdOperacion) != null  && (det.IdTipoOperacion.equals("COMTER") || det.IdTipoOperacion.equals("VENTER") ) ){
		    		RetiroCustodia.ObjDatosEntrada	ObjRetiro = new RetiroCustodia.ObjDatosEntrada();
		    		IngresoCustodia.ObjDatosEntrada	ObjIngreso = new IngresoCustodia.ObjDatosEntrada(); 
		    		
					//reriro de la cartera vigente actual  destino 
					
					ObjRetiro.IdGenerarMovimientoCustodia			= det.IdReserva+det.IdCarteraOrigenDetalle;  
		    		ObjRetiro.TablaAfectada 						= "CARTERA";
					ObjRetiro.AccionOperacion						= "RETIRO";		
					ObjRetiro.IdCliente								= det.IdTipoOperacion.contains("COM")?det.IdCliente:det.IdClienteCorredor;
					ObjRetiro.IdPortafolio							= det.IdTipoOperacion.contains("COM")?det.IdPortafolio:det.IdPortafolioCorredor;
					ObjRetiro.FolioCustodia							= det.FolioCarteraDestinoDetalle;
					ObjRetiro.IdCustodia							= det.IdCarteraDestinoDetalle;
					ObjRetiro.IdConcepto							= conceptoAnulacion.IdConcepto;
					ObjRetiro.Cantidad								= det.CantidadCarteraDestinoDetalle;	
					ObjRetiro.ValorRescate							= det.ValorRescateDestinoDetalle;   
					ObjRetiro.IdInstrumento							= det.IdInstrumentoDetalle;
					ObjRetiro.FechaLiquidacion						= det.FechaLiquidacion;
					ObjRetiro.FechaOperacion						= Fecha.toDate(fechaSistema);
					ObjRetiro.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
					ObjRetiro.EsFungible							= det.EsFungible;
					ObjRetiro.IdTipoProducto						= det.IdTipoProducto;
					ObjRetiro.IdTipoOperacion						= ("COMTER").equals(det.IdTipoOperacion)?"ANUCTE":"ANUVTE";
					ObjRetiro.IdEmisor								= det.IdEmisor;
					// MSH
					ObjRetiro.IdMonedaEmision						= det.IdTipoReajuste;
					ObjRetiro.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
					ObjRetiro.IdTipoReajusteInstrumento				= det.IdTipoReajuste;
					ObjRetiro.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
					p_RetiroCartera.add(ObjRetiro);	
					
					
					//ingreso cartera total nueva a folio antiguo	
					Utils.copyPojo(ObjRetiro, ObjIngreso);
					ObjIngreso.EsRegistroNuevo 				= "N";
					ObjIngreso.GenerarNuevoFolioCustodia	= "N";
					ObjIngreso.TablaAfectada 				= "CARTERA";
					ObjIngreso.AccionOperacion				= "INGRESO";		
					ObjIngreso.IdCliente					= det.IdTipoOperacion.contains("COM")?det.IdCliente:det.IdClienteCorredor;
					ObjIngreso.IdPortafolio					= det.IdTipoOperacion.contains("COM")?det.IdPortafolio:det.IdPortafolioCorredor;
					ObjIngreso.FolioCustodia				= det.FolioCarteraDestinoDetalle;
					ObjIngreso.IdCustodia					= det.IdCarteraDestinoDetalle;
					ObjIngreso.Cantidad						= "IF".equalsIgnoreCase(det.IdClasificacionTipoInstrumento) ? BigDecimal.ONE : det.CantidadDetalle.add(det.CantidadCarteraDestinoDetalle);// ingreso del vigente mas el retorno 
					ObjIngreso.ValorRescate					= "RF".equalsIgnoreCase(det.IdClasificacionTipoInstrumento) ? BigDecimal.ZERO : det.ValorRescateDetalle.add(det.ValorRescateDestinoDetalle);// ingreso del vigente mas el retorno 
					ObjIngreso.PlazoEnDiasOperacion			= det.PlazoEnDias;
					ObjIngreso.EsFungible					= det.EsFungible;
					ObjIngreso.IdTipoProducto				= det.IdTipoProducto;
					ObjIngreso.IdTipoOperacion				= ("COMTER").equals(det.IdTipoOperacion)?"ANUCTE":"ANUVTE";
					ObjIngreso.IdEmisor						= det.IdEmisor;
					// MSH
					ObjIngreso.IdMonedaEmision				= det.IdTipoReajuste;
					ObjIngreso.IdMonedaInstrumento			= det.IdMonedaInstrumento;	
					ObjIngreso.IdTipoReajusteInstrumento	= det.IdTipoReajuste;
					//EME
					ObjIngreso.PrecioCompra					= det.PrecioCompra;
					ObjIngreso.TasaCompra					= det.TasaCompra;
					
					p_IngresoCartera.add(ObjIngreso);
		    		
		    		}
		    	}
	    	
	    }
	    
	    //ANULACION DE CARTERAS EN TRANSITO
	    AnularMovimientosEnTransito movTransito = new AnularMovimientosEnTransito();
	    AnularMovimientosEnTransito.AnularMovimientosEnTransitoEntrada ObjEntradaMovTra = new AnularMovimientosEnTransito.AnularMovimientosEnTransitoEntrada();
	    AnularMovimientosEnTransito.AnularMovimientosEnTransitoSalida ObjSalidaMovTra = new AnularMovimientosEnTransito.AnularMovimientosEnTransitoSalida();
	    ObjEntradaMovTra.p_IdEmpresa = in.p_IdEmpresa;
	    ObjEntradaMovTra.p_IdUsuario = in.p_IdUsuario;
	    ObjEntradaMovTra.p_IdModulo  = in.p_IdModulo;
	    ObjEntradaMovTra.p_FechaOperacion = fechaSistema;
	    
	    for (AnularMovimientosEnTransito.ObjMovimientos det: detOpeTransito){
	    	ObjEntradaMovTra.P_MOVIMIENTOSANULAR.add(det);
	    }
	    
	    if(ObjEntradaMovTra.P_MOVIMIENTOSANULAR.size()>0) {
	    	ObjSalidaMovTra = movTransito.logica(conn, ObjEntradaMovTra);
	    	
	    	if(ObjSalidaMovTra.P_OUT_ERRORESCONTADOR.compareTo(0L)!= 0) {	    		
	    		out.P_OUT_MENSAJES.addAll(ObjSalidaMovTra.P_OUT_MENSAJES);
	    		Id++;
	    	}
	    }
	    
	    //RETIROS DE CUSTODIAS
	    if (p_RetiroCartera.size() > 0 || p_IngresoCartera.size() > 0){
	    	List<ObjMovimiento>  entLogCus = new ArrayList<>();
	    	TicketAtencion = LogCustodia.logCustodia(conn,entLogCus);
	    }
	    if (p_RetiroCartera.size() > 0){
	    	log.write("INICIO::MRF.RetiroCustodia");
	    	RetiroCustodia retiroCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSPRetiro.p_IdEmpresa			= in.p_IdEmpresa;
			paramInSPRetiro.p_SimularOperacion	= "N";
			paramInSPRetiro.p_TicketAtencion	= TicketAtencion;
			paramInSPRetiro.p_DatosEntrada		= p_RetiroCartera;
			paramInSPRetiro.p_ValorizacionAutomatica = "N";
			RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(conn, paramInSPRetiro);	
			
			out.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (out.P_OUT_ERRORESCONTADOR > 0){
				return out;
			}
			for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida c: resultRetiros.P_OUT_DATOSSALIDA){
				mapMsg.clear();
				mapMsg.put("v_Cantidad", c.Cantidad.toString());
				mapMsg.put("v_FolioCustodia", c.FolioCustodia.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_RETIRO_TITULO_OK",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			log.write("FIN::MRF.RetiroCustodia");
	    }
	    
	    
	    //INGRESO CUSTODIA OPERACION A TERMINO
	    if (p_IngresoCartera.size() > 0){
	    	log.write("INICIO::MRF.IngresoCustodia");
	    	IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSPIngreso = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSPIngreso.p_IdEmpresa		= in.p_IdEmpresa;
			paramInSPIngreso.p_IdModulo			= in.p_IdModulo;
			paramInSPIngreso.p_SimularOperacion	= "N";
			paramInSPIngreso.p_TicketAtencion	= TicketAtencion;
			paramInSPIngreso.p_DatosEntrada		= p_IngresoCartera;
			paramInSPIngreso.p_ValorizacionAutomatica	= "N";
			IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(conn, paramInSPIngreso);	
			
			out.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (out.P_OUT_ERRORESCONTADOR > 0){
				return out;
			}
			for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida c: resultIngresos.P_OUT_DATOSSALIDA){
				mapMsg.clear();
				mapMsg.put("Folio", c.FolioCustodia.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_INGRESO_TITULO_OK",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			log.write("FIN::MRF.IngresoCustodia");
	    }
	    
	  //ANULACION TESORERIA
	    this.crearComprobanteTesoreria(conn,entidadesTesoreria,fechaSistema,in,out);
	    if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
	    
	  
	    //ANULACION CARGOS Y ABONOS CTA INVERSION
	    this.crearCargoAbonoCtaInversion(conn,fechaSistema,paramsDetOpIn,conceptoAnulacion,in,out);
		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
	    
  		   
  		Long idOperacionWebSocket = 0L;  		
	    ///Cambio estado para las operaciones
	    CambiarEstadoOperacionEntrada inCambioEstado = new CambiarEstadoOperacionEntrada(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "N", in.p_UsaReglasNegocio, idPaisSistema);
	    for (DetalleOperacion det: detOpe){
		    ObjCambiarEstadoOperacionIN estadoOp = new ObjCambiarEstadoOperacionIN(); 
		    estadoOp.IdOperacion 					= det.IdOperacion;
		    idOperacionWebSocket					= det.IdOperacion;
		    estadoOp.IdTipoProducto 				= det.IdTipoProducto;
		    estadoOp.IdNuevoEstado 					= ((Operacion)inOperacionesHash.get(det.IdOperacion)).IdEstadoSiguiente;
		    estadoOp.IdReserva						= det.IdReserva;
		    estadoOp.FolioCarteraCustodia			= det.FolioCarteraDestinoDetalle;
		    estadoOp.MontoOperacionAnticipo			= BigDecimal.ZERO;
		    estadoOp.MontoOperacionInicialAnticipo	= BigDecimal.ZERO;
		    estadoOp.MontoOperacionFinalAnticipo	= BigDecimal.ZERO;
		    estadoOp.TasaAnticipo					= BigDecimal.ZERO;
		    estadoOp.MontoOperacionMonedaAnticipo	= BigDecimal.ZERO;
		    estadoOp.MontoOperacionInicialMonedaAnticipo	= BigDecimal.ZERO;
		    estadoOp.MontoOperacionFinalMonedaAnticipo		= BigDecimal.ZERO;
		    estadoOp.IdMonedaLiquidacionAnticipo			= "";
		    estadoOp.IdCondicionLiquidacionAnticipo			= "";
		    estadoOp.FechaCondicionLiquidacionAnticipo		= null;
		    estadoOp.Glosa 							= det.Glosa;
		    
		    if(esRechazo) {
		    	estadoOp.EstadoConfirmacion			= "RZD";
		    }else {
		    	estadoOp.EstadoConfirmacion			= "NCF";
		    }
			inCambioEstado.p_DataCambiarEstado.add(estadoOp);
	    }
	    log.write("INICIO::MRF.CambiarEstadoOperacion");
		CambiarEstadoOperacion cambioEstado = new CambiarEstadoOperacion();
		CambiarEstadoOperacionSalida cambioEstadoOut = cambioEstado.logica(conn, inCambioEstado);
		 
		MensajeDinamico mensaje = new MensajeDinamico();
		
		mensaje.IdOperacion =idOperacionWebSocket;

		
		Gson gson = new Gson();
		String mensajeDinamico = gson.toJson(mensaje);
		/***** WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS MESARF *****/
		CacheServicio.invalidarPorTag(CacheMRF.TAG_POSICION);
		AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "ANUMRF",mensajeDinamico);
		
		out.P_OUT_MENSAJES.addAll(cambioEstadoOut.P_OUT_MENSAJES);
		out.P_OUT_OPERACIONES.addAll(cambioEstadoOut.P_OUT_OPERACION);
		log.write("FIN::MRF.CambiarEstadoOperacion");
	    log.write("FIN::MRF.AnularOperacionMesa");
	  //agregamos log de quien esta anulando
	    agregarLogSistema(conn,in, fechaSistema, out);
	    
	    CacheValorizacion.valorizar(conn);
		return out;
	}
	
	

	private AnularOperacionMesa_OUT validacionesBasicas(Connection conn, List<EntidadTesoreria_Proceso> entidadesTesoreria,List<DetalleOperacion> detOpe ,List<DetalleValidacion> detOpeReservada,List<DetalleValidacion> detOpeComprometida, List<DetalleOperacion> detOpeCarteraRetiro,	Concepto conceptoAnulacion, AnularOperacionMesa_IN in, AnularOperacionMesa_OUT out) throws Exception {
		logger.debug("INICIO::AnularOperacionMesa.validacionesBasicas");
		long inicioMetodo = System.currentTimeMillis();		
		HashMap<String,String> mapMsg = new HashMap<>();		
		Long Id = 0L;
				
	    for (EntidadTesoreria_Proceso ent : entidadesTesoreria) {
			if(ent.IdEstado.equalsIgnoreCase("LIQ") || "LIP".equals(ent.IdEstado)){
				mapMsg.put("Id",Long.toString(ent.Id));
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"COMPROBANTE_TESORERIA_LIQUIDADO",mapMsg,conn,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = 1L;
			
			}
			if( "EMI".equals(ent.IdEstado)){
				mapMsg.put("Id",Long.toString(ent.Id));
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MOVIMIENTOS_TESORERIA_EMITIDOS",mapMsg,conn,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = 1L;
				
			}
		}
	    
	     if(out.P_OUT_ERRORESCONTADOR > 0) {
	    	 return out;
	     }
	    
	    //Validaciones, antes de anular se verifica si la cartera esta reservada o comprometida
	    for (DetalleValidacion det :detOpeReservada){
	    	if ( det.CantidadReservada.compareTo(BigDecimal.ZERO) !=0 ) {
	    		out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_CARTERA_ESTA_RESERVADA",mapMsg,conn,in.p_IdEmpresa));
				Id++;
	    	} 
	    }
	    
	    for (DetalleValidacion det :detOpeComprometida){
	    	if ( det.CantidadReservada.compareTo(BigDecimal.ZERO) !=0 ) {
	    		out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_CARTERA_ESTA_COMPROMETIDA",mapMsg,conn,in.p_IdEmpresa));
				Id++;
	    	} 
	    }
	    
	    //Lista los folios origen para buscar en cartera
	    List<Long> listaFolioOrigen = new ArrayList<Long>();
	    for(DetalleOperacion det : detOpeCarteraRetiro) {	    	
	    	listaFolioOrigen.add(det.FolioCarteraOrigenDetalle); 
	    }
	 
		    
	    //valido que vengan datos
	    if (conceptoAnulacion == null){
	    	//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_CONCEPTOS_MESA_NO_CONFIGURADO",mapMsg,conn,in.p_IdEmpresa));
			Id++;
	    }
	    if (in.p_Operaciones.size() == 0){
			//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_NO_SE_INGRESO_OPERACION_PARA_PROCESAR",mapMsg,conn,in.p_IdEmpresa));
			Id++;
		}
	    if (detOpe.size() == 0){
			//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_NO_SE_ENCONTRO_CARTERA_DE_OPERACION",mapMsg,conn,in.p_IdEmpresa));
			Id++;
		}
	    long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::AnularOperacionMesa.validacionesBasicas :" , finMetodo- inicioMetodo);
		
		return out;
	}



	private void crearComprobanteTesoreria(Connection conn, List<EntidadTesoreria_Proceso> entidadesTesoreria, String fechaSistema,AnularOperacionMesa_IN in, AnularOperacionMesa_OUT out) throws Exception {
		logger.debug("INICIO::AnularOperacionMesa.crearComprobanteTesoreria");
		long inicioMetodo = System.currentTimeMillis();
		
		if (entidadesTesoreria.size() > 0) {
			
			AnularRegistros_Entrada entradaAnularTesoreria = new AnularRegistros_Entrada();
			entradaAnularTesoreria.p_EjecutaAutorizacion = "N";
			entradaAnularTesoreria.p_FechaMovimiento = fechaSistema;
			entradaAnularTesoreria.p_IdEmpresa = in.p_IdEmpresa;
			entradaAnularTesoreria.p_IdModulo = in.p_IdModulo;
			entradaAnularTesoreria.p_IdUsuario = in.p_IdUsuario;
			List<Registro_Entrada> lista = new ArrayList<>();
			for (EntidadTesoreria_Proceso ent : entidadesTesoreria) {
				Registro_Entrada arg0 = new Registro_Entrada();
				arg0.Entidad = ent.Entidad;
				arg0.Id = ent.Id;
				lista.add(arg0 );
			}
			entradaAnularTesoreria.p_RegistrosAProcesar = lista ;
			entradaAnularTesoreria.p_simularOperacion = "N";
			entradaAnularTesoreria.p_UsaReglasNegocio = "S";//DEBERIA SER S... cuando se migre tesoreria a java hay que agreagar la logica que se anule El comprobante y el movto. (ahora valida que el movimiento no este Emitido y rechaza la anulacion... esto solo por que envio los dos de 1 vez)
			
			
			AnularRegistros anularTesoreriaService = new AnularRegistros();
			AnularRegistros_Salida salidaAnularTesoreria = anularTesoreriaService.logica(conn, entradaAnularTesoreria);
			log.write("Salida AnularRegistrosTesoreria: " + salidaAnularTesoreria.toString());
			
			out.P_OUT_MENSAJES.addAll(salidaAnularTesoreria.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::AnularOperacionMesa.crearComprobanteTesoreria :" , finMetodo- inicioMetodo);
		
	}



	private void crearCargoAbonoCtaInversion(Connection conn, String fechaSistema, HashMap<String, Object> paramsDetOpIn, Concepto conceptoAnulacion,AnularOperacionMesa_IN in, AnularOperacionMesa_OUT out) throws Exception {
		logger.debug("INICIO::AnularOperacionMesa.crearCargoAbonoCtaInversion");
		long inicioMetodo = System.currentTimeMillis();
		SQL _sql = new SQL();
		
		List<CargoAbono_Proceso> cargosAbonos = _sql.query(conn, SQL_SELECT_DETALLE_CUENTA_INVERSION, paramsDetOpIn, CargoAbono_Proceso.class);
			
			if (cargosAbonos.size() > 0) {
				
				AnularCargoAbono_Entrada entradaAnularCargoAbono = new AnularCargoAbono_Entrada();
				List<CargoAbono_Entrada> lista = new ArrayList<>();
				for (CargoAbono_Proceso cabon : cargosAbonos) {
					CargoAbono_Entrada arg0 = new CargoAbono_Entrada();
					arg0.IdCargoAbonoMovimiento = cabon.IdCargoAbonoMovimiento;
					arg0.GlosaAnulacion = conceptoAnulacion.NombreConcepto;
					lista.add(arg0);
				}
				entradaAnularCargoAbono.p_CargoAbonoAAnular = lista ;
				entradaAnularCargoAbono.p_EjecutaAutorizacion = "N";
				entradaAnularCargoAbono.p_OmiteRegistroSinAutorizar = "N";
				entradaAnularCargoAbono.p_IdModulo = in.p_IdModulo;
				entradaAnularCargoAbono.p_IdUsuario = in.p_IdUsuario;
				entradaAnularCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
				entradaAnularCargoAbono.p_simularOperacion = "N";
				entradaAnularCargoAbono.p_UsaReglasNegocio = "S";
				entradaAnularCargoAbono.p_FechaMovimiento = fechaSistema;
				entradaAnularCargoAbono.p_EsAnulacionManual = "N";
				
				AnularCargoAbono anularCargoAbonoService = new AnularCargoAbono();
				AnularCargoAbono_Salida salidaAnularCargoAbono = anularCargoAbonoService.logica(conn, entradaAnularCargoAbono);
				log.write("Salida AnularCargoAbono: " + salidaAnularCargoAbono.toString());
				out.P_OUT_MENSAJES.addAll(salidaAnularCargoAbono.P_OUT_MENSAJES);
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			}
		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::AnularOperacionMesa.crearCargoAbonoCtaInversion :" , finMetodo- inicioMetodo);
		
	}



	private List<ObjDatosEntrada> obtenerSubyacente(List<DetalleOperacion> detOpePactoSubyacenteRetiro,	HashMap<Long, Object> inOperacionesHash, String fechaSistema, Concepto conceptoAnulacion) {
		logger.debug("INICIO::AnularOperacionMesa.obtenerSubyacente");
		long inicioMetodo = System.currentTimeMillis();		
		List<ObjDatosEntrada> p_RetiroCartera = new ArrayList<>();
		
		for (DetalleOperacion det: detOpePactoSubyacenteRetiro){
	    	if (inOperacionesHash.get(det.IdOperacion) != null  && (det.IdTipoOperacion.equals("COMPAC") || det.IdTipoOperacion.equals("VENPAC") )   ){
	    		RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
	    		
	    		ObjEntrada.IdGenerarMovimientoCustodia			= det.IdCarteraSubyacenteOrigenDetalle;//det.IdReserva;
	    		ObjEntrada.TablaAfectada 						= "SUBYACENTE";
				ObjEntrada.AccionOperacion						= "ANULACION";		
				ObjEntrada.IdCliente							= det.IdCliente;//det.IdTipoOperacion.contains("COM")?det.IdClienteCorredor:det.IdCliente;
				ObjEntrada.IdPortafolio							= det.IdPortafolio;//det.IdTipoOperacion.contains("COM")?det.IdPortafolioCorredor:det.IdPortafolio;
				ObjEntrada.FolioCustodia						= det.FolioCarteraOrigenDetalle;
				ObjEntrada.IdCustodia							= det.IdCarteraSubyacenteOrigenDetalle;//det.IdCarteraOrigenDetalle;
				ObjEntrada.IdConcepto							= conceptoAnulacion.IdConcepto;
				ObjEntrada.Cantidad								= det.CantidadDetalle;
				ObjEntrada.ValorRescate							= det.ValorRescateDetalle;
				ObjEntrada.IdInstrumento						= det.IdInstrumentoDetalle;
				ObjEntrada.FechaLiquidacion						= det.FechaLiquidacion;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.EsFungible							= det.EsFungible;
				ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
				ObjEntrada.IdTipoOperacion						= ("COMPAC").equals(det.IdTipoOperacion)?"ANUCPA":"ANUVPA";
				ObjEntrada.IdEmisor								= det.IdEmisor;
				// MSH
				ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
				ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
				ObjEntrada.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
				
				p_RetiroCartera.add(ObjEntrada);
	    	}
	    }
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::AnularOperacionMesa.obtenerSubyacente :" , finMetodo- inicioMetodo);
		
		
		return p_RetiroCartera;
	}
	
	private List<ObjDatosEntrada> obtenerDetalle(List<DetalleOperacion> detOpeCarteraOrigenRetiro,	HashMap<Long, Object> inOperacionesHash, String fechaSistema, Concepto conceptoAnulacion) {
		logger.debug("INICIO::AnularOperacionMesa.obtenerDetalle");
		long inicioMetodo = System.currentTimeMillis();
		List<ObjDatosEntrada> p_RetiroCartera = new ArrayList<>();
		
	    for (DetalleOperacion det: detOpeCarteraOrigenRetiro){
	    	if (inOperacionesHash.get(det.IdOperacion) != null && (det.IdTipoOperacion.equals("COMPAC") || det.IdTipoOperacion.equals("VENPAC") ) ){
				RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
				
				ObjEntrada.IdGenerarMovimientoCustodia			= det.IdReserva;
				ObjEntrada.TablaAfectada 						= "CARTERA";
				ObjEntrada.AccionOperacion						= "RETIRO";		
				ObjEntrada.IdCliente							= det.IdTipoOperacion.equals("COMPAC")? det.IdClienteCorredor: det.IdCliente;      
				ObjEntrada.IdPortafolio							= det.IdTipoOperacion.equals("COMPAC")? det.IdPortafolioCorredor:det.IdPortafolio;
				ObjEntrada.FolioCustodia						= det.FolioCarteraDestinoDetalle;
				ObjEntrada.IdCustodia							= det.IdCarteraDestinoDetalle;
				ObjEntrada.IdConcepto							= conceptoAnulacion.IdConcepto;
				ObjEntrada.Cantidad								= det.CantidadDetalle;
																			   
				ObjEntrada.IdInstrumento						= det.IdInstrumentoDetalle;
				ObjEntrada.ValorRescate							= det.ValorRescateDetalle;
				ObjEntrada.FechaLiquidacion						= det.FechaLiquidacion;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.EsFungible							= det.EsFungible;
				ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
				ObjEntrada.IdTipoOperacion						= ("COMPAC").equals(det.IdTipoOperacion)?"ANUCPA":"ANUVPA";					
				ObjEntrada.IdEmisor								= det.IdEmisor;
				// MSH
				ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
				ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
				ObjEntrada.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
				
				p_RetiroCartera.add(ObjEntrada);	
		
	    	}
	    }
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::AnularOperacionMesa.obtenerDetalle :" , finMetodo- inicioMetodo);
		
		
		return p_RetiroCartera;
	}
	
	private List<ObjDatosEntrada> obtenerCabecera(List<DetalleOperacion> detOpePactoRetiro,	HashMap<Long, Object> inOperacionesHash, String fechaSistema, Concepto conceptoAnulacion) {
		logger.debug("INICIO::AnularOperacionMesa.obtenerCabecera");
		long inicioMetodo = System.currentTimeMillis();
		List<ObjDatosEntrada> p_RetiroCartera = new ArrayList<>();
				
		 for (DetalleOperacion det: detOpePactoRetiro ){
		    	if (inOperacionesHash.get(det.IdOperacion) != null  && (det.IdTipoOperacion.equals("COMPAC") || det.IdTipoOperacion.equals("VENPAC") ) ){
		    		RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
		    		ObjEntrada.IdGenerarMovimientoCustodia			= det.IdOperacion;
		    		ObjEntrada.TablaAfectada 						= "CARTERA";
					ObjEntrada.AccionOperacion						= "ANULACION";		
					ObjEntrada.IdCliente							= det.IdCliente;
					ObjEntrada.IdPortafolio							= det.IdPortafolio;
					ObjEntrada.FolioCustodia						= det.FolioCarteraPacto;
					ObjEntrada.IdCustodia							= det.IdCarteraPacto;
					ObjEntrada.IdConcepto							= conceptoAnulacion.IdConcepto;
					ObjEntrada.Cantidad								= BigDecimal.ONE;
					ObjEntrada.IdInstrumento						= det.IdInstrumentoPacto;
					ObjEntrada.ValorRescate							= det.ValorRescatePacto;
					ObjEntrada.FechaLiquidacion						= det.FechaLiquidacion;
					ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
					ObjEntrada.EsFungible							= det.EsFungible;
					ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
					ObjEntrada.IdTipoOperacion						= ("COMPAC").equals(det.IdTipoOperacion)?"ANUCPA":"ANUVPA";
					// MSH
					ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
					ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
					ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
					ObjEntrada.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;					
					
					p_RetiroCartera.add(ObjEntrada);				
		    	}
		    }
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::AnularOperacionMesa.obtenerCabecera :" , finMetodo- inicioMetodo);		
		
		return p_RetiroCartera;
	}
	

	private List<DetalleOperacion> verificarIdCarteraVigente(Connection conn,List<DetalleOperacion> opePacDetalle) throws Exception {
		logger.debug("INICIO::CrearOperacionMesa.verificarIdCarteraVigente");
		long inicioMetodo = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> listaFolios = opePacDetalle.stream().map(detalle -> detalle.FolioCarteraDestinoDetalle).collect(Collectors.toList());
		params.put("ListaFolioCartera", listaFolios);
		
		Map<Long,VerificarCartera> mapCarterasVigentes =  sql.query(conn, SQL_SELECT_ID_CARTERA_RESERVA, params, VerificarCartera.class)
												.stream().collect(Collectors.toMap(cartera -> cartera.FolioCartera, cartera-> cartera));
		
		List<DetalleOperacion> detallesActualizados  = opePacDetalle.stream().map(detalle -> {			
			VerificarCartera carteraVerificada = mapCarterasVigentes.get(detalle.FolioCarteraDestinoDetalle);
			
			if(detalle.FolioCarteraDestinoDetalle!=0 && carteraVerificada.IdCartera.compareTo(detalle.IdCarteraDestinoDetalle)!=0) {
				detalle.IdCarteraDestinoDetalle= carteraVerificada.IdCartera;
			}
			
			return detalle;
		}).collect(Collectors.toList());		
		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionMesa.verificarIdCarteraVigente :" , finMetodo- inicioMetodo);
		
		return detallesActualizados;
		
	}
	
	
	private List<MensajeSalida> obtenerMensajeAdvertenciaLimites(Connection conn,AnularOperacionMesa_IN entrada, List<DetalleOperacion> detOpe) throws Exception {
		log.write("INICIO::AnularOperacionMesa.obtenerMensajeAdvertenciaLimites");
		long inicio = System.currentTimeMillis();
		ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF entradaValidarLimites = new ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF();
		
		entradaValidarLimites.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimites.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarLimites.p_IdModulo  = entrada.p_IdModulo;
		entradaValidarLimites.p_UsaReglasNegocio = "S";
		entradaValidarLimites.p_IdTipoProceso = _cl.PROCESO_ANULAR;
		
		entradaValidarLimites.p_Operaciones = detOpe.stream()
				.filter(det -> _cl.TIPO_OPERACION_MESA_VALIDO.contains(det.IdTipoOperacion))
				.map(det ->{
					ValidarLimiteCuentaPropiaIFRF.Operacion operacion = new ValidarLimiteCuentaPropiaIFRF.Operacion();
					
					operacion.IdInternoGrupo = det.IdOperacion;
					operacion.IdInterno = det.IdInterno;
					operacion.IdOperacion = det.IdOperacion;
					operacion.IdTipoOperacion = det.IdTipoOperacion;
					operacion.Folio = det.FolioOperacion.toString();
					operacion.IdCliente = det.IdCliente;
					operacion.IdPortafolio = det.IdPortafolio;
					operacion.IdInstrumento = det.IdInstrumentoDetalle;
					operacion.IdTipoCanalIngreso = det.IdTipoCanalIngreso;
					operacion.IdMonedaOperacion = det.IdMonedaEmision;
					operacion.Cantidad = det.CantidadDetalle;
					operacion.ValorRescate = det.ValorRescateDetalle;
					operacion.MontoCompraMonedaSistema = det.MontoOperacion;
					operacion.Tasa = det.Tasa;
					operacion.FechaVencimientoInstrumento = Fecha.toDate(Fecha.toStringDate(det.FechaVencimientoInstrumentoLimite));
					
					
					return operacion;
				})
				.collect(Collectors.toList());
		
		ValidarLimiteCuentaPropiaIFRF.SalidaValidarLimiteCuentaPropiaIFRF salidaValidarLimites = new ValidarLimiteCuentaPropiaIFRF().logica(conn, entradaValidarLimites);
		
		long fin = System.currentTimeMillis();
		log.write("FIN::AnularOperacionMesa.obtenerMensajeAdvertenciaLimites {}",fin-inicio);
		
		return salidaValidarLimites.P_OUT_MENSAJES;
	}
	private void agregarLogSistema(Connection con, AnularOperacionMesa_IN entrada, String fechaSistema, AnularOperacionMesa_OUT out) throws Exception {
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		if(out.P_OUT_OPERACIONES.size()>0) {
				for(ObjEstadoOperacionOUT OPIN : out.P_OUT_OPERACIONES) {
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdCabeceraLog = OPIN.IdOperacion;
					log.IdTipoLog = "ANULACION";
					log.IdModulo = "MRF";
					log.IdUsuario = entrada.p_IdUsuario;
					log.IdEmpresa = entrada.p_IdEmpresa;
					log.FechaSistemaLog = fechaSistema;
					log.IdUsuarioAtendido = entrada.p_IdUsuario;
					log.IdCliente =  0L;
					log.EstadoCliente = "";
					log.NombreCliente = "";
					log.IdTipoDocumentoIdentidad = "";
					log.IdDocumentoIdentidad = "";
					log.IdPortafolio =  0L;
					log.EstadoPortafolio = "";
					log.IdTipoRegistro = "ANULACION";
					log.EstadoRegistro = "ANU";
					log.IdTipoOperacion = "ANULACION";
					log.Folio = OPIN.IdOperacion;
					log.Cantidad = BigDecimal.ZERO;
					log.PrecioTasa =   BigDecimal.ZERO;
					log.Monto =   BigDecimal.ZERO;
					log.IdMonedaOrigen =  "";
					log.IdMonedaLiquidacion = "";
					log.TipoCambio = "";
					log.Nemo = "";
					log.IdProceso = 0L;
					log.EstadoProceso = "";
					log.NombreProceso = "MRF.AnularOperacionMesa";
					entradaAgregarLog.p_Log.add(log);
				
					
				}
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
			}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		AnularOperacionMesa_IN jin = gson.fromJson(in.toString(), AnularOperacionMesa_IN.class);
		AnularOperacionMesa_OUT out = logica(conn, jin);
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
			conn = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
			//datos entrada
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operaciones\":[{\"IdOperacion\":1510,\"IdEstadoSiguiente\":\"ANU\"}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operaciones\":[{\"IdOperacion\":191607,\"IdEstadoSiguiente\":\"ANU\"}]}"; 


			AnularOperacionMesa_IN objIn = gson.fromJson(json, AnularOperacionMesa_IN.class);
			//mientras pruebo hare el rollback
			AnularOperacionMesa demo = new AnularOperacionMesa();

			AnularOperacionMesa_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());

			conn.commit();
			//conn.rollback();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");

	};
	
}