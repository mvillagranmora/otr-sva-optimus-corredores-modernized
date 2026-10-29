SELECT  
	 ConsultarOrden."IdOrden" 
	,CASE ConsultarOrden."IdClasificacionTipoInstrumento" WHEN 'DV' THEN ConsultarOrden."IdInstrumento" ELSE ConsultarOrden."IdInstrumentoSubyacente" END AS "IdInstrumento" 
	,ConsultarOrden."CantidadDisponibleOrden"
	,ConsultarOrden."PrecioMercado"
	,ConsultarOrden."MontoOrden" AS "Monto"
	,'TRANSITO' AS "Origen"
FROM  "BSCL.Orden.BD.Vistas::ConsultarOrden" AS  ConsultarOrden
WHERE  ConsultarOrden."IdPortafolio"         = :p_IdPortafolio  
AND    ConsultarOrden."IdBolsa"              = :p_IdBolsa
AND    (ConsultarOrden."IdTipoInstrumento"    = :p_IdTipoInstrumento OR ConsultarOrden."IdClasificacionTipoInstrumento" = 'DV')
AND    (ConsultarOrden."IdInstrumento"        = :p_IdInstrumento OR ConsultarOrden."IdClasificacionTipoInstrumento" = 'DV')
AND    ConsultarOrden."IdEstadoOrden"  IN  ('ING','OFE')
AND   (ConsultarOrden."FechaVigencia" >= :fechaSistema OR  ConsultarOrden."FechaVigencia" = '0001-01-01')
AND    ConsultarOrden."CantidadDisponibleOrden" > 0;