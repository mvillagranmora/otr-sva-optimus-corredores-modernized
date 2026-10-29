package cl.bcs.utils.report;

import java.util.Map;

public class CrystalReportRequest {

    public String templateId;
    public String format;
    public Map<String, Object> beanData;
    public String imageBase64;
    public String imageName;
    public String barcodeData;
    public String barcodeName;

    public CrystalReportRequest() {}

    public static CrystalReportRequest pdf(String templateId) {
        CrystalReportRequest r = new CrystalReportRequest();
        r.templateId = templateId;
        r.format = "pdf";
        return r;
    }

    public static CrystalReportRequest excel(String templateId) {
        CrystalReportRequest r = new CrystalReportRequest();
        r.templateId = templateId;
        r.format = "excel";
        return r;
    }

    public static CrystalReportRequest word(String templateId) {
        CrystalReportRequest r = new CrystalReportRequest();
        r.templateId = templateId;
        r.format = "word";
        return r;
    }
}
