package cl.bcs.normativa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class NotificarRespuestaCircularizacion implements JavaCall {
	private final String SQL_SELECT_NOMINA = loadResourceToString("cl/bcs/normativa/sql/NotificarRespuestaCircularizacion_ConsultarNomina.sql");
	
	public static class SalidaCorreoCircularizacion extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long    P_OUT_ADVERTENCIASCONTADOR = 0L;
    }

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCirc circIn = gson.fromJson(jin.toString(), EntradaCirc.class);
		SalidaCorreoCircularizacion out = logica(conn, circIn);
	    JsonParser parser = new JsonParser();
	    JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
	    return dataout;

    }
	public static class EntradaCirc{
		Long IdUsuario;
		Long IdEmpresa;
		Long IdCliente;
		String Motivo;
		Long IdNomina;
		Long IdPortafolio;
		String Respuesta;
		
		@Override
        public String toString() {
            return Utils.printJson(this, false);
        }
	}
	public static class Nomina {
        public Long IdNomina;
        public Long IdPersona;
        public Long IdPortafolio;
        public Long IdDireccionElectronica;
        public String Corredora;
        public String AtencionCliente;
        public String NombreCompleto;
        public String DocumentoIdentidad;
        public String TipoDocumentoIdentidad;
        public Long IdDireccionElectronicaPrincipal;
        public String Agente;
        public String AccesoCliente = "N";
        public Long IdCliente;
        public String CodigoPortafolio;
		public String NombreAgente;
		public Long IdDireccionElectronicaPrincipalAgente;
		public Long IdDireccionElectronicaAgente;
		public Long IdAgente;
        
        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
	
	public static class Agente{
		public Long IdNomina;
		public String Nombre;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdDireccionElectronica;
		
		@Override
        public String toString() {
            return Utils.printJson(this, false);
        }
	}
	
	public static class Imagen extends BcsPojo {
		public byte[] Contenido;

		public Imagen() {
			Contenido = new byte[0];
		}
	}
	
	public SalidaCorreoCircularizacion logica(Connection con, EntradaCirc in) {
		SalidaCorreoCircularizacion Salida = new SalidaCorreoCircularizacion();
		try {
			SQL _sql = new SQL();
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String contenido = ConfiguracionSistema.getParametroConfiguracionString(con, in.IdEmpresa, "PLATAFORMA", "PLA.UrlLogoCorredoraPublic");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdNomina", in.IdNomina);
			params.put("IdEmpresa", in.IdEmpresa);
			params.put("IdCliente", in.IdCliente);
			
			List<Nomina> nom = _sql.query(con, SQL_SELECT_NOMINA,params, Nomina.class);
			AgregarEvento.ServiceEntrada eventoIn = new AgregarEvento.ServiceEntrada();
			
			eventoIn.P_IDEMPRESA = in.IdEmpresa;
			eventoIn.P_IDUSUARIO = in.IdUsuario;
			eventoIn.P_IDMODULO  = "NOR";
			eventoIn.P_EVENTOS             = new ArrayList<>();
	        eventoIn.P_VALORES             = new ArrayList<>();
	        eventoIn.P_ARCHIVOSADJUNTOS    = new ArrayList<>();
	        eventoIn.P_IDDIRECCIONES     = new ArrayList<>();
			for(Nomina n:nom) {
				AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
		        evt.CodigoTipoEvento   = "APD".equalsIgnoreCase(in.Respuesta) ? "RESPUESTA_CIRCULARIZACION_APD" : "RESPUESTA_CIRCULARIZACION_RZD";
		        evt.IdSecuencia        = n.IdNomina;
		        evt.FechaCreacionSistema  = fechaSistema;
		        evt.IdPersonaRemitente    = 0L;
		        evt.IdPersonaDestinatario = n.IdAgente;
		        evt.IdPortafolio          = 0L;
		        eventoIn.P_EVENTOS.add(evt);
		        
		        AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
		        ndir.IdSecuencia         = n.IdNomina;
		        ndir.TipoDestinatario     = "TO";
		        ndir.IdDireccion         = n.IdDireccionElectronica != null ? n.IdDireccionElectronica : n.IdDireccionElectronicaPrincipal;
		        eventoIn.P_IDDIRECCIONES.add(ndir);
		        
		        eventoIn.addValor(n.IdNomina,"NOMBRECLIENTE", n.NombreCompleto +"")
		        .addValor(n.IdNomina, "DOCUMENTOIDENTIDAD", n.DocumentoIdentidad+"")
		        .addValor(n.IdNomina, "MOTIVO", in.Motivo+"")
		        .addValor(n.IdNomina, "CORREDORA", n.Corredora+"")
		        .addValor(n.IdNomina, "FECHA", fechaSistema+"")
		        .addValor(n.IdNomina, "CUENTA", n.CodigoPortafolio+"")
		        .addValor(n.IdNomina, "LOGO", contenido); 
		        
		        if ("RZD".equalsIgnoreCase(in.Respuesta)) {
		        	AgregarEvento.ObjEVENTOS evtAgt = new AgregarEvento.ObjEVENTOS();
		        	evtAgt.CodigoTipoEvento   = "RESPUESTA_CIRCULARIZACION_RZD_AGT";
		        	evtAgt.IdSecuencia        = n.IdNomina + n.IdCliente;
		        	evtAgt.FechaCreacionSistema  = fechaSistema;
		        	evtAgt.IdPersonaRemitente    = 0L;
		        	evtAgt.IdPersonaDestinatario = n.IdPersona;
		        	evtAgt.IdPortafolio          = 0L;
			        eventoIn.P_EVENTOS.add(evtAgt);
			        
			        AgregarEvento.ObjIDDIRECCIONES ndirAgt = new AgregarEvento.ObjIDDIRECCIONES();
			        ndirAgt.IdSecuencia         = n.IdNomina + n.IdCliente;
			        ndirAgt.TipoDestinatario     = "TO";
			        ndirAgt.IdDireccion         = n.IdDireccionElectronicaAgente != null ? n.IdDireccionElectronicaAgente : n.IdDireccionElectronicaPrincipalAgente;
			        eventoIn.P_IDDIRECCIONES.add(ndirAgt);
			        
			        eventoIn.addValor(n.IdNomina + n.IdCliente,"NOMBRECLIENTE", n.NombreCompleto +"")
    		        .addValor(n.IdNomina + n.IdCliente, "DOCUMENTOIDENTIDAD", n.DocumentoIdentidad+"")
    		        .addValor(n.IdNomina + n.IdCliente, "MOTIVO", in.Motivo+"")
    		        .addValor(n.IdNomina + n.IdCliente, "CORREDORA", n.Corredora+"")
    		        .addValor(n.IdNomina + n.IdCliente, "FECHA", fechaSistema+"")
    		        .addValor(n.IdNomina + n.IdCliente, "CUENTA", n.CodigoPortafolio+"")
    		        .addValor(n.IdNomina + n.IdCliente, "LOGO", contenido);
		        }

			}
	        AgregarEvento evento = new AgregarEvento();
            AgregarEvento.ServiceSalida eventoOut = evento.logica(con, eventoIn);
            System.out.print(eventoOut.toString());
		}catch(Exception e) {
			e.printStackTrace();
			Salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		return Salida;
		
	};

	
	
	public static void main(String args[]) throws SQLException {
        System.out.println("== TEST ==");
        try (Connection con = PlataformaUtils.getDESAConnection()) {
        	NotificarRespuestaCircularizacion demo = new NotificarRespuestaCircularizacion();
        	EntradaCirc in = new EntradaCirc();	
        	in.IdCliente = 123569L;
        	in.IdEmpresa = 1L;
        	in.IdUsuario = 0L;
        	in.Respuesta = "RZD";
        	in.Motivo = "Motivo";
        	in.IdPortafolio = 1451985L;
        	in.IdNomina = 4775L;
        	
            try {
                demo.logica(con, in);
                con.rollback();
            } catch(Exception e) {
                e.printStackTrace();
                con.rollback();
            } finally {
                con.close();
            }
        }
        System.out.println("== END ==");
    }

}
