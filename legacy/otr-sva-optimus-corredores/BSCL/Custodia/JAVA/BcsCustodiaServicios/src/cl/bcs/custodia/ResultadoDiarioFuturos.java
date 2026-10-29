package cl.bcs.custodia;

/**
 * Objetivo: Obtener informacion para el Resultado Diario de Futuros.
 */
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class ResultadoDiarioFuturos implements JavaCall {

	private final String SQL_SELECT_RESULTADO_DIARIO_FUTUROS = loadResourceToString("cl/bcs/custodia/sql/ResultadoDiarioFuturos_SelectResulado_Diario_Futuros.sql");	
	private static final SimpleDateFormat FORMATO_YYYYMMDD     = new SimpleDateFormat("yyyy-MM-dd");
	// --------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------
	// clases que son utilizadas unicamente en este java deben ser private y no estaticas
//	private class EntradaResultadoDiarioFuturos{
	public static class	EntradaResultadoDiarioFuturos{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;

		public String 	p_IdModulo;		
		public String 	p_FechaConsulta;
		public Long 	p_IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	
	// --------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------
	
//	private class SalidaResultadoDiarioFuturos {
	public static class SalidaResultadoDiarioFuturos {	
		private List<MensajeSalida>				P_OUT_MENSAJES;
		private Long							P_OUT_ERRORESCONTADOR;
		private List<ResultadoDiarioFuturo>		P_OUT_RESULTADO_DIARIO_FUTUROS;
		
		
		private SalidaResultadoDiarioFuturos()
		{	
			this.P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO_DIARIO_FUTUROS	= new ArrayList<ResultadoDiarioFuturo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// --------------------------------------------------------
	// CLASES DE PROCESO
	// --------------------------------------------------------

	public static class ResultadoDiarioFuturo {
		public Long 		IdCartera;
		public String 		DocumentoIdentidad;
		public Long 		PortafolioDerivados;
		public String		CodigoPortafolio;
		public String 		Nombre;
		public String 		Nemotecnico;
		public String 		FechaVencimiento;
		public String 		Fecha;
		public String 		InteresAbierto;
		public BigDecimal 	CantidadContratos;
		public Long			TamanoContratos;
		public BigDecimal 	PrecioAnterior;
		public BigDecimal 	PrecioBase;
		public BigDecimal 	Resultado;
		public String 		TipoMovimiento;
		public BigDecimal 	Abono;
		public BigDecimal 	Cargo;
		public BigDecimal 	Saldo;
		public String 		DescMovimiento;
		public String 		IdTipoInstrumento;
		public Long       	IdInstrumento;
		public Long 		IdCargoAbonoMovimiento;
	
	}
	


	private static final Logger logger = LogManager.getLogger(ResultadoDiarioFuturos.class);	
	
	// --------------------------------------------------------
	// LOGICA
	// --------------------------------------------------------

	public SalidaResultadoDiarioFuturos logica(Connection con, EntradaResultadoDiarioFuturos in) throws Exception {
		logger.info("INICIO RESULADO DIARIO FUTUROS");
		
		logger.info("ENTRADA > " + in.toString());
		SalidaResultadoDiarioFuturos out = sendResultadoDiarioFuturos(con, in);
		logger.info("TERMINO CONSULTAR SORTEO CONTRATOS");	
		return out;
	}
	
	// --------------------------------------------------------
	// METODOS
	// --------------------------------------------------------
	public SalidaResultadoDiarioFuturos sendResultadoDiarioFuturos(Connection con, EntradaResultadoDiarioFuturos entrada) throws Exception{
		
		
		

		HashMap<String, String> mapMensageSalida 	= new HashMap<>();		
		SalidaResultadoDiarioFuturos salida = new SalidaResultadoDiarioFuturos();		
		// ----------------------------------------------------------
		String tipoVencimiento= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "DER.LiquidacionFuturos.Tipo");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		String IdMonedaSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");
		
		SQL sql=new SQL();
		
		try {
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdPortafolio",entrada.p_IdPortafolio);
			params.put("FechaSistema",entrada.p_FechaConsulta);
			params.put("TipoVencimiento",tipoVencimiento);

		    List<ResultadoDiarioFuturo> salidaResultadoDiarioFuturos = sql.query(con, SQL_SELECT_RESULTADO_DIARIO_FUTUROS, params, ResultadoDiarioFuturo.class);
		    
			EntregarPreciosTasas entregarPreciosTasas = new EntregarPreciosTasas();
			EntregarPreciosTasas_Entrada entradaEntregarPreciosTasas = new EntregarPreciosTasas_Entrada();
			
			entradaEntregarPreciosTasas.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEntregarPreciosTasas.p_IdUsuario = entrada.p_IdUsuario;
			entradaEntregarPreciosTasas.p_IdModulo = entrada.p_IdModulo;
			entradaEntregarPreciosTasas.p_IdPais = idPaisEmpresa;
			
			
			for(ResultadoDiarioFuturo precio : salidaResultadoDiarioFuturos){
				EntregarPreciosTasas_Entrada_DatosInstrumentos tasa = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
				tasa.IdRegistro = precio.IdCartera;
				tasa.TipoCartera = "0";
				tasa.IdUso = "CARTERA";
				tasa.IdTipoCriterioTasa = "0";
				tasa.IdClasificacionTipoInstrumento = "DV";//AGREGAR evento.IdClasificacionTipoInstrumento;
				tasa.IdTipoInstrumento = precio.IdTipoInstrumento;
				tasa.IdInstrumento = precio.IdInstrumento;
				tasa.Nemotecnico = precio.Nemotecnico;
				tasa.IdMoneda = IdMonedaSistema;
				tasa.FechaConsulta = new java.sql.Date(FORMATO_YYYYMMDD.parse(entrada.p_FechaConsulta).getTime());
				tasa.PlazoResidual = 0L;
				entradaEntregarPreciosTasas.p_DatosInstrumentos.add(tasa);
			}

			EntregarPreciosTasas_Salida salidaPrecio = entregarPreciosTasas.logica(con, entradaEntregarPreciosTasas);
			
			HashMap<Long,Object> hPrecios = Utils.List2Hash(salidaPrecio.P_OUT_PRECIOSTASAS, "IdRegistro");
			
			System.out.println(salidaPrecio.P_OUT_PRECIOSTASAS);
			

		    if (!salidaResultadoDiarioFuturos.isEmpty()) {				    
			    for(ResultadoDiarioFuturo salidaResultado :  salidaResultadoDiarioFuturos) {
			    	EntregarPreciosTasas_Salida_PreciosTasas tasa = (EntregarPreciosTasas_Salida_PreciosTasas) hPrecios.get(salidaResultado.IdCartera);
					BigDecimal precio = tasa != null && tasa.Precio !=null ? tasa.Precio : BigDecimal.ZERO;
			    	BigDecimal tamanoContratos = new BigDecimal(salidaResultado.TamanoContratos);
			    	salidaResultado.PrecioBase				= precio;	
			    	salidaResultado.Resultado				= salidaResultado.CantidadContratos.multiply(tamanoContratos, MathContext.DECIMAL128).multiply(precio.subtract(salidaResultado.PrecioAnterior));
					salida.P_OUT_RESULTADO_DIARIO_FUTUROS.add(salidaResultado);				    	
				}				    

			    	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "SRV_DER_OK_AL_CONSULTAR_FUTUROS", mapMensageSalida, con, entrada.p_IdEmpresa));
				    
		    }
		    else
		    {
		    	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "SRV_DER_NO_EXISTE_INFORMACION_PARA_FECHA", mapMensageSalida, con, entrada.p_IdEmpresa));
		    }
		}    
		
    	catch (Exception e){
    		e.printStackTrace();
    		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "SRV_DER_ERROR_AL_CONSULTAR_CONTRATOS", mapMensageSalida, con, entrada.p_IdEmpresa));
    		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
    	}

		
//		System.out.println(salida.P_OUT_RESULTADO_DIARIO_FUTUROS);
		return salida;
    	
	};
    	
	
	
	// --------------------------------------------------------
	// CALL
	// --------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();	
		EntradaResultadoDiarioFuturos in=gson.fromJson(jin.toString() ,EntradaResultadoDiarioFuturos.class);
				
		SalidaResultadoDiarioFuturos out=logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// --------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------------
	
	public static void main(String[] args) {		
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try {
			Connection con = PlataformaUtils.getDESAConnection();			
			
			Gson gson = Utils.getGsonBuilder();	
			String json = "{\r\n" + 
					"  \"p_IdEmpresa\": 1,\r\n" + 
					"  \"p_IdUsuario\": 21,\r\n" + 
					"  \"p_IdModulo\": \"CUS\",\r\n" + 					
					"  \"p_FechaConsulta\": \"2020-03-17\",\r\n" + 
					"  \"p_IdPortafolio\": 0\r\n" +
					"}";			
			
			EntradaResultadoDiarioFuturos Objin = gson.fromJson(json, EntradaResultadoDiarioFuturos.class);
			ResultadoDiarioFuturos demo=new ResultadoDiarioFuturos();
			
			try {
				SalidaResultadoDiarioFuturos ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.rollback();
 				con.commit();
 				
			}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

};
