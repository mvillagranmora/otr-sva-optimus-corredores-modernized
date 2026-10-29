package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarCustodia;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarCustodiaMasiva implements JavaCall{	
	
	private final Log log = new Log("ValorizarCustodiaMasiva");
	private final String SQL_Insert_Instrumentos = "insert into \"BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Instrumentos\""
			+ "(\"IdClasificacionTipoInstrumento\",\"IdTipoInstrumento\",\"IdInstrumento\",\"IdEmisor\",\"FechaVencimiento\",\"IdTipoReajuste\",\"EsFungible\",\"IdBoveda\")"
			+ "values (:IdClasificacionTipoInstrumento,:IdTipoInstrumento,:IdInstrumento,:IdEmisor,:FechaVencimiento,:IdTipoReajuste,:EsFungible,:IdBoveda)";
	
	private final String SQL_Insert_Carteras = "insert into \"BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Carteras\""
			+ "(\"FolioCartera\",\"FolioGarantia\",\"FolioSubyacente\",\"IdCliente\",\"IdPortafolio\")"
			+ "values (:FolioCartera,:FolioGarantia,:FolioSubyacente,:IdCliente,:IdPortafolio)";
	

	private final String SQL_SelectTablaCarera		= loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodiaMasiva_SEL_Cartera.sql");
	private final String SQL_SelectTablaSubyacente	= loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodiaMasiva_SEL_Subyacente.sql");
	private final String SQL_SelectTablaGarantia	= loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodiaMasiva_SEL_Garantia.sql");
	
	
	public static class ObjDatosTablasCustodia {
		public Long				IdGenerarMovimientoCustodia;
		public Long				IdCustodia; 
		public Long				FolioCustodia;
		public Long				IdCliente;
		public Long				IdPortafolio;
		
		//-----------------------------------------------------------
		public java.sql.Date	FechaOperacion;
		public java.sql.Date	FechaLiquidacion;
		public java.sql.Date	FechaVencimientoOperacion;
		public Long				PlazoEnDiasOperacion;				//Solo para operaciones a plazo		
		public String			IdClasificacionTipoInstrumento; 
		public String			IdTipoInstrumento; 
		public Long				IdInstrumento; 
		public String			Nemotecnico; 
		public java.sql.Date	FechaVencimientoInstrumento;
		public java.sql.Date	FechaEmisionInstrumento;
		public String			TieneCorteDeCupon;
		public String			IdMonedaInstrumento;		
		public String			IdTipoReajusteInstrumento;
		public String 			IdMonedaEmisionInstrumento;
		public String 			Emisor;
		public BigDecimal		Cantidad; 
		public BigDecimal		ValorRescate; 
		public BigDecimal		MontoCompra;						//en moneda del instrumento
		
		//-----------------------------------------------------------
		public BigDecimal		PrecioCompra; 
		public BigDecimal		TasaCompra;	
		public BigDecimal		PrecioSubyacente; 
		public BigDecimal		TasaSubyacente; 
		public BigDecimal		PrecioGarantia; 
		public BigDecimal		TasaGarantia;
	};
	
	
	//CLASES
	public static class Instrumentos {
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				IdEmisor; 
		public java.sql.Date		FechaVencimiento; 
		public String				IdTipoReajuste; 
		public String				EsFungible; 
		public String				IdBoveda; 
	}
	
	public static class Carteras {
		public Long					FolioCartera; 
		public Long					FolioGarantia; 
		public Long					FolioSubyacente; 
		public Long					IdCliente; 
		public Long					IdPortafolio; 
	}

	//ENTRADA Y SALIDA DE LA CLASE
	public static class InValorizarCustodiaMasiva{
		public Long 				p_IdEmpresa; 			
		public  Long 				p_IdUsuario;
		public String 				p_IdModulo;
		public Long 				p_IdPais;
		public String 				p_FechaAnterior;
		public String 				p_FechaActual;
		public String 				p_FechaProximo;
		public String 				p_ValorizarCarteras;
		public String 				p_ValorizarGarantias;
		public String 				p_ValorizaATasaMercado;
		public String 				p_ValorizaTIF;
		public String 				p_SimularOperacion;
		public Long 				p_TicketProcesamiento;
		public String 				p_OrigenProcesamiento;
		public String 				p_OmiteValorizarAnteriorWS;
		public List<Instrumentos> 	p_Instrumentos;
		public List<Carteras> 		p_Carteras;
	    public InValorizarCustodiaMasiva(){
	    	p_Instrumentos = new ArrayList<Instrumentos>();
	    	p_Carteras = new ArrayList<Carteras>();
	    }
	}
	public static class OutValorizarCustodiaMasiva{
		public Long	 		P_OUT_ERRORESCONTADOR;
		public Long	 		p_out_TicketProcesamiento;
	}

	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InValorizarCustodiaMasiva in=gson.fromJson(jin.toString() ,InValorizarCustodiaMasiva.class);
		OutValorizarCustodiaMasiva out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public OutValorizarCustodiaMasiva logica(Connection con, InValorizarCustodiaMasiva entrada) throws Exception {
		boolean debug = true;
		return ejecutar(con,debug,entrada);
	}
	
	/**********************
	 * métodos de la clase
	 **********************/
	public OutValorizarCustodiaMasiva ejecutar(Connection con, boolean debug, InValorizarCustodiaMasiva entrada) throws Exception {
		OutValorizarCustodiaMasiva salida = new OutValorizarCustodiaMasiva();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql=new SQL();
		
		String tFechaSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa).substring(0, 10);
		HashMap<String,Object> paramsIN = new HashMap<>();
		paramsIN.put("p_IdEmpresa", entrada.p_IdEmpresa);
		
		try {
			if(entrada.p_Instrumentos.size()==0) entrada.p_Instrumentos.add(new Instrumentos());
			if(entrada.p_Carteras.size()==0) entrada.p_Carteras.add(new Carteras());
			
			ArrayList<Instrumentos> instrumentos = (ArrayList<Instrumentos>)entrada.p_Instrumentos;
			sql.batch(con,SQL_Insert_Instrumentos, instrumentos, Instrumentos.class);		
			ArrayList<Carteras> carteras = (ArrayList<Carteras>)entrada.p_Carteras;
			sql.batch(con,SQL_Insert_Carteras, carteras, Carteras.class);
			
			
			List<ObjDatosTablasCustodia> TablaCartera = sql.query(con, SQL_SelectTablaCarera, paramsIN, ObjDatosTablasCustodia.class);
			List<ObjDatosTablasCustodia> TablaSubyacente = sql.query(con, SQL_SelectTablaSubyacente, paramsIN, ObjDatosTablasCustodia.class);
			List<ObjDatosTablasCustodia> TablaGarantia = sql.query(con, SQL_SelectTablaGarantia, paramsIN, ObjDatosTablasCustodia.class);
			
			
			List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> InstrumentosAValorizar = new ArrayList<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada>();
			for (ObjDatosTablasCustodia CAR : TablaCartera){
				ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada Cartera = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
				Utils.copyPojo(CAR,Cartera);			
				Cartera.TablaOrigen				= "CARTERA";
				
				if ( CAR.FechaOperacion.compareTo( Fecha.toDate(tFechaSistema) ) == 0 )
					Cartera.EsRegistroNuevo		= "S";
				else
					Cartera.EsRegistroNuevo		= "N";
				
				InstrumentosAValorizar.add(Cartera);
			}
			for (ObjDatosTablasCustodia SUB : TablaSubyacente){
				ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada Subyacente = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();			
				Utils.copyPojo(SUB,Subyacente);			
				Subyacente.TablaOrigen			= "SUBYACENTE";
				
				if ( SUB.FechaOperacion.compareTo( Fecha.toDate(tFechaSistema) ) == 0 )
					Subyacente.EsRegistroNuevo	= "S";
				else
					Subyacente.EsRegistroNuevo	= "N";
				
				InstrumentosAValorizar.add(Subyacente);
			};
			for (ObjDatosTablasCustodia GAR : TablaGarantia){
				ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada Garantia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();			
				Utils.copyPojo(GAR,Garantia);			
				Garantia.TablaOrigen			= "GARANTIA";
				
				if ( GAR.FechaOperacion.compareTo( Fecha.toDate(tFechaSistema) ) == 0 )
					Garantia.EsRegistroNuevo	= "S";
				else
					Garantia.EsRegistroNuevo	= "N";
				
				InstrumentosAValorizar.add(Garantia);
			};
			
			
			if (InstrumentosAValorizar.size() > 0 ) {
				ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
				InformacionEmpresa EMP = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
				
				ValorizarCustodia.ValorizarCustodiaEntrada  VCE  = new ValorizarCustodia.ValorizarCustodiaEntrada();
				VCE.p_IdEmpresa				= entrada.p_IdEmpresa;
				VCE.p_IdUsuario				= entrada.p_IdUsuario;
				VCE.p_IdModulo				= entrada.p_IdModulo;
				VCE.p_IdPais				= EMP.IdPais;
				VCE.p_OrigenProcesamiento	= "VALORIZACION_MASIVA";
				VCE.p_InstrumentosAValorizar = InstrumentosAValorizar;
				
				ValorizarCustodia  VC = new ValorizarCustodia();
				ValorizarCustodia.ValorizarCustodiaSalida ResultadoValorizacion = VC.logica(con, VCE);
				salida.P_OUT_ERRORESCONTADOR = ResultadoValorizacion.P_OUT_ERRORESCONTADOR;
			};
		
		}
		catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			e.printStackTrace();
		}		
		return salida;
	};
	
	/**********************
	 * main para pruebas
	 **********************/
	public static void main(String[] args){
		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa","connections.properties")){
			try {
				Gson gson = Utils.getGsonBuilder();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_FechaAnterior\":null,\"p_FechaActual\":\"2023-04-20\",\"p_FechaProximo\":null,\"p_ValorizarCarteras\":\"S\",\"p_ValorizarGarantias\":\"N\",\"p_ValorizaATasaMercado\":\"S\",\"p_ValorizaTIF\":\"S\",\"p_SimularOperacion\":\"N\",\"p_TicketProcesamiento\":0,\"p_OrigenProcesamiento\":\"CONSULTA_CARTERAS\",\"p_OmiteValorizarAnteriorWS\":\"N\",\"p_Instrumentos\":[{\"IdClasificacionTipoInstrumento\":null,\"IdTipoInstrumento\":null,\"IdInstrumento\":null,\"IdEmisor\":null,\"FechaVencimiento\":null,\"IdTipoReajuste\":null,\"EsFungible\":null,\"IdBoveda\":null}],\"p_Carteras\":[{\"FolioCartera\":\"141\",\"FolioGarantia\":null,\"FolioSubyacente\":null,\"IdCliente\":66553,\"IdPortafolio\":308818}]}";
				InValorizarCustodiaMasiva Objin = gson.fromJson(json, InValorizarCustodiaMasiva.class);
								
				ValorizarCustodiaMasiva  kk = new ValorizarCustodiaMasiva();
				kk.logica(con, Objin);				
				//con.commit();
				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
        System.out.println("== END ==");
	}
}
