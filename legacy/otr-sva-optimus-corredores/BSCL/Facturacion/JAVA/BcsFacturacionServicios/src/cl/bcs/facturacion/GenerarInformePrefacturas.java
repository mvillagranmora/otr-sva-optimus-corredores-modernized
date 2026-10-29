package cl.bcs.facturacion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import cl.bcs.facturacion.GenerarInformeIGK.Imagen;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.StringUtils;

public class GenerarInformePrefacturas implements JavaCall{

    private final Log logger = new Log("FM.GenerarInformePrefacturas");

    private final String SQL_CONSULTAR_PREFACTURAS 		 	 = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarPrefacturas.sql");
    private final String SQL_CONSULTAR_PREFACTURAS_TODOS 	 = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarPrefacturasTodas.sql");
    private final String SQL_CONSULTAR_PREFACTURAS_FAC       = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarPrefacturasFAC.sql");
    private final String SQL_CONSULTAR_PREFACTURAS_FAC_TODOS = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarPrefacturasFACTodas.sql");
    private final String SQL_CONSULTAR_TEMPLATE 		 	 = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarTemplate.sql");
    private final String SQL_CONSULTAR_LOGO_CORREDORA    	 = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarLogoCorredora.sql");
    private final String SQL_CALL_CONSULTAR_COMPROBANTE  	 = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarComprobante.sql");
    private final String SQL_SELECT_RAIZ_ARCHIVO         	 = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarRaizArchivo.sql");
    private final String SQL_INSERT_PORTAFOLIO_TABLA_PASO 	 = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_InsertPortafolioTablaPaso.sql");
    private final String SQL_DELETE_PORTAFOLIO_TABLA_PASO 	 = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformePrefacturas_DeletePortafolioTablaPaso.sql");
    
    public static class EntradaGenerarInformePrefacturas {
        public Long            p_IdEmpresa;
        public Long            p_IdUsuario;
        public String          p_FechaComprobante;
        public ArrayList<Long> p_ListaIdPortafolio;
        public String          p_FormatoSalida;
        public Long            p_IdProceso;
        public DataSource      p_DataSource;
        public boolean         p_ExportarTodo;
    }

    public static class SalidaGenerarInformePrefacturas extends BcsPojo {
        public Long                                P_OUT_ERRORESCONTADOR;
        public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
        public String                              p_out_RutaSalida;
        public String                              p_out_NombreSalida;

        public SalidaGenerarInformePrefacturas() {
            P_OUT_ERRORESCONTADOR = 0L;
            P_OUT_MENSAJES        = new ArrayList<>();
        }
    }

    public static class Prefactura extends BcsPojo {
        public Long      IdComprobante;
        public Long      IdRespuestaDTE;
        public Long      IdPortafolio;
        public Timestamp FechaComprobante;
        public String    IdEstadoComprobante;
        public String    NombreLargoCliente;
        public String    CodigoPortafolio;
    }

    public static class PortafolioTablaPaso extends BcsPojo {
        public Long IdPortafolio;
        public Long IdPrograma;
        public PortafolioTablaPaso() {}
        public PortafolioTablaPaso(Long idPortafolio, Long idPrograma) {
            this.IdPortafolio = idPortafolio;
            this.IdPrograma = idPrograma;
        }
    }

    public static class ProgramaTablaPaso extends BcsPojo {
        public Long IdPrograma;
        public ProgramaTablaPaso(Long idPrograma) { this.IdPrograma = idPrograma; }
    }

    public static class Template extends BcsPojo {
        public String IdReporte;
        public String NombreReporte;
        public Long   IdServicio;
        public String FormatoSalida;
    }

    public static class ProgramaBatch extends BcsPojo {
        public Long   IdPrograma;
        public String RutaArchivo;

        public ProgramaBatch(Long IdPrograma, String RutaArchivo) {
            this.IdPrograma   = IdPrograma;
            this.RutaArchivo  = RutaArchivo; 
        }
    }

    private static final Map<String, String> esquemaPrefactura = new HashMap<>();
    static {
        esquemaPrefactura.put("LogoCorredora", "blob");
    }
    
    public static class RaizArchivo extends BcsPojo {
		public String RutaArchivo;
		public String RutaFAC;
	}

    public SalidaGenerarInformePrefacturas logica(Connection con, EntradaGenerarInformePrefacturas entrada) throws Exception {
        logger.write("INICIO GenerarInformePrefacturas");
        long inicio = System.currentTimeMillis();

        SalidaGenerarInformePrefacturas salida = new SalidaGenerarInformePrefacturas();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
	    String fechaConsultaStr  = entrada.p_FechaComprobante.replace("-", "");
	    String fechaSistemaHora  = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	    String nombreTimestamp   = fechaConsultaStr + "_" + fechaSistemaHora;
		    
		LocalDate fechaConsulta = LocalDate.parse(entrada.p_FechaComprobante);
		LocalDate fechaSistemaDate = LocalDate.parse(fechaSistema);
		LocalDate fechaMinima = fechaSistemaDate.minusMonths(6);
		
		if (fechaConsulta.isBefore(fechaMinima)) {
		    salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FECHA_FUERA_DE_RANGO", new HashMap<>(), con, entrada.p_IdEmpresa));
		    salida.P_OUT_ERRORESCONTADOR++;
		    return salida;
		}
		
        try {
        	
            RaizArchivo raizArchivo = obtenerRaizArchivo(con, entrada.p_IdEmpresa);
            if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaFAC)) {
                salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), con, entrada.p_IdEmpresa));
                salida.P_OUT_ERRORESCONTADOR++;
                return salida;
            }

            // Para pruebas locales descomenta esta linea y comenta la siguiente
            //String rutaBase = System.getProperty("user.home") + File.separator + "Documents" + File.separator;
            String rutaBase     = raizArchivo.RutaArchivo;
            String rutaRelativa = raizArchivo.RutaFAC;
            new File(rutaBase).mkdirs();

            List<Prefactura> prefacturas = consultarPrefacturas(con, entrada, fechaSistema);
            logger.write("Total prefacturas encontradas: " + prefacturas.size());
            
            if (prefacturas.isEmpty()) {
                salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "SIN_DATOS", new HashMap<>(), con, entrada.p_IdEmpresa));
                salida.P_OUT_ERRORESCONTADOR++;
                return salida;
            }

            Map<Long, List<Prefactura>> grupos = prefacturas.stream()
            	    .collect(Collectors.groupingBy(p -> p.IdPortafolio));
            	logger.write("Total portafolios a procesar: " + grupos.size());

        	Template template = null;
        	boolean hayFac = grupos.values().stream()
        	    .flatMap(List::stream)
        	    .anyMatch(p -> p.IdEstadoComprobante.equals("FAC"));
            if (hayFac) {
                template = consultarTemplate(con, entrada.p_IdEmpresa, "");
                logger.write("Template obtenido: " + template.IdReporte + " - " + template.NombreReporte);
            } else {
                logger.write("No hay comprobantes FAC se procesara solo DTE");
            }

            List<String> archivosGenerados = new ArrayList<>();

            logger.write(entrada.p_ExportarTodo ? "Exportar Todo" : "Exportar Seleccionado");
            for (Map.Entry<Long, List<Prefactura>> grupo : grupos.entrySet()) {
            	try {
            		List<String> archivosPortafolio = procesarPortafolio(con, grupo, template, entrada, rutaBase, nombreTimestamp, salida);
            		archivosGenerados.addAll(archivosPortafolio);
            	} catch (Exception e) {
            		logger.write("ADVERTENCIA: portafolio " + grupo.getKey() + " omitido por error", e);
            		HashMap<String, String> mapErrPortafolio = new HashMap<>();
            		mapErrPortafolio.put("IdPortafolio", String.valueOf(grupo.getKey()));
            		mapErrPortafolio.put("Mensaje", e.getMessage());
            		salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "PORTAFOLIO_ERROR_GENERACION", mapErrPortafolio, con, entrada.p_IdEmpresa));
            		salida.P_OUT_ERRORESCONTADOR++;
            	}
            }
            
            if (archivosGenerados.isEmpty()) {
                logger.write("No se generaron archivos para ningun portafolio");
                salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "SIN_ARCHIVOS_GENERADOS", new HashMap<>(), con, entrada.p_IdEmpresa));
                return salida;
            }
            
            String nombreZip = comprimirArchivos(nombreTimestamp, rutaBase, "InformePrefacturas_", archivosGenerados);
            salida.p_out_NombreSalida = nombreZip;
            salida.p_out_RutaSalida   = rutaRelativa + nombreZip;
            logger.write("ZIP generado: " + salida.p_out_RutaSalida);
        } catch (Exception e) {
            logger.write("Error en logica", e);
            HashMap<String, String> mapErr = new HashMap<>();
            mapErr.put("Mensaje", e.getMessage());
            salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_GENERACION", mapErr, con, entrada.p_IdEmpresa));
            salida.P_OUT_ERRORESCONTADOR++;
            throw e;
        }
        long fin = System.currentTimeMillis();
        logger.write("FIN GenerarInformePrefacturas - " + (fin - inicio) + "ms - Errores: " + salida.P_OUT_ERRORESCONTADOR);
        return salida;
    }
    
    private List<Prefactura> consultarPrefacturas(Connection con, EntradaGenerarInformePrefacturas entrada, String fechaSistema) throws Exception {
        boolean esFechaActual = entrada.p_FechaComprobante.equals(fechaSistema);
        boolean tienePortafolios = entrada.p_ListaIdPortafolio != null
                                   && !entrada.p_ListaIdPortafolio.isEmpty();

        HashMap<String, Object> params = new HashMap<>();
        params.put("FechaConsulta", entrada.p_FechaComprobante);

        if (!tienePortafolios) {
            return esFechaActual
                ? new SQL().query(con, SQL_CONSULTAR_PREFACTURAS_FAC_TODOS, params, Prefactura.class)
                : new SQL().query(con, SQL_CONSULTAR_PREFACTURAS_TODOS, params, Prefactura.class);
        }

        SQL sql = new SQL();
        List<PortafolioTablaPaso> portafolios = entrada.p_ListaIdPortafolio.stream()
            .distinct()
            .map(idPortafolio -> new PortafolioTablaPaso(idPortafolio, entrada.p_IdProceso))
            .collect(Collectors.toList());
        sql.batch(con, SQL_INSERT_PORTAFOLIO_TABLA_PASO, portafolios, PortafolioTablaPaso.class);

        params.put("IdPrograma", entrada.p_IdProceso);

        try {
            return esFechaActual
                ? sql.query(con, SQL_CONSULTAR_PREFACTURAS_FAC, params, Prefactura.class)
                : sql.query(con, SQL_CONSULTAR_PREFACTURAS, params, Prefactura.class);
        } finally {
            List<ProgramaTablaPaso> paraEliminar = new ArrayList<>();
            paraEliminar.add(new ProgramaTablaPaso(entrada.p_IdProceso));
            sql.batch(con, SQL_DELETE_PORTAFOLIO_TABLA_PASO, paraEliminar, ProgramaTablaPaso.class);
        }
    }
    
    private Template consultarTemplate(Connection con, Long idEmpresa, String idTipoComprobante) throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpresa",          idEmpresa);
        params.put("IdTipoComprobante",  idTipoComprobante);
        params.put("LocalizadorSistema", "CL");

        List<Template> resultado = new SQL().query(con, SQL_CONSULTAR_TEMPLATE, params, Template.class);
        if (resultado == null || resultado.isEmpty()) {
            throw new Exception("No se encontro template CMBFAC - IdEmpresa: " + idEmpresa);
        }
        return resultado.get(0);
    }
    
    private List<String> procesarPortafolio(Connection con, Map.Entry<Long, List<Prefactura>> grupo, Template template, EntradaGenerarInformePrefacturas entrada, String rutaBase, String nombreTimestamp, SalidaGenerarInformePrefacturas salida) throws Exception {
	    List<Prefactura> prefacturasGrupo = grupo.getValue();
	    Long idPortafolio = grupo.getKey();
	    String nombrePortafolio = prefacturasGrupo.get(0).NombreLargoCliente
	        .replaceAll("[^a-zA-Z0-9_-]", "_").trim();
	    String codigoPortafolio = prefacturasGrupo.get(0).CodigoPortafolio != null 
	    	    ? prefacturasGrupo.get(0).CodigoPortafolio 
	    	    : String.valueOf(idPortafolio);
	    //informePrefacturas_nombreCliente_idPortafolio_fechaConsulta_fechaSistemaHora
	    String nombreBase = "informePrefacturas_" + nombrePortafolio + "_" + codigoPortafolio + "_" + nombreTimestamp;

	    List<Prefactura> grupoFac = new ArrayList<>();
	    List<Prefactura> grupoDte = new ArrayList<>();

	    for (Prefactura pref : prefacturasGrupo) {
	        if (pref.IdEstadoComprobante.equals("FAC")) {
	            grupoFac.add(pref);
	        } else if (pref.IdEstadoComprobante.equals("ENV")) {
	            grupoDte.add(pref);
	        } else {
	            logger.write("Comprobante " + pref.IdComprobante
	                + " con estado " + pref.IdEstadoComprobante + " - omitido");
	        }
	    }

	    logger.write("Portafolio "  + " - FAC: " + grupoFac.size() + " DTE: " + grupoDte.size());

	    return generarArchivosPorFormato(con, grupoFac, grupoDte, template, entrada, rutaBase, nombreBase, salida);
    }

    private List<String> generarArchivosPorFormato(Connection con, List<Prefactura> grupoFac, List<Prefactura> grupoDte, Template template, EntradaGenerarInformePrefacturas entrada, String rutaBase, String nombreBase, SalidaGenerarInformePrefacturas salida) throws Exception {
        List<String> archivos = new ArrayList<>();
        int TAMANIO_LOTE = 200;

        // Obtener datos FAC
        Map<Long, List<DatosComprobanteCrystal>> datosFac = new HashMap<>(grupoFac.size() * 2);
        if (!grupoFac.isEmpty()) {
            String logoB64 = obtenerLogoCorredoraB64(con, entrada.p_IdEmpresa);
            SQL sql = new SQL();
            for (Prefactura pref : grupoFac) {
                List<DatosComprobanteCrystal> datos = obtenerDatosComprobante(con, sql, entrada.p_IdEmpresa, pref.IdComprobante, logoB64);
                if (!datos.isEmpty()) {
                    datosFac.put(pref.IdComprobante, datos);
                } else {
                    logger.write("Sin datos para comprobante: " + pref.IdComprobante + " - omitido");
                }
            }
        }

        List<String> temporalesPDF = new ArrayList<>();
        List<String> temporalesTXT = new ArrayList<>();

        // Lotes FAC
        if (!datosFac.isEmpty()) {
            List<Long> keys = new ArrayList<>(datosFac.keySet());
            int totalLotesFAC = (int) Math.ceil((double) keys.size() / TAMANIO_LOTE);
            
            if ("TXT".equalsIgnoreCase(entrada.p_FormatoSalida) || "TODOS".equalsIgnoreCase(entrada.p_FormatoSalida)) {
                String rutaHeaderFac = rutaBase + nombreBase + "_header_fac.txt";
                try (OutputStream os = new FileOutputStream(rutaHeaderFac)) {
                    StringBuilder header = new StringBuilder();
                    header.append(campo("", 50)).append("*** PREFACTURAS ***").append("\n\n");
                    os.write(header.toString().getBytes("UTF-8"));
                }
                temporalesTXT.add(rutaHeaderFac);
            }
            
            for (int i = 0; i < keys.size(); i += TAMANIO_LOTE) {
                List<Long> keysLote = keys.subList(i, Math.min(i + TAMANIO_LOTE, keys.size()));
                Map<Long, List<DatosComprobanteCrystal>> datosFacLote = keysLote.stream()
                    .collect(Collectors.toMap(key -> key, datosFac::get));

                logger.write("Procesando lote FAC " + (i / TAMANIO_LOTE + 1) + " de " + totalLotesFAC + " (" + datosFacLote.size() + " comprobantes)");

                switch (entrada.p_FormatoSalida.toUpperCase()) {
                    case "PDF":
                        temporalesPDF.addAll(generarTemporalesFAC(datosFacLote, template, "pdf", rutaBase, nombreBase, entrada.p_DataSource));
                        break;
                    case "TXT":
                        temporalesTXT.addAll(generarTemporalesTXTManual(datosFacLote, rutaBase, nombreBase));
                        break;
                    case "TODOS":
                        temporalesPDF.addAll(generarTemporalesFAC(datosFacLote, template, "pdf", rutaBase, nombreBase + "_PDF", entrada.p_DataSource));
                        temporalesTXT.addAll(generarTemporalesTXTManual(datosFacLote, rutaBase, nombreBase + "_TXT"));
                        break;
                }
            }
        }

        // Lotes DTE
        if (!grupoDte.isEmpty()) {
            List<Prefactura> grupoDteList = new ArrayList<>(grupoDte);
            int totalLotesDTE = (int) Math.ceil((double) grupoDteList.size() / TAMANIO_LOTE);
            
            if ("TXT".equalsIgnoreCase(entrada.p_FormatoSalida) || "TODOS".equalsIgnoreCase(entrada.p_FormatoSalida)) {
                String rutaHeaderEnv = rutaBase + nombreBase + "_header_env.txt";
                try (OutputStream os = new FileOutputStream(rutaHeaderEnv)) {
                    StringBuilder header = new StringBuilder();
                    if (!datosFac.isEmpty()) header.append("\n");
                    header.append(campo("", 50)).append("*** FACTURAS ***").append("\n\n");
                    os.write(header.toString().getBytes("UTF-8"));
                }
                temporalesTXT.add(rutaHeaderEnv);
            }
            
            for (int i = 0; i < grupoDteList.size(); i += TAMANIO_LOTE) {
                List<Prefactura> loteDTE = grupoDteList.subList(i, Math.min(i + TAMANIO_LOTE, grupoDteList.size()));
                logger.write("Procesando lote DTE " + (i / TAMANIO_LOTE + 1) + " de " + totalLotesDTE + " (" + loteDTE.size() + " comprobantes)");

                switch (entrada.p_FormatoSalida.toUpperCase()) {
                    case "PDF":
                        temporalesPDF.addAll(generarTemporalesDTE(con, loteDTE, rutaBase, nombreBase, "pdf", entrada.p_IdEmpresa, salida));
                        break;
                    case "TXT":
                        Map<Long, List<DatosComprobanteCrystal>> datosDteTXT = obtenerDatosParaDTE(con, loteDTE, entrada.p_IdEmpresa);
                        if (!datosDteTXT.isEmpty()) temporalesTXT.addAll(generarTemporalesTXTManual(datosDteTXT, rutaBase, nombreBase));
                        break;
                    case "TODOS":
                        temporalesPDF.addAll(generarTemporalesDTE(con, loteDTE, rutaBase, nombreBase + "_PDF", "pdf", entrada.p_IdEmpresa, salida));
                        Map<Long, List<DatosComprobanteCrystal>> datosDteTODOS = obtenerDatosParaDTE(con, loteDTE, entrada.p_IdEmpresa);
                        if (!datosDteTODOS.isEmpty()) temporalesTXT.addAll(generarTemporalesTXTManual(datosDteTODOS, rutaBase, nombreBase + "_TXT"));
                        break;
                }
            }
        }

        // Mergear todo al final
        String archivo;
        switch (entrada.p_FormatoSalida.toUpperCase()) {
            case "PDF":
                if (!temporalesPDF.isEmpty()) {
                    archivo = mergearTemporales(temporalesPDF, rutaBase + nombreBase + ".pdf", "pdf");
                    archivos.add(archivo);
                }
                break;
            case "TXT":
                if (!temporalesTXT.isEmpty()) {
                    archivo = mergearTemporales(temporalesTXT, rutaBase + nombreBase + ".txt", "txt");
                    archivos.add(archivo);
                }
                break;
            case "TODOS":
                if (!temporalesPDF.isEmpty()) {
                    archivo = mergearTemporales(temporalesPDF, rutaBase + nombreBase + "_PDF.pdf", "pdf");
                    archivos.add(archivo);
                }
                if (!temporalesTXT.isEmpty()) {
                    archivo = mergearTemporales(temporalesTXT, rutaBase + nombreBase + "_TXT.txt", "txt");
                    archivos.add(archivo);
                }
                break;
        }

        return archivos;
    }
	
	private List<String> generarTemporalesFAC(Map<Long, List<DatosComprobanteCrystal>> datosPorComprobante, Template template, String extension, String rutaBase, String nombreBase, DataSource dataSource) throws Exception {
	    List<String> temporales = new ArrayList<>();
	
	    ReportDocument report = new ReportDocument(Utils.getSchema(), dataSource);
	    if (!report.readDocument(template.IdReporte)) {
	        throw new Exception("No se pudo cargar template: " + template.IdReporte);
	    }
	
	    for (Map.Entry<Long, List<DatosComprobanteCrystal>> entry : datosPorComprobante.entrySet()) {
	        Long idComprobante = entry.getKey();
	        List<DatosComprobanteCrystal> datos = entry.getValue();
	        String json = Utils.getGsonBuilder().toJson(datos);
	        JsonArray jsonData = new JsonParser().parse(json).getAsJsonArray();
	        String rutaTemporal = rutaBase + nombreBase + "_tmp_" + idComprobante + "." + extension;
	
	        report.setDocumentResultSet(esquemaPrefactura, jsonData);
	
	        try (InputStream is = report.exportDocument(extension)) {
	            if (is == null) throw new Exception("exportDocument retorno null para comprobante: " + idComprobante);
	            FileUtils.copyInputStreamToFile(is, new File(rutaTemporal));
	        }
	
	        logger.write("Temporal FAC generado: " + rutaTemporal);
	        temporales.add(rutaTemporal);
	    }
	
	    return temporales;
	}
	
	private List<String> generarTemporalesTXTManual(Map<Long, List<DatosComprobanteCrystal>> datosPorComprobante, String rutaBase, String nombreBase) throws Exception {
	    List<String> temporales = new ArrayList<>();
	
	    for (Map.Entry<Long, List<DatosComprobanteCrystal>> entry : datosPorComprobante.entrySet()) {
	        Long idComprobante = entry.getKey();
	        DatosComprobanteCrystal dato = entry.getValue().get(0);
	        String contenido = generarContenidoTXTManual(dato);
	        String rutaTemporal = rutaBase + nombreBase + "_tmp_" + idComprobante + ".txt";
	
	        try (OutputStream os = new FileOutputStream(rutaTemporal)) {
	            os.write(contenido.getBytes("UTF-8"));
	        }
	
	        logger.write("Temporal TXT manual generado: " + rutaTemporal);
	        temporales.add(rutaTemporal);
	    }
	
	    return temporales;
	}
	
	private Map<Long, List<DatosComprobanteCrystal>> obtenerDatosParaDTE(Connection con, List<Prefactura> grupoDte, Long idEmpresa) {
	    Map<Long, List<DatosComprobanteCrystal>> datos = new HashMap<>();
	    SQL sql = new SQL();
	
	    for (Prefactura pref : grupoDte) {
	        List<DatosComprobanteCrystal> lista = obtenerDatosComprobante(con, sql, idEmpresa, pref.IdComprobante, "");
	        if (!lista.isEmpty()) {
	            datos.put(pref.IdComprobante, lista);
	        } else {
	            logger.write("Sin datos DTE para comprobante: " + pref.IdComprobante + " - omitido");
	        }
	    }
	    return datos;
	}
    
	private List<String> generarTemporalesDTE(Connection con, List<Prefactura> prefacturas, String rutaBase, String nombreBase, String formato, Long idEmpresa, SalidaGenerarInformePrefacturas salida) throws Exception {
        ConsultaDocumentoDTE.Entrada entradaDTE = new ConsultaDocumentoDTE.Entrada();
        entradaDTE.p_IdEmpresa = idEmpresa;
        entradaDTE.p_IdUsuario = 1L;
        entradaDTE.p_Tipo      = formato.toUpperCase();

        for (Prefactura pref : prefacturas) {
        	if (pref.IdRespuestaDTE == null || pref.IdRespuestaDTE == 0L) {
        	    logger.write("ADVERTENCIA: comprobante " + pref.IdComprobante + " sin IdRespuestaDTE valido - omitido");
        	    HashMap<String, String> mapDte = new HashMap<>();
        	    mapDte.put("IdComprobante", String.valueOf(pref.IdComprobante));
        	    salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "DTE_SIN_ID_RESPUESTA", mapDte, con, idEmpresa));
        	    continue;
        	}
            ConsultaDocumentoDTE.DocumentoDTE doc = new ConsultaDocumentoDTE.DocumentoDTE();
            doc.IdDocumento = String.valueOf(pref.IdRespuestaDTE);
            doc.FileName    = nombreBase + "_" + pref.IdRespuestaDTE + "." + formato.toLowerCase();
            entradaDTE.p_IdDocumentos.add(doc);
        }

        if (entradaDTE.p_IdDocumentos.isEmpty()) {
            logger.write("ADVERTENCIA: ningun comprobante DTE valido para procesar");
            return new ArrayList<>();
        }

        ConsultaDocumentoDTE.Salida salidaDTE = new ConsultaDocumentoDTE().logica(con, entradaDTE);

        List<String> temporales = new ArrayList<>();
        for (ConsultaDocumentoDTE.DocumentoDTE_Salida docSalida : salidaDTE.P_FILES) {
            if (docSalida.resultado.equals("OK") && !docSalida.encodedString.isEmpty()) {
                try {
                    String rutaTemporal = rutaBase + nombreBase + "_tmp_dte_" + docSalida.dteNumber + "." + formato.toLowerCase();
                    byte[] contenido    = Base64.decodeBase64(docSalida.encodedString);
                    try (FileOutputStream fos = new FileOutputStream(rutaTemporal)) {
                        fos.write(contenido);
                        fos.flush();
                    }
                    logger.write("Temporal DTE generado: " + rutaTemporal);
                    temporales.add(rutaTemporal);
                } catch (Exception e) {
                    logger.write("Error escribiendo DTE " + docSalida.dteNumber + ": " + e.getMessage());
                    HashMap<String, String> mapDteErr = new HashMap<>();
                    mapDteErr.put("DteNumber", String.valueOf(docSalida.dteNumber));
                    mapDteErr.put("Mensaje", e.getMessage());
                    salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "DTE_ERROR_ARCHIVO", mapDteErr, con, idEmpresa));
                    salida.P_OUT_ERRORESCONTADOR++;
                }

            } else {
                logger.write("Error DTE " + docSalida.dteNumber + ": " + docSalida.mensaje);
                HashMap<String, String> mapDteSrv = new HashMap<>();
                mapDteSrv.put("DteNumber", String.valueOf(docSalida.dteNumber));
                mapDteSrv.put("Mensaje", docSalida.mensaje);
                salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "DTE_ERROR_SERVICIO", mapDteSrv, con, idEmpresa));
                salida.P_OUT_ERRORESCONTADOR++;
            }
        }

        return temporales;
    }

    private String mergearTemporales(List<String> temporales, String rutaFinal, String extension) throws Exception {
        if (temporales.isEmpty()) {
            throw new Exception("No se generaron archivos temporales para: " + rutaFinal);
        }
        if (temporales.size() == 1) {
            new File(temporales.get(0)).renameTo(new File(rutaFinal));
        } else {
            if (extension.equals("pdf")) {
                mergePDFs(temporales, rutaFinal);
            } else {
                mergeTXTs(temporales, rutaFinal);
            }
        }
        logger.write("Archivo final generado: " + rutaFinal);
        return rutaFinal;
    }

    private String mergePDFs(List<String> rutasPDFs, String rutaFinal) throws Exception {
        Document document = new Document();
        FileOutputStream fos = new FileOutputStream(rutaFinal);
        PdfCopy copy = new PdfCopy(document, fos);
        document.open();

        List<PdfReader> readers = new ArrayList<>();
        int paginasAgregadas = 0;
        try {
            for (String rutaPDF : rutasPDFs) {
                if (!new File(rutaPDF).exists()) {
                    logger.write("ADVERTENCIA: archivo temporal no existe: " + rutaPDF);
                    continue;
                }
                try {
                    PdfReader reader = new PdfReader(rutaPDF);
                    readers.add(reader);
                    for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                        copy.addPage(copy.getImportedPage(reader, i));
                        paginasAgregadas++;
                    }
                } catch (Exception e) {
                    logger.write("ADVERTENCIA: PDF corrupto omitido: " + rutaPDF + " - " + e.getMessage());
                }
            }
            if (paginasAgregadas > 0) {
                document.close();
            }
        } finally {
            for (PdfReader reader : readers) {
                try { reader.close(); } catch (Exception e) {
                	logger.write("ADVERTENCIA: no se pudo cerrar el archivo");
                }
            }
            fos.close();
        }

        if (paginasAgregadas == 0) {
            FileUtils.deleteQuietly(new File(rutaFinal));
            throw new Exception("No se genero el PDF: ningun archivo temporal valido entre "
                + rutasPDFs.size() + " esperados (" + rutaFinal + ")");
        }

        for (String rutaPDF : rutasPDFs) {
            try {
                FileUtils.forceDelete(new File(rutaPDF));
            } catch (Exception e) {
                logger.write("ADVERTENCIA: no se pudo borrar temporal: " + rutaPDF);
            }
        }

        logger.write("PDF mergeado: " + rutaFinal);
        return rutaFinal;
    }

    private String mergeTXTs(List<String> rutasTXTs, String rutaFinal) throws Exception {
        try (OutputStream os = new FileOutputStream(rutaFinal)) {
            for (String rutaTXT : rutasTXTs) {
                try (InputStream is = new FileInputStream(rutaTXT)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) > 0) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                try {
                    FileUtils.forceDelete(new File(rutaTXT));
                } catch (Exception e) {
                    logger.write("ADVERTENCIA: no se pudo borrar temporal: " + rutaTXT);
                }
            }
        }
        logger.write("TXT mergeado: " + rutaFinal);
        return rutaFinal;
    }
       
    private String generarContenidoTXTManual(DatosComprobanteCrystal d) {
        int ANCHO_DOC    = 256;
        int OFFSET_TOTALES   = 87;
        int ANCHO_LABEL_TOT  = 25;
        int ANCHO_MONTO_TOT  = 15;
        StringBuilder sb = new StringBuilder(128);

        Double montoOperacion;
        String accionLetra;
        if (d.Compra != null && d.Compra > 0) {
            montoOperacion = d.Compra;
            accionLetra = "C";
        } else if (d.Venta != null && d.Venta > 0) {
            montoOperacion = d.Venta;
            accionLetra = "V";
        } else {
            montoOperacion = 0.0;
            accionLetra = "";
        }

        String fechaLiq = "";
        if (d.FechaLiquidacion != null && d.FechaLiquidacion.length() == 10) {
            fechaLiq = d.FechaLiquidacion.substring(6, 10)
                     + d.FechaLiquidacion.substring(3, 5)
                     + d.FechaLiquidacion.substring(0, 2);
        } else {
            fechaLiq = d.FechaLiquidacion != null ? d.FechaLiquidacion : "";
        }

        String comisionStr;
        if (d.TotalComisCmbte != null && d.TotalComisCmbte > 0 && d.Monto != null && d.Monto > 0) {
            double porcentaje = (d.TotalComisCmbte / d.Monto) * 100;
            java.text.NumberFormat nfPct = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
            nfPct.setMinimumFractionDigits(2);
            nfPct.setMaximumFractionDigits(2);
            comisionStr = "Comision " + nfPct.format(porcentaje) + "%";
        } else {
            comisionStr = "Comision";
        }

        String glosaSaldo = obtenerGlosaSaldo(d);
        double subtotalCargos = d.TotalComisCmbte + d.TotalDereCmbte
                + d.TotalGastoCmbte + d.TotalImptoCmbte;
        
        // Fecha centrada
        String fecha = d.FechaComprobante != null ? d.FechaComprobante.replace("-", "/") : "";
        int espacios = (128 - fecha.length()) / 2;
        sb.append(String.format("%" + (espacios + fecha.length()) + "s", fecha)).append("\n");

        // Nombre cliente
        sb.append(campo("", 28))
          .append(campo(d.RazonSocialReceptor, ANCHO_DOC - 5)).append("\n\n");

        // Direccion y comuna
        sb.append(campo("", 28))
          .append(campo(d.DireccionReceptor, 40))
          .append(campoD(d.ComunaReceptor, 20)).append("\n\n");

        // RUT Agente Folio Empresa
        sb.append(campo("", 10))
          .append(campo(d.DocumentoIdentidadReceptor, 18))
          .append(campo("", 30))
          .append(campo(d.NombreEmpleadoEjecutivoPortafolio, 38))
          .append(campo("", 1))
          .append(campoD(String.valueOf(d.FolioComprobante), 15))
          .append(campo("", 5))
          .append(campo(d.NombreEmpresa, 25)).append("\n\n");

        // Sucursal
        if (d.Sucursal != null && !d.Sucursal.trim().isEmpty()) {
            sb.append(campo("", 50))
              .append(campo("Sucursal: " + d.Sucursal, ANCHO_DOC - 3)).append("\n\n");
        }

        // Detalle NEMO
        String SEP = campo("", 15); // separador de 15 espacios
        sb.append(campo(d.NemotecnicoInstrumento, 15))
          .append(SEP)
          .append(campo(fechaLiq, 14))
          .append(SEP)
          .append(campoD(formatearPrecio(d.PRECIO), 18))
          .append(SEP)
          .append(campoD(formatearEntero(d.Cantidad), 10))
          .append(SEP)
          .append(campoD(formatearEntero(montoOperacion), 13))
          .append(SEP)
          .append(campo(accionLetra, 1)).append("\n");

        sb.append(campo("", 117))
        	.append(campoD(formatearEntero(d.Monto), 13)).append("\n");
        for (int i = 0; i < 8; i++) sb.append("\n");

        // Totales
        sb.append(campo("", OFFSET_TOTALES))
          .append(campo(comisionStr, ANCHO_LABEL_TOT))
          .append(campoD(formatearEntero(d.TotalComisCmbte), ANCHO_MONTO_TOT)).append("\n");
        sb.append(campo("", OFFSET_TOTALES))
          .append(campo("Derechos", ANCHO_LABEL_TOT))
          .append(campoD(formatearEntero(d.TotalDereCmbte), ANCHO_MONTO_TOT)).append("\n");
        sb.append(campo("", OFFSET_TOTALES))
          .append(campo("Gastos", ANCHO_LABEL_TOT))
          .append(campoD(formatearEntero(d.TotalGastoCmbte), ANCHO_MONTO_TOT)).append("\n");
        sb.append(campo("", OFFSET_TOTALES))
          .append(campo("I.V.A.", ANCHO_LABEL_TOT))
          .append(campoD(formatearEntero(d.TotalImptoCmbte), ANCHO_MONTO_TOT)).append("\n");
        sb.append(campo("", OFFSET_TOTALES))
          .append(campo("", ANCHO_LABEL_TOT))
          .append(campoD(formatearEntero(subtotalCargos), ANCHO_MONTO_TOT)).append("\n");

        sb.append(campo("", 27))
          .append(campo(glosaSaldo, 85))
          .append(campoD(formatearEntero(d.MontoLiquidacionFinal), ANCHO_MONTO_TOT)).append("\n\n");

        return sb.toString();
    }
    
    private String obtenerGlosaSaldo(DatosComprobanteCrystal d) {
        if (d.GlosaSaldo == null || d.GlosaSaldo.trim().isEmpty()) return "";
        if ("COMMEN".equals(d.IdConceptoCmbte)) return "";

        List<String> tiposValidos = java.util.Arrays.asList("FAC","FAE","FOE","FOC","BOE","FEE","FOX");
        boolean esTipoValido = tiposValidos.contains(d.IdTipoComprobante);
        boolean esNEEAnulacion = "NEE".equals(d.IdTipoComprobante)
            && ("ANANPR".equals(d.IdTipoOperacion) || "ANANEM".equals(d.IdTipoOperacion));

        if (esTipoValido || esNEEAnulacion) {
            String texto = d.GlosaSaldo.contains("CONTRA") || d.GlosaSaldo.contains("contra")
                ? "En Contra Cliente"
                : "A Favor Cliente";
            return texto + " (" + d.CodigoCondicionLiquidacion + ")";
        }
        return "";
    }

    private String campo(String val, int ancho) {
        if (val == null) val = "";
        if (val.length() > ancho) val = val.substring(0, ancho);
        return String.format("%-" + ancho + "s", val);
    }

    private String campoD(String val, int ancho) {
        if (val == null) val = "";
        if (val.length() > ancho) val = val.substring(0, ancho);
        return String.format("%" + ancho + "s", val);
    }
    
    private List<DatosComprobanteCrystal> obtenerDatosComprobante(Connection con, SQL sql, Long idEmpresa, Long idComprobante, String logoB64) {
        List<DatosComprobanteCrystal> lista = new ArrayList<>();
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("IdEmpresa",     idEmpresa);
            params.put("IdComprobante", idComprobante);

            HashMap<String, Class> paramOUT = new HashMap<>();
            paramOUT.put("ResultSet_0", DatosComprobanteCrystal.class);

            HashMap<String, Object> resultado = sql.call(con, SQL_CALL_CONSULTAR_COMPROBANTE, params, paramOUT);
            List<HashMap> rawList = (List<HashMap>) resultado.get("ResultSet_0");

            if (rawList == null || rawList.isEmpty()) return lista;

            for (HashMap raw : rawList) {
                DatosComprobanteCrystal dato = new DatosComprobanteCrystal();
                dato.IdComprobante                               = toLong(raw.get("IdComprobante"));
                dato.FolioComprobante                            = toLong(raw.get("FolioComprobante"));
                dato.IdTipoComprobante                           = toStr(raw.get("IdTipoComprobante"));
                dato.Comprobante                                 = toStr(raw.get("Comprobante"));
                dato.Folio                                       = toLong(raw.get("Folio"));
                dato.FechaComprobante                            = toStr(raw.get("FechaComprobante"));
                dato.NombreTipoComprobante                       = toStr(raw.get("NombreTipoComprobante"));
                dato.AccionTipoOperacion                         = toStr(raw.get("AccionTipoOperacion"));
                dato.IdCliente                                   = toLong(raw.get("IdCliente"));
                dato.TipoDocumentoIdentidadReceptor              = toStr(raw.get("TipoDocumentoIdentidadReceptor"));
                dato.DocumentoIdentidadReceptor                  = toStr(raw.get("DocumentoIdentidadReceptor"));
                dato.RazonSocialReceptor                         = toStr(raw.get("RazonSocialReceptor"));
                dato.NombrePortafolio                            = toStr(raw.get("NombrePortafolio"));
                dato.NacionalidadReceptor                        = toStr(raw.get("NacionalidadReceptor"));
                dato.DireccionReceptor                           = toStr(raw.get("DireccionReceptor"));
                dato.CiudadReceptor                              = toStr(raw.get("CiudadReceptor"));
                dato.ComunaReceptor                              = toStr(raw.get("ComunaReceptor"));
                dato.IdCondicionLiquidacion                      = toStr(raw.get("IdCondicionLiquidacion"));
                dato.CodigoCondicionLiquidacion                  = toStr(raw.get("CodigoCondicionLiquidacion"));
                dato.FechaLiquidacion                            = toStr(raw.get("FechaLiquidacion"));
                dato.IdEmpleadoEjecutivoPortafolio               = toLong(raw.get("IdEmpleadoEjecutivoPortafolio"));
                dato.NombreEmpleadoEjecutivoPortafolio           = toStr(raw.get("NombreEmpleadoEjecutivoPortafolio"));
                dato.NombreEmpresa                               = toStr(raw.get("NombreEmpresa"));
                dato.Sucursal                                    = toStr(raw.get("Sucursal"));
                dato.NemotecnicoInstrumento                      = toStr(raw.get("NemotecnicoInstrumento"));
                dato.Monto                                       = toDouble(raw.get("Monto"));
                dato.MontoLiquidacion                            = toDouble(raw.get("MontoLiquidacion"));
                dato.MontoLiquidacionFinal                       = toDouble(raw.get("MontoLiquidacionFinal"));
                dato.TotalComisCmbte                             = toDouble(raw.get("TotalComisCmbte"));
                dato.TotalImptoCmbte                             = toDouble(raw.get("TotalImptoCmbte"));
                dato.TotalDereCmbte                              = toDouble(raw.get("TotalDereCmbte"));
                dato.TotalGastoCmbte                             = toDouble(raw.get("TotalGastoCmbte"));
                dato.TotalRetencionCmbte                         = toDouble(raw.get("TotalRetencionCmbte"));
                dato.Detalle                                     = toStr(raw.get("Detalle"));
                dato.PRECIO                                      = toDouble(raw.get("PRECIO"));
                dato.PrecioOtraMoneda                            = toDouble(raw.get("PrecioOtraMoneda"));
                dato.Cantidad                                    = toDouble(raw.get("Cantidad"));
                dato.PrecioPromedio                              = toDouble(raw.get("PrecioPromedio"));
                dato.IdMonedaComprobanteDetalle                  = toStr(raw.get("IdMonedaComprobanteDetalle"));
                dato.ValorTipoCambio                             = toDouble(raw.get("ValorTipoCambio"));
                dato.CodigoMonedaComprobanteDetalle              = toStr(raw.get("CodigoMonedaComprobanteDetalle"));
                dato.IdMonedaComprobanteComprobanteDetalle       = toStr(raw.get("IdMonedaComprobanteComprobanteDetalle"));
                dato.CodigoMonedaComprobanteComprobanteDetalle   = toStr(raw.get("CodigoMonedaComprobanteComprobanteDetalle"));
                dato.SimboloMonedaComprobanteComprobanteDetalle  = toStr(raw.get("SimboloMonedaComprobanteComprobanteDetalle"));
                dato.Compra                                      = toDouble(raw.get("Compra"));
                dato.Venta                                       = toDouble(raw.get("Venta"));
                dato.IdMonedaLiquidacion                         = toStr(raw.get("IdMonedaLiquidacion"));
                dato.CodigoMonedaLiquidacion                     = toStr(raw.get("CodigoMonedaLiquidacion"));
                dato.SimboloMonedaLiquidacion                    = toStr(raw.get("SimboloMonedaLiquidacion"));
                dato.FechaSistema                                = toStr(raw.get("FechaSistema"));
                dato.RazonSocialEmisor                           = toStr(raw.get("RazonSocialEmisor"));
                dato.GiroEmisor                                  = toStr(raw.get("GiroEmisor"));
                dato.TipoDocumentoIdentidadEmisor                = toStr(raw.get("TipoDocumentoIdentidadEmisor"));
                dato.DocumentoIdentidadEmisor                    = toStr(raw.get("DocumentoIdentidadEmisor"));
                dato.DireccionEmisor                             = toStr(raw.get("DireccionEmisor"));
                dato.ComunaEmisor                                = toStr(raw.get("ComunaEmisor"));
                dato.CiudadEmisor                                = toStr(raw.get("CiudadEmisor"));
                dato.TelefonoEmisor                              = toStr(raw.get("TelefonoEmisor"));
                dato.DireccionElectronicaEmisor                  = toStr(raw.get("DireccionElectronicaEmisor"));
                dato.GlosaNCredito                               = toStr(raw.get("GlosaNCredito"));
                dato.IdTipoComprobanteAsocANC                    = toStr(raw.get("IdTipoComprobanteAsocANC"));
                dato.TipoComprobanteAsocANC                      = toStr(raw.get("TipoComprobanteAsocANC"));
                dato.IdComprobanteReferenciaAsocANC              = toLong(raw.get("IdComprobanteReferenciaAsocANC"));
                dato.FolioComprobanteReferenciaAsocANC           = toLong(raw.get("FolioComprobanteReferenciaAsocANC"));
                dato.FechaComprobanteAsocANC                     = toStr(raw.get("FechaComprobanteAsocANC"));
                dato.Motivo                                      = toStr(raw.get("Motivo"));
                dato.GlosaSaldo                                  = toStr(raw.get("GlosaSaldo"));
                dato.GlosaAPB                                    = toStr(raw.get("GlosaAPB"));
                dato.GlosaTipoProducto                           = toStr(raw.get("GlosaTipoProducto"));
                dato.Codigo                                      = toStr(raw.get("Codigo"));
                dato.DescripcionPE                               = toStr(raw.get("DescripcionPE"));
                dato.ModPrefFact                                 = toStr(raw.get("ModPrefFact"));
                dato.ValorUnitario                               = toDouble(raw.get("ValorUnitario"));
                dato.ComisionBVLMonedaLiquidacion                = toDouble(raw.get("ComisionBVLMonedaLiquidacion"));
                dato.ComisionSABMonedaLiquidacion                = toDouble(raw.get("ComisionSABMonedaLiquidacion"));
                dato.ComisionSMVMonedaLiquidacion                = toDouble(raw.get("ComisionSMVMonedaLiquidacion"));
                dato.ComisionCavaliMonedaLiquidacion             = toDouble(raw.get("ComisionCavaliMonedaLiquidacion"));
                dato.FondoGarantiaMonedaLiquidacion              = toDouble(raw.get("FondoGarantiaMonedaLiquidacion"));
                dato.FondoLiquidacionMonedaLiquidacion           = toDouble(raw.get("FondoLiquidacionMonedaLiquidacion"));
                dato.OtrasComisionesMonedaLiquidacion            = toDouble(raw.get("OtrasComisionesMonedaLiquidacion"));
                dato.ImpuestoIGVMonedaLiquidacion                = toDouble(raw.get("ImpuestoIGVMonedaLiquidacion"));
                dato.FechaVencimiento                            = toStr(raw.get("FechaVencimiento"));
                dato.Tasa                                        = toDouble(raw.get("Tasa"));
                dato.DiasPlazo                                   = toDouble(raw.get("DiasPlazo"));
                dato.NombreBovedaCuentaCustodioPortafolio        = toStr(raw.get("NombreBovedaCuentaCustodioPortafolio"));
                dato.NombreTipoCustodia                          = toStr(raw.get("NombreTipoCustodia"));
                dato.NombreCompletoBancoCustodio                 = toStr(raw.get("NombreCompletoBancoCustodio"));
                dato.NombreCompletoBancoLiquidador               = toStr(raw.get("NombreCompletoBancoLiquidador"));
                dato.CodigoDeCuenta                              = toStr(raw.get("CodigoDeCuenta"));
                dato.Intereses                                   = toDouble(raw.get("Intereses"));
                dato.ReajustesVAC                                = toDouble(raw.get("ReajustesVAC"));
                dato.ValorParidad                                = toDouble(raw.get("ValorParidad"));
                dato.ValorParidadSecundaria                      = toDouble(raw.get("ValorParidadSecundaria"));
                dato.MuestraObservacion                          = toStr(raw.get("MuestraObservacion"));
                dato.IdEstadoModPrefFact                         = toStr(raw.get("IdEstadoModPrefFact"));
                dato.IdConceptoCmbte                             = toStr(raw.get("IdConceptoCmbte"));
                dato.IdClasificacionTipoInstrumento              = toStr(raw.get("IdClasificacionTipoInstrumento"));
                dato.PrecioDecimales                             = toDouble(raw.get("PrecioDecimales"));
                dato.TasaDecimales                               = toDouble(raw.get("TasaDecimales"));
                dato.TipoReferenciaOtrosConceptos                = toStr(raw.get("TipoReferenciaOtrosConceptos"));
                dato.FolioReferenciaOtrosConceptos               = toStr(raw.get("FolioReferenciaOtrosConceptos"));
                dato.FechaReferenciaOtrosConceptos               = toStr(raw.get("FechaReferenciaOtrosConceptos"));
                dato.RazonReferenciaOtrosConceptos               = toStr(raw.get("RazonReferenciaOtrosConceptos"));
                dato.IdTipoOperacion                             = toStr(raw.get("IdTipoOperacion"));
                dato.ValorAPagar                                 = toDouble(raw.get("ValorAPagar"));
                dato.Glosa                                       = toStr(raw.get("Glosa"));
                dato.TipoDocumentoIdentidadReceptorpE            = toStr(raw.get("TipoDocumentoIdentidadReceptorpE"));
                dato.DocumentoIdentidadReceptorPe                = toStr(raw.get("DocumentoIdentidadReceptorPe"));
                dato.TipoDocumentoIdentidadAsociado              = toStr(raw.get("TipoDocumentoIdentidadAsociado"));
                dato.DocumentoIdentidadAsociado                  = toStr(raw.get("DocumentoIdentidadAsociado"));
                dato.TipoDocumentoIdentidadAdicionalReceptor     = toStr(raw.get("TipoDocumentoIdentidadAdicionalReceptor"));
                dato.DocumentoIdentidadAdicionalReceptor         = toStr(raw.get("DocumentoIdentidadAdicionalReceptor"));
                dato.LogoCorredora                               = logoB64;
                lista.add(dato);
            }

        } catch (Exception e) {
            logger.write("Error obteniendo comprobante " + idComprobante, e);
        }

        return lista;
    }
    
    private String formatearPrecio(Double val) {
        if (val == null) return "0";
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(new java.util.Locale("es", "CL"));
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(6);
            return nf.format(val);
        } catch (Exception e) {
            return "0";
        }
    }
    
    private String formatearEntero(Double val) {
        if (val == null) return "0";
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(new java.util.Locale("es", "CL"));
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            return nf.format(val);
        } catch (Exception e) {
            return "0";
        }
    }
    
    private String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws Exception {
    	HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		Imagen logo = new SQL().query(conn, SQL_CONSULTAR_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(logo.Contenido);
		return contenido;
    }
    private String toStr(Object val) {
        return val == null ? "" : val.toString();
    }
    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long) return (Long) val;
        try { return Long.parseLong(val.toString().split("\\.")[0]); }
        catch (Exception e) { return 0L; }
    }
    private Double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Double) return (Double) val;
        try { return Double.parseDouble(val.toString()); }
        catch (Exception e) { return 0.0; }
    }

    public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream().findFirst().orElse(new RaizArchivo());
		return raiz;
	}

    private String comprimirArchivos(String timestamp, String rutaBase, String nombreBase, List<String> rutasArchivos) throws Exception {
        logger.write("Comprimiendo " + rutasArchivos.size() + " archivos");
        long inicio = System.currentTimeMillis();

        String nombreZip = nombreBase + timestamp + ".zip";
        try (OutputStream os = new FileOutputStream(rutaBase + nombreZip);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            for (String rutaArchivo : rutasArchivos) {
                try (InputStream is = new FileInputStream(rutaArchivo)) {
                    zos.putNextEntry(new ZipEntry(FilenameUtils.getName(rutaArchivo)));
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) > 0) {
                        zos.write(buffer, 0, bytesRead);
                    }
                    zos.closeEntry();
                }
                FileUtils.forceDelete(new File(rutaArchivo));
            }
        }

        logger.write("FIN comprimirArchivos: " + nombreZip + " en " + (System.currentTimeMillis() - inicio) + "ms");
        return nombreZip;
    }
    
    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
    	return null;
    }
    
    public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try {
			Connection conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
			EntradaGenerarInformePrefacturas entrada = new EntradaGenerarInformePrefacturas();

	        entrada.p_IdEmpresa = 1L;
	        entrada.p_IdUsuario = 1L;
	        entrada.p_FechaComprobante = "2026-04-21";
	        //entrada.p_ListaIdPortafolio = new ArrayList<>(List.of(6697L));
	        entrada.p_FormatoSalida = "PDF";  // "PDF", "TXT", "TODOS"
	        entrada.p_IdProceso = 1L;
	        entrada.p_DataSource = PlataformaUtils.getDESADatasource();
	        entrada.p_ExportarTodo = true; 
			SalidaGenerarInformePrefacturas salida = new GenerarInformePrefacturas().logica(conn,entrada);
			System.out.println(salida);
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== FIN TEST ==");
	}
}