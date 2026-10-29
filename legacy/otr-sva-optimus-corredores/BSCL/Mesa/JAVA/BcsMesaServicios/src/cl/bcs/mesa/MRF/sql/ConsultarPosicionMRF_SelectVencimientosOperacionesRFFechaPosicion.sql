SELECT	OPE."IdTipoProducto"
		,OPE."IdTipoOperacion"
		--,TMP."IdAgrupacion"
		--,TMP."IdTipoEstado"
		,'' AS "IdEstado"
		,SUM(OPE."MontoOperacionFinal") AS "MtoEvento"
FROM	"BSCL.Mesa.BD::MES.Operacion" OPE
WHERE	CAST("FechaVencimiento" AS DATE) = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD')
		AND OPE."IdTipoProducto" = :IdTipoProducto
		AND OPE."IdTipoOperacion" = :IdTipoOperacion
		AND OPE."IdTipoReajuste" = :IdTipoReajuste							
		AND OPE."IdMesaOrigen" = :IdMesa 
		AND OPE."IdEstado" IN ('LIQ')
GROUP BY OPE."IdTipoProducto", OPE."IdTipoOperacion"