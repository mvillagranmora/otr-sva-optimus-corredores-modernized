package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.normativa.cartola.GestionarNomina.GestionarNominaEntrada;
import cl.bcs.normativa.cartola.GestionarNomina.GestionarNominaSalida;
import cl.bcs.normativa.cartola.GestionarNomina.GestionarNominaSalida.ObjCartolaSalida;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarCostoUF_MLT implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ActualizarCostoUF_MLT.class);
	
	private final String SQL_SELECT_SOLICITUDES_RESCATES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectSolicitudesRescates.sql");
	private final String SQL_SELECT_SOLICITUDES_APORTES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectSolicitudesAportes.sql");
	//private final String SQL_SELECT_SOLICITUDES_REINVERSIONES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectSolicitudesReinversiones.sql");
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectCarteras.sql");
	
	private final String SQL_UPDATE_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateSolicitudes.sql");
	private final String SQL_UPDATE_SOLICITUDES_APORTES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateSolicitudesAportes.sql");
	private final String SQL_UPDATE_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateCarteras.sql");
	
	private final String SQL_INSERT_PROCESO_MLT = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_InsertProcesoMandatos.sql");
	private final String SQL_UPDATE_PROCESO_MLT = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateProcesoMandatosPRO.sql");
	private final String SQL_UPDATE_REGISTRO_MLT = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_UpdateRegistroMLT.sql");
	
	private final String SQL_SELECT_RESCATES_PROXIMOS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectRescateReinversion.sql");
	private final String SQL_SELECT_APORTES_PROXIMOS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectAporteReinversion.sql");
	
	private final String SQL_SELECT_RESCATES_PROXIMOS_FECHA = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectFechaRescateReinversion.sql");
	
	private final String SQL_UPDATE_SOLICITUD_PROXIMOS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateProcesoReinversion.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaActualizarCostoUF{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public Long p_Portafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaActualizarCostoUF{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaActualizarCostoUF() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudAporteRescate{
		public Long IdEmpresa;
		public Long IdSolicitudAporteRescate;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public BigDecimal CostoUF;
		public BigDecimal NumeroCuotas;
		public BigDecimal MontoMonedaSistema;
		public BigDecimal ValorUFFechaInversion;
		public Long IdAgrupador;
		public String IdSerieFondo;
		public String IdRegimenTributario;
		public String FondoExtranjero;
		public String RES_IndicadorMLT;
		public String RES_IdRegimenTributario;
		public String RES_FondoExtranjero;
		public BigDecimal ValorUF;
		public BigDecimal CostoTotalRescatado;
		public BigDecimal MontoTotalRescatado;
		public java.sql.Date FechaCertificacion;
		
		public SolicitudAporteRescate() {
			this.CostoUF = BigDecimal.ZERO;
			this.CostoTotalRescatado = BigDecimal.ZERO;
			this.MontoTotalRescatado = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Clase para agendar proximos proceso a rescate reinversion
	public static class ProximosRescates{
		public Long IdSolicitudAporteRescate;
		public java.sql.Date FechaCertificacion;
		
		public ProximosRescates() {
			this.IdSolicitudAporteRescate = 0L;
						
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProximosAportes{
		public Long IdSolicitudAporteRescate;
		public java.sql.Date FechaCertificacion;
		
		public ProximosAportes() {
			this.IdSolicitudAporteRescate = 0L;
						
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ProcesoMLT {
		
		public Long IdEmpresa;
		public String IdEstado;
		public Long FolioCartera;
		public Long IdCartera;
		public Long IdSolicitudAporteRescate;
		public String TipoSolicitud;
		public java.sql.Date FechaCertificacion;
		public Long IdPortafolio;
		public BigDecimal INGMontoATasaMercadoActual;
		public BigDecimal CostoInversionEnFactorAjuste;
		public BigDecimal PRO_CostoInversionEnFactorAjuste;
		public BigDecimal EGRCostoInversionEnFactorAjuste;
		public BigDecimal PRO_EGRCostoInversionEnFactorAjuste;
		public BigDecimal PRO_CostoTotalRescatado;
		public BigDecimal MontoTotalRescatado;
		public Long IdAgrupador;
		public ProcesoMLT(){
			CostoInversionEnFactorAjuste= BigDecimal.ZERO;
			PRO_CostoInversionEnFactorAjuste= BigDecimal.ZERO;
			EGRCostoInversionEnFactorAjuste = BigDecimal.ZERO;
			PRO_EGRCostoInversionEnFactorAjuste = BigDecimal.ZERO;
			this.PRO_CostoTotalRescatado = BigDecimal.ZERO;
			this.MontoTotalRescatado = BigDecimal.ZERO;
		}
		
	}
	
	public static class Cartera{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public Long EGRIdOperacion;
		public Long IdPortafolio;
		public BigDecimal Cantidad;
		public BigDecimal EGRCantidad;
		public BigDecimal CostoInversionEnFactorAjuste;
		public BigDecimal ValorFactorAjusteaFechaCurse;
		public BigDecimal INGCostoInversionEnFactorAjuste;
		public BigDecimal INGValorFactorAjusteaFechaCurse;
		public BigDecimal EGRValorFactorAjusteaFechaCurse;
		public BigDecimal EGRCostoInversionEnFactorAjuste;
		public BigDecimal MontoCompra;
		public BigDecimal INGMontoATasaMercadoActual;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public BigDecimal PRO_EGRCostoInversionEnFactorAjuste;
		public BigDecimal PRO_CostoInversionEnFactorAjuste;
		public Cartera(){
			EGRCostoInversionEnFactorAjuste 		= BigDecimal.ZERO;
			PRO_EGRCostoInversionEnFactorAjuste		= BigDecimal.ZERO;
			CostoInversionEnFactorAjuste			= BigDecimal.ZERO;
			PRO_CostoInversionEnFactorAjuste 		= BigDecimal.ZERO;
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Reinversion{
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondoDestino;
		public BigDecimal Porcentaje;
		public String IdRegimenTributario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaPrograma {
		public Long						P_OUT_IDNOMINA;
		public Long						P_OUT_IDPROGRAMA;
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long P_OUT_IDAGENDA;
		
		public SalidaPrograma() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaActualizarCostoUF jin = gson.fromJson(in.toString(), EntradaActualizarCostoUF.class);
		SalidaActualizarCostoUF out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaActualizarCostoUF logica (Connection con, EntradaActualizarCostoUF entrada) throws Exception {
		logger.debug("INICIO::ActualizarCostoUF");
		SalidaActualizarCostoUF out = new SalidaActualizarCostoUF();
		SQL sql=new SQL();
		
		System.out.println("FECHA DESDE::" + entrada.p_FechaDesde);
		System.out.println("FECHA HASTA::" + entrada.p_FechaHasta);
		System.out.println("PORTAFOLIO::" + entrada.p_Portafolio);
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		params.put("Portafolio", entrada.p_Portafolio);
		
		List<SolicitudAporteRescate> solicitudesRescates = sql.query(con, SQL_SELECT_SOLICITUDES_RESCATES, params, SolicitudAporteRescate.class);
		
		//List<Reinversion> reinversiones = sql.query(con, SQL_SELECT_SOLICITUDES_REINVERSIONES, params, Reinversion.class);
		List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERAS, params, Cartera.class);
		
		Map<Long, List<Cartera>> hashCarterasRescates = carteras.stream().filter(cartera->_c.TipoSolicitudRescate.equals(cartera.TipoSolicitud)).collect(Collectors.groupingBy(cartera->cartera.EGRIdOperacion));
		Map<Long, List<Cartera>> hashCarterasAportes = carteras.stream().filter(cartera->_c.TipoSolicitudAporte.equals(cartera.TipoSolicitud)).collect(Collectors.groupingBy(cartera->cartera.IdOperacion));
		//Map<Long, List<Cartera>> hashCarterasAportes = carteras.parallelStream().collect(Collectors.groupingBy(cartera->cartera.IdOperacion));
		
		/*
		Map<String, List<Reinversion>> hashReinversiones = reinversiones.parallelStream().collect(Collectors.groupingBy(item->{
			return item.IdSolicitudAporteRescate + "_" + item.IdSerieFondoDestino + "_" + item.IdRegimenTributario;
		}));
		*/
		System.out.println("Rescates::" + solicitudesRescates.size());
		
		///RESCATES
		System.out.println("INICIO:: RESCATES");
		System.out.println("Loop RESCATES");
		 List<ProcesoMLT> DetalleCarteraResct = new ArrayList<ProcesoMLT>();
		for(SolicitudAporteRescate solicitud : solicitudesRescates) {
			List<Cartera> carterasSolicitud = hashCarterasRescates.get(solicitud.IdSolicitudAporteRescate) != null ? hashCarterasRescates.get(solicitud.IdSolicitudAporteRescate) : new ArrayList<Cartera>();//carteras.parallelStream().filter(cartera -> cartera.EGRIdOperacion.equals(solicitud.IdSolicitudAporteRescate) && _c.TipoSolicitudRescate.equals(cartera.TipoSolicitud)).collect(Collectors.toList());
			carterasSolicitud.stream().forEach(cartera -> {
				
				/*CALCULO DE COSTO UF*/
				cartera.PRO_EGRCostoInversionEnFactorAjuste = cartera.EGRCantidad.divide(cartera.Cantidad, MathContext.DECIMAL128).multiply(cartera.CostoInversionEnFactorAjuste, MathContext.DECIMAL128);
				
				ProcesoMLT reg = new ProcesoMLT();
				
			        reg.IdEmpresa=solicitud.IdEmpresa;
				
			       
			        reg.IdEstado="ING";

			        reg.FolioCartera=cartera.FolioCartera;
			        reg.IdCartera=cartera.IdCartera;
			        reg.IdSolicitudAporteRescate=solicitud.IdSolicitudAporteRescate;
			        

			        reg.TipoSolicitud=cartera.TipoSolicitud;
			        
			        reg.FechaCertificacion=solicitud.FechaCertificacion;
			        
			        reg.IdPortafolio=cartera.IdPortafolio;
			        reg.IdAgrupador=solicitud.IdAgrupador;			        
			        
			        reg.INGMontoATasaMercadoActual=cartera.INGMontoATasaMercadoActual;
			        //reg.INGMontoATasaMercadoActual=null;
			        
			        reg.EGRCostoInversionEnFactorAjuste=cartera.EGRCostoInversionEnFactorAjuste;
			        reg.PRO_EGRCostoInversionEnFactorAjuste=cartera.PRO_EGRCostoInversionEnFactorAjuste;
			        

			        reg.CostoInversionEnFactorAjuste=cartera.CostoInversionEnFactorAjuste;
			        reg.PRO_CostoInversionEnFactorAjuste=BigDecimal.ZERO;
			        
			        reg.PRO_CostoTotalRescatado=cartera.PRO_EGRCostoInversionEnFactorAjuste;
			        reg.MontoTotalRescatado=solicitud.MontoMonedaSistema;
			        
			        if ( !Objects.isNull(reg.IdEmpresa) &&  !Objects.isNull(reg.IdEstado) &&  !Objects.isNull(reg.FolioCartera) &&  !Objects.isNull(reg.IdCartera) && !Objects.isNull(reg.IdSolicitudAporteRescate) &&  !Objects.isNull(reg.TipoSolicitud) 
			        		&&  !Objects.isNull(reg.FechaCertificacion) &&  !Objects.isNull(reg.IdPortafolio) &&  !Objects.isNull(reg.IdAgrupador) &&  !Objects.isNull(reg.INGMontoATasaMercadoActual) 
			        		&&  !Objects.isNull(reg.EGRCostoInversionEnFactorAjuste) &&  !Objects.isNull(reg.PRO_EGRCostoInversionEnFactorAjuste) && !Objects.isNull(reg.CostoInversionEnFactorAjuste)
			        		&&  !Objects.isNull(reg.PRO_CostoInversionEnFactorAjuste) && !Objects.isNull(reg.PRO_CostoTotalRescatado) &&  !Objects.isNull(reg.MontoTotalRescatado)) 
			        	
			        	{
			        	DetalleCarteraResct.add(reg);
			        	
			        	}
			        
			        
			        
				
				
			});
			/*
			List<Cartera> carterasReinversion = carterasSolicitud.parallelStream().filter(cartera -> _c.Reinversion.equals(cartera.IdTipoMovimientoFondoMutuo)).collect(Collectors.toList());
			carterasReinversion.stream().forEach(cartera->{
				List<SolicitudAporteRescate> solicitudesAportesReinversion = hashAportes.get(solicitud.IdAgrupador + "_" + solicitud.IdRegimenTributario) != null ? hashAportes.get(solicitud.IdAgrupador + "_" + solicitud.IdRegimenTributario) : new ArrayList<SolicitudAporteRescate>();//solicitudesAportes.parallelStream().filter(solicitudRescate -> solicitudRescate.IdAgrupador.equals(solicitud.IdAgrupador) && !solicitudRescate.IdAgrupador.equals(0L)).collect(Collectors.toList());
				solicitudesAportesReinversion.parallelStream().forEach(solicitudAporteReinversion->{
					String id = solicitud.IdSolicitudAporteRescate + "_" + solicitudAporteReinversion.IdSerieFondo + "_" + solicitudAporteReinversion.IdRegimenTributario;
					List<Reinversion> reinversion = hashReinversiones.get(id) != null ? hashReinversiones.get(id) : new ArrayList<Reinversion>();//reinversiones.parallelStream().filter(item -> item.IdSolicitudAporteRescate.equals(solicitud.IdSolicitudAporteRescate) && item.IdSerieFondoDestino.equals(solicitudAporteReinversion.IdSerieFondo) && item.IdRegimenTributario.equals(solicitudAporteReinversion.IdRegimenTributario)).collect(Collectors.toList());
					BigDecimal porcentaje = reinversion.size() == 0 ? new BigDecimal("100") : reinversion.get(0).Porcentaje;
					porcentaje = porcentaje.divide(new BigDecimal("100"),MathContext.DECIMAL128);
					solicitudAporteReinversion.CostoUF = solicitudAporteReinversion.CostoUF.add(cartera.PRO_EGRCostoInversionEnFactorAjuste.multiply(porcentaje, MathContext.DECIMAL128));
	
				});
			});
			*/
			BigDecimal carteraSum = carterasSolicitud.stream().map(cartera->cartera.PRO_EGRCostoInversionEnFactorAjuste).reduce(BigDecimal.ZERO, (subtotal, cartera)-> subtotal.add(cartera, MathContext.DECIMAL128));
			
			
			if(carteraSum != null && carteraSum.compareTo(BigDecimal.ZERO) != 0) {
				solicitud.CostoUF = carteraSum;
			}
			
		}
		
		if(DetalleCarteraResct.size() > 0){
		
		
		
		try {
			sql.batch(con, SQL_INSERT_PROCESO_MLT, DetalleCarteraResct,ProcesoMLT.class);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
		//	for(ProcesoMLT CarteraResct : DetalleCarteraResct) {
		//		System.out.println("IN_MLT -> IdSolicitudAporteRescate" + CarteraResct.IdSolicitudAporteRescate.toString() );
		//	}

			e.printStackTrace();
		}
		System.out.println("MLT::INSERT PROCESO RESCATES");
		
		
		try {
			sql.batch(con, SQL_UPDATE_SOLICITUDES, DetalleCarteraResct,ProcesoMLT.class);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SQL::UPDATE SOLICITUD RESCATES");
		
		try {
			sql.batch(con, SQL_UPDATE_CARTERAS, DetalleCarteraResct,ProcesoMLT.class);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SQL::UPDATE CARTERAS RESCATES");
		
		}
		
		///APORTES
		System.out.println("INICIO:: APORTES");
		List<SolicitudAporteRescate> solicitudesAportes = sql.query(con, SQL_SELECT_SOLICITUDES_APORTES, params, SolicitudAporteRescate.class);
//		Map<String, List<SolicitudAporteRescate>> hashAportes = solicitudesAportes.parallelStream().filter(solicitud->!solicitud.IdAgrupador.equals(0L)).collect(Collectors.groupingBy(solicitud->solicitud.IdAgrupador + "_" + solicitud.IdRegimenTributario));
		System.out.println("Aportes::" + solicitudesAportes.size());
		System.out.println("Loop APORTES");
		
		 List<ProcesoMLT> DetalleCarteraApor = new ArrayList<ProcesoMLT>();
		for(SolicitudAporteRescate solicitud : solicitudesAportes) {
			List<Cartera> carterasAporte = hashCarterasAportes.get(solicitud.IdSolicitudAporteRescate) != null ? hashCarterasAportes.get(solicitud.IdSolicitudAporteRescate) : new ArrayList<Cartera>();
			carterasAporte.stream().forEach(cartera -> {
				
				BigDecimal montoRescate;
				if (solicitud.MontoTotalRescatado.compareTo( new BigDecimal(0)) > 0) {
					montoRescate=solicitud.MontoTotalRescatado; 
				}else {montoRescate=new BigDecimal(1);};				
				
				/*CALCULO DE COSTO UF*/
				if (solicitud.RES_IndicadorMLT.equals("S") && solicitud.RES_IdRegimenTributario.equals("18Q") && solicitud.IdRegimenTributario.equals("18Q") && solicitud.RES_FondoExtranjero.equals("N") && solicitud.FondoExtranjero.equals("N")) {
					cartera.PRO_CostoInversionEnFactorAjuste=(solicitud.CostoTotalRescatado.multiply(cartera.MontoCompra)).divide(montoRescate, MathContext.DECIMAL128); //HEREDA COSTO
				} else if (Arrays.asList("APV-A", "BF", "DC-DL", "DC-AL", "DCR-R").contains(solicitud.RES_IdRegimenTributario)) {
					cartera.PRO_CostoInversionEnFactorAjuste=(solicitud.CostoTotalRescatado.multiply(cartera.MontoCompra)).divide(montoRescate, MathContext.DECIMAL128); //HEREDA COSTO
				} else {
					cartera.PRO_CostoInversionEnFactorAjuste = (solicitud.MontoMonedaSistema.divide(solicitud.ValorUF,8,RoundingMode.HALF_UP)); //NO HEREDA COSTO
				}
				
				ProcesoMLT reg = new ProcesoMLT();
		        reg.IdEmpresa=solicitud.IdEmpresa;
		        reg.IdEstado="ING";

		        reg.FolioCartera=cartera.FolioCartera;
		        reg.IdCartera=cartera.IdCartera;
		        reg.IdSolicitudAporteRescate=solicitud.IdSolicitudAporteRescate;
		        
		        reg.TipoSolicitud=cartera.TipoSolicitud;
		        reg.FechaCertificacion=solicitud.FechaCertificacion;
		        reg.IdPortafolio=cartera.IdPortafolio;
		        reg.IdAgrupador=solicitud.IdAgrupador;
		        reg.INGMontoATasaMercadoActual=cartera.INGMontoATasaMercadoActual;
		        reg.EGRCostoInversionEnFactorAjuste=cartera.EGRCostoInversionEnFactorAjuste;
		        reg.PRO_EGRCostoInversionEnFactorAjuste=BigDecimal.ZERO;
		        
		        reg.CostoInversionEnFactorAjuste=cartera.CostoInversionEnFactorAjuste;
		        reg.PRO_CostoInversionEnFactorAjuste=cartera.PRO_CostoInversionEnFactorAjuste;
		        
		        reg.PRO_CostoTotalRescatado=solicitud.CostoTotalRescatado;
		        reg.MontoTotalRescatado=solicitud.MontoTotalRescatado;
		        
		        if ( !Objects.isNull(reg.IdEmpresa) &&  !Objects.isNull(reg.IdEstado) &&  !Objects.isNull(reg.FolioCartera) &&  !Objects.isNull(reg.IdCartera) && !Objects.isNull(reg.IdSolicitudAporteRescate) &&  !Objects.isNull(reg.TipoSolicitud) 
			        		&&  !Objects.isNull(reg.FechaCertificacion) &&  !Objects.isNull(reg.IdPortafolio) &&  !Objects.isNull(reg.IdAgrupador) &&  !Objects.isNull(reg.INGMontoATasaMercadoActual) 
			        		&&  !Objects.isNull(reg.EGRCostoInversionEnFactorAjuste) &&  !Objects.isNull(reg.PRO_EGRCostoInversionEnFactorAjuste) && !Objects.isNull(reg.CostoInversionEnFactorAjuste)
			        		&&  !Objects.isNull(reg.PRO_CostoInversionEnFactorAjuste) && !Objects.isNull(reg.PRO_CostoTotalRescatado) &&  !Objects.isNull(reg.MontoTotalRescatado)) 
		        	
		        	{
		        	 DetalleCarteraApor.add(reg);	
		        	}
			});
		}
				
		if(DetalleCarteraApor.size() > 0){
			
		try {
			sql.batch(con, SQL_INSERT_PROCESO_MLT, DetalleCarteraApor,ProcesoMLT.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block

		//	for(ProcesoMLT CarteraApor : DetalleCarteraApor) {
		//		System.out.println("IN_MLTAPO -> IdSolicitudAporteRescate" + CarteraApor.IdSolicitudAporteRescate.toString() );
		//	}

			e.printStackTrace();
		}
		System.out.println("MLT::INSERT PROCESO APORTES");
		

		try {
			sql.batch(con, SQL_UPDATE_SOLICITUDES_APORTES, DetalleCarteraApor,ProcesoMLT.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SQL::UPDATE SOLICITUD APORTES");
		
		try {
			sql.batch(con, SQL_UPDATE_CARTERAS, DetalleCarteraApor,ProcesoMLT.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SQL::UPDATE CARTERAS APORTES");
		}
		//Lista de solicitudes de aportes para versificar rescates reinvertidos
				
		List<Long> IdCartera = new ArrayList<Long>();
		
		for(ProcesoMLT in : DetalleCarteraApor){
			if(!IdCartera.contains(in.IdCartera)) 
				IdCartera.add(in.IdCartera);
		}
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("IdCartera", IdCartera);
			
				
		List<ProximosRescates> rescatesProximos = sql.query(con, SQL_SELECT_RESCATES_PROXIMOS, parametros,ProximosRescates.class);
		
		List<Long> IdSolicitudAporteRescate = new ArrayList<Long>();
		
		for(ProximosRescates in : rescatesProximos){
			if(!IdSolicitudAporteRescate.contains(in.IdSolicitudAporteRescate)) 
				IdSolicitudAporteRescate.add(in.IdSolicitudAporteRescate);
		}
		
		HashMap<String,Object> par = new HashMap<String,Object>();
		par.put("IdEmpresa", entrada.p_IdEmpresa);
		par.put("IdSolicitudAporteRescate", IdSolicitudAporteRescate);
		
		
		List<ProximosAportes> aportesProximos = sql.query(con, SQL_SELECT_APORTES_PROXIMOS, par,ProximosAportes.class);
		
		
		System.out.println("SQL::BUSCAR RESCATES POR REINVERSION RELACIONADOS");
		String fechaProxima="";		
		if(rescatesProximos.size() > 0){
			System.out.println("PROCESO MLT::EXISTEN RESCATES POR REINVERSION RELACIONADOS ::"+ solicitudesAportes.size());
		
			
			//buscamos la fecha maxima para procesar, la minima se reutiliza la del proceso principal
			
			//determina nueva fecha hasta
			List<Long> IdAporteRescateFecha = new ArrayList<Long>();
			
			
			for(ProximosRescates in : rescatesProximos){
				if(!IdAporteRescateFecha.contains(in.IdSolicitudAporteRescate)) 
					IdAporteRescateFecha.add(in.IdSolicitudAporteRescate);
			}
			
			for(ProximosAportes in : aportesProximos){
				if(!IdAporteRescateFecha.contains(in.IdSolicitudAporteRescate)) 
					IdAporteRescateFecha.add(in.IdSolicitudAporteRescate);
			}
			
			HashMap<String,Object> parfech = new HashMap<String,Object>();
			parfech.put("IdEmpresa", entrada.p_IdEmpresa);
			parfech.put("IdSolicitudAporteRescate", IdAporteRescateFecha);
			
			
			
			List<ProximosRescates> proximaFecha = sql.query(con, SQL_SELECT_RESCATES_PROXIMOS_FECHA, parfech,ProximosRescates.class);
			if(proximaFecha.size() > 0){
				for(ProximosRescates in : proximaFecha){
					fechaProxima=in.FechaCertificacion.toString();
				}
			}
			else {
				
				fechaProxima=entrada.p_FechaHasta;
			}
			
		}
		
		//Actualizacion de estados

		try {
			sql.batch(con, SQL_UPDATE_PROCESO_MLT, DetalleCarteraResct,ProcesoMLT.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			sql.batch(con, SQL_UPDATE_PROCESO_MLT, DetalleCarteraApor,ProcesoMLT.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		System.out.println("MLT::UPDATE PROCESO MLT -ESTADOS");
		
		if(rescatesProximos.size() > 0){
			
			//Quitamos resctes proximos del proceso actual, para ser considerados en el siguiente
			try {
				sql.batch(con, SQL_UPDATE_SOLICITUD_PROXIMOS, rescatesProximos,ProximosRescates.class);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				sql.batch(con, SQL_UPDATE_SOLICITUD_PROXIMOS, aportesProximos,ProximosAportes.class);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("MLT::RESCATES_PROXIMOS PROCESO MLT -ESTADOS");
			SalidaPrograma outProg = new SalidaPrograma();
			outProg.P_OUT_ERRORESCONTADOR = 0L;
			AgregarProceso(con,entrada, fechaProxima,outProg);
		}
		

		
		
		if (entrada.p_FechaDesde=="1900-01-01" && entrada.p_FechaHasta=="1900-01-01"){
			try {
				sql.batch(con, SQL_UPDATE_REGISTRO_MLT, solicitudesAportes, SolicitudAporteRescate.class);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		logger.debug("FIN::ActualizarCostoUF");
		
		return out;
	}
	
	private boolean AgregarProceso (Connection con, EntradaActualizarCostoUF in,String fechaProxima, SalidaPrograma out) throws Exception{		
		try {
			logger.debug("Inicio del proceso ingresar programa");
			System.out.println("MLT::AGREGAR PROGRAMAS INICIO");

			//00 VARIABLES DE SISTEMA
			String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
								
			//02 INSCRIBIR PROGRAMAS
			List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdEmpresa.NombreParametro 	= "IdEmpresa";
			paramIdEmpresa.Valor			= in.p_IdEmpresa.toString();
			paramsParametros.add(paramIdEmpresa);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdUsuario.NombreParametro 	= "IdUsuario";
			paramIdUsuario.Valor			= in.p_IdUsuario.toString();
			paramsParametros.add(paramIdUsuario);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdPortafolio = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdPortafolio.NombreParametro 	= "IdPortafolio";
			paramIdPortafolio.Valor				= in.p_Portafolio.toString();
			paramsParametros.add(paramIdPortafolio);
			
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramFechaDesde = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramFechaDesde.NombreParametro 	= "FechaDesde";
			paramFechaDesde.Valor				= in.p_FechaDesde;
			paramsParametros.add(paramFechaDesde);
			
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramFechaHasta = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramFechaHasta.NombreParametro 	= "FechaHasta";
			paramFechaHasta.Valor				= fechaProxima;
			paramsParametros.add(paramFechaHasta);
			
			System.out.println("MLT:: PROGRAMA NUEVO PORTAFOLIO ::"+ in.p_Portafolio.toString());
			System.out.println("MLT:: PROGRAMA NUEVA FECHA DESDE ::"+ in.p_FechaDesde);
			System.out.println("MLT:: PROGRAMA NUEVA FECHA HASTA ::"+ fechaProxima);

			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
			paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
			paramsIn.p_IdModulo			= in.p_IdModulo;
			paramsIn.p_IdUsuario		= in.p_IdUsuario;
			paramsIn.p_IdTipoPrograma	= "COSTO_TRIBUTARIO_COMPLEMENTO";
			paramsIn.p_Segundos			= 0L;
			paramsIn.p_FechaSistema		= FechaSistema; 
			paramsIn.p_Parametros		= paramsParametros;
			AgregarPrograma AP = new AgregarPrograma();
			AgregarPrograma.OutAgregarPrograma result = AP.logica(con, paramsIn);
			
			out.P_OUT_IDPROGRAMA		= result.P_OUT_IDPROGRAMA;
			out.P_OUT_ERRORESCONTADOR 	= result.P_OUT_ERRORESCONTADOR;
			System.out.println("MLT::AGREGAR PROGRAMAS FIN");
			if ( out.P_OUT_ERRORESCONTADOR.equals(1L) ){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, result.P_OUT_MENSAJE, new HashMap<>() ,con, in.p_IdEmpresa));
			} else {
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "PROGRAMA_INGRESADO", new HashMap<String,String>() ,con,in.p_IdEmpresa));
			};
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.debug("== ERROR ==",e);
			throw e;
		}		
		return true;
	};
	
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			//entradaActualizarCostoUF.p_FechaDesde = "2010-01-01";
			//entradaActualizarCostoUF.p_FechaHasta = "2010-12-31";
			
			//entradaActualizarCostoUF.p_FechaDesde = "2015-01-01";
			//entradaActualizarCostoUF.p_FechaHasta = "2015-06-30";
			
			//entradaActualizarCostoUF.p_FechaDesde = "2015-07-01";
			//entradaActualizarCostoUF.p_FechaHasta = "2015-12-31";
			
			//String [] fechaDesde = {"2008-01-01","2009-01-01","2010-01-01","2011-01-01","2011-07-01","2012-01-01","2012-07-01","2013-01-01","2013-07-01","2014-01-01","2014-07-01","2015-01-01","2015-07-01","2016-01-01","2016-07-01","2017-01-01","2017-07-01","2018-01-01","2018-07-01","2019-01-01","2019-07-01","2020-01-01"};
			//String [] fechaHasta = {"2008-12-31","2009-12-31","2010-12-31","2011-06-30","2011-12-31","2012-06-30","2012-12-31","2013-06-30","2013-12-31","2014-06-30","2014-12-31","2015-06-30","2015-12-31","2016-06-30","2016-12-31","2017-06-30","2017-12-31","2018-06-30","2018-12-31","2019-06-30","2019-12-31","2020-06-30"};
			
			String [] fechaDesde = {"2023-11-30"};
			//String [] fechaHasta = {"2022-11-16"};
			
			String [] fechaHasta = {"2024-09-06"};


			
			//String [] fechaDesde = {"2022-10-06"};
			//String [] fechaHasta = {"2022-10-12"};
			
			try {
				for(int i= 0; i<fechaDesde.length; i++){
					ActualizarCostoUF_MLT actualiza = new ActualizarCostoUF_MLT();
					EntradaActualizarCostoUF entradaActualizarCostoUF = new EntradaActualizarCostoUF();
					entradaActualizarCostoUF.p_IdEmpresa = 1L;
					entradaActualizarCostoUF.p_IdUsuario = 21L;
					entradaActualizarCostoUF.p_IdModulo = "FMM";
					entradaActualizarCostoUF.p_FechaDesde = fechaDesde[i];
					entradaActualizarCostoUF.p_FechaHasta = fechaHasta[i];
					entradaActualizarCostoUF.p_Portafolio=	486979L;
					//entradaActualizarCostoUF.p_Portafolio=	478411L;
					
					SalidaActualizarCostoUF salida = actualiza.logica(con, entradaActualizarCostoUF);
					System.out.println(salida);
					con.commit();
				}
				
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
		
	}
	
}
