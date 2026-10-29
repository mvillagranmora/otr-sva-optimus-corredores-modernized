SELECT 
	CAR."IdCliente",
	CLI."NombreCompleto" AS "NombreCliente",
	CAR."Nemotecnico",
	SUM("INGCantidad") AS "Nominales",
	SUM("INGMontoMercadoActualMonedaInstrumento") AS "Pesos"

FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CAR."IdCliente" = CLI."IdCliente")
WHERE CAR."IdEstado" = 'VIG'
AND CAR."IdClasificacionTipoInstrumento" = (CASE WHEN COALESCE(:IdClasificacionTipoInstrumento,'') = '' THEN CAR."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END)
AND CAR."IdTipoInstrumento" = (CASE WHEN COALESCE(:IdTipoInstrumento,'') = '' THEN CAR."IdTipoInstrumento" ELSE :IdTipoInstrumento END)
AND CAR."FechaRespaldo" = :FechaConsulta
GROUP BY CAR."IdCliente", CLI."NombreCompleto", CAR."Nemotecnico";