SELECT
	 COALESCE(PPT."MontoPorTramo",0)	AS "MontoPorTramo"
	,COALESCE(PPT."ValorCompra",0)		AS "PuntaCompra"
	,COALESCE(PPT."ValorVenta",0)		AS "PuntaVenta"
	,PTA."PuntaCompra"					AS "PuntaCompraDefault"
	,PTA."PuntaVenta"					AS "PuntaVentaDefault"	
	,COALESCE(PTA."CheckPrecioPorTramo",'NO') AS "CheckPrecioPorTramo"
FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo" PPT
INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" PTA
	ON PTA."IdPuntas" = PPT."IdPuntas" 
WHERE PTA."IdMonedaPunta" = :IdMoneda
  AND PTA."IdEmpresa" = :IdEmpresa
  AND PPT."Canal" = :Canal;