package cl.bcs.plataforma.ftp;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.BcsPojo;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;
import cl.bcs.utils.Fecha;

/**
 * ORACLE FAKE of legacy cl.bcs.plataforma.ftp.FileManager (cl/bcs/plataforma/ftp/FileManager.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Only the two row POJOs are needed (the file transfer itself is not part of the characterised classes).
 */
public class FileManager {

	// legacy cl/bcs/plataforma/ftp/FileManager.java:106-122 (verbatim)
	public static class Cabecera extends BcsPojo {
		public Long IdCabecera;
		public String IdCodigoTipo;
		public String IdEstado;
		public java.sql.Timestamp FechaCreacion;
		public Long IdUsuario;
		public String NombreArchivo;
		
		public Cabecera() {
			this.IdCabecera = 0L;
			this.IdCodigoTipo = "";
			this.IdEstado = "";
			this.FechaCreacion = Fecha.toDateTime("0001-01-01");
			this.IdUsuario = 0L;
			this.NombreArchivo = "";
		}
	}

	// legacy cl/bcs/plataforma/ftp/FileManager.java:124-138 (verbatim)
	public static class Detalle extends BcsPojo {
		public Long IdDetalle;
		public Long IdCabecera;
		public String Linea;
		public String IdTipoEntidad;
		public Long IdEntidad;
		
		public Detalle() {
			this.IdDetalle = 0L;
			this.IdCabecera = 0L;
			this.Linea = "";
			this.IdTipoEntidad = "";
			this.IdEntidad = 0L;
		}
	}


}
