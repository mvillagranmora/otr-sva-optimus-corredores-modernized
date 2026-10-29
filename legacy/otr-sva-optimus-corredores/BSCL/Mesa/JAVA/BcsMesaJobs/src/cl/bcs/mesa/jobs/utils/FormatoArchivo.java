package cl.bcs.mesa.jobs.utils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.scheduler.BcsJob;


public class FormatoArchivo {

	private Long   idEmpresa;
	private Long   idNominaTransferencia;
	private Long   idFormatoArchivo;	
	private Long   idBanco;
	private String nombreBanco;	
	private String idMoneda;
	private String idMedioDePago;	
	private String formatoSalida;
	private String descripcion;
	private String servicio;
	private String rutaArchivo;
	private List<FormatoArchivoDetalle> formatoArchivoDetalle = new ArrayList<FormatoArchivoDetalle>();

	public Long getIdEmpresa() {
		return idEmpresa;
	}
	
	
	public Long getIdNominaTransferencia() {
		return idNominaTransferencia;
	}	
	
	public Long getIdFormatoArchivo() {
		return idFormatoArchivo;
	}
	
	public Long getIdBanco() {
		return idBanco;
	}

	public String getNombreBanco() {
		return nombreBanco;
	}
	
	public String getIdMoneda() {
		return idMoneda;
	}


	public String getIdMedioDePago() {
		return idMedioDePago;
	}


	public String getFormatoSalida() {
		return formatoSalida;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public String getServicio() {
		return servicio;
	}
	
	public String getRutaArchivo() {
		return rutaArchivo;
	}	
	
	
	public void setIdEmpresa(Long idEmpresa) {
		this.idEmpresa = idEmpresa;
	}


	public void setIdNominaTransferencia(Long idNominaTransferencia) {
		this.idNominaTransferencia = idNominaTransferencia;
	}


	public void setIdBanco(Long idBanco) {
		this.idBanco = idBanco;
	}


	public void setIdMoneda(String idMoneda) {
		this.idMoneda = idMoneda;
	}


	public void setIdMedioDePago(String idMedioDePago) {
		this.idMedioDePago = idMedioDePago;
	}	

	/**
	 *  Método Constructor. Setea las variables con los parámeteros de entrada al JOB
	 *  y el resto los obtiene desde BD. 
	 */
	public FormatoArchivo(final BcsJob bcsJob, final Connection conexion)
	{	
		PreparedStatement pstmt = null;
		int cantidadRegistros = 0;

		try 
		{	
			cargarFormatoArchivoDetalle(conexion);
			
		}
		catch(Exception e)
		{	
			e.printStackTrace();
		}
				
	}
	
	

	
	/**
	 * Carga el formato archivo detalle en la variable tipo Lista "formatoArchivoDetalle"
	 * @param conexion: conexion a BD
	 */
	public void cargarFormatoArchivoDetalle(final Connection conexion)
	{	
		PreparedStatement pstmt = null;
		int cantidadRegistros = 0;
		
		try 
		{   
			String query = String.format("SELECT \"IdFormatoArchivo\", \"IdFormatoArchivoDetalle\",\"TipoRegistro\", \"Campo\", \"TipoDato\", \"Largo\", \"Decimales\", \"ValorDefecto\", \"Orden\", \"Descripcion\" FROM \"BSCL.Mesa.BD::MES.FormatoArchivoDetalle\" WHERE \"IdFormatoArchivo\" = 'ANEXOXIX'");
			
			pstmt = conexion.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();		
			
			
			while(rs.next()) 
			{	
				cantidadRegistros++;
				FormatoArchivoDetalle itemFormatoArchivoDetalle = new FormatoArchivoDetalle();
				
				itemFormatoArchivoDetalle.setIdFormatoArchivoDetalle(rs.getLong("IdFormatoArchivoDetalle"));
				itemFormatoArchivoDetalle.setTipoRegistro(rs.getInt("TipoRegistro"));
				itemFormatoArchivoDetalle.setCampo(rs.getString("Campo"));
				itemFormatoArchivoDetalle.setTipoDato(rs.getString("TipoDato"));
				itemFormatoArchivoDetalle.setLargo(rs.getInt("Largo"));					 
				itemFormatoArchivoDetalle.setDecimales(rs.getInt("Decimales"));
				itemFormatoArchivoDetalle.setValorDefecto(rs.getString("ValorDefecto"));
				itemFormatoArchivoDetalle.setOrden(rs.getInt("Orden"));
				itemFormatoArchivoDetalle.setDescripcion(rs.getString("Descripcion"));
				 
				formatoArchivoDetalle.add(itemFormatoArchivoDetalle);
			}
			
			if (cantidadRegistros == 0)
			{	
				throw new Exception("NO EXISTE FORMATO DETALLE PARA EL ARCHIVO A CREAR");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}			
	}	

	
		
	/**
	 * Busca el formato de un campo en la lista de formatos 
	 * @param nombreCampo: nombre del campo a buscar el formato
	 * @return: objeto con el formato encontrado
	 */
	private FormatoArchivoDetalle buscarFormatoArchivoDetalle(final String nombreCampo) 
	{	
		FormatoArchivoDetalle formatoArchivoDetalleLocal = null;
		
		try 
		{	
			if (!formatoArchivoDetalle.isEmpty())
			{
				for (FormatoArchivoDetalle fad : formatoArchivoDetalle)
				{
					if(fad.getCampo().equals(nombreCampo))
					{
						formatoArchivoDetalleLocal =  fad;
						break;
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return (formatoArchivoDetalleLocal);
	}
	

	/**
	 * Da formato a un valor de acuerdo al formato configurado. 
	 * Si no existe formato se devuelve vacio, ya que esto indica que no debe informarse.
	 * @param nombreCampo: nombre del campo a formatear
	 * @param valor: valor del campo a formatear
	 * @return valor formateado
	 */
	public String formatear(final String nombreCampo, String valor)
	{	
		String valorFormateado = ""; 
		Integer largo = 0;
		
		try
		{			
			FormatoArchivoDetalle fad = buscarFormatoArchivoDetalle(nombreCampo);
			
			if (fad != null)  //Si existe formato, formatéa
			{
				largo  = fad.getLargo();
				valor  = valor.trim();
				
				if (valor.length() > largo) //si largo exede el permitido, se ajusta					
					valor = valor.trim().substring(0, largo);  
				
				//Si no viene valor, se asigna valor por defecto
				if (valor.length() < 1 && fad.getValorDefecto().compareTo("") != 0)				
					valor = fad.getValorDefecto();
				
				//Cadenas: Se rellena con espacios a la derecha(Ej.: "xxx  ")
				if (fad.getTipoDato().compareTo("CHAR") == 0)
				{					
					valorFormateado = String.format("%1$-"+largo+"s", valor);
				}
				
				//Numeros: Se rellena con ceros a la izquierda(Ej.: "00005")
				if (fad.getTipoDato().compareTo("NUM") == 0)
				{	
					valorFormateado = String.format("%1$"+largo+"s", valor);
				}
				
				//Decimales: Se rellena con ceros a la izquierda(Ej.: "0005.82")
				if (fad.getTipoDato().compareTo("DEC") == 0)
				{
					//fad.getDecimales(); //usar para formatear parte decimal y entera por separado
					valorFormateado = String.format("%1$"+largo+"s", valor).replace(' ', ' ');
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
		
		
		return valorFormateado;
	}
	

}