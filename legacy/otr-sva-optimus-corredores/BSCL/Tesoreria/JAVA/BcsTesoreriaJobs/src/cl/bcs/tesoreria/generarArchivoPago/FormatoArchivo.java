package cl.bcs.tesoreria.generarArchivoPago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;

public class FormatoArchivo {
	private Long   idEmpresa;
	private Long   idNominaTransferencia;
	private Long   idFormatoArchivo;	
	private String codigoFormatoArchivo;
	private Long   idBanco;
	private String nombreBanco;	
	private String idMoneda;
	private String idMedioDePago;
	private String formatoSalida;
	private String descripcion;
	private String servicio;
	private String rutaTesoreria; 				// Ej: tes/    --> se usa para descargar el archivo creado
	private String rutaCompleta;  				// Ej: /mnt/optimus/des/empresa1/tes/     --> se usa para crear el archivo
	private Integer cantidadDeTiposDeRegistro;  // cantidad de formatos o tipos de línea para un archivo
	private String  nombreArchivoSalida;
	private String  separadorCampos; 		// Ej: | (solo para archivos TXT)	
	private String  separadorDecimales; 	// Caracter para separar decimales (Ej. Punto: 999.99   Coma: 999,99    Vacio: 99999)
	private String  rellenarCampo; 			// Indica si se rellena el campo.  Para NUM 0 a la izquierda (00099)  Para CHAR espacios a la derecha ('XX   ')
	private String  tipoSaltoLinea; 		// Indica si el salto de linea es en formato Windows (CRLF) o Linux (LF).

	
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

	public String getCodigoFormatoArchivo() {
		return codigoFormatoArchivo;
	}
	public String getSeparadorCampos() {
		return separadorCampos;
	}
	public String getSeparadorDecimales() {
		return separadorDecimales;
	}
	public String getRellenarCampo() {
		return rellenarCampo;
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
	
	public String getRutaTesoreria() {
		return rutaTesoreria;
	}	
	
	public String getRutaCompleta() {
		return rutaCompleta;
	}
	
	
	public String getNombreArchivoSalida() {
		return nombreArchivoSalida;
	}	
	
	public Integer getCantidadDeTiposDeRegistro() {
		return cantidadDeTiposDeRegistro;
	}
	
	public String getTipoSaltoLinea() {
		return tipoSaltoLinea;
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
	
	public void setRutaTesoreria(String rutaTesoreria) {
		this.rutaTesoreria = rutaTesoreria;
	}	

	public void setRutaCompleta(String rutaCompleta) {
		this.rutaCompleta = rutaCompleta;
	}
	
	
	public void setNombreArchivoSalida(String nombreArchivoSalida) {
		this.nombreArchivoSalida = nombreArchivoSalida;
	}	
	
	public void setTipoSaltoLinea(String tipoSaltoLinea) {
		this.tipoSaltoLinea = tipoSaltoLinea;
	}
	/**
	 *  Método Constructor. Setea las variables de clase 
	 */
	public FormatoArchivo(final Connection conexion, Long idEmpresa,Long idNominaTransferencia,Long idBanco,String idMoneda, String idMedioDePago)
	{	
		PreparedStatement pstmt = null;
		int cantidadRegistros = 0;

		try 
		{
			String query = String.format("SELECT \"IdFormatoArchivo\",\"CodigoFormatoArchivo\", \"SeparadorCampos\", \"SeparadorDecimales\",\"RellenarCampo\",\"CodigoFormatoArchivo\",\"NombreBanco\",\"FormatoSalida\",\"Servicio\",\"Descripcion\",\"RutaTesoreria\",\"RutaCompleta\",\"NombreArchivoSalida\",\"CantidadDeTiposDeRegistro\",\"TipoSaltoLinea\" FROM BSCL.\"BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo\" WHERE \"IdEmpresa\" = %s AND \"IdBanco\" = %s AND \"IdMoneda\" = '%s' AND \"IdMedioDePago\" = '%s' AND \"IdEstado\" = 'ACT' ", idEmpresa, idBanco, idMoneda, idMedioDePago);
			
			pstmt = conexion.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();		
			
			while(rs.next()) 
			{	
				cantidadRegistros++;
				this.idFormatoArchivo = rs.getLong("IdFormatoArchivo");
				this.nombreBanco = rs.getString("NombreBanco");
				this.descripcion = rs.getString("Descripcion");
				this.formatoSalida = rs.getString("FormatoSalida");
				this.servicio = rs.getString("Servicio");
				this.rutaTesoreria = rs.getString("RutaTesoreria"); // Ej: tes/    --> se usa para descargar el archivo creado
				this.rutaCompleta = rs.getString("RutaCompleta");   // Ej: /mnt/optimus/des/empresa1/tes/     --> se usa para crear el archivo
				this.nombreArchivoSalida = rs.getString("NombreArchivoSalida");
				this.cantidadDeTiposDeRegistro = rs.getInt("CantidadDeTiposDeRegistro");
				this.codigoFormatoArchivo = rs.getString("CodigoFormatoArchivo");
				this.separadorCampos = rs.getString("SeparadorCampos"); // Ej: | (sólo para rchivos TXT)
				this.separadorDecimales = rs.getString("SeparadorDecimales"); // Caracter para separar decimales (Ej. Punto: 999.99   Coma: 999,99    Vacio: 99999)
				this.rellenarCampo= rs.getString("RellenarCampo"); 		// Indica si se rellena el campo.  Para NUM 0 a la izquierda (00099)  Para CHAR espacios a la derecha ('XX   ')
				this.tipoSaltoLinea= rs.getString("TipoSaltoLinea"); 		// Indica si el salto de linea es en formato Windows (CRLF) o Linux (LF).
			}

			if (cantidadRegistros == 0)
			{	
				throw new Exception("NO EXISTE FORMATO PARA EL ARCHIVO A CREAR");
			}
			
			//Setea la estructura del archivo en la variable de tipo Lista "formatoArchivoDetalle" 
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
			String query = String.format("SELECT \"IdFormatoArchivo\",\"CodigoFormatoArchivo\", \"IdFormatoArchivoDetalle\",\"TipoRegistro\", \"Campo\", \"TipoDato\", \"Largo\", \"Decimales\", \"ValorDefecto\", \"Orden\", \"DescripcionFormatoDetalle\",\"Formato\" FROM BSCL.\"BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle\" WHERE \"IdFormatoArchivo\" = %s ", idFormatoArchivo);
			
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
				itemFormatoArchivoDetalle.setDescripcion(rs.getString("DescripcionFormatoDetalle"));
				itemFormatoArchivoDetalle.setFormato(rs.getString("Formato"));
				itemFormatoArchivoDetalle.setCodigoFormatoArchivo(rs.getString("CodigoFormatoArchivo"));
				 
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
	 * @param tipoRegistro: tipo de registro(Ej.: 1: encabezado  2: detalle)
	 * @param nombreCampo: nombre del campo a buscar el formato
	 * @return: objeto con el formato encontrado
	 */
	private FormatoArchivoDetalle buscarFormatoArchivoDetalle(int tipoRegistro, String nombreCampo) 
	{	
		FormatoArchivoDetalle formatoArchivoDetalleLocal = null;
		
		try 
		{	
			if (!formatoArchivoDetalle.isEmpty())
			{
				for (FormatoArchivoDetalle fad : formatoArchivoDetalle)
				{
					if(fad.getTipoRegistro() == tipoRegistro && fad.getCampo().equals(nombreCampo))
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
	 * @param tipoRegistro: tipo de registro (Ej: 1: encabezado   2: detalle)
	 * @param nombreCampo: nombre del campo a formatear
	 * @param valor: valor del campo a formatear
	 * @return valor formateado
	 */

	public String formatear(int tipoRegistro,String nombreCampo, String valor)
	{	
		String valorFormateado = ""; 
		Integer largo = 0;
		String valorParteEntera = "0";
		String valorParteDecimal = "0";	
		
		try
		{		
			//busca la configuración del registro según nombre campo y tipo registro
			FormatoArchivoDetalle fad = buscarFormatoArchivoDetalle(tipoRegistro, nombreCampo);
			
			if (fad != null)  //Si existe formato, formatéa
			{
				largo  = fad.getLargo();
				valor  = valor.trim();
				
				
				//Si no viene valor, se asigna valor por defecto
				if (valor.length() < 1 && fad.getValorDefecto().compareTo("") != 0)				
					valor = fad.getValorDefecto();
				
				
				
				//CADENAS: Se rellena con espacios a la derecha(Ej.: "xxx  ")
				if (fad.getTipoDato().compareTo("CHAR") == 0)
				{

		            if("\\n".equals(valor)) {
						valor = "\n";
		            }
					
					if (valor.length() > largo) //si largo exede el permitido, se trunca
					{	
						valor = valor.trim().substring(0, largo);
					}	
					
					//valorFormateado = String.format("%1$-"+largo+"s", valor);
					if (this.rellenarCampo.equalsIgnoreCase("S"))
						valorFormateado = String.format("%1$-"+largo+"s", valor); //Se rellena con espacios a la derecha(Ej.: "xxx  ")
					else
						valorFormateado = valor; //No se rellena
				}

				
				//FECHAS
				if (fad.getTipoDato().compareTo("DATE") == 0)
				{	
					if (fad.getFormato() != null && !fad.getFormato().equalsIgnoreCase("") && valor != null && !valor.equalsIgnoreCase("") )
					{   //Si viene formato para fecha, se formatea
						SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat formateador = new SimpleDateFormat(fad.getFormato()); // aplica el formato 

						Date date = parseador.parse(valor);
						valorFormateado = String.format("%1$-"+largo+"s", formateador.format(date));
					}
					else	
					{	//Si no existe formato o valor es vacío	
						valorFormateado = String.format("%1$-"+largo+"s", valor);
					}
				}
				
				//NUMEROS: Se rellena con ceros a la izquierda(Ej.: "00005")
				if (fad.getTipoDato().compareTo("NUM") == 0)
				{	
					if (this.rellenarCampo.equalsIgnoreCase("S"))
						valorFormateado = String.format("%1$"+largo+"s", valor).replace(' ', '0');  // Se rellena con ceros a la izquierda(Ej.: "00005")
					else
						valorFormateado = valor.replace(' ', '0'); // No se rellena
				}
				
				
				//DECIMALES: Se rellena con ceros a la izquierda(Ej.: "0005.82")
				if (fad.getTipoDato().compareTo("DEC") == 0)
				{	
					Integer largoParteEntera = largo - fad.getDecimales();
					Integer largoParteDecimal = (fad.getDecimales() != null) ? fad.getDecimales() : 0;
					
					if (valor.indexOf(".") == -1)  // Si valor no contiene "."  Ej. "OE-80" 
					{
						valor = "0";
					}
					
					String[] partes = valor.split("\\."); // separar por .
					
					String parteEntera = (partes.length > 0) ? partes[0] : "0";
					String parteDecimal = (partes.length > 0) ? partes[1] : "0";
					
					
					if (parteDecimal.length() > largoParteDecimal) //si largo de parte Decimal exede el permitido, se trunca
					{	
						parteDecimal = parteDecimal.trim().substring(0, largoParteDecimal);
					}
					
					if (this.rellenarCampo.equalsIgnoreCase("S"))
					{
						valorParteEntera  = String.format("%1$"+largoParteEntera+"s", parteEntera).replace(' ', '0');    // Se rellena con ceros a la izquierda(Ej.: "0005.82")
						valorParteDecimal = String.format("%1$"+largoParteDecimal+"s", parteDecimal).replace(' ', '0');  // Se rellena con ceros a la izquierda(Ej.: "0005.82")
					}
					else
					{
						valorParteEntera  = parteEntera.replace(' ', '0');    //No se rellena
						valorParteDecimal = parteDecimal.replace(' ', '0');   //No se rellena
					}
					
					valorFormateado = valorParteEntera + this.getSeparadorDecimales() + valorParteDecimal;
					
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		return valorFormateado;
	}
	
	
	/**
	 * Actualiza datos de la transacción ligada al archivo creado. Nombre del archivo
	 * @param conexion: conexion a BD
	 * @param nombreArchivo: nombre del archivo generado
	 */
	public void actualizarTransferencia(final Connection conexion, Log log,final String nombreArchivo)
	{	
		PreparedStatement pstmt = null;
		
		try 
		{	
			final String queryUpdate = "UPDATE \"BSCL.Tesoreria.BD::TES.NominaTransferencia\" SET \"NombreArchivo\" = ? WHERE \"IdNominaTransferencia\" = ? AND \"IdEmpresa\" = ?";
		
			pstmt = conexion.prepareStatement(queryUpdate);
			pstmt.setString(1, nombreArchivo);
        	pstmt.setLong(2, this.getIdNominaTransferencia());
        	pstmt.setLong(3, this.getIdEmpresa());
        	
        	int respuesta = pstmt.executeUpdate();

        	log.write ("ACTUALIZACION DE TRANSFERENCIA : "+ ((respuesta==1)? "OK" : "NOK" ));
		}
		catch (Exception e)
		{
			System.out.println("ERROR AL ACTUALIZAR TRANSFERENCIA");
			e.printStackTrace();
		}
	}
	
	public String toString()
	{
		return Utils.printJson(this, false);
	}
		
}