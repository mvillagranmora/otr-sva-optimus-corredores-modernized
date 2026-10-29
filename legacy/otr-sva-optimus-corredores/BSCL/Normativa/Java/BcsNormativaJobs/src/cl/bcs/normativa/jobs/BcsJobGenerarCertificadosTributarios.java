package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.normativa.declaraciones.GenerarCertificado21Masivo.Rut;

import cl.bcs.normativa.declaraciones.GenerarCertificado21Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado21Masivo.EntradaGenerarCertificado21Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado21Masivo.SalidaGenerarCertificado21Masivo;

import cl.bcs.normativa.declaraciones.GenerarCertificado9Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado9Masivo.EntradaGenerarCertificado9Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado9Masivo.SalidaGenerarCertificado9Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado17Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado17Masivo.EntradaGenerarCertificado17Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado17Masivo.SalidaGenerarCertificado17Masivo;

import cl.bcs.normativa.declaraciones.GenerarCertificado24Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado24Masivo.EntradaGenerarCertificado24Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado24Masivo.SalidaGenerarCertificado24Masivo;

import cl.bcs.normativa.declaraciones.GenerarCertificado36Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado36Masivo.EntradaGenerarCertificado36Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado36Masivo.SalidaGenerarCertificado36Masivo;

import cl.bcs.normativa.declaraciones.GenerarCertificado43Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado43Masivo.EntradaGenerarCertificado43Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado43Masivo.SalidaGenerarCertificado43Masivo;

import cl.bcs.normativa.declaraciones.GenerarCertificado71Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado71Masivo.EntradaGenerarCertificado71Masivo;
import cl.bcs.normativa.declaraciones.GenerarCertificado71Masivo.SalidaGenerarCertificado71Masivo;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;

public class BcsJobGenerarCertificadosTributarios extends BcsJob  {
	
	
	AtomicBoolean running = new AtomicBoolean(false);
	public String IdTipoPrograma = "GenerarCertificadosTributarios";
	private final Log log = new Log("FM.BcsJobGenerarCertificadosTributarios");
	
	
	private static FileLoader loader = new FileLoader();
	private static String SQL_SELECT_GRUPO_CERTIF21 = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectGrupoCertificado21.sql");
	private static String SQL_UPDATE_GRUPO_CERTIF21_INACTIVO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateGrupoCertificado21Inactivo.sql");
	private static String SQL_SELECT_GRUPO_CERTIF43 = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectGrupoCertificado43.sql");
	private static String SQL_UPDATE_GRUPO_CERTIF43_INACTIVO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateGrupoCertificado43Inactivo.sql");
	private static String SQL_SELECT_GRUPO_CERTIF71 = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectGrupoCertificado71.sql");
	private static String SQL_UPDATE_GRUPO_CERTIF71_INACTIVO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateGrupoCertificado71Inactivo.sql");
	private static String SQL_SELECT_GRUPO_CERTIF24 = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectGrupoCertificado24.sql");
	private static String SQL_UPDATE_GRUPO_CERTIF24_INACTIVO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateGrupoCertificado24Inactivo.sql");
	private static String SQL_SELECT_GRUPO_CERTIF36 = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectGrupoCertificado36.sql");
	private static String SQL_UPDATE_GRUPO_CERTIF36_INACTIVO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateGrupoCertificado36Inactivo.sql");
	private static String SQL_SELECT_GRUPO_CERTIF17 = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectGrupoCertificado17.sql");
	private static String SQL_UPDATE_GRUPO_CERTIF17_INACTIVO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateGrupoCertificado17Inactivo.sql");
	private static String SQL_SELECT_GRUPO_CERTIF9 = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectGrupoCertificado9.sql");
	private static String SQL_UPDATE_GRUPO_CERTIF9_INACTIVO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateGrupoCertificado9Inactivo.sql");

	private static String SQL_UPDATE_CERTIFICADO_ESTADO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateCertificadoClienteEstado.sql");
	private static String SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateCertificadoClienteActivo.sql");
	
	private static String SQL_SELECT_RESUMEN_TRIBUTARIO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_SelectResumenTributario.sql");
	private static String SQL_INSERT_RESUMEN_TRIBUTARIO = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_InsertResumenTributario.sql");
	
	private final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";
	private final String SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO = "UPDATE \"BSCL.Normativa.BD::TRI.ResumenGeneracion\" SET \"NombreArchivo\"=:RutaArchivo, \"Glosa\"=:Glosa, \"TotalRegistrosGenerados\"=:TotalRegistrosGenerados WHERE \"IdProceso\"= :IdProceso";
	
	
	
	//	Rut desde solicitud
	private final String rutSolicitud ="SELECT RTRIM(P.\"Valor\") AS \"Rut\" FROM \"BSCL.Normativa.BD::NOR.SolicitudParametros\" P WHERE P.\"IdSolicitudCircular\"=? AND P.\"TipoParametro\" ='RUT_MAS' UNION ALL "
	+ "SELECT RTRIM(CL.\"DocumentoIdentidad\") AS \"Rut\" FROM \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" CL INNER JOIN \"BSCL.Normativa.BD::NOR.SolicitudParametros\" P ON CL.\"IdCliente\"=P.\"Valor\" AND P.\"TipoParametro\"='IDCLI' WHERE P.\"IdSolicitudCircular\"=?";

	

	
	public static class ResumenGeneracion extends BcsPojo{
		public Long 	IdProceso;
		public String 	RutaArchivo;
		public String 	Glosa;
		public Long TotalRegistrosGenerados;
		
		public ResumenGeneracion(Long IdProceso, String RutaArchivo, String Glosa,Long TotalRegistrosGenerados)
		{
			this.IdProceso = IdProceso;
			this.RutaArchivo = RutaArchivo;		
			this.Glosa = Glosa;
			this.TotalRegistrosGenerados = TotalRegistrosGenerados;
		}
	}
	
	public static class ResumenDJ extends ResumenGeneracion{
		public Long idEmpresa;
		public Long idUsuario;
		public String EsquemaDJ;		
		public String VersionDJ;
		
		public ResumenDJ(Long IdProceso, String RutaArchivo, String Glosa, Long TotalRegistrosGenerados, String EsquemaDJ, String VersionDJ,Long idEmpresa,Long idUsuario) {
			super(IdProceso, RutaArchivo, Glosa, TotalRegistrosGenerados);
			this.IdProceso = IdProceso;
			this.RutaArchivo = RutaArchivo;		
			this.Glosa = Glosa;
			this.TotalRegistrosGenerados = TotalRegistrosGenerados;
			this.EsquemaDJ = EsquemaDJ;
			this.VersionDJ = VersionDJ;
			this.idEmpresa = idEmpresa;
			this.idUsuario = idUsuario;
			 
			
		}
	
		
		
	}
	public static class RutParticion extends Rut{
				
		public Long   Cantidad;
		public RutParticion () {
			
			this.Cantidad = 0L;
		}
	}
	
	public static class Entrada extends BcsPojo{
		
		
		public List<Rut> p_listaRut = new ArrayList<Rut>();
		
		public Long idEmpresa;
		public Long idUsuario;
		public String idModulo ; 
		
		public String listaRutStr;//389,387
		public String IdTipoDocumento;
		public String VersionArchivo;
		public Long IdSolicitudCircular;
		public Long NumeroTramo;
		public double CantidadTramos;
		
		public String EsquemaDJ;		
		public String VersionDJ;
		
		public Entrada () {
			
			this.idEmpresa = 0L;
		}
	}
	
	public static class SalidaPrograma {
		public Long						P_OUT_IDNOMINA;
		public Long						P_OUT_IDPROGRAMA;
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long P_OUT_IDAGENDA;
		
		public SalidaPrograma() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class ResResumen extends BcsPojo{
		public Long count;
		public ResResumen () {
			
			this.count = 0L;
		}
		
	}
	public static class VersionCertificado extends BcsPojo{
		public String IdTipoDocumento;		
		public String VersionArchivo;
		public String IdEstado;
		
		public VersionCertificado(String IdTipoDocumento, String VersionArchivo,String IdEstado)
		{
			this.IdTipoDocumento = IdTipoDocumento;
			this.VersionArchivo = VersionArchivo;		
			this.IdEstado = IdEstado;
		}
	}
	
	public static class CertificadoEstadoRut extends Rut{
		public String IdTipoDocumento;		
		public String VersionArchivo;
		public String IdEstado;
		
		
		public CertificadoEstadoRut(String IdTipoDocumento, String VersionArchivo,String IdEstado,String Rut)
		{
			this.IdTipoDocumento = IdTipoDocumento;
			this.VersionArchivo = VersionArchivo;		
			this.IdEstado = IdEstado;
			this.Rut = Rut;
			
		}
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		log.write("*BcsJobGenerarCertificadosTributarios* Inicio de Job");
		running.set(true);
		String Estado="Procesando";
		
		try {
			List<Rut> p_listaRut = new ArrayList<Rut>();
			
			/*
			Long idEmpresa = 1L;
			Long idUsuario = 74226L;
			String idModulo = "NOR"; 
			//String listaRutStr ="10411329-K";//389,387
			String listaRutStr ="";//389,387
			String IdTipoDocumento="CERT43";
			String VersionArchivo="2023";
			Long IdSolicitudCircular =3526L;
			Long NumeroTramo =0L;
			double CantidadTramos=Double.parseDouble("0.0"); 
			String EsquemaDJ="DJ1922";
			String VersionDJ="2023";
			*/
			
		Long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			Long idUsuario = Long.parseLong(this.getParam("IdUsuario"));
			String idModulo = this.getParam("IdModulo"); 
			String listaRutStr = this.getParam("ListaRut");
			String IdTipoDocumento=this.getParam("IdTipoDocumento");
			String VersionArchivo=this.getParam("VersionArchivo");
			Long IdSolicitudCircular=Long.parseLong(this.getParam("IdSolicitudCircular"));
			double CantidadTramos=Double.parseDouble("0.0"); 
			try {
				  CantidadTramos =Double.parseDouble(this.getParam("CantidadTramos"));
			    } catch (Exception e) {
				  CantidadTramos=Double.parseDouble("0.0");
			}	
			Long NumeroTramo =0L; 
			
			try {
				    NumeroTramo =Long.parseLong(this.getParam("NumeroTramo"));
			    } catch (Exception e) {
					NumeroTramo=0L; 
			}	
			String EsquemaDJ =""; 
			try {
				EsquemaDJ =this.getParam("Esquema");
			    } catch (Exception e) {
			    EsquemaDJ=""; 
			}			
					
			String VersionDJ =""; 
			try {
				VersionDJ =this.getParam("Version");
			    } catch (Exception e) {
			    	VersionDJ=""; 
			}	
			
			PreparedStatement pstmt = con.prepareStatement(rutSolicitud);
			pstmt.setLong(1, IdSolicitudCircular);
			pstmt.setLong(2, IdSolicitudCircular);
			ResultSet rs = pstmt.executeQuery();
			long contador = 0;
			while (rs.next()) {
				contador++;
				Rut Rut = new Rut();
				Rut.Rut =rs.getString("Rut").replace("\r", "");
				p_listaRut.add(Rut);	
			};
			if (contador==0){
				String arrRut [] = listaRutStr.split(",");
				for (String id : arrRut) {
					Rut Rut = new Rut();
					Rut.Rut =  id;
					if (id != ""){
						log.write(Rut.toString());
						p_listaRut.add(Rut);	
					};
								
				}
			}
			if (contador>0){Estado="*Procesando";};
			SQL sql = new SQL();
			log.write( "*BcsJobGenerarCertificadosTributarios* Parametros de entrada");
			log.write( "*BcsJobGenerarCertificadosTributarios* p_IdEmpresa: " + idEmpresa);
	        log.write( "*BcsJobGenerarCertificadosTributarios* p_IdUsuario: " + idUsuario);
	        log.write( "*BcsJobGenerarCertificadosTributarios* p_IdModulo: " + idModulo);
	        log.write( "*BcsJobGenerarCertificadosTributarios* p_IdTipoDocumento: " + IdTipoDocumento);
	        log.write( "*BcsJobGenerarCertificadosTributarios* p_VersionArchivo: " + VersionArchivo);
	        
					
			log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida tributario");
	        List<ResumenGeneracion> prog = new ArrayList<ResumenGeneracion>();
			prog.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
			
			
			List<ResResumen> resResumen =new ArrayList<ResResumen>();
			HashMap<String, Object> par = new HashMap<>(2);
			par.put("IdPrograma", this.getJobId());
			resResumen = new SQL().query(con, SQL_SELECT_RESUMEN_TRIBUTARIO, par, ResResumen.class); //GRUPO

			for (ResResumen r: resResumen) {
				
				if (r.count == 0L){
					
					log.write( "*BcsJobGenerarGenerarCertificadosTributarios* iNSERT  RESUMEN tributario");
			        List<ResumenDJ> prog2 = new ArrayList<ResumenDJ>();
					prog2.add(new ResumenDJ(this.getJobId(), "","ASIGNADA",0L,EsquemaDJ,VersionDJ,idEmpresa,idUsuario));
		        	sql.batch(con, SQL_INSERT_RESUMEN_TRIBUTARIO, prog2,ResumenDJ.class);
		        	con.commit();
				};;
							
			}
			
        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, prog,ResumenGeneracion.class);
        	con.commit();
        	
        	
        	
        	//INICIO TRAMOS
        	List<RutParticion> rutParticion =new ArrayList<RutParticion>();
        	
        	Entrada ent=new Entrada();
			ent.CantidadTramos=CantidadTramos;
			ent.idEmpresa=idEmpresa;
			ent.idModulo=idModulo;
			ent.IdSolicitudCircular=IdSolicitudCircular;
			ent.IdTipoDocumento=IdTipoDocumento;
			ent.idUsuario=idUsuario;
			ent.listaRutStr=listaRutStr;
			ent.NumeroTramo=NumeroTramo;
			ent.p_listaRut=p_listaRut;
			ent.VersionArchivo=VersionArchivo;
			ent.EsquemaDJ=EsquemaDJ;
			ent.VersionDJ=VersionDJ;
			
        	String nombreZip= "TRI_" + IdTipoDocumento ;
        	if (p_listaRut.size()==0) {
				String cantidadPDF="";
				
		    	try {
		    		//parametro de cantidad de clientes x tramo
		    		cantidadPDF=ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "NORMATIVA", "TRI_" + IdTipoDocumento + "_CANTIDAD_CERTIFICADOS_PDF");
					if(cantidadPDF.length()==0)
						{
						cantidadPDF="0";
						};
					} catch (Exception e) {
						cantidadPDF="0";
					}	
				
				    //PARAMETRO DE TRAMOS
		    	
				   if (cantidadPDF != "0") 
				   {
					   log.write( "*BcsJobGenerarCertificadosTributarios* Generacion Activa x tramos, valor parametro :" + cantidadPDF);
					  
						
						HashMap<String, Object> params = new HashMap<>(2);
						params.put("VersionArchivo", VersionArchivo);
						params.put("IdTipoDocumento", IdTipoDocumento);
						
						params.put("cantidadPDF", cantidadPDF);
						switch( IdTipoDocumento ){
						case "CERT21":
							rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF21, params, RutParticion.class); //GRUPO
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_SELECT_GRUPO_CERTIF21");
							//Validacion si es continuacion o es el inicial de la particion
							if (rutParticion.size()==0 && NumeroTramo==0L) {
								List<VersionCertificado> certact = new ArrayList<VersionCertificado>();
								certact.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"ACT"));
								sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO, certact,VersionCertificado.class); //ACTIVOS
								log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO");
								con.commit();
								rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF21, params, RutParticion.class); //NEW GRUPO
							};
							
							List<VersionCertificado> cert = new ArrayList<VersionCertificado>();
							cert.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"INA"));
							sql.batch(con, SQL_UPDATE_GRUPO_CERTIF21_INACTIVO, cert,VersionCertificado.class); //INACTIVOS SIN DETALLE
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_GRUPO_CERTIF21_INACTIVO");
							
							
							List<CertificadoEstadoRut> certEstadoRut = new ArrayList<CertificadoEstadoRut>();
							for (Rut rut: rutParticion) {
							
								if (rut.Rut != ""){
									log.write(rut.Rut.toString());
									certEstadoRut.add(new CertificadoEstadoRut(IdTipoDocumento, VersionArchivo,"ASI",rut.Rut));
									
								};;
									
							}
							sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO, certEstadoRut,CertificadoEstadoRut.class); //ESTADOS ASIGNADOS
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
							
							
							
							
				        	con.commit();
							break;
						case "CERT9":
							rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF9, params, RutParticion.class); //GRUPO
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_SELECT_GRUPO_CERTIF9");
							//Validacion si es continuacion o es el inicial de la particion
							if (rutParticion.size()==0 && NumeroTramo==0L) {
								List<VersionCertificado> certact = new ArrayList<VersionCertificado>();
								certact.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"ACT"));
								sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO, certact,VersionCertificado.class); //ACTIVOS
								log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO");
								con.commit();
								rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF9, params, RutParticion.class); //NEW GRUPO
							};
							
							List<VersionCertificado> cert9 = new ArrayList<VersionCertificado>();
							cert9.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"INA"));
							sql.batch(con, SQL_UPDATE_GRUPO_CERTIF9_INACTIVO, cert9,VersionCertificado.class); //INACTIVOS SIN DETALLE
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_GRUPO_CERTIF9_INACTIVO");
							
							
							List<CertificadoEstadoRut> cert9EstadoRut = new ArrayList<CertificadoEstadoRut>();
							for (Rut rut: rutParticion) {
							
								if (rut.Rut != ""){
									log.write(rut.Rut.toString());
									cert9EstadoRut.add(new CertificadoEstadoRut(IdTipoDocumento, VersionArchivo,"ASI",rut.Rut));
									
								};;
									
							}
							sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO, cert9EstadoRut,CertificadoEstadoRut.class); //ESTADOS ASIGNADOS
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
							
							con.commit();
							break;
						case "CERT17":
							rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF17, params, RutParticion.class); //GRUPO
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_SELECT_GRUPO_CERTIF17");
							//Validacion si es continuacion o es el inicial de la particion
							if (rutParticion.size()==0 && NumeroTramo==0L) {
								List<VersionCertificado> certact = new ArrayList<VersionCertificado>();
								certact.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"ACT"));
								sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO, certact,VersionCertificado.class); //ACTIVOS
								log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO");
								con.commit();
								rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF17, params, RutParticion.class); //NEW GRUPO
							};
							
							List<VersionCertificado> cert17 = new ArrayList<VersionCertificado>();
							cert17.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"INA"));
							sql.batch(con, SQL_UPDATE_GRUPO_CERTIF17_INACTIVO, cert17,VersionCertificado.class); //INACTIVOS SIN DETALLE
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_GRUPO_CERTIF17_INACTIVO");
							
							
							List<CertificadoEstadoRut> cert17EstadoRut = new ArrayList<CertificadoEstadoRut>();
							for (Rut rut: rutParticion) {
							
								if (rut.Rut != ""){
									log.write(rut.Rut.toString());
									cert17EstadoRut.add(new CertificadoEstadoRut(IdTipoDocumento, VersionArchivo,"ASI",rut.Rut));
									
								};;
									
							}
							sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO, cert17EstadoRut,CertificadoEstadoRut.class); //ESTADOS ASIGNADOS
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
							
							con.commit();
							break;
						case "CERT24":
							rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF24, params, RutParticion.class); //GRUPO
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_SELECT_GRUPO_CERTIF24");
							//Validacion si es continuacion o es el inicial de la particion
							if (rutParticion.size()==0 && NumeroTramo==0L) {
								List<VersionCertificado> certact = new ArrayList<VersionCertificado>();
								certact.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"ACT"));
								sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO, certact,VersionCertificado.class); //ACTIVOS
								log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO");
								con.commit();
								rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF24, params, RutParticion.class); //NEW GRUPO
							};
							
							List<VersionCertificado> cert24 = new ArrayList<VersionCertificado>();
							cert24.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"INA"));
							sql.batch(con, SQL_UPDATE_GRUPO_CERTIF24_INACTIVO, cert24,VersionCertificado.class); //INACTIVOS SIN DETALLE
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_GRUPO_CERTIF24_INACTIVO");
							
							
							List<CertificadoEstadoRut> cert24EstadoRut = new ArrayList<CertificadoEstadoRut>();
							for (Rut rut: rutParticion) {
							
								if (rut.Rut != ""){
									log.write(rut.Rut.toString());
									cert24EstadoRut.add(new CertificadoEstadoRut(IdTipoDocumento, VersionArchivo,"ASI",rut.Rut));
									
								};;
									
							}
							sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO, cert24EstadoRut,CertificadoEstadoRut.class); //ESTADOS ASIGNADOS
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
							
							con.commit();
							break;
						case "CERT36":
							rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF36, params, RutParticion.class); //GRUPO
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_SELECT_GRUPO_CERTIF36");
							//Validacion si es continuacion o es el inicial de la particion
							if (rutParticion.size()==0 && NumeroTramo==0L) {
								List<VersionCertificado> certact = new ArrayList<VersionCertificado>();
								certact.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"ACT"));
								sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO, certact,VersionCertificado.class); //ACTIVOS
								log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO");
								con.commit();
								rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF36, params, RutParticion.class); //NEW GRUPO
							};
							
							List<VersionCertificado> cert36 = new ArrayList<VersionCertificado>();
							cert36.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"INA"));
							sql.batch(con, SQL_UPDATE_GRUPO_CERTIF36_INACTIVO, cert36,VersionCertificado.class); //INACTIVOS SIN DETALLE
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_GRUPO_CERTIF36_INACTIVO");
							
							
							List<CertificadoEstadoRut> cert36EstadoRut = new ArrayList<CertificadoEstadoRut>();
							for (Rut rut: rutParticion) {
							
								if (rut.Rut != ""){
									log.write(rut.Rut.toString());
									cert36EstadoRut.add(new CertificadoEstadoRut(IdTipoDocumento, VersionArchivo,"ASI",rut.Rut));
									
								};;
									
							}
							sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO, cert36EstadoRut,CertificadoEstadoRut.class); //ESTADOS ASIGNADOS
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
							
							con.commit();
							break;
						case "CERT43":
							rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF43, params, RutParticion.class); //GRUPO
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_SELECT_GRUPO_CERTIF43");
							//Validacion si es continuacion o es el inicial de la particion
							if (rutParticion.size()==0 && NumeroTramo==0L) {
								List<VersionCertificado> certact = new ArrayList<VersionCertificado>();
								certact.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"ACT"));
								sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO, certact,VersionCertificado.class); //ACTIVOS
								log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO");
								con.commit();
								rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF43, params, RutParticion.class); //NEW GRUPO
							};
							
							List<VersionCertificado> cert43 = new ArrayList<VersionCertificado>();
							cert43.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"INA"));
							sql.batch(con, SQL_UPDATE_GRUPO_CERTIF43_INACTIVO, cert43,VersionCertificado.class); //INACTIVOS SIN DETALLE
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_GRUPO_CERTIF43_INACTIVO");
							
							
							List<CertificadoEstadoRut> cert43EstadoRut = new ArrayList<CertificadoEstadoRut>();
							for (Rut rut: rutParticion) {
							
								if (rut.Rut != ""){
									log.write(rut.Rut.toString());
									cert43EstadoRut.add(new CertificadoEstadoRut(IdTipoDocumento, VersionArchivo,"ASI",rut.Rut));
									
								};;
									
							}
							sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO, cert43EstadoRut,CertificadoEstadoRut.class); //ESTADOS ASIGNADOS
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
							
							con.commit();
							break;
						case "CERT71":
							rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF71, params, RutParticion.class); //GRUPO
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_SELECT_GRUPO_CERTIF71");
							//Validacion si es continuacion o es el inicial de la particion
							if (rutParticion.size()==0 && NumeroTramo==0L) {
								List<VersionCertificado> certact = new ArrayList<VersionCertificado>();
								certact.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"ACT"));
								sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO, certact,VersionCertificado.class); //ACTIVOS
								log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO_ACTIVO");
								con.commit();
								rutParticion = new SQL().query(con, SQL_SELECT_GRUPO_CERTIF71, params, RutParticion.class); //NEW GRUPO
							};
							
							List<VersionCertificado> cert71 = new ArrayList<VersionCertificado>();
							cert71.add(new VersionCertificado(IdTipoDocumento, VersionArchivo,"INA"));
							sql.batch(con, SQL_UPDATE_GRUPO_CERTIF71_INACTIVO, cert71,VersionCertificado.class); //INACTIVOS SIN DETALLE
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_GRUPO_CERTIF71_INACTIVO");
							
							
							List<CertificadoEstadoRut> cert71EstadoRut = new ArrayList<CertificadoEstadoRut>();
							for (Rut rut: rutParticion) {
							
								if (rut.Rut != ""){
									log.write(rut.Rut.toString());
									cert71EstadoRut.add(new CertificadoEstadoRut(IdTipoDocumento, VersionArchivo,"ASI",rut.Rut));
									
								};;
									
							}
							sql.batch(con, SQL_UPDATE_CERTIFICADO_ESTADO, cert71EstadoRut,CertificadoEstadoRut.class); //ESTADOS ASIGNADOS
							log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
							
							con.commit();
							break;
						default: 
							 log.write( "Ningun certificado cargado para tramos");
							 break;
				        }	
						//log.write( "*ruts: " + rutParticion.toString());
					
				
						if (rutParticion.size()>0){
							
							long cantidadrut= rutParticion.get(0).Cantidad;
							log.write( "*BcsJobGenerarCertificadosTributarios* tramos - , Cant. Ruts :" + Long.toString(cantidadrut) );
							if (CantidadTramos==Double.parseDouble("0.0"))
							{
								float div=(float)cantidadrut / Long.parseLong(cantidadPDF);//redondear hacia arriba	
								CantidadTramos=Math.ceil((double)div);
								ent.CantidadTramos=CantidadTramos;
							}
							log.write( "*BcsJobGenerarCertificadosTributarios* tramos - , Cant. Tramos :" + Long.toString((long)CantidadTramos) );
							for (RutParticion rut: rutParticion) {
								Rut Rut = new Rut();
								Rut.Rut =  rut.Rut; 
								p_listaRut.add(Rut);				
							}
						}
					
						//nombre de tramos
						if (NumeroTramo ==0L) {NumeroTramo=1L;}else{NumeroTramo =NumeroTramo+1;};
						if (CantidadTramos ==Double.parseDouble("0.0")) {CantidadTramos=Double.parseDouble("1");};
						
						nombreZip=nombreZip + "_PT" + NumeroTramo + "de" + (long)CantidadTramos;
						ent.CantidadTramos=CantidadTramos;
						ent.NumeroTramo=NumeroTramo;
						log.write( "*BcsJobGenerarCertificadosTributarios* tramos - ,nombreZip :" + nombreZip );
				   };
        	};//fin p_listaRut  
        	
		    //FIN TRAMOS
        	
			nombreZip = nombreZip + "_";
			switch( IdTipoDocumento ){
			
				case "CERT21":
					
				    GenerarCertificado21Masivo certificadosMasivoCert21 = new GenerarCertificado21Masivo();
					EntradaGenerarCertificado21Masivo entradaCert21 = new EntradaGenerarCertificado21Masivo();
					SalidaGenerarCertificado21Masivo salidaCert21 = new SalidaGenerarCertificado21Masivo();
					
					entradaCert21.p_IdEmpresa= idEmpresa;
					entradaCert21.p_IdUsuario =idUsuario;
					entradaCert21.p_IdModulo= idModulo;
					entradaCert21.p_IdTipoDocumento= IdTipoDocumento;
					entradaCert21.p_VersionArchivo= VersionArchivo;
					entradaCert21.p_IdProceso=this.getJobId();
					entradaCert21.p_listaRut=p_listaRut;
					entradaCert21.p_DataSource = super.getDataSource();
					entradaCert21.p_NombreZIP=nombreZip;
					
					log.write( "*GenerarCertificadosTributarios* Se ejecuta llamada");
					salidaCert21 = certificadosMasivoCert21.logica(con, entradaCert21);
					
					log.write( "*BcsJobGenerarCertificadosTributarios* entrada.p_listaRut: ",listaRutStr);
			        List<GenerarCertificado21Masivo.ProgramaBatch> programaBatchCert21 = new ArrayList<GenerarCertificado21Masivo.ProgramaBatch>();
			        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert21.P_OUT_ERRORESCONTADOR);
			        if(salidaCert21.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida batch");
			        	programaBatchCert21.add(new GenerarCertificado21Masivo.ProgramaBatch(this.getJobId(), salidaCert21.p_out_RutaZip));
			        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatchCert21,GenerarCertificado21Masivo.ProgramaBatch.class);
			        }
			        
			        List<ResumenGeneracion> programaTribCert21 = new ArrayList<ResumenGeneracion>();
			        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert21.P_OUT_ERRORESCONTADOR);
			        if(salidaCert21.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida tributario");
			        	if (contador>0){Estado="*Finalizada";}else{Estado="Finalizada";};
			        	programaTribCert21.add(new ResumenGeneracion(this.getJobId(), salidaCert21.p_out_NombreZip,Estado,salidaCert21.p_out_contadorArchivos));
			        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert21,ResumenGeneracion.class);
			        	con.commit();
			        }else{
			        	
			        			for (MensajeSalida s : salidaCert21.P_OUT_MENSAJES) {
			        				Estado=s.descripcion;
			        			}
			        	programaTribCert21.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
			        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert21,ResumenGeneracion.class);
			        	con.commit();
			        };
			        
					break;
			       
					case "CERT9":
					
					List<GenerarCertificado9Masivo.Rut> ListaRutCert9 = new ArrayList<GenerarCertificado9Masivo.Rut>();
				    GenerarCertificado9Masivo certificadosMasivoCert9 = new GenerarCertificado9Masivo();
					EntradaGenerarCertificado9Masivo entradaCert9 = new EntradaGenerarCertificado9Masivo();
					SalidaGenerarCertificado9Masivo salidaCert9 = new SalidaGenerarCertificado9Masivo();
					
					for (Rut l :p_listaRut) {
						GenerarCertificado9Masivo.Rut aux = new GenerarCertificado9Masivo.Rut();
						aux.Rut=l.Rut;
						ListaRutCert9.add(aux);
					}

					entradaCert9.p_IdEmpresa= idEmpresa;
					entradaCert9.p_IdUsuario =idUsuario;
					entradaCert9.p_IdModulo= idModulo;
					entradaCert9.p_IdTipoDocumento= IdTipoDocumento;
					entradaCert9.p_VersionArchivo= VersionArchivo;
					entradaCert9.p_IdProceso=this.getJobId();
					entradaCert9.p_listaRut=ListaRutCert9;
					entradaCert9.p_DataSource = super.getDataSource();
					entradaCert9.p_NombreZIP=nombreZip;
					
					log.write( "*GenerarCertificadosTributarios* Se ejecuta llamada");
					salidaCert9 = certificadosMasivoCert9.logica(con, entradaCert9);
					
					log.write( "*BcsJobGenerarCertificadosTributarios* entrada.p_listaRut: ",listaRutStr);
			        List<GenerarCertificado9Masivo.ProgramaBatch> programaBatchCert9 = new ArrayList<GenerarCertificado9Masivo.ProgramaBatch>();
			        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert9.P_OUT_ERRORESCONTADOR);
			        if(salidaCert9.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida batch");
			        	programaBatchCert9.add(new GenerarCertificado9Masivo.ProgramaBatch(this.getJobId(), salidaCert9.p_out_RutaZip));
			        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatchCert9,GenerarCertificado9Masivo.ProgramaBatch.class);
			        }
			        
			        List<ResumenGeneracion> programaTribCert9 = new ArrayList<ResumenGeneracion>();
			        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert9.P_OUT_ERRORESCONTADOR);
			        if(salidaCert9.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida tributario");
			        	if (contador>0){Estado="*Finalizada";}else{Estado="Finalizada";};
			        	programaTribCert9.add(new ResumenGeneracion(this.getJobId(), salidaCert9.p_out_NombreZip,Estado,salidaCert9.p_out_contadorArchivos));
			        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert9,ResumenGeneracion.class);
			        	con.commit();
			        }else{
			        	
			        			for (MensajeSalida s : salidaCert9.P_OUT_MENSAJES) {
			        				Estado=s.descripcion;
			        			}
			        	programaTribCert9.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
			        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert9,ResumenGeneracion.class);
			        	con.commit();
			        };
			        
					break;

					case "CERT17":
						
						List<GenerarCertificado17Masivo.Rut> ListaRutCert17 = new ArrayList<GenerarCertificado17Masivo.Rut>();
					    GenerarCertificado17Masivo certificadosMasivoCert17 = new GenerarCertificado17Masivo();
						EntradaGenerarCertificado17Masivo entradaCert17 = new EntradaGenerarCertificado17Masivo();
						SalidaGenerarCertificado17Masivo salidaCert17 = new SalidaGenerarCertificado17Masivo();
						
						for (Rut l :p_listaRut) {
							GenerarCertificado17Masivo.Rut aux = new GenerarCertificado17Masivo.Rut();
							aux.Rut=l.Rut;
							ListaRutCert17.add(aux);
						}
						
						entradaCert17.p_IdEmpresa= idEmpresa;
						entradaCert17.p_IdUsuario =idUsuario;
						entradaCert17.p_IdModulo= idModulo;
						entradaCert17.p_IdTipoDocumento= IdTipoDocumento;
						entradaCert17.p_VersionArchivo= VersionArchivo;
						entradaCert17.p_IdProceso=this.getJobId();
						entradaCert17.p_listaRut=ListaRutCert17;
						entradaCert17.p_DataSource = super.getDataSource();
						entradaCert17.p_NombreZIP=nombreZip;
						
						log.write( "*GenerarCertificadosTributarios* Se ejecuta llamada");
						salidaCert17 = certificadosMasivoCert17.logica(con, entradaCert17);
						
						log.write( "*BcsJobGenerarCertificadosTributarios* entrada.p_listaRut: ",listaRutStr);
				        List<GenerarCertificado17Masivo.ProgramaBatch> programaBatchCert17 = new ArrayList<GenerarCertificado17Masivo.ProgramaBatch>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert17.P_OUT_ERRORESCONTADOR);
				        if(salidaCert17.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida batch");
				        	programaBatchCert17.add(new GenerarCertificado17Masivo.ProgramaBatch(this.getJobId(), salidaCert17.p_out_RutaZip));
				        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatchCert17,GenerarCertificado17Masivo.ProgramaBatch.class);
				        }
				        
				        List<ResumenGeneracion> programaTribCert17 = new ArrayList<ResumenGeneracion>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert17.P_OUT_ERRORESCONTADOR);
				        if(salidaCert17.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida tributario");
				        	if (contador>0){Estado="*Finalizada";}else{Estado="Finalizada";};
				        	programaTribCert17.add(new ResumenGeneracion(this.getJobId(), salidaCert17.p_out_NombreZip,Estado,salidaCert17.p_out_contadorArchivos));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert17,ResumenGeneracion.class);
				        	con.commit();
				        }else{
				        	
				        			for (MensajeSalida s : salidaCert17.P_OUT_MENSAJES) {
				        				Estado=s.descripcion;
				        			}
				        	programaTribCert17.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert17,ResumenGeneracion.class);
				        	con.commit();
				        };
				        
						break;
						
						
					case "CERT24":
						
						List<GenerarCertificado24Masivo.Rut> ListaRutCert24 = new ArrayList<GenerarCertificado24Masivo.Rut>();
					    GenerarCertificado24Masivo certificadosMasivoCert24 = new GenerarCertificado24Masivo();
						EntradaGenerarCertificado24Masivo entradaCert24 = new EntradaGenerarCertificado24Masivo();
						SalidaGenerarCertificado24Masivo salidaCert24 = new SalidaGenerarCertificado24Masivo();
						
						for (Rut l :p_listaRut) {
							GenerarCertificado24Masivo.Rut aux = new GenerarCertificado24Masivo.Rut();
							aux.Rut=l.Rut;
							ListaRutCert24.add(aux);
						}
						
						entradaCert24.p_IdEmpresa= idEmpresa;
						entradaCert24.p_IdUsuario =idUsuario;
						entradaCert24.p_IdModulo= idModulo;
						entradaCert24.p_IdTipoDocumento= IdTipoDocumento;
						entradaCert24.p_VersionArchivo= VersionArchivo;
						entradaCert24.p_IdProceso=this.getJobId();
						entradaCert24.p_listaRut=ListaRutCert24;
						entradaCert24.p_DataSource = super.getDataSource();
						entradaCert24.p_NombreZIP=nombreZip;
						
						log.write( "*GenerarCertificadosTributarios* Se ejecuta llamada");
						salidaCert24 = certificadosMasivoCert24.logica(con, entradaCert24);
						
						log.write( "*BcsJobGenerarCertificadosTributarios* entrada.p_listaRut: ",listaRutStr);
				        List<GenerarCertificado24Masivo.ProgramaBatch> programaBatchCert24 = new ArrayList<GenerarCertificado24Masivo.ProgramaBatch>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert24.P_OUT_ERRORESCONTADOR);
				        if(salidaCert24.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida batch");
				        	programaBatchCert24.add(new GenerarCertificado24Masivo.ProgramaBatch(this.getJobId(), salidaCert24.p_out_RutaZip));
				        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatchCert24,GenerarCertificado24Masivo.ProgramaBatch.class);
				        }
				        
				        List<ResumenGeneracion> programaTribCert24 = new ArrayList<ResumenGeneracion>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert24.P_OUT_ERRORESCONTADOR);
				        if(salidaCert24.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida tributario");
				        	if (contador>0){Estado="*Finalizada";}else{Estado="Finalizada";};
				        	programaTribCert24.add(new ResumenGeneracion(this.getJobId(), salidaCert24.p_out_NombreZip,Estado,salidaCert24.p_out_contadorArchivos));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert24,ResumenGeneracion.class);
				        	con.commit();
				        }else{
				        	
				        			for (MensajeSalida s : salidaCert24.P_OUT_MENSAJES) {
				        				Estado=s.descripcion;
				        			}
				        	programaTribCert24.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert24,ResumenGeneracion.class);
				        	con.commit();
				        };
				        
						break;						

					case "CERT36":
						
						List<GenerarCertificado36Masivo.Rut> ListaRutCert36 = new ArrayList<GenerarCertificado36Masivo.Rut>();
					    GenerarCertificado36Masivo certificadosMasivoCert36 = new GenerarCertificado36Masivo();
						EntradaGenerarCertificado36Masivo entradaCert36 = new EntradaGenerarCertificado36Masivo();
						SalidaGenerarCertificado36Masivo salidaCert36 = new SalidaGenerarCertificado36Masivo();
						
						for (Rut l :p_listaRut) {
							GenerarCertificado36Masivo.Rut aux = new GenerarCertificado36Masivo.Rut();
							aux.Rut=l.Rut;
							ListaRutCert36.add(aux);
						}
						
						entradaCert36.p_IdEmpresa= idEmpresa;
						entradaCert36.p_IdUsuario =idUsuario;
						entradaCert36.p_IdModulo= idModulo;
						entradaCert36.p_IdTipoDocumento= IdTipoDocumento;
						entradaCert36.p_VersionArchivo= VersionArchivo;
						entradaCert36.p_IdProceso=this.getJobId();
						entradaCert36.p_listaRut=ListaRutCert36;
						entradaCert36.p_DataSource = super.getDataSource();
						entradaCert36.p_NombreZIP=nombreZip;
						
						log.write( "*GenerarCertificadosTributarios* Se ejecuta llamada");
						salidaCert36 = certificadosMasivoCert36.logica(con, entradaCert36);
						
						log.write( "*BcsJobGenerarCertificadosTributarios* entrada.p_listaRut: ",listaRutStr);
				        List<GenerarCertificado36Masivo.ProgramaBatch> programaBatchCert36 = new ArrayList<GenerarCertificado36Masivo.ProgramaBatch>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert36.P_OUT_ERRORESCONTADOR);
				        if(salidaCert36.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida batch");
				        	programaBatchCert36.add(new GenerarCertificado36Masivo.ProgramaBatch(this.getJobId(), salidaCert36.p_out_RutaZip));
				        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatchCert36,GenerarCertificado36Masivo.ProgramaBatch.class);
				        }
				        
				        List<ResumenGeneracion> programaTribCert36 = new ArrayList<ResumenGeneracion>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert36.P_OUT_ERRORESCONTADOR);
				        if(salidaCert36.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* Update  salida tributario");
				        	if (contador>0){Estado="*Finalizada";}else{Estado="Finalizada";};
				        	programaTribCert36.add(new ResumenGeneracion(this.getJobId(), salidaCert36.p_out_NombreZip,Estado,salidaCert36.p_out_contadorArchivos));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert36,ResumenGeneracion.class);
				        	con.commit();
				        }else{
				        	
				        			for (MensajeSalida s : salidaCert36.P_OUT_MENSAJES) {
				        				Estado=s.descripcion;
				        			}
				        	programaTribCert36.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert36,ResumenGeneracion.class);
				        	con.commit();
				        };
				        
						break;

					case "CERT43":
						
						List<GenerarCertificado43Masivo.Rut> ListaRutCert43 = new ArrayList<GenerarCertificado43Masivo.Rut>();
					    GenerarCertificado43Masivo certificadosMasivoCert43 = new GenerarCertificado43Masivo();
						EntradaGenerarCertificado43Masivo entradaCert43 = new EntradaGenerarCertificado43Masivo();
						SalidaGenerarCertificado43Masivo salidaCert43 = new SalidaGenerarCertificado43Masivo();
						
						
						for (Rut l :p_listaRut) {
							GenerarCertificado43Masivo.Rut aux = new GenerarCertificado43Masivo.Rut();
							aux.Rut=l.Rut;
							ListaRutCert43.add(aux);
						}
						
						entradaCert43.p_IdEmpresa= idEmpresa;
						entradaCert43.p_IdUsuario =idUsuario;
						entradaCert43.p_IdModulo= idModulo;
						entradaCert43.p_IdTipoDocumento= IdTipoDocumento;
						entradaCert43.p_VersionArchivo= VersionArchivo;
						entradaCert43.p_IdProceso=this.getJobId();
						entradaCert43.p_listaRut=ListaRutCert43;
						entradaCert43.p_DataSource = super.getDataSource();
						entradaCert43.p_NombreZIP=nombreZip;
						
						log.write( "*GenerarCertificadosTributarios* Se ejecuta llamada");
						salidaCert43 = certificadosMasivoCert43.logica(con, entradaCert43);
						
						log.write( "*BcsJobGenerarCertificadosTributarios* entrada.p_listaRut: ",listaRutStr);
				        List<GenerarCertificado43Masivo.ProgramaBatch> programaBatchCert43 = new ArrayList<GenerarCertificado43Masivo.ProgramaBatch>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert43.P_OUT_ERRORESCONTADOR);
				        if(salidaCert43.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida batch");
				        	programaBatchCert43.add(new GenerarCertificado43Masivo.ProgramaBatch(this.getJobId(), salidaCert43.p_out_RutaZip));
				        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatchCert43,GenerarCertificado43Masivo.ProgramaBatch.class);
				        }
				        
				        List<ResumenGeneracion> programaTribCert43 = new ArrayList<ResumenGeneracion>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert43.P_OUT_ERRORESCONTADOR);
				        if(salidaCert43.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida tributario");
				        	if (contador>0){Estado="*Finalizada";}else{Estado="Finalizada";};
				        	programaTribCert43.add(new ResumenGeneracion(this.getJobId(), salidaCert43.p_out_NombreZip,Estado,salidaCert43.p_out_contadorArchivos));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert43,ResumenGeneracion.class);
				        	con.commit();
				        }else{
				        	
				        			for (MensajeSalida s : salidaCert43.P_OUT_MENSAJES) {
				        				Estado=s.descripcion;
				        			}
				        	programaTribCert43.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert43,ResumenGeneracion.class);
				        	con.commit();
				        	throw new Exception(Estado);
				        };
				        
						break;
						
					case "CERT71":
						
						List<GenerarCertificado71Masivo.Rut> ListaRutCert71 = new ArrayList<GenerarCertificado71Masivo.Rut>();
					    GenerarCertificado71Masivo certificadosMasivoCert71 = new GenerarCertificado71Masivo();
						EntradaGenerarCertificado71Masivo entradaCert71 = new EntradaGenerarCertificado71Masivo();
						SalidaGenerarCertificado71Masivo salidaCert71 = new SalidaGenerarCertificado71Masivo();
						
						for (Rut l :p_listaRut) {
							GenerarCertificado71Masivo.Rut aux = new GenerarCertificado71Masivo.Rut();
							aux.Rut=l.Rut;
							ListaRutCert71.add(aux);
						}
						
						entradaCert71.p_IdEmpresa= idEmpresa;
						entradaCert71.p_IdUsuario =idUsuario;
						entradaCert71.p_IdModulo= idModulo;
						entradaCert71.p_IdTipoDocumento= IdTipoDocumento;
						entradaCert71.p_VersionArchivo= VersionArchivo;
						entradaCert71.p_IdProceso=this.getJobId();
						entradaCert71.p_listaRut=ListaRutCert71;
						entradaCert71.p_DataSource = super.getDataSource();
						entradaCert71.p_NombreZIP=nombreZip;
						
						log.write( "*GenerarCertificadosTributarios* Se ejecuta llamada");
						salidaCert71 = certificadosMasivoCert71.logica(con, entradaCert71);
						
						log.write( "*BcsJobGenerarCertificadosTributarios* entrada.p_listaRut: ",listaRutStr);
				        List<GenerarCertificado71Masivo.ProgramaBatch> programaBatchCert71 = new ArrayList<GenerarCertificado71Masivo.ProgramaBatch>();
				        log.write( "*BcsJobGenerarGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert71.P_OUT_ERRORESCONTADOR);
				        if(salidaCert71.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarGenerarCertificadosTributarios* UPdate  salida batch");
				        	programaBatchCert71.add(new GenerarCertificado71Masivo.ProgramaBatch(this.getJobId(), salidaCert71.p_out_RutaZip));
				        	sql.batch(con, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, programaBatchCert71,GenerarCertificado71Masivo.ProgramaBatch.class);
				        }
				        
				        List<ResumenGeneracion> programaTribCert71 = new ArrayList<ResumenGeneracion>();
				        log.write( "*BcsJobGenerarCertificadosTributarios* salida.P_OUT_ERRORESCONTADOR",salidaCert71.P_OUT_ERRORESCONTADOR);
				        if(salidaCert71.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				        	log.write( "*BcsJobGenerarCertificadosTributarios* UPdate  salida tributario");
				        	if (contador>0){Estado="*Finalizada";}else{Estado="Finalizada";};
				        	programaTribCert71.add(new ResumenGeneracion(this.getJobId(), salidaCert71.p_out_NombreZip,Estado,salidaCert71.p_out_contadorArchivos));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert71,ResumenGeneracion.class);
				        	con.commit();
				        }else{
				        	
				        			for (MensajeSalida s : salidaCert71.P_OUT_MENSAJES) {
				        				Estado=s.descripcion;
				        			}
				        	programaTribCert71.add(new ResumenGeneracion(this.getJobId(), "",Estado,0L));
				        	sql.batch(con, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programaTribCert71,ResumenGeneracion.class);
				        	con.commit();
				        };
				        
						break;						
				default: 
					 log.write( "Ningun certificado cargado");
					 break;
		}

	        
			/*inicio tramos*/
			
			if (rutParticion.size()>0){
				
				if (NumeroTramo < CantidadTramos) {
					
					
					System.out.println( "*BcsJobGenerarGenerarCertificadosTributarios* AgregarProceso"  + NumeroTramo + " de " + CantidadTramos );
					SalidaPrograma outProg = new SalidaPrograma();
					outProg.P_OUT_ERRORESCONTADOR = 0L;
					log.write( "*BcsJobGenerarGenerarCertificadosTributarios* AgregarProceso"  + NumeroTramo + " de " + CantidadTramos );
					AgregarProceso(con,ent, outProg);
					
										
				};
				
				
			};
			
			/*fin tramos*/
			
			
	    	log.write( "*BcsJobGenerarCertificadosTributarios* Fin ejecucion de servicio job");
			
			
		}catch(Exception e) {
			log.write("Error", e);
			throw e;
		}finally {
			running.set(false);
		}
		
		running.set(false);
		return true;
		
	}

	@Override
	public void onError(Throwable t) {
		log.write(":ProcesadorTransacciones:  ****** ERROR  *******", t);
        t.printStackTrace();
		
	}

	@Override
	public void pause() {
		
	}
	
	
	
	private boolean AgregarProceso (Connection con, Entrada in, SalidaPrograma out) throws Exception{		
		try {
			log.write("Inicio del proceso ingresar programa");
			System.out.println("MLT::AGREGAR PROGRAMAS INICIO");

			//00 VARIABLES DE SISTEMA
			String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.idEmpresa,"PLATAFORMA", "PLA.FechaSistema");

	
					
								
			//02 INSCRIBIR PROGRAMAS
			List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdEmpresa.NombreParametro 	= "IdEmpresa";
			paramIdEmpresa.Valor			= in.idEmpresa.toString();
			paramsParametros.add(paramIdEmpresa);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdUsuario.NombreParametro 	= "IdUsuario";
			paramIdUsuario.Valor			= in.idUsuario.toString();
			paramsParametros.add(paramIdUsuario);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramidModulo = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramidModulo.NombreParametro 	= "IdModulo";
			paramidModulo.Valor				= in.idModulo.toString();
			paramsParametros.add(paramidModulo);
				
			AgregarPrograma.InAgregarPrograma.Parametro	paramListaRut = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramListaRut.NombreParametro 	= "ListaRut";
			paramListaRut.Valor				= in.listaRutStr;
			paramsParametros.add(paramListaRut);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdTipoDocumento = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdTipoDocumento.NombreParametro 	= "IdTipoDocumento";
			paramIdTipoDocumento.Valor				= in.IdTipoDocumento;
			paramsParametros.add(paramIdTipoDocumento);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramVersionArchivo = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramVersionArchivo.NombreParametro 	= "VersionArchivo";
			paramVersionArchivo.Valor				= in.VersionArchivo;
			paramsParametros.add(paramVersionArchivo);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdSolicitudCircular = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdSolicitudCircular.NombreParametro 	= "IdSolicitudCircular";
			paramIdSolicitudCircular.Valor				= in.IdSolicitudCircular.toString();
			paramsParametros.add(paramIdSolicitudCircular);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramCantidadTramos = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramCantidadTramos.NombreParametro 	= "CantidadTramos";
			paramCantidadTramos.Valor				= String.valueOf(in.CantidadTramos);
			paramsParametros.add(paramCantidadTramos);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramNumeroTramo = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramNumeroTramo.NombreParametro 	= "NumeroTramo";
			paramNumeroTramo.Valor				= String.valueOf(in.NumeroTramo);
			paramsParametros.add(paramNumeroTramo);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramCodigoPrograma = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramCodigoPrograma.NombreParametro 	= "CodigoPrograma";
			paramCodigoPrograma.Valor				= in.IdTipoDocumento.replace("CERT", "CERTIFICADO");
			paramsParametros.add(paramCodigoPrograma);
						
			AgregarPrograma.InAgregarPrograma.Parametro	paramEsquemaDJ = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramEsquemaDJ.NombreParametro 	= "Esquema";
			paramEsquemaDJ.Valor				= in.EsquemaDJ;
			paramsParametros.add(paramEsquemaDJ);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramVersionDJ = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramVersionDJ.NombreParametro 	= "Version";
			paramVersionDJ.Valor				= in.VersionDJ;
			paramsParametros.add(paramVersionDJ);
						
					
			System.out.println("MLT:: PROGRAMA NUEVO NumeroTramo ::"+ String.valueOf(in.NumeroTramo));
			System.out.println("MLT:: PROGRAMA NUEVA CantidadTramos ::"+ String.valueOf(in.CantidadTramos));
			

			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
			paramsIn.p_IdEmpresa		= in.idEmpresa;
			paramsIn.p_IdModulo			= in.idModulo;
			paramsIn.p_IdUsuario		= in.idUsuario;
			paramsIn.p_IdTipoPrograma	= "CERTIFICADO_TRIBUTARIO_MASIVO";
			paramsIn.p_Segundos			= 0L;
			paramsIn.p_FechaSistema		= FechaSistema; 
			paramsIn.p_Parametros		= paramsParametros;
			AgregarPrograma AP = new AgregarPrograma();
			AgregarPrograma.OutAgregarPrograma result = AP.logica(con, paramsIn);
			
			out.P_OUT_IDPROGRAMA		= result.P_OUT_IDPROGRAMA;
			out.P_OUT_ERRORESCONTADOR 	= result.P_OUT_ERRORESCONTADOR;
			System.out.println("MLT::AGREGAR PROGRAMAS FIN");
			if ( out.P_OUT_ERRORESCONTADOR.equals(1L) ){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, result.P_OUT_MENSAJE, new HashMap<>() ,con, in.idEmpresa));
			} else {
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "PROGRAMA_INGRESADO", new HashMap<String,String>() ,con,in.idEmpresa));
			};
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write("== ERROR ==",e);
			throw e;
		}		
		return true;
	};
	
	
	
	
	
	public static void main(String[] args) throws SQLException, Exception {
		//Connection con = PlataformaUtils.getDESAConnection();
		try(Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")){
	    	BcsJobGenerarCertificadosTributarios p = new BcsJobGenerarCertificadosTributarios();
	        try {
	            p.transaction(con);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        } catch (Throwable e) {
	            e.printStackTrace();
	        }
		}
	
	}
	
	

}
