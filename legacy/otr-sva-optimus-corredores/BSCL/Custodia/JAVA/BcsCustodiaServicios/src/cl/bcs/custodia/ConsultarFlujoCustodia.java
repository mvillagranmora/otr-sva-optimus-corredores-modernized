package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarFlujoCustodia implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarFlujoCustodia.class);

	//private String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectCarteras.sql");
	private String SQL_SELECT_CARTERAS_PARAM = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectCarterasParam.sql");
	private String SQL_SELECT_CARTERAS_SELECT_CLAUSE = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectCarterasSelectClause.sql");
	private String SQL_SELECT_CARTERAS_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectCarterasHistorico.sql");
	private String SQL_SELECT_CARTERAS_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectCarterasTransito.sql");
	private String SQL_SELECT_CARTERAS_TRANSITO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectCarterasTransitoHistorico.sql");
	private String SQL_SELECT_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectTransito.sql");
	private String SQL_SELECT_TRANSITO_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectTransitoHistorico.sql");
	private String SQL_SELECT_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectGarantia.sql");
	private String SQL_SELECT_GARANTIA_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectGarantiaHistorico.sql");
	private String SQL_SELECT_SUBYACENTE = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectSubyacente.sql");
	private String SQL_SELECT_SUBYACENTE_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectSubyacenteHistorico.sql");
	private String SQL_SELECT_CARTERA_RESERVADA = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectCarteraReservada.sql");
	private String SQL_SELECT_CARTERA_RESERVADA_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectCarteraReservadaHistorico.sql");
	private String SQL_SELECT_ORDENES_POR_AUTORIZAR = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectOrdenesPorAutorizar.sql");
	private String SQL_SELECT_ORDENES_INGRESADAS = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectOrdenesIngresadas.sql");
	private String SQL_SELECT_ORDENES_REPORTE_POR_AUTORIZAR = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectOrdenesReportePorAutorizar.sql");
	private String SQL_SELECT_ORDENES_REPORTE_INGRESADAS = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectOrdenesReporteIngresadas.sql");
	private String SQL_SELECT_ASIGNACIONES_NO_ENVIADAS_A_FACTURAR = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectAsignacionesNoEnviadasAFacturar.sql");
	private String SQL_SELECT_ASIGNACIONES_REPORTE_NO_ENVIADAS_A_FACTURAR = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectAsignacionesReporteNoEnviadasAFacturar.sql");
	private String SQL_SELECT_MOVIMIENTOS_FACTURACION = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectMovimientosFacturacion.sql");
	private String SQL_SELECT_MOVIMIENTOS_REPORTE_FACTURACION = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectMovimientosReporteFacturacion.sql");
	private String SQL_SELECT_EVENTOS_DE_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectEventosDeCapital.sql");
	private String SQL_SELECT_FONDOS_MUTUOS = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectFondosMutuos.sql");
	
	private String SQL_SELECT_INTRUMENTOS_FLUJO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectInstrumentosFlujo.sql");
	private String SQL_SELECT_PORTAFOLIOS_CUENTA_PROPIA = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectPortafoliosCuentaPropia.sql");	
	private String SQL_SELECT_TIPO_INSTRUMENTO_PARA_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectTipoInstrumentoParaGarantia.sql");
	
	private String SQL_DELETE_INSERTFLUJOCUSTODIA_MATRIZUSO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_DeleteFlujoCustodia_MatrizUso.sql");
	private String SQL_INSERT_INSERTFLUJOCUSTODIA_MATRIZUSO = loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_InsertFlujoCustodia_MatrizUso.sql");
	
	private HashMap<String, String> valoresItemCalculoHash = setItemCalculoHash();
	
	public final List<String> tipoOperacion = new ArrayList<>(Arrays.asList("VENSIM", "PRESTA", "EMPRES", "COMPAC", "VENPAC", "COMRRV", "VENRRV", "COMRRF", "VENRRF"));
	public final List<String> tipoOperacionEvtoCapital = new ArrayList<>(Arrays.asList("COMSIM", "VENSIM", "PRESTA", "COMPAC", "VENPAC", "COMRRV", "VENRRV", "COMRRF", "VENRRF"));
	
	
	
	public static class FlujoCustodiaEntrada {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String 			p_IdModulo; //L = 3
		public String 			p_FechaSaldo; //L = 10
		
		public Long 			p_IdPais; //-- DEFAULT -1
		public Integer 			p_DiasProyeccion; //Cantidad de dias a la q se proyecta el saldo consultado -- DEFAULT 0
		public String 			p_DiaHabil = "S"; //L = 1 -- DEFAULT 'S'
		public String 			p_ValorizarFlujo; //L = 1 -- DEFAULT 'N'
		public BarraProgreso	p_BarraProgreso;
		
		
		public List<FlujoUso> 				p_Uso;
		public List<FlujoFiltrosBusqueda> 	p_FiltrosBusqueda;
		public List<FlujoEsAgrupadoPor> 	p_EsAgrupadoPor;
			
		public FlujoCustodiaEntrada() {
			this.p_Uso = new ArrayList<>();
			this.p_FiltrosBusqueda = new ArrayList<>();
			this.p_EsAgrupadoPor = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FlujoCustodiaSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public Long 						p_out_KeyFlujoCustodia;
		public List<FlujoCustodia> 			p_out_FlujoTotalizado;
		public List<FlujoCustodiaDetallado> p_out_FlujoDetallado;
		
		public FlujoCustodiaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.p_out_FlujoTotalizado = new ArrayList<FlujoCustodia>(); //
			this.p_out_FlujoDetallado = new ArrayList<FlujoCustodiaDetallado>(); //
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FlujoUso {
		public String UsoTipoOperacion; // L = 6
		public Long IdCliente;
		public Long IdPortafolio;
		public String BusquedaEspecifica; // L = 40
		public String IdTipoInstrumentoEspecifico; // L = 3
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		@Override
		public boolean equals(Object o) {
			FlujoUso u1 = (FlujoUso)o;
			return (this.UsoTipoOperacion+"").equals(u1.UsoTipoOperacion+"") 
					&& (this.IdCliente+"").equals(u1.IdCliente+"")
					&& (this.IdPortafolio+"").equals(u1.IdPortafolio+"")
					&& (this.BusquedaEspecifica+"").equals(u1.BusquedaEspecifica+"")
					&& (this.IdTipoInstrumentoEspecifico+"").equals(u1.IdTipoInstrumentoEspecifico+"");
		}
		
		@Override
		public int hashCode() {
			return (UsoTipoOperacion + IdCliente + IdPortafolio + BusquedaEspecifica + IdTipoInstrumentoEspecifico).hashCode();
		}
		
	}

	/**
	 * 
	 * IdClasificacionTipoInstrumento L = 3 <br/>
	 * IdTipoInstrumento L = 3 <br/>
	 * IdInstrumento L = 3 <br/>
	 * IdEmisor L = 3 <br/>
	 * FechaVencimiento L = 3 <br/>
	 * IdTipoReajuste L = 3 <br/>
	 * EsFungible L = 3 <br/>
	 * IdBoveda L = 3 <br/>
	 */
	public static class FlujoFiltrosBusqueda {
		public String IdClasificacionTipoInstrumento; // L = 3
		public String IdTipoInstrumento; // L = 3
		public Long IdInstrumento;
		public String IdEmisor; // L = 40
		public java.sql.Date FechaVencimiento;
		public String IdTipoReajuste; // L = 3
		public String EsFungible; // L = 1
		public String IdBoveda; // L = 3 
		public String IdRegimenFondoMutuo; //L = 40
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		@Override
		public boolean equals(Object o) {
			FlujoFiltrosBusqueda u1 = (FlujoFiltrosBusqueda)o;
			return (this.IdClasificacionTipoInstrumento+"").equals(u1.IdClasificacionTipoInstrumento+"") 
					&& (this.IdTipoInstrumento+"").equals(u1.IdTipoInstrumento+"")
					&& (this.IdInstrumento+"").equals(u1.IdInstrumento+"")
					&& (this.IdEmisor+"").equals(u1.IdEmisor+"")
					&& (this.FechaVencimiento+"").equals(u1.FechaVencimiento+"")
					&& (this.IdTipoReajuste+"").equals(u1.IdTipoReajuste+"")
					&& (this.EsFungible+"").equals(u1.EsFungible+"")
					&& (this.IdBoveda+"").equals(u1.IdBoveda+"")
					&& (this.IdRegimenFondoMutuo+"").equals(u1.IdRegimenFondoMutuo+"");
		}
		
		@Override
		public int hashCode() {
			return (IdClasificacionTipoInstrumento + IdTipoInstrumento + IdInstrumento + IdEmisor + FechaVencimiento + IdTipoReajuste
					+ EsFungible + IdBoveda + IdRegimenFondoMutuo
					).hashCode();
		}
	}
	
	public static class FlujoUsoFiltrosBusqueda {
		public String UsoTipoOperacion; // L = 6
		public Long IdCliente;
		public Long IdPortafolio;
		public String BusquedaEspecifica; // L = 40
		public String IdTipoInstrumentoEspecifico; // L = 3
		
		public String IdClasificacionTipoInstrumento; // L = 3
		public String IdTipoInstrumento; // L = 3
		public Long IdInstrumento;
		public String IdEmisor; // L = 40
		public java.sql.Date FechaVencimiento;
		public String IdTipoReajuste; // L = 3
		public String EsFungible; // L = 1
		public String IdBoveda; // L = 3
		public String IdRegimenFondoMutuo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FlujoEsAgrupadoPor {
		public String ClasificacionTipoInstrumento; // L = 1
		public String TipoInstrumento; // L = 1
		public String Instrumento; // L = 1
		public String Emisor; // L = 1
		public String Nemotecnico; // L = 1
		public String NemoSVS; // L = 1
		public String FechaEmisionInstrumento; // L = 1
		public String FechaVencimientoInstrumento; // L = 1
		public String FechaLiquidacionInstrumento; // L = 1
		public String TipoReajuste; // L = 1
		public String EsFungible; // L = 1
		public String CodigoIFRS; // L = 1
		public String Boveda; // L = 1
		public String Bolsa; // L = 1
		public String TipoOperacion; // L = 1
		public String Mesa; // L = 1
		public String FolioRegistro; // L = 1
		public String ItemOrigen; // L = 1
		public String IdRegimenFondoMutuo; // L = 1
		public String GrupoRegimen;
		public String MonedaInstrumento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FlujoCustodia {
		public Long				IdProcesamientoInterno;
		public String 			UsoTipoOperacion; // L = 6
		public String 			BusquedaEspecifica; // L = 40
		public java.sql.Date 	FechaSaldo; // L = 10
		public Long				Dias = 0L;
		public Long 			IdCliente;
		public Long 			IdPortafolio;
		public String 			IdClasificacionTipoInstrumento; // L = 3
		public String 			IdTipoInstrumento; // L = 3
		public Long 			IdInstrumento;
		public String 			IdEmisor; // L = 40
		public String 			Nemotecnico; // L = 40
		public String 			NemoSVS; // L = 40
		public BigDecimal		CorteMinimo;
		public String 			IdMonedaInstrumento; // L = 3
		public String 			IdTipoReajuste; // L = 3
		public java.sql.Date 	FechaEmisionInstrumento;
		public java.sql.Date 	FechaVencimientoInstrumento;
		public java.sql.Date 	FechaLiquidacionInstrumento;
		public String 			CodigoIFRS; // L = 40
		public String 			EsFungible; // L = 1
		public String 			IdBoveda; // L = 3
		public String 			IdBolsa; // L = 3
		public String 			IdTipoOperacion; // L = 6
		public String 			Accion; // L = 40
		public Long 			IdMesa;
		public Long 			FolioRegistro;
		public String 			ItemOrigen; // L = 40
		public BigDecimal 		Cantidad;
		public BigDecimal 		ValorRescate;		
		public BigDecimal 		PrecioMercado;
		public BigDecimal 		TasaMercado;
		public BigDecimal 		MontoAMercadoMonedaSistema;	
		public BigDecimal 		MontoAMercadoMonedaInstrumento;
		public String 			IdMonedaEmisionInstrumento;
		public String 			IdRegimenFondoMutuo;
		public String 			GrupoRegimen;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FlujoCustodiaDetallado {
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento; // L = 3
		public String IdTipoInstrumento; // L = 3
		public Long IdInstrumento;
		public String TipoValorizacion; // L = 10
		public String IdEmisor; // L = 40
		public String Nemotecnico; // L = 40
		public String NemoSVS; // L = 40
		public BigDecimal	CorteMinimo;
		public String IdMonedaInstrumento; // L = 3
		public String IdTipoReajuste; // L = 3
		public java.sql.Date FechaEmisionInstrumento;
		public java.sql.Date FechaVencimientoInstrumento;
		public String CodigoIFRS; // L = 40
		public String EsFungible; // L = 1
		public String IdBoveda; // L = 3
		public String IdBolsa; // L = 3
		public String IdTipoOperacion; // L = 6
		public String Accion; // L = 40
		public String IdTipoProducto; // L = 4
		public Long IdMesa;
		public java.sql.Date FechaLiquidacion;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public BigDecimal PrecioCompra;
		public BigDecimal TasaCompra;
		public BigDecimal MontoATasaCompra;
		public BigDecimal PrecioMercado;
		public BigDecimal TasaMercado;
		public BigDecimal MontoATasaMercado;
		public BigDecimal PrecioVector;
		public BigDecimal MontoVector;
		public Long IdRegistro;
		public Long FolioRegistro;
		
		public String IdConcepto; // L = 6
		public Long IdDatosFlujoCustodia;
		public Long IdUsoFlujoCustodia;
		public String UsoTipoOperacion; // L = 6
		public String BusquedaEspecifica; // L = 40
		public String ItemCalculo; // L = 40
		public String ItemOrigen; // L = 40
		public String FuncionOperador; // L = 1
		public BigDecimal CantidadProceso;
		public BigDecimal ValorRescateProceso;
		public BigDecimal PrecioCompraProceso;
		public BigDecimal TasaCompraProceso;
		public BigDecimal MontoCompraProceso;
		public BigDecimal PrecioMercadoProceso;
		public BigDecimal TasaMercadoProceso;
		public BigDecimal MontoMercadoProceso;
		public BigDecimal PrecioVectorProceso;
		public BigDecimal MontoVectorProceso;
		public java.sql.Date FechaLiquidacionProceso; // L = 10
		public String RequiereValorizacion; // L = 1
		public String ValorizarFlujo; // L = 1
		public String IdRegimenFondoMutuo;
		public String GrupoRegimen;
		public java.sql.Date FechaOrigenCompra;
		public java.sql.Date FechaOperacion;
		public Long			IdContraparte;
		public Long			IdCorredorContraparte;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*
	 * 
	 * Configuracion del uso del flujo de custodia. Corresponde a un join entre las
	 * tablas "BSCL.Custodia.BD::CUS.UsoFlujoCustodia" y "BSCL.Custodia.BD::CUS.ItemsFlujoCustodia"
	 * 
	 */
	public static class ConfiguracionUsoFlujoCustodia {
		public Long IdUsuario;
		public Long IdUsoFlujoCustodia;
		public String UsoTipoOperacion;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdCliente;
		public Long IdPortafolio;
		public String BusquedaEspecifica;
		public java.sql.Timestamp FechaIngresoRegistro;

		public String ItemCalculo;
		public String ItemOrigen;
		public String Descripcion;
		public String FuncionOperador;
		public String UsoValorizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
		
		public static List<ConfiguracionUsoFlujoCustodia> obtenerUsoFlujoCustodia(Connection con, long idEmpresa) throws SQLException {
			String SqlSelectUsoFlujoCustodia = (new FileLoader()).loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectUsoFlujoCustodia.sql");
			SQL sql = new SQL();
			HashMap<String, Object> parms = new HashMap<String, Object>();
			parms.put("IdEmpresa", idEmpresa);
			return sql.query(con, SqlSelectUsoFlujoCustodia, parms, ConfiguracionUsoFlujoCustodia.class);
		}
		
		public static List<ConfiguracionUsoFlujoCustodia> obtenerItemsFlujoCustodia(Connection con, long idEmpresa) throws SQLException {
			String SqlSelectUsoFlujoCustodia = (new FileLoader()).loadResourceToString("cl/bcs/custodia/sql/FlujoCustodia_SelectItemsFlujoCustodia.sql");
			SQL sql = new SQL();
			HashMap<String, Object> parms = new HashMap<String, Object>();
			parms.put("IdEmpresa", idEmpresa);
			return sql.query(con, SqlSelectUsoFlujoCustodia, parms, ConfiguracionUsoFlujoCustodia.class);
		}
		
	}
	
	
	public static class MatrizUso {
		public Long IdTrabajo;
		public Long IdCliente;
		public Long IdPortafolio;
		public String UsoTipoOperacion; // NVARCHAR(6),
		public String IdClasificacionTipoInstrumento; // NVARCHAR(3),
		public String IdTipoInstrumento; // NVARCHAR(3),
		public Long IdInstrumento;
		public String IdEmisor; // NVARCHAR(40),
		public java.sql.Date FechaVencimiento;
		public String IdTipoReajuste; // NVARCHAR(3),
		public String EsFungible; // NVARCHAR(1),
		public String IdBoveda; // NVARCHAR(3),
		public String IdTipoInstrumentoEspecifico; // NVARCHAR(3),
		public Long IdUsoFlujoCustodia;
		public String BusquedaEspecifica; // NVARCHAR(40),
		public String IdRegimenFondoMutuo;
		public String IdEstado; // NVARCHAR(3),
		public Long IdEmpresa;
		
		public String ItemCalculo;
		public String ItemOrigen;
		
		public String FuncionOperador;
		
		public int MayorSimilitud;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FlujoCustodiaItems {
		public String				ItemCalculo; 
		public String				ItemOrigen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FlujoCustodia_MatrizUso {
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public String				UsoTipoOperacion; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				IdEmisor; 
		public java.sql.Date		FechaVencimiento; 
		public String				IdTipoReajuste; 
		public String				EsFungible; 
		public String				IdBoveda; 
		public String				IdTipoInstrumentoEspecifico; 
		public Long					IdUsoFlujoCustodia; 
		public String				BusquedaEspecifica; 
		public String				IdEstado; 
		public Long					IdEmpresa; 
	}
	
	public static class DatosFlujoCustodia {
		public Long					IdDatosFlujoCustodia; 
		public Long					KeyFlujoCustodia; 
		public Long					IdUsoFlujoCustodia; 
		public String				UsoTipoOperacion; 
		public String				BusquedaEspecifica; 
		public String				ItemCalculo; 
		public String				ItemOrigen; 
		public String				FuncionOperador; 
		public java.sql.Timestamp	FechaEjecucion; 
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public String				EsCuentaPropia; 
		public Long					IdRegistro; 
		public Long					FolioRegistro; 
		public String				IdConcepto; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				TipoValorizacion; 
		public String				IdEmisor; 
		public String				Nemotecnico; 
		public String				NemoSVS;
		public BigDecimal			CorteMinimo;
		public String				IdMonedaInstrumento; 
		public String				IdTipoReajuste; 
		public java.sql.Date		FechaEmisionInstrumento; 
		public java.sql.Date		FechaVencimientoInstrumento; 
		public String				CodigoIFRS; 
		public String				EsFungible; 
		public String				TipoCarteraCorredor; 
		public String				IdBoveda; 
		public String				IdTipoInstrumentoEspecifico; 
		public String				IdBolsa; 
		public String				IdTipoOperacion; 
		public String				Accion; 
		public String				IdTipoProducto; 
		public Long					IdMesa; 
		public java.sql.Date		FechaLiquidacion; 
		public BigDecimal			Cantidad; 
		public BigDecimal			ValorRescate; 
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra; 
		public BigDecimal			MontoATasaCompra; 
		public BigDecimal			PrecioMercado; 
		public BigDecimal			TasaMercado; 
		public BigDecimal			MontoATasaMercado; 
		public BigDecimal			PrecioVector; 
		public BigDecimal			MontoVector; 
		public BigDecimal			CantidadProceso; 
		public BigDecimal			ValorRescateProceso; 
		public BigDecimal			PrecioCompraProceso; 
		public BigDecimal			TasaCompraProceso; 
		public BigDecimal			MontoCompraProcesoMonedaSistema; 
		public BigDecimal			MontoCompraProcesoMonedaInstrumento;
		
		public BigDecimal			PrecioMercadoProceso; 
		public BigDecimal			TasaMercadoProceso; 
		public BigDecimal			MontoMercadoProcesoMonedaSistema;
		public BigDecimal			MontoMercadoProcesoMonedaInstrumento;
		
		public java.sql.Date		FechaLiquidacionProceso; 
		public String				RequiereValorizacion; 
		public String				TipoRegistro; 
		public String				MotivoEliminacioRegistro; 
		public String				ValorizarFlujo; 
		public String				VALTipoCartera; 
		public String				VALUsoCriterio; 
		public String				VALIdTipoCriterioTasa; 
		public BigDecimal			VALPrecioCompra; 
		public BigDecimal			VALTasaCompra; 
		public BigDecimal			VALMontoATasaCompraMonedaInstrumento;
		public BigDecimal			VALMontoATasaCompraMonedaSistema;
		
		public BigDecimal			VALPrecioMercado; 
		public BigDecimal			VALTasaMercado; 
		public BigDecimal			VALMontoATasaMercadoMonedaInstrumento; 
		public BigDecimal			VALMontoATasaMercadoMonedaSistema;
		
		public BigDecimal			MontoCompraMonedaLocalProceso;
		
		public String				IdAccionOperacion;
		public String 				IdMonedaEmision;
		
		public String				IdRegimenFondoMutuo;
		public String				GrupoRegimen;
		public java.sql.Date		FechaOrigenCompra;
		public java.sql.Date		FechaOperacion;
		public String				EsVentaDescubierta;
		public Long					IdContraparte;
		public Long                 IdCorredorContraparte;
		
		public DatosFlujoCustodia() {
			this.FuncionOperador			= "";
			this.TipoRegistro				= "R";	// R: Real obtenido desde tablas de sistema,
													// P: Proyeccion segun dias de consulta,
													// M: Datos Matriz, 
													// D: Registros eliminados no cumplen con la configuracion.
			this.MotivoEliminacioRegistro				= "";	// Solo si el TipoRegistro = D
			this.ValorizarFlujo							= "N";	// S: Se solicita valorizar, N: No se solicita valorizar
			this.VALTipoCartera							= "";
			this.VALUsoCriterio							= "";
			this.VALIdTipoCriterioTasa					= "";
			this.VALPrecioCompra						= new BigDecimal(0);
			this.VALTasaCompra							= new BigDecimal(0);
			this.VALMontoATasaCompraMonedaSistema		= new BigDecimal(0);
			this.VALMontoATasaCompraMonedaInstrumento	= new BigDecimal(0);
			this.VALPrecioMercado						= new BigDecimal(0);
			this.VALTasaMercado							= new BigDecimal(0);
			this.VALMontoATasaMercadoMonedaSistema		= new BigDecimal(0);
			this.VALMontoATasaMercadoMonedaInstrumento	= new BigDecimal(0);
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public String toStringA() {
			String st = "{\n";
			String temp = "";
			
			temp = this.IdDatosFlujoCustodia           == null ? "\"NULL\"" : "\"" + this.IdDatosFlujoCustodia.toString() + "\""           ;
			st += "\"IdDatosFlujoCustodia\":"           + temp + ",\n";
			temp = this.KeyFlujoCustodia               == null ? "\"NULL\"" : "\"" + this.KeyFlujoCustodia.toString() + "\""               ;
			st += "\"KeyFlujoCustodia\":"               + temp + ",\n";
			temp = this.IdUsoFlujoCustodia             == null ? "\"NULL\"" : "\"" + this.IdUsoFlujoCustodia.toString() + "\""             ;
			st += "\"IdUsoFlujoCustodia\":"             + temp + ",\n";
			temp = this.UsoTipoOperacion               == null ? "\"NULL\"" : "\"" + this.UsoTipoOperacion.toString() + "\""               ;
			st += "\"UsoTipoOperacion\":"               + temp + ",\n";
			temp = this.BusquedaEspecifica             == null ? "\"NULL\"" : "\"" + this.BusquedaEspecifica.toString() + "\""             ;
			st += "\"BusquedaEspecifica\":"             + temp + ",\n";
			temp = this.IdTipoInstrumentoEspecifico    == null ? "\"NULL\"" : "\"" + this.IdTipoInstrumentoEspecifico.toString() + "\""    ;
			st += "\"IdTipoInstrumentoEspecifico\":"    + temp + ",\n";
			temp = this.ItemCalculo                    == null ? "\"NULL\"" : "\"" + this.ItemCalculo.toString() + "\""                    ;
			st += "\"ItemCalculo\":"                    + temp + ",\n";
			temp = this.ItemOrigen                     == null ? "\"NULL\"" : "\"" + this.ItemOrigen.toString() + "\""                     ;
			st += "\"ItemOrigen\":"                     + temp + ",\n";
			temp = this.FechaEjecucion                 == null ? "\"NULL\"" : "\"" + this.FechaEjecucion.toString() + "\""                 ;
			st += "\"FechaEjecucion\":"                 + temp + ",\n";
			temp = this.IdClasificacionTipoInstrumento == null ? "\"NULL\"" : "\"" + this.IdClasificacionTipoInstrumento.toString() + "\"" ;
			st += "\"IdClasificacionTipoInstrumento\":" + temp + ",\n";
			temp = this.IdTipoInstrumento              == null ? "\"NULL\"" : "\"" + this.IdTipoInstrumento.toString() + "\""              ;
			st += "\"IdTipoInstrumento\":"              + temp + ",\n";
			temp = this.IdInstrumento                  == null ? "\"NULL\"" : "\"" + this.IdInstrumento.toString() + "\""                  ;
			st += "\"IdInstrumento\":"                  + temp + ",\n";
			temp = this.IdEmisor                       == null ? "\"NULL\"" : "\"" + this.IdEmisor.toString() + "\""                       ;
			st += "\"IdEmisor\":"                       + temp + ",\n";
			temp = this.Nemotecnico                    == null ? "\"NULL\"" : "\"" + this.Nemotecnico.toString() + "\""                    ;
			st += "\"Nemotecnico\":"                    + temp + ",\n";
			temp = this.NemoSVS                        == null ? "\"NULL\"" : "\"" + this.NemoSVS.toString() + "\""                        ;
			st += "\"NemoSVS\":"                        + temp + ",\n";
			temp = this.IdMonedaInstrumento            == null ? "\"NULL\"" : "\"" + this.IdMonedaInstrumento.toString() + "\""            ;
			st += "\"IdMonedaInstrumento\":"            + temp + ",\n";
			temp = this.IdTipoReajuste                 == null ? "\"NULL\"" : "\"" + this.IdTipoReajuste.toString() + "\""                 ;
			st += "\"IdTipoReajuste\":"                 + temp + ",\n";
			temp = this.FechaEmisionInstrumento        == null ? "\"NULL\"" : "\"" + this.FechaEmisionInstrumento.toString() + "\""        ;
			st += "\"FechaEmisionInstrumento\":"        + temp + ",\n";
			temp = this.FechaVencimientoInstrumento    == null ? "\"NULL\"" : "\"" + this.FechaVencimientoInstrumento.toString() + "\""    ;
			st += "\"FechaVencimientoInstrumento\":"    + temp + ",\n";
			temp = this.CodigoIFRS                     == null ? "\"NULL\"" : "\"" + this.CodigoIFRS.toString() + "\""                     ;
			st += "\"CodigoIFRS\":"                     + temp + ",\n";
			temp = this.EsFungible                     == null ? "\"NULL\"" : "\"" + this.EsFungible.toString() + "\""                     ;
			st += "\"EsFungible\":"                     + temp + ",\n";
			temp = this.TipoCarteraCorredor            == null ? "\"NULL\"" : "\"" + this.TipoCarteraCorredor.toString() + "\""            ;
			st += "\"TipoCarteraCorredor\":"            + temp + ",\n";
			temp = this.IdBoveda                       == null ? "\"NULL\"" : "\"" + this.IdBoveda.toString() + "\""                       ;
			st += "\"IdBoveda\":"                       + temp + ",\n";
			temp = this.IdBolsa                        == null ? "\"NULL\"" : "\"" + this.IdBolsa.toString() + "\""                        ;
			st += "\"IdBolsa\":"                        + temp + ",\n";
			temp = this.IdTipoOperacion                == null ? "\"NULL\"" : "\"" + this.IdTipoOperacion.toString() + "\""                ;
			st += "\"IdTipoOperacion\":"                + temp + ",\n";
			temp = this.IdMesa                         == null ? "\"NULL\"" : "\"" + this.IdMesa.toString() + "\""                         ;
			st += "\"IdMesa\":"                         + temp + ",\n";
			temp = this.FechaLiquidacion               == null ? "\"NULL\"" : "\"" + this.FechaLiquidacion.toString() + "\""               ;
			st += "\"FechaLiquidacion\":"               + temp + ",\n";
			temp = this.Cantidad                       == null ? "\"NULL\"" : "\"" + this.Cantidad.toString() + "\""                       ;
			st += "\"Cantidad\":"                       + temp + ",\n";
			temp = this.ValorRescate                   == null ? "\"NULL\"" : "\"" + this.ValorRescate.toString() + "\""                   ;
			st += "\"ValorRescate\":"                   + temp + ",\n";
			temp = this.PrecioCompra                   == null ? "\"NULL\"" : "\"" + this.PrecioCompra.toString() + "\""                   ;
			st += "\"PrecioCompra\":"                   + temp + ",\n";
			temp = this.TasaCompra                     == null ? "\"NULL\"" : "\"" + this.TasaCompra.toString() + "\""                     ;
			st += "\"TasaCompra\":"                     + temp + ",\n";
			temp = this.MontoATasaCompra               == null ? "\"NULL\"" : "\"" + this.MontoATasaCompra.toString() + "\""               ;
			st += "\"MontoATasaCompra\":"               + temp + ",\n";
			temp = this.PrecioMercado                  == null ? "\"NULL\"" : "\"" + this.PrecioMercado.toString() + "\""                  ;
			st += "\"PrecioMercado\":"                  + temp + ",\n";
			temp = this.TasaMercado                    == null ? "\"NULL\"" : "\"" + this.TasaMercado.toString() + "\""                    ;
			st += "\"TasaMercado\":"                    + temp + ",\n";
			temp = this.MontoATasaMercado              == null ? "\"NULL\"" : "\"" + this.MontoATasaMercado.toString() + "\""              ;
			st += "\"MontoATasaMercado\":"              + temp + ",\n";
			temp = this.IdCliente                      == null ? "\"NULL\"" : "\"" + this.IdCliente.toString() + "\""                      ;
			st += "\"IdCliente\":"                      + temp + ",\n";
			temp = this.IdPortafolio                   == null ? "\"NULL\"" : "\"" + this.IdPortafolio.toString() + "\""                   ;
			st += "\"IdPortafolio\":"                   + temp + ",\n";
			temp = this.FechaLiquidacionProceso        == null ? "\"NULL\"" : "\"" + this.FechaLiquidacionProceso.toString() + "\""        ;
			st += "\"FechaLiquidacionProceso\":"        + temp + ",\n";
			temp = this.IdRegistro                     == null ? "\"NULL\"" : "\"" + this.IdRegistro.toString() + "\""                     ;
			st += "\"IdRegistro\":"                     + temp + ",\n";
			temp = this.FolioRegistro                  == null ? "\"NULL\"" : "\"" + this.FolioRegistro.toString() + "\""                  ;
			st += "\"FolioRegistro\":"                  + temp + ",\n";
			temp = this.IdConcepto                     == null ? "\"NULL\"" : "\"" + this.IdConcepto.toString() + "\""                     ;
			st += "\"IdConcepto\":"                     + temp + ",\n";
			temp = this.RequiereValorizacion           == null ? "\"NULL\"" : "\"" + this.RequiereValorizacion.toString() + "\""           ;
			st += "\"RequiereValorizacion\":"           + temp + ",\n";
			temp = this.IdRegimenFondoMutuo           	== null ? "\"NULL\"" : "\"" + this.IdRegimenFondoMutuo.toString() + "\""           ;
			st += "\"IdRegimenFondoMutuo\":"           + temp + ",\n";
			temp = this.ValorizarFlujo                 == null ? "\"NULL\"" : "\"" + this.ValorizarFlujo.toString() + "\""                 ;
			st += "\"ValorizarFlujo\":"                 + temp + "}";
			return st;
		}
	}
	
	public static class DatosFlujoCustodia2 {
		public Long	IdDatosFlujoCustodia;
		public Long	KeyFlujoCustodia;
		public Long	IdUsoFlujoCustodia;
		
		public String	UsoTipoOperacion;
		public String	BusquedaEspecifica;
		public String	ItemCalculo;
		public String	ItemOrigen;
		
		public String	FechaEjecucion;
		
		public Long	IdCliente;
		public Long	IdPortafolio;
		public String	EsCuentaPropia;
		public Long	IdRegistro;
		public Long	FolioRegistro;
		public String	IdConcepto;
		public String	IdClasificacionTipoInstrumento;
		public String	IdTipoInstrumento;
		public Long	IdInstrumento;
		public String	TipoValorizacion;
		public String	IdEmisor;
		public String	Nemotecnico;
		public String	NemoSVS;
		public String	IdMonedaInstrumento;
		public String	IdTipoReajuste;
		public java.sql.Date	FechaEmisionInstrumento;
		public java.sql.Date	FechaVencimientoInstrumento;
		public String	CodigoIFRS;
		public String	EsFungible;
		public String	TipoCarteraCorredor;
		public String	IdBoveda;
		public String	IdTipoInstrumentoEspecifico;
		public String	IdBolsa;
		public String	IdTipoOperacion;
		public String	Accion;
		public String	IdTipoProducto;
		public Long	IdMesa;
		public java.sql.Date	FechaLiquidacion;
		
		public BigDecimal	Cantidad;
		public BigDecimal	ValorRescate;
		public BigDecimal	PrecioCompra;
		public BigDecimal	TasaCompra;
		public BigDecimal	MontoATasaCompra;
		public BigDecimal	PrecioMercado;
		public BigDecimal	TasaMercado;
		public BigDecimal	MontoATasaMercado;
		public BigDecimal	PrecioVector;
		public BigDecimal	MontoVector;
		
		public BigDecimal	CantidadProceso;
		public BigDecimal	ValorRescateProceso;
		public BigDecimal	PrecioCompraProceso;
		public BigDecimal	TasaCompraProceso;
		public BigDecimal	MontoCompraProceso;
		public BigDecimal	MontoCompraMonedaLocalProceso;
		public BigDecimal	PrecioMercadoProceso;
		public BigDecimal	TasaMercadoProceso;
		public BigDecimal	MontoMercadoProceso;	
		public BigDecimal	PrecioVectorProceso;
		public BigDecimal	MontoVectorProceso;	
		public java.sql.Date	FechaLiquidacionProceso;
		public String	RequiereValorizacion;
		public String	TipoRegistro;
		public String	MotivoEliminacioRegistro;
		
		public String	ValorizarFlujo;
		public String	VALTipoCartera;
		public String	VALUsoCriterio;
		public String	VALIdTipoCriterioTasa;
		public BigDecimal	VALPrecioCompra;
		public BigDecimal	VALTasaCompra;
		public BigDecimal	VALMontoATasaCompra;	
		public BigDecimal	VALPrecioMercado;
		public BigDecimal	VALTasaMercado;
		public BigDecimal	VALMontoATasaMercado;	
		public BigDecimal	VALPrecioVector;
		public BigDecimal	VALMontoVector;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosProcesadosFlujo {
		public java.sql.Date		FechaSaldo; 
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public Long					FolioRegistro = 0L;
		public String				IdTipoOperacion = "";
		public String				Accion = "";
		public String				IdMonedaInstrumento = "";
		public String				IdMonedaEmision;
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				Nemotecnico = "";
		public String				NemoSVS = "";
		public BigDecimal			CorteMinimo;
		public String				IdEmisor; 
		public java.sql.Date		FechaEmisionInstrumento; 
		public java.sql.Date		FechaVencimientoInstrumento; 
		public java.sql.Date		FechaLiquidacionInstrumento; 
		public String				IdTipoReajuste; 
		public String				EsFungible; 
		public String				IdBoveda; 
		public String				IdBolsa = "";
		public String				CodigoIFRS = "";
		public Long					IdMesa = 0L;
		public Long					IdUsoFlujoCustodia; 
		public String				UsoTipoOperacion; 
		public String				BusquedaEspecifica; 
		public String				ItemOrigen; 
		public String				ItemCalculo; 
		public BigDecimal			CantidadProceso = new BigDecimal(0);
		public BigDecimal			ValorRescateProceso = new BigDecimal(0);
		public BigDecimal			PrecioCompraProceso = new BigDecimal(0);
		public BigDecimal			TasaCompraProceso = new BigDecimal(0);
		public BigDecimal			MontoCompraProcesoMonedaSistema = new BigDecimal(0);
		public BigDecimal			MontoCompraProcesoMonedaInstrumento = new BigDecimal(0);
		public BigDecimal			PrecioMercadoProceso = new BigDecimal(0);
		public BigDecimal			TasaMercadoProceso = new BigDecimal(0);
		public BigDecimal			MontoMercadoProcesoMonedaSistema = new BigDecimal(0);
		public BigDecimal			MontoMercadoProcesoMonedaInstrumento = new BigDecimal(0);
		public BigDecimal			PrecioVectorProceso = new BigDecimal(0);
		public BigDecimal			MontoVectorProceso = new BigDecimal(0);
		public Long					CantidadItem; 
		public Long					SItemCalculo;
		public String				hashSItemCalculo;
		public Long					SItemOrigen; 
		public String				hashSItemOrigen;
		public Long					SREG;
		public String				hashSREG; 
		public BigDecimal			MontoCompraMonedaLocalProceso = new BigDecimal(0);
		
		public String				Fecha;
		public String				ConsiderarRegistro;
		
		public String				IdRegimenFondoMutuo;
		public String				GrupoRegimen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera {
		public Long					IdCartera; 
		public Long					FolioCartera; 
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public String				IdConcepto; 
		public String				IdTipoOperacion; 
		public Long					IdMesa; 
		public String				IdBolsa; 
		public java.sql.Date		FechaLiquidacion; 
		public String				TipoCarteraCorredor;	
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				Nemotecnico; 
		public String				IdEmisor; 
		public String				NemoSVS; 
		public String				IdTipoReajuste; 
		public java.sql.Date		FechaEmisionInstrumento; 
		public java.sql.Date		FechaVencimientoInstrumento; 
		public String				EsFungible; 
		public String				CodigoIFRS; 
		public String				IdBoveda; 
		public String				IdMonedaInstrumento; 
		public java.sql.Date		INGFechaLiquidacion; 
		public BigDecimal			INGCantidad; 
		public BigDecimal			INGValorRescate; 
		public BigDecimal			INGPrecioCompra; 
		public BigDecimal			INGTasaCompra; 
		public BigDecimal			INGMontoCompraActualMonedaInstrumento; 
		public BigDecimal			INGPrecioMercadoActual; 
		public BigDecimal			INGTasaMercadoActual; 
		public BigDecimal			INGMontoMercadoActualMonedaInstrumento; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafoliosCuentaPropia {
		public Long IdPortafolio;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoInstrumentoParaGarantia {
		public String IdTipoInstrumentoAval;
		public String IdTipoInstrumentoGarantizado;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public HashMap<String, TipoInstrumentoParaGarantia> obtenerTipoInstrumentoParaGarantia(List<TipoInstrumentoParaGarantia> list) {
			HashMap<String, TipoInstrumentoParaGarantia> map = new HashMap<String, TipoInstrumentoParaGarantia>();
			for (TipoInstrumentoParaGarantia item : list) {
				String hash = item.IdTipoInstrumentoAval+"#"+item.IdTipoInstrumentoGarantizado;
				if (!map.containsKey(hash)) {
					map.put(hash, item);
				}
			}
			return map;
		}
	}
	
	
	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		FlujoCustodiaEntrada in = gson.fromJson(jin.toString(), FlujoCustodiaEntrada.class);
		FlujoCustodiaSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	
	/*
	 * Este servicio optimiza las llamadas internas para java dado que no hay necesidad de usar JSON
	 * 
	 * Estrategias a la hora de implementar:
	 * 
	 * - Obtener datos masivamente de la base datos - procesar logica de forma unitaria - Enviar datos masivamente a la base datos con batch
	 * 
	 * 
	 */
	@Trace
	public FlujoCustodiaSalida logica(Connection con, FlujoCustodiaEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		SQL sql = new SQL();
		Gson gson = Utils.getGsonBuilder();
		JsonParser parser = new JsonParser();
		FlujoCustodiaSalida out = new FlujoCustodiaSalida();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		logger.info("Inicio servicio ConsultarFlujoCustodia");
		if (in.p_BarraProgreso == null) {
            // No habilitado para servicio principal
            
        } else {
            in.p_BarraProgreso.actualizar(1L, "PROCESANDO", "Inicio flujo custodia");
        }
		List<MatrizUso> matrizUso = new ArrayList<MatrizUso>();
		try {
			if (in.p_Uso.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SIN_INFORMAR_USO_FLUJO_CUSTODIA",null,con,in.p_IdEmpresa));
				{
					if(in.p_BarraProgreso != null) {
						JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
						in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
					}
				}
				return out;
			}
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String agregarSimultaneasEventoCapitalCli = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "CUS.AgregarSimultaneasEventoCapitalCli");
			
			java.sql.Date dateFechaSistema = Fecha.toDate(fechaSistema);
			//variables
			java.sql.Date fechaOperacionMinima = Fecha.toDate("0001-01-01");
			java.sql.Timestamp fechaEjecucionTimestamp = Fecha.toTimeStamp(in.p_FechaSaldo + " 00:00:00");
			
			long startTime = System.nanoTime();
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
			Long idPaisEmpresa = in.p_IdPais == -1 ? empresa.IdPais : in.p_IdPais;
			
			List<FlujoFiltrosBusqueda> filtrosBusqueda = in.p_FiltrosBusqueda == null || in.p_FiltrosBusqueda.size() == 0 ? new ArrayList<FlujoFiltrosBusqueda>() : in.p_FiltrosBusqueda;
			List<FlujoEsAgrupadoPor> esAgrupadoPor = in.p_EsAgrupadoPor == null || in.p_EsAgrupadoPor.size() == 0 ? new ArrayList<FlujoEsAgrupadoPor>() : in.p_EsAgrupadoPor;
			
			List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::FlujoCustodia", new Long(1));
			out.p_out_KeyFlujoCustodia = secuencias.get(0);
			
			ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil = in.p_DiaHabil;
			inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
			
			for(int i = 1; i <= in.p_DiasProyeccion; i++) {
				FechasIn f = new FechasIn();
				f.fechaInicial = in.p_FechaSaldo;
				f.numeroDias = new Long(i);
				inObtenerDiaSiguiente.p_fechas.add(f);
			}
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(con, inObtenerDiaSiguiente);
			HashMap<String,Object> hmFechas = Utils.List2Hash(outObtenerDiaSiguiente.p_out_fechas, "fechaCalculada");
			
			HashMap<String, String> fechasCalculadasHash = new HashMap<String, String>();
			fechasCalculadasHash.put(in.p_FechaSaldo, in.p_FechaSaldo);
			List<String> fechasCalculadas = new ArrayList<String>();
			fechasCalculadas.add(in.p_FechaSaldo);
			for (FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) {
				if (!fechasCalculadasHash.containsKey(fecha.fechaCalculada)) {
					fechasCalculadasHash.put(fecha.fechaCalculada, fecha.fechaCalculada);
					fechasCalculadas.add(fecha.fechaCalculada);
				}
			}
			String fechaMinima = Collections.min(fechasCalculadas);
			String fechaMaxima = Collections.max(fechasCalculadas);			
			Date fechaMinimaDate = Fecha.toDate(fechaMinima);
			
			
			
			//SQL_SELECT_PORTAFOLIOS_CUENTA_PROPIA
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(10L,"PROCESANDO","Obteniendo datos flujo custodia");
				
			}
			logger.debug("Inicio SQL_SELECT_PORTAFOLIOS_CUENTA_PROPIA");
			startTime = System.nanoTime();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", empresa.IdEmpresa);
			List<PortafoliosCuentaPropia> portafoliosCuentaPropia = sql.query(con, SQL_SELECT_PORTAFOLIOS_CUENTA_PROPIA, params, PortafoliosCuentaPropia.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_PORTAFOLIOS_CUENTA_PROPIA");
			
			HashMap<Long, PortafoliosCuentaPropia> hashPortafoliosCuentaPropia = new HashMap<Long, PortafoliosCuentaPropia>();
			for (PortafoliosCuentaPropia item : portafoliosCuentaPropia ) {
				hashPortafoliosCuentaPropia.put(item.IdPortafolio, item);
			}
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(15L,"PROCESANDO","Obteniendo datos flujo custodia");
				
			}
			//SQL_SELECT_TIPO_INSTRUMENTO_PARA_GARANTIA
			logger.debug("Inicio SQL_SELECT_TIPO_INSTRUMENTO_PARA_GARANTIA");
			startTime = System.nanoTime();
			params.clear();
			List<TipoInstrumentoParaGarantia> tipoInstrumentoParaGarantia = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO_PARA_GARANTIA, params, TipoInstrumentoParaGarantia.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_TIPO_INSTRUMENTO_PARA_GARANTIA");
			
			HashMap<String, TipoInstrumentoParaGarantia> hashTipoInstrumentoParaGarantia = new HashMap<String, TipoInstrumentoParaGarantia>();
			for (TipoInstrumentoParaGarantia item : tipoInstrumentoParaGarantia ) {
				hashTipoInstrumentoParaGarantia.put(item.IdTipoInstrumentoAval + "#" + item.IdTipoInstrumentoGarantizado, item);
			}
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(20L,"PROCESANDO","Obteniendo datos flujo custodia");
				
			}
			logger.debug("Inicio servicio ConsultarDataFlujoCustodia");
			startTime = System.nanoTime();
			List<ConfiguracionUsoFlujoCustodia> usoFlujoCustodia = ConfiguracionUsoFlujoCustodia.obtenerUsoFlujoCustodia(con, in.p_IdEmpresa);
			List<ConfiguracionUsoFlujoCustodia> itemsFlujoCustodia = ConfiguracionUsoFlujoCustodia.obtenerItemsFlujoCustodia(con, in.p_IdEmpresa);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin servicio ConsultarDataFlujoCustodia");
			
			logger.debug("Inicio Loop Similitudes");
			startTime = System.nanoTime();
			List<FlujoUsoFiltrosBusqueda> flujoUsoFiltrosBusqueda = new ArrayList<FlujoUsoFiltrosBusqueda>();
			List<String> usoTipoOperacionList = new ArrayList<String>();
			HashMap<String, Boolean> usoTipoOperacionHash = new HashMap<String, Boolean>();
			
			//diff p uso
			List<FlujoUso> diff_p_Uso = in.p_Uso.parallelStream().distinct().collect(Collectors.toList());
			filtrosBusqueda = filtrosBusqueda.parallelStream().distinct().collect(Collectors.toList());
			//diff p_FiltrosBusqueda
			
			for (FlujoUso fu : diff_p_Uso) {
				for (FlujoFiltrosBusqueda ffb : filtrosBusqueda) {//no se quita ciclo for: hace cruce completo
					FlujoUsoFiltrosBusqueda fufb = copyPojoFlujoUsoFiltrosBusqueda(fu, ffb);
					flujoUsoFiltrosBusqueda.add(fufb);
					
					if (!usoTipoOperacionHash.containsKey(fufb.UsoTipoOperacion)) {
						usoTipoOperacionHash.put(fufb.UsoTipoOperacion, true);
						usoTipoOperacionList.add(fufb.UsoTipoOperacion);
					}
				}
			}
			
			//Filtra la lista usoFlujoCustodia con los UsoTipoOperacion de la lista flujoUsoFiltrosBusqueda
			List<ConfiguracionUsoFlujoCustodia> usoFlujoCustodiaFilter = usoFlujoCustodia.stream().filter(u -> usoTipoOperacionList.contains(u.UsoTipoOperacion)).collect(Collectors.toList());
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(30L,"PROCESANDO","Obteniendo configuracion flujo custodia");
				
			}
			HashMap<String,MatrizUso> matrizUsoHash = new HashMap<String,MatrizUso>();
			for (FlujoUsoFiltrosBusqueda u : flujoUsoFiltrosBusqueda) {
				for (ConfiguracionUsoFlujoCustodia ufc : usoFlujoCustodiaFilter) {//no se quita ciclo for: no hay llave entre la lista flujoUsoFiltrosBusqueda y usoFlujoCustodiaFilter
					if((ufc.UsoTipoOperacion+"").equals(u.UsoTipoOperacion+"")) {
						MatrizUso ma = copyPojoFlujoUsoFiltrosBusquedaMatrizUso(u);
						ma.IdUsoFlujoCustodia = ufc.IdUsoFlujoCustodia;
						ma.IdEstado = "ACT";
						ma.IdEmpresa = empresa.IdEmpresa;
						ma.ItemCalculo = ufc.ItemCalculo;
						ma.ItemOrigen = ufc.ItemOrigen;
						
						int MayorSimilitud = 2;
						if (u.UsoTipoOperacion.equals("")) {
							MayorSimilitud = 1;
						}
						
						if ((ufc.IdClasificacionTipoInstrumento+"").equals(u.IdClasificacionTipoInstrumento+"")) {
							MayorSimilitud += 2;
						} else if ((u.IdClasificacionTipoInstrumento+"").equals("")){
							MayorSimilitud += 1;
						}
						
						ufc.IdTipoInstrumento = ufc.IdTipoInstrumento == null ? "" : ufc.IdTipoInstrumento;
						if (ufc.IdTipoInstrumento.equals(u.IdTipoInstrumento+"")) {
							MayorSimilitud += 2;
						} else if ((u.IdTipoInstrumento+"").equals("")){
							MayorSimilitud += 1;
						}
						
						if ((ufc.IdInstrumento == null && u.IdInstrumento == null) || (ufc.IdInstrumento.equals(u.IdInstrumento))) {
							MayorSimilitud += 2;
						} else if (u.IdInstrumento == null || u.IdInstrumento == 0){
							MayorSimilitud += 1;
						}
						
						if ((ufc.IdCliente == null && u.IdCliente == null) || (ufc.IdCliente.equals(u.IdCliente))) {
							MayorSimilitud += 2;
						} else if (u.IdCliente == null || u.IdCliente == 0){
							MayorSimilitud += 1;
						}
						
						if ((ufc.IdPortafolio == null && u.IdPortafolio == null) || (ufc.IdPortafolio.equals(u.IdPortafolio))) {
							MayorSimilitud += 2;
						} else if (u.IdPortafolio == null || u.IdPortafolio == 0){
							MayorSimilitud += 1;
						}
						
						if ((ufc.BusquedaEspecifica+"").equals(u.BusquedaEspecifica+"")) {
							MayorSimilitud += 2;
						} else if (u.BusquedaEspecifica.equals("")){
							MayorSimilitud += 1;
						}
						
						ma.MayorSimilitud = MayorSimilitud;
						String hash = u.IdClasificacionTipoInstrumento + "||" + u.IdTipoInstrumento + "||" + u.IdInstrumento + "||" + u.UsoTipoOperacion + "||" + u.IdCliente + "||" + u.IdPortafolio + "||" + u.BusquedaEspecifica + "||" + u.IdTipoInstrumentoEspecifico+"" + "||" + u.IdRegimenFondoMutuo+"" + "||" + u.IdEmisor+"";
						
						MatrizUso mah = matrizUsoHash.get(hash);
						if (mah == null || mah.MayorSimilitud < MayorSimilitud) {
							matrizUsoHash.put(hash,ma);
						}
					}
				}
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Loop Similitudes");
			
			logger.debug("Inicio Loop Hash");
			startTime = System.nanoTime();
			List<MatrizUso> tablaBase = new ArrayList<MatrizUso>();
			HashMap<String, MatrizUso> tablaBaseHash = new HashMap<String, MatrizUso>();
			HashMap<String, ConfiguracionUsoFlujoCustodia> hashItemsFlujoCustodia = new HashMap<String, ConfiguracionUsoFlujoCustodia>();
			HashMap<String, String> hashFlujoCustodiaItems = new HashMap<String, String>();
			HashMap<String, Boolean> hashItems = new HashMap<String, Boolean>();
			Set<String> hashItemsSaldos = new HashSet<String>();
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(35L,"PROCESANDO","Obteniendo configuracion flujo custodia");
				
			}
			for (Entry<String, MatrizUso> entry : matrizUsoHash.entrySet()) {
				MatrizUso mu = entry.getValue();
				mu.IdTrabajo = idTrabajo;
				matrizUso.add(mu);
				
				for (ConfiguracionUsoFlujoCustodia ifc : itemsFlujoCustodia) {					
					hashItemsFlujoCustodia.put(ifc.IdUsoFlujoCustodia + "#" +ifc.ItemCalculo, ifc);
					
					if (mu.IdUsoFlujoCustodia.equals(ifc.IdUsoFlujoCustodia)) {
						MatrizUso tb = copyPojoMatrizUsoMatrizUso(mu);
						tb.ItemCalculo = ifc.ItemCalculo;
						tb.ItemOrigen = ifc.ItemOrigen;
						tb.FuncionOperador = ifc.FuncionOperador;
						
						String hash = tb.IdCliente + "#" + tb.IdPortafolio + "#"
								+ tb.UsoTipoOperacion + "#" + tb.IdClasificacionTipoInstrumento + "#"
								+ tb.IdTipoInstrumento + "#" + tb.IdInstrumento + "#" + tb.IdEmisor + "#"
								+ tb.FechaVencimiento + "#" + tb.IdTipoReajuste + "#" + tb.EsFungible + "#"
								+ tb.IdBoveda + "#" + tb.IdTipoInstrumentoEspecifico + "#" + tb.IdUsoFlujoCustodia + "#"
								+ tb.BusquedaEspecifica + "#" + tb.IdRegimenFondoMutuo + "#" + tb.IdEstado + "#"
								+ tb.IdEmpresa + "#" + tb.ItemCalculo + "#" + tb.ItemOrigen + "#" + tb.FuncionOperador + "#" + tb.MayorSimilitud;
						if (!tablaBaseHash.containsKey(hash)) {
							tablaBaseHash.put(hash, tb);
							tablaBase.add(tb);
						}
						
						hashFlujoCustodiaItems.put(ifc.ItemCalculo, ifc.ItemOrigen);
						if (valoresItemCalculoHash.get(ifc.ItemCalculo) != null && !hashItems.containsKey(valoresItemCalculoHash.get(ifc.ItemCalculo))) {
							hashItems.put(valoresItemCalculoHash.get(ifc.ItemCalculo), true);
							hashItemsSaldos.add(valoresItemCalculoHash.get(ifc.ItemCalculo));
						}
					}
				}
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Loop Hash");
			
			logger.debug("Inicio Secuencia - Iterator");
			startTime = System.nanoTime();
			List<Long> secuenciasDatosFlujoCustodia = LongStream.rangeClosed(0, tablaBase.size()).boxed().collect(Collectors.toList());
			Iterator<Long> iteratorDatosFlujoCustodia = secuenciasDatosFlujoCustodia.iterator();
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Secuencia - Iterator");
			
			logger.debug("Inicio listaDatosFlujoCustodia.add");
			startTime = System.nanoTime();
			List<DatosFlujoCustodia> listaDatosFlujoCustodia = new ArrayList<DatosFlujoCustodia>();
			for (MatrizUso tb : tablaBase) {
				DatosFlujoCustodia dfc = copyPojoMatrizUsoDatosFlujoCustodia(tb);
				dfc.IdDatosFlujoCustodia = iteratorDatosFlujoCustodia.next();
				dfc.KeyFlujoCustodia = out.p_out_KeyFlujoCustodia;
				dfc.FechaEjecucion = Fecha.toTimeStamp(in.p_FechaSaldo + " 00:00:00");
				dfc.TipoRegistro = "M";
				dfc.ValorizarFlujo = in.p_ValorizarFlujo;
				listaDatosFlujoCustodia.add(dfc);
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin listaDatosFlujoCustodia.add");
			
			sql.batch(con, SQL_INSERT_INSERTFLUJOCUSTODIA_MATRIZUSO, matrizUso, MatrizUso.class);	
			
			logger.debug("Inicio listaDatosFlujoCustodia.addAll");
			java.sql.Date dateFechaMaxima = Fecha.toDate(fechaMaxima);
			startTime = System.nanoTime();
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FechaMaxima", dateFechaMaxima);
			params.put("IdTrabajo", idTrabajo);
			logger.debug("Inicio Loop Select:" + hashItemsSaldos.size());
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(45L,"PROCESANDO","Consultando flujo custodia historico");
				
			}
			
			String simultaneasEVC = matrizUsoHash.keySet().stream()
					.anyMatch(key -> key.contains("FLUJO_CLIENTES_DERECHO_CAPITAL")) ? "S" : "N";
				
			for (String item : hashItemsSaldos) {
				switch (item) {
				case "CARTERA": //SQL_SELECT_CARTERAS
					List<DatosFlujoCustodia> carteras = new ArrayList<DatosFlujoCustodia>();
					params.put("SimultaneaEVC", simultaneasEVC);
					if(dateFechaMaxima.compareTo(dateFechaSistema) == -1) {
						logger.debug("SELECT CARTERA HISTORICO:" + params.size());
						logger.debug("INICIO::SQL_SELECT_CARTERAS_HISTORICO");
						carteras = sql.query(con, SQL_SELECT_CARTERAS_HISTORICO, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_CARTERAS_HISTORICO");
					}else {
						logger.debug("SELECT CARTERA");
						carteras = queryCarteras(con,matrizUso.get(0),params);
						logger.debug("END SELECT CARTERA:" + carteras.size());
					}
					for (DatosFlujoCustodia rec : carteras) {
						rec.ItemCalculo = "INFORMACION_CARTERA";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("INFORMACION_CARTERA");
					}
					params.remove("SimultaneaEVC");
					listaDatosFlujoCustodia.addAll(carteras);
					break;
				case "CARTERA_TRANSITO": //SQL_SELECT_MOVIMIENTOS_FACTURACION
					List<DatosFlujoCustodia> carterasTransito = new ArrayList<DatosFlujoCustodia>();
					if(dateFechaMaxima.compareTo(dateFechaSistema) == -1) {
						logger.debug("SELECT CARTERA TRANSITO HISTORICO"+ params.size());
						logger.debug("INICIO::SQL_SELECT_CARTERAS_TRANSITO_HISTORICO");
						carterasTransito = sql.query(con, SQL_SELECT_CARTERAS_TRANSITO_HISTORICO, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_CARTERAS_TRANSITO_HISTORICO");
					}else {
						logger.debug("SELECT CARTERA TRANSITO"+ params.size());
						logger.debug("INICIO::SQL_SELECT_CARTERAS_TRANSITO");
						carterasTransito = sql.query(con, SQL_SELECT_CARTERAS_TRANSITO, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_CARTERAS_TRANSITO");
					}
					for (DatosFlujoCustodia rec : carterasTransito) {
						rec.ItemCalculo = "CARTERA_TRANSITO_" + rec.Accion;//Accion : COMPRA o VENTA
						rec.ItemOrigen = hashFlujoCustodiaItems.get("CARTERA_TRANSITO_" + rec.Accion);
					}
					listaDatosFlujoCustodia.addAll(carterasTransito);
				break;
				
				case "TRANSITO": //SQL_SELECT_TRANSITO
					List<DatosFlujoCustodia> transito = new ArrayList<DatosFlujoCustodia>();
					if(dateFechaMaxima.compareTo(dateFechaSistema) == -1) {
						logger.debug("SELECT TRANSITO HISTORICO"+ params.size());
						logger.debug("INICIO::SQL_SELECT_TRANSITO_HISTORICO");
						transito = sql.query(con, SQL_SELECT_TRANSITO_HISTORICO, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_TRANSITO_HISTORICO");
					}else {
						logger.debug("SELECT TRANSITO"+ params.size());
						logger.debug("INICIO::SQL_SELECT_TRANSITO");
						transito = sql.query(con, SQL_SELECT_TRANSITO, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_TRANSITO");
					}
					for (DatosFlujoCustodia rec : transito) {
						if ("S".equalsIgnoreCase(rec.EsVentaDescubierta) && rec.IdAccionOperacion.equalsIgnoreCase("RET")) {
							rec.ItemCalculo = "MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA";
							rec.ItemOrigen = hashFlujoCustodiaItems.get("MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA");
						}
						else {
							String nombreAccionOperacion = "ING".equals(rec.IdAccionOperacion) ? "_INGRESO" : "RET".equals(rec.IdAccionOperacion) ? "_RETIRO" : "";
							rec.ItemCalculo = "INFORMACION_TRANSITO" + nombreAccionOperacion;
							rec.ItemOrigen = hashFlujoCustodiaItems.get("INFORMACION_TRANSITO" + nombreAccionOperacion);
						}
					}
					listaDatosFlujoCustodia.addAll(transito);
					break;
				case "GARANTIA": //SQL_SELECT_TRANSITO
					List<DatosFlujoCustodia> garantia = new ArrayList<DatosFlujoCustodia>();
					if(dateFechaMaxima.compareTo(dateFechaSistema) == -1) {
						logger.debug("SELECT GARANTIA HISTORICO"+ params.size());
						logger.debug("INICIO::SQL_SELECT_GARANTIA_HISTORICO");
						garantia = sql.query(con, SQL_SELECT_GARANTIA_HISTORICO, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_GARANTIA_HISTORICO");
					}else {
						logger.debug("SELECT GARANTIA"+ params.size());
						logger.debug("INICIO::SQL_SELECT_GARANTIA");
						garantia = sql.query(con, SQL_SELECT_GARANTIA, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_GARANTIA");
					}
					for (DatosFlujoCustodia rec : garantia) {
						rec.ItemCalculo = "INFORMACION_GARANTIA";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("INFORMACION_GARANTIA");
					}

					listaDatosFlujoCustodia.addAll(garantia);
					break;
				case "SUBYACENTE": //SQL_SELECT_SUBYACENTE
				HashMap<String,Object> paramsSubyacente = new HashMap<String,Object>();
				HashMap<String,Object> paramsSubyacenteHis = new HashMap<String,Object>();
				
				if (("FLUJO_CLIENTES_DERECHO_CAPITAL").equalsIgnoreCase(matrizUso.get(0).BusquedaEspecifica)) {
					paramsSubyacente.put("idsTipoOperacion", tipoOperacionEvtoCapital);
					paramsSubyacenteHis.put("idsTipoOperacion", tipoOperacionEvtoCapital);
				}else {
					paramsSubyacente.put("idsTipoOperacion", tipoOperacion);
					paramsSubyacenteHis.put("idsTipoOperacion", tipoOperacion);
				}
				
				paramsSubyacente.put("IdEmpresa", params.get("IdEmpresa"));
				paramsSubyacente.put("IdTrabajo", params.get("IdTrabajo"));
				paramsSubyacenteHis.put("IdEmpresa", params.get("IdEmpresa"));
				paramsSubyacenteHis.put("IdTrabajo", params.get("IdTrabajo"));
				paramsSubyacente.put("FechaSaldo", in.p_FechaSaldo);
				paramsSubyacenteHis.put("FechaMaxima", dateFechaMaxima);
				String texto = "";
				
				List<DatosFlujoCustodia> subyacente = new ArrayList<DatosFlujoCustodia>();
				  if ("S".equalsIgnoreCase(agregarSimultaneasEventoCapitalCli)){
					  if(dateFechaMaxima.compareTo(dateFechaSistema) == -1) {
						  logger.debug("SELECT SUBYACENTE HISTORICO"+ params.size());
						  logger.debug("INICIO::SQL_SELECT_SUBYACENTE_HISTORICO");
						  subyacente = sql.query(con, SQL_SELECT_SUBYACENTE_HISTORICO, paramsSubyacenteHis, DatosFlujoCustodia.class);
						  logger.debug("FIN::SQL_SELECT_SUBYACENTE_HISTORICO");
						}else {
							logger.debug("SELECT SUBYACENTE"+ params.size());
							logger.debug("INICIO::SQL_SELECT_SUBYACENTE");
							subyacente = sql.query(con, SQL_SELECT_SUBYACENTE, paramsSubyacente, DatosFlujoCustodia.class);
							logger.debug("FIN::SQL_SELECT_SUBYACENTE");
						}
						
						for (DatosFlujoCustodia rec : subyacente) {
							switch (rec.IdTipoOperacion) {
								case "COMSIM": texto = "SIMULTANEA_COMPRA"; break;
								case "VENSIM": texto = "SIMULTANEA_VENTA"; break;
								case "COMRRV":
								case "COMRRF": texto = "REPORTE_COMPRA"; break;
								case "VENRRV":
								case "VENRRF": texto = "REPORTE_VENTA"; break;
								case "PRESTA": texto = "PRESTAMO"; break;
								case "EMPRES": texto = "EMPRESTITO"; break;
								case "COMPAC": texto = "COMPRA_PACTO"; break;
								case "VENPAC": texto = "VENTA_PACTO"; break;
							}
							rec.ItemCalculo = texto;
							rec.ItemOrigen = hashFlujoCustodiaItems.get(texto);
						}
						listaDatosFlujoCustodia.addAll(subyacente);
						break;
				  }
					
				case "CARTERA_RESERVADA": //SQL_SELECT_CARTERA_RESERVADA
					List<DatosFlujoCustodia> carteraReservada = new ArrayList<DatosFlujoCustodia>();
					if(dateFechaMaxima.compareTo(dateFechaSistema) == -1) {
						logger.debug("SELECT CARTERA RESERVADA HISTORICO"+ params.size());
						logger.debug("INICIO::SQL_SELECT_CARTERA_RESERVADA_HISTORICO");
						carteraReservada = sql.query(con, SQL_SELECT_CARTERA_RESERVADA_HISTORICO, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_CARTERA_RESERVADA_HISTORICO");
					}else {
						logger.debug("SELECT CARTERA RESERVADA"+ params.size());
						logger.debug("INICIO::SQL_SELECT_CARTERA_RESERVADA");
						carteraReservada = sql.query(con, SQL_SELECT_CARTERA_RESERVADA, params, DatosFlujoCustodia.class);
						logger.debug("FIN::SQL_SELECT_CARTERA_RESERVADA");
					}
					texto = "";
					for (DatosFlujoCustodia rec : carteraReservada) {
						switch (rec.IdTipoOperacion) {
							case "COMPAC": texto = "RESERVA_COMPRA_PACTOS"; break;
							case "VENPAC": texto = "RESERVA_VENTA_PACTO"; break;
							case "COMTER": texto = "RESERVA_COMPRA_TERMINO"; break;
							case "VENTER": texto = "RESERVA_VENTA_TERMINO"; break;
						}
						rec.ItemCalculo = texto;
						rec.ItemOrigen = hashFlujoCustodiaItems.get(texto);
					}
					listaDatosFlujoCustodia.addAll(carteraReservada);
					break;
				case "ORDENES_POR_AUTORIZAR": //SQL_SELECT_ORDENES_POR_AUTORIZAR
					logger.debug("SELECT ORDENES POR AUTORIZAR"+ params.size());
					logger.debug("INICIO::SQL_SELECT_ORDENES_POR_AUTORIZAR");
					List<DatosFlujoCustodia> ordenesPorAutorizar = sql.query(con, SQL_SELECT_ORDENES_POR_AUTORIZAR, params, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_ORDENES_POR_AUTORIZAR");
					for (DatosFlujoCustodia rec : ordenesPorAutorizar) {
						rec.ItemCalculo = "ORDENES_" + rec.Accion + "_PENDIENTES";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("ORDENES_" + rec.Accion + "_PENDIENTES");
					}
					listaDatosFlujoCustodia.addAll(ordenesPorAutorizar);
					break;
				case "ORDENES_INGRESADAS": //SQL_SELECT_ORDENES_INGRESADAS
					HashMap<String,Object> paramsOI = new HashMap<String,Object>();
					paramsOI.put("IdEmpresa", in.p_IdEmpresa);
					paramsOI.put("FechaMaxima", dateFechaMaxima);
					paramsOI.put("FechaMinima", fechaMinimaDate);
					paramsOI.put("IdTrabajo", idTrabajo);
					logger.debug("SELECT ORDENES INGRESADAS"+ params.size());
					logger.debug("INICIO::SQL_SELECT_ORDENES_INGRESADAS");
					List<DatosFlujoCustodia> ordenesIngresadas = sql.query(con, SQL_SELECT_ORDENES_INGRESADAS, paramsOI, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_ORDENES_INGRESADAS");
					for (DatosFlujoCustodia rec : ordenesIngresadas) {
						rec.ItemCalculo = "ORDENES_" + rec.Accion + "_INGRESADAS";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("ORDENES_" + rec.Accion + "_INGRESADAS");
					}
					listaDatosFlujoCustodia.addAll(ordenesIngresadas);
					break;
				case "ORDENES_REPORTE_POR_AUTORIZAR": //SQL_SELECT_ORDENES_REPORTE_POR_AUTORIZAR
					logger.debug("SELECT ORDENES REPORTE POR AUTORIZAR"+ params.size());
					logger.debug("INICIO::SQL_SELECT_ORDENES_REPORTE_POR_AUTORIZAR");
					List<DatosFlujoCustodia> ordenesReportePorAutorizar = sql.query(con, SQL_SELECT_ORDENES_REPORTE_POR_AUTORIZAR, params, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_ORDENES_REPORTE_POR_AUTORIZAR");
					for (DatosFlujoCustodia rec : ordenesReportePorAutorizar) {
						rec.ItemCalculo = "ORDENES_REPORTE_" + rec.Accion + "_PENDIENTES";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("ORDENES_REPORTE_" + rec.Accion + "_PENDIENTES");
					}
					listaDatosFlujoCustodia.addAll(ordenesReportePorAutorizar);
					break;
				case "ORDENES_REPORTE_INGRESADAS": //SQL_SELECT_ORDENES_REPORTE_INGRESADAS
					logger.debug("SELECT ORDENES REPORTE INGRESADAS"+ params.size());
					logger.debug("INICIO::SQL_SELECT_ORDENES_REPORTE_INGRESADAS");
					List<DatosFlujoCustodia> ordenesReporteIngresadas = sql.query(con, SQL_SELECT_ORDENES_REPORTE_INGRESADAS, params, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_ORDENES_REPORTE_INGRESADAS");
					for (DatosFlujoCustodia rec : ordenesReporteIngresadas) {
						rec.ItemCalculo = "ORDENES_REPORTE_" + rec.Accion + "_INGRESADAS";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("ORDENES_REPORTE_" + rec.Accion + "_INGRESADAS");
					}
					listaDatosFlujoCustodia.addAll(ordenesReporteIngresadas);
					break;
				case "ASIGNACIONES_NO_ENVIAS_A_FACTURAR": //SQL_SELECT_ASIGNACIONES_NO_ENVIADAS_A_FACTURAR
					logger.debug("SELECT ASIGNACIONES NO ENVIADAS A FACTURAR"+ params.size());
					logger.debug("INICIO::SQL_SELECT_ASIGNACIONES_NO_ENVIADAS_A_FACTURAR");
					List<DatosFlujoCustodia> asignacionesNoEnviadasAFacturar = sql.query(con, SQL_SELECT_ASIGNACIONES_NO_ENVIADAS_A_FACTURAR, params, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_ASIGNACIONES_NO_ENVIADAS_A_FACTURAR");
					for (DatosFlujoCustodia rec : asignacionesNoEnviadasAFacturar) {
						rec.ItemCalculo = "ASIGNACIONES_" + rec.Accion + "_PENDIENTE";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("ASIGNACIONES_" + rec.Accion + "_PENDIENTE");
					}
					listaDatosFlujoCustodia.addAll(asignacionesNoEnviadasAFacturar);
					break;
				case "ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR": //SQL_SELECT_ASIGNACIONES_REPORTE_NO_ENVIADAS_A_FACTURAR
					logger.debug("SELECT ASIGNACIONES REPORTE NO ENVIADAS A FACTURAR"+ params.size());
					logger.debug("INICIO::SQL_SELECT_ASIGNACIONES_REPORTE_NO_ENVIADAS_A_FACTURAR");
					List<DatosFlujoCustodia> asignacionesReporteNoEnviadasAFacturar = sql.query(con, SQL_SELECT_ASIGNACIONES_REPORTE_NO_ENVIADAS_A_FACTURAR, params, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_ASIGNACIONES_REPORTE_NO_ENVIADAS_A_FACTURAR");
					for (DatosFlujoCustodia rec : asignacionesReporteNoEnviadasAFacturar) {
						rec.ItemCalculo = "ASIGNACIONES_REPORTE_" + rec.Accion + "_PENDIENTE";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("ASIGNACIONES_REPORTE_" + rec.Accion + "_PENDIENTE");
					}
					listaDatosFlujoCustodia.addAll(asignacionesReporteNoEnviadasAFacturar);
					break;
				case "MOVIMIENTOS_FACTURACION": //SQL_SELECT_MOVIMIENTOS_FACTURACION
					logger.debug("SELECT MOVIMIENTOS FACTURAR"+ params.size());
					logger.debug("INICIO::SQL_SELECT_MOVIMIENTOS_FACTURACION");
					List<DatosFlujoCustodia> movimientosFacturacion = sql.query(con, SQL_SELECT_MOVIMIENTOS_FACTURACION, params, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_MOVIMIENTOS_FACTURACION");
					for (DatosFlujoCustodia rec : movimientosFacturacion) {
						rec.ItemCalculo = "MOVIMIENTOS_" + rec.Accion + "_FACTURACION";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("MOVIMIENTOS_" + rec.Accion + "_FACTURACION");
					}
					listaDatosFlujoCustodia.addAll(movimientosFacturacion);
					break;					
				case "MOVIMIENTOS_REPORTE_FACTURACION": //SQL_SELECT_MOVIMIENTOS_REPORTE_FACTURACION
					logger.debug("SELECT MOVIMIENTOS FACTURAR REPORTES"+ params.size());
					logger.debug("INICIO::SQL_SELECT_MOVIMIENTOS_REPORTE_FACTURACION");
					List<DatosFlujoCustodia> movimientosReporteFacturacion = sql.query(con, SQL_SELECT_MOVIMIENTOS_REPORTE_FACTURACION, params, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_MOVIMIENTOS_REPORTE_FACTURACION");
					for (DatosFlujoCustodia rec : movimientosReporteFacturacion) {
						rec.ItemCalculo = "MOVIMIENTOS_REPORTE_" + rec.Accion + "_FACTURACION";
						rec.ItemOrigen = hashFlujoCustodiaItems.get("MOVIMIENTOS_REPORTE_" + rec.Accion + "_FACTURACION");
					}
					listaDatosFlujoCustodia.addAll(movimientosReporteFacturacion);
					break;					
				case "EVENTOS_DE_CAPITAL": //SQL_SELECT_EVENTOS_DE_CAPITAL
					logger.debug("SELECT EVENTOS DE CAPITAL"+ params.size());
					logger.debug("INICIO::SQL_SELECT_EVENTOS_DE_CAPITAL");
					List<DatosFlujoCustodia> eventosDeCapital = sql.query(con, SQL_SELECT_EVENTOS_DE_CAPITAL, params, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_EVENTOS_DE_CAPITAL");
					texto = "";
					for (DatosFlujoCustodia rec : eventosDeCapital) {
						switch (rec.Accion) {
							case "SU": texto = "SUBSCRIPCIONES"; break;
							case "SL": texto = "SORTEO_LETRAS"; break;
							case "PB": texto = "PREPAGO_BONO"; break;
						}
						rec.ItemCalculo = texto;
						rec.ItemOrigen = texto;
					}
					listaDatosFlujoCustodia.addAll(eventosDeCapital);
					break;
				case "FONDOS_MUTUOS": //SQL_SELECT_FONDOS_MUTUOS
					
					HashMap<String,Object> paramsFFMM = new HashMap<String,Object>();
					paramsFFMM.put("IdEmpresa", in.p_IdEmpresa);
					paramsFFMM.put("FechaMaxima", dateFechaMaxima);
					paramsFFMM.put("FechaMinima", fechaMinimaDate);
					paramsFFMM.put("IdTrabajo", idTrabajo);
					logger.debug("SELECT FFMM"+ paramsFFMM.size());
					logger.debug("INICIO::SQL_SELECT_FONDOS_MUTUOS");
					List<DatosFlujoCustodia> fondosMutuos = sql.query(con, SQL_SELECT_FONDOS_MUTUOS, paramsFFMM, DatosFlujoCustodia.class);
					logger.debug("FIN::SQL_SELECT_FONDOS_MUTUOS");
					for (DatosFlujoCustodia rec : fondosMutuos) {
						texto = "APORFM".equals(rec.IdTipoOperacion) ? "APORTE_FONDOS_MUTUOS" : "RESCFM".equals(rec.IdTipoOperacion) ? "RESCATE_FONDOS_MUTUOS" : "";
						rec.ItemCalculo = texto;
						rec.ItemOrigen = "FONDOSMUTUOS";
					}
					listaDatosFlujoCustodia.addAll(fondosMutuos);
					break;				
				}
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin listaDatosFlujoCustodia.addAll");
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(55L,"PROCESANDO","Consultando flujo custodia ");
				
			}
			logger.debug("Inicio Loop descarte por garantia");
			startTime = System.nanoTime();
			Set<String> listaSetIntrumentosFlujo = new HashSet<String>();
			List<String> listaIntrumentosFlujo = new ArrayList<String>();
			for (DatosFlujoCustodia rec : listaDatosFlujoCustodia) {
				if (rec.TipoRegistro.equals("R") ){
					rec.FechaEjecucion = fechaEjecucionTimestamp;
					rec.KeyFlujoCustodia = out.p_out_KeyFlujoCustodia;
					rec.FechaLiquidacionProceso = rec.FechaLiquidacion.after(fechaMinimaDate) ? rec.FechaLiquidacion : fechaMinimaDate;
					rec.ValorizarFlujo = in.p_ValorizarFlujo;
					rec.EsCuentaPropia = hashPortafoliosCuentaPropia.containsKey(rec.IdPortafolio) ? "S" : "N";
					
					String hash = rec.IdUsoFlujoCustodia + "#" + rec.ItemCalculo;
					rec.FuncionOperador = hashItemsFlujoCustodia.get(hash) == null ? "" : hashItemsFlujoCustodia.get(hash).FuncionOperador;
					
					BigDecimal factor = rec.FuncionOperador.equals("-") ? new BigDecimal(-1) : new BigDecimal(1);
					rec.CantidadProceso = rec.Cantidad.multiply(factor);
					rec.ValorRescateProceso = rec.ValorRescate.multiply(factor);				
					rec.PrecioCompraProceso = rec.PrecioCompra;
					rec.TasaCompraProceso = rec.TasaCompra;
					rec.PrecioMercadoProceso = rec.PrecioMercado;
					rec.TasaMercadoProceso = rec.TasaMercado;
					
					String hashTIPG = rec.IdTipoInstrumento + "#" + rec.IdTipoInstrumentoEspecifico;
					TipoInstrumentoParaGarantia tipg = hashTipoInstrumentoParaGarantia.get(hashTIPG);
					
					if (rec.FuncionOperador.equals("")) {
						rec.TipoRegistro = "D";
						rec.MotivoEliminacioRegistro = "CONFIGURACION_SIN_FUNCION_OPERADOR";
					} else if (rec.TipoCarteraCorredor.equals("CC")) {
						rec.TipoRegistro = "D";
						rec.MotivoEliminacioRegistro = "SE_EXCLUYE_CARTERA_COMPROMETIDA";
					} else if (rec.UsoTipoOperacion == null && rec.BusquedaEspecifica.equals("")) {
						rec.TipoRegistro = "D";
						rec.MotivoEliminacioRegistro = "CONFIGURACION_TIPO_OPERACION_EXCLUYE_INSTRUMENTO";
					} else if ((tipg == null || tipg.IdTipoInstrumentoAval == null) && rec.BusquedaEspecifica != null && rec.BusquedaEspecifica.equals("SOLO_GARANTIZABLE") && !rec.UsoTipoOperacion.equals("INGGPR")) {
						rec.TipoRegistro = "D";
						rec.MotivoEliminacioRegistro = "CONFIGURACION_GARANTIA_EXCLUYE_INSTRUMENTO";
					} // Caso pendiente abajo
				}
				
				String hash = "";
				hash += rec.IdTipoInstrumentoEspecifico == null ? "NULL#" : rec.IdTipoInstrumentoEspecifico + "#";
				hash += rec.IdTipoInstrumento == null ? "NULL#" : rec.IdTipoInstrumento + "#";
				hash += rec.IdInstrumento == null ? "-1" : rec.IdInstrumento;
				listaSetIntrumentosFlujo.add(hash); // TODO Revisar
			}
			listaIntrumentosFlujo.addAll(listaSetIntrumentosFlujo);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Loop descarte por garantia");
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(65L,"PROCESANDO","Consultando flujo custodia proyectado");
				
			}
			//SQL_SELECT_INTRUMENTOS_FLUJO
			logger.debug("Inicio Select Instrumentos Subyacentes");
			startTime = System.nanoTime();
			params.clear();
			params.put("Instrumentos", listaIntrumentosFlujo);
			logger.debug("INICIO::SQL_SELECT_INTRUMENTOS_FLUJO");
			List<DatosFlujoCustodia> intrumentosSubyacentes = sql.query(con, SQL_SELECT_INTRUMENTOS_FLUJO, params, DatosFlujoCustodia.class);
			logger.debug("FIN::SQL_SELECT_INTRUMENTOS_FLUJO");
			List<Long> instrumentosSubyacentesDetectados = intrumentosSubyacentes.stream().map(a -> a.IdInstrumento).distinct().collect(Collectors.toList());
			int coincidencia = 0;
			if (instrumentosSubyacentesDetectados.size() > 0) {
				for (DatosFlujoCustodia rec : listaDatosFlujoCustodia) {
					for (Long instrumentoEncontado : instrumentosSubyacentesDetectados) {
						if (instrumentoEncontado.equals(rec.IdInstrumento)) {
							if ("R".equals(rec.TipoRegistro) && rec.IdInstrumento == null && "SOLO_SUBYACENTE".equals(rec.BusquedaEspecifica)) {
								rec.TipoRegistro = "D";
								rec.MotivoEliminacioRegistro = "CONFIGURACION_SUBYACENTE_EXCLUYE_INSTRUMENTO";
							}
							coincidencia++;
						}
					}
					if ("SOLO_SUBYACENTE".equals(rec.BusquedaEspecifica) && coincidencia == 0) {
						rec.TipoRegistro = "D";
						rec.MotivoEliminacioRegistro = "INSTRUMENTO_NO_SUBYACENTE";
					}
					coincidencia = 0;
				}
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Select Instrumentos Subyacentes");
						
			logger.debug("Inicio DATOS PROYECTADOS Secuencia - Iterator");
			startTime = System.nanoTime();
			secuenciasDatosFlujoCustodia = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia", new Long(listaDatosFlujoCustodia.size() * fechasCalculadas.size()));
			iteratorDatosFlujoCustodia = secuenciasDatosFlujoCustodia.iterator();
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin DATOS PROYECTADOS Secuencia - Iterator");
			
			List<DatosFlujoCustodia> listaDatosFlujoCustodiaProyectados = new ArrayList<DatosFlujoCustodia>();
			logger.debug("Inicio Datos Flujo Custodia Proyectados");
			startTime = System.nanoTime();
			for (DatosFlujoCustodia rec : listaDatosFlujoCustodia) {
				if (rec.TipoRegistro.equals("R")) {
					FlujoCustodiaDetallado flujoDetallado = copyPojoFlujoCustodiaDetallado(rec);
					out.p_out_FlujoDetallado.add(flujoDetallado);
				}
				for (String fecha : fechasCalculadas) {
					if (rec.TipoRegistro.equals("R") && !(rec.FechaLiquidacionProceso.toString()).equals(fecha)) {
						Date fechaD = Fecha.toDate(fecha);
						//Utils.copyPojo(rec, dfc);
						DatosFlujoCustodia dfc = copyPojoDatosFlujoCustodiaDatosFlujoCustodia(rec);
						
						dfc.IdDatosFlujoCustodia = iteratorDatosFlujoCustodia.next();
						if (dfc.FechaLiquidacionProceso.after(fechaD)) {
							dfc.CantidadProceso = new BigDecimal(0);
							dfc.ValorRescateProceso = new BigDecimal(0);
							dfc.PrecioCompraProceso = new BigDecimal(0);
							dfc.TasaCompraProceso = new BigDecimal(0);
							dfc.PrecioMercadoProceso = new BigDecimal(0);
							dfc.TasaMercadoProceso = new BigDecimal(0);
						}
						dfc.FechaLiquidacionProceso = fechaD;
						dfc.TipoRegistro = "P";
						listaDatosFlujoCustodiaProyectados.add(dfc);
					}
				}
			}
			
			Collections.sort(out.p_out_FlujoDetallado, new Comparator<FlujoCustodiaDetallado>() {
				@Override
				public int compare(FlujoCustodiaDetallado fcd1, FlujoCustodiaDetallado fcd2) {
					return fcd1.IdRegistro.compareTo(fcd2.IdRegistro);
				}
			} );
			listaDatosFlujoCustodia.addAll(listaDatosFlujoCustodiaProyectados);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Datos Flujo Custodia Proyectados");
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(75L,"PROCESANDO","Agrupando datos flujo custodia");
				
			}
			// AGRUPACION DE REGISTROS
			logger.debug("Inicio Loop Agrupacion");
			startTime = System.nanoTime();
			List<DatosProcesadosFlujo> listaDatosProcesadosFlujo = new ArrayList<DatosProcesadosFlujo>();
			ConcurrentHashMap<String, Integer> cantidadItem = new ConcurrentHashMap<String,Integer>();
			ConcurrentHashMap<String, Integer> sItemCalculo = new ConcurrentHashMap<String,Integer>();
			ConcurrentHashMap<String, Integer> sItemOrigen = new ConcurrentHashMap<String,Integer>();
			ConcurrentHashMap<String, Integer> sReg = new ConcurrentHashMap<String,Integer>();
			
			HashMap<Long, DatosFlujoCustodia> instrumentos = new HashMap<>(); //respaldar configuracion general del isntrumento (clasificacion, tipo, etc)
			
			List<DatosFlujoCustodia> listaDatosFlujoCustodiaFiltrados = listaDatosFlujoCustodia.parallelStream().filter(dfc -> {
				return (!"M".equals(dfc.TipoRegistro) && !"D".equals(dfc.TipoRegistro));
			}).collect(Collectors.toList());
			
			Map<String, List<DatosFlujoCustodia>> hashListaDatosFlujoCustodiaFiltrados = listaDatosFlujoCustodiaFiltrados.parallelStream().collect(Collectors.groupingBy(dfc ->{
					return Fecha.stringToSqlDate(dfc.FechaLiquidacionProceso) + "#" + dfc.IdUsoFlujoCustodia + "#" + dfc.ItemOrigen + "#" + dfc.ItemCalculo;
			}));
			
			logger.debug("tablaBase.size = " + tablaBase.size());
			logger.debug("listaDatosFlujoCustodia.size = " + listaDatosFlujoCustodia.size());
			tablaBase.stream().forEach(tb->{
				for (String fecha : fechasCalculadas) {
					AtomicBoolean join = new AtomicBoolean(false);
					
					String hashSItem = fecha + "#" + tb.IdCliente + "#" + tb.IdPortafolio + "#" + tb.UsoTipoOperacion + "#" 
							+ tb.IdClasificacionTipoInstrumento + "#" + tb.IdTipoInstrumento + "#" + tb.IdInstrumento + "#" + tb.IdEmisor + "#" 
							+ tb.FechaVencimiento + "#" + tb.IdTipoReajuste + "#" + tb.EsFungible + "#" + tb.IdBoveda + "#" + tb.IdUsoFlujoCustodia + "#" + tb.IdRegimenFondoMutuo;
					String hashSReg = fecha + "#" + tb.IdCliente + "#" + tb.IdPortafolio + "#" + tb.UsoTipoOperacion + "#" + tb.BusquedaEspecifica + "#" + tb.IdInstrumento;
					
					String hashFiltro = fecha + "#" + tb.IdUsoFlujoCustodia + "#" + tb.ItemOrigen + "#" + tb.ItemCalculo;
					
					List<DatosFlujoCustodia> listaFiltro = hashListaDatosFlujoCustodiaFiltrados.get(hashFiltro);
					if(listaFiltro != null) {
						List<DatosProcesadosFlujo> listaDatosProcesadosFlujoPorFiltro = listaFiltro.parallelStream().filter(dfc-> (tb.IdCliente == null || dfc.IdCliente.equals(tb.IdCliente))
								&& (tb.IdPortafolio == null || dfc.IdPortafolio.equals(tb.IdPortafolio))
								&& (tb.IdClasificacionTipoInstrumento == null || dfc.IdClasificacionTipoInstrumento.equals(tb.IdClasificacionTipoInstrumento))
								&& (tb.IdTipoInstrumento == null || dfc.IdTipoInstrumento.equals(tb.IdTipoInstrumento))
								&& (tb.IdInstrumento == null || dfc.IdInstrumento.equals(tb.IdInstrumento))
								&& (tb.IdRegimenFondoMutuo == null || dfc.IdRegimenFondoMutuo.equals(tb.IdRegimenFondoMutuo))).map(dfc->{
									if (dfc.IdInstrumento != null) instrumentos.put(dfc.IdInstrumento, dfc);
									//n2++;
									join.set(true);
									
									DatosProcesadosFlujo item = new DatosProcesadosFlujo();
									item.FechaSaldo = dfc.FechaLiquidacionProceso == null ? Fecha.toDate(fecha) : dfc.FechaLiquidacionProceso;
									item.IdCliente = tb.IdCliente == null ? (dfc.IdCliente == null ? 0 : dfc.IdCliente) : tb.IdCliente;
									item.IdPortafolio = tb.IdPortafolio == null ? (dfc.IdPortafolio == null ? 0 : dfc.IdPortafolio) : tb.IdPortafolio;
									item.FolioRegistro = dfc.FolioRegistro == null ? (long) 0 : dfc.FolioRegistro;
									item.IdTipoOperacion = dfc.IdTipoOperacion == null ? "" : dfc.IdTipoOperacion;
									item.Accion = dfc.Accion == null ? "" : dfc.Accion;
									item.IdMonedaInstrumento = dfc.IdMonedaInstrumento == null ? "" : dfc.IdMonedaInstrumento;
									item.IdMonedaEmision = dfc.IdMonedaEmision == null ? "" : dfc.IdMonedaEmision;
									item.IdClasificacionTipoInstrumento = dfc.IdClasificacionTipoInstrumento == null ? tb.IdClasificacionTipoInstrumento : dfc.IdClasificacionTipoInstrumento;
									item.IdTipoInstrumento = dfc.IdTipoInstrumento == null ? tb.IdTipoInstrumento : dfc.IdTipoInstrumento;
									item.IdInstrumento = dfc.IdInstrumento == null ? tb.IdInstrumento : dfc.IdInstrumento;
									item.Nemotecnico = dfc.Nemotecnico == null ? "" : dfc.Nemotecnico;
									item.NemoSVS = dfc.NemoSVS == null ? "" : dfc.NemoSVS;
									item.IdEmisor = dfc.IdEmisor == null ? tb.IdEmisor : dfc.IdEmisor;
									item.FechaEmisionInstrumento = dfc.FechaEmisionInstrumento == null ? fechaOperacionMinima : dfc.FechaEmisionInstrumento;
									item.FechaVencimientoInstrumento = dfc.FechaVencimientoInstrumento == null ? tb.FechaVencimiento : dfc.FechaVencimientoInstrumento;
									item.FechaLiquidacionInstrumento = dfc.FechaLiquidacion == null ? fechaOperacionMinima : dfc.FechaLiquidacion;
									item.IdTipoReajuste = dfc.IdTipoReajuste == null ? tb.IdTipoReajuste : dfc.IdTipoReajuste;
									item.EsFungible = dfc.EsFungible == null ? tb.EsFungible : dfc.EsFungible;
									item.IdBoveda = dfc.IdBoveda == null ? tb.IdBoveda : dfc.IdBoveda;
									item.IdBolsa = dfc.IdBolsa == null ? "" : dfc.IdBolsa;
									item.CodigoIFRS = dfc.CodigoIFRS == null ? "" : dfc.CodigoIFRS;
									item.IdMesa = dfc.IdMesa == null ? (long )0 : dfc.IdMesa;
									item.IdUsoFlujoCustodia = tb.IdUsoFlujoCustodia;
									item.UsoTipoOperacion = tb.UsoTipoOperacion;
									item.BusquedaEspecifica = tb.BusquedaEspecifica;
									item.ItemOrigen = tb.ItemOrigen;
									item.ItemCalculo = tb.ItemCalculo;
									if (dfc.ItemCalculo.equals(tb.ItemCalculo)) {
										item.CantidadProceso = dfc.CantidadProceso == null ? new BigDecimal(0) : dfc.CantidadProceso;
										item.ValorRescateProceso = dfc.ValorRescateProceso == null ? new BigDecimal(0) : dfc.ValorRescateProceso;
										item.PrecioCompraProceso = dfc.PrecioCompraProceso == null ? new BigDecimal(0) : dfc.PrecioCompraProceso;
										item.TasaCompraProceso = dfc.TasaCompraProceso == null ? new BigDecimal(0) : dfc.TasaCompraProceso;
										item.MontoCompraProcesoMonedaSistema = dfc.MontoCompraProcesoMonedaSistema == null ? new BigDecimal(0) : dfc.MontoCompraProcesoMonedaSistema;
										item.MontoCompraProcesoMonedaInstrumento = dfc.MontoCompraProcesoMonedaInstrumento == null ? new BigDecimal(0) : dfc.MontoCompraProcesoMonedaInstrumento;
										item.PrecioMercadoProceso = dfc.PrecioMercadoProceso == null ? new BigDecimal(0) : dfc.PrecioMercadoProceso;
										item.TasaMercadoProceso = dfc.TasaMercadoProceso == null ? new BigDecimal(0) : dfc.TasaMercadoProceso;
										item.MontoMercadoProcesoMonedaSistema = dfc.MontoMercadoProcesoMonedaSistema == null ? new BigDecimal(0) : dfc.MontoMercadoProcesoMonedaSistema;
										item.MontoMercadoProcesoMonedaInstrumento = dfc.MontoMercadoProcesoMonedaInstrumento == null ? new BigDecimal(0) : dfc.MontoMercadoProcesoMonedaInstrumento;
										item.MontoCompraMonedaLocalProceso = dfc.MontoCompraMonedaLocalProceso == null ? new BigDecimal(0) : dfc.MontoCompraMonedaLocalProceso;
									}
									
									item.Fecha = fecha;
									item.CorteMinimo = dfc.CorteMinimo;
									
									item.hashSItemCalculo = hashSItem + tb.ItemCalculo;
									if (!sItemCalculo.containsKey(item.hashSItemCalculo)) {
										sItemCalculo.put(item.hashSItemCalculo, 0);
									}
									int countSItemCalculo = dfc.ItemCalculo == null ? 0 : 1;
									sItemCalculo.put(item.hashSItemCalculo, sItemCalculo.get(item.hashSItemCalculo) + countSItemCalculo);
									
									item.hashSItemOrigen = hashSItem + tb.ItemOrigen;
									if (!sItemOrigen.containsKey(item.hashSItemOrigen)) {
										sItemOrigen.put(item.hashSItemOrigen, 0);
									}
									int countSItemOrigen = dfc.ItemCalculo == null ? 0 : 1;
									sItemOrigen.put(item.hashSItemOrigen, sItemOrigen.get(item.hashSItemOrigen) + countSItemOrigen);
									
									item.hashSREG = hashSReg;
									if (!sReg.containsKey(hashSReg)) {
										sReg.put(hashSReg, 0);
									}
									int countSReg = dfc.ItemCalculo == null ? 0 : 1;
									sReg.put(hashSReg, sReg.get(hashSReg) + countSReg);
									
									item.IdRegimenFondoMutuo = dfc.IdRegimenFondoMutuo;
									item.GrupoRegimen = dfc.GrupoRegimen;
									
									return item;
								}).collect(Collectors.toList());
						
						listaDatosProcesadosFlujo.addAll(listaDatosProcesadosFlujoPorFiltro);
					}
					
					if(!join.get()) {
						//n1++;
						DatosProcesadosFlujo item = new DatosProcesadosFlujo();

						item.FechaSaldo = Fecha.toDate(fecha);
						item.FechaEmisionInstrumento = fechaOperacionMinima;
						item.FechaLiquidacionInstrumento = fechaOperacionMinima;
						
						item.IdCliente = tb.IdCliente == null ? 0 : tb.IdCliente;
						item.IdPortafolio = tb.IdPortafolio == null ? 0 : tb.IdPortafolio;

						item.IdClasificacionTipoInstrumento = tb.IdClasificacionTipoInstrumento;
						item.IdTipoInstrumento = tb.IdTipoInstrumento;
						item.IdInstrumento = tb.IdInstrumento;
						item.IdEmisor = tb.IdEmisor;
						item.FechaVencimientoInstrumento = tb.FechaVencimiento;
						item.IdTipoReajuste = tb.IdTipoReajuste;
						item.EsFungible = tb.EsFungible;
						item.IdBoveda = tb.IdBoveda;

						item.IdUsoFlujoCustodia = tb.IdUsoFlujoCustodia;
						item.UsoTipoOperacion = tb.UsoTipoOperacion;
						item.BusquedaEspecifica = tb.BusquedaEspecifica;
						item.ItemOrigen = tb.ItemOrigen;
						item.ItemCalculo = tb.ItemCalculo;
						
						item.Fecha = fecha;
						
						item.hashSItemCalculo = hashSItem + tb.ItemCalculo;
						if (!sItemCalculo.containsKey(item.hashSItemCalculo)) {
							sItemCalculo.put(item.hashSItemCalculo, 0);
						}
						sItemCalculo.put(item.hashSItemCalculo, sItemCalculo.get(item.hashSItemCalculo));
						
						item.hashSItemOrigen = hashSItem + tb.ItemOrigen;
						if (!sItemOrigen.containsKey(item.hashSItemOrigen)) {
							sItemOrigen.put(item.hashSItemOrigen, 0);
						}
						sItemOrigen.put(item.hashSItemOrigen, sItemOrigen.get(item.hashSItemOrigen));
						
						item.hashSREG = hashSReg;
						if (!sReg.containsKey(hashSReg)) {
							sReg.put(hashSReg, 0);
						}
						sReg.put(hashSReg, sReg.get(hashSReg));
						
						item.IdRegimenFondoMutuo = tb.IdRegimenFondoMutuo;
						item.GrupoRegimen = "";
						
						listaDatosProcesadosFlujo.add(item);
					}
					
					String hashCantidadItem = fecha + "#" + tb.ItemOrigen;
					if(!cantidadItem.containsKey(hashCantidadItem)) {
						cantidadItem.put(hashCantidadItem, 0);
					}
					cantidadItem.put(hashCantidadItem, cantidadItem.get(hashCantidadItem) + 1);
				}
			});
			
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Loop Agrupacion");
			
			logger.debug("listaDatosProcesadosFlujo.size = " + listaDatosProcesadosFlujo.size());
			logger.debug("cantidadItem.size = " + cantidadItem.size());
			logger.debug("sItemCalculo.size = " + sItemCalculo.size());
			logger.debug("sItemOrigen.size = " + sItemOrigen.size());
			logger.debug("sReg.size = " + sReg.size());
			logger.debug("instrumentos.size = " + instrumentos.size());
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(85L,"PROCESANDO","Agrupando flujo custodia totalizado");
				
			}
			logger.debug("Inicio Loop Map Flujo Totalizado");
			startTime = System.nanoTime();
			HashMap<String, List<DatosProcesadosFlujo>> mapFlujoTotalizado = new HashMap<String, List<DatosProcesadosFlujo>>();			
			for (DatosProcesadosFlujo dfc : listaDatosProcesadosFlujo) {
				dfc.CantidadItem = cantidadItem.get(dfc.Fecha + "#" + dfc.ItemOrigen) != null ? (long) cantidadItem.get(dfc.Fecha + "#" + dfc.ItemOrigen) : 0L;
				dfc.SItemCalculo = sItemCalculo.get(dfc.hashSItemCalculo) != null ? (long) sItemCalculo.get(dfc.hashSItemCalculo) : 0L;
				dfc.SItemOrigen = sItemOrigen.get(dfc.hashSItemOrigen) != null ? (long) sItemOrigen.get(dfc.hashSItemOrigen) : 0L;
				dfc.SREG = sReg.get(dfc.hashSREG) != null ? (long) sReg.get(dfc.hashSREG) : 0L;
				DatosProcesadosFlujo item = new DatosProcesadosFlujo();
				
				for (FlujoEsAgrupadoPor eap : esAgrupadoPor) {
					item.FechaSaldo = dfc.FechaSaldo;
					item.IdCliente = dfc.IdCliente;
					item.IdPortafolio = dfc.IdPortafolio;
					//item.IdMonedaInstrumento = (eap.MonedaInstrumento == null||eap.MonedaInstrumento.equals("S")?dfc.IdMonedaInstrumento:"N"); //(Se intento agregar agrupador para instrumento, pero no muestra asignaciones)
					item.IdMonedaInstrumento =dfc.IdMonedaInstrumento;
					item.IdMonedaEmision = dfc.IdMonedaEmision;
					item.IdClasificacionTipoInstrumento = (eap.ClasificacionTipoInstrumento == null ? "N" : eap.ClasificacionTipoInstrumento).equals("S") ? dfc.IdClasificacionTipoInstrumento : "";
					item.IdTipoInstrumento = (eap.TipoInstrumento == null ? "N" : eap.TipoInstrumento).equals("S") ? dfc.IdTipoInstrumento : "";
					item.IdInstrumento = (eap.Instrumento == null ? "N" : eap.Instrumento).equals("S") ? dfc.IdInstrumento : new Long(0);
					item.IdEmisor = (eap.Emisor == null ? "N" : eap.Emisor).equals("S") ? dfc.IdEmisor : "";
					item.Nemotecnico = (eap.Nemotecnico == null ? "N" : eap.Nemotecnico).equals("S") ? dfc.Nemotecnico : "";
					item.NemoSVS = (eap.NemoSVS == null ? "N" : eap.NemoSVS).equals("S") ? dfc.NemoSVS : "";
					item.FechaEmisionInstrumento = (eap.FechaEmisionInstrumento == null ? "N" : eap.FechaEmisionInstrumento).equals("S") ? dfc.FechaEmisionInstrumento : fechaOperacionMinima;					
					item.FechaVencimientoInstrumento = (eap.FechaVencimientoInstrumento == null ? "N" : eap.FechaVencimientoInstrumento).equals("S") ? dfc.FechaVencimientoInstrumento : fechaOperacionMinima;					
					item.FechaLiquidacionInstrumento = (eap.FechaLiquidacionInstrumento == null ? "N" : eap.FechaLiquidacionInstrumento).equals("S") ? dfc.FechaLiquidacionInstrumento : fechaOperacionMinima;
					item.IdTipoReajuste = (eap.TipoReajuste == null ? "N" : eap.TipoReajuste).equals("S") ? dfc.IdTipoReajuste : "";
					item.EsFungible = (eap.EsFungible == null ? "N" : eap.EsFungible).equals("S") ? dfc.EsFungible : "";
					item.CodigoIFRS = (eap.CodigoIFRS == null ? "N" : eap.CodigoIFRS).equals("S") ? dfc.CodigoIFRS : "";
					item.IdBoveda = (eap.Boveda == null ? "N" : eap.Boveda).equals("S") ? dfc.IdBoveda : "";
					item.IdBolsa = (eap.Bolsa == null ? "N" : eap.Bolsa).equals("S") ? dfc.IdBolsa : "";
					item.IdTipoOperacion = (eap.TipoOperacion == null ? "N" : eap.TipoOperacion).equals("S") ? dfc.IdTipoOperacion : "";
					item.Accion = (eap.TipoOperacion == null ? "N" : eap.TipoOperacion).equals("S") ? dfc.Accion : "";
					item.IdMesa = (eap.Mesa == null ? "N" : eap.Mesa).equals("S") ? dfc.IdMesa : new Long(0);
					item.FolioRegistro = (eap.FolioRegistro == null ? "N" : eap.FolioRegistro).equals("S") ? dfc.FolioRegistro : new Long(0);
					item.IdRegimenFondoMutuo = (eap.IdRegimenFondoMutuo == null ? "N" : eap.IdRegimenFondoMutuo).equals("S") ? dfc.IdRegimenFondoMutuo : "";
					item.GrupoRegimen = (eap.GrupoRegimen == null ? "N" : eap.GrupoRegimen).equals("S") ? dfc.GrupoRegimen : "";
					String ItemOrigen = eap.ItemOrigen == null ? "N" : eap.ItemOrigen;
					item.ItemOrigen = ItemOrigen.equals("S") ? dfc.ItemOrigen : "";
					if(ItemOrigen.equals("S") && (long)dfc.CantidadItem > 0 && (long)dfc.SItemOrigen == 0) {
						item.ConsiderarRegistro = "S";
					} else if(ItemOrigen.equals("S") && (long)dfc.CantidadItem > 0 && (long)dfc.SItemOrigen > 0 && (long)dfc.SItemCalculo == 0) {
						item.ConsiderarRegistro = "N";
					} else if(ItemOrigen.equals("N") && (long)dfc.SREG > 0 && (long)dfc.SItemCalculo == 0) {
						item.ConsiderarRegistro = "N";
					} else {
						item.ConsiderarRegistro = "S";
					}
					item.CantidadProceso = dfc.CantidadProceso;
					item.ValorRescateProceso = dfc.ValorRescateProceso;
					item.UsoTipoOperacion = dfc.UsoTipoOperacion;
					item.BusquedaEspecifica = dfc.BusquedaEspecifica;
					item.CorteMinimo = dfc.CorteMinimo;
					
					String hash = item.UsoTipoOperacion + "#" +
							item.BusquedaEspecifica + "#" +
							item.FechaSaldo + "#" +
							item.IdCliente + "#" +
							item.IdPortafolio + "#" +
							item.IdClasificacionTipoInstrumento + "#" +
							item.IdTipoInstrumento + "#" +
							item.Accion + "#" +
							item.IdInstrumento + "#" +
							item.IdEmisor + "#" +
							item.Nemotecnico + "#" +
							item.NemoSVS + "#" +
							////item.CorteMinimo + "#" +
						
							// Corte Minimo.. si es null o cero debe quedar en 1 ya que en cartera el corte minimo es 1 para RV
							(item.CorteMinimo != null && item.CorteMinimo.compareTo(BigDecimal.ZERO) != 0 ? item.CorteMinimo.doubleValue() : new BigDecimal(1L).doubleValue()) + "#" +	
							item.IdMonedaInstrumento + "#" +
							item.IdTipoReajuste + "#" +
							item.FechaEmisionInstrumento + "#" +
							item.FechaVencimientoInstrumento + "#" +
							item.FechaSaldo + "#" +
							item.CodigoIFRS + "#" +
							item.EsFungible + "#" +
							item.IdBoveda + "#" +
							item.IdBolsa + "#" +
							item.IdMesa + "#" +
							item.FolioRegistro + "#" +
							item.ItemOrigen + "#" +
							item.IdTipoOperacion + "#" +
							item.PrecioMercadoProceso + "#" +
							item.TasaMercadoProceso + "#" + 
							item.IdRegimenFondoMutuo + "#";
					
					if (!mapFlujoTotalizado.containsKey(hash)) {
						mapFlujoTotalizado.put(hash, new ArrayList<DatosProcesadosFlujo>());
					}
					else {
						mapFlujoTotalizado.get(hash);
					}
					List<DatosProcesadosFlujo> list = mapFlujoTotalizado.get(hash);
					list.add(item);
					mapFlujoTotalizado.put(hash, list);
				}
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Loop Map Flujo Totalizado");
			
			logger.debug("Inicio Flujo Totalizado");
			startTime = System.nanoTime();
			Long i = 1L;
			for (Entry<String, List<DatosProcesadosFlujo>> entry : mapFlujoTotalizado.entrySet()) {
				List<DatosProcesadosFlujo> list = entry.getValue();
				if (list.get(0).ConsiderarRegistro.equals("S")) {					
					FechasOut fecha = (FechasOut) hmFechas.get(list.get(0).FechaSaldo.toString());
					FlujoCustodia fc = new FlujoCustodia();
					fc.IdProcesamientoInterno = i;
					fc.UsoTipoOperacion = list.get(0).UsoTipoOperacion;
					fc.BusquedaEspecifica = list.get(0).BusquedaEspecifica;
					fc.FechaSaldo = list.get(0).FechaSaldo;
					fc.Dias = fecha != null ? fecha.numeroDiasInicial : fc.Dias;
					fc.IdCliente = list.get(0).IdCliente;
					fc.IdPortafolio = list.get(0).IdPortafolio;
					fc.IdClasificacionTipoInstrumento = list.get(0).IdClasificacionTipoInstrumento;
					fc.IdTipoInstrumento = list.get(0).IdTipoInstrumento;
					fc.IdInstrumento = list.get(0).IdInstrumento;
					fc.IdEmisor = list.get(0).IdEmisor;
					fc.Nemotecnico = list.get(0).Nemotecnico;
					fc.NemoSVS = list.get(0).NemoSVS;
					fc.CorteMinimo = list.get(0).CorteMinimo;
					fc.IdMonedaInstrumento = list.get(0).IdMonedaInstrumento;
					fc.IdMonedaEmisionInstrumento = list.get(0).IdMonedaEmision;
					fc.IdTipoReajuste = list.get(0).IdTipoReajuste;
					fc.FechaEmisionInstrumento = list.get(0).FechaEmisionInstrumento;
					fc.FechaVencimientoInstrumento = list.get(0).FechaVencimientoInstrumento;
					fc.FechaLiquidacionInstrumento = list.get(0).FechaLiquidacionInstrumento;
					fc.CodigoIFRS = list.get(0).CodigoIFRS;
					fc.EsFungible = list.get(0).EsFungible;
					fc.IdBoveda = list.get(0).IdBoveda;
					fc.IdBolsa = list.get(0).IdBolsa;
					fc.IdTipoOperacion = list.get(0).IdTipoOperacion;
					fc.Accion = list.get(0).Accion;
					fc.IdMesa = list.get(0).IdMesa;
					fc.FolioRegistro = list.get(0).FolioRegistro;
					fc.ItemOrigen = list.get(0).ItemOrigen;
					fc.IdRegimenFondoMutuo = list.get(0).IdRegimenFondoMutuo;
					fc.GrupoRegimen = list.get(0).GrupoRegimen;
					
					fc.PrecioMercado = list.get(0).PrecioMercadoProceso;
					fc.TasaMercado = list.get(0).TasaMercadoProceso;					
					fc.Cantidad = new BigDecimal(0);
					fc.ValorRescate = new BigDecimal(0);
					fc.MontoAMercadoMonedaSistema = new BigDecimal(0);
					fc.MontoAMercadoMonedaInstrumento = new BigDecimal(0);
							
					for (DatosProcesadosFlujo item : list) {
						fc.Cantidad = fc.Cantidad.add(item.CantidadProceso);
						fc.ValorRescate = fc.ValorRescate.add(item.ValorRescateProceso);
						fc.MontoAMercadoMonedaSistema = fc.MontoAMercadoMonedaSistema.add(item.MontoMercadoProcesoMonedaSistema);
						fc.MontoAMercadoMonedaInstrumento = fc.MontoAMercadoMonedaInstrumento.add(item.MontoMercadoProcesoMonedaInstrumento);	
					}
					out.p_out_FlujoTotalizado.add(fc);
					i++;
				}
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Flujo Totalizado");
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(95L,"PROCESANDO","Agrupando flujo custodia totalizado");
				
			}
			//VALORIZAR
			logger.debug("Inicio Valorizar");
			startTime = System.nanoTime();
			if (in.p_ValorizarFlujo.equals("S")){
				//Valorizar Instrumentos
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(96L,"PROCESANDO","Valorizando instrumentos flujo custodia");
					
				}
				ValorizarInstrumento.ValorizarInstrumentoEntrada VIE = new ValorizarInstrumento.ValorizarInstrumentoEntrada();
				VIE.p_IdEmpresa				= in.p_IdEmpresa;		
				VIE.p_IdUsuario				= in.p_IdUsuario;
				VIE.p_IdModulo				= in.p_IdModulo;
				VIE.p_IdPais				= in.p_IdPais;
				VIE.p_OrigenProcesamiento 	= "FLUJOCUSTODIA";
				
				for (FlujoCustodia rec : out.p_out_FlujoTotalizado) {
					if (rec.Cantidad.equals(BigDecimal.ZERO) || (rec.ValorRescate.equals(BigDecimal.ZERO) && rec.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF"))){
						continue;
					}					
					DatosFlujoCustodia ins = instrumentos.get(rec.IdInstrumento);
					ValorizarInstrumento.ObjEntradaInstrumentos Instrumentos = new ValorizarInstrumento.ObjEntradaInstrumentos();
					ConfiguracionUsoFlujoCustodia uso = new ConfiguracionUsoFlujoCustodia(); 
					//for (ConfiguracionUsoFlujoCustodia ufc : usoFlujoCustodia) {
					for (ConfiguracionUsoFlujoCustodia ufc : usoFlujoCustodiaFilter) {
						if(ufc.UsoTipoOperacion.equals(rec.UsoTipoOperacion)) {
							uso = ufc;
							break;
						}
					}
					Instrumentos.IdValorizacion					= rec.IdProcesamientoInterno;
					Instrumentos.FechaValorizacion				= Fecha.toFormat(ins.FechaLiquidacionProceso== null ? rec.FechaLiquidacionInstrumento: ins.FechaLiquidacionProceso,"yyyy-MM-dd"); 		
					Instrumentos.IdClasificacionTipoInstrumento	= ins.IdClasificacionTipoInstrumento; 
					Instrumentos.IdTipoInstrumento				= ins.IdTipoInstrumento; 
					Instrumentos.IdInstrumento					= rec.IdInstrumento; 
					Instrumentos.Nemotecnico					= rec.Nemotecnico;
					Instrumentos.FechaOperacion					= Fecha.toFormat(ins.FechaEjecucion,"yyyy-MM-dd");
					//Instrumentos.Nemotecnico					= rec.Nemotecnico; 
					Instrumentos.FechaVencimiento				= Fecha.toFormat(ins.FechaVencimientoInstrumento == null ? rec.FechaVencimientoInstrumento : ins.FechaVencimientoInstrumento,"yyyy-MM-dd");
					//Instrumentos.FechaDeLiquidacion				= "0001-01-01";
					Instrumentos.FechaEmision					= Fecha.toFormat(ins.FechaEmisionInstrumento == null ? rec.FechaEmisionInstrumento : ins.FechaEmisionInstrumento,"yyyy-MM-dd");
					Instrumentos.IdMonedaInstrumento			= ins.IdMonedaInstrumento;		
					Instrumentos.IdTipoReajusteInstrumento		= ins.IdTipoReajuste;
					Instrumentos.Cantidad						= rec.Cantidad; 
					Instrumentos.TasaCompra						= ins.TasaMercado;
					Instrumentos.ValorRescate					= rec.ValorRescate; 
					Instrumentos.IdPortafolio					= rec.IdPortafolio;
					Instrumentos.Emisor							= rec.IdEmisor;
					Instrumentos.IdMonedaEmisionInstrumento		= rec.IdMonedaEmisionInstrumento == null?rec.IdTipoReajuste : rec.IdMonedaEmisionInstrumento;
					Instrumentos.TablaOrigen					= uso.UsoValorizacion;				
					VIE.p_Instrumentos.add(Instrumentos);
				};
				
				if (VIE.p_Instrumentos.size() > 0){					
					ValorizarInstrumento VI = new ValorizarInstrumento();
					ValorizarInstrumento.ValorizarInstrumentoSalida resultValorizacion = VI.logica(con, VIE);
					
					if (VIE != null && !resultValorizacion.P_OUT_ERRORESCONTADOR.equals(0L)){
						out.P_OUT_ERRORESCONTADOR 	= 1L;
						out.P_OUT_MENSAJES			= resultValorizacion.P_OUT_MENSAJES;
						//return out;
					}
					
					//Filtra la lista resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS con DimensionTasa = MERC
					List<ObjResultadoValorizacion> resultadoValorizacionFilter = resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS.stream().filter(rv -> "MERC".equals(rv.DimensionTasa)).collect(Collectors.toList());
					HashMap<Long, ObjResultadoValorizacion> objResultadoValorizacionHash = new HashMap<Long, ObjResultadoValorizacion>();
					for (ObjResultadoValorizacion ResultadoValorizacion : resultadoValorizacionFilter) {
						objResultadoValorizacionHash.put(ResultadoValorizacion.IdValorizacion, ResultadoValorizacion);
					}
					
					for (FlujoCustodia rec : out.p_out_FlujoTotalizado) {
						ObjResultadoValorizacion ResultadoValorizacion = objResultadoValorizacionHash.get(rec.IdProcesamientoInterno);
						if (ResultadoValorizacion != null) {
							rec.PrecioMercado = ResultadoValorizacion.Precio;
							rec.TasaMercado = ResultadoValorizacion.Tasa;
							rec.MontoAMercadoMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
							rec.MontoAMercadoMonedaSistema = ResultadoValorizacion.MontoMonedaSistema;
						}
					}
				};
			};
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Valorizar");
			
			//logger.debug("p_out_FlujoDetallado: "+out.p_out_FlujoDetallado);
			//logger.debug("p_out_FlujoTotalizado "+out.p_out_FlujoTotalizado);

		} catch (Exception e) {
			logger.error(e);
			throw e;
		} finally {
			sql.batch(con, SQL_DELETE_INSERTFLUJOCUSTODIA_MATRIZUSO, matrizUso, MatrizUso.class);
		}
		if(in.p_BarraProgreso != null) {
			in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Fin flujo custodia");
			
		}
		logger.info("Termino del servicio ConsultarFlujoCustodia, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static class CountCartera {
		public Long Count;
	}
	
	private List<DatosFlujoCustodia> queryCarteras(Connection con, MatrizUso matrizUso, HashMap<String, Object> params) throws SQLException {
		int umbral = 500000;
		int pagina = 100000;
		
		SQL sql = new SQL();
		String queryCarteraBase = generarQueryCarteras(SQL_SELECT_CARTERAS_PARAM,matrizUso);
		String queryCartera = queryCarteraBase.replace("<<SELECT_CLAUSE>>",SQL_SELECT_CARTERAS_SELECT_CLAUSE);
		
		String countCartera = queryCarteraBase.replace("<<SELECT_CLAUSE>>","COUNT(1) AS \"Count\"").replace("<<LIMIT_OFFSET_CLAUSE>>", "");
		List<CountCartera> count = sql.query(con, countCartera, params, CountCartera.class);
		List<DatosFlujoCustodia> res = new ArrayList<>();
		if (count.get(0).Count > umbral) {
			
			int index = 0;
			while (index < count.get(0).Count) {
				
				res.addAll(sql.query(con, queryCartera.replace("<<LIMIT_OFFSET_CLAUSE>>", "LIMIT " + pagina + " OFFSET " + index), params, DatosFlujoCustodia.class));	
				index+=pagina;
			}
			
			return res;
			
		}
		else {
			return sql.query(con, queryCartera.replace("<<LIMIT_OFFSET_CLAUSE>>", ""), params, DatosFlujoCustodia.class);	
		}
		
		
	}


	private String generarQueryCarteras(String SELECT, MatrizUso matrizUso) {
		String query = SELECT;
		
		List<String> predicates = new ArrayList<>();
		if (matrizUso.IdCliente != null) {
			predicates.add("(CAR.\"IdCliente\" = MZU.\"IdCliente\")");	
		}
		if (matrizUso.IdPortafolio != null) {
			predicates.add("(CAR.\"IdPortafolio\" = MZU.\"IdPortafolio\")");	
		}
		if (matrizUso.IdClasificacionTipoInstrumento != null) {
			predicates.add("(CAR.\"IdClasificacionTipoInstrumento\" = MZU.\"IdClasificacionTipoInstrumento\")");	
		}
		if (matrizUso.IdTipoInstrumento != null) {
			predicates.add("(CAR.\"IdTipoInstrumento\" = MZU.\"IdTipoInstrumento\")");	
		}
		if (matrizUso.IdInstrumento != null) {
			predicates.add("(CAR.\"IdInstrumento\" = MZU.\"IdInstrumento\")");	
		}
		if (matrizUso.IdEmisor != null) {
			predicates.add("( rtrim(CAR.\"IdEmisor\") = MZU.\"IdEmisor\")");	
		}
		if ( (matrizUso.FechaVencimiento != null) && (matrizUso.FechaVencimiento.compareTo(Fecha.toDate("0001-01-01")) != 0) ) {
			  if ("IF".equalsIgnoreCase(matrizUso.IdClasificacionTipoInstrumento) || "RF".equalsIgnoreCase(matrizUso.IdClasificacionTipoInstrumento)){
				  predicates.add("(CAR.\"FechaVencimientoInstrumento\" = MZU.\"FechaVencimiento\")");
			  }else {
				  predicates.add("(CAR.\"FechaVencimientoOperacion\" = MZU.\"FechaVencimiento\")");
			  }			
		}
		if (matrizUso.IdTipoReajuste != null) {
			predicates.add("(CAR.\"IdTipoReajuste\" = MZU.\"IdTipoReajuste\")");	
		}
		if (matrizUso.EsFungible != null) {
			predicates.add("(CAR.\"EsFungible\" = MZU.\"EsFungible\")");	
		}
		if (matrizUso.IdBoveda != null) {
			predicates.add("(CAR.\"IdBoveda\" = MZU.\"IdBoveda\")");	
		}
		if (matrizUso.IdRegimenFondoMutuo != null) {
			predicates.add("(CAR.\"IdRegimenFondoMutuo\" = MZU.\"IdRegimenFondoMutuo\")");	
		}
		if (predicates.isEmpty()) {
			query = query.replace("<<BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso>>", "(1==1)");
		}
		else {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i<predicates.size(); i++) {
				b.append(predicates.get(i));
				if (i != predicates.size() -1) {
					b.append(" AND ");
				}
			}
			query = query.replace("<<BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso>>", b.toString());
		}
		
		return query;
	}


	private HashMap<String, String> setItemCalculoHash() {
		HashMap<String, String> itemCalculoSet = new HashMap<String, String>();
		// 1.1 CARTERA
		itemCalculoSet.put("INFORMACION_CARTERA", "CARTERA");
		// 1.1.1 OPERACIONES EN TRANSITO - Operaciones facturadas, pero que se liquidan más adelante (EJ: Compra CN). No considera Ingresos/Retiros de Títulos no certificados.
		itemCalculoSet.put("CARTERA_TRANSITO_COMPRA", "CARTERA_TRANSITO");
		itemCalculoSet.put("CARTERA_TRANSITO_VENTA", "CARTERA_TRANSITO");
		itemCalculoSet.put("CARTERA_TRANSITO_VENTA_RPA", "CARTERA_VENTA_TRANSITO_RPA");
		// 1.2 TRANSITO
		itemCalculoSet.put("INFORMACION_TRANSITO_INGRESO", "TRANSITO");
		itemCalculoSet.put("INFORMACION_TRANSITO_RETIRO", "TRANSITO");
		itemCalculoSet.put("MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA", "TRANSITO");
		// 1.3 GARANTIA
		itemCalculoSet.put("INFORMACION_GARANTIA", "GARANTIA");
		// 1.4 SUBYACENTE
		itemCalculoSet.put("SIMULTANEA_COMPRA", "SUBYACENTE");
		itemCalculoSet.put("SIMULTANEA_VENTA", "SUBYACENTE");
		itemCalculoSet.put("REPORTE_COMPRA", "SUBYACENTE");
		itemCalculoSet.put("REPORTE_VENTA", "SUBYACENTE");
		itemCalculoSet.put("PRESTAMO", "SUBYACENTE");
		itemCalculoSet.put("EMPRESTITO", "SUBYACENTE");
		itemCalculoSet.put("COMPRA_PACTO", "SUBYACENTE");
		itemCalculoSet.put("VENTA_PACTO", "SUBYACENTE");
		// 1.5 CARTERA RESERVADA
		itemCalculoSet.put("RESERVA_COMPRA_PACTOS", "CARTERA_RESERVADA");
		itemCalculoSet.put("RESERVA_VENTA_PACTO", "CARTERA_RESERVADA");
		itemCalculoSet.put("RESERVA_COMPRA_TERMINO", "CARTERA_RESERVADA");
		itemCalculoSet.put("RESERVA_VENTA_TERMINO", "CARTERA_RESERVADA");
		// 1.6.1 ORDENES PENDIENTES DE AUTORIZACION
		itemCalculoSet.put("ORDENES_COMPRA_PENDIENTES", "ORDENES_POR_AUTORIZAR");
		itemCalculoSet.put("ORDENES_VENTA_PENDIENTES", "ORDENES_POR_AUTORIZAR");
		// 1.6.2 ORDENES INGRESADAS
		itemCalculoSet.put("ORDENES_COMPRA_INGRESADAS", "ORDENES_INGRESADAS");
		itemCalculoSet.put("ORDENES_VENTA_INGRESADAS", "ORDENES_INGRESADAS");
		// 1.6.3 ORDENES REPORTE PENDIENTES DE AUTORIZACION
		itemCalculoSet.put("ORDENES_REPORTE_COMPRA_PENDIENTES", "ORDENES_REPORTE_POR_AUTORIZAR");
		itemCalculoSet.put("ORDENES_REPORTE_VENTA_PENDIENTES", "ORDENES_REPORTE_POR_AUTORIZAR");
		// 1.6.4 ORDENES REPORTE INGRESADAS
		itemCalculoSet.put("ORDENES_REPORTE_COMPRA_INGRESADAS", "ORDENES_REPORTE_INGRESADAS");
		itemCalculoSet.put("ORDENES_REPORTE_VENTA_INGRESADAS", "ORDENES_REPORTE_INGRESADAS");
		// 1.7.1 ASIGNACIONES NO ENVIAS A FACTURAR
		itemCalculoSet.put("ASIGNACIONES_COMPRA_PENDIENTE", "ASIGNACIONES_NO_ENVIAS_A_FACTURAR");
		itemCalculoSet.put("ASIGNACIONES_VENTA_PENDIENTE", "ASIGNACIONES_NO_ENVIAS_A_FACTURAR");
		// 1.7.2 ASIGNACIONES REPORTE NO ENVIAS A FACTURAR
		itemCalculoSet.put("ASIGNACIONES_REPORTE_COMPRA_PENDIENTE", "ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR");
		itemCalculoSet.put("ASIGNACIONES_REPORTE_VENTA_PENDIENTE", "ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR");
		// 1.8.1 MOVIMIENTOS FACTURACION
		itemCalculoSet.put("MOVIMIENTOS_COMPRA_FACTURACION", "MOVIMIENTOS_FACTURACION");
		itemCalculoSet.put("MOVIMIENTOS_VENTA_FACTURACION", "MOVIMIENTOS_FACTURACION");
		// 1.8.2 MOVIMIENTOS REPORTE FACTURACION
		itemCalculoSet.put("MOVIMIENTOS_REPORTE_COMPRA_FACTURACION", "MOVIMIENTOS_REPORTE_FACTURACION");
		itemCalculoSet.put("MOVIMIENTOS_REPORTE_VENTA_FACTURACION", "MOVIMIENTOS_REPORTE_FACTURACION");
		// 1.9 EVENTOS DE CAPITAL
		itemCalculoSet.put("SUBSCRIPCIONES", "EVENTOS_DE_CAPITAL");
		itemCalculoSet.put("PREPAGO_BONO", "EVENTOS_DE_CAPITAL");
		itemCalculoSet.put("SORTEO_LETRAS", "EVENTOS_DE_CAPITAL");
		// 1.10 FONDOS MUTUOS
		itemCalculoSet.put("APORTE_FONDOS_MUTUOS", "FONDOS_MUTUOS");
		itemCalculoSet.put("RESCATE_FONDOS_MUTUOS", "FONDOS_MUTUOS");
		return itemCalculoSet;
	}
	
	
	public static FlujoUsoFiltrosBusqueda copyPojoFlujoUsoFiltrosBusqueda(FlujoUso fu, FlujoFiltrosBusqueda ffb) {
		FlujoUsoFiltrosBusqueda fufb = new FlujoUsoFiltrosBusqueda();
		fufb.UsoTipoOperacion = fu.UsoTipoOperacion;
		fufb.IdCliente = fu.IdCliente;
		fufb.IdPortafolio = fu.IdPortafolio;
		fufb.BusquedaEspecifica = fu.BusquedaEspecifica;
		fufb.IdTipoInstrumentoEspecifico = fu.IdTipoInstrumentoEspecifico;
		fufb.IdClasificacionTipoInstrumento = ffb.IdClasificacionTipoInstrumento;
		fufb.IdTipoInstrumento = ffb.IdTipoInstrumento;
		fufb.IdInstrumento = ffb.IdInstrumento;
		fufb.IdEmisor = ffb.IdEmisor;
		fufb.FechaVencimiento = ffb.FechaVencimiento;
		fufb.IdTipoReajuste = ffb.IdTipoReajuste;
		fufb.EsFungible = ffb.EsFungible;
		fufb.IdBoveda = ffb.IdBoveda;
		fufb.IdRegimenFondoMutuo = ffb.IdRegimenFondoMutuo;
		return fufb;
	}
	
	private MatrizUso copyPojoFlujoUsoFiltrosBusquedaMatrizUso(FlujoUsoFiltrosBusqueda u) {
		MatrizUso ma = new MatrizUso();
		ma.IdCliente = u.IdCliente;
		ma.IdPortafolio = u.IdPortafolio;
		ma.UsoTipoOperacion = u.UsoTipoOperacion;
		ma.IdClasificacionTipoInstrumento = u.IdClasificacionTipoInstrumento;
		ma.IdTipoInstrumento = u.IdTipoInstrumento;
		ma.IdInstrumento = u.IdInstrumento;
		ma.IdEmisor = u.IdEmisor;
		ma.FechaVencimiento = u.FechaVencimiento;
		ma.IdTipoReajuste = u.IdTipoReajuste;
		ma.EsFungible = u.EsFungible;
		ma.IdBoveda = u.IdBoveda;
		ma.IdTipoInstrumentoEspecifico = u.IdTipoInstrumentoEspecifico;
		//ma.IdUsoFlujoCustodia = u.IdUsoFlujoCustodia;
		ma.BusquedaEspecifica = u.BusquedaEspecifica;
		ma.IdRegimenFondoMutuo = u.IdRegimenFondoMutuo;
		//ma.IdEstado = u.IdEstado;
		//ma.IdEmpresa = u.IdEmpresa;
		//ma.ItemCalculo = u.ItemCalculo;
		//ma.ItemOrigen = u.ItemOrigen;
		//ma.FuncionOperador = u.FuncionOperador;
		//ma.MayorSimilitud = u.MayorSimilitud;
		return ma;
	}
	
	private MatrizUso copyPojoMatrizUsoMatrizUso(MatrizUso mu) {
		MatrizUso tb = new MatrizUso();
		tb.IdCliente = mu.IdCliente;
		tb.IdPortafolio = mu.IdPortafolio;
		tb.UsoTipoOperacion = mu.UsoTipoOperacion;
		tb.IdClasificacionTipoInstrumento = mu.IdClasificacionTipoInstrumento;
		tb.IdTipoInstrumento = mu.IdTipoInstrumento;
		tb.IdInstrumento = mu.IdInstrumento;
		tb.IdEmisor = mu.IdEmisor;
		tb.FechaVencimiento = mu.FechaVencimiento;
		tb.IdTipoReajuste = mu.IdTipoReajuste;
		tb.EsFungible = mu.EsFungible;
		tb.IdBoveda = mu.IdBoveda;
		tb.IdTipoInstrumentoEspecifico = mu.IdTipoInstrumentoEspecifico;
		tb.IdUsoFlujoCustodia = mu.IdUsoFlujoCustodia;
		tb.BusquedaEspecifica = mu.BusquedaEspecifica;
		tb.IdRegimenFondoMutuo = mu.IdRegimenFondoMutuo;
		tb.IdEstado = mu.IdEstado;
		tb.IdEmpresa = mu.IdEmpresa;
		tb.ItemCalculo = mu.ItemCalculo;
		tb.ItemOrigen = mu.ItemOrigen;
		tb.FuncionOperador = mu.FuncionOperador;
		tb.MayorSimilitud = mu.MayorSimilitud;
		return tb;
	}
	
	private DatosFlujoCustodia copyPojoMatrizUsoDatosFlujoCustodia(MatrizUso tb) {
		DatosFlujoCustodia dfc = new DatosFlujoCustodia();
		//dfc.IdDatosFlujoCustodia = tb.IdDatosFlujoCustodia;
		//dfc.KeyFlujoCustodia = tb.KeyFlujoCustodia;
		dfc.IdUsoFlujoCustodia = tb.IdUsoFlujoCustodia;
		dfc.UsoTipoOperacion = tb.UsoTipoOperacion;
		dfc.BusquedaEspecifica = tb.BusquedaEspecifica;
		dfc.ItemCalculo = tb.ItemCalculo;
		dfc.ItemOrigen = tb.ItemOrigen;
		dfc.FuncionOperador = tb.FuncionOperador;
		//dfc.FechaEjecucion = tb.FechaEjecucion;
		dfc.IdCliente = tb.IdCliente;
		dfc.IdPortafolio = tb.IdPortafolio;
		//dfc.EsCuentaPropia = tb.EsCuentaPropia;
		//dfc.IdRegistro = tb.IdRegistro;
		//dfc.FolioRegistro = tb.FolioRegistro;
		//dfc.IdConcepto = tb.IdConcepto;
		dfc.IdClasificacionTipoInstrumento = tb.IdClasificacionTipoInstrumento;
		dfc.IdTipoInstrumento = tb.IdTipoInstrumento;
		dfc.IdInstrumento = tb.IdInstrumento;
		//dfc.TipoValorizacion = tb.TipoValorizacion;
		dfc.IdEmisor = tb.IdEmisor;
		//dfc.Nemotecnico = tb.Nemotecnico;
		//dfc.NemoSVS = tb.NemoSVS;
		//dfc.CorteMinimo = tb.CorteMinimo;
		//dfc.IdMonedaInstrumento = tb.IdMonedaInstrumento;
		dfc.IdTipoReajuste = tb.IdTipoReajuste;
		//dfc.FechaEmisionInstrumento = tb.FechaEmisionInstrumento;
		//dfc.FechaVencimientoInstrumento = tb.FechaVencimientoInstrumento;
		//dfc.CodigoIFRS = tb.CodigoIFRS;
		dfc.EsFungible = tb.EsFungible;
		//dfc.TipoCarteraCorredor = tb.TipoCarteraCorredor;
		dfc.IdBoveda = tb.IdBoveda;
		dfc.IdTipoInstrumentoEspecifico = tb.IdTipoInstrumentoEspecifico;
		//dfc.IdBolsa = tb.IdBolsa;
		//dfc.IdTipoOperacion = tb.IdTipoOperacion;
		//dfc.Accion = tb.Accion;
		//dfc.IdTipoProducto = tb.IdTipoProducto;
		//dfc.IdMesa = tb.IdMesa;
		//dfc.FechaLiquidacion = tb.FechaLiquidacion;
		//dfc.Cantidad = tb.Cantidad;
		//dfc.ValorRescate = tb.ValorRescate;
		//dfc.PrecioCompra = tb.PrecioCompra;
		//dfc.TasaCompra = tb.TasaCompra;
		//dfc.MontoATasaCompra = tb.MontoATasaCompra;
		//dfc.PrecioMercado = tb.PrecioMercado;
		//dfc.TasaMercado = tb.TasaMercado;
		//dfc.MontoATasaMercado = tb.MontoATasaMercado;
		//dfc.PrecioVector = tb.PrecioVector;
		//dfc.MontoVector = tb.MontoVector;
		//dfc.CantidadProceso = tb.CantidadProceso;
		//dfc.ValorRescateProceso = tb.ValorRescateProceso;
		//dfc.PrecioCompraProceso = tb.PrecioCompraProceso;
		//dfc.TasaCompraProceso = tb.TasaCompraProceso;
		//dfc.MontoCompraProcesoMonedaSistema = tb.MontoCompraProcesoMonedaSistema;
		//dfc.MontoCompraProcesoMonedaInstrumento = tb.MontoCompraProcesoMonedaInstrumento;
		//dfc.PrecioMercadoProceso = tb.PrecioMercadoProceso;
		//dfc.TasaMercadoProceso = tb.TasaMercadoProceso;
		//dfc.MontoMercadoProcesoMonedaSistema = tb.MontoMercadoProcesoMonedaSistema;
		//dfc.MontoMercadoProcesoMonedaInstrumento = tb.MontoMercadoProcesoMonedaInstrumento;
		//dfc.FechaLiquidacionProceso = tb.FechaLiquidacionProceso;
		//dfc.RequiereValorizacion = tb.RequiereValorizacion;
		//dfc.TipoRegistro = tb.TipoRegistro;
		//dfc.MotivoEliminacioRegistro = tb.MotivoEliminacioRegistro;
		//dfc.ValorizarFlujo = tb.ValorizarFlujo;
		//dfc.VALTipoCartera = tb.VALTipoCartera;
		//dfc.VALUsoCriterio = tb.VALUsoCriterio;
		//dfc.VALIdTipoCriterioTasa = tb.VALIdTipoCriterioTasa;
		//dfc.VALPrecioCompra = tb.VALPrecioCompra;
		//dfc.VALTasaCompra = tb.VALTasaCompra;
		//dfc.VALMontoATasaCompraMonedaInstrumento = tb.VALMontoATasaCompraMonedaInstrumento;
		//dfc.VALMontoATasaCompraMonedaSistema = tb.VALMontoATasaCompraMonedaSistema;
		//dfc.VALPrecioMercado = tb.VALPrecioMercado;
		//dfc.VALTasaMercado = tb.VALTasaMercado;
		//dfc.VALMontoATasaMercadoMonedaInstrumento = tb.VALMontoATasaMercadoMonedaInstrumento;
		//dfc.VALMontoATasaMercadoMonedaSistema = tb.VALMontoATasaMercadoMonedaSistema;
		//dfc.MontoCompraMonedaLocalProceso = tb.MontoCompraMonedaLocalProceso;
		//dfc.IdAccionOperacion = tb.IdAccionOperacion;
		//dfc.IdMonedaEmision = tb.IdMonedaEmision;
		dfc.IdRegimenFondoMutuo = tb.IdRegimenFondoMutuo;
		//dfc.EsVentaDescubierta = tb.EsVentaDescubierta;
		//dfc.GrupoRegimen = tb.GrupoRegimen;
		return dfc;
	}
	
	public FlujoCustodiaDetallado copyPojoFlujoCustodiaDetallado(DatosFlujoCustodia rec) {
		FlujoCustodiaDetallado flujoDetallado = new FlujoCustodiaDetallado();
		flujoDetallado.IdCliente = rec.IdCliente;
		flujoDetallado.IdPortafolio = rec.IdPortafolio;
		flujoDetallado.IdClasificacionTipoInstrumento = rec.IdClasificacionTipoInstrumento;
		flujoDetallado.IdTipoInstrumento = rec.IdTipoInstrumento;
		flujoDetallado.IdInstrumento = rec.IdInstrumento;
		flujoDetallado.TipoValorizacion = rec.TipoValorizacion;
		flujoDetallado.IdEmisor = rec.IdEmisor;
		flujoDetallado.Nemotecnico = rec.Nemotecnico;
		flujoDetallado.NemoSVS = rec.NemoSVS;
		flujoDetallado.CorteMinimo = rec.CorteMinimo;
		flujoDetallado.IdMonedaInstrumento = rec.IdMonedaInstrumento;
		flujoDetallado.IdTipoReajuste = rec.IdTipoReajuste;
		flujoDetallado.FechaEmisionInstrumento = rec.FechaEmisionInstrumento;
		flujoDetallado.FechaVencimientoInstrumento = rec.FechaVencimientoInstrumento;
		flujoDetallado.CodigoIFRS = rec.CodigoIFRS;
		flujoDetallado.EsFungible = rec.EsFungible;
		flujoDetallado.IdBoveda = rec.IdBoveda;
		flujoDetallado.IdBolsa = rec.IdBolsa;
		flujoDetallado.IdTipoOperacion = rec.IdTipoOperacion;
		flujoDetallado.Accion = rec.Accion;
		flujoDetallado.IdTipoProducto = rec.IdTipoProducto;
		flujoDetallado.IdMesa = rec.IdMesa;
		flujoDetallado.FechaLiquidacion = rec.FechaLiquidacion;
		flujoDetallado.Cantidad = rec.Cantidad;
		flujoDetallado.ValorRescate = rec.ValorRescate;
		flujoDetallado.PrecioCompra = rec.PrecioCompra;
		flujoDetallado.TasaCompra = rec.TasaCompra;
		flujoDetallado.MontoATasaCompra = rec.MontoATasaCompra;
		flujoDetallado.PrecioMercado = rec.PrecioMercado;
		flujoDetallado.TasaMercado = rec.TasaMercado;
		flujoDetallado.MontoATasaMercado = rec.MontoATasaMercado;
		flujoDetallado.PrecioVector = rec.PrecioVector;
		flujoDetallado.MontoVector = rec.MontoVector;
		flujoDetallado.IdRegistro = rec.IdRegistro;
		flujoDetallado.FolioRegistro = rec.FolioRegistro;
		flujoDetallado.IdConcepto = rec.IdConcepto;
		flujoDetallado.IdDatosFlujoCustodia = rec.IdDatosFlujoCustodia;
		flujoDetallado.IdUsoFlujoCustodia = rec.IdUsoFlujoCustodia;
		flujoDetallado.UsoTipoOperacion = rec.UsoTipoOperacion;
		flujoDetallado.BusquedaEspecifica = rec.BusquedaEspecifica;
		flujoDetallado.ItemCalculo = rec.ItemCalculo;
		flujoDetallado.ItemOrigen = rec.ItemOrigen;
		flujoDetallado.FuncionOperador = rec.FuncionOperador;
		flujoDetallado.CantidadProceso = rec.CantidadProceso;
		flujoDetallado.ValorRescateProceso = rec.ValorRescateProceso;
		flujoDetallado.PrecioCompraProceso = rec.PrecioCompraProceso;
		flujoDetallado.TasaCompraProceso = rec.TasaCompraProceso;
		//flujoDetallado.MontoCompraProceso = rec.MontoCompraProceso;
		flujoDetallado.PrecioMercadoProceso = rec.PrecioMercadoProceso;
		flujoDetallado.TasaMercadoProceso = rec.TasaMercadoProceso;
		//flujoDetallado.MontoMercadoProceso = rec.MontoMercadoProceso;
		//flujoDetallado.PrecioVectorProceso = rec.PrecioVectorProceso;
		//flujoDetallado.MontoVectorProceso = rec.MontoVectorProceso;
		flujoDetallado.FechaLiquidacionProceso = rec.FechaLiquidacionProceso;
		flujoDetallado.RequiereValorizacion = rec.RequiereValorizacion;
		flujoDetallado.ValorizarFlujo = rec.ValorizarFlujo;
		flujoDetallado.IdRegimenFondoMutuo = rec.IdRegimenFondoMutuo;
		flujoDetallado.GrupoRegimen = rec.GrupoRegimen;
		flujoDetallado.FechaOrigenCompra = rec.FechaOrigenCompra;
		flujoDetallado.FechaOperacion = rec.FechaOperacion;
		flujoDetallado.IdContraparte = rec.IdContraparte;
		flujoDetallado.IdCorredorContraparte = rec.IdCorredorContraparte;
		return flujoDetallado;
	}
	
	private DatosFlujoCustodia copyPojoDatosFlujoCustodiaDatosFlujoCustodia(DatosFlujoCustodia rec) {
		DatosFlujoCustodia dfc = new DatosFlujoCustodia();
		dfc.IdDatosFlujoCustodia = rec.IdDatosFlujoCustodia;
		dfc.KeyFlujoCustodia = rec.KeyFlujoCustodia;
		dfc.IdUsoFlujoCustodia = rec.IdUsoFlujoCustodia;
		dfc.UsoTipoOperacion = rec.UsoTipoOperacion;
		dfc.BusquedaEspecifica = rec.BusquedaEspecifica;
		dfc.ItemCalculo = rec.ItemCalculo;
		dfc.ItemOrigen = rec.ItemOrigen;
		dfc.FuncionOperador = rec.FuncionOperador;
		dfc.FechaEjecucion = rec.FechaEjecucion;
		dfc.IdCliente = rec.IdCliente;
		dfc.IdPortafolio = rec.IdPortafolio;
		dfc.EsCuentaPropia = rec.EsCuentaPropia;
		dfc.IdRegistro = rec.IdRegistro;
		dfc.FolioRegistro = rec.FolioRegistro;
		dfc.IdConcepto = rec.IdConcepto;
		dfc.IdClasificacionTipoInstrumento = rec.IdClasificacionTipoInstrumento;
		dfc.IdTipoInstrumento = rec.IdTipoInstrumento;
		dfc.IdInstrumento = rec.IdInstrumento;
		dfc.TipoValorizacion = rec.TipoValorizacion;
		dfc.IdEmisor = rec.IdEmisor;
		dfc.Nemotecnico = rec.Nemotecnico;
		dfc.NemoSVS = rec.NemoSVS;
		dfc.CorteMinimo = rec.CorteMinimo;
		dfc.IdMonedaInstrumento = rec.IdMonedaInstrumento;
		dfc.IdTipoReajuste = rec.IdTipoReajuste;
		dfc.FechaEmisionInstrumento = rec.FechaEmisionInstrumento;
		dfc.FechaVencimientoInstrumento = rec.FechaVencimientoInstrumento;
		dfc.CodigoIFRS = rec.CodigoIFRS;
		dfc.EsFungible = rec.EsFungible;
		dfc.TipoCarteraCorredor = rec.TipoCarteraCorredor;
		dfc.IdBoveda = rec.IdBoveda;
		dfc.IdTipoInstrumentoEspecifico = rec.IdTipoInstrumentoEspecifico;
		dfc.IdBolsa = rec.IdBolsa;
		dfc.IdTipoOperacion = rec.IdTipoOperacion;
		dfc.Accion = rec.Accion;
		dfc.IdTipoProducto = rec.IdTipoProducto;
		dfc.IdMesa = rec.IdMesa;
		dfc.FechaLiquidacion = rec.FechaLiquidacion;
		dfc.Cantidad = rec.Cantidad;
		dfc.ValorRescate = rec.ValorRescate;
		dfc.PrecioCompra = rec.PrecioCompra;
		dfc.TasaCompra = rec.TasaCompra;
		dfc.MontoATasaCompra = rec.MontoATasaCompra;
		dfc.PrecioMercado = rec.PrecioMercado;
		dfc.TasaMercado = rec.TasaMercado;
		dfc.MontoATasaMercado = rec.MontoATasaMercado;
		dfc.PrecioVector = rec.PrecioVector;
		dfc.MontoVector = rec.MontoVector;
		dfc.CantidadProceso = rec.CantidadProceso;
		dfc.ValorRescateProceso = rec.ValorRescateProceso;
		dfc.PrecioCompraProceso = rec.PrecioCompraProceso;
		dfc.TasaCompraProceso = rec.TasaCompraProceso;
		dfc.MontoCompraProcesoMonedaSistema = rec.MontoCompraProcesoMonedaSistema;
		dfc.MontoCompraProcesoMonedaInstrumento = rec.MontoCompraProcesoMonedaInstrumento;
		dfc.PrecioMercadoProceso = rec.PrecioMercadoProceso;
		dfc.TasaMercadoProceso = rec.TasaMercadoProceso;
		dfc.MontoMercadoProcesoMonedaSistema = rec.MontoMercadoProcesoMonedaSistema;
		dfc.MontoMercadoProcesoMonedaInstrumento = rec.MontoMercadoProcesoMonedaInstrumento;
		dfc.FechaLiquidacionProceso = rec.FechaLiquidacionProceso;
		dfc.RequiereValorizacion = rec.RequiereValorizacion;
		dfc.TipoRegistro = rec.TipoRegistro;
		dfc.MotivoEliminacioRegistro = rec.MotivoEliminacioRegistro;
		dfc.ValorizarFlujo = rec.ValorizarFlujo;
		dfc.VALTipoCartera = rec.VALTipoCartera;
		dfc.VALUsoCriterio = rec.VALUsoCriterio;
		dfc.VALIdTipoCriterioTasa = rec.VALIdTipoCriterioTasa;
		dfc.VALPrecioCompra = rec.VALPrecioCompra;
		dfc.VALTasaCompra = rec.VALTasaCompra;
		dfc.VALMontoATasaCompraMonedaInstrumento = rec.VALMontoATasaCompraMonedaInstrumento;
		dfc.VALMontoATasaCompraMonedaSistema = rec.VALMontoATasaCompraMonedaSistema;
		dfc.VALPrecioMercado = rec.VALPrecioMercado;
		dfc.VALTasaMercado = rec.VALTasaMercado;
		dfc.VALMontoATasaMercadoMonedaInstrumento = rec.VALMontoATasaMercadoMonedaInstrumento;
		dfc.VALMontoATasaMercadoMonedaSistema = rec.VALMontoATasaMercadoMonedaSistema;
		dfc.MontoCompraMonedaLocalProceso = rec.MontoCompraMonedaLocalProceso;
		dfc.IdAccionOperacion = rec.IdAccionOperacion;
		dfc.IdMonedaEmision = rec.IdMonedaEmision;
		dfc.IdRegimenFondoMutuo = rec.IdRegimenFondoMutuo;
		dfc.EsVentaDescubierta = rec.EsVentaDescubierta;
		dfc.GrupoRegimen = rec.GrupoRegimen;
		dfc.FechaOrigenCompra = rec.FechaOrigenCompra;
		return dfc;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();					
				String jsonDE = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":53,\"p_IdModulo\":\"CUS\",\"p_FechaSaldo\":\"2020-04-01\",\"p_IdPais\":44,\"p_DiasProyeccion\":0,\"p_DiaHabil\":\"S\",\"p_ValorizarFlujo\":\"S\",\"p_Uso\":[{\"UsoTipoOperacion\":\"FLEVCP\",\"IdCliente\":null,\"IdPortafolio\":null,\"BusquedaEspecifica\":\"FLUJO_CLIENTES_DERECHO_CAPITAL\",\"IdTipoInstrumentoEspecifico\":null}],\"p_FiltrosBusqueda\":[{\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":252875,\"IdEmisor\":null,\"FechaVencimiento\":null,\"IdTipoReajuste\":null,\"EsFungible\":null,\"IdBoveda\":null,\"IdRegimenFondoMutuo\":null}],\"p_EsAgrupadoPor\":[{\"ClasificacionTipoInstrumento\":\"S\",\"TipoInstrumento\":\"S\",\"Instrumento\":\"S\",\"Emisor\":\"N\",\"Nemotecnico\":\"N\",\"NemoSVS\":\"N\",\"FechaEmisionInstrumento\":\"N\",\"FechaVencimientoInstrumento\":\"N\",\"FechaLiquidacionInstrumento\":\"N\",\"TipoReajuste\":\"N\",\"EsFungible\":\"N\",\"CodigoIFRS\":\"N\",\"Boveda\":\"N\",\"Bolsa\":\"N\",\"TipoOperacion\":\"N\",\"Mesa\":\"N\",\"FolioRegistro\":\"N\",\"ItemOrigen\":\"N\",\"IdRegimenFondoMutuo\":null,\"GrupoRegimen\":null,\"MonedaInstrumento\":null}]}";
				FlujoCustodiaEntrada Objin = gson.fromJson(jsonDE, FlujoCustodiaEntrada.class);
							
				ConsultarFlujoCustodia kk = new ConsultarFlujoCustodia();
				FlujoCustodiaSalida Objout = kk.logica(con, Objin);
				System.out.println(Objout.p_out_FlujoTotalizado);
				con.rollback();
				
	    	}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
}