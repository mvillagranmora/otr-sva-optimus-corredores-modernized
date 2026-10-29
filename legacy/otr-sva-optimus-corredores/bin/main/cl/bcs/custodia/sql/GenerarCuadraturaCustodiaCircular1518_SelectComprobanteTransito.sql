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
AND TIPO_OPERACION."Accion" IN ('VENTA','COMPRA')
AND COMPROBANTE_CABECERA."IdCondicionLiquidacion" != 'TP'
AND COMPROBANTE."IdEstadoComprobanteDetalle" != 'ANU'
AND COMPROBANTE_CABECERA."IdTipoComprobante" NOT IN ('NCE','NCR','NDR','NEE','NEO') 
AND NOT EXISTS (SELECT 1 FROM "BSCL.Facturacion.BD::FAC.Comprobante" NOTA_CREDITO WHERE NOTA_CREDITO."FolioComprobanteReferencia" = COMPROBANTE_CABECERA."FolioComprobante" 
AND NOTA_CREDITO."IdTipoComprobante" IN ('NCE','NCR','NDR','NEE','NEO')AND NOTA_CREDITO."FolioComprobanteReferencia" > 0)
UNION ALL
SELECT
	CASE 
		WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN 
			"NemoSVS" 
		ELSE 
			"Nemotecnico" 
	END AS "Nemotecnico",
	CASE 
		WHEN "Accion" = 'COMPRA' THEN 
			CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "ValorRescate"  ELSE "Cantidad"  END 
		ELSE TO_DECIMAL(0,28,6)
	END AS "CantidadCompra",
	CASE 
		WHEN "Accion" = 'VENTA' THEN 
			CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "ValorRescate"  ELSE "Cantidad"  END 
		ELSE TO_DECIMAL(0,28,6)
	END AS "CantidadVenta",
	"PrecioCompra" AS "Precio",
	"IdTipoInstrumento",
	"IdClasificacionTipoInstrumento"
FROM
	"BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito"
WHERE
	"FechaLiquidacion" > :FechaCuadratura
	AND "IdTipoOperacion" = 'COMTER';