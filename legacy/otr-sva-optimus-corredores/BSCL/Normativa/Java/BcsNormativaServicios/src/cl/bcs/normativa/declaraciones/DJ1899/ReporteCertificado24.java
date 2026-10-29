package cl.bcs.normativa.declaraciones.DJ1899;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ReporteCertificado24 implements JavaCall {
	static final Logger logger = LogManager.getLogger(ReporteCertificado24.class.getName());
	
	
	public static class DatosCorredor{
		public String 		Cor_RazonSocial;
		public Long 		Cor_IdEmpresa;
		public String 		Cor_Direccion;
		public String 		Cor_Rut;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosCorredor() {
			this.Cor_RazonSocial = "";
			this.Cor_IdEmpresa = 0L;
			this.Cor_Direccion = "";
			this.Cor_Rut = "";
			
		};
	}
	
	public static class DatosRepresentante{
		public String 		Rep_Rut;
		public String 		Rep_Nombre;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosRepresentante() {
			this.Rep_Rut = "";
			this.Rep_Nombre = "";
		};
	}
	public static class DatosCertificado{
		public Long 		IdCertificadoCliente;
	
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosCertificado() {
			this.IdCertificadoCliente =0L;
			
		};
	}
	
	
	public static class FechaCertificado{
		public String 		DefineFechaCertificado;
		public String 		FechaCertificado;
		
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public FechaCertificado() {};
	}
	
	public static class ExisteTabRut extends BcsPojo{
		public Long count;
	}
	
	public static class DatosReporte{ //SON LOS CAMPOS DE DETALLE EN EL SP DEL CRYSTAL "BSCL.Normativa.CrystalReports.Servicios::Certificado24" v_DatosDetalle
		public Long Folio;
		public Long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;

		public String NombreEntidadRecaudadora;
		public String DocumentoIdentidadEntidad;	  
		public String DireccionEntidadRecaudadora;
		public Long IdCliente;
		public String NombreCompletoCliente;
		public String DocumentoIdentidadCliente;
		public String DireccionCliente;
		public String RazonSocialEmpresa;
		public String DocumentoIdentidadEmpresa;
		public String DireccionEmpresa;
		public String CiudadFecha;	
		public int Mes;
		public String DescripcionMes;
		public BigDecimal MontoModalidadEmpleador;
		public BigDecimal MontoModalidadTrabajador;
		public BigDecimal MontoCotizacionesObligatorias;
		public BigDecimal MontoCotizacionesVoluntarias;
		public BigDecimal MontoTrabajadoresActivos;
		public BigDecimal MontoPensionados;
		public BigDecimal MontoRetencion;
		public String Certificacion;
		public String EstadoCertificado;
		public Long ActivoPensionado;
		public BigDecimal AhorrosModalidadIndirectaEmpleador;
		public BigDecimal AhorrosModalidadDirectaTrabajador;
		public BigDecimal CotizacionesModalidadIndirectaEmpleador;
		public BigDecimal CotizacionesModalidadDirectaTrabajador;
		public BigDecimal CotizacionesTrabajadorIndependiente;
		public BigDecimal MontoRetiroPorAhorrosIncisoPrimero;
		public BigDecimal RetencionImpuestoSobreRetiros;
		public BigDecimal MontoRetiroPorAhorrosIncisoSegundo;
		public BigDecimal RetiroEmpleadorActualizado;
		
		public BigDecimal ResumenTrabajadorActivo;
		public BigDecimal ResumenTrabajadorPensionado;

		public BigDecimal TotalActivo;
		public BigDecimal TotalPensionado;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosReporte() {};
	}
	
	
	public static class ReporteCertificado24Entrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public String p_Version;
		
		 public String toString() {
				return Utils.printJson(this, true);
			}
		 
	}
		

	
	public static class ReporteCertificado24Salida extends BcsPojo{

		public Long Folio;
		public Long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;

		public String NombreEntidadRecaudadora;
		public String DocumentoIdentidadEntidad;	  
		public String DireccionEntidadRecaudadora;
		public Long IdCliente;
		public String NombreCompletoCliente;
		public String DocumentoIdentidadCliente;
		public String DireccionCliente;
		public String RazonSocialEmpresa;
		public String DocumentoIdentidadEmpresa;
		public String DireccionEmpresa;
		public String CiudadFecha;	
		public int Mes;
		public String DescripcionMes;
		public BigDecimal MontoModalidadEmpleador;
		public BigDecimal MontoModalidadTrabajador;
		public BigDecimal MontoCotizacionesObligatorias;
		public BigDecimal MontoCotizacionesVoluntarias;
		public BigDecimal MontoTrabajadoresActivos;
		public BigDecimal MontoPensionados;
		public BigDecimal MontoRetencion;
		public String Certificacion;
		public String EstadoCertificado;
		public Long ActivoPensionado;
		public BigDecimal AhorrosModalidadIndirectaEmpleador;
		public BigDecimal AhorrosModalidadDirectaTrabajador;
		public BigDecimal CotizacionesModalidadIndirectaEmpleador;
		public BigDecimal CotizacionesModalidadDirectaTrabajador;
		public BigDecimal CotizacionesTrabajadorIndependiente;
		public BigDecimal MontoRetiroPorAhorrosIncisoPrimero;
		public BigDecimal RetencionImpuestoSobreRetiros;
		public BigDecimal MontoRetiroPorAhorrosIncisoSegundo;
		public BigDecimal RetiroEmpleadorActualizado;
		
		public BigDecimal ResumenTrabajadorActivo;
		public BigDecimal ResumenTrabajadorPensionado;

		public BigDecimal TotalActivo;
		public BigDecimal TotalPensionado;

		public String RazonSocialCorredora;
		public String DireccionCorredora;
		public String DocumentoIdentidadCorredora;

		public String rut_representante;
		public String nombre_representante;
		
		public String RutCertificado;
		
		public ReporteCertificado24Salida() {
			this.Folio = 0L;
		} 
	
	}
	
	
		private static FileLoader loader = new FileLoader();
		private static String SQL_DATOS_CERTIFICADO_CLIENTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1899/sql/ReporteCertificado24_SelectCertificadoCliente.sql");
		
		private static String SQL_DATOS_CORREDOR = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1899/sql/ReporteCertificado24_SelectDatosCorredor.sql");
		private static String SQL_FECHA_CERTIFICADO = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1899/sql/ReporteCertificado24_SelectFechaCertificado.sql");
		private static String SQL_DATOS_REPRESENTANTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1899/sql/ReporteCertificado24_SelectDatosRepresentante.sql");
		private static String SQL_DATOS_REPORTES = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1899/sql/ReporteCertificado24_SelectDatosReporte.sql");
		
		public  final String SQL_EXIST_TABLA_RUT  		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARCERTIFICADO24MASIVO_RUT'";
		
		
		///ESPECIFICO
		private static String SQL_SIN_DATOS_REPORTES = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1899/sql/ReporteCertificado24_SelectMesSinDatosReporte.sql");
		
		
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ReporteCertificado24Entrada in=gson.fromJson(jin.toString() ,ReporteCertificado24Entrada.class);
		List<ReporteCertificado24Salida>  out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public List<ReporteCertificado24Salida> logica(Connection con, ReporteCertificado24Entrada entrada) throws Exception {
		List<ReporteCertificado24Salida> out = new ArrayList<ReporteCertificado24Salida>();
		//out.P_OUT_ERRORESCONTADOR = 0L;
		//out.P_OUT_MENSAJE = "";
		
        try {
	        SQL sql = new SQL();

	        final String fechaSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(con, entrada.p_IdEmpresa), "yyyy-MM-dd");
			
			String Fechacertificado=fechaSistema;
			
	    	HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
						
			List<DatosCorredor> dataCorredor = sql.query(con, SQL_DATOS_CORREDOR, params, DatosCorredor.class);
			DatosCorredor Corredor = new DatosCorredor();
			if (dataCorredor.size()>0) {
				Corredor.Cor_RazonSocial=dataCorredor.get(0).Cor_RazonSocial;
				Corredor.Cor_IdEmpresa=dataCorredor.get(0).Cor_IdEmpresa;
				Corredor.Cor_Direccion=dataCorredor.get(0).Cor_Direccion;
				Corredor.Cor_Rut=dataCorredor.get(0).Cor_Rut;
			}
			
			
			List<DatosRepresentante> dataRepresentante = sql.query(con, SQL_DATOS_REPRESENTANTE, params, DatosRepresentante.class);
			DatosRepresentante Representante = new DatosRepresentante();
			if (dataRepresentante.size()>0) {
				Representante.Rep_Nombre=dataRepresentante.get(0).Rep_Nombre;
				Representante.Rep_Rut=dataRepresentante.get(0).Rep_Rut;
			}
			
			HashMap<String, Object> params2 = new HashMap<String, Object>();
			params2.put("Version", entrada.p_Version);
			List<FechaCertificado> dataFechaCertif = sql.query(con, SQL_FECHA_CERTIFICADO, params2, FechaCertificado.class);//format 2024-03-18
			
			if (dataFechaCertif.size()>0) {
				if (dataFechaCertif.get(0).DefineFechaCertificado.contains("S")) {
					Fechacertificado=dataFechaCertif.get(0).FechaCertificado;
				}	
			}
			
			HashMap<String, Object> paramsCertif = new HashMap<String, Object>();
			paramsCertif.put("Version", entrada.p_Version);
			paramsCertif.put("IdEmpresa", entrada.p_IdEmpresa);
			
			try {
					HashMap<String, Object> par = new HashMap<>(2);
					List<ExisteTabRut> TabRut =new ArrayList<ExisteTabRut>();
					
					TabRut = new SQL().query(con, SQL_EXIST_TABLA_RUT, par, ExisteTabRut.class);
					
					if(TabRut.size()>0) {
						
							List<Long> IdCertificadoCliente = new ArrayList<Long>();
						    List<DatosCertificado> dataCertificado = sql.query(con, SQL_DATOS_CERTIFICADO_CLIENTE, paramsCertif, DatosCertificado.class);
						    for(DatosCertificado in : dataCertificado){
								if(!IdCertificadoCliente.contains(in.IdCertificadoCliente)) 
									IdCertificadoCliente.add(in.IdCertificadoCliente);
							}
						    HashMap<String, Object> paramsReporte = new HashMap<String, Object>();
							paramsReporte.put("Version", entrada.p_Version);
							paramsReporte.put("FechaSistema", Fechacertificado);
							paramsReporte.put("IdEmpresa", entrada.p_IdEmpresa);
							paramsReporte.put("IdCertificadoCliente",IdCertificadoCliente);
						
							List<DatosReporte> dataReporte = sql.query(con, SQL_DATOS_REPORTES, paramsReporte, DatosReporte.class);
							
							if (dataReporte.size()>0) {			
								List<DatosReporte> union = new ArrayList<DatosReporte>();
								union.addAll(dataReporte);
								
								///A)INICIO: ESPECIFICA CERTIF.
								
								  List<DatosReporte> dataMesSindata = sql.query(con, SQL_SIN_DATOS_REPORTES, paramsReporte, DatosReporte.class);
								  union.addAll(dataMesSindata);
								
								///A)FIN: ESPECIFICA CERTIF.								
								
								out=AgregarvaloresSalida(union,Representante,Corredor); // datos reporte
								
							}else { // sin datos : valores
								
								out=obtenervaloresDefecto();
								
							};
					
					}else{
						out=obtenervaloresDefecto();
					};
			}catch (Exception e) {
				e.printStackTrace();
			}
        } catch (SQLException e) {
        	//out.P_OUT_ERRORESCONTADOR = 1L;
			//out.P_OUT_MENSAJE = "";
			e.printStackTrace();
			return out;
		}
        con.commit();
		return out;
		 
	}

	
	private List<ReporteCertificado24Salida> AgregarvaloresSalida(List<DatosReporte> dataReporte,DatosRepresentante Representante,DatosCorredor Corredor )  {
		
		List<ReporteCertificado24Salida> o = new ArrayList<ReporteCertificado24Salida>();
		for (DatosReporte datos : dataReporte) {
			ReporteCertificado24Salida out = new ReporteCertificado24Salida();
			
			out.Folio		=datos.Folio;
			out.IdEmpresa	=datos.IdEmpresa;
			out.Esquema		=datos.Esquema;
			out.Version		=datos.Version;
			out.Anio			=datos.Anio;
			out.Periodo		=datos.Periodo;


			out.NombreEntidadRecaudadora		=datos.NombreEntidadRecaudadora;
			out.DocumentoIdentidadEntidad	=datos.DocumentoIdentidadEntidad;	  
			out.DireccionEntidadRecaudadora	=datos.DireccionEntidadRecaudadora;
			out.IdCliente					=datos.IdCliente;
			out.NombreCompletoCliente		=datos.NombreCompletoCliente;
			out.DocumentoIdentidadCliente	=datos.DocumentoIdentidadCliente;
			out.DireccionCliente				=datos.DireccionCliente;
			out.RazonSocialEmpresa			=datos.RazonSocialEmpresa;
			out.DocumentoIdentidadEmpresa	=datos.DocumentoIdentidadEmpresa;
			out.DireccionEmpresa				=datos.DireccionEmpresa;
			out.CiudadFecha					=datos.CiudadFecha;	
			out.Mes							=datos.Mes;
			out.DescripcionMes				=datos.DescripcionMes;
			out.MontoModalidadEmpleador		=datos.MontoModalidadEmpleador;
			out.MontoModalidadTrabajador		=datos.MontoModalidadTrabajador;
			out.MontoCotizacionesObligatorias=datos.MontoCotizacionesObligatorias;
			out.MontoCotizacionesVoluntarias	=datos.MontoCotizacionesVoluntarias;
			out.MontoTrabajadoresActivos		=datos.MontoTrabajadoresActivos;
			out.MontoPensionados				=datos.MontoPensionados;
			out.MontoRetencion				=datos.MontoRetencion;
			out.Certificacion				=datos.Certificacion;
			out.EstadoCertificado			=datos.EstadoCertificado;
			out.ActivoPensionado				=datos.ActivoPensionado;

			out.AhorrosModalidadIndirectaEmpleador		=datos.AhorrosModalidadIndirectaEmpleador;
			out.AhorrosModalidadDirectaTrabajador		=datos.AhorrosModalidadDirectaTrabajador;
			out.CotizacionesModalidadIndirectaEmpleador	=datos.CotizacionesModalidadIndirectaEmpleador;
			out.CotizacionesModalidadDirectaTrabajador	=datos.CotizacionesModalidadDirectaTrabajador;
			out.CotizacionesTrabajadorIndependiente		=datos.CotizacionesTrabajadorIndependiente;
			out.MontoRetiroPorAhorrosIncisoPrimero		=datos.MontoRetiroPorAhorrosIncisoPrimero;
			out.RetencionImpuestoSobreRetiros			=datos.RetencionImpuestoSobreRetiros;
			out.MontoRetiroPorAhorrosIncisoSegundo		=datos.MontoRetiroPorAhorrosIncisoSegundo;
			out.RetiroEmpleadorActualizado				=datos.RetiroEmpleadorActualizado;

			out.ResumenTrabajadorActivo				    =datos.ResumenTrabajadorActivo;
			out.ResumenTrabajadorPensionado				=datos.ResumenTrabajadorPensionado;

			out.TotalActivo				    =datos.TotalActivo;
			out.TotalPensionado				=datos.TotalPensionado;

			out.RazonSocialCorredora=Corredor.Cor_RazonSocial;
			out.DireccionCorredora=Corredor.Cor_Direccion;
			out.DocumentoIdentidadCorredora=Corredor.Cor_Rut;
			out.rut_representante=Representante.Rep_Rut;
			out.nombre_representante=Representante.Rep_Nombre;
			
			o.add(out);
		};
		
		return o;
	}
	
	private List<ReporteCertificado24Salida> obtenervaloresDefecto()  {
		List<ReporteCertificado24Salida> o = new ArrayList<ReporteCertificado24Salida>();
		ReporteCertificado24Salida out = new ReporteCertificado24Salida();
		
		out.Folio=0L;
		out.IdEmpresa=0L;
		out.Esquema="";
		out.Version="";
		out.Anio="";
		out.Periodo="";

		out.NombreEntidadRecaudadora	="";
		out.DocumentoIdentidadEntidad	="";	  
		out.DireccionEntidadRecaudadora	="";
		out.IdCliente					=0L;
		out.NombreCompletoCliente		="";
		out.DocumentoIdentidadCliente	="";
		out.DireccionCliente			="";
		out.RazonSocialEmpresa			="";
		out.DocumentoIdentidadEmpresa	="";
		out.DireccionEmpresa			="";
		out.CiudadFecha					="";	
		
		out.Mes =0;
		out.DescripcionMes ="";
		out.MontoModalidadEmpleador =new BigDecimal("0.0");
		out.MontoModalidadTrabajador =new BigDecimal("0.0");
		out.MontoCotizacionesObligatorias=new BigDecimal("0.0");
		out.MontoCotizacionesVoluntarias =new BigDecimal("0.0");
		out.MontoTrabajadoresActivos =new BigDecimal("0.0");
		out.MontoPensionados =new BigDecimal("0.0");
		out.MontoRetencion =new BigDecimal("0.0");
		out.ActivoPensionado =0L;

		out.AhorrosModalidadIndirectaEmpleador		=new BigDecimal("0.0");
		out.AhorrosModalidadDirectaTrabajador		=new BigDecimal("0.0");
		out.CotizacionesModalidadIndirectaEmpleador	=new BigDecimal("0.0");
		out.CotizacionesModalidadDirectaTrabajador	=new BigDecimal("0.0");
		out.CotizacionesTrabajadorIndependiente		=new BigDecimal("0.0");
		out.MontoRetiroPorAhorrosIncisoPrimero		=new BigDecimal("0.0");
		out.RetencionImpuestoSobreRetiros			=new BigDecimal("0.0");
		out.MontoRetiroPorAhorrosIncisoSegundo		=new BigDecimal("0.0");
		out.RetiroEmpleadorActualizado				=new BigDecimal("0.0");

		out.ResumenTrabajadorActivo	=new BigDecimal("0.0");
		out.ResumenTrabajadorPensionado	=new BigDecimal("0.0");

		out.TotalActivo	=new BigDecimal("0.0");
		out.TotalPensionado	=new BigDecimal("0.0");

		o.add(out);
		
		return o;
		 
	};
	
	public static void main (String [] args) throws Exception{
		
        //prueba de la clase
  		try {
  			//Connection con = PlataformaUtils.getDESAConnection();
  			Connection con = PlataformaUtils.getDESAConnection();
  			System.out.println(con);
  			try{
  				
  				ReporteCertificado24Entrada entrada = new ReporteCertificado24Entrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 6L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= "2023";
  				//entrada.p_FuenteDatos= "INICIAL";
  				List<ReporteCertificado24Salida> Salida = new ArrayList<ReporteCertificado24Salida>();
  				ReporteCertificado24 demo = new ReporteCertificado24();
  				Salida = demo.logica(con,entrada);
  				System.out.println(Salida.toString());
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
