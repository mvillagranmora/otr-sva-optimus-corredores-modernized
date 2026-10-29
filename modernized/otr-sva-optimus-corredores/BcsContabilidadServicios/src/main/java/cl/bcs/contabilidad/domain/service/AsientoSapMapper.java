package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.model.sap.AsientoEnvio;
import cl.bcs.contabilidad.domain.model.sap.BusinessPartner;
import cl.bcs.contabilidad.domain.model.sap.CuentaSap;
import cl.bcs.contabilidad.domain.model.sap.JournalEntry;
import cl.bcs.contabilidad.domain.model.sap.JournalEntryLine;
import cl.bcs.contabilidad.domain.model.sap.JournalVoucher;
import cl.bcs.contabilidad.domain.model.sap.PreComprobanteEnvio;
import cl.bcs.contabilidad.domain.model.sap.ResultadoAsientoSap;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Builds the SAP B1 JournalVouchers and BusinessPartners from the staged pre-vouchers.
 *
 * <p>Legacy {@code GenerarJsonAsientoSap.logica / CrearDetalle} (GenerarJsonAsientoSap.java:164-347), CONT-B-20..29:
 * <ul>
 *   <li>One voucher per requested id, in request order, duplicates repeated; an id without header is skipped.</li>
 *   <li>Lines are the staging rows whose ParentKey is the header JdtNum, in staging order.</li>
 *   <li>Each line on an analytic account (OACT LocManTran exactly "Y") adds one partner, even if the line is later
 *       skipped and even if the CardCode repeats. When OACT repeats an AcctCode the last row wins.</li>
 *   <li>A line whose four amounts are zero is skipped; Debit/Credit are sent only if one of them is positive, and the
 *       same for FCDebit/FCCredit.</li>
 *   <li>Agrupado lines carry only the base fields; Detallado ones also the references and U_BCS_* fields, and the
 *       costing codes when the account is ActType E/I.</li>
 * </ul>
 * Wire JSON is plain Gson (nulls omitted, BigDecimal written verbatim), as CallerWSJournalVoucher.java:168,:234.
 */
public final class AsientoSapMapper {

    private static final Gson WIRE = new Gson();
    private static final String LOCALIZACION_PERU = "PE";

    private AsientoSapMapper() {}

    /** SAP Service Layer JSON of a payload (voucher, partner, list or login body). */
    public static String toSapJson(Object payload) {
        return WIRE.toJson(payload);
    }

    public static ResultadoAsientoSap generar(List<Long> idsSolicitados, List<PreComprobanteEnvio> cabeceras,
                                              List<AsientoEnvio> asientos, List<CuentaSap> planCuentas,
                                              String localizacion) {
        Map<String, CuentaSap> plan = new HashMap<>();
        planCuentas.forEach(c -> plan.put(c.AcctCode(), c));
        boolean peru = LOCALIZACION_PERU.equalsIgnoreCase(localizacion);

        List<BusinessPartner> socios = new ArrayList<>();
        List<JournalVoucher> vouchers = new ArrayList<>();
        for (Long id : idsSolicitados) {
            Optional<PreComprobanteEnvio> cabecera = cabeceras.stream().filter(c -> Objects.equals(c.JdtNum(), id)).findFirst();
            if (cabecera.isEmpty()) continue;
            PreComprobanteEnvio p = cabecera.get();
            List<JournalEntryLine> lineas = new ArrayList<>();
            for (AsientoEnvio a : asientos) {
                if (!Objects.equals(a.ParentKey(), p.JdtNum())) continue;
                CuentaSap cuenta = plan.get(a.AccountCode());
                boolean analitica = cuenta != null && cuenta.esAnalitica();
                if (analitica) socios.add(socio(a, peru));
                linea(a, cuenta, analitica).ifPresent(lineas::add);
            }
            vouchers.add(JournalVoucher.of(new JournalEntry(p.ReferenceDate(), p.Memo(), p.DueDate(), p.TaxDate(),
                    p.JdtNum(), String.valueOf(p.U_BCS_IDPreComp()), lineas, String.valueOf(p.U_BCS_TipoCen()))));
        }
        return new ResultadoAsientoSap(socios, vouchers);
    }

    /** GenerarJsonAsientoSap.java:238-263: PE sends FederalTaxID "0" except for suppliers. */
    private static BusinessPartner socio(AsientoEnvio a, boolean peru) {
        String tipo = a.TipoSocioNegocio();
        if ("CONTRAPARTE".equalsIgnoreCase(tipo)) {
            return BusinessPartner.of(a.ShortName(), peru ? "0" : a.U_BCS_Rut(), a.U_BCS_Nombre(),
                    BusinessPartner.TIPO_CLIENTE, BusinessPartner.CONTRAPARTE);
        }
        if ("PROVEEDOR".equalsIgnoreCase(tipo)) {
            return BusinessPartner.of(a.ShortName(), a.U_BCS_Rut(), a.U_BCS_Nombre(),
                    BusinessPartner.TIPO_PROVEEDOR, BusinessPartner.PROVEEDOR);
        }
        return BusinessPartner.of(a.ShortName(), peru ? "0" : a.U_BCS_Rut(), a.U_BCS_Nombre(),
                BusinessPartner.TIPO_CLIENTE, BusinessPartner.CLIENTE);
    }

    private static Optional<JournalEntryLine> linea(AsientoEnvio a, CuentaSap cuenta, boolean analitica) {
        BigDecimal debit = a.Debit();
        BigDecimal credit = a.Credit();
        BigDecimal fcDebit = a.FCDebit();
        BigDecimal fcCredit = a.FCCredit();
        if (debit.signum() == 0 && credit.signum() == 0 && fcDebit.signum() == 0 && fcCredit.signum() == 0) {
            return Optional.empty();
        }
        boolean local = debit.signum() > 0 || credit.signum() > 0;
        boolean extranjera = fcCredit.signum() > 0 || fcDebit.signum() > 0;
        boolean det = a.esDetallado();
        boolean costo = det && cuenta != null && cuenta.usaCentroCosto();
        String memo = a.LineMemo().replace("<", "").replace(">", "");
        return Optional.of(new JournalEntryLine(
                a.AccountCode(), local ? debit : null, local ? credit : null, memo, a.BPLId(), a.DueDate(), a.Project(),
                det ? a.Reference1() : null, det ? a.Reference2() : null, det ? a.AdditionalReference() : null,
                extranjera ? fcDebit : null, extranjera ? fcCredit : null, det ? a.FCCurrency() : null,
                analitica ? a.ShortName() : null, det ? a.ReferenceDate1() : null, det ? a.TaxDate() : null,
                costo ? a.CostingCode() : null, costo ? a.CostingCode2() : null, costo ? a.CostingCode3() : null,
                costo ? a.CostingCode4() : null, costo ? a.CostingCode5() : null,
                det ? a.U_BCS_TipoPersona() : null, det ? a.U_BCS_Rut() : null, det ? a.U_BCS_Portafolio() : null,
                det ? a.U_BCS_Nombre() : null, det ? a.U_BCS_CtaDCV() : null, det ? a.U_BCS_EmitidoA() : null,
                det ? a.U_BCS_Comprobante() : null, det ? a.U_BCS_Folio() : null, det ? a.U_BCS_NDcto() : null,
                det ? a.U_BCS_Concepto() : null, det ? a.DueDate() : null, det ? a.U_BCS_CV() : null,
                det ? a.U_BCS_Nemo() : null, det ? a.U_BCS_IFRSCtaPropia() : null, det ? a.U_BCS_CodigoProducto() : null,
                det ? a.U_BCS_Bolsa() : null, det ? a.U_BCS_Relacion() : null, det ? a.U_glosa() : null,
                det ? a.U_BCS_FolioDTE() : null));
    }
}
