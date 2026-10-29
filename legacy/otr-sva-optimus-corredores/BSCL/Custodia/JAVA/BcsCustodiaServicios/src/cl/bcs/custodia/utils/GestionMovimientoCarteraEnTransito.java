package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GestionMovimientoCarteraEnTransito {	
	private static FileLoader loader = new FileLoader();	
	private static String SQL_SelectMovimientoEnTransito = loader.loadResourceToString("cl/bcs/custodia/sql/GestionMovimientoEnTransito_SEL_MovimientoEnTransito.sql");
	private static String SQL_UpdateTablaTransito = loader.loadResourceToString("cl/bcs/custodia/sql/GestionMovimientoEnTransito_UPD_MovimientoCarteraEnTransito.sql");
	private static String SQL_UpdateTablaTransitoNoRelacionado = loader.loadResourceToString("cl/bcs/custodia/sql/GestionMovimientoEnTransito_UPD_MovimientoCarteraEnTransitoNoRelacionado.sql");
	
	
	public static class ObjMovimientosEnTransito {
		public Long					IdMovimientoCarteraEnTransito; 
		public Long					IdMovimientoCarteraEnTransitoRelacionado; 
		public java.sql.Timestamp	FechaIngresoRegistro; 
		public java.sql.Timestamp	FechaCertificacionRegistro; 
		public java.sql.Timestamp	FechaAnulacionRegistro; 
		public Long					IdUsuarioIngreso; 
		public Long					IdUsuarioCertificacion; 
		public Long					IdUsuarioAnulacion; 
		public java.sql.Date		FechaOperacion; 
		public java.sql.Date		FechaOperacionOriginal;
		public java.sql.Date		FechaVencimientoOperacion; 
		public Long					PlazoEnDiasOperacion; 
		public Long					FolioCustodia; 
		public String				IdTipoComprobante; 
		public Long					FolioComprobante;
		public Long					IdMovimientoComprobante;
		public String				EsRegistroGeneradoPorSistema; 
		public String				MueveCustodia; 
		public String				PermiteCertificacionPorUsuario; 
		public String				OrigenCustodia; 
		public Long					IdCustodiaOrigen; 
		public String				IdTipoOperacionOrigen; 
		public Long					FolioCarteraGarantizada; 
		public Long					IdInstrumentoCarteraGarantizada; 
		public String				EsEspecifica; 
		public Long					IdContraparteGarantia; 
		public String				RazonSocialContraparteGarantia; 
		public String				CuentaContraparteGarantia; 
		public String				IdOrigenGarantia; 
		public Long					IdEspecieGarantia; 
		public Long					IdEmpresa; 
		public Long					IdPais; 
		public String				EsNominal; 
		public String				FolioExternoReferencia; 
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public Long					IdCuentaCustodioPortafolio;			
		public Long					IdClienteRelacionado;					//----------------------------------------------------
		public Long					IdPortafolioRelacionado;				//----------------------------------------------------
		public Long					IdCuentaCustodioPortafolioRelacionado;	//----------------------------------------------------	
		public String				IdEstado; 
		public String				IdTipoCanalIngreso; 
		public String				IdMonedaSistema; 
		public String				IdMonedaAdicional; 
		public java.sql.Date		FechaFactorMonedaAdicional; 
		public BigDecimal			ValorFactorMonedaAdicional; 
		public String				CodigoIFRS; 
		public String				IdConcepto; 
		public String				IdTipoOperacion; 
		public Long					IdOperacion;
		public Long					IdMesa; 
		public String				IdBolsa; 
		public Long					IdSucursal; 
		public Long					IdAgente; 
		public String				IdCondicionLiquidacion; 
		public java.sql.Date		FechaLiquidacion; 
		public String				EsIntraday; 
		public String				UsaGarantiaEspecifica; 
		public String				Glosa; 
		public Long					IdUsuario; 
		public String				IdModulo; 
		public String				TipoCarteraCorredor; 
		public String				TipoCarteraNormativa; 
		public java.sql.Date		FechaCurse; 
		public String				TipoFactorAjuste; 
		public BigDecimal			ValorFactorAjusteaFechaCurse; 
		public BigDecimal			CostoEnFactorAjuste; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public Long					IdInstrumentoAnterior; 
		public String				Nemotecnico; 
		public String				IdEmisor; 
		public String				NemoSVS; 
		public String				IdTipoReajuste; 
		public String				IdMonedaEmision; 
		public java.sql.Date		FechaEmisionInstrumento; 
		public java.sql.Date		FechaVencimientoInstrumento; 
		public String				EsFungible; 
		public String				ValorizaPor; 
		public String				TipoValorizacion; 
		public String				TieneCorteDeCupon; 
		public String				IdMonedaInstrumento; 
		public Long					Tamano; 
		public String				IdBoveda; 
		public String				CuentaBoveda; 
		public String				ISIN; 
		public java.sql.Date		FechaFactorMonedaInstrumento; 
		public BigDecimal			ValorFactorMonedaInstrumento; 
		public BigDecimal			Cantidad; 
		public BigDecimal			CorteMinimo; 
		public BigDecimal			ValorRescate;
		public BigDecimal			PrecioAPlazo;
		public BigDecimal			PrecioSucio;
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra; 
		public BigDecimal			MontoCompra; 
		public BigDecimal			MontoCompraMonedaInstrumento; 
		public BigDecimal			MontoCompraMonedaAdicional;
		public BigDecimal			MontoAPlazo; 
		public BigDecimal			MontoAPlazoMonedaInstrumento;
		public Long					IdCarga; 
		public Long					IdSecuenciaCarteraMigracion; 
		public String				CodigoPeriodo;
		public String				EsVentaCorta;
		public String				IdTipoProducto;
		public String				IdRegimenFondoMutuo;
		public java.sql.Date		FechaOrigenCompra;
		public String               NombreCliente;
		public String               CodigoPortafolio;
		public String 				DocumentoIdentidad;
		public BigDecimal			TasaTIR;
		public String 				esEnTransito="N";
		public String toString() {
			return Utils.printJson(this, true);
		};
	};
	public static class ObjMovimientosAActualizar {
		public Long		IdMovimientoCarteraEnTransito;
		public String	EsCertificacion = "N";
		public String	EsAnulacion = "N";
		public Long		IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, true);
		};
	};
	
	public static List<ObjMovimientosEnTransito> ObtenerMovimientoEnTransito(Connection conn,List<Long> IdMovimientosCarteraEnTransito) throws SQLException{
		List<ObjMovimientosEnTransito> MovimientosEnTransito = new ArrayList<ObjMovimientosEnTransito>();
		SQL sql = new SQL();
		
		HashMap<String,Object> paramsTransito = new HashMap<>();

		if (IdMovimientosCarteraEnTransito.size() > 0 ){
			paramsTransito.put("IdMovimientoCarteraEnTransito", IdMovimientosCarteraEnTransito);
			MovimientosEnTransito = sql.query(conn, SQL_SelectMovimientoEnTransito, paramsTransito, ObjMovimientosEnTransito.class);
		};			
		
		return MovimientosEnTransito;
	};
	
	public static boolean ActualizarMovimientoEnTransito(Connection conn,List<ObjMovimientosAActualizar> IdMovimientosCarteraEnTransito) throws SQLException{
		SQL sql = new SQL();
		
		List<ObjMovimientosEnTransito> ActualizacionTransitos = new ArrayList<ObjMovimientosEnTransito>();
		
		for (ObjMovimientosAActualizar in : IdMovimientosCarteraEnTransito) {
			ObjMovimientosEnTransito paramsMovimientoTransito = new ObjMovimientosEnTransito();
			paramsMovimientoTransito.IdMovimientoCarteraEnTransito 	= in.IdMovimientoCarteraEnTransito;
			paramsMovimientoTransito.IdEstado						= in.EsCertificacion.equalsIgnoreCase("S") ? "CRT" : (in.EsAnulacion.equalsIgnoreCase("S") ? "ANU" : "ERR");
			paramsMovimientoTransito.IdUsuarioCertificacion			= in.EsCertificacion.equalsIgnoreCase("S") ? in.IdUsuario : null;
			paramsMovimientoTransito.IdUsuarioAnulacion				= in.EsAnulacion.equalsIgnoreCase("S") ? in.IdUsuario : null;
			ActualizacionTransitos.add(paramsMovimientoTransito);
		};
		
		sql.batch(conn, SQL_UpdateTablaTransito, ActualizacionTransitos, ObjMovimientosEnTransito.class);
	
		return true;
	};	
	
	public static boolean ActualizarMovimientoEnTransitoNoRelacionado(Connection conn,List<ObjMovimientosAActualizar> IdMovimientosCarteraEnTransito) throws SQLException{
		SQL sql = new SQL();
		
		List<ObjMovimientosEnTransito> ActualizacionTransitos = new ArrayList<ObjMovimientosEnTransito>();
		
		for (ObjMovimientosAActualizar in : IdMovimientosCarteraEnTransito) {
			ObjMovimientosEnTransito paramsMovimientoTransito = new ObjMovimientosEnTransito();
			paramsMovimientoTransito.IdMovimientoCarteraEnTransito 	= in.IdMovimientoCarteraEnTransito;
			paramsMovimientoTransito.IdEstado						= in.EsCertificacion.equalsIgnoreCase("S") ? "CRT" : (in.EsAnulacion.equalsIgnoreCase("S") ? "ANU" : "ERR");
			paramsMovimientoTransito.IdUsuarioCertificacion			= in.EsCertificacion.equalsIgnoreCase("S") ? in.IdUsuario : null;
			paramsMovimientoTransito.IdUsuarioAnulacion				= in.EsAnulacion.equalsIgnoreCase("S") ? in.IdUsuario : null;
			ActualizacionTransitos.add(paramsMovimientoTransito);
		};
		
		sql.batch(conn, SQL_UpdateTablaTransitoNoRelacionado, ActualizacionTransitos, ObjMovimientosEnTransito.class);
	
	
		return true;
	};		
	
	
};
