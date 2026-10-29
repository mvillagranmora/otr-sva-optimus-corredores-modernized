SELECT 
	COM."IdMovimientoComprobante",
	COM."EsComprobIndividual" AS "EsComprobIndividualAntiguo",
	COM."Glosa" AS "GlosaAntigua",
	COM."IdFormaCustodia" AS "IdFormaCustodiaAntigua",
	COM."IdPortafolio" AS "IdPortafolioAntiguo",
	COM."IdCliente",
	COM."NombreLargoCliente" AS "NombreCliente",
	COM."DocumentoIdentidad"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS COM
WHERE COM."IdMovimientoComprobante" IN (:IdMovimientoComprobante);