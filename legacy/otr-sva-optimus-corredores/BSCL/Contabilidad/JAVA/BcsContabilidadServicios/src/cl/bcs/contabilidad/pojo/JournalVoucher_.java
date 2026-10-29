
package cl.bcs.contabilidad.pojo;

import java.util.HashMap;
import java.util.Map;

import cl.bcs.utils.Utils;

public class JournalVoucher_ {

    private JournalEntry JournalEntry;

    public JournalEntry getJournalEntry() {
        return JournalEntry;
    }

    public void setJournalEntry(JournalEntry JournalEntry) {
        this.JournalEntry = JournalEntry;
    }
    
    public String toString() {
		return Utils.printJson(this, false);
	}


}
