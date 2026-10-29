package cl.bcs.normativa.declaraciones.DJ1922;

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

public class ReporteCertificado43 implements JavaCall {
	static final Logger logger = LogManager.getLogger(ReporteCertificado43.class.getName());
	
	
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
	
	public static class DatosReporte{ //SON LOS CAMPOS DE DETALLE EN EL SP DEL CRYSTAL "BSCL.Normativa.CrystalReports.Servicios::Certificado43" v_DatosDetalle
		public long Folio;
		public long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;
		
		public Long IdCliente;
        public int IdPortafolio;
        public String NombreCompletoEmisor;
        public String RutEmisor;
        public String TipoOperacionCertificado;
		public String NombreCompletoCliente;
        public String DocumentoIdentidadCliente;
        public String RazonSocialEmpresa;
        public String DocumentoIdentidadEmpresa;
        public String DireccionEmpresa;
		public String CiudadFecha;
        public String GiroActividad;
        public String FechaNumeroCuotas;
        public String FechaMontoActualizado;
        public Long IdCertificadoCliente;
		public String FechaCertificado;
        public Long IdOperacion;
        public String RutAdministradora;
        public String IdEmisor;
        public String RutFondo;
		public String RunFondo;
        public String Fondo;
        public String TipoFondo;
        public String RutCliente;
        public String NombreParticipe;
		public String TaxId;
        public String CodigoPais;
        public String TipoOperacion;
        public Date FechaInversion;
		public String FechaInversionText;
        public String MonedaReajuste;
		public BigDecimal MontoNominalAportes;
        public BigDecimal MontoNominalAportesActualizado;
        public Long NumeroSerieCuotas;
        public BigDecimal CantidadCuotasParticipe;
        public Long ParticipeCumpleRequisitos;
		public Long CuotasCumplenRequisitos;
        public BigDecimal ValorCuotaPesos;
        public BigDecimal NumCuotasSerieParticipe;
        public BigDecimal PorcentajeParticipacion31Dic;
        public BigDecimal ValNominalCuotasRescatadas;
        public BigDecimal CantidadCuotasRescatadas;
		public BigDecimal DiferenciaPorRescateOEnajenacion;
        public BigDecimal DiferenciaPorRescateOEnajenacionActualizado;
        public BigDecimal DifPorRescateOEnajenacionCFIAcogido107;
        public BigDecimal DifPorRescateOEnajenacionCFIAcogido107Actualizado;
        public BigDecimal MENORVALOR_I;
        public BigDecimal MAYORVALOR_I;
        public BigDecimal MENORVALOR_FM;     
        public BigDecimal MAYORVALOR_FM;      
		public BigDecimal ValNominalInversionaFechaLiquidacion;
        public BigDecimal CantCuotasCedidasPorLiquidacion;
        public BigDecimal DiferenciaValorCuotasFechaLiquidacion;
        public BigDecimal DiferenciaValorCuotasFechaLiquidacionActualizado;
        public String TributaSa;
		public BigDecimal DivRemDistAfectGCConIDPC;
        public BigDecimal DivRemDistAfectGCConIDPCal31122016;
        public BigDecimal DivRemDistAfectGCConIDPCVOL;
        public BigDecimal DivRemDistAfectGCSINDC;
        public BigDecimal DisRemRentasTribCProvRap_C96;
		public BigDecimal DisRemRentasTribCORP_C97;
        public BigDecimal DisRemRentasTribCExDD_C98;
        public BigDecimal DisRemRentasTribCUtAFFut_C99;
        public BigDecimal DivRemDistNoRentaFUT;
        public BigDecimal DisRemRentasExIGCAfecImptoAdi_C100;
		public BigDecimal CredImpGloCompNSRSD;
        public BigDecimal CredImpGloCompNSRCD;
        public BigDecimal CredIGCAcumD01012017RANSRD01012020SDD_C101;
        public BigDecimal CredIGCAcumD01012017RANSRD01012020CDD_C102;
        public BigDecimal CredImpGloCompSRSD;   
		public BigDecimal CredImpGloCompSRCD;
        public BigDecimal CredIGCAcumD01012017RXSRSDD_C103;
        public BigDecimal CredIGCAcumD01012017RXSRCDD_C104;
        public BigDecimal CredImpGloCompCTDCIF;
        public BigDecimal CredImpGloCompCDD31122016;
		public BigDecimal CredImpGloCompSDD31122016;
        public BigDecimal CredIGCAcumH31122016RXSDD_C105;
        public BigDecimal CredIGCAcumH31122016RXCDD_C106;
        public BigDecimal CredImpGloCompCTDCIF31122016;
        public BigDecimal CredImpGloCompCITAExArt21;  
		public String DetBenNetoPerRutFdo_C107;
        public BigDecimal DetBenNetoPerPOS_C113;
        public BigDecimal DetBenNetoPerNEG_C114;
        public BigDecimal DividendosExentosIGCoAdicional;
        public BigDecimal DividendosExentosIGCoAdicionalActualizado;
		public BigDecimal DividendosNoConstitutivosDeRenta;
        public BigDecimal DividendosNoConstitutivosDeRentaActualizado;
        public BigDecimal DivRemDistNoRentaFE;
        public BigDecimal DividendosAfectosImptoUnicoTasa10;
        public BigDecimal DividendosAfectosImptoUnicoTasa10Actualizado;
		public BigDecimal DividendosAfectosImptoUnicoTasa4;
        public BigDecimal DividendosAfectosImptoUnicoTasa4Actualizado;
        public BigDecimal DistribucionXDiminucionNoImputadaCapital;
        public BigDecimal DevolucionesDeCapital;
        public BigDecimal DevolucionesDeCapitalActualizado;                       
		public BigDecimal MontoInversionenCuotasCFIAnterior93;
        public Long NumeroCertificado;
        public BigDecimal FactorActualizacion;
        public String FechaOrigenInversion;
        public BigDecimal SumaNumeroCuotas; 
		public BigDecimal SumaMontoActualizado;
		public String NombreFondo;
		public String RunNemotecnico;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosReporte() {};
	}
	
	
	public static class ReporteCertificado43Entrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public String p_Version;
		
		 public String toString() {
				return Utils.printJson(this, true);
			}
		 
	}
		

	
	public static class ReporteCertificado43Salida extends BcsPojo{

		public Long Folio;
		public Long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;
  
		public Long IdCliente;
        public int IdPortafolio;
        public String NombreCompletoEmisor;
        public String RutEmisor;
        public String TipoOperacionCertificado;
		public String NombreCompletoCliente;
        public String DocumentoIdentidadCliente;
        public String RazonSocialEmpresa;
        public String DocumentoIdentidadEmpresa;
        public String DireccionEmpresa;
		public String CiudadFecha;
        public String GiroActividad;
        public String FechaNumeroCuotas;
        public String FechaMontoActualizado;
        public Long IdCertificadoCliente;
		public String FechaCertificado;
        public Long IdOperacion;
        public String RutAdministradora;
        public String IdEmisor;
        public String RutFondo;
		public String RunFondo;
        public String Fondo;
        public String TipoFondo;
        public String RutCliente;
        public String NombreParticipe;
		public String TaxId;
        public String CodigoPais;
        public String TipoOperacion;
        public Date FechaInversion;
		public String FechaInversionText;
        public String MonedaReajuste;
		public BigDecimal MontoNominalAportes;
        public BigDecimal MontoNominalAportesActualizado;
        public Long NumeroSerieCuotas;
        public BigDecimal CantidadCuotasParticipe;
        public Long ParticipeCumpleRequisitos;
		public Long CuotasCumplenRequisitos;
        public BigDecimal ValorCuotaPesos;
        public BigDecimal NumCuotasSerieParticipe;
        public BigDecimal PorcentajeParticipacion31Dic;
        public BigDecimal ValNominalCuotasRescatadas;
        public BigDecimal CantidadCuotasRescatadas;
		public BigDecimal DiferenciaPorRescateOEnajenacion;
        public BigDecimal DiferenciaPorRescateOEnajenacionActualizado;
        public BigDecimal DifPorRescateOEnajenacionCFIAcogido107;
        public BigDecimal DifPorRescateOEnajenacionCFIAcogido107Actualizado;
        public BigDecimal MENORVALOR_I;
        public BigDecimal MAYORVALOR_I;
        public BigDecimal MENORVALOR_FM;     
        public BigDecimal MAYORVALOR_FM;      
		public BigDecimal ValNominalInversionaFechaLiquidacion;
        public BigDecimal CantCuotasCedidasPorLiquidacion;
        public BigDecimal DiferenciaValorCuotasFechaLiquidacion;
        public BigDecimal DiferenciaValorCuotasFechaLiquidacionActualizado;
        public String TributaSa;
		public BigDecimal DivRemDistAfectGCConIDPC;
        public BigDecimal DivRemDistAfectGCConIDPCal31122016;
        public BigDecimal DivRemDistAfectGCConIDPCVOL;
        public BigDecimal DivRemDistAfectGCSINDC;
        public BigDecimal DisRemRentasTribCProvRap_C96;
		public BigDecimal DisRemRentasTribCORP_C97;
        public BigDecimal DisRemRentasTribCExDD_C98;
        public BigDecimal DisRemRentasTribCUtAFFut_C99;
        public BigDecimal DivRemDistNoRentaFUT;
        public BigDecimal DisRemRentasExIGCAfecImptoAdi_C100;
		public BigDecimal CredImpGloCompNSRSD;
        public BigDecimal CredImpGloCompNSRCD;
        public BigDecimal CredIGCAcumD01012017RANSRD01012020SDD_C101;
        public BigDecimal CredIGCAcumD01012017RANSRD01012020CDD_C102;
        public BigDecimal CredImpGloCompSRSD;   
		public BigDecimal CredImpGloCompSRCD;
        public BigDecimal CredIGCAcumD01012017RXSRSDD_C103;
        public BigDecimal CredIGCAcumD01012017RXSRCDD_C104;
        public BigDecimal CredImpGloCompCTDCIF;
        public BigDecimal CredImpGloCompCDD31122016;
		public BigDecimal CredImpGloCompSDD31122016;
        public BigDecimal CredIGCAcumH31122016RXSDD_C105;
        public BigDecimal CredIGCAcumH31122016RXCDD_C106;
        public BigDecimal CredImpGloCompCTDCIF31122016;
        public BigDecimal CredImpGloCompCITAExArt21;  
		public String DetBenNetoPerRutFdo_C107;
        public BigDecimal DetBenNetoPerPOS_C113;
        public BigDecimal DetBenNetoPerNEG_C114;
        public BigDecimal DividendosExentosIGCoAdicional;
        public BigDecimal DividendosExentosIGCoAdicionalActualizado;
		public BigDecimal DividendosNoConstitutivosDeRenta;
        public BigDecimal DividendosNoConstitutivosDeRentaActualizado;
        public BigDecimal DivRemDistNoRentaFE;
        public BigDecimal DividendosAfectosImptoUnicoTasa10;
        public BigDecimal DividendosAfectosImptoUnicoTasa10Actualizado;
		public BigDecimal DividendosAfectosImptoUnicoTasa4;
        public BigDecimal DividendosAfectosImptoUnicoTasa4Actualizado;
        public BigDecimal DistribucionXDiminucionNoImputadaCapital;
        public BigDecimal DevolucionesDeCapital;
        public BigDecimal DevolucionesDeCapitalActualizado;                       
		public BigDecimal MontoInversionenCuotasCFIAnterior93;
        public Long NumeroCertificado;
        public BigDecimal FactorActualizacion;
        public String FechaOrigenInversion;
        public BigDecimal SumaNumeroCuotas; 
		public BigDecimal SumaMontoActualizado;
        public String Certificado;
        public String EstadoCertificado;
        public String NombreFondo;


		public String RazonSocialCorredora;
		public String DireccionCorredora;
		public String DocumentoIdentidadCorredora;

		public String rut_representante;
		public String nombre_representante;
		
		public String RutCertificado;
		public String RunNemotecnico;
		
		public ReporteCertificado43Salida() {
			this.Folio = 0L;
		} 
	
	}
	
	
		private static FileLoader loader = new FileLoader();
		private static String SQL_DATOS_CERTIFICADO_CLIENTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1922/sql/ReporteCertificado43_SelectCertificadoCliente.sql");
		
		private static String SQL_DATOS_CORREDOR = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1922/sql/ReporteCertificado43_SelectDatosCorredor.sql");
		private static String SQL_FECHA_CERTIFICADO = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1922/sql/ReporteCertificado43_SelectFechaCertificado.sql");
		private static String SQL_DATOS_REPRESENTANTE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1922/sql/ReporteCertificado43_SelectDatosRepresentante.sql");
		private static String SQL_DATOS_REPORTES = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1922/sql/ReporteCertificado43_SelectDatosReporte.sql");
		
		public  final String SQL_EXIST_TABLA_RUT  		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARCERTIFICADO43MASIVO_RUT'";
		
		
		///ESPECIFICO
		//private static String SQL_SIN_DATOS_REPORTES = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ReporteCertificado9_SelectMesSinDatosReporte.sql");
		
		
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ReporteCertificado43Entrada in=gson.fromJson(jin.toString() ,ReporteCertificado43Entrada.class);
		List<ReporteCertificado43Salida>  out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public List<ReporteCertificado43Salida> logica(Connection con, ReporteCertificado43Entrada entrada) throws Exception {
		List<ReporteCertificado43Salida> out = new ArrayList<ReporteCertificado43Salida>();
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
				if (dataFechaCertif.get(0).DefineFechaCertificado=="S") {
					
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
								
								  //List<DatosReporte> dataMesSindata = sql.query(con, SQL_SIN_DATOS_REPORTES, paramsReporte, DatosReporte.class);
								  //union.addAll(dataMesSindata);
								
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

	
	private List<ReporteCertificado43Salida> AgregarvaloresSalida(List<DatosReporte> dataReporte,DatosRepresentante Representante,DatosCorredor Corredor )  {
		
		List<ReporteCertificado43Salida> o = new ArrayList<ReporteCertificado43Salida>();
		for (DatosReporte datos : dataReporte) {
			ReporteCertificado43Salida out = new ReporteCertificado43Salida();
			
			out.Folio=datos.Folio;
			out.IdEmpresa=datos.IdEmpresa;
			out.Esquema=datos.Esquema;
			out.Version=datos.Version;
			out.Anio=datos.Anio;
			out.Periodo=datos.Periodo;
			
			out.RazonSocialEmpresa=datos.RazonSocialEmpresa;
			out.DocumentoIdentidadCliente=datos.DocumentoIdentidadCliente;
			out.DireccionEmpresa=datos.DireccionEmpresa;
			out.IdCliente=datos.IdCliente;
			out.IdPortafolio= datos.IdPortafolio;
			out.NombreCompletoCliente=datos.NombreCompletoCliente;
			out.DocumentoIdentidadEmpresa=datos.DocumentoIdentidadEmpresa;
			out.CiudadFecha=datos.CiudadFecha;
			
			out.IdPortafolio=datos.IdPortafolio;
            out.NombreCompletoEmisor=datos.NombreCompletoEmisor;
            out.RutEmisor=datos.RutEmisor;
            out.TipoOperacionCertificado=datos.TipoOperacionCertificado;
            out.NombreCompletoCliente=datos.NombreCompletoCliente;
            out.DocumentoIdentidadCliente=datos.DocumentoIdentidadCliente;
            out.RazonSocialEmpresa=datos.RazonSocialEmpresa;
            out.DocumentoIdentidadEmpresa=datos.DocumentoIdentidadEmpresa;
            out.DireccionEmpresa=datos.DireccionEmpresa;
            out.CiudadFecha=datos.CiudadFecha;
            out.GiroActividad=datos.GiroActividad;
            out.FechaNumeroCuotas=datos.FechaNumeroCuotas;
            out.FechaMontoActualizado=datos.FechaMontoActualizado;
            out.IdCertificadoCliente=datos.IdCertificadoCliente;
            out.FechaCertificado=datos.FechaCertificado;
            out.IdOperacion=datos.IdOperacion;
            out.RutAdministradora=datos.RutAdministradora;
            out.IdEmisor=datos.IdEmisor;
            out.RutFondo=datos.RutFondo;
            out.RunFondo=datos.RunFondo;
            out.Fondo=datos.Fondo;
            out.TipoFondo=datos.TipoFondo;
            out.RutCliente=datos.RutCliente;
            out.NombreParticipe=datos.NombreParticipe;
            out.TaxId=datos.TaxId;
            out.CodigoPais=datos.CodigoPais;
            out.TipoOperacion=datos.TipoOperacion;
            out.FechaInversion=datos.FechaInversion;
			out.FechaInversionText=datos.FechaInversionText;
            out.MonedaReajuste=datos.MonedaReajuste;
            out.MontoNominalAportes=datos.MontoNominalAportes;
            out.MontoNominalAportesActualizado=datos.MontoNominalAportesActualizado;
            out.NumeroSerieCuotas=datos.NumeroSerieCuotas;
            out.CantidadCuotasParticipe=datos.CantidadCuotasParticipe;
            out.ParticipeCumpleRequisitos=datos.ParticipeCumpleRequisitos;
            out.CuotasCumplenRequisitos=datos.CuotasCumplenRequisitos;
            out.ValorCuotaPesos=datos.ValorCuotaPesos;
            out.NumCuotasSerieParticipe=datos.NumCuotasSerieParticipe;
            out.PorcentajeParticipacion31Dic=datos.PorcentajeParticipacion31Dic;
            out.ValNominalCuotasRescatadas=datos.ValNominalCuotasRescatadas;
            out.CantidadCuotasRescatadas=datos.CantidadCuotasRescatadas;
            out.DiferenciaPorRescateOEnajenacion=datos.DiferenciaPorRescateOEnajenacion;
            out.DiferenciaPorRescateOEnajenacionActualizado=datos.DiferenciaPorRescateOEnajenacionActualizado;
            out.DifPorRescateOEnajenacionCFIAcogido107=datos.DifPorRescateOEnajenacionCFIAcogido107;
            out.DifPorRescateOEnajenacionCFIAcogido107Actualizado=datos.DifPorRescateOEnajenacionCFIAcogido107Actualizado;
            out.MENORVALOR_I =datos.MENORVALOR_I; 
            out.MAYORVALOR_I =datos.MAYORVALOR_I;
            out.MENORVALOR_FM =datos.MENORVALOR_FM;
            out.MAYORVALOR_FM =datos.MAYORVALOR_FM;
            out.ValNominalInversionaFechaLiquidacion=datos.ValNominalInversionaFechaLiquidacion;
            out.CantCuotasCedidasPorLiquidacion=datos.CantCuotasCedidasPorLiquidacion;
            out.DiferenciaValorCuotasFechaLiquidacion=datos.DiferenciaValorCuotasFechaLiquidacion;
            out.DiferenciaValorCuotasFechaLiquidacionActualizado=datos.DiferenciaValorCuotasFechaLiquidacionActualizado;
            out.TributaSa=datos.TributaSa;
            out.DivRemDistAfectGCConIDPC=datos.DivRemDistAfectGCConIDPC;
            out.DivRemDistAfectGCConIDPCal31122016=datos.DivRemDistAfectGCConIDPCal31122016;
            out.DivRemDistAfectGCConIDPCVOL=datos.DivRemDistAfectGCConIDPCVOL;
            out.DivRemDistAfectGCSINDC=datos.DivRemDistAfectGCSINDC;
            out.DisRemRentasTribCProvRap_C96=datos.DisRemRentasTribCProvRap_C96;
            out.DisRemRentasTribCORP_C97=datos.DisRemRentasTribCORP_C97;
            out.DisRemRentasTribCExDD_C98=datos.DisRemRentasTribCExDD_C98;
            out.DisRemRentasTribCUtAFFut_C99=datos.DisRemRentasTribCUtAFFut_C99;
            out.DivRemDistNoRentaFUT=datos.DivRemDistNoRentaFUT;
            out.DisRemRentasExIGCAfecImptoAdi_C100=datos.DisRemRentasExIGCAfecImptoAdi_C100;
            out.CredImpGloCompNSRSD=datos.CredImpGloCompNSRSD;
            out.CredImpGloCompNSRCD=datos.CredImpGloCompNSRCD;
            out.CredIGCAcumD01012017RANSRD01012020SDD_C101=datos.CredIGCAcumD01012017RANSRD01012020SDD_C101;
            out.CredIGCAcumD01012017RANSRD01012020CDD_C102=datos.CredIGCAcumD01012017RANSRD01012020CDD_C102;
            out.CredImpGloCompSRSD =datos.CredImpGloCompSRSD; 
            out.CredImpGloCompSRCD=datos.CredImpGloCompSRCD;
            out.CredIGCAcumD01012017RXSRSDD_C103=datos.CredIGCAcumD01012017RXSRSDD_C103;
            out.CredIGCAcumD01012017RXSRCDD_C104=datos.CredIGCAcumD01012017RXSRCDD_C104;
            out.CredImpGloCompCTDCIF=datos.CredImpGloCompCTDCIF;
            out.CredImpGloCompCDD31122016=datos.CredImpGloCompCDD31122016;
            out.CredImpGloCompSDD31122016=datos.CredImpGloCompSDD31122016;
            out.CredIGCAcumH31122016RXSDD_C105=datos.CredIGCAcumH31122016RXSDD_C105;
            out.CredIGCAcumH31122016RXCDD_C106=datos.CredIGCAcumH31122016RXCDD_C106;
            out.CredImpGloCompCTDCIF31122016=datos.CredImpGloCompCTDCIF31122016;
            out.CredImpGloCompCITAExArt21 =datos.CredImpGloCompCITAExArt21; 
            out.DetBenNetoPerRutFdo_C107=datos.DetBenNetoPerRutFdo_C107;
            out.DetBenNetoPerPOS_C113=datos.DetBenNetoPerPOS_C113;
            out.DetBenNetoPerNEG_C114=datos.DetBenNetoPerNEG_C114;
            out.DividendosExentosIGCoAdicional=datos.DividendosExentosIGCoAdicional;
            out.DividendosExentosIGCoAdicionalActualizado=datos.DividendosExentosIGCoAdicionalActualizado;
            out.DividendosNoConstitutivosDeRenta=datos.DividendosNoConstitutivosDeRenta;
            out.DividendosNoConstitutivosDeRentaActualizado=datos.DividendosNoConstitutivosDeRentaActualizado;
            out.DivRemDistNoRentaFE=datos.DivRemDistNoRentaFE;
            out.DividendosAfectosImptoUnicoTasa10=datos.DividendosAfectosImptoUnicoTasa10;
            out.DividendosAfectosImptoUnicoTasa10Actualizado=datos.DividendosAfectosImptoUnicoTasa10Actualizado;
            out.DividendosAfectosImptoUnicoTasa4=datos.DividendosAfectosImptoUnicoTasa4;
            out.DividendosAfectosImptoUnicoTasa4Actualizado=datos.DividendosAfectosImptoUnicoTasa4Actualizado;
            out.DistribucionXDiminucionNoImputadaCapital=datos.DistribucionXDiminucionNoImputadaCapital;
            out.DevolucionesDeCapital=datos.DevolucionesDeCapital;
            out.DevolucionesDeCapitalActualizado =datos.DevolucionesDeCapitalActualizado; 
            out.MontoInversionenCuotasCFIAnterior93=datos.MontoInversionenCuotasCFIAnterior93;
            out.NumeroCertificado=datos.NumeroCertificado;
            out.FactorActualizacion=datos.FactorActualizacion;
            out.FechaOrigenInversion=datos.FechaOrigenInversion;
            out.SumaNumeroCuotas =datos.SumaNumeroCuotas; 
            out.SumaMontoActualizado=datos.SumaMontoActualizado;
            out.NombreFondo=datos.NombreFondo;
			
			out.RazonSocialCorredora=Corredor.Cor_RazonSocial;
			out.DireccionCorredora=Corredor.Cor_Direccion;
			out.DocumentoIdentidadCorredora=Corredor.Cor_Rut;
			out.rut_representante=Representante.Rep_Rut;
			out.nombre_representante=Representante.Rep_Nombre;
			out.RunNemotecnico = datos.RunNemotecnico;
			
			//out.Certificacion="N";
			out.EstadoCertificado="NGE";
			out.RutCertificado=datos.DocumentoIdentidadCliente;
			
			
			
			o.add(out);
		};
		
		return o;
	}
	
	private List<ReporteCertificado43Salida> obtenervaloresDefecto()  {
		List<ReporteCertificado43Salida> o = new ArrayList<ReporteCertificado43Salida>();
		ReporteCertificado43Salida out = new ReporteCertificado43Salida();
		String FechaDefecto = "01-01-1900";
		java.util.Date utilDate = new java.util.Date();
		
		out.Folio=0L;
		out.IdEmpresa=0L;
		out.Esquema="";
		out.Version="";
		out.Anio="";
		out.Periodo="";
		
		out.RazonSocialEmpresa="";
		out.DocumentoIdentidadCliente="";
		out.DireccionEmpresa="";
		//out.NombreEntidadRecaudadora="";
		out.IdCliente=0L;
		out.IdPortafolio=0;
		out.NombreCompletoCliente="";
		out.DocumentoIdentidadEmpresa="";
		out.CiudadFecha="";
		
		out.NombreCompletoEmisor="";
		out.GiroActividad="";
		out.FechaNumeroCuotas="";
		out.FechaMontoActualizado="";
		out.IdCertificadoCliente=0L;
		out.FechaCertificado="";
		out.IdOperacion=0L;
		out.RutAdministradora="";
		out.IdEmisor="";
		out.RutFondo="";
		out.RunFondo="";
		out.Fondo="";
		out.TipoFondo="";
		out.RutCliente="";
		out.NombreParticipe="";
		out.TaxId="";
		out.CodigoPais="";
		out.TipoOperacion="";
		java.sql.Date sqlDate = new java.sql.Date(01,01,1900);
		out.FechaInversion=sqlDate;
		out.FechaInversionText = "";
		out.MonedaReajuste="";
		out.MontoNominalAportes=new BigDecimal("0.0");
		out.MontoNominalAportesActualizado=new BigDecimal("0.0");
		out.NumeroSerieCuotas=0L;
		out.CantidadCuotasParticipe=new BigDecimal("0.0");
		out.ParticipeCumpleRequisitos=0L;
		out.CuotasCumplenRequisitos=0L;
		out.ValorCuotaPesos=new BigDecimal("0.0");
		out.NumCuotasSerieParticipe=new BigDecimal("0.0");
		out.PorcentajeParticipacion31Dic=new BigDecimal("0.0");
		out.ValNominalCuotasRescatadas=new BigDecimal("0.0");
		out.CantidadCuotasRescatadas=new BigDecimal("0.0");
		out.DiferenciaPorRescateOEnajenacion=new BigDecimal("0.0");
		out.DiferenciaPorRescateOEnajenacionActualizado=new BigDecimal("0.0");
		out.DifPorRescateOEnajenacionCFIAcogido107=new BigDecimal("0.0");
		out.DifPorRescateOEnajenacionCFIAcogido107Actualizado=new BigDecimal("0.0");
		out.MENORVALOR_I=new BigDecimal("0.0");
		out.MAYORVALOR_I=new BigDecimal("0.0");
		out.MENORVALOR_FM=new BigDecimal("0.0");     
		out.MAYORVALOR_FM=new BigDecimal("0.0");
		out.ValNominalInversionaFechaLiquidacion=new BigDecimal("0.0");
		out.CantCuotasCedidasPorLiquidacion=new BigDecimal("0.0");
		out.DiferenciaValorCuotasFechaLiquidacion=new BigDecimal("0.0");
		out.DiferenciaValorCuotasFechaLiquidacionActualizado=new BigDecimal("0.0");
		out.TributaSa="";
		out.DivRemDistAfectGCConIDPC=new BigDecimal("0.0");
		out.DivRemDistAfectGCConIDPCal31122016=new BigDecimal("0.0");
		out.DivRemDistAfectGCConIDPCVOL=new BigDecimal("0.0");
		out.DivRemDistAfectGCSINDC=new BigDecimal("0.0");
		out.DisRemRentasTribCProvRap_C96=new BigDecimal("0.0");
		out.DisRemRentasTribCORP_C97=new BigDecimal("0.0");
		out.DisRemRentasTribCExDD_C98=new BigDecimal("0.0");
		out.DisRemRentasTribCUtAFFut_C99=new BigDecimal("0.0");
		out.DivRemDistNoRentaFUT=new BigDecimal("0.0");
		out.DisRemRentasExIGCAfecImptoAdi_C100=new BigDecimal("0.0");
		out.CredImpGloCompNSRSD=new BigDecimal("0.0");
		out.CredImpGloCompNSRCD=new BigDecimal("0.0");
		out.CredIGCAcumD01012017RANSRD01012020SDD_C101=new BigDecimal("0.0");
		out.CredIGCAcumD01012017RANSRD01012020CDD_C102=new BigDecimal("0.0");
		out.CredImpGloCompSRSD=new BigDecimal("0.0");   
		out.CredImpGloCompSRCD=new BigDecimal("0.0");
		out.CredIGCAcumD01012017RXSRSDD_C103=new BigDecimal("0.0");
		out.CredIGCAcumD01012017RXSRCDD_C104=new BigDecimal("0.0");
		out.CredImpGloCompCTDCIF=new BigDecimal("0.0");
		out.CredImpGloCompCDD31122016=new BigDecimal("0.0");
		out.CredImpGloCompSDD31122016=new BigDecimal("0.0");
		out.CredIGCAcumH31122016RXSDD_C105=new BigDecimal("0.0");
		out.CredIGCAcumH31122016RXCDD_C106=new BigDecimal("0.0");
		out.CredImpGloCompCTDCIF31122016=new BigDecimal("0.0");
		out.CredImpGloCompCITAExArt21=new BigDecimal("0.0");
		out.DetBenNetoPerRutFdo_C107="";
		out.DetBenNetoPerPOS_C113=new BigDecimal("0.0");
		out.DetBenNetoPerNEG_C114=new BigDecimal("0.0");
		out.DividendosExentosIGCoAdicional=new BigDecimal("0.0");
		out.DividendosExentosIGCoAdicionalActualizado=new BigDecimal("0.0");
		out.DividendosNoConstitutivosDeRenta=new BigDecimal("0.0");
		out.DividendosNoConstitutivosDeRentaActualizado=new BigDecimal("0.0");
		out.DivRemDistNoRentaFE=new BigDecimal("0.0");
		out.DividendosAfectosImptoUnicoTasa10=new BigDecimal("0.0");
		out.DividendosAfectosImptoUnicoTasa10Actualizado=new BigDecimal("0.0");
		out.DividendosAfectosImptoUnicoTasa4=new BigDecimal("0.0");
		out.DividendosAfectosImptoUnicoTasa4Actualizado=new BigDecimal("0.0");
		out.DistribucionXDiminucionNoImputadaCapital=new BigDecimal("0.0");
		out.DevolucionesDeCapital=new BigDecimal("0.0");
		out.DevolucionesDeCapitalActualizado=new BigDecimal("0.0");                      
		out.MontoInversionenCuotasCFIAnterior93=new BigDecimal("0.0");
		out.NumeroCertificado=0L;
        out.FactorActualizacion=new BigDecimal("0.0");
        out.FechaOrigenInversion="01-01-1900";
        out.RunNemotecnico = "";

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
  				
  				ReporteCertificado43Entrada entrada = new ReporteCertificado43Entrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 6L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= "2023";
  				//entrada.p_FuenteDatos= "INICIAL";
  				List<ReporteCertificado43Salida> Salida = new ArrayList<ReporteCertificado43Salida>();
  				ReporteCertificado43 demo = new ReporteCertificado43();
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
