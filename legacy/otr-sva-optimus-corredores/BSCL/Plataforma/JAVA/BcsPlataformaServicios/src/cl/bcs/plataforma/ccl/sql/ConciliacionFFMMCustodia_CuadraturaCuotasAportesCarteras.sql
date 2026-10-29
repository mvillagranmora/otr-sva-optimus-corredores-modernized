SELECT "IdSolicitudAporteRescate"
FROM
(
	SELECT "IdSolicitudAporteRescate",count(car."IdCartera") as "Carteras",SUM("NumeroCuotas") as "NumeroCuotasFFMM",SUM(COALESCE(CAR."INGCantidad",0)) AS "NumeroCuotasCustodia"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdOperacion" = SOL."IdSolicitudAporteRescate" AND CAR."IdClasificacionTipoInstrumento" = 'FM'
	WHERE SOL."Estado" = 'CRT'
	AND SOL."TipoSolicitud" = 'APO' and SOL."IdCarga" = 0
	GROUP BY "IdSolicitudAporteRescate"
)
where "NumeroCuotasFFMM" != "NumeroCuotasCustodia"