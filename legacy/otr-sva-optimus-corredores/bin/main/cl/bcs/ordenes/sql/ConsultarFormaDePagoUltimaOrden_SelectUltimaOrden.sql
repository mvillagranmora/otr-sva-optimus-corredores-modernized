SELECT TOP 1 ORD."IdOrden"
FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
LEFT JOIN "BSCL.Orden.BD::ORD.FormaDePagoOrden" AS FPAG
ON ORD."IdOrden" = FPAG."IdOrden"
WHERE ORD."IdPortafolioCliente" = :IdPortafolio
	AND ORD."IdTipoOperacion" = :IdTipoOperacion
	AND ORD."IdMonedaLiquidacionOrden" = :IdMonedaLiquidacionOrden
ORDER BY ORD."IdOrden" DESC;