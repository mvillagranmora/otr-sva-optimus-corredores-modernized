package cl.bcs.contabilidad.domain.model.sap;

import java.util.List;

/** SAP B1 journal entry inside a JournalVoucher (legacy pojo/JournalEntry.java). Lines default to an empty list. */
public record JournalEntry(String ReferenceDate, String Memo, String DueDate, String TaxDate, Long JdtNum,
                           String U_BCS_IDPreComp, List<JournalEntryLine> JournalEntryLines, String U_BCS_TipoCen) {

    public JournalEntry {
        JournalEntryLines = JournalEntryLines == null ? List.of() : List.copyOf(JournalEntryLines);
    }
}
