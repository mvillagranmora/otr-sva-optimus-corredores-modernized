package cl.bcs.normativa.circulares;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Chunk;
import com.lowagie.text.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.codec.binary.Base64;
import cl.bcs.normativa.CircularizacionGrabarCertificado.DatosCertificadoCustodia;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarCircularizacion implements JavaCall {
	private static final Logger logger = LogManager.getLogger(NotificarCircularizacion.class);
	private final String SQL_SELECT_CIRCULARIZACIONES = loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarCircularizacion_SelectCirculares.sql");
	private final String SQL_SELECT_PARAMS_CIRCULARIZACION = loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarCircularizacion_SolicitudParametros.sql");
	private final String SQL_SELECT_CLIENTES_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarCircularizacion_SelectClientesPortafolio.sql");
	private final String SQL_UPDATE_NOMINA = loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarCircularizacion_UpdateNomina.sql");
	private final String SQL_SELECT_LOGO_CORREDORA = "SELECT TOP 1 Imagen.\"Binario\" AS \"Contenido\" FROM \"BSCL.Plataforma.BD::PLA.Imagenes\" Imagen WHERE Imagen.\"NombreImagen\" = 'LOGO_CORREDOR' AND Imagen.\"IdEmpresa\" = :IdEmpresa ORDER BY Imagen.\"Version\" DESC;";  
	private final String SQL_CERTIFICADOS_CUSTODIA = "SELECT \"JsonData\" FROM \"BSCL.Normativa.BD::CER.CertificadoCliente\" WHERE \"Periodo\"= :Periodo AND \"IdCliente\" in (:IdClientes) and \"IdPortafolio\" in (:IdPortafolios)";
	private final String SQL_PARAMETROS_MERCADO = "SELECT (CASE \"Valor\" WHEN 'registroRV' THEN 'RV' WHEN 'registroRF' THEN 'RF' WHEN 'registroIIF' THEN 'IF' WHEN 'registroFFMM' THEN 'FM' ELSE NULL END) AS \"TipoInstrumento\" FROM \"BSCL.Normativa.BD::CIR.Parametro\" WHERE \"Periodo\"='' AND \"Codigo\"='MERCADO' AND \"IdEstado\"='ACT';";
	
    public static class NotificarCircularizacionEntrada extends BcsPojo {
    	public Long p_IdEmpresa;
    	public Long p_IdUsuario;
    	public Long p_IdSolicitudCircularizacion = 0L;
    	public List<Long> p_IdNominas = new ArrayList<>();
    	public String p_IdModulo = "NOR";
    	public String p_Periodo = "";
    }
    
    public static class SalidaNotificarCircularizacion extends BcsPojo {
		public List<Long> P_OUT_ID_EVENTOS = new ArrayList<>();
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public Long	P_OUT_ADVERTENCIASCONTADOR = 0L;
    }    
    
	public static class Nomina extends BcsPojo {		
		public Long IdNomina;
		public Long IdPersona;
		public Long IdPortafolio;
		public Long IdDireccionElectronica;
		public String Corredora;
		public String AtencionCliente;
		public String NombreCompleto;
		public String RutCorredora;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public Long IdDireccionElectronicaPrincipal;
		public String Agente;
		public String AccesoCliente = "N";
		public Long IdCliente;
		public String LogoCorredora;
		public String CodigoPortafolio;
	}
	
	public static class ParametrosCircularizacion extends BcsPojo {
		public String RutCliente;
		public String IdCliente;
		public String TRUT;
		public String Periodo;
		public String Usuario;
	}
	
	public static class ClientePortafolio extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
	}
	
	public static class Cert {
		public byte[] JsonData;
	}
	
	public static class Imagen extends BcsPojo {
		public byte[] Contenido;

		public Imagen() {
			Contenido = new byte[0];
		}
	}
	
	public static class Parametro extends BcsPojo {
		public String TipoInstrumento;
	}
		
	private static HashMap<String, String> generarTablas(List<DatosCertificadoCustodia> carteras, List<Parametro> parametros) {
	    HashMap<String, List<DatosCertificadoCustodia>> map = new HashMap<>();
	    for (DatosCertificadoCustodia cartera : carteras) {
	        String key = cartera.IdCliente + "_" + cartera.IdPortafolio;
	        map.putIfAbsent(key, new ArrayList<>());
	        map.get(key).add(cartera);
	    }

	    HashMap<String, String> tables = new HashMap<>();
	    for (HashMap.Entry<String, List<DatosCertificadoCustodia>> entry : map.entrySet()) {
	        String key = entry.getKey();
	        List<DatosCertificadoCustodia> carterasPorClientePortafolio = entry.getValue();

	        HashMap<String, List<DatosCertificadoCustodia>> carterasPorClasificacion = new LinkedHashMap<>();
	        for (DatosCertificadoCustodia cartera : carterasPorClientePortafolio) {
	            if (!"CUENTA CAJA".equalsIgnoreCase(cartera.Grupo) && !"".equalsIgnoreCase(cartera.Grupo) && parametros.stream().anyMatch(p -> p.TipoInstrumento.equalsIgnoreCase(cartera.IdClasificacionTipoInstrumento))) {
	                String clasificacionKey = cartera.Grupo;
	                carterasPorClasificacion.putIfAbsent(clasificacionKey, new ArrayList<>());
	                carterasPorClasificacion.get(clasificacionKey).add(cartera);
	            }
	        }

	        StringBuilder sb = new StringBuilder();
	        sb.append("<table width=\"100%\" class=\"table\">\n");
	        sb.append("<tr class=\"headerTable\">\n");
	        sb.append("<th>Emisor o Instrumento</th>\n");
	        sb.append("<th>Moneda</th>\n");
	        sb.append("<th>Cantidad Valor Nominal</th>\n");
	        sb.append("<th>Fecha Vencimiento</th>\n");
	        sb.append("<th>Precio Cierre</th>\n");
	        sb.append("<th>Valor Presente</th>\n");
	        sb.append("<th>Tasa</th>\n");
	        sb.append("</tr>\n");

	        if (carterasPorClasificacion.size() <= 0) {
	            sb.append("<tr ><td colspan=\"8\">NO HAY INFORMACIÓN</td></tr>\n");
	        }

	        int sectionCounter = 0;

	        for (HashMap.Entry<String, List<DatosCertificadoCustodia>> clasificacionEntry : carterasPorClasificacion.entrySet()) {
	            if (sectionCounter > 0) {
	                sb.append("<tr class=\"divider\"><td colspan=\"8\"></td></tr>\n");
	            }

	            String clasificacion = clasificacionEntry.getKey();
	            sb.append("<tr>\n");
	            sb.append("<th colspan=\"2\" class=\"mercadoSection\" >").append(clasificacion).append("</th>\n");
	            sb.append("</tr>\n");

	            for (DatosCertificadoCustodia cartera : clasificacionEntry.getValue()) {
	                sb.append("<tr class=\"contentTable\">\n");
	                sb.append("<td>").append(cartera.Nemotecnico).append("</td>\n");
	                sb.append("<td>").append(cartera.IdMoneda).append("</td>\n");
	                sb.append("<td>").append(cartera.Cantidad != null ? cartera.Cantidad.setScale(4, RoundingMode.HALF_UP) : new BigDecimal(0L).setScale(4, RoundingMode.HALF_UP)).append("</td>\n");
	                sb.append("<td>").append(cartera.FechaVencimiento).append("</td>\n");
	                sb.append("<td>").append(cartera.PrecioCierre != null ? cartera.PrecioCierre.setScale(4, RoundingMode.HALF_UP) : new BigDecimal(0L).setScale(4, RoundingMode.HALF_UP)).append("</td>\n");
	                sb.append("<td>").append(cartera.ValorPresente != null ? cartera.ValorPresente.setScale(4, RoundingMode.HALF_UP) : new BigDecimal(0L).setScale(4, RoundingMode.HALF_UP)).append("</td>\n");
	                sb.append("<td>").append(cartera.Tasa != null ? cartera.Tasa.setScale(2, RoundingMode.HALF_UP) : new BigDecimal(0L).setScale(4, RoundingMode.HALF_UP)).append("</td>\n");
	                sb.append("</tr>\n");
	            }
	            sectionCounter++;
	        }
	        sb.append("</table>\n");

	        tables.put(key, sb.toString());
	    }

	    return tables;
	}
	
	private byte[] createPDF(String logoBase64, Nomina nomina, SalidaNotificarCircularizacion out) {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    Document document = new Document(PageSize.LETTER);

	    try {
	        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
	        document.open();
	        document.add(createImage(logoBase64, 110f, Image.ALIGN_LEFT));

	        document.add(createParagraph("Señores, ", 10));
	        document.add(createParagraph(nomina.Corredora, 10));
	        document.add(createParagraph(nomina.RutCorredora, 10));
	        document.add(createParagraph("Telefono: " + nomina.AtencionCliente, 10));
	        addNewLines(document, 1);
	        document.add(createParagraph("En respuesta a lo solicitado por " + nomina.Agente +", con respecto a la información de los valores mantenidos en custodia de esta corredora, manifiesto mi:", 10));
	        addNewLines(document, 2);

	        PdfPTable table = createTable(2, 20, Element.ALIGN_LEFT, new int[]{1, 4});
	        PdfPCell[] cells = createCellsForTable(new String[]{"Acepto", "Rechazo"});
	        for (PdfPCell cell : cells) {
	            table.addCell(cell);
	        }
	        document.add(table);
	        addNewLines(document, 4);
	        document.add(createParagraph("Observaciones: (Completar en caso de rechazo)", 10));
	        addNewLines(document, 4);
	        document.add(createParagraph("Atentamente,", 10));
	        addNewLines(document, 4);
	        
	        table = new PdfPTable(3);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);
	        table.setSpacingAfter(10f);

	        table.addCell(getCell("", PdfPCell.NO_BORDER));
	        table.addCell(getCell("", PdfPCell.NO_BORDER));
	        PdfPCell cell = getCellWithParagraphs(nomina.NombreCompleto, "Rut: " + nomina.DocumentoIdentidad, PdfPCell.TOP);
	        table.addCell(cell);

	        document.add(table);

	        document.close();
	        writer.close();
	        return outputStream.toByteArray();  
	    } catch (Exception e) {
	        e.printStackTrace();
	        out.P_OUT_ERRORESCONTADOR++;
	    }

	    return null;
	}
    
    private PdfPCell getCellWithParagraphs(String text1, String text2, int border) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(border);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        cell.addElement(createParagraph(text1, 8));
        cell.addElement(createParagraph(text2, 8));
        
        PdfPTable ta = new PdfPTable(1); // Crear una tabla con una columna
        ta.setWidthPercentage(20); // Establecer el ancho de la tabla como porcentaje del ancho de la página

        return cell;
    }
    
    private PdfPCell getCell(String text, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBorder(border);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
    
    private Image createImage(String logoBase64, float width, int alignment) throws DocumentException, IOException {
        byte[] decodedString = java.util.Base64.getDecoder().decode(logoBase64);
        Image image = Image.getInstance(decodedString);
        float height = image.getHeight() * width / image.getWidth();
        image.scaleAbsolute(width, height);
        image.setAlignment(alignment);
        return image;
    }

    private Paragraph createParagraph(String text, float fontSize) {
        Font font = new Font(Font.HELVETICA, fontSize, Font.NORMAL);
        Paragraph paragraph = new Paragraph(text, font);
        return paragraph;
    }

    private void addNewLines(Document document, int lineCount) throws DocumentException {
        for (int i = 0; i < lineCount; i++) {
            document.add(new Paragraph("\n"));
        }
    }

    private PdfPTable createTable(int numColumns, int widthPercentage, int alignment, int[] columnWidths) throws DocumentException {
        PdfPTable table = new PdfPTable(numColumns);
        table.setWidthPercentage(widthPercentage);
        table.setHorizontalAlignment(alignment);
        table.setWidths(columnWidths);
        return table;
    }

    private PdfPCell[] createCellsForTable(String[] options) {
        PdfPCell[] cells = new PdfPCell[options.length * 3];
        for (int i = 0; i < options.length; i++) {
            cells[i * 3] = createCheckboxCell();
            cells[i * 3 + 1] = createTextCell(options[i]);
            cells[i * 3 + 2] = createSpacerCell(2);
        }
        return cells;
    }

    private PdfPCell createCheckboxCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setFixedHeight(20);
        cell.addElement(new Chunk(" ", FontFactory.getFont(FontFactory.HELVETICA, 1, Font.NORMAL, Color.BLACK)));
        return cell;
    }

    private PdfPCell createTextCell(String text) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPhrase(createParagraph(text, 10));
        return cell;
    }

    private PdfPCell createSpacerCell(int colspan) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setColspan(colspan);
        return cell;
    }
    
	private String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::NotificarCircularizacion.obtenerLogoCorredora");

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		Imagen logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(logo.Contenido);

		logger.debug("FIN::NotificarCircularizacion.obtenerLogoCorredora");

		return contenido;
	}

	public SalidaNotificarCircularizacion logica(Connection conn, NotificarCircularizacionEntrada in) throws Exception {
		logger.debug("INICIO::NotificarCircularizacion.logica");
		SalidaNotificarCircularizacion out = new SalidaNotificarCircularizacion();
		try {
			SQL _sql = new SQL();
			String logoCorredora = obtenerLogoCorredoraB64(conn, in.p_IdEmpresa);
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String URLOptimus = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.UrlOptimusClientes");
			String logoCorredorRutaPublica = "";
			ConsultarImagen consultarImagen = new ConsultarImagen();
			ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
			consultarImagenIn.p_IdEmpresa = in.p_IdEmpresa;
			consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR");
			ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(conn, consultarImagenIn);
			if (consultarImagenOut.OUT_IMAGENES.size() > 0 ) {
				logoCorredorRutaPublica = consultarImagenOut.OUT_IMAGENES.get(0).RutaPublica;
			}
			
			List<Long> idPortafolios = new ArrayList<>();
			List<Long> idClientes  = new ArrayList<>();
			String estadoNotificacion = "";
			String periodo = null;
			if (in.p_IdSolicitudCircularizacion > 0L && in.p_IdNominas.size() == 0L) {
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdSolicitudCircular", in.p_IdSolicitudCircularizacion);
				ParametrosCircularizacion parametrosCirc = _sql.query(conn, SQL_SELECT_PARAMS_CIRCULARIZACION, params, ParametrosCircularizacion.class).stream().findFirst().orElse(new ParametrosCircularizacion());
				periodo = parametrosCirc.Periodo;
				if (parametrosCirc.IdCliente != null) {
					params.clear();
					params.put("IdCliente", parametrosCirc.IdCliente);
					List<ClientePortafolio> clientesPortafolio = _sql.query(conn, SQL_SELECT_CLIENTES_PORTAFOLIO, params, ClientePortafolio.class);
					idPortafolios = clientesPortafolio.stream().map(c -> c.IdPortafolio).distinct().collect(Collectors.toList());
					idClientes = clientesPortafolio.stream().map(c -> c.IdCliente).collect(Collectors.toList());
				}
				estadoNotificacion = "PEN";
			} else {
				periodo = in.p_Periodo;
				estadoNotificacion = "ENV";
			}
			
			HashMap<String, Object> params = new HashMap<>();
	        params.put("Periodo", periodo);
	        params.put("IdClientes", idClientes);
	        params.put("IdPortafolios", idPortafolios);
	        params.put("ClientesAux", idClientes.size() > 0);
	        params.put("PortafoliosAux", idPortafolios.size() > 0);
	        params.put("IdNominas", in.p_IdNominas);
	        params.put("NominasAux", in.p_IdNominas.size() > 0);
	        params.put("IdEmpresa", in.p_IdEmpresa);
	        params.put("EstadoNotificacion", estadoNotificacion);
	        
            List<Nomina> nominas = _sql.query(conn, SQL_SELECT_CIRCULARIZACIONES, params, Nomina.class);
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(periodo + "01", formatter);
            String  fechaFormateada = LocalDate.parse(fechaSistema, inputFormat).format(formatter);
            
            Locale spanish = new Locale("es", "ES");
            String mes = date.getMonth().getDisplayName(TextStyle.FULL, spanish);
            
            HashMap<String, String> tablas = new HashMap<String, String>();
        	
            if (nominas.size() > 0) {
            	logger.info("INICIO::NotificarCircularizacion.logica::ProcesadoEventos");
            	AgregarEvento.ServiceEntrada eventoIn = new AgregarEvento.ServiceEntrada();
            	eventoIn.P_IDEMPRESA = in.p_IdEmpresa;
            	eventoIn.P_IDUSUARIO = in.p_IdUsuario;
            	eventoIn.P_IDMODULO  = in.p_IdModulo;
            	eventoIn.P_EVENTOS 			= new ArrayList<>();
            	eventoIn.P_VALORES 			= new ArrayList<>();
            	eventoIn.P_ARCHIVOSADJUNTOS	= new ArrayList<>();
            	eventoIn.P_IDDIRECCIONES 	= new ArrayList<>();
            	
            	HashMap<String, Object> paramsCert = new HashMap<>();
				idClientes = nominas.stream().filter(n -> !"S".equalsIgnoreCase(n.AccesoCliente)).map(n -> n.IdCliente).distinct().collect(Collectors.toList());
				idPortafolios = nominas.stream().filter(n -> !"S".equalsIgnoreCase(n.AccesoCliente)).map(n -> n.IdPortafolio).collect(Collectors.toList());
            	
            	paramsCert.put("Periodo", periodo);
            	paramsCert.put("IdClientes", idClientes);
            	paramsCert.put("IdPortafolios", idPortafolios);
            	
            	List<Cert> certList = _sql.query(conn, SQL_CERTIFICADOS_CUSTODIA, paramsCert, Cert.class);
            	List<Parametro> parametrosMercado = _sql.query(conn, SQL_PARAMETROS_MERCADO, new HashMap<>(), Parametro.class);
            	List<DatosCertificadoCustodia> listaCertificados = new ArrayList<>();
            	
            	for (Cert c : certList) {
            		Gson gson = new Gson();
            		String jsonData = new String(c.JsonData);	
            		DatosCertificadoCustodia[] certArray = gson.fromJson(jsonData, DatosCertificadoCustodia[].class);
            		listaCertificados.addAll(Arrays.asList(certArray));
            		tablas = generarTablas(listaCertificados, parametrosMercado);
            	}
            	
            	for (Nomina n : nominas) {
            		String key = n.IdCliente + "_" + n.IdPortafolio;
                    if (!"S".equalsIgnoreCase(n.AccesoCliente)) {
                    	logger.info("INICIO::Se genera obtiene tabla y se genera PDF");
                		String table =  tablas.get(key) != null ? tablas.get(key) : "";
                    	
                    	eventoIn.addValorBlob(n.IdNomina, "TABLA", table);
                    	
                    	AgregarEvento.ObjARCHIVOSADJUNTOS r = new AgregarEvento.ObjARCHIVOSADJUNTOS();
                    	r.ArchivoAdjunto = createPDF(logoCorredora, n, out);
                    	r.IdSecuencia = n.IdNomina;
                    	r.Nombre = "CIRCERTIFICADOCUSTODIA" + "_" + n.DocumentoIdentidad + "_" + n.CodigoPortafolio + "_" + fechaFormateada + ".pdf";
                    	r.Contrasenia = "";
                    	logger.info("{} {}" , r.ArchivoAdjunto.toString(), r.ArchivoAdjunto != null);
                    	eventoIn.P_ARCHIVOSADJUNTOS.add(r);
                    	logger.info("FIN::Se genera obtiene tabla y se genera PDF");
                    }
                    
            		AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
                    evt.CodigoTipoEvento 		= "S".equalsIgnoreCase(n.AccesoCliente) ? "CIRCULARIZACION_CLIENTE" : "CIRCULARIZACION_CLIENTE_SIN_OPTIMUS";
                    evt.IdSecuencia 			= n.IdNomina;
                    evt.FechaCreacionSistema 	= fechaSistema;            
                    evt.IdPersonaRemitente 		= 0L;
                    evt.IdPersonaDestinatario 	= n.IdPersona;            
                    evt.IdPortafolio 			= n.IdPortafolio;
                    eventoIn.P_EVENTOS.add(evt);
                    
                    AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
                    ndir.IdSecuencia 		= n.IdNomina;
                    ndir.TipoDestinatario 	= "TO";
                    ndir.IdDireccion 		= n.IdDireccionElectronica != null ? n.IdDireccionElectronica : n.IdDireccionElectronicaPrincipal;
                    
                    eventoIn.P_IDDIRECCIONES.add(ndir);
                    
                    eventoIn.addValor(n.IdNomina, "CLIENTE"				, n.NombreCompleto + "")
	        			.addValor(n.IdNomina, "AGENTE" 					, n.Agente + "")
	        			.addValor(n.IdNomina, "DOCUMENTOIDENTIDAD"		, n.DocumentoIdentidad + "")
	        			.addValor(n.IdNomina, "TIPODOCUMENTOIDENTIDAD"	, n.TipoDocumentoIdentidad + "") 
	        			.addValor(n.IdNomina, "CORREDORA"				, n.Corredora + "")  
			            .addValor(n.IdNomina, "ATENCIONCLIENTE"			, n.AtencionCliente + "")
			            .addValor(n.IdNomina, "FECHA"					, fechaSistema + "")
                    	.addValor(n.IdNomina, "MES"						,  mes.substring(0, 1).toUpperCase() + mes.substring(1).toLowerCase() + "")
                    	.addValor(n.IdNomina, "LOGOCORREDORA"			, logoCorredorRutaPublica + "")
                    	.addValor(n.IdNomina, "URLCLIENTES", URLOptimus);
            	}
            	
            	logger.info("FIN::NotificarCircularizacion.logica::ProcesadoEventos");
            	if (eventoIn.P_EVENTOS.size() > 0 && out.P_OUT_ERRORESCONTADOR.equals(0L)) {
            		AgregarEvento evento = new AgregarEvento();
            		AgregarEvento.ServiceSalida eventoOut = evento.logica(conn, eventoIn);
            		_sql.batch(conn, SQL_UPDATE_NOMINA, eventoOut.P_OUT_IDEVENTO, AgregarEvento.ObjIDEVENTO.class);
            		out.P_OUT_ID_EVENTOS = eventoOut.P_OUT_IDEVENTO.stream().map(e -> e.IdEvento).collect(Collectors.toList());
            		logger.info("{}  {}", eventoOut.P_OUT_IDEVENTO.size(), eventoOut.toString());
            	}	
            }
            return out;
		} catch (Exception e) {
			e.printStackTrace();
			out.P_OUT_ERRORESCONTADOR++;
		}
		logger.debug("FIN::NotificarCircularizacion.logica");
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		NotificarCircularizacionEntrada in = gson.fromJson(jin.toString(), NotificarCircularizacionEntrada.class);
		SalidaNotificarCircularizacion out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			NotificarCircularizacion demo = new NotificarCircularizacion();
			try {
				NotificarCircularizacionEntrada objIn = new NotificarCircularizacionEntrada();
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				
//				objIn.p_IdEmpresa = 1L;
//				objIn.p_IdUsuario = 0L; // 6693L
//				objIn.p_IdSolicitudCircularizacion = 6766L; // 6655L
//				objIn.p_Periodo = "202308";
////				objIn.p_DataSource = PlataformaUtils.getDESADatasource();
//				List<Long> nominas = new ArrayList<>();
//				nominas.add(8169L);
//				objIn.p_IdNominas = nominas;
				String inJSON = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"NOR\",\"p_Periodo\":\"202310\",\"p_IdSolicitudCircularizacion\":0,\"p_DataSource\":null,\"p_IdNominas\":[9065]}";
				objIn = gson.fromJson(inJSON, NotificarCircularizacionEntrada.class);		
				
				SalidaNotificarCircularizacion out = demo.logica(con, objIn);
				System.out.print(out.toString());
				con.commit();
			} catch(Exception e) {
				e.printStackTrace();
				con.rollback();
			} finally {
				con.close();
			}
		}
		System.out.println("== END ==");
	}

}
