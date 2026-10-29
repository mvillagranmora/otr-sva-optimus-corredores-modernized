package cl.bcs.normativa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.Fecha;

public class GenerarCartasCIR1985 implements JavaCall {

    private static final Log logger = new Log("GenerarCartasCIR1985");

    private final String SQL_CONSULTAR_ARCHIVO_NORMATIVA 	= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarArchivoNormativa.sql");
    private final String SQL_SELECT_ORDENES_EJECUTADAS   	= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarOrdenesEjecutadas.sql");
    private final String SQL_CONSULTAR_VERSION_ESQUEMA   	= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarVersionEsquema.sql");
    private final String SQL_CONSULTAR_LOGO              	= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarLogo.sql");
    private final String SQL_CONSULTAR_RUTA_BASE         	= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarRutaBase.sql");
    private final String SQL_CONSULTAR_SFTP_MAESTRO      	= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarSFTPMaestro.sql");
    private final String SQL_CONSULTAR_DATOS_CIR1985_MASIVO = loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarDatosCir1985Masivo.sql");
    private final String SQL_CONSULTAR_DATOS_CORREDORA 		= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarDatosCorredora.sql");
    private final String SQL_INSERT_CLIENTE_TABLA_PASO   	= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_InsertClienteTablaPaso.sql");
    private final String SQL_DELETE_CLIENTE_TABLA_PASO   	= loadResourceToString("cl/bcs/normativa/sql/GenerarCartasCIR1985_DeleteClienteTablaPaso.sql");

    private static final HashMap<String, String> esquemaCIR1985 = new HashMap<>();
    static {
    	esquemaCIR1985.put("LogoCorredora", "blob");
    }

    // CLASES
    public static class GenerarCartasCIR1985_Entrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public Long   p_IdCliente;
        public List<Long> p_IdClientes;
        public Long   p_IdPrograma;
        public String p_TipoOrigen;
        public DataSource p_DataSource;
    }

    public static class SalidaGenerarCartasCIR1985 extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
        public Long p_out_ContadorCartas;
        public String p_out_RutaCartas;

        public SalidaGenerarCartasCIR1985() {
            P_OUT_ERRORESCONTADOR = 0L;
            P_OUT_MENSAJES        = new ArrayList<>();
            p_out_ContadorCartas  = 0L;
            p_out_RutaCartas      = "";
        }

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ArchivoNormativa {
        public String IdArchivo;
        public String NombreArchivo;
        public String ServicioArchivo;
        public String Esquema;
    }

    public static class Esquema {
        public String p_FechaDesde;
        public String p_out_Esquema;
        public String p_out_Version;
    }

    public static class Imagen {
        public byte[] Contenido;
    }

    public static class DatosCIR1985Crystal {
        public Long       IdEjecucion;
        public Long       IdDocumento;
        public String     NombreCompleto;
        public String     DireccionFisica;
        public String     RutCliente;
        public String     Telefono;
        public String     DireccionElectronica;
        public Long       FolioGeneracion;
        public Long       IdOrden;
        public String     Folio;
        public Long       IdCliente;
        public Long       IdPortafolio;
        public String     CodigoPortafolio;
        public String     IdClasificacionTipoInstrumento;
        public String     IdTipoInstrumento;
        public String     IdTipoOperacion;
        public Long       IdInstrumento;
        public BigDecimal Cantidad;
        public BigDecimal Monto;
        public BigDecimal MontoValorizado;
        public BigDecimal Precio;
        public String     IdMoneda;
        public String     IdCondicionDeLiquidacion;
        public String     FechaRecepcion;
        public String     FechaAceptacion;
        public String     TipoEjecucion;
        public String     FechaEjecucion;
        public String     FechaHoraEjecucion;
        public String     IdBolsa;
        public String     EsOD;
        public String     Modalidad;
        public BigDecimal PrecioEjecucion;
        public BigDecimal MontoTransado;
        public BigDecimal CantidadTransada;
        public BigDecimal ComisionTransada;
        public String     MejorPrecioOtraBolsa;
        public BigDecimal CantidadMejorPrecioOtraBolsa;
        public String     PrecioMinHoraBolsaMcdo;
        public String     PrecioMaxHoraBolsaMcdo;
        public BigDecimal MejorPrecioMercado;
        public BigDecimal PrecioMinimoMercado;
        public String     HoraPrecioMinimoMercado;
        public String     IdBolsaPrecioMinimoMercado;
        public BigDecimal PrecioMaximoMercado;
        public String     HoraPrecioMaximoMercado;
        public String     IdBolsaPrecioMaximoMercado;
        public String     PrecioMinHoraBolsaCorr;
        public String     PrecioMaxHoraBolsaCorr;
        public BigDecimal MayorPrecioOperacionCorredor;
        public String     HoraPrecioMayorCorredor;
        public BigDecimal MenorPrecioOperacionCorredor;
        public String     HoraPrecioMenorCorredor;
        public Long       IdEmpleadoDigitador;
        public Long       FolioComprobanteFacturacion;
        public Timestamp  FechaGeneracion;
        public String     FolioExterno;
        public String     IdEstadoNotificacion;
        public String     IdEstadoCarta;
        public Timestamp  FechaIngreso;
        public Timestamp  FechaCreacion;
        public Long       IdUsuarioCreador;
        public String     NombreAgente;
        public String     AccionOperacion;
        public String     TelefonoAgente;
        public String     FechaSistemaLOCAL;
        public String     HoraSistemaLOCAL;
        public String     MensajeError;
        public String     Nemotecnico;
        public String     PrecioMinHoraBolsaBEC;
        public String     PrecioMaxHoraBolsaBEC;
        public String     RelacionCorredor;
        public String     FolioOrden;
        public String     InstrumentoNegociacion;
        public String     RazonCorredora;
        public String     DireccionCorredora;
        public String     RutCorredora;
        public String     TelefonoCorredora;
        public String 	  FechaReferenciaAgrupador;
        public BigDecimal MejorPrecioOtraBolsaNum;
    }

    public static class DatosCIR1985CrystalCompleto extends DatosCIR1985Crystal {
        public String LogoCorredora;
    }
    
    public static class RutaArchivo{
    	public String RutaCircular;
    	public String RutaArchivo;
    	public String RutaSFTP;
    }
    
    public static class ObjEjecucionOrdenes {
        public Long   IdPortafolio;
        public Long   IdCliente;
        public String ValorIndicador;
        public String FechaReferencia;
        public String FechaEjecucion;
    }

    public static class ClienteTablaPaso {
        public Long IdCliente;
        public Long IdPrograma;
        public ClienteTablaPaso() {}
        public ClienteTablaPaso(Long idCliente, Long idPrograma) {
            this.IdCliente = idCliente;
            this.IdPrograma = idPrograma;
        }
    }

    public static class ProgramaTablaPaso {
        public Long IdPrograma;
        public ProgramaTablaPaso() {}
        public ProgramaTablaPaso(Long idPrograma) { this.IdPrograma = idPrograma; }
    }
    
    public static class ConfigSFTP {
        public String Ip;
        public Long   Puerto;
        public String Carpeta;
        public String Usuario;
        public String Clave;
    }
    
    public static class DatosCorredora{
    	public String RazonSocial;
        public String Direccion;
        public String RUT;
        public String Telefono;
    }

    // LOGICA
    public SalidaGenerarCartasCIR1985 logica(Connection con, GenerarCartasCIR1985_Entrada in) throws Exception {
        SQL sql = new SQL();
        SalidaGenerarCartasCIR1985 out = new SalidaGenerarCartasCIR1985();
        
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        Long parametroDias = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "NORMATIVA", "RANGO_CONSULTA_FECHA_CIRCULAR_1985");

        long diasVigenciaSFTP = 0L;
        try {
            diasVigenciaSFTP = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "NORMATIVA", "CIR1985_SFTP_DIAS_VIGENCIA");
        } catch (Exception e) {
            logger.write("CIR1985_SFTP_DIAS_VIGENCIA no configurado, limpieza SFTP inactiva");
        }

        // Validar rango de fechas solo para proceso MANUAL
        if ("MANUAL".equals(in.p_TipoOrigen)) {
            long diasRango = ChronoUnit.DAYS.between(
                LocalDate.parse(in.p_FechaDesde),
                LocalDate.parse(in.p_FechaHasta)
            );
            if (diasRango > parametroDias) {
            	logger.write("Rango de fechas excedido: " + diasRango + " dias (max: " + parametroDias + ")");
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "RANGO_FECHA_EXCEDIDO", new HashMap<>(), con, in.p_IdEmpresa));
                return out;
            }
        }
        
        logger.write("INICIO GenerarCartasCIR1985");
        long inicio = System.currentTimeMillis();
        
        // Ruta base descomentar para pruebas locales
//        String rutaBase = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Prueba" + File.separator;
//        String rutaCircular = "nor/";
        logger.write("Obteniendo ruta base...");
        RutaArchivo rutaObj = null;
        try {
        	rutaObj = obtenerRutaBase(con, sql, in.p_IdEmpresa);
        } catch (Exception e) {
        	logger.write("Error obteniendo ruta base: " + e.getMessage());
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), con, in.p_IdEmpresa));
            return out;
        }
        
        String rutaBase     = rutaObj.RutaArchivo;
        String rutaCircular = rutaObj.RutaCircular;
        
        //Obtener version esquema
        Esquema esquema = null;
        try {
            esquema = consultarVersionEsquema(con, sql, fechaSistema);
        } catch (Exception e) {
        	logger.write("Error obteniendo version esquema: " + e.getMessage());
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_VERSION_ESQUEMA", new HashMap<>(), con, in.p_IdEmpresa));
            return out;
        }
        
        //Obtener archivo normativa template
        ArchivoNormativa archivo = null;
        try {
            archivo = consultarArchivoNormativa(con, in.p_IdEmpresa, sql, esquema);
        } catch (Exception e) {
        	logger.write("Error obteniendo archivo normativa: " + e.getMessage());
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_ARCHIVO_NORMATIVA", new HashMap<>(), con, in.p_IdEmpresa));
            return out;
        }

        List<Long> idClientes = (in.p_IdClientes != null && !in.p_IdClientes.isEmpty())
            ? in.p_IdClientes.stream().distinct().collect(Collectors.toList())
            : (in.p_IdCliente != null && in.p_IdCliente > 0 ? Collections.singletonList(in.p_IdCliente) : null);

        HashMap<String, Object> params = new HashMap<>();
        params.put("p_FechaDesde", "PAUTA".equals(in.p_TipoOrigen) ? in.p_FechaHasta : in.p_FechaDesde);
        params.put("p_FechaHasta", in.p_FechaHasta);
        params.put("p_TodosClientes", idClientes == null ? 1L : 0L);
        params.put("p_IdPrograma", in.p_IdPrograma != null ? in.p_IdPrograma : 0L);

        List<ObjEjecucionOrdenes> listaOrdenes;
        if (idClientes != null) {
            List<ClienteTablaPaso> clientesPaso = idClientes.stream()
                .map(idCliente -> new ClienteTablaPaso(idCliente, in.p_IdPrograma))
                .collect(Collectors.toList());
            sql.batch(con, SQL_INSERT_CLIENTE_TABLA_PASO, clientesPaso, ClienteTablaPaso.class);
            try {
                listaOrdenes = sql.query(con, SQL_SELECT_ORDENES_EJECUTADAS, params, ObjEjecucionOrdenes.class);
            } finally {
                List<ProgramaTablaPaso> paraEliminar = new ArrayList<>();
                paraEliminar.add(new ProgramaTablaPaso(in.p_IdPrograma));
                sql.batch(con, SQL_DELETE_CLIENTE_TABLA_PASO, paraEliminar, ProgramaTablaPaso.class);
            }
        } else {
            listaOrdenes = sql.query(con, SQL_SELECT_ORDENES_EJECUTADAS, params, ObjEjecucionOrdenes.class);
        }
        logger.write("Total portafolios a procesar: " + listaOrdenes.size());

        if (listaOrdenes.isEmpty()) {
            logger.write("Sin ordenes para procesar");
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L,
                PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "SIN_ORDENES_CIR1985",
                new HashMap<>(), con, in.p_IdEmpresa));
            return out;
        }

        generacionDeCartas(con, sql, archivo, listaOrdenes, in, out, rutaBase, rutaCircular, rutaObj.RutaSFTP, esquema, diasVigenciaSFTP);
        long fin = System.currentTimeMillis();
        logger.write("FIN::GenerarCartasCIR1985 - Total: " + (fin - inicio) + " ms");
        
        return out;
    }

    // GENERACION DE CARTAS
    private void generacionDeCartas(Connection con, SQL sql, ArchivoNormativa archivo, List<ObjEjecucionOrdenes> listaOrdenes, GenerarCartasCIR1985_Entrada in, SalidaGenerarCartasCIR1985 out, String rutaBase, String rutaCircular, String rutaSFTP, Esquema esquema, long diasVigenciaSFTP) throws Exception {
        logger.write("INICIO: generacion de cartas");
        long inicio = System.currentTimeMillis();

        // Cargar template
        logger.write("Cargando template: " + archivo.IdArchivo);
        ReportDocument report = new ReportDocument(Utils.getSchema(), in.p_DataSource);
        if (!report.readDocument(archivo.IdArchivo)) {
            throw new Exception("No se pudo cargar template: " + archivo.IdArchivo);
        }

        // Obtener logo
        logger.write("Obteniendo logo corredora...");
        long tLogo = System.currentTimeMillis();
        String logoB64 = obtenerLogoCorredoraB64(con, sql, in.p_IdEmpresa);
        logger.write("Logo obtenido - " + (System.currentTimeMillis() - tLogo) + " ms");

        // Fecha y hora sistema
        int segundosZona = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZona);
        String tFechaHora = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
        Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHora);
        String fechaSistemaLocal = Fecha.toFormat(vFechaHoraSistema, "dd-MM-yyyy");
        String horaSistemaLocal  = Fecha.toStringTime(vFechaHoraSistema);

        // Datos corredora
        DatosCorredora corredora = consultarDatosCorredora(con, sql, in.p_IdEmpresa);

        // Una sola consulta masiva con todos los clientes
        List<Long> todosLosClientes = listaOrdenes.stream()
            .map(o -> o.IdCliente)
            .distinct()
            .collect(Collectors.toList());

        HashMap<String, Object> paramsMasivo = new HashMap<>();
        paramsMasivo.put("p_FechaDesde", "PAUTA".equals(in.p_TipoOrigen) ? in.p_FechaHasta : in.p_FechaDesde);
        paramsMasivo.put("p_FechaHasta", in.p_FechaHasta);
        paramsMasivo.put("p_Version",    esquema.p_out_Version);
        paramsMasivo.put("p_IdClientes", todosLosClientes);

        long tConsulta = System.currentTimeMillis();
        List<DatosCIR1985Crystal> todosDatos = sql.query(con, SQL_CONSULTAR_DATOS_CIR1985_MASIVO, paramsMasivo, DatosCIR1985Crystal.class);
        logger.write("SQL masivo total: " + todosDatos.size() + " registros en " + (System.currentTimeMillis() - tConsulta) + " ms");

        // Agrupar en memoria por IdPortafolio + FechaReferenciaAgrupador
        HashMap<String, List<DatosCIR1985Crystal>> mapaDatos = new HashMap<>();
        for (DatosCIR1985Crystal dato : todosDatos) {
            String key = dato.IdPortafolio + "_" + dato.FechaReferenciaAgrupador;
            mapaDatos.computeIfAbsent(key, k -> new ArrayList<>()).add(dato);
        }

        int exitosas = 0;
        int fallidas  = 0;
        List<String> rutasArchivos = new ArrayList<>();

        for (ObjEjecucionOrdenes ord : listaOrdenes) {
            try {
                logger.write("Procesando portafolio: " + ord.IdPortafolio + " cliente: " + ord.IdCliente);

                String fechaRef = ord.FechaReferencia != null ? ord.FechaReferencia : ord.FechaEjecucion;
                String key = ord.IdPortafolio + "_" + fechaRef;
                List<DatosCIR1985Crystal> datos = mapaDatos.getOrDefault(key, new ArrayList<>());

                if (datos == null || datos.isEmpty()) {
                    logger.write("Sin datos para portafolio: " + ord.IdPortafolio);
                    continue;
                }

                // Construir datos completos con logo y corredora
                List<DatosCIR1985CrystalCompleto> datosCompletos = construirDatosConLogo(datos, logoB64, corredora, fechaSistemaLocal, horaSistemaLocal);
                
                String rutCliente       = datos.get(0).RutCliente       != null ? datos.get(0).RutCliente       : "";
                String codigoPortafolio = datos.get(0).CodigoPortafolio != null ? datos.get(0).CodigoPortafolio : "";
                String rutaArchivo = rutaBase + "CIRCULAR1985-" + rutCliente + "-" + codigoPortafolio + "-" + fechaRef.replace("-", "") + ".pdf";

                String json = Utils.getGsonBuilder().toJson(datosCompletos);
                JsonArray jsonData = new JsonParser().parse(json).getAsJsonArray();
                report.setDocumentResultSet(esquemaCIR1985, jsonData);

                try (InputStream is = report.exportDocument("pdf")) {
                    if (is == null) throw new Exception("exportDocument retorno null para portafolio: " + ord.IdPortafolio);
                    FileUtils.copyInputStreamToFile(is, new File(rutaArchivo));
                }

                rutasArchivos.add(rutaArchivo);
                exitosas++;
                logger.write("Carta generada: " + rutaArchivo);

            } catch (Exception e) {
                fallidas++;
                logger.write("Error generando carta portafolio " + ord.IdPortafolio + ": " + e.getMessage());
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "ERROR_GENERAR_CARTA_CIR1985", new HashMap<>(), con, in.p_IdEmpresa));
            }
        }

        if ("MANUAL".equals(in.p_TipoOrigen) && !rutasArchivos.isEmpty()) {
            String timestamp = in.p_FechaDesde + "_" + in.p_FechaHasta;
            String nombreZip = comprimirArchivos(timestamp, rutaBase, "CIR1985_", rutasArchivos, in.p_IdCliente);
            out.p_out_RutaCartas = rutaCircular + nombreZip;
        } else if ("PAUTA".equals(in.p_TipoOrigen) && !rutasArchivos.isEmpty()) {
            transferirSFTP(con, sql, in.p_IdEmpresa, rutaBase, rutaSFTP, out, in, rutasArchivos, diasVigenciaSFTP);
            out.p_out_RutaCartas = rutaBase;
        }

        out.p_out_ContadorCartas = (long) exitosas;
        if (exitosas > 0) {
            registrarLog(con, in, exitosas, fallidas);
        }
        long fin = System.currentTimeMillis();
        logger.write("FIN::generacionDeCartas - Exitosas: " + exitosas + " - Fallidas: " + fallidas + " - Total: " + (fin - inicio) + " ms");
    }

    // CONSULTAS
    private Esquema consultarVersionEsquema(Connection con, SQL sql, String fechaSistema) throws Exception {
    	logger.write("INICIO:: consultar version esquema");
    	long inicio = System.currentTimeMillis();
        HashMap<String, Object> params = new HashMap<>();
        params.put("p_TipoDocumento", "NOTCIR1985");
        params.put("p_Fecha",         fechaSistema);

        List<Esquema> resultado = sql.query(con, SQL_CONSULTAR_VERSION_ESQUEMA, params, Esquema.class);

        if (resultado == null || resultado.isEmpty()) {
            throw new Exception("No se encontro version de esquema para NOTCIR1985 - Fecha: " + fechaSistema);
        }
        long fin = System.currentTimeMillis();
        logger.write("FIN:: consultar version esquema total: " + (fin - inicio) + " ms");
        return resultado.get(0);
    }
    
    private RutaArchivo obtenerRutaBase(Connection con, SQL sql, Long idEmpresa) throws Exception {
    	HashMap<String, Object> params = new HashMap<>();
    	params.put("IdEmpresa", idEmpresa);
    	List<RutaArchivo> resultado = sql.query(con, SQL_CONSULTAR_RUTA_BASE, params, RutaArchivo.class);
    	
    	if(resultado == null || resultado.isEmpty() || resultado.get(0).RutaArchivo == null) {
    		throw new Exception ("No se ecnontro ruta base para CIR1985");
    	}
    	
    	logger.write("Ruta Base obtenida: " + resultado.get(0).RutaArchivo);
    	return resultado.get(0);
    }

    private ArchivoNormativa consultarArchivoNormativa(Connection con, Long idEmpresa, SQL sql, Esquema esquema) throws Exception {
    	logger.write("INICIO:: consultar archivo normativa");
    	long inicio = System.currentTimeMillis();
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdTipoDocumento", "NOTCIR1985");
        params.put("TipoArchivo",     "pdf");
        params.put("Version",         esquema.p_out_Version);
        params.put("IdEmpresa",       idEmpresa);

        List<ArchivoNormativa> resultado = sql.query(con, SQL_CONSULTAR_ARCHIVO_NORMATIVA, params, ArchivoNormativa.class);

        if (resultado == null || resultado.isEmpty()) {
            throw new Exception("No se encontro archivo normativa CIR1985");
        }
        long fin = System.currentTimeMillis();
        logger.write("FIN:: consultar archivo normativa total: " + (fin - inicio) + " ms");
        return resultado.get(0);
    }

    private String obtenerLogoCorredoraB64(Connection con, SQL sql, Long idEmpresa) throws Exception {
    	logger.write("INICIO:: consultar Logo corredora");
    	long inicio = System.currentTimeMillis();
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);

        Imagen logo = sql.query(con, SQL_CONSULTAR_LOGO, params, Imagen.class).stream().findFirst().orElse(new Imagen());

        if (logo.Contenido == null) {
        	logger.write("No se encontro logo para IdEmpresa: " + idEmpresa);
            return "";
        }
        long fin = System.currentTimeMillis();
        logger.write("FIN:: consultar logo corredora: "+ (fin - inicio) + " ms");
        return Base64.encodeBase64String(logo.Contenido);
    }
    
    private DatosCorredora consultarDatosCorredora(Connection con, SQL sql, Long idEmpresa) throws Exception {
    	logger.write("INICIO:: consultarDatosCorredora");
    	HashMap<String, Object> params = new HashMap<>();
    	params.put("IdEmpresa", idEmpresa);
    	List<DatosCorredora> resultado = sql.query(con, SQL_CONSULTAR_DATOS_CORREDORA, params, DatosCorredora.class);
    	if(resultado == null || resultado.isEmpty()) {
    		throw new Exception("No se encontraron datos de la corredora");
    	}
    	return resultado.get(0);
    }
    
    private String comprimirArchivos(String timestamp, String rutaBase, String nombreBase, List<String> rutasArchivos, Long idCliente) throws Exception {
        logger.write("Comprimiendo " + rutasArchivos.size() + " archivos");
        long inicio = System.currentTimeMillis();

        String nombreZip = idCliente != null && idCliente > 0 
                ? nombreBase + timestamp + "_" + idCliente + ".zip"
                : nombreBase + timestamp + ".zip";
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
    
    private List<DatosCIR1985CrystalCompleto> construirDatosConLogo(List<DatosCIR1985Crystal> datos, String logoB64, DatosCorredora corredora, String fechaSistemaLocal, String horaSistemaLocal) {
        List<DatosCIR1985CrystalCompleto> lista = new ArrayList<>();
        for (DatosCIR1985Crystal dato : datos) {
            DatosCIR1985CrystalCompleto completo = new DatosCIR1985CrystalCompleto();
            completo.IdEjecucion                    = dato.IdEjecucion;
            completo.IdDocumento                    = dato.IdDocumento;
            completo.NombreCompleto                 = dato.NombreCompleto;
            completo.DireccionFisica                = dato.DireccionFisica;
            completo.RutCliente                     = dato.RutCliente;
            completo.Telefono                       = dato.Telefono;
            completo.DireccionElectronica           = dato.DireccionElectronica;
            completo.FolioGeneracion                = dato.FolioGeneracion;
            completo.IdOrden                        = dato.IdOrden;
            completo.Folio                          = dato.Folio;
            completo.IdCliente                      = dato.IdCliente;
            completo.IdPortafolio                   = dato.IdPortafolio;
            completo.CodigoPortafolio               = dato.CodigoPortafolio;
            completo.IdClasificacionTipoInstrumento = dato.IdClasificacionTipoInstrumento;
            completo.IdTipoInstrumento              = dato.IdTipoInstrumento;
            completo.IdTipoOperacion                = dato.IdTipoOperacion;
            completo.IdInstrumento                  = dato.IdInstrumento;
            completo.Cantidad                       = dato.Cantidad;
            completo.Monto                          = dato.Monto;
            completo.MontoValorizado                = dato.MontoValorizado;
            completo.Precio                         = dato.Precio;
            completo.IdMoneda                       = dato.IdMoneda;
            completo.IdCondicionDeLiquidacion       = dato.IdCondicionDeLiquidacion;
            completo.FechaRecepcion                 = dato.FechaRecepcion;
            completo.FechaAceptacion                = dato.FechaAceptacion;
            completo.TipoEjecucion                  = dato.TipoEjecucion;
            completo.FechaEjecucion                 = dato.FechaEjecucion;
            completo.FechaHoraEjecucion             = dato.FechaHoraEjecucion;
            completo.IdBolsa                        = dato.IdBolsa;
            completo.EsOD                           = dato.EsOD;
            completo.Modalidad                      = dato.Modalidad;
            completo.PrecioEjecucion                = dato.PrecioEjecucion;
            completo.MontoTransado                  = dato.MontoTransado;
            completo.CantidadTransada               = dato.CantidadTransada;
            completo.ComisionTransada               = dato.ComisionTransada;
            completo.MejorPrecioOtraBolsa           = (dato.MejorPrecioOtraBolsaNum == null || dato.MejorPrecioOtraBolsaNum.compareTo(BigDecimal.ZERO) == 0) 
            											? "-/---" 
            	    									: dato.MejorPrecioOtraBolsaNum.toPlainString() + "/BEC";
            completo.CantidadMejorPrecioOtraBolsa   = dato.CantidadMejorPrecioOtraBolsa;
            completo.PrecioMinHoraBolsaMcdo         = dato.PrecioMinHoraBolsaMcdo;
            completo.PrecioMaxHoraBolsaMcdo         = dato.PrecioMaxHoraBolsaMcdo;
            completo.MejorPrecioMercado             = dato.MejorPrecioMercado;
            completo.PrecioMinimoMercado            = dato.PrecioMinimoMercado;
            completo.HoraPrecioMinimoMercado        = dato.HoraPrecioMinimoMercado;
            completo.IdBolsaPrecioMinimoMercado     = dato.IdBolsaPrecioMinimoMercado;
            completo.PrecioMaximoMercado            = dato.PrecioMaximoMercado;
            completo.HoraPrecioMaximoMercado        = dato.HoraPrecioMaximoMercado;
            completo.IdBolsaPrecioMaximoMercado     = dato.IdBolsaPrecioMaximoMercado;
            completo.PrecioMinHoraBolsaCorr         = dato.PrecioMinHoraBolsaCorr;
            completo.PrecioMaxHoraBolsaCorr         = dato.PrecioMaxHoraBolsaCorr;
            completo.MayorPrecioOperacionCorredor   = dato.MayorPrecioOperacionCorredor;
            completo.HoraPrecioMayorCorredor        = dato.HoraPrecioMayorCorredor;
            completo.MenorPrecioOperacionCorredor   = dato.MenorPrecioOperacionCorredor;
            completo.HoraPrecioMenorCorredor        = dato.HoraPrecioMenorCorredor;
            completo.IdEmpleadoDigitador            = dato.IdEmpleadoDigitador;
            completo.FolioComprobanteFacturacion    = dato.FolioComprobanteFacturacion;
            completo.FechaGeneracion                = dato.FechaGeneracion;
            completo.FolioExterno                   = dato.FolioExterno;
            completo.IdEstadoNotificacion           = dato.IdEstadoNotificacion;
            completo.IdEstadoCarta                  = dato.IdEstadoCarta;
            completo.FechaIngreso                   = dato.FechaIngreso;
            completo.FechaCreacion                  = dato.FechaCreacion;
            completo.IdUsuarioCreador               = dato.IdUsuarioCreador;
            completo.NombreAgente                   = dato.NombreAgente;
            completo.AccionOperacion                = dato.AccionOperacion;
            completo.TelefonoAgente                 = dato.TelefonoAgente;
            completo.FechaSistemaLOCAL 				= fechaSistemaLocal;
            completo.HoraSistemaLOCAL  				= horaSistemaLocal;
            completo.MensajeError      				= "";
            completo.Nemotecnico                    = dato.Nemotecnico;
            completo.PrecioMinHoraBolsaBEC          = dato.PrecioMinHoraBolsaBEC;
            completo.PrecioMaxHoraBolsaBEC          = dato.PrecioMaxHoraBolsaBEC;
            completo.RelacionCorredor 				= dato.RelacionCorredor != null ? dato.RelacionCorredor : "SREL";
            completo.FolioOrden                     = dato.FolioOrden;
            completo.InstrumentoNegociacion         = dato.InstrumentoNegociacion;
            completo.RazonCorredora     = corredora.RazonSocial  != null ? corredora.RazonSocial  : "";
            completo.DireccionCorredora = corredora.Direccion     != null ? corredora.Direccion     : "";
            completo.RutCorredora       = corredora.RUT           != null ? corredora.RUT           : "";
            completo.TelefonoCorredora  = corredora.Telefono      != null ? corredora.Telefono      : "";
            completo.LogoCorredora                  = logoB64;
            lista.add(completo);
        }
        return lista;
    }
    
    // CALL
    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    
    private void registrarLog(Connection con, GenerarCartasCIR1985_Entrada in, long exitosas, long fallidas) {
        try {
            String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(
                con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"
            );
            InAgregarLog agregarLogEntrada = new InAgregarLog();
            AgregarLog.InAgregarLog.Log logGenerarCartas = new AgregarLog.InAgregarLog.Log();
            logGenerarCartas.IdCabeceraLog     = in.p_IdEmpresa;
            logGenerarCartas.IdTipoLog         = "INFORMATIVO";
            logGenerarCartas.IdModulo          = in.p_IdModulo;
            logGenerarCartas.IdUsuario         = in.p_IdUsuario;
            logGenerarCartas.IdEmpresa         = in.p_IdEmpresa;
            logGenerarCartas.IdCliente         = in.p_IdCliente != null ? in.p_IdCliente : 0L;
            logGenerarCartas.IdUsuarioAtendido = in.p_IdUsuario;
            logGenerarCartas.IdTipoRegistro    = "CIR1985_GENERACION_CARTAS";
            logGenerarCartas.NombreProceso     = "GenerarCartasCIR1985";
            logGenerarCartas.FechaSistemaLog   = fechaSistema;
            agregarLogEntrada.p_Log.add(logGenerarCartas);
            agregarLogEntrada.agregarDetalleLogBasico(
                in.p_IdEmpresa, Long.valueOf(0L), "TipoOrigen", "", in.p_TipoOrigen, "PROCESO"
            );
            agregarLogEntrada.agregarDetalleLogBasico(
                in.p_IdEmpresa, Long.valueOf(0L), "CartasGeneradas", "", String.valueOf(exitosas), "RESULTADO"
            );
            agregarLogEntrada.agregarDetalleLogBasico(
                in.p_IdEmpresa, Long.valueOf(0L), "CartasFallidas",  "", String.valueOf(fallidas), "RESULTADO"
            );
            agregarLogEntrada.agregarDetalleLogBasico(
                in.p_IdEmpresa, Long.valueOf(0L), "FechaDesde", "", in.p_FechaDesde, "FILTRO"
            );
            agregarLogEntrada.agregarDetalleLogBasico(
                in.p_IdEmpresa, Long.valueOf(0L), "FechaHasta", "", in.p_FechaHasta, "FILTRO"
            );
            new AgregarLog().logica(con, agregarLogEntrada);
            logger.write("Log registrado correctamente");
        } catch (Exception e) {
            logger.write("Error registrando log: " + e.getMessage());
        }
    }
    
    private void transferirSFTP(Connection con, SQL sql, Long idEmpresa, String rutaBase, String rutaSFTP, SalidaGenerarCartasCIR1985 out, GenerarCartasCIR1985_Entrada in, List<String> rutasArchivos, long diasVigenciaSFTP) throws Exception {
        logger.write("INICIO:: transferir SFTP");
        long inicio = System.currentTimeMillis();

        // Si no hay ruta SFTP configurada, proceso inactivo
        if (rutaSFTP == null || rutaSFTP.isEmpty()) {
            logger.write("SFTP inactivo: NOR.RUTACIRCULAR1985 no configurada");
            out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "SFTP_INACTIVO", new HashMap<>(), con, in.p_IdEmpresa));
            return;
        }

        // Obtener credenciales desde FTP.Maestro
        List<ConfigSFTP> configList = sql.query(con, SQL_CONSULTAR_SFTP_MAESTRO, 
            new HashMap<>(), ConfigSFTP.class);
        if (configList == null || configList.isEmpty()) {
            logger.write("SFTP inactivo: no se encontro configuracion en FTP.Maestro");
            
            return;
        }
        ConfigSFTP config = configList.get(0);

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession(config.Usuario, config.Ip, config.Puerto.intValue());
            session.setPassword(config.Clave);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            logger.write("Conexion SFTP establecida: " + config.Ip);

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            // Subir todos los PDFs
            File directorio = new File(rutaBase);

            for (String rutaArchivo : rutasArchivos) {
                File archivo = new File(rutaArchivo);
                if (archivo.exists()) {
                    String destino = rutaSFTP + archivo.getName();
                    try (FileInputStream fis = new FileInputStream(archivo)) {
                        sftpChannel.put(fis, destino, ChannelSftp.OVERWRITE);
                        logger.write("Transferido: " + archivo.getName() + " -> " + destino);
                    }
                    FileUtils.forceDelete(archivo);
                }
            }

            logger.write("FIN:: transferir SFTP: " + (System.currentTimeMillis() - inicio) + "ms");

            limpiarArchivosVencidosSFTP(sftpChannel, rutaSFTP, diasVigenciaSFTP);

        } catch (Exception e){
        	logger.write("Error en transferencia SFTP" + e.getMessage());
        	throw e;
        }

        finally {
            if (sftpChannel != null) {
                sftpChannel.exit();
                sftpChannel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private void limpiarArchivosVencidosSFTP(ChannelSftp sftpChannel, String rutaSFTP, long diasVigencia) {
        if (diasVigencia <= 0) {
            logger.write("Limpieza SFTP inactiva: CIR1985_SFTP_DIAS_VIGENCIA no configurado");
            return;
        }

        logger.write("INICIO:: limpiar archivos vencidos SFTP - vigencia: " + diasVigencia + " dias");
        long inicio = System.currentTimeMillis();
        int eliminados = 0;

        try {
            Instant limite = Instant.now().minus(Duration.ofDays(diasVigencia));

            @SuppressWarnings("unchecked")
            Vector<LsEntry> listado = sftpChannel.ls(rutaSFTP);

            for (LsEntry entry : listado) {
                String nombreArchivo = entry.getFilename();
                SftpATTRS attrs = entry.getAttrs();

                if (".".equals(nombreArchivo) || "..".equals(nombreArchivo) || attrs.isDir() || !nombreArchivo.startsWith("CIRCULAR1985-")) {
                    continue;
                }

                Instant fechaModificacion = Instant.ofEpochSecond(attrs.getMTime());
                if (fechaModificacion.isBefore(limite)) {
                    try {
                        sftpChannel.rm(rutaSFTP + nombreArchivo);
                        eliminados++;
                        logger.write("Archivo vencido eliminado del SFTP: " + nombreArchivo + " (antiguedad: " + Duration.between(fechaModificacion, Instant.now()).toDays() + " dias)");
                    } catch (Exception e) {
                        logger.write("Error eliminando archivo vencido " + nombreArchivo + ": " + e.getMessage());
                    }
                }
            }

            logger.write("FIN:: limpiar archivos vencidos SFTP - Eliminados: " + eliminados + " - Total: " + (System.currentTimeMillis() - inicio) + " ms");
        } catch (Exception e) {
            logger.write("Error en limpieza de archivos vencidos SFTP: " + e.getMessage());
        }
    }

    // PRUEBAS LOCALES
    public static void main(String[] args) {
        System.out.println("== TEST GenerarCartasCIR1985 ==");
        try {
            Connection conn = PlataformaUtils.getPropertiesConnection("qa02", "connections.properties");
            GenerarCartasCIR1985 cartas1985 = new GenerarCartasCIR1985();
            GenerarCartasCIR1985_Entrada in = new GenerarCartasCIR1985_Entrada();
            in.p_IdEmpresa    = 1L;
            in.p_IdModulo     = "NOR"; 
            in.p_IdUsuario    = 1L;
            in.p_FechaDesde = "2026-06-24";
            in.p_FechaHasta = "2026-06-24";
            in.p_TipoOrigen = "PAUTA";
            //in.p_IdCliente = (long) 1861;
            in.p_DataSource = PlataformaUtils.getQADatasource();
            SalidaGenerarCartasCIR1985 resultado = cartas1985.logica(conn, in);
            System.out.println(resultado);
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== FIN ==");
    }
}