package cl.bcs.utils.report;

public interface CrystalReportsPort {
    byte[] generateReport(CrystalReportRequest request);
}
