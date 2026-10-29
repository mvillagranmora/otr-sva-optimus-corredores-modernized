package cl.bcs.custodia.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class InformacionConfiguracionCustodia {	
	private static FileLoader loader = new FileLoader();	
	private static String SQL_SelectConfiguracionCustodia = loader.loadResourceToString("cl/bcs/custodia/sql/InformacionConfiguracionCustodia_SEL_ConfiguracionMovimientoCustodia.sql");
	
	
	public static class ObjConfiguracionMovimientoCustodia {		
		public String				IdTipoOperacion;
		
		public String				IdTipoOperacionRetornoCartera;   //Tipo de Operacion que se usara para la cartera devuelta en un vencimiento, anticipo, etc
		
		
		public String				IdAccionOperacion; 		
		public String				PoseeInstrumentoSubyacente; 
		public String				PermiteRetiroParcialSubyacente;		
		public String				EsGarantia; 
		public String				PermiteGarantiaAutomatica; 		
		public String				GeneraCarteraNueva; 
		public String				IdConceptoFusionCartera;		
		public String				RetornaCarteraAlPrecioTasa;		
		public String				GrabaClienteRelacionado; 
		public String				EsClienteIngresoClienteRelacionado; 
		public String				PoseeInstrumentoRelacionado; 
		public String				EsIngresoInstrumentoRelacionado; 		
		public String				PermiteAnulacion; 
		public String				PermiteAnularOperacionesCertificadas; 		
		public String				IdConceptoVentaCorta; 
		public String				IdTipoOperacionVentaCorta; 
		public String				IdTipoPrestamoVentaCorta; 		
		public Long					BaseCalculoDeInteres;			//debe venir del negocio
		public String				OperacionMigrada;
		public String				CertificaEn;		
		public String				IdEstadoOperacion;
		
		//////////////////////////	Analizar continuidad de los campos que siguen
		public String				InstrumentoPorIdTipoOperacion; 
		public String				UtilizaFechaLiquidacionBusqueda;
		public String				AgrupaPorIdGrupoOperacionInterno; 
		public String				OrdenEjecucionMovimientos;
		public String				UtilizaPlantillaMovimientoOriginal; 
		public String				InvierteClientesPlantillaMovimientoOriginal; 
		public String				BuscaMovimientosGeneradosPorCustodia;
		public String				CliIngGeneraMvtoEnTransitoSinCustodia; 
		public String				CliIngMueveCarteraEnTransitoING; 
		public String				CliIngMueveCarteraING; 
		public String				CliIngMueveSubyacenteING; 
		public String				CliIngMueveGarantiaING; 
		public String				CliIngMueveCarteraEnTransitoEGR; 
		public String				CliIngMueveCarteraEGR; 
		public String				CliIngMueveSubyacenteEGR; 
		public String				CliIngMueveGarantiaEGR; 
		public String				CliIngMueveCarteraEnTransitoAutomaticaING; 
		public String				CliIngMueveCarteraAutomaticaING; 
		public String				CliIngMueveSubyacenteAutomaticaING; 
		public String				CliIngMueveGarantiaAutomaticaING; 
		public String				CliIngMueveCarteraEnTransitoAutomaticaEGR; 
		public String				CliIngMueveCarteraAutomaticaEGR; 
		public String				CliIngMueveSubyacenteAutomaticaEGR; 
		public String				CliIngMueveGarantiaAutomaticaEGR; 
		public String				CliIngIdCondicionLiquidacionTransitoAutoING; 
		public String				CliIngIdCondicionLiquidacionCarteraAutoING; 
		public String				CliIngIdCondicionLiquidacionSubyacenteAutoING; 
		public String				CliIngIdCondicionLiquidacionGarantiaAutoING; 
		public String				CliIngIdCondicionLiquidacionTransitoAutoEGR; 
		public String				CliIngIdCondicionLiquidacionCarteraAutoEGR; 
		public String				CliIngIdCondicionLiquidacionSubyacenteAutoEGR; 
		public String				CliIngIdCondicionLiquidacionGarantiaAutoEGR; 
		public String				CliEgrGeneraMvtoEnTransitoSinCustodia; 
		public String				CliEgrMueveCarteraEnTransitoING; 
		public String				CliEgrMueveCarteraING; 
		public String				CliEgrMueveSubyacenteING; 
		public String				CliEgrMueveGarantiaING; 
		public String				CliEgrMueveCarteraEnTransitoEGR; 
		public String				CliEgrMueveCarteraEGR; 
		public String				CliEgrMueveSubyacenteEGR; 
		public String				CliEgrMueveGarantiaEGR; 
		public String				CliEgrMueveCarteraEnTransitoAutomaticaING; 
		public String				CliEgrMueveCarteraAutomaticaING; 
		public String				CliEgrMueveSubyacenteAutomaticaING; 
		public String				CliEgrMueveGarantiaAutomaticaING; 
		public String				CliEgrMueveCarteraEnTransitoAutomaticaEGR; 
		public String				CliEgrMueveCarteraAutomaticaEGR; 
		public String				CliEgrMueveSubyacenteAutomaticaEGR; 
		public String				CliEgrMueveGarantiaAutomaticaEGR; 
		public String				CliEgrIdCondicionLiquidacionTransitoAutoING; 
		public String				CliEgrIdCondicionLiquidacionCarteraAutoING; 
		public String				CliEgrIdCondicionLiquidacionSubyacenteAutoING; 
		public String				CliEgrIdCondicionLiquidacionGarantiaAutoING; 
		public String				CliEgrIdCondicionLiquidacionTransitoAutoEGR; 
		public String				CliEgrIdCondicionLiquidacionCarteraAutoEGR; 
		public String				CliEgrIdCondicionLiquidacionSubyacenteAutoEGR; 
		public String				CliEgrIdCondicionLiquidacionGarantiaAutoEGR;		
		
		
		public String				IdAccionOperacionRelacionada;
		public String 				IdTipoOperacionRelacionada;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	};

	public static List<ObjConfiguracionMovimientoCustodia> informacionConfiguracion(Connection conn,Long IdEmpresa) throws SQLException{
		List<ObjConfiguracionMovimientoCustodia> ConfiguracionCustodia = new ArrayList<ObjConfiguracionMovimientoCustodia>();
		SQL sql = new SQL();
		HashMap<String,Object> paramsConfiguracionMovimientoCustodia = new HashMap<>();
		paramsConfiguracionMovimientoCustodia.put("IdEmpresa", IdEmpresa);
		ConfiguracionCustodia = sql.query(conn, SQL_SelectConfiguracionCustodia, paramsConfiguracionMovimientoCustodia, ObjConfiguracionMovimientoCustodia.class);			
		return ConfiguracionCustodia;
	};	
};
