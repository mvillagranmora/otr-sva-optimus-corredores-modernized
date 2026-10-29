package cl.bcs.normativa.declaraciones.DJ1944;

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

public class ReporteCertificado17 implements JavaCall {
	static final Logger logger = LogManager.getLogger(ReporteCertificado17.class.getName());
	
	public static class DatosCorredor {
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
	
	public static class DatosRepresentante {
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
	
	public static class DatosCertificado {
		public Long IdCertificadoCliente;
	
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosCertificado() {
			this.IdCertificadoCliente =0L;
			
		};
	}
	
	public static class FechaCertificado {
		public String 		DefineFechaCertificado;
		public String 		FechaCertificado;
		
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public FechaCertificado() {};
	}
	
	public static class ExisteTabRut extends BcsPojo {
		public Long count;
	}
	
	public static class DatosReporte { // SON LOS CAMPOS DE DETALLE EN EL SP DEL CRYSTAL "BSCL.Normativa.CrystalReports.Servicios::Certificado17" v_Resultado1
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
        public String NombreDelGiro;
        public BigDecimal SalAhorroNetoEjerNEGT1;
        public BigDecimal SalAhorroNetoEjerT2;
        public BigDecimal SalAhorroNetoEjeNegT3;
        public BigDecimal MtoRenRetFFMMPOS;
        public BigDecimal MtoRenRetFFMMNEG;
        
        public String Certificacion;
        public String EstadoCertificado;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosReporte() {};
	}
	
	public static class ReporteCertificado17Entrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public String p_Version;
		
		 public String toString() {
				return Utils.printJson(this, true);
			}
		 
	}
		
	public static class ReporteCertificado17Salida extends BcsPojo {

		public Long Folio;
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
        
        public String NombreDelGiro;
        public BigDecimal SalAhorroNetoEjerNEGT1;
        public BigDecimal SalAhorroNetoEjerT2;
        public BigDecimal SalAhorroNetoEjeNegT3;
        public BigDecimal MtoRenRetFFMMPOS;
        public BigDecimal MtoRenRetFFMMNEG;
        
        public String Certificacion;
        public String EstadoCertificado;

		public String RazonSocialCorredora;
		public String DireccionCorredora;
		public String DocumentoIdentidadCorredora;
		public String rut_representante;
		public String nombre_representante;
		public String RutCertificado;
		
		public ReporteCertificado17Salida() {
			this.Folio = 0L;
		} 
	
	}
	
		private static FileLoader loader = new FileLoader();
		private static String SQL_DATOS_CERTIFICADO_CLIENTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1944/sql/ReporteCertificado17_SelectCertificadoCliente.sql");
		
		private static String SQL_DATOS_CORREDOR = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1944/sql/ReporteCertificado17_SelectDatosCorredor.sql");
		private static String SQL_FECHA_CERTIFICADO = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1944/sql/ReporteCertificado17_SelectFechaCertificado.sql");
		private static String SQL_DATOS_REPRESENTANTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1944/sql/ReporteCertificado17_SelectDatosRepresentante.sql");
		private static String SQL_DATOS_REPORTES = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1944/sql/ReporteCertificado17_SelectDatosReporte.sql");
		
		public  final String SQL_EXIST_TABLA_RUT = "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARCERTIFICADO17MASIVO_RUT'";
		
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ReporteCertificado17Entrada in = gson.fromJson(jin.toString(), ReporteCertificado17Entrada.class);
		List<ReporteCertificado17Salida> out = logica(con, in);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public List<ReporteCertificado17Salida> logica(Connection con, ReporteCertificado17Entrada entrada) throws Exception {
		List<ReporteCertificado17Salida> out = new ArrayList<ReporteCertificado17Salida>();
		//out.P_OUT_ERRORESCONTADOR = 0L;
		//out.P_OUT_MENSAJE = "";
		
        try {
	        SQL sql = new SQL();

	        final String fechaSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(con, entrada.p_IdEmpresa), "yyyy-MM-dd");
			
			String Fechacertificado = fechaSistema;
			
	    	HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
						
			List<DatosCorredor> dataCorredor = sql.query(con, SQL_DATOS_CORREDOR, params, DatosCorredor.class);
			DatosCorredor Corredor = new DatosCorredor();
			if (dataCorredor.size() > 0) {
				Corredor.Cor_RazonSocial = dataCorredor.get(0).Cor_RazonSocial;
				Corredor.Cor_IdEmpresa = dataCorredor.get(0).Cor_IdEmpresa;
				Corredor.Cor_Direccion = dataCorredor.get(0).Cor_Direccion;
				Corredor.Cor_Rut = dataCorredor.get(0).Cor_Rut;
			}
			
			
			List<DatosRepresentante> dataRepresentante = sql.query(con, SQL_DATOS_REPRESENTANTE, params, DatosRepresentante.class);
			DatosRepresentante Representante = new DatosRepresentante();
			if (dataRepresentante.size() > 0) {
				Representante.Rep_Nombre=dataRepresentante.get(0).Rep_Nombre;
				Representante.Rep_Rut=dataRepresentante.get(0).Rep_Rut;
			}
			
			HashMap<String, Object> params2 = new HashMap<String, Object>();
			params2.put("Version", entrada.p_Version);
			List<FechaCertificado> dataFechaCertif = sql.query(con, SQL_FECHA_CERTIFICADO, params2, FechaCertificado.class); // format 2024-03-18
			
			if (dataFechaCertif.size() > 0) {
				if (dataFechaCertif.get(0).DefineFechaCertificado.contains("S")) {
					Fechacertificado=dataFechaCertif.get(0).FechaCertificado;
				}
			}
			
			HashMap<String, Object> paramsCertif = new HashMap<String, Object>();
			paramsCertif.put("Version", entrada.p_Version);
			paramsCertif.put("IdEmpresa", entrada.p_IdEmpresa);
			
						
			try {
					HashMap<String, Object> par = new HashMap<>(2);
					List<ExisteTabRut> TabRut = new ArrayList<ExisteTabRut>();
					
					TabRut = new SQL().query(con, SQL_EXIST_TABLA_RUT, par, ExisteTabRut.class);
					
					if (TabRut.size() > 0) {
						
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
							paramsReporte.put("IdCertificadoCliente", IdCertificadoCliente);
						
							List<DatosReporte> dataReporte = sql.query(con, SQL_DATOS_REPORTES, paramsReporte, DatosReporte.class);
							
							if (dataReporte.size() > 0) {
															

								
								List<DatosReporte> union = new ArrayList<DatosReporte>();
								union.addAll(dataReporte);								
								
								out=AgregarvaloresSalida( union,Representante,Corredor); // Datos reporte
								
							} else { // Sin datos : valores
								out=obtenervaloresDefecto();
							};
					
					} else {
						out=obtenervaloresDefecto();
					};
			} catch (Exception e) {
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
	
	private List<ReporteCertificado17Salida> AgregarvaloresSalida(List<DatosReporte> dataReporte, DatosRepresentante Representante, DatosCorredor Corredor)  {
		
		List<ReporteCertificado17Salida> o = new ArrayList<ReporteCertificado17Salida>();
		for (DatosReporte datos : dataReporte) {
			ReporteCertificado17Salida out = new ReporteCertificado17Salida();
			
			out.Folio = datos.Folio;
			out.IdEmpresa = datos.IdEmpresa;
			out.Esquema = datos.Esquema;
			out.Version = datos.Version;
			out.Anio = datos.Anio;
			out.Periodo = datos.Periodo;
			
			out.RazonSocialEmpresa = datos.RazonSocialEmpresa;
			out.DocumentoIdentidadCliente = datos.DocumentoIdentidadCliente;
			out.DireccionEmpresa = datos.DireccionEmpresa;
			out.NombreEntidadRecaudadora = datos.NombreEntidadRecaudadora;
			
			out.IdCliente = datos.IdCliente;
			out.NombreCompletoCliente = datos.NombreCompletoCliente;
			out.DocumentoIdentidadEmpresa = datos.DocumentoIdentidadEmpresa;
			out.CiudadFecha = datos.CiudadFecha;
			
			out.NombreDelGiro = datos.NombreDelGiro;
			out.SalAhorroNetoEjerNEGT1 = datos.SalAhorroNetoEjerNEGT1;
			out.SalAhorroNetoEjerT2 = datos.SalAhorroNetoEjerT2;
			out.SalAhorroNetoEjeNegT3 = datos.SalAhorroNetoEjeNegT3;
			out.MtoRenRetFFMMPOS = datos.MtoRenRetFFMMPOS;
			out.MtoRenRetFFMMNEG = datos.MtoRenRetFFMMNEG;
			
			out.Certificacion = datos.Certificacion;
			out.EstadoCertificado = datos.EstadoCertificado;
			
			out.RazonSocialCorredora = Corredor.Cor_RazonSocial;
			out.DireccionCorredora = Corredor.Cor_Direccion;
			out.DocumentoIdentidadCorredora = Corredor.Cor_Rut;
			out.rut_representante = Representante.Rep_Rut;
			out.nombre_representante = Representante.Rep_Nombre;
			
			out.Certificacion = "N";
			out.EstadoCertificado = "NGE";
			out.RutCertificado = datos.DocumentoIdentidadCliente;
			
			o.add(out);
		};
		
		return o;
	}
	
	private List<ReporteCertificado17Salida> obtenervaloresDefecto() {
		List<ReporteCertificado17Salida> o = new ArrayList<ReporteCertificado17Salida>();
		ReporteCertificado17Salida out = new ReporteCertificado17Salida();
		
		out.Folio = 0L;
		out.IdEmpresa = 0L;
		out.Esquema = "";
		out.Version = "";
		out.Anio = "";
		out.Periodo = "";
		
		out.RazonSocialEmpresa = "";
		out.DocumentoIdentidadEmpresa = "";
		out.DireccionEmpresa = "";
		out.NombreEntidadRecaudadora = "";
		
		out.IdCliente = 0L;
		out.NombreCompletoCliente = "";
		out.DocumentoIdentidadCliente = "";
		out.CiudadFecha = "";
		
		out.NombreDelGiro = "";
		out.SalAhorroNetoEjerNEGT1 = new BigDecimal("0.0");
		out.SalAhorroNetoEjerT2 = new BigDecimal("0.0");
		out.SalAhorroNetoEjeNegT3 = new BigDecimal("0.0");
		out.MtoRenRetFFMMPOS = new BigDecimal("0.0");
		out.MtoRenRetFFMMNEG = new BigDecimal("0.0");
		
		out.Certificacion = "";
		out.EstadoCertificado = "";

		o.add(out);
		
		return o;
		 
	};
	
	public static void main (String [] args) throws Exception{
		
        // prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			try{
  				
  				ReporteCertificado17Entrada entrada = new ReporteCertificado17Entrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 6L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= "2023";
  				// entrada.p_FuenteDatos= "INICIAL";
  				List<ReporteCertificado17Salida> Salida = new ArrayList<ReporteCertificado17Salida>();
  				ReporteCertificado17 demo = new ReporteCertificado17();
  				Salida = demo.logica(con, entrada);
  				System.out.println(Salida.toString());
  				con.commit();
  				
  			} catch(Exception e) {
  				con.rollback();
  				e.printStackTrace();
  			} finally{
  				con.close();
  			}
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		
        System.out.println("== END ==");
	}
}
