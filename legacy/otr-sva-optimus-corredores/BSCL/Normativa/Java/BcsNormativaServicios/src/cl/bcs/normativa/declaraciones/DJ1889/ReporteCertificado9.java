package cl.bcs.normativa.declaraciones.DJ1889;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
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

public class ReporteCertificado9 implements JavaCall {
	static final Logger logger = LogManager.getLogger(ReporteCertificado9.class.getName());
	
	
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
	
	public static class DatosReporte{ //SON LOS CAMPOS DE DETALLE EN EL SP DEL CRYSTAL "BSCL.Normativa.CrystalReports.Servicios::Certificado9" v_Resultado1
		public long Folio;
		public long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;
		
		public String RazonSocialEmpresa;
		public String DocumentoIdentidadEmpresa;
		public String DireccionEmpresa;
		public String NombreEntidadRecaudadora;
		 
		public Long IdCliente;
		public String NombreCompletoCliente;
		public String DocumentoIdentidadCliente;
		public String CiudadFecha;
		public int Mes;
		public String DescripcionMes;
		public BigDecimal MontoRetiroNominal;
        public BigDecimal RentaPorRetiroPositiva;
        public BigDecimal RentaPorRetiroNegativa;
        public BigDecimal MontoRetiroNominal42;
        public BigDecimal RentaPorRetiroPositiva42;
        public BigDecimal RentaPorRetiroNegativa42;
        public BigDecimal FactorActualizacion;
        public BigDecimal RentaActualizadaPositiva;
        public BigDecimal RentaActualizadaNegativa;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosReporte() {};
	}
	
	
	public static class ReporteCertificado9Entrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public String p_Version;
		
		 public String toString() {
				return Utils.printJson(this, true);
			}
		 
	}
		

	
	public static class ReporteCertificado9Salida extends BcsPojo{

		public long Folio;
		public Long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;
  
        public String RazonSocialEmpresa;
        public String DocumentoIdentidadEmpresa;
        public String DireccionEmpresa;
        public String NombreEntidadRecaudadora;
        public Long IdCliente;
        public String NombreCompletoCliente;
        public String DocumentoIdentidadCliente;
        public String CiudadFecha;

        public int Mes;
        public String DescripcionMes;
        public BigDecimal MontoRetiroNominal;
        public BigDecimal RentaPorRetiroPositiva;
        public BigDecimal RentaPorRetiroNegativa;
        public BigDecimal MontoRetiroNominal42;
        public BigDecimal RentaPorRetiroPositiva42;
        public BigDecimal RentaPorRetiroNegativa42;
        public BigDecimal FactorActualizacion;
        public BigDecimal RentaActualizadaPositiva;
        public BigDecimal RentaActualizadaNegativa;
        public String Certificacion;
        public String EstadoCertificado;

		public String RazonSocialCorredora;
		public String DireccionCorredora;
		public String DocumentoIdentidadCorredora;
		public String rut_representante;
		public String nombre_representante;
		public String RutCertificado;
		
		public ReporteCertificado9Salida() {
			this.Folio = 0;
		} 
	
	}
	
	
		private static FileLoader loader = new FileLoader();
		private static String SQL_DATOS_CERTIFICADO_CLIENTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ReporteCertificado9_SelectCertificadoCliente.sql");
		
		private static String SQL_DATOS_CORREDOR = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ReporteCertificado9_SelectDatosCorredor.sql");
		private static String SQL_FECHA_CERTIFICADO = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ReporteCertificado9_SelectFechaCertificado.sql");
		private static String SQL_DATOS_REPRESENTANTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ReporteCertificado9_SelectDatosRepresentante.sql");
		private static String SQL_DATOS_REPORTES = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ReporteCertificado9_SelectDatosReporte.sql");
		
		public  final String SQL_EXIST_TABLA_RUT  		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARCERTIFICADO9MASIVO_RUT'";
		
		
		///ESPECIFICO
		private static String SQL_SIN_DATOS_REPORTES = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ReporteCertificado9_SelectMesSinDatosReporte.sql");
		
		
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ReporteCertificado9Entrada in=gson.fromJson(jin.toString() ,ReporteCertificado9Entrada.class);
		List<ReporteCertificado9Salida>  out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public List<ReporteCertificado9Salida> logica(Connection con, ReporteCertificado9Entrada entrada) throws Exception {
		List<ReporteCertificado9Salida> out = new ArrayList<ReporteCertificado9Salida>();
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
							paramsReporte.put("Fechacertificado", Fechacertificado);
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
								
								out=AgregarvaloresSalida( union,Representante,Corredor);//datos reporte
								
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

	
	private List<ReporteCertificado9Salida> AgregarvaloresSalida(List<DatosReporte> dataReporte,DatosRepresentante Representante,DatosCorredor Corredor )  {
		
		List<ReporteCertificado9Salida> o = new ArrayList<ReporteCertificado9Salida>();
		for (DatosReporte datos : dataReporte) {
			ReporteCertificado9Salida out = new ReporteCertificado9Salida();
			
			out.Folio=datos.Folio;
			out.IdEmpresa=datos.IdEmpresa;
			out.Esquema=datos.Esquema;
			out.Version=datos.Version;
			out.Anio=datos.Anio;
			out.Periodo=datos.Periodo;
			
			out.RazonSocialEmpresa=datos.RazonSocialEmpresa;
			out.DocumentoIdentidadCliente=datos.DocumentoIdentidadCliente;
			out.DireccionEmpresa=datos.DireccionEmpresa;
			out.NombreEntidadRecaudadora=datos.NombreEntidadRecaudadora;
			out.IdCliente=datos.IdCliente;
			out.NombreCompletoCliente=datos.NombreCompletoCliente;
			out.DocumentoIdentidadEmpresa=datos.DocumentoIdentidadEmpresa;
			out.CiudadFecha=datos.CiudadFecha;
			
			out.Mes= datos.Mes;
			out.DescripcionMes=datos.DescripcionMes;
			out.MontoRetiroNominal=datos.MontoRetiroNominal;
	        out.RentaPorRetiroPositiva=datos.RentaPorRetiroPositiva;
	        out.RentaPorRetiroNegativa=datos.RentaPorRetiroNegativa;
	        out.MontoRetiroNominal42=datos.MontoRetiroNominal42;
	        out.RentaPorRetiroPositiva42=datos.RentaPorRetiroPositiva42;
	        out.RentaPorRetiroNegativa42=datos.RentaPorRetiroNegativa42;
	        out.FactorActualizacion=datos.FactorActualizacion;
	        out.RentaActualizadaPositiva=datos.RentaActualizadaPositiva;
	        out.RentaActualizadaNegativa=datos.RentaActualizadaNegativa;
			
			out.RazonSocialCorredora=Corredor.Cor_RazonSocial;
			out.DireccionCorredora=Corredor.Cor_Direccion;
			out.DocumentoIdentidadCorredora=Corredor.Cor_Rut;
			out.rut_representante=Representante.Rep_Rut;
			out.nombre_representante=Representante.Rep_Nombre;
			
			out.Certificacion="N";
			out.EstadoCertificado="NGE";
			out.RutCertificado=datos.DocumentoIdentidadCliente;
			
			
			
			o.add(out);
		};
		
		return o;
	}
	
	private List<ReporteCertificado9Salida> obtenervaloresDefecto()  {
		List<ReporteCertificado9Salida> o = new ArrayList<ReporteCertificado9Salida>();
		ReporteCertificado9Salida out = new ReporteCertificado9Salida();
		
		out.Folio=0L;
		out.IdEmpresa=0L;
		out.Esquema="";
		out.Version="";
		out.Anio="";
		out.Periodo="";
		
		out.RazonSocialEmpresa="";
		out.DocumentoIdentidadCliente="";
		out.DireccionEmpresa="";
		out.NombreEntidadRecaudadora="";
		out.IdCliente=0L;
		out.NombreCompletoCliente="";
		out.DocumentoIdentidadEmpresa="";
		out.CiudadFecha="";
		
		out.Mes=0;
		out.DescripcionMes="";
		out.MontoRetiroNominal= new BigDecimal("0.0");
        out.RentaPorRetiroPositiva= new BigDecimal("0.0");
        out.RentaPorRetiroNegativa= new BigDecimal("0.0");
        out.MontoRetiroNominal42= new BigDecimal("0.0");
        out.RentaPorRetiroPositiva42= new BigDecimal("0.0");
        out.RentaPorRetiroNegativa42= new BigDecimal("0.0");
        out.FactorActualizacion= new BigDecimal("0.0");
        out.RentaActualizadaPositiva= new BigDecimal("0.0");
        out.RentaActualizadaNegativa= new BigDecimal("0.0");

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
  				
  				ReporteCertificado9Entrada entrada = new ReporteCertificado9Entrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 6L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= "2023";
  				//entrada.p_FuenteDatos= "INICIAL";
  				List<ReporteCertificado9Salida> Salida = new ArrayList<ReporteCertificado9Salida>();
  				ReporteCertificado9 demo = new ReporteCertificado9();
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
