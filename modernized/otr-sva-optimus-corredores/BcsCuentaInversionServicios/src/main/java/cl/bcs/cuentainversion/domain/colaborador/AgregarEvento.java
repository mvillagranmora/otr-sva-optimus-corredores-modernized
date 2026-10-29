package cl.bcs.cuentainversion.domain.colaborador;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.evt.AgregarEvento}: field names and defaults are the legacy Entrada/Salida POJOs,
 * so the modern services hand {@code Colaboradores.invocar} the same JSON the legacy call log recorded.
 */
public final class AgregarEvento {

    private AgregarEvento() {
    }

    public static final String CLASE = "cl.bcs.plataforma.evt.AgregarEvento";

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

	public static class ServiceSalida {
		public List<ObjIDEVENTO>  P_OUT_IDEVENTO;

		public ServiceSalida() {
			P_OUT_IDEVENTO = new ArrayList<>();
		}
		
}	

	public static class ObjEVENTOS {
		public String	CodigoTipoEvento;
		public Long 	IdSecuencia;
		public String 	FechaCreacionSistema;
		public Long 	IdPersonaRemitente;
		public Long 	IdPersonaDestinatario;
		public Long 	IdPortafolio;
		public Long 	IdEvento = 0L;

	}

	public static class ObjVALORES {
		public Long 		 IdSecuencia;
		public String		 Llave;
		public Long 		 IdEvento;
		public Long 		 IdEventoDetalle;
		public String		 Valor;             //Para valores pequennos (menores a 5000 caracteres, tipo de datos NVARCHAR(5000))
		public String		 ValorBlob ;  //Para valores grandes (mayores a 5000 caracteres, tipo de datos TEXT)
		
}

	public static class ObjARCHIVOSADJUNTOS {
		public Long IdSecuencia;
		public byte[]		 ArchivoAdjunto;
		public String		 Nombre;
		public String		 Contrasenia;
		public Long			IdEvento;
	}

	public static class ObjIDDIRECCIONES {
		public Long IdSecuencia;
		public Long IdDireccion;
		public String		 TipoDestinatario;
}

	public static class ObjIDEVENTO {
		public String CodigoTipoEvento;
		public Long IdEvento;
		public Long IdSecuencia;
}
}
