package cl.bcs.contabilidad.domain.model.sap;

import com.google.gson.annotations.SerializedName;

/**
 * Body of {@code POST /b1s/v1/JournalVouchersService_Add}: {@code {"JournalVoucher":{"JournalEntry":{...}}}}
 * (legacy pojo/JournalVoucher.java and JournalVoucher_.java).
 */
public record JournalVoucher(@SerializedName("JournalVoucher") Contenido contenido) {

    public record Contenido(@SerializedName("JournalEntry") JournalEntry journalEntry) {}

    public static JournalVoucher of(JournalEntry entry) {
        return new JournalVoucher(new Contenido(entry));
    }

    public JournalEntry journalEntry() {
        return contenido.journalEntry();
    }
}
