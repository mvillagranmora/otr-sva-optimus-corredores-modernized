package cl.bcs.normativa.ils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.ils.pojo.ConstantesILS;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class DetalleCoberturaILS  implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(DetalleCoberturaILS.class);
	
	private final String SQL_SELECT_CARTERA_OBLIGACIONES 		= loadResourceToString("cl/bcs/normativa/ils/sql/DetalleCoberturaILS_Categorias.sql");	
	private final String SQL_SELECT_DERECHO_FUTURO				= loadResourceToString("cl/bcs/normativa/ils/sql/DetalleCoberturaILS_DerechoFuturo.sql");
	private final String SQL_SELECT_DERECHO_ME 					= loadResourceToString("cl/bcs/normativa/ils/sql/DetalleCoberturaILS_DerechoME.sql");
	private final String SQL_SELECT_OBLIGACION_FUTURO 			= loadResourceToString("cl/bcs/normativa/ils/sql/DetalleCoberturaILS_ObligacionesFuturo.sql");	
	private final String SQL_SELECT_SIMULTANEA_DERECHO			= loadResourceToString("cl/bcs/normativa/ils/sql/DetalleCoberturaILS_SimultaneaDerechos.sql");
	private final String SQL_SELECT_SIMULTANEA_GARANTIA			= loadResourceToString("cl/bcs/normativa/ils/sql/DetalleCoberturaILS_SimultaneaGarantia.sql");
	private final String SQL_SELECT_SIMULTANEA_OBLIGACION		= loadResourceToString("cl/bcs/normativa/ils/sql/DetalleCoberturaILS_SimultaneaObligaciones.sql");
	
	private final ConstantesILS _c = new ConstantesILS();
	
	
	
	public static class DetalleCoberturaILS_Entrada{
		
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		
		public String p_UsaReglasNegocio;		
		public Long   p_IdTipoInforme;		
		public String p_FechaContable;
		public String p_FechaEmision;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleCoberturaILS_Salida{
		
		public List<ObjDetalleCobertura> 	P_TABLARESULTADODETALLECOBERTURA;
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public DetalleCoberturaILS_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
			this.P_TABLARESULTADODETALLECOBERTURA=new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	public static class ObjDetalleCobertura{
		
		public String Titulo;
		public String Categoria;
		public BigDecimal MontoGarantia;         //monto garantia banco
		public BigDecimal MontoBancosyEmpresas;  //montoEmpresa
		public BigDecimal FactorA;  			 //min%CB
		public BigDecimal FactorB;				 //max%CB
		public BigDecimal MontoCobertura;		 //montoCobertura
		public BigDecimal MontoEntidadFinanciera;
		public String Fecha;
		public Long IdEmpresa;
		
		public ObjDetalleCobertura () {
			this.Titulo				 ="";
			this.Categoria			 ="";
			this.MontoGarantia       = BigDecimal.ZERO;
			this.MontoBancosyEmpresas= BigDecimal.ZERO;
			this.FactorA    		 = BigDecimal.ZERO;
			this.FactorB			 = BigDecimal.ZERO;
			this.MontoCobertura		 = BigDecimal.ZERO;
			this.MontoEntidadFinanciera = BigDecimal.ZERO;
		}
		
		public ObjDetalleCobertura (String titulo) {
			this.Titulo				 =titulo;
			this.Categoria			 ="";
			this.MontoGarantia       = BigDecimal.ZERO;
			this.MontoBancosyEmpresas= BigDecimal.ZERO;
			this.FactorA    		 = BigDecimal.ZERO;
			this.FactorB			 = BigDecimal.ZERO;
			this.MontoCobertura		 = BigDecimal.ZERO;
			this.MontoEntidadFinanciera = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjTablaCoberturaPatrimonial{
		
		public String     Categoria;
		public String     TipoEmisor;		
		public BigDecimal MinPorcentajeCobertura;
		public BigDecimal MaxPorcentajeCobertura;		
		public BigDecimal TotalValorMercado;
				
	    public ObjTablaCoberturaPatrimonial() {
	    	this.TipoEmisor="";
	    	this.TotalValorMercado=BigDecimal.ZERO;
			this.MaxPorcentajeCobertura=BigDecimal.ZERO;
			this.MinPorcentajeCobertura=BigDecimal.ZERO;			
		}
		
		public String toString() {
			return Utils.printJson(this, false); 
		}
	}
	
	public static class ObjMonto{
		
		public BigDecimal monto;
		
		public ObjMonto() {
			this.monto= BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false); 
		}		
	}
	
	public DetalleCoberturaILS_Salida logica(Connection con,DetalleCoberturaILS_Entrada in) throws Exception  {
		
		SQL _sql = new SQL();
		DetalleCoberturaILS_Salida out= new DetalleCoberturaILS_Salida();
		
		try {
			
			List<ObjDetalleCobertura> resultadosCatergoria = new ArrayList <>();
			HashMap<String, Object> params = new HashMap<>();
			params.put("FechaContable", in.p_FechaContable);
			params.put("IdEmpresa", in.p_IdEmpresa);
			
			resultadosCatergoria = calculaCoberturaCategoria(con, in.p_IdEmpresa, in.p_FechaContable);
						
			List<ObjDetalleCobertura> DerechoFuturo= _sql.query(con, SQL_SELECT_DERECHO_FUTURO, params, ObjDetalleCobertura.class);
			resultadosCatergoria.add(verificaResultado(DerechoFuturo,"INDICES MONEDA EXTRANJERA"));			
			
			List<ObjDetalleCobertura> ObligacionFuturo= _sql.query(con, SQL_SELECT_OBLIGACION_FUTURO, params, ObjDetalleCobertura.class);
			resultadosCatergoria.add(verificaResultado(ObligacionFuturo,""));		
			
			List<ObjDetalleCobertura> DerechoME= _sql.query(con, SQL_SELECT_DERECHO_ME, params, ObjDetalleCobertura.class);		
			resultadosCatergoria.add(verificaResultado(DerechoME,""));	
			
			List<ObjDetalleCobertura> SimultaneaGarantia= _sql.query(con, SQL_SELECT_SIMULTANEA_GARANTIA, params, ObjDetalleCobertura.class);
			resultadosCatergoria.add(verificaResultado(SimultaneaGarantia,"SIMULTANEAS"));				
			
			List<ObjDetalleCobertura> SimultaneaObligaciones= _sql.query(con, SQL_SELECT_SIMULTANEA_OBLIGACION, params, ObjDetalleCobertura.class);
			resultadosCatergoria.add(verificaResultado(SimultaneaObligaciones,""));	
			
			List<ObjDetalleCobertura> SimultaneaDerecho= _sql.query(con, SQL_SELECT_SIMULTANEA_DERECHO, params, ObjDetalleCobertura.class);
			resultadosCatergoria.add(verificaResultado(SimultaneaDerecho,""));	
			
			out.P_TABLARESULTADODETALLECOBERTURA=resultadosCatergoria;
			
			
		}catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR=1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_DETALLE_COBERTURA_ERROR","No se pudo recolecion de datos"));
		}
					
		return out;
	}
	
	public ObjDetalleCobertura verificaResultado(List<ObjDetalleCobertura> listaConsulta,String titulo) {
		
		ObjDetalleCobertura obj;
		
		if(listaConsulta.size()>0) {
			obj=listaConsulta.get(0);
			obj.MontoCobertura = null == obj.MontoCobertura ? BigDecimal.ZERO : obj.MontoCobertura.divide(new BigDecimal(1000),20,RoundingMode.HALF_UP);
		}else {
			if(titulo.equals("")) {
				obj= new ObjDetalleCobertura();
			}else {
				obj= new ObjDetalleCobertura(titulo);
			}
		}
		return obj;
	}
	
	
	public List<ObjDetalleCobertura> calculaCoberturaCategoria(Connection con, Long idEmpresa, String fechaContable) throws SQLException {//(List<ObjTablaCoberturaPatrimonial> ListaDatosCategoria , DetalleCoberturaILS_Entrada in) {
		SQL _sql = new SQL();
		
		List<ObjDetalleCobertura> resultadosCatergoria = new ArrayList <>();
		BigDecimal MontoGarantia = BigDecimal.ZERO;
		BigDecimal MontoBancosyEmpresas = BigDecimal.ZERO;
		BigDecimal MontoCobertura = BigDecimal.ZERO;
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaContable", fechaContable);
		params.put("IdEmpresa", idEmpresa);
		
		List<ObjTablaCoberturaPatrimonial> ListaResumenCategorias= _sql.query(con, SQL_SELECT_CARTERA_OBLIGACIONES, params, ObjTablaCoberturaPatrimonial.class);
		
		for(String cat : _c.CATEGORIAS) {
			BigDecimal TotalEstado =BigDecimal.ZERO;
			BigDecimal TotalFinanciera =BigDecimal.ZERO;
			BigDecimal MinPorcentajeCobertura  =new BigDecimal(-1);
			BigDecimal MaxPorcentajeCobertura =BigDecimal.ZERO;
			List<ObjTablaCoberturaPatrimonial> ListaTmp = ListaResumenCategorias.stream().filter(c -> c.Categoria.equals(cat)).collect(Collectors.toList());
			if (ListaTmp.size() > 0) {
				for(ObjTablaCoberturaPatrimonial obj : ListaTmp) {	
					
					if("A".equals(obj.Categoria)) {				
						if("ES".equals(obj.TipoEmisor)) {
							TotalEstado=TotalEstado.add(obj.TotalValorMercado);
							MinPorcentajeCobertura=obj.MinPorcentajeCobertura;			
						}else{
							TotalFinanciera=TotalFinanciera.add(obj.TotalValorMercado);
							MinPorcentajeCobertura=BigDecimal.ZERO;
						}				
					}else {
						if("ES".equals(obj.TipoEmisor)) {
							TotalEstado=TotalEstado.add(obj.TotalValorMercado);								
						}else{
							TotalFinanciera=TotalFinanciera.add(obj.TotalValorMercado);
						}
						
						if(MinPorcentajeCobertura.compareTo(BigDecimal.ZERO)<0) {
							MinPorcentajeCobertura=obj.MinPorcentajeCobertura;
						}else {
							MinPorcentajeCobertura=MinPorcentajeCobertura.min(obj.MinPorcentajeCobertura);
						}
					}
					
					MaxPorcentajeCobertura= MaxPorcentajeCobertura.max(obj.MaxPorcentajeCobertura);
					//datosCategoria.Categoria=obj.Categoria;
					//resultadosCatergoria.add(datosCategoria);
				}
			}
			else {
				TotalEstado 			= BigDecimal.ZERO;
				TotalFinanciera 		= BigDecimal.ZERO;
				MinPorcentajeCobertura  = BigDecimal.ZERO;
				MaxPorcentajeCobertura 	= BigDecimal.ZERO;
			}
			MinPorcentajeCobertura = MinPorcentajeCobertura.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			MaxPorcentajeCobertura = MaxPorcentajeCobertura.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			TotalEstado = TotalEstado.divide(new BigDecimal(1000),20,RoundingMode.HALF_UP);
			TotalFinanciera = TotalFinanciera.divide(new BigDecimal(1000),20,RoundingMode.HALF_UP);
			
			ObjDetalleCobertura datosCategoria= new ObjDetalleCobertura();
			datosCategoria.Categoria=cat;
			datosCategoria.IdEmpresa= idEmpresa;
			datosCategoria.Fecha= fechaContable;
			datosCategoria.MontoGarantia = (MinPorcentajeCobertura.multiply(TotalEstado));
			datosCategoria.MontoBancosyEmpresas=TotalFinanciera.multiply(MaxPorcentajeCobertura);
			datosCategoria.FactorA= MinPorcentajeCobertura.multiply(new BigDecimal(100));
			datosCategoria.FactorB= MaxPorcentajeCobertura.multiply(new BigDecimal(100));
			datosCategoria.MontoCobertura =datosCategoria.MontoGarantia.add(datosCategoria.MontoBancosyEmpresas);
			
			resultadosCatergoria.add(datosCategoria);
			
			MontoGarantia = MontoGarantia.add(datosCategoria.MontoGarantia);
			MontoBancosyEmpresas = MontoBancosyEmpresas.add(datosCategoria.MontoBancosyEmpresas);
			MontoCobertura = MontoCobertura.add(datosCategoria.MontoCobertura);
		}
		resultadosCatergoria.get(0).Titulo="CARTERA OBLIGACIONES";
		
		// Agrego el resumen de  totales por columna
		ObjDetalleCobertura datosCategoria= new ObjDetalleCobertura();
		datosCategoria.Titulo = "";
		datosCategoria.Categoria = "TOTAL GENERAL";
		datosCategoria.IdEmpresa= idEmpresa;
		datosCategoria.Fecha= fechaContable;
		datosCategoria.MontoGarantia = MontoGarantia;
		datosCategoria.MontoBancosyEmpresas = MontoBancosyEmpresas;
		datosCategoria.FactorA = BigDecimal.ZERO;
		datosCategoria.FactorB = BigDecimal.ZERO;
		datosCategoria.MontoCobertura = MontoCobertura;
		datosCategoria.MontoEntidadFinanciera = BigDecimal.ZERO;
		resultadosCatergoria.add(datosCategoria);
		
		
		return resultadosCatergoria;
	}
	
	
	

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		DetalleCoberturaILS_Entrada in = gson.fromJson(jin.toString(), DetalleCoberturaILS_Entrada.class);
		DetalleCoberturaILS_Salida out = logica(con,in);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"ILS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoInforme\":1,\"p_FechaContable\":\"2023-03-30\",\"p_FechaEmision\":\"\"}";
			
			DetalleCoberturaILS_Entrada Objin = gson.fromJson(json, DetalleCoberturaILS_Entrada.class);
			DetalleCoberturaILS demo = new DetalleCoberturaILS();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				DetalleCoberturaILS_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}


}
