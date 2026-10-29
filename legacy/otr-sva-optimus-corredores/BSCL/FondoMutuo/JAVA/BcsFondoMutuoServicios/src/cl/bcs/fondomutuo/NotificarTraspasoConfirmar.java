package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES;
import cl.bcs.plataforma.evt.AgregarEvento.ServiceEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarTraspasoConfirmar implements JavaCall{
	
	private final Log log = new Log("FM.NotificarTraspasoConfirmar");
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_TRASPASO = loadResourceToString("cl/bcs/fondomutuo/sql/NotificarTraspasoConfirmar_SelectTraspaso.sql");
	
	public static class EntradaNotificarTraspasoConfirmar{
		
		public Long p_IdEmpresa;		
		public Long p_IdUsuario;		
		public String p_IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudNotificar{
		
		
		public Long IdPersona;
		public Long IdDireccionElectronicaPrincipal;
		public String NombreCompleto;
		public String AtencionCliente;
		public String Corredora;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaNotificarTraspasoConfirmar{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaNotificarTraspasoConfirmar(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaNotificarTraspasoConfirmar jin = gson.fromJson(in.toString(), EntradaNotificarTraspasoConfirmar.class);
		SalidaNotificarTraspasoConfirmar out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaNotificarTraspasoConfirmar logica(Connection con, EntradaNotificarTraspasoConfirmar entrada) throws Exception{
		log.write("INICIO NotificarTraspasoConfirmar");
		SQL sql=new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		 
		String configTraspasoNotificar="";
		long parDiasTranscurridos=0L;
		
		Long idEmpresa = entrada.p_IdEmpresa;
		
		
	   try{configTraspasoNotificar = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "TRASPASO_PAUTA_NOTIFICAR");
       } catch (Exception e) {if (configTraspasoNotificar==""){configTraspasoNotificar=_c.NO;};};
   
       try{parDiasTranscurridos = Long.parseLong(ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "TRASPASO_DIAS_NOTIFICAR"));
       } catch (Exception e) {if (parDiasTranscurridos==0L){parDiasTranscurridos=0L;};};
   
		
		
   	log.write( "*NOTIFICARTRASPASOFFMM* Inicio de seleccion de traspasos");
   	SalidaNotificarTraspasoConfirmar out = new SalidaNotificarTraspasoConfirmar();
	if(_c.SI.equals(configTraspasoNotificar))
	{
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		params.put("DiasTranscurridos", parDiasTranscurridos);
		
		List<SolicitudNotificar> traspasos = sql.query(con, SQL_SELECT_TRASPASO, params, SolicitudNotificar.class);
     
		String logoCorredorRutaPublica = "";
		ConsultarImagen consultarImagen = new ConsultarImagen();
		ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
		consultarImagenIn.p_IdEmpresa = idEmpresa;
		consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR");
		ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(con, consultarImagenIn);
		if (consultarImagenOut.OUT_IMAGENES.size() > 0 ) {
			logoCorredorRutaPublica = consultarImagenOut.OUT_IMAGENES.get(0).RutaPublica;
		}

	   	log.write( "*NOTIFICARTRASPASOFFMM* Inicio de entrada para Notificacion");
					ServiceEntrada eventoEntrada = new ServiceEntrada();
					eventoEntrada.P_IDEMPRESA = entrada.p_IdEmpresa;
					eventoEntrada.P_IDMODULO = entrada.p_IdModulo;
					eventoEntrada.P_IDUSUARIO = entrada.p_IdUsuario;
					
					for(SolicitudNotificar s : traspasos){
						ObjEVENTOS evento = new ObjEVENTOS();
						evento.IdSecuencia = s.IdPersona;
						evento.CodigoTipoEvento = "NOTIFICAR_TRASPASO_FFMM_EMPLEADO";
						evento.FechaCreacionSistema = fechaSistema;
						evento.IdPersonaRemitente = 0L;
						evento.IdPersonaDestinatario =s.IdPersona;
						evento.IdPortafolio =  0L;
						eventoEntrada.P_EVENTOS.add(evento);
						
						ObjIDDIRECCIONES dir = new ObjIDDIRECCIONES();
						dir.IdDireccion = s.IdDireccionElectronicaPrincipal;
						dir.IdSecuencia = s.IdPersona;
						dir.TipoDestinatario = "TO";
						eventoEntrada.P_IDDIRECCIONES.add(dir);
						
						//DETALLE
						eventoEntrada.addValor(s.IdPersona,"NombreCompleto",s.NombreCompleto)
									 .addValor(s.IdPersona,"CORREDORA",s.Corredora)
									 .addValor(s.IdPersona,"ATENCIONCLIENTE",s.AtencionCliente)
									 .addValor(s.IdPersona, "Fecha", fechaSistema)
									 .addValor(s.IdPersona, "LogoCorredor", logoCorredorRutaPublica);					
						
						
					}
					
					if (eventoEntrada.P_EVENTOS.size() > 0) {
					    AgregarEvento notif = new AgregarEvento();
					    notif.logica(con, eventoEntrada);
					}
	}
					
		log.write("SALIDA NotificarTraspasoConfirmar");
		return out;
	}
	
    public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getDESAConnection();
    	NotificarTraspasoConfirmar p = new NotificarTraspasoConfirmar();
        try {
        	EntradaNotificarTraspasoConfirmar in = new EntradaNotificarTraspasoConfirmar();
			in.p_IdEmpresa = 1L;
			in.p_IdModulo = "NOR";
			in.p_IdUsuario = 6L;
			
            p.logica(con, in);
            con.commit();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

	
}
