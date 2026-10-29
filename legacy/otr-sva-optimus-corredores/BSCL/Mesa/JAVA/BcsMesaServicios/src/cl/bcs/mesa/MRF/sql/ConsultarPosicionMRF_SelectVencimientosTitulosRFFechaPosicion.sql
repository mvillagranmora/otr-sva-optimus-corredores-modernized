SELECT	CAR."IdTipoProducto"
		,CAR."IdTipoOperacion"
		,'' AS "IdEstado"
		,SUM(CAR."INGValorRescate") AS "MtoEvento"
FROM	"BSCL.Custodia.BD::CUS.Cartera" CAR
WHERE	"FechaVencimientoInstrumento" = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD')
		AND CAR."IdTipoReajuste" = :IdTipoReajuste							
		--AND CAR."IdMesa" = :IdMesa 
		AND CAR."IdClasificacionTipoInstrumento" IN ('RF','IF')
		AND CAR."IdEstado" = 'VCD'
		AND CAR."IdPortafolio" = :IdPortafolio
GROUP BY CAR."IdTipoProducto", CAR."IdTipoOperacion"