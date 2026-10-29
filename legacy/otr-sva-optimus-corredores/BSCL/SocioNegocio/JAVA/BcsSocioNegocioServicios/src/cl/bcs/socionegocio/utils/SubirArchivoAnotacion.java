package cl.bcs.socionegocio.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.SubirArchivoAnotacionCliente;
import cl.bcs.socionegocio.cli.SubirArchivoAnotacionCliente.EntradaSubirArchivoAnotacionCliente;
import cl.bcs.socionegocio.cli.SubirArchivoAnotacionCliente.SalidaSubirArchivoAnotacionCliente;
import cl.bcs.socionegocio.por.SubirArchivoAnotacionPortafolio;
import cl.bcs.socionegocio.por.SubirArchivoAnotacionPortafolio.EntradaSubirArchivoAnotacionPortafolio;
import cl.bcs.socionegocio.por.SubirArchivoAnotacionPortafolio.SalidaSubirArchivoAnotacionPortafolio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;

public class SubirArchivoAnotacion implements JavaCall{

	
	public static class EntradaSubirArchivoAnotacion extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<ArchivoAnotacion> p_Anotaciones;
		
		public EntradaSubirArchivoAnotacion () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "SOC";
			this.p_UsaReglasNegocio = "N";
			this.p_Anotaciones = new ArrayList<ArchivoAnotacion>();
		}
	}

	public static class ArchivoAnotacion extends BcsPojo{
		public Long IdAnotacion;
		public String NombreArchivo;
		public String TipoArchivo;
		public String Entidad;
		public String ArchivoBase64;
		public byte[] ArchivoBlob;
		public String Existe;
		
		public ArchivoAnotacion(){
			this.IdAnotacion = 0L;
			this.NombreArchivo = "";
			this.TipoArchivo = "";
			this.Entidad = "";
			this.ArchivoBase64 = "";
			this.ArchivoBlob = new byte[0];
			this.Existe = "N";
		}
	}
	
	public static class SalidaSubirArchivoAnotacion extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaSubirArchivoAnotacion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
	}
	
	public SalidaSubirArchivoAnotacion logica (Connection con , EntradaSubirArchivoAnotacion entrada) throws Exception {
		
		SalidaSubirArchivoAnotacion salida = new SalidaSubirArchivoAnotacion();
		
		if(entrada.p_Anotaciones.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ENTRADA_SIN_ARCHIVOS_ANOTACIONES", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}

		List<ArchivoAnotacion> anotacionesCli = new ArrayList<ArchivoAnotacion>();
		if(entrada.p_Anotaciones.stream().anyMatch(anot -> "ANOTACION_CLIENTE".equals(anot.Entidad))) {
			anotacionesCli.addAll(entrada.p_Anotaciones.stream().filter(anot -> "ANOTACION_CLIENTE".equals(anot.Entidad)).collect(Collectors.toList()));
			SubirArchivoAnotacionCliente subirArchivoAnotacionCli = new SubirArchivoAnotacionCliente();
			EntradaSubirArchivoAnotacionCliente entradaSubirArchivoAnotacionCliente = new EntradaSubirArchivoAnotacionCliente();
			entradaSubirArchivoAnotacionCliente.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaSubirArchivoAnotacionCliente.p_IdModulo = entrada.p_IdModulo;
			entradaSubirArchivoAnotacionCliente.p_IdUsuario = entrada.p_IdUsuario;
			entradaSubirArchivoAnotacionCliente.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaSubirArchivoAnotacionCliente.p_Anotaciones = anotacionesCli;
			SalidaSubirArchivoAnotacionCliente salidaCliente = subirArchivoAnotacionCli.logica(con, entradaSubirArchivoAnotacionCliente);
			
			salida.P_OUT_MENSAJES.addAll(salidaCliente.P_OUT_MENSAJES);
		}
		
		List<ArchivoAnotacion> anotacionesPort = new ArrayList<ArchivoAnotacion>();
		if(entrada.p_Anotaciones.stream().anyMatch(anot -> "ANOTACION_PORTAFOLIO".equals(anot.Entidad))) {
			anotacionesPort.addAll(entrada.p_Anotaciones.stream().filter(anot -> "ANOTACION_PORTAFOLIO".equals(anot.Entidad)).collect(Collectors.toList()));
			SubirArchivoAnotacionPortafolio subirArchivoAnotacionPort = new SubirArchivoAnotacionPortafolio();
			EntradaSubirArchivoAnotacionPortafolio entradaSubirArchivoAnotacionPortafolio = new EntradaSubirArchivoAnotacionPortafolio();
			entradaSubirArchivoAnotacionPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaSubirArchivoAnotacionPortafolio.p_IdModulo = entrada.p_IdModulo;
			entradaSubirArchivoAnotacionPortafolio.p_IdUsuario = entrada.p_IdUsuario;
			entradaSubirArchivoAnotacionPortafolio.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaSubirArchivoAnotacionPortafolio.p_Anotaciones = anotacionesPort;
			SalidaSubirArchivoAnotacionPortafolio salidaPortafolio = subirArchivoAnotacionPort.logica(con, entradaSubirArchivoAnotacionPortafolio);
			
			salida.P_OUT_MENSAJES.addAll(salidaPortafolio.P_OUT_MENSAJES);
		}
		
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		return salida;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaSubirArchivoAnotacion jin=gson.fromJson(in.toString() ,EntradaSubirArchivoAnotacion.class);
		SalidaSubirArchivoAnotacion out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

}
