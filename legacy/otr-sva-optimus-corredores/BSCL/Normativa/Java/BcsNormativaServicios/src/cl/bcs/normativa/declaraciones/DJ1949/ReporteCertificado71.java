package cl.bcs.normativa.declaraciones.DJ1949;

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

public class ReporteCertificado71 implements JavaCall {
	static final Logger logger = LogManager.getLogger(ReporteCertificado71.class.getName());
	
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
	
	public static class DatosReporte { // SON LOS CAMPOS DE DETALLE EN EL SP DEL CRYSTAL "BSCL.Normativa.CrystalReports.Servicios::Certificado71" v_Resultado
		public Long Folio;
		public Long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;

        public BigDecimal NroCertificadoF5;
        public BigDecimal FactorF7;
        public BigDecimal NroDividendoF2;
        public BigDecimal MontoF6;
        public BigDecimal MontoActualizadoF8;
        public BigDecimal ConCreditoIDPCDesed2017C6F9;
        public BigDecimal ConCreditoIDPCHasta2017C7F10;
        public BigDecimal ConDerechoIDPCVoluntarioC8F11;
        public BigDecimal SinDerechoCreditoC9F12;
        public BigDecimal MtoDivReaREXTribCumpRentasRap_C10F13;
        public BigDecimal MtoDivReaREXTribCumpOtrasRentas_C11F14;
        public BigDecimal MtoDivReaREXTribCumpExcDist_C12F15;
        public BigDecimal MtoDivReaREXTribCumpUtiISFUT_C13F16;
        public BigDecimal MtoDivReaREXTribCumpRentasISFUT_C14F17;
        public BigDecimal MtoDivReaREXRentExIGCAfecIAart11_C15F18;
        public BigDecimal RentasExcentasIGCC16F19;
        public BigDecimal MtoDivReaREXNoConstRenta_C17F20;
        public BigDecimal CIGCDesde01012017RentAfecNoRestSDD_C18F21;
        public BigDecimal CIGCDesde01012017RentAfecNoRestCDD_C19F22;
        public BigDecimal CIGCDesde01012017RentAfecNoRestSDD_C20F23;
        public BigDecimal CIGCDesde01012017RentAfecNoRestCDD_C21F24;
        public BigDecimal CIGCDesde01012017RentAfecRestSDD_C22F25;
        public BigDecimal CIGCDesde01012017RentAfecRestCDD_C23F26;
        public BigDecimal CIGCDesde01012017RentExenRestSDD_C24F27;
        public BigDecimal CIGCDesde01012017RentExenRestCDD_C25F28;
        public BigDecimal CIGCDesde01012017CredIPE_C26F29;
        public BigDecimal CIGCHasta31122016RentAfecSDD_C27F30;
        public BigDecimal CIGCHasta31122016RentAfecCDD_C28F31;
        public BigDecimal CIGCHasta31122016RentExenSDD_C29F32;
        public BigDecimal CIGCHasta31122016RentExenCDD_C30F33;
        public BigDecimal CIGCHasta31122016CredIPE_C31F34;
        public BigDecimal CIGCImptoTasaAdicional_C32F35;
        public BigDecimal DevCapArt17N7LIR_C33F36;
        public BigDecimal TasaEfectCredFUT;
        public BigDecimal TasaEfectCredFUNT;
        public String FechaDividendo;
        public String RutSocPagadora;
        public String RazonSocialSocPagadora;
        public String TipoSociedad;
        public String RutReceptor;
        public String RazonSocialEmpresa;
        public String DocumentoIdentidadEmpresa;
        public String DireccionEmpresa;
        public String NombreEntidadRecaudadora;
        public Long IdCliente;
        public String NombreCompletoCliente;
        public String DocumentoIdentidadCliente;
        public String CiudadFecha;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosReporte() {};
	}
	
	public static class ReporteCertificado71Entrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public String p_Version;
		
		 public String toString() {
				return Utils.printJson(this, true);
			}
		 
	}
		
	public static class ReporteCertificado71Salida extends BcsPojo {

		public Long Folio;
		public Long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;

        public BigDecimal NroCertificadoF5;
        public BigDecimal FactorF7;
        public BigDecimal NroDividendoF2;
        public BigDecimal MontoF6;
        public BigDecimal MontoActualizadoF8;
        public BigDecimal ConCreditoIDPCDesed2017C6F9;
        public BigDecimal ConCreditoIDPCHasta2017C7F10;
        public BigDecimal ConDerechoIDPCVoluntarioC8F11;
        public BigDecimal SinDerechoCreditoC9F12;
        public BigDecimal MtoDivReaREXTribCumpRentasRap_C10F13;
        public BigDecimal MtoDivReaREXTribCumpOtrasRentas_C11F14;
        public BigDecimal MtoDivReaREXTribCumpExcDist_C12F15;
        public BigDecimal MtoDivReaREXTribCumpUtiISFUT_C13F16;
        public BigDecimal MtoDivReaREXTribCumpRentasISFUT_C14F17;
        public BigDecimal MtoDivReaREXRentExIGCAfecIAart11_C15F18;
        public BigDecimal RentasExcentasIGCC16F19;
        public BigDecimal MtoDivReaREXNoConstRenta_C17F20;
        public BigDecimal CIGCDesde01012017RentAfecNoRestSDD_C18F21;
        public BigDecimal CIGCDesde01012017RentAfecNoRestCDD_C19F22;
        public BigDecimal CIGCDesde01012017RentAfecNoRestSDD_C20F23;
        public BigDecimal CIGCDesde01012017RentAfecNoRestCDD_C21F24;
        public BigDecimal CIGCDesde01012017RentAfecRestSDD_C22F25;
        public BigDecimal CIGCDesde01012017RentAfecRestCDD_C23F26;
        public BigDecimal CIGCDesde01012017RentExenRestSDD_C24F27;
        public BigDecimal CIGCDesde01012017RentExenRestCDD_C25F28;
        public BigDecimal CIGCDesde01012017CredIPE_C26F29;
        public BigDecimal CIGCHasta31122016RentAfecSDD_C27F30;
        public BigDecimal CIGCHasta31122016RentAfecCDD_C28F31;
        public BigDecimal CIGCHasta31122016RentExenSDD_C29F32;
        public BigDecimal CIGCHasta31122016RentExenCDD_C30F33;
        public BigDecimal CIGCHasta31122016CredIPE_C31F34;
        public BigDecimal CIGCImptoTasaAdicional_C32F35;
        public BigDecimal DevCapArt17N7LIR_C33F36;
        public BigDecimal TasaEfectCredFUT;
        public BigDecimal TasaEfectCredFUNT;
        public String FechaDividendo;
        public String RutSocPagadora;
        public String RazonSocialSocPagadora;
        public String TipoSociedad;
        public String RutReceptor;
        public String RazonSocialEmpresa;
        public String DocumentoIdentidadEmpresa;
        public String DireccionEmpresa;
        public String NombreEntidadRecaudadora;
        public Long IdCliente;
        public String NombreCompletoCliente;
        public String DocumentoIdentidadCliente;
        public String CiudadFecha;

		public String RazonSocialCorredora;
		public String DireccionCorredora;
		public String DocumentoIdentidadCorredora;
		public String rut_representante;
		public String nombre_representante;
		public String RutCertificado;
		
		public ReporteCertificado71Salida() {
			this.Folio = 0L;
		} 
	
	}
	
		private static FileLoader loader = new FileLoader();
		private static String SQL_DATOS_CERTIFICADO_CLIENTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1949/sql/ReporteCertificado71_SelectCertificadoCliente.sql");
		
		private static String SQL_DATOS_CORREDOR = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1949/sql/ReporteCertificado71_SelectDatosCorredor.sql");
		private static String SQL_FECHA_CERTIFICADO = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1949/sql/ReporteCertificado71_SelectFechaCertificado.sql");
		private static String SQL_DATOS_REPRESENTANTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1949/sql/ReporteCertificado71_SelectDatosRepresentante.sql");
		private static String SQL_DATOS_REPORTES = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1949/sql/ReporteCertificado71_SelectDatosReporte.sql");
		
		public  final String SQL_EXIST_TABLA_RUT = "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARCERTIFICADO71MASIVO_RUT'";
		
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ReporteCertificado71Entrada in = gson.fromJson(jin.toString(), ReporteCertificado71Entrada.class);
		List<ReporteCertificado71Salida> out = logica(con, in);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public List<ReporteCertificado71Salida> logica(Connection con, ReporteCertificado71Entrada entrada) throws Exception {
		List<ReporteCertificado71Salida> out = new ArrayList<ReporteCertificado71Salida>();
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
				if (dataFechaCertif.get(0).DefineFechaCertificado == "S") {
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
	
	private List<ReporteCertificado71Salida> AgregarvaloresSalida(List<DatosReporte> dataReporte, DatosRepresentante Representante, DatosCorredor Corredor)  {
		
		List<ReporteCertificado71Salida> o = new ArrayList<ReporteCertificado71Salida>();
		for (DatosReporte datos : dataReporte) {
			ReporteCertificado71Salida out = new ReporteCertificado71Salida();
			
			out.Folio=datos.Folio;
            out.IdEmpresa=datos.IdEmpresa;
            out.Esquema=datos.Esquema;
            out.Version=datos.Version;
            out.Anio=datos.Anio;
            out.Periodo=datos.Periodo;

            out.NroCertificadoF5=datos.NroCertificadoF5;
            out.FactorF7=datos.FactorF7;
            out.NroDividendoF2=datos.NroDividendoF2;
            out.MontoF6=datos.MontoF6;
            out.MontoActualizadoF8=datos.MontoActualizadoF8;
            out.ConCreditoIDPCDesed2017C6F9=datos.ConCreditoIDPCDesed2017C6F9;
            out.ConCreditoIDPCHasta2017C7F10=datos.ConCreditoIDPCHasta2017C7F10;
            out.ConDerechoIDPCVoluntarioC8F11=datos.ConDerechoIDPCVoluntarioC8F11;
            out.SinDerechoCreditoC9F12=datos.SinDerechoCreditoC9F12;
            out.MtoDivReaREXTribCumpRentasRap_C10F13=datos.MtoDivReaREXTribCumpRentasRap_C10F13;
            out.MtoDivReaREXTribCumpOtrasRentas_C11F14=datos.MtoDivReaREXTribCumpOtrasRentas_C11F14;
            out.MtoDivReaREXTribCumpExcDist_C12F15=datos.MtoDivReaREXTribCumpExcDist_C12F15;
            out.MtoDivReaREXTribCumpUtiISFUT_C13F16=datos.MtoDivReaREXTribCumpUtiISFUT_C13F16;
            out.MtoDivReaREXTribCumpRentasISFUT_C14F17=datos.MtoDivReaREXTribCumpRentasISFUT_C14F17;
            out.MtoDivReaREXRentExIGCAfecIAart11_C15F18=datos.MtoDivReaREXRentExIGCAfecIAart11_C15F18;
            out.RentasExcentasIGCC16F19=datos.RentasExcentasIGCC16F19;
            out.MtoDivReaREXNoConstRenta_C17F20=datos.MtoDivReaREXNoConstRenta_C17F20;
            out.CIGCDesde01012017RentAfecNoRestSDD_C18F21=datos.CIGCDesde01012017RentAfecNoRestSDD_C18F21;
            out.CIGCDesde01012017RentAfecNoRestCDD_C19F22=datos.CIGCDesde01012017RentAfecNoRestCDD_C19F22;
            out.CIGCDesde01012017RentAfecNoRestSDD_C20F23=datos.CIGCDesde01012017RentAfecNoRestSDD_C20F23;
            out.CIGCDesde01012017RentAfecNoRestCDD_C21F24=datos.CIGCDesde01012017RentAfecNoRestCDD_C21F24;
            out.CIGCDesde01012017RentAfecRestSDD_C22F25=datos.CIGCDesde01012017RentAfecRestSDD_C22F25;
            out.CIGCDesde01012017RentAfecRestCDD_C23F26=datos.CIGCDesde01012017RentAfecRestCDD_C23F26;
            out.CIGCDesde01012017RentExenRestSDD_C24F27=datos.CIGCDesde01012017RentExenRestSDD_C24F27;
            out.CIGCDesde01012017RentExenRestCDD_C25F28=datos.CIGCDesde01012017RentExenRestCDD_C25F28;
            out.CIGCDesde01012017CredIPE_C26F29=datos.CIGCDesde01012017CredIPE_C26F29;
            out.CIGCHasta31122016RentAfecSDD_C27F30=datos.CIGCHasta31122016RentAfecSDD_C27F30;
            out.CIGCHasta31122016RentAfecCDD_C28F31=datos.CIGCHasta31122016RentAfecCDD_C28F31;
            out.CIGCHasta31122016RentExenSDD_C29F32=datos.CIGCHasta31122016RentExenSDD_C29F32;
            out.CIGCHasta31122016RentExenCDD_C30F33=datos.CIGCHasta31122016RentExenCDD_C30F33;
            out.CIGCHasta31122016CredIPE_C31F34=datos.CIGCHasta31122016CredIPE_C31F34;
            out.CIGCImptoTasaAdicional_C32F35=datos.CIGCImptoTasaAdicional_C32F35;
            out.DevCapArt17N7LIR_C33F36=datos.DevCapArt17N7LIR_C33F36;
            out.TasaEfectCredFUT=datos.TasaEfectCredFUT;
            out.TasaEfectCredFUNT=datos.TasaEfectCredFUNT;
            out.FechaDividendo=datos.FechaDividendo;
            out.RutSocPagadora=datos.RutSocPagadora;
            out.RazonSocialSocPagadora=datos.RazonSocialSocPagadora;
            out.TipoSociedad=datos.TipoSociedad;
            out.RutReceptor=datos.RutReceptor;
            out.RazonSocialEmpresa=datos.RazonSocialEmpresa;
            out.DocumentoIdentidadEmpresa=datos.DocumentoIdentidadEmpresa;
            out.DireccionEmpresa=datos.DireccionEmpresa;
            out.NombreEntidadRecaudadora=datos.NombreEntidadRecaudadora;
            out.IdCliente=datos.IdCliente;
            out.NombreCompletoCliente=datos.NombreCompletoCliente;
            out.DocumentoIdentidadCliente=datos.DocumentoIdentidadCliente;
            out.CiudadFecha=datos.CiudadFecha;
            
            out.RazonSocialCorredora = Corredor.Cor_RazonSocial;
			out.DireccionCorredora = Corredor.Cor_Direccion;
			out.DocumentoIdentidadCorredora = Corredor.Cor_Rut;
			out.rut_representante = Representante.Rep_Rut;
			out.nombre_representante = Representante.Rep_Nombre;
            
			o.add(out);
		};
		
		return o;
	}
	
	private List<ReporteCertificado71Salida> obtenervaloresDefecto() {
		List<ReporteCertificado71Salida> o = new ArrayList<ReporteCertificado71Salida>();
		ReporteCertificado71Salida out = new ReporteCertificado71Salida();
		
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
		
		out.NroCertificadoF5 = new BigDecimal("0.0");
		out.FactorF7 = new BigDecimal("0.0");
		out.NroDividendoF2 = new BigDecimal("0.0");
		out.MontoF6 = new BigDecimal("0.0");
		out.MontoActualizadoF8 = new BigDecimal("0.0");
		out.ConCreditoIDPCDesed2017C6F9 = new BigDecimal("0.0");
		out.ConCreditoIDPCHasta2017C7F10 = new BigDecimal("0.0");
		out.ConDerechoIDPCVoluntarioC8F11 = new BigDecimal("0.0");
		out.SinDerechoCreditoC9F12 = new BigDecimal("0.0");
		out.MtoDivReaREXTribCumpRentasRap_C10F13 = new BigDecimal("0.0");
		out.MtoDivReaREXTribCumpOtrasRentas_C11F14 = new BigDecimal("0.0");
		out.MtoDivReaREXTribCumpExcDist_C12F15 = new BigDecimal("0.0");
		out.MtoDivReaREXTribCumpUtiISFUT_C13F16 = new BigDecimal("0.0");
		out.MtoDivReaREXTribCumpRentasISFUT_C14F17 = new BigDecimal("0.0");
		out.MtoDivReaREXRentExIGCAfecIAart11_C15F18 = new BigDecimal("0.0");
		out.RentasExcentasIGCC16F19 = new BigDecimal("0.0");
		out.MtoDivReaREXNoConstRenta_C17F20 = new BigDecimal("0.0");
		out.CIGCDesde01012017RentAfecNoRestSDD_C18F21 = new BigDecimal("0.0");
		out.CIGCDesde01012017RentAfecNoRestCDD_C19F22 = new BigDecimal("0.0");
		out.CIGCDesde01012017RentAfecNoRestSDD_C20F23 = new BigDecimal("0.0");
		out.CIGCDesde01012017RentAfecNoRestCDD_C21F24 = new BigDecimal("0.0");
		out.CIGCDesde01012017RentAfecRestSDD_C22F25 = new BigDecimal("0.0");
		out.CIGCDesde01012017RentAfecRestCDD_C23F26 = new BigDecimal("0.0");
		out.CIGCDesde01012017RentExenRestSDD_C24F27 = new BigDecimal("0.0");
		out.CIGCDesde01012017RentExenRestCDD_C25F28 = new BigDecimal("0.0");
		out.CIGCDesde01012017CredIPE_C26F29 = new BigDecimal("0.0");
		out.CIGCHasta31122016RentAfecSDD_C27F30 = new BigDecimal("0.0");
		out.CIGCHasta31122016RentAfecCDD_C28F31 = new BigDecimal("0.0");
		out.CIGCHasta31122016RentExenSDD_C29F32 = new BigDecimal("0.0");
		out.CIGCHasta31122016RentExenCDD_C30F33 = new BigDecimal("0.0");
		out.CIGCHasta31122016CredIPE_C31F34 = new BigDecimal("0.0");
		out.CIGCImptoTasaAdicional_C32F35 = new BigDecimal("0.0");
		out.DevCapArt17N7LIR_C33F36 = new BigDecimal("0.0");
		out.TasaEfectCredFUT = new BigDecimal("0.0");
		out.TasaEfectCredFUNT = new BigDecimal("0.0");
		
		out.FechaDividendo = "";
		out.RutSocPagadora = "";
		out.RazonSocialSocPagadora = "";
		out.TipoSociedad = "";
		out.RutReceptor = "";
		out.RazonSocialEmpresa = "";
		out.DocumentoIdentidadEmpresa = "";
		out.DireccionEmpresa = "";
		out.NombreEntidadRecaudadora = "";
		out.IdCliente = 0L;
		out.NombreCompletoCliente = "";
		out.DocumentoIdentidadCliente = "";
		out.CiudadFecha = "";

		o.add(out);
		
		return o;
		 
	};
	
	public static void main (String [] args) throws Exception{
		
        // prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			try{
  				
  				ReporteCertificado71Entrada entrada = new ReporteCertificado71Entrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 6L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= "2023";
  				// entrada.p_FuenteDatos= "INICIAL";
  				List<ReporteCertificado71Salida> Salida = new ArrayList<ReporteCertificado71Salida>();
  				ReporteCertificado71 demo = new ReporteCertificado71();
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
