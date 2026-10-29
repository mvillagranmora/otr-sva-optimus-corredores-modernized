package cl.bcs.reports.domain;

import java.util.Map;

public class GenerateReportRequest {
    public String templateId;       // UUID in HANA "BSCL.Report::Template"
    public String format;           // pdf | excel | word | rtf | txt
    public Map<String, Object> beanData;  // optional: fields for bean DataSource
    public String imageBase64;      // optional: base64 image for logo
    public String imageName;        // optional: image field name in template
    public String barcodeData;      // optional: PDF417 barcode data
    public String barcodeName;      // optional: barcode field name
}
