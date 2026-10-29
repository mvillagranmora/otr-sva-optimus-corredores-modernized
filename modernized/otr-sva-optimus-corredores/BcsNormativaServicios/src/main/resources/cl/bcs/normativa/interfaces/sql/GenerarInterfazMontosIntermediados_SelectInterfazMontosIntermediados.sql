SELECT "TipoRegistro"
	   ,"CodigoAI"
	   ,"Ejercicio"
	   ,"Segmento"
	   ,"CodigoCuenta"
	   , SUM(CASE WHEN "AccionTipoOperacion" = 'COMPRA' THEN "MontoOrden" ELSE 0 END) AS "Compra"
	   , SUM(CASE WHEN "AccionTipoOperacion" = 'VENTA' THEN "MontoOrden" ELSE 0 END) AS "Venta"
FROM (
	SELECT
	'D' AS "TipoRegistro" --Constante
	,'S80080' AS "CodigoAI" --Constante
	,TO_CHAR("FechaLiquidacion",'MMYYYY') AS "Ejercicio"
	,'01' AS "Segmento" --Pendiente Mapeo
	,'0001' AS "CodigoCuenta" --Pendiente Mapeo
	,"MontoOrden"
	,"AccionTipoOperacion"
	FROM "BSCL.Orden.BD.Vistas::ConsultarOrden"
	WHERE TO_CHAR("FechaLiquidacion",'MMYYYY') = :Ejercicio AND "EstadoOrden" = 'OFERTADO' AND "IdEmpresa" = :IdEmpresa
)
GROUP BY "TipoRegistro","CodigoAI","Ejercicio","Segmento","CodigoCuenta";