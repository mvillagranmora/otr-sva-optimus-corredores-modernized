package cl.bcs.plataforma.pau;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pau.utils.PautaUtils;
import cl.bcs.plataforma.pla.CambiarFechaSistema;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.replica.ReplicaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

import cl.bcs.custodia.RespaldarCustodia;
import cl.bcs.custodia.RespaldarCustodia.Salida;

public class EjecutarProcesosDiaInhabil implements JavaCall {	
	private Log log = null;
	
	public static class Entrada {
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdModulo;
		public Long IdPais;
		public String ultimoDia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EjecutarProcesosSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public EjecutarProcesosSalida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJE = "";
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();		
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		EjecutarProcesosSalida out=logica(con,in, true);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


	public EjecutarProcesosSalida logica(Connection conn, Entrada entrada, boolean debug) throws Exception {
		EjecutarProcesosSalida out = new EjecutarProcesosSalida();
		
    	String rutaBaseArchivos = Utils.getConfiguracion(conn, Utils.getSchema(), entrada.IdEmpresa, "PLA.RutaBaseArchivos");
		if (debug) {
			rutaBaseArchivos = "C:\\";
		}
		log = new Log("PLA.EjecutarProcesosDiaInhabil",rutaBaseArchivos,"PAU","yyyyMMdd_HHmmss");
		log.write("ENTRADA FECHA ULTIMO DIA :" ,entrada.ultimoDia );
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.IdEmpresa, entrada.IdUsuario, entrada.IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			out.P_OUT_MENSAJE = "Error Requisitos Basicos \n";
			
		}
		
		
		//Valorizar Cartera RF
        ValorizarCarteraRF.ValorizarCarteraRFEntrada valorizarCarteraParametrosRF = new ValorizarCarteraRF.ValorizarCarteraRFEntrada();
        valorizarCarteraParametrosRF.p_FechaSistema = entrada.ultimoDia;
        valorizarCarteraParametrosRF.p_IdEmpresa = entrada.IdEmpresa;
        valorizarCarteraParametrosRF.p_IdPais = entrada.IdPais;
        valorizarCarteraParametrosRF.p_IdUsuario = entrada.IdUsuario;
        valorizarCarteraParametrosRF.p_SimularOperacion = "N";
		ValorizarCarteraRF valorizarCarteraRF = new ValorizarCarteraRF();
		ValorizarCarteraRF.ValorizarCarteraRFSalida valorizarCarteraRFSalidaOut = new ValorizarCarteraRF.ValorizarCarteraRFSalida(); 
		
		try {
			valorizarCarteraRFSalidaOut = valorizarCarteraRF.logica(conn, valorizarCarteraParametrosRF);
			log.write("EjecutarProcesosDiaInhabil: Proceso Valorizar Cartera RF -- OK");
		} catch (Throwable e) {
			out.P_OUT_ERRORESCONTADOR = valorizarCarteraRFSalidaOut.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJE = "Error Valorizar Cartera RF \n";
			e.printStackTrace();
			
		}
		
        //Valorizar Cartera IF
        ValorizarCarteraIF.ValorizarCarteraIFEntrada valorizarCarteraParametrosIF = new ValorizarCarteraIF.ValorizarCarteraIFEntrada();
        valorizarCarteraParametrosIF.p_FechaSistema = entrada.ultimoDia;
        valorizarCarteraParametrosIF.p_IdEmpresa = entrada.IdEmpresa;
        valorizarCarteraParametrosIF.p_IdPais = entrada.IdPais;
        valorizarCarteraParametrosIF.p_IdUsuario = entrada.IdUsuario;
        valorizarCarteraParametrosIF.p_SimularOperacion = "N";
		ValorizarCarteraIF valorizarCarteraIF = new ValorizarCarteraIF();
		ValorizarCarteraIF.ValorizarCarteraIFSalida valorizarCarteraIFSalidaOut = new ValorizarCarteraIF.ValorizarCarteraIFSalida(); 
		try {
			valorizarCarteraIFSalidaOut = valorizarCarteraIF.logica(conn, valorizarCarteraParametrosIF);
			log.write("EjecutarProcesosDiaInhabil: Proceso Valorizar Cartera IF -- OK");
		} catch (Throwable e) {
			out.P_OUT_ERRORESCONTADOR = valorizarCarteraIFSalidaOut.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJE = "Error Valorizar Cartera IF \n";
			e.printStackTrace();
			
		}
	
		//Valorizar Cartera Pactos
		ValorizarCarteraPactos.ValorizarCarteraPactosEntrada valorizarCarteraPactosParametros = new ValorizarCarteraPactos.ValorizarCarteraPactosEntrada(); 
		valorizarCarteraPactosParametros.p_FechaSistema = entrada.ultimoDia;
		valorizarCarteraPactosParametros.p_IdEmpresa = entrada.IdEmpresa;
		valorizarCarteraPactosParametros.p_IdPais = entrada.IdPais;
		valorizarCarteraPactosParametros.p_IdUsuario = entrada.IdUsuario;
		valorizarCarteraPactosParametros.p_SimularOperacion = "N";
		ValorizarCarteraPactos valorizarCarteraPactos = new ValorizarCarteraPactos();
		ValorizarCarteraPactos.ValorizarCarteraPactosSalida valorizarCarteraPactosOut = new ValorizarCarteraPactos.ValorizarCarteraPactosSalida(); 
		try {
			valorizarCarteraPactosOut = valorizarCarteraPactos.logica(conn, valorizarCarteraPactosParametros);
			log.write("EjecutarProcesosDiaInhabil: Proceso Valorizar Cartera Pactos -- OK");
		} catch (Throwable e) {
			out.P_OUT_ERRORESCONTADOR = valorizarCarteraPactosOut.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJE = "Error Valorizar Cartera Pactos \n";
			e.printStackTrace();
			
		}
		
        
		//Respaldar Custodias
		RespaldarCustodia respaldarCustodia = new RespaldarCustodia();
		RespaldarCustodia.Entrada entradaCustodia = new RespaldarCustodia.Entrada();
		entradaCustodia.p_FechaRespaldo = entrada.ultimoDia;
		Salida salidaRespaldar = new Salida(); 
		try {
			salidaRespaldar = respaldarCustodia.logica(conn, entradaCustodia);
			log.write("EjecutarProcesosDiaInhabil: Proceso Respaldar Custodias -- OK");
		} catch (Throwable e) {
			out.P_OUT_ERRORESCONTADOR = salidaRespaldar.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJE = "Error Respaldo Custodia \n";
			e.printStackTrace();
			
		}
		
		String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,entrada.IdEmpresa,"PLATAFORMA","PLA.FechaSistema");
		
		
		try {
			log.write("Iniciando respaldo replica Sebra para dia inhabil");
			ReplicaUtils utils = new ReplicaUtils();
			utils.procesarRespaldoSebra(conn,confFechaSistema,entrada.IdEmpresa);
		}
		catch (Exception e) {
			log.write(e);
		}
				
		// cambio de fecha 
	
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
    	PautaUtils pautaUtil = new PautaUtils();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, entrada.IdEmpresa);
		
        ObtenerDiaSiguiente dsig = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.InObtenerDiaSiguiente dsigin = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();	
		
		String esFinMesInhabil = "S";
		dsigin.p_idPais = empresa.IdPais;
		dsigin.p_diaHabil = esFinMesInhabil;
		dsigin.p_soloCaeDiaHabil = "S";
		
		ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn itemFec = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
		itemFec.fechaInicial = confFechaSistema;
		itemFec.numeroDias = 1L;
		dsigin.p_fechas.add(itemFec);
		log.write("ObtenerDiaSiguiente : obteniendo proximo dia habil");
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente dsigout = dsig.logica(conn, dsigin);				
		log.write("Siguiente dia Habil  : ",dsigout.p_out_fechas.get(0).fechaCalculada);
		CambiarFechaSistema cf = new CambiarFechaSistema();
		CambiarFechaSistema.CambiarFechaSistemaEntrada cfin = new CambiarFechaSistema.CambiarFechaSistemaEntrada();
		cfin.p_IdEmpresa = entrada.IdEmpresa;
		cfin.p_FechaSistema = dsigout.p_out_fechas.get(0).fechaCalculada;
		cfin.p_FechaSistemaActual = confFechaSistema;
		log.write("INICIO CambiarFechaSistema   : ");
		CambiarFechaSistema.CambiarFechaSistemaSalida cfout = cf.logica(conn, cfin);
		log.write("FIN CambiarFechaSistema   : ");
			
	
		return out;
	}
	

	public static void main(String[] args) throws Throwable {
		
		EjecutarProcesosDiaInhabil demo=new EjecutarProcesosDiaInhabil();		
		Entrada entrada = new Entrada();
		entrada.IdEmpresa = 1L;
		entrada.IdUsuario = 97L;
		entrada.IdModulo = "PLA";
		entrada.ultimoDia="2024-03-31";

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02","c:/archivos/connections.properties")) {
			System.out.println(demo.logica(conn, entrada, true).toString());
			//conn.rollback();
			conn.commit();
		} catch (Exception e) {
    		e.printStackTrace();
    	}
	}   
}
