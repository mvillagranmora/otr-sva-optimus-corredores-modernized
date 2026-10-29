package cl.bcs.custodia;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.businessobjects.reports.crprompting.CRPromptValue.DateTime;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.cuentaInversion.GuardarPatrimonioCaja;
import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.EntradaGuardarPatrimonioCaja;
import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.SalidaGuardarPatrimonioCaja;
import cl.bcs.custodia.CopiarPatrimonioConsolidadoParaInhabil.EntradaCopiarPatrimonioConsolidadoParaInhabil;
import cl.bcs.custodia.CopiarPatrimonioConsolidadoParaInhabil.SalidaCopiarPatrimonioConsolidadoParaInhabil;
import cl.bcs.custodia.GuardarPatrimonioCustodia.EntradaGuardarPatrimonioCustodia;
import cl.bcs.custodia.GuardarPatrimonioCustodia.SalidaGuardarPatrimonioCustodia;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;

import cl.bcs.utils.Utils;

public class GuardarPatrimonioRetroactivo implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(GuardarPatrimonioRetroactivo.class);
	
	   	     
	
	public static class EntradaGuardarPatrimonioRetroactivo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaInicio;
		public String p_FechaTermino;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EntradaFechasRetroactivo{
		public String p_FechaInicio;
		public String p_FechaTermino;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaFechasRetroactivoDesde{
		public DateTime p_out_FechaDesde;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaFechasRetroactivoHasta{
		public DateTime p_out_FechaHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGuardarPatrimonioRetroactivo{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGuardarPatrimonioRetroactivo() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaGuardarPatrimonioRetroactivo jin = gson.fromJson(in.toString(), EntradaGuardarPatrimonioRetroactivo.class);
		SalidaGuardarPatrimonioRetroactivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGuardarPatrimonioRetroactivo logica(Connection con, EntradaGuardarPatrimonioRetroactivo entrada) throws Exception {
		logger.info("INICIO::EntradaGuardarPatrimonioConsolidado");
		SalidaGuardarPatrimonioRetroactivo out = new SalidaGuardarPatrimonioRetroactivo();
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		if (!entrada.p_FechaInicio.isEmpty() && !entrada.p_FechaTermino.isEmpty()) {
			List<String> rangoFechas = obtenerRangoDeFechas(entrada.p_FechaInicio, entrada.p_FechaTermino); // PARA OBTENER RANGO DE FECHAS DE CONSULTA
			
			for(String fecha : rangoFechas) {
				Boolean esDiaHabil = Feriados.esDiaHabil(con, fecha, idPaisEmpresa);
				Boolean DiaSiguienteInHabil = DiaSiguienteInhabil(con, fecha, idPaisEmpresa);
				String fechaDia = fecha;
				if(esDiaHabil) { // SOLO SE EJECUTARA SI SE ENCUENTRA EN DIA HABIL
					try {
						
						logger.info("Comienza patrimonio custodia");
						EntradaGuardarPatrimonioCustodia entradaGuardarPatrimonioCustodia = new EntradaGuardarPatrimonioCustodia();
						entradaGuardarPatrimonioCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaGuardarPatrimonioCustodia.p_IdUsuario = entrada.p_IdUsuario;
						entradaGuardarPatrimonioCustodia.p_IdModulo = entrada.p_IdModulo;
						entradaGuardarPatrimonioCustodia.p_Fecha = fechaDia;
						GuardarPatrimonioCustodia guardarPatrimonioCustodia = new GuardarPatrimonioCustodia();
						SalidaGuardarPatrimonioCustodia salidaGuardarPatrimonioConsolidado = guardarPatrimonioCustodia.logica(con, entradaGuardarPatrimonioCustodia);
						out.P_OUT_ERRORESCONTADOR += salidaGuardarPatrimonioConsolidado.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES.addAll(salidaGuardarPatrimonioConsolidado.P_OUT_MENSAJES);
						logger.info("Finaliza patrimonio custodia");
					
					
						logger.info("Comienza patrimonio caja");
						EntradaGuardarPatrimonioCaja entradaGuardarPatrimonioCaja = new EntradaGuardarPatrimonioCaja();
						entradaGuardarPatrimonioCaja.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaGuardarPatrimonioCaja.p_IdUsuario = entrada.p_IdUsuario;
						entradaGuardarPatrimonioCaja.p_IdModulo = entrada.p_IdModulo;
						entradaGuardarPatrimonioCaja.p_Fecha = fechaDia;
						GuardarPatrimonioCaja guardarPatrimonioCaja = new GuardarPatrimonioCaja();
						SalidaGuardarPatrimonioCaja salidaGuardarPatrimonioCaja = guardarPatrimonioCaja.logica(con, entradaGuardarPatrimonioCaja);
						out.P_OUT_ERRORESCONTADOR += salidaGuardarPatrimonioCaja.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES.addAll(salidaGuardarPatrimonioCaja.P_OUT_MENSAJES);
						logger.info("Finaliza patrimonio caja");
						
						if (salidaGuardarPatrimonioConsolidado.P_OUT_ERRORESCONTADOR.equals(0L) && salidaGuardarPatrimonioCaja.P_OUT_ERRORESCONTADOR.equals(0L)) {
							  System.out.println("No hubo error");
							  con.commit();
							  
							  
							if(DiaSiguienteInHabil) { // EN EL CASO DE QUE EL DIA SIGUIENTE SEA INHABIL SE REALIZA COPIA DE PATRIMONIO
								logger.info("Comienza copia patrimonio dia inhabil");
								CopiarPatrimonioConsolidadoParaInhabil copiarPatrimonioConsolidadoParaInhabil = new CopiarPatrimonioConsolidadoParaInhabil();
					    		EntradaCopiarPatrimonioConsolidadoParaInhabil entradaCopiarPatrimonioConsolidadoParaInhabil = new EntradaCopiarPatrimonioConsolidadoParaInhabil();
					    		
					    		entradaCopiarPatrimonioConsolidadoParaInhabil.p_IdEmpresa = entrada.p_IdEmpresa;
					    		entradaCopiarPatrimonioConsolidadoParaInhabil.p_IdUsuario = entrada.p_IdUsuario;
					    		entradaCopiarPatrimonioConsolidadoParaInhabil.p_IdModulo  = "PLA";
					    		entradaCopiarPatrimonioConsolidadoParaInhabil.p_Fecha 	  = fechaDia;
					    		SalidaCopiarPatrimonioConsolidadoParaInhabil salidaCopiarPatrimonioConsolidadoParaInhabil = copiarPatrimonioConsolidadoParaInhabil.logica(con, entradaCopiarPatrimonioConsolidadoParaInhabil);
					    		out.P_OUT_ERRORESCONTADOR = salidaCopiarPatrimonioConsolidadoParaInhabil.P_OUT_ERRORESCONTADOR;
					    		logger.info("Finaliza copia patrimonio dia inhabil");
					    		if(salidaCopiarPatrimonioConsolidadoParaInhabil.P_OUT_ERRORESCONTADOR.equals(0L)) {
					    			System.out.println("No hubo error");
					    			con.commit();
					    			}
					    		else {
					    			
					    			con.rollback();
					    			return out;
					    			}
					    		
					    		
								}
							  
							}

					
					
					
				}
				catch(Exception er){
					er.printStackTrace();	
					con.rollback();
					return out;
				}
				}
			}
		}
		
		
		
		
		logger.info("FIN::EntradaGuardarPatrimonioRetroactivo");
		return out;
	}
	
	public static List<String> obtenerRangoDeFechas(String fechaInicio, String fechaTermino) {
        List<String> listaFechas = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate inicio = LocalDate.parse(fechaInicio, formatter);
        LocalDate termino = LocalDate.parse(fechaTermino, formatter);
        
        
        while (!inicio.isAfter(termino)) {
            listaFechas.add(inicio.format(formatter)); 
            inicio = inicio.plusDays(1); 
        }
        
        return listaFechas;
    }
	
	public static Boolean DiaSiguienteInhabil (Connection con, String fechaConsulta, Long idPaisEmpresa) {
		Boolean DiaSiguienteInhabil = false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fechaFormateada = LocalDate.parse(fechaConsulta, formatter);
        LocalDate diaSiguiente = fechaFormateada.plusDays(1);
        String FechaDiaSiguiente = diaSiguiente.format(formatter);
		try {
			 Boolean  esDiaInHabil = Feriados.esDiaHabil(con, FechaDiaSiguiente, idPaisEmpresa);
			 if (!esDiaInHabil) {
				 DiaSiguienteInhabil = true;
				 return DiaSiguienteInhabil;
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		
		return DiaSiguienteInhabil;

        
        
        
    }
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
			EntradaGuardarPatrimonioRetroactivo Objin = new EntradaGuardarPatrimonioRetroactivo();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "CUS";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaInicio = "2024-07-08";
		Objin.p_FechaTermino = "2024-07-08";
		GuardarPatrimonioRetroactivo  demo=new GuardarPatrimonioRetroactivo();				
		try {
			SalidaGuardarPatrimonioRetroactivo ObjOut=demo.logica(con, Objin);
			//con.commit();
			con.rollback();
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
