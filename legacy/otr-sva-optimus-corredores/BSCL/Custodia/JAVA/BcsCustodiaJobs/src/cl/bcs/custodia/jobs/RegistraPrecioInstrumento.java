package cl.bcs.custodia.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RegistraPrecioInstrumento implements JavaCall {	
	private final String SELECT_INSTRUMENTOS  = loadResourceToString("cl/bcs/custodia/jobs/sql/RegistraPrecioBolsaInstrumento_Select_Instrumentos.sql");
	private final String DELETE_INSTRUMENTOS_HISTORIA  = loadResourceToString("cl/bcs/custodia/jobs/sql/RegistraPrecioBolsaInstrumento_Delete_Instrumentos_Historia.sql");
	private final String DELETE_INSTRUMENTOS  = loadResourceToString("cl/bcs/custodia/jobs/sql/RegistraPrecioBolsaInstrumento_Delete_Instrumentos.sql");
	private final String INSERT_INSTRUMENTOS  = loadResourceToString("cl/bcs/custodia/jobs/sql/RegistraPrecioBolsaInstrumento_Insert_Instrumentos.sql");

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarInstrumentoPrecioBolsa_Entrada jin = gson.fromJson(in.toString(), ConsultarInstrumentoPrecioBolsa_Entrada.class);
		ConsultarInstrumentoPrecioBolsa_Salida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static class ConsultarInstrumentoPrecioBolsa_Entrada {
		public Long p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_Fecha; //opcional, cuando se necesite calcular el precio de una fecha especifica
		public List<Instrumento> p_Instrumentos;		
		
		public ConsultarInstrumentoPrecioBolsa_Entrada(){
			p_Instrumentos = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Instrumento{
		public Long IdInterno;
		public Long IdEmpresa;
		public Long IdInstrumento; 
		public String Fecha;//oblig
		public String IdMoneda;
		public String IdClasificacionTipoInstrumento;//oblig	
		public String IdTipoInstrumento; 				
		public String IdCondicionDeLiquidacion;
		public String Nemotecnico;
		
		public Instrumento() {
			
		};
	}
	
	public static class ConsultarInstrumento_Salida {
		public List<DatosConsultaInstrumento_Salida> p_Instrumentos;
		
		public ConsultarInstrumento_Salida() {
			p_Instrumentos = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosConsultaInstrumento_Salida{
		public String IdClasificacionTipoInstrumento;	
		public String IdTipoInstrumento; 				
		public Long IdInstrumento; 					
		public String Nemotecnico; 	
		public String IdMoneda;
		public String Fecha;
	}

	
	public static class ConsultarInstrumentoPrecioBolsa_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public List<RegistraPrecioBolsaInstrumento_Calculado> P_OUT_INSTRUMENTO;
		
		public ConsultarInstrumentoPrecioBolsa_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_INSTRUMENTO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class PaisEmpresa {
		public Long IdPais;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class RegistraPrecioBolsaInstrumento_Calculado {
		public Long 				IdEmpresa;
		public Long 				IdInstrumento;
		public BigDecimal 			Precio;
		public String 				FechaSistema;
		public String 				CodigoFuente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarInstrumentoPrecioBolsa_Salida logica(Connection conn, ConsultarInstrumentoPrecioBolsa_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		
		ConsultarInstrumentoPrecioBolsa_Salida salida = new ConsultarInstrumentoPrecioBolsa_Salida();
		EntregarPreciosTasas_Salida entregarPreciosTasasSalida = new EntregarPreciosTasas_Salida();
		
		List<Long> idInstrumentos = new ArrayList<>();
		List<String> idTiposInstrumentos = new ArrayList<>();
		List<String> nemos = new  ArrayList<>();
		List<String> idClasificaciones = new ArrayList<>();
		
		HashMap<String,Object> filtrosConsulta = new HashMap<>();
		{	
			filtrosConsulta.put("filtroPorId", 1);
			filtrosConsulta.put("filtroPorTipo", 1);
			filtrosConsulta.put("filtroPorNemo", 1);
			filtrosConsulta.put("filtroPorClasificacion",1);
			
			idInstrumentos = entrada.p_Instrumentos.stream().filter(a -> a.IdInstrumento != null).map(e -> e.IdInstrumento).distinct().collect(Collectors.toList());
			idInstrumentos.forEach(e -> filtrosConsulta.put("filtroPorId", 0));
			
			idTiposInstrumentos = entrada.p_Instrumentos.stream().filter(a -> a.IdTipoInstrumento != null).map(e -> e.IdTipoInstrumento).distinct().collect(Collectors.toList());
			idTiposInstrumentos.forEach(e -> filtrosConsulta.put("filtroPorTipo", 0));
			
			nemos =  entrada.p_Instrumentos.stream().filter(a -> a.Nemotecnico != null).map(e -> e.Nemotecnico).distinct().collect(Collectors.toList());
			nemos.forEach(e -> filtrosConsulta.put("filtroPorNemo", 0));
			
			idClasificaciones = entrada.p_Instrumentos.stream().filter(a -> a.IdClasificacionTipoInstrumento != null).map(e -> e.IdClasificacionTipoInstrumento).distinct().collect(Collectors.toList());
			idClasificaciones.forEach(e -> filtrosConsulta.put("filtroPorClasificacion",0));
			
			filtrosConsulta.put("idInstrumentos", idInstrumentos);
			filtrosConsulta.put("idTiposInstrumentos", idTiposInstrumentos);
			filtrosConsulta.put("nemos", nemos);
			filtrosConsulta.put("idClasificaciones",idClasificaciones);
	    }
		
		List<DatosConsultaInstrumento_Salida> instrumentos = _sql.query(conn, SELECT_INSTRUMENTOS, filtrosConsulta, DatosConsultaInstrumento_Salida.class);
		
		 entrada.p_IdEmpresa = entrada.p_IdEmpresa == null ? entrada.p_Instrumentos.get(0).IdEmpresa : entrada.p_IdEmpresa;
		
		if(instrumentos.size() == 0){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SIN_INSTRUMENTOS_PARA_FILTROS_CONSULTADOS",null,conn,entrada.p_IdEmpresa));				
			return salida;
		}
		
		List<Instrumento> instrumentosACargar = new ArrayList<Instrumento>();
		
		for(Instrumento insentrada: entrada.p_Instrumentos){
			for (DatosConsultaInstrumento_Salida inssalida : instrumentos){	
				entrada.p_IdEmpresa = insentrada.IdEmpresa;//para aquellos que no traen IdEmpresa en la entrada
				if(insentrada.IdClasificacionTipoInstrumento != null && insentrada.Nemotecnico == null && insentrada.IdInstrumento == null 
							&& insentrada.IdClasificacionTipoInstrumento.equalsIgnoreCase(inssalida.IdClasificacionTipoInstrumento)){
					instrumentosACargar.add(CargarInstrumento(insentrada, inssalida, entrada.p_IdEmpresa));
			    }else if(insentrada.IdInstrumento != null){ 
			    	if(inssalida.IdInstrumento.equals(insentrada.IdInstrumento)){
						instrumentosACargar.add(CargarInstrumento(insentrada, inssalida, entrada.p_IdEmpresa));
			    	}	
			    }else if(insentrada.Nemotecnico != null){
			    	if(inssalida.Nemotecnico.equals(insentrada.Nemotecnico)){
						instrumentosACargar.add(CargarInstrumento(insentrada, inssalida, entrada.p_IdEmpresa));
			    	}	
			    } 
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		if (entrada.p_Fecha != null) {
			fechaSistema = entrada.p_Fecha;
		}
		
		{	
			EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
							
			entregarPreciosTasasEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			entregarPreciosTasasEntrada.p_IdModulo = "PLA";
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		    InformacionEmpresa empresa = consultarEmpresaService.logica(conn, entrada.p_IdEmpresa);		
		    entregarPreciosTasasEntrada.p_IdPais = empresa.IdPais;
			entregarPreciosTasasEntrada.p_IdUsuario = entrada.p_IdUsuario;
						
			for (Instrumento ins : instrumentosACargar) {
							EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
							entradaDatosInstrumentos.IdRegistro 					= 1L;
							entradaDatosInstrumentos.TipoCartera 					= "0";
							entradaDatosInstrumentos.IdUso 							= "CARTERA";
							entradaDatosInstrumentos.IdTipoCriterioTasa 			= "0";
							entradaDatosInstrumentos.IdClasificacionTipoInstrumento	= ins.IdClasificacionTipoInstrumento;
							entradaDatosInstrumentos.IdTipoInstrumento 				= ins.IdTipoInstrumento;
							entradaDatosInstrumentos.IdInstrumento 					= ins.IdInstrumento;
							entradaDatosInstrumentos.Nemotecnico 					= ins.Nemotecnico;
							entradaDatosInstrumentos.IdMoneda 						= ins.IdMoneda;
							entradaDatosInstrumentos.FechaConsulta 					= Fecha.toDate(fechaSistema);  
							entradaDatosInstrumentos.PlazoResidual 					= 0L;
							entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
			}
					
			if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
				 EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
				 entregarPreciosTasasSalida = entregarPreciosTasasService.logica(conn, entregarPreciosTasasEntrada);
				 
				 if(entregarPreciosTasasSalida.P_OUT_ERRORESCONTADOR != null && entregarPreciosTasasSalida.P_OUT_ERRORESCONTADOR  > 0){
					 salida.P_OUT_ERRORESCONTADOR = entregarPreciosTasasSalida.P_OUT_ERRORESCONTADOR;
					 salida.P_OUT_MENSAJES = entregarPreciosTasasSalida.P_OUT_MENSAJES;
					 return salida;
				 }
			}
		}
			
		List<RegistraPrecioBolsaInstrumento_Calculado> instrumentosARegistrar = new ArrayList<RegistraPrecioBolsaInstrumento_Calculado>();
		for(EntregarPreciosTasas_Salida_PreciosTasas precioTasa :  entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS){
			for (DatosConsultaInstrumento_Salida instrumento: instrumentos){
				if(instrumento.IdInstrumento.equals(precioTasa.IdInstrumento) && precioTasa.Precio != null){				
					RegistraPrecioBolsaInstrumento_Calculado instrumentoPrecioCalculado = new RegistraPrecioBolsaInstrumento_Calculado();
					instrumentoPrecioCalculado.IdEmpresa = entrada.p_IdEmpresa;
					instrumentoPrecioCalculado.IdInstrumento = instrumento.IdInstrumento;
					instrumentoPrecioCalculado.Precio = precioTasa.Precio;
					instrumentoPrecioCalculado.FechaSistema = fechaSistema;
					instrumentoPrecioCalculado.CodigoFuente = precioTasa.CodigoFuente;
					instrumentosARegistrar.add(instrumentoPrecioCalculado);
				}
			}
		}
		
		salida.P_OUT_INSTRUMENTO = instrumentosARegistrar;
		SQL.exec(conn, DELETE_INSTRUMENTOS_HISTORIA);
		SQL.exec(conn, DELETE_INSTRUMENTOS.replace(":FechaSistema",fechaSistema));
		_sql.batch(conn, INSERT_INSTRUMENTOS, instrumentosARegistrar, RegistraPrecioBolsaInstrumento_Calculado.class);
		
		return salida;
	}
	
	private static Instrumento CargarInstrumento(Instrumento insentrada,DatosConsultaInstrumento_Salida inssalida,Long idEmpresa){
		
		Instrumento instrumento = new Instrumento();
		instrumento.IdInterno = insentrada.IdInterno == null ? 0:insentrada.IdInterno;
		instrumento.IdEmpresa = idEmpresa;
		instrumento.IdInstrumento = inssalida.IdInstrumento; 
		instrumento.IdMoneda = inssalida.IdMoneda;
		instrumento.Fecha = insentrada.Fecha;
		instrumento.IdClasificacionTipoInstrumento = inssalida.IdClasificacionTipoInstrumento;
		instrumento.IdTipoInstrumento = inssalida.IdTipoInstrumento; 				
		instrumento.Nemotecnico = inssalida.Nemotecnico;
		instrumento.IdCondicionDeLiquidacion = insentrada.IdCondicionDeLiquidacion;
		
		return instrumento;
	}
	
	public static void main(String[] args) throws Exception  {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("scotia","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_Fecha\" : '2024-10-29',\"p_Instrumentos\":[{\"IdEmpresa\":1,\"Fecha\" : \"2024-10-29\",\"IdClasificacionTipoInstrumento\":\"RV\"},{\"IdEmpresa\":1,\"IdClasificacionTipoInstrumento\":\"FM\"}]}";
			//String json = "{\"p_Instrumentos\":[{\"IdEmpresa\":1,\"IdClasificacionTipoInstrumento\":\"RV\",\"Fecha\":\"2022-05-31\"}]}";
			//String json = "{\"p_Instrumentos\":[{\"IdEmpresa\":1,\"IdInstrumento\":263093,\"IdClasificacionTipoInstrumento\":\"RV\"}]}";
			ConsultarInstrumentoPrecioBolsa_Entrada Objin = gson.fromJson(json, ConsultarInstrumentoPrecioBolsa_Entrada.class);
			RegistraPrecioInstrumento demo = new RegistraPrecioInstrumento();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ConsultarInstrumentoPrecioBolsa_Salida ObjOut = demo.logica(con, Objin);
 				//con.rollback();
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
 				System.out.println(er.toString());
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	
}




