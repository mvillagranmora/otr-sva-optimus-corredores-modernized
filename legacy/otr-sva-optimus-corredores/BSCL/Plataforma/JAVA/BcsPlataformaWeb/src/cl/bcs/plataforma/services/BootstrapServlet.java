package cl.bcs.plataforma.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Sesion;
import cl.bcs.utils.Sesion.SesionData;
import cl.bcs.utils.Utils;

/***
 * 
 * @author cstange
 *
 */
@WebServlet("/bootstrap")
public class BootstrapServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(BootstrapServlet.class.getName());
	
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BootstrapServlet() {
        super();
    }

    
    public static class Diccionario {
    	public String Codigo;
    	public String Descripcion;
    	public String Abreviado;
    }
    
    protected String getDiccionario(Connection conn, String lenguaje,boolean js) throws SQLException {
    	String queryDiccionario = "select \"CodDiccionario\" as \"Codigo\", rtrim(ltrim(\"Descripcion\")) as \"Descripcion\" , rtrim(ltrim(\"Abreviado\")) as \"Abreviado\" " +
				"FROM \"BSCL.Plataforma.BD::PLA.Diccionario\" where \"CodLenguaje\" = :CodLenguaje order by \"CodDiccionario\";";
    	
    	if (lenguaje==null) lenguaje="CL";
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("CodLenguaje", lenguaje);
		List<Diccionario> diccionario = _sql.query(conn,queryDiccionario , params , Diccionario.class);
		
		JsonObject outkey=new JsonObject();
		for (int i=0;i<diccionario.size();i++) {
			Diccionario obj = diccionario.get(i);
			outkey.addProperty(obj.Codigo, obj.Descripcion);
			outkey.addProperty(obj.Codigo+"_ABR", obj.Abreviado);
		}
		
		if (js) return "var BCS_DICCIONARIO= "+ outkey.toString() +";\n";
		return outkey.toString();

    }
    
    public static class Empresa {
        public Long IdEmpresa;
        public String Nombre;
    }
    
    protected String getEmpresas(Connection con, boolean js) throws Exception {
        JsonArray output = new JsonArray();
        String query = "select \"IdEmpresa\",\"Nombre\" from \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" where \"IdEstado\" = 'ACT'";
        
        SQL sql = new SQL();
        List<Empresa> empresas = sql.query(con, query, new HashMap<>(), Empresa.class);
        
        for (Empresa emp : empresas) {
            JsonObject obj = new JsonObject();
            obj.addProperty("IdEmpresa", emp.IdEmpresa);
            obj.addProperty("Nombre", emp.Nombre);
            output.add(obj);
        }
        
        if (js) return "var BCS_EMPRESAS= " + output.toString() + ";\n";
        return output.toString();
    }
    
    public static class Imagen {
        public String NombreImagen;
        public byte[] Binario;
    }
    
    protected String getImagen(Connection con, boolean js) throws Exception {
        JsonArray outputImg = new JsonArray();
        String query = "select \"NombreImagen\",\"Binario\" from \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen\" where \"NombreImagen\" = 'LOGO_CORREDOR_LOGIN'";
        
        SQL sql = new SQL();
        List<Imagen> imagenes = sql.query(con, query, new HashMap<>(), Imagen.class);
        
        for (Imagen img : imagenes) {
            JsonObject obj = new JsonObject();
            obj.addProperty("NombreImagen", img.NombreImagen);
            
            if (img.Binario != null) {
                obj.addProperty("Binario", Base64.encodeBase64String(img.Binario));
            } else {
                obj.addProperty("Binario", "");
            }
            
            outputImg.add(obj);
        }
        
        if (js) return "var BCS_IMAGEN= " + outputImg.toString() + ";\n";
        return outputImg.toString();
    }
    
    public static class DiaFeriado {
		public Long IdFeriado;
		public String Tipo;
		public String Fecha;
		public Integer Dia;
		public String DescripcionDia;
		public Integer AgnoCarga;
		public Long IdPais;
	}
    protected String getFeriados(Connection con, boolean js) throws Exception {
    	JsonObject output=new JsonObject();
        String query="select  \"IdFeriado\",\"Tipo\",TO_VARCHAR (\"Fecha\",'YYYY-MM-DD') as \"Fecha\",\"Dia\",\"DescripcionDia\",\"AgnoCarga\",\"IdPais\" FROM  \"BSCL.Plataforma.BD::REG.DiaFeriado\" where \"IdEstado\" = 'ACT'";
        SQL sql = new SQL();
	    List<DiaFeriado> feriados = sql.query(con,query,new HashMap<>(),DiaFeriado.class);
	    for (DiaFeriado feriado: feriados) {
	    	if (output.get(feriado.IdPais+"") == null) output.add(feriado.IdPais+"", new JsonObject());
	    	JsonObject obj=output.get(feriado.IdPais+"").getAsJsonObject();
    		JsonObject objFecha=new JsonObject();
    		obj.add(feriado.Fecha, objFecha);            		
    		objFecha.addProperty("Tipo", feriado.Tipo);
    		objFecha.addProperty("Dia", feriado.Dia);
    		objFecha.addProperty("DescripcionDia", feriado.DescripcionDia);
    		objFecha.addProperty("AgnoCarga", feriado.AgnoCarga);       
	    }
	    
        
		if (js) return "var bcs_feriados = "+ output.toString() +";\n";
		return output.toString();       
    }    
    
    protected String getConfiguracionAsJson(HashMap<String,HashMap<String,String>> conf) {
    	List<String> parametrosPermitidos = Arrays.asList(new String[]{"APV.Habilitado", "TOTALREFACTURACION","URLWIKIOPT","TasaPrestamoIgualTasaEmprestito",
    			"REFAC_MASIVA","Desabilitar_OD_AFP","EVT.TIEMPO_PULL_NOTIFICACIONES","REFACTURAR_BATCH_DESDE","CalculoPrecioPunta","ParametroPrecioMercado", "ActivaGeneraSpot"} );
    	JsonObject output=new JsonObject();
    	for (String confModulo : conf.keySet()) {
    		JsonObject modulo = new JsonObject();
    		for (String param : conf.get(confModulo).keySet()) {
    			if (parametrosPermitidos.contains(param) || "PRODUCTO".equalsIgnoreCase(confModulo)) {
    				modulo.addProperty(param, conf.get(confModulo).get(param));
    			}
    		}
    		output.add(confModulo,modulo);
    	}
    	return output.toString();
    }
    
    public static class TipoOperacionInstrumento {
        public String IdTipoInstrumento;
        public String IdTipoOperacion;
        public String AccionOrden;
        public String IdTipoProducto;
        public String EsVentaCorta;
        public String EsPutEsCall;
    }
    
    protected String getTipoOperacionInstrumento(Connection con) throws Exception {
        JsonArray output = new JsonArray();
        
        String query = "select TI.\"IdTipoInstrumento\",TTO.\"IdTipoOperacion\",TTO.\"AccionOrden\",TTO.\"IdTipoProducto\",TTO.\"EsVentaCorta\",TTO.\"EsPutEsCall\" \r\n" + 
                "from \"BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento\" TI \r\n" + 
                "INNER join \"BSCL.Plataforma.BD::INS.TipoOperacion\" tto ON TI.\"IdTipoOperacion\" = TTO.\"IdTipoOperacion\" " +
                "WHERE TTO.\"AccionOrden\" IS NOT NULL AND TTO.\"EsOrden\" = 'S'";

        SQL sql = new SQL();
        List<TipoOperacionInstrumento> resultados = sql.query(con, query, new HashMap<>(), TipoOperacionInstrumento.class);

        for (TipoOperacionInstrumento res : resultados) {
            JsonObject linea = new JsonObject();
            linea.addProperty("AccionOrden", res.AccionOrden);
            linea.addProperty("IdTipoInstrumento", res.IdTipoInstrumento);
            linea.addProperty("IdTipoOperacion", res.IdTipoOperacion);
            linea.addProperty("EsVentaCorta", res.EsVentaCorta);
            linea.addProperty("IdTipoProducto", res.IdTipoProducto);
            linea.addProperty("EsPutEsCall", res.EsPutEsCall);
            
            output.add(linea);
        }
        
        return output.toString();
    }    
    
    public static class Preferencia {
        public String Llave;
        public String Valor;
    }
    
    protected String getPreferencias(Connection con, String IdEmpresa, String IdUsuario, boolean js) throws Exception {
        JsonObject output = new JsonObject();
        
        String query = "SELECT \"Llave\", \"Valor\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario\" " +
                       "WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdUsuario\" = :IdUsuario";
        
        if (IdEmpresa != null && IdUsuario != null) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("IdEmpresa", Long.parseLong(IdEmpresa));
            params.put("IdUsuario", Long.parseLong(IdUsuario));

            SQL sql = new SQL();
            List<Preferencia> preferencias = sql.query(con, query, params, Preferencia.class);

            for (Preferencia pref : preferencias) {
                output.addProperty(pref.Llave, pref.Valor);
            }
        }

        if (js) return "var BCS_PREFERENCIAS = " + output.toString() + ";\n";
        return output.toString();
    }
    
    public static class PreferenciaGrilla {
        public String Llave;
        public String Valor;
    } 
    
    protected String getPreferenciasGrilla(Connection con, String IdEmpresa, boolean js) throws Exception {
	    JsonObject output = new JsonObject();
	    
	    String query = "SELECT \"Llave\", \"Valor\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla\" " +
	                   "WHERE \"IdEmpresa\" = :IdEmpresa";
	    
	    if (IdEmpresa != null) {
	        HashMap<String, Object> params = new HashMap<>();
	        params.put("IdEmpresa", Long.parseLong(IdEmpresa));

	        SQL sql = new SQL();
	        List<PreferenciaGrilla> prefs = sql.query(con, query, params, PreferenciaGrilla.class);

	        for (PreferenciaGrilla pref : prefs) {
	            output.addProperty(pref.Llave, pref.Valor);
	        }
	    }

	    if (js) return "var BCS_PREFERENCIAS_GRILLA = " + output.toString() + ";\n";
	    return output.toString();
	}  
    
    public static class ConfiguracionFiltro {
        public Long IdConfiguracionFiltroInterfaz;
        public String Controlador;
        public String TipoObjeto;
        public String NombreObjeto;
        public String IdEstado;
        public String Filtros;
    }
    
    protected String getFiltroComboBase(Connection con, boolean js) throws Exception {
	    JsonObject output = new JsonObject();
	    String query = "select \"IdConfiguracionFiltroInterfaz\",\"Controlador\",\"TipoObjeto\",\"NombreObjeto\",\"IdEstado\",\"Filtros\" " +
	                   "FROM \"BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz\" WHERE \"IdEstado\" = 'ACT'";
	    
	    SQL sql = new SQL();
	    List<ConfiguracionFiltro> listaFiltros = sql.query(con, query, new HashMap<>(), ConfiguracionFiltro.class);
	    
	    for (ConfiguracionFiltro f : listaFiltros) {
	        
	    	String filtrosLimpios = f.Filtros != null ? f.Filtros : "";
	        filtrosLimpios = filtrosLimpios.replace("\t", "").replace("\r", "").replace("\n", "");

	        if (output.get(f.Controlador) == null) {
	            output.add(f.Controlador, new JsonObject());
	        }
	        JsonObject ele = output.get(f.Controlador).getAsJsonObject();
	        
	        if (ele.get(f.TipoObjeto) == null) {
	            ele.add(f.TipoObjeto, new JsonObject());
	        }
	        JsonObject elTpo = ele.get(f.TipoObjeto).getAsJsonObject();
	        
	        elTpo.addProperty(f.NombreObjeto, filtrosLimpios);
	    }
	    
	    if (js) return "var bcs_filtrocombobase = " + output.toString() + ";\n";
	    return output.toString();
	}
    
    public static class UsuarioAcceso {
	    public String IdElementoHtml;
	    public String EsVisibleMenu;
	    public String Existe;
	    public Long IdUsuario;
	    public Long IdAcceso;
	    public String NombreAcceso;
	    public String TipoAcceso;
	    public Long Padre;
	    public Integer Orden;
	    public Long IdServicio;
	}
    
    protected String getAccesos(Connection con, String IdEmpresa, String IdUsuario, boolean js) throws Exception {
	    JsonObject output = new JsonObject();
	    long idEmpresaLong = Long.parseLong(IdEmpresa);
	    long idUsuarioLong = Long.parseLong(IdUsuario);

	    StoredProcedure pSP = new StoredProcedure("BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin");
	    JsonObject input = new JsonObject();
	    input.addProperty("p_IdEmpresa", idEmpresaLong);
	    input.addProperty("p_IdUsuario", idUsuarioLong);
	    
	    JsonObject routput = pSP.call(con, input);
	    String usuarioMaster = routput.get("p_out_EsAdmin".toUpperCase()).getAsString();
	    boolean isAdmin = "S".equals(usuarioMaster);
	    output.addProperty("ADMIN", isAdmin);

	    String existePorDefecto = Utils.getConfiguracion(con, idEmpresaLong, "PLA.ConflictoAccesoDefecto");

	    String query = "SELECT * FROM \"BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso\" " +
	                   "WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdUsuario\" = :IdUsuario " +
	                   "AND \"MostrarAcceso\" = 'S' ORDER BY \"Padre\", \"Orden\"";

	    HashMap<String, Object> params = new HashMap<>();
	    params.put("IdEmpresa", idEmpresaLong);
	    params.put("IdUsuario", idUsuarioLong);

	    SQL sql = new SQL();
	    List<UsuarioAcceso> accesos = sql.query(con, query, params, UsuarioAcceso.class);

	    for (UsuarioAcceso acc : accesos) {
	        String idHtml = acc.IdElementoHtml;
	        String existeDB = acc.Existe;
	        String valorFinalExiste = isAdmin ? "S" : existeDB;

	        if (output.get(idHtml) == null) {
	            output.add(idHtml, new JsonObject());
	        }
	        JsonObject ele = output.get(idHtml).getAsJsonObject();

	        if (ele.has("Existe") && !isAdmin) {
	            if (!ele.get("Existe").getAsString().equals(existeDB)) {
	                valorFinalExiste = existePorDefecto;
	            }
	        }

	        ele.addProperty("IdElementoHtml", idHtml);
	        ele.addProperty("Existe", valorFinalExiste);
	        ele.addProperty("EsVisible", acc.EsVisibleMenu);
	    }

	    if (js) return "var BCS_ACCESOS = " + output.toString() + ";\n";
	    return output.toString();
	}
    
    protected String getFormatos(Connection con) throws Exception { 
    	String toutput="";
    	
    	StoredProcedure pSP=new StoredProcedure("BSCL.Plataforma.BD.Servicios::ConsultarFormatos");
		JsonObject output=pSP.call(con, new JsonObject());
		
		// ---------------------------------------------------------------------------------------------
		// Logica que arma la matriz de cantidad [IdMoneda][IdTipoInstrumento][IdTipoOperacion][X][Y]
		// ---------------------------------------------------------------------------------------------
		{			
			JsonArray  ar_resl=output.get("p_out_MonedaFormato".toUpperCase()).getAsJsonArray();
			JsonObject ar_obj=new JsonObject();			
			for (int i=0;i<ar_resl.size();i++)		
			{
				JsonObject el_obj=ar_resl.get(i).getAsJsonObject();			
				if (el_obj.get("IdUsoMonedaFormato") == null) continue;
				
				String IdUsoMonedaFormato=el_obj.get("IdUsoMonedaFormato").getAsString();												
				if (ar_obj.get(IdUsoMonedaFormato) == null)  ar_obj.add(IdUsoMonedaFormato, new JsonObject());
				JsonObject objUsoMonedaFormato = ar_obj.get(IdUsoMonedaFormato).getAsJsonObject();
				
				String IdMoneda=el_obj.get("IdMoneda").getAsString();												
				if (objUsoMonedaFormato.get(IdMoneda) == null)  objUsoMonedaFormato.add(IdMoneda, new JsonObject());
				JsonObject objMoneda = objUsoMonedaFormato.get(IdMoneda).getAsJsonObject();

				String IdTipoInstrumento=el_obj.get("IdTipoInstrumento").getAsString();												
				if (objMoneda.get(IdTipoInstrumento) == null)  objMoneda.add(IdTipoInstrumento, new JsonObject());
				JsonObject objTipoInstrumento = objMoneda.get(IdTipoInstrumento).getAsJsonObject();
								
				//Para algunas ramas el IdTipoOperacion no se utiliza por lo que debe quedar vacÃ­o en BD y en matriz).
				String IdTipoOperacion=el_obj.get("IdTipoOperacion").getAsString();												
				if (objTipoInstrumento.get(IdTipoOperacion) == null)  objTipoInstrumento.add(IdTipoOperacion, new JsonObject());
				JsonObject objTipoOperacion = objTipoInstrumento.get(IdTipoOperacion).getAsJsonObject();
				 
				objTipoOperacion.addProperty("CantidadDecimales", el_obj.get("CantidadDecimales").getAsInt());
				objTipoOperacion.addProperty("CantidadTrucaRedondea", el_obj.get("CantidadTrucaRedondea").getAsString());						 
			}			
			toutput+="\n\nvar bcs_formato_cantidad = "+ar_obj.toString()+";";
		}
	
		// ---------------------------------------------------------------------------------------------
		// Logica que arma la matriz de Precios [IdMoneda][IdTipoInstrumento][IdTipoOperacion][X][Y]
		// ---------------------------------------------------------------------------------------------
		{
			JsonArray  ar_resl=output.get("p_out_MonedaFormato".toUpperCase()).getAsJsonArray();
			JsonObject ar_obj=new JsonObject();			
			for (int i=0;i<ar_resl.size();i++)		
			{
				JsonObject el_obj=ar_resl.get(i).getAsJsonObject();			
				if (el_obj.get("IdUsoMonedaFormato") == null) continue;
				
				String IdUsoMonedaFormato=el_obj.get("IdUsoMonedaFormato").getAsString();												
				if (ar_obj.get(IdUsoMonedaFormato) == null)  ar_obj.add(IdUsoMonedaFormato, new JsonObject());
				JsonObject objUsoMonedaFormato = ar_obj.get(IdUsoMonedaFormato).getAsJsonObject();

				String IdMoneda=el_obj.get("IdMoneda").getAsString();												
				if (objUsoMonedaFormato.get(IdMoneda) == null)  objUsoMonedaFormato.add(IdMoneda, new JsonObject());
				JsonObject objMoneda = objUsoMonedaFormato.get(IdMoneda).getAsJsonObject();

				String IdTipoInstrumento=el_obj.get("IdTipoInstrumento").getAsString();												
				if (objMoneda.get(IdTipoInstrumento) == null)  objMoneda.add(IdTipoInstrumento, new JsonObject());
				JsonObject objTipoInstrumento = objMoneda.get(IdTipoInstrumento).getAsJsonObject();

				
				String IdTipoOperacion=el_obj.get("IdTipoOperacion").getAsString();												
				if (ar_obj.get(IdTipoOperacion) == null)  objTipoInstrumento.add(IdTipoOperacion, new JsonObject());
				JsonObject objTipoOperacion = objTipoInstrumento.get(IdTipoOperacion).getAsJsonObject();
				
				objTipoOperacion.addProperty("PrecioDecimales", el_obj.get("PrecioDecimales").getAsInt());
				objTipoOperacion.addProperty("PrecioTrucaRedondea", el_obj.get("PrecioTrucaRedondea").getAsString());						 
				
			}
			toutput+="\n\nvar bcs_formato_precio = "+ar_obj.toString()+";";
		}
		
		// ---------------------------------------------------------------------------------------------
		// Logica que arma la matriz de Tasas [IdMoneda][IdTipoInstrumento][IdTipoOperacion][X][Y]
		// ---------------------------------------------------------------------------------------------	
		{
			JsonArray  ar_resl=output.get("p_out_MonedaFormato".toUpperCase()).getAsJsonArray();
			JsonObject ar_obj=new JsonObject();			
			for (int i=0;i<ar_resl.size();i++)		
			{
				JsonObject el_obj=ar_resl.get(i).getAsJsonObject();			
				if (el_obj.get("IdUsoMonedaFormato") == null) continue;
				
				String IdUsoMonedaFormato=el_obj.get("IdUsoMonedaFormato").getAsString();												
				if (ar_obj.get(IdUsoMonedaFormato) == null)  ar_obj.add(IdUsoMonedaFormato, new JsonObject());
				JsonObject objUsoMonedaFormato = ar_obj.get(IdUsoMonedaFormato).getAsJsonObject();

				String IdMoneda=el_obj.get("IdMoneda").getAsString();												
				if (objUsoMonedaFormato.get(IdMoneda) == null)  objUsoMonedaFormato.add(IdMoneda, new JsonObject());
				JsonObject objMoneda = objUsoMonedaFormato.get(IdMoneda).getAsJsonObject();

				String IdTipoInstrumento=el_obj.get("IdTipoInstrumento").getAsString();												
				if (objMoneda.get(IdTipoInstrumento) == null)  objMoneda.add(IdTipoInstrumento, new JsonObject());
				JsonObject objTipoInstrumento = objMoneda.get(IdTipoInstrumento).getAsJsonObject();

				
				String IdTipoOperacion=el_obj.get("IdTipoOperacion").getAsString();												
				if (ar_obj.get(IdTipoOperacion) == null)  objTipoInstrumento.add(IdTipoOperacion, new JsonObject());
				JsonObject objTipoOperacion = objTipoInstrumento.get(IdTipoOperacion).getAsJsonObject();
				
				objTipoOperacion.addProperty("TasaDecimales", el_obj.get("TasaDecimales").getAsInt());
				objTipoOperacion.addProperty("TasaTrucaRedondea", el_obj.get("TasaTrucaRedondea").getAsString());						 
				
			}
			toutput+="\n\nvar bcs_formato_tasa = "+ar_obj.toString()+";";			
		}
		
		// ---------------------------------------------------------------------------------------------
		// Logica que arma la matriz de Montos [IdMoneda][IdTipoInstrumento][IdTipoOperacion][X][Y]
		// ---------------------------------------------------------------------------------------------
		{
			JsonArray  ar_resl=output.get("p_out_MonedaFormato".toUpperCase()).getAsJsonArray();
			JsonObject ar_obj=new JsonObject();			
			for (int i=0;i<ar_resl.size();i++)		
			{
				JsonObject el_obj=ar_resl.get(i).getAsJsonObject();			
				if (el_obj.get("IdUsoMonedaFormato") == null) continue;
				
				String IdUsoMonedaFormato=el_obj.get("IdUsoMonedaFormato").getAsString();												
				if (ar_obj.get(IdUsoMonedaFormato) == null)  ar_obj.add(IdUsoMonedaFormato, new JsonObject());
				JsonObject objUsoMonedaFormato = ar_obj.get(IdUsoMonedaFormato).getAsJsonObject();

				String IdMoneda=el_obj.get("IdMoneda").getAsString();												
				if (objUsoMonedaFormato.get(IdMoneda) == null)  objUsoMonedaFormato.add(IdMoneda, new JsonObject());
				JsonObject objMoneda = objUsoMonedaFormato.get(IdMoneda).getAsJsonObject();

				String IdTipoInstrumento=el_obj.get("IdTipoInstrumento").getAsString();												
				if (objMoneda.get(IdTipoInstrumento) == null)  objMoneda.add(IdTipoInstrumento, new JsonObject());
				JsonObject objTipoInstrumento = objMoneda.get(IdTipoInstrumento).getAsJsonObject();

				//Para algunas ramas el IdTipoOperacion no se utiliza por lo que debe quedar vacÃ­o(en BD y en matriz).				
				String IdTipoOperacion=el_obj.get("IdTipoOperacion").getAsString();												
				if (ar_obj.get(IdTipoOperacion) == null)  objTipoInstrumento.add(IdTipoOperacion, new JsonObject());
				JsonObject objTipoOperacion = objTipoInstrumento.get(IdTipoOperacion).getAsJsonObject();
				
				objTipoOperacion.addProperty("MontoDecimales", el_obj.get("MontoDecimales").getAsInt());
				objTipoOperacion.addProperty("MontoTrucaRedondea", el_obj.get("MontoTrucaRedondea").getAsString());						 
				
			}
			toutput+="\n\nvar bcs_formato_monto = "+ar_obj.toString()+";";			
		}
		
			
		// ---------------------------------------------------------------------------------------------
		// Logica que arma la matriz de Tipo Cambio [IdMoneda][IdTipoInstrumento][IdTipoOperacion][X][Y]
		// ---------------------------------------------------------------------------------------------	
		{
			JsonArray  ar_resl=output.get("p_out_MonedaFormato".toUpperCase()).getAsJsonArray();
			JsonObject ar_obj=new JsonObject();			
			for (int i=0;i<ar_resl.size();i++)		
			{
				JsonObject el_obj=ar_resl.get(i).getAsJsonObject();			
				if (el_obj.get("IdUsoMonedaFormato") == null) continue;
				
				String IdUsoMonedaFormato=el_obj.get("IdUsoMonedaFormato").getAsString();												
				if (ar_obj.get(IdUsoMonedaFormato) == null)  ar_obj.add(IdUsoMonedaFormato, new JsonObject());
				JsonObject objUsoMonedaFormato = ar_obj.get(IdUsoMonedaFormato).getAsJsonObject();

				String IdMoneda=el_obj.get("IdMoneda").getAsString();												
				if (objUsoMonedaFormato.get(IdMoneda) == null)  objUsoMonedaFormato.add(IdMoneda, new JsonObject());
				JsonObject objMoneda = objUsoMonedaFormato.get(IdMoneda).getAsJsonObject();

				String IdTipoInstrumento=el_obj.get("IdTipoInstrumento").getAsString();												
				if (objMoneda.get(IdTipoInstrumento) == null)  objMoneda.add(IdTipoInstrumento, new JsonObject());
				JsonObject objTipoInstrumento = objMoneda.get(IdTipoInstrumento).getAsJsonObject();

				//Para algunas ramas el IdTipoOperacion no se utiliza por lo que debe quedar vacÃ­o(en BD y en matriz).				
				String IdTipoOperacion=el_obj.get("IdTipoOperacion").getAsString();												
				if (ar_obj.get(IdTipoOperacion) == null)  objTipoInstrumento.add(IdTipoOperacion, new JsonObject());
				JsonObject objTipoOperacion = objTipoInstrumento.get(IdTipoOperacion).getAsJsonObject();
				
				objTipoOperacion.addProperty("TipoCambioDecimales", el_obj.get("TipoCambioDecimales").getAsInt());
				objTipoOperacion.addProperty("TipoCambioTrucaRedondea", el_obj.get("TipoCambioTrucaRedondea").getAsString());						 
				
			}
			toutput+="\n\nvar bcs_formato_tipoCambio = "+ar_obj.toString()+";";			
		}
		
			
		// ---------------------------------------------------------------------------------------------
		// Logica que arma la matriz de Paridad [IdMoneda][IdTipoInstrumento][IdTipoOperacion][X][Y]
		// ---------------------------------------------------------------------------------------------	 
		{
			JsonArray  ar_resl=output.get("p_out_MonedaFormato".toUpperCase()).getAsJsonArray();
			JsonObject ar_obj=new JsonObject();			
			for (int i=0;i<ar_resl.size();i++)		
			{
				JsonObject el_obj=ar_resl.get(i).getAsJsonObject();			
				if (el_obj.get("IdUsoMonedaFormato") == null) continue;
				
				String IdUsoMonedaFormato=el_obj.get("IdUsoMonedaFormato").getAsString();												
				if (ar_obj.get(IdUsoMonedaFormato) == null)  ar_obj.add(IdUsoMonedaFormato, new JsonObject());
				JsonObject objUsoMonedaFormato = ar_obj.get(IdUsoMonedaFormato).getAsJsonObject();

				String IdMoneda=el_obj.get("IdMoneda").getAsString();												
				if (objUsoMonedaFormato.get(IdMoneda) == null)  objUsoMonedaFormato.add(IdMoneda, new JsonObject());
				JsonObject objMoneda = objUsoMonedaFormato.get(IdMoneda).getAsJsonObject();

				String IdTipoInstrumento=el_obj.get("IdTipoInstrumento").getAsString();												
				if (objMoneda.get(IdTipoInstrumento) == null)  objMoneda.add(IdTipoInstrumento, new JsonObject());
				JsonObject objTipoInstrumento = objMoneda.get(IdTipoInstrumento).getAsJsonObject();

				//Para algunas ramas el IdTipoOperacion no se utiliza por lo que debe quedar vacÃ­o(en BD y en matriz).				
				String IdTipoOperacion=el_obj.get("IdTipoOperacion").getAsString();												
				if (ar_obj.get(IdTipoOperacion) == null)  objTipoInstrumento.add(IdTipoOperacion, new JsonObject());
				JsonObject objTipoOperacion = objTipoInstrumento.get(IdTipoOperacion).getAsJsonObject();
				
				objTipoOperacion.addProperty("ParidadDecimales", el_obj.get("ParidadDecimales").getAsInt());
				objTipoOperacion.addProperty("ParidadTrucaRedondea", el_obj.get("ParidadTrucaRedondea").getAsString());						 
				
			}
			toutput+="\n\nvar bcs_formato_paridad = "+ar_obj.toString()+";";			
		}		

		// ---------------------------------------------------------------------------------------------
		// Logica que arma la matriz de Puntos [IdMoneda][IdTipoInstrumento][IdTipoOperacion][X][Y]
		// ---------------------------------------------------------------------------------------------	 
		{
			JsonArray  ar_resl=output.get("p_out_MonedaFormato".toUpperCase()).getAsJsonArray();
			JsonObject ar_obj=new JsonObject();			
			for (int i=0;i<ar_resl.size();i++)		
			{
				JsonObject el_obj=ar_resl.get(i).getAsJsonObject();			
				if (el_obj.get("IdUsoMonedaFormato") == null) continue;
				
				String IdUsoMonedaFormato=el_obj.get("IdUsoMonedaFormato").getAsString();												
				if (ar_obj.get(IdUsoMonedaFormato) == null)  ar_obj.add(IdUsoMonedaFormato, new JsonObject());
				JsonObject objUsoMonedaFormato = ar_obj.get(IdUsoMonedaFormato).getAsJsonObject();

				String IdMoneda=el_obj.get("IdMoneda").getAsString();												
				if (objUsoMonedaFormato.get(IdMoneda) == null)  objUsoMonedaFormato.add(IdMoneda, new JsonObject());
				JsonObject objMoneda = objUsoMonedaFormato.get(IdMoneda).getAsJsonObject();

				String IdTipoInstrumento=el_obj.get("IdTipoInstrumento").getAsString();												
				if (objMoneda.get(IdTipoInstrumento) == null)  objMoneda.add(IdTipoInstrumento, new JsonObject());
				JsonObject objTipoInstrumento = objMoneda.get(IdTipoInstrumento).getAsJsonObject();

				//Para algunas ramas el IdTipoOperacion no se utiliza por lo que debe quedar vacÃ­o(en BD y en matriz).				
				String IdTipoOperacion=el_obj.get("IdTipoOperacion").getAsString();												
				if (ar_obj.get(IdTipoOperacion) == null)  objTipoInstrumento.add(IdTipoOperacion, new JsonObject());
				JsonObject objTipoOperacion = objTipoInstrumento.get(IdTipoOperacion).getAsJsonObject();
				
				objTipoOperacion.addProperty("PuntosDecimales", el_obj.get("PuntosDecimales").getAsInt());
				objTipoOperacion.addProperty("PuntosTrucaRedondea", el_obj.get("PuntosTrucaRedondea").getAsString());						 
				
			}
			toutput+="\n\nvar bcs_formato_puntos = "+ar_obj.toString()+";";			
		}	
		
		
		return toutput;		
    }        


    protected String parsePagina(String opcion,HashMap<String,String>  parm, HashMap<String, HashMap<String, String>> parametrosConfiguracion) throws IOException {
    	String pagina=null;
    	String libbase=null;
    	String libbasecss=null;
    	String libextracss = null;
    	String libthemecss = null;
    	String config=null;
    	String modulos=null;
    	String menu=null;
    	String ventana=null;
    	String rumdatadog=null;
    	
    	logger.info("INICIO PARAMETROS");
    	for (String param : parm.keySet()) {
    		String value = parm.get(param);
    		logger.info("Parametro : " + param + " Valor:" + value);	
    	}
    	
    	logger.info("FIN PARAMETROS");
    	
		
    	
    	try (Connection conn = dataSource.getConnection();) 
    	{ 
    		
    		//PAGE
    		if (opcion.equals("HOME")) pagina=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.views","home.html");
    		if (opcion.equals("LOGIN")) pagina=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.views","login.html");
    		if (opcion.equals("401")) pagina=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.views","401.html");
    		
    		//LIBS BASE    		
    		libbase=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.resources","config_lib.html");
    		libbasecss=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.resources","config_css.html");
    		libthemecss = "<link href='styles/app.css' rel='stylesheet' />"; 		
    		if (parm.get("modob1").equals("true")) libthemecss+="\n<link href='styles/appB1.css' rel='stylesheet' />";

    		//config=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.resources","config_var.html");
    		config = "";
    		modulos=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.resources","config_mod.html");
    		String menuOPC = null;
    		
    		if (parm.get("ambiente") == null) {
    			libextracss = "";   
    		}
    		else {
    			libextracss = "<link href='styles/extra." + parm.get("ambiente").toLowerCase() + ".css' rel='stylesheet' />";   
    		}
    		
    		if (parm.get("menu") == null) {
    			menuOPC="config_menu"+parm.get("ambiente").replaceAll("DES","")+".js";	
    		}
    		else {
    			menuOPC="config_menu"+parm.get("menu").replaceAll("DES","")+".js";
    		}
    		
    		menu=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.resources",menuOPC);
    		ventana=Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.resources","config_ventana.js");
    		
    		//DINAMICOS    		
    		String diccionario=getDiccionario(conn,parm.get("local"),false);
    		String empresas=getEmpresas(conn,false);
    		String logoCorredor=getImagen(conn,false);
    		String feriados="{ }";
    		String preferencias=" { }";
    		String preferenciasGrilla=" { }";
    		String formatos="";
    		String filtroCombo="{ }";
    		String accesos="{ }";   
    		String TipoOperacionInstrumento = "{ }";
    		String configuracion = "{ }";
    		if (opcion.equals("HOME")) {
        		feriados = getFeriados(conn, false);
        		preferencias=getPreferencias(conn,parm.get("IdEmpresa"),parm.get("IdUsuario"),false);
        		preferenciasGrilla=getPreferenciasGrilla(conn,parm.get("IdEmpresa"),false);
        		formatos=getFormatos(conn);
        		filtroCombo=getFiltroComboBase(conn,true);
        		accesos=getAccesos(conn,parm.get("IdEmpresa"),parm.get("IdUsuario"),false);
        		TipoOperacionInstrumento=getTipoOperacionInstrumento(conn);
        		configuracion = getConfiguracionAsJson(parametrosConfiguracion);
    		}
    		
    		//Configuracion RUM Datadog
    		try {
    			rumdatadog = Utils.getWebStaticFile(conn,parm.get("ambiente"),parm.get("RutaArchivosWeb"),"BSCL.Plataforma.UI.resources","config_datadog.js");
    			rumdatadog = "<script>\n" + rumdatadog + "\n</script>\n";;
    		} catch(Exception ex) {
    			rumdatadog = "";
    		}
    		
    		libbase = StringUtils.replace(libbase, "<::DATADOG-RUM-CONFIG::>", rumdatadog);
    		
    		//Configuraciones dinamicas
    		if (parm.get("debug").equals("true")){
    			libbase = libbase.replace(".min.",".");      	
    			libbasecss = libbasecss.replace(".min.",".");
    		}			
    		/*config = config.replace("::LOCALE::",parm.get("local"));
    		config = config.replace("::OFFSET::",parm.get("offset"));
    		config = config.replace("::SESION::",parm.get("sesion"));
    		config = config.replace("::FORMATOS::",formatos);
    		config = config.replace("::FERIADOS::",feriados);
    		config = config.replace("::MENU::",menu);
    		config = config.replace("::VENTANAS::",ventana);
    		config = config.replace("::MENU_USUARIO::",parm.get("CodigoMenuUsuario"));
    		config = config.replace("::FILTROCOMBOBASE::",filtroCombo);
    		config = config.replace("::ACCESOS::", accesos);
    		config = config.replace("::PREFERENCIAS::", preferencias);
    		config = config.replace("::PREFERENCIAS_GRILLA::", preferenciasGrilla);
    		config = config.replace("::VERSION::", parm.get("version"));
    		config = config.replace("::AMBIENTE::",parm.get("ambiente"));
    		config = config.replace("::TIMESTAMP_LOCAL::",parm.get("fechasistema"));
    		config = config.replace("'::OPERACION_INSTRUMENTO::'",TipoOperacionInstrumento);
    		config = config.replace("'::CONFIGURACION::'",configuracion);
    		config = config.replace("::DICCIONARIO::",diccionario);*/
    		
    		config = "<script>\n";
    		config += "const CONF_LOCALE 				= '" + parm.get("local") + "';\n";
    		config += "const CONF_IMAGEN_FONDO 			= ['santiago-bg.jpg','santiago2-bg.jpg'];\n";
    		config += "const BCS_DICCIONARIO			= " + diccionario + ";\n";
    		config += "const BCS_SESION					= " + parm.get("sesion") + ";\n";
    		config += "const BCS_OFFSET					= " + parm.get("offset") + ";\n";
    		config += "const BCS_VERSION				= " + parm.get("version") + ";\n";
    		config += "const BCS_ACCESOS				= " + accesos + ";\n";
    		config += "const BCS_PREFERENCIAS			= " + preferencias + ";\n";
    		config += "const BCS_PREFERENCIAS_GRILLA	= " + preferenciasGrilla + ";\n";
    		config += "const BCS_AMBIENTE				= '" + parm.get("ambiente") + "';\n";
    		config += "const BCS_TIMESTAMP_LOCAL		= '" + parm.get("fechasistema") + "';\n";
    		config += "const BCS_MUESTRA_SSO_GOOGLE		= '" + parm.get("MuestraSSOGoogle") + "';\n";
    		config += "const bcs_feriados				= " + feriados + ";\n";
    		config += "const BCS_OPERACION_INSTRUMENTO	= " + TipoOperacionInstrumento + ";\n";
    		config += "const BCS_ESTADO_MODULO			= {};\n";
    		config += "const BCS_CONFIGURACION			= " + configuracion + ";\n";
    		config += "const BCS_OFFLINE				= true;\n";
    		config += formatos + "\n";
    		config += ventana + "\n";
    		config += menu + "\n";
    		config += filtroCombo + "\n";
    		config += "var BCS_USER_MENU			= " + parm.get("CodigoMenuUsuario") + ";\n";
    		config += "</script>\n";
    		
    		//Replace TAGs
    		/*
    		pagina = pagina.replace("<::CONFIG::>",config);
    		pagina = pagina.replace("<::LIB_BASE::>",libbase);
    		pagina = pagina.replace("<::LIB_BASE_CSS::>",libbasecss);
    		pagina = pagina.replace("<::LIB_THEME_CSS::>",libthemecss);
    		pagina = pagina.replace("<::LOCALIZACION::>","<script src='resources/region_"+parm.get("local")+".js'></script>");
    		pagina = pagina.replace("<::MODULOS::>",modulos);
    		pagina = pagina.replace("<::EMPRESAS::>>", empresas);
    		*/
    		
    		pagina = StringUtils.replace(pagina, "<::CONFIG::>", config);
    		pagina = StringUtils.replace(pagina, "<::LIB_BASE::>", libbase);
    		pagina = StringUtils.replace(pagina, "<::LIB_BASE_CSS::>", libbasecss);
    		pagina = StringUtils.replace(pagina, "<::LIB_THEME_CSS::>", libthemecss);
    		pagina = StringUtils.replace(pagina, "<::LIB_EXTRA_CSS::>", libextracss);
    		pagina = StringUtils.replace(pagina, "<::LOCALIZACION::>", "<script src='resources/region_"+parm.get("local")+".js'></script>");
    		pagina = StringUtils.replace(pagina, "<::MODULOS::>", modulos);
    		pagina = StringUtils.replace(pagina, "<::EMPRESAS::>", empresas);
    		pagina = StringUtils.replace(pagina, "<::LOGO_CORREDOR::>", logoCorredor);

    		
    	}catch(Exception er) {
    		er.printStackTrace();
    		throw new IOException("INTERNAL ERROR",er);
    	}
    	if (pagina==null) throw new IOException("NOT FOUND PAGE");
    	return pagina;
    }
    
    
 
     
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();						
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();			
		}
	}
	
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HashMap<String,String>  parms=new HashMap<String,String>();
		parms.put("local", "CL");		
		parms.put("debug", "false");
		parms.put("modob1", "false");
		parms.put("version", "0");
		parms.put("ambiente", "PRO");
		parms.put("fechasistema", "");	
		parms.put("sesion","{ }");			
		parms.put("IdEmpresa", "-1");	
		parms.put("IdUsuario", "-1");	
		parms.put("RutaArchivosWeb", "");	
		parms.put("CodigoMenuUsuario", "BCS_FULLMENU");  
		response.setContentType("text/html; charset=UTF-8");
	    response.setCharacterEncoding("UTF-8");
	   try (Connection conn = dataSource.getConnection()) {
	    	
	    
		    try {
		    	String pdebug=request.getParameter("debug");
				String psinauth=request.getParameter("p"); 		
				String ptoken= request.getParameter("X-SESSION-TOKEN");	
				String pmodob1= request.getParameter("sapb1");
				if (pdebug!=null && (pdebug.equalsIgnoreCase("true")||pdebug.equalsIgnoreCase("1"))) parms.put("debug", "true");			
				if (pmodob1 != null && pmodob1.equalsIgnoreCase("true")) parms.put("modob1", "true");
				
				//default parameters IdEmpresa = 1
				HashMap<String, HashMap<String, String>> parametrosConfiguracion = Utils.getConfiguraciones(conn,1);
				HashMap<String,String> configPla = parametrosConfiguracion.get("PLATAFORMA");
				parms.put("ambiente", configPla.get("PLA.Ambiente"));
				parms.put("menu", configPla.get("PLA.Menu"));
				parms.put("offset", configPla.get("PLA.SegundosZonaHoraria"));
				parms.put("RutaArchivosWeb", configPla.get("PLA.RutaArchivosWeb"));		
					
				//page login			
				if ("login".equals(psinauth)) {
					parms.put("MuestraSSOGoogle", configPla.get("PLA.MuestraSSOGoogle"));		
					response.setStatus(200);
				    response.getOutputStream().write(parsePagina("LOGIN",parms,parametrosConfiguracion).getBytes("UTF-8"));
					response.getOutputStream().flush();
					conn.commit();
					logger.info("Load Login Page");
					return;
				}
				//token			
				if (StringUtils.isBlank(ptoken)) {
					response.setStatus(401);
					response.getOutputStream().write(parsePagina("401",parms,parametrosConfiguracion).getBytes("UTF-8"));
					response.getOutputStream().flush();
					conn.commit(); 
					return;
				}
				//Get Session Data
				SesionData oSession = Sesion.getSesionData(conn, ptoken);
				if (oSession.P_OUT_ERRORESCONTADOR > 0L) {
					conn.rollback();
					response.setStatus(401);
					response.getOutputStream().write(parsePagina("LOGIN",parms,parametrosConfiguracion).getBytes("UTF-8"));
					response.getOutputStream().flush();
					logger.info("Load Login Page for user "+oSession.NombreUsuario+" SesionIdUsuario:"+oSession.IdUsuario);
					return;
				}
				else {
					String CodigoMenuUsuario=oSession.CodigoMenuUsuario.toUpperCase();
					parms.put("local", configPla.get("PLA.LocalizacionSistema"));
					parms.put("offset", configPla.get("PLA.SegundosZonaHoraria"));
					parms.put("version", configPla.get("PLA.Version"));
					parms.put("ambiente", configPla.get("PLA.Ambiente"));
					parms.put("RutaArchivosWeb", configPla.get("PLA.RutaArchivosWeb"));
					parms.put("fechasistema", Utils.getFechaHoraSistemaLocal(conn,"",oSession.IdEmpresa));	
					parms.put("sesion", oSession.toString());	
					parms.put("IdEmpresa", oSession.IdEmpresa+"");	
					parms.put("IdUsuario", oSession.IdUsuario+"");	
					if (CodigoMenuUsuario.length()>0) {
						parms.put("CodigoMenuUsuario", CodigoMenuUsuario);
					}
										
					response.setStatus(200);
				    response.getOutputStream().write(parsePagina("HOME",parms,parametrosConfiguracion).getBytes("UTF-8"));
					response.getOutputStream().flush();			
					logger.info("Load Home Page for user "+oSession.NombreUsuario+" SesionIdUsuario:"+oSession.IdUsuario);
					conn.commit();
					return;
							
				}
				
			} catch (Exception internalEx) {
				logger.error("Excepción crítica ", internalEx);
				conn.rollback();
				throw internalEx; 
			}
	   }
	   catch(Exception er) {
		   logger.error("Internal server error", er);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getOutputStream().print("500 - INTERNAL SERVER ERROR");
			response.getOutputStream().flush();
	   }
	   
	}
	
}
