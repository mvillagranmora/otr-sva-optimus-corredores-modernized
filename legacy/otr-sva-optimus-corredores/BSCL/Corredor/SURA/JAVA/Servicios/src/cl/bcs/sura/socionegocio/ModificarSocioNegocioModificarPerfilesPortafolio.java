package cl.bcs.sura.socionegocio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.SocioNegocio;
import cl.bcs.socionegocio.soc.ModificarSocioNegocio.EntidadModificada;
import cl.bcs.sura.socionegocio.pojo.IdsEntidadYPerfiles;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCallExtra;
import cl.bcs.utils.Utils;

public class ModificarSocioNegocioModificarPerfilesPortafolio implements JavaCallExtra{

	static final Logger logger = LogManager.getLogger(ModificarSocioNegocioModificarPerfilesPortafolio.class);
	
	public static class EntradaServicioModificarSocioNegocio extends BcsPojo{
		
		public Long 				p_IdEmpresa;
		public Long					p_IdUsuario; 
		public String				p_ClaveUsuario;
		public List<SocioNegocio>	p_SocioNegocio;
		public String 				p_ActualizarIndicadorFormaCobro;

		public EntradaServicioModificarSocioNegocio()
		{
			p_SocioNegocio = new ArrayList<SocioNegocio>();
			p_ActualizarIndicadorFormaCobro = "N";
		}

	}

	public static class SalidaServicioModificarSocioNegocio extends BcsPojo
	{
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						p_Out_ContadorEntidadModificada;
		public List<EntidadModificada>	p_Out_EntidadModificada;

		public SalidaServicioModificarSocioNegocio()
		{
			this.p_Out_ContadorEntidadModificada = 0L;
			this.p_Out_EntidadModificada = new ArrayList<EntidadModificada>();
		}
	}

	public static class IdsEntidad extends BcsPojo {
		
		public Long IdPortafolio;
		public Long IdCliente;
		
		public IdsEntidad() {
			 this.IdPortafolio = 0L;
			 this.IdCliente = 0L;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin, JsonObject jout) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaServicioModificarSocioNegocio in = gson.fromJson(jin.toString(), EntradaServicioModificarSocioNegocio.class);
		SalidaServicioModificarSocioNegocio out = gson.fromJson(jout.toString(), SalidaServicioModificarSocioNegocio.class);
		
		SalidaServicioModificarSocioNegocio outExtra = logica(conn,in,out);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(outExtra)).getAsJsonObject();
		return dataout;
	}

	public SalidaServicioModificarSocioNegocio logica(Connection conn, EntradaServicioModificarSocioNegocio in, SalidaServicioModificarSocioNegocio out) throws Exception {
		logger.info("INICIO::ModificarSocioNegocioModificarPerfilesPortafolio");
		long inicioServicio = System.currentTimeMillis();
		
		ModificarPerfilesPortafolioUtils modificarPerfilesUtils = new ModificarPerfilesPortafolioUtils();
		
		List<String> documentosIdentidad = new ArrayList<>();
				
		in.p_SocioNegocio.parallelStream().forEach(soc -> {
			documentosIdentidad.add(soc.DocumentoIdentidad);
		});
		
		List<IdsEntidadYPerfiles> ids = modificarPerfilesUtils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(conn, documentosIdentidad);
		
		ids = modificarPerfilesUtils.obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(conn, in.p_SocioNegocio, ids);
		
		if(!ids.isEmpty()) {
			for(IdsEntidadYPerfiles id : ids) {
				if(id.IdPerfilComercial.isEmpty() || id.IdPerfilProductos.isEmpty() || id.IdPerfilCobros.isEmpty()) {
					HashMap<String, String> parametrosMensaje = new HashMap<>(2);
					
					parametrosMensaje.put("IdFormaCobro", id.IdFormaCobro);
					parametrosMensaje.put("TipoEntidad", "PORTAFOLIO");
					parametrosMensaje.put("IdEntidad", id.IdPortafolio.toString());
					
					out.p_Out_EntidadModificada.add(modificarPerfilesUtils.agregarMensajeErrorModificacion(conn, id.IdCliente, in.p_IdEmpresa, parametrosMensaje, "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS"));
					out.P_OUT_ERRORESCONTADOR++;
				}
			}
			
			if(out.P_OUT_ERRORESCONTADOR == 0) {
				
				CrearSocioNegocioModificarPerfilesPortafolio crearSocioNegocio = new CrearSocioNegocioModificarPerfilesPortafolio();
				ids = crearSocioNegocio.ingresarIndicadoresCobroCustodia(conn, in.p_SocioNegocio, ids);
				if( "N".equals(in.p_ActualizarIndicadorFormaCobro) )
					ids = modificarPerfilesUtils.validarIndicadorFormaCobroPortafolioApv(conn, ids);
				modificarPerfilesUtils.modificarIndicadoresCobroCustodia(conn, ids);
				
				modificarPerfilesUtils.modificarPerfilesPortafolio(conn, ids);
			}
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarSocioNegocioModificarPerfilesPortafolio", finServicio - inicioServicio);
		return out;
		
	}
	
	public static void main(String[] args){
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection con = PlataformaUtils.getQAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String jsonCL = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": \"1\",\r\n"
					+ "  \"p_ClaveUsuario\": \"\",\r\n"
					+ "  \"p_SocioNegocio\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdEntidad\": 0,\r\n"
					+ "      \"TipoEntidad\": \"N\",\r\n"
					+ "      \"TipoDocumentoIdentidad\": \"R\",\r\n"
					+ "      \"DocumentoIdentidad\": \"10000562-K\",\r\n"
					+ "      \"Nombres\": \"DANIELA\",\r\n"
					+ "      \"DatosCliente\": [\r\n"
					+ "        {\r\n"
					+ "          \"IdCategoriaInversionista\": \"CONSERVADOR\",\r\n"
					+ "          \"NombreSegmentoCliente\": \"RETAIL\"\r\n"
					+ "        }\r\n"
					+ "      ],\r\n"
					+ "      \"IndicadorPortafolio\": [\r\n"
					+ "        {\r\n"
					+ "          \"ValorIndicador\": \"OPCIÓN 1\",\r\n"
					+ "          \"CodigoTipoIndicador\": \"COBROCUST\"\r\n"
					+ "        }\r\n"
					+ "      ]\r\n"
					+ "    } "
					+ "  ]\r\n"
					+ "}";
			String salidaMaster01= "{\"P_OUT_ERRORESCONTADOR\":0,\"p_Out_ContadorEntidadModificada\":1,\"p_Out_EntidadModificada\":[{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":1425996,\"Mensajes\":[{\"IdInterno\":0,\"TipoMensaje\":\"INFO\",\"Codigo\":\"MODIFICACION_EXITOSA\",\"Mensaje\":\"Modificacion Exitosa\"}]},{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":1425996,\"Mensajes\":[{\"IdInterno\":0,\"TipoMensaje\":\"INFO\",\"Codigo\":\"MODIFICACION_EXITOSA\",\"Mensaje\":\"Modificacion Exitosa\"}]}]}";
			
			EntradaServicioModificarSocioNegocio Objin = gson.fromJson(jsonCL, EntradaServicioModificarSocioNegocio.class);
			SalidaServicioModificarSocioNegocio Objout = gson.fromJson(salidaMaster01, SalidaServicioModificarSocioNegocio.class);
			
			ModificarSocioNegocioModificarPerfilesPortafolio demo = new ModificarSocioNegocioModificarPerfilesPortafolio();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaServicioModificarSocioNegocio ObjOut = demo.logica(con, Objin, Objout);
				con.rollback();
//				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}
