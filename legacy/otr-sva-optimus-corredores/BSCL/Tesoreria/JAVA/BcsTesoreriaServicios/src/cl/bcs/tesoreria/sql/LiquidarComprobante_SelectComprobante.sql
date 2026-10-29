
----------------------------------------------------
-- Solo para visualizar contenido de la tabla global 
----------------------------------------------------

SELECT
	"IdInterno"
   ,"IdComprobanteInformado"
   ,"IdTipoMovimientoInformado"
   ,"IdComprobante"
   ,"IdTipoMovimiento"
   ,"MontoComprobante"
   ,"CantidadFormaDePago"
   ,"MontoTotalFormaDePago"
   ,"IdEstadoComprobante"
   ,"AfectaCuentaInversion"
   ,"IdPortafolioTitularPago"
   ,"IdConcepto"
   ,"FechaMovimiento"
   ,"IdPersona"
   ,"NombrePersona"
   ,"TitularPago"
   ,"IdTipoPersonaTitularPago"
   ,"IdPersonaTitularPago"
   ,"FechaLiquidacion"
   ,"IdCaja"
FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante";
