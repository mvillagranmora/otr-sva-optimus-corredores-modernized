SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMovimientoComprobanteFormaDePago"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMovimientoComprobanteFormaDePago" As
SELECT
	cmbte."IdEmpresa",
    cmbte."IdCliente",
    cmbte."IdPortafolio",
    cmbte."ModPrefFact",
    cmbte."NombreLargoCliente",
    cmbte."IdEstadoComprobante",
    cmbte."MontoCobro",   
    cmbte."IdMonedaComprobante",    
    cmbte."IdTipoComprobante",
    cmbte."IdComprobante",
    cmbte."IdConcepto",
    MvtoCmbteFPag."IdMovimientoComprobanteFormaDePago",
    MvtoCmbteFPag."IdMovimientoComprobante",
    MvtoCmbteFPag."IdTipoMovimiento",
    MvtoCmbteFPag."IdMedioDePago",
    MvtoCmbteFPag."NombreMedioDePago",
    MvtoCmbteFPag."NumeroSerie",
    MvtoCmbteFPag."NumeroDocumento",
    MvtoCmbteFPag."IdCuentaBancaria",
    MvtoCmbteFPag."NombreCuentaBancaria",
    MvtoCmbteFPag."IdTipoCuentaBancaria",
    MvtoCmbteFPag."NombreTipoCuentaBancaria",
    MvtoCmbteFPag."NumeroCuentaBancaria",
    MvtoCmbteFPag."IdTarjetaBancaria",
    MvtoCmbteFPag."IdTipoTarjetaBancaria",
    MvtoCmbteFPag."NombreTipoTarjetaBancaria",
    MvtoCmbteFPag."NumeroTarjetaBancaria",
    MvtoCmbteFPag."FechaVctoTarjetaBancaria",
    MvtoCmbteFPag."IdModalidadDePago",
    MvtoCmbteFPag."NombreModalidadDePago",
    MvtoCmbteFPag."IdBanco",
    MvtoCmbteFPag."CodigoBanco",
    MvtoCmbteFPag."NombreBanco",
    MvtoCmbteFPag."IdMoneda",
    MvtoCmbteFPag."FechaDocumentoPago",
    MvtoCmbteFPag."MontoLiquidacionFormaDePago",
    MvtoCmbteFPag."IdEstado", 
    MvtoCmbteFPag."FechaIngresoRegistro"
        
From "BSCL.Facturacion.BD::FAC.Comprobante" As Cmbte
 Inner Join "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" As CmbteDet
    ON Cmbte."IdComprobante" = CmbteDet."IdComprobante"
 Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MvtoCmbteDet
    ON CmbteDet."IdComprobanteDetalle" = MvtoCmbteDet."IdComprobanteDetalle"
 Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte
    ON MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteDet."IdMovimientoComprobante"
 Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" As MvtoCmbteFPag
    On MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteFPag."IdMovimientoComprobante";
    
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMovimientoComprobanteFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;     
