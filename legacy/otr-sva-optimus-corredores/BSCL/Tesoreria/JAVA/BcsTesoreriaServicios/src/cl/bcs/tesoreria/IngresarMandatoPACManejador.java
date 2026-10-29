package cl.bcs.tesoreria;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Connection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

import cl.bcs.tesoreria.IngresarMandatoPAC.InIngresoPAC;
import cl.bcs.tesoreria.IngresarMandatoPAC.EntradaIngresoMandatoPAC;
import cl.bcs.tesoreria.IngresarMandatoPAC.SalidaCrearIngresoPAC;

public class IngresarMandatoPACManejador implements JavaCall{
	private final Log log = new Log("TES.InsertarPACManejador");
	
	public static class EntradaPAC{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InIngresoPAC> p_IngresoPAC;
		
		public EntradaPAC() {
			this.p_IngresoPAC = new ArrayList<InIngresoPAC>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearIngresoPACManejador{
		public Long	P_OUT_ERRORESCONTADOR;
		public List<EntidadMandato>	MANDATO;
	
		public SalidaCrearIngresoPACManejador(){
			MANDATO = new ArrayList<EntidadMandato>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
		
	public static class EntidadMandato{
		public Long	IdFolioPAC;
		public List<MensajesErrores> Mensajes;
        
		public EntidadMandato(){
			Mensajes = new ArrayList<MensajesErrores>();
		}
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class MensajesErrores{
		public Long	IdInterno;
		public String	Tipo;
		public String	Codigo;
		public String	Descripcion;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaPAC jin = gson.fromJson(in.toString(), EntradaPAC.class);
		SalidaCrearIngresoPACManejador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearIngresoPACManejador logica (Connection con, EntradaPAC entrada) throws Exception{
		log.write("INICIO IngresarMandatoPACManejador");
		SalidaCrearIngresoPACManejador out = new SalidaCrearIngresoPACManejador();
		List<SalidaCrearIngresoPAC> salidas = new ArrayList<SalidaCrearIngresoPAC>();
		List<String> folios = new ArrayList<String>();
		try{//CUENTA_BANCARIA_NO_ENCONTRADA
			for (InIngresoPAC mov : entrada.p_IngresoPAC) {
				SalidaCrearIngresoPAC salidaIngresoMandatoPAC =  new SalidaCrearIngresoPAC();
				if(mov.IdCuentaBancaria == null){
					salidaIngresoMandatoPAC.P_OUT_ERRORESCONTADOR++;
					salidaIngresoMandatoPAC.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CUENTA_BANCARIA_NO_ENCONTRADA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					folios.add(mov.FolioCSC);
					salidas.add(salidaIngresoMandatoPAC);
				}else if(mov.TipoPago == null){
					salidaIngresoMandatoPAC.P_OUT_ERRORESCONTADOR++;
					salidaIngresoMandatoPAC.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"TIPO_PAGO_NO_ENCONTRADO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					folios.add(mov.FolioCSC);
					salidas.add(salidaIngresoMandatoPAC);		
				}else if(mov.IdCliente == null){
					salidaIngresoMandatoPAC.P_OUT_ERRORESCONTADOR++;
					salidaIngresoMandatoPAC.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CLIENTE_NO_ENCONTRADO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					folios.add(mov.FolioCSC);
					salidas.add(salidaIngresoMandatoPAC);
				}else if(mov.IdPortafolio == null){
					salidaIngresoMandatoPAC.P_OUT_ERRORESCONTADOR++;
					salidaIngresoMandatoPAC.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_NO_ENCONTRADO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					folios.add(mov.FolioCSC);
					salidas.add(salidaIngresoMandatoPAC);
				}else {
					EntradaIngresoMandatoPAC entradaIngresoMandatoPAC = new EntradaIngresoMandatoPAC();
					InIngresoPAC inMandato = new InIngresoPAC();
				
					inMandato.IdCliente = mov.IdCliente;
					inMandato.IdPortafolio = mov.IdPortafolio;
					inMandato.FolioCSC = mov.FolioCSC;
					inMandato.CuentaCorriente = mov.CuentaCorriente;
					inMandato.FolioSAAPV = mov.FolioSAAPV;
					inMandato.FolioPACMandatoAgrupador = mov.FolioPACMandatoAgrupador;
					inMandato.Receptor = mov.Receptor;
					inMandato.Fecha = mov.Fecha;
					inMandato.NumeroMandato = mov.NumeroMandato;
					inMandato.Firma = mov.Firma;
					inMandato.IdentServicio = mov.IdentServicio;
					inMandato.Estado = mov.Estado;
					inMandato.EstadoCargo = mov.EstadoCargo;
					inMandato.IdEmpresa = mov.IdEmpresa;
					inMandato.IdCuentaBancaria = mov.IdCuentaBancaria;
					inMandato.Canal = mov.Canal;
					inMandato.TipoPago = mov.TipoPago;
					inMandato.DocumentoIdentidad = mov.DocumentoIdentidad;
						
					entradaIngresoMandatoPAC.p_IngresoPAC.add(inMandato);
				
					entradaIngresoMandatoPAC.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaIngresoMandatoPAC.p_IdModulo = entrada.p_IdModulo;
					entradaIngresoMandatoPAC.p_IdUsuario = entrada.p_IdUsuario;
					entradaIngresoMandatoPAC.p_SimularOperacion = entrada.p_SimularOperacion;
					entradaIngresoMandatoPAC.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				
					IngresarMandatoPAC ingresarMandatoPAC = new IngresarMandatoPAC();
					salidaIngresoMandatoPAC = ingresarMandatoPAC.logica(con, entradaIngresoMandatoPAC);
					salidas.add(salidaIngresoMandatoPAC);
					folios.add(mov.FolioCSC);
				}		
			}
			
			int i = 0;
			
			for(SalidaCrearIngresoPAC salida : salidas){
			 EntidadMandato entidadMandato = new EntidadMandato();
				List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
				
				for (MensajeSalida mensajeSalida : salida.P_OUT_MENSAJES){
					MensajesErrores mensajesErrores = new MensajesErrores();
					mensajesErrores.IdInterno = mensajeSalida.id;
					mensajesErrores.Tipo = mensajeSalida.tipo;
					mensajesErrores.Codigo = mensajeSalida.codigo;
					mensajesErrores.Descripcion = mensajeSalida.descripcion;
					listMensajesErrores.add(mensajesErrores);
					entidadMandato.Mensajes = listMensajesErrores;
				}
				
				entidadMandato.IdFolioPAC = salida.P_OUT_RESULTADO.size() > 0 ? salida.P_OUT_RESULTADO.get(0).IdMandatoPAC : 0;
				
				out.MANDATO.add(entidadMandato);
				
				if(salida.P_OUT_ERRORESCONTADOR > 0)
					out.P_OUT_ERRORESCONTADOR++;
				
				i++;
			}
			
			return out;
			
		}catch(Exception error){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(error);
			throw error;
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"TES\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IngresoPAC\":[{\"IdCliente\":249194,\"IdPortafolio\":\"1\",\"FolioCSC\":1503,\"FolioPAC\":1503,\"CuentaCorriente\":\"123123\",\"FolioSAAPV\":0,\"Fecha\":\"2018-11-11\",\"NumeroMandato\":12,\"Firma\":\"Si\",\"IdentServicio\":1,\"IdEmpresa\":1,\"IdCuentaBancaria\":255,\"Canal\":\"Optimus\",\"DocumentoIdentidad\":\"15443645-6\",\"TipoPago\":\"S\"}]}";	
			EntradaPAC  Objin = gson.fromJson(json, EntradaPAC.class);
			IngresarMandatoPACManejador demo = new IngresarMandatoPACManejador();
			SalidaCrearIngresoPACManejador  ObjOut = demo.logica(con, Objin);
			try {
				demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut);
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}			
		}
		System.out.println("== END ==");
	}
}
