package cl.bcs.plataforma.ins;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ins.pojo.Instrumento;
import cl.bcs.plataforma.ins.pojo.TipoInstrumento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearInstrumentos implements JavaCall {
	private static final Logger LOGGER = LogManager.getLogger(CrearInstrumentos.class);
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");
	private static final List<String> CLASIFICACION_TIPO_INSTRUMENTO = Arrays.asList(new String[]{"RV","FM","DV"});
	
	private final String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/ins/sql/CrearInstrumentos_SelectInstrumentos.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/ins/sql/CrearInstrumentos_SelectTipoInstrumento.sql");
	private final String SQL_INSERT_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/ins/sql/CrearInstrumentos_InsertInstrumentos.sql");
	private final String SQL_INSERT_VALOR_RIESGO = loadResourceToString("cl/bcs/plataforma/ins/sql/CrearInstrumentos_InsertValorRiesgo.sql");
	private final String SQL_INSERT_BOLSA_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/ins/sql/CrearInstrumentos_InsertBolsaInstrumento.sql");
	
	
	
	public static class InstrumentoEntrada extends Instrumento{
		
		public Long IdInterno;
		//public String IdEntidadClasificadoraRiesgo;
		//public String ValorClasificacionRiesgo;
		//public BigDecimal PrecioBase;
		//public String ValorizaPor;
		
		/*No se sabe si van ya que lo envia la UI pero no lo recibe el servicio SQL
		public BigDecimal PorcentajeCastigo;//: null
		public String FechaHastaAcreditado;//: null
		public String NemotecnicoCambio;//: null
		*/
		
		
		public InstrumentoEntrada() throws Exception {
			super();
			ISIN = "";
			CorteMinimo = BigDecimal.ZERO;
			EstaInscrito = "N";
			RazonSocial = "";
			FechaEmision = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
			FechaVencimiento = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
			IdEstado = "INA";
			Tamano = 0L;
			Precio = BigDecimal.ZERO;//PrecioBase = BigDecimal.ZERO;
			PrecioReferencia = BigDecimal.ZERO;
			FechaReferencia = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
			FrecuenciaCupon = BigDecimal.ZERO;
			NumeroCupones = 0;
			InstrumentoAcreditado = "N";
			IdInstrumentoRelacionado = 0L;
			EsPutEsCall = "";
			PrecioStrike = BigDecimal.ZERO;
			TipoBonoIRF = "";
			OrigenInstrumento = "";
			TipoRenta = "";
			RelacionadoGrupo = "N";
			FormadorMercado = "N";
			PactoAndino = "N"; 
			FactorExposicion = BigDecimal.ZERO;
			LargoPlazo = "N";
			Tasa = BigDecimal.ZERO;
			IdEmisor = "";
			IdTipoReajuste = "";
			IdMoneda = "";
			IdEstado = "";
			Tamano = 0L;
			Precio = BigDecimal.ZERO;
			PrecioReferencia  = BigDecimal.ZERO;
			PactoAndino = "";
			PorcentajeCastigo = BigDecimal.ZERO;
			EsPutEsCall = "";
			PrecioStrike = BigDecimal.ZERO;
			IdTipoInstrumento = "";
			AfectoArt107 = "N";
			AfectoArt104 = "N";
			CobroComisionesCustodia = "S";
			EsInstrumentoCalificado = "N";
		}
		
		public String toString() {
			
			return Utils.printJson(this, false);
		}

	}
	
	public static class ValorRiesgoEntrada{
		public Long 	IdInterno;//BIGINT,
		public String 	IdEntidadClasificadoraRiesgo;//"		NVARCHAR(10),
		public String 	ValorClasificacionRiesgo;//"			NVARCHAR(20)
		
		//internos
		public Long		IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class BolsaEntrada{
		public Long 	IdInterno;//"							BIGINT,
		public String 	IdBolsa;//"							NVARCHAR(3)
		
		//internos
		public Long		IdBolsaInstrumento;
		public Long		IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class CrearInstrumentoEntrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InstrumentoEntrada> p_Instrumentos;
		public List<ValorRiesgoEntrada> p_ValoresRiesgo;
		public List<BolsaEntrada> p_Bolsas;
		
		
		public CrearInstrumentoEntrada() {
			this.p_Instrumentos = new ArrayList<CrearInstrumentos.InstrumentoEntrada>();
			this.p_ValoresRiesgo = new ArrayList<CrearInstrumentos.ValorRiesgoEntrada>();
			this.p_Bolsas = new ArrayList<CrearInstrumentos.BolsaEntrada>();
		}


		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearInstrumentoSalida{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		
		public CrearInstrumentoSalida() {
			P_OUT_MENSAJES = new ArrayList<PlataformaUtils.MensajeSalida>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		CrearInstrumentoEntrada in =gson.fromJson(jin.toString() ,CrearInstrumentoEntrada.class);
		CrearInstrumentoSalida out=logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public CrearInstrumentoSalida logica(Connection conn, CrearInstrumentoEntrada in) throws Exception {
		CrearInstrumentoSalida out = new CrearInstrumentoSalida();
		SQL _sql = new SQL();
		//String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<Long> correlativos = new ArrayList<Long>();
		List<String> nemotecnicos = new ArrayList<String>();
		List<String> idTipoInstrumento = new ArrayList<String>();
		
		try {
			//validaciones
			if (in.p_Instrumentos.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_INSTRUMENTOS",null,conn,in.p_IdEmpresa)); 
				return out;
			}
			
			for (InstrumentoEntrada ins: in.p_Instrumentos) {
				if (correlativos.contains(ins.IdInterno)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",null,conn,in.p_IdEmpresa));
					return out;
				} else {
					correlativos.add(ins.IdInterno);
					nemotecnicos.add(ins.Nemotecnico);
					idTipoInstrumento.add(ins.IdTipoInstrumento);
				}
			}
			
			//--Nemo en blanco
			Long cantInstBlanco = in.p_Instrumentos.stream().filter(t-> t.Nemotecnico == null || t.Nemotecnico.equals("")).count();
			if (cantInstBlanco.compareTo(0L) > 1) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NEMOTECNICO_VACIO",null,conn,in.p_IdEmpresa));
				return out;
			}
			//--validacion nemo repetido
			params = new HashMap<String, Object>();
			params.put("Nemotecnico", nemotecnicos);
			List<Instrumento> resultInst = _sql.query(conn, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
			if (!resultInst.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NEMOTECNICO_YA_EXISTE",null,conn,in.p_IdEmpresa));
				return out;
			}
			//--validacion nemo sin tipo instrumento
			params = new HashMap<String, Object>();
			params.put("IdTipoInstrumento", idTipoInstrumento);
			Long cantInstSinTipo = in.p_Instrumentos.stream().filter(t-> t.IdTipoInstrumento == null || t.IdTipoInstrumento.equals("")).count();
			List<TipoInstrumento> resultTipoInst = _sql.query(conn, SQL_SELECT_TIPO_INSTRUMENTO, params, TipoInstrumento.class);
			
			if (cantInstSinTipo.compareTo(0L) > 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"INSTRUMENTO_SIN_TIPO",null,conn,in.p_IdEmpresa));
				return out;
			}
			//--validacion nemo sin emisor
			Long cantInstSinEmisor = in.p_Instrumentos.stream().filter(t-> t.IdEmisor == null || t.IdEmisor.equals("")).count();
			if (cantInstSinEmisor.compareTo(0L) > 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"INSTRUMENTO_SIN_EMISOR",null,conn,in.p_IdEmpresa));
				return out;
			}
			//--validacion instrumento sin moneda
			Long cantInstSinMoneda = in.p_Instrumentos.stream().filter(t-> t.IdMoneda == null || t.IdMoneda.equals("")).count();
			if (cantInstSinMoneda.compareTo(0L) > 1) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"INSTRUMENTO_SIN_MONEDA",null,conn,in.p_IdEmpresa));
				return out;
			}
			//obtengo las secuencias para los instrumentos a crear
			List<Long> ids = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::INSInstrumento", (long) in.p_Instrumentos.size());
			List<Long> idsBolsa = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento", (long) in.p_Bolsas.size());
			for (InstrumentoEntrada ins: in.p_Instrumentos) {
				TipoInstrumento tipoInst = resultTipoInst.stream().filter(t->t.IdTipoInstrumento.equalsIgnoreCase(ins.IdTipoInstrumento)).findFirst().get();
				Long countTipoIns = CLASIFICACION_TIPO_INSTRUMENTO.stream().filter(t->t.equalsIgnoreCase(tipoInst.IdClasificacionTipoInstrumento)).count();
				/****/
				ins.IdInstrumento = ids.get(in.p_Instrumentos.indexOf(ins));
				if (tipoInst != null && countTipoIns > 0L) {
					ins.ValorizaPor = "P";
				}
				ins.ISIN = ins.ISIN != null ? ins.ISIN: "";
				ins.CorteMinimo = ins.CorteMinimo != null ? ins.CorteMinimo : BigDecimal.ZERO;
				ins.EstaInscrito = ins.EstaInscrito != null ? ins.EstaInscrito : "N";
				ins.RazonSocial = ins.RazonSocial != null ? ins.RazonSocial : "";
				ins.FechaEmision = ins.FechaEmision != null ? ins.FechaEmision : new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
				ins.FechaVencimiento = ins.FechaVencimiento != null ? ins.FechaVencimiento : new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
				ins.IdEmisor = ins.IdEmisor != null ? ins.IdEmisor:"";
				ins.IdTipoReajuste = ins.IdTipoReajuste != null ? ins.IdTipoReajuste: "";
				ins.IdMoneda = ins.IdMoneda != null ? ins.IdMoneda :"";
				ins.IdTipoInstrumento = ins.IdTipoInstrumento != null ? ins.IdTipoInstrumento : "";
				ins.IdEstado = ins.IdEstado != null ? ins.IdEstado : "INA";
				ins.Tamano = ins.Tamano != null ? ins.Tamano : 0L;
				//ins.IdEntidadClasificadoraRiesgo = ins.IdEntidadClasificadoraRiesgo != null ? ins.IdEntidadClasificadoraRiesgo : "";
				//ins.ValorClasificacionRiesgo = ins.ValorClasificacionRiesgo != null ? ins.ValorClasificacionRiesgo : "";
				ins.Precio = ins.Precio != null ? ins.Precio : BigDecimal.ZERO;//ins.PrecioBase = ins.PrecioBase != null ? ins.PrecioBase : BigDecimal.ZERO;
				ins.PrecioReferencia = ins.PrecioReferencia != null ? ins.PrecioReferencia : BigDecimal.ZERO;
				ins.FechaReferencia = ins.FechaReferencia != null ? ins.FechaReferencia : new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
				ins.FrecuenciaCupon = ins.FrecuenciaCupon != null ? ins.FrecuenciaCupon : BigDecimal.ZERO;
				ins.NumeroCupones = ins.NumeroCupones != null ? ins.NumeroCupones : 0;
				ins.InstrumentoAcreditado = ins.InstrumentoAcreditado != null ?ins.InstrumentoAcreditado : "N";
				ins.IdInstrumentoRelacionado = ins.IdInstrumentoRelacionado != null ? ins.IdInstrumentoRelacionado : 0L;
				ins.EsPutEsCall = ins.EsPutEsCall != null ? ins.EsPutEsCall : "";
				ins.PrecioStrike = ins.PrecioStrike != null ? ins.PrecioStrike : BigDecimal.ZERO;
				ins.TipoBonoIRF = ins.TipoBonoIRF != null ? ins.TipoBonoIRF : "";
				ins.OrigenInstrumento = ins.OrigenInstrumento != null ? ins.OrigenInstrumento : "";
				ins.TipoRenta = ins.TipoRenta != null ? ins.TipoRenta : "";
				ins.RelacionadoGrupo = ins.RelacionadoGrupo != null ? ins.RelacionadoGrupo : "N";
				ins.FormadorMercado = ins.FormadorMercado != null ? ins.FormadorMercado : "N";
				ins.PactoAndino = ins.PactoAndino != null ? ins.PactoAndino : "N"; 
				ins.FactorExposicion = ins.FactorExposicion != null ? ins.FactorExposicion : BigDecimal.ZERO;
				ins.LargoPlazo = ins.LargoPlazo != null ? ins.LargoPlazo : "N";
				ins.Tasa = ins.Tasa != null ? ins.Tasa : BigDecimal.ZERO;
				ins.AfectoArt107 = ins.AfectoArt107 !=null ? ins.AfectoArt107 : "N";
				ins.AfectoArt104 = ins.AfectoArt104 !=null ? ins.AfectoArt104 : "N";
				ins.CobroComisionesCustodia = ins.CobroComisionesCustodia != null ? ins.CobroComisionesCustodia : "S";
				ins.EsInstrumentoCalificado = ins.EsInstrumentoCalificado != null ? ins.EsInstrumentoCalificado : "N";
				/****/
				List<ValorRiesgoEntrada> valRie = in.p_ValoresRiesgo.stream().filter(t->t.IdInterno.equals(ins.IdInterno)).collect(Collectors.toList());
				for(ValorRiesgoEntrada val: valRie) {
					val.IdInstrumento 					= ins.IdInstrumento;
					val.IdEntidadClasificadoraRiesgo	= val.IdEntidadClasificadoraRiesgo;
					val.ValorClasificacionRiesgo		= val.ValorClasificacionRiesgo;
				}
				List<BolsaEntrada> valBolsa = in.p_Bolsas.stream().filter(t->t.IdInterno.equals(ins.IdInterno)).collect(Collectors.toList());
				for(BolsaEntrada val: valBolsa) {
					val.IdBolsaInstrumento 	= idsBolsa.get(in.p_Bolsas.indexOf(val));
					val.IdInstrumento 		= ins.IdInstrumento;
					val.IdBolsa				= val.IdBolsa;
				}
			}
			
			// hago insert en la tabla de instrumento
			_sql.batch(conn, SQL_INSERT_INSTRUMENTOS, in.p_Instrumentos, InstrumentoEntrada.class);
			//Insert valor Riesgo
			_sql.batch(conn, SQL_INSERT_VALOR_RIESGO, in.p_ValoresRiesgo, ValorRiesgoEntrada.class);
			//Insert BolsaInstrumento
			_sql.batch(conn, SQL_INSERT_BOLSA_INSTRUMENTO, in.p_Bolsas, BolsaEntrada.class);
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_INSTRUMENTO_INGRESADO",null,conn,in.p_IdEmpresa));
			
		}catch (Exception e) {
			// TODO: handle exception
			LOGGER.error(e);
			e.printStackTrace();
			throw e;
		}finally {
			for(MensajeSalida mje : out.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
					out.P_OUT_ERRORESCONTADOR++;
				}
			}
		}
		
		return out;
	}
	
	
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Instrumentos\":[{\"IdInterno\":0,\"Nemotecnico\":\"sdssseeee\",\"ISIN\":null,\"CorteMinimo\":null,\"EstaInscrito\":\"N\",\"InstrumentoAcreditado\":\"N\",\"RazonSocial\":\"DSDSDSD\",\"FechaEmision\":2010-01-15,\"FechaVencimiento\":null,\"IdEmisor\":\"122376428\",\"IdTipoReajuste\":null,\"IdMoneda\":\"CLP\",\"IdTipoInstrumento\":\"IOA\",\"IdEstado\":\"ACT\",\"Tamano\":null,\"IdEntidadClasificadoraRiesgo\":null,\"ValorClasificacionRiesgo\":null,\"Precio\":null,\"PrecioReferencia\":0,\"FechaReferencia\":\"0001-01-01\",\"FrecuenciaCupon\":null,\"NumeroCupones\":null,\"TipoBonoIRF\":null,\"OrigenInstrumento\":null,\"TipoRenta\":null,\"RelacionadoGrupo\":null,\"FormadorMercado\":null,\"PactoAndino\":null,\"FactorExposicion\":null,\"LargoPlazo\":\"N\",\"IdInstrumentoRelacionado\":0,\"EsPutEsCall\":null,\"PrecioStrike\":null,\"PorcentajeCastigo\":null,\"FechaHastaAcreditado\":null,\"NemotecnicoCambio\":null,\"Tasa\":null}],\"p_ValoresRiesgo\":[],\"p_Bolsas\":[{\"IdInterno\":0,\"IdBolsa\":\"BCS\",\"IdInstrumentoSubyacente\":null,\"IdTipoInstrumento\":\"\",\"IdTipoInstrumentoSubyacente\":\"IOA\",\"EsGarantizable\":\"\"}]}";
			
			CrearInstrumentoEntrada Objin = gson.fromJson(json, CrearInstrumentoEntrada.class);

			CrearInstrumentos demo = new CrearInstrumentos();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				CrearInstrumentoSalida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			//con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
