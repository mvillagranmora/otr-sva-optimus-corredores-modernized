SET SCHEMA "BSCL"; 
GO 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarLibroDteComprobante";
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarLibroDteComprobante" As 
SELECT
        Cmbte."IdEmpresa",
        LibCmbte."IdLibro",
        Cmbte."IdPortafolio", 
        Cmbte."IdCliente",
        Cmbte."NombreLargoCliente",        
        Cmbte."FechaComprobante",
        Cmbte."IdComprobante", 
        Cmbte."IdTipoComprobante",
        TipCmbte."Nombre" AS "NombreTipoComprobante",
        Cmbte."IdMoneda",
        moneda."Nombre" AS "NombreComprobante",
        Cmbte."Monto",
        Cmbte."IdMonedaComprobante",
        MonedaCmbte."Nombre" AS "NombreMonedaComprobante",
        Cmbte."MontoComprobante",
        Cmbte."IdMonedaLiquidacion",
        MonedaLiq."Nombre" AS "NombreMonedaLiquidacion",
        Cmbte."MontoLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" As Cmbte
  Left Join "BSCL.Facturacion.BD::FAC.LibroDteComprobante" As LibCmbte
    On LibCmbte."IdComprobante" = Cmbte."IdComprobante"
  Left Outer Join "BSCL.Plataforma.BD::PLA.Moneda" As Moneda 
    On Moneda."IdMoneda" = Cmbte."IdMoneda"
  Left Outer Join "BSCL.Plataforma.BD::PLA.Moneda" As MonedaCmbte 
    On MonedaCmbte."IdMoneda" = Cmbte."IdMonedaComprobante"
  Left Outer Join "BSCL.Plataforma.BD::PLA.Moneda" As MonedaLiq 
    On MonedaLiq."IdMoneda" = Cmbte."IdMonedaLiquidacion"  
  Left Outer Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte 
    On TipCmbte."IdTipoComprobante" = Cmbte."IdTipoComprobante"
WHERE Cmbte."IdEstadoComprobante" = 'ENV';
GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarLibroDteComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
