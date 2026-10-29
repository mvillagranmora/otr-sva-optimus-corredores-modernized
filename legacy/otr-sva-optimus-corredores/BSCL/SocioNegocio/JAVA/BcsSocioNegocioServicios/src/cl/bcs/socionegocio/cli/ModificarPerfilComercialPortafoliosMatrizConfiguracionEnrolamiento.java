package cl.bcs.socionegocio.cli;

import java.sql.Connection;
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
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento implements JavaCall {

    static final Logger logger = LogManager.getLogger(ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.class);
    
    private final String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarPerfilComercialPortafolioMatrizConfiguracionEnrolamiento_SelectClienteDatosGenerales.sql");
    private final String SQL_SELECT_PERFIL_COMERCIAL = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarPerfilComercialPortafolioMatrizConfiguracionEnrolamiento_SelectPerfilComercial.sql");
    private final String SQL_SELECT_MATRIZ_CONFIGURACION_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarPerfilComercialPortafolioMatrizConfiguracionEnrolamiento_SelectMatrizConfiguracionEnrolamiento.sql");
    private final String SQL_SELECT_DATOS_CLIENTE_PORTAFOLIOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarPerfilComercialPortafolioMatrizConfiguracionEnrolamiento_SelectDatosClientePortafolios.sql");
    private final String SQL_UPDATE_PORTAFOLIO_PERFIL = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarPerfilComercialPortafolioMatrizConfiguracionEnrolamiento_UpdatePortafolioPerfil.sql");
    
    public static class EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento extends BcsPojo {
    	
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdCliente;
		
    }
    
	public static class ClientePerfilComercial extends BcsPojo {
    	public Long IdPortafolio;
		public String IdPerfilProductos;
		public String IdPerfilComercialBase;
		public String IdCategoriaInversionista;		
	}

    public static class SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento extends BcsPojo {
    	
    	public Long P_OUT_ERRORESCONTADOR;
    	public List<MensajeSalida> P_OUT_MENSAJES;
    	
		public SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento() {
		    this.P_OUT_ERRORESCONTADOR = 0L;
		    this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
    }
    
    public static class MatrizConfiguracionEnrolamiento extends BcsPojo {
    	public String IdCategoriaInversionista;
    	public String IdFormaCobro;
    	public String IdPerfilComercialBase;
    	public String IdPerfilCobros;
    	public String IdPerfilProductos;
    	public String IdEstado;
    	public String PorDefecto;
    	public Long IdEmpresa;
    	public String CodigoPortafolio;
    }
    
    public static class DatosClientePortafolios extends BcsPojo {
    	public Long IdCliente;
    	public Long IdPortafolio;
    	public String DocumentoIdentidad;
    	public String IdCategoriaInversionista;
    	public String IdFormaCobro;
    	public String IdPerfilComercial;
    	public String IdPerfilCobros;
    	public String IdPerfilProductos;
    	public String CodigoPortafolio;
    	public String Proposito;
    }
    public static class UpdatePortafoliosPerfil extends BcsPojo {
    	public Long IdPortafolio;
    	public String IdPerfilComercial;
    	public String IdPerfilCobros;
    	public String IdPerfilProductos;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
	Gson gson = Utils.getGsonBuilder();
	EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento jin = gson.fromJson(in.toString(), EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.class);
	SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento out = logica(conn, jin);
	JsonParser parser = new JsonParser();
	JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
	return dataout;
    }

    public SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento logica(Connection con, EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento entrada) throws Exception {
		logger.info("INICIO::SalidaConfirmarModificarClienteDatosGeneralesMasivo.logica");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento salida = new SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento();
	
		if ( entrada.p_IdCliente == 0 ) {
		    salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_CLIENTE", null, con, entrada.p_IdEmpresa));
		    return salida;
		}
		
		salida = this.modificarPerfilComercial(con, entrada);
	
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.logica", finServicio - inicioServicio);
	
		return salida;
    }
    
    
    private SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento modificarPerfilComercial(Connection con, EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento entrada ) throws Exception {
		logger.debug("INICIO::ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.modificarPerfilComercial");
		long inicio = System.currentTimeMillis();
	
		SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento salida = new SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put("IdCliente", entrada.p_IdCliente);		
		
		cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales cliente = sql.query(con, SQL_SELECT_CLIENTE, params, cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales.class)
																			.stream()
																			.findFirst()
																			.orElse(new cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales());
		
		params.clear();
		params.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
        List<DatosClientePortafolios> datosClientePortafolios = sql.query(con, SQL_SELECT_DATOS_CLIENTE_PORTAFOLIOS, params, DatosClientePortafolios.class);
       
		params.clear();
		List<MatrizConfiguracionEnrolamiento> matrizConfiguracionEnrolamiento = sql.query(con, SQL_SELECT_MATRIZ_CONFIGURACION_ENROLAMIENTO, params, MatrizConfiguracionEnrolamiento.class);
		        
        HashMap<String, Object> paramSelectPerfilComercial = new HashMap<>(2);
		paramSelectPerfilComercial.put("IdCategoriaInversionista", cliente.IdCategoriaInversionista);
		paramSelectPerfilComercial.put("TipoEnrolamiento", cliente.TipoEnrolamiento);		
		List<ClientePerfilComercial> perfilComercial = sql.query(con, SQL_SELECT_PERFIL_COMERCIAL, paramSelectPerfilComercial, ClientePerfilComercial.class);
        
		
		if (perfilComercial.isEmpty()) {
			HashMap<String, String> parametros = new HashMap<>(2);
			parametros.put("CategoriaInversionista", cliente.IdCategoriaInversionista);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdCliente, MensajeSalida.MENSAJE_ERROR, "UI_CATEGORIA_INVERSIONISTA_SIN_PERFIL_COMERCIAL_POR_DEFECTO", parametros, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR += 1L;

			return salida;
		}
		
		List<UpdatePortafoliosPerfil> updatePortafoliosPerfil = new ArrayList<>();
					
		for( DatosClientePortafolios datoClientePortafolio: datosClientePortafolios ) {
			
			MatrizConfiguracionEnrolamiento matrizConfiguracionEnrolamientoFiltrado = new MatrizConfiguracionEnrolamiento();
			boolean a = datoClientePortafolio.Proposito.contains("APV");
			matrizConfiguracionEnrolamientoFiltrado = matrizConfiguracionEnrolamiento
					.stream()
					.filter(item -> (datoClientePortafolio.Proposito.contains("APV") ? item.CodigoPortafolio.equalsIgnoreCase("APV") : item.CodigoPortafolio.equalsIgnoreCase(datoClientePortafolio.CodigoPortafolio)) &&
									item.IdCategoriaInversionista.equalsIgnoreCase(datoClientePortafolio.IdCategoriaInversionista) &&
									item.IdFormaCobro.equalsIgnoreCase(datoClientePortafolio.IdFormaCobro))
					.findFirst().orElse(new MatrizConfiguracionEnrolamiento());

			UpdatePortafoliosPerfil paramUpdatePortafolioPerfil = new UpdatePortafoliosPerfil();
			paramUpdatePortafolioPerfil.IdPortafolio 			= datoClientePortafolio.IdPortafolio;
			paramUpdatePortafolioPerfil.IdPerfilComercial 		= matrizConfiguracionEnrolamientoFiltrado.IdPerfilComercialBase == null
					 												? perfilComercial.get(0).IdPerfilComercialBase 
					 												: matrizConfiguracionEnrolamientoFiltrado.IdPerfilComercialBase;
			paramUpdatePortafolioPerfil.IdPerfilCobros 			= matrizConfiguracionEnrolamientoFiltrado.IdPerfilCobros;
			paramUpdatePortafolioPerfil.IdPerfilProductos 		= matrizConfiguracionEnrolamientoFiltrado.IdPerfilProductos == null
					 												? perfilComercial.get(0).IdPerfilProductos
					 												: matrizConfiguracionEnrolamientoFiltrado.IdPerfilProductos;
			updatePortafoliosPerfil.add(paramUpdatePortafolioPerfil);

        }
		
		sql.batch(con, SQL_UPDATE_PORTAFOLIO_PERFIL, updatePortafoliosPerfil, UpdatePortafoliosPerfil.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.modificarPerfilComercial", fin - inicio);
		
		return salida;
    }
    
    public static void main(String[] args) throws Exception {
	System.out.println("== TEST ==");

	try (Connection con = PlataformaUtils.getDESAConnection()) {
	    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
	    String json = "{p_IdEmpresa:1,p_IdUsuario:21,p_IdModulo:\"SOC\",p_UsaReglasNegocio: \"N\",p_ClienteDatosGenerales: [{IdCliente:1424326}],p_IdEstadoAutorizacion: \"AUT\"}";
	    EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento Objin = gson.fromJson(json, EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.class);

	    ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento demo = new ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento();
	    System.out.println("ENTRADA");
	    System.out.println(Objin.toString());
	    System.out.println("SALIDA");

	    try {
	    	SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento ObjOut = demo.logica(con, Objin);
		con.rollback();
		System.out.println(ObjOut.toString());
	    } catch (Exception er) {
		er.printStackTrace();
	    } finally {
		con.rollback();
		con.close();
	    }
	}
	System.out.println("== END ==");
    }
    
}
