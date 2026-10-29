package cl.bcs.plataforma.evt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;

/**
 * ORACLE FAKE of cl.bcs.plataforma.evt.AgregarEvento.
 */
public class AgregarEvento implements JavaCall {

	// legacy cl/bcs/plataforma/evt/AgregarEvento.java:51-97 (verbatim)
	public static class ServiceEntrada {	
		public Long P_IDEMPRESA;
		public Long P_IDUSUARIO;
		public String		 P_IDMODULO;
		public List<ObjEVENTOS>  P_EVENTOS;
		public List<ObjVALORES>  P_VALORES;
		public List<ObjARCHIVOSADJUNTOS>  P_ARCHIVOSADJUNTOS;
		public List<ObjIDDIRECCIONES>  P_IDDIRECCIONES;
		public List<String> P_DESCRIPCIONORIGEN = new ArrayList<>();
		public List<String> P_IDTIPOEVENTOORIGEN = new ArrayList<>(); 
		public List<String> P_NEMOEVENTOORIGEN = new ArrayList<>(); 
		public List<Long> P_SECUENCIAORIGEN = new ArrayList<>(); 
		public List<String> P_TIPOEVENTOORIGEN = new ArrayList<>();
		public String P_CONTROLPROCESO = "";
	
		public ServiceEntrada() {
			P_ARCHIVOSADJUNTOS = new ArrayList<>();
			P_IDDIRECCIONES = new ArrayList<>();
			P_VALORES = new ArrayList<>();
			P_EVENTOS = new ArrayList<>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}	
		
		public ServiceEntrada addValor(Long IdSecuencia,String llave,String valor) {
			ObjVALORES obj=new ObjVALORES();
			obj.IdSecuencia	= IdSecuencia;
			obj.Llave		= llave;
			
			obj.Valor		= valor;    //Para valores pequennos (menores a 5000 caracteres, tipo de datos NVARCHAR(5000))			
			this.P_VALORES.add(obj);
			return this;
		}
		

		
		public ServiceEntrada addValorBlob(Long IdSecuencia,String llave,String valorBlob) {
			ObjVALORES obj=new ObjVALORES();
			obj.IdSecuencia	= IdSecuencia;
			obj.Llave		= llave;			
			obj.ValorBlob	= valorBlob; //Para valores grandes (mayores a 5000 caracteres, tipo de datos TEXT)
			this.P_VALORES.add(obj);
			return this;
		}
	}

	// legacy cl/bcs/plataforma/evt/AgregarEvento.java:99-109 (verbatim)
	public static class ServiceSalida {
		public List<ObjIDEVENTO>  P_OUT_IDEVENTO;

		public ServiceSalida() {
			P_OUT_IDEVENTO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}	

	// legacy cl/bcs/plataforma/evt/AgregarEvento.java:117-129 (verbatim)
	public static class ObjEVENTOS {
		public String	CodigoTipoEvento;
		public Long 	IdSecuencia;
		public String 	FechaCreacionSistema;
		public Long 	IdPersonaRemitente;
		public Long 	IdPersonaDestinatario;
		public Long 	IdPortafolio;
		public Long 	IdEvento = 0L;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy cl/bcs/plataforma/evt/AgregarEvento.java:131-144 (verbatim)
	public static class ObjVALORES {
		public Long 		 IdSecuencia;
		public String		 Llave;
		public Long 		 IdEvento;
		public Long 		 IdEventoDetalle;
		public String		 Valor;             //Para valores pequennos (menores a 5000 caracteres, tipo de datos NVARCHAR(5000))
		public String		 ValorBlob ;  //Para valores grandes (mayores a 5000 caracteres, tipo de datos TEXT)
		
		@Override
		public String toString() {
			return "ObjVALORES [IdSecuencia=" + IdSecuencia + ", Llave=" + Llave +", IdEvento="+ IdEvento +", IdEventoDetalle="+IdEventoDetalle
					+ ", Valor=" + Valor + ", ValorBlob=" + ValorBlob + "]";
		}
	}

	// legacy cl/bcs/plataforma/evt/AgregarEvento.java:146-152 (verbatim)
	public static class ObjARCHIVOSADJUNTOS {
		public Long IdSecuencia;
		public byte[]		 ArchivoAdjunto;
		public String		 Nombre;
		public String		 Contrasenia;
		public Long			IdEvento;
	}

	// legacy cl/bcs/plataforma/evt/AgregarEvento.java:154-163 (verbatim)
	public static class ObjIDDIRECCIONES {
		public Long IdSecuencia;
		public Long IdDireccion;
		public String		 TipoDestinatario;
		@Override
		public String toString() {
			return "ObjIDDIRECCIONES [IdSecuencia=" + IdSecuencia + ", IdDireccion=" + IdDireccion
					+ ", TipoDestinatario=" + TipoDestinatario + "]";
		}
	}

	// legacy cl/bcs/plataforma/evt/AgregarEvento.java:166-173 (verbatim)
	public static class ObjIDEVENTO {
		public String CodigoTipoEvento;
		public Long IdEvento;
		public Long IdSecuencia;
		public String toString() {
            return Utils.printJson(this, false);
        }
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }

	/**
	 * FAKE of AgregarEvento.logica (legacy creates notification events and mails): records the input; returns one ObjIDEVENTO per event
	 * with IdEvento = 9000 + index, or null when "colaboradores.agregarEventoSinId" is true (legacy: notification not sent).
	 */
	public ServiceSalida logica(Connection con, ServiceEntrada entrada) throws Exception {
		Hooks.registrar("AgregarEvento", entrada);
		ServiceSalida out = new ServiceSalida();
		boolean sinId = Hooks.bool("agregarEventoSinId", false);
		long n = 9000;
		for (ObjEVENTOS e : entrada.P_EVENTOS) {
			ObjIDEVENTO id = new ObjIDEVENTO();
			id.IdSecuencia = e.IdSecuencia;
			id.IdEvento = sinId ? null : Long.valueOf(n++);
			out.P_OUT_IDEVENTO.add(id);
		}
		return out;
	}

}
