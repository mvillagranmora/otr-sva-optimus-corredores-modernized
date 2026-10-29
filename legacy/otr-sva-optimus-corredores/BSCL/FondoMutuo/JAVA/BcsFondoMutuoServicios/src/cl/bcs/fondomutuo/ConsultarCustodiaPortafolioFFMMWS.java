package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.EntradaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCustodiaPortafolioFFMMWS implements JavaCall{
	
	//private final String SQL_SELECT_MATRIZ_AGRUPADORA= loadResourceToString("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectMatrizAgrupadora.sql");
	private final String SQL_SELECT_MAESTRO_INSTRUMENTOS= loadResourceToString("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectMaestroInstrumentos.sql");
	private final String SQL_SELECT_CLIENTE_PORTAFOLIO= loadResourceToString("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectClientePortafolio.sql");
	private final String SQL_SELECT_EMISOR= loadResourceToString("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectEmisor.sql");
	private final String SQL_SELECT_VALOR_CUOTA= loadResourceToString("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectValorCuota.sql");
	
	
	
	//"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"  
	public static class WSConsultarCustodiaPortafolioFFMM_Entrada{
		
		public Long	  p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		
		public Long   p_IdCliente;
		public Long   p_IdPortafolio;
		public String p_FechaConsulta;
		public Long   p_IdInstrumento;		
		public Long   p_IdProductoAgrupador;
		public String p_DocumentoIdentidad;
		public String p_DocumentoIdentidadAdministradora;
		
		//Si es APV
		public String p_TipoCuenta;
		public String p_TipoAhorro;
		public String p_Origen;
		public String p_IdEmisor;
		public String p_IdSerieFondo;
		
		public WSConsultarCustodiaPortafolioFFMM_Entrada(){
			this.p_DocumentoIdentidad="";
			this.p_DocumentoIdentidadAdministradora="";
			this.p_IdCliente=0L;
			this.p_IdPortafolio=0L;
			this.p_IdInstrumento=0L;	
			this.p_IdProductoAgrupador=null;
			this.p_FechaConsulta="0001-01-01";
			
			this.p_TipoCuenta="";
			this.p_TipoAhorro="";
			this.p_Origen="";
			this.p_IdEmisor="";
			this.p_IdSerieFondo="";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class WSConsultarCustodiaPortafolioFFMM_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<ObjFondoMutuo> P_OUT_FFMM;
		
		public WSConsultarCustodiaPortafolioFFMM_Salida() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_FFMM =  new ArrayList<ObjFondoMutuo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ObjFiltros{
		public Long IdInterno;
		public String Filtro;
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjFondoMutuo{
		public  String  	IdClasificacionTipoInstrumento ;
		public  String  	NombreClasificacionTipoInstrumento ;
		public  String  	IdTipoInstrumento ;
		public  String  	NombreTipoInstrumento ;
		public  Long    	IdInstrumento ;
		public  String  	Nemotecnico ;
		public  Long    	IdSocioDeNegocio ;
		public  Long    	IdCliente ;
		public  Long    	IdPortafolio ;
		public  String  	DocumentoIdentidad ;
		public  String  	RazonSocial ;
		public  String  	CodigoPortafolio ;
		public  String  	IdBoveda ;
		public  String  	Relacion ;
		public  BigDecimal  CantidadCustodia ;
		public  BigDecimal  Monto ;
		public  String      IdMoneda ;
		public  BigDecimal  ValorCuota ;
		public  Long  		DecimalesValorCuota ;
		public  BigDecimal  CantidadGarantia ;
		public  BigDecimal  CantidadTransSolicitudesAporte ;
		public  BigDecimal  CantidadTransSolicitudesRescate ;
		public  BigDecimal  INGPrecioPromedioCompra ;
		public  BigDecimal  INGMontoPrecioPromedioCompra ;
		public  BigDecimal  INGMontoPrecioPromedioMonedaInstrumento ;
		public  String  	IdMonedaPrincipal ;
		public  String  	IdMonedaFondo ;
		public  String  	Producto ;
		public  BigDecimal  MontoMonedaSistema ;
		public  String  	DocumentoIdentidadAdministradora ;
		public  String  	NombreAdministradora ;
		public  String  	DescripcionTipoCuenta ;
		public  String  	TipoCuenta ;
		public  String  	DescripcionTipoAhorro ;
		public  String  	TipoAhorro ;
		public  String  	RegimenFondoOrigen ;
		public  String  	SerieFondoOrigen ;
		public  String  	ClasificacionMLT ;
		public  String  	Origen ;
		public  String  	FechaValorCuota ;
		public  String  		Fondo ;
		public  String  	NombreFondo ;
		public  Long  		IdMatrizAgrupador ;
		public  String  	EsADC ;
		public  String  	IdSerieFondo ;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ObjMatrizAgrupador{
		public Long IdCliente;
		public Long IdPortafolio;
		public String Producto;
		public String RegimenTributario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjMaestroInstrumento{
		public String IdSerieFondo;			
		public Long   IdInstrumento;			
		public String IdFondo;				
		public String CodigoSerie;			
		public String IdEmisor;				
		public String IdRegimenFondoMutuo;	
		public String DescripcionTipoCuenta;	
		public String TipoCuenta;			
		public String DescripcionTipoAhorro;	
		public String TipoAhorro;			
		public String RegimenTributario;     
		public String Regimen;				
		public String ClasificacionMLT;		
		public String DocumentoIdentidad;    
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjClientePortafolio {
		
		public Long IdCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ObjEmisor {
		
		public String IdEmisor;
		
		public ObjEmisor() {
			this.IdEmisor="";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ObjValorCuota{
		public String IdSerieFondo;
		public String FechaCierre;
		public BigDecimal ValorCuota;
		
		public ObjValorCuota() {
			this.IdSerieFondo="";
			this.FechaCierre="0001-01-01";
			this.ValorCuota=BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
		
	}
	
	public static class TipoCuenta {
		public String DescripcionTipoAhorro;
		public String DescripcionTipoCuenta;		
		public String TipoCuenta;
		public String TipoAhorro;
		public String RegimenTributario;
		public String RegimenFondoOrigen;
		
		public TipoCuenta (){
			this.DescripcionTipoAhorro="";
			this.DescripcionTipoCuenta="";		
			this.TipoCuenta="";
			this.TipoAhorro="";
			this.RegimenTributario="";
			this.RegimenFondoOrigen="";
			
		}
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	private WSConsultarCustodiaPortafolioFFMM_Salida logica(Connection con,WSConsultarCustodiaPortafolioFFMM_Entrada in) throws Exception {
		
	SQL sql = new SQL();
	WSConsultarCustodiaPortafolioFFMM_Salida out = new WSConsultarCustodiaPortafolioFFMM_Salida();
	
	String filtraADC 	 = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FiltroAdministracionCarteraWS");
	String APVHabilitado = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "APV.Habilitado");
	List<ObjMatrizAgrupador> 	MatrizAgrupadora = new ArrayList<ObjMatrizAgrupador>();
	List<ObjMaestroInstrumento> MaestroInstrumentos = new ArrayList<ObjMaestroInstrumento>();
	List<ObjClientePortafolio>  ClientePortafolio   = new ArrayList<ObjClientePortafolio>();
	List<ObjEmisor>  Emisor   = new ArrayList<ObjEmisor>();
	List<ObjValorCuota>  ValorCuota   = new ArrayList<ObjValorCuota>();
	List<ObjFondoMutuo>  ListaCustodia = new ArrayList<ObjFondoMutuo>();
	HashMap<String,Object> params = new HashMap<String,Object>();
	
	
	//SI APV ESTA ENCENDIDO
	if(("S").equals(APVHabilitado)) {
		
		//falta agregar parametros para filtrar y hacer mas rapidas las llamadas.
				
		//MatrizAgrupadora = sql.query(con, SQL_SELECT_MATRIZ_AGRUPADORA, params, ObjMatrizAgrupador.class);		
		MaestroInstrumentos = sql.query(con, SQL_SELECT_MAESTRO_INSTRUMENTOS, params, ObjMaestroInstrumento.class);		
		
	}
	
	//SI VIENE FILTRO DE DOCUMENTO IDENTIDAD SE BUSCA EL ID DEL CLIENTE	
	if(!("").equals(in.p_DocumentoIdentidad)) {		
		params.clear();
		params.put("DocumentoIdentidad", in.p_DocumentoIdentidad);
		ClientePortafolio = sql.query(con, SQL_SELECT_CLIENTE_PORTAFOLIO, params, ObjClientePortafolio.class);
	}
	
	//SI VIENE FILTRO DE DOCUMENTO IDENTIDAD ADMINISTRADORA SE BUSCA EL ID DEL CLIENTE		
	if(!("").equals(in.p_DocumentoIdentidadAdministradora)) {		
		params.clear();
		params.put("DocumentoIdentidad", in.p_DocumentoIdentidadAdministradora);
		Emisor = sql.query(con, SQL_SELECT_EMISOR, params, ObjEmisor.class);
	}
	
	
	//LLAMADA CONSULTA CUSTODIA PORTAFOLIO FFMM
	EntradaConsultarCustodiaPortafolioFondoMutuo InConsultaFFMM = new EntradaConsultarCustodiaPortafolioFondoMutuo();
	SalidaConsultarCustodiaPortafolioFondoMutuo  OutConsultaFFMM = new SalidaConsultarCustodiaPortafolioFondoMutuo();
	ConsultarCustodiaPortafolioFondoMutuo ConsultaFFMM = new ConsultarCustodiaPortafolioFondoMutuo();
	
	
	InConsultaFFMM.p_IdEmpresa= in.p_IdEmpresa;
	InConsultaFFMM.p_IdUsuario= in.p_IdUsuario;
	InConsultaFFMM.p_IdModulo= "FMM";
	InConsultaFFMM.p_IdCliente=("").equals(in.p_DocumentoIdentidad)? in.p_IdCliente:ClientePortafolio.get(0).IdCliente;
	InConsultaFFMM.p_IdPortafolio= in.p_IdPortafolio;
	InConsultaFFMM.p_IdInstrumento = in.p_IdInstrumento;	
	InConsultaFFMM.p_IdProductoAgrupador=in.p_IdProductoAgrupador;	
	if(Emisor.size()>0) {
		InConsultaFFMM.p_IdEmisor=Emisor.get(0).IdEmisor;
	}
	
	OutConsultaFFMM = ConsultaFFMM.logica(con, InConsultaFFMM);
	
	List<String> ListaIdSerieFondo = OutConsultaFFMM.P_OUT_TBL.stream().map(c->c.IdSerieFondo).collect(Collectors.toList());	
	params.clear();
	params.put("IdSerieFondo", ListaIdSerieFondo);
	ValorCuota = sql.query(con, SQL_SELECT_VALOR_CUOTA, params, ObjValorCuota.class);
	
	//PREPARA DATOS DE INSTRUMENTOS NO REPETIDOS
	HashMap<String,ObjMaestroInstrumento> HashInstrumentos = new HashMap<String,ObjMaestroInstrumento>();
	for(ObjMaestroInstrumento ins : MaestroInstrumentos) {
		String key = ins.IdEmisor+ins.IdSerieFondo+ins.ClasificacionMLT;
		
		if(HashInstrumentos.get(key)==null) {
			HashInstrumentos.put(key,ins);
		}
	}
	
	//PREPARA LA SALIDA CON LOS DATOS FALTANTES
	ListaCustodia.addAll(copyFondoMutuos(OutConsultaFFMM.P_OUT_TBL,ValorCuota,HashInstrumentos));
	
	if(("N").equals(APVHabilitado)) {
		out.P_OUT_FFMM = ListaCustodia;
	}else {		
		List<ObjFondoMutuo> ListaFFMM = filtrosAPV(ListaCustodia,in,filtraADC);
		out.P_OUT_FFMM = ListaFFMM;
	}
		
		return out;
	}
	
	public List<ObjFondoMutuo> filtrosAPV(List<ObjFondoMutuo> ListaFFMM  ,WSConsultarCustodiaPortafolioFFMM_Entrada in,String filtraADC) throws Exception{
		
		List<ObjFondoMutuo> ListaFiltrada = new ArrayList<ObjFondoMutuo>();
		List<ObjFondoMutuo> tmp = new ArrayList<ObjFondoMutuo>();
		
		if(!("").equals(in.p_TipoCuenta)) {
			tmp= ListaFFMM.stream().filter(c->c.TipoCuenta.equals(in.p_TipoCuenta)).collect(Collectors.toList());
			ListaFiltrada.addAll(tmp);
		}else if(!("").equals(in.p_Origen)) {
			 tmp = new ArrayList<ObjFondoMutuo>();
			 tmp= ListaFFMM.stream().filter(c->c.Origen.equals(in.p_Origen)).collect(Collectors.toList());
			 ListaFiltrada.addAll(tmp);
		}else if(!("").equals(in.p_TipoCuenta)) {
			 tmp = new ArrayList<ObjFondoMutuo>();
			 tmp= ListaFFMM.stream().filter(c->c.TipoCuenta.equals(in.p_TipoCuenta)).collect(Collectors.toList());
			 ListaFiltrada.addAll(tmp);
		}else if(!("").equals(in.p_IdEmisor)) {
			 tmp = new ArrayList<ObjFondoMutuo>();
			 tmp= ListaFFMM.stream().filter(c->c.NombreAdministradora.equals(in.p_IdEmisor)).collect(Collectors.toList());
			 ListaFiltrada.addAll(tmp);
		}else if(!("").equals(in.p_IdSerieFondo)) {
			 tmp = new ArrayList<ObjFondoMutuo>();
			 tmp= ListaFFMM.stream().filter(c->c.IdSerieFondo.equals(in.p_IdSerieFondo)).collect(Collectors.toList());
			 ListaFiltrada.addAll(tmp);
		}else if(!("").equals(filtraADC)) {
			 tmp = new ArrayList<ObjFondoMutuo>();
			 tmp= ListaFFMM.stream().filter(c->c.EsADC.equals("N")).collect(Collectors.toList());
			 ListaFiltrada.addAll(tmp);
		}
		
		return ListaFiltrada;
	}
	
	public List<ObjFondoMutuo> copyFondoMutuos (List<SalidaCustodia> P_OUT_TBL ,List<ObjValorCuota>  ListaValorCuota ,HashMap<String,ObjMaestroInstrumento> HashInstrumentos) throws Exception{
		
		List<ObjFondoMutuo> ListaFFMM = new ArrayList<ObjFondoMutuo>();
		Map<String,ObjValorCuota> HashValorCuota = ListaValorCuota.parallelStream().collect(Collectors.toMap(valorCuota->valorCuota.IdSerieFondo, valorCuota->valorCuota));
				
		for(SalidaCustodia cus : P_OUT_TBL ) {
			ObjFondoMutuo obj = new ObjFondoMutuo();
			TipoCuenta tp = new TipoCuenta();
			String key= cus.IdEmisor+cus.IdSerieFondo+cus.MLT;
			tp = DatosCuenta(cus);
			
					
			obj.IdClasificacionTipoInstrumento            =cus.IdClasificacionTipoInstrumento;
			obj.NombreClasificacionTipoInstrumento        =cus.NombreClasificacionTipoInstrumento;
			obj.IdTipoInstrumento                         =cus.IdTipoInstrumento;
			obj.NombreTipoInstrumento                     =cus.NombreTipoInstrumento;
			obj.IdInstrumento                             =cus.IdInstrumento;
			obj.Nemotecnico                               =cus.Nemotecnico;
			obj.IdSocioDeNegocio                          =cus.IdSocioDeNegocio;
			obj.IdCliente                                 =cus.IdCliente;
			obj.IdPortafolio                              =cus.IdPortafolio;
			obj.DocumentoIdentidad                        =cus.DocumentoIdentidad;
			obj.RazonSocial                               =cus.RazonSocial;
			obj.CodigoPortafolio                          =cus.CodigoPortafolio;
			obj.IdBoveda                                  =cus.IdBoveda;
			obj.Relacion                                  =cus.Relacion;
			obj.CantidadCustodia                          =cus.CantidadCustodia;
			obj.Monto                                     =cus.Monto;
			obj.IdMoneda                                  =cus.IdMoneda;
			obj.ValorCuota                                =HashValorCuota.get(cus.IdSerieFondo) != null ? HashValorCuota.get(cus.IdSerieFondo).ValorCuota : BigDecimal.ZERO;
			obj.DecimalesValorCuota                       =cus.DecimalesValorCuota;
			obj.CantidadGarantia                          =cus.CantidadGarantia;
			obj.CantidadTransSolicitudesAporte            =cus.CantidadTransSolicitudesAporte;
			obj.CantidadTransSolicitudesRescate           =cus.CantidadTransSolicitudesRescate;
			obj.INGPrecioPromedioCompra                   =cus.INGPrecioPromedioCompra;
			obj.INGMontoPrecioPromedioCompra              =cus.INGMontoPrecioPromedioCompra;
			obj.INGMontoPrecioPromedioMonedaInstrumento   =cus.INGMontoPrecioPromedioMonedaInstrumento;
			obj.IdMonedaPrincipal                         =cus.IdMonedaPrincipal;
			obj.IdMonedaFondo                             =cus.IdMonedaFondo;
			obj.Producto                                  =cus.Producto;
			obj.MontoMonedaSistema                        =cus.MontoMonedaSistema;
			obj.DocumentoIdentidadAdministradora          =HashInstrumentos.get(key)!=null ? HashInstrumentos.get(key).DocumentoIdentidad:"";
			obj.NombreAdministradora                      =HashInstrumentos.get(key)!=null ? HashInstrumentos.get(key).IdEmisor:"";          
			obj.DescripcionTipoCuenta                     =tp.DescripcionTipoCuenta;
			obj.TipoCuenta                                =cus.IdRegimenFondoMutuo;
			obj.DescripcionTipoAhorro                     =tp.DescripcionTipoAhorro;
			obj.TipoAhorro                                =tp.TipoAhorro;
			obj.RegimenFondoOrigen                        =cus.IdRegimenFondoMutuo;  //tp.RegimenFondoOrigen
			obj.SerieFondoOrigen                          =HashInstrumentos.get(key)!=null ? HashInstrumentos.get(key).CodigoSerie:"";       
			obj.ClasificacionMLT                          =HashInstrumentos.get(key)!=null ? HashInstrumentos.get(key).ClasificacionMLT:"";  
			obj.Origen                                    =cus.Producto!=null?"APV":"NOAPV";
			obj.FechaValorCuota                           =HashValorCuota.get(cus.IdSerieFondo) != null ? HashValorCuota.get(cus.IdSerieFondo).FechaCierre :"0001-01-01";
			obj.Fondo                                     =cus.Fondo;
			obj.NombreFondo                               =cus.NombreFondo;
			obj.IdMatrizAgrupador                         =cus.IdMatrizAgrupador;
			obj.EsADC                                     =cus.EsADC;
			obj.IdSerieFondo                              =cus.IdSerieFondo;
			
			ListaFFMM.add(obj);
		}
		
		return ListaFFMM;
	}
	
	public TipoCuenta DatosCuenta (SalidaCustodia objCustodia) {
		TipoCuenta tp = new TipoCuenta();
		
		String subReg [] = objCustodia.IdRegimenFondoMutuo.split("-");
		
		if(objCustodia.IdRegimenFondoMutuo.equals("APV-A")||objCustodia.IdRegimenFondoMutuo.equals("APV-B")||objCustodia.IdRegimenFondoMutuo.equals("BF")) {	
			tp.DescripcionTipoAhorro = "AHORRO PREVISIONAL VOLUNTARIO";
			
			if(objCustodia.IdRegimenFondoMutuo.equals("BF")) {
				tp.DescripcionTipoCuenta= "BONIFICACION FISCAL";
				tp.RegimenFondoOrigen= "";
			}else {
				tp.DescripcionTipoCuenta= "AHORRO PREVISIONAL VOLUNTARIO"+" "+(objCustodia.IdRegimenFondoMutuo.equals("APV-B")?"B":"A");
				tp.RegimenFondoOrigen= objCustodia.IdRegimenFondoMutuo.equals("APV-B")?"B":"A";
			}
			tp.TipoAhorro="APV";
		}
		else if (objCustodia.IdRegimenFondoMutuo.equals("18T")||objCustodia.IdRegimenFondoMutuo.equals("18Q")) {
			tp.DescripcionTipoAhorro= "REGIMEN NORMAL";
			tp.DescripcionTipoCuenta= tp.DescripcionTipoAhorro;
			tp.TipoAhorro="RTN";
			tp.RegimenFondoOrigen=objCustodia.IdRegimenFondoMutuo;
		}
		else if(objCustodia.IdRegimenFondoMutuo.equals("57B")||objCustodia.IdRegimenFondoMutuo.equals("57T")) {			
			tp.DescripcionTipoAhorro= "57 BIS";
			tp.DescripcionTipoCuenta= tp.DescripcionTipoAhorro;
			tp.TipoAhorro="57B";
			tp.RegimenFondoOrigen=objCustodia.IdRegimenFondoMutuo;
		}
		else if( subReg.length>1  &&  subReg [1].equals("DC")) {
			tp.DescripcionTipoAhorro= "'DEPOSITO CONVENIDO'";
			tp.DescripcionTipoCuenta= tp.DescripcionTipoAhorro+" "+objCustodia.IdRegimenFondoMutuo;
			tp.TipoAhorro="DC";
			tp.RegimenFondoOrigen= "";			
		}
		
		tp.TipoCuenta=objCustodia.IdRegimenFondoMutuo;
		
		return tp;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		WSConsultarCustodiaPortafolioFFMM_Entrada jin = gson.fromJson(in.toString(), WSConsultarCustodiaPortafolioFFMM_Entrada.class);
		WSConsultarCustodiaPortafolioFFMM_Salida out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_DocumentoIdentidad\":\"15983577-4\",\"p_IdCliente\":0,\"p_IdPortafolio\":0,\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"N\",\"p_IdProductoAgrupador\":0,\"p_TipoCuenta\":\"\",\"p_TipoAhorro\":\"\",\"p_Origen\":\"\",\"p_IdEmisor\":\"\",\"p_IdSerieFondo\":\"\"}";
			WSConsultarCustodiaPortafolioFFMM_Entrada Objin = gson.fromJson(json, WSConsultarCustodiaPortafolioFFMM_Entrada.class);
			
			/*WSConsultarCustodiaPortafolioFFMM_Entrada Objin = new WSConsultarCustodiaPortafolioFFMM_Entrada();
			Objin.p_IdEmpresa=1L;
			Objin.p_IdUsuario=21L;
			Objin.p_IdModulo="FMM";
			Objin.p_DocumentoIdentidad="15983577-4";*/
			
			
			ConsultarCustodiaPortafolioFFMMWS  demo=new ConsultarCustodiaPortafolioFFMMWS();				
			try {
				WSConsultarCustodiaPortafolioFFMM_Salida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
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
