package cl.bcs.normativa.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import cl.bcs.normativa.dal.LocalizadorArchivo;
import cl.bcs.nuam.StoredProcedure;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.ListMapResultSet;
import cl.bcs.utils.Utils;

public final class BcsJobCertificadosClienteMasivo extends BcsJob {

    private static final Logger LOGGER = Logger
            .getLogger(BcsJobCertificadosClienteMasivo.class.getName());

    private String ActualizarSolicitud(
            final long p_IdDocumento,
            String p_NombreArchivo,
            String p_RutaArchivo,
            final long p_NumRegistros,
            final Connection conn,
            final long jobId) {

        String sqlCall = "";

        final String PROCEDURE_NAME_ACTUALIZAR = "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular";
        p_NombreArchivo = "'" + p_NombreArchivo + "'";
        p_RutaArchivo = "'" + p_RutaArchivo + "'";
        sqlCall = String.format("{CALL %s.%s(%s,%s,%s,%s,%s)}", getQuotedString("BSCL"),
                getQuotedString(PROCEDURE_NAME_ACTUALIZAR), p_IdDocumento, p_NombreArchivo,
                p_RutaArchivo, p_NumRegistros, jobId);
        System.out.println(sqlCall);

        try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
            conn.setAutoCommit(false);

            if (cstmt.execute()) {
                conn.commit();

                try (final ResultSet rs = cstmt.getResultSet()) {
                    if (rs.next()) {
                        LOGGER.log(Level.SEVERE, "Proceso de Actualizacion de Solicitud exitoso");
                        return "OK";
                    } else {
                        LOGGER.log(Level.SEVERE,
                                "Error durante proceso de Actualizacion de Solicitud, no RS");
                        return "NOK";

                    }

                } catch (final Exception ex) {

                    LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud",
                            ex);
                    return "NOK";
                }

            } else {

                LOGGER.log(Level.SEVERE,
                        "Error no se pudo ejecutar servicio actualizaciÃ¯Â¿Â½n");
                return "NOK";
            }

        } catch (final Exception ex) {

            LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud", ex);
            return "NOK";
        }

    }

    private String getQuotedString(
            final String name) {
        return "\"" + name + "\"";
    }

    private ResultSet getServiceData(
            final Map<String, Object> params)
                    throws Exception {

        String schema;
        if ((schema = (String) params.get("schema")) == null) {
            return null;
        }
        JsonObject dataJsonIn;
        if ((dataJsonIn = (JsonObject) params.get("dataJsonIn")) == null) {
            return null;
        }
        String serviceId;
        if ((serviceId = (String) params.get("serviceId")) == null) {
            return null;
        }
        JsonObject serviceData;
        if ((serviceData = (JsonObject) params.get("serviceData")) == null) {
            return null;
        }
        final String serviceResponse = (String) params.get("serviceResponse");
        final long companyId = (long) params.get("companyId");
        if (params.get("companyId") == null) {
            return null;
        }
        final long userId = (long) params.get("userId");
        if (params.get("userId") == null) {
            return null;
        }
        final Connection conn = (Connection) params.get("connection");

        ListMapResultSet rs;
        JsonArray results = null;
        final Map<String, Integer> schemaTypes = null;

        final JsonObject dataSRVIn = new JsonObject();
        dataSRVIn.addProperty("p_IdEmpresa", companyId);
        dataSRVIn.addProperty("p_IdUsuario", userId);
        dataSRVIn.addProperty("p_Hash", serviceId);
        final StoredProcedure kHashSrv = new StoredProcedure("BSCL.Plataforma.BD.Servicios::ACC.ValidaHashServicioUsuario");
        final JsonObject dataSRVOut = kHashSrv.call(conn, dataSRVIn);

        final String tipoCALL = dataSRVOut.get("p_out_Tipo".toUpperCase()).getAsString();
        final String spCALL = dataSRVOut.get("p_out_Servicio".toUpperCase()).getAsString();

        if (tipoCALL.equalsIgnoreCase("p")) {
            // call procedure
            final JsonObject dataJsonCallIn = dataJsonIn;
            if (dataJsonCallIn.get("IdEmpresa") != null) {
                dataJsonCallIn.addProperty("IdEmpresa", companyId);
            }
            if (dataJsonCallIn.get("p_IdEmpresa") != null) {
                dataJsonCallIn.addProperty("p_IdEmpresa", companyId);
            }
            if (dataJsonCallIn.get("IdUsuario") != null) {
                dataJsonCallIn.addProperty("IdUsuario", userId);
            }
            if (dataJsonCallIn.get("p_IdUsuario") != null) {
                dataJsonCallIn.addProperty("p_IdUsuario", userId);
            }

            // CallTime = System.currentTimeMillis();

            // procedure = new KProcedure("BSCL", "POC.ARQ.BD::TestCR");
            final StoredProcedure procedure = new StoredProcedure(spCALL);
            final JsonObject procedureResult = procedure.call(conn, serviceData);

            // CallTime = System.currentTimeMillis() - CallTime;

            results = procedureResult.get("ResultSet_0").isJsonArray()
                    ? procedureResult.get("ResultSet_0").getAsJsonArray() : null;

            if (serviceResponse.equals("json") && results.size() == 1) {
                String dataBase64 = "";
                for (final Entry<String, JsonElement> entry : results.get(0).getAsJsonObject()
                        .entrySet()) {
                    dataBase64 = entry.getValue().getAsString();
                    break;
                }
                // System.out.println( entry.getKey() + " : " +
                // entry.getValue());

                // dataBase64 =
                // results.get(0).getAsJsonObject().get("JsonData").getAsString();
                final byte[] dataByte = Base64.getDecoder().decode(dataBase64);
                final String jsonData = new String(dataByte);
                final JsonElement dataJsonElement = new JsonParser().parse(jsonData);
                results = dataJsonElement.getAsJsonArray();
            }

            conn.commit();
            // return;

        }

        rs = new ListMapResultSet();

        if (results != null) {
            final Pattern pFixDate = Pattern.compile("(\\/Date\\()([0-9]*)(\\)\\/)");
            String val = null;
            Matcher m1 = null;
            final List<Object> nList = new ArrayList<Object>();
            for (int j = 0; j < results.size(); j++) {
                final Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                final Map<String, Object> map = (Map<String, Object>) new Gson()
                        .fromJson(results.get(j).getAsJsonObject(), type);

                if (tipoCALL.equalsIgnoreCase("q")) {
                    for (final Map.Entry<String, Object> entry : map.entrySet()) {
                        if (schemaTypes != null) {
                            switch (schemaTypes.get(entry.getKey())) {
                            case Types.DATE:
                                val = entry.getValue().toString();
                                m1 = pFixDate.matcher(val);
                                if (m1.find()) {
                                    final long l = Long.valueOf(m1.group(2)).longValue();
                                    entry.setValue(new java.sql.Date(l).toString());
                                }
                                break;
                            case Types.TIMESTAMP:
                                val = entry.getValue().toString();
                                m1 = pFixDate.matcher(val);
                                if (m1.find()) {
                                    final long l = Long.valueOf(m1.group(2)).longValue();
                                    entry.setValue(new java.sql.Timestamp(l).toString());
                                }
                                break;
                            }

                        }
                    }
                }

                nList.add(map);
            }

            rs.populate(nList);
            return rs;
        }

        return null;
    }

    @Override
    public void onError(
            final Throwable t) {
        System.out.println(":GENERAR CERTIFICADOS MASIVOS:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    @Override
    public boolean transaction(
            final Connection conn)
                    throws InterruptedException,
                    Throwable {

        System.out.println(":GENERAR CERTIFICADOS MASIVOS:  ****** INICIO  *******");

        final DataSource datasource = super.getDataSource();

        final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
        final long secuenciaSolicitud = Long.parseLong(this.getParam("SecuenciaSolicitud"));
        // obtener la ruta de archivos para normativo
        final String ruta = LocalizadorArchivo.buscarRutaRaiz(conn, Utils.getSchema(), idEmpresa)
                + File.separator;

        // obtener los datos para la generacion de cada certificado
        final String selectCertificados = "select * from \"BSCL.Normativa.BD::NOR.SolicitudParametros\""
                + "INNER JOIN \"BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente\" ON \"Valor\" = \"IdCertificadoCliente\""
                + "where \"IdSolicitudCircular\" = ? AND \"IdEmpresa\"=? AND \"TipoParametro\" = 'IdCertificado' ";

        try (PreparedStatement pstmt = conn.prepareStatement(selectCertificados)) {
            pstmt.setLong(1, secuenciaSolicitud);
            pstmt.setLong(2, idEmpresa);

            try (ResultSet rs = pstmt.executeQuery()) {
                final List<String> archivos = new ArrayList<String>();
                long contadorArchivos = 0;
                while (rs.next()) {
                    contadorArchivos++;
                    final String reportId = rs.getString("IdArchivo");

                    final ReportDocument report = new ReportDocument(Utils.getSchema(), datasource);
                    if (!report.readDocument(reportId)) {
                        return false;
                    }

                    try {
                        // Construir la misma estructura que se le envia
                        // mediante HTTP al servicio de reportes
                        final JsonObject rootJson = new JsonObject();
                        rootJson.addProperty("reportId", reportId);
                        rootJson.add("parameters", new JsonArray());
                        rootJson.addProperty("exportFormat", rs.getString("TipoArchivo"));

                        final JsonObject mainReport = new JsonObject();
                        mainReport.addProperty("reportName", rs.getString("NombreArchivo"));
                        mainReport.add("parameters", new JsonArray());

                        final JsonObject configuration = new JsonObject();

                        final JsonObject data = new JsonObject();
                        data.addProperty("serviceId", rs.getString("ServicioArchivo"));
                        data.addProperty("serviceResponse", "json");

                        final JsonObject serviceData = new JsonObject();
                        serviceData.addProperty("P_IDEMPRESA", idEmpresa);
                        serviceData.addProperty("P_IDUSUARIO", 0);
                        serviceData.addProperty("P_IDMODULO", "NOR");
                        serviceData.addProperty("P_IDCERTIFICADOCLIENTE",
                                rs.getString("IdCertificadoCliente"));

                        data.add("serviceData", serviceData);
                        mainReport.add("data", data);
                        configuration.add("mainReport", mainReport);
                        configuration.add("subReports", new JsonArray());
                        rootJson.add("configuration", configuration);

                        System.out.println(rootJson.toString());
                        final Map<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("connection", conn);
                        parameters.put("schema", Utils.getSchema());
                        parameters.put("dataJsonIn", rootJson);

                        parameters.put("serviceId", rs.getString("ServicioArchivo"));
                        parameters.put("serviceData", data.get("serviceData"));
                        parameters.put("serviceResponse", "json");
                        parameters.put("companyId", idEmpresa);
                        parameters.put("userId", Long.valueOf("0"));

                        // Aqui se carga el resultado del servicio que en
                        // este caso es un json.
                        final ResultSet rsReporte = getServiceData(parameters);
                        report.setDocumentResultSet(rsReporte);

                        // exportar el reporte en formato pdf, se guarda
                        // fisicamente en <<ruta>>.
                        try (InputStream inStreamRpt = report
                                .exportDocument(rs.getString("TipoArchivo"))) {
                            final File fileTemp = new File(ruta + rs.getString("NombreArchivo")
                                    + contadorArchivos + ".pdf");
                            FileUtils.copyInputStreamToFile(inStreamRpt, fileTemp);
                            System.out.println("ARCHIVO: " + fileTemp.getAbsolutePath());
                            // se guardar las rutas de cada archivo para
                            // luego zipearlos
                            archivos.add(fileTemp.getAbsolutePath());
                        }

                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
                // Nombre y ruta ZIP
                final String zipName = "MASIVOCLIENTE_"
                        + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".zip";
                final String zip = ruta + zipName;
                System.out.println("ZIPFILE: " + zip);
                // Crear ZIP
                try (OutputStream os = new FileOutputStream(zip)) {
                    try (ZipOutputStream zos = new ZipOutputStream(os)) {
                        for (final String archivo : archivos) {
                            // obtener cada archivo a zipear
                            final String fileName = FilenameUtils.getName(archivo);
                            System.out.println("COMPRESSFILE: " + fileName);

                            try (InputStream fis = new FileInputStream(archivo)) {
                                zos.putNextEntry(new ZipEntry(fileName));
                                // escribie un archivo en el zip
                                int length;
                                final byte[] buffer = new byte[1];
                                while ((length = fis.read(buffer)) > 0) {
                                    zos.write(buffer, 0, length);
                                }
                                zos.closeEntry();
                                System.out.println("COMPRESSFILE: " + archivo + " DONE");
                            }
                            FileUtils.forceDeleteOnExit(new File(archivo));// Borrar
                                                                           // cada
                                                                           // arhivo
                                                                           // incluido
                                                                           // en
                                                                           // el
                                                                           // zip
                                                                           // al
                                                                           // terminar.
                        }
                    }
                }
                // actualiza la ruta del archivo y el estado de la solicitud
                ActualizarSolicitud(secuenciaSolicitud, zipName, ruta, contadorArchivos, conn,
                        getJobId());
            }
        }

        System.out.println(":GENERAR CERTIFICADOS MASIVOS:  ****** FIN  *******");
        return true;
    }

}
