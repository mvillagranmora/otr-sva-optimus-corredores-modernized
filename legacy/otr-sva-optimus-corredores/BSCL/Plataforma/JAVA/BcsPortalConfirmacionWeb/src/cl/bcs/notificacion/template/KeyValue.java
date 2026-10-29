package cl.bcs.notificacion.template;

public class KeyValue {
	
 	    public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getAgrupa() {
		return agrupa;
	}
	public void setAgrupa(String agrupa) {
		this.agrupa = agrupa;
	}
		public String key;
		public String value;
		public String codigo;
		public String agrupa;
		public int idEvento;
	    
	    public KeyValue(String CodigoTipoEvento, String key, String value, int idEvento){
	        this.key = key;
	        this.value = value;
	        this.codigo = CodigoTipoEvento;
	        this.idEvento = idEvento;
	    }
	    
 

}
