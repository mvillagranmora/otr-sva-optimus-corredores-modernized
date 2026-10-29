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
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntidadCreada;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntradaCrearClienteFullWS;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.SalidaCrearClienteFullWS;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.SocioNegocio;
import cl.bcs.sura.socionegocio.pojo.IdsEntidadYPerfiles;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCallExtra;
import cl.bcs.utils.Utils;

public class CrearClienteFullWSModificarPerfilesPortafolio implements JavaCallExtra{

	static final Logger logger = LogManager.getLogger(CrearClienteFullWSModificarPerfilesPortafolio.class);
	
	public static class SalidaServicioCrearClienteFullWS extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public Long	P_OUT_ADVERTENCIASCONTADOR;
		public List<EntidadCreada> P_OUT_ENTIDADCREADA;
		
		public SalidaServicioCrearClienteFullWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ENTIDADCREADA = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin, JsonObject jout) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearClienteFullWS in = gson.fromJson(jin.toString(), EntradaCrearClienteFullWS.class);
		SalidaCrearClienteFullWS out = gson.fromJson(jout.toString(), SalidaCrearClienteFullWS.class);
		
		SalidaServicioCrearClienteFullWS outExtra = logica(conn,in,out);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(outExtra)).getAsJsonObject();
		return dataout;
	}
	public static class IdIndicadorPortafolio extends BcsPojo{
		public Long IdPortafolio;
		public Long IdIndicador;
	}
	
	public static class modificacionPortafolio extends BcsPojo {
		
		public Long IdPortafolio;
		public Long IdCliente;
		public List<IdIndicadorPortafolio> indicadorPortafolio;
		
		public modificacionPortafolio() {
			 this.IdPortafolio = 0L;
			 this.IdCliente = 0L;
		}
	}
	
	public Long contarAdvetencias(SalidaCrearClienteFullWS salida) {
	    logger.debug("INICIO::CrearClienteFullWSModificarPerfilesPortafolio.contarAdvetencias");
	    long inicio = System.currentTimeMillis();
	    
	    Long advetencias = salida.P_OUT_ENTIDADCREADA.parallelStream().reduce(0L,
			    (sum, ent) -> sum += ent.Mensajes.stream().filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA.equals(m.TipoMensaje)).count(), 
			    (sum1, sum2) -> sum1 + sum2);
	    
	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::CrearClienteFullWSModificarPerfilesPortafolio.contarAdvetencias", fin - inicio);
	    return advetencias;
	}

	public SalidaServicioCrearClienteFullWS logica(Connection conn, EntradaCrearClienteFullWS in, SalidaCrearClienteFullWS out) throws Exception {
		logger.info("INICIO::CrearClienteFullWSModificarPerfilesPortafolio");
		long inicioServicio = System.currentTimeMillis();
		
		ModificarPerfilesPortafolioUtils modificarUtils = new ModificarPerfilesPortafolioUtils();
		SalidaServicioCrearClienteFullWS salida = obtenerSalida(out);
		
		List<String> documentosIdentidad = new ArrayList<>();
		
		in.p_SocioNegocio.parallelStream().forEach(soc -> {
			if(!soc.TiposDocumento.isEmpty()) {
				documentosIdentidad.add(soc.TiposDocumento.parallelStream().findFirst().get().NumeroDocumento);	
			}
		});
		
		List<IdsEntidadYPerfiles> ids = modificarUtils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(conn, documentosIdentidad);
		
		ids = ingresarIndicadoresCobroCustodia(conn, in.p_SocioNegocio, ids);
		
		modificarUtils.modificarIndicadoresCobroCustodia(conn, ids);
		
		if(!ids.isEmpty()) {
			ids = modificarUtils.obtenerEntidadesYPerfilesCompletas(conn, ids);
			
			for(IdsEntidadYPerfiles id : ids) {
				if(id.IdPerfilComercial.isEmpty() || id.IdPerfilProductos.isEmpty() || id.IdPerfilCobros.isEmpty()) {
					HashMap<String, String> parametrosMensaje = new HashMap<>(2);
					parametrosMensaje.put("IdFormaCobro", id.IdFormaCobro);
					parametrosMensaje.put("TipoEntidad", "PORTAFOLIO");
					parametrosMensaje.put("IdEntidad", id.IdPortafolio.toString());
					
					salida.P_OUT_ENTIDADCREADA.add(modificarUtils.agregarMensajeErrorWSBase(conn, id.IdCliente, in.p_IdEmpresa, parametrosMensaje, "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS"));
					salida.P_OUT_ERRORESCONTADOR++;
					
					
				}			
			}
			
			if(salida.P_OUT_ERRORESCONTADOR == 0) {
				modificarUtils.modificarPerfilesPortafolio(conn, ids);
			}
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearClienteFullWSModificarPerfilesPortafolio", finServicio - inicioServicio);
		return salida;
	}
	
	private SalidaServicioCrearClienteFullWS obtenerSalida(SalidaCrearClienteFullWS out) {
		logger.info("INICIO::CrearClienteFullWSModificarPerfilesPortafolio.obtenerSalida");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaServicioCrearClienteFullWS salida = new SalidaServicioCrearClienteFullWS();
		
		salida.P_OUT_ENTIDADCREADA = out.P_OUT_ENTIDADCREADA;
		salida.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_ADVERTENCIASCONTADOR = contarAdvetencias(out);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearClienteFullWSModificarPerfilesPortafolio.obtenerSalida", finServicio - inicioServicio);
		return salida;
	}

	public List<IdsEntidadYPerfiles> ingresarIndicadoresCobroCustodia(Connection conn, List<SocioNegocio> sociosNegocio, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.info("INICIO::CrearClienteFullWSModificarPerfilesPortafolio.ingresarIndicadoresCobroCustodia");
		long inicioServicio = System.currentTimeMillis();
		
		ModificarPerfilesPortafolioUtils modificarUtils = new ModificarPerfilesPortafolioUtils();
		
		List<IdsEntidadYPerfiles> listaPerfiles = new ArrayList<IdsEntidadYPerfiles>();
		
		for(SocioNegocio socio : sociosNegocio) {
			
			List<String> listadoDocumentos = socio.TiposDocumento.parallelStream().map(doc->{ return doc.NumeroDocumento;}).collect(Collectors.toList());
			
			List<IdsEntidadYPerfiles> perfiles = ids.parallelStream().filter(id -> listadoDocumentos.contains(id.DocumentoIdentidad)).collect(Collectors.toList());
			
			if(!socio.DatosCliente.DatosPortafolio.PreferenciasNegocio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).collect(Collectors.toList()).isEmpty() && !perfiles.isEmpty()) {
				
				cl.bcs.socionegocio.soc.CrearSocioNegocio.Indicador indicadorFormaCobro = new cl.bcs.socionegocio.soc.CrearSocioNegocio.Indicador();
				indicadorFormaCobro.ValorIndicador = socio.DatosCliente.DatosPortafolio.PreferenciasNegocio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).findFirst().get().ValorIndicador;
				indicadorFormaCobro.CodigoTipoIndicador = socio.DatosCliente.DatosPortafolio.PreferenciasNegocio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).findFirst().get().CodigoTipoIndicador;
				
				perfiles.parallelStream().forEach(perfil ->{
						perfil.IdFormaCobro = indicadorFormaCobro.ValorIndicador;
				});
				
				modificarUtils.ingresarIndicadoresCobroCustodia(conn, indicadorFormaCobro, perfiles);
			}
			listaPerfiles.addAll(perfiles);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearClienteFullWSModificarPerfilesPortafolio.ingresarIndicadoresCobroCustodia", finServicio - inicioServicio);
		return listaPerfiles;
	}
	
	public static void main(String[] args){
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection con = PlataformaUtils.getDESAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String entrada = "{\r\n"
					+ "\"p_IdEmpresa\": 1,\r\n"
					+ "\"p_IdUsuario\": 4,\r\n"
					+ "\"p_SocioNegocio\": [\r\n"
					+ " { \"IdInterno\": 0,\r\n"
					+ "   \"IdTipoPersona\": \"N\",\r\n"
					+ "   \"Nombres\": \"PEDRO\",\r\n"
					+ "   \"ApellidoPaterno\": \"CASTRO\",\r\n"
					+ "   \"ApellidoMaterno\": \"MARDONES\",\r\n"
					+ "   \"IdGenero\": \"M\",\r\n"
					+ "   \"RazonSocial\": \"\",\r\n"
					+ "   \"IdPaisNacimientoConstitucion\": 44,\r\n"
					+ "   \"FechaNacimientoConstitucion\": \"2000-02-16\",\r\n"
					+ "   \"IdEstadoCivil\": \"SO\",\r\n"
					+ "   \"IdRegimenConyugal\": \"\",\r\n"
					+ "   \"IdPaisResidenciaCasaMatriz\": 44,\r\n"
					+ "   \"ResidenciaTriburariaFueraChile\": \"N\",\r\n"
					+ "   \"IdPaisNacionalidadPrincipal\": 44,\r\n"
					+ "   \"IdGiro\": 19,\r\n"
					+ "   \"IdProfesionActividad\": 50,\r\n"
					+ "   \"Cargo\": \"EMPLEA\",\r\n"
					+ "   \"DetalleCargo\": \"NECESITA DETALLE\",\r\n"
					+ "   \"RangoPatrimonio\": 1,\r\n"
					+ "   \"InicioActividad\": \"0001-01-01\",\r\n"
					+ "   \"GlobalLegal\": \"\",\r\n"
					+ "   \"Empleador\": {\r\n"
					+ "       \"IdTipoPersona\": \"N\",\r\n"
					+ "       \"TipoDocumentoIdentidad\": \"R\",\r\n"
					+ "       \"DocumentoIdentidad\": \"9189855-1\",\r\n"
					+ "       \"Nombres\": \"MANUEL\",\r\n"
					+ "       \"ApellidoPaterno\": \"MEDRANO\",\r\n"
					+ "       \"ApellidoMaterno\": \"SHIAA\"\r\n"
					+ "    },\r\n"
					+ "   \"DireccionesFisicas\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdProposito\": \"PART\",\r\n"
					+ "      \"Sector\": \"DASD\",\r\n"
					+ "      \"CodigoPostal\": \"123123\",\r\n"
					+ "      \"IdComuna\": 146,\r\n"
					+ "      \"Calle\": \"LAS DIRECCIONES\",\r\n"
					+ "      \"Numero\": \"5\",\r\n"
					+ "      \"Departamento\": \"DEPTO 405\",\r\n"
					+ "      \"Comentario\": \"ASDAD\",\r\n"
					+ "      \"IdCiudad\": 492,\r\n"
					+ "      \"CodigoUnicoTerritorial\": \"146\",\r\n"
					+ "      \"Predeterminado\": \"S\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"IdProposito\": \"PART\",\r\n"
					+ "      \"Sector\": \"DASD\",\r\n"
					+ "      \"CodigoPostal\": \"123123\",\r\n"
					+ "      \"IdComuna\": 146,\r\n"
					+ "      \"Calle\": \"LA SEGUNDA DIRECCION\",\r\n"
					+ "      \"Numero\": \"705\",\r\n"
					+ "      \"Departamento\": \"DEPTO 405\",\r\n"
					+ "      \"Comentario\": \"ASDAD\",\r\n"
					+ "      \"IdCiudad\": 492,\r\n"
					+ "      \"CodigoUnicoTerritorial\": \"146\",\r\n"
					+ "      \"Predeterminado\": \"N\"\r\n"
					+ "    }\r\n"
					+ "    ],\r\n"
					+ "   \"Telefonos\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdProposito\": \"PART\",\r\n"
					+ "      \"Numero\": \"+56253123127\",\r\n"
					+ "      \"IdPais\": 44,\r\n"
					+ "      \"Tipo\": \"MOVIL\",\r\n"
					+ "      \"Anexo\": \"DASDASD\",\r\n"
					+ "      \"Predeterminado\": \"S\"\r\n"
					+ "    }],\r\n"
					+ "   \"DireccionesElectronicas\": [\r\n"
					+ "    {\r\n"
					+ "      \"Comentario\": \"DASDAD\",\r\n"
					+ "      \"IdProposito\": \"PART\",\r\n"
					+ "      \"Direccion\": \"CORREO@EMAIL.CP\",\r\n"
					+ "      \"Predeterminado\": \"S\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"Comentario\": \"DASDAD\",\r\n"
					+ "      \"IdProposito\": \"PART\",\r\n"
					+ "      \"Direccion\": \"NUMERODOS@EMAIL.CP\",\r\n"
					+ "      \"Predeterminado\": \"N\"\r\n"
					+ "    }],\r\n"
					+ "   \"TiposDocumento\": [\r\n"
					+ "    {\r\n"
					+ "      \"TipoDocumento\": \"R\",\r\n"
					+ "      \"NumeroDocumento\": \"20046547-4\",\r\n"
					+ "      \"NumeroSerie\": \"3123123\",\r\n"
					+ "      \"FechaEmision\": \"2020-08-19\",\r\n"
					+ "      \"FechaVencimiento\": \"2025-05-06\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"TipoDocumento\": \"P\",\r\n"
					+ "      \"NumeroDocumento\": \"8710435-4\",\r\n"
					+ "      \"NumeroSerie\": \"3123123\",\r\n"
					+ "      \"FechaEmision\": \"2020-08-19\",\r\n"
					+ "      \"FechaVencimiento\": \"2025-05-06\"\r\n"
					+ "    }\r\n"
					+ "	   ],\r\n"
					+ "   \"CuentasBancarias\": [],\r\n"
					+ "   \"TarjetasBancarias\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdBanco\": 29,\r\n"
					+ "      \"IdTipo\": \"TDEB\",\r\n"
					+ "      \"NumeroTarjeta\": \"3123123123\",\r\n"
					+ "      \"CodigoVerificacion\": 642,\r\n"
					+ "      \"FechaVencimiento\": \"\"\r\n"
					+ "    }],\r\n"
					+ "   \"IndicadoresPersona\": [],\r\n"
					+ "   \"DatosCliente\": {\r\n"
					+ "      \"IdCategoriaInversionista\": \"ARRIESGADO\",\r\n"
					+ "      \"IdPerfilComercial\": \"PROJO\",\r\n"
					+ "      \"IdTipoCliente\": \"RETAIL\",\r\n"
					+ "      \"IdSegmentoCliente\":1,\r\n"
					+ "      \"CodigoEjecutivo\": 11496,\r\n"
					+ "      \"CodigoEjecutivoExterno\": \"ASD\",\r\n"
					+ "      \"TipoClienteCMF\": 805,\r\n"
					+ "      \"AreaCliente\": \"ADMCAR\",\r\n"
					+ "      \"Sucursal\": 1,\r\n"
					+ "      \"ClienteFinal\": \"S\",\r\n"
					+ "      \"IndicadoresCliente\": [],\r\n"
					+ "      \"DatosPortafolio\": {\r\n"
					+ "         \"CuentasCustodio\": [],\r\n"
					+ "         \"PreferenciasNegocio\": [ \r\n"
					+ "				{\r\n"
					+ "					\"ValorIndicador\": \"OPCION 1\",\r\n"	//para probar debe ser con tilde
					+ "					\"CodigoTipoIndicador\": \"COBROCUST\" \r\n"
					+ " 			}\r\n"
					+ " 		]\r\n"
					+ "       }\r\n"
					+ "    } \r\n"
					+ " }]\r\n"
					+ "}";
			
			String salida= "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_ENTIDADCREADA\":[{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":0,\"IdInterno\":0,\"Mensajes\":[{\"IdInterno\":0,\"TipoMensaje\":\"OK\",\"Codigo\":\"UI_CLIENTE_CREADO\",\"Mensaje\":\"CLIENTE CREADO\"},{\"IdInterno\":0,\"TipoMensaje\":\"OK\",\"Codigo\":\"CREAR_AUTORIZADO_ORDEN_EXITO\",\"Mensaje\":\"CREAR_AUTORIZADO_ORDEN_EXITO sin traduccion\"}]}]}";
			String salida2 = "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_ENTIDADCREADA\":[{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":0,\"IdInterno\":0,\"Mensajes\":[{\"IdInterno\":21851,\"TipoMensaje\":\"ADVERTENCIA\",\"Codigo\":\"UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE\",\"Mensaje\":\"UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE\"}]}]}";
			EntradaCrearClienteFullWS Objin = gson.fromJson(entrada, EntradaCrearClienteFullWS.class);
			SalidaCrearClienteFullWS Objout = gson.fromJson(salida2, SalidaCrearClienteFullWS.class);
			
			CrearClienteFullWSModificarPerfilesPortafolio demo = new CrearClienteFullWSModificarPerfilesPortafolio();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println(Objout.toString());
			
			try {
				SalidaServicioCrearClienteFullWS ObjOut = demo.logica(con, Objin, Objout);
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
