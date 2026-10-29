package cl.bcs.sura.socionegocio;

import java.sql.Connection;
import java.sql.SQLException;
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
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.EntidadCreada;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.Indicador;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.SocioNegocio;
import cl.bcs.sura.socionegocio.pojo.IdsEntidadYPerfiles;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCallExtra;
import cl.bcs.utils.Utils;

public class CrearSocioNegocioModificarPerfilesPortafolio implements JavaCallExtra{
	
	static final Logger logger = LogManager.getLogger(CrearSocioNegocioModificarPerfilesPortafolio.class);
	
	public static class EntradaServicioCrearSocioNegocio extends BcsPojo{
		public Long 				p_IdEmpresa;
		public Long					p_IdUsuario; 
		public String				p_ClaveUsuario;
		public List<SocioNegocio>	p_SocioNegocio;
		
		public EntradaServicioCrearSocioNegocio()
		{
			p_SocioNegocio = new ArrayList<SocioNegocio>();
		}

	}

	public static class SalidaServicioCrearSocioNegocio extends BcsPojo
	{
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					p_Out_ContadorEntidadCreada;
		public List<EntidadCreada>	p_Out_EntidadCreada;
		public List<MensajeSalida>	P_OUT_ADVERTENCIAS;
		public Long					P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaServicioCrearSocioNegocio () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.p_Out_ContadorEntidadCreada = 0L;
			this.p_Out_EntidadCreada = new ArrayList<>();
			this.P_OUT_ADVERTENCIAS = new ArrayList<>();
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
		EntradaServicioCrearSocioNegocio in = gson.fromJson(jin.toString(), EntradaServicioCrearSocioNegocio.class);
		SalidaServicioCrearSocioNegocio out = gson.fromJson(jout.toString(), SalidaServicioCrearSocioNegocio.class);
		
		SalidaServicioCrearSocioNegocio outExtra = logica(conn,in,out);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(outExtra)).getAsJsonObject();
		return dataout;
	}

	public SalidaServicioCrearSocioNegocio logica(Connection conn, EntradaServicioCrearSocioNegocio in, SalidaServicioCrearSocioNegocio out) throws Exception {
		logger.info("INICIO::CrearSocioNegocioModificarPerfilesPortafolio");
		long inicioServicio = System.currentTimeMillis();
		
		ModificarPerfilesPortafolioUtils modificarPerfilesUtils = new ModificarPerfilesPortafolioUtils();
		
		List<String> documentosIdentidad = new ArrayList<>();
				
		in.p_SocioNegocio.parallelStream().forEach(soc -> {
			if(soc.TipoEntidad.equals("CLIENTE")) {
				documentosIdentidad.add(soc.DocumentoIdentidad);
			}
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
					
					out.p_Out_EntidadCreada.add(modificarPerfilesUtils.agregarMensajeError(conn, id.IdCliente, in.p_IdEmpresa, parametrosMensaje, "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS"));
					out.P_OUT_ERRORESCONTADOR++;
				}
			}
			
			if(out.P_OUT_ERRORESCONTADOR == 0) {
				ids = ingresarIndicadoresCobroCustodia(conn, in.p_SocioNegocio, ids);
				
				modificarPerfilesUtils.modificarIndicadoresCobroCustodia(conn, ids);
				
				modificarPerfilesUtils.modificarPerfilesPortafolio(conn, ids);
			}
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSocioNegocioModificarPerfilesPortafolio", finServicio - inicioServicio);
		return out;
	}
	
	public List<IdsEntidadYPerfiles> ingresarIndicadoresCobroCustodia(Connection conn, List<SocioNegocio> sociosNegocio, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.info("INICIO::CrearSocioNegocioModificarPerfilesPortafolio.ingresarIndicadoresCobroCustodia");
		long inicioServicio = System.currentTimeMillis();
		
		ModificarPerfilesPortafolioUtils modificarPerfilesUtils = new ModificarPerfilesPortafolioUtils();
		
		List<IdsEntidadYPerfiles> listaPerfiles = new ArrayList<IdsEntidadYPerfiles>();
		
		for(SocioNegocio socio : sociosNegocio) {
			List<String> listadoDocumentos = new ArrayList<String>();
			listadoDocumentos.add(socio.DocumentoIdentidad);
			
			List<IdsEntidadYPerfiles> perfiles = ids.parallelStream().filter(id -> listadoDocumentos.contains(id.DocumentoIdentidad)).collect(Collectors.toList());
			
			if(!socio.IndicadorPortafolio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).collect(Collectors.toList()).isEmpty() && !perfiles.isEmpty()) {
				
				Indicador indicadorFormaCobro = new Indicador();
				indicadorFormaCobro.ValorIndicador = socio.IndicadorPortafolio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).findFirst().get().ValorIndicador;
				indicadorFormaCobro.CodigoTipoIndicador = socio.IndicadorPortafolio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).findFirst().get().CodigoTipoIndicador;
				
				perfiles.parallelStream().forEach(perfil ->{ perfil.IdFormaCobro = indicadorFormaCobro.ValorIndicador;});
				
				modificarPerfilesUtils.ingresarIndicadoresCobroCustodia(conn, indicadorFormaCobro, perfiles);
			}
			listaPerfiles.addAll(perfiles);
		}
			
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSocioNegocioModificarPerfilesPortafolio.ingresarIndicadoresCobroCustodia", finServicio - inicioServicio);
		return listaPerfiles;
	}
	
	public static void main(String[] args){
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String pruebaMaster01 = "{ \r\n"
					+ "    \"p_ClaveUsuario\": \"\", \r\n"
					+ "    \"p_IdEmpresa\": 1, \r\n"
					+ "    \"p_IdUsuario\": \"1\", \r\n"
					+ "    \"p_SocioNegocio\": [ \r\n"
					+ "        { \r\n"
					+ "            \"CuentaBancaria\": [], \r\n"
					+ "            \"DatosCliente\": [\r\n"
					+ "                { \r\n"
					+ "                    \"CodigoEjecutivo\": \"335\", \r\n"
					+ "                    \"IdCategoriaInversionista\": \"MODERADO\", \r\n"
					+ "                    \"IdPerfilComercial\": \"NO APV_OPCION 1_CONSERVADOR\", \r\n"
					+ "                    \"NombreSegmentoCliente\": \"INSTITUCIONAL\", \r\n"
					+ "                    \"IdTipoCliente\": \"RETAIL\" \r\n"
					+ "  		}    \r\n"
					+ "            ], \r\n"
					+ "            \"DeclaracionOrigenFondo\": [], \r\n"
					+ "            \"DireccionElectronica\": [ \r\n"
					+ "                { \r\n"
					+ "                    \"Direccion\": \"OTRO@EMAIL.COM\", \r\n"
					+ "                    \"IdProposito\": \"PART\" \r\n"
					+ "                } \r\n"
					+ "    	    ], \r\n"
					+ "            \"DireccionFisica\": [ \r\n"
					+ "                { \r\n"
					+ "                    \"Calle\": \"LAS ROSAS\", \r\n"
					+ "                    \"IdCiudad\": 2931, \r\n"
					+ "                    \"IdComuna\": 327, \r\n"
					+ "                    \"IdProposito\": \"PART\", \r\n"
					+ "                    \"Numero\": \"456\" \r\n"
					+ "                } \r\n"
					+ "     	    ], \r\n"
					+ "            \"DocumentoIdentidad\": \"18898560-2\", \r\n"
					+ "            \"FechaNacimientoConstitucion\": \"2020-12-05\", \r\n"
					+ "            \"IdEntidad\": 3, \r\n"
					+ "            \"IdEntidadRelacionada\": 1, \r\n"
					+ "            \"IdTipoPersona\": \"N\", \r\n"
					+ "            \"IdTipoRelacion\": \"CEMDOR\", \r\n"
					+ "            \"IndicadorCliente\": [], \r\n"
					+ "            \"IndicadorPersona\": [], \r\n"
					+ "            \"IndicadorPortafolio\": ["
					+ "				{ \r\n"
					+ "					\"ValorIndicador\": \"OPCION 1\",\r\n"	
					+ "					\"CodigoTipoIndicador\": \"COBROCUST\" \r\n"
					+ "             } \r\n"
					+ "				], \r\n"
					+ "            \"Nombres\": \"MARTIN\", \r\n"
					+ "            \"ApellidoMaterno\": \"CORTES\", \r\n"
					+ "            \"ApellidoPaterno\": \"COTES\", \r\n"
					+ "            \"IdProfesion\": 203, \r\n"
					+ "            \"IdTipoCanalIngreso\": \"CANAL PRUEBA SURA\",\r\n"
					+ "            \"TarjetasBancaria\": [], \r\n"
					+ "            \"Telefono\": [ \r\n"
					+ "                { \r\n"
					+ "                    \"IdPais\": 44, \r\n"
					+ "                    \"IdProposito\": \"PART\", \r\n"
					+ "                    \"Numero\": \"+5612345789\", \r\n"
					+ "                    \"Tipo\": \"FIJO\" \r\n"
					+ "                } \r\n"
					+ "     	    ], \r\n"
					+ "            \"TipoDocumentoIdentidad\": \"R\", \r\n"
					+ "            \"TipoEntidad\": \"CLIENTE\", \r\n"
					+ "            \"TiposDocumento\": [] \r\n"
					+ " 	} \r\n"
					+ "    ] \r\n"
					+ "}";
			
			String salidaMaster01= "{\"P_OUT_ERRORESCONTADOR\":0,\"p_Out_ContadorEntidadCreada\":1,\"p_Out_EntidadCreada\":[{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":3,\"Estado\":\"ACT\",\"Nuevo\":\"S\",\"Mensajes\":[]}]}";
			String salida2 = "{\"P_OUT_ERRORESCONTADOR\":0,\"p_Out_ContadorEntidadCreada\":1,\"p_Out_EntidadCreada\":[{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":1,\"Estado\":\"ACT\",\"Nuevo\":\"S\",\"Mensajes\":[]},{\"TipoEntidad\":\"PERSONA\",\"IdEntidad\":3,\"Estado\":\"ACT\",\"Nuevo\":\"S\",\"Mensajes\":[]},{\"TipoEntidad\":\"PERSONA\",\"IdEntidad\":2,\"Estado\":\"ACT\",\"Nuevo\":\"S\",\"Mensajes\":[]}],\"P_OUT_ADVERTENCIAS\":[{\"id\":3,\"tipo\":\"ADVERTENCIA\",\"codigo\":\"UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE\",\"descripcion\":\"La persona relacionada ha sido Seleccionada pero Ya Existe y No Fue Actualizada.\"},{\"id\":2,\"tipo\":\"ADVERTENCIA\",\"codigo\":\"UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE\",\"descripcion\":\"La persona relacionada ha sido Seleccionada pero Ya Existe y No Fue Actualizada.\"}],\"P_OUT_ADVERTENCIASCONTADOR\":2}";
			
			EntradaServicioCrearSocioNegocio Objin = gson.fromJson(pruebaMaster01, EntradaServicioCrearSocioNegocio.class);
			SalidaServicioCrearSocioNegocio Objout = gson.fromJson(salida2, SalidaServicioCrearSocioNegocio.class);
			
			CrearSocioNegocioModificarPerfilesPortafolio demo = new CrearSocioNegocioModificarPerfilesPortafolio();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaServicioCrearSocioNegocio ObjOut = demo.logica(con, Objin, Objout);
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
