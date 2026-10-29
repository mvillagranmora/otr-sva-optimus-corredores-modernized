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
import cl.bcs.socionegocio.wbs.DatosPortafolio;
import cl.bcs.socionegocio.wbs.ModificarClienteWS.EntradaModificarClienteWS;
import cl.bcs.socionegocio.wbs.ModificarClienteWS.SocioNegocio;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.Indicador;
import cl.bcs.sura.socionegocio.pojo.IdsEntidadYPerfiles;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCallExtra;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarClienteModificarPerfilesPortafolioWS implements JavaCallExtra {
	
	static final Logger logger = LogManager.getLogger(ModificarClienteModificarPerfilesPortafolioWS.class);
	
	private final String SQL_INSERT_INDICADOR_COBRO_CUSTODIA_MODIFICAR = loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarClienteModificarPerfilesPortafolioWS_InsertIndicadorCobroCustodia.sql");
	private final String SQL_UPDATE_INDICADOR_COBRO_CUSTODIA = loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarClienteModificarPerfilesPortafolioWS_UpdateIndicadorCobroCustodia.sql");
	
	public static class EntradaServicioModificarCliente extends BcsPojo{
		public Long p_IdEmpresa;
		public Long	p_IdUsuario; 
		public String p_ClaveUsuario;
		public String p_ActualizarIndicadorFormaCobro;
		public List<SocioNegocio> p_SocioNegocio;
		
		public EntradaServicioModificarCliente()
		{
			p_SocioNegocio = new ArrayList<SocioNegocio>();
			p_ActualizarIndicadorFormaCobro = "S";
		}

	}

	public static class SalidaServicioModificarClienteExtra extends BcsPojo
	{
		public Long	P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<EntidadManipulada> P_OUT_ENTIDADES_MANIPULADAS;
		
		
		public SalidaServicioModificarClienteExtra()
		{
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ENTIDADES_MANIPULADAS = new ArrayList<EntidadManipulada>();
		}

	}
	
	public static class ObjetoIndicador extends BcsPojo
	{
	    public String	ValorIndicador;
		public Long 	IdEntidad;
		
		public ObjetoIndicador() {
			this.ValorIndicador = "";
			this.IdEntidad = 0L;
		}
	}
	
	public static class ObjetoDatosIndicador extends BcsPojo
	{
        public Long	IdIndicador;
		public Long IdTipoIndicador;
		public Long IdPortafolio;
		
		public void ObjetoDatosIndicador() {
			this.IdIndicador = 0L;
			this.IdTipoIndicador = 0L;
			this.IdPortafolio = 0L;
		}
	
	}

	public static class ObjetoClienteIdFormaCobro extends BcsPojo
	{        
		public Long IdCliente;
		public String IdFormaCobro;
		
		public void ObjetoDatosIndicador() {
			this.IdCliente = 0L;
			this.IdFormaCobro = "";
		}
	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin, JsonObject jout) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarClienteWS in = gson.fromJson(jin.toString(), EntradaModificarClienteWS.class);
		SalidaWBS out = gson.fromJson(jout.toString(), SalidaWBS.class);
		
		SalidaServicioModificarClienteExtra outExtra = logica(conn,in,out);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(outExtra)).getAsJsonObject();
		return dataout;
	}

	private SalidaServicioModificarClienteExtra logica(Connection conn, EntradaModificarClienteWS in, SalidaWBS out) throws Exception {
		logger.info("INICIO::ModificarClienteModificarPerfilesPortafolioWS");
		long inicioServicio = System.currentTimeMillis();
		ModificarPerfilesPortafolioUtils modificarPerfilesUtils = new ModificarPerfilesPortafolioUtils();
		
		SalidaServicioModificarClienteExtra salida = obtenerSalida(out);
		
		List<String> documentosIdentidad = in.p_SocioNegocio.stream()
				.map(docIde -> docIde.DocumentoIdentidad)
				.collect(Collectors.toList());
		
		List<IdsEntidadYPerfiles> ids = modificarPerfilesUtils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(conn, documentosIdentidad);
		ids = ingresarIndicadoresCobroCustodia(conn, in.p_SocioNegocio, ids);
		if( "N".equals(in.p_ActualizarIndicadorFormaCobro) )
			ids = modificarPerfilesUtils.validarIndicadorFormaCobroPortafolioApv(conn, ids);
		modificarPerfilesUtils.modificarIndicadoresCobroCustodia(conn, ids);
		
		if(!ids.isEmpty()) {
			ids = modificarPerfilesUtils.obtenerEntidadesYPerfilesCompletas(conn, ids);
			
			for(IdsEntidadYPerfiles id : ids) {
				if(id.IdPerfilComercial.isEmpty() || id.IdPerfilProductos.isEmpty() || id.IdPerfilCobros.isEmpty()) {
					HashMap<String, String> parametrosMensaje = new HashMap<>(2);
					
					parametrosMensaje.put("IdFormaCobro", id.IdFormaCobro);
					parametrosMensaje.put("TipoEntidad", "PORTAFOLIO");
					parametrosMensaje.put("IdEntidad", id.IdPortafolio.toString());
					
					salida.P_OUT_ENTIDADES_MANIPULADAS.add(modificarPerfilesUtils.agregarMensajeErrorModificacionCliente(conn, id.IdCliente, in.p_IdEmpresa, parametrosMensaje, "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS"));
					salida.P_OUT_ERRORESCONTADOR++;
				}
			}
			if(salida.P_OUT_ERRORESCONTADOR == 0) {
				modificarPerfilesUtils.modificarPerfilesPortafolio(conn, ids);
			}
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarClienteModificarPerfilesPortafolioWS", finServicio - inicioServicio);
		return salida;
	}
	
	private SalidaServicioModificarClienteExtra obtenerSalida(SalidaWBS out) {
		logger.info("INICIO::ModificarClienteModificarPerfilesPortafolioWS.obtenerSalida");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaServicioModificarClienteExtra salida = new SalidaServicioModificarClienteExtra();
		
		salida.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_ENTIDADES_MANIPULADAS = out.P_OUT_ENTIDADES_MANIPULADAS;
		salida.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_ADVERTENCIASCONTADOR;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarClienteModificarPerfilesPortafolioWS.obtenerSalida", finServicio - inicioServicio);
		return salida;
	}

	public List<IdsEntidadYPerfiles> ingresarIndicadoresCobroCustodia(Connection conn, List<SocioNegocio> sociosNegocio, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.info("INICIO::ModificarClienteModificarPerfilesPortafolioWS.ingresarIndicadoresCobroCustodia");
		long inicioServicio = System.currentTimeMillis();
				
		List<IdsEntidadYPerfiles> listaPerfiles = new ArrayList<IdsEntidadYPerfiles>();
		
		for(SocioNegocio socio : sociosNegocio) {
			List<String> listadoDocumentos = new ArrayList<String>();
			listadoDocumentos.add(socio.DocumentoIdentidad);
			
			List<IdsEntidadYPerfiles> perfiles = ids.stream().filter(id -> listadoDocumentos.contains(id.DocumentoIdentidad)).collect(Collectors.toList());
			
				for(DatosPortafolio datos: socio.DatosPortafolio) {
				
					if(!datos.Indicadores.stream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).collect(Collectors.toList()).isEmpty() && !perfiles.isEmpty()) {
						
						Indicador indicadorFormaCobro = new Indicador();
						indicadorFormaCobro.ValorIndicador = datos.Indicadores.stream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).findFirst().get().ValorIndicador;
						indicadorFormaCobro.CodigoTipoIndicador = datos.Indicadores.stream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).findFirst().get().CodigoTipoIndicador;
						
						perfiles.stream().forEach(perfil ->{ perfil.IdFormaCobro = indicadorFormaCobro.ValorIndicador;});
						
					   ingresarIndicadoresCobroCustodia(conn, indicadorFormaCobro, perfiles);
					}
				listaPerfiles.addAll(perfiles);
			}
		}
			
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarClienteModificarPerfilesPortafolioWS.ingresarIndicadoresCobroCustodia", finServicio - inicioServicio);
		return listaPerfiles;
	}
	
	public void ingresarIndicadoresCobroCustodia(Connection conn, Indicador indicador, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.trace("INICIO::ModificarClienteModificarPerfilesPortafolioWS.ingresarIndicadorCobroCustodia");
		long inicio = System.currentTimeMillis();
		
		List<ObjetoIndicador> listObj = ids.parallelStream().filter(id-> id.IdIndicador == 0 ).map(cliente->{
			ObjetoIndicador objetoIndicador = new ObjetoIndicador();
			objetoIndicador.IdEntidad = cliente.IdPortafolio;
			objetoIndicador.ValorIndicador = indicador.ValorIndicador;
			return objetoIndicador;
		}).collect(Collectors.toList());
		
	    SQL sql = new SQL();
	    sql.batch(conn, SQL_INSERT_INDICADOR_COBRO_CUSTODIA_MODIFICAR, listObj, ObjetoIndicador.class);	
	    
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarClienteModificarPerfilesPortafolioWS.ingresarIndicadorCobroCustodia", fin - inicio);
	}
	
	public void modificarIndicadoresCobroCustodia(Connection conn, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.info("INICIO::ModificarClienteModificarPerfilesPortafolioWS.modificarIndicadoresCobroCustodia");
		long inicioServicio = System.currentTimeMillis();
		
		List<ObjetoIndicador> listObj = ids.parallelStream().filter(id-> id.IdIndicador != 0 && "S".equals(id.ActualizarFormaCobro) ).map(cliente->{
			ObjetoIndicador objetoIndicador = new ObjetoIndicador();
			objetoIndicador.IdEntidad = cliente.IdIndicador;
			objetoIndicador.ValorIndicador = cliente.IdFormaCobro;
			return objetoIndicador;
		}).collect(Collectors.toList());
		
		SQL sql = new SQL();
	    sql.batch(conn, SQL_UPDATE_INDICADOR_COBRO_CUSTODIA, listObj, ObjetoIndicador.class);	
	    
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarClienteModificarPerfilesPortafolioWS.modificarIndicadoresCobroCustodia", finServicio - inicioServicio);
	}
	
	public static void main(String[] args){
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection con = PlataformaUtils.getQAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String pruebaModificar = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 6,\r\n"
					+ "	   \"p_ActualizarIndicadorFormaCobro\": \"N\", \r\n"
					+ "    \"p_SocioNegocio\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"TipoDocumentoIdentidad\": \"R\",\r\n"
					+ "            \"DocumentoIdentidad\": \"10000562-K\",\r\n"
					+ "			   \"DatosClienteDatosGenerales\": [\r\n"
					+ "                {\r\n"
					+ " 				\"CodigoEjecutivo\": \"335\","
	                + " 				\"IdCategoriaInversionista\": \"MODERADO\" \r\n"
					+ "                }\r\n"
					+ "            ],\r\n"
					+ "			   \"DatosPortafolio\": [\r\n"
					+ "				{\r\n"
					+ "					\"IdPortafolio\": 488166,\r\n"
					+ "					\"Indicadores\": [\r\n"
					+ "						{\r\n"
					+ "							\"ValorIndicador\": \"OPCIÓN 1\",\r\n"
					+ "							\"CodigoTipoIndicador\": \"COBROCUST\"\r\n"
					+ "						}\r\n"
					+ "					]\r\n"
					+ "				}"
					+ "			]"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			
			String salidaMaster01= "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_ADVERTENCIASCONTADOR\":1,\"P_OUT_ENTIDADES_MANIPULADAS\":[{\"TipoEntidad\":\"RELACION\",\"IdEntidad\":0,\"Mensajes\":[{\"id\":1774617,\"tipo\":\"ADVERTENCIA\",\"codigo\":\"UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE\",\"descripcion\":\"La persona relacionada ha sido Seleccionada pero Ya Existe y No Fue Actualizada.\"}]},{\"TipoEntidad\":\"DATOS GENERALES PERSONA\",\"IdEntidad\":1795536,\"Mensajes\":[{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"ENTIDAD_MODIFICADA_CORRECTAMENTE\",\"descripcion\":\"ENTIDAD_MODIFICADA_CORRECTAMENTE\"}]}]}";
			
			EntradaModificarClienteWS Objin = gson.fromJson(pruebaModificar, EntradaModificarClienteWS.class);
			SalidaWBS Objout = gson.fromJson(salidaMaster01, SalidaWBS.class);
			
			ModificarClienteModificarPerfilesPortafolioWS demo = new ModificarClienteModificarPerfilesPortafolioWS();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaServicioModificarClienteExtra ObjOut = demo.logica(con, Objin, Objout);
				con.rollback();
				//con.commit();
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
