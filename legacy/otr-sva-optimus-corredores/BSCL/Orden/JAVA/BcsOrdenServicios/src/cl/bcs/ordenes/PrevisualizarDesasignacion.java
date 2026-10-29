package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class PrevisualizarDesasignacion implements JavaCall {
	
	private final String SQL_PREVISUALIZAR_DESASIGNACION = loadResourceToString("cl/bcs/ordenes/sql/PrevisualizarDesasignacion_ConsultarAsignacionesParaAnular.sql");
	private final String SQL_PREVISUALIZAR_DESASIGNACION_MASIVA_POR_ID = loadResourceToString("cl/bcs/ordenes/sql/PrevisualizarDesasignacion_ConsultarAsignacionesParaAnularMasivaPorId.sql");

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		PrevisualizarDesasignacionEntrada in=gson.fromJson(jin.toString() ,PrevisualizarDesasignacionEntrada.class);
		PrevisualizarDesasignacionSalida out=logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		for (MensajeSalida msje : out.p_out_Mensajes) {
			msje.descripcion = Diccionario.getMensaje(con,in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class PrevisualizarDesasignacionEntrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public Long p_IdLibroOrdenes;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoInstrumento;
		public String p_IdTipoCanalIngreso;
		public String p_AccionOrden;
		public String p_IdBolsa;
		public Long p_IdInstrumento;
		public Long p_IdAsignacionMasiva;
		public Long p_IdPortafolio;
		public String p_IdMonedaOperacion;
		public String p_IdCondicionDeLiquidacion;
		public String p_MarcaAsignacion;
	}
	
	public static class PrevisualizarDesasignacionSalida extends BcsPojo {
		public List<Asignacion_Proceso> p_out_Asignaciones;
		public List<MensajeSalida> p_out_Mensajes; 
		public Long P_OUT_ERRORESCONTADOR;
		
		public PrevisualizarDesasignacionSalida() {
			p_out_Asignaciones = new ArrayList<>();
			p_out_Mensajes = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
	}
	
	public PrevisualizarDesasignacionSalida logica(Connection conn,PrevisualizarDesasignacionEntrada entrada) throws Exception {
		//inicializacion salida
		PrevisualizarDesasignacionSalida salida = new PrevisualizarDesasignacionSalida();
				
		//requisitos basicos
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida requisitosBasicosSalida = basicos.logica(conn, entrada.p_IdEmpresa,entrada.p_IdUsuario,entrada.p_IdModulo);
		
		
		if (MensajeSalida.getNumeroErrores(requisitosBasicosSalida.p_out_Mensajes) > 0) {
			salida.p_out_Mensajes.addAll(requisitosBasicosSalida.p_out_Mensajes);
			return salida;
		}
	
		if (entrada.p_IdAsignacionMasiva == null) {
			if (entrada.p_IdTipoCanalIngreso == null) {
			salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"TIPO_CANAL_INGRESO_VACIO",""));
			return salida;
			}
			if (entrada.p_FechaDesde== null) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FECHA_DESDE_VACIA",""));
				return salida;
			}
			if (entrada.p_FechaHasta == null) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FECHA_HASTA_VACIA",""));
				return salida;
			}
			
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
				sdf.parse(entrada.p_FechaDesde);
			}
			catch(ParseException e) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FORMATO_FECHA_DESDE_INCORRECTO",""));
				return salida;
			}
			
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
				sdf.parse(entrada.p_FechaHasta);
			}
			catch(ParseException e) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FORMATO_FECHA_HASTA_INCORRECTO",""));
				return salida;
			}
		}

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
		
		SQL sql = new SQL();
		HashMap<String,Object> parms = new HashMap<>(2);
		List<Asignacion_Proceso> asignaciones;
		
		if (entrada.p_IdAsignacionMasiva != null) {
            parms.put("IdAsignacionMasiva", entrada.p_IdAsignacionMasiva);
            asignaciones = sql.query(conn, SQL_PREVISUALIZAR_DESASIGNACION_MASIVA_POR_ID, parms, Asignacion_Proceso.class);
			salida.p_out_Asignaciones.addAll(asignaciones);
        } else {
            parms.put("FechaDesde", entrada.p_FechaDesde);
            parms.put("FechaHasta", entrada.p_FechaHasta);
            asignaciones = sql.query(conn, SQL_PREVISUALIZAR_DESASIGNACION, parms, Asignacion_Proceso.class);

			for (Asignacion_Proceso asig : asignaciones) {
				if (entrada.p_AccionOrden != null && !asig.AccionOrden.equalsIgnoreCase(entrada.p_AccionOrden)) {
					continue;
				}
				if (entrada.p_IdBolsa != null  && !asig.IdBolsa.equalsIgnoreCase(entrada.p_IdBolsa)) {
					continue;
				}
				if (entrada.p_IdTipoCanalIngreso != null && (asig.IdTipoCanalIngreso == null || !asig.IdTipoCanalIngreso.equalsIgnoreCase(entrada.p_IdTipoCanalIngreso))) {
					continue;
				}
				if (entrada.p_IdInstrumento != null && (asig.IdInstrumento.longValue() != entrada.p_IdInstrumento.longValue()) ) {
					continue;
				}
				if (entrada.p_IdLibroOrdenes != null && (asig.IdLibroOrdenes.longValue() != entrada.p_IdLibroOrdenes.longValue()) ) {
					continue;
				}
				if (entrada.p_IdTipoInstrumento != null && !asig.IdTipoInstrumento.equalsIgnoreCase(entrada.p_IdTipoInstrumento)) {
					continue;
				}
				if (!asig.IdTipoCanalIngreso.equalsIgnoreCase(entrada.p_IdTipoCanalIngreso)) {
					continue;
				}
				if (entrada.p_IdPortafolio != null && (asig.IdPortafolio.longValue() != entrada.p_IdPortafolio.longValue()) ) {
					continue;
				}
				if (entrada.p_IdMonedaOperacion != null && (asig.IdMonedaOperacion == null || !asig.IdMonedaOperacion.equalsIgnoreCase(entrada.p_IdMonedaOperacion))) {
					continue;
				}
				if (entrada.p_IdCondicionDeLiquidacion != null && (asig.IdCondicionDeLiquidacion == null || !asig.IdCondicionDeLiquidacion.equalsIgnoreCase(entrada.p_IdCondicionDeLiquidacion))) {
					continue;
				}
				if (entrada.p_MarcaAsignacion != null && (asig.MarcaAsignacion == null || !asig.MarcaAsignacion.equalsIgnoreCase(entrada.p_MarcaAsignacion))) {
					continue;
				}
				salida.p_out_Asignaciones.add(asig);
			}
        }
		return salida;	
	}
	
	public static class Asignacion_Proceso extends BcsPojo {
		public Long IdAsignacion;
		public Long IdAsignacionRef;
		public Timestamp FechaAsignacion;
		public Timestamp FechaIngresoRegistro;
		public BigDecimal CantidadAsignada;
		public BigDecimal MontoAsignado;
		public BigDecimal ValorRescateAsignado;
		public BigDecimal MontoAsignadoLocal;
		public BigDecimal MontoAsignadoLiquidacion;
		public String IdEstadoAsignacion;
		public Long IdTransaccion;
		public Long IdOrden;
		public Long IdLibroOrdenes;
		public String EsAPlazo;
		public Long FolioCartera;
		
		public String IdBolsa;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoCanalIngreso;
		public String NombreTipoCanalIngreso;
		public String NombreLibroOrdenes;
		public String IdTipoInstrumento;
		public Long IdAsignacionMasiva;
		public String AccionOrden;

		public Long IdCliente;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public String FolioTransaccion;
		public BigDecimal Precio;
		public String IdMonedaOperacion;
		public String IdMonedaLiquidacionOrden;
		public BigDecimal ValorTipoCambio;
		public String CodigoPortafolio;
		public String IdCondicionDeLiquidacion;
		public String MarcaAsignacion;

	}
	
	public static void main(String[]args) throws Exception{
		Long idEmpresa = 1L;
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")){
			PrevisualizarDesasignacionEntrada ent = new PrevisualizarDesasignacionEntrada();
			ent.p_IdEmpresa = 1L;
			ent.p_IdUsuario = 97L;
			ent.p_IdModulo = "ORD";
			ent.p_FechaDesde = "2026-07-03 13:08:24";
			ent.p_IdBolsa = "BCS";
			ent.p_IdTipoCanalIngreso = "TRA";
			ent.p_IdInstrumento = null;
			ent.p_FechaHasta = "2026-07-04 02:59:59";
			ent.p_IdAsignacionMasiva = null;
			ent.p_IdPortafolio = 1124L;
			PrevisualizarDesasignacion d = new PrevisualizarDesasignacion();
			PrevisualizarDesasignacionSalida salida = d.logica(conn, ent);
			for (Asignacion_Proceso asig : salida.p_out_Asignaciones) {
				System.out.println(asig.toString());
			}
		}	
	}
}