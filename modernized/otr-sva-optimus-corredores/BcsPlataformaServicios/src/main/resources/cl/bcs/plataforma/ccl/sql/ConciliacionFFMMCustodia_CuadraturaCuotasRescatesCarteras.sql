SELECT "IdSolicitudAporteRescate", "IdCliente","IdSerieFondo", "NumeroCuotasFFMM" ,  "NumeroCuotasCustodia"
FROM (
	SELECT "IdSolicitudAporteRescate",SOL."IdCliente",SOL."IdSerieFondo",count(car."IdCartera") as "Carteras","NumeroCuotas" as "NumeroCuotasFFMM",SUM(COALESCE(CAR."EGRCantidad",0)) AS "NumeroCuotasCustodia"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."EGRIdOperacion" = SOL."IdSolicitudAporteRescate" AND CAR."IdClasificacionTipoInstrumento" = 'FM'
	WHERE SOL."Estado" = 'CRT' 
	AND SOL."TipoSolicitud" = 'RES' and SOL."IdCarga" = 0
	GROUP BY SOL."IdSolicitudAporteRescate", SOL."NumeroCuotas", SOL."IdCliente",SOL."IdSerieFondo"
)
where "NumeroCuotasFFMM" != "NumeroCuotasCustodia"