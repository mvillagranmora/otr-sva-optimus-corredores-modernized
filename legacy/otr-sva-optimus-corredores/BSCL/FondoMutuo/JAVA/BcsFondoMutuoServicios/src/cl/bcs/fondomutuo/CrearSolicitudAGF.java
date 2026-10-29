package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSolicitudAGF implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CrearSolicitudAGF.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAGF_SelectSolicitud.sql");
	private final String SQL_SELECT_BENEFICIARIO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAGF_SelectBeneficiario.sql");
	private final String SQL_INSERT_BENEFICIARIO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAGF_InsertBeneficiario.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAGF_InsertSolicitud.sql");
	
	public static class EntradaCrearSolicitudAGF{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_EsManual;
		public List<Solicitudes> p_Solicitudes;
		
		public EntradaCrearSolicitudAGF() {
			this.p_Solicitudes= new ArrayList<Solicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Solicitudes{
		public Long IdSolicitudAporteRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class SalidaCrearSolicitudAGF{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <Resultado> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudAGF() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO= new ArrayList<Resultado>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado{
		public Long IdSolicitudAporteRescate;
		public Long IdSolicitudAporteRescateRelacionado;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Date FechaLiquidacion;
		public Date FechaAceptacion;
		public String TipoSolicitud;
		public String IdEstadoSolicitud;
		public String IdSerieFondo;
		public Long IdUsuario;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public BigDecimal Monto;
		public String CalculoPorMontoCuota;
		public String IdMoneda;
		public String PropositoSerie;
		public String IdSerieFondoDestino;
		public String IdEstadoSerieFondo;
		public String IndicadorMLT;
		public String IdEstado;
		public Long IdEmpresa;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectBeneficiario{
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreCompleto;
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InsertBeneficiario{
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreCompleto;
		public String FechaIngreso;
		public String IdEstado;
	   	public String TipoRegistro;
	   	public Long IdCliente;
	   	public Long IdPortafolio;
	   	public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectSolicitud{
		public Long IdSolicitudAporteRescate;
		public Long IdSolicitudAporteRescateRelacionado;
		public String DocumentoIdentidad;
	    public String CodigoPortafolio;
	    public Date FechaLiquidacion;
	    public Date FechaAceptacion;
	    public String TipoSolicitud;
	    public String IdEstadoSolicitud;
	    public String IdSerieFondo;
	    public Long IdUsuario;
	    public BigDecimal NumeroCuotas;
	    public BigDecimal ValorCuota;
	    public BigDecimal Monto;
	    public String CalculoPorMontoCuota;
	    public String IdMoneda;
	    public String PropositoSerie;
	    public String IdSerieFondoDestino;
	    public String IdEstadoSerieFondo;
		public String IndicadorMLT;
		public String NombreCompleto;
		public String IdTipoMovimientoFondoMutuo;
		public String TipoContratoOperacion;
		public String FolioContratoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InsertSolicitud{
		public Long IdSolicitudAporteRescate;
		public Long IdSolicitudAporteRescateRelacionado;
		public String DocumentoIdentidad;
	    public String CodigoPortafolio;
	    public Date FechaLiquidacion;
	    public Date FechaAceptacion;
	    public String TipoSolicitud;
	    public String IdEstadoSolicitud;
	    public String IdSerieFondo;
	    public Long IdUsuario;
	    public BigDecimal NumeroCuotas;
	    public BigDecimal ValorCuota;
	    public BigDecimal Monto;
	    public String CalculoPorMontoCuota;
	    public String IdMoneda;
	    public String PropositoSerie;
	    public String IdSerieFondoDestino;
	    public String IdEstadoSerieFondo;
		public String IndicadorMLT;
		public String NombreCompleto;
		public String FechaIngreso;
		public String IdEstado;
		public String TipoRegistro;
		public Long IdEmpresa;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearSolicitudAGF jin = gson.fromJson(in.toString(), EntradaCrearSolicitudAGF.class);
		SalidaCrearSolicitudAGF out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearSolicitudAGF logica(Connection con, EntradaCrearSolicitudAGF entrada) throws Exception{
		logger.info("INICIO::CrearSolicitudAGF");
		SQL sql=new SQL();
		
		SalidaCrearSolicitudAGF out = new SalidaCrearSolicitudAGF();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String usaReplicaSigaFondoMutuo = _c.NO;
		try {
			usaReplicaSigaFondoMutuo = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "UsaReplicaSigaFondoMutuo");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::CrearSolicitudAGF::No se encuentra parametro UsaReplicaSigaFondoMutuo");
		}
		
		if(_c.SI.equals(usaReplicaSigaFondoMutuo)) {
			if(entrada.p_Solicitudes.size() == 0){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			
			List<Long> solTemp = new ArrayList<Long>();
			for(Solicitudes s : entrada.p_Solicitudes){
				if(solTemp.contains(s.IdSolicitudAporteRescate)){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUDES_REPETIDAS",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				}else{
					solTemp.add(s.IdSolicitudAporteRescate);
				}
			}
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdSolicitudAporteRescate", solTemp);
			logger.debug("INICIO::SQL_SELECT_BENEFICIARIO");
			List<SelectBeneficiario> solBeneficiario = sql.query(con, SQL_SELECT_BENEFICIARIO, params, SelectBeneficiario.class);
			logger.debug("FIN::SQL_SELECT_BENEFICIARIO");
			logger.debug("INICIO::SQL_SELECT_SOLICITUD");
			List<SelectSolicitud> solicitudes = sql.query(con, SQL_SELECT_SOLICITUD, params, SelectSolicitud.class);
			logger.debug("FIN::SQL_SELECT_SOLICITUD");
			List<InsertBeneficiario> inBenef = new ArrayList<InsertBeneficiario>();
			for(SelectBeneficiario benef : solBeneficiario){
				InsertBeneficiario in = new InsertBeneficiario();
				//in = Utils.copyPojo(benef, in);
				in = getPojoBeneficiario(benef, in);
				in.FechaIngreso = fechaSistema;
				in.IdEstado = _c.EstadoPendiente;
			   	in.TipoRegistro = _c.TipoRegistroParticipe;
			   	in.IdEmpresa = entrada.p_IdEmpresa;
			   	inBenef.add(in);
			}
			
			HashMap<Long,Object> hSol = Utils.List2Hash(solicitudes, "IdSolicitudAporteRescate");
			
			List<InsertSolicitud> inSol = new ArrayList<InsertSolicitud>();
			for(Solicitudes sol : entrada.p_Solicitudes){
				InsertSolicitud in = new InsertSolicitud();
				SelectSolicitud ss = (SelectSolicitud) hSol.get(sol.IdSolicitudAporteRescate);
				if(ss == null){
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdSolicitudAporteRescate,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRO_SOLICITUD",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				if((ss.NumeroCuotas == null || BigDecimal.ZERO.compareTo(ss.NumeroCuotas) == 0) || (ss.ValorCuota == null || BigDecimal.ZERO.compareTo(ss.ValorCuota) == 0))
				{
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("IdSerieFondo", 		ss.IdSerieFondo);
					valores.put("DocumentoIdentidad", 	ss.DocumentoIdentidad);
					valores.put("TipoContrato", 		ss.TipoContratoOperacion);
					valores.put("FolioOperacion", 		ss.FolioContratoOperacion);
					
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdSolicitudAporteRescate,MensajeSalida.MENSAJE_ERROR,"VALOR_CUOTA_VACIO",valores,con,entrada.p_IdEmpresa));
					continue;
				}
				//in = Utils.copyPojo(ss, in);
				in = getPojoSolicitud(ss,in);
				in.FechaIngreso = fechaSistema;
				in.IdEstado = _c.EstadoPendiente;
				in.TipoRegistro = _c.Transferencia.equalsIgnoreCase(ss.IdTipoMovimientoFondoMutuo) || _c.TraspasoPortafolio.equalsIgnoreCase(ss.IdTipoMovimientoFondoMutuo) ? _c.TipoRegistroTraspaso : _c.TipoRegistroAporteRescate;
				in.IdEmpresa = entrada.p_IdEmpresa;
				inSol.add(in);
			}
			
			if(inBenef.size() > 0){
				sql.batch(con, SQL_INSERT_BENEFICIARIO, inBenef, InsertBeneficiario.class);
			}
			
			if(inSol.size() > 0){
				sql.batch(con, SQL_INSERT_SOLICITUD, inSol, InsertSolicitud.class);
			}
			
			for(InsertSolicitud sol : inSol){
				Resultado r = new Resultado();
				//r = Utils.copyPojo(sol, r);
				r = getPojoResultado(sol, r);
				out.P_OUT_RESULTADO.add(r);
			}
		}
		
		logger.info("FIN::CrearSolicitudAGF");
		return out;
	}
	
	public InsertBeneficiario getPojoBeneficiario(SelectBeneficiario sb, InsertBeneficiario ib){
		ib.DocumentoIdentidad = sb.DocumentoIdentidad;
		ib.CodigoPortafolio = sb.CodigoPortafolio;
		ib.NombreCompleto = sb.NombreCompleto;
		//ib.FechaIngreso = sb.FechaIngreso;
		//ib.IdEstado = sb.IdEstado;
		//ib.TipoRegistro = sb.TipoRegistro;
		ib.IdCliente = sb.IdCliente;
		ib.IdPortafolio = sb.IdPortafolio;
		//ib.IdEmpresa = sb.IdEmpresa;
		return ib;
	}
	
	public InsertSolicitud getPojoSolicitud(SelectSolicitud ss, InsertSolicitud is){
		is.IdSolicitudAporteRescate = ss.IdSolicitudAporteRescate;
		is.IdSolicitudAporteRescateRelacionado = ss.IdSolicitudAporteRescateRelacionado;
		is.DocumentoIdentidad = ss.DocumentoIdentidad;
		is.CodigoPortafolio = ss.CodigoPortafolio;
		is.FechaLiquidacion = ss.FechaLiquidacion;
		is.FechaAceptacion = ss.FechaAceptacion;
		is.TipoSolicitud = ss.TipoSolicitud;
		is.IdEstadoSolicitud = ss.IdEstadoSolicitud;
		is.IdSerieFondo = ss.IdSerieFondo;
		is.IdUsuario = ss.IdUsuario;
		is.NumeroCuotas = ss.NumeroCuotas;
		is.ValorCuota = ss.ValorCuota;
		is.Monto = ss.Monto;
		is.CalculoPorMontoCuota = ss.CalculoPorMontoCuota;
		is.IdMoneda = ss.IdMoneda;
		is.PropositoSerie = ss.PropositoSerie;
		is.IdSerieFondoDestino = ss.IdSerieFondoDestino;
		is.IdEstadoSerieFondo = ss.IdEstadoSerieFondo;
		is.IndicadorMLT = ss.IndicadorMLT;
		is.NombreCompleto = ss.NombreCompleto;
		//is.FechaIngreso = ss.FechaIngreso;
		//is.IdEstado = ss.IdEstado;
		//is.TipoRegistro = ss.TipoRegistro;
		//is.IdEmpresa = ss.IdEmpresa;
		return is;
	}
	
	public Resultado getPojoResultado(InsertSolicitud is, Resultado r){
		r.IdSolicitudAporteRescate = is.IdSolicitudAporteRescate;
		r.IdSolicitudAporteRescateRelacionado = is.IdSolicitudAporteRescateRelacionado;
		r.DocumentoIdentidad = is.DocumentoIdentidad;
		r.CodigoPortafolio = is.CodigoPortafolio;
		r.FechaLiquidacion = is.FechaLiquidacion;
		r.FechaAceptacion = is.FechaAceptacion;
		r.TipoSolicitud = is.TipoSolicitud;
		r.IdEstadoSolicitud = is.IdEstadoSolicitud;
		r.IdSerieFondo = is.IdSerieFondo;
		r.IdUsuario = is.IdUsuario;
		r.NumeroCuotas = is.NumeroCuotas;
		r.ValorCuota = is.ValorCuota;
		r.Monto = is.Monto;
		r.CalculoPorMontoCuota = is.CalculoPorMontoCuota;
		r.IdMoneda = is.IdMoneda;
		r.PropositoSerie = is.PropositoSerie;
		r.IdSerieFondoDestino = is.IdSerieFondoDestino;
		r.IdEstadoSerieFondo = is.IdEstadoSerieFondo;
		r.IndicadorMLT = is.IndicadorMLT;
		r.IdEstado = is.IdEstado;
		r.IdEmpresa = is.IdEmpresa;
		return r;
	}
	
}
