package cl.bcs.ordenes.pojo;

import cl.bcs.utils.Utils;

public class TransaccionBECMarketData {
    public int Accion;
    public boolean Anticipable;
    public String BCSCodeComprador;
    public String BCSCodeCompradorOpdir;
    public String BCSCodeVendedor;
    public String BCSCodeVendedorOpdir;
    public String Board;
    public double Cantidad;
    public String ClientIDCompra;
    public String ClientIDVenta;
    public String CompradorBCSTraderID;
    public String CompradorIdCorredor;
    public String CompradorIdOpdir;
    public String CompradorTraderID;
    public String CompradorTraderIdOpdir;
    public String CompradorXStreamIdCorredor;
    public String CompradorXStreamIdOpdir;
    public String EmisorID;
    public String EmisorNombre;
    public String Fecha;
    public String FechaLiquidacion;
    public String FechaRegistro;
    public int Folio;
    public int FolioRelacionado;
    public String FolioXStream;
    public String Generico;
    public String Grupo;
    public String Hora;
    public long ID;
    public String Instrumento;
    public String LugarPago;
    public int Mercado;
    public String Moneda;
    public double Monto;
    public int Plazo;
    public String PlazoAAMM;
    public String PlazoDias;
    public double Precio;
    public String Reajustabilidad;
    public String TipoInstrumentoRF;
    public double ValorPar;
    public double Variacion;
    public String VendedorBCSTraderID;
    public String VendedorIdCorredor;
    public String VendedorIdOpdir;
    public String VendedorTraderID;
    public String VendedorTraderIdOpdir;
    public String VendedorXStreamIdCorredor;
    public String VendedorXStreamIdOpdir;
    public String fondoCompra;
    public String fondoVenta;
    public boolean madurez;
    public boolean odAutomatica;
    public boolean offshore;
    public String puntaCompraBolsa;
    public double puntaCompraCantidad;
    public double puntaCompraPrecio;
    public String puntaVentaBolsa;
    public double puntaVentaCantidad;
    public double puntaVentaPrecio;
    public boolean ventaCorta;
    
    // SIM
    public double PrecioPlazo;
    public double MontoPlazo;
    public String FechaLiquidacionPlazo;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
