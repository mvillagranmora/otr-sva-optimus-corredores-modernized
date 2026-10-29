package cl.bcs.plataforma.bat;

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
import java.io.InputStream;

/**
 * ORACLE SHIM of cl.bcs.plataforma.bat.InscribirProcesoBatch: only imported (never invoked) by ActualizarSaldoCargoAbono; POJOs verbatim.
 */
public class InscribirProcesoBatch implements JavaCall {

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:42-58 (verbatim)
	public static class Parametro{
		public Long   	IdInterno;
		public String	NombreParametro;
		public String	Valor;
		public String	ValorBlob;
		
		public Parametro(Long IdInterno, String NombreParametro, String Valor, String ValorBlob){
			this.IdInterno = IdInterno;
			this.NombreParametro = NombreParametro;
			this.Valor = Valor;
			this.ValorBlob = ValorBlob;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:60-70 (verbatim)
	public static class ParametrosAgenda {
		public Long					IdAgenda; 
		public Integer				Orden; 
		public String				NombreParametro; 
		public String				TipoDato; 
		public String				Valor;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:72-92 (verbatim)
	public static class InTipoPrograma{
		public Long   IdInterno;
		public String IdTipoPrograma;
		public Long	  SegundosDesfase;
		public String Comentario = "";
		public InTipoPrograma() {
			
		}
		public InTipoPrograma(Long idInterno, String idTipoPrograma, Long segundosDesfase, String comentario) {
			super();
			IdInterno = idInterno;
			IdTipoPrograma = idTipoPrograma;
			SegundosDesfase = segundosDesfase;
			Comentario = comentario;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:94-108 (verbatim)
	public static class OutPrograma{
		public Long   	 IdInterno;
		public Long	  	 IdPrograma;
		public Timestamp FechaEjecucion;
		
		public OutPrograma(Long IdInterno, Long IdPrograma, Timestamp FechaEjecucion){
			this.IdInterno = IdInterno;
			this.IdPrograma = IdPrograma;
			this.FechaEjecucion = FechaEjecucion;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:110-129 (verbatim)
	public static class ParametroPrograma{
		public Long IdGrupoParametro;
		public Long IdPrograma;
		public String NombreParametro;
		public String Valor;
		public String ValorBlob;
		public InputStream BlobStream;
		
		public ParametroPrograma(Long IdGrupoParametro,Long IdPrograma, String NombreParametro,String Valor,String ValorBlob){
			this.IdGrupoParametro = IdGrupoParametro;
			this.IdPrograma = IdPrograma;
			this.NombreParametro = NombreParametro;
			this.Valor = Valor;
			this.ValorBlob = ValorBlob;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:131-155 (verbatim)
	public static class Programa{
		public Long IdPrograma;
		public Long IdGrupoParametro;
		public String IdTipoPrograma;
		public Long	IdUsuario;
		public String FechaEjecucion;
		public String FechaSistema;
		public String Nodo;
		public String Comentario;
		
		public Programa(Long IdPrograma,Long IdGrupoParametro,String IdTipoPrograma,Long IdUsuario,String FechaEjecucion,String FechaSistema, String Nodo, String Comentario){
			this.IdPrograma = IdPrograma;
			this.IdGrupoParametro = IdGrupoParametro;
			this.IdTipoPrograma = IdTipoPrograma;
			this.IdUsuario = IdUsuario;
			this.FechaEjecucion = FechaEjecucion;
			this.FechaSistema = FechaSistema;
			this.Nodo = Nodo;
			this.Comentario = Comentario;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:157-170 (verbatim)
	public static class TipoPrograma{
		public String IdTipoPrograma;
		public String Descripcion;
		public Integer MaximoIntentos;
		public String Modulo;
		public String NombreClasePrograma;
		public String TipoClasePrograma;
		public String Nodo;
		public String EsPauta;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:172-178 (verbatim)
	public static class Secuencia{
		public Long IdSecuencia;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:180-190 (verbatim)
	public static class InscribirProcesoBatchEntrada {
	    public Long     		  p_IdEmpresa;
	    public Long				  p_IdUsuario;
	    public String			  p_IdModulo;		  
	    public List<InTipoPrograma> p_TipoPrograma;
	    public List<Parametro> 	  p_Parametro;
	    @Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/bat/InscribirProcesoBatch.java:192-200 (verbatim)
	public static class InscribirProcesoBatchSalida{
		public Long			  		P_OUT_ERRORESCONTADOR;
		public List<OutPrograma> 	P_OUT_PROGRAMA;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }

}
