package cl.bcs.custodia;


import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RetiraCustodiaDeMovimientoNominal implements JavaCall {
	static final Logger logger = LogManager.getLogger(RetiroCustodia.class);

	private final String SQL_CONSULTAR_MOVIMIENTOS_EN_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/RetiraCustodiaDeMovimientoNominal_SelectMovimientos.sql");
	private final String SQL_UPDATE_ESTADO = loadResourceToString("cl/bcs/custodia/sql/RetiraCustodiaDeMovimientoNominal_UpdateEstado.sql");
	private final String SQL_UPDATE_ESNOMINAL= loadResourceToString("cl/bcs/custodia/sql/RetiraCustodiaDeMovimientoNominal_UpdateEsNominal.sql");
	
			
	public static class Parametros_Salida{
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectConfiguracionNew {
		public String	CodigoParametro;					
		public String	Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MovimientoEnTransito {
		public Long 	IdMovimientoCarteraEnTransito;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	//copiado de GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito
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
		public BigDecimal			CantidadCustodia; 
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
		public String toString() {
			return Utils.printJson(this, true);
		};
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		Salida out = logica(conn,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		for (MensajeSalida msje : out.P_OUT_MENSAJES) {
			msje.descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public Salida logica(Connection conn,Entrada entrada) throws Exception {
		SQL sql = new SQL();

		HashMap<String,Object> 				params = new HashMap<>();

		
		List<ObjMovimientosEnTransito> MovimientosEnTransito = new ArrayList<ObjMovimientosEnTransito>();
		List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		List<MovimientoEnTransito> idsMovimientosUPD_EsNominal = new ArrayList<MovimientoEnTransito>();
		List<MovimientoEnTransito> idsMovimientosUPD_Estado = new ArrayList<MovimientoEnTransito>();
		
		Salida 		salida = new Salida();
		
        params.clear();
        //params.put("p_IdEmpresa", entrada.p_idEmpresa);
        MovimientosEnTransito = sql.query(conn, SQL_CONSULTAR_MOVIMIENTOS_EN_TRANSITO, params, ObjMovimientosEnTransito.class);

		for (ObjMovimientosEnTransito MovimientoEnTransito : MovimientosEnTransito) {
			if(MovimientoEnTransito.CantidadCustodia.compareTo(MovimientoEnTransito.Cantidad)>=0) {

				RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada(); 
				ObjEntrada.IdGenerarMovimientoCustodia			= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
				ObjEntrada.TablaAfectada 						= "CARTERA";
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen	= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdTipoComprobanteBusqueda			= MovimientoEnTransito.IdTipoComprobante.equalsIgnoreCase("CUS") ? null : MovimientoEnTransito.IdTipoComprobante;
				ObjEntrada.FolioComprobanteBusqueda				= MovimientoEnTransito.IdTipoComprobante.equalsIgnoreCase("CUS") ? null : MovimientoEnTransito.FolioComprobante;
				ObjEntrada.FolioCustodia						= MovimientoEnTransito.FolioCustodia == 0 ? null : MovimientoEnTransito.FolioCustodia;
				ObjEntrada.AccionOperacion						= "RETIRO";
				ObjEntrada.IdCliente							= MovimientoEnTransito.IdCliente;
				ObjEntrada.IdPortafolio							= MovimientoEnTransito.IdPortafolio;	
				ObjEntrada.FechaOperacion						= MovimientoEnTransito.FechaOperacion;
				ObjEntrada.FechaComprobante						= MovimientoEnTransito.FechaOperacion;
				ObjEntrada.IdTipoOperacion						= MovimientoEnTransito.IdTipoOperacion;
				ObjEntrada.IdConcepto							= MovimientoEnTransito.IdConcepto;
				if(MovimientoEnTransito.FechaLiquidacion.compareTo(Fecha.toDate(entrada.p_Fecha))>0) {
					ObjEntrada.IdCondicionLiquidacion			= MovimientoEnTransito.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion					= MovimientoEnTransito.FechaLiquidacion;
				}else {
					ObjEntrada.IdCondicionLiquidacion			= "CN"; 
					ObjEntrada.FechaLiquidacion					= Fecha.toDate(entrada.p_Fecha);//Fecha.toDate("2022-02-08");//	
				}
				ObjEntrada.IdTipoCanalIngreso					= MovimientoEnTransito.IdTipoCanalIngreso; 		 
				ObjEntrada.IdTipoComprobante					= MovimientoEnTransito.IdTipoComprobante; 
				ObjEntrada.FolioComprobante						= MovimientoEnTransito.FolioComprobante;
				ObjEntrada.IdUsuario							= MovimientoEnTransito.IdUsuario;
				ObjEntrada.IdSucursal							= MovimientoEnTransito.IdSucursal; 
				ObjEntrada.IdAgente								= MovimientoEnTransito.IdAgente;
				ObjEntrada.Glosa								= MovimientoEnTransito.Glosa;
				ObjEntrada.IdInstrumento						= MovimientoEnTransito.IdInstrumento;
				ObjEntrada.FechaVencimientoInstrumento			= MovimientoEnTransito.FechaVencimientoInstrumento;
				ObjEntrada.IdBoveda								= MovimientoEnTransito.IdBoveda;
				ObjEntrada.CuentaBoveda							= MovimientoEnTransito.CuentaBoveda;
				ObjEntrada.IdEmisor								= MovimientoEnTransito.IdEmisor;
				ObjEntrada.EsFungible							= MovimientoEnTransito.EsFungible;
				ObjEntrada.IdMonedaEmision						= MovimientoEnTransito.IdMonedaEmision; 
				ObjEntrada.Cantidad								= MovimientoEnTransito.Cantidad; 
				ObjEntrada.CorteMinimo							= MovimientoEnTransito.CorteMinimo; 
				ObjEntrada.ValorRescate							= MovimientoEnTransito.ValorRescate; 
				ObjEntrada.PrecioCompra							= MovimientoEnTransito.PrecioCompra; 
				ObjEntrada.TasaCompra							= MovimientoEnTransito.TasaCompra;
				ObjEntrada.MontoCompraMonedaInstrumento			= MovimientoEnTransito.MontoCompraMonedaInstrumento;
				ObjEntrada.IdMonedaAdicional					= MovimientoEnTransito.IdMonedaAdicional;
				ObjEntrada.IdOperacion							= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdClasificacionTipoInstrumento		= MovimientoEnTransito.IdClasificacionTipoInstrumento;
				ObjEntrada.EsNominal							= "N";
				p_RetiroCartera.add(ObjEntrada);
			}else {
				MovimientoEnTransito mov = new MovimientoEnTransito();
				mov.IdMovimientoCarteraEnTransito = MovimientoEnTransito.IdMovimientoCarteraEnTransito;
				idsMovimientosUPD_EsNominal.add(mov);
				
			}
			
		}

		if ( !p_RetiroCartera.isEmpty() ){
			System.out.println(p_RetiroCartera.size());
			RetiroCustodia retiroCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;
			paramInSP.p_SimularOperacion	= "N";
			paramInSP.p_TicketAtencion		= 0L;
			paramInSP.p_DatosEntrada		= p_RetiroCartera;
			paramInSP.p_ValorizacionAutomatica = "N";
			RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(conn, paramInSP);	
 
			for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result : resultRetiros.P_OUT_DATOSSALIDA){
				MovimientoEnTransito mov = new MovimientoEnTransito();
				logger.info(result.IdGenerarMovimientoCustodia + " " + result.Mensaje);
				if (result.CodigoMensaje.equalsIgnoreCase("CARTERA_RETIRADA")){
					mov.IdMovimientoCarteraEnTransito = result.IdGenerarMovimientoCustodia;
					idsMovimientosUPD_Estado.add(mov);
					
				}else {
					mov.IdMovimientoCarteraEnTransito = result.IdGenerarMovimientoCustodia;
					idsMovimientosUPD_EsNominal.add(mov);
					
				}
						
			};
		};

		if(!idsMovimientosUPD_Estado.isEmpty()) {
			sql.batch(conn, SQL_UPDATE_ESTADO, idsMovimientosUPD_Estado, MovimientoEnTransito.class);
		}

		if(!idsMovimientosUPD_EsNominal.isEmpty()) {
			sql.batch(conn, SQL_UPDATE_ESNOMINAL, idsMovimientosUPD_EsNominal, MovimientoEnTransito.class);
		}
		return salida;

	}
	
	public static class Entrada {
		public Long p_IdEmpresa;
		public String p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//SALIDAS
	public static class Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<String> P_OUT_SALIDA;
		
		public Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_SALIDA = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
				
				String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":377,\"p_IdModulo\":\"CUS\",\"p_Fecha\":\"2022-02-10\"}";
				
				
				Entrada Objin = gson.fromJson(Json, Entrada.class);
				RetiraCustodiaDeMovimientoNominal  demo=new RetiraCustodiaDeMovimientoNominal();				
				try {
					Salida ObjOut=demo.logica(con, Objin);
					con.rollback();
					System.out.println(ObjOut.toString());
				}
				catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
 
		}	
	}
}
	