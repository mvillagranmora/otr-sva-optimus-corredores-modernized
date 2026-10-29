
package cl.bcs.contabilidad.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.Utils;

public class JournalEntry {

	public String ReferenceDate;
    public String Memo;
    public String DueDate;
    public String TaxDate;
    public Long JdtNum;
    public String U_BCS_IDPreComp;
    public List<JournalEntryLine> JournalEntryLines;
	public String U_BCS_TipoCen;
   
	public JournalEntry() {
		JournalEntryLines = new ArrayList<>();
	}
	
    public String toString() {
		return Utils.printJson(this, false);
	}

}
