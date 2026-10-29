package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
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

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarDetalleSolicitud implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarDetalleSolicitud.class);
	
	public static class EntradaConsultarDetalleSolicitud{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_SolicitudesAporteRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarDetalleSolicitud{
		public List<OutSolicitudesAporteRescate> P_OUT_RESULTADO;
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConsultarDetalleSolicitud() {
			this.P_OUT_RESULTADO = new ArrayList<OutSolicitudesAporteRescate>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutSolicitudesAporteRescate{
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
		public Long IdPortafolio;
		public Date FechaEmision;
		public Timestamp FechaIngresoRegistro;
		public String IdTipoMovimientoFondoMutuo;
		public String TipoSolicitud;
		public String DescripcionTipoMovimiento;
		public String CalculoMontoCuotaUI;
		public String EstadoSolicitud;
		public String NombreEstado;
		public String NombreFondo;
		public String NombreFondoReal;
		public String IdSerieFondo;
		public String NombreSerieFondo;
		public String CalculoPorMontoCuotaUI;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public BigDecimal Monto;
		public BigDecimal MontoRetencion;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public Long IdInstrumento;
		public String IdRegimenTributario;
		public String DescripcionPortafolio;
		public Date FechaCertificacion;
		public Date FechaLiquidacion;
		public Date FechaAceptacion;
		public Date FechaAnulacion;
		public Timestamp FechaAceptacionRegistro;
		public Timestamp FechaCertificacionRegistro;
		public Timestamp FechaLiquidacionRegistro;
		public Timestamp FechaAnulacionRegistro;
		public String NombreEmpleado;
		public String IdEmisor;
		public Long IdEmpresa;
		public String IdMoneda;
		public String TextoPreferenciaPago;
		public String Glosa;
		public Long DecimalesValorCuota;
		public Long DecimalesCuota;
		public String EsOvernightUI;
		public Long IdSolicitudAporteRescateRelacionado;
		public String DescripcionPortafolioDestino;
		public Long IdPortafolioDestino;
		public Long IdClienteDestino;
		public String DocumentoIdentidadDestino;
		public String NombreCompletoDestino;
		public String EsIndicadorMLTUI;
		public BigDecimal MontoComision;
		public String IdTipoMovimientoFondoMutuoRelacionado;
		public String DescripcionTipoMovimientoRelacionado;
		public String IdMonedaFondo;
		public String EsDiferido;
		public String EsDiferidoUI;
		public BigDecimal CostoMonedaLocal;
		public BigDecimal ParidadFechaInversion;
		public BigDecimal MontoInversion;
		public BigDecimal NumeroCuotasInversion;
		public BigDecimal ValorCuotaInversion;
		public BigDecimal CostoMonedaLocalInversion;
		public BigDecimal ParidadFechaCertificacion;
		public String CalculoPorMontoCuota;
		public String IdSerieFondoDestino;
		public String IdEmisorDestino;
		public String SolicitudBatch;
		public String OrigenOperacion;
		public String ComisionInformada;
		public String EstadoLiquidacion;
		public String NombreEstadoLiquidacion;
		public Long FolioCartaLiquidacion;
		public BigDecimal CostoUF;
		public BigDecimal ValorUFFechaInversion;
		public BigDecimal Spread;
		public Date FechaOriginalInversion;
		public BigDecimal MontoMovimientoAsociado;
		public BigDecimal NumeroCuotasMovimientoAsociado;
		public String IndicadorMLT;
		public String IdEmisorOrigen;
		public String CodigoPortafolio;
		public String CodigoPortafolioDestino;
		public String CodigoSerie;
		public String IdFondoRelacionado;
		public String CodigoSerieRelacionado;
		public String DescripcionRegimenTributario;
		public String IdFondo;
		public String DescripcionTipoSolicitud;
		public BigDecimal MontoMonedaSistema;
		public Long IdDividendo;
		public String Switch;
		public String SwitchUI;
		public String IdTipoCanalIngreso;
		public Long FolioContrato;
		public String Producto;
		public Long IdProductoAgrupador;
		public String CodigoUsuario;
		//SPOT
	 	public Long IdIngresoEgreso;
		public String NombreEstadoTesoreria;	
		public String FechaLiquidacionTesoreria;
		public Long IdOperacionSpot;
		public String NombreEstadoSpot;
		public String NombreEstadoConfirmacionSpot;
		public Date FechaLiquidacionSpot;
		public String IdTipoOperacionSpot;
		public String NombreTipoOperacionSpot;
		//OMNIBUS
		public Long IdOmnibus;
		//REINVERSION 1N
		public Long IdAgrupador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarDetalleSolicitud jin = gson.fromJson(in.toString(), EntradaConsultarDetalleSolicitud.class);
		SalidaConsultarDetalleSolicitud out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarDetalleSolicitud logica(Connection con, EntradaConsultarDetalleSolicitud entrada) throws Exception{
		logger.info("INICIO::ConsultarDetalleSolicitud");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarDetalleSolicitud out = new SalidaConsultarDetalleSolicitud();

		if(entrada.p_SolicitudesAporteRescate == null || entrada.p_SolicitudesAporteRescate == 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<ConsultarSolicitudAporteRescateUI.Solicitud> solicitudes = obtenerInformacionSolicitud(con, entrada);
		 
		out.P_OUT_RESULTADO = obtenerResultado(solicitudes);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarDetalleSolicitud", finServicio - inicioServicio);
		return out;
	}
	
	private List<ConsultarSolicitudAporteRescateUI.Solicitud> obtenerInformacionSolicitud(Connection con, EntradaConsultarDetalleSolicitud entrada) throws Exception{
		logger.debug("INICIO::ConsultarDetalleSolicitud.obtenerInformacionSolicitud");
		long inicio = System.currentTimeMillis();
		ConsultarSolicitudAporteRescateUI consultarSolicitudAporteRescateUI = new ConsultarSolicitudAporteRescateUI();
		ConsultarSolicitudAporteRescateUI.EntradaConsultarSolicitudAporteRescateUI entradaConsultarSolicitudAporteRescateUI = new ConsultarSolicitudAporteRescateUI.EntradaConsultarSolicitudAporteRescateUI();
		entradaConsultarSolicitudAporteRescateUI.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarSolicitudAporteRescateUI.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarSolicitudAporteRescateUI.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarSolicitudAporteRescateUI.p_IdSolicitudAporteRescate = entrada.p_SolicitudesAporteRescate;
		ConsultarSolicitudAporteRescateUI.SalidaConsultarSolicitudAporteRescateUI salidaConsultarSolicitudAporteRescateUI = consultarSolicitudAporteRescateUI.logica(con, entradaConsultarSolicitudAporteRescateUI);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarDetalleSolicitud.obtenerInformacionSolicitud", fin - inicio);
		return salidaConsultarSolicitudAporteRescateUI.P_OUT_RESULTADO;
		
	}
	
	private List<OutSolicitudesAporteRescate> obtenerResultado(List<ConsultarSolicitudAporteRescateUI.Solicitud> solicitudes){
		logger.debug("INICIO::ConsultarDetalleSolicitud.obtenerResultado");
		long inicio = System.currentTimeMillis();
		List<OutSolicitudesAporteRescate> resultados = solicitudes.parallelStream().map(solicitud->{
			OutSolicitudesAporteRescate resultado = new OutSolicitudesAporteRescate();
			resultado.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
			resultado.IdCliente = solicitud.IdCliente;
			resultado.IdPortafolio = solicitud.IdPortafolio;
			resultado.FechaEmision = solicitud.FechaEmision;
			resultado.FechaIngresoRegistro = solicitud.FechaIngresoRegistro;
			resultado.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
			resultado.TipoSolicitud = solicitud.TipoSolicitud;
			resultado.DescripcionTipoMovimiento = solicitud.DescripcionTipoMovimiento;
			resultado.CalculoMontoCuotaUI = solicitud.CalculoMontoCuotaUI;
			resultado.EstadoSolicitud = solicitud.EstadoSolicitud;
			resultado.NombreEstado = solicitud.NombreEstado;
			resultado.NombreFondo = solicitud.NombreFondo;
			resultado.NombreFondoReal = solicitud.NombreFondoReal;
			resultado.IdSerieFondo = solicitud.IdSerieFondo;
			resultado.NombreSerieFondo = solicitud.NombreSerieFondo;
			resultado.CalculoPorMontoCuotaUI = solicitud.CalculoPorMontoCuotaUI;
			resultado.NumeroCuotas = solicitud.NumeroCuotas;
			resultado.ValorCuota = solicitud.ValorCuota;
			resultado.Monto = solicitud.Monto;
			resultado.MontoRetencion = solicitud.MontoRetencion;
			resultado.DocumentoIdentidad = solicitud.DocumentoIdentidad;
			resultado.NombreCompleto = solicitud.NombreCompleto;
			resultado.Nombres = solicitud.Nombres;
			resultado.ApellidoPaterno = solicitud.ApellidoPaterno;
			resultado.ApellidoMaterno = solicitud.ApellidoMaterno;
			resultado.IdInstrumento = solicitud.IdInstrumento;
			resultado.IdRegimenTributario = solicitud.IdRegimenTributario;
			resultado.DescripcionPortafolio = solicitud.DescripcionPortafolio;
			resultado.FechaCertificacion = solicitud.FechaCertificacion;
			resultado.FechaLiquidacion = solicitud.FechaLiquidacion;
			resultado.FechaAceptacion = solicitud.FechaAceptacion;
			resultado.FechaAnulacion = solicitud.FechaAnulacion;
			resultado.FechaAceptacionRegistro = solicitud.FechaAceptacionRegistro;
			resultado.FechaCertificacionRegistro = solicitud.FechaCertificacionRegistro;
			resultado.FechaLiquidacionRegistro = solicitud.FechaLiquidacionRegistro;
			resultado.FechaAnulacionRegistro = solicitud.FechaAnulacionRegistro;
			resultado.NombreEmpleado = solicitud.NombreEmpleado;
			resultado.IdEmisor = solicitud.IdEmisor;
			resultado.IdEmpresa = solicitud.IdEmpresa;
			resultado.IdMoneda = solicitud.IdMoneda;
			resultado.TextoPreferenciaPago = solicitud.TextoPreferenciaPago;
			resultado.Glosa = solicitud.Glosa;
			resultado.DecimalesValorCuota = solicitud.DecimalesValorCuota;
			resultado.DecimalesCuota = solicitud.DecimalesCuota;
			resultado.EsOvernightUI = solicitud.EsOvernightUI;
			resultado.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado;
			resultado.DescripcionPortafolioDestino = solicitud.DescripcionPortafolioDestino;
			resultado.IdPortafolioDestino = solicitud.IdPortafolioDestino;
			resultado.IdClienteDestino = solicitud.IdClienteDestino;
			resultado.DocumentoIdentidadDestino = solicitud.DocumentoIdentidadDestino;
			resultado.NombreCompletoDestino = solicitud.NombreCompletoDestino;
			resultado.EsIndicadorMLTUI = solicitud.EsIndicadorMLTUI;
			resultado.MontoComision = solicitud.MontoComision;
			resultado.IdTipoMovimientoFondoMutuoRelacionado = solicitud.IdTipoMovimientoFondoMutuoRelacionado;
			resultado.DescripcionTipoMovimientoRelacionado = solicitud.DescripcionTipoMovimientoRelacionado;
			resultado.IdMonedaFondo = solicitud.IdMonedaFondo;
			resultado.EsDiferido = solicitud.EsDiferido;
			resultado.EsDiferidoUI = solicitud.EsDiferidoUI;
			resultado.CostoMonedaLocal = solicitud.CostoMonedaLocal;
			resultado.ParidadFechaInversion = solicitud.ParidadFechaInversion;
			resultado.MontoInversion = solicitud.MontoInversion;
			resultado.NumeroCuotasInversion = solicitud.NumeroCuotasInversion;
			resultado.ValorCuotaInversion = solicitud.ValorCuotaInversion;
			resultado.CostoMonedaLocalInversion = solicitud.CostoMonedaLocalInversion;
			resultado.ParidadFechaCertificacion = solicitud.ParidadFechaCertificacion;
			resultado.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			resultado.IdSerieFondoDestino = solicitud.IdSerieFondoDestino;
			resultado.IdEmisorDestino = solicitud.IdEmisorDestino;
			resultado.SolicitudBatch = solicitud.SolicitudBatch;
			resultado.OrigenOperacion = solicitud.OrigenOperacion;
			resultado.ComisionInformada = solicitud.ComisionInformada;
			resultado.EstadoLiquidacion = solicitud.EstadoLiquidacion;
			resultado.NombreEstadoLiquidacion = solicitud.NombreEstadoLiquidacion;
			resultado.FolioCartaLiquidacion = solicitud.FolioCartaLiquidacion;
			resultado.CostoUF = solicitud.CostoUF;
			resultado.ValorUFFechaInversion = solicitud.ValorUFFechaInversion;
			resultado.Spread = solicitud.Spread;
			resultado.FechaOriginalInversion = solicitud.FechaOriginalInversion;
			resultado.MontoMovimientoAsociado = solicitud.MontoMovimientoAsociado;
			resultado.NumeroCuotasMovimientoAsociado = solicitud.NumeroCuotasMovimientoAsociado;
			resultado.IndicadorMLT = solicitud.IndicadorMLT;
			resultado.IdEmisorOrigen = solicitud.IdEmisorOrigen;
			resultado.CodigoPortafolio = solicitud.CodigoPortafolio;
			resultado.CodigoPortafolioDestino = solicitud.CodigoPortafolioDestino;
			resultado.CodigoSerie = solicitud.CodigoSerie;
			resultado.IdFondoRelacionado = solicitud.IdFondoRelacionado;
			resultado.CodigoSerieRelacionado = solicitud.CodigoSerieRelacionado;
			resultado.DescripcionRegimenTributario = solicitud.DescripcionRegimenTributario;
			resultado.IdFondo = solicitud.IdFondo;
			resultado.DescripcionTipoSolicitud = solicitud.DescripcionTipoSolicitud;
			resultado.MontoMonedaSistema = solicitud.MontoMonedaSistema;
			resultado.IdDividendo = solicitud.IdDividendo;
			resultado.Switch = solicitud.Switch;
			resultado.SwitchUI = solicitud.SwitchUI;
			resultado.IdTipoCanalIngreso = solicitud.IdTipoCanalIngreso;
			resultado.FolioContrato = solicitud.FolioContrato;
			resultado.Producto = solicitud.Producto;
			resultado.IdProductoAgrupador = solicitud.IdProductoAgrupador;
			resultado.CodigoUsuario = solicitud.CodigoUsuario;
			resultado.IdIngresoEgreso = solicitud.IdIngresoEgreso;
			resultado.NombreEstadoTesoreria = solicitud.NombreEstadoTesoreria;	
			resultado.FechaLiquidacionTesoreria = solicitud.FechaLiquidacionTesoreria;
			resultado.IdOperacionSpot = solicitud.IdOperacionSpot;
			resultado.NombreEstadoSpot = solicitud.NombreEstadoSpot;
			resultado.NombreEstadoConfirmacionSpot = solicitud.NombreEstadoConfirmacionSpot;
			resultado.FechaLiquidacionSpot = solicitud.FechaLiquidacionSpot;
			resultado.IdTipoOperacionSpot = solicitud.IdTipoOperacionSpot;
			resultado.NombreTipoOperacionSpot = solicitud.NombreTipoOperacionSpot;
			resultado.IdOmnibus = solicitud.IdOmnibus;
			resultado.IdAgrupador = solicitud.IdAgrupador;
			return resultado;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarDetalleSolicitud.obtenerResultado", fin - inicio);
		return resultados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":54,\"p_IdModulo\":\"FMM\",\"p_SolicitudesAporteRescate\":399131}";
		
		EntradaConsultarDetalleSolicitud Objin = gson.fromJson(Json, EntradaConsultarDetalleSolicitud.class);
		ConsultarDetalleSolicitud demo = new ConsultarDetalleSolicitud();				
		try {
			SalidaConsultarDetalleSolicitud ObjOut = demo.logica(con, Objin);
			//con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}

}
