SELECT
	PT."IdPuntasPorTramo"	
	,PT."IdPuntas"			
	,PT."MontoPorTramo" 	
	,PT."PorcentajeTramo"	
	,PT."MontoCentavos"  	
	,PT."ValorCompra"  	
	,PT."ValorVenta"  		
	,PT."Estado"			
	,PT."IdUsuario"		
	,PT."FechaModificacion"
	,PT."Canal"			
FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo" PT
INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" P ON PT."IdPuntas" = P."IdPuntas"
WHERE P."IdPuntas" = :IdPuntas