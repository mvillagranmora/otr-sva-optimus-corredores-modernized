SELECT
	CAR."IdCliente",
	CLI."NombreCompleto" AS "NombreCliente",
	CAR."Nemotecnico",	
	SUM(CAR."INGCantidad") AS "Nominales",
	SUM(CAR."INGMontoMercadoActualMonedaInstrumento") AS "Pesos"

FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = CAR."IdCliente")
WHERE CAR."IdEstado" = 'VIG'
AND CAR."IdClasificacionTipoInstrumento" = (CASE WHEN COALESCE(:IdClasificacionTipoInstrumento,'') = '' THEN CAR."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END)
AND CAR."IdTipoInstrumento" = (CASE WHEN COALESCE(:IdTipoInstrumento,'') = '' THEN CAR."IdTipoInstrumento" ELSE :IdTipoInstrumento END)
AND   CAR."FechaOperacion" <=:FechaConsulta
GROUP BY  CAR."IdCliente", CLI."NombreCompleto", CAR."Nemotecnico";

