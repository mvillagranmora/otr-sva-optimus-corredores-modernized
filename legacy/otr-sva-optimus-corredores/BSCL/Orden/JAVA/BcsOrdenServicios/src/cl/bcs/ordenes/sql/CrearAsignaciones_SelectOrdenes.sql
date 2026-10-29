/********************************************************** 
Proposito	: consulta de ordenes para asignar
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT ord."IdOrden",ord."IdTipoOperacion",ord."IdProducto" AS "IdTipoProducto",ord."IdTipoPrecio",ord."EsVentaCorta", ord."PrecioOrden",inst."Tamano",
ord."IdPortafolioCliente" as "IdPortafolio",ordDisp."CantidadDisponible", ordDisp."MontoDisponible", ordDisp."ValorRescateDisponible", 
ord."IdTipoCanalIngreso",ord."IdEstadoOrden",ord."IdMonedaOrden",
ord."IdMonedaLiquidacionOrden",ord."Cantidad",ord."IdGrupoCobro", ord."IdInstrumento",ord."IdCondicionDeLiquidacion",
ord."IdBolsa",ord."FechaOperacion", ord."IdClasificacionTipoInstrumento",ord."TipoCambio",ord."IdLibroOrdenes",ord."ValorRescate",tipoope."AccionOrden",
ord."IdInstrumentoSubyacente",ord."IdTipoInstrumento",ord."PlazoSimultanea",ord."EsFungible",ord."BaseTasa",ord."PlazoReporte",ord."MontoInteresContado",ord."MontoInteresPlazo",
ord."PorcentajeCobros",ord."TasaReporte",ord."TasaPromedioReporte",ord."TipoInteres",ord."IdCorredorCorresponsal",cpor."EsCuentaPropia",

CASE 
	WHEN ord."IdTipoInstrumento" = 'SIM' THEN ord."TasaSimultanea" 
	WHEN ord."EsVentaCorta" = 'S' THEN ord."TasaVentaCorta" 
	WHEN ord."IdClasificacionTipoInstrumento" = 'RF' THEN ord."TasaRFIF"
	WHEN ord."IdClasificacionTipoInstrumento" = 'IF' THEN ord."TasaRFIF" 
	WHEN ord."IdClasificacionTipoInstrumento" = 'RE' THEN ord."TasaReporte" 
	ELSE 0  
END AS "Tasa",
ord."IdCliente" AS "IdCliente", 
COALESCE(ind."ValorIndicador",'N') AS "PermiteAsignarOEODMismoRut"
FROM "BSCL.Orden.BD::ORD.Orden" ord 
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" cpor ON ord."IdPortafolioCliente" = cpor."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst ON inst."IdInstrumento" = ord."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipoope ON  tipoope."IdTipoOperacion" = ord."IdTipoOperacion"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrden" ordDisp on ord."IdOrden" = ordDisp."IdOrden"
LEFT JOIN "BSCL.Orden.BD::ORD.CrearAsignaciones_Proceso" CAP ON ord."IdOrden" = CAP."IdOrden"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" ind
	ON ord."IdPortafolioCliente" = ind."IdPortafolio"
	AND "IdTipoEntidad" = 'PORTAFOLIO' AND "CodigoTipoIndicador" = 'PER_ASIGNAR_MISMORUT_OEOD'
WHERE CAP."IdOrden" IS NOT NULL;