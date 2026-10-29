package cl.bcs.normativa.interfaces.pojo;

import java.io.BufferedWriter;

import cl.bcs.utils.BcsPojo;

public class ParametrosGeneracionP40 extends BcsPojo {
	
	public String CodigoTenedor;
	public String TipoCartera;
	public String FechaOperacionyyyymmdd;
	public String JerarquiaValorRazonable;
	public BufferedWriter Archivo;
	public BufferedWriter ArchivoLog;

}
