package cl.bcs.plataforma.bat;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.sql.Connection;
import com.google.gson.JsonObject;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.plataforma.bat.InscribirProcesoBatch: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.InscribirProcesoBatch.lanza"), may run scripted SQL on the job connection ("colaboradores.InscribirProcesoBatch.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.InscribirProcesoBatch.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class InscribirProcesoBatch implements JavaCall {

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

    public InscribirProcesoBatchSalida logica(Connection con, InscribirProcesoBatchEntrada entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("InscribirProcesoBatch", entrada, InscribirProcesoBatchSalida.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
