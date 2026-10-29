package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarCobrosPortafolio implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarCobrosPortafolio.class);
	
	private final String SQL_CONSULTAR_COBROS_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarCobrosPortafolio_ConsultarCobrosBase.sql");
	private final String SQL_CONSULTAR_COBROS_EXTRA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarCobrosPortafolio_ConsultarCobrosExtra.sql");
	
	ConstantesPlataformaComercial _com = new ConstantesPlataformaComercial();
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarCobros_Entrada in = gson.fromJson(jin.toString(), ConsultarCobros_Entrada.class);
		ConsultarCobros_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public ConsultarCobros_Salida logica(Connection conn,ConsultarCobros_Entrada entrada) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ConsultarCobrosPortafolio");
		
		ConsultarCobros_Salida salida = new ConsultarCobros_Salida();

		HashMap<Object, Object> hashOperaciones = Utils.List2Hash(entrada.p_Operaciones, "IdInterno");
		HashMap<Object,InformacionOperacion> hashLogOperaciones = new HashMap<Object,InformacionOperacion>();
		String logCobro = "N";
		
		SQL _sql = new SQL();
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		Set<Long> idsPortafolios = new HashSet<Long>();
		for (InformacionOperacion ope : entrada.p_Operaciones) {
			idsPortafolios.add(ope.IdPortafolio);
		}
		params1.put("idsPortafolios", new ArrayList<Long>(idsPortafolios));
		params1.put("IdEmpresa", entrada.p_IdEmpresa);
		List<LineaCobroProceso> base = _sql.query(conn, SQL_CONSULTAR_COBROS_BASE, params1, LineaCobroProceso.class);
		List<LineaCobroProceso> extra = _sql.query(conn, SQL_CONSULTAR_COBROS_EXTRA, params1, LineaCobroProceso.class);

		try {
			logCobro = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "LogCobros");
		}catch(Exception e) {
			logCobro = "N";
		}
		
		//CONSOLIDAMOS
		List<LineaCobroProceso> consolidado = new ArrayList<>();
		{
			for (LineaCobroProceso cobextra : extra) {
				if (cobextra.IdEstadoDetallePerfilCobros.equalsIgnoreCase("ACT"))
					consolidado.add(cobextra);
			}
			
			for (LineaCobroProceso cobbase : base) {
				boolean found = false;
				for (LineaCobroProceso cobactual : consolidado) {
					if (cobactual.IdPortafolio.equals(cobbase.IdPortafolio) &&
							(cobactual.IdBolsa.equalsIgnoreCase(cobbase.IdBolsa) || cobbase.IdBolsa.equalsIgnoreCase("0")) &&
							(cobactual.IdTipoProducto.equalsIgnoreCase(cobbase.IdTipoProducto) || cobbase.IdTipoProducto.equalsIgnoreCase("0")) &&
							(cobactual.IdTipoOperacion.equalsIgnoreCase(cobbase.IdTipoOperacion) || cobbase.IdTipoOperacion.equalsIgnoreCase("0")) &&
							(cobactual.IdTipoCanalIngreso.equalsIgnoreCase(cobbase.IdTipoCanalIngreso) || cobbase.IdTipoCanalIngreso.equalsIgnoreCase("0")) && 
							(cobactual.IdCobro.equalsIgnoreCase(cobbase.IdCobro))) {
						
						found = true;
						break;
					}
				}
				if (!found && cobbase.IdEstadoDetallePerfilCobros.equalsIgnoreCase("ACT")) {
					consolidado.add(cobbase);
				}
			}
		}
		
		//FORMAMOS GRUPOS
		HashMap<GrupoCobro,ArrayList<LineaCobroProceso>> cobrosPorGrupo = new HashMap<>();
		for (LineaCobroProceso linea : consolidado) {
			GrupoCobro c = new GrupoCobro();
			c.IdBolsa = linea.IdBolsa;
			c.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
			c.IdTipoOperacion = linea.IdTipoOperacion;
			c.IdTipoProducto = linea.IdTipoProducto;
			c.IdPortafolio = linea.IdPortafolio;
			c.EsPerfilPersonalizado = linea.EsPerfilPersonalizado;
			if (!cobrosPorGrupo.containsKey(c)) {
				cobrosPorGrupo.put(c, new ArrayList<>());
			}
			
			ArrayList<LineaCobroProceso> lineas = cobrosPorGrupo.get(c);
			lineas.add(linea);
		}
		
		//FORMAMOS HASH OPERACION-GRUPOS
		HashMap<Long,ArrayList<GrupoCobro>> gruposPorOperacion = new HashMap<>();
		for (InformacionOperacion info : entrada.p_Operaciones) {
			for (GrupoCobro grupo : cobrosPorGrupo.keySet()) {
				if("S".equalsIgnoreCase(logCobro) && "S".equalsIgnoreCase(grupo.EsPerfilPersonalizado)&& grupo.IdTipoProducto.equalsIgnoreCase(info.IdTipoProducto) || grupo.IdTipoProducto.equalsIgnoreCase("0")) {
					if (!hashLogOperaciones.containsKey(info.IdInterno)) {
						hashLogOperaciones.put(info.IdInterno,info);
					}
				}
				
				if (
						(grupo.IdBolsa.equalsIgnoreCase(info.IdBolsa) || grupo.IdBolsa.equalsIgnoreCase("0")) &&
						(grupo.IdPortafolio.equals(info.IdPortafolio)) &&
						(grupo.IdTipoCanalIngreso.equalsIgnoreCase(info.IdTipoCanalIngreso) || grupo.IdTipoCanalIngreso.equalsIgnoreCase("0")) &&
						(grupo.IdTipoOperacion.equalsIgnoreCase(info.IdTipoOperacion) || grupo.IdTipoOperacion.equalsIgnoreCase("0")) &&
						(grupo.IdTipoProducto.equalsIgnoreCase(info.IdTipoProducto) || grupo.IdTipoProducto.equalsIgnoreCase("0"))
						) {
					
					ArrayList<GrupoCobro> grupos = gruposPorOperacion.get(info.IdInterno);
					if (grupos == null) {
						grupos = new ArrayList<>();
						gruposPorOperacion.put(info.IdInterno, grupos);
					}
					grupos.add(grupo);	
				}
			}
		}
		
		HashMap<Long,GrupoCobro> mejoresGruposPorOperacion = new HashMap<>();
		for (Long operacion : gruposPorOperacion.keySet()) {
			ArrayList<GrupoCobro> grupos = gruposPorOperacion.get(operacion);
			InformacionOperacion info = (InformacionOperacion) hashOperaciones.get(operacion);
			if (grupos == null) continue;
			
			long mejorPuntaje = 0;
			GrupoCobro mejorGrupo = null;
			
			for (GrupoCobro grupo : grupos) {
				//calculamos puntaje
				long puntaje = 0;
				if (info.IdBolsa.equalsIgnoreCase(grupo.IdBolsa)) {
					puntaje = 128;
				}
				else if (grupo.IdBolsa.equalsIgnoreCase("0")) {
					puntaje = 64;
				}
				else {
					puntaje = 0;
				}
				
				if (info.IdTipoProducto.equalsIgnoreCase(grupo.IdTipoProducto)) {
					puntaje = puntaje * 32;
				}
				else if (grupo.IdTipoProducto.equalsIgnoreCase("0")) {
					puntaje = puntaje * 16;
				}
				else {
					puntaje = puntaje * 0;
				}
				
				if (info.IdTipoOperacion.equalsIgnoreCase(grupo.IdTipoOperacion)) {
					puntaje = puntaje * 8;
				}
				else if (grupo.IdTipoOperacion.equalsIgnoreCase("0")) {
					puntaje = puntaje * 4;
				}
				else {
					puntaje = puntaje * 0;
				}
				
				if (info.IdTipoCanalIngreso.equalsIgnoreCase(grupo.IdTipoCanalIngreso)) {
					puntaje = puntaje * 2;
				}
				else if (grupo.IdTipoCanalIngreso.equalsIgnoreCase("0")) {
					puntaje = puntaje * 1;
				}
				else {
					puntaje = puntaje * 0;
				}
				
				if (puntaje > mejorPuntaje) {
					mejorPuntaje = puntaje;
					mejorGrupo = grupo;
				}
			}
			if (mejorGrupo != null)
				mejoresGruposPorOperacion.put(operacion, mejorGrupo);
		}
		
		for (Long operacion : mejoresGruposPorOperacion.keySet()) {
			GrupoCobro grupo = mejoresGruposPorOperacion.get(operacion);
			ArrayList<LineaCobroProceso> cobros = cobrosPorGrupo.get(grupo);
			for (LineaCobroProceso linea : cobros) {
				//Utils.copyPojo(linea, cob);
				LineaCobroProceso cob = copyPojoLineaCobroProceso(linea);
				cob.IdInterno = operacion;
				salida.p_out_MatrizOperacional.add(cob);
			}
		}
        
		
		if ("S".equalsIgnoreCase(logCobro) && hashLogOperaciones.size() >= 1) {
			AgregarLog(conn,entrada,hashLogOperaciones,salida.p_out_MatrizOperacional);
		}
		
		logger.info("Termino del servicio ConsultarCobrosPortafolio, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}

	public void AgregarLog(Connection con,ConsultarCobros_Entrada in,HashMap<Object,InformacionOperacion> Operaciones, List<LineaCobroProceso> Cobros) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long IdCabeceraLog =  in.p_IdUsuario;
			
			//CABECERA
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			
			entradaAgregarLog.p_IdEmpresa = in.p_IdEmpresa;
			entradaAgregarLog.p_IdModulo = in.p_IdModulo;
			entradaAgregarLog.p_IdUsuario = in.p_IdUsuario;

			for (Object ope : Operaciones.keySet()) {

				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				InformacionOperacion info = (InformacionOperacion) Operaciones.get(ope);
				
				log.IdCabeceraLog = info.IdInterno +1L;
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = 0L;
				log.IdCliente = 0L;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = info.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "COBRO";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = "";
				log.Folio = 0L;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa =  BigDecimal.ZERO;
				log.Monto = BigDecimal.ZERO;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = "";
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.IdPersona = 0L;
				log.IdTipoLog = "INGRESO";
				log.NombreProceso = "Consultar Cobros";

				entradaAgregarLog.p_Log.add(log);
				for (LineaCobroProceso cob : Cobros) {
					if(cob.IdInterno.equals(info.IdInterno)) {
						InAgregarLog.DetalleLog detalleValorCobro= new InAgregarLog.DetalleLog();

						detalleValorCobro.IdCabeceraLog = info.IdInterno +1L;
						detalleValorCobro.SubRegistro = info.IdInterno.toString();
						detalleValorCobro.ValorAntiguo = "";
						detalleValorCobro.ValorNuevo= cob.IdTipoProducto.toString().concat("//"+cob.IdPerfilCobros.toString()).concat("//"+cob.ValorCobro.toString());
						detalleValorCobro.CampoRegistro = cob.IdCobro.toString();
						entradaAgregarLog.p_LogDetalle.add(detalleValorCobro);
					}
				}
				
			}
			
			//REGISTRO
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class GrupoCobro {
		public Long	IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public String EsPerfilPersonalizado;
		
		@Override
		public boolean equals(Object o) {
			GrupoCobro e = (GrupoCobro)o;
			return (this.IdTipoProducto.equalsIgnoreCase(e.IdTipoProducto)) && 
					(this.IdTipoOperacion.equalsIgnoreCase(e.IdTipoOperacion)) &&
					(this.IdBolsa.equalsIgnoreCase(e.IdBolsa)) &&
					(this.IdTipoCanalIngreso.equalsIgnoreCase(e.IdTipoCanalIngreso) &&
					(this.IdPortafolio.equals(e.IdPortafolio)));
		}
		
		@Override
		public int hashCode() {
			return (this.IdTipoProducto + this.IdTipoOperacion + this.IdTipoCanalIngreso + this.IdBolsa + this.IdPortafolio).hashCode();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class LineaCobroProceso {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdPerfilCobros;
		public String EsPerfilPersonalizado;
		public String IdEstadoPerfilCobros;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public String IdValorCobro;
		public String DescripcionValorCobro;
		public String IdCobro;
		public BigDecimal ValorCobro;
		public String FormaCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;
		public String NombreCobro;
		public Long IdCobroClasificacion;
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor;
		public Long IdEmpresa;
		public String IdEstadoDetallePerfilCobros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarCobros_Salida {
		public List<LineaCobroProceso> p_out_MatrizOperacional;
		public List<MensajeSalida> p_out_Mensajes;
		
		public ConsultarCobros_Salida() {
			p_out_MatrizOperacional = new ArrayList<>();
			p_out_Mensajes = new ArrayList<>();
		}
	}
	
	public static class ConsultarCobros_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<InformacionOperacion> p_Operaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarCobros_Entrada() {
			this.p_Operaciones = new ArrayList<InformacionOperacion>();
		}
	}
	
	public static class InformacionOperacion {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private LineaCobroProceso copyPojoLineaCobroProceso(LineaCobroProceso linea) {
		LineaCobroProceso cob = new LineaCobroProceso();
		cob.IdInterno = linea.IdInterno;
		cob.IdPortafolio = linea.IdPortafolio;
		cob.IdPerfilCobros = linea.IdPerfilCobros;
		cob.EsPerfilPersonalizado = linea.EsPerfilPersonalizado;
		cob.IdEstadoPerfilCobros = linea.IdEstadoPerfilCobros;
		cob.IdTipoProducto = linea.IdTipoProducto;
		cob.IdTipoOperacion = linea.IdTipoOperacion;
		cob.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
		cob.IdBolsa = linea.IdBolsa;
		cob.IdValorCobro = linea.IdValorCobro;
		cob.DescripcionValorCobro = linea.DescripcionValorCobro;
		cob.IdCobro = linea.IdCobro;
		cob.ValorCobro = linea.ValorCobro;
		cob.FormaCobro = linea.FormaCobro;
		cob.ValorTopeMinimoOrden = linea.ValorTopeMinimoOrden;
		cob.IdMoneda = linea.IdMoneda;
		cob.NombreCobro = linea.NombreCobro;
		cob.IdCobroClasificacion = linea.IdCobroClasificacion;
		cob.CodigoCobroClasificacion = linea.CodigoCobroClasificacion;
		cob.PermiteCambiarValor = linea.PermiteCambiarValor;
		cob.IdEmpresa = linea.IdEmpresa;
		cob.IdEstadoDetallePerfilCobros = linea.IdEstadoDetallePerfilCobros;
		return cob;
	}
	

	public static void main(String[] args) throws Exception {
		Connection con = PlataformaUtils.getDESAConnection();
		ConsultarCobrosPortafolio cc = new ConsultarCobrosPortafolio();
		ConsultarCobros_Entrada entrada = new ConsultarCobros_Entrada();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdModulo = "ORD";
		entrada.p_IdUsuario = 21L;
		InformacionOperacion e = new InformacionOperacion();
		e.IdBolsa = "BCS";
		e.IdInterno = 0L;
		e.IdPortafolio = 512341L;
		e.IdTipoCanalIngreso = "AGE";
		e.IdTipoOperacion = "VENSIM";
		e.IdTipoProducto = "INSI";
		entrada.p_Operaciones.add(e);
		
		ConsultarCobros_Salida a = cc.logica(con, entrada);
		System.out.println(a.p_out_MatrizOperacional.toString());
	}
}