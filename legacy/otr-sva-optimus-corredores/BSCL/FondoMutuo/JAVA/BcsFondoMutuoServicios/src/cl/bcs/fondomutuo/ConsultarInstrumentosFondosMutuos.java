package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
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

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarInstrumentosFondosMutuos implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarInstrumentosFondosMutuos.class);
	
	private final String SQL_SELECT_FONDO				= loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarInstrumentosFondosMutuos_SelectFondoMutuo.sql");
	private final String SQL_SELECT_FONDO_REINVERSION	= loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarInstrumentosFondosMutuos_SelectFondoMutuoReinversion.sql");
	private final String SQL_SELECT_REGIMEN				= loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarInstrumentosFondosMutuos_SelectRegimen.sql");
	
	public static class ConsultarInstrumentosFondosMutuos_Entrada
	{
		public long				p_IdEmpresa;
		public long				p_IdUsuario;
		public String			p_IdModulo;
		public InFondos			p_EntradaFondosMutuos;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class InFondos
	{
		public String IdSerieFondo;
		public String IdEmisor;
		public String IdFamiliaFondo;
		public List<String> IdRegimenFondoMutuo;
		public String IdSerieFondoOrigen; // Cuando tiene valor se obtienen los instrumentos candidatos a un aporte por reinversion
		public String IdCategoriaPerfilInversionista;
		public String IdEstado;
		public String IdMoneda;
		public String FondoExtranjero;
	    public String InstrumentoOWP;
	    public String EsAPV;
	    public String OcultarBloqueados;
	    
	    public InFondos() {
	    	this.IdSerieFondo = "";
	    	this.IdEmisor = "";
	    	this.IdFamiliaFondo = "";
	    	this.IdRegimenFondoMutuo = new ArrayList<String>();
	    	this.IdSerieFondoOrigen = "";
	    	this.IdCategoriaPerfilInversionista = "";
	    	this.IdEstado = "";
	    	this.IdMoneda = "";
	    	this.FondoExtranjero = "";
	    	this.InstrumentoOWP = "";
	    	this.EsAPV = "";
	    	this.OcultarBloqueados = "N";
	    }
	    
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarInstrumentosFondosMutuos_Salida
	{
		public List<OutFondoMutuo>		FondosMutuos;
		public List<MensajesErrores>	Mensajes;
		public Long						ContadorErrores;
		
		public ConsultarInstrumentosFondosMutuos_Salida()
		{
			this.FondosMutuos		= new ArrayList<OutFondoMutuo>();
			this.Mensajes			= new ArrayList<MensajesErrores>();
			this.ContadorErrores	= 0L;
		}
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutFondoMutuo
	{
		public String		IdSerieFondo;
		public String		CodigoFondo;
		public String		NombreFondo;
		public String		Nemotecnico;
		public String		CodigoSerie;
		public String		NombreSerie;
		public String		IdEmisor;
		public String		NombreEmisor;
		public String		IdMoneda;
		public String		IdFamiliaFondo;
		public BigDecimal	ValorCuota;
		public Long	DecimalesValorCuota;
		public String		FechaValorCuota;
		public BigDecimal	MontoMinimoInversionPesos;
		public BigDecimal	MontoMinimoInversionTotal;
		public BigDecimal	MontoMaximoInversionPesos;
		public BigDecimal	MontoMaximoInversionTotal;
		public String		IdCategoriaPerfilInversionista;
		public String		NombreCategoriaPerfilInversionista;
		public String		CobraComision;
		public String       InstrumentoOWP;
		public String       EsAPV;
		public String		RestringeAporte;
		public BigDecimal	MontoPatrimonioEmisor; 
		public List<OutListaRegimen> ListaRegimen;
		
		public OutFondoMutuo()
		{
			this.ListaRegimen = new ArrayList<OutListaRegimen>();
		}
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutListaRegimen
	{
		public String IdRegimen;
		public String DescripcionRegimen;
		public String PermiteMLT;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Regimen
	{
		public String IdSerieFondo;
		public String IdRegimenFondoMutuo;
		public String DescripcionRegimen;
		public String PermiteMLT;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class MensajesErrores
	{
		public Long		IdInterno;
		public String	TipoMensaje;
		public String	Codigo;
		public String	Mensaje;
		
		public MensajesErrores(Long id,String tipo,String codigo, HashMap<String, String> valores,Connection con,Long idEmpresa) throws Exception
		{
			this.IdInterno		= id;
			this.TipoMensaje	= tipo;
			this.Codigo			= codigo;
			this.Mensaje		= Diccionario.getMensaje(con,idEmpresa, codigo, valores);
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = new Gson();	
		ConsultarInstrumentosFondosMutuos_Entrada in=gson.fromJson(jin.toString(), ConsultarInstrumentosFondosMutuos_Entrada.class);
		ConsultarInstrumentosFondosMutuos_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	
	private ConsultarInstrumentosFondosMutuos_Salida logica(Connection conn, ConsultarInstrumentosFondosMutuos_Entrada in) throws Exception
	{
		logger.info("INICIO::ConsultaInstrumentosFondosMutuos");
		ConsultarInstrumentosFondosMutuos_Salida out = new ConsultarInstrumentosFondosMutuos_Salida();
		
		if(in.p_EntradaFondosMutuos == null){
			out.Mensajes.add(new MensajesErrores(0L,MensajeSalida.MENSAJE_ERROR,"UI_SIN_VALORES",new HashMap<String,String>(),conn,in.p_IdEmpresa));
			out.ContadorErrores = 1L;
			return out;
		}
		
		SQL sql = new SQL();
		String MostrarBloqueados;

		HashMap<String,Object> params =  new HashMap<>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdSerieFondo", in.p_EntradaFondosMutuos.IdSerieFondo);
		params.put("IdEmisor", in.p_EntradaFondosMutuos.IdEmisor);
		params.put("IdFamiliaFondo", in.p_EntradaFondosMutuos.IdFamiliaFondo);
		params.put("FiltroRegimen", in.p_EntradaFondosMutuos.IdRegimenFondoMutuo.size() > 0 ? 1L : 0L);
		params.put("IdRegimenFondoMutuo", in.p_EntradaFondosMutuos.IdRegimenFondoMutuo);
		params.put("IdCategoriaPerfilFondoMutuo", in.p_EntradaFondosMutuos.IdCategoriaPerfilInversionista);
		params.put("IdEstado", in.p_EntradaFondosMutuos.IdEstado);
		params.put("IdMoneda", in.p_EntradaFondosMutuos.IdMoneda);
		params.put("FondoExtranjero", in.p_EntradaFondosMutuos.FondoExtranjero);
		params.put("InstrumentoOWP", in.p_EntradaFondosMutuos.InstrumentoOWP);
		params.put("EsAPV", in.p_EntradaFondosMutuos.EsAPV);
		logger.debug("== select de fondos ==");
		if("".equals(in.p_EntradaFondosMutuos.IdSerieFondoOrigen)) {
			
			if("S".equalsIgnoreCase(in.p_EntradaFondosMutuos.OcultarBloqueados)) {
				MostrarBloqueados="N";
			}else {
				MostrarBloqueados="S";
			}
			
			params.put("MostrarBloqueados", MostrarBloqueados);
			out.FondosMutuos = sql.query(conn, SQL_SELECT_FONDO, params, OutFondoMutuo.class);
		}else {
			params.put("IdSerieFondoOrigen", in.p_EntradaFondosMutuos.IdSerieFondoOrigen);
			out.FondosMutuos = sql.query(conn, SQL_SELECT_FONDO_REINVERSION, params, OutFondoMutuo.class);
		}
		
		List<String> SerieFondo = out.FondosMutuos.parallelStream().map(serie->serie.IdSerieFondo).collect(Collectors.toList());
		params.clear();
		params.put("IdSerieFondo", SerieFondo);
		logger.debug("== select de regimen ==");
		List<Regimen>  regimenes = sql.query(conn, SQL_SELECT_REGIMEN, params, Regimen.class);
			
		//Armar salida
		out.FondosMutuos.parallelStream().forEach(salida->{
			salida.ListaRegimen = regimenes.parallelStream().filter(regimen -> regimen.IdSerieFondo.equals(salida.IdSerieFondo)).map(regimen->{
				OutListaRegimen outRegimen = new OutListaRegimen();
				outRegimen.DescripcionRegimen=regimen.DescripcionRegimen;
				outRegimen.IdRegimen=regimen.IdRegimenFondoMutuo;
				outRegimen.PermiteMLT=regimen.PermiteMLT;
				return outRegimen;
			}).collect(Collectors.toList());
		});
		
		logger.info("FIN::ConsultaInstrumentosFondosMutuos");
		return out;
		
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"EntradaFondosMutuos\":[{\"IdSerieFondo\":\"CFICCLAT-B\"}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_IdModulo\":\"FMM\",\"p_EntradaFondosMutuos\":{\"IdSerieFondo\":\"00149CUSD\",\"IdEmisor\":\"\",\"IdFamiliaFondo\":\"\",\"IdRegimenFondoMutuo\":[\"18T\",\"18Q\"],\"IdSerieFondoOrigen\":\"\",\"IdCategoriaPerfilInversionista\":\"\",\"InstrumentoOWP\":\"\",\"EsAPV\":\"\"}}";
			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"EntradaFondosMutuos\":[]}";
			
			ConsultarInstrumentosFondosMutuos_Entrada Objin = gson.fromJson(json, ConsultarInstrumentosFondosMutuos_Entrada.class);
			ConsultarInstrumentosFondosMutuos  demo=new ConsultarInstrumentosFondosMutuos();				
			try {
				
				ConsultarInstrumentosFondosMutuos_Salida ObjOut=demo.logica(con, Objin);
				//con.rollback();
				System.out.println(ObjOut);
				System.out.println(ObjOut.FondosMutuos.size());
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