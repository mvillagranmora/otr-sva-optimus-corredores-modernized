package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarImagen implements JavaCall {

	static final Logger logger = LogManager.getLogger(ConsultarEmpresa.class);
	
	private final String SQL_SELECT_IMAGENES = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarImagen_SelectImagenes.sql");
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static class ConsultarImagenEntrada {
		public long p_IdEmpresa;
		public List<String> p_imagenNombres = new ArrayList<>();
	}
	
	public static class ConsultarImagenSalida extends SalidaEstandar {
		public List<Imagen> OUT_IMAGENES;
		public ConsultarImagenSalida() {
			this.OUT_IMAGENES = new ArrayList<>();
		}
	}
	public static class Imagen {
		public Long IdEmpresa;
		public String IdImagen;
		public String NombreImagen;
		public String Tipo;
		public String Extension;
		public String Version;
		public String RutaPublica;
		public Imagen() {
			this.RutaPublica = "";
			this.NombreImagen = "";
		}
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	@Trace
	public ConsultarImagenSalida logica(Connection conn, ConsultarImagenEntrada entrada) throws Exception {
		ConsultarImagenSalida salida = new ConsultarImagenSalida();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_NombreImagen", entrada.p_imagenNombres);
        params.put("p_IdEmpresa", entrada.p_IdEmpresa);
        salida.OUT_IMAGENES = sql.query(conn, SQL_SELECT_IMAGENES, params, Imagen.class);
		return salida;
	}
}
