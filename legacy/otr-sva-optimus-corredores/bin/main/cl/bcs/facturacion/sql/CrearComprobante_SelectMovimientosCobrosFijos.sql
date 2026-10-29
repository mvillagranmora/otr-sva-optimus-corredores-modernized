SELECT MVTO."IdCliente", 
       MVTO."IdPortafolio", 
       COUNT(COB."IdMovimientoComprobanteCobro") AS "Cantidad"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTO 
  LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" COB
     ON COB."IdMovimientoComprobante" = MVTO."IdMovimientoComprobante"
    AND COB."IdEstado" = 'VIG' 
    AND COB."IdCobro" IN ('COFI', 'DEFI', 'GAFI', 'GFFA', 'GFOD')
WHERE MVTO."IdCliente" IN (:idsCliente)
  AND MVTO."IdPortafolio" IN (:idsPortafolio)
  AND MVTO."FechaMovimientoComprobante" IN (:idsFechaComprobante)
  AND MVTO."IdEstadoFacturacion" NOT IN('ANU', 'ING')
  AND MVTO."IdTipoComprobante" NOT IN ('NCR','NCE','NEO','NDE','NDR','NCO','NEE')
  AND NOT EXISTS (SELECT 1 FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS NOTA_CREDITO
					INNER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TIPO_COMPROBANTE ON NOTA_CREDITO."IdTipoComprobante" = TIPO_COMPROBANTE."IdTipoComprobante" 
					WHERE NOTA_CREDITO."IdComprobanteReferencia" = MVTO."IdComprobante"
					AND TIPO_COMPROBANTE."TipoComprobanteFacturacion" IN ('NCR','NCD'))
GROUP BY MVTO."IdCliente", MVTO."IdPortafolio", MVTO."FechaMovimientoComprobante"; 