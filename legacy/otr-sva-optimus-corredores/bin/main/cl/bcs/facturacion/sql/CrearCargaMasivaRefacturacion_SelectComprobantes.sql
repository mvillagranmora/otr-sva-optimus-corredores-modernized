SELECT 
	MOVCOMP."IdComprobante"  AS "SecuenciaComprobante"

FROM "BSCL.Facturacion.BD::GBL.CrearCargaMasivaRefacturacion_Transaccion" AS GBL
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRA ON (TRA."Folio" = GBL."FolioTransaccion" AND TRA."Nemotecnico" = GBL."Nemotecnico"  AND TRA."FechaSistemaCreacion" = GBL."FechaSistemaCreacion")
LEFT JOIN  "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MOVCOMP ON (TRA."IdTransaccion" = MOVCOMP."IdTransaccion")
GROUP BY MOVCOMP."IdComprobante";
