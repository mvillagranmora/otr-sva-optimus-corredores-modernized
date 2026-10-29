
package cl.bcs.contabilidad.pojo;

import cl.bcs.utils.Utils;

public class JournalVoucher {

    private JournalVoucher_ JournalVoucher;

    public JournalVoucher_ getJournalVoucher() {
        return JournalVoucher;
    }

    public void setJournalVoucher(JournalVoucher_ JournalVoucher) {
        this.JournalVoucher = JournalVoucher;
    }
    
    public String toString() {
		return Utils.printJson(this, false);
	}



}
