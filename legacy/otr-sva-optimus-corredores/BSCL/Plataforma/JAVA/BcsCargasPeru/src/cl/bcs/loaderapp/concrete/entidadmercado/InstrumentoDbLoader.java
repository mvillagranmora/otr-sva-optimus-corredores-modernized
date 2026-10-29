package cl.bcs.loaderapp.concrete.entidadmercado;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.instrumentos.Instrumento;
import cl.bcs.loaderapp.utils.LoaderUtils;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.kaiser.hana.KProcedureException;

public class InstrumentoDbLoader implements DbLoader {
	
	public String SQL_INGRESAR_INSTRUMENTO = loadResourceToString("cl/bcs/loaderapp/sql/instrumento/Instrumento_IngresarInstrumento.sql");
	public String SQL_INGRESAR_INSTRUMENTO_BONO = loadResourceToString("cl/bcs/loaderapp/sql/instrumento/Instrumento_IngresarInstrumentoBono.sql");
	public String SQL_INGRESAR_BOLSA_INSTRUMENTO = loadResourceToString("cl/bcs/loaderapp/sql/instrumento/Instrumento_IngresarBolsaInstrumento.sql");
	
	public enum ConfiguracionMonedasBolsa {
		USD("USD", "BVL"),
		PEN("PEN", "BVL"),
		CAD("CAD", "OUT"),
		CLP("CLP", "MIL"),
		COP("COP", "MIL"),
		EUR("EUR", "OUT"),
		AUD("AUD", "OUT"),
		GBP("GBP", "OUT"),
		MXN("MXN", "MIL"),
		CHF("CHF", "OUT"),
		NOK("NOK", "OUT");
		
	    protected final String moneda;
	    protected final String bolsa;

	    ConfiguracionMonedasBolsa(String moneda, String bolsa) {
	        this.moneda = moneda;
	        this.bolsa = bolsa;
	    }

	    public String getMoneda() {
	        return moneda;
	    }

	    public String getBolsa() {
	        return bolsa;
	    }
	}
	
	public static class EntidadInstrumento{
		public Long IdInstrumento;
		public String Nemotecnico;
		public String ISIN;
		public Double CorteMinimo;
		public String EstaInscrito;
		public String EsDivisible;
		public String RazonSocial;
		public String Lista;
		public String FechaEmision;
		public String FechaVencimiento;
		public Long IdInstrumentoRelacionado;
		public String IdEmisor;
		public String Reajustable;
		public String IdMoneda;
		public String IdTipoInstrumento;
		public String IdEstado;
		public Long DecimalesPrecio;
		public Long Tamano;
		public String FechaInicioVigencia;
		public String IndArt104;
		public Double TasaFiscal;
		public String IdEntidadClasificadoraRiesgo;
		public String ValorClasificacionRiesgo;
		public String TipoValorizacion;
		public String ValorizaPor;
		public Double Precio;
		public Double Tasa;
		public String TieneCupon;
		public Long NumeroCuotasCirculacionMesActual;
		public Long NumeroAccionesCirculacion;
		public String IndicadorLiquidaDCV;
		public Long IdPaisEmisor;
		public Double FrecuenciaCupon;
		public String Serie;
		public Double PrecioReferencia;
		public String IndicadorBolson;
		public String FechaReferencia;
		public Long NumeroCupones;
		public Long CodigoRubro;
		public String FechaDevengo;
		public String PeriodoPagoCupon;
		public Integer TipoCalculoInteresesDevengado;
		public Integer TipoCalculoInteresesCupon;
		public String IndicadorCuponeraVariable;
		public String IndicadorValorizarendiaHabil;
		public String ConversionDias;
		public String AjustefindeMes;
		public String IndicadorMercadoLocal;
		public String TipodeReajuste;
		public String CUSIP;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadBolsaInstrumento{
		public Long IdBolsaInstrumento;
		public Long IdInstrumento;
		public String IdBolsa;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadInstrumentoBono{
		public Long IdInstrumento;
		public String FechaDevengo;
		public String AjusteFinDeMes;
		public String CallConfirmado;
		public String ConvencionDias;
		public String CuponeraVariable;
		public Double TasaEmision;
		public Integer TipoCalculoInteresesCupon;
		public Integer TipoCalculoInteresesDevengados;
		public String MercadoLocal;
		public String MercadoExtrabursatil;
		public String MercadoExtranjero;
		public String PresenciaBursatil;
		public String CUSIP;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral entrada, List<Object> lines) {

		List<String> salida = new ArrayList<String>();
        Long idEmpresaDefecto = entrada.p_IdEmpresa;
		
		try (Connection con = jsJB.getConnection()){
			IdentificarLlaves llaves = new IdentificarLlavesBuilder(con)
			        .setPaises()
			        .build();
			String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), idEmpresaDefecto);
			String codigoPais = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresaDefecto, "PLA.LocalizacionSistema");
			
			List<Instrumento> instrumentosCSV = (List<Instrumento>) (Object) lines;
			System.out.println("EJECUTAR EN PARALELO CON: "  + Runtime.getRuntime().availableProcessors() + " PROCESOS");
			
			instrumentosCSV.parallelStream().forEach((instrumento) -> {
				try (Connection conn = jsJB.getConnection()){
					SQL sql = new SQL();
					LoaderUtils loaderUtils = new LoaderUtils();	
	
					String mensaje = "";
					boolean estado = false;
					int linea = instrumento.getLinea();
					try {
						
						StringBuilder errores = new StringBuilder();
						EntidadInstrumento ins = new EntidadInstrumento();
						ins.IdInstrumento = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Instrumento", (long) 1).get(0);
						ins.Nemotecnico = instrumento.getNemotecnico();
						ins.ISIN = instrumento.getISIN();
						ins.CorteMinimo = Double.parseDouble( (instrumento.getCorteMinimo()!=null) ? instrumento.getCorteMinimo() : "0" );
						ins.EstaInscrito = instrumento.getEstaInscrito();
						ins.EsDivisible = instrumento.getEsDivisible();
						ins.RazonSocial = instrumento.getRazonSocial();
						ins.Lista = instrumento.getLista();
						ins.FechaEmision = instrumento.getFechaEmision();
						
						SimpleDateFormat parser=new SimpleDateFormat("dd/MM/yyyy");
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						
						if(ins.FechaEmision==null) {
							//errores.append("FechaEmision; ");
							ins.FechaEmision = "0001-01-01";
						}else {
							Date fechaEmision = parser.parse(ins.FechaEmision);
							ins.FechaEmision = formatter.format(fechaEmision);
						}
						ins.FechaVencimiento = instrumento.getFechaVencimiento();
						if(ins.FechaVencimiento==null) {
							//errores.append("FechaVencimiento; ");
							ins.FechaVencimiento = "0001-01-01";
						}else {
							Date fechaVencimiento = parser.parse(ins.FechaVencimiento);
							ins.FechaVencimiento = formatter.format(fechaVencimiento);
						}
						ins.IdInstrumentoRelacionado = Long.parseLong( (instrumento.getIdInstrumentoRelacionado()!=null && !instrumento.getIdInstrumentoRelacionado().equals("")) ? instrumento.getIdInstrumentoRelacionado() : "0" );
						ins.IdEmisor = instrumento.getIdEmisor();
						ins.Reajustable = (instrumento.getReajustable()!=null) ? instrumento.getReajustable() : "";
						ins.IdMoneda = (instrumento.getIdMoneda()!=null) ? instrumento.getIdMoneda() : "PEN";
						ins.IdTipoInstrumento = instrumento.getIdTipoInstrumento();
						if(ins.IdTipoInstrumento==null) {
							errores.append("IdTipoInstrumento;");
						}
						ins.IdEstado = instrumento.getIdEstado();
						ins.DecimalesPrecio = Long.parseLong((instrumento.getDecimalesPrecio()!=null) ? instrumento.getDecimalesPrecio() : "0" );
						ins.Tamano = Long.parseLong((instrumento.getTamano()!=null && !instrumento.getTamano().equals("")) ? instrumento.getTamano() : "0" );
						ins.FechaInicioVigencia = instrumento.getFechaInicioVigencia();
						ins.IndArt104 = instrumento.getIndArt104();
						ins.TasaFiscal = Double.parseDouble((instrumento.getTasaFiscal()!=null && !instrumento.getTasaFiscal().equals("")) ? instrumento.getTasaFiscal() : "0");
						ins.IdEntidadClasificadoraRiesgo = instrumento.getIdEntidadClasificadoraRiesgo();
						ins.ValorClasificacionRiesgo = instrumento.getValorClasificacionRiesgo();
						ins.TipoValorizacion = instrumento.getTipoValorizacion();
						ins.ValorizaPor = (instrumento.getValorizaPor()!=null) ? instrumento.getValorizaPor() : "P";
						ins.Precio = Double.parseDouble((instrumento.getPrecio()!=null && !instrumento.getPrecio().equals("")) ? instrumento.getPrecio() : "0" );
						ins.Tasa = Double.parseDouble((instrumento.getTasa()!=null && !instrumento.getTasa().equals("")) ? instrumento.getTasa() : "0" ); 
						ins.TieneCupon = (instrumento.getTieneCupon()!=null) ? instrumento.getTieneCupon() : "0";
						ins.NumeroCuotasCirculacionMesActual = Long.parseLong((instrumento.getNumeroCuotasCirculacionMesActual()!=null && !instrumento.getNumeroCuotasCirculacionMesActual().equals("")) ? instrumento.getNumeroCuotasCirculacionMesActual() : "0" );
						ins.NumeroAccionesCirculacion = Long.valueOf((instrumento.getNumeroAccionesCirculacion()!=null && !instrumento.getNumeroAccionesCirculacion().equals("")) ? instrumento.getNumeroAccionesCirculacion() : "0" );
						ins.IndicadorLiquidaDCV = (instrumento.getIndicadorLiquidaDCV()!=null) ? instrumento.getIndicadorLiquidaDCV() : "N";
						ins.IdPaisEmisor = llaves.getPais(instrumento.getIdPaisEmisor());
						if(ins.IdPaisEmisor==null) {
							errores.append("IdPaisEmisor; ");
							ins.IdPaisEmisor = Long.parseLong("1");
						}
						ins.FrecuenciaCupon = Double.parseDouble((instrumento.getFrecuenciaCupon()!=null) ? instrumento.getFrecuenciaCupon() : "0" );
						ins.Serie = (instrumento.getSerie()!=null)? instrumento.getSerie() : "";
						ins.PrecioReferencia = Double.parseDouble( (instrumento.getPrecioReferencia()!=null && !instrumento.getPrecioReferencia().equals("")) ? instrumento.getPrecioReferencia() : "0" );
						ins.IndicadorBolson = (instrumento.getIndicadorBolson()!=null) ? instrumento.getIndicadorBolson() : "N";
						ins.FechaReferencia = instrumento.getFechaReferencia();
						if(ins.FechaReferencia==null) {
							//errores.append("FechaReferencia; ");
							ins.FechaReferencia = "0001-01-01";
						}else {
							Date fechaReferencia = parser.parse(ins.FechaReferencia);
							ins.FechaReferencia = formatter.format(fechaReferencia);
						}
						ins.NumeroCupones = Long.parseLong((instrumento.getNumeroCupones()!=null) ? instrumento.getNumeroCupones() : "0" );
						ins.CodigoRubro = Long.parseLong((instrumento.getCodigoRubro()!=null && !instrumento.getCodigoRubro().equals("")) ? instrumento.getCodigoRubro() : "0");
						
						/*
						 * agregar cosas de los cuponess
						 */
						
						ins.FechaDevengo = instrumento.getFechaDevengo();
						ins.PeriodoPagoCupon = instrumento.getPeriodoPagoCupon();
						ins.TipoCalculoInteresesDevengado = Integer.parseInt(instrumento.getTipoCalculoInteresesDevengado());
						ins.TipoCalculoInteresesCupon = Integer.parseInt(instrumento.getTipoCalculoInteresesCupon());
						ins.IndicadorCuponeraVariable = instrumento.getIndicadorCuponeraVariable();
						ins.IndicadorValorizarendiaHabil = instrumento.getIndicadorValorizarendiaHabil();
						ins.ConversionDias = instrumento.getConversionDias();
						ins.AjustefindeMes = instrumento.getAjustefindeMes();
						ins.IndicadorMercadoLocal = instrumento.getIndicadorMercadoLocal();
						ins.TipodeReajuste = instrumento.getTipodeReajuste();
						ins.CUSIP = instrumento.getCUSIP();
						
						if(errores.length()>0) {
							mensaje = linea + " - INSTRUMENTO " + instrumento.getNemotecnico() + " NO INGRESADO, FALTAN CAMPOS OBLIGATORIOS: " + errores;
						}else {
							int[] res  = sql.batch(conn, SQL_INGRESAR_INSTRUMENTO, Arrays.asList(ins), EntidadInstrumento.class);
							if(res!=null && res[0]==1){
								EntidadBolsaInstrumento enBolsa = new EntidadBolsaInstrumento();
								enBolsa.IdBolsaInstrumento = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::BolsaInstrumento", (long) 1).get(0);;
								enBolsa.IdInstrumento = ins.IdInstrumento;
								
								enBolsa.IdBolsa = ConfiguracionMonedasBolsa.valueOf(ins.IdMoneda).getBolsa();
																
								res = sql.batch(conn, SQL_INGRESAR_BOLSA_INSTRUMENTO, Arrays.asList(enBolsa), EntidadBolsaInstrumento.class);
								if(res!=null && res[0]==1){
									
									if(ins.IndicadorCuponeraVariable!=null && ins.IndicadorCuponeraVariable.equalsIgnoreCase("S")) {
										//evaluar si instrumento es bono.
										EntidadInstrumentoBono bono = new EntidadInstrumentoBono(); 
										bono.IdInstrumento = ins.IdInstrumento;
										bono.FechaDevengo = ins.FechaDevengo;
										bono.AjusteFinDeMes = ins.AjustefindeMes;
										bono.CallConfirmado = "";
										bono.ConvencionDias = (ins.ConversionDias!=null) ? ins.ConversionDias : "";
										bono.CuponeraVariable = ins.IndicadorCuponeraVariable;
										bono.TasaEmision = ins.Tasa;
										bono.TipoCalculoInteresesCupon = ins.TipoCalculoInteresesCupon;
										bono.TipoCalculoInteresesDevengados = ins.TipoCalculoInteresesDevengado;
										bono.MercadoLocal = ins.IndicadorMercadoLocal;
										bono.MercadoExtrabursatil = "";
										bono.MercadoExtranjero = "";
										bono.PresenciaBursatil	 = "";
										bono.CUSIP = ins.CUSIP;										
										
										res = sql.batch(conn, SQL_INGRESAR_INSTRUMENTO_BONO, Arrays.asList(bono), EntidadInstrumentoBono.class);
									}
									
									if(res!=null && res[0]==1){
										mensaje = linea + " - INSTRUMENTO " + instrumento.getNemotecnico() + " INGRESADO CORRECTAMENTE";
										estado = true;
									}else {
										mensaje = linea + " - INSTRUMENTO " + instrumento.getNemotecnico() + " NO INGRESADO, NO SE PUDO INGRESAR INSTRUMENTO BONO";
									}
								}else{
									mensaje = linea + " - INSTRUMENTO " + instrumento.getNemotecnico() + " NO INGRESADO, NO SE PUDO INGRESAR LA BOLSA INSTRUMENTO";
								}
							}else{
								mensaje = linea + " - INSTRUMENTO " + instrumento.getNemotecnico() + " NO INGRESADO";
							}
						}
						
						
					} catch (Exception e) {
						mensaje = linea + " - INSTRUMENTO " + instrumento.getNemotecnico() + " ERROR GRAVE: "+e.getLocalizedMessage();
					}
					
					if(estado) conn.commit(); else conn.rollback();
					
					loaderUtils.log(conn, fechaSistema, codigoPais, "INSTRUMENTO", linea, mensaje, (estado)?"OK":"NOK", instrumento.toString());
					salida.add(mensaje);
					conn.commit();
				} catch (SQLException e1) {
					salida.add(instrumento.getLinea() + " ERROR DE CONEXION GRAVE: " + e1.getMessage());
					e1.printStackTrace();
				}
			});
		
		} catch (SQLException e1) {
			salida.add("ERROR AL OBTENER LLAVES");
		} catch (KProcedureException e1) {
			salida.add("ERROR AL OBTENER PARAMEETROS DE CONFIGURACION");
		}
		
		return salida;
	}

}
