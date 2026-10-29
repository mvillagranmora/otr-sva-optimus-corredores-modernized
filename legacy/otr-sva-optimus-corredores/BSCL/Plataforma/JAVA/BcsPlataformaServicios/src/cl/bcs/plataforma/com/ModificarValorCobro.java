package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarValorCobro implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ModificarValorCobro.class);

	private final String SQL_SELECT_VALOR_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarValorCobro_Select_Valor.sql");
	private final String SQL_UPDATE_VALOR_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarValorCobro_Update_Valor.sql");

	public static class EntradaModificarValorCobro extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdValorCobro;
		public String p_DescripcionValorCobro;
		public String p_IdTipoProducto;
		public String p_IdCobro;
		public BigDecimal p_ValorCobro;
		public BigDecimal p_ValorTopeMinimoOrden;
		public String p_IdMoneda;
		public String p_IdEstado;
	}

	public static class ErrorDetalle extends BcsPojo {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class AdvertenciaDetalle extends BcsPojo {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	public static class SalidaModificarValorCobro extends BcsPojo {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaModificarValorCobro() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}

	public static class ValorCobro extends BcsPojo {
		public String IdValorCobro;
		public String DescripcionValorCobro;
		public String IdTipoProducto;
		public String IdCobro;
		public BigDecimal ValorCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;
		public String IdEstado;
		public Long IdEmpresa;
	}
	
	public static class CamposModificados extends BcsPojo{
        public String IdValorCobro;
        public String NombreCampo;
        public String ValorAntiguo;
        public String ValorNuevo;    
	}

	public SalidaModificarValorCobro logica(Connection conn, EntradaModificarValorCobro in) throws Exception {
		logger.info("INICIO::ModificarValorCobro.logica");
		long inicio = System.currentTimeMillis();
		SalidaModificarValorCobro salida = new SalidaModificarValorCobro();
		SQL sql = new SQL();

		try {
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("IdValorCobro", in.p_IdValorCobro);
			ValorCobro valorCobroOriginal = sql.query(conn, SQL_SELECT_VALOR_COBRO, params, ValorCobro.class).get(0);
			
			// Actualizar valor de cobro
			logger.debug("UPDATE valor cobro");
			ValorCobro valorCobroModificado = new ValorCobro();
			valorCobroModificado.IdValorCobro = in.p_IdValorCobro;
			valorCobroModificado.DescripcionValorCobro = in.p_DescripcionValorCobro;
			valorCobroModificado.IdTipoProducto = in.p_IdTipoProducto;
			valorCobroModificado.IdCobro = in.p_IdCobro;
			valorCobroModificado.ValorCobro = in.p_ValorCobro;
			valorCobroModificado.ValorTopeMinimoOrden = in.p_ValorTopeMinimoOrden;
			valorCobroModificado.IdMoneda = in.p_IdMoneda;
			valorCobroModificado.IdEstado = in.p_IdEstado;
			valorCobroModificado.IdEmpresa = in.p_IdEmpresa;

			sql.batch(conn, SQL_UPDATE_VALOR_COBRO, java.util.Arrays.asList(valorCobroModificado), ValorCobro.class);
			
			List<CamposModificados> camposModificados = camposModificados(conn, valorCobroOriginal, valorCobroModificado);      
		    agregarLog(conn, in, camposModificados);

			salida.P_OUT_REFID = 1L;

		} catch (Exception ex) {
			logger.error("Error en ModificarValorCobro: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_MODIFICAR_VALOR_COBRO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarValorCobro.agregarLog {} ms", fin - inicio);
		return salida;
	}
	
	private void agregarLog(Connection con, EntradaModificarValorCobro entrada, List<CamposModificados> solicitudCambio) throws Exception {
		logger.debug("INICIO::ModificarValorCobro.agregarLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = entrada.p_IdUsuario;
		log.IdTipoLog = "MODIFICACION";
		log.IdModulo = entrada.p_IdModulo;
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdRegistro = entrada.p_IdValorCobro;
		log.IdTipoRegistro = "PLANTILLA_COBROS";
		log.EstadoRegistro = "";
		log.IdProceso = 0L;
		log.EstadoProceso = "";
		log.NombreProceso = "Modificar valor de cobros";

		entradaAgregarLog.p_Log = Collections.singletonList(log);

		entradaAgregarLog.p_LogDetalle = solicitudCambio.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();			
			detalleLog.IdTrabajo = 0L; 
			detalleLog.IdCabeceraLog = entrada.p_IdUsuario;			
			detalleLog.SubRegistro = entrada.p_IdValorCobro;
			detalleLog.CampoRegistro = solicitud.NombreCampo;
			detalleLog.ValorAntiguo = solicitud.ValorAntiguo; 
			detalleLog.ValorNuevo = solicitud.ValorNuevo;	
			return detalleLog;
		}).collect(Collectors.toList());

		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarValorCobro.agregarLog {} ms", fin - inicio);
	}
	
	private List<CamposModificados> camposModificados(Connection con, ValorCobro valorAntes, ValorCobro valorDespues) throws Exception {
	    List<CamposModificados> listaSalida = new ArrayList<>();

		if ((valorAntes.DescripcionValorCobro == null && valorDespues.DescripcionValorCobro != null)
				|| (valorAntes.DescripcionValorCobro != null && (valorDespues.DescripcionValorCobro == null
						|| !valorAntes.DescripcionValorCobro.equalsIgnoreCase(valorDespues.DescripcionValorCobro)))) {
	        CamposModificados aux = new CamposModificados();
	        aux.IdValorCobro = valorAntes.IdValorCobro;
	        aux.NombreCampo = "DescripcionValorCobro";
	        aux.ValorAntiguo = valorAntes.DescripcionValorCobro;
	        aux.ValorNuevo = valorDespues.DescripcionValorCobro;
	        listaSalida.add(aux);
	    }

		if ((valorAntes.IdTipoProducto == null && valorDespues.IdTipoProducto != null)
				|| (valorAntes.IdTipoProducto != null && (valorDespues.IdTipoProducto == null
						|| !valorAntes.IdTipoProducto.equalsIgnoreCase(valorDespues.IdTipoProducto)))) {
	        CamposModificados aux = new CamposModificados();
	        aux.IdValorCobro = valorAntes.IdValorCobro;
	        aux.NombreCampo = "IdTipoProducto";
	        aux.ValorAntiguo = valorAntes.IdTipoProducto;
	        aux.ValorNuevo = valorDespues.IdTipoProducto;
	        listaSalida.add(aux);
	    }

		if ((valorAntes.IdCobro == null && valorDespues.IdCobro != null) || (valorAntes.IdCobro != null
				&& (valorDespues.IdCobro == null || !valorAntes.IdCobro.equalsIgnoreCase(valorDespues.IdCobro)))) {
	        CamposModificados aux = new CamposModificados();
	        aux.IdValorCobro = valorAntes.IdValorCobro;
	        aux.NombreCampo = "IdCobro";
	        aux.ValorAntiguo = valorAntes.IdCobro;
	        aux.ValorNuevo = valorDespues.IdCobro;
	        listaSalida.add(aux);
	    }

		if ((valorAntes.ValorCobro == null && valorDespues.ValorCobro != null)
				|| (valorAntes.ValorCobro != null && (valorDespues.ValorCobro == null
						|| valorAntes.ValorCobro.compareTo(valorDespues.ValorCobro) != 0))) {
	        CamposModificados aux = new CamposModificados();
	        aux.IdValorCobro = valorAntes.IdValorCobro;
	        aux.NombreCampo = "ValorCobro";
	        aux.ValorAntiguo = valorAntes.ValorCobro.toString();
	        aux.ValorNuevo = valorDespues.ValorCobro.toString();
	        listaSalida.add(aux);
	    }

		if ((valorAntes.ValorTopeMinimoOrden == null && valorDespues.ValorTopeMinimoOrden != null)
				|| (valorAntes.ValorTopeMinimoOrden != null && (valorDespues.ValorTopeMinimoOrden == null
						|| valorAntes.ValorTopeMinimoOrden.compareTo(valorDespues.ValorTopeMinimoOrden) != 0))) {
	        CamposModificados aux = new CamposModificados();
	        aux.IdValorCobro = valorAntes.IdValorCobro;
	        aux.NombreCampo = "ValorTopeMinimoOrden";
	        aux.ValorAntiguo = MonedaFormato.formatear(con, valorAntes.ValorTopeMinimoOrden, MonedaFormato.MONTO, valorAntes.IdMoneda, "", "").valorConFormato;
	        aux.ValorNuevo = MonedaFormato.formatear(con, valorDespues.ValorTopeMinimoOrden, MonedaFormato.MONTO, valorDespues.IdMoneda, "", "").valorConFormato;
	        listaSalida.add(aux);
	    }

		if ((valorAntes.IdMoneda == null && valorDespues.IdMoneda != null) || (valorAntes.IdMoneda != null
				&& (valorDespues.IdMoneda == null || !valorAntes.IdMoneda.equalsIgnoreCase(valorDespues.IdMoneda)))) {
	        CamposModificados aux = new CamposModificados();
	        aux.IdValorCobro = valorAntes.IdValorCobro;
	        aux.NombreCampo = "IdMoneda";
	        aux.ValorAntiguo = valorAntes.IdMoneda;
	        aux.ValorNuevo = valorDespues.IdMoneda;
	        listaSalida.add(aux);
	    }

		if ((valorAntes.IdEstado == null && valorDespues.IdEstado != null) || (valorAntes.IdEstado != null
				&& (valorDespues.IdEstado == null || !valorAntes.IdEstado.equalsIgnoreCase(valorDespues.IdEstado)))) {
	        CamposModificados aux = new CamposModificados();
	        aux.IdValorCobro = valorAntes.IdValorCobro;
	        aux.NombreCampo = "IdEstado";
	        aux.ValorAntiguo = valorAntes.IdEstado;
	        aux.ValorNuevo = valorDespues.IdEstado;
	        listaSalida.add(aux);
	    }

	    return listaSalida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarValorCobro in = gson.fromJson(jin.toString(), EntradaModificarValorCobro.class);
		SalidaModificarValorCobro out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdValorCobro\":\"TEST\",\"p_DescripcionValorCobro\":\"TEST\",\"p_IdTipoProducto\":\"EVCA\",\"p_IdCobro\":\"COFI\",\"p_ValorCobro\":0.03,\"p_ValorTopeMinimoOrden\":0,\"p_IdMoneda\":\"CLP\",\"p_IdEstado\":\"INA\"}";
			EntradaModificarValorCobro entrada = gson.fromJson(json, EntradaModificarValorCobro.class);
			ModificarValorCobro demo = new ModificarValorCobro();
			SalidaModificarValorCobro salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
