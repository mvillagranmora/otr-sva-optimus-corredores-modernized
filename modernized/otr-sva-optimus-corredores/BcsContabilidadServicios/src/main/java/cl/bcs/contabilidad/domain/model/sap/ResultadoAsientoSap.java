package cl.bcs.contabilidad.domain.model.sap;

import java.util.List;

/** Output of GenerarJsonAsientoSap: partners to ensure in SAP and one voucher per requested header (CONT-B-20..29). */
public record ResultadoAsientoSap(List<BusinessPartner> socios, List<JournalVoucher> vouchers) {

    public ResultadoAsientoSap {
        socios = List.copyOf(socios);
        vouchers = List.copyOf(vouchers);
    }

    public static ResultadoAsientoSap vacio() {
        return new ResultadoAsientoSap(List.of(), List.of());
    }
}
