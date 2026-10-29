package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.SaldoCuentaInversion.EntradaSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoInicialFinal;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaMovimiento;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SalidaSaldoCuentaInversion;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.ValorCuota;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;

import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;

public class ConsultarSaldoCuentaInversion implements JavaCall {
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_INSERT_CUENTA_INVERSION_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Insert_Cuenta_Inversion_Tabla_Paso.sql");
	private final String SQL_INSERT_CUENTA_INVERSION_CLIENTE_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Insert_Cuenta_Inversion_Cliente_Tabla_Paso.sql");
	private final String SQL_INSERT_PORTAFOLIO_MONEDA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Insert_Portafolio_Moneda.sql");
	private final String SQL_INSERT_PORTAFOLIO_MONEDA_FILTRO  = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Insert_Portafolio_Moneda_Filtro.sql");
	
	private final String SQL_DELETE_CUENTA_INVERSION_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Delete_Cuenta_Inversion_Tabla_Paso.sql");
	private final String SQL_SELECT_INFO_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Info_Cuenta_Inversion.sql");
	private final String SQL_SELECT_SALDO_EN_GARANTIA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldo_En_Garantia.sql");
	private final String SQL_SELECT_SALDO_A_PLAZO_COMPROMISOS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldo_A_Plazo_Compromisos.sql");
	private final String SQL_SELECT_SALDO_A_PLAZO_SIMULTANEAS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldo_A_Plazo_Simultaneas.sql");
	private final String SQL_SELECT_SALDO_A_PLAZO_SIMULTANEAS_HISTORICO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Historico_Saldo_A_Plazo_Simultaneas.sql");

	private final String SQL_SELECT_SALDO_A_PLAZO_PRESTAMOSEMPRESTITOS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldo_A_Plazo_PrestamosEmprestitos.sql");
	private final String SQL_SELECT_PORTAFOLIOS = "SELECT DISTINCT \"IdPortafolio\" FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso\";";
	private final String SQL_INSERT_PERMITIDOS = "INSERT INTO \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete\"(\"IdTrabajo\",\"IdPortafolio\") VALUES (:IdTrabajo,:IdPortafolio);";
	private final String SQL_DELETE_NO_PERMITIDOS = "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso\" WHERE \"IdPortafolio\" NOT IN (SELECT \"IdPortafolio\" FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete\") AND \"IdTrabajo\" = :IdTrabajo";
	private final String SQL_CLEAN_DELETE = "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete\" WHERE \"IdTrabajo\" = :IdTrabajo;";
	
	private final String SQL_SELECT_SALDO_FECHA_EFECTIVA_TESORERIA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_SaldoFechaEfectiva_Tesoreria.sql");
	private final String SQL_SELECT_SALDO_FECHA_MOVIMIENTO_TESORERIA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_SaldoFechaMovimiento_Tesoreria.sql");

	private final String SQL_SELECT_SALDO_FECHA_EFECTIVA_TESORERIA_PARTIDAS_INGRESADAS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_SaldoFechaEfectiva_Tesoreria_Partidas_Estado_Ingresado.sql");

	private final String SQL_SELECT_ID_ENTIDAD = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Id_Empleado.sql"); 
	private final String SQL_SELECT_INSTRUMENTOS_MONEY_MARKET = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Instrumentos_Money_Market.sql");
	private final String SQL_SELECT_CANTIDAD_CUSTODIA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Cantidad_Custodia.sql");
	
	private final String SQL_SELECT_CANTIDAD_CUSTODIA_HISTORICA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Cantidad_CustodiaHistorica.sql");

	private final String SQL_SELECT_SALDOS_RETENIDOS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldos_Retenidos.sql");
	private final String SQL_SELECT_SALDOS_PEND_LIBERACION_DAP = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldos_Pend_Liberacion_DAP.sql");
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_SolicitudesFFMM.sql");
	private final static Logger logger = LogManager.getLogger(ConsultarSaldoCuentaInversion.class);
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	
	private final String SQL_INSERT_ULTIMO_MOVIMIENTO_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Insert_Ultimo_Movimiento_Tabla_Paso.sql");
	private final String SQL_SELECT_ULTIMO_MOVIMIENTO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Ultimo_Movimiento.sql");

	// ---------------------------------------------------
	// CLASES DE ENTRADA 
	// ---------------------------------------------------
	
	public static class EntradaConsultarSaldoCuentaInversion extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InCuentaInversionConsultar> p_CuentaInversionAConsultar;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_SoloSaldoNegativo;
		public String p_SoloSaldoDisponible;
		public String p_SoloSaldoDisponibleNegativo;
		public String p_IncluyeCargosPendientes;
		public String p_IncluyeAbonosPendientes;
		public String p_IncluyeOtrosSaldos;
		public Long p_IdPortafolio;
		
		public String p_IncluyeProductoApv;
		public Long p_IdEjecutivo;
		public Long p_IdCliente;
		public Long p_IdProductoAgrupador;
		public String p_IdMoneda;

		public List<String> p_TipoSaldoConsultar; // TODO, CONTABLE, DISPONIBLE, RETENIDO , TRANSITO, PENDIENTE
		
		public EntradaConsultarSaldoCuentaInversion(){
			this.p_SoloSaldoNegativo = "N";
			this.p_SoloSaldoDisponible = "N";
			this.p_SoloSaldoDisponibleNegativo = "N";
			this.p_IncluyeCargosPendientes = "N";
			this.p_IncluyeAbonosPendientes = "N";
			this.p_IncluyeOtrosSaldos = "N";
			this.p_IncluyeProductoApv = "N";
			this.p_IdProductoAgrupador = 9999L;
			this.p_CuentaInversionAConsultar = new ArrayList<InCuentaInversionConsultar>();
			this.p_TipoSaldoConsultar = new ArrayList<>(Arrays.asList("TODO"));
		}
	}
	
	public static class EntradaCliente extends BcsPojo{
		public Long p_IdCliente;
		public String p_IdMoneda;
		public Long p_IdTrabajo;
	}
	
	public static class InfoEntidad extends BcsPojo{
		public Long IdEntidad;
		public String Tipo;
	}
	
	public static class Cliente extends BcsPojo{
		public Long IdCliente;
		public String EstadoPortafolio;
		public Long IdPortafolio;
	}
	
	public static class MoneyMarket extends BcsPojo{
		public Long IdCliente;
		public BigDecimal Monto;
	}
	
	public static class InstrumentosMoneyMarket extends BcsPojo{
		public String Nemotecnico;
		public Long IdInstrumento;
	}
	public static class CantidadCustodia extends BcsPojo{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal CantidadCustodia;
		public String Nemotecnico;
	}
	
	public static class Portafolio extends BcsPojo{
		public Long IdPortafolio;
	}
	
	public static class Solicitud extends BcsPojo{
		public Long IdInterno;
		public BigDecimal CantidadMontoAporte;
		public Long IdCliente;
	    public Long IdPortafolio;
	    public String IdMonedaFondo;
	    public String FechaEmision;
	    public BigDecimal MontoMonedaSistemaAporte;
	}
	
	public static class UltimoMovimiento extends BcsPojo{
		public Long IdTrabajo;
		public Long IdCuentaInversion;
		public String KeyUltimoMovimiento;
		public Date FechaSaldo;
	  	public Date FechaUltimoMovimiento;
	  	
	  	public UltimoMovimiento() {
	  		this.FechaUltimoMovimiento = Fecha.toDate("0001-01-01");
	  	}
	}
	
	private Long obtenerPaisSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.obtenerPaisSistema");
		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		logger.info("FIN::ActualizarCarterasFondosMutuos.obtenerPaisSistema");
		return paisSistema;
	}

	
	// ---------------------------------------------------
	// CLASES DE SALIDA 
	// ---------------------------------------------------
	
	public static class SalidaConsultarSaldoCuentaInversion extends BcsPojo{
		public List<SaldoInicialFinal> P_OUT_SALDOINICIALFINAL;
		public List<SaldoPorFechaEfectiva> P_OUT_SALDOPORFECHAEFECTIVA;
		public List<SaldoPorFechaMovimiento> P_OUT_SALDOPORFECHAMOVIMIENTO;
		public Long P_OUT_INFORMACIONCONTADOR;
		public List<InformacionDetalle> P_OUT_INFORMACIONDETALLE;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciasDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErroresDetalle> P_OUT_ERRORESDETALLE;
		
		public SalidaConsultarSaldoCuentaInversion(){
			this.P_OUT_SALDOINICIALFINAL = new ArrayList<SaldoInicialFinal>();
			this.P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<SaldoPorFechaEfectiva>();
			this.P_OUT_SALDOPORFECHAMOVIMIENTO = new ArrayList<SaldoPorFechaMovimiento>();
			this.P_OUT_INFORMACIONDETALLE = new ArrayList<InformacionDetalle>();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciasDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErroresDetalle>();
		}
	}
	
	public static class OtrosSaldos extends BcsPojo{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		public String FechaSaldo;
	}
	
	public static class MontoEnTransitoAporte extends BcsPojo{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		public String FechaEmision;
	}
	
	public static class InfoCuentaInversion extends BcsPojo{
		public Long IdCuentaInversion;
	    public Long IdEjecutivo;
	    public String NombreEjecutivo;
	    public String CodigoSucursal;
	    public String NombreSucursal;
	    public Long IdPersona;
	    public String DocumentoIdentidad;
	    public String NombreCliente;
	    public String NombrePortafolio;
	    public String PropositoPortafolio;
	    public String CodigoPortafolio;
	    public String CodigoMoneda;
	    public String NombreMoneda;
	    public String CuentaInversionPrincipal;
	    public String IdMoneda;
	    public Long IdPortafolio;
	    public String Producto;
	    public Long IdMatrizAgrupador;
	    public String EsADC;
	    public String Relacion;
	    public String TipoCliente;
	    public String CodigoEjecutivo;
	    public Long IdProductoAgrupador;
	}
	
	public static class InformacionDetalle extends BcsPojo{
		public String Codigo;
		public String Descripcion;
	}
	
	public static class AdvertenciasDetalle extends BcsPojo{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	public static class ErroresDetalle extends BcsPojo{
		public String Codigo;
		public String Descripcion;
	}
	
	
	public static class CuentaInversion_Proceso extends BcsPojo{
		public Long IdTrabajo;
		public Long IdEjecutivo;
		public Long IdPortafolio;
		public String IdMoneda;
		
		@Override
		public boolean equals(Object b) {
			CuentaInversion_Proceso a = (CuentaInversion_Proceso)b;
			return (this.IdTrabajo == null && a.IdTrabajo == null || this.IdTrabajo.equals(a.IdTrabajo)) && (this.IdMoneda == null || this.IdMoneda.equals(a.IdMoneda)) 
					&& (this.IdPortafolio == null || this.IdPortafolio.equals(a.IdPortafolio)) || (this.IdEjecutivo == null || this.IdEjecutivo.equals(a.IdEjecutivo));
		}
		
		@Override
		public int hashCode() {
			return ((this.IdEjecutivo==null?0:this.IdEjecutivo) + (this.IdMoneda == null?"":this.IdMoneda) + (this.IdPortafolio == null?0:this.IdPortafolio) + (this.IdTrabajo == null?0:this.IdTrabajo)).hashCode();
		}
	}
	
	
	// ---------------------------------------------------
	// CALL 
	// ---------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarSaldoCuentaInversion jin = gson.fromJson(in.toString(), EntradaConsultarSaldoCuentaInversion.class);
		SalidaConsultarSaldoCuentaInversion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	

	// ---------------------------------------------------
	// LOGICA 
	// ---------------------------------------------------	
	public SalidaConsultarSaldoCuentaInversion logica(Connection con, EntradaConsultarSaldoCuentaInversion entrada) throws Exception {
		logger.info("INICIO::ConsultarSaldoCuentaInversion");
		SalidaConsultarSaldoCuentaInversion out = new SalidaConsultarSaldoCuentaInversion();
		SQL sql=new SQL();
	    List<String> tipos = entrada.p_TipoSaldoConsultar;
	    if (tipos.isEmpty()) {
	        tipos.add("TODO");
	    }

		try{
			
			//calcular mi idempleadoconsulta para saber que portafolios me correspoden
			//3 casos
			//viene el idejecutivo en la entrada -> no lo usa
			//viene en el primer objeto de la lista de cuenta inversion
			//lo saco del usuario
			if (entrada.p_IdEjecutivo == null) {
				if (entrada.p_CuentaInversionAConsultar.size() > 0 && entrada.p_CuentaInversionAConsultar.get(0).IdEjecutivo != null) {
					entrada.p_IdEjecutivo = entrada.p_CuentaInversionAConsultar.get(0).IdEjecutivo;
				}
				else {
					List<InfoEntidad> listIdsEntidad = new ArrayList<InfoEntidad>();
					
					HashMap<String,Object> paramsIdEmpleado =  new HashMap<>();
					paramsIdEmpleado.put("p_IdUsuario", entrada.p_IdUsuario);
					logger.debug("INICIO - Obtener Id empleado");
					listIdsEntidad = sql.query(con, SQL_SELECT_ID_ENTIDAD, paramsIdEmpleado, InfoEntidad.class);
					logger.debug("FIN - Obtener Id empleado o cliente");
					if (listIdsEntidad.isEmpty()) {
						logger.error("Entrada invalida");
						out.P_OUT_ERRORESCONTADOR = 1L;
						ErroresDetalle e = new ErroresDetalle();
						e.Codigo = "UI_ENTRADA_SIN_ID_EJECUTIVO_EMPLEADO";
						e.Descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, e.Codigo).msg();
						out.P_OUT_ERRORESDETALLE.add(e);
						return out;
					}
					if ("CLI".equalsIgnoreCase(listIdsEntidad.get(0).Tipo)) {
						entrada.p_IdEjecutivo = -1L; //-1 significa que esta consultando un cliente, y no debe validar los portafolios permitidos
					}
					else {
						entrada.p_IdEjecutivo = listIdsEntidad.get(0).IdEntidad;	
					}
					
				}
			}
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");			
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
						
			//MUY IMPORTANTE, AQUI SE DETALLAN LOS CASOS PERMITIDOS EN LA ENTRADA*****************
			//se debe calcular todas las cuentas de inversion a consultar
			//modos
				//caso 1, cabecera trae cliente, no trae portafolio
				//caso 2, caebecera trae portafolio
				//caso 3, cabecera no trae cliente ni portafolio, pasamos a revisar la lista
				//caso 3a -> viene un lote de portafolios (patrimonio)
				//caso 3b -> no vienen registros con portafolio, tomamos todos los portafolios del usuario que llamo
			
			Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
			boolean fullPortafolios = false;
			//caso 1
			if (entrada.p_IdCliente != null && entrada.p_IdCliente > 0 && entrada.p_IdPortafolio == null) {
				List<EntradaCliente> n = new ArrayList<>();
				
				EntradaCliente en = new EntradaCliente();
				en.p_IdCliente = entrada.p_IdCliente;
				en.p_IdMoneda = entrada.p_CuentaInversionAConsultar.get(0).IdMoneda;
				en.p_IdTrabajo = idTrabajo;
				n.add(en);
				
				sql.batch(con, SQL_INSERT_CUENTA_INVERSION_CLIENTE_TABLA_PASO, n , EntradaCliente.class);
				sql.batch(con, SQL_INSERT_PORTAFOLIO_MONEDA.replace(":IdTrabajo", idTrabajo+""), new ArrayList<>(), Object.class);
				SQL.exec(con, "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso\" WHERE \"IdMoneda\" IS NULL ");
			}
			//caso 2
			else if (entrada.p_IdPortafolio != null) {
				List<CuentaInversion_Proceso> lista = new ArrayList<>();
				CuentaInversion_Proceso arg0 = new CuentaInversion_Proceso();
				arg0.IdTrabajo = idTrabajo;
				arg0.IdPortafolio = entrada.p_IdPortafolio;
				arg0.IdMoneda = entrada.p_CuentaInversionAConsultar.get(0).IdMoneda;
				lista.add(arg0);
				
				logger.debug("INICIO - Insert tabla paso cuenta inversion");
				sql.batch(con, SQL_INSERT_CUENTA_INVERSION_TABLA_PASO, lista, CuentaInversion_Proceso.class);
				sql.batch(con, SQL_INSERT_PORTAFOLIO_MONEDA.replace(":IdTrabajo", idTrabajo+""), new ArrayList<>(), Object.class);
				SQL.exec(con, "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso\" WHERE \"IdMoneda\" IS NULL ");
				logger.debug("FIN - Insert tabla paso cuenta inversion");
			}
			//caso 3
			else if (entrada.p_IdCliente == null && entrada.p_IdPortafolio == null && !entrada.p_CuentaInversionAConsultar.isEmpty()) {
				HashSet<CuentaInversion_Proceso> portafolios = new HashSet<>();
				for (InCuentaInversionConsultar in : entrada.p_CuentaInversionAConsultar) {
					if (in.IdPortafolio == null) continue;
					CuentaInversion_Proceso c = new CuentaInversion_Proceso();
					c.IdEjecutivo = in.IdEjecutivo;
					c.IdMoneda = entrada.p_CuentaInversionAConsultar.get(0).IdMoneda;
					c.IdPortafolio = in.IdPortafolio;
					c.IdTrabajo = idTrabajo;
					portafolios.add(c);
				}
				
				//caso 3a
				if (!portafolios.isEmpty()) {
					logger.debug("INICIO - Insert tabla paso cuenta inversion");
					sql.batch(con, SQL_INSERT_CUENTA_INVERSION_TABLA_PASO, new ArrayList<>(portafolios), CuentaInversion_Proceso.class);
					sql.batch(con, SQL_INSERT_PORTAFOLIO_MONEDA.replace(":IdTrabajo", idTrabajo+""), new ArrayList<>(), Object.class);
					SQL.exec(con, "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso\" WHERE \"IdMoneda\" IS NULL ");
					logger.debug("FIN - Insert tabla paso cuenta inversion");	
				}
				//caso 3b
				else {
					
					ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
					entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
					entradaConsulta.p_IdModulo = entrada.p_IdModulo;
					entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEjecutivo;
					entradaConsulta.p_ListaIdPortafolios = new ArrayList<>();
					entradaConsulta.p_filtrarListaPortafolios = "N";
					
					logger.debug("INICIO - ObtenerPortafolios");
					ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
					List<Long> listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(con, entradaConsulta);
					logger.debug("FIN - ObtenerPortafolios");	
					
					List<CuentaInversion_Proceso> lista = new ArrayList<>();
					for (Long l : listIdPortafolio) {
						CuentaInversion_Proceso arg0 = new CuentaInversion_Proceso();
						arg0.IdTrabajo = idTrabajo;
						arg0.IdPortafolio = l;
						arg0.IdMoneda = entrada.p_CuentaInversionAConsultar.get(0).IdMoneda;
						lista.add(arg0);
					}
					
					sql.batch(con, SQL_INSERT_CUENTA_INVERSION_TABLA_PASO, lista, CuentaInversion_Proceso.class);
					sql.batch(con, SQL_INSERT_PORTAFOLIO_MONEDA.replace(":IdTrabajo", idTrabajo+""), new ArrayList<>(), Object.class);
					SQL.exec(con, "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso\" WHERE \"IdMoneda\" IS NULL ");
					fullPortafolios = true;
				}
			}
			else {
				logger.error("Entrada invalida");
				out.P_OUT_ERRORESCONTADOR = 1L;
				ErroresDetalle e = new ErroresDetalle();
				e.Codigo = "ENTRADA_INVALIDA";
				e.Descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, e.Codigo).msg();
				out.P_OUT_ERRORESDETALLE.add(e);
				return out;
			}
			
			//FIN PARSEO ENTRADA
			
			//no vuelvo a consultar si voy por full
			if (!fullPortafolios && (entrada.p_IdEjecutivo != null && entrada.p_IdEjecutivo != -1)) {
				
				//SELECT PORTAFOLIOS
				
				List<CuentaInversion_Proceso> portafolios = sql.query(con, SQL_SELECT_PORTAFOLIOS, new HashMap<>(), CuentaInversion_Proceso.class);
				List<Long> listIdPortafolio = portafolios.parallelStream().map(s -> s.IdPortafolio).collect(Collectors.toList());
				
				//filtro los portafolios que me corresponden
				ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
				entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
				entradaConsulta.p_IdModulo = entrada.p_IdModulo;
				entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEjecutivo;
				entradaConsulta.p_ListaIdPortafolios = listIdPortafolio;
				entradaConsulta.p_filtrarListaPortafolios = "S";
				
				logger.debug("INICIO - ObtenerPortafolios - con restriccion");
				ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
				listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(con, entradaConsulta);
				logger.debug("FIN - ObtenerPortafolios - con restriccion");
				
				List<CuentaInversion_Proceso> c = new ArrayList<>();
				for (Long o : listIdPortafolio) {
					CuentaInversion_Proceso cc = new CuentaInversion_Proceso();
					cc.IdPortafolio = o;
					cc.IdTrabajo = idTrabajo;
					c.add(cc);
				}
				
				sql.batch(con, SQL_INSERT_PERMITIDOS, c, CuentaInversion_Proceso.class);
				SQL.exec(con, SQL_DELETE_NO_PERMITIDOS.replace(":IdTrabajo", idTrabajo+""));
				SQL.exec(con, SQL_CLEAN_DELETE.replace(":IdTrabajo", idTrabajo+""));
			}
			
			//Obtener informacion de la cuenta inversion
			Long idProductoAgrupador = entrada.p_IdProductoAgrupador;
			if(idProductoAgrupador != null) {
				idProductoAgrupador = idProductoAgrupador == 9999 ? 0L : entrada.p_IdProductoAgrupador;
			}
			HashMap<String,Object> parmsInfoCtaInv =  new HashMap<>();
			parmsInfoCtaInv.put("p_IdProductoAgrupador", idProductoAgrupador);
			parmsInfoCtaInv.put("p_IdTrabajo", idTrabajo);
			
			List<InfoCuentaInversion> infoCuentaInversion = new ArrayList<InfoCuentaInversion>();
			logger.debug("INICIO - Select Obtener info cuenta inversion");
			infoCuentaInversion = sql.query(con, SQL_SELECT_INFO_CUENTA_INVERSION, parmsInfoCtaInv, InfoCuentaInversion.class);
			logger.debug("FIN - Select Obtener info cuenta inversion");
			
			//Obtener informacion de MontoTransSolicitudesAportes para WS
			String filtraSoloSolicitudesNoAutomaticas = obtenerParametroFiltroSoloSolicitudesNoAutomaticas(con, entrada.p_IdEmpresa);
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			
			if(_c.SI.equals(filtraSoloSolicitudesNoAutomaticas)) {
				parametros.put("IngresoAutomatico", _c.NO);
				parametros.put("FiltraIngresoAutomatico", _c.SI);
			}else {
				parametros.put("IngresoAutomatico", "");
				parametros.put("FiltraIngresoAutomatico", _c.NO);
			}	
			parametros.put("IdTrabajo", idTrabajo);
			parametros.put("IdProductoAgrupador", entrada.p_IdProductoAgrupador);
			List<Solicitud> solicitudes = new ArrayList<Solicitud>();
			if (tipos.contains("TODO") || tipos.contains("TRANSITO")) {
				solicitudes = 	obtenerSolicitudesTransito(con, entrada, parametros);
			}		
		
		
			List<InCuentaInversionConsultar> listaCuentas = infoCuentaInversion.parallelStream().map(p -> {
				InCuentaInversionConsultar n = new InCuentaInversionConsultar();
				n.IdEjecutivo = p.IdEjecutivo;
				n.IdMoneda = p.IdMoneda;
				n.IdPortafolio = p.IdPortafolio;
				return n;
			}).collect(Collectors.toList());
			
			SalidaSaldoCuentaInversion salidaSaldo =  new SalidaSaldoCuentaInversion();	
			
			if(!listaCuentas.isEmpty()){
				//Obtener Saldo disponible en cuenta inversion
				EntradaSaldoCuentaInversion entradaSaldo = new EntradaSaldoCuentaInversion();
				entradaSaldo.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSaldo.p_FechaDesde = entrada.p_FechaDesde;
				entradaSaldo.p_FechaHasta = entrada.p_FechaHasta;
				entradaSaldo.p_SoloSaldoNegativo = entrada.p_SoloSaldoNegativo;
				entradaSaldo.p_SoloSaldoDisponible = entrada.p_SoloSaldoDisponible;
				entradaSaldo.p_SoloSaldoDisponibleNegativo = entrada.p_SoloSaldoDisponibleNegativo;
				entradaSaldo.p_IncluyeCargosPendientes = entrada.p_IncluyeCargosPendientes;
				entradaSaldo.p_IncluyeAbonosPendientes = entrada.p_IncluyeAbonosPendientes;
				entradaSaldo.p_CuentaInversionAConsultar = listaCuentas;
						
				logger.debug("INICIO - Select obtener saldos");
				SaldoCuentaInversion saldoCuentaInversion = new SaldoCuentaInversion();
				salidaSaldo = saldoCuentaInversion.logica(con, entradaSaldo);
				logger.debug("FIN - Select Obtener saldos"); 
				
				HashMap<Long,Object> infoCuentHash = Utils.List2Hash(infoCuentaInversion, "IdCuentaInversion");
					
				//Monto en transito de solicitudes de aporte 
				for (SaldoPorFechaMovimiento s: salidaSaldo.P_OUT_SALDOPORFECHAMOVIMIENTO) {
					for (Solicitud sol : solicitudes) {
						if(s.IdCliente.compareTo(sol.IdCliente)==0 && s.IdPortafolio.compareTo(sol.IdPortafolio)==0 && s.IdMoneda.equalsIgnoreCase(sol.IdMonedaFondo)) {
							if(Fecha.toDate(sol.FechaEmision).before(Fecha.toDate(s.FechaSaldo)) || Fecha.toDate(sol.FechaEmision).equals(Fecha.toDate(s.FechaSaldo))) {
								s.MontoTransSolicitudesAporte = s.MontoTransSolicitudesAporte.add(sol.CantidadMontoAporte, MathContext.DECIMAL128);
							}
						}
					}	
				}
				
				for (SaldoPorFechaEfectiva s: salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA) {
					for (Solicitud sol : solicitudes) {
						if(s.IdCliente.compareTo(sol.IdCliente)==0 && s.IdPortafolio.compareTo(sol.IdPortafolio)==0 && s.IdMoneda.equalsIgnoreCase(sol.IdMonedaFondo)) {
							if(Fecha.toDate(sol.FechaEmision).before(Fecha.toDate(s.FechaSaldo)) || Fecha.toDate(sol.FechaEmision).equals(Fecha.toDate(s.FechaSaldo))) {
								s.MontoTransSolicitudesAporte = s.MontoTransSolicitudesAporte.add(sol.CantidadMontoAporte, MathContext.DECIMAL128);
							}
						}
					}	
				}
				
				//Obtener otros saldos (Operaciones que afectaran el saldo)
				if(entrada.p_IncluyeOtrosSaldos.equalsIgnoreCase("S")){					
					HashMap<String,Object> paramsOtrosSaldos =  new HashMap<>();
					paramsOtrosSaldos.put("p_IdEmpresa", entrada.p_IdEmpresa);
					paramsOtrosSaldos.put("p_FechaHasta", entrada.p_FechaHasta);
					paramsOtrosSaldos.put("IdTrabajo", idTrabajo);

					HashMap<String, OtrosSaldos> saldosEnGarantiaHash = new HashMap<>();
					if (tipos.contains("TODO") || tipos.contains("CONTABLE")) {
						consultarSaldosGarantia(sql, con, paramsOtrosSaldos, saldosEnGarantiaHash);
					}

					paramsOtrosSaldos.put("p_FechaDesde", entrada.p_FechaDesde);
					paramsOtrosSaldos.remove("p_FechaHasta");

					HashMap<String, OtrosSaldos> saldosCompromisosHash = new HashMap<>();
					if (tipos.contains("TODO") || tipos.contains("TRANSITO")) {
						consultarSaldosCompromisos(sql, con, paramsOtrosSaldos, saldosCompromisosHash);
					}

					HashMap<String, OtrosSaldos> saldosSimultaneasHash = new HashMap<>();
					if (tipos.contains("TODO") || tipos.contains("TRANSITO")) {
						consultarSaldosSimultaneas(sql, con, paramsOtrosSaldos, saldosSimultaneasHash, fechaSistema, entrada.p_FechaDesde);
					}

					HashMap<String, OtrosSaldos> saldosPresEmprHash = new HashMap<>();
					if (tipos.contains("TODO") || tipos.contains("TRANSITO")) {
						consultarSaldosPrestamosEmprestitos(sql, con, paramsOtrosSaldos, saldosPresEmprHash);
					}

					HashMap<String, OtrosSaldos> saldosFechaEfectivaTesoreriaHash = new HashMap<>();
					if (tipos.contains("TODO") || tipos.contains("DISPONIBLE")) {
						consultarSaldosTesoreriaEfectiva(sql, con, paramsOtrosSaldos, saldosFechaEfectivaTesoreriaHash, entrada.p_FechaHasta);
					}

					HashMap<String, OtrosSaldos> saldosFechaMovimientoTesoreriaHash = new HashMap<>();
					if (tipos.contains("TODO") || tipos.contains("CONTABLE")) {
						consultarSaldosTesoreriaMovimiento(sql, con, paramsOtrosSaldos, saldosFechaMovimientoTesoreriaHash);
					}

					HashMap<String, OtrosSaldos> saldosRetenidosHash = new HashMap<>();
					if (tipos.contains("TODO") || tipos.contains("RETENIDO")) {
						consultarSaldosRetenidos(sql, con, paramsOtrosSaldos, saldosRetenidosHash);
					}
					
					HashMap<String, OtrosSaldos> saldosPendLiberacionDAP = new HashMap<>();
					if (tipos.contains("TODO") || tipos.contains("PENDIENTE")) {
						consultarSaldosPendLiberacionDAP(sql, con, paramsOtrosSaldos, saldosPendLiberacionDAP);
					}
					

					//Agregar informacion faltante de las cuentas de inversion

					for (SaldoPorFechaEfectiva s: salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA) {
						String llaveHash = s.IdCliente.toString() + s.IdPortafolio.toString() + s.IdMoneda + s.FechaSaldo;
						
						OtrosSaldos saldoGarantia = saldosEnGarantiaHash.get(llaveHash);
						OtrosSaldos saldoCompromiso = saldosCompromisosHash.get(llaveHash);
						OtrosSaldos saldoSimultaneas = saldosSimultaneasHash.get(llaveHash);
						OtrosSaldos saldoPresEmpr = saldosPresEmprHash.get(llaveHash);
						
						OtrosSaldos saldoFechaEfectivaTesoreria = saldosFechaEfectivaTesoreriaHash.get("0" + s.IdPortafolio.toString() + s.IdMoneda + s.FechaSaldo);
						OtrosSaldos saldoRetenido = saldosRetenidosHash.get("0"+ s.IdPortafolio.toString() + s.IdMoneda + s.FechaSaldo);
						OtrosSaldos saldoPendLiberacionDAP = saldosPendLiberacionDAP.get("0"+ s.IdPortafolio.toString() + s.IdMoneda + s.FechaSaldo);
						
						
						s.SaldoEnGarantia = saldoGarantia != null ? saldoGarantia.Monto : BigDecimal.ZERO;
						s.SaldoAPlazoCompromiso = saldoCompromiso != null ? saldoCompromiso.Monto : BigDecimal.ZERO;
						s.SaldoAPlazoSimultanea = saldoSimultaneas != null ? saldoSimultaneas.Monto : BigDecimal.ZERO;
						s.SaldoAPlazoVentaCorta = saldoPresEmpr != null ? saldoPresEmpr.Monto : BigDecimal.ZERO;
						

						s.SaldoRetiro = saldoFechaEfectivaTesoreria != null ? saldoFechaEfectivaTesoreria.Monto : BigDecimal.ZERO;
						
						s.SaldoRetenido = saldoRetenido != null ? saldoRetenido.Monto : BigDecimal.ZERO;
						s.SaldoPendienteLibDAP = saldoPendLiberacionDAP != null ? saldoPendLiberacionDAP.Monto : BigDecimal.ZERO;
						
					}
				
					for (SaldoPorFechaMovimiento s: salidaSaldo.P_OUT_SALDOPORFECHAMOVIMIENTO) {
						String llaveHash = s.IdCliente.toString() + s.IdPortafolio.toString() + s.IdMoneda + s.FechaSaldo;
						
						OtrosSaldos saldoGarantia = saldosEnGarantiaHash.get(llaveHash);
						OtrosSaldos saldoCompromiso = saldosCompromisosHash.get(llaveHash);
						OtrosSaldos saldoSimultaneas = saldosSimultaneasHash.get(llaveHash);
						OtrosSaldos saldoPresEmpr = saldosPresEmprHash.get(llaveHash);
						
						
						OtrosSaldos saldoFechaMovimientoTesoreria = saldosFechaMovimientoTesoreriaHash.get("0" + s.IdPortafolio + s.IdMoneda + s.FechaSaldo);
						
						s.SaldoEnGarantia = saldoGarantia != null ? saldoGarantia.Monto : BigDecimal.ZERO;
						s.SaldoAPlazoCompromiso = saldoCompromiso != null ? saldoCompromiso.Monto : BigDecimal.ZERO;
						s.SaldoAPlazoSimultanea = saldoSimultaneas != null ? saldoSimultaneas.Monto : BigDecimal.ZERO;
						s.SaldoAPlazoVentaCorta = saldoPresEmpr != null ? saldoPresEmpr.Monto : BigDecimal.ZERO;
						
						s.SaldoRetiro = saldoFechaMovimientoTesoreria != null ? saldoFechaMovimientoTesoreria.Monto : BigDecimal.ZERO;
						
						s.SaldoRetenido = BigDecimal.ZERO;
						s.SaldoPendienteLibDAP = BigDecimal.ZERO;
					}	
				}

				//----------------------------------------------------------------------------------------
				// CALCULO DE MONEY MARKET
				HashMap<String,Object> parmsInstrMoney =  new HashMap<>();
				List<ValorCuota> valoresCuota = new ArrayList<ValorCuota>();
				HashMap<String,Object> params = new HashMap<String,Object>();
				List<CantidadCustodia> cantidad = new ArrayList<CantidadCustodia>();
				
				//Obtiene los instrumentos marcados como MoneyMarket				
				List<InstrumentosMoneyMarket> instrumentoMoney = sql.query(con, SQL_SELECT_INSTRUMENTOS_MONEY_MARKET, parmsInstrMoney, InstrumentosMoneyMarket.class);
				List<String> nemotecnicos = instrumentoMoney.stream().filter(s -> s.IdInstrumento != null).map(s -> s.Nemotecnico).collect(Collectors.toList());

				List<Long> IdPortafolio = salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA.stream().map(s -> s.IdPortafolio).collect(Collectors.toList());
				HashMap<String,Object> valoresCuot;
				Map<String,List<CantidadCustodia>> cantidadesAgrupadas;
				
				
				//Se agrega el portafolio en CLP a la tabla de paso, ya que es necesario para el calculo en USD.
				//Al agregar el portafolio en CLP a la tabla de paso se obtiene el monto de CantidadCustodia necesario para calcular el moneyMarket cuando se filtra solo para USD
				String FiltroMoneda  = entrada.p_CuentaInversionAConsultar.get(0).IdMoneda;
				if( FiltroMoneda != null ) {
					sql.batch(con, SQL_INSERT_PORTAFOLIO_MONEDA_FILTRO.replace(":IdTrabajo", idTrabajo+""), new ArrayList<>(), Object.class);					
				}
				
				//No calcular cuando el proceso sea masivo, sino se va a la B			
				if (nemotecnicos.size()>0 && IdPortafolio.size()>0)
				{
					
					//Obtiene valor cuota de nemotecnicos marcados para MoneyMarket
					valoresCuota = obtenerPrecios(con, entrada.p_IdEmpresa, nemotecnicos, entrada.p_IdMoneda, entrada.p_FechaDesde);					
					
					//Obtiene el total custodia por portafolio, monedainstrumento y nemotecnico
					params.clear();
					
					if (entrada.p_FechaDesde.equals(fechaSistema))
					{
						params.put("Nemotecnico", nemotecnicos);
						cantidad = sql.query(con, SQL_SELECT_CANTIDAD_CUSTODIA, params, CantidadCustodia.class);
					}
					else
					{
						params.put("Nemotecnico", nemotecnicos);
						params.put("FechaConsulta", entrada.p_FechaDesde);
						
						cantidad = sql.query(con, SQL_SELECT_CANTIDAD_CUSTODIA_HISTORICA, params, CantidadCustodia.class);
					}
				

					valoresCuot = Utils.List2Hash(valoresCuota, "IdSerieFondo");
					cantidadesAgrupadas = cantidad.stream().collect(Collectors.groupingBy(cant-> cant.IdPortafolio + "_" + cant.IdMoneda));
				}
				else
				{
					valoresCuot = new HashMap<String,Object>();
					cantidadesAgrupadas = new HashMap<String,List<CantidadCustodia>>();
				}

				CalcularParidad servicioCalculaParidad = new CalcularParidad();
				CalcularParidad.CalcularParidadEntrada ParidadEntrada = new CalcularParidad.CalcularParidadEntrada();
				List<CalcularParidad.CalculoEntrada> listaParidadEntrada = new ArrayList<CalcularParidad.CalculoEntrada>();
				HashMap<Long,BigDecimal> paridadMontoMoneyMarket = new HashMap<Long,BigDecimal>();
				
				
				ParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				ParidadEntrada.p_IdTrabajo = 0L;
				

				salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA.forEach(saldo -> {
					
						saldo.MontoMoneyMarket = BigDecimal.ZERO;						
						// Calcula MoneyMarket General
						List<CantidadCustodia> cantidades =   cantidadesAgrupadas.get(saldo.IdPortafolio + "_" + saldo.IdMoneda);
						if(cantidades!=null){
							for(CantidadCustodia reg : cantidades){
								ValorCuota val = (ValorCuota) valoresCuot.get(reg.Nemotecnico);
								if(val!=null){
									saldo.MontoMoneyMarket = saldo.MontoMoneyMarket.add(valoresCuot == null ? BigDecimal.ZERO : reg.CantidadCustodia.multiply(val.ValorCuota, MathContext.DECIMAL128));
								}
							}
						}
						
						// Si la moneda es distinta a la moneda sistema (CLP) se hace convergencia del MoneyMarket Calculado anteriormente a la moneda que corresponde
						if (!saldo.IdMoneda.equalsIgnoreCase(monedaSistema))
						{
							CalculoEntrada entradaCalculoParidad = new CalculoEntrada();
							entradaCalculoParidad.IdTrabajo = 0L;
							entradaCalculoParidad.IdInterno = saldo.IdCuentaInversion;
							
							entradaCalculoParidad.Fecha = entrada.p_FechaDesde;
							
							entradaCalculoParidad.IdMonedaOrigen = monedaSistema;
							entradaCalculoParidad.IdMonedaDestino = saldo.IdMoneda; // MONDEA A TRANSFORMAR
							entradaCalculoParidad.ValorCambioLocalModificado = BigDecimal.ZERO;
							entradaCalculoParidad.IdUso = "";
							entradaCalculoParidad.MontoOrigen = saldo.MontoMoneyMarket;

				            listaParidadEntrada.add(entradaCalculoParidad);
						}

				});
				
				if (listaParidadEntrada.size() > 0)
				{
					ParidadEntrada.p_CalculosEntrada = listaParidadEntrada;
					CalcularParidadSalida paridadCargo = servicioCalculaParidad.logica(con, ParidadEntrada); 
					
					for(CalculoSalida paridadTotal :paridadCargo.p_out_CalculosSalida ) {
						paridadMontoMoneyMarket.put(paridadTotal.IdInterno, paridadTotal.MontoDestino);
					}		
				}
				
				List<UltimoMovimiento> ultimosMovimientos = new ArrayList<UltimoMovimiento>();

				if(!salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()) {
					List<UltimoMovimiento> cuentasAConsultar = salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA.stream()
						    .map(saldo -> {
						        UltimoMovimiento um = new UltimoMovimiento();
						        um.IdTrabajo = idTrabajo;
						        um.IdCuentaInversion = saldo.IdCuentaInversion;
						        um.FechaSaldo = Fecha.toDate(saldo.FechaSaldo);
						        return um;
						    })
						    .collect(Collectors.toList());
					
					logger.debug("INICIO - Insert tabla paso ultimos movimientos");
					sql.batch(con, SQL_INSERT_ULTIMO_MOVIMIENTO_TABLA_PASO, cuentasAConsultar, UltimoMovimiento.class);
					logger.debug("FIN - Insert tabla paso ultimos movimientos");
					
					HashMap<String,Object> parmsUltimoMovimiento =  new HashMap<>(1);
					parmsUltimoMovimiento.put("p_IdTrabajo", idTrabajo);
					
					logger.debug("INICIO - Select Obtener ultimos movimientos");
					ultimosMovimientos = sql.query(con, SQL_SELECT_ULTIMO_MOVIMIENTO, parmsUltimoMovimiento, UltimoMovimiento.class);
					logger.debug("FIN - Select Obtener ultimos movimientos");
				}				
				
				HashMap<String, Object> hashUltimosMovimientos = Utils.List2Hash(ultimosMovimientos, "KeyUltimoMovimiento");

				//PREPARAR SALIDA FINAL.
				salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA.forEach(saldo -> {
					InfoCuentaInversion infoextra = (InfoCuentaInversion) infoCuentHash.get(saldo.IdCuentaInversion);
					if(Objects.nonNull(infoextra)) {
						saldo.IdEjecutivo = infoextra.IdEjecutivo;
						saldo.NombreEjecutivo = infoextra.NombreEjecutivo;
						saldo.CodigoEjecutivo = infoextra.CodigoEjecutivo;
						saldo.DocumentoIdentidad = infoextra.DocumentoIdentidad;
						saldo.CodigoSucursal = Optional.ofNullable(infoextra.CodigoSucursal).orElse("");
						saldo.NombreSucursal = Optional.ofNullable(infoextra.NombreSucursal).orElse("");
						saldo.NombreCliente = infoextra.NombreCliente;
						//saldo.NombrePortafolio = infoextra.NombrePortafolio;
						saldo.NombrePortafolio = "(" + infoextra.CodigoPortafolio + ") - " + infoextra.PropositoPortafolio;
						saldo.PropositoPortafolio = infoextra.PropositoPortafolio;
						saldo.CodigoPortafolio = infoextra.CodigoPortafolio;
						saldo.NombreMoneda = infoextra.NombreMoneda;
						saldo.CodigoMoneda = infoextra.CodigoMoneda;
						saldo.Producto = Optional.ofNullable(infoextra.Producto).orElse("");
						

						saldo.IdProductoAgrupador = infoextra.IdProductoAgrupador;
						
						saldo.SaldoEnGarantia = Optional.ofNullable(saldo.SaldoEnGarantia).orElse(BigDecimal.ZERO);
						saldo.SaldoAPlazoSimultanea = Optional.ofNullable(saldo.SaldoAPlazoSimultanea).orElse(BigDecimal.ZERO);
						saldo.SaldoAPlazoVentaCorta = Optional.ofNullable(saldo.SaldoAPlazoVentaCorta).orElse(BigDecimal.ZERO);
						saldo.SaldoAPlazoCompromiso = Optional.ofNullable(saldo.SaldoAPlazoCompromiso).orElse(BigDecimal.ZERO);
						saldo.SaldoRetiro = Optional.ofNullable(saldo.SaldoRetiro).orElse(BigDecimal.ZERO);
						saldo.IdMatrizAgrupador = infoextra.IdMatrizAgrupador;
						saldo.EsADC = infoextra.EsADC;
						saldo.Relacion = infoextra.Relacion;
						
						if (saldo.CodigoMoneda.equalsIgnoreCase("CLP")) {
							saldo.Saldo = saldo.Saldo.setScale(0, RoundingMode.DOWN);
							saldo.SaldoDisponible = saldo.SaldoDisponible.setScale(0, RoundingMode.DOWN);
							saldo.SaldoCargosPendientes = saldo.SaldoCargosPendientes.setScale(0, RoundingMode.DOWN);
							saldo.SaldoAbonosPendientes = saldo.SaldoAbonosPendientes.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoEnGarantia = saldo.SaldoEnGarantia.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoAPlazoSimultanea = saldo.SaldoAPlazoSimultanea.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoAPlazoVentaCorta = saldo.SaldoAPlazoVentaCorta.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoAPlazoCompromiso = saldo.SaldoAPlazoCompromiso.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoRetiro = saldo.SaldoRetiro.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoRetenido = saldo.SaldoRetenido.setScale(0, RoundingMode.DOWN);
						    saldo.MontoTransSolicitudesAporte = saldo.MontoTransSolicitudesAporte.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoPendienteLibDAP = saldo.SaldoPendienteLibDAP.setScale(0, RoundingMode.DOWN);
						}
						
						// Tipo Cliente
						saldo.TipoCliente = Optional.ofNullable(infoextra.TipoCliente).orElse("");
						
						// Si la moneda de la cuenta inversion es distinta a la moneda sistema (CLP) se reemplaza MoneyMarket por su paridad en la moneda que corresponda
						if (!saldo.IdMoneda.equalsIgnoreCase(monedaSistema))
						{
							BigDecimal montoMoneyMarket = paridadMontoMoneyMarket.get(saldo.IdCuentaInversion);
							
							saldo.MontoMoneyMarket = montoMoneyMarket != null ? montoMoneyMarket : BigDecimal.ZERO;
									
						}

						saldo.TotalLiquidez = saldo.MontoMoneyMarket.add(saldo.Saldo);
						String keyUltimoMovimiento = idTrabajo + saldo.IdCuentaInversion.toString() + saldo.FechaSaldo;
						UltimoMovimiento ultimoMovimientoCuentaInversion = (UltimoMovimiento) hashUltimosMovimientos.getOrDefault(keyUltimoMovimiento, new UltimoMovimiento());
						saldo.FechaUltimoMovimiento = Fecha.stringToSqlDate(ultimoMovimientoCuentaInversion.FechaUltimoMovimiento);
					}
				});
				salidaSaldo.P_OUT_SALDOPORFECHAMOVIMIENTO.forEach(saldo -> {
					InfoCuentaInversion infoextra = (InfoCuentaInversion) infoCuentHash.get(saldo.IdCuentaInversion);
					if(Objects.nonNull(infoextra)) {
						saldo.IdEjecutivo = infoextra.IdEjecutivo;
						saldo.NombreEjecutivo = infoextra.NombreEjecutivo;
						saldo.CodigoEjecutivo = infoextra.CodigoEjecutivo;
						saldo.DocumentoIdentidad = infoextra.DocumentoIdentidad;
						saldo.CodigoSucursal = Optional.ofNullable(infoextra.CodigoSucursal).orElse("");
						saldo.NombreSucursal = Optional.ofNullable(infoextra.NombreSucursal).orElse("");
						saldo.NombreCliente = infoextra.NombreCliente;
						saldo.NombrePortafolio = infoextra.NombrePortafolio;
						saldo.PropositoPortafolio = infoextra.PropositoPortafolio;
						saldo.CodigoPortafolio = infoextra.CodigoPortafolio;
						saldo.CodigoMoneda = infoextra.CodigoMoneda;
						saldo.NombreMoneda = infoextra.NombreMoneda;
						saldo.Producto = Optional.ofNullable(infoextra.Producto).orElse("");
						

						saldo.IdProductoAgrupador = infoextra.IdProductoAgrupador;
						
						saldo.SaldoEnGarantia = Optional.ofNullable(saldo.SaldoEnGarantia).orElse(BigDecimal.ZERO);
						saldo.SaldoAPlazoSimultanea = Optional.ofNullable(saldo.SaldoAPlazoSimultanea).orElse(BigDecimal.ZERO);
						saldo.SaldoAPlazoVentaCorta = Optional.ofNullable(saldo.SaldoAPlazoVentaCorta).orElse(BigDecimal.ZERO);
						saldo.SaldoAPlazoCompromiso = Optional.ofNullable(saldo.SaldoAPlazoCompromiso).orElse(BigDecimal.ZERO);
						saldo.SaldoRetiro = Optional.ofNullable(saldo.SaldoRetiro).orElse(BigDecimal.ZERO);
						saldo.IdMatrizAgrupador = infoextra.IdMatrizAgrupador;
						saldo.EsADC = infoextra.EsADC;
						
						if (saldo.CodigoMoneda.equalsIgnoreCase("CLP")) {
							saldo.Saldo = saldo.Saldo.setScale(0, RoundingMode.DOWN);
							saldo.SaldoDisponible = saldo.SaldoDisponible.setScale(0, RoundingMode.DOWN);
							saldo.SaldoCargosPendientes = saldo.SaldoCargosPendientes.setScale(0, RoundingMode.DOWN);
							saldo.SaldoAbonosPendientes = saldo.SaldoAbonosPendientes.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoEnGarantia = saldo.SaldoEnGarantia.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoAPlazoSimultanea = saldo.SaldoAPlazoSimultanea.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoAPlazoVentaCorta = saldo.SaldoAPlazoVentaCorta.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoAPlazoCompromiso = saldo.SaldoAPlazoCompromiso.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoRetiro = saldo.SaldoRetiro.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoRetenido = saldo.SaldoRetenido.setScale(0, RoundingMode.DOWN);
						    saldo.MontoTransSolicitudesAporte = saldo.MontoTransSolicitudesAporte.setScale(0, RoundingMode.DOWN);
						    saldo.SaldoPendienteLibDAP = saldo.SaldoPendienteLibDAP.setScale(0, RoundingMode.DOWN);
						}
					}
				});
			}
			
			SQL.exec(con, SQL_DELETE_CUENTA_INVERSION_TABLA_PASO.replace(":IdTrabajo", idTrabajo+""));
			
			out.P_OUT_SALDOPORFECHAEFECTIVA = salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA;
			out.P_OUT_SALDOPORFECHAMOVIMIENTO = salidaSaldo.P_OUT_SALDOPORFECHAMOVIMIENTO;
			out.P_OUT_SALDOINICIALFINAL = salidaSaldo.P_OUT_SALDOINICIALFINAL;
			out.P_OUT_ERRORESCONTADOR = 0L; 
			out.P_OUT_ADVERTENCIASCONTADOR = 0L; 
			out.P_OUT_INFORMACIONCONTADOR = 0L;

		}catch(Exception e){
			logger.error("ERROR",e);
			throw e;
		}
		
		logger.info("FIN::ConsultarSaldoCuentaInversion");
		return out;
	}
	
	
	private String obtenerParametroFiltroSoloSolicitudesNoAutomaticas(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarSaldoCuentaInversion.obtenerParametroFiltroSoloSolicitudesNoAutomaticas");
		long inicio = System.currentTimeMillis();
		String filtraSoloSolicitudesNoAutomaticas = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS");
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSaldoCuentaInversion.obtenerParametroFiltroSoloSolicitudesNoAutomaticas", fin - inicio);
		return filtraSoloSolicitudesNoAutomaticas;
	}
	
	private List<Solicitud> obtenerSolicitudesTransito(Connection con, EntradaConsultarSaldoCuentaInversion entrada, HashMap<String, Object> parametrosConsultaSolicitudes) throws Exception{
		logger.debug("INICIO::ConsultarSaldoCuentaInversion.obtenerSolicitudes");
		long inicio = System.currentTimeMillis();
		SQL sql=new SQL();
		List<Solicitud> solicitudes = sql.query(con, SQL_SELECT_SOLICITUDES, parametrosConsultaSolicitudes, Solicitud.class);
		solicitudes = asignarIdInternoSolicitudes(solicitudes);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSaldoCuentaInversion.obtenerSolicitudesTransito", fin - inicio);
		return solicitudes;
	}
	
	private List<Solicitud> asignarIdInternoSolicitudes(List<Solicitud> solicitudes){
		logger.debug("INICIO::ConsultarSaldoCuentaInversion.asignarIdInternoSolicitudes");
		long inicio = System.currentTimeMillis();
		AtomicLong idInterno = new AtomicLong(1L);
		solicitudes.parallelStream().forEach(solicitud->{
			solicitud.IdInterno = idInterno.incrementAndGet();
		});
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSaldoCuentaInversion.asignarIdInternoSolicitudes", fin - inicio);
		return solicitudes;
	}
	
	private List<ValorCuota> obtenerPrecios(Connection con, Long idEmpresa, List<String> series, String idMoneda, String p_FechaDesde) throws Exception {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.obtenerPrecios");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdSerieFondo", series);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		params.clear();
		if(seriesSolicitudes.size() == 0)
			return null;
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		EntregarPreciosTasas precioTasa = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		paramsPrecioTasa.p_IdEmpresa = idEmpresa;
		paramsPrecioTasa.p_IdModulo = null;
		paramsPrecioTasa.p_IdPais = obtenerPaisSistema(con,idEmpresa);
		paramsPrecioTasa.p_IdUsuario = null;
		paramsPrecioTasa.p_DatosInstrumentos = series.parallelStream()
		.map(dato->{
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(dato);
			EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
			paramsRegIn.FechaConsulta = Fecha.utilDateToSqlDate(Fecha.toDate(p_FechaDesde));
			paramsRegIn.IdClasificacionTipoInstrumento = "FM";
			paramsRegIn.IdInstrumento = serie.IdInstrumento;
			paramsRegIn.IdMoneda = idMoneda;
			/*if(dato.IdInterno != 0 && dato.IdInterno != null)
				paramsRegIn.IdRegistro = dato.IdInterno;
			else*/
			paramsRegIn.IdRegistro = serie.IdInstrumento; 
			paramsRegIn.IdTipoCriterioTasa = "0";
			paramsRegIn.IdTipoInstrumento = serie.IdTipoInstrumento;
			paramsRegIn.IdUso = "CARTERA";
			paramsRegIn.Nemotecnico = dato;
			paramsRegIn.PlazoResidual = 0L;
			paramsRegIn.TipoCartera = "0";
			return paramsRegIn;
		})
		.collect(Collectors.toList());
		
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = precioTasa.logica(con, paramsPrecioTasa);
		//Map<Long,Object> preciosMap = salidaPrecioTasa.P_OUT_PRECIOSTASAS.parallelStream().filter(precio -> precio.IdInstrumento != null).collect(Collectors.toMap(instrumento->instrumento.IdRegistro, precio-> precio));
		List<ValorCuota> precios = new ArrayList<ValorCuota>();
		for (EntregarPreciosTasas_Salida_PreciosTasas s : salidaPrecioTasa.P_OUT_PRECIOSTASAS) {
			ValorCuota vCuota = new ValorCuota();
			vCuota.IdSerieFondo = s.Nemotecnico;
			vCuota.ValorCuota = s.Precio;
			if(s.FechaFuente != null) {
				vCuota.FechaValorCuota = s.FechaFuente.toString();
			}else {
				vCuota.FechaValorCuota =null;
			}
			precios.add(vCuota);
		}

		return precios;
	}

	private void consultarSaldosGarantia(SQL sql, Connection con, HashMap<String, Object> params, Map<String, OtrosSaldos> hash) throws Exception {
		logger.debug("INICIO::ConsultarSaldoCuentaInversion.consultarSaldosGarantia");
		long inicio = System.currentTimeMillis();
		List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDO_EN_GARANTIA, params, OtrosSaldos.class);
		for (OtrosSaldos sc : lista) {
			String key = sc.IdCliente.toString() + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
			hash.put(key, sc);
		}
		logger.debug("FIN::ConsultarSaldoCuentaInversion.consultarSaldosGarantia - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
	}

	private void consultarSaldosCompromisos(SQL sql, Connection con, HashMap<String, Object> params, Map<String, OtrosSaldos> hash) throws Exception {
		logger.debug("INICIO::ConsultarSaldoCuentaInversion.consultarSaldosCompromisos");
		long inicio = System.currentTimeMillis();
		List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDO_A_PLAZO_COMPROMISOS, params, OtrosSaldos.class);
		for (OtrosSaldos sc : lista) {
			String key = sc.IdCliente.toString() + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
			hash.put(key, sc);
		}
		logger.debug("FIN::ConsultarSaldoCuentaInversion.consultarSaldosCompromisos - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
	}

	private void consultarSaldosSimultaneas(SQL sql, Connection con, HashMap<String, Object> params, Map<String, OtrosSaldos> hash, String fechaSistema, String fechaDesdeEntrada) throws Exception {
		Date fechaDesde = Fecha.toDate(fechaDesdeEntrada);
		Date fechaSistemaDate = Fecha.toDate(fechaSistema);

		if (fechaDesde.compareTo(fechaSistemaDate) >= 0) {
			logger.debug("INICIO - SELECT SALDO SIMULTANEAS");
			long inicio = System.currentTimeMillis();
			List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDO_A_PLAZO_SIMULTANEAS, params, OtrosSaldos.class);
			logger.debug("FIN - SELECT SALDO SIMULTANEAS - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
			for (OtrosSaldos sc : lista) {
				String key = sc.IdCliente.toString() + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
				hash.put(key, sc);
			}
		} else if (fechaDesde.compareTo(fechaSistemaDate) < 0) {
			logger.debug("INICIO - SELECT SALDO SIMULTANEAS HISTORICO");
			long inicio = System.currentTimeMillis();
			List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDO_A_PLAZO_SIMULTANEAS_HISTORICO, params, OtrosSaldos.class);
			logger.debug("FIN - SELECT SALDO SIMULTANEAS HISTORICO - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
			for (OtrosSaldos sc : lista) {
				String key = sc.IdCliente.toString() + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
				hash.put(key, sc);
			}
		}
	}

	private void consultarSaldosPrestamosEmprestitos(SQL sql, Connection con, HashMap<String, Object> params, Map<String, OtrosSaldos> hash) throws Exception {
		logger.debug("INICIO - SELECT SALDO PRESTAMOS Y EMPRESTITOS");
		long inicio = System.currentTimeMillis();
		List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDO_A_PLAZO_PRESTAMOSEMPRESTITOS, params, OtrosSaldos.class);
		logger.debug("FIN - SELECT SALDO PRESTAMOS Y EMPRESTITOS - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
		for (OtrosSaldos sc : lista) {
			String key = sc.IdCliente.toString() + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
			hash.put(key, sc);
		}
	}

	private void consultarSaldosTesoreriaEfectiva(SQL sql, Connection con, HashMap<String, Object> params, Map<String, OtrosSaldos> hash, String fechaHasta) throws Exception {
		logger.debug("INICIO - SELECT SALDO FECHA EFECTIVA TESORERIA");
		long inicio = System.currentTimeMillis();

		List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDO_FECHA_EFECTIVA_TESORERIA, params, OtrosSaldos.class);

		params.put("p_FechaHasta", fechaHasta);
		List<OtrosSaldos> lista2 = sql.query(con, SQL_SELECT_SALDO_FECHA_EFECTIVA_TESORERIA_PARTIDAS_INGRESADAS, params, OtrosSaldos.class);
		lista.addAll(lista2);
		params.remove("p_FechaHasta");

		for (OtrosSaldos sc : lista) {
			String key = "0" + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
			if (hash.containsKey(key))
				sc.Monto = sc.Monto.add(hash.get(key).Monto);
			hash.put(key, sc);
		}

		logger.debug("FIN - SELECT SALDO FECHA EFECTIVA  TESORERIA - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
	}

	private void consultarSaldosTesoreriaMovimiento(SQL sql, Connection con, HashMap<String, Object> params, Map<String, OtrosSaldos> hash) throws Exception {
		logger.debug("INICIO - SELECT SALDO FECHA MOVIMIENTO TESORERIA");
		long inicio = System.currentTimeMillis();
		List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDO_FECHA_MOVIMIENTO_TESORERIA, params, OtrosSaldos.class);
		for (OtrosSaldos sc : lista) {
			String key = "0" + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
			hash.put(key, sc);
		}
		logger.debug("FIN - SELECT SALDO FECHA MOVIMIENTO  TESORERIA - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
	}

	private void consultarSaldosRetenidos(SQL sql, Connection con, HashMap<String, Object> params, Map<String, OtrosSaldos> hash) throws Exception {
		logger.debug("INICIO - SELECT SALDO RETENIDO");
		long inicio = System.currentTimeMillis();
		List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDOS_RETENIDOS, params, OtrosSaldos.class);
		for (OtrosSaldos sc : lista) {
			String key = "0" + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
			hash.put(key, sc);
		}
		logger.debug("FIN - SELECT SALDO RETENIDO - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
	}
	
	private void consultarSaldosPendLiberacionDAP(SQL sql, Connection con, HashMap<String, Object> params, Map<String, OtrosSaldos> hash) throws Exception {
		logger.debug("INICIO - SELECT SALDO PENDIENTE LIBERACION DAP");
		long inicio = System.currentTimeMillis();
		List<OtrosSaldos> lista = sql.query(con, SQL_SELECT_SALDOS_PEND_LIBERACION_DAP, params, OtrosSaldos.class);
		for (OtrosSaldos sc : lista) {
			String key = "0" + sc.IdPortafolio.toString() + sc.IdMoneda + sc.FechaSaldo;
			hash.put(key, sc);
		}
		logger.debug("FIN - SELECT SALDO PENDIENTE LIBERACION DAP - Tiempo: " + (System.currentTimeMillis() - inicio) + "ms");
	}
		
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		//String jsonERR =   "";
		String miJson = "{\n"
				+ "    \"p_IdEmpresa\": 1,\n"
				+ "    \"p_IdUsuario\": 9497,\n"
				+ "    \"p_IdModulo\": \"CTA\",\n"
				+ "    \"p_UsaReglasNegocio\": \"S\",\n"
				+ "    \"p_IncluyeOtrosSaldos\": \"S\",\n"
				+ "    \"p_IdPais\": 44,\n"
				+ "    \"p_FechaDesde\": \"2026-06-22\",\n"
				+ "    \"p_FechaHasta\": \"2026-06-22\",\n"
				+ "    \"p_IdPortafolio\": 7757,\n"
				+ "    \"p_IdCliente\": 3050,\n"
				+ "    \"p_CuentaInversionAConsultar\": [\n"
				+ "        {\n"
				+ "            \"IdEjecutivo\": null,\n"
				+ "            \"IdPortafolio\": 7757,\n"
				+ "            \"IdMoneda\": null\n"
				+ "        }\n"
				+ "    ]\n"
				+ "}";
		EntradaConsultarSaldoCuentaInversion Objin = gson.fromJson(miJson, EntradaConsultarSaldoCuentaInversion.class);
		ConsultarSaldoCuentaInversion demo=new ConsultarSaldoCuentaInversion();				
		try {
			SalidaConsultarSaldoCuentaInversion ObjOut=demo.logica(con, Objin);
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
