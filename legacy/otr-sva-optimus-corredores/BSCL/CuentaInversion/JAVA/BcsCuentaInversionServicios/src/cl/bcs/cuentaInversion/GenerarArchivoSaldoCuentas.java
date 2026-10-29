package cl.bcs.cuentaInversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.util.Matrix;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerarArchivoSaldoCuentas implements JavaCall{
	
	private final Log log = new Log("CTA.GenerarArchivoSaldoCuentas");

	private final String SQL_SELECT_CONFIGURACION         = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoCargoAbono_SelectConfiguracion.sql");  
	
	// ----------------------------------------------------
	// CLASES DE ENTRADA SALIDA
	// ----------------------------------------------------
	public static class GenerarArchivoSaldoCuentas_Entrada
	{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;					
    	public String	p_FechaDesde;
    	public String	p_FechaHasta;    	
    	public String	p_SoloClientesSobregirados; // S : N;
    	public String	p_SoloClientesConSaldoDisponible; // S : N;	
    	public String   p_SoloClientesConSaldoDisponibleClientesSobregirados; // S : N;	
    	public String	p_IncluyeProductoApv; // S : N;   
    	public String	p_IncluyeOtrosSaldos; // S : N;  
		public Long		p_IdEjecutivo;
    	public String	p_IdMoneda;
    	public List<String> p_Encabezados = new ArrayList<>();
    			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	public static class GenerarArchivoSaldoCuentas_Salida {

		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_RUTAARCHIVO;
		
		public GenerarArchivoSaldoCuentas_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// ----------------------------------------------------
	// CLASES DE PROCESO
	// ----------------------------------------------------

	public static class Configuracion 
	{
		public String			RutaBase;
		public String			RutaCta;
		public String			RutaArchivo;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfigEncabezadoData{
		public String Encabezado;
		public String Datos;
		public String Formato;
		public Long AnchoExtra;
		
		  public ConfigEncabezadoData(String encabezado, String datos, String formato, Long anchoExtra) {
		        this.Encabezado = encabezado;
		        this.Datos = datos;
		        this.Formato = formato;
		        this.AnchoExtra = anchoExtra;
		    }
		    @Override
		    public boolean equals(Object o) {
		        if (this == o) return true;
		        if (!(o instanceof ConfigEncabezadoData)) return false;
		        ConfigEncabezadoData that = (ConfigEncabezadoData) o;
		        return Objects.equals(Encabezado, that.Encabezado);
		    }

		    @Override
		    public int hashCode() {
		        return Objects.hash(Encabezado);
		    } 
		
	}
	
	// ----------------------------------------------------
	// CALL
	// ----------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		
		GenerarArchivoSaldoCuentas_Entrada in = gson.fromJson(jin.toString(),GenerarArchivoSaldoCuentas_Entrada.class);
		GenerarArchivoSaldoCuentas_Salida out = logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	
	
	// ----------------------------------------------------
	// METODOS COMPLEMENTARIOS
	// ----------------------------------------------------
	// Genera el nombre de un arhivo , con el formato:	<nombreArhivoSalida>_<fechaHora>.<extension>	
	private String generarNombreArchivo(String nombre, String extension)
	{
		String nombreArchivo = "";
		
		try 
		{
			Date fechaActual = new Date();			
			DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String fechaHora = formatoFecha.format(fechaActual);
			nombreArchivo =  nombre + "_"+ fechaHora +"."+ extension;			
		}
		catch(Exception ex)
		{
			nombreArchivo = "";
		}
		
		return nombreArchivo;	
	}
	
	
	// ----------------------------------------------------
	// LOGICA DEL PROGRAMA
	// ----------------------------------------------------
	public  GenerarArchivoSaldoCuentas_Salida logica(Connection con, GenerarArchivoSaldoCuentas_Entrada in) throws Exception {
		GenerarArchivoSaldoCuentas_Salida out = new GenerarArchivoSaldoCuentas_Salida();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		HashMap<String,String> codigo = new HashMap<String, String>();
		String rutaArchivo 		= "";
		String nombreArchivo 	= "";
		String rutaCta          = "";
		String rutaCompleta 	= "";  // ruta + nombre archivo
		
		log.write("ENTRADA > " + in);
		
		
		//------------------------------------------------------------------------
		// OBTIENE CONFIGURACION (nombre y ruta para guardar archivo)
		List<Configuracion> lstConfiguracion = new ArrayList<Configuracion>();
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);		
		lstConfiguracion = sql.query(con, SQL_SELECT_CONFIGURACION, params, Configuracion.class);
		
		if (!lstConfiguracion.isEmpty())
		{
			for (Configuracion conf : lstConfiguracion)
			{
				rutaArchivo = conf.RutaArchivo != null ? conf.RutaArchivo : "";
				rutaCta     = conf.RutaCta != null ? conf.RutaCta : "";				
			}		
		}
		
		if (rutaArchivo.equalsIgnoreCase("") || rutaCta.equalsIgnoreCase(""))
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_CONFIGURACION_RUTA_ARCHIVO",null,con,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
			return out;
		}

		nombreArchivo = generarNombreArchivo("SaldoCuentas", "csv");
		rutaCompleta  = rutaArchivo + nombreArchivo; // Ruta completa donde generar el archivo
		rutaCta       = rutaCta + nombreArchivo;     // Ruta Cta para devolver y desde donde leer el archivo
		
		log.write("NOMBRE ARCHIVO > " + nombreArchivo);
		log.write("RUTA COMPLETA > " + rutaCompleta);
		

		
		//------------------------------------------------------------------------
		// OBTIENE SALDOS CUENTAS INVERSION
		ConsultarSaldoCuentaInversion servicioConsultarSaldoCuenta = new ConsultarSaldoCuentaInversion();
		EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuenta = new EntradaConsultarSaldoCuentaInversion();
		

		entradaConsultarSaldoCuenta.p_IdEmpresa 			= in.p_IdEmpresa;
		entradaConsultarSaldoCuenta.p_IdUsuario         	= in.p_IdUsuario;
		entradaConsultarSaldoCuenta.p_IdModulo				= in.p_IdModulo;
		entradaConsultarSaldoCuenta.p_UsaReglasNegocio		= "S";
		entradaConsultarSaldoCuenta.p_IncluyeOtrosSaldos	= in.p_IncluyeOtrosSaldos;		
		entradaConsultarSaldoCuenta.p_SoloSaldoNegativo		= in.p_SoloClientesSobregirados;
		entradaConsultarSaldoCuenta.p_SoloSaldoDisponible	= in.p_SoloClientesConSaldoDisponible;
		entradaConsultarSaldoCuenta.p_SoloSaldoDisponibleNegativo = in.p_SoloClientesConSaldoDisponibleClientesSobregirados;
		entradaConsultarSaldoCuenta.p_FechaDesde			= in.p_FechaDesde;
		entradaConsultarSaldoCuenta.p_FechaHasta			= in.p_FechaHasta;
		entradaConsultarSaldoCuenta.p_IdEjecutivo			= in.p_IdEjecutivo;
		entradaConsultarSaldoCuenta.p_IdMoneda				= in.p_IdMoneda;	
		entradaConsultarSaldoCuenta.p_IncluyeProductoApv    = in.p_IncluyeProductoApv;		
		entradaConsultarSaldoCuenta.p_IdCliente				= null; // null: Archivo masivo no se genera por cliente ni portafolio
		entradaConsultarSaldoCuenta.p_IdPortafolio			= null; //512347L;  null: Archivo masivo no se genera por cliente ni portafolio
		entradaConsultarSaldoCuenta.p_IdProductoAgrupador	= null;	
		
		  
		List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
		InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();
		objCuentaInversionConsultar.IdEjecutivo  = in.p_IdEjecutivo;
		objCuentaInversionConsultar.IdMoneda 	 = in.p_IdMoneda;
		objCuentaInversionConsultar.IdPortafolio = null;  //512347L;    null:  Archivo masivo no se genera por cliente ni portafolio
		lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
		
		entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = lstCuentaInversionConsultar;
		
		
		SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = servicioConsultarSaldoCuenta.logica(con, entradaConsultarSaldoCuenta);
		
		String linea = "";
		if (salidaConsultarSaldoCuenta.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0)
		{
			if (salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.size() > 0)
			{
				log.write("CANTIDAD REGISTROS > " + salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.size());
				//File archivo = new File("C:\\__ARCHIVO_PRUEBA\\" + nombreArchivo); // >> PARA PRUEBA LOCALES
			//	File archivo = new File(rutaCompleta);      //  >> PARA EN SERVIDOR (cuando se libera debe ser con esta ruta)				
			//	BufferedWriter bwArchivo;
			//	bwArchivo = new BufferedWriter(new FileWriter(archivo));
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BufferedWriter bwArchivo = new BufferedWriter( new OutputStreamWriter(baos, StandardCharsets.UTF_8));
				
				ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
				baos2.write(0xEF);
				baos2.write(0xBB);
				baos2.write(0xBF);
				OutputStreamWriter osw = new OutputStreamWriter(baos2, StandardCharsets.UTF_8);
     			CSVWriter csvWriter = new CSVWriter(osw,';',CSVWriter.NO_QUOTE_CHARACTER,CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);     			
				
                Map<String,ConfigEncabezadoData> EncabezadosMayuscula = new LinkedHashMap<>();
                List<String>EncabezadosEntradaMayuscula = new ArrayList<>();

                Set<ConfigEncabezadoData> ListadoEncabezados = new LinkedHashSet<>(
					Arrays.asList(	
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_EFECTIVA", codigo) , "FechaSaldo","",0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_EJECUTIVO", codigo) , "NombreEjecutivo","", 40l),			
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SUCURSAL", codigo) , "NombreSucursal","", 40l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_CLIENTE", codigo) , "NombreCliente","", 80l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_TIPO_CLIENTE", codigo) , "TipoCliente","", 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DOCUMENTO_IDENTIDAD", codigo) , "DocumentoIdentidad","", 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_PORTAFOLIO", codigo) , "NombrePortafolio","", 40l),			
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_CUENTA_INVERSION", codigo) , "CodigoMoneda","", 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DISPONIBLE", codigo) , "SaldoDisponible", MonedaFormato.MONTO, 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_MONEY_MARKET_CONSOLIDADO", codigo) , "MontoMoneyMarket", MonedaFormato.MONTO, 0l),			
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_TOTAL_LIQUIDEZ", codigo) , "TotalLiquidez", MonedaFormato.MONTO, 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_GARANTIA", codigo) , "SaldoEnGarantia", MonedaFormato.MONTO, 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_INGRESOS_EGRESOS", codigo) , "SaldoRetiro", MonedaFormato.MONTO, 0l),			
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_A_PLAZO_SIMULTANEA", codigo) , "SaldoAPlazoSimultanea", MonedaFormato.MONTO, 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_A_PLAZO_PRESTAMOS", codigo) , "SaldoAPlazoVentaCorta", MonedaFormato.MONTO, 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_A_PLAZO_COMPROMISO", codigo) , "SaldoAPlazoCompromiso", MonedaFormato.MONTO, 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SALDO_RETENIDO", codigo) ,"SaldoRetenido", MonedaFormato.MONTO, 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_PRODUCTO", codigo) , "Producto","", 40l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_RELACION", codigo) , "Relacion", "", 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_CODIGO_EJECUTIVO", codigo) , "CodigoEjecutivo", "", 0l),
							new ConfigEncabezadoData( Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_ULTIMO_MOVIMIENTO", codigo), "FechaUltimoMovimiento", "FECHA", 0l)
							)			
				 );
                
              //Linea para encabezado del archivo csv
				if (in.p_Encabezados.size()<1) {
					linea = "";
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_EFECTIVA", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_EJECUTIVO", codigo) +";");				
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SUCURSAL", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_CLIENTE", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_TIPO_CLIENTE", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DOCUMENTO_IDENTIDAD", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_PORTAFOLIO", codigo) +";");				
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_CUENTA_INVERSION", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DISPONIBLE", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_MONEY_MARKET_CONSOLIDADO", codigo) +";");				
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_TOTAL_LIQUIDEZ", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_GARANTIA", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_INGRESOS_EGRESOS", codigo) +";");				
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_A_PLAZO_SIMULTANEA", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_A_PLAZO_PRESTAMOS", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_A_PLAZO_COMPROMISO", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SALDO_RETENIDO", codigo) +";");	
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_PRODUCTO", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_RELACION", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_CODIGO_EJECUTIVO", codigo) +";");
					linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_ULTIMO_MOVIMIENTO", codigo) +";");
					
				}else {
					linea = "";		
                    //pasa a mayusculas y quita tildes del listado              
                    for (ConfigEncabezadoData Lem : ListadoEncabezados) {
                    	EncabezadosMayuscula.put(normalizar(Lem.Encabezado), new ConfigEncabezadoData(Lem.Encabezado,Lem.Datos,Lem.Formato, Lem.AnchoExtra));
                    	}
                    
                    for (String EncabezadoEntrada : in.p_Encabezados) {  
                    	EncabezadosEntradaMayuscula.add(camposEspeciales(EncabezadoEntrada, con, in, codigo));                
                       	};
                       
                   //se agregan los encabezados al documento
                    for (Map.Entry<String, ConfigEncabezadoData> EncabezadoMayuscula : EncabezadosMayuscula.entrySet()) {              		
                	   if (!EncabezadosEntradaMayuscula.contains(EncabezadoMayuscula.getKey())) {
               				ConfigEncabezadoData Data =  EncabezadoMayuscula.getValue();
               				linea = linea.concat(Data.Encabezado) +";";
               				}                	    		
                   		}
                   
				}
				bwArchivo.write(linea);
				bwArchivo.newLine();
				csvWriter.writeNext(linea.split(";"));
				
				for(SaldoPorFechaEfectiva saldo : salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA)
				{	
					if (in.p_Encabezados.size()<1) {
					//Linea con data de saldos para archivo csv
						linea = "";
						linea = linea.concat(saldo.FechaSaldo +";");
						linea = linea.concat(saldo.NombreEjecutivo +";"); 
						linea = linea.concat(saldo.NombreSucursal +";"); 
						linea = linea.concat(saldo.NombreCliente +";");
						linea = linea.concat(saldo.TipoCliente +";"); 
						linea = linea.concat(saldo.DocumentoIdentidad +";");
						linea = linea.concat(saldo.NombrePortafolio +";");
						linea = linea.concat(saldo.CodigoMoneda +";");
						saldo.SaldoDisponible = MonedaFormato.formatear(con, saldo.SaldoDisponible, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.SaldoDisponible.toString().replace(".", ",") +";");
						saldo.MontoMoneyMarket = MonedaFormato.formatear(con, saldo.MontoMoneyMarket, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.MontoMoneyMarket.toString().replace(".", ",") +";");
						saldo.TotalLiquidez = MonedaFormato.formatear(con, saldo.TotalLiquidez, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.TotalLiquidez.toString().replace(".", ",") +";");
						saldo.SaldoEnGarantia = MonedaFormato.formatear(con, saldo.SaldoEnGarantia, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.SaldoEnGarantia.toString().replace(".", ",") +";");
						saldo.SaldoRetiro = MonedaFormato.formatear(con, saldo.SaldoRetiro, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.SaldoRetiro.toString().replace(".", ",") +";");
						saldo.SaldoAPlazoSimultanea = MonedaFormato.formatear(con, saldo.SaldoAPlazoSimultanea, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.SaldoAPlazoSimultanea.toString().replace(".", ",") +";");
						saldo.SaldoAPlazoVentaCorta = MonedaFormato.formatear(con, saldo.SaldoAPlazoVentaCorta, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.SaldoAPlazoVentaCorta.toString().replace(".", ",") +";");
						saldo.SaldoAPlazoCompromiso = MonedaFormato.formatear(con, saldo.SaldoAPlazoCompromiso, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.SaldoAPlazoCompromiso.toString().replace(".", ",") +";");
						saldo.SaldoRetenido = MonedaFormato.formatear(con, saldo.SaldoRetenido, MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
						linea = linea.concat(saldo.SaldoRetenido.toString().replace(".", ",") +";");
						linea = linea.concat(saldo.Producto +";");
						linea = linea.concat(saldo.Relacion +";");
						linea = linea.concat(saldo.CodigoEjecutivo +";");
						String fechaUltimoMovimiento = saldo.FechaUltimoMovimiento.equals("0001-01-01") ? "" : saldo.FechaUltimoMovimiento;
						linea = linea.concat(fechaUltimoMovimiento);
										
						//log.write("LINEA > " + linea);
						bwArchivo.write(linea);
						bwArchivo.newLine();
						csvWriter.writeNext(linea.split(";"));
					}else {
						linea = "";
		                   for (Map.Entry<String, ConfigEncabezadoData> EncabezadoMayuscula : EncabezadosMayuscula.entrySet()) {              		
		                	   if (!EncabezadosEntradaMayuscula.contains(EncabezadoMayuscula.getKey())) {
		               				ConfigEncabezadoData Data =  EncabezadoMayuscula.getValue();
		    						try {
		    						    Field field = saldo.getClass().getDeclaredField(Data.Datos);
		    						    field.setAccessible(true);
		    						    Object valorClase = field.get(saldo);
		    						    if ( Data.Formato.equals(MonedaFormato.MONTO) ){
		    						    		valorClase = MonedaFormato.formatear(con, String.valueOf(valorClase), MonedaFormato.MONTO, saldo.CodigoMoneda).valor;
		    						    		linea = linea.concat(valorClase.toString().replace(".", ",") +";");
		    						    	}else if( Data.Formato.equals("FECHA") ) {
		    						    		String fechaUltimoMovimiento = String.valueOf(valorClase).equals("0001-01-01") ? "" : String.valueOf(valorClase);
		    						    		linea = linea.concat(fechaUltimoMovimiento);
		    						    	}else {		    						    	
		    						    		linea = linea.concat(valorClase +";");
		    						    	}
		    								} catch (Exception e) {
		    									e.printStackTrace();
		    								}
		               				}                	    		
		                   		}
						bwArchivo.write(linea);
						bwArchivo.newLine();
						csvWriter.writeNext(linea.split(";"));
					}					
				}				
				bwArchivo.flush();
				csvWriter.flush();
				
				bwArchivo.close();
				csvWriter.close();
				byte[] BytestoPdf  = baos.toByteArray();
				byte[] BytestoCsv= baos2.toByteArray();
				
								
				Map<String, Long> MatrizAnchoExtra= new HashMap<>();
				for (ConfigEncabezadoData Lem : ListadoEncabezados) {				  
					MatrizAnchoExtra.put(Lem.Encabezado.toUpperCase(), Lem.AnchoExtra);           		  
				}
             // generar el PDF
				generarPdfDesdeCsv(rutaCompleta,BytestoPdf,BytestoCsv,MatrizAnchoExtra,nombreArchivo);
			}
			else
			{
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTEN_MOVIMIENTOS_PARA_GENERAR_ARCHIVO",null,con,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
				return out;
			}			
		}
		else
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_EN_GENERACION_ARCHIVO",null,con,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
			return out;
		}
		

		out.P_OUT_RUTAARCHIVO = rutaCta;  // CTA/SaldoCuentas_20200421_135102.csv
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		log.write("SALIDA > " + out);
		return out;
	}
	
	
	public String normalizar(String texto) {
	    if (texto == null) return null;

	    String limpio = Normalizer.normalize(texto, Normalizer.Form.NFD);
	    limpio = limpio.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

	    return limpio.trim().toUpperCase();
	}
	
	public String camposEspeciales(String texto, Connection con,GenerarArchivoSaldoCuentas_Entrada in, HashMap<String,String>codigo) throws Exception {

		String retorno = null; 
		if (texto == null) {
			retorno = null;
		}else {
			 String limpio = Normalizer.normalize(texto, Normalizer.Form.NFD);
			 limpio = limpio.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");			 
	         if ( limpio.trim().toUpperCase().equalsIgnoreCase("CODIGO PORTAFOLIO") ) {
	        	retorno = Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_PORTAFOLIO", codigo).trim().toUpperCase();
             }else if ( limpio.trim().toUpperCase().equalsIgnoreCase("MONEYMARKET") ) {
            	retorno = Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_MONEY_MARKET_CONSOLIDADO", codigo).trim().toUpperCase();
             }else{		
            	retorno =  limpio.trim().toUpperCase();
             }
		}
	    return retorno;
	}

	public void generarPdfDesdeCsv(String rutaCsv, byte[] BytesToPdf,byte[] BytesToCsv, Map<String, Long> columnasExtra ,String NombreArchivoCsv) {

	    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	    symbols.setDecimalSeparator(',');
	    symbols.setGroupingSeparator('.');
	    DecimalFormat formatoNumero = new DecimalFormat("#,##0.##", symbols);

	    try (PDDocument doc = new PDDocument()) {

	        PDRectangle horizontal = new PDRectangle(PDRectangle.LEGAL.getHeight(), PDRectangle.LEGAL.getWidth());

	        PDPage page = new PDPage(horizontal);
	        doc.addPage(page);

	        PDPageContentStream content = new PDPageContentStream(doc, page);

	        // ===== MEDIDAS =====
	        BigDecimal margenIzq = new BigDecimal("10");
	        BigDecimal margenDer = new BigDecimal("10");
	        BigDecimal margenSup = new BigDecimal("20");
	        BigDecimal margenInf = new BigDecimal("20");

	        BigDecimal fontSize = new BigDecimal("6");
	        BigDecimal padding = new BigDecimal("2");
	        BigDecimal lineSpacing = fontSize.add(new BigDecimal("2"));

	        BigDecimal y = BigDecimal.valueOf(page.getMediaBox().getHeight()).subtract(margenSup);
	        BigDecimal tableWidth = BigDecimal.valueOf(page.getMediaBox().getWidth()).subtract(margenIzq).subtract(margenDer);

	        BigDecimal yInicioTabla = y;

	     // BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(rutaCsv), StandardCharsets.UTF_8));//para debugear local
	        BufferedReader br = new BufferedReader( new InputStreamReader(
	        						new ByteArrayInputStream(BytesToPdf), StandardCharsets.UTF_8)  );
	             
	        String linea;
	        boolean esHeader = true;

	        int numCols = -1;
	        BigDecimal[] colWidths = null;

	        PDFont fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
	        PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

	        while ((linea = br.readLine()) != null) {

	            String[] celdas = linea.split(";");

	            // ===== CALCULAR ANCHOS  =====
	            if (numCols == -1) {
	                numCols = celdas.length;
	                colWidths = new BigDecimal[numCols];

	                BigDecimal totalExtra = BigDecimal.ZERO;

	                for (int i = 0; i < numCols; i++) {
	                    String nombreCol = celdas[i].trim().toUpperCase();
	                    if (columnasExtra.containsKey(nombreCol)) {
	                        totalExtra = totalExtra.add(BigDecimal.valueOf(columnasExtra.get(nombreCol)));
	                    }
	                }

	                BigDecimal anchoBase = tableWidth.subtract(totalExtra)
	                        .divide(BigDecimal.valueOf(numCols), 10, RoundingMode.HALF_UP);

	                for (int i = 0; i < numCols; i++) {
	                    colWidths[i] = anchoBase;
	                    String nombreCol = celdas[i].trim().toUpperCase();
	                    if (columnasExtra.containsKey(nombreCol)) {
	                        colWidths[i] = colWidths[i].add(BigDecimal.valueOf(columnasExtra.get(nombreCol)));
	                    }
	                }
	            }

	            PDFont font = esHeader ? fontBold : fontNormal;

	            // ===== PRECALCULAR WRAP =====
	            List<List<String>> filaLineas = new ArrayList<>();
	            int maxLineas = 1;

	            for (int i = 0; i < numCols; i++) {
	                String texto = i < celdas.length ? celdas[i] : "";
	                List<String> lines = pdfWrapText(texto, font, fontSize.floatValue(), colWidths[i].subtract(padding.multiply(new BigDecimal("2"))).floatValue());
	                filaLineas.add(lines);
	                if (lines.size() > maxLineas) maxLineas = lines.size();
	            }

	            BigDecimal rowHeight = BigDecimal.valueOf(maxLineas).multiply(lineSpacing).add(new BigDecimal("2"));

	            // ===== SALTO DE PAGINA =====
	            if (y.subtract(rowHeight).compareTo(margenInf) < 0) {

	                dibujarLineasVerticales(content, margenIzq, yInicioTabla, y, colWidths);

	                content.close();

	                page = new PDPage(horizontal);
	                doc.addPage(page);
	                content = new PDPageContentStream(doc, page);

	                y = BigDecimal.valueOf(page.getMediaBox().getHeight()).subtract(margenSup);
	                yInicioTabla = y;
	                continue;
	            }

	            // ===== FONDO HEADER =====
	            if (esHeader) {
	                content.setNonStrokingColor(0.9f, 0.9f, 0.9f);
	                content.addRect(margenIzq.floatValue(), y.subtract(rowHeight).floatValue(), tableWidth.floatValue(), rowHeight.floatValue());
	                content.fill();
	                content.setNonStrokingColor(0, 0, 0);

	                content.setLineWidth(0.8f);
	                content.moveTo(margenIzq.floatValue(), y.floatValue());
	                content.lineTo(margenIzq.add(tableWidth).floatValue(), y.floatValue());
	                content.stroke();
	            }

	            // ===== TEXTO =====
	            content.beginText();
	            content.setFont(font, fontSize.floatValue());

	            for (int c = 0; c < numCols; c++) {

	                String textoOriginal = c < celdas.length ? celdas[c].trim() : "";
	                
	                boolean esNumero = false;
	                boolean esFecha = false;
	                double valorNumero = 0; 

	                try {
	                    valorNumero = Double.parseDouble(textoOriginal.replace(".", "").replace(",", "."));
	                    esNumero = true;
	                } catch (Exception ex) {}

	                if (!esNumero && textoOriginal.matches("\\d{2}/\\d{2}/\\d{4}")) {
	                    esFecha = true;
	                }

	                String texto = textoOriginal;

	                if (esNumero && !esHeader) {
	                    texto = formatoNumero.format(valorNumero);
	                }

	                List<String> lines = pdfWrapText(texto, font,fontSize.floatValue(),colWidths[c].subtract(padding.multiply(new BigDecimal("2"))).floatValue());

	                BigDecimal baseY = y.subtract(fontSize).subtract(new BigDecimal("2"));

	                BigDecimal xAcum = margenIzq;
	                for (int k = 0; k < c; k++) {
	                    xAcum = xAcum.add(colWidths[k]);
	                }

	                for (int l = 0; l < lines.size(); l++) {

	                    String lineaTexto = lines.get(l);
	                    float textWidth = font.getStringWidth(lineaTexto) / 1000f * fontSize.floatValue();

	                    BigDecimal textX;

	                    if (esNumero && !esHeader) {
	                        textX = xAcum.add(colWidths[c]).subtract(padding).subtract(BigDecimal.valueOf(textWidth));
	                    } else if (esFecha) {
	                        textX = xAcum.add(colWidths[c].divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP))
	                                .subtract(BigDecimal.valueOf(textWidth / 2f));
	                    } else {
	                        textX = xAcum.add(padding);
	                    }

	                    content.setTextMatrix(Matrix.getTranslateInstance(
	                            textX.floatValue(),
	                            baseY.subtract(lineSpacing.multiply(BigDecimal.valueOf(l))).floatValue()
	                    ));
	                    content.showText(lineaTexto);
	                }
	            }

	            content.endText();

	            // ===== LINEA INFERIOR =====
	            content.setLineWidth(0.3f);
	            content.moveTo(margenIzq.floatValue(), y.subtract(rowHeight).floatValue());
	            content.lineTo(margenIzq.add(tableWidth).floatValue(), y.subtract(rowHeight).floatValue());
	            content.stroke();

	            y = y.subtract(rowHeight);
	            esHeader = false;
	        }

	        // ===== LINEAS VERTICALES FINALES =====
	        dibujarLineasVerticales(content, margenIzq, yInicioTabla, y, colWidths);

	        br.close();
	        content.close();
	        
	        //lo guardo en la memoria
	        ByteArrayOutputStream memoria = new ByteArrayOutputStream();
	        doc.save(memoria); 
    
	        byte[] pdfBytes = memoria.toByteArray();
	        guardarZip(BytesToCsv, pdfBytes, rutaCsv,NombreArchivoCsv );

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	private void dibujarLineasVerticales( PDPageContentStream content, BigDecimal xIni, BigDecimal yIni,  BigDecimal yFin,  BigDecimal[] colWidths) throws IOException {
	    content.setLineWidth(0.3f);
	    BigDecimal x = xIni;
	    content.moveTo(x.floatValue(), yIni.floatValue());
	    content.lineTo(x.floatValue(), yFin.floatValue());

	    for (int i = 0; i < colWidths.length; i++) {
	        x = x.add(colWidths[i]);
	        content.moveTo(x.floatValue(), yIni.floatValue());
	        content.lineTo(x.floatValue(), yFin.floatValue());
	    }

	    content.stroke();
	}
	
	
	private List<String> pdfWrapText(String text, PDFont font, float fontSize, float maxWidth) throws Exception {
	    List<String> lines = new ArrayList<>();
	    
	    if (text == null || text.isEmpty()) return lines;
	    String[] words = text.split(" ");
	    StringBuilder line = new StringBuilder();
	    
	    for (String word : words) {
	        String testLine = line.length() == 0 ? word : line + " " + word;
	        float size = font.getStringWidth(testLine) / 1000 * fontSize;
	        if (size > maxWidth) {
	            lines.add(line.toString());
	            line = new StringBuilder(word);
	        } else {
	            line = new StringBuilder(testLine);
	        }
	    }

	    if (line.length() > 0) {
	        lines.add(line.toString());
	    }

	    return lines;
	}
	
    private void guardarZip(byte[] cvsBytes,byte[] pdfBytes,String rutaZip, String NombreArchivoCsv) throws IOException {
       
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {

            ZipEntry excelEntry = new ZipEntry(NombreArchivoCsv);
            zos.putNextEntry(excelEntry);
            zos.write(cvsBytes);
            zos.closeEntry();

            ZipEntry pdfEntry = new ZipEntry(NombreArchivoCsv.replace(".csv", ".pdf"));
            zos.putNextEntry(pdfEntry);
            zos.write(pdfBytes);
            zos.closeEntry();
        }
        Files.write(Paths.get(rutaZip.replace(".csv", ".zip")), zipOut.toByteArray());

    }
	

	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) 
		{
			Gson gson = Utils.getGsonBuilder();			
		//	String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CTA\",\"p_SoloClientesSobregirados\":\"N\",\"p_SoloClientesConSaldoDisponible\":\"N\",\"p_FechaDesde\":\"2024-07-08\",\"p_FechaHasta\":\"2024-07-08\",\"p_IncluyeProductoApv\":\"S\",\"p_IncluyeOtrosSaldos\":\"S\",\"p_IdEjecutivo\":null,\"p_IdMoneda\":\"CLP\"}";
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CTA\",\"p_FechaDesde\":\"2026-01-20\",\"p_FechaHasta\":\"2026-01-20\",\"p_SoloClientesSobregirados\":\"N\",\"p_SoloClientesConSaldoDisponible\":\"N\",\"p_SoloClientesConSaldoDisponibleClientesSobregirados\":\"N\",\"p_IncluyeProductoApv\":\"S\",\"p_IncluyeOtrosSaldos\":\"S\",\"p_IdEjecutivo\":null,\"p_IdMoneda\":\"CLP\"}";
		
			GenerarArchivoSaldoCuentas_Entrada Objin = gson.fromJson(json, GenerarArchivoSaldoCuentas_Entrada.class);
			GenerarArchivoSaldoCuentas demo = new GenerarArchivoSaldoCuentas();			
 			System.out.println("ENTRADA:" + Objin.toString());
 			
 			try 
 			{	
 				GenerarArchivoSaldoCuentas_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.rollback();
 				con.commit();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();	
				//con.commit();
			}
 			
 			con.close();
	    }
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
}
