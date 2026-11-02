// GENERATED DOUBLE of the legacy cl.bcs.custodia.ConsultarFlujoCustodia by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarFlujoCustodia implements JavaCall {
	
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
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarFlujoCustodia", "call", new Object[]{con, jin}, JsonObject.class); }

	
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
	public FlujoCustodiaSalida logica(Connection con, FlujoCustodiaEntrada in) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarFlujoCustodia", "logica", new Object[]{con, in}, FlujoCustodiaSalida.class); }
	
	public static class CountCartera {
		public Long Count;
	}
	
	
	public static FlujoUsoFiltrosBusqueda copyPojoFlujoUsoFiltrosBusqueda(FlujoUso fu, FlujoFiltrosBusqueda ffb) { throw new UnsupportedOperationException("oracle double ConsultarFlujoCustodia.copyPojoFlujoUsoFiltrosBusqueda: not part of the double"); }
	
	public FlujoCustodiaDetallado copyPojoFlujoCustodiaDetallado(DatosFlujoCustodia rec) { throw new UnsupportedOperationException("oracle double ConsultarFlujoCustodia.copyPojoFlujoCustodiaDetallado: not part of the double"); }
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double ConsultarFlujoCustodia.main: not part of the double"); };
}