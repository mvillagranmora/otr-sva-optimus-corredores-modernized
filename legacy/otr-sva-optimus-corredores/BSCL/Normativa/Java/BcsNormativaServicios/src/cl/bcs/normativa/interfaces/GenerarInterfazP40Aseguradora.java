package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch;
import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch.InValorizacionValoresAdicionales;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatoRUT;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatosFFMM;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatosRFIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatosRV;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.DatoPais;

public class GenerarInterfazP40Aseguradora implements JavaCall{
	/*
	 * CELZORRO-682
	 * Interfaz P40 - Aseguradora 
	 * 
	 * NOTA:  Esta interfaz genera informacion detallada por el tipo de instrumentos que comprenden rubros de
	 * "Instrumentos para negociacion", "Instrumentos de inversion disponibles para la venta" e "Instrumentos
	 * de inversion hasta el vencimiento", mencionados en el Compendio de Normas Conables.
	 * 
	 * La informacion corresponde a las posiciones mantenidas por cuenta propia de la CORREDORA en forma individual
	 * 
	 * Primer Registro
	 * 		Codigo del banco
	 * 		Identifiacion del archivo (P40)
	 * 		Fecha (AAAAMMDD)
	 * 
	 * Registros siguientes
	 * 		01 	Instrumentos de deuda		(RF e IF)
	 * 		02 	Cuotas de fondos mutuos		(FFMM)
	 * 		03 	Instrumentos de capital		(RV)
	 */
	
	static final String NombreArchivo = "S40_054_";
	private final String SQL_p40_ASEGURADORA_SELECT_RUT_FILIAL = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRUTCorredoraSeguros.sql");
	private final String SQL_P40_Select_Pais = "SELECT \"IdPais\" FROM BSCL.\"BSCL.Plataforma.BD::PLA.Empresa\" WHERE \"IdEmpresa\" = :IdEmpresa;";
	
	private final String SQL_P40_ASEGURADORA_SELECT_RF_IF = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRF_IF.sql");
	private final String SQL_P40_ASEGURADORA_SELECT_FFMM = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectFFMM.sql");	
	private final String SQL_P40_ASEGURADORA_SELECT_RV_CUSTODIA_GARANTIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_CustodiaGarantia.sql");
	private final String SQL_P40_ASEGURADORA_SELECT_RV_SIN_CUSTODIA_GARANTIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_SinCustodiaGarantia.sql");
	private final String SQL_P40_ASEGURADORA_SELECT_RV_CUSTODIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_Custodia.sql");
	private final String SQL_P40_ASEGURADORA_SELECT_RV_SIN_CUSTODIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_SinCustodia.sql");
	private final String SQL_P40_ASEGURADORA_SELECT_RV_SIMULTANEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_Simultanea.sql");	
	
	private final String SQL_P40_ASEGURADORA_HIS_SELECT_RF_IF = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRF_IF.sql");
	private final String SQL_P40_ASEGURADORA_HIS_SELECT_FFMM = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectFFMM.sql");	
	private final String SQL_P40_ASEGURADORA_HIS_SELECT_RV_CUSTODIA_GARANTIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_CustodiaGarantia.sql");
	private final String SQL_P40_ASEGURADORA_HIS_SELECT_RV_SIN_CUSTODIA_GARANTIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_SinCustodiaGarantia.sql");
	private final String SQL_P40_ASEGURADORA_HIS_SELECT_RV_CUSTODIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_Custodia.sql");
	private final String SQL_P40_ASEGURADORA_HIS_SELECT_RV_SIN_CUSTODIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_SinCustodia.sql");
	private final String SQL_P40_ASEGURADORA_HIS_SELECT_RV_SIMULTANEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_Simultanea.sql");	
	
	public static class EntradaGenerarInterfazP40Aseguradora {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario; 
		public String 	p_IdModulo;
		public String	p_Fecha;
		public String	p_Ruta;
		public String	p_Banco;				// (223) -- Valor fijo 
		public String	p_SBIF;					// (277) -- Valor fijo
		public String	p_TipoCartera;			// (2)	 -- Valor Fijo
		public String	p_UsaNemoSVS;			// (S/N) -- Uso de Nemo SVS (solo para IF)
		public String	p_ExcluyeGarantia;		// (S/N) -- Excluye Garantia 	(solo para RV)		
		public String	p_ExcluyeSimultanea;	// (S/N) -- Excluye Simultanea 	(solo para RV)
		public String	p_UsaCustodia;			// (S/N) -- Incluye Custodia
		public String	p_TipoMercado;			// (RV/RF/IF/"") -- Tipo de mercado si esta "vacio" son todas 
		public String	p_FamiliaInstrumento;
		
		public EntradaGenerarInterfazP40Aseguradora() {		// Default values
			p_IdModulo			= "NOR";
			p_Banco 			= "223";
			p_TipoMercado		= "";					// (RF/IF/ACN/FM/"": todos) -> ACN: Acciones Nacionales
		}
	}
	
	public static class SalidaGenerarInterfazP40Aseguradora{
		
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;

		public String 				P_OUT_NOMBRE_ARCHIVO_LOG;
		public String 	 			P_OUT_RUTA_ARCHIVO_LOG;
		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String 				P_OUT_COMENTARIO;
		
		public SalidaGenerarInterfazP40Aseguradora() {
			P_OUT_ARCHIVOS = new ArrayList<String>();
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
			
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarInterfazP40Aseguradora in = gson.fromJson(jin.toString(), EntradaGenerarInterfazP40Aseguradora.class);
		SalidaGenerarInterfazP40Aseguradora out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	private static String formatTasa(String valorStr) {
		// Formateo de tasas a como se esperan de la p40 (2 enteros 4 Decimales 1 Signo) -12.3456 -> 123456-
	    double 	valor  		= Double.parseDouble(valorStr);
	    char 	signo    	= valor < 0 ? '-' : '+';
	    String 	result 		= String.format("%07.4f", Math.abs(valor));
	    return 	result.replaceAll("[.,]", "") + signo;
	}
	
	//Metodos de escritura en Archivo
	public void escribeRFIF(List<DatosRFIF> data, String fechaFormato, EntradaGenerarInterfazP40Aseguradora in, GenerarInterfazP40 p40, BufferedWriter Archivo , BufferedWriter ArchivoLog) throws IOException {
		
			for(GenerarInterfazP40.DatosRFIF r : data) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				String tasaCompra25String     = formatTasa(r.TasaCompra25);
				String tasaValorizacionString = formatTasa(r.TasaValorizacion);
				BigDecimal duracionModif      = r.DuracionModificada;
				BigDecimal convexidad         = r.Convexidad;
				try {
				    if (((r.Familia.equalsIgnoreCase("10") && r.CondicionInstrumento.equalsIgnoreCase("1"))
				            || r.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF"))
				            && r.MonedaNominal.equalsIgnoreCase("999")) {
				        // Anualizado de tasas únicamente si la moneda es CLP y IF o CLP y familia 10 condicion 1

				        BigDecimal tasaCompra25Anual = new BigDecimal(r.TasaCompra25.trim())
				                .multiply(BigDecimal.valueOf(12))
				                .setScale(4, RoundingMode.HALF_UP);
				        
				        BigDecimal tasaValorizacionAnual = new BigDecimal(r.TasaValorizacion.trim())
				                .multiply(BigDecimal.valueOf(12))
				                .setScale(4, RoundingMode.HALF_UP);

				        // Duracion y Convexidad se calculan con la tasa ya anualizada
				        LocalDate fechaHoy          	= LocalDate.parse(fechaFormato, formatter);
				        LocalDate fechaVencimiento  	= LocalDate.parse(r.FechaVencimientoInstrumento, formatter);
				        BigDecimal plazoAVencimiento 	= BigDecimal.valueOf(Math.abs(ChronoUnit.DAYS.between(fechaHoy, fechaVencimiento)));

				        BigDecimal duracion = plazoAVencimiento.divide(BigDecimal.valueOf(360), 6, RoundingMode.HALF_UP);
				        BigDecimal tasa     = tasaValorizacionAnual;

				        duracionModif = duracion
				                .divide(new BigDecimal(1).add(tasa.divide(new BigDecimal(100))), 6, RoundingMode.HALF_UP)
				                .setScale(4, RoundingMode.DOWN);

				        convexidad = duracionModif
				                .multiply(new BigDecimal(1).add(duracion))
				                .divide(new BigDecimal(1).add(tasa.divide(new BigDecimal(100))), 6, RoundingMode.HALF_UP)
				                .setScale(4, RoundingMode.DOWN);
				        
				        tasaCompra25String = formatTasa(tasaCompra25Anual.toPlainString());
				        tasaValorizacionString = formatTasa(tasaValorizacionAnual.toPlainString());
				    }
				    
					r.TasaValorizacion = tasaValorizacionString;
					r.TasaCompra25 = tasaCompra25String;
				} catch (Exception e) { System.out.println("Error en " + r.Nemo + ": " + e.getMessage());}
				
				if (!r.Familia.equalsIgnoreCase("04") && !r.Familia.equalsIgnoreCase("06")) {
					try { r.PrecioCompra = (r.ValorRazonable.divide(r.ValorPar, 6 , RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
					} catch (Exception e) { System.out.println("Error en " + r.Nemo + ": " + e.getMessage());}
				}
				
				r.TipoTasaIF20 		= p40.calculoTipoTasaRF(r,"20",r.PeriodoCupon);
				r.TipoTasaIF24_29 	= p40.calculoTipoTasaRF(r,"24_29",r.PeriodoCupon);

				StringBuilder r1 = new StringBuilder();			
				r1.append("01");													// WS-R1-CAMPO-01 (Tipo de Registro)
				r1.append(String.format("%04d", Integer.valueOf(in.p_SBIF)) );		// WS-R1-CAMPO-02 (Codigo Tenedor SBIF)
				r1.append(fechaFormato);											// WS-R1-CAMPO-03 (Fecha Proceso)
				r1.append(r.FechaInversion);										// WS-R1-CAMPO-04 (Fecha inver)
				r1.append("1");														// WS-R1-CAMPO-05 (Tipo de Cartera)
				r1.append(r.RutEmisor);												// WS-R1-CAMPO-06 (Rut)
				r1.append(String.format("%3s","160"));								// WS-R1-CAMPO-07 (Pais,Valor Fijo)
				r1.append(String.format("%2s",r.Familia));							// WS-R1-CAMPO-08 (Familia de Instr)
				r1.append(String.format("%-20s",r.Nemo));							// WS-R1-CAMPO-09 (Nemotecnico)
				r1.append(String.format("%1s",r.TipoRendimiento));					// WS-R1-CAMPO-10 (Tipo de Rendimiento)
				r1.append(String.format("%1s",r.PeriodoCupon));						// WS-R1-CAMPO-11 (Periodo)
				r1.append(r.FechaUltimoCupon);										// WS-R1-CAMPO-12 (FechaUltimo cupon)
				r1.append(r.FechaProximoCupon);										// WS-R1-CAMPO-13 (FechaProximo cupon)
				r1.append(r.FechaVencimientoInstrumento);							// WS-R1-CAMPO-14 (Fecha vencimiento instrumento)
				r1.append(String.format("%2s","01"));								// WS-R1-CAMPO-15 (Derivados)
				r1.append(p40.getFormatAmount(r.NominalInicial,18));				// WS-R1-CAMPO-16 (Nomina inicial)
				r1.append(p40.getFormatAmount(r.NominalActual,18));					// WS-R1-CAMPO-17 (Nomina Actual)
				r1.append(String.format("%3s",r.MonedaNominal));					// WS-R1-CAMPO-18 (Moneda Nominal)
				r1.append(String.format("%3s",r.MonedaNominal));					// WS-R1-CAMPO-19 (Moneda reajuste)
				r1.append(String.format("%7s",r.TipoTasaIF20));						// WS-R1-CAMPO-20 (Tipo tasa Emisor)
				r1.append(p40.getFormatAmount(r.TasaEmision,6));					// WS-R1-CAMPO-21 (Tasa emisor)
				r1.append(p40.getFormatAmount(r.Tera,6));							// WS-R1-CAMPO-22 (Tera)
				r1.append(p40.getFormatAmount(r.ValorPar,18));						// WS-R1-CAMPO-23 (Valor par)
				r1.append(String.format("%7s",r.TipoTasaIF24_29));					// WS-R1-CAMPO-24 (Tipo Tasa Compra)				
				r1.append(String.format("%5s",tasaCompra25String));						// WS-R1-CAMPO-25 (TasaCompra)				
				r1.append(String.format("%0" + 14 + "d", r.CostoAdquisicion.toBigInteger()).substring(0, 14));// WS-R1-CAMPO-26 (Costo Adquisicion)					
				r1.append(p40.getFormatAmount(r.TasaCompra27,14));					// WS-R1-CAMPO-27 (Tasa Compra)				
				r1.append(String.format("%0" + 14 + "d", r.ValorRazonable.toBigInteger()).substring(0, 14));// WS-R1-CAMPO-28 (Valor razonable, Valor Fijo)			
				r1.append(String.format("%7s",r.TipoTasaIF24_29));					// WS-R1-CAMPO-29 (Tipo Tasa valorzacion)
				r1.append(String.format("%5s",tasaValorizacionString));					// WS-R1-CAMPO-30 (Tasa Valoizacion)
				r1.append(String.format("%1s","3"));								// WS-R1-CAMPO-31 (Tipo valorizacion)				
				r1.append(p40.getFormatAmount(r.PrecioCompra,10));					// WS-R1-CAMPO-32 (pecio compra)	
				r1.append(p40.getFormatAmount(duracionModif,6));				// WS-R1-CAMPO-33 (Duracion modif)
				r1.append(p40.getFormatAmount(convexidad,8));						// WS-R1-CAMPO-34 (Convexividad)				
				r1.append(String.format("%14s","00000000000000"));					// WS-R1-CAMPO-35 (Valor deterioro, valor fijo)
				r1.append(String.format("%1s",r.CondicionInstrumento));				// WS-R1-CAMPO-36 (Condicion instrumento)
				r1.append(r.FechaInicioCond);										// WS-R1-CAMPO-37 (Fecha inicio condicion)
				r1.append(r.FechaTermCond);											// WS-R1-CAMPO-38 (Fecha termino condicion)
		 		r1.append("0");														// WS-R1-CAMPO-39 (01) Jerarquia del valor razonable
				r1.append("0");														// WS-R1-CAMPO-40 (01) Valor de deterioro y su fase de aumento en el riesgo de credito
				r1.append("0");														// WS-R1-CAMPO-41 (01) Aplicacion del supuesto que el instrumento tiene un riesgo crediticio bajo
				r1.append("00000000000000");										// WS-R1-CAMPO-42 (14) Costo de adquisicion actualizado segun la contabilidad
				Archivo.write(r1.toString());
				if(data.indexOf(r)< data.size()) {
					Archivo.write("\r\n");
				}
				
				//Validaciones RFIF
				 
				p40.validarCamposRFIF(r, fechaFormato, in.p_TipoCartera, ArchivoLog);
				 
			}	
	};
	
	public void escribeRV(List<DatosRV> data, String fechaFormato, EntradaGenerarInterfazP40Aseguradora in, GenerarInterfazP40 p40,BufferedWriter Archivo , BufferedWriter ArchivoLog) throws IOException {
		
					
		for(DatosRV r: data) {
			StringBuilder r1 = new StringBuilder();
			r1.append("03");													// WS-R1-CAMPO-01 (Tipo de Registro)
			r1.append(String.format("%04d", Integer.valueOf(in.p_SBIF)) );		// WS-R1-CAMPO-02 (Codigo Tenedor SBIF)
			r1.append(fechaFormato);								// WS-R1-CAMPO-03 (Fecha Proceso)
			r1.append(r.FechaCompra);								// WS-R1-CAMPO-04 (Fecha inver)
			r1.append(r.Nemotecnico);								// WS-R1-CAMPO-05 (Nemotecnico)
			r1.append(in.p_TipoCartera);							// WS-R1-CAMPO-06 (Tipo de Cartera)
			r1.append(p40.getFormatAmount(r.NumeroInstrMant, 14));	// WS-R1-CAMPO-07 (CantidadCustodia)
			r1.append(String.format("%3s",r.Moneda));				// WS-R1-CAMPO-08 (Moneda)
			r1.append(p40.getFormatAmount(r.PrecioCompra,14));		// WS-R1-CAMPO-09 (Precio promedio comprometido)
			r1.append(p40.getFormatAmount(   r.ValorRazonable.setScale(0, RoundingMode.HALF_UP)      , 14));	// WS-R1-CAMPO-10 (Cantidad custodia * precio cieree)
			r1.append(String.format("%1s",r.CondicionInstrumento));	// WS-R1-CAMPO-11 (Condicion instrumento)
			r1.append(r.FechaInicioCondicion);						// WS-R1-CAMPO-12 (Fecha inicio condicion)
			r1.append(r.FechaFinCondicion);							// WS-R1-CAMPO-13 (Fecha fin condicion)
			r1.append(String.format("%176s",""));					// Filler
			Archivo.write(r1.toString());
			if(data.indexOf(r)< data.size()) {
				Archivo.write("\r\n");
			}
			
			p40.validarCamposRV(r, ArchivoLog);
			
			/*
			 * WS-R3-CAMPO-11: CONDICION DEL INSTRUMENTO. Indica la condicion en que se encuentran los
			 * instrumentos, segun los siguientes codigos: 
			 * 1 Sin restricciones para su venta o cesion 
			 * 3 Entregado en garantia 
			 * 9 Otras condiciones que impiden su venta o cesion.
			 * 
			 * FECHA INICIO CONDICION Y FECHA FINAL CONDICION
			 *  Para aquellos instrumentos sin restricciones (codigo 1), el campo se llenara
             *  con ceros.
			 */
			
			
		}	
	};
	
	public void escribeFM(List<DatosFFMM> data, String fechaFormato, EntradaGenerarInterfazP40Aseguradora in, GenerarInterfazP40 p40,BufferedWriter Archivo , BufferedWriter ArchivoLog) throws IOException {

		for(DatosFFMM r: data) {
			StringBuilder r1 = new StringBuilder();
			
			r1.append("02");													// WS-R1-CAMPO-01 (Tipo de Registro)
			r1.append(String.format("%04d", Integer.valueOf(in.p_SBIF)) );		// WS-R1-CAMPO-02 (Codigo Tenedor SBIF)
			r1.append(fechaFormato);								// WS-R1-CAMPO-03 (Fecha Proceso)
			r1.append(r.FechaInversion);							// WS-R1-CAMPO-04 (Tipo de Cartera)
			r1.append(String.format("%010d", 176220759));			// WS-R1-CAMPO-05 (Rut)
			r1.append(String.format("%3s","160"));					// WS-R1-CAMPO-06 (Pais,Valor Fijo)
			r1.append(String.format("%1s","1"));					// WS-R1-CAMPO-07 (Tipo de Fondo)
			r1.append(String.format("%14s","12345678901234"));		// WS-R1-CAMPO-08 (Costo adquisicion)
			r1.append(String.format("%3s","999"));					// WS-R1-CAMPO-9 (Moneda)
			r1.append(String.format("%14s","12345678901234"));		// WS-R1-CAMPO-10 (valor cuota)
			r1.append(String.format("%14s","12345678901234"));		// WS-R1-CAMPO-11 (valor cuota cierre)
			r1.append(String.format("%14s","12345678901234"));		// WS-R1-CAMPO-12 (Monto Act Ajus)
			r1.append(String.format("%180s",""));					// WS-R1-CAMPO-13 (Filler)
			Archivo.write(r1.toString());
			if(data.indexOf(r)< data.size()) {
				Archivo.write("\r\n");
			}
			
			p40.validarCamposFM(r, ArchivoLog);
		}
	};
	
	

	
	//LOGICA 
	
	public SalidaGenerarInterfazP40Aseguradora logica(Connection conn, EntradaGenerarInterfazP40Aseguradora in) throws Exception {
		SalidaGenerarInterfazP40Aseguradora salida = new SalidaGenerarInterfazP40Aseguradora();
		List<String> listArchivos = new ArrayList<>();
		GenerarInterfazP40 p40 = new GenerarInterfazP40();

			String fechaSistema = Utils.getFechaHoraSistema(conn, "BSCL", in.p_IdEmpresa);
			Date fechaActual = Fecha.toDate(fechaSistema);
			
			Boolean Historico = false;
	        // valida si la fecha de entrada es nula
			if (in.p_Fecha == null || in.p_Fecha.equals("")) {	
				in.p_Fecha = fechaActual.toString();
			}
			
			if (!fechaActual.toString().equalsIgnoreCase(in.p_Fecha)) // si no es igual la fecha de entrada entonces se utiliza para ver el historico
			{	Historico = true ;		}		
			
			ValorizacionValoresAdicionalesBatch  valAdicional = new ValorizacionValoresAdicionalesBatch();
			InValorizacionValoresAdicionales entradaVal = new InValorizacionValoresAdicionales();	
			entradaVal.p_IdEmpresa= in.p_IdEmpresa;		
			entradaVal.p_IdUsuario = in.p_IdUsuario;
			entradaVal.p_IdModulo = in.p_IdModulo;
			entradaVal.p_FechaSistema = in.p_Fecha;
			entradaVal.p_ValorizaCarteraParaInterfaces = "S";
			try {
			 //ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales salidaVal = valAdicional.logica(conn,entradaVal);
			}catch (Exception e) {
				salida.P_OUT_ERRORESCONTADOR = Long.sum(salida.P_OUT_ERRORESCONTADOR, 1L)  ;
				salida.P_OUT_COMENTARIO = e.getMessage();
			} 	
			// Asignacion de formato en Fechas
			Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(in.p_Fecha);	
			SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 
			String fechayyyymmdd = sdfyyyyMMdd.format(pFecha);
			
			// Configuracion para salida de archivo      
			salida.P_OUT_NOMBRE_ARCHIVO = NombreArchivo + fechayyyymmdd + ".txt";
			salida.P_OUT_NOMBRE_ARCHIVO_LOG = NombreArchivo + fechayyyymmdd + "DET.log";		
			salida.P_OUT_RUTA_ARCHIVO = in.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
			salida.P_OUT_RUTA_ARCHIVO_LOG = in.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
			
			File archivo = new File(salida.P_OUT_RUTA_ARCHIVO);
			File archivoLog = new File(salida.P_OUT_RUTA_ARCHIVO_LOG);
			
			listArchivos.add(in.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO);
			
			SQL sql = new SQL();
			
			try (BufferedWriter bwArchivoAseguradora = new BufferedWriter(new FileWriter(archivo))) {
				try (BufferedWriter bwArchivoAseguradoraLog = new BufferedWriter(new FileWriter(archivoLog));) {
					p40.registrarLog(conn, in.p_IdEmpresa, Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_FECHA_EJECUCION_PROCESO").msg(),bwArchivoAseguradoraLog);
					
					
					// Obtiene el Rut de la Filial de la Corredora en el parametro RutP40Filial	de la tabla BSCL.Normativa.BD::NOR.Configuracion		
					HashMap<String, Object> paramsRut = new HashMap<String, Object>();
					paramsRut.put("IdEmpresa", in.p_IdEmpresa);
					List<DatoRUT> datoRut = sql.query(conn, SQL_p40_ASEGURADORA_SELECT_RUT_FILIAL, paramsRut, DatoRUT.class);
					
					if (datoRut.isEmpty()) {
						salida.P_OUT_ERRORESCONTADOR = 1L;
						salida.P_OUT_COMENTARIO = Diccionario.getMensaje(conn, in.p_IdEmpresa, "RUT_FILIAL_CORREDORA_NO_ENCONTRADO").msg();
						p40.registrarLog(conn, in.p_IdEmpresa, salida.P_OUT_COMENTARIO,bwArchivoAseguradoraLog);
					
					} else {
					StringBuilder r0 = new StringBuilder("");
					r0.append("0" +in.p_Banco);								// (03) Codigo del banco  (0223)					
					r0.append("P40");										// (03) Interfaz		  (P40)
					r0.append(fechayyyymmdd);								// (08) Fecha del proceso
					r0.append(String.format("%260s",""));					// Filler
					
					bwArchivoAseguradora.write(r0.toString());				// Registra informacion en la primera linea del archivo
					bwArchivoAseguradora.write("\r\n");
					
					if(in.p_TipoMercado.equals("IF")||in.p_TipoMercado.equals("RF")|| in.p_TipoMercado.equals("")) {// si Mercado es IF o RF o TODOS
							
						
							HashMap<String, Object> paramsRFIF = new HashMap<String, Object>();
							paramsRFIF.put("IdEmpresa", in.p_IdEmpresa);
							paramsRFIF.put("UsaNemoSVS", "S");
							paramsRFIF.put("IdCliente", datoRut.get(0).IdCliente);
							paramsRFIF.put("Mercado", in.p_TipoMercado);
							paramsRFIF.put("TipoCartera", in.p_TipoCartera);
							paramsRFIF.put("Fecha", in.p_Fecha);
							
							List<DatosRFIF> datosR1; 
							
							if (Historico) 	{		// query solo con ambos mercados IF/RF								
								datosR1 = sql.query(conn, SQL_P40_ASEGURADORA_HIS_SELECT_RF_IF, paramsRFIF,DatosRFIF.class);
							}else 			{
								datosR1 = sql.query(conn, SQL_P40_ASEGURADORA_SELECT_RF_IF, paramsRFIF,DatosRFIF.class);
							} 
							escribeRFIF(datosR1, fechayyyymmdd, in, p40, bwArchivoAseguradora, bwArchivoAseguradoraLog);
													
						}
					if(in.p_TipoMercado.equals("ACN")|| in.p_TipoMercado.equals("")  ) {// si Mercado es ACN o TODOS
						
						HashMap<String, Object> paramsRV = new HashMap<String, Object>();
						paramsRV.put("IdEmpresa", in.p_IdEmpresa);
						paramsRV.put("IdCliente", datoRut.get(0).IdCliente);
						paramsRV.put("Mercado",in.p_TipoMercado);
						
						if(in.p_UsaCustodia.equals("S")) {
							List<DatosRV> datosR3;
							
							if (Historico) { 		// query con custodia
								paramsRV.put("Fecha", in.p_Fecha);
								datosR3 = sql.query(conn, SQL_P40_ASEGURADORA_HIS_SELECT_RV_CUSTODIA, paramsRV, DatosRV.class);
							}else {
								datosR3 = sql.query(conn, SQL_P40_ASEGURADORA_SELECT_RV_CUSTODIA, paramsRV, DatosRV.class);
							}
							
							escribeRV(datosR3, fechayyyymmdd, in,p40, bwArchivoAseguradora, bwArchivoAseguradoraLog);
							
							if(!in.p_ExcluyeGarantia.equals("S")) {
	
								List<DatosRV> datosR4;
								if (Historico) { 		// query con custodia y garantia
									datosR4 = sql.query(conn, SQL_P40_ASEGURADORA_HIS_SELECT_RV_CUSTODIA_GARANTIA, paramsRV, DatosRV.class);
								}else {
									datosR4 = sql.query(conn, SQL_P40_ASEGURADORA_SELECT_RV_CUSTODIA_GARANTIA, paramsRV, DatosRV.class);
								}
								
								escribeRV(datosR4, fechayyyymmdd, in,p40, bwArchivoAseguradora, bwArchivoAseguradoraLog);
								
							}
						}else {
							List<DatosRV> datosR3 ;
							if (Historico) {		 // query Sin custodia
								datosR3 = sql.query(conn, SQL_P40_ASEGURADORA_SELECT_RV_SIN_CUSTODIA, paramsRV, DatosRV.class);			
							}else {
							 datosR3  = sql.query(conn, SQL_P40_ASEGURADORA_SELECT_RV_SIN_CUSTODIA, paramsRV, DatosRV.class); // query Sin custodia
							}
							escribeRV(datosR3, fechayyyymmdd, in,p40, bwArchivoAseguradora, bwArchivoAseguradoraLog);
							
							if(!in.p_ExcluyeGarantia.equals("S")) {
								List<DatosRV> datosR4;
								if (Historico) { 				// query Sin custodia con garantia
									datosR4 = sql.query(conn, SQL_P40_ASEGURADORA_HIS_SELECT_RV_SIN_CUSTODIA_GARANTIA, paramsRV, DatosRV.class);
								}else {
									datosR4 = sql.query(conn, SQL_P40_ASEGURADORA_SELECT_RV_SIN_CUSTODIA_GARANTIA, paramsRV, DatosRV.class); 
								}
								
								
								escribeRV(datosR4, fechayyyymmdd, in,p40, bwArchivoAseguradora, bwArchivoAseguradoraLog);
							}
						}
						
						
						if(in.p_ExcluyeSimultanea.equals("N")) {
							HashMap<String, Object> paramsRVSimultanea = new HashMap<String, Object>();
							paramsRVSimultanea.put("IdEmpresa", in.p_IdEmpresa);
							paramsRVSimultanea.put("IdCliente", datoRut.get(0).IdCliente);

							
							List<DatosRV> datosR3;
							
							if(Historico) {			// Query Simultanea
								paramsRVSimultanea.put("Fecha", in.p_Fecha);
								datosR3 = sql.query(conn, SQL_P40_ASEGURADORA_HIS_SELECT_RV_SIMULTANEA, paramsRVSimultanea, DatosRV.class);
							}else {
								datosR3 = sql.query(conn, SQL_P40_ASEGURADORA_SELECT_RV_SIMULTANEA, paramsRVSimultanea, DatosRV.class);
							}
								
							escribeRV(datosR3, fechayyyymmdd, in,p40, bwArchivoAseguradora, bwArchivoAseguradoraLog);
						}
						
					}
					if(in.p_TipoMercado.equals("FM")|| in.p_TipoMercado.equals("") ) { // si Mercado es FM o TODOS
							
						HashMap<String, Object> paramsFM = new HashMap<String, Object>();
						paramsFM.put("IdEmpresa", in.p_IdEmpresa);
						paramsFM.put("IdCliente", datoRut.get(0).IdCliente);
						
						List<DatosFFMM> datosR2 ;
						if (Historico) {
							paramsFM.put("Fecha", in.p_Fecha);
							datosR2 = sql.query(conn, SQL_P40_ASEGURADORA_HIS_SELECT_FFMM, paramsFM, DatosFFMM.class);
						}else {
							datosR2 = sql.query(conn, SQL_P40_ASEGURADORA_SELECT_FFMM, paramsFM, DatosFFMM.class);
						}
						
						
						escribeFM(datosR2, fechayyyymmdd, in,p40, bwArchivoAseguradora, bwArchivoAseguradoraLog);

					}
					
					salida.P_OUT_COMENTARIO = Diccionario.getMensaje(conn, in.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
					salida.P_OUT_ARCHIVOS = listArchivos;
					p40.registrarLog(conn, in.p_IdEmpresa, salida.P_OUT_COMENTARIO,bwArchivoAseguradoraLog);
					}
				}
				catch (NullPointerException e) {
					salida.P_OUT_ERRORESCONTADOR = 1L;
					salida.P_OUT_COMENTARIO = Diccionario.getMensaje(conn, in.p_IdEmpresa, "VALOR_NULO").msg();
					
				} catch (Exception e) {
 						salida.P_OUT_ERRORESCONTADOR = 1L;
						salida.P_OUT_COMENTARIO = Diccionario.getMensaje(conn, in.p_IdEmpresa, "ERROR_ABRIR_ARCHIVO").msg() + " " + salida.P_OUT_RUTA_ARCHIVO_LOG;
						salida.P_OUT_COMENTARIO = e.getMessage();
					} 
			}
				catch (Exception e) {
					salida.P_OUT_ERRORESCONTADOR = 1L;
					salida.P_OUT_COMENTARIO = Diccionario.getMensaje(conn, in.p_IdEmpresa, "ERROR_ABRIR_ARCHIVO").msg() + " " + salida.P_OUT_RUTA_ARCHIVO;
				}
				
 		return salida;
	}
	
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		try {			
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection();
			EntradaGenerarInterfazP40Aseguradora entrada = new EntradaGenerarInterfazP40Aseguradora();
			entrada.p_IdEmpresa 			= 1l;
			entrada.p_IdUsuario 			= 51l;
			entrada.p_IdModulo 				= "NOR";
			entrada.p_Fecha 				= "";	// Fecha ejecucion vacia => Hoy ; Diferente a hoy => Historica
			entrada.p_Ruta 					= System.getenv("USERPROFILE") + "\\Desktop\\Interfaces";
			entrada.p_Banco 				= "223";
			entrada.p_SBIF 					= "862";
			entrada.p_TipoCartera 			= "2";
			entrada.p_UsaNemoSVS 			= "N";
			entrada.p_ExcluyeGarantia 		= "N";
			entrada.p_ExcluyeSimultanea 	= "N";
			entrada.p_UsaCustodia 			= "S";
			entrada.p_TipoMercado 			= "";
			entrada.p_FamiliaInstrumento 	= "49";
			
			GenerarInterfazP40Aseguradora demo = new GenerarInterfazP40Aseguradora();
			SalidaGenerarInterfazP40Aseguradora ObjOut = demo.logica(conn, entrada);
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");		
}
}
