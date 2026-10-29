package cl.bcs.plataforma.cre;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pau.AdelantarPauta;
import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar.Pauta;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class EntregarSistema implements JavaCall{	
	private final Log log = new Log("EntregarSistema");
	//QUERIES
	//[no se utilizan archivos sql para evitar exceso de archivos]
	private final String SQL_CALL_ENTREGARSISTEMA = "call \"BSCL.Plataforma.BD.Servicios::CRE.EntregaSistema\"( p_IdEmpresa => :p_IdEmpresa, p_IdUsuario => :p_IdUsuario, p_IdModulo => :p_IdModulo, p_simularOperacion => :p_simularOperacion, p_UsaReglasNegocio => :p_UsaReglasNegocio, p_out_Validaciones => $p_out_Validaciones, p_out_AdvertenciasContador => @p_out_AdvertenciasContador, p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle, p_out_ErroresContador => @p_out_ErroresContador, p_out_ErroresDetalle => $p_out_ErroresDetalle );";
	//CLASES
	public static class OutValidaciones {
		public String TipoRequisito;
		public String Error;
	}
	public static class OutAdvertenciasDetalle{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	public static class OutErroresDetalle{
		public String Codigo;
		public String Descripcion;
	}	
	//ENTRADA Y SALIDA DE LA CLASE
	public static class InEntregarSistema{
		public Long  		p_IdEmpresa;
		public Long  		p_IdUsuario;
	   	public String  		p_IdModulo;                                                                       
	   	public String   	p_simularOperacion;
	   	public String   	p_UsaReglasNegocio;
	   	public String		p_TipoProgramaAdelantar;
	   	public Long			p_SegundosDelayAdelantar;
	}
	public static class OutEntregarSistema{
		public Long	 		P_OUT_ADVERTENCIASCONTADOR;
		public Long	 		P_OUT_ERRORESCONTADOR;
		public String		p_out_SistemaCerrado;
		public String		p_out_TipoProgramaAdelantado;
		public List<OutValidaciones> 	p_out_Validaciones;
		public List<OutAdvertenciasDetalle> p_out_AdvertenciasDetalle;
		public List<OutErroresDetalle> 	p_out_ErroresDetalle;
		public OutEntregarSistema(){
			p_out_Validaciones = new ArrayList<OutValidaciones>();
			p_out_AdvertenciasDetalle = new ArrayList<OutAdvertenciasDetalle>();
			p_out_ErroresDetalle = new ArrayList<OutErroresDetalle>();
			p_out_SistemaCerrado = "N";
			p_out_TipoProgramaAdelantado="N";
		}
	}

	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InEntregarSistema in=gson.fromJson(jin.toString() ,InEntregarSistema.class);
		OutEntregarSistema out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public OutEntregarSistema logica(Connection con, InEntregarSistema entrada) throws Exception {
		boolean debug = true;
		SQL sql=new SQL();
		OutEntregarSistema salida = new OutEntregarSistema();		
		//Entrada
		HashMap<String,Object> parmsIn =new HashMap<String,Object>();
		parmsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parmsIn.put("p_IdUsuario", entrada.p_IdUsuario);
		parmsIn.put("p_IdModulo", entrada.p_IdModulo);
		parmsIn.put("p_simularOperacion", entrada.p_simularOperacion);
		parmsIn.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
		//Salida
		HashMap<String,Class> parmsOut =new HashMap<String,Class>();
		parmsOut.put("p_out_AdvertenciasContador", Long.class);
		parmsOut.put("p_out_ErroresContador", Long.class);
		parmsOut.put("p_out_Validaciones", OutValidaciones.class);
		parmsOut.put("p_out_AdvertenciasDetalle", OutAdvertenciasDetalle.class);
		parmsOut.put("p_out_ErroresDetalle", OutErroresDetalle.class);
		//Realiza el call
		HashMap<String,Object> respuestaES = sql.call(con, SQL_CALL_ENTREGARSISTEMA, parmsIn, parmsOut);	
		//Recuperar datos temporales globales de salida
		//Asigna Salida
		salida.P_OUT_ERRORESCONTADOR =  (Long)respuestaES.get("p_out_ErroresContador");
		salida.P_OUT_ADVERTENCIASCONTADOR = (Long)respuestaES.get("p_out_AdvertenciasContador");
		salida.p_out_ErroresDetalle = (List<OutErroresDetalle>)respuestaES.get("p_out_ErroresDetalle");
		salida.p_out_AdvertenciasDetalle = (List<OutAdvertenciasDetalle>)respuestaES.get("p_out_AdvertenciasDetalle");
		salida.p_out_ErroresDetalle = (List<OutErroresDetalle>)respuestaES.get("p_out_ErroresDetalle");
		salida.p_out_Validaciones = (List<OutValidaciones>)respuestaES.get("p_out_Validaciones");
		if(salida.P_OUT_ERRORESCONTADOR>0){
			log.write("Invocación a SP Hana EntregarSistema con error");
			//return salida;
		}else log.write("Invocación a SP Hana EntregarSistema sin error");
		//Una vez hechas las p_out_Validaciones de define cerrar automáticamente el sistema:
		
		log.write("Caso prueba pauta, de todos modos se cierra sistema!");
		
		CambiarEstadoModulo.InCambiarEstadoModulo incambest = new CambiarEstadoModulo.InCambiarEstadoModulo();
		incambest.IdEmpresa = entrada.p_IdEmpresa;
		incambest.TipoEstado = "C";
		incambest.IdModulo = "SIS";
        CambiarEstadoModulo cambest=new CambiarEstadoModulo();
        CambiarEstadoModulo.OutCambiarEstadoModulo outcambest = cambest.logica(con, incambest);        
        log.writeAsJson("OutCambiarEstadoModulo",outcambest);
    	if(outcambest.ErroresContador>0){
    		log.write("Invocación a servicio JAVA CambiarEstadoModulo con error");
    		if(!outcambest.TipoEstadoAnterior.equals("C")){
    			salida.P_OUT_ERRORESCONTADOR=salida.P_OUT_ERRORESCONTADOR+outcambest.ErroresContador;
        		OutErroresDetalle item = new OutErroresDetalle();
        		item.Codigo="CIERRE_SISTEMA_NO_REALIZADO";
        		item.Descripcion="No se ha realizado el cierre del módulo";
        		salida.p_out_ErroresDetalle.add(item);
        		return salida;
    		}else{
    			salida.p_out_SistemaCerrado = "S";
        		log.write("El sistema ya se encontraba cerrado, de todos modos se continúa ejecución");
    		}
    	}else {
    		salida.p_out_SistemaCerrado = "S";
    		log.write("Invocación a servicio JAVA CambiarEstadoModulo sin error");
    	}
    	//lógica de adelantamiento de la pauta
    	if(entrada.p_TipoProgramaAdelantar!=null||!entrada.p_TipoProgramaAdelantar.equals("")){
    		if(entrada.p_SegundosDelayAdelantar==null||entrada.p_SegundosDelayAdelantar<0)entrada.p_SegundosDelayAdelantar=0L;
    		String SQL_PautaAdelantar = "select \"IdTipoPrograma\",\"Descripcion\",\"MaximoIntentos\",\"Modulo\",\"NombreClasePrograma\",\"TipoClasePrograma\",\"EsPauta\",\"Ejecucion\",\"RequiereTipoPrograma\",\"SiguienteTipoPrograma\",\"IdPrograma\",\"IdGrupoParametro\",\"IdAgenda\",\"Intento\",\"IdUsuario\",\"IdEstadoPrograma\",\"NombreEstadoPrograma\",\"ResultadoPrograma\",\"FechaCreacionPrograma\",\"FechaEjecucionPrograma\",\"FechaFinPrograma\",\"FechaSistemaPrograma\",\"IdProgramaReprocesado\",\"IdControlPauta\",\"FechaSistema\",\"FechaCreacion\",\"IdEstado\",\"NombreEstado\",\"Mensaje\""
    				+ "from \"BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta\" where \"IdTipoPrograma\"=:IdTipoPrograma and \"FechaEjecucionPrograma\">ADD_SECONDS(CURRENT_UTCTIMESTAMP,"+entrada.p_SegundosDelayAdelantar.toString()+") and \"IdEstadoPrograma\"='PEN' order by \"FechaEjecucionPrograma\";";
    		ConsultarPautaEjecutar.Pauta pauta = new ConsultarPautaEjecutar.Pauta();
    		HashMap<String,Object> parmsPautaActual = new HashMap<String,Object>();
    		parmsPautaActual.put("IdTipoPrograma", entrada.p_TipoProgramaAdelantar);	
    		List<ConsultarPautaEjecutar.Pauta> resPautaActual = sql.query(con, SQL_PautaAdelantar,parmsPautaActual,ConsultarPautaEjecutar.Pauta.class);
    		if(resPautaActual.size()>0){//llamar adelantar pauta:
    			ConsultarPautaEjecutar.Pauta p=resPautaActual.get(0);
    			AdelantarPauta adp = new AdelantarPauta();
				AdelantarPauta.AdelantarPautaEntrada adpin = new AdelantarPauta.AdelantarPautaEntrada();
				adpin.p_IdEmpresa = entrada.p_IdEmpresa;
				adpin.p_IdUsuario = entrada.p_IdUsuario;
				adpin.p_IdModulo = "PLA";
				adpin.p_IdPrograma = p.IdPrograma;
				adpin.p_Segundos = entrada.p_SegundosDelayAdelantar;//30L;
				AdelantarPauta.AdelantarPautaSalida adpout = adp.logica(con, adpin);
				if(adpout.P_OUT_ERRORESCONTADOR>0){
					log.write("ADVERTENCIA!: Error al adelantar pauta(update): IdPrograma="+adpin.p_IdPrograma+",TipoPrograma="+entrada.p_TipoProgramaAdelantar);
				}else{
					log.write("Pauta adelantada con éxito(update): IdPrograma="+adpin.p_IdPrograma+",TipoPrograma="+entrada.p_TipoProgramaAdelantar);
				}
    		}else{//llamar crear programa:
    			AgregarPrograma agrprg = new AgregarPrograma();
    		   	AgregarPrograma.InAgregarPrograma inagrprg = new AgregarPrograma.InAgregarPrograma();
    		   	inagrprg.p_IdEmpresa = entrada.p_IdEmpresa;
    		   	inagrprg.p_IdUsuario = entrada.p_IdUsuario;
    		   	inagrprg.p_IdModulo = "PLA";
    		   	inagrprg.p_IdTipoPrograma = entrada.p_TipoProgramaAdelantar;
    		   	inagrprg.p_Segundos = entrada.p_SegundosDelayAdelantar;//30L;
    		   	AgregarPrograma.OutAgregarPrograma outagrprg = agrprg.logica(con, inagrprg);
    		   	if(outagrprg.P_OUT_ERRORESCONTADOR>0){
    		   		log.write("ADVERTENCIA!: Error al adelantar pauta(insert): TipoPrograma="+entrada.p_TipoProgramaAdelantar);
    		   	}else{
    		   		salida.p_out_TipoProgramaAdelantado="S";
    		   		log.write("Pauta adelantada con éxito(insert): IdPrograma="+outagrprg.P_OUT_IDPROGRAMA+",TipoPrograma="+entrada.p_TipoProgramaAdelantar);
    		   	}
    		}
    	}
		//Imprime en consola tablas de entrada y salida
		if(debug){
			log.writeAsJson("EntregarSistema>Entrada",entrada);
			log.writeAsJson("EntregarSistema>Salida",salida);
		}
		return salida;
	}
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        //prueba de la clase
		try {
			Connection con = ds.getConnection();
			InEntregarSistema entrada = new InEntregarSistema();
			entrada.p_IdEmpresa=1L;
			entrada.p_IdUsuario=37L;
			entrada.p_IdModulo="PLA";                                                                       
			entrada.p_simularOperacion="N";
			entrada.p_UsaReglasNegocio="N";
			entrada.p_TipoProgramaAdelantar="CAMBIO_FECHA_SISTEMA";
			EntregarSistema demo = new EntregarSistema();
			OutEntregarSistema salida = demo.logica(con, entrada);
			if(salida.P_OUT_ERRORESCONTADOR.equals(0L))con.commit();
			else con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
