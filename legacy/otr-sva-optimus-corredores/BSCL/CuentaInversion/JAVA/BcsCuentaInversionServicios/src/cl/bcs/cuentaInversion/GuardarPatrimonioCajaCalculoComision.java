package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.PatrimonioCaja;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class GuardarPatrimonioCajaCalculoComision implements JavaCall{
	private final static Logger logger = LogManager.getLogger(GuardarPatrimonioCajaCalculoComision.class);
	
	private final String SQL_SELECT_CARGO_ABONO_POR_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCajaCalculoComision_SelectCargoAbonoCuentaInversion.sql");
	private final String SQL_SELECT_PATRIMONIO_AFECTO_DIA_ANTERIOR = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCajaCalculoComision_SelectPatrimonioAfectoDiaAnterior.sql");
	private final String SQL_CREAR_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCajaCalculoComision_CrearTablaPaso.sql");
	private final String SQL_DROP_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCajaCalculoComision_DropTablaPaso.sql");
	private final String SQL_DELETE_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCajaCalculoComision_DeleteTablaPaso.sql");
	private final String SQL_INSERT_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCajaCalculoComision_InsertTablaPaso.sql");
	private final String SQL_SELECT_MONTO_COMISION = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCajaCalculoComision_SelectMontoComision.sql");
	
	public static class EntradaGuardarPatrimonioCajaCalculoComision extends BcsPojo {
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario;
		public String p_UsaReglasNegocio;
		public List<CuentaInversionConsultar> p_CuentaInversionAConsultar;
		public List<PatrimonioCaja> p_PatrimoniosCaja;
		public String p_MonedaConsulta;
		public String  p_FechaConsulta;
		
		public EntradaGuardarPatrimonioCajaCalculoComision () {
			this.p_IdEmpresa = 1L;
			this.p_IdModulo = "CTA";
			this.p_IdUsuario = 0L;
			this.p_UsaReglasNegocio = "S";
			this.p_CuentaInversionAConsultar = new ArrayList<CuentaInversionConsultar>();
			this.p_PatrimoniosCaja = new ArrayList<PatrimonioCaja>();
			this.p_MonedaConsulta = "";
			this.p_FechaConsulta = "0001-01-01";
		}
	}

	public static class SalidaGuardarPatrimonioCajaCalculoComision extends BcsPojo {
		public List<PatrimonioCaja> P_TBL_PATRIMONIO;
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGuardarPatrimonioCajaCalculoComision() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_TBL_PATRIMONIO =  new ArrayList<PatrimonioCaja>();
		}
	}
	
	public static class CuentaInversionConsultar extends BcsPojo {
		public Long IdCuentaInversion;
		public Long IdPortafolio;
		public Long IdCliente;
		public String FechaPatrimonio;
		public String IdMoneda;
		public BigDecimal MontoPatrimonioAfectoComision;
		
		public CuentaInversionConsultar() {
			this.IdCuentaInversion = 0L;
			this.IdPortafolio = 0L;
			this.IdCliente = 0L;
			this.FechaPatrimonio = "0001-01-01";
			this.IdMoneda = "CLP";
			this.MontoPatrimonioAfectoComision = BigDecimal.ZERO;
		}
		public CuentaInversionConsultar (Long idCuentaInversion, Long idPortafolio, Long idCliente, String idMoneda, String fechaConsulta) {//
			this.IdCuentaInversion = idCuentaInversion;
			this.IdPortafolio = idPortafolio;
			this.IdCliente = idCliente;
			this.IdMoneda = idMoneda;
			this.FechaPatrimonio = fechaConsulta;
			this.MontoPatrimonioAfectoComision = BigDecimal.ZERO;
		}
	}
	
	public static class MovimientoCargoAbono extends BcsPojo {
		public Long IdCuentaInversion;
		public Date FechaSistema;
		public Date FechaEfectivaMovimiento;
		public Long IdCargoAbonoMovimiento;
		public String IdTipoMovimiento;
		public BigDecimal Monto;
		public Long IdPortafolio;
		
		public MovimientoCargoAbono() {
			this.IdCuentaInversion = 0L;
			this.FechaSistema = Fecha.toDate("0001-01-01");
			this.FechaEfectivaMovimiento = Fecha.toDate("0001-01-01");
			this.IdCargoAbonoMovimiento = 0L;
			this.IdTipoMovimiento = "";
			this.Monto = BigDecimal.ZERO;
			this.IdPortafolio = 0L;
		}
	}

	public static class MontoComision extends BcsPojo{
		public String PortafolioAPV;
		public String IdClasificacionTipoInstrumento;
		public Long OpcionCobro;
		public BigDecimal PorcentajeCobro;
		public Long IdPortafolio;
		public BigDecimal MontoPatrimonioAfectoComision;
		public BigDecimal MontoComision;
		public String FechaPatrimonio;
		
		public MontoComision() {
			this.PortafolioAPV = "N";
			this.IdClasificacionTipoInstrumento = "";
			this.OpcionCobro = 0L;
			this.PorcentajeCobro = BigDecimal.ZERO;
			this.MontoPatrimonioAfectoComision = BigDecimal.ZERO;
			this.MontoComision = BigDecimal.ZERO;
			this.FechaPatrimonio = "0001-01-01";
		}
	}

	public SalidaGuardarPatrimonioCajaCalculoComision logica(Connection con, EntradaGuardarPatrimonioCajaCalculoComision entrada) throws Exception {
		logger.info("INICIO :: GuardarPatrimonioCajaCalculoComision");
		SalidaGuardarPatrimonioCajaCalculoComision salida = new SalidaGuardarPatrimonioCajaCalculoComision();
		SQL sql=new SQL();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		if(entrada.p_CuentaInversionAConsultar.isEmpty()) {
			logger.info("INFO :: GuardarPatrimonioCajaCalculoComision: No hay Cuentas de Inversion en la Entrada");
			return salida;
		}
		
		try{
			logger.info("INFO :: GuardarPatrimonioCajaCalculoComision => Se Crea tabla paso");
			SQL.exec(con, SQL_CREAR_TABLA_PASO);
			
			logger.info("INFO :: GuardarPatrimonioCajaCalculoComision => Se rellena tabla paso");
			sql.batch(con, SQL_INSERT_TABLA_PASO, entrada.p_CuentaInversionAConsultar, CuentaInversionConsultar.class);
			
			HashMap<String, Object> params = new HashMap<String, Object>(3);
			params.put("p_FechaConsulta", entrada.p_FechaConsulta);
			
			logger.info("INFO :: GuardarPatrimonioCajaCalculoComision: Fecha Consulta: " + entrada.p_FechaConsulta +" -- Cantidad de Cuentas Inversion: " + entrada.p_CuentaInversionAConsultar.size());
			logger.info("INFO :: GuardarPatrimonioCajaCalculoComision: Se obtienen movimientos filtrados por IdConcepto no excluidos y se agrupan");
			Map<Long, List<MovimientoCargoAbono>> hashMovimientosAgrupados = sql.query(con, SQL_SELECT_CARGO_ABONO_POR_CUENTA_INVERSION, params, MovimientoCargoAbono.class).stream().collect(Collectors.groupingBy( mov -> mov.IdCuentaInversion));
			
			String fechaConsultaDiaHabilAnterior = obtenerDiaHabilAnterior(con, entrada.p_IdEmpresa, entrada.p_FechaConsulta); 
			
			params.clear();
			params.put("p_FechaConsulta", fechaConsultaDiaHabilAnterior);
			logger.info("INFO :: GuardarPatrimonioCajaCalculoComision => Se buscan Monto Patrimonio Afecto Dia Anterior");
			Map<Long, PatrimonioCaja> patrimoniosDiaAnterior = sql.query(con, SQL_SELECT_PATRIMONIO_AFECTO_DIA_ANTERIOR, params, PatrimonioCaja.class).stream().collect(Collectors.toMap( patr -> patr.IdPortafolio, patr -> patr));
			logger.info("INFO :: GuardarPatrimonioCajaCalculoComision => Se obtuvieron los registros de Monto Patrimonio Afecto Dia Anterior");
			
			List<PatrimonioCaja> patrimoniosAfectoComision = new ArrayList<PatrimonioCaja>();
			
			CalcularParidad calculoParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 0L;
			
			for(CuentaInversionConsultar cuenta : entrada.p_CuentaInversionAConsultar) {
				MovimientoCargoAbono movto = hashMovimientosAgrupados.get(cuenta.IdCuentaInversion) != null ? hashMovimientosAgrupados.get(cuenta.IdCuentaInversion).get(0) : new MovimientoCargoAbono();
				BigDecimal saldoDiarioCargoAbono = movto.Monto;
				
				logger.trace("INFO :: GuardarPatrimonioCajaCalculoComision => SaldoDiarioCargoAbono: " +saldoDiarioCargoAbono);
				PatrimonioCaja patrimonioDiaAnterior =  patrimoniosDiaAnterior.getOrDefault(cuenta.IdPortafolio, new PatrimonioCaja());
				PatrimonioCaja patrimonioAfectoComision = new PatrimonioCaja();
				patrimonioAfectoComision.IdPortafolio = cuenta.IdPortafolio;
				
				patrimonioDiaAnterior.MontoPatrimonioAfectoComision = patrimonioDiaAnterior != null ? patrimonioDiaAnterior.MontoPatrimonioAfectoComision.add(saldoDiarioCargoAbono, MathContext.DECIMAL128) : BigDecimal.ZERO;
			}
			
			
			patrimoniosDiaAnterior.forEach((llave, patr) -> {
				patr.FechaPatrimonio = entrada.p_FechaConsulta;
				patrimoniosAfectoComision.add(patr);
			});
			
			if(!patrimoniosAfectoComision.isEmpty()) {
				SQL.exec(con, SQL_DELETE_TABLA_PASO);
				logger.info("INFO :: GuardarPatrimonioCajaCalculoComision => Se rellena tabla paso");
				sql.batch(con, SQL_INSERT_TABLA_PASO, patrimoniosAfectoComision, PatrimonioCaja.class);
				
				logger.info("INFO :: GuardarPatrimonioCajaCalculoComision => Se obtienen Montos de Comision");
				Map<String, MontoComision> hashMontoComision = sql.query(con, SQL_SELECT_MONTO_COMISION, new HashMap<String, Object>(0), MontoComision.class).stream().collect(Collectors.toMap(mc-> mc.IdPortafolio.toString() + "_" + mc.FechaPatrimonio, mc -> mc));
				
				for(PatrimonioCaja patrimonio : patrimoniosAfectoComision) {
					logger.info("INFO :: GuardarPatrimonioCajaCalculoComision => Se formatean Montos para Portafolio: "+patrimonio.IdPortafolio);
					String llave = patrimonio.IdPortafolio.toString() + "_" + patrimonio.FechaPatrimonio;
					
					BigDecimal montoAfectoComision = hashMontoComision.get(llave) != null ? hashMontoComision.get(llave).MontoPatrimonioAfectoComision : BigDecimal.ZERO;
					BigDecimal porcentajeCobro =  hashMontoComision.get(llave) != null ? hashMontoComision.get(llave).PorcentajeCobro : BigDecimal.ZERO;
					BigDecimal montoComision = hashMontoComision.get(llave) != null ? hashMontoComision.get(llave).MontoComision : BigDecimal.ZERO;
					patrimonio.MontoPatrimonioAfectoComision = MonedaFormato.formatear(con,montoAfectoComision, MonedaFormato.MONTO, idMonedaSistema).valor;
					patrimonio.PorcentajeCobroComision = porcentajeCobro;
					patrimonio.MontoComision =  MonedaFormato.formatear(con,montoComision.compareTo(BigDecimal.ZERO) > 0 ? montoComision : BigDecimal.ZERO, MonedaFormato.MONTO, idMonedaSistema).valor;
					patrimonio.IdEstadoComision = patrimonio.MontoComision.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "VIG";
					logger.info("INFO :: GuardarPatrimonioCajaCalculoComision => MontoPatrimonioAfectoComision: " + patrimonio.MontoPatrimonioAfectoComision +"  -- MontoComision: " + patrimonio.MontoComision );
				}
			}
			
			salida.P_TBL_PATRIMONIO.addAll(patrimoniosAfectoComision);
			
		}catch (Exception e){
			logger.error("ERROR",e);
			throw e;
		}finally {
			SQL.exec(con, SQL_DROP_TABLA_PASO);
		}
				
		logger.info("FIN :: GuardarPatrimonioCajaCalculoComision");
		return salida;
	}


	public String obtenerDiaHabilAnterior(Connection con, Long p_IdEmpresa,String p_FechaConsulta) throws Exception {
		logger.info("INICIO :: GuardarPatrimonioCajaCalculoComision.obtenerDiaHabilAnterior");
		
		String diaHabilAnterior = "0001-01-01";
		ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores entradaObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores();
        entradaObtenerDiasHabilesAnteriores.idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		entradaObtenerDiasHabilesAnteriores.diaHabil = "S";
		entradaObtenerDiasHabilesAnteriores.soloCaeDiaHabil = "S";
		FechasIn fecha = new FechasIn(p_FechaConsulta, 1L);
		entradaObtenerDiasHabilesAnteriores.fechas.add(fecha);
		ObtenerDiasHabilesAnteriores obtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores();
		OutObtenerDiasHabilesAnteriores salidaObtenerDiaHabilAnterior = obtenerDiasHabilesAnteriores.logica(con, entradaObtenerDiasHabilesAnteriores);
		
		diaHabilAnterior = salidaObtenerDiaHabilAnterior.fechas.stream().filter(u -> u.numeroDiasInicial == 1L && p_FechaConsulta.equals(u.fechaInicial)).map(u -> u.fechaCalculada).findFirst().orElse(p_FechaConsulta);
		
		logger.info("FIN :: GuardarPatrimonioCajaCalculoComision.obtenerDiaHabilAnterior");
		return diaHabilAnterior;
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01")){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"CUS\",\"p_IdUsuario\":35,\"p_UsaReglasNegocio\":\"S\",\"p_CuentaInversionAConsultar\":[{\"IdCuentaInversion\":8397,\"IdPortafolio\":190021,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":98925,\"IdPortafolio\":190018,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":265968,\"IdPortafolio\":190019,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":518068,\"IdPortafolio\":190011,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":518071,\"IdPortafolio\":190014,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":518074,\"IdPortafolio\":190012,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":518077,\"IdPortafolio\":190009,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":550377,\"IdPortafolio\":190016,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":584874,\"IdPortafolio\":190022,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":588739,\"IdPortafolio\":190013,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":588742,\"IdPortafolio\":190010,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":612070,\"IdPortafolio\":190008,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":947542,\"IdPortafolio\":190020,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":1063762,\"IdPortafolio\":190015,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":1111036,\"IdPortafolio\":190017,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"CLP\"},{\"IdCuentaInversion\":8398,\"IdPortafolio\":190021,\"IdCliente\":63547,\"FechaConsulta\":null,\"IdMoneda\":\"USD\"}],\"p_PatrimoniosCaja\":[],\"p_MonedaConsulta\":\"\",\"p_FechaConsulta\":\"2024-05-16\"}";
			EntradaGuardarPatrimonioCajaCalculoComision Objin = gson.fromJson(json, EntradaGuardarPatrimonioCajaCalculoComision.class);
			
			GuardarPatrimonioCajaCalculoComision demo = new GuardarPatrimonioCajaCalculoComision();
			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaGuardarPatrimonioCajaCalculoComision ObjOut = demo.logica(con,Objin);
 				System.out.println(ObjOut.toString());
 				con.rollback();
 			}catch (Exception er) {
 				er.printStackTrace();	
				con.rollback();
 			}
 			
		}catch (SQLException e) {
	    	e.printStackTrace();
	    }
		System.out.println("== END ==");
	}
}
