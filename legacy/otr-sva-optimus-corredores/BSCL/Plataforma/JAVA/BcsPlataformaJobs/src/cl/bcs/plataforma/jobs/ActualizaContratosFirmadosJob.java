package cl.bcs.plataforma.jobs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.ctt.ImprimirContrato;
import cl.bcs.socionegocio.ctt.ImprimirContrato.ImprimirContrato_Entrada;
import cl.bcs.socionegocio.ctt.ImprimirContrato.ImprimirContrato_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizaContratosFirmadosJob extends BcsJob implements JavaCall {
	AtomicBoolean running=new AtomicBoolean(false);
	private final Log log = new Log("PLA.ActualizaContratosFirmadosJob");

	static final Logger logger = LogManager.getLogger(ActualizaContratosFirmadosJob.class);

	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_CONTRATOS_ACTUALIZAR_SUBIR = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaContratosFirmadosJob_ConsultarFirmaContrato.sql");
	public String SQL_DOCUMENTO_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::DOC.Documento\" (\"IdDocumento\",\"IdTipoDocumento\",\"IdEmpresa\",\"IdCliente\",\"IdUltimoAnexo\")values( :IdDocumento,:IdTipoDocumento,:IdEmpresa,:IdCliente,:IdUltimoAnexo);";
	public String SQL_ANEXO_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::DOC.Anexo\"(\"IdAnexo\",\"IdDocumento\",\"IdEstado\",\"NumeroFirmantesEmpresa\",\"NumeroFirmantesCliente\",\"NombreFirmanteEmpresa1\", \"NombreFirmanteEmpresa2\",\"NombreFirmanteEmpresa3\",\"NombreFirmanteCliente1\",\"NombreFirmanteCliente2\",\"NombreFirmanteCliente3\",\"FechaIngreso\",\"FechaModificacion\",\"FechaVigencia\",\"DocumentoFisicoReferencia\",\"NombreArchivoAdjunto\", \"FormatoArchivoAdjunto\", \"IdTipoCanalIngreso\", \"NumeroFirmasMinimas\", \"NumeroFirmasMaximas\")values(:IdAnexo,:IdDocumento,:IdEstado,:NumeroFirmantesEmpresa,:NumeroFirmantesCliente,'','','','','','',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,'0001-01-01','',:NombreArchivoAdjunto,:FormatoArchivoAdjunto,:IdTipoCanalIngreso, :NumeroFirmasMinimas, :NumeroFirmasMaximas);";
	public String SQL_ANEXO_UPDATE_ESTADO = "UPDATE \"BSCL.Plataforma.BD::DOC.Anexo\" SET \"IdEstado\" = :IdEstado ,\"FechaVigencia\" = CURRENT_UTCTIMESTAMP WHERE \"IdAnexo\" = :IdAnexo AND \"IdDocumento\" = :IdDocumento";
	public String SQL_SELECT_TIPO_DOCUMENTO = "SELECT TD.\"IdTipoDocumento\", TD.\"NumeroFirmasMinimas\", TD.\"NumeroFirmasMaximas\" FROM \"BSCL.Plataforma.BD::DOC.TipoDocumento\" TD WHERE TD.\"IdTipoDocumento\" = :IdTipoDocumento;";

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class ConsultarFirmaContrato_Entrada  {
		public Long v_IdEmpresa;
		public String v_TipoDocumento;
		public String v_IdTipoPersona;
		public String v_RequiereFirmaCliente;
		public String v_CanalesDeEnrolamiento;
	}

	public static class DatosEnviaSubirImpreso  {
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdDocumento;
		public Long IdPersona;
		public String Nombres;
		public String ApellidoPaterno;
		public String IdTipoCliente;
		public String TipoPersona;
		public String IdTipoDocumento;
		public String IdTipoPersona;
		public String NombreDocumento;
		public String CodPlantilla;
		public String RequiereFirmaCliente;
		public Integer NumeroFirmantesCliente;
		public Integer NumeroFirmantesEmpresa;
		public Long IdAnexo;
		public String EstadoAnexo;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String IdTipoCanalIngreso;
	}

	public static class EntidadTipoDocumento {
		public String IdTipoDocumento;
		public Integer NumeroFirmasMinimas;
		public Integer NumeroFirmasMaximas;

		public EntidadTipoDocumento() {
			IdTipoDocumento = "";
			NumeroFirmasMinimas = 0;
			NumeroFirmasMaximas = 0;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntidadDocumento {
		public Long IdDocumento;
		public String IdTipoDocumento;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUltimoAnexo;
	}

	public static class EntidadAnexo {
		public Long IdAnexo;
		public Long IdDocumento;
		public String IdEstado;
		public Long NumeroFirmantesEmpresa;
		public Long NumeroFirmantesCliente;
		public Integer NumeroFirmasMinimas;
		public Integer NumeroFirmasMaximas;
		public String NombreArchivoAdjunto;
		public String FormatoArchivoAdjunto;
		public String IdTipoCanalIngreso;
	}

	public List<DatosEnviaSubirImpreso> BuscaContratos(Connection conn, ConsultarFirmaContrato_Entrada param)
			throws Exception {
		List<String> TipoPersona = new ArrayList<String>();
		if (param.v_IdTipoPersona.equalsIgnoreCase("A")) {
			TipoPersona.add("J");
			TipoPersona.add("N");
			TipoPersona.add("M");
		} else {
			TipoPersona.add(param.v_IdTipoPersona);
		}
		
		List<String> EstadoAnexo = new ArrayList<String>();
		if(param.v_RequiereFirmaCliente.equals("S") ) {
			EstadoAnexo.add("ACT");
			EstadoAnexo.add("VCD");
			EstadoAnexo.add("PEN");
			EstadoAnexo.add("PDF");
		}else {	
			EstadoAnexo.add("PDF");
			EstadoAnexo.add("PEN");
		}
		
		List<String> canalesIngreso = new ArrayList<String>(Arrays.asList(param.v_CanalesDeEnrolamiento.split("-")));
		
		HashMap<String, Object> params = new HashMap<>(4);
		params.put("IdTipoDocumento", param.v_TipoDocumento);
		params.put("IdTipoPersona",TipoPersona);
		params.put("EstadoAnexo", EstadoAnexo);
		params.put("IdsTipoCanalIngreso", canalesIngreso);
		
		SQL sql = new SQL();
		List<DatosEnviaSubirImpreso> datosEnviaSubirImpresos = sql.query(conn, SQL_SELECT_CONTRATOS_ACTUALIZAR_SUBIR,params, DatosEnviaSubirImpreso.class);

		return datosEnviaSubirImpresos;
		
		
	}

	public InputStream ImprimeContratosPDF(Connection conn, String IdEmpresa, String IdPlantilla, String IdModulo,
			String TipoDocumentoIdentidad, String DocumentoIdentidad) {
		InputStream filePart = null;
		try {
			
			ImprimirContrato_Entrada entrada = new ImprimirContrato_Entrada();
			
			entrada.p_IdEmpresa = Long.valueOf(IdEmpresa );
			entrada.p_CodigoContrato= IdPlantilla;
			entrada.p_IdModulo= IdModulo ; 
			entrada.p_TipoDocumentoIdentidad =  TipoDocumentoIdentidad;
			entrada.p_DocumentoIdentidad =  DocumentoIdentidad ;
			
			ImprimirContrato_Salida salida = new ImprimirContrato().logica(conn, entrada);

			String b64 = salida.p_out_ContratoB64;

			salida.p_out_ContratoB64 = "ContenidoEnBase64"; 
			
			if (salida.P_OUT_ERRORESCONTADOR > 0L) {
				conn.rollback();
			}

		 
			filePart = new ByteArrayInputStream(Base64.decodeBase64(b64));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

		return filePart;
	}

	public void SubeDocumentoDB(Connection conn, InputStream filePart, String IdTipoDocumento, Long IdEmpresa,
			Long IdCliente, String IdDocumento, String NumeroFirmantesEmpresa, String NumeroFirmantesCliente,
			String IdTipoCanalIngreso,String EstadoAnexo, String RequiereFirmaCliente, Long pIdAnexo) throws Exception {
		String IdAnexo = String.valueOf(pIdAnexo);
		Long IdAnexoEntrada = Long.parseLong(IdAnexo);
		String FormatoArchivoAdjunto = "PDF";
		String NombreArchivoAdjunto = IdTipoDocumento + IdAnexo + ".pdf";

		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdTipoDocumento", IdTipoDocumento);
		EntidadTipoDocumento tipoDocumento = sql
				.query(conn, SQL_SELECT_TIPO_DOCUMENTO, params, EntidadTipoDocumento.class).stream().findFirst()
				.orElse(new EntidadTipoDocumento());

			if (IdDocumento != null && IdDocumento.equals("0")) {
		
			Long idDocumento = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Documento", (long) 1).get(0);

			try {
				EntidadDocumento entidadDocumento = new EntidadDocumento();
				entidadDocumento.IdDocumento = idDocumento;
				entidadDocumento.IdTipoDocumento = IdTipoDocumento;
				entidadDocumento.IdEmpresa = IdEmpresa;
				entidadDocumento.IdCliente = IdCliente;
				entidadDocumento.IdUltimoAnexo = 0L;
				sql.batch(conn, SQL_DOCUMENTO_INSERT, Arrays.asList(entidadDocumento), EntidadDocumento.class);

				IdDocumento = idDocumento.toString();

			} catch (Exception e) {
				conn.rollback();
				throw new Exception(e);
			}
		}

		
		if (IdAnexo != null) {
		
			Long idAnexo = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Anexo", (long) 1).get(0);

			try {
				EntidadAnexo entidadAnexo = new EntidadAnexo();
				entidadAnexo.IdAnexo = idAnexo;
				entidadAnexo.IdDocumento = new Long(IdDocumento);
				entidadAnexo.IdEstado = "PDF";
				entidadAnexo.NumeroFirmantesEmpresa = Long.parseLong(NumeroFirmantesEmpresa);
				entidadAnexo.NumeroFirmantesCliente = Long.parseLong(NumeroFirmantesCliente);
				entidadAnexo.NumeroFirmasMinimas = tipoDocumento.NumeroFirmasMinimas;
				entidadAnexo.NumeroFirmasMaximas = tipoDocumento.NumeroFirmasMaximas;
				entidadAnexo.NombreArchivoAdjunto = NombreArchivoAdjunto;
				entidadAnexo.FormatoArchivoAdjunto = FormatoArchivoAdjunto;
				entidadAnexo.IdTipoCanalIngreso = IdTipoCanalIngreso;
				sql.batch(conn, SQL_ANEXO_INSERT, Arrays.asList(entidadAnexo), EntidadAnexo.class);

			
				IdAnexo = idAnexo.toString();
			} catch (Exception e) {
				conn.rollback();
				throw new Exception(e);
			}
		}

		{
			String update = "UPDATE \"" + Utils.getSchema() + "\".\"BSCL.Plataforma.BD::DOC.Documento\" "
					+ "SET \"IdUltimoAnexo\" =?, \"DocumentoSubido\" ='N' WHERE \"IdDocumento\" = ?";

			  logger.debug(update);

			try (PreparedStatement pstmt = conn.prepareStatement(update)) {
				pstmt.setLong(1, new Long(IdAnexo));
				pstmt.setLong(2, new Long(IdDocumento));
				pstmt.executeUpdate();

			} catch (SQLException ex) {
				conn.rollback();
				throw new Exception(ex);
			}
		}

		{
			String update = "UPDATE \"" + Utils.getSchema() + "\".\"BSCL.Plataforma.BD::DOC.Anexo\" " + "SET"
					+ " \"ArchivoAdjunto\" =?," + " \"IdEstado\" = ?,"
					+ "\"NombreArchivoAdjunto\" =?, \"FormatoArchivoAdjunto\" =?"
					+ "WHERE \"IdDocumento\" = ? AND \"IdAnexo\" = ?";

			try (PreparedStatement pstmt = conn.prepareStatement(update)) {
				pstmt.setBlob(1, filePart);
				pstmt.setString(2, "PDF");
				pstmt.setString(3, NombreArchivoAdjunto);
				pstmt.setString(4, FormatoArchivoAdjunto);
				pstmt.setLong(5, new Long(IdDocumento));
				pstmt.setLong(6, new Long(IdAnexo));
				pstmt.executeUpdate();
			
			} catch (SQLException ex) {
				conn.rollback();
				throw new Exception(ex);
			}
		}
		
		
		if ( EstadoAnexo.equalsIgnoreCase("PDF") || EstadoAnexo.equalsIgnoreCase("PEN") ) {
				
			String delete = "DELETE FROM \"" + Utils.getSchema() + "\".\"BSCL.Plataforma.BD::DOC.Anexo\" " + " WHERE "+ "\"IdAnexo\"= ? ";
		 	try (PreparedStatement pstmt = conn.prepareStatement(delete)) {
				 pstmt.setLong(1, IdAnexoEntrada);
			     pstmt.execute();

			} catch (SQLException ex) {
				conn.rollback();
				throw new Exception(ex);
			}			
		}else {
    		if (IdAnexoEntrada != null && IdAnexoEntrada != 0) {
			try {
				EntidadAnexo entidadAnexo = new EntidadAnexo();
				entidadAnexo.IdAnexo = IdAnexoEntrada;
				entidadAnexo.IdDocumento = Long.parseLong(IdDocumento);
				entidadAnexo.IdEstado = "VCD";
				sql.batch(conn, SQL_ANEXO_UPDATE_ESTADO, Arrays.asList(entidadAnexo), EntidadAnexo.class);
			} catch (Exception e) {
				conn.rollback();
				throw new Exception(e);
			}
		 }
		}
	
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}

	@Override
	public void onError(Throwable t) {
		log.write(t.getMessage());
        log.write("****** ERROR TAREA  *******");
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean logica(Connection conn, boolean debug)	throws Exception {
	    running.set(false);
	 	    
		logger.info("INICIO::ConsultarFirmaContrato");
		long inicio = System.currentTimeMillis();
		
		ConsultarFirmaContrato_Entrada  ingreso   =new ConsultarFirmaContrato_Entrada();
			 
		 if (debug) 
	        {   //Paramatros para Debug
	        	
			    ingreso.v_IdEmpresa =(long)1;
			    ingreso.v_TipoDocumento ="CONTMARFWD";
			    ingreso.v_IdTipoPersona ="A";
			    ingreso.v_RequiereFirmaCliente ="S";		
	        } 
	        else
	        { 
				ingreso.v_IdEmpresa = Long.parseLong(this.getParam("p_IdEmpresa"));
				ingreso.v_TipoDocumento = String.valueOf(this.getParam("p_TipoDocumento"));
				ingreso.v_IdTipoPersona = String.valueOf(this.getParam("p_IdTipoPersona"));
				ingreso.v_RequiereFirmaCliente = String.valueOf(this.getParam("p_RequiereFirmaCliente"));
				ingreso.v_CanalesDeEnrolamiento = String.valueOf(this.getParam("p_CanalesDeEnrolamiento"));
	        }
		 

		 List<DatosEnviaSubirImpreso> inArry = new ArrayList<DatosEnviaSubirImpreso>();
		inArry = BuscaContratos(conn, ingreso);
		
		for (int x = 0; x < inArry.size(); ++x) {		
		
			if ((!inArry.get(x).CodPlantilla.toUpperCase().equalsIgnoreCase("FALSE")) && ( inArry.get(x).CodPlantilla != null)) {
			InputStream ArchivoBuffer = null;
			ArchivoBuffer = ImprimeContratosPDF(conn, String.valueOf(ingreso.v_IdEmpresa),
					inArry.get(x).CodPlantilla, "PLA", inArry.get(x).IdTipoDocumentoIdentidad,
					inArry.get(x).DocumentoIdentidad);			
			
			  SubeDocumentoDB(conn, ArchivoBuffer , 
					         inArry.get(x).IdTipoDocumento ,
			                 ingreso.v_IdEmpresa , 
			                 inArry.get(x).IdCliente ,
		                	 String.valueOf(inArry.get(x).IdDocumento) , 
		                	 String.valueOf(inArry.get(x).NumeroFirmantesEmpresa) ,
			                 String.valueOf(inArry.get(x).NumeroFirmantesCliente) ,
			                 inArry.get(x).IdTipoCanalIngreso ,
			                 inArry.get(x).EstadoAnexo ,
			                 ingreso.v_RequiereFirmaCliente,
			                 inArry.get(x).IdAnexo );
			 		 
			  logger.info(String.valueOf(ingreso.v_IdEmpresa)+" "+
				       	    String.valueOf(inArry.get(x).IdCliente)+" "+
					        inArry.get(x).DocumentoIdentidad +" "+
				         	inArry.get(x).CodPlantilla +" "+inArry.get(x).IdTipoCanalIngreso + " "+
				         	inArry.get(x).IdTipoDocumento +" "+
				         	inArry.get(x).IdTipoDocumentoIdentidad +" "+
				         	String.valueOf(inArry.get(x).IdDocumento)
				           	);
			  
	  
		}else {
			logger.info("sin plantilla");
		}
		}
		 
		  logger.info("== END ==");

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarFirmaContrato", fin - inicio);

		return true;

	}
	
	// -------------------------------------------------
		// METODO PARA PRUEBAS LOCALES
		// -------------------------------------------------
		public static void main(String[] args) {
			System.out.println("== INICIO PRUEBA LOCAL ==");

			try (Connection con = PlataformaUtils.getDESAConnection()) {			
				try {
					
				ActualizaContratosFirmadosJob demo = new ActualizaContratosFirmadosJob();
				demo.logica(con,true);
			
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}
				con.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			System.out.println("== FIN PRUEBA LOCAL ==");
		}

}