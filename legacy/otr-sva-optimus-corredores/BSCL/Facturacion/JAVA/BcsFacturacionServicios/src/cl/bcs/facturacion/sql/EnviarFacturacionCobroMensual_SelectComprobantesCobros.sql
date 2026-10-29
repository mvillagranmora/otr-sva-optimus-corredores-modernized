-- CONVENIO
SELECT "IdContratoComision"
      ,"IdComprobante"
 FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenio"
WHERE "IdComprobante" 	IN (:IdComprobanteConvenio)
  AND "IdEmpresa" 		= :IdEmpresa

UNION ALL
-- CONTRATO
SELECT "IdContratoComision"
      ,"IdComprobante"
 FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" AS CONT
WHERE "IdComprobante" 	IN (:IdComprobanteContrato)
  AND "IdEmpresa" 		= :IdEmpresa  
