package cl.bcs.utils;

public interface  JavaCallMetadata {
	
	public String[] metadata = new String[4];
	/**
	 * Interface used to manage plugins
	 * @param conn
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public void setMetadata(Object in,Object out);
	public default String[] getMetadata() {
		return metadata;
	};
	
}
