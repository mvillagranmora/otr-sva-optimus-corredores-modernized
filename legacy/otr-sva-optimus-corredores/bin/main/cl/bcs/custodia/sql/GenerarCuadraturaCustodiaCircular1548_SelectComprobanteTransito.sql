SELECT 
CASE 
	WHEN COALESCE(COMPROBANTE."NemotecnicoSubyacente",'') != '' THEN COMPROBANTE."NemotecnicoSubyacente"
	WHEN COMPROBANTE."IdClasificacionTipoInstrumento" = 'IF' THEN COMPROBANTE."NemoSVS"
	ELSE COMPROBANTE."NemotecnicoInstrumento"
END AS "Nemotecnico",
CASE TIPO_OPERACION."Accion" WHEN 'COMPRA' THEN
	CASE
		WHEN COMPROBANTE."IdClasificacionTipoInstrumento" = 'IF' THEN COMPROBANTE."ValorRescate"
		ELSE COMPROBANTE."Cantidad"
	END
	ELSE TO_DECIMAL(0,28,6)
END AS "CantidadCompra",
CASE TIPO_OPERACION."Accion" WHEN 'VENTA' THEN
	CASE
		WHEN COMPROBANTE."IdClasificacionTipoInstrumento" = 'IF' THEN COMPROBANTE."ValorRescate"
		ELSE COMPROBANTE."Cantidad"
	END
	ELSE TO_DECIMAL(0,28,6)
END AS "CantidadVenta",
COMPROBANTE."Precio",
CASE 
	WHEN COALESCE(COMPROBANTE."NemotecnicoSubyacente",'') != '' THEN COMPROBANTE."IdTipoInstrumentoSubyacente"
	ELSE COMPROBANTE."IdTipoInstrumento"
END AS "IdTipoInstrumento",
COALESCE(TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS COMPROBANTE
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMPROBANTE_CABECERA ON COMPROBANTE_CABECERA."IdComprobante" = COMPROBANTE."IdComprobante"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO 
ON TIPO_INSTRUMENTO."IdTipoInstrumento" = CASE 
												WHEN COALESCE(COMPROBANTE."NemotecnicoSubyacente",'') != '' THEN COMPROBANTE."IdTipoInstrumentoSubyacente"
												ELSE COMPROBANTE."IdTipoInstrumento"
										END
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIPO_OPERACION ON COMPROBANTE."IdTipoOperacion" = TIPO_OPERACION."IdTipoOperacion"
WHERE COMPROBANTE."FechaLiquidacion" > :FechaCuadratura 
AND COMPROBANTE."FechaComprobanteDetalle" <= :FechaCuadratura 
AND COMPROBANTE."IdClasificacionTipoInstrumento" IN ('RV','RF','IF') 
AND COMPROBANTE."IdFormaCustodia" = 'CUS' 
AND TIPO_OPERACION."Accion" IN ('VENTA','COMPRA')
AND COMPROBANTE."IdEstadoComprobanteDetalle" != 'ANU'
AND NOT EXISTS (SELECT 1 FROM "BSCL.Facturacion.BD::FAC.Comprobante" NOTA_CREDITO WHERE NOTA_CREDITO."FolioComprobanteReferencia" = COMPROBANTE_CABECERA."FolioComprobante" AND NOTA_CREDITO."IdTipoComprobante" IN ('NCE','NCR','NDR','NEE','NEO'))