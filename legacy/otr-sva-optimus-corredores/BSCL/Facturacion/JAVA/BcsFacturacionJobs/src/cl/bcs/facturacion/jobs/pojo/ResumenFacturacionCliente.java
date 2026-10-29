package cl.bcs.facturacion.jobs.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class ResumenFacturacionCliente {

	private long IdCliente;
	private long IdPortafolio;
	private String NombreLargoCliente;
	private String DocumentoIdentidad;
	private String CodigoPortafolio;
	private String AccionTipoOperacion;
	private String IdClasificacionTipoInstrumento;
	private String NombreTipoInstrumento;
	private String NemotecnicoInstrumento;
	private String Codigo;
	
	private String Nombre;
	private String TelefonoPrincipal;
	private String NombreCompleto;
	private String IdPersona;
	private String Direccion;
	private String Numero;
	
	private String Fecha;
	private String Hora;
	
	private String IdMoneda;
	private String IdMonedaComprobante;
	private String IdMonedaLiquidacion;
		
	private BigDecimal CantidadTotal;
	private BigDecimal PrecioPromedio;
	private BigDecimal Total;
	
	private long IdPersonaRemitente;
	private long IdPersonaDestinatario;
	
	private String Tabla;
	private String TablaCompra;
	private String TablaVenta;
	private String Row;
	
	
	public long getIdCliente() {
		return IdCliente;
	}

	public void setIdCliente(long idCliente) {
		IdCliente = idCliente;
	}

	public long getIdPortafolio() {
		return IdPortafolio;
	}

	public void setIdPortafolio(long idPortafolio) {
		IdPortafolio = idPortafolio;
	}

	public String getNombreLargoCliente() {
		return NombreLargoCliente;
	}

	public void setNombreLargoCliente(String nombreLargoCliente) {
		NombreLargoCliente = nombreLargoCliente;
	}

	public String getDocumentoIdentidad() {
		return DocumentoIdentidad;
	}

	public void setDocumentoIdentidad(String documentoIdentidad) {
		DocumentoIdentidad = documentoIdentidad;
	}

	public String getCodigoPortafolio() {
		return CodigoPortafolio;
	}

	public void setCodigoPortafolio(String codigoPortafolio) {
		CodigoPortafolio = codigoPortafolio;
	}

	public String getAccionTipoOperacion() {
		return AccionTipoOperacion;
	}

	public void setAccionTipoOperacion(String accionTipoOperacion) {
		AccionTipoOperacion = accionTipoOperacion;
	}

	public String getIdClasificacionTipoInstrumento() {
		return IdClasificacionTipoInstrumento;
	}

	public void setIdClasificacionTipoInstrumento(String idClasificacionTipoInstrumento) {
		IdClasificacionTipoInstrumento = idClasificacionTipoInstrumento;
	}

	public String getNombreTipoInstrumento() {
		return NombreTipoInstrumento;
	}

	public void setNombreTipoInstrumento(String nombreTipoInstrumento) {
		NombreTipoInstrumento = nombreTipoInstrumento;
	}

	public String getNemotecnicoInstrumento() {
		return NemotecnicoInstrumento;
	}

	public void setNemotecnicoInstrumento(String nemotecnicoInstrumento) {
		NemotecnicoInstrumento = nemotecnicoInstrumento;
	}

	public String getCodigo() {
		return Codigo;
	}

	public void setCodigo(String codigo) {
		Codigo = codigo;
	}

	public String getNombre() {
		return Nombre;
	}

	public void setNombre(String nombre) {
		Nombre = nombre;
	}

	public String getTelefonoPrincipal() {
		return TelefonoPrincipal;
	}

	public void setTelefonoPrincipal(String telefonoPrincipal) {
		TelefonoPrincipal = telefonoPrincipal;
	}

	public String getNombreCompleto() {
		return NombreCompleto;
	}

	public void setNombreCompleto(String nombreCompleto) {
		NombreCompleto = nombreCompleto;
	}

	public String getIdPersona() {
		return IdPersona;
	}

	public void setIdPersona(String idPersona) {
		IdPersona = idPersona;
	}

	public String getDireccion() {
		return Direccion;
	}

	public void setDireccion(String direccion) {
		Direccion = direccion;
	}

	public String getNumero() {
		return Numero;
	}

	public void setNumero(String numero) {
		Numero = numero;
	}
	
	

	public String getFecha() {
		return Fecha;
	}

	public void setFecha(String fecha) {
		Fecha = fecha;
	}

	public String getHora() {
		return Hora;
	}

	public void setHora(String hora) {
		Hora = hora;
	}
	
	public String getIdMoneda() {
		return IdMoneda;
	}

	public void setIdMoneda(String idMoneda) {
		IdMoneda = idMoneda;
	}

	public String getIdMonedaComprobante() {
		return IdMonedaComprobante;
	}

	public void setIdMonedaComprobante(String idMonedaComprobante) {
		IdMonedaComprobante = idMonedaComprobante;
	}

	public String getIdMonedaLiquidacion() {
		return IdMonedaLiquidacion;
	}

	public void setIdMonedaLiquidacion(String idMonedaLiquidacion) {
		IdMonedaLiquidacion = idMonedaLiquidacion;
	}

	public BigDecimal getCantidadTotal() {
		return CantidadTotal;
	}

	public void setCantidadTotal(BigDecimal cantidadTotal) {
		CantidadTotal = cantidadTotal;
	}

	public BigDecimal getPrecioPromedio() {
		return PrecioPromedio;
	}

	public void setPrecioPromedio(BigDecimal precioPromedio) {
		PrecioPromedio = precioPromedio;
	}

	public BigDecimal getTotal() {
		return Total;
	}

	public void setTotal(BigDecimal total) {
		Total = total;
	}

	public long getIdPersonaRemitente() {
		return IdPersonaRemitente;
	}

	public void setIdPersonaRemitente(long idPersonaRemitente) {
		IdPersonaRemitente = idPersonaRemitente;
	}

	public long getIdPersonaDestinatario() {
		return IdPersonaDestinatario;
	}

	public void setIdPersonaDestinatario(long idPersonaDestinatario) {
		IdPersonaDestinatario = idPersonaDestinatario;
	}

	public String getTabla() {
		return Tabla;
	}

	public void setTabla(String tabla) {
		Tabla = tabla;
	}

	public String getRow() {
		return Row;
	}

	public void setRow(String row) {
		Row = row;
	}
	
	public String getTablaCompra() {
		return TablaCompra;
	}

	public void setTablaCompra(String tablaCompra) {
		TablaCompra = tablaCompra;
	}

	public String getTablaVenta() {
		return TablaVenta;
	}

	public void setTablaVenta(String tablaVenta) {
		TablaVenta = tablaVenta;
	}

	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
	
}
