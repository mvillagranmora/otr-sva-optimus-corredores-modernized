package cl.bcs.plataforma.pla.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.utils.SQL;

public class ConfiguracionSistema {
	
	private static HashMap<String,HashMap<String,String>> registros; //MODULO,CODIGO,VALOR
	private static HashMap<String,String> SQLS; //MODULO,QUERY PARA CARGAR
	private static FileLoader loader;
	
	static {
		loader = new FileLoader();
		SQLS = new HashMap<>();
		registros = new HashMap<>();
		SQLS.put("PLATAFORMA", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionPlataforma.sql"));
		SQLS.put("ORDENES", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionOrdenes.sql"));
		SQLS.put("CUENTAINVERSION", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionCuentaInversion.sql"));
		SQLS.put("TESORERIA", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionTesoreria.sql"));
		SQLS.put("FACTURACION", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionFacturacion.sql"));
		SQLS.put("SOCIODENEGOCIO", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionSocioDeNegocio.sql"));
		SQLS.put("CUSTODIA", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionCustodia.sql"));
		SQLS.put("APV", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionAPV.sql"));
		SQLS.put("CONTABILIDAD", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionContabilidad.sql"));
		SQLS.put("FONDOMUTUO", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionFondoMutuo.sql"));
		SQLS.put("NORMATIVA", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionNormativa.sql"));
		SQLS.put("MESA", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionMesa.sql"));
		SQLS.put("PRODUCTO", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionProducto.sql"));//para ver productos habilitados en el corredor
		SQLS.put("LIMITE", loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionLimite.sql"));
	}
	
	public static void limpiarParametros(){
		registros.clear();
	}
	
	private static synchronized void getConfiguracionModulo(java.sql.Connection con,Long idEmpresa,String modulo) throws Exception {
		if (!SQLS.containsKey(modulo)) {
			throw new Exception("Configuracion modulo " + modulo + " no encontrada");
		}
		HashMap<String,Object> parms = new HashMap<>();
		SQL sql=new SQL();
		List<RegistroConfiguracion> ll =sql.query(con, SQLS.get(modulo),parms,RegistroConfiguracion.class);
		
		for(RegistroConfiguracion reg: ll) {
			if (registros.get(modulo+"-"+reg.IdEmpresa) == null) {
				registros.put(modulo+"-"+reg.IdEmpresa, new HashMap<String,String>());
			}
			registros.get(modulo+"-"+reg.IdEmpresa).put(reg.CodigoParametro, reg.Valor);
		}
	}
	
	private static String getParametro(Connection con,Long idEmpresa,String modulo,String nombreParametro) throws Exception {
		//if (!registros.containsKey(modulo+"-"+idEmpresa)) {
		//DESACTIVAMOS EL CACHE DE PARAMETROS, MIENTRAS RESOLVEMOS COMO COMPARTIR LA VARIABLE ESTATICA ENTRE VARIOS WAR
		if (true) {
			getConfiguracionModulo(con, idEmpresa,modulo);
		}
		
		if (!registros.get(modulo+"-"+idEmpresa).containsKey(nombreParametro))
			throw new Exception("Parametro " + nombreParametro + " en modulo " + modulo + " no encontrado");
		
		return registros.get(modulo+"-"+idEmpresa).get(nombreParametro);
		
	}
	
	public static long getParametroConfiguracionLong(Connection con,Long idEmpresa,String modulo,String nombreParametro) throws Exception {
		return Long.parseLong(getParametro(con,idEmpresa,modulo,nombreParametro));
	}
	
	public static String getParametroConfiguracionString(Connection con,Long idEmpresa,String modulo,String nombreParametro) throws Exception {
		return getParametro(con,idEmpresa,modulo,nombreParametro);
	}
	
	public static BigDecimal getParametroConfiguracionDecimal(Connection con,Long idEmpresa,String modulo,String nombreParametro) throws Exception {
		return new BigDecimal(getParametro(con,idEmpresa,modulo,nombreParametro));
	}

	public static Set<String> getParametroConfiguracionSet(Connection con, Long idEmpresa, String modulo, String nombreParametro, String delimeter) throws Exception {
		return Arrays.stream(getParametro(con, idEmpresa, modulo, nombreParametro).split(delimeter)).collect(Collectors.toSet());
	}
	
	public static class RegistroConfiguracion {
		public Long IdEmpresa;
		public String CodigoParametro;
		public String Valor;
	}	
	
}