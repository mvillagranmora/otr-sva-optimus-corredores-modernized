SELECT	OPE."IdTipoProducto"
		,OPE."IdTipoOperacion"
		--,TMP."IdTipoEstado" 
		,'' AS "IdEstado"
		,SUM(OPE."MontoOperacionFinalAnticipo") AS "MtoEvento"
FROM	"BSCL.Mesa.BD::MES.Operacion" OPE
WHERE	CAST("FechaLiquidacion" AS DATE) = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD')
		AND OPE."IdTipoProducto" = :IdTipoProducto
		AND OPE."IdTipoOperacion" = :IdTipoOperacion
		AND OPE."IdTipoReajuste" = :IdTipoReajuste							
		AND OPE."IdMesaOrigen" = :IdMesa 
		AND OPE."IdEstado" = 'ANT'
GROUP BY OPE."IdTipoProducto", OPE."IdTipoOperacion"