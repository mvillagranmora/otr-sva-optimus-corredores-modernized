package cl.bcs.plataforma.services.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.bcs.plataforma.services.RegistrosConfiguracionCache;
import cl.bcs.plataforma.services.config.pojo.RegistroConfiguracion;

/**
 * Servlet implementation class ConfiguracionServlet
 */
@WebServlet("/Configuraciones")
public class ConfiguracionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/hanaDB")
	DataSource dataSource;
	private final Logger logger = LogManager.getLogger(ConfiguracionServlet.class);
	
	private static final String PLATAFORMA = "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'PLATAFORMA' AS \"Modulo\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'APV' AS \"Modulo\" FROM \"BSCL.APV.BD::PLA.ConfiguracionAPV\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'CONTABILIDAD' AS \"Modulo\" FROM \"BSCL.Contabilidad.BD::CON.ConfiguracionContabilidad\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'CUENTAINVERSION' AS \"Modulo\" FROM \"BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'CUSTODIA' AS \"Modulo\" FROM \"BSCL.Custodia.BD::CUS.Configuracion\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'FACTURACION' AS \"Modulo\" FROM \"BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion\"  UNION ALL  SELECT \"IdEmpresa\",\"NombreTipoParametro\" AS \"CodigoParametro\", \"ValorParametro\" AS \"Valor\", 'FONDOMUTUO' AS \"Modulo\" FROM \"BSCL.FondoMutuo.BD::FMM.Parametro\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'MESA' AS \"Modulo\" FROM \"BSCL.Mesa.BD::MES.ConfiguracionMesa\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'NORMATIVA' AS \"Modulo\" FROM \"BSCL.Normativa.BD::NOR.Configuracion\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'ORDENES' AS \"Modulo\" FROM \"BSCL.Orden.BD::ORD.ConfiguracionOrdenes\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'SOCIODENEGOCIO' AS \"Modulo\" FROM \"BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio\"  UNION ALL  SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\", 'TESORERIA' AS \"Modulo\" FROM \"BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria\"; ";
	private RegistrosConfiguracionCache CACHE = RegistrosConfiguracionCache.getInstance();
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ConfiguracionServlet() {
		super();
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 * EJ:
	 * {
        "p_CodigoParametro": "CONSULTASALDOBANCOINCLUYESALDOSPENDIENTE",
        "p_IdEmpresa": 1,
        "p_IdModulo": "TESORERIA"
       }
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{
		logger.info("INICIO CONFIGURACION SERVLET");
		try {
			boolean clearCache = false;
			
			StringBuilder sb = new StringBuilder();
	        String s;
			while ((s = request.getReader().readLine()) != null) {
	            sb.append(s);
	        } 
			
			String bodyParams= (sb.length()>0) ? sb.toString() : null;

			List<RegistroConfiguracion> confs = new ArrayList<>();
			logger.debug(bodyParams);
			if(bodyParams!=null) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode jsonNode = mapper.readTree(bodyParams);
			
				if(jsonNode.isArray()) {
					confs = Arrays.asList(mapper.readValue(bodyParams, RegistroConfiguracion[].class));
				}else {
			        String codigoParametro = (jsonNode.get("codigoParametro")!= null && !jsonNode.get("codigoParametro").isNull())? jsonNode.get("codigoParametro").asText() : "";
			        String idModulo = (jsonNode.get("modulo")!= null && !jsonNode.get("modulo").isNull())? jsonNode.get("modulo").asText() : "";
			        Long idEmpresa = (jsonNode.get("idEmpresa")!= null && !jsonNode.get("idEmpresa").isNull())? jsonNode.get("idEmpresa").asLong() : 0L;
			        //si en el json individual viene la variable clearCache - cargar nuevamente.
			        clearCache = (jsonNode.get("clearCache")!= null);
					confs = Arrays.asList(new RegistroConfiguracion(idEmpresa,idModulo,codigoParametro));
				}
				
				logger.trace("confs: " + confs);
				logger.trace("cache size: " + CACHE.size());
				
				if(configuracionesVacias(confs) || clearCache) {
					//si las llaves estan invalidadas o esta vacio el cache
					llenarCache();
				}
				
			}
			sendAsJson(response, getConfiguraciones(confs));
		} catch (SQLException | IOException e) {
			logger.error(e);
		}
		logger.info("FIN CONFIGURACION SERVLET");
	}
	
	private void llenarCache() throws SQLException {
		logger.info("ACTUALIZANDO CACHE...");
		QueryRunner run = new QueryRunner(dataSource);
		ResultSetHandler<List<RegistroConfiguracion>> h = new BeanListHandler<>(RegistroConfiguracion.class);
		run.query(PLATAFORMA, h).forEach(conf -> CACHE.put(conf.toString(), conf));
		logger.info("SE HAN CARGADO: " + CACHE.size() + " ELEMENTOS");
	}
	
	private boolean configuracionesVacias(List<RegistroConfiguracion> confs) {
		return getConfiguraciones(confs).isEmpty();
	}
	
		
	private List<RegistroConfiguracion> getConfiguraciones(List<RegistroConfiguracion> confs){				 
		return confs.stream()
				.map(conf -> CACHE.get(conf.getIdEmpresa()+"_"+conf.getModulo()+"_"+conf.getCodigoParametro()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}


	private void sendAsJson(HttpServletResponse response, List<RegistroConfiguracion> obj) throws IOException{
		response.setContentType("application/json");
		//marshalling con jackson
		String res = new ObjectMapper().writeValueAsString(obj);
		PrintWriter out = response.getWriter();
		out.print(res);
		out.flush();
	}

}
