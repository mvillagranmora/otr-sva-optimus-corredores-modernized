package cl.bcs.normativa.cartolas.tools.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import cl.bcs.normativa.cartolas.master.config.GraficoMaster;
import cl.bcs.normativa.cartolas.master.config.ReporteMaster;
import cl.bcs.utils.FileUtils;
import cl.bcs.utils.PropertiesUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public final class GeneratorApp {

    private static final String PROCEDURES_FOLDER_NAME = "sp";

    private static void generateProcedure(
            final Template template,
            final File folder,
            final List<Field> fieldList,
            final String reporte)
                    throws FileNotFoundException {

        final List<List<NameValue>> dummyData = DataGenerator.generateDummyData(fieldList, 10);

        final VelocityContext context = new VelocityContext();
        context.put("package", "BSCL.Normativa.BD.Servicios::CAR");
        context.put("procedureName", reporte);
        context.put("tempName", "#temp_data");
        context.put("dummyData", dummyData);
        context.put("fieldList", fieldList);
        context.put("fieldListSize", fieldList.size());

        final StringWriter writer = new StringWriter();
        template.merge(context, writer);

        try (final PrintStream ps = new PrintStream(getSpFilePath(folder, reporte))) {
            ps.print(writer.toString());
        }
    }

    public static void generateProcedures(
            final VelocityEngine ve,
            final File path)
                    throws ParserConfigurationException,
                    JRException,
                    IOException {
        final Template template = ve.getTemplate("procedure.vm");
        final File folder = FileUtils.createFolder(path, PROCEDURES_FOLDER_NAME);
        final Set<String> nombres = new HashSet();

        for (final ReporteMaster reporte : ReporteMaster.values()) {
            final String nombreDataSource = reporte.getNombreDataSource();
            if (nombreDataSource != null && !nombres.contains(nombreDataSource)) {
                final JasperDesign jd = getJasperDesign(reporte);
                final List<Field> fieldList = getFieldList(jd);
                if (!fieldList.isEmpty()) {
                    generateProcedure(template, folder, fieldList, nombreDataSource);
                    nombres.add(nombreDataSource);
                }
            }
        }

        generateProceduresForGraphics(template, folder);
    }

    private static void generateProceduresForGraphics(
            final Template template,
            final File folder)
                    throws FileNotFoundException {
        final List<Field> fields0 = new ArrayList();
        fields0.add(new Field("grupo", "String"));
        fields0.add(new Field("totPorcActivo", "java.math.BigDecimal"));
        generateProcedure(template, folder, fields0, GraficoMaster.GraficoActivoPorProducto.name());

        final List<Field> fields1 = new ArrayList();
        fields1.add(new Field("Fecha", "java.sql.Timestamp"));
        fields1.add(new Field("Patrimonio_Final", "java.math.BigDecimal"));
        generateProcedure(template, folder, fields1, GraficoMaster.GraficoFM.name());
    }

    private static List<Field> getFieldList(
            final JasperDesign jd) {
        final List<Field> fieldList = new ArrayList<Field>();
        for (final JRField jrField : jd.getFields()) {
            fieldList.add(Field.newInstance(jrField));
        }

        return fieldList;
    }

    private static JasperDesign getJasperDesign(
            final ReporteMaster reporte)
                    throws JRException,
                    IOException {
        final String path = "/rpt/" + reporte.getNombreArchivo() + ".jrxml";
        try (final InputStream is = GeneratorApp.class.getResourceAsStream(path)) {
            return JRXmlLoader.load(is);
        }
    }

    private static String getSpFilePath(
            final File folder,
            final String reporte) {
        return folder.getAbsolutePath() + File.separator + reporte + ".sql";
    }

    public static void main(
            final String[] args)
                    throws ParserConfigurationException,
                    IOException,
                    JRException {
        final Properties props = PropertiesUtil.newProperties("cartolas.properties");
        final String outputPath = props.getProperty("outputPath");
        final File file = new File(outputPath);

        final VelocityEngine ve = new VelocityEngine();
        ve.init();

        generateProcedures(ve, file);
    }

    private GeneratorApp() {
    }

}
