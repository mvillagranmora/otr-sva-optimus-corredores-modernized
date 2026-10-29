package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarRepresentante implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarRepresentante.class);
		
	public static class EntradaConsultarRepresentante extends BcsPojo {
		public Long p_IdEmpresa;
		public String p_IdUsuario;
		public String p_ClaveUsuario;
		public List<Filtros> p_Filtros;
		
		public EntradaConsultarRepresentante () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = "";
			this.p_ClaveUsuario = "";
			this.p_Filtros = new ArrayList<Filtros>();
		}
		
	}
	public static class Filtros extends BcsPojo {
		public Long IdInterno;
		public String Filtro;
		public String Valor;
	}
	
	
	public static class SalidaConsultarRepresentante extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Representante> P_OUT_RESULTADO_REPRESENTANTE;
		public List<Representante> P_OUT_RESULTADO_CLIENTE;
		
		public SalidaConsultarRepresentante () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO_REPRESENTANTE = new ArrayList<Representante>();
			this.P_OUT_RESULTADO_CLIENTE = new ArrayList<Representante>();
		}
	}

	public static class Representante extends BcsPojo {
		public Integer IdInterno;
		public String TipoAutorizacion;
		public String CodigoPortafolio;
		public String PropositoPortafolio;
		public String EstadoPortafolio;
		public String Nombre;
		public String DocumentoIdentidad;
		
		public Representante() {
			this.IdInterno = 0;
			this.TipoAutorizacion = "";
			this.CodigoPortafolio = "";
			this.PropositoPortafolio = "";
			this.EstadoPortafolio = "";
			this.Nombre = "";
			this.DocumentoIdentidad = "";
		}
	}
	
	public static class RepresentanteCompleto extends BcsPojo {
		public Integer IdInterno;
		public String TipoAutorizacion;
		public String CodigoPortafolio;
		public String PropositoPortafolio;
		public String EstadoPortafolio;
		public String Nombre;
		public String DocumentoIdentidad;
		public String TipoAcceso;
		
		public RepresentanteCompleto() {
			this.IdInterno = 0;
			this.TipoAutorizacion = "";
			this.CodigoPortafolio = "";
			this.PropositoPortafolio = "";
			this.EstadoPortafolio = "";
			this.Nombre = "";
			this.DocumentoIdentidad = "";
			this.TipoAcceso = "";
		}
	}
	
	
	public SalidaConsultarRepresentante logica(Connection con, EntradaConsultarRepresentante entrada) throws Exception {
		logger.info("INICIO::ConsultarRepresentantes");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarRepresentante salida = new SalidaConsultarRepresentante();
		
		Map<Long, List<Filtros>> grupos =
			    entrada.p_Filtros.stream()
			        .collect(Collectors.groupingBy(f -> f.IdInterno));

		
		String sqlConsulta = grupos.entrySet().stream().map(entry -> {

	        Long idInterno = entry.getKey();
	        List<Filtros> grupo = entry.getValue();

	        String condiciones = grupo.stream().map(f -> {

	                switch (f.Filtro) {
	                    case "DocumentoIdentidad":
	                        return "(CAO.\"DocumentoIdentidad\" = '" + f.Valor + "')";
	                    case "CodigoPortafolio":
	                        return "CAO.\"CodigoPortafolio\" = '" + f.Valor + "'";
	                    default:
	                        throw new RuntimeException(
	                            "Filtro no soportado: " + f.Filtro);
	                }
	            })
	            .collect(Collectors.joining(" AND "));

	        return
	            "SELECT " +
	            idInterno + " AS \"IdInterno\", " +
	            "CAO.\"NombreTipoAutorizado\" AS \"TipoAutorizacion\", " +
	            "CAO.\"CodigoPortafolio\" AS \"CodigoPortafolio\", " +
	            "PORT.\"Proposito\" AS \"PropositoPortafolio\", " +
	            "PORT.\"IdEstado\" AS \"EstadoPortafolio\", " +
	            "CAO.\"AutorizadoPor\" AS \"Nombre\", " +
	            "CAO.\"TipoAcceso\" AS \"TipoAcceso\", " +
	            "CAO.\"DocumentoIdentidadCLI\" AS \"DocumentoIdentidad\" " +
	            "FROM \"BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden\" CAO " +
	            "LEFT JOIN \"BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio\" PORT " +
	            "ON PORT.\"IdPortafolio\" = CAO.\"IdPortafolio\" " +
	            "WHERE CAO.\"IdTipoRelacion\" = 'AUTICL' AND " + condiciones;

	    })
	    .collect(Collectors.joining(" UNION ALL "));
		
		SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(6);
		
        List<RepresentanteCompleto> Datos = sql.query(con, sqlConsulta, parametros, RepresentanteCompleto.class); 
        
        List<Representante> clientes = Datos.stream()
        	    .filter(d -> "Clientes".equalsIgnoreCase(d.TipoAcceso))
        	    .map(this::convertirRepresentante)
        	    .collect(Collectors.toList());

        List<Representante> relacionados = Datos.stream()
        	    .filter(d -> "Relacionados".equalsIgnoreCase(d.TipoAcceso))
        	    .map(this::convertirRepresentante)
        	    .collect(Collectors.toList());
        
        salida.P_OUT_RESULTADO_CLIENTE = clientes;
        salida.P_OUT_RESULTADO_REPRESENTANTE = relacionados;
                
		if(salida.P_OUT_RESULTADO_REPRESENTANTE.isEmpty() && salida.P_OUT_RESULTADO_CLIENTE.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_SIN_REPRESENTANTES",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
		} else {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CONSULTA_REPRESENTANTES_OK",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarRepresentantes", fin - inicio);
		return salida;
	}
	
	private Representante convertirRepresentante(RepresentanteCompleto dato) {

	    Representante representante = new Representante();

	    representante.IdInterno = dato.IdInterno;
	    representante.TipoAutorizacion = dato.TipoAutorizacion;
	    representante.CodigoPortafolio = dato.CodigoPortafolio;
	    representante.PropositoPortafolio = dato.PropositoPortafolio;
	    representante.EstadoPortafolio = dato.EstadoPortafolio;
	    representante.Nombre = dato.Nombre;
	    representante.DocumentoIdentidad = dato.DocumentoIdentidad;

	    return representante;
	}



	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarRepresentante jin=gson.fromJson(in.toString() ,EntradaConsultarRepresentante.class);
		SalidaConsultarRepresentante out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd")
						.serializeNulls().create();

				String jsonCL =  "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_ClaveUsuario\":\"\",\"p_Filtros\":["
						+ "{ \"IdInterno\": 0, \"Filtro\": \"DocumentoIdentidad\", \"Valor\": \"10236372-8\" }]}"; //15364160-9
						/*+ "{ \"IdInterno\": 1, \"Filtro\": \"CodigoPortafolio\", \"Valor\": \"80\" },"
						+ "{ \"IdInterno\": 1, \"Filtro\": \"DocumentoIdentidad\", \"Valor\": \"18020056-8\" }]}";
						+ "{ \"IdInterno\": 2, \"Filtro\": \"DocumentoIdentidad\", \"Valor\": \"12857513-1\" },"
						+ "{ \"IdInterno\": 3, \"Filtro\": \"DocumentoIdentidad\", \"Valor\": \"12857413-1\" },"
						+ "{ \"IdInterno\": 4, \"Filtro\": \"DocumentoIdentidad\", \"Valor\": \"12857313-1\" },"
						+ "{ \"IdInterno\": 1, \"Filtro\": \"Portafolio\", \"Valor\": \"0\" }]}";*/
				EntradaConsultarRepresentante in = gson.fromJson(jsonCL, EntradaConsultarRepresentante.class);

				ConsultarRepresentante demo = new ConsultarRepresentante();
				System.out.println("ENTRADA");
				System.out.println(in);
				
				try {
					SalidaConsultarRepresentante out = demo.logica(con, in);
					con.rollback();
					System.out.println("SALIDA");
					System.out.println(out);
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}
				
			}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}

	}

}