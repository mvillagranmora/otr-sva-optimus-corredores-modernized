package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.normativa.interfaces.pojo.ParametrosGeneracionP40;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.utils.Utils;


public class GenerarInterfazP40_Scotia {
	
	private Boolean logRegistrado;	// Log registrado por validacion
	public static final String CODIGO_BANCO = "223";
	public static final String CODIGO_MODULO = "NOR";	
	
	public enum TipoRV {
	  CARTERA,
	  GARANTIA,
	  SIMULTANEA
	}
	
	public static class DatosRFIF {
		public Long 		IdCartera;
		public String 		IdClasificacionTipoInstrumento;
		public Long 		TipoCalculo;
		public Long 		TipoTasaPago;
		public Long 		TasaPago;
		public Long 		ComputoDeDias;
		public Long 		AnoBase;
		public String 		FechaInversion;
		public String 		RutEmisor;
		public String 		Familia;		
		public String 		Nemo;
		public String 		Nemotecnico;
		public String 		TipoRendimiento;
		public String 		PeriodoCupon;
		public String 		FechaUltimoCupon;
		public String 		FechaProximoCupon;
		public String 		FechaVencimientoInstrumento;
		public BigDecimal 	NominalInicial;
		public BigDecimal 	NominalActual;		
		public String 		MonedaNominal;
		public String		TipoTasaIF20;
		public BigDecimal	TasaEmision;
		public BigDecimal	Tera;
		public BigDecimal 	ValorPar;
		public String		TipoTasaIF24_29;
		public String 		TasaCompra25;
		public BigDecimal	CostoAdquisicion;
		public BigDecimal	TasaCompra27;		
		public BigDecimal	ValorRazonable;
		public String 		TasaValorizacion;
		public String 		SIGIRP40;
		public String 		BANESTADO3334;
		public BigDecimal	PrecioCompra;	
		public BigDecimal	DuracionModificada;
		public BigDecimal	Convexidad;	
		public String 		CondicionInstrumento;		
		public String 		FechaInicioCond;
		public String 		FechaTermCond;
		public String 		FechaVencCompAdj;
		public String 		Emisor;
		public String 		JerarquiaValorRazonable;
		public String		ValorDeterioroyFaseAumento;
		public String 		AplicacionRiesgoCrediticioBajo;
		public BigDecimal	CostoAdquisicionSegunContabilidad;
		public String 		Serie;
		public BigDecimal	Cambio;
		
		public DatosRFIF() {
			SIGIRP40 = "S";
			BANESTADO3334 = "S";
		}
	}
	
	public static class DatosFFMM {
		public String 		FechaInversion;
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;		
		public String 		RutAdminsitradorFondo;		
		public String 		Pais;		
		public String 		IdTipoFondo;
		public BigDecimal 	NumeroDeCuotas;
		public String 		Moneda;
		public BigDecimal 	ValorInicialCuota;
		public BigDecimal 	ValorCuota;
		public BigDecimal 	ValorRazonable;
		public String 		Nemotecnico;
		public String 		JerarquiaValorRazonable;
	}
	
	public static class DatosRV {
		public String 		FechaCompra;
		public String 		Nemotecnico;
		public String 		TipoCartera;
		public BigDecimal	NumeroInstrMant;		
		public String 		Moneda;
		public BigDecimal	PrecioCompra;
		public BigDecimal 	ValorRazonable;
		public String		CondicionInstrumento;
		public String 		FechaInicioCondicion;
		public String 		FechaFinCondicion;
		public String 		JerarquiaValorRazonable;
	}
	
	public static class DatoRUT {
		public String 		Rut;
		public Long 		IdCliente;
	}
	
	public static class DatoFilial {
		public String 		Rut;
		public Long 		IdCliente;
		public String 		NombreFilial;
		public String		CodigoTenedor;
		public String		NombreZip;
		public String		NombreArchivo;
	}	
	
	// Registra el inicio y fin del archivo LOG 
	public void registrarLog (Connection con, Long IdEmpresa, String msg, BufferedWriter bufferedWriter) throws IOException, StoredProcedureException {
		
		String fechaSistema = Utils.getFechaHoraSistema(con, "BSCL", IdEmpresa);	
		
		StringBuilder logRegister = new StringBuilder("");
		logRegister.append(fechaSistema);
		logRegister.append(" - ");
		logRegister.append(msg);
		
		writeInFile(logRegister.toString(), bufferedWriter);
	}

	// Registra el mensaje de validacion en el archivo LOG
	public void registrarMensajeLog (String msg, Boolean mostrarFecha, BufferedWriter bufferedWriter) throws IOException {
		
		StringBuilder logRegister = new StringBuilder("");
		logRegister.append(" - ");
		logRegister.append(msg);
		
		writeInFile(logRegister.toString(), bufferedWriter);
	}
	
	// Registra el texto en el archivo de la interfaz
	public void writeInFile(String msg, BufferedWriter bufferedWriter) throws IOException {
		bufferedWriter.write(msg);
		bufferedWriter.write("\r\n");
	}
	
	// Registra el texto en el archivo del Log de la interfaz
	public void writeInFileLog(String msg, String nemo, BufferedWriter bufferedWriter) throws IOException {
		StringBuilder log = new StringBuilder();
		log.append(nemo);
		log.append(": ");
		log.append(msg);
		
		bufferedWriter.write(log.toString());
		bufferedWriter.write("\r\n");
		logRegistrado = true;
	}	
	
	public void generarRFIF(DatosRFIF rfif, ParametrosGeneracionP40 parametrosP40) {
		BigDecimal 	Duracionmodif 				= rfif.DuracionModificada	;
		BigDecimal 	Convexidad 					= rfif.Convexidad			;
		String 		JerarquiaValorRazonable 	= "";
		
		rfif.TipoTasaIF20 		= calculoTipoTasaRF(rfif,"20"	,rfif.PeriodoCupon);
		rfif.TipoTasaIF24_29 	= calculoTipoTasaRF(rfif,"24_29",rfif.PeriodoCupon);
		
		String Emisor = rfif.RutEmisor.replace("-","");
		if (Emisor.length()==9 ) {
		Emisor = "0" + Emisor;
		}
		
		
		if (rfif.IdClasificacionTipoInstrumento.equalsIgnoreCase( "IF")) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate FechaHoy = LocalDate.parse(parametrosP40.FechaOperacionyyyymmdd, formatter);
			LocalDate FechaVencimiento = LocalDate.parse(rfif.FechaVencimientoInstrumento, formatter);		
			BigDecimal PlazoAVebcimiento = BigDecimal.valueOf(Math.abs(ChronoUnit.DAYS.between(FechaHoy,FechaVencimiento)));
	 
	        BigDecimal Duracion = PlazoAVebcimiento.divide(BigDecimal.valueOf(360), 6 , RoundingMode.HALF_UP); //duracion es anualizada los dias a la fecha de vencimiento / 360 (duracion de Macaulay)
	        BigDecimal TASA = new BigDecimal(rfif.TasaValorizacion.replace("+", "").replace("-", "")).divide(new BigDecimal(10000), 6 , RoundingMode.HALF_UP);  // se divide en 10000 por el formato en el que se usa para la interfaz
	        
	        Duracionmodif 	= (Duracion.divide( new BigDecimal(1).add( (TASA.divide(new BigDecimal (100)))  ), 6 , RoundingMode.HALF_UP  ))														.setScale(4, RoundingMode.DOWN)	;  
	        Convexidad 		= ((Duracionmodif.multiply( new BigDecimal(1).add(Duracion) )).divide( new BigDecimal(1).add(  TASA.divide(new BigDecimal (100)) ) , 6 , RoundingMode.HALF_UP  ))	.setScale(4, RoundingMode.DOWN)	; 
	        
	        //IMPBESTOPT-1391								Valor Ingerior 								Valor Superior
			//IMPBESTOPT-1391 PrecioInstrumento  (  (Valor Razonable/Valor par  * 99 ) +  (  Valor Razonable/Valor par * 101 )  )   / 2
			BigDecimal RangoIngerior; 
			BigDecimal RangoSuperior;
			if (rfif.ValorPar.compareTo(BigDecimal.ZERO) != 0) {
				RangoIngerior = rfif.ValorRazonable
			                    .divide(rfif.ValorPar, 4, RoundingMode.HALF_UP)
			                    .multiply(new BigDecimal(99));
				
				RangoSuperior = rfif.ValorRazonable
	                    .divide(rfif.ValorPar, 4, RoundingMode.HALF_UP)
	                    .multiply(new BigDecimal(101));
			} else {
				RangoIngerior = BigDecimal.ZERO; 
				RangoSuperior = BigDecimal.ZERO;
			}
			rfif.PrecioCompra			= (RangoIngerior.add(RangoSuperior)).divide(new BigDecimal(2),4,RoundingMode.HALF_UP) ;
		}
		
		JerarquiaValorRazonable = "00".equals(parametrosP40.JerarquiaValorRazonable) ? "0" : "1";
	
		if (rfif.Familia.equalsIgnoreCase("10") && rfif.CondicionInstrumento.equalsIgnoreCase("1")) {
		    String tasaStr = rfif.TasaCompra25.trim();
		    String signoFinal = tasaStr.substring(tasaStr.length() - 1); // "+" o "-"
		    String parteNumerica = tasaStr.substring(0, tasaStr.length() - 1); 
		    String conDecimal = parteNumerica.substring(0, 2) + "." + parteNumerica.substring(2);
		    BigDecimal tasaDecimal = new BigDecimal(conDecimal);
		    BigDecimal resultado = tasaDecimal.multiply(BigDecimal.valueOf(12)); // Anualizacion de la tasa
		    resultado = resultado.setScale(4, RoundingMode.HALF_UP); // 4 decimales
		    String resultadoStr = resultado.movePointRight(4).toPlainString().replace(".", "");
		    rfif.TasaCompra25 = resultadoStr + signoFinal;
		}
		if ( rfif.CondicionInstrumento.equalsIgnoreCase("3")  && rfif.CondicionInstrumento.equalsIgnoreCase("1")) {
			Map<String, Integer> codigoAFrecuencia = new HashMap<>();
			codigoAFrecuencia.put("0", 0);   // Cero cupón
			codigoAFrecuencia.put("1", 1);   // Mensual
			codigoAFrecuencia.put("2", 3);   // Trimestral
			codigoAFrecuencia.put("3", 4);   // Cuatrimestral
			codigoAFrecuencia.put("4", 6);   // Semestral
			codigoAFrecuencia.put("5", 12);  // Anual
 														
			int mesesASumar = codigoAFrecuencia.getOrDefault(rfif.PeriodoCupon, 0);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate fechaProximoCupon = LocalDate.parse(rfif.FechaProximoCupon, formatter);
 
			LocalDate nuevaFecha = fechaProximoCupon.plusMonths(mesesASumar);
			rfif.FechaProximoCupon = nuevaFecha.format(formatter);
		}
		
		
		if (rfif.ValorPar == null){rfif.ValorPar = new BigDecimal(0);}	

		try {
			if ( rfif.ValorPar.compareTo( rfif.NominalActual  ) < 0  || rfif.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") ) {// Si el valor par es menor al nominal actual 
				rfif.ValorPar =  rfif.NominalInicial  ;
			}
			if (rfif.MonedaNominal.equalsIgnoreCase("998")  ) { 
				try {rfif.ValorPar = rfif.ValorPar.multiply(rfif.Cambio).setScale(0, RoundingMode.HALF_UP); ; } catch (Exception e) {}
				//System.out.println("UF convertida :"  + rfif.ValorPar + " CLP : " + rfif.ValorPar.multiply(rfif.Cambio).toString() );
			}
		}catch (Exception e){}
		
		if (!rfif.Familia.equalsIgnoreCase("04") && !rfif.Familia.equalsIgnoreCase("06")) {
			try { rfif.PrecioCompra = (rfif.ValorRazonable.divide(rfif.ValorPar, 6 , RoundingMode.HALF_UP)).multiply(new BigDecimal(100));	}catch (Exception e){}
		}
		
		

		StringBuilder reg1 = new StringBuilder("");
		reg1.append("01");															// WS-R1-CAMPO-01 9(02) 	Tipo de registro    - (Valor fijo)
		reg1.append(parametrosP40.CodigoTenedor);									// WS-R1-CAMPO-02 9(04) 	Codigo tenedor 	   - (Valor fijo) Parametro = 277 	
		reg1.append(parametrosP40.FechaOperacionyyyymmdd);							// WS-R1-CAMPO-03 F(08) 	Fecha de proceso	   - Parametro
		reg1.append(rfif.FechaInversion);											// WS-R1-CAMPO-04 F(08) 	Fecha inversion 	   - FechaOperacion de BSCL.Mesa.BD::MES.Operacion
		reg1.append(parametrosP40.TipoCartera);										// WS-R1-CAMPO-05 9(01) 	Tipo cartera 	   - (Valor fijo) Parametro = 0 
		reg1.append(Emisor);														// WS-R1-CAMPO-06 R(09)		+VX(01) = (10) 	RUT Emisor		   - DocumentoIdentidad de BSCL.SocioNegocio.BD::PER.DocumentoIdentidad		
		reg1.append("160");															// WS-R1-CAMPO-07 9(03) 	Pais				   - (Valor fijo)
		reg1.append(rfif.Familia);													// WS-R1-CAMPO-08 (02) 		Familia			
		reg1.append(String.format("%-20.20s", rfif.Nemo));							// WS-R1-CAMPO-09 (20) 		Nemo				   - Nemotecnico o NemoSVS de BSCL.Custodia.BD::CUS.Cartera		
		reg1.append(rfif.TipoRendimiento);											// WS-R1-CAMPO-10 9(01) 	Tipo rendimiento	
		reg1.append(rfif.PeriodoCupon);												// WS-R1-CAMPO-11 9(01) 	Periodo cupon	   - FrecuenciaCupon de BSCL.Plataforma.BD::INS.Instrumento
		reg1.append(rfif.FechaUltimoCupon);											// WS-R1-CAMPO-12 F(08) 	Fecha ult cupon 	   - INGFechaCorteCupon de BSCL.Custodia.BD::CUS.Cartera 
		reg1.append(rfif.FechaProximoCupon);										// WS-R1-CAMPO-13 F(08) 	Fecha prox cupon    - INGFechaPagoCorteCupon de BSCL.Custodia.BD::CUS.Cartera
		reg1.append(rfif.FechaVencimientoInstrumento);								// WS-R1-CAMPO-14 F(08) 	Fecha venc instr    - FechaVencimientoInstrumento de BSCL.Custodia.BD::CUS.Cartera
		reg1.append("01");															// WS-R1-CAMPO-15 9(02) 	Derivados 		   - (Valor fijo)
		reg1.append(getFormatAmount(rfif.NominalInicial, 18));						// WS-R1-CAMPO-16 9(14,4) 	Nominal inicial   -  Valor Rescate si es if si no ingcantidad
		reg1.append(getFormatAmount(rfif.NominalActual, 18));						// WS-R1-CAMPO-17 9(14,4)	 Nominal actual    -  Valor Rescate si es if si no ingcantidad	
		reg1.append(rfif.MonedaNominal);											// WS-R1-CAMPO-18 9(03) 	Moneda nominal 
		reg1.append(rfif.MonedaNominal);											// WS-R1-CAMPO-19 9(03) 	Moneda reajuste	   - Igual a Moneda nominal		
		reg1.append(rfif.TipoTasaIF20);												// WS-R1-CAMPO-20 9(07) 	Tipo tasa		   - Formula con los campos WS-CAMPO-1 AL 7
		reg1.append(getFormatAmount(rfif.TasaEmision,6) );							// WS-R1-CAMPO-21 9(2,4) 	Tasa emision		 
		reg1.append(getFormatAmount(rfif.Tera, 6));									// WS-R1-CAMPO-22 9(2,4) 	Tera				   - Tera de BSCL.Plataforma.BD::INS.Instrumento
		reg1.append(getFormatAmount(rfif.ValorPar,18));								// WS-R1-CAMPO-23 9(14,4) 	Valor par
		reg1.append(rfif.TipoTasaIF24_29);											// WS-R1-CAMPO-24 9(07) 	Tipo de tasa compra - Formula con los campos WS-CAMPO-1 AL 7	
		reg1.append(String.format("%7s", rfif.TasaCompra25).replace(' ', '0'));												// WS-R1-CAMPO-25 s9(02)v9(04)=(6) -> 7 por formato 	Tasa compra	??		
		reg1.append(String.format("%014d", rfif.CostoAdquisicion.toBigInteger()).substring(0, 14));	// WS-R1-CAMPO-26 9(14) Costo adquisicion   - CostoInversionEnFactorAjuste de BSCL.Custodia.BD::CUS.Cartera
		reg1.append("00000000000000");												// WS-R1-CAMPO-27 9(14) Costo amortizado	   - INGTasaMercadoActual de BSCL.Custodia.BD::CUS.Cartera
		reg1.append(String.format("%014d", rfif.ValorRazonable.toBigInteger()).substring(0, 14));	// WS-R1-CAMPO-28 9(14) Valor razonable	   - INGMontoATasaMercadoActual de BSCL.Custodia.BD::CUS.Cartera
		reg1.append(rfif.TipoTasaIF24_29);											// WS-R1-CAMPO-29 9(07) 	Tipo tasa		   - 
		reg1.append(rfif.TasaValorizacion);											// WS-R1-CAMPO-30 9(07) 	Tasa valorizacion   - INGTasaValorRazonableActivo de BSCL.Custodia.BD::CUS.Cartera
		reg1.append("3");															// WS-R1-CAMPO-31 9(01) 	Tipo valorizacion   - (Valor fijo)
		reg1.append(getFormatAmount(rfif.PrecioCompra, 10));						// WS-R1-CAMPO-32 9(6,4) 	Precio compra		
		reg1.append(getFormatAmount(Duracionmodif, 6));								// WS-R1-CAMPO-33 9(2,4) 	Duracion modif	   - INGConvexidadCompraActual de BSCL.Custodia.BD::CUS.Cartera			
		reg1.append(getFormatAmount(Convexidad, 8));								// WS-R1-CAMPO-34 9(4,4) 	Convexidad		   - INGDurationCompraActual de BSCL.Custodia.BD::CUS.Cartera				
		reg1.append("00000000000000");												// WS-R1-CAMPO-35 9(14) 	Valor deterioro	   - (Valor fijo)
		reg1.append(rfif.CondicionInstrumento);										// WS-R1-CAMPO-36 9(01) 	Condicion instr	   - Consulta de Cartera (1), Pactos (2) y Garantia (3)   
		reg1.append(rfif.FechaInicioCond);											// WS-R1-CAMPO-37 9(08) 	Fecha inicio cond   - FechaOperacion de BSCL.Mesa.BD::MES.Operacion
		reg1.append(rfif.FechaTermCond);											// WS-R1-CAMPO-38 9(08) 	Fecha term cond 	   - FechaVencimiento de BSCL.Mesa.BD::MES.Operacion
		reg1.append(JerarquiaValorRazonable);										// WS-R1-CAMPO-39 9(01) 	Jerarquia del valor razonable
		reg1.append("0");															// WS-R1-CAMPO-40 9(01) 	Valor de deterioro y su fase de aumento en el riesgo de credito
		reg1.append("0");															// WS-R1-CAMPO-41 9(01) 	Aplicacion del supuesto que el instrumento tiene un riesgo crediticio bajo
		reg1.append("00000000000000");												// WS-R1-CAMPO-42 9(14) 	Costo de adquisicion actualizado segun la contabilidad
		//reg1.append("00");														// WS-R1-CAMPO-42 (01) 		Costo de adquisicion actualizado segun la contabilidad
		// Escribe en archivo
		try {
			writeInFile(reg1.toString(), parametrosP40.Archivo);
		
		
		// Validacion campos de RF e IF
		validarCamposRFIF(rfif, parametrosP40.FechaOperacionyyyymmdd, parametrosP40.TipoCartera, parametrosP40.ArchivoLog);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<DatosRFIF>  ObtenerFechasHabilesRFIF(Connection con,List<DatosRFIF> data, String FechaInterfaz) throws Exception {
		// para las fechas de ultimo y proximo cupon se calcula si la fecha es habil y se retorna la fecha habil o habil siguiente usando ObtenerDiaSiguiente.java
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
		List<String> FechasDistintas = new ArrayList<String>();
		for (DatosRFIF dato : data) {		
		    if (!FechasDistintas.contains(dato.FechaUltimoCupon)&& !dato.FechaUltimoCupon.equalsIgnoreCase("00000000")   ) 	{   FechasDistintas.add(formato.format(new SimpleDateFormat("yyyyMMdd").parse(dato.FechaUltimoCupon))	);		}
		    if (!FechasDistintas.contains(dato.FechaProximoCupon)&& !dato.FechaProximoCupon.equalsIgnoreCase("00000000") ) 	{	FechasDistintas.add(formato.format(new SimpleDateFormat("yyyyMMdd").parse(dato.FechaProximoCupon))	);	}
		    if (!FechasDistintas.contains(dato.FechaVencimientoInstrumento)&& !dato.FechaVencimientoInstrumento.equalsIgnoreCase("00000000") ) 	{	FechasDistintas.add(formato.format(new SimpleDateFormat("yyyyMMdd").parse(dato.FechaVencimientoInstrumento))	);	}
		    if (!FechasDistintas.contains(dato.FechaInversion)&& !dato.FechaInversion.equalsIgnoreCase("00000000")   ) 	{   FechasDistintas.add(formato.format(new SimpleDateFormat("yyyyMMdd").parse(dato.FechaInversion))	);		}
		}

		List<FechasIn> fechas = FechasDistintas.stream().map(fecha -> {
			        FechasIn fechaa = new FechasIn();
			        fechaa.fechaInicial = fecha;
			        fechaa.numeroDias = 0L;
			        fechaa.pais = 44L;
			        return fechaa;
			    }).collect(Collectors.toList());
		
		InObtenerDiaSiguiente IN = new InObtenerDiaSiguiente();
		IN.p_idPais = 44L;
		IN.p_diaHabil= "N";					//al sumar dias, considera solo dias habiles
		IN.p_soloCaeDiaHabil = "S";			//el resultado debe ser un dia habil
		IN.p_fechas = fechas;			 
		ObtenerDiaSiguiente o = new ObtenerDiaSiguiente();					
		OutObtenerDiaSiguiente  salidaDias = o.logica(con, IN);
		
		Map<String, String> fechaCalculadaMap = salidaDias.p_out_fechas.stream().collect(Collectors.toMap(fechasOut -> fechasOut.fechaInicial, fechasOut -> fechasOut.fechaCalculada, (fecha1, fecha2) -> fecha1));
		Map<String, String> updatedMap = new HashMap<>();
		for (Map.Entry<String, String> entry : fechaCalculadaMap.entrySet()) {
		    String oldKey = entry.getKey();
		    String value = entry.getValue();
		    String newKey = oldKey.replace("-", "");
		    updatedMap.put(newKey, value);
		}
		fechaCalculadaMap = updatedMap;
		
		for (DatosRFIF rfif : data) {
			if (!rfif.FechaUltimoCupon.equalsIgnoreCase("00000000") ) {	
				String fechacalculada  = fechaCalculadaMap.get(rfif.FechaUltimoCupon ); 
				if (fechacalculada != null) {	rfif.FechaUltimoCupon = fechacalculada.replaceAll("-", "");	}
			}	
			if (!rfif.FechaProximoCupon.equals("00000000")) {
				String fechacalculada  = fechaCalculadaMap.get(rfif.FechaProximoCupon) ; 
				if (fechacalculada != null) {	rfif.FechaProximoCupon = fechacalculada.replaceAll("-", "");	}
			}
			if (!rfif.FechaVencimientoInstrumento.equals("00000000")) {
				String fechacalculada  = fechaCalculadaMap.get(rfif.FechaVencimientoInstrumento) ; 
				if (fechacalculada != null) 	{	rfif.FechaVencimientoInstrumento = fechacalculada.replaceAll("-", "");	}
			} 
			if (!rfif.FechaInversion.equals("00000000")) {
				String fechacalculada  = fechaCalculadaMap.get(rfif.FechaInversion) ; 
				if (fechacalculada != null) 	{ 	rfif.FechaInversion = fechacalculada.replaceAll("-", "");		}
			} 
		}


		data = data.stream().map(dataItem -> {
			/*
			if (dataItem.Nemo.equalsIgnoreCase("BN060722")) {
				System.out.print("aaaaaaaaaaa");
			}
			*/  // if para debug 

		    
		 try {
		 LocalDate date1 = LocalDate.parse(FechaInterfaz);
		 LocalDate date2 = LocalDate.parse(dataItem.FechaVencimientoInstrumento,DateTimeFormatter.ofPattern("yyyyMMdd") );
		 BigDecimal daysBetween = BigDecimal.valueOf(ChronoUnit.DAYS.between(date1, date2));
		    
		if (dataItem.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
		  MathContext mc = new MathContext(5, RoundingMode.HALF_UP);
		  String numericValue = dataItem.TasaValorizacion.replaceAll("[^\\d]", ""); // tasa valorizacion es tasa mercado
		  int numericValueLength = numericValue.length();
		  int scale = 4; 
		  String numericValueWithDecimal = numericValue.substring(0, numericValueLength - scale) + "." + numericValue.substring(numericValueLength - scale);
		  BigDecimal TasaValBD = new BigDecimal(numericValueWithDecimal);
		  if (dataItem.TasaValorizacion.endsWith("-")) {   TasaValBD = TasaValBD.negate();	}
		  dataItem.ValorPar = dataItem.NominalInicial ;
		  //IMPBESTOPT-1203     [   VP/(1+(X)*(Y)) ]       /   VR*100
		  BigDecimal X = TasaValBD.divide( new BigDecimal("100"),mc);
		  BigDecimal Y = daysBetween.divide(new BigDecimal("360"),mc) ;
		  BigDecimal W = BigDecimal.ONE.add(X.multiply(Y))  ;
		  dataItem.PrecioCompra = dataItem.NominalInicial.divide( W,mc )    ; // VP/(1+(i/100)*(t/360))
		  dataItem.PrecioCompra = dataItem.PrecioCompra.divide(dataItem.NominalInicial,mc).multiply(new BigDecimal("100"))  ;
		  }	
		 }catch (Exception e) {
			 dataItem.PrecioCompra = BigDecimal.ZERO;
			 System.out.println(e);
		 }
		    return dataItem;
		}).collect(Collectors.toList());

		
		
		return data;
	}
	
	
	
	public void generarFFMM(DatosFFMM fm, ParametrosGeneracionP40 parametrosP40) throws IOException { 
		
		// Estructura de datos de linea R2 - Fondos Mutuos
		String AdminFondo = fm.RutAdminsitradorFondo.replace("-","");
		if (AdminFondo.length()==9 ) {
		AdminFondo = "0" + AdminFondo;
		}

		String JerarquiaValorRazonable = "00".equals(parametrosP40.JerarquiaValorRazonable) ? "0" : "1";

		StringBuilder reg2 = new StringBuilder("");
		reg2.append("02");															// WS-R2-CAMPO-01 (02) Tipo de registro  	- (Valor fijo) 
		reg2.append(parametrosP40.CodigoTenedor);								// WS-R2-CAMPO-02 (04) Codigo tenedor 	   	- (Valor fijo) Parametro = 105
		reg2.append(parametrosP40.FechaOperacionyyyymmdd);							// WS-R2-CAMPO-03 (08) Fecha de proceso	   	- Parametro	
		reg2.append(fm.FechaInversion);												// WS-R2-CAMPO-04 (08) Fecha Inversion	   	- FechaOperacion de BSCL.Custodia.BD::CUS.Cartera
		reg2.append(AdminFondo);										// WS-R2-CAMPO-05 (10) Rut Admin. del fondo - 
		reg2.append(fm.Pais);														// WS-R2-CAMPO-06 (03) Pais del fondo  	   	- IdPaisOrigen de BSCL.FondoMutuo.BD::FMM.Fondo			
		reg2.append(fm.IdTipoFondo);												// WS-R2-CAMPO-07 (01) Tipo de fondo  	 	- IdTipoFondo de BSCL.FondoMutuo.BD::FMM.Fondo			
		reg2.append(String.format("%014d", fm.NumeroDeCuotas.toBigInteger()));		// WS-R2-CAMPO-08 (14) Numero de cuotas	 	- INGCantidad de BSCL.Custodia.BD::CUS.Cartera			
		reg2.append(fm.Moneda);														// WS-R2-CAMPO-09 (03) Moneda			 	- IdMonedaInstrumento de BSCL.Custodia.BD::CUS.Cartera			
		reg2.append(String.format("%014d", fm.ValorInicialCuota.toBigInteger()));	// WS-R2-CAMPO-10 (14) Valor Inicial Cuota 	- INGPrecioCompra de BSCL.Custodia.BD::CUS.Cartera
		reg2.append(String.format("%014d", fm.ValorCuota.toBigInteger()));			// WS-R2-CAMPO-11 (14) Valor Cuota 			- INGPrecioPromedioCompra de BSCL.Custodia.BD::CUS.Cartera 
		reg2.append(String.format("%014d", fm.ValorRazonable.toBigInteger()).substring(0, 14));		// WS-R2-CAMPO-12 (14) Valor Razonable 		- INGMontoATasaMercadoActual de BSCL.Custodia.BD::CUS.Cartera	
		reg2.append("1");															// WS-R2-CAMPO-13 (01) Tipo Cartera
		reg2.append(JerarquiaValorRazonable);							// WS-R2-CAMPO-14 (01) Jerarquia del valor razonable
		reg2.append(String.format("%211s", " "));									// Filler (211)
		
		// Escribe en archivo
		writeInFile(reg2.toString(), parametrosP40.Archivo);
		
		// Validacion campos Fondos Mutuos
		validarCamposFM(fm, parametrosP40.ArchivoLog);
	}
	
	public void generarRV(DatosRV rv, ParametrosGeneracionP40 parametrosP40, TipoRV tipo) throws IOException {
		
		// Inicializa los valores para la cartera
		String campo12 = "00000000", campo13 = "00000000", condicionInstrumento = "1";
		if (tipo==TipoRV.GARANTIA) { 
			campo12 = parametrosP40.FechaOperacionyyyymmdd;
			campo13 = parametrosP40.FechaOperacionyyyymmdd;
			condicionInstrumento = "3";			
		} else if (tipo==TipoRV.SIMULTANEA) { 
			campo12 = rv.FechaInicioCondicion;
			campo13 = rv.FechaFinCondicion;
			condicionInstrumento = "9";			
		}
		
		String JerarquiaValorRazonable = "00".equals(parametrosP40.JerarquiaValorRazonable) ? "0" : "1";
		
		// Estructura de datos de linea R3 - Renta Variable en Custodia (Instrumentos de Capital)
		StringBuilder reg3 = new StringBuilder("");
		reg3.append("03");														// WS-R3-CAMPO-01 (02) Tipo de registro  	- (Valor fijo)
		reg3.append(parametrosP40.CodigoTenedor);							// WS-R3-CAMPO-02 (04) Codigo tenedor 	   	- (Valor fijo) Parametro = 277
		reg3.append(parametrosP40.FechaOperacionyyyymmdd);						// WS-R3-CAMPO-03 (08) Fecha de proceso	   	- 	
		reg3.append(rv.FechaCompra);											// WS-R3-CAMPO-04 (08) Fecha de compra	   	- FechaOperacion de BSCL.Custodia.BD::CUS.Cartera
		reg3.append(String.format("%-14.14s", rv.Nemotecnico));					// WS-R3-CAMPO-05 (14) Nemotecnico		 	- Nemotecnico de BSCL.Custodia.BD::CUS.Cartera
		reg3.append(rv.TipoCartera);											// WS-R3-CAMPO-06 (01) Tipo de cartera 	   	- (Valor fijo) 1: Instrumentos para negociacion		
		reg3.append(String.format("%014d", rv.NumeroInstrMant.toBigInteger()).substring(0, 14));	// WS-R3-CAMPO-07 (14) Num de Instr Manten 	- INGCantidad de BSCL.Custodia.BD::CUS.Cartera		
		reg3.append(rv.Moneda);													// WS-R3-CAMPO-08 (03) Moneda			 	- IdMonedaInstrumento de BSCL.Custodia.BD::CUS.Cartera		
		reg3.append(String.format("%014d", rv.PrecioCompra.toBigInteger()));	// WS-R3-CAMPO-09 (14) Precio Compra	 	- INGPrecioCompra de BSCL.Custodia.BD::CUS.Cartera			
		reg3.append(  String.format("%014d", rv.ValorRazonable.setScale(0, RoundingMode.HALF_UP)  .toBigInteger( )).substring(0, 14)   );	// WS-R3-CAMPO-10 (14) Valor Razonable	 	- INGMontoATasaMercadoActual de BSCL.Custodia.BD::CUS.Cartera
		reg3.append(condicionInstrumento);										// WS-R3-CAMPO-11 (01) Cond del instrumento	- Sin restricciones para su venta o cesion 
		reg3.append(campo12);													// WS-R3-CAMPO-12 (08) Fecha Inicio Condic	- FechaEmisionInstrumento de BSCL.Custodia.BD::CUS.Cartera	
		reg3.append(campo13);													// WS-R3-CAMPO-13 (08) Fecha Fin Condicion	- FechaVencimientoInstrumento de BSCL.Custodia.BD::CUS.Cartera			
		reg3.append(JerarquiaValorRazonable);									// WS-R3-CAMPO-14 (01) Jerarquia del valor razonable
		reg3.append(String.format("%208s", " "));								// Filler (208)
		
		/*
		 * WS-R3-CAMPO-11: CONDICION DEL INSTRUMENTO. Indica la condicion en que se encuentran los
		 * instrumentos, segun los siguientes codigos: 
		 * 
		 * 1 Sin restricciones para su venta o cesion 
		 * 3 Entregado en garantia 
		 * 9 Otras condiciones que impiden su venta o cesion.
		 * 
		 * FECHA INICIO CONDICION Y FECHA FINAL CONDICION
		 *  Para aquellos instrumentos sin restricciones (codigo 1), el campo se llenara
         *  con ceros.
		 */
		
		// Escribe en archivo
		writeInFile(reg3.toString(), parametrosP40.Archivo);

		// Validacion campos Renta Variable
		validarCamposRV(rv, parametrosP40.ArchivoLog);
	}	
	
	// Validar Campos de Renta Fija e Intermediacion Financiera
	public void validarCamposRFIF(DatosRFIF rfif, String fecha, String TipoCartera, BufferedWriter bufferedWriterLog) throws IOException {
		
		logRegistrado = false;
		
		// Valida campo WS-R1-CAMPO-10 - Tipo rendimiento
		if(Integer.parseInt(rfif.TipoRendimiento) > 4 && Integer.parseInt(rfif.TipoRendimiento) < 9) {
			writeInFileLog("ERROR, Registro 1 Campo 10 (Tipo de rendimiento), valor debe estar entre 1 y 3 o 9", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campos WS-R1-CAMPO-11 - Periodo cupon, WS-R1-CAMPO-12 - Fecha ult cupon y 
		// Valida WS-R1-CAMPO-12 - Fecha ultimo cupon y WS-R1-CAMPO-13 - Fecha prox cupon para el caso que el rendimiento sea 1
		if(Integer.parseInt(rfif.TipoRendimiento) == 1) {
			if(Integer.parseInt(rfif.PeriodoCupon) > 0) {
				writeInFileLog("ERROR, Registro 1 Campo 11 (Periocidad del cupon), valor debe ser 0 cuando el campo 10 (Tipo de rendimiento) es 1", rfif.Nemo, bufferedWriterLog);
			}
			if(Integer.parseInt(rfif.FechaUltimoCupon) > 0) {
				writeInFileLog("ERROR, Registro 1 Campo 12 (Fecha ultimo corte cupon), valor debe ser 0 cuando el campo 10 (Tipo de rendimiento) es 1", rfif.Nemo, bufferedWriterLog);
			}
			if(Integer.parseInt(rfif.FechaProximoCupon) > 0) {
				writeInFileLog("ERROR, Registro 1 Campo 13 (Fecha proximo corte cupon), valor debe ser 0 cuando el campo 10 (Tipo de rendimiento) es 1", rfif.Nemo, bufferedWriterLog);
			}
		}
		
		// Valida campos WS-R1-CAMPO-11 - Periodo cupon
		if(Integer.parseInt(rfif.TipoRendimiento) > 5 && Integer.parseInt(rfif.TipoRendimiento) < 9) {
			writeInFileLog("ERROR, Registro 1 Campo 11 (Periocidad del cupon), valor debe estar entre 0 y 5 o 9", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campos WS-R1-CAMPO-16 - Nominal inicial y WS-R1-CAMPO-17 - Nominal actual
		if(Integer.parseInt(rfif.TipoRendimiento) == 0) {
			if(rfif.NominalInicial.compareTo(rfif.NominalActual) == 0 ) {
				writeInFileLog("ERROR, Registro 1 Campo 16 (Nominal inicial) y 17 (Nominal actual) deben ser igual cuando Campo 11 (Periocidad del cupon) es igual 0", rfif.Nemo, bufferedWriterLog);
			}
			// Valida campo WS-R1-CAMPO-14 - Fecha venc instr
			if(Integer.parseInt(fecha) > Integer.parseInt(rfif.FechaVencimientoInstrumento)) {
				writeInFileLog("ERROR, Registro 1 Campo 3 (Fecha de proceso) debe ser menor al Campo 14 (Fecha vencimiento instrumento) cuando Campo 11 (Periocidad del cupon) es igual a 0", rfif.Nemo, bufferedWriterLog);
			}
			
		}else {
			// Validacion de campo WS-R1-CAMPO-11 - Periodo cupon
			if (rfif.PeriodoCupon.equals(rfif.TipoTasaIF20.substring(2,3))) {
				writeInFileLog("ERROR, Registro 1 Campo 11 (Periocidad del cupon), el tercer digito del Campo 20 (Tipo de tasa de emision) debe ser igual al Campo 11 (Periocidad del cupon)", rfif.Nemo, bufferedWriterLog);
			}
		}
		
		// Valida campo WS-R1-CAMPO-16 - Nominal inicial
		if (rfif.NominalInicial.compareTo(BigDecimal.ZERO)==0) {
			writeInFileLog("ERROR, Registro 1 Campo 16 (Nominal inicial), valor debe ser distinto de 0", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campo WS-R1-CAMPO-17 - Nominal actual
		if (rfif.NominalActual.compareTo(BigDecimal.ZERO)==0) {
			writeInFileLog("ERROR, Registro 1 Campo 17 (Nominal actual), valor debe ser distinto de 0", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campo WS-R1-CAMPO-17 - Nominal actual
		if(rfif.NominalActual.compareTo(rfif.NominalInicial) > 0) {
			writeInFileLog("ERROR, Registro 1 Campo 17 (Nominal actual), valor debe ser menor que el Campo 16 (Nominal inicial)", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campo WS-R1-CAMPO-12 - Fecha ult cupon
		if(!(Integer.parseInt(rfif.FechaUltimoCupon)==0) && (Integer.parseInt(fecha) < Integer.parseInt(rfif.FechaUltimoCupon))) {
				writeInFileLog("ERROR, Registro 1 Campo 12 (Fecha ultimo corte cupon), debe ser menor que Campo 3 (Fecha proceso)", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campo WS-R1-CAMPO-13 - Fecha prox cupon
		if(Integer.parseInt(rfif.FechaProximoCupon) != 0) {
			if(Integer.parseInt(rfif.FechaProximoCupon) < Integer.parseInt(rfif.FechaUltimoCupon)) {
				writeInFileLog("ERROR, Registro 1 Campo 13 (Fecha proximo corte cupon), debe ser mayor que Campo 12 (Fecha ultimo corte cupon)", rfif.Nemo, bufferedWriterLog);
			}
		}
		
		// Valida campo WS-R1-CAMPO-14 - Fecha venc instr
		if(Integer.parseInt(rfif.FechaVencimientoInstrumento) != 0) {
			if(Integer.parseInt(rfif.FechaProximoCupon) > Integer.parseInt(rfif.FechaVencimientoInstrumento)) {
				writeInFileLog("ERROR, Registro 1 Campo 14 (Fecha vencimiento instrumento), debe ser mayor o igual que el Campo 13 (Fecha proximo corte cupon)", rfif.Nemo, bufferedWriterLog);
			}
		}

		// Valida campo WS-R1-CAMPO-20 - Tipo tasa
		if(rfif.TipoTasaIF20.substring(0,1).equals("1")) {
			if(!(rfif.TipoTasaIF20.substring(4,5).equals("0") || rfif.TipoTasaIF20.substring(5,6).equals("0") || rfif.TipoTasaIF20.substring(6,7).equals("0"))) {
				writeInFileLog("ERROR, Registro 1 Campo 20 (Tipo de tasa de emision), los digitos 5,6,7 deben ser igual a 0 cuando el digito 1 es igual a 1", rfif.Nemo, bufferedWriterLog);
			}
		}else {
			if(rfif.TipoTasaIF20.substring(4,5).equals("0") || rfif.TipoTasaIF20.substring(5,6).equals("0") || rfif.TipoTasaIF20.substring(6,7).equals("0")) {
				writeInFileLog("ERROR, Registro 1 Campo 20 (Tipo de tasa de emision), los digitos 5,6,7 deben ser distinto a 0 cuando el digito 1 es distinto a 1", rfif.Nemo, bufferedWriterLog);
			}
		}

		// Valida campo WS-R1-CAMPO-21 - Tasa emision
		if(rfif.MonedaNominal.equals("999") && rfif.Familia.equals("10") && !rfif.TasaEmision.equals(BigDecimal.ZERO) && !(rfif.IdClasificacionTipoInstrumento.equals("IF"))) {
				writeInFileLog("ERROR, Registro 1 Campo 21 (Tasa de emision), debe ser mayor que 0 cuando Campo 18 (Moneda del nominal) y Campo 19 (Moneda de reajuste) es igual 999 y Campo 8 (Familia de instrumentos) es igual 10", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campo WS-R1-CAMPO-22 - Tera
		if(rfif.Tera.compareTo(BigDecimal.ZERO) < 0) {
			writeInFileLog("ERROR, Registro 1 Campo 22 (Tera), debe ser mayor igual a 0", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campo WS-R1-CAMPO-24 - Tipo de tasa compra
		if(rfif.TipoTasaIF24_29.substring(0,1).equals("1")) { 
			if(!(rfif.TipoTasaIF24_29.substring(4,5).equals("0") || rfif.TipoTasaIF24_29.substring(5,6).equals("0") || rfif.TipoTasaIF24_29.substring(6,7).equals("0"))) {
				writeInFileLog("ERROR, Registro 1 Campo 24 (Tipo de tasa de compra), los digitos 5,6,7 deben ser igual a 0 cuando el digito 1 es igual a 1", rfif.Nemo, bufferedWriterLog);
			}
		} else {
			if(rfif.TipoTasaIF24_29.substring(4,5).equals("0") || rfif.TipoTasaIF24_29.substring(5,6).equals("0") || rfif.TipoTasaIF24_29.substring(6,7).equals("0")) {
				writeInFileLog("ERROR, Registro 1 Campo 24 (Tipo de tasa de compra), los digitos 5,6,7 deben ser distinto a 0 cuando el digito 1 es distinto a 1", rfif.Nemo, bufferedWriterLog);
			}
		}
		
		// Valida campo WS-R1-CAMPO-25 - Tasa compra
		if(Integer.parseInt(rfif.TasaCompra25.replaceAll("[+-]", "").substring(0,5)  ) == 0 ) {
			writeInFileLog("ERROR, Registro 1 Campo 25 (Tasa de compra), debe ser distinto de 0", rfif.Nemo, bufferedWriterLog);
			
		}else {
			if(rfif.MonedaNominal.equals("999") && Integer.parseInt(rfif.TasaCompra25.substring(1,4)) < 0) {
				writeInFileLog("ERROR, Registro 1 Campo 25 (Tasa de compra), debe ser mayor que 0 cuando Campo 18 (Moneda del nominal) y Campo 19 (Moneda de reajuste) es igual a 999", rfif.Nemo, bufferedWriterLog);

			}
		}
		
		// Valida campo WS-R1-CAMPO-26 - Costo adquisicion 
		if(rfif.CostoAdquisicion.compareTo(BigDecimal.ZERO) == 0) {
			writeInFileLog("ERROR, Registro 1 Campo 26 (Costo de adquisicion), debe ser mayor que 0", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campo WS-R1-CAMPO-27 - Tasa compra
		if(TipoCartera.equals("3")) {
			if(rfif.TasaCompra27.compareTo(BigDecimal.ZERO) > 0 ) {
				writeInFileLog("ERROR, Registro 1 Campo 27 (Costo amortizado), debe ser mayor que 0 cuando Campo 5 (Tipo de cartera) es igual a 3", rfif.Nemo, bufferedWriterLog);
			}
		} else if(!(rfif.TasaCompra27.compareTo(BigDecimal.ZERO) == 0) ) {
				writeInFileLog("ERROR, Registro 1 Campo 27 (Costo amortizado), debe ser igual a 0 cuando Campo 5 (Tipo de cartera) es igual a 1 o 2", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campo WS-R1-CAMPO-28 - Valor razonable
		if(TipoCartera.equals("1")||TipoCartera.equals("2")||TipoCartera.equals("3")) {
			if(rfif.ValorRazonable.compareTo(BigDecimal.ZERO) == 0) {
				writeInFileLog("ERROR, Registro 1 Campo 28 (Valor razonable), debe ser mayor que 0 cuando Campo 5 (Tipo de cartera) esta entre 1 y 3", rfif.Nemo, bufferedWriterLog);
			}
		}
		
		// Valida campo WS-R1-CAMPO-29 - Tipo tasa
		if(rfif.TipoTasaIF24_29.substring(0,1).equals("1")) {
			if(!(rfif.TipoTasaIF24_29.substring(4,5).equals("0") || rfif.TipoTasaIF24_29.substring(5,6).equals("0") || rfif.TipoTasaIF24_29.substring(6,7).equals("0"))) {
				writeInFileLog("ERROR, Registro 1 Campo 29 (Tipo de tasa de valoracion), los digitos 5,6,7 deben ser igual a 0 cuando el digito 1 es igual a 1", rfif.Nemo, bufferedWriterLog);
			}
		}else {
			if(rfif.TipoTasaIF24_29.substring(4,5).equals("0") || rfif.TipoTasaIF24_29.substring(5,6).equals("0") || rfif.TipoTasaIF24_29.substring(6,7).equals("0")) {
				writeInFileLog("ERROR, Registro 1 Campo 29 (Tipo de tasa de valoracion), los digitos 5,6,7 deben ser distinto a 0 cuando el digito 1 es distinto a 1", rfif.Nemo, bufferedWriterLog);
			}
		}
	
		// Valida campos
		// WS-R1-CAMPO-23 - Valor par
		// WS-R1-CAMPO-32 - Tipo valorizacion 
		// WS-R1-CAMPO-33 - Duracion modif 
		// WS-R1-CAMPO-34 - Convexidad
		if (!(rfif.IdClasificacionTipoInstrumento.equals("RF") && rfif.SIGIRP40.equals("N")) &&
			!(rfif.IdClasificacionTipoInstrumento.equals("IF") && rfif.BANESTADO3334.equals("N"))) {

			// Valida campo WS-R1-CAMPO-23 - Valor par
			if(rfif.ValorPar.compareTo(BigDecimal.ZERO) == 0 && !rfif.IdClasificacionTipoInstrumento.equals("IF")) {
				writeInFileLog("ERROR, Registro 1 Campo 23 (Valor par), debe ser mayor que 0", rfif.Nemo, bufferedWriterLog);
			}
			
			if(rfif.PrecioCompra.compareTo(BigDecimal.ZERO) == 0 && !rfif.IdClasificacionTipoInstrumento.equals("IF")) {
				writeInFileLog("ERROR, Registro 1 Campo 32 (Precio del instrumento), debe ser mayor que 0", rfif.Nemo, bufferedWriterLog);
			}
			
			if(rfif.DuracionModificada.compareTo(BigDecimal.ZERO) == 0) {
				writeInFileLog("ERROR, Registro 1 Campo 33 (Duracion modificada), debe ser mayor que 0", rfif.Nemo, bufferedWriterLog);
			}
			
			if(rfif.Convexidad.compareTo(BigDecimal.ZERO) == 0) {
				writeInFileLog("ERROR, Registro 1 Campo 34 (Convexidad), debe ser mayor que 0", rfif.Nemo, bufferedWriterLog);
			}	
		}
		
		// Valida campos WS-R1-CAMPO-36 - Condicion instr
		if((Integer.parseInt(rfif.CondicionInstrumento) > 3 && Integer.parseInt(rfif.CondicionInstrumento) < 9) || rfif.CondicionInstrumento.equals("0")){
			writeInFileLog("ERROR, Registro 1 Campo 36 (Condicion del instrumento), debe ser 1, 2, 3 o 9 ", rfif.Nemo, bufferedWriterLog);
		}
		
		// Valida campos WS-R1-CAMPO-37 - Fecha inicio cond y WS-R1-CAMPO-38 - Fecha term cond
		if(rfif.CondicionInstrumento.equals("1")) {
			if(!rfif.FechaInicioCond.equals("00000000")) {
				writeInFileLog("ERROR, Registro 1 Campo 37 (Fecha de inicio condicion), debe ser igual a 00000000 cuando campo 36 (Condicion del instrumento) es igual a 1", rfif.Nemo, bufferedWriterLog);
			}
			if(!rfif.FechaTermCond.equals("00000000")) {
				writeInFileLog("ERROR, Registro 1 Campo 38 (Fecha final de condicion), debe ser igual a 00000000 cuando campo 36 (Condicion del instrumento) es igual a 1", rfif.Nemo, bufferedWriterLog);
			}
			
		}else {
			if(rfif.FechaInicioCond.equals("00000000")){
				writeInFileLog("ERROR, Registro 1 Campo 37 (Fecha de inicio condicion), debe ser distinto a 00000000 cuando campo 36 (Condicion del instrumento) es distinto a 1", rfif.Nemo, bufferedWriterLog);
			}
			if(Integer.parseInt(rfif.FechaInicioCond)< Integer.parseInt(rfif.FechaInversion)) {
				writeInFileLog("ERROR, Registro 1 Campo 37 (Fecha de inicio condicion), debe ser mayor igual que Campo 3 (Fecha proceso) cuando Campo 36 (Condicion del instrumento) es distinto de 1", rfif.Nemo, bufferedWriterLog);
			}
			
			if(!rfif.FechaTermCond.equals("00000000") && !rfif.FechaTermCond.equals("99999999")) {
				if(Integer.parseInt(rfif.FechaTermCond)< Integer.parseInt(rfif.FechaInversion)) {
					writeInFileLog("ERROR, Registro 1 Campo 38 (Fecha final de condicion), debe ser mayor igual que Campo 3 (Fecha proceso) cuando Campo 36 (Condicion del instrumento) es distinto de 1", rfif.Nemo, bufferedWriterLog);
				}
				if(Integer.parseInt(rfif.FechaInicioCond )> Integer.parseInt(rfif.FechaTermCond)) {
					writeInFileLog("ERROR, Registro 1 Campo 38 (Fecha final de condicion), debe ser mayor que Campo 37 (Fecha de inicio condicion) cuando Campo 36 (Condicion del instrumento) es distinto de 1", rfif.Nemo, bufferedWriterLog);
				}
			}
		}
		
		// Separador de log por registro
		if(logRegistrado) bufferedWriterLog.write("\r\n");

	}
	
	// Validar campos de Fondos Mutuos
	public void validarCamposFM(DatosFFMM fm, BufferedWriter bufferedWriterLog) throws IOException {
		
		logRegistrado = false;
		
		// WS-R2-CAMPO-08 - Numero de cuotas
		if (!(fm.NumeroDeCuotas.compareTo(BigDecimal.ZERO)==1)) {
			writeInFileLog("ERROR, Registro 2 Campo 8 (Numero de cuotas mantenidas), valor debe ser mayor que 0", fm.Nemotecnico, bufferedWriterLog);
		}
		
		// WS-R2-CAMPO-10 - Valor Inicial Cuota
		if (!(fm.ValorInicialCuota.compareTo(BigDecimal.ZERO)==1)) {
			writeInFileLog("ERROR, Registro 2 Campo 10 (Valor inicial cuota), valor debe ser mayor que 0", fm.Nemotecnico, bufferedWriterLog);
		} 
			
		// WS-R2-CAMPO-11 - Valor Cuota
		if (!(fm.ValorCuota.compareTo(BigDecimal.ZERO)==1)) {
			writeInFileLog("ERROR, Registro 2 Campo 11 (Valor cuota), valor debe ser mayor que 0", fm.Nemotecnico, bufferedWriterLog);
		} 
		
		// WS-R2-CAMPO-12 - Valor Razonable
		if (!(fm.ValorRazonable.compareTo(BigDecimal.ZERO)==1)) {
			writeInFileLog("ERROR, Registro 2 Campo 12 (Valor razonable), valor debe ser mayor que 0", fm.Nemotecnico, bufferedWriterLog);
		}
		
		// Separador de log por registro
		if(logRegistrado) bufferedWriterLog.write("\r\n");
	}
	
	// Validar campos de Renta Variable
	public void validarCamposRV(DatosRV rv, BufferedWriter bufferedWriterLog) throws IOException {
		
		logRegistrado = false;
		
		// WS-R3-CAMPO-07 - Num de Instr Manten
		if (!(rv.NumeroInstrMant.compareTo(BigDecimal.ZERO)==1)) {
			writeInFileLog("ERROR, Registro 3 Campo 7 (Numero de instrumentos mantenidas), valor debe ser mayor que 0", rv.Nemotecnico, bufferedWriterLog);
		}
		
		// WS-R3-CAMPO-09 - Precio Compra
		if (!(rv.PrecioCompra.compareTo(BigDecimal.ZERO)==1)) {
			writeInFileLog("ERROR, Registro 3 Campo 9 (Precio de compra), valor debe ser mayor que 0", rv.Nemotecnico, bufferedWriterLog);
		} 
		
		// WS-R3-CAMPO-10 - Valor Razonable
		if (!(rv.ValorRazonable.compareTo(BigDecimal.ZERO)==1)) {
			writeInFileLog("ERROR, Registro 3 Campo 10 (Valor razonable), valor debe ser mayor que 0", rv.Nemotecnico, bufferedWriterLog);
		}
		
		// Separador de log por registro
		if(logRegistrado) bufferedWriterLog.write("\r\n");
	}
	
	// Calcula el campo Tipo Tasa para Renta Fija (WS-R1-CAMPO-20 - 7 digitos) 
	public String calculoTipoTasaRF(DatosRFIF rfif,String Campo , String PeriodoCupon) {
		//  ; //tipoCalculo, Long tipoTasaPago, String periocidadDePago, Long computoDeDias, Long anoBase, Long tipoTasa, String tipoinstrumento,				String Nemotecnico) {
		//rfif.TipoCalculo, rfif.TipoTasaPago, 	rfif.PeriodoCupon,		rfif.ComputoDeDias, rfif.AnoBase, 	rfif.TasaPago, rfif.IdClasificacionTipoInstrumento,	rfif.Nemotecnico
		
		String posicion1TipoCalculo= "2";
		String posicion2TipoTasaPago= "1";
		String posicion3Periodicidad= "9";
		String posicion4ComputoDeDias= "9";
		String posicion56TipoTasa = "00";
		String posicion7TipoTasa = "0";
		
		// define el 1er caracter con el campo Tipo de Calculo
		if (rfif.TipoCalculo != 2) {
			posicion1TipoCalculo = "1";
		}
		
		// define el 2do caracter con el campo Tipo Tasa Pago
		if (rfif.TipoTasaPago != 0) {
			posicion2TipoTasaPago = "1";
		}		
		
		// 3er digito
		if ( Campo.equals("24_29") ) {
			if ( rfif.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") ) {
				if (rfif.Nemotecnico.equalsIgnoreCase("PAGARE NR")  ) {
					posicion3Periodicidad = "9";
				}
				if ( rfif.Nemotecnico.equalsIgnoreCase("PAGARE R")||rfif.Nemotecnico.equalsIgnoreCase("PDBC") )  {
					posicion3Periodicidad = "5";
				}
			}else {
				if (rfif.PeriodoCupon.equals("0") ) {
					posicion3Periodicidad = "9" ;
				}else {
					posicion3Periodicidad = rfif.PeriodoCupon ;
				}
			}
		}
		if   ( Campo.equals("20")){
		      switch (PeriodoCupon) {
	            case "1":
	            	posicion3Periodicidad = "1";
	                break;
	            case "2":
	            	posicion3Periodicidad = "2";
	                break;
	            case "3":
	            	posicion3Periodicidad = "3";
	                break;
	            case "4":
	            	posicion3Periodicidad = "4";
	                break;
	            case "5":
	            	posicion3Periodicidad = "5";
	                break;
	            default:
	            	posicion3Periodicidad = "9";
	                break;
	        }
		     if (rfif.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
		    	 posicion3Periodicidad = "9";
		     }
			
		}
		
		
		
		
		// define el 4to caracter con los campos Computo de Dias (cd) y Agno Base (ab) 
		if (rfif.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF") ) {
			if (rfif.Familia.equalsIgnoreCase("4")) {
				posicion4ComputoDeDias= "4";
			}else {
				posicion4ComputoDeDias= "2";	
			}
			
		}else if ( rfif.Nemotecnico.equalsIgnoreCase("PAGARE NR") ) {
			posicion4ComputoDeDias= "3";
		}else if ( rfif.Nemotecnico.equalsIgnoreCase("PAGARE R")||rfif.Nemotecnico.equalsIgnoreCase("PDBC") ) {
			posicion4ComputoDeDias= "1";
		}
		 
		
		
		
		
		// define el 5to y 6to caracter con el resultado del campo Tipo de Calculo
		if (!posicion1TipoCalculo.equals("1")) {
			if (rfif.TasaPago == 3 || rfif.TasaPago == 8) {
				posicion56TipoTasa = "03";				
			} else if (rfif.TasaPago == 9) {	
				posicion56TipoTasa = "07";
			} else {
				posicion56TipoTasa = "99";				
			}
		}
		
		// define el 7mo caracter con el resultado del campo Tipo de Calculo
		if (!posicion1TipoCalculo.equals("1")) {
			posicion7TipoTasa = "9";
		}
		
		StringBuilder resultado = new StringBuilder();
		resultado.append(posicion1TipoCalculo	);		// Tipo de calculo - Fija (1) o Flotante (2) 	
		resultado.append(posicion2TipoTasaPago	);	// Tipo Tasa Pago  - Compuesta (0) o lineal (1)
		resultado.append(posicion3Periodicidad		);	// Periocidad de pago
		resultado.append(posicion4ComputoDeDias	);	// Computo de dias
		resultado.append(posicion56TipoTasa		);		
		resultado.append(posicion7TipoTasa		);
		
		return resultado.toString();
	}
	
	// Formato de numeros decimales con relleno de n 0s a la izquierda (eliminando el punto decimal)
	// e.g. 5,30 = 00053000 para una longitud de 8 digitos
	public String getFormatAmount(BigDecimal amount, Integer longitud) {
	    if (amount == null) {
	        amount = BigDecimal.ZERO;
	    }
	    String format = "";
	    if (amount.compareTo(BigDecimal.ZERO) == 0) {
	        format = String.format("%0" + longitud + "d", 0);
	    } else {
	        amount = amount.setScale(4, RoundingMode.FLOOR);
	        String amountStr = amount.toPlainString();
	        String decimals = amountStr.substring(amountStr.length() - 4);
	        format = amount.toBigInteger().toString() + decimals;
	    }
	    String result = String.format("%1$" + longitud + "s", format).replace(' ', '0');
	    if (result.length() > longitud) {
	        result = result.substring(result.length() - longitud);
	    }
	    return result;
	}
	
}
