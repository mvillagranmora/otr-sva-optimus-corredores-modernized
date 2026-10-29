-- ******************************************************************** 
-- SQL de servicio No. 700.146 - ValidarTransferenciaDVP.java

  SELECT DISTINCT
		CFP."ClaveDCV"
		,CD."IdTipoOperacion"
		,NT."IdEstado"
  FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" NT
  INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" NTD ON NTD."IdNominaTransferencia" = NT."IdNominaTransferencia"
  INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago"  
  INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CD ON CD."IdComprobante" = CFP."IdComprobante"
  WHERE NTD."IdNominaTransferencia" = :IdNominaTransferencia