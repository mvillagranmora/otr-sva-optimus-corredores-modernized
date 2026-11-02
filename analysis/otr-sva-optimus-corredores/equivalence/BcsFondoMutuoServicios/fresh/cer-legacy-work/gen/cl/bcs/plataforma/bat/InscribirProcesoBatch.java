// GENERATED DOUBLE of the legacy cl.bcs.plataforma.bat.InscribirProcesoBatch by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.plataforma.bat;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;


import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class InscribirProcesoBatch implements JavaCall{
	/*******************
	 * clases in/out
	 *******************/
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
	
	public static class Secuencia{
		public Long IdSecuencia;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
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
	
	public static class InscribirProcesoBatchSalida{
		public Long			  		P_OUT_ERRORESCONTADOR;
		public List<OutPrograma> 	P_OUT_PROGRAMA;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("InscribirProcesoBatch", "call", new Object[]{con, jin}, JsonObject.class); }
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	@Trace
	public InscribirProcesoBatchSalida logica(Connection con, InscribirProcesoBatchEntrada entrada) throws Exception { return cl.bcs.oracle.Doubles.answer("InscribirProcesoBatch", "logica", new Object[]{con, entrada}, InscribirProcesoBatchSalida.class); }
	
	
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double InscribirProcesoBatch.main: not part of the double"); }
	
	
	
}
