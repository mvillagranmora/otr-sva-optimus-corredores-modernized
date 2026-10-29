package cl.bcs.normativa;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


@SuppressWarnings({"rawtypes", "unchecked"})
public class CircularizacionGrabarCertificado implements JavaCall{
	
		static final Logger logger = LogManager.getLogger(CircularizacionGrabarCertificado.class);
		private final Log log = new Log("NOR.CircularizacionGrabarCertificado");
		
		private final String SQL_SELECT_LOGO_CORREDORA 			= loadResourceToString("cl/bcs/normativa/sql/CircularizacionGrabarCertificado_SelectLogoCorredora.sql");
		private final String SQL_CALL_CERTIFICADO	   			= loadResourceToString("cl/bcs/normativa/sql/CircularizacionGrabarCertificado_CertificadoCustodia_Call.sql");
		private final String SQL_CALL_GRABAR_CERTIFICADO	    = loadResourceToString("cl/bcs/normativa/sql/CircularizacionGrabarCertificado_CertificadoCustodiaGrabar_Call.sql");
		private final String SQL_SELECT_GRUPO_CERTIFICA 		= loadResourceToString("cl/bcs/normativa/sql/CircularizacionGrabarCertificado_SelectGrupoCertificado.sql");
		
		public  final String SQL_EXIST_TABLA_CERTIFICADO  		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GRABARCERTIFICADO_CUSTODIA_CIRCULAR'";
		private final String SQL_CREATE_TABLA_CERTIFICADO 		= "CREATE LOCAL TEMPORARY TABLE #GRABARCERTIFICADO_CUSTODIA_CIRCULAR (\"IdCliente\" BIGINT,\"IdPortafolio\" BIGINT,\"JsonData\" BLOB)";													
		private final String SQL_INSERT_TABLA_CERTIFICADO		= "INSERT INTO #GRABARCERTIFICADO_CUSTODIA_CIRCULAR (\"IdCliente\",\"IdPortafolio\",\"JsonData\") VALUES (:IdCliente,:IdPortafolio,:JsonData)";
		public  final String SQL_DROP_TABLA_CERTIFICADO  		= "DROP TABLE #GRABARCERTIFICADO_CUSTODIA_CIRCULAR";
		
		final String SQL_UPDATE_SOLICITUD = "UPDATE BSCL.\"BSCL.Normativa.BD::NOR.SolicitudCircular\" SET \"EstadoSolicitud\"='',\"NombreArchivo\"=:NombreArchivo,\"IdEstado\"=:IdEstado,\"NumeroRegistros\"=:NumeroRegistros  WHERE \"IdSolicitudCircular\"=:IdSolicitudCircular";
		 
	public static class EntradaCircularizacionGrabarCertificado{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_Periodo;
		public String	p_IdTipoDocumento="CIRCCarta";
		public String   p_UltimoDiaMes ="";
		public Long 	p_IdSolicitudCircular;
		
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCircularizacionGrabarCertificado {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaCircularizacionGrabarCertificado() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class DatosCertificadoCustodia extends BcsPojo{

		public Long 		OrdenInterno;	
		public String 		NombreDestinatario;
		public String 		Rut;
		public String 		CodigoPortafolio;
		public Long 		IdPortafolio;
		public String 		Direccion;
		public Long 		IdCliente;
		
		public String 		Comuna;
		public String 		Ciudad;
		public String 		TelefonoDestinatario;
		public String 		IdTipoPersona;
		public String 		FormaSalida;
		public String 		FechaConsulta;
		public String 		Grupo;
		public String 		IdClasificacionTipoInstrumento;
		public String 		Nemotecnico;
		public String 		IdMoneda;
 		public BigDecimal 	Cantidad;
 		public BigDecimal 	ValorPresente;
 		public String 		FechaVencimiento;
 		public BigDecimal 	PrecioCierre;
 		public BigDecimal 	Tasa;
 		
 		public String 		FechaSistemaLOCAL;
 		public String 		HoraSistemaLOCAL;
 		public String 		CartaConfirmacionCliente;
 		public BigDecimal 	PrecioDecimales;
 		public BigDecimal 	TasaDecimales;
 		public String 		EjecutivoNombre;
 		public String 		EjecutivoDireccionElectronica;
 		public String 		Sexo;
 		public String 		EstadoCivil;
 		//-------------------------------------------------------
 		public String 		RazonCorredora;
 		public String 		DireccionCorredora;
 		public String 		RutCorredora;
 		public String 		TelefonoCorredora;
 		//-------------------------------------------------------
 		//public String 		LogoCorredora;
 		public String 		EsMasivo;
        public String       NombreCompletoRepresentante;
        public String        DocumentoIdentidadRepresentante;
		
			public DatosCertificadoCustodia() {
			this.OrdenInterno = 0L;
		} 
	
	}
	public static class RegistroCertificado extends BcsPojo{

		public Long 		p_IdEmpresa;	
		public String 		p_IdTipoDocumento;
		public String 		p_Periodo;
		public List<tabCertificado> P_Certificados;
	}
	
	public static class tabCertificado extends BcsPojo{

		public Long 		IdCliente;	
		public Long 		IdPortafolio;
		public String 		JsonData;
		
		
	}
	
	public static class ExisteTabCertificado extends BcsPojo{
		public Long count;
	}
	
    public static class ResumenGeneracion extends BcsPojo{
		public Long 	IdSolicitudCircular;
		public String 	NombreArchivo;
		public String 	IdEstado;
		public Long NumeroRegistros;
		public ResumenGeneracion(Long IdSolicitudCircular, String NombreArchivo,String IdEstado,Long NumeroRegistros)
		{
			this.IdSolicitudCircular = IdSolicitudCircular;
			this.NombreArchivo = NombreArchivo;		
			this.IdEstado = IdEstado;	
			this.NumeroRegistros = NumeroRegistros;	
		}
	}
	public static class Cert extends BcsPojo{
		public Long Cert;		
	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCircularizacionGrabarCertificado jin = gson.fromJson(in.toString(), EntradaCircularizacionGrabarCertificado.class);
		SalidaCircularizacionGrabarCertificado out = logica(conn,jin);

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public  SalidaCircularizacionGrabarCertificado logica(Connection con, EntradaCircularizacionGrabarCertificado entrada) throws Exception{
		logger.info("INICIO::CircularizacionGrabarCertificado");
		SalidaCircularizacionGrabarCertificado out = new SalidaCircularizacionGrabarCertificado();
		
				
		SQL sql = new SQL();		
		
		try {
			
			
			HashMap<String, Object> params = new HashMap<>(2);
			params.put("p_FechaConsulta",entrada.p_UltimoDiaMes);
			
			Cert certLista = new SQL().query(con, SQL_SELECT_GRUPO_CERTIFICA, params, Cert.class).stream().findFirst().orElse(new Cert());
			
			if(certLista.Cert>0L) {
				String logoCorredoraB64  = obtenerLogoCorredoraB64(con, entrada.p_IdEmpresa);
				
				HashMap<String,Object> paramIN = new HashMap<>();
				paramIN.put("p_IdEmpresa", entrada.p_IdEmpresa);
				paramIN.put("p_IdCliente", 0L);
				paramIN.put("p_IdPortafolio", 0L);
				paramIN.put("p_FechaConsulta", entrada.p_UltimoDiaMes);
				paramIN.put("p_FormaSalida","CARTA");
				paramIN.put("p_UnificarPortafolios","N");
				paramIN.put("p_IdUsuario", entrada.p_IdUsuario);
				paramIN.put("p_CartaConfirmacionCliente","S");
				HashMap<String, Class> paramOUT = new HashMap<>();
				
				paramOUT.put("p_Salida", DatosCertificadoCustodia.class);
				List<DatosCertificadoCustodia> listaCertificados =new ArrayList<DatosCertificadoCustodia>();
				log.write("*CertificadoCustodiaGrabar* { ejecutando CALL obtener certificados del mes }");
				logger.info("PROCESO::CircularizacionGrabarCertificado.. ejecutando CALL obtener certificados del mes" );
				GrabarSolicitud ( con,entrada.p_IdSolicitudCircular , "EPR", certLista.Cert,0L );
				HashMap<String, Object> resultadoSP = sql.call(con, SQL_CALL_CERTIFICADO, paramIN, paramOUT);
						
				listaCertificados =(List<DatosCertificadoCustodia>) resultadoSP.get("p_Salida");
						 
				if (listaCertificados.size() != 0) {
					GrabarSolicitud ( con,entrada.p_IdSolicitudCircular , "EPR", certLista.Cert,1L );
					 // armar JsonData
					JsonArray json_Obj = new JsonArray();
					long v_counCertif=0L;long v_counClienteCertif=0L;long v_countDetalle=0L; long v_IdCliente=0L;long v_IdPortafolio=0L;

					List<tabCertificado> ListaCert = new  ArrayList<>();
					
					for(DatosCertificadoCustodia rs : listaCertificados){
						 JsonObject registro = new JsonObject();
						if (v_countDetalle==0L){
							v_IdCliente=rs.IdCliente;
							v_IdPortafolio=rs.IdPortafolio;
							};
						registro.addProperty("OrdenInterno", rs.OrdenInterno);
						 String NombreDestinatario=rs.NombreDestinatario;
						 NombreDestinatario =removerCaracteresEspeciales(NombreDestinatario);
						registro.addProperty("NombreDestinatario", NombreDestinatario);
						registro.addProperty("Rut", rs.Rut);
						registro.addProperty("CodigoPortafolio", rs.CodigoPortafolio);
						registro.addProperty("IdPortafolio", rs.IdPortafolio);
						registro.addProperty("Direccion", rs.Direccion);
						
						registro.addProperty("Comuna", rs.Comuna);
						registro.addProperty("Ciudad", rs.Ciudad);
						registro.addProperty("TelefonoDestinatario", rs.TelefonoDestinatario);
						registro.addProperty("IdTipoPersona", rs.IdTipoPersona);
						registro.addProperty("FormaSalida", rs.FormaSalida);
						registro.addProperty("FechaConsulta", rs.FechaConsulta);
						registro.addProperty("Grupo", rs.Grupo);
						registro.addProperty("IdClasificacionTipoInstrumento", rs.IdClasificacionTipoInstrumento);
						registro.addProperty("Nemotecnico", rs.Nemotecnico);
						registro.addProperty("IdMoneda", rs.IdMoneda);
						registro.addProperty("Cantidad", rs.Cantidad);
						registro.addProperty("ValorPresente", rs.ValorPresente);
						registro.addProperty("FechaVencimiento", rs.FechaVencimiento);
						registro.addProperty("PrecioCierre", rs.PrecioCierre);
						registro.addProperty("Tasa", rs.Tasa);
						registro.addProperty("IdCliente", rs.IdCliente);
						registro.addProperty("FechaSistemaLOCAL", rs.FechaSistemaLOCAL);
						registro.addProperty("HoraSistemaLOCAL", rs.HoraSistemaLOCAL);
						registro.addProperty("CartaConfirmacionCliente", rs.CartaConfirmacionCliente);
						registro.addProperty("PrecioDecimales", rs.PrecioDecimales);
						registro.addProperty("TasaDecimales", rs.TasaDecimales);
						registro.addProperty("EjecutivoNombre", rs.EjecutivoNombre);
						registro.addProperty("EjecutivoDireccionElectronica", rs.EjecutivoDireccionElectronica);
						registro.addProperty("Sexo", rs.Sexo);
						registro.addProperty("EstadoCivil", rs.EstadoCivil);
						//-------------------------------------------------------
						registro.addProperty("RazonCorredora", rs.RazonCorredora);
						registro.addProperty("DireccionCorredora", rs.DireccionCorredora);
						registro.addProperty("RutCorredora", rs.RutCorredora);
						registro.addProperty("TelefonoCorredora", rs.TelefonoCorredora);
				 		//-------------------------------------------------------
						registro.addProperty("LogoCorredora",logoCorredoraB64);						
						registro.addProperty("EsMasivo",rs.EsMasivo);
                        registro.addProperty("NombreCompletoRepresentante", rs.NombreCompletoRepresentante);
                        registro.addProperty("DocumentoIdentidadRepresentante", rs.DocumentoIdentidadRepresentante);

						if (v_IdCliente==rs.IdCliente && 
								v_IdPortafolio==rs.IdPortafolio){
							json_Obj.add(registro);
							if ((v_countDetalle + 1) ==listaCertificados.size())//final
							{
								
								tabCertificado certi = new tabCertificado();
								certi.IdCliente=v_IdCliente;
								certi.IdPortafolio=v_IdPortafolio;
								certi.JsonData=json_Obj.toString();
								ListaCert.add(certi);
								 	int startingLength = json_Obj.size();
								    for (int i = startingLength - 1; i >= 0; i--) {
								    	json_Obj.remove(i);
								    }

							}
							
							}else{
								
								
								tabCertificado certi = new tabCertificado();
								certi.IdCliente=v_IdCliente;
								certi.IdPortafolio=v_IdPortafolio;
								if (json_Obj.size()>100)
								{
									log.write( "*CertificadoCustodiaGrabar* { PROBLEMA" + v_IdCliente + "-" + v_IdPortafolio  );
								}
								certi.JsonData=json_Obj.toString();
								ListaCert.add(certi);
								 int startingLength = json_Obj.size();
								    for (int i = startingLength - 1; i >= 0; i--) {
								    	json_Obj.remove(i);
								    }
								json_Obj.add(registro);
								v_counCertif++;
								v_counClienteCertif=v_counClienteCertif+1;
								if (v_counClienteCertif>=4000)
								{
									RegistroCertificado certificado = new RegistroCertificado();
									certificado.p_IdEmpresa=entrada.p_IdEmpresa;
									certificado.p_IdTipoDocumento=entrada.p_IdTipoDocumento;
									certificado.p_Periodo=entrada.p_Periodo;
									certificado.P_Certificados=ListaCert;
									//Grabado de registros
									String Resultado = GrabarCertificado( con,  entrada, certificado);
									GrabarSolicitud ( con,entrada.p_IdSolicitudCircular , "EPR", certLista.Cert,v_counCertif );
									ListaCert.clear();
									v_counClienteCertif=0;
									
								}
								v_IdCliente=rs.IdCliente;
								v_IdPortafolio=rs.IdPortafolio;
								
								
							};
						
						v_countDetalle=v_countDetalle + 1;
						
					}
					
					RegistroCertificado certificado = new RegistroCertificado();
					certificado.p_IdEmpresa=entrada.p_IdEmpresa;
					certificado.p_IdTipoDocumento=entrada.p_IdTipoDocumento;
					certificado.p_Periodo=entrada.p_Periodo;
					certificado.P_Certificados=ListaCert;
					//Grabado de registros
					String Resultado = GrabarCertificado( con,  entrada, certificado);
					GrabarSolicitud ( con,entrada.p_IdSolicitudCircular , "PRO", certLista.Cert,v_counCertif +1);
					
				}
			}else{
				logger.info("FIN::CircularizacionGrabarCertificado:sin regsitros");
				GrabarSolicitud ( con,entrada.p_IdSolicitudCircular , "PRO", certLista.Cert,0L );
			};
	
		}catch (Exception e) {
			e.printStackTrace();
	
		}
		
		
		
		
		logger.info("FIN::CircularizacionGrabarCertificado");
		return out;
	}
	public String GrabarSolicitud(Connection con,Long secuenciaSolicitud, String Estado,Long tot,Long reg) throws SQLException {
		SQL sql = new SQL();
		List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
		String NombreArchivo="< " + reg + " de "+ tot + " >";
		if (reg==0L){
			NombreArchivo="Buscando certificados...";
		};
		
		programa.add(new ResumenGeneracion(secuenciaSolicitud,NombreArchivo,  Estado,0L));
		sql.batch(con, SQL_UPDATE_SOLICITUD, programa,ResumenGeneracion.class);
		con.commit();
		return NombreArchivo;
	}
	

	
	
    public static String removerCaracteresEspeciales(String str)
    {
        // replace the given string
        // with empty string
        // except the pattern "[^a-zA-Z0-9]"
        String StringNormalizado = Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
       
        // return string
        return StringNormalizado;
    } 
    
	public String GrabarCertificado(Connection con, EntradaCircularizacionGrabarCertificado entrada,RegistroCertificado certificado) throws SQLException {
		String resultGrabar="";
		SQL sql = new SQL();	
		HashMap<String, Class> paramOUT = new HashMap<>();
		try {
			HashMap<String, Object> params = new HashMap<>(2);
			List<ExisteTabCertificado> TabRut =new ArrayList<ExisteTabCertificado>();
			TabRut = new SQL().query(con, SQL_EXIST_TABLA_CERTIFICADO, params, ExisteTabCertificado.class);
			if(TabRut.size()>0) {
				try {				
					SQL.exec(con, SQL_DROP_TABLA_CERTIFICADO);				
					log.write( "*CertificadoCustodiaGrabar* { SE EJECUTA EL DROP INICIAL}");
				} catch (Exception er) { 
					//er.printStackTrace();
					log.write("*CertificadoCustodiaGrabar* {DROP INICIAL, NO LOGRADO}");
				}
				
			}else{
				log.write("*CertificadoCustodiaGrabar* { NO SE EJECUTA EL DROP INICIAL}");
			}
			
			
			SQL.exec(con, SQL_CREATE_TABLA_CERTIFICADO);			
			sql.batch(con,SQL_INSERT_TABLA_CERTIFICADO ,certificado.P_Certificados, tabCertificado.class);
			
			
			HashMap<String,Object> paramCerIN = new HashMap<>();
			paramCerIN.put("p_IdEmpresa", certificado.p_IdEmpresa);
			paramCerIN.put("p_IdTipoDocumento", certificado.p_IdTipoDocumento);
			paramCerIN.put("p_Periodo" , certificado.p_Periodo);		
			log.write("*CertificadoCustodiaGrabar* { ejecutando CALL grabar BLOB de certificados }");
			logger.info("PROCESO::CircularizacionGrabarCertificado.. ejecutando CALL grabar BLOB de certificados" );
			HashMap<String, Object> resultadoSPGrab = sql.call(con, SQL_CALL_GRABAR_CERTIFICADO, paramCerIN, paramOUT);
			con.commit();
			 resultGrabar = resultadoSPGrab.get("ResultSet_0") == null ? new String() : (resultadoSPGrab.get("ResultSet_0").toString());
			

						
						
		}catch (Exception e) {
			e.printStackTrace();
	
		}
		return resultGrabar;
	}
	public String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::CircularizacionGrabarCertificado.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		Imagen logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(logo.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CircularizacionGrabarCertificado.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	
	public static class Imagen extends BcsPojo {
		public byte[] Contenido;

		public Imagen() {
			Contenido = new byte[0];
		}
	}
	

public static void main (String [] args) throws Exception{
		
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			//Connection con = PlataformaUtils.getMASTER1Connection();
  			try{
  				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
  				 		
  				
  				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Periodo\":\"202208\",\"p_IdTipoDocumento\":\"CIRCCarta\",\"p_UltimoDiaMes\":\"2022-08-31\",\"p_IdSolicitudCircular\":1894}";
  				EntradaCircularizacionGrabarCertificado Objin_json = gson.fromJson(json, EntradaCircularizacionGrabarCertificado.class);
  				
  				CircularizacionGrabarCertificado demo = new CircularizacionGrabarCertificado();	
  				SalidaCircularizacionGrabarCertificado ObjOut = demo.logica(con, Objin_json);  				
  				System.out.println(ObjOut.toString());
  				//con.rollback();
 				
  				con.commit();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
  		
        System.out.println("== END ==");
	}
	
	
}
