SELECT ord."IdInstrumento",ord."IdInstrumentoSubyacente",ord."IdTipoOperacion",ord."EsVentaCorta",ord."IdTipoCanalIngreso",ord."IdTipoInstrumento", ord."IdBolsa", 
IFNULL(SUM(CASE ord_tipope."AccionOrden" WHEN 'COMPRA' THEN (CASE WHEN ord."EsFungible" = 'S' THEN ord."ValorRescate" ELSE ord."CantidadOrden" END) WHEN 'VENTA' THEN 0 ELSE 0 END),0) AS "OrdenadoCompra",
IFNULL(SUM(CASE ord_tipope."AccionOrden" WHEN 'VENTA' THEN (CASE WHEN ord."EsFungible" = 'S' THEN ord."ValorRescate" ELSE ord."CantidadOrden" END) WHEN 'COMPRA' THEN 0 ELSE 0 END),0) AS "OrdenadoVenta"
FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenEnNegociacion"  ord 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" ord_tipope on ord."IdTipoOperacion" = ord_tipope."IdTipoOperacion"
WHERE ord."IdEstadoOrden" IN ('OFE') /*AND ord."OrdenVigente" = 'S' */AND TO_DATE(ord."FechaOperacionOrden") <= :FechaConsulta AND  "IdEstadoAsignacion" <> 'ASC'
GROUP BY ord."IdInstrumento",ord."IdInstrumentoSubyacente",ord."IdTipoOperacion",ord."EsVentaCorta",ord."IdTipoCanalIngreso",ord."IdTipoInstrumento", ord."IdBolsa"
