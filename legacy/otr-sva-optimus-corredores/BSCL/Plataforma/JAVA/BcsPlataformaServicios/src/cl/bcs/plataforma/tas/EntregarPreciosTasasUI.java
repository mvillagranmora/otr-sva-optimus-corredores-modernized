package cl.bcs.plataforma.tas;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class EntregarPreciosTasasUI implements JavaCall{
	
	private final Log log = new Log("PLA.EntregarPreciosTasasUI");
	
	public static class EntregarPreciosTasasUI_Entrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdPais;
		public List<EntregarPreciosTasasUI_Entrada_DatosInstrumentos> p_DatosInstrumentos;
		
		public EntregarPreciosTasasUI_Entrada() {
			p_DatosInstrumentos = new ArrayList<EntregarPreciosTasasUI_Entrada_DatosInstrumentos>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntregarPreciosTasasUI_Entrada_DatosInstrumentos {
		public Long IdRegistro;
		public Long IdTrabajo;
		public String TipoCartera;
		public String IdUso;
		public String IdTipoCriterioTasa;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMoneda;
		public String FechaConsulta;
		public Long PlazoResidual;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public EntregarPreciosTasas_Salida logica(Connection con, EntregarPreciosTasasUI_Entrada entrada) throws Exception {
		log.write("INICIO:: EntregarPreciosTasasUI");
		
		ConsultarEmpresa e = new ConsultarEmpresa();
		InformacionEmpresa empresa = e.logica(con, entrada.p_IdEmpresa);
		
		EntregarPreciosTasas_Salida out = new EntregarPreciosTasas_Salida();
		EntregarPreciosTasas servicio = new EntregarPreciosTasas();
		EntregarPreciosTasas_Entrada entradaServicio = new EntregarPreciosTasas_Entrada();
		entradaServicio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaServicio.p_IdUsuario = entrada.p_IdUsuario;
		entradaServicio.p_IdModulo = entrada.p_IdModulo;
		entradaServicio.p_IdPais = empresa.IdPais;
		SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		for(EntregarPreciosTasasUI_Entrada_DatosInstrumentos instrumento : entrada.p_DatosInstrumentos){
			EntregarPreciosTasas_Entrada_DatosInstrumentos ins = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
			ins.IdRegistro = instrumento.IdRegistro;
			ins.TipoCartera = instrumento.TipoCartera;
			ins.IdUso = instrumento.IdUso;
			ins.IdTipoCriterioTasa = instrumento.IdTipoCriterioTasa;
			ins.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
			ins.IdTipoInstrumento = instrumento.IdTipoInstrumento;
			ins.IdInstrumento = instrumento.IdInstrumento;
			ins.Nemotecnico = instrumento.Nemotecnico;
			ins.IdMoneda = instrumento.IdMoneda;
			ins.FechaConsulta = new java.sql.Date(formatoFecha.parse(instrumento.FechaConsulta).getTime());
			ins.PlazoResidual = instrumento.PlazoResidual;
			entradaServicio.p_DatosInstrumentos.add(ins);
		}
		out = servicio.logica(con, entradaServicio);
		log.write("FIN:: EntregarPreciosTasasUI");
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntregarPreciosTasasUI_Entrada in=gson.fromJson(jin.toString() ,EntregarPreciosTasasUI_Entrada.class);
		EntregarPreciosTasas_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) {
		try(Connection conn = PlataformaUtils.getMBPEConnection()){
			Gson gson = Utils.getGsonBuilder();	
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdPais\":1,\"p_DatosInstrumentos\":[{\"IdRegistro\":0,\"IdTrabajo\":0,\"TipoCartera\":0,\"IdUso\":\"ORDEN\",\"IdTipoCriterioTasa\":0,\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"E1\",\"IdInstrumento\":181228,\"Nemotecnico\":\"BACKUSI1\",\"IdMoneda\":\"PEN\",\"FechaConsulta\":\"2019-02-26\",\"PlazoResidual\":0}]}";
			EntregarPreciosTasasUI_Entrada in=gson.fromJson(json.toString() ,EntregarPreciosTasasUI_Entrada.class);
			EntregarPreciosTasasUI servicio = new EntregarPreciosTasasUI();
			EntregarPreciosTasas_Salida out= servicio.logica(conn,in);
			System.out.println(out);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
