SELECT ord."IdInstrumento",ord."IdInstrumentoSubyacente",ord."IdTipoOperacion",ord."EsVentaCorta",ord."IdTipoCanalIngreso",ord."IdTipoInstrumento",
	IFNULL(SUM(
		CASE ord_tipope."AccionOrden" 
			WHEN 'COMPRA' THEN (CASE WHEN ord."EsFungible" = 'S' THEN ord."ValorRescate" ELSE ord."Cantidad" END) 
			WHEN 'VENTA' THEN 0 ELSE 0 END),0) 
	- IFNULL(SUM(
		CASE ord_tipope."AccionOrden" 
			WHEN 'VENTA' THEN (CASE WHEN ord."EsFungible" = 'S' THEN asi."ValorRescateAsignado" ELSE asi."CantidadAsignada" END) 
			WHEN 'COMPRA' THEN 0 ELSE 0 END),0) AS "OrdenadoCompra",
	
		
	IFNULL(SUM(
		CASE ord_tipope."AccionOrden" 
			WHEN 'VENTA' THEN (CASE WHEN ord."EsFungible" = 'S' THEN ord."ValorRescate" ELSE ord."Cantidad" END) 
			WHEN 'COMPRA' THEN 0 ELSE 0 END),0) 
	- IFNULL(SUM(
		CASE ord_tipope."AccionOrden" 
			WHEN 'VENTA' THEN (CASE WHEN ord."EsFungible" = 'S' THEN asi."ValorRescateAsignado" ELSE asi."CantidadAsignada" END) 
			WHEN 'COMPRA' THEN 0 ELSE 0 END),0)	AS "OrdenadoVenta"

FROM 		"BSCL.Orden.BD::ORD.Orden" 		ord 
LEFT JOIN  	"BSCL.Orden.BD::ORD.Asignacion"	asi
ON (ord."IdOrden" = asi."IdOrden")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" ord_tipope on ord."IdTipoOperacion" = ord_tipope."IdTipoOperacion"
WHERE ord."IdEstadoOrden" IN ('ING','OFE') AND ord."OrdenVigente" = 'S' AND TO_DATE(ord."FechaOperacion") <= :FechaConsulta
GROUP BY ord."IdInstrumento",ord."IdInstrumentoSubyacente",ord."IdTipoOperacion",ord."EsVentaCorta",ord."IdTipoCanalIngreso",ord."IdTipoInstrumento"
