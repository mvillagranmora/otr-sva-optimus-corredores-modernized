SET SCHEMA "BSCL";  
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePagoComprobante"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePagoComprobante" As
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
    CmbteFPag."IdComprobanteFormaDePago",
    CmbteFPag."IdMedioDePago",
    CmbteFPag."NombreMedioDePago",
    CmbteFPag."NumeroSerie",
    CmbteFPag."NumeroDocumento",
    CmbteFPag."IdCuentaBancaria",
    CmbteFPag."NombreCuentaBancaria",
    CmbteFPag."IdTipoCuentaBancaria",
    CmbteFPag."NombreTipoCuentaBancaria",
    CmbteFPag."NumeroCuentaBancaria",
    CmbteFPag."IdTarjetaBancaria",
    CmbteFPag."IdTipoTarjetaBancaria",
    CmbteFPag."NombreTipoTarjetaBancaria",
    CmbteFPag."NumeroTarjetaBancaria",
    CmbteFPag."FechaVctoTarjetaBancaria",
    CmbteFPag."IdModalidadDePago",
    CmbteFPag."NombreModalidadDePago",
    CmbteFPag."IdBanco",
    CmbteFPag."CodigoBanco",
    CmbteFPag."NombreBanco",
    CmbteFPag."IdMoneda",
    CmbteFPag."FechaDocumentoPago",
    CmbteFPag."MontoLiquidacionFormaDePago",
    CmbteFPag."IdEstado", 
    CmbteFPag."FechaIngresoRegistro"
        
From "BSCL.Facturacion.BD::FAC.Comprobante" As Cmbte
 Left Join "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago" As CmbteFPag
    On Cmbte."IdComprobante" = CmbteFPag."IdComprobante";
    
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePagoComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;     
