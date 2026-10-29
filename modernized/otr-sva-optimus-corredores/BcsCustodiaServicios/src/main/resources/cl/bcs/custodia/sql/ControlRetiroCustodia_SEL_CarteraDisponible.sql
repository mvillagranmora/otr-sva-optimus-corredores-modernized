SELECT CASE "IdClasificacionTipoInstrumento" WHEN 'IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "CantidadORescateDisponible","FechaLiquidacion",
"IdInstrumento","IdPortafolio","IdCliente","NemoSVS",
c."IdInstrumento" || '-' || c."IdPortafolio" || '-' || c."IdCliente" || '-' || CASE "IdClasificacionTipoInstrumento" WHEN 'IF' THEN "NemoSVS" ELSE '' END as "hashBusqueda",
"IdClasificacionTipoInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" c
WHERE 
c."IdInstrumento" || '-' || c."IdPortafolio" || '-' || c."IdCliente" || '-' || CASE "IdClasificacionTipoInstrumento" WHEN 'IF' THEN "NemoSVS" ELSE '' END IN (:lista) AND 
"IdEstado" = 'VIG';

