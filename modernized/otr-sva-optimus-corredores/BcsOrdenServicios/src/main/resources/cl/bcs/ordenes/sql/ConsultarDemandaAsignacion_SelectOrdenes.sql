SELECT ord_tipope."AccionOrden",tipoins."Nombre" AS "NombreTipoInstrumento",COALESCE(sub."Nemotecnico",ord."Nemotecnico") AS "Nemotecnico",ord."IdTipoPrecio",t."Nombre" AS "NombreTipoPrecio",

CASE ord."IdTipoPrecio" WHEN 'M' THEN 0 ELSE ord."PrecioOrden" END AS "Precio",

SUM(CASE ord."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ord."ValorRescate" ELSE ord."Cantidad" END) - SUM(CASE ord."IdClasificacionTipoInstrumento" WHEN 'IF' THEN asi."ValorRescateAsignado" ELSE asi."CantidadAsignada" END)AS "Cantidad" 
FROM "BSCL.Orden.BD::ORD.Orden" ord 
LEFT JOIN  	"BSCL.Orden.BD::ORD.Asignacion"	asi
ON (ord."IdOrden" = asi."IdOrden")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" ord_tipope on ord."IdTipoOperacion" = ord_tipope."IdTipoOperacion"
LEFT JOIN "BSCL.Orden.BD::ORD.TipoPrecioOrden" t ON ord."IdTipoPrecio" = t."IdTipoPrecioOrden"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" sub ON sub."IdInstrumento" = ord."IdInstrumentoSubyacente"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins ON tipoins."IdTipoInstrumento" = ord."IdTipoInstrumento"
WHERE ord."IdEstadoOrden" IN ('ING','OFE') AND ord."OrdenVigente" = 'S' AND TO_DATE(ord."FechaOperacion") <= :FechaConsulta 
AND ord."IdEmpresa" = :IdEmpresa AND ord."EsVentaCorta" = :EsVentaCorta AND ((ord."IdTipoInstrumento"= :IdTipoInstrumento AND ord."IdInstrumento" = :IdInstrumento) OR (ord."IdTipoInstrumento"= :IdTipoInstrumento AND ord."IdInstrumentoSubyacente" = :IdInstrumento)) 
GROUP BY ord_tipope."AccionOrden",tipoins."Nombre",ord."Nemotecnico",ord."IdInstrumentoSubyacente",ord."IdTipoPrecio",ord."PrecioOrden",t."Nombre",sub."Nemotecnico"
