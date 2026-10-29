/**
 * Adapted from legacy cl.bcs.plataforma.report.api.ReportDocument.
 * Changes:
 *   - Package changed to cl.bcs.reports.report
 *   - Removed import cl.bcs.utils.ListMapResultSet (now local stub in same package)
 *   - Removed log4j2 Configurator calls (replaced with SLF4J logger)
 *   - Apache POI pinned to 3.17 as in the legacy (Cell.CELL_TYPE_NUMERIC kept; no POI 5 API)
 */
package cl.bcs.reports.report;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.krysalis.barcode4j.BarcodeException;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import com.crystaldecisions.sdk.occa.report.application.PrintOutputController;
import com.crystaldecisions.sdk.occa.report.application.ReportClientDocument;
import com.crystaldecisions.sdk.occa.report.application.ReportObjectController;
import com.crystaldecisions.sdk.occa.report.definition.AreaSectionKind;
import com.crystaldecisions.sdk.occa.report.definition.ISection;
import com.crystaldecisions.sdk.occa.report.definition.PictureObject;
import com.crystaldecisions.sdk.occa.report.exportoptions.ExcelExportFormatOptions;
import com.crystaldecisions.sdk.occa.report.exportoptions.ExportOptions;
import com.crystaldecisions.sdk.occa.report.exportoptions.ReportExportFormat;
import com.crystaldecisions.sdk.occa.report.lib.IStrings;
import com.crystaldecisions.sdk.occa.report.lib.ReportSDKException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

/**
 * @author Raúl Catalán (original) — adapted for BcsCrystalReports quarantine service
 */
public class ReportDocument {

    private static final Logger log = LoggerFactory.getLogger(ReportDocument.class);

    private DataSource dataSource;
    private Sql2o sql2o;
    private ReportClientDocument reportDoc;
    private File documentFile = null;

    private String documentId = "";
    private String documentName = "";
    private String documentRSAlias = "";
    private String documentQuery = "";
    private ResultSet documentRS = null;

    private Map<String, SubreportDocument> subreportsMap = new HashMap<String, SubreportDocument>();

    private File documentTmpFile;
    private final int BUFFER_SIZE = 4096;
    private String TABLE = "\"BSCL.Report::Template\"";

    /* Constructors */

    /**
     * Crea un reporte vacío.
     */
    public ReportDocument() throws ReportSDKException {
        this.reportDoc = new ReportClientDocument();
    }

    /**
     * Crea un reporte vacío, pero con una conexión a base de datos.
     * @param schema esquema HANA
     * @param dataSource conexión a base de datos.
     */
    public ReportDocument(String schema, DataSource dataSource) throws ReportSDKException {
        setDataSource(dataSource);
        this.reportDoc = new ReportClientDocument();
        this.TABLE = schema != null ? schema + "." + this.TABLE : this.TABLE;
    }

    /* Public methods */

    public File getDocumentFile() {
        return this.documentFile;
    }

    public String getDocumentId() {
        return this.documentId;
    }

    public String getDocumentName() {
        return this.documentName;
    }

    public String getDocumentQuery() {
        return this.documentQuery;
    }

    public ResultSet getDocumentResultSet() {
        return this.documentRS;
    }

    public Map<String, SubreportDocument> getSubreports() {
        return this.subreportsMap;
    }

    public void setDocumentQuery(String query) {
        this.documentQuery = query;
    }

    public void setDocumentResultSet(ResultSet resultSet) {
        this.documentRS = resultSet;

        try {
            if (this.reportDoc.isOpen() && this.documentRS != null) {
                this.documentRSAlias = this.reportDoc.getDatabaseController().getDatabase().getTables().getTable(0).getAlias();
                this.reportDoc.getDatabaseController().setDataSource(this.documentRS, this.documentRSAlias, "resultsetTable");
            }
        } catch (ReportSDKException e) {
            e.printStackTrace();
        }
    }

    public void setDocumentResultSet(String query) {
        setDocumentResultSet(resulSetFromQuery(query));
    }

    public void setDocumentResultSet(Map<String, String> schema, JsonArray data) throws Exception {
        setDocumentResultSet(resultSetFromJson(schema, data));
    }

    public void setDocumentImage(String imageBase64, String imageName) {
        try {
            ReportObjectController objectController = this.reportDoc.getReportDefController().getReportObjectController();
            PictureObject picture = (PictureObject) this.reportDoc.getReportDefController().findObjectByName(imageName);
            if (picture == null) {
                throw new ReportSDKException(-1, "imageName: '" + imageName + "' not found");
            }
            ISection section = this.reportDoc.getReportDefController().findSectionByName(picture.getSectionName());

            String imageBase64Array = imageBase64.split(",").length > 1 ? imageBase64.split(",")[1] : imageBase64;
            byte[] imageByte = Base64.getDecoder().decode(imageBase64Array);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageByte);

            BufferedImage image = null;
            image = ImageIO.read(bais);
            bais.close();
            if (image != null) {
                int heightAdjusted = twipsToPixels(picture.getHeight());
                int widthAdjusted = (int) (heightAdjusted * image.getWidth() / image.getHeight());
                BufferedImage resizeImage = resizeImage(image, heightAdjusted, widthAdjusted);
                File imageFile = TempFiles.newFile("png");
                String fileName = imageFile.getPath();
                try {
                    ImageIO.write(resizeImage, "png", imageFile);

                    PictureObject newPicture = objectController.importPicture(fileName, section, picture.getLeft(), picture.getTop());
                    newPicture.setName(imageName);
                    newPicture.setSectionName(picture.getSectionName());
                    newPicture.setSectionCode(picture.getSectionCode());
                } finally {
                    TempFiles.deleteQuietly(imageFile);
                }
                objectController.remove(picture);
            }

        } catch (ReportSDKException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setDocumentBarcodeImage(String type, String data, String imageName) {
        File imageFile = TempFiles.newFile("png");
        String fileName = imageFile.getPath();

        BarcodeUtil util = BarcodeUtil.getInstance();
        BarcodeGenerator gen;

        DefaultConfiguration cfg = new DefaultConfiguration("barcode");
        DefaultConfiguration child = new DefaultConfiguration("PDF417");
        cfg.addChild(child);
        DefaultConfiguration attr = new DefaultConfiguration("human-readable");
        DefaultConfiguration subAttr = new DefaultConfiguration("placement");
        subAttr.setValue("bottom");
        attr.addChild(subAttr);
        child.addChild(attr);

        try {
            if (!type.equals("pdf417")) {
                throw new ReportSDKException(-1, "Wrong input parameter: type -> '" + type + "'");
            }
            ReportObjectController objectController = this.reportDoc.getReportDefController().getReportObjectController();
            PictureObject picture = (PictureObject) this.reportDoc.getReportDefController().findObjectByName(imageName);
            if (picture == null) {
                throw new ReportSDKException(-1, "imageName: '" + imageName + "' not found");
            }
            ISection section = this.reportDoc.getReportDefController().findSectionByName(picture.getSectionName());

            gen = util.createBarcodeGenerator((Configuration) cfg);

            int resolution = 200;
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(resolution, BufferedImage.TYPE_BYTE_BINARY, false, 0);

            gen.generateBarcode(canvas, data);

            canvas.finish();

            BufferedImage barcode = canvas.getBufferedImage();
            int width = barcode.getWidth();
            int height = barcode.getHeight();

            BufferedImage bitmap = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g2d = (Graphics2D) bitmap.getGraphics();
            g2d.setBackground(Color.white);
            g2d.setColor(Color.black);
            g2d.clearRect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            g2d.drawImage(barcode, 0, 0, width, height, null);

            int heightAdjusted = twipsToPixels(picture.getHeight());
            int widthAdjusted = (int) (heightAdjusted * bitmap.getWidth() / bitmap.getHeight());
            BufferedImage resizeBitmap = resizeImage(bitmap, heightAdjusted, widthAdjusted);
            log.debug("ImageIO.write resizeBitmap {}", resizeBitmap);
            log.debug("ImageIO.write imageFile {}", imageFile.getAbsolutePath());
            ImageIO.write(resizeBitmap, "png", imageFile);

            PictureObject newPicture = objectController.importPicture(fileName, section, picture.getLeft(), picture.getTop());
            newPicture.setName(imageName);
            newPicture.setSectionName(picture.getSectionName());
            newPicture.setSectionCode(picture.getSectionCode());
            TempFiles.deleteQuietly(imageFile);
            objectController.remove(picture);

        } catch (ReportSDKException | IOException | ConfigurationException | BarcodeException e) {
            e.printStackTrace();
        } finally {
            TempFiles.deleteQuietly(imageFile);
        }
    }

    public void setSubdocumentsResultSet(Map<String, ResultSet> subdocsResultSet) {
        Iterator<String> iterator = subdocsResultSet.keySet().iterator();
        int i = 1;
        while (iterator.hasNext()) {
            String subdocName = iterator.next();
            SubreportDocument subdoc = this.subreportsMap.get(subdocName);
            subdoc.setResultSet(subdocsResultSet.get(subdocName));
            try {
                this.reportDoc.getSubreportController().setDataSource(
                        subdoc.getName(), subdoc.getResultSet(), subdoc.getRSAlias(), "resultsetSubreport" + i + "Table");
                i++;
            } catch (ReportSDKException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSubdocumentsResultSetFromJson(Map<String, JsonResultSet> subdocsJsonResultSet) {
        Map<String, ResultSet> subdocsResultSet = new HashMap<String, ResultSet>();

        for (Entry<String, JsonResultSet> entry : subdocsJsonResultSet.entrySet()) {
            String key = entry.getKey();
            JsonResultSet value = entry.getValue();
            try {
                subdocsResultSet.put(key, resultSetFromJson(value.getSchema(), value.getData()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setSubdocumentsResultSet(subdocsResultSet);
    }

    public void setSubdocumentsResultSetFromQuery(Map<String, String> subdocsQuery) {
        Map<String, ResultSet> subdocsResultSet = new HashMap<String, ResultSet>();
        Iterator<String> iterator = subdocsQuery.keySet().iterator();
        while (iterator.hasNext()) {
            String subdocName = iterator.next();
            ResultSet subdocRS = resulSetFromQuery(subdocsQuery.get(subdocName));
            subdocsResultSet.put(subdocName, subdocRS);
        }
        setSubdocumentsResultSet(subdocsResultSet);
    }

    public void setDataSource(DataSource ds) {
        this.dataSource = ds;
        this.sql2o = new Sql2o(ds);
    }

    public void close() {
        try {
            this.reportDoc.close();
        } catch (ReportSDKException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TempFiles.deleteQuietly(this.documentTmpFile);
        }
    }

    private static void closeQuietly(java.io.Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {
                // nothing to do: the file is deleted by the caller's cleanup
            }
        }
    }

    public String uploadDocument(String reportName, InputStream reportStream, int docVersion, long companyId) {
        String uuid = "";

        OutputStream out = null;
        File tmpFile = TempFiles.newFile("rpt");
        String filePath = tmpFile.getPath();

        try {
            byte[] reportByte = IOUtils.toByteArray(reportStream);
            InputStream in = null;
            in = new ByteArrayInputStream(reportByte);

            // registered before open so close() removes it even if open() throws or the file is not a report
            this.documentTmpFile = tmpFile;
            out = new FileOutputStream(filePath);
            int bytesRead = -1;
            byte[] buffer = new byte[this.BUFFER_SIZE];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            out.close();

            this.reportDoc.open(filePath, 0);
            if (!this.reportDoc.isOpen())
                return "";

            log.debug("loadDocument this.documentTmpFile: {}", this.documentTmpFile.getAbsolutePath());
            reportStream = new ByteArrayInputStream(reportByte);
            uuid = insertDocument(reportName, reportStream, null, docVersion, companyId);
            reportStream.close();
            reportByte = null;

            this.documentName = reportName;
            this.documentFile = null;
            if (uuid.equals(""))
                return uuid;

            IStrings subreportNames = this.reportDoc.getSubreportController().getSubreportNames();
            String subreportName;
            Iterator<String> subreportNameIterador = subreportNames.iterator();
            while (subreportNameIterador.hasNext()) {
                subreportName = subreportNameIterador.next();
                String subreportId = insertDocument(subreportName, null, uuid, docVersion, companyId);
                if (subreportId.equals(""))
                    return "";
                String rsAliasSubreport = this.reportDoc.getSubreportController().getSubreportDatabase(subreportName)
                        .getTables().getTable(0).getAlias();
                this.subreportsMap.put(subreportName,
                        new SubreportDocument(subreportId, subreportName, rsAliasSubreport, "", null));
            }
        } catch (ReportSDKException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            closeQuietly(out);
        }

        return uuid;
    }

    public String uploadDocument(String docName, File docFile, int docVersion, long companyId) {
        String uuid = "";

        InputStream reportStream = null;

        try {
            reportStream = new FileInputStream(docFile);
            uuid = uploadDocument(docName, reportStream, docVersion, companyId);
            this.documentFile = docFile;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return uuid;
    }

    public String loadDocument_(String docName, File docFile) {
        this.documentName = docName;
        this.documentFile = docFile;
        String uuid = "";

        InputStream in = null;
        OutputStream out = null;
        File tmpFile = TempFiles.newFile("rpt");
        String filePath = tmpFile.getPath();

        try {
            this.documentTmpFile = tmpFile;
            in = new FileInputStream(docFile);
            out = new FileOutputStream(filePath);
            int bytesRead = -1;
            byte[] buffer = new byte[this.BUFFER_SIZE];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            out.close();

            this.reportDoc.open(filePath, 0);
            if (!this.reportDoc.isOpen())
                return "";

            log.debug("uploadDocument this.documentTmpFile: {}", this.documentTmpFile.getAbsolutePath());
            if (uuid.equals(""))
                return uuid;

            IStrings subreportNames = this.reportDoc.getSubreportController().getSubreportNames();
            String subreportName;
            Iterator<String> subreportNameIterador = subreportNames.iterator();
            while (subreportNameIterador.hasNext()) {
                subreportName = subreportNameIterador.next();
                String subreportId = "";
                if (subreportId.equals(""))
                    return "";
                String rsAliasSubreport = this.reportDoc.getSubreportController().getSubreportDatabase(subreportName)
                        .getTables().getTable(0).getAlias();
                this.subreportsMap.put(subreportName,
                        new SubreportDocument(subreportId, subreportName, rsAliasSubreport, "", null));
            }
        } catch (ReportSDKException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return uuid;
    }

    public InputStream getDocument(String reportId) {
        InputStream stream = null;

        String select = "SELECT \"ReportFile\" FROM " + this.TABLE + " where \"Id\" = :id ";

        try (org.sql2o.Connection conn = this.sql2o.open()) {
            stream = conn.createQuery(select, false)
                    .addParameter("id", reportId)
                    .executeScalar(InputStream.class);
        }

        return stream;
    }

    public boolean readDocument(String docId) {
        boolean read = false;
        InputStream in = null;
        OutputStream out = null;
        File tmpFile = TempFiles.newFile("rpt");
        String filePath = tmpFile.getPath();
        boolean registered = false;   // true once this.documentTmpFile owns the file (close() deletes it)

        PreparedStatement ps;
        try (Connection conn = this.dataSource.getConnection()) {
            String stmt = "SELECT * FROM " + this.TABLE + " where \"Id\" = ? ";
            ps = conn.prepareStatement(stmt);
            ps.setString(1, docId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getBlob("ReportFile") == null)
                return false;
            in = rs.getBlob("ReportFile").getBinaryStream();
            int reportVersion = rs.getInt("ReportVersion");
            long companyId = rs.getLong("CompanyId");
            out = new FileOutputStream(filePath);

            int bytesRead = -1;
            byte[] buffer = new byte[this.BUFFER_SIZE];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            in.close();
            out.close();

            if (this.reportDoc.isOpen()) {
                this.close();
            }
            this.documentTmpFile = tmpFile;
            registered = true;
            this.documentFile = new File(filePath);

            this.reportDoc = new ReportClientDocument();
            this.reportDoc.setLocale(Locale.ENGLISH);

            this.reportDoc.open(this.documentFile.getAbsolutePath(), 0);
            read = this.reportDoc.isOpen();

            log.debug("readDocument this.documentTmpFile: {}", this.documentTmpFile.getAbsolutePath());
            IStrings subreportNames = this.reportDoc.getSubreportController().getSubreportNames();
            String subreportName;
            Iterator<String> subreportNameIterador = subreportNames.iterator();
            while (subreportNameIterador.hasNext()) {
                subreportName = subreportNameIterador.next();
                String subreportId = insertDocument(subreportName, null, null, reportVersion, companyId);
                String rsAliasSubreport = this.reportDoc.getSubreportController().getSubreportDatabase(subreportName)
                        .getTables().getTable(0).getAlias();
                this.subreportsMap.put(subreportName,
                        new SubreportDocument(subreportId, subreportName, rsAliasSubreport, "", null));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ReportSDKException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(in);
            closeQuietly(out);
            if (!registered) {
                TempFiles.deleteQuietly(tmpFile);   // failed before ownership: nobody else would remove it
            }
        }

        return read;
    }

    public File exportDocumentToFile(String format) {
        File file = null;

        UUID fileName = UUID.randomUUID();

        try {
            switch (format.toLowerCase()) {
                case "pdf":
                    file = new File(fileName + ".pdf");
                    break;
                case "excel":
                    file = new File(fileName + ".xls");
                    break;
                case "word":
                    file = new File(fileName + ".doc");
                    break;
                case "rtf":
                    file = new File(fileName + ".rtf");
                    break;
                case "txt":
                    file = new File(fileName + ".txt");
                    break;
            }
            log.debug("exportDocumentToFile file: {}", file != null ? file.getAbsolutePath() : null);
            if (file != null) {
                ByteArrayInputStream bais = (ByteArrayInputStream) exportDocument(format);
                byte byteArray[] = new byte[bais.available()];
                FileOutputStream fos = new FileOutputStream(file);

                ByteArrayOutputStream baos = new ByteArrayOutputStream(bais.available());
                int x = bais.read(byteArray, 0, bais.available());

                baos.write(byteArray, 0, x);
                baos.writeTo(fos);

                int bytesRead = -1;
                byte[] buffer = new byte[this.BUFFER_SIZE];
                while ((bytesRead = bais.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }

                bais.close();
                baos.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public InputStream exportDocument(String format) {
        InputStream bais = null;

        try {
            if (this.reportDoc.isOpen()) {
                PrintOutputController po = null;

                switch (format.toLowerCase()) {
                    case "pdf":
                        po = this.reportDoc.getPrintOutputController();
                        bais = (ByteArrayInputStream) po.export(ReportExportFormat.PDF);
                        break;
                    case "excel":
                        po = this.reportDoc.getPrintOutputController();
                        ExportOptions exportOptions = new ExportOptions();
                        ExcelExportFormatOptions excelOptions = new ExcelExportFormatOptions();
                        excelOptions.setBaseAreaType(AreaSectionKind.detail);
                        ReportExportFormat exportFormat = ReportExportFormat.MSExcel;
                        exportOptions.setExportFormatType(exportFormat);
                        exportOptions.setFormatOptions(excelOptions);
                        bais = (ByteArrayInputStream) po.export(exportOptions);
                        break;
                    case "word":
                        po = this.reportDoc.getPrintOutputController();
                        bais = (ByteArrayInputStream) po.export(ReportExportFormat.MSWord);
                        break;
                    case "rtf":
                        po = this.reportDoc.getPrintOutputController();
                        bais = (ByteArrayInputStream) po.export(ReportExportFormat.RTF);
                        break;
                    case "txt":
                        // 1613487 - How to export Crystal Reports RPT files to TXT format using Java SDK
                        // ReportExportformat.text doesn't work using JRC SDK; use Excel then convert.
                        po = this.reportDoc.getPrintOutputController();
                        bais = (ByteArrayInputStream) po.export(ReportExportFormat.MSExcel);

                        StringBuffer data = new StringBuffer();

                        Locale l = Locale.getDefault();
                        l = new Locale("en", "US");
                        HSSFWorkbook wb = new HSSFWorkbook(new POIFSFileSystem(bais));
                        DataFormatter objDefaultFormat = new DataFormatter(l);
                        Pattern pFixDate = Pattern.compile("([\\d-]*)\"[\\s]*\"([\\d]*)\\\\(:[\\d]*)");
                        Pattern pFixNumber = Pattern.compile("^(?=.)(\\d{1,3}(,\\d{3})*)?(\\.\\d+)?$");

                        FormulaEvaluator objFormulaEvaluator = new HSSFFormulaEvaluator((HSSFWorkbook) wb);
                        HSSFSheet sheet = wb.getSheetAt(0);
                        Cell cell;
                        Row row;
                        Iterator<Row> rowIterator = sheet.iterator();
                        while (rowIterator.hasNext()) {
                            row = rowIterator.next();
                            Iterator<Cell> cellIterator = row.cellIterator();
                            while (cellIterator.hasNext()) {
                                cell = cellIterator.next();
                                String cellValueStr = objDefaultFormat.formatCellValue(cell, objFormulaEvaluator);
                                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                    Matcher m1 = pFixDate.matcher(cellValueStr);
                                    Matcher m2 = pFixNumber.matcher(cellValueStr);
                                    if (m1.find()) {
                                        cellValueStr = m1.replaceFirst("$1 $2$3");
                                    } else if (m2.find()) {
                                        cellValueStr = m2.replaceFirst("$1").replace(",", ".");
                                        cellValueStr += m2.replaceFirst("$3").replace(".", ",");
                                    }
                                }
                                data.append(cellValueStr + "\t");
                            }
                            data.append("\r\n");
                        }
                        bais = new ByteArrayInputStream(data.toString().getBytes());
                        break;
                }
            }
        } catch (ReportSDKException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bais;
    }

    public boolean isReadOnly() throws ReportSDKException {
        return this.reportDoc.isReadOnly();
    }

    public boolean updateDocument(String docId, String docName, InputStream docStream, String masterDocId, int docVersion, long idCompany) {
        this.documentName = docName;
        this.documentFile = null;
        byte[] reportByte = null;

        try {
            reportByte = IOUtils.toByteArray(docStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int n = reportByte == null ? 0 : reportByte.length;

        String update = " UPDATE " + this.TABLE + " SET ";
        if (n > 0) update += " \"ReportFile\" = :reportFile, ";
        update += " \"ReportName\" = :reportName, "
                + " \"MasterReportId\" = :masterReportId, "
                + " \"ReportVersion\" = :reportVersion, "
                + " \"CompanyId\" = :companyId "
                + " WHERE \"Id\" = :id ";

        try (org.sql2o.Connection conn = this.sql2o.open()) {
            Query q = conn.createQuery(update, false)
                    .addParameter("reportName", docName)
                    .addParameter("masterReportId", (String) null)
                    .addParameter("reportVersion", docVersion)
                    .addParameter("companyId", idCompany)
                    .addParameter("id", docId);
            if (n > 0) q.addParameter("reportFile", new ByteArrayInputStream(reportByte));

            q.executeUpdate().commit();

            return true;
        }
    }

    public boolean updateDocument(String docId, String docName, File docFile, String masterDocId, int docVersion, long idCompany) {
        InputStream docStream = null;
        if (docFile != null) {
            try {
                docStream = new FileInputStream(docFile);
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                return false;
            }
        }

        if (updateDocument(docId, docName, docStream, masterDocId, docVersion, idCompany)) {
            this.documentName = docName;
            this.documentFile = docFile;
            return true;
        }

        return false;
    }

    /* Private methods. */

    private String getId() {
        String uuid = "";
        if (this.dataSource == null)
            return uuid;
        Statement stmt;
        try (Connection conn = this.dataSource.getConnection()) {
            stmt = conn.createStatement();
            String suid = "SELECT CAST (SYSUUID AS VARCHAR) AS UUID FROM DUMMY ";
            ResultSet rs = stmt.executeQuery(suid);
            rs.next();
            uuid = rs.getString("UUID");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return uuid;
    }

    private ResultSet resulSetFromQuery(String query) {
        if (this.dataSource != null) {
            try (Connection conn = this.dataSource.getConnection()) {
                Statement stmt = conn.createStatement();
                ResultSet resultSet = stmt.executeQuery(query);
                return resultSet;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String insertDocument(String reportName, InputStream reportStream, String masterReportId, int reportVersion, long companyId) {
        String uuid = getId();
        if (uuid.equals(""))
            return uuid;

        this.documentName = reportName;

        String insert = "INSERT INTO " + this.TABLE
                + " ( \"Id\", \"ReportName\", \"MasterReportId\", \"ReportFile\", \"ReportVersion\", \"CompanyId\" ) "
                + " VALUES "
                + " ( :id, :reportName, :masterReportId, :reportFile, :reportVersion, :companyId ) ";

        try (org.sql2o.Connection conn = this.sql2o.open()) {
            conn.createQuery(insert, false)
                    .addParameter("id", uuid)
                    .addParameter("reportName", reportName)
                    .addParameter("masterReportId", masterReportId)
                    .addParameter("reportFile", reportStream)
                    .addParameter("reportVersion", reportVersion)
                    .addParameter("companyId", companyId)
                    .executeUpdate()
                    .commit();
            int n = conn.getResult();
            log.debug("INSERT: {}", n);
        }

        String select = "SELECT \"Id\" FROM " + this.TABLE + " where \"Id\" = :id ";
        String resultId = "";
        try (org.sql2o.Connection conn = this.sql2o.open()) {
            resultId = conn.createQuery(select, false)
                    .addParameter("id", uuid)
                    .executeScalar(String.class);
            log.debug("INSERT RESULT: {}", resultId);
        }

        return uuid;
    }

    private int twipsToPixels(int twips) {
        int ppi = 0;
        try {
            ppi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
        } catch (HeadlessException e) {
            ppi = 96;
        }
        return (int) (twips * ppi / 1440);
    }

    private BufferedImage resizeImage(BufferedImage image, int height, int width) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }

    @SuppressWarnings("unchecked")
    private ResultSet resultSetFromJson(Map<String, String> schema, JsonArray data) throws Exception {

        Map<String, Integer> schemaTypes = null;
        if (schema != null) {
            schemaTypes = new HashMap<String, Integer>();

            Iterator<String> iterator = schema.keySet().iterator();
            while (iterator.hasNext()) {
                String field = iterator.next();
                String type = schema.get(field);
                switch (type) {
                    case "number":
                        schemaTypes.put(field, Types.DECIMAL);
                        break;
                    case "date":
                        schemaTypes.put(field, Types.DATE);
                        break;
                    case "datetime":
                        schemaTypes.put(field, Types.TIMESTAMP);
                        break;
                    case "string":
                        schemaTypes.put(field, Types.VARCHAR);
                        break;
                    case "blob":
                        schemaTypes.put(field, Types.BLOB);
                        break;
                    default:
                        schemaTypes.put(field, Types.JAVA_OBJECT);
                        break;
                }
            }
        }

        ListMapResultSet rs = new ListMapResultSet();

        if (data != null) {
            Pattern pFixDate = Pattern.compile("(\\/Date\\()([0-9]*)(\\)\\/)");
            String val = null;
            Matcher m1 = null;
            List<Object> nList = new ArrayList<Object>();
            for (int i = 0; i < data.size(); i++) {
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(data.get(i).getAsJsonObject(), type);

                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (schemaTypes != null && schemaTypes.containsKey(entry.getKey())) {
                        switch (schemaTypes.get(entry.getKey())) {
                            case Types.DATE:
                                val = entry.getValue().toString();
                                m1 = pFixDate.matcher(val);
                                if (m1.find()) {
                                    long l = Long.valueOf(m1.group(2)).longValue();
                                    entry.setValue((new java.sql.Date(l)).toString());
                                }
                                break;
                            case Types.TIMESTAMP:
                                val = entry.getValue().toString();
                                m1 = pFixDate.matcher(val);
                                if (m1.find()) {
                                    long l = Long.valueOf(m1.group(2)).longValue();
                                    entry.setValue((new java.sql.Timestamp(l)).toString());
                                }
                                break;
                            case Types.BLOB:
                                val = entry.getValue().toString();
                                byte[] dataByte = Base64.getDecoder().decode(val);
                                entry.setValue(dataByte);
                                break;
                        }
                    }
                }

                nList.add(map);
            }

            rs.populate(nList);

            return (ResultSet) rs;
        }
        return null;
    }
}
