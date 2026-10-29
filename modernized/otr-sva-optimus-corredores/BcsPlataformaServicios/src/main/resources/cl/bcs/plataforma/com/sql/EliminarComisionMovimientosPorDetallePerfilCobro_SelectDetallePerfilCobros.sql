SELECT 
	  DETALLE."IdDetallePerfilCobros"
	, DETALLE."IdPerfilCobros"
	, DETALLE."IdBolsa"
	, DETALLE."IdTipoProducto"
	, DETALLE."IdTipoOperacion"
	, DETALLE."IdCanalIngreso" AS "IdTipoCanalIngreso"
	, TO_BIGINT(SUBSTR_AFTER(DETALLE."IdPerfilCobros",'_')) AS "IdPortafolio"
	, VALCOB."IdCobro"
	, DETALLE."IdMoneda"
	, DETALLE."Borrado"
	, TO_DECIMAL(VALCOB."ValorCobro",26 ,3) AS "ValorCobro"
FROM "BSCL.Plataforma.BD::COM.DetallePerfilCobros" AS DETALLE
INNER JOIN "BSCL.Plataforma.BD::COM.ValorCobro" AS VALCOB ON VALCOB."IdValorCobro" = DETALLE."IdValorCobro"
WHERE
	DETALLE."IdDetallePerfilCobros" IN (:IdDetallePerfilCobros)
	AND VALCOB."IdCobro" IN ('COVA', 'COFI')
	AND  DETALLE."Borrado" = 'S';