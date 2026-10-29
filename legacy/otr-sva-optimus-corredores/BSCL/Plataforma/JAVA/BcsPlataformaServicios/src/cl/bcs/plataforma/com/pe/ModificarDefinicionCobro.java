package cl.bcs.plataforma.com.pe;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.pojo.DefinicionCobroCorresponsal_DB;
import cl.bcs.plataforma.com.pojo.DefinicionCobroEmisor_DB;
import cl.bcs.plataforma.com.pojo.DefinicionCobroInstrumento_DB;
import cl.bcs.plataforma.com.pojo.DefinicionCobroPortafolio_DB;
import cl.bcs.plataforma.com.pojo.DefinicionCobroRango_DB;
import cl.bcs.plataforma.com.pojo.DefinicionCobroTipoDeCliente_DB;
import cl.bcs.plataforma.com.pojo.DefinicionCobro_DB;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ModificarDefinicionCobro implements JavaCall {

	private final String SQL_UPDATE_DEFINICION_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_UpdateDefinicionCobro.sql");
	private final String SQL_INSERT_DEFINICION_RANGO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroRango.sql");
	private final String SQL_CONSULTAR_DEFINICION_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_ConsultarDefinicionCobro.sql");
	
	private final String SQL_DELETE_DEFINICION_COBRO_RANGO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroRango.sql");
	private final String SQL_DELETE_DEFINICION_COBRO_EMISOR = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroEmisor.sql");
	private final String SQL_DELETE_DEFINICION_COBRO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroInstrumento.sql");
	private final String SQL_DELETE_DEFINICION_COBRO_TIPO_CLIENTE = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroTipoCliente.sql");
	private final String SQL_DELETE_DEFINICION_COBRO_PORTAFOLIO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroPortafolio.sql");
	private final String SQL_DELETE_DEFINICION_COBRO_CORRESPONSAL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroCorredoraCorresponsal.sql");
	
	private final String SQL_INSERT_DEFINICION_COBRO_EMISOR = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroEmisor.sql");
	private final String SQL_INSERT_DEFINICION_COBRO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroInstrumento.sql");
	private final String SQL_INSERT_DEFINICION_COBRO_TIPO_CLIENTE = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroTipoCliente.sql");
	private final String SQL_INSERT_DEFINICION_COBRO_PORTAFOLIO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroPortafolio.sql");
	private final String SQL_INSERT_DEFINICION_COBRO_CORRESPONSAL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroCorredoraCorresponsal.sql");
	
	
	public ModificarDefinicionCobro_Salida logica(Connection conn,ModificarDefinicionCobro_Entrada entrada) throws Exception {
		ModificarDefinicionCobro_Salida salida = new ModificarDefinicionCobro_Salida();
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida basicosSalida = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(basicosSalida.p_out_Mensajes) > 0) {
			salida.p_out_Mensajes.addAll(basicosSalida.p_out_Mensajes);
			return salida;
		}
		
		SQL sql = new SQL();
		
		HashMap<String,Object> params1 = new HashMap<>();
		params1.put("IdEmpresa", entrada.p_IdEmpresa);
		params1.put("CodigoDefinicionCobro", entrada.p_DefinicionCobro.CodigoDefinicionCobro);
			
		List<DefinicionCobro_DB> cobro = sql.query(conn, SQL_CONSULTAR_DEFINICION_COBRO, params1, DefinicionCobro_DB.class);
		
		if (cobro.size() == 0) {
			salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CODIGO_COBRO_NO_EXISTE", ""));
			return salida;
			
		}
		
		ArrayList<DefinicionCobro_DB> defs = new ArrayList<>();
		
		DefinicionCobro in = entrada.p_DefinicionCobro;
		DefinicionCobro_DB aux = new DefinicionCobro_DB();
		aux.IdEmpresa                                       = entrada.p_IdEmpresa;
		aux.IdClaseCobro                                    = in.IdClaseCobro;
		aux.IdTipoCobro                                     = in.IdTipoCobro;
		aux.CodigoDefinicionCobro                           = in.CodigoDefinicionCobro;
		aux.IdEstado                                        = in.IdEstado;
		aux.Descripcion                                     = in.Descripcion;
		aux.CheckCobroMonedaOperacion                       = in.CheckCobroMonedaOperacion;
		aux.IdMonedaCobroOtraMoneda                         = in.IdMonedaCobroOtraMoneda;
		aux.IdTipoProducto									= in.IdTipoProducto;
		aux.MontoFijo_MontoCobro                            = in.MontoFijo_MontoCobro;
		aux.MontoFijo_CheckMontoOperacionMenor              = in.MontoFijo_CheckMontoOperacionMenor;
		aux.MontoFijo_ValorMontoOperacionMenor              = in.MontoFijo_ValorMontoOperacionMenor;
		aux.MontoFijo_CheckSiNo                             = in.MontoFijo_CheckSiNo;
		aux.MontoFijo_ValorMontoSiNo                        = in.MontoFijo_ValorMontoSiNo;
		aux.MontoFijoPorUnidad_MontoCobro					= in.MontoFijoPorUnidad_MontoCobro;
		aux.MontoFijoPorUnidad_AllInCheckValor				= in.MontoFijoPorUnidad_AllInCheckValor;
		
		aux.PorcentualFijo_ValorCobro                       = in.PorcentualFijo_ValorCobro; 
		aux.PorcentualFijo_CheckSobreMonto                  = in.PorcentualFijo_CheckSobreMonto;
		aux.PorcentualFijo_CheckSobreCantidad               = in.PorcentualFijo_CheckSobreCantidad;
		aux.PorcentualFijo_CheckMayorEntreDos               = in.PorcentualFijo_CheckMayorEntreDos;
		aux.PorcentualFijo_CheckMenorEntreDos               = in.PorcentualFijo_CheckMenorEntreDos;
		aux.PorcentualFijo_CheckMontoMinimo                 = in.PorcentualFijo_CheckMontoMinimo;
		aux.PorcentualFijo_ValorMontoMinimo                 = in.PorcentualFijo_ValorMontoMinimo;
		aux.PorcentualFijo_CheckMontoMaximo                 = in.PorcentualFijo_CheckMontoMaximo;
		aux.PorcentualFijo_ValorMontoMaximo                 = in.PorcentualFijo_ValorMontoMaximo;
		aux.PorcentualFijo_IdMonedaMinimoMaximo             = in.PorcentualFijo_IdMonedaMinimoMaximo;
		aux.PorcentualTasaPlazo_ValorCobro					= in.PorcentualTasaPlazo_ValorCobro;
		aux.Otros_CheckOperacionContado                     = in.Otros_CheckOperacionContado;
		aux.Otros_CheckOperacionPlazo                       = in.Otros_CheckOperacionPlazo;
		aux.Otros_CheckBaseTasa                             = in.Otros_CheckBaseTasa;
		aux.Otros_ValorBaseTasa                             = in.Otros_ValorBaseTasa;
		aux.Otros_CheckInteresSimple                        = in.Otros_CheckInteresSimple;
		aux.Otros_CheckInteresCompuesto                     = in.Otros_CheckInteresCompuesto;
		aux.Otros_CheckAllIn                                = in.Otros_CheckAllIn;
		aux.Otros_ValorAllInMontoOperacionBase              = in.Otros_ValorAllInMontoOperacionBase;
		aux.Otros_CheckAfectoImpuesto                       = in.Otros_CheckAfectoImpuesto;
		aux.Otros_CheckAplicaPortafolio                     = in.Otros_CheckAplicaPortafolio;
		aux.Otros_CheckAplicaCorresponsal                   = in.Otros_CheckAplicaCorresponsal;
		aux.Otros_CheckPermiteAumentarValor                 = in.Otros_CheckPermiteAumentarValor;
		aux.Otros_CheckPermiteDisminuirValor                = in.Otros_CheckPermiteDisminuirValor;
		aux.PorcentualRango_IdMonedaRangoMonto              = in.PorcentualRango_IdMonedaRangoMonto;
		aux.PorcentualRango_IdTipoRangoCobro                = in.PorcentualRango_IdTipoRangoCobro;
		aux.Otros_IncluyeExcluyeEmisoresCheck               = in.Otros_IncluyeExcluyeEmisoresCheck;
		aux.Otros_IncluyeExcluyeInstrumentosCheck           = in.Otros_IncluyeExcluyeInstrumentosCheck;
		aux.Otros_IncluyeExcluyeTiposDeClienteCheck			= in.Otros_IncluyeExcluyeTiposDeClienteCheck;
		aux.Otros_IncluyeExcluyeClientesCheck               = in.Otros_IncluyeExcluyeClientesCheck;
		aux.Otros_IncluyeExcluyeCorresponsalesCheck         = in.Otros_IncluyeExcluyeCorresponsalesCheck;
		aux.Otros_IncluyeExcluyeEmisoresValor				= in.Otros_IncluyeExcluyeEmisoresValor;
		aux.Otros_IncluyeExcluyeInstrumentosValor           = in.Otros_IncluyeExcluyeInstrumentosValor;
		aux.Otros_IncluyeExcluyeTiposDeClienteValor			= in.Otros_IncluyeExcluyeTiposDeClienteValor;
		aux.Otros_IncluyeExcluyeClientesValor               = in.Otros_IncluyeExcluyeClientesValor;
		aux.Otros_IncluyeExcluyeCorresponsalesValor         = in.Otros_IncluyeExcluyeCorresponsalesValor;
		
		defs.add(aux);
		
		ArrayList<DefinicionCobroRango_DB> rangos = new ArrayList<>();
		for (Rango inn: in.Rangos) {
			DefinicionCobroRango_DB r1 = new DefinicionCobroRango_DB();
			r1.IdEmpresa = entrada.p_IdEmpresa;
			r1.CodigoDefinicionCobro = aux.CodigoDefinicionCobro;
			r1.Hasta = inn.Hasta;
			r1.Valor = inn.Valor;
			rangos.add(r1);
		}
		
		ArrayList<DefinicionCobroEmisor_DB> emisores = new ArrayList<>();
		for (Emisor inn: in.Emisores) {
			DefinicionCobroEmisor_DB r1 = new DefinicionCobroEmisor_DB();
			r1.IdEmpresa = entrada.p_IdEmpresa;
			r1.CodigoDefinicionCobro = aux.CodigoDefinicionCobro;
			r1.IdEmisor = inn.IdEmisor;
			emisores.add(r1);
		}
		
		ArrayList<DefinicionCobroInstrumento_DB> instrumentos = new ArrayList<>();
		for (Instrumento inn: in.Instrumentos) {
			DefinicionCobroInstrumento_DB r1 = new DefinicionCobroInstrumento_DB();
			r1.IdEmpresa = entrada.p_IdEmpresa;
			r1.CodigoDefinicionCobro = aux.CodigoDefinicionCobro;
			r1.IdInstrumento = inn.IdInstrumento;
			instrumentos.add(r1);
		}
		
		ArrayList<DefinicionCobroTipoDeCliente_DB> tiposDeCliente = new ArrayList<>();
		for (TipoDeCliente inn: in.TiposCliente) {
			DefinicionCobroTipoDeCliente_DB r1 = new DefinicionCobroTipoDeCliente_DB();
			r1.IdEmpresa = entrada.p_IdEmpresa;
			r1.CodigoDefinicionCobro = aux.CodigoDefinicionCobro;
			r1.IdTipoCliente = inn.IdTipoCliente;
			tiposDeCliente.add(r1);
		}
		
		ArrayList<DefinicionCobroPortafolio_DB> portafolios = new ArrayList<>();
		for (Portafolio inn: in.Clientes) {
			DefinicionCobroPortafolio_DB r1 = new DefinicionCobroPortafolio_DB();
			r1.IdEmpresa = entrada.p_IdEmpresa;
			r1.CodigoDefinicionCobro = aux.CodigoDefinicionCobro;
			r1.IdPortafolio = inn.IdPortafolio;
			portafolios.add(r1);
		}
		
		ArrayList<DefinicionCobroCorresponsal_DB> corresponsales = new ArrayList<>();
		for (Corresponsal inn: in.CorredorasCorresponsales) {
			DefinicionCobroCorresponsal_DB r1 = new DefinicionCobroCorresponsal_DB();
			r1.IdEmpresa = entrada.p_IdEmpresa;
			r1.CodigoDefinicionCobro = aux.CodigoDefinicionCobro;
			r1.IdCorredoraCorresponsal = inn.IdCorredoraCorresponsal;
			corresponsales.add(r1);
		}
		
		sql.batch(conn, SQL_UPDATE_DEFINICION_COBRO, defs, DefinicionCobro_DB.class);
		
		ArrayList<Parametros> params = new ArrayList<Parametros>();
		Parametros p1 = new Parametros();
		p1.IdEmpresa = entrada.p_IdEmpresa;
		p1.CodigoDefinicionCobro = entrada.p_DefinicionCobro.CodigoDefinicionCobro;
		params.add(p1);
		
		sql.batch(conn, SQL_DELETE_DEFINICION_COBRO_RANGO, params, Parametros.class);
		sql.batch(conn, SQL_DELETE_DEFINICION_COBRO_EMISOR, params, Parametros.class);
		sql.batch(conn, SQL_DELETE_DEFINICION_COBRO_INSTRUMENTO, params, Parametros.class);
		sql.batch(conn, SQL_DELETE_DEFINICION_COBRO_TIPO_CLIENTE, params, Parametros.class);
		sql.batch(conn, SQL_DELETE_DEFINICION_COBRO_PORTAFOLIO, params, Parametros.class);
		sql.batch(conn, SQL_DELETE_DEFINICION_COBRO_CORRESPONSAL, params, Parametros.class);
		
		if (rangos.size() > 0) sql.batch(conn, SQL_INSERT_DEFINICION_RANGO, rangos, DefinicionCobroRango_DB.class);
		if (emisores.size() > 0) sql.batch(conn, SQL_INSERT_DEFINICION_COBRO_EMISOR, emisores, DefinicionCobroEmisor_DB.class);
		if (instrumentos.size() > 0) sql.batch(conn, SQL_INSERT_DEFINICION_COBRO_INSTRUMENTO, instrumentos, DefinicionCobroInstrumento_DB.class);
		if (tiposDeCliente.size() > 0) sql.batch(conn, SQL_INSERT_DEFINICION_COBRO_TIPO_CLIENTE, tiposDeCliente, DefinicionCobroTipoDeCliente_DB.class);
		if (portafolios.size() > 0) sql.batch(conn, SQL_INSERT_DEFINICION_COBRO_PORTAFOLIO, portafolios, DefinicionCobroPortafolio_DB.class);
		if (corresponsales.size() > 0) sql.batch(conn, SQL_INSERT_DEFINICION_COBRO_CORRESPONSAL, corresponsales, DefinicionCobroCorresponsal_DB.class);
		
		salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "COBRO_MODIFICADO", ""));
		return salida;
	}
	
	public static class Parametros {
		public Long IdEmpresa;
		public String CodigoDefinicionCobro;
	}
	
	public static class ModificarDefinicionCobro_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public DefinicionCobro p_DefinicionCobro;
		
		public ModificarDefinicionCobro_Entrada() {
			this.p_DefinicionCobro = new DefinicionCobro();
		}
		
	}
	
	
	public static class ModificarDefinicionCobro_Salida {
		public List<MensajeSalida> p_out_Mensajes;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ModificarDefinicionCobro_Salida() {
			p_out_Mensajes = new ArrayList<MensajeSalida>();
		}
		
	}
	
	public static void main (String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getQAPEConnection()){
			
			ModificarDefinicionCobro cdc = new ModificarDefinicionCobro();
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_DefinicionCobro\":{\"IdClaseCobro\":\"COMISION_CAVALI\",\"IdTipoCobro\":\"PORCENTAJETASAPLAZO\",\"CodigoDefinicionCobro\":\"COMISION CAVALI REPORTES PLAZO\",\"IdEstado\":\"ACT\",\"Descripcion\":\"COMISION CAVALI REPORTES PLAZO\",\"CheckCobroMonedaOperacion\":\"N\",\"IdMonedaCobroOtraMoneda\":\"USD\",\"IdTipoProducto\":\"RERV\",\"MontoFijo_MontoCobro\":0,\"MontoFijo_CheckMontoOperacionMenor\":\"N\",\"MontoFijo_ValorMontoOperacionMenor\":0,\"MontoFijo_CheckSiNo\":\"N\",\"MontoFijo_ValorMontoSiNo\":0,\"PorcentualFijo_ValorCobro\":0,\"PorcentualFijo_CheckSobreMonto\":\"N\",\"PorcentualFijo_CheckSobreCantidad\":\"N\",\"PorcentualFijo_CheckMayorEntreDos\":\"N\",\"PorcentualFijo_CheckMenorEntreDos\":\"N\",\"PorcentualFijo_CheckMontoMinimo\":\"N\",\"PorcentualFijo_ValorMontoMinimo\":0,\"PorcentualFijo_CheckMontoMaximo\":\"N\",\"PorcentualFijo_ValorMontoMaximo\":0,\"PorcentualFijo_IdMonedaMinimoMaximo\":\"\",\"PorcentualTasaPlazo_ValorCobro\":0.1,\"Otros_CheckOperacionContado\":\"N\",\"Otros_CheckOperacionPlazo\":\"S\",\"Otros_CheckBaseTasa\":\"N\",\"Otros_ValorBaseTasa\":1,\"Otros_CheckInteresSimple\":\"N\",\"Otros_CheckInteresCompuesto\":\"S\",\"Otros_CheckAllIn\":\"N\",\"Otros_ValorAllInMontoOperacionBase\":0,\"Otros_CheckAfectoImpuesto\":\"S\",\"Otros_CheckAplicaPortafolio\":\"S\",\"Otros_CheckAplicaCorresponsal\":\"N\",\"Otros_CheckPermiteAumentarValor\":\"N\",\"Otros_CheckPermiteDisminuirValor\":\"N\",\"PorcentualRango_IdTipoRangoCobro\":\"\",\"PorcentualRango_IdMonedaRangoMonto\":\"\",\"Otros_IncluyeExcluyeEmisoresCheck\":\"N\",\"Otros_IncluyeExcluyeInstrumentosCheck\":\"N\",\"Otros_IncluyeExcluyeTiposDeClienteCheck\":\"N\",\"Otros_IncluyeExcluyeClientesCheck\":\"N\",\"Otros_IncluyeExcluyeCorresponsalesCheck\":\"N\",\"Otros_IncluyeExcluyeEmisoresValor\":\"N\",\"Otros_IncluyeExcluyeInstrumentosValor\":\"N\",\"Otros_IncluyeExcluyeTiposDeClienteValor\":\"N\",\"Otros_IncluyeExcluyeClientesValor\":\"N\",\"Otros_IncluyeExcluyeCorresponsalesValor\":\"N\",\"Rangos\":[],\"Emisores\":[],\"Instrumentos\":[],\"TiposCliente\":[],\"Clientes\":[],\"CorredorasCorresponsales\":[]}}";
			ModificarDefinicionCobro_Entrada Objin = gson.fromJson(json, ModificarDefinicionCobro_Entrada.class);
			cdc.logica(conn, Objin);
			
			
			/*cdc_entrada.p_IdEmpresa = 1L;
			cdc_entrada.p_IdUsuario = 4L;
			cdc_entrada.p_IdModulo = "ORD";
			DefinicionCobro dc = new DefinicionCobro ();
			
			dc.IdClaseCobro = "COMISION"; 
			dc.IdTipoCobro = "MONTOFIJO"; 
			dc.CodigoDefinicionCobro = "CODIGO_TEST2"; 
			dc.IdEstado = "ACT";
			dc.Descripcion = "";
			dc.CheckCobroMonedaOperacion = "S"; 
			dc.IdMonedaCobroOtraMoneda = null;
			dc.MontoFijo_MontoCobro = new BigDecimal("5100");
			dc.MontoFijo_CheckMontoOperacionMenor = "N";
			dc.MontoFijo_ValorMontoOperacionMenor = BigDecimal.ZERO;
			dc.MontoFijo_CheckSiNo = "N";
			dc.MontoFijo_ValorMontoSiNo = BigDecimal.ZERO; 
			dc.PorcentualFijo_ValorCobro = BigDecimal.ZERO; 
			dc.PorcentualFijo_CheckSobreMonto = "N"; 
			dc.PorcentualFijo_CheckSobreCantidad = "N"; 
			dc.PorcentualFijo_CheckMayorEntreDos = "N"; 
			dc.PorcentualFijo_CheckMenorEntreDos = "N"; 
			dc.PorcentualFijo_CheckMontoMinimo = "N"; 
			dc.PorcentualFijo_ValorMontoMinimo = BigDecimal.ZERO; 
			dc.PorcentualFijo_CheckMontoMaximo = "N"; 
			dc.PorcentualFijo_ValorMontoMaximo = BigDecimal.ZERO; 
			dc.PorcentualFijo_IdMonedaMinimoMaximo = ""; 
			dc.Otros_CheckOperacionContado = "N";  
			dc.Otros_CheckOperacionPlazo = "N"; 
			dc.Otros_CheckBaseTasa = "N"; 
			dc.Otros_ValorBaseTasa = 0L;
			dc.Otros_CheckInteresSimple = "N";  
			dc.Otros_CheckInteresCompuesto = "N"; 
			dc.Otros_CheckAllIn = "N"; 
			dc.Otros_ValorAllInMontoOperacionBase = BigDecimal.ZERO; 
			dc.Otros_CheckAfectoImpuesto = "N";  
			dc.Otros_CheckAplicaPortafolio = "N";  
			dc.Otros_CheckAplicaCorresponsal = "N";  
			dc.Otros_CheckPermiteAumentarValor = "N"; 
			dc.Otros_CheckPermiteDisminuirValor = "N";
			cdc_entrada.p_DefinicionCobro = dc;
			
			Rango r = new Rango();
			r.Hasta = new BigDecimal(2);
			r.Valor = new BigDecimal(3);
			cdc_entrada.p_DefinicionCobro.Rangos = new ArrayList();
			cdc_entrada.p_DefinicionCobro.Rangos.add(r);
			
			TipoDeCliente t = new TipoDeCliente();
			t.IdTipoCliente = "";
			cdc_entrada.p_DefinicionCobro.TiposCliente = new ArrayList();
			cdc_entrada.p_DefinicionCobro.TiposCliente.add(t);
			
			Portafolio p = new Portafolio();
			p.IdPortafolio = 1L;
			cdc_entrada.p_DefinicionCobro.Clientes = new ArrayList();
			cdc_entrada.p_DefinicionCobro.Clientes.add(p);
			
			Corresponsal c = new Corresponsal();
			c.IdCorredoraCorresponsal = 1L;
			cdc_entrada.p_DefinicionCobro.CorredorasCorresponsales = new ArrayList();
			cdc_entrada.p_DefinicionCobro.CorredorasCorresponsales.add(c);
			
			Emisor e = new Emisor();
			e.IdEmisor = "CHILE";
			cdc_entrada.p_DefinicionCobro.Emisores = new ArrayList();
			cdc_entrada.p_DefinicionCobro.Emisores.add(e);
			
			Instrumento i = new Instrumento();
			i.IdInstrumento = 1368L;
			cdc_entrada.p_DefinicionCobro.Instrumentos = new ArrayList();
			cdc_entrada.p_DefinicionCobro.Instrumentos.add(i);
			
			cdc.logica(conn, cdc_entrada);
			conn.commit();*/

		}	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		ModificarDefinicionCobro_Entrada in=gson.fromJson(jin.toString() ,ModificarDefinicionCobro_Entrada.class);
		ModificarDefinicionCobro_Salida out=logica(conn,in);
		for (MensajeSalida msje : out.p_out_Mensajes) {
			msje.descripcion = Diccionario.getMensaje(conn,in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class Rango {
		public BigDecimal Valor;
		public BigDecimal Hasta;
	}
	
	public static class Emisor {
		public String IdEmisor;
	}
	
	public static class Instrumento {
		public Long IdInstrumento;
	}
	
	public static class TipoDeCliente {
		public String IdTipoCliente;
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
	}
	
	public static class Corresponsal {
		public Long IdCorredoraCorresponsal;
	}
	
	public static class DefinicionCobro {
		public String		IdClaseCobro; 
		public String		IdTipoCobro; 
		public String		CodigoDefinicionCobro; 
		public String		IdEstado; 
		public String		Descripcion; 
		public String		CheckCobroMonedaOperacion; 
		public String		IdMonedaCobroOtraMoneda; 
		public String		IdTipoProducto; 
		public BigDecimal	MontoFijo_MontoCobro; 
		public String		MontoFijo_CheckMontoOperacionMenor; 
		public BigDecimal	MontoFijo_ValorMontoOperacionMenor; 
		public String		MontoFijo_CheckSiNo; 
		public BigDecimal	MontoFijo_ValorMontoSiNo; 
		public String 		MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal 	MontoFijoPorUnidad_MontoCobro;
		public BigDecimal	PorcentualFijo_ValorCobro; 
		public String		PorcentualFijo_CheckSobreMonto; 
		public String		PorcentualFijo_CheckSobreCantidad; 
		public String		PorcentualFijo_CheckMayorEntreDos; 
		public String		PorcentualFijo_CheckMenorEntreDos; 
		public String		PorcentualFijo_CheckMontoMinimo; 
		public BigDecimal	PorcentualFijo_ValorMontoMinimo; 
		public String		PorcentualFijo_CheckMontoMaximo; 
		public BigDecimal	PorcentualFijo_ValorMontoMaximo; 
		public String		PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal	PorcentualTasaPlazo_ValorCobro;
		public String		Otros_CheckOperacionContado; 
		public String		Otros_CheckOperacionPlazo; 
		public String		Otros_CheckBaseTasa; 
		public Long			Otros_ValorBaseTasa; 
		public String		Otros_CheckInteresSimple; 
		public String		Otros_CheckInteresCompuesto; 
		public String		Otros_CheckAllIn; 
		public BigDecimal	Otros_ValorAllInMontoOperacionBase; 
		public String		Otros_CheckAfectoImpuesto; 
		public String		Otros_CheckAplicaPortafolio; 
		public String		Otros_CheckAplicaCorresponsal; 
		public String		Otros_CheckPermiteAumentarValor; 
		public String		Otros_CheckPermiteDisminuirValor; 
		public String		PorcentualRango_IdMonedaRangoMonto; 
		public String		PorcentualRango_IdTipoRangoCobro;
		public String		Otros_IncluyeExcluyeEmisoresCheck;
		public String		Otros_IncluyeExcluyeInstrumentosCheck;
		public String		Otros_IncluyeExcluyeTiposDeClienteCheck;
		public String		Otros_IncluyeExcluyeClientesCheck;
		public String		Otros_IncluyeExcluyeCorresponsalesCheck;
		public String		Otros_IncluyeExcluyeEmisoresValor;
		public String		Otros_IncluyeExcluyeInstrumentosValor;
		public String		Otros_IncluyeExcluyeTiposDeClienteValor;
		public String		Otros_IncluyeExcluyeClientesValor;
		public String		Otros_IncluyeExcluyeCorresponsalesValor;
		
		public List<Rango> Rangos;
		
		public List<Emisor> Emisores;
		public List<Instrumento> Instrumentos;
		public List<TipoDeCliente> TiposCliente;
		public List<Portafolio> Clientes;
		public List<Corresponsal> CorredorasCorresponsales;
		
		
	}
}






