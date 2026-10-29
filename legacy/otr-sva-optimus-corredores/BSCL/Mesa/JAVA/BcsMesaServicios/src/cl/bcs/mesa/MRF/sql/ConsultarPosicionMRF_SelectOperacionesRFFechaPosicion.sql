SELECT	OPE."IdTipoProducto"
		,OPE."IdTipoOperacion"
		--,TMP."IdAgrupacion"
		--,TMP."IdTipoEstado"
		,'' AS "IdEstado"
		,SUM(OPE."MontoOperacionInicial") AS "MtoEvento"
FROM	"BSCL.Mesa.BD::MES.Operacion" OPE
WHERE	(CAST("FechaLiquidacion" AS DATE) = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD') OR CAST("FechaCondicionLiquidacion" AS DATE) = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD') )
		AND OPE."IdTipoProducto" = :IdTipoProducto
		AND OPE."IdTipoOperacion" = :IdTipoOperacion
		AND OPE."IdTipoReajuste" = :IdTipoReajuste							
		AND OPE."IdMesaOrigen" = :IdMesa 
		AND ( OPE."IdEstado" = 'VIG' OR OPE."IdEstado" = 'PEN' )
		--AND TMP."IdTipoEstado" = 'OPE'	
GROUP BY OPE."IdTipoProducto", OPE."IdTipoOperacion"