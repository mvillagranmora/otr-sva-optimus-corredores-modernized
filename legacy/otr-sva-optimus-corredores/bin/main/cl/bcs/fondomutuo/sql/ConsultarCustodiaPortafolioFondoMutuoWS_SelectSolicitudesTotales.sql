SELECT
"Solicitudes"."IdPortafolio"
,"SerieFondo"."IdInstrumento"
,TO_BIGINT(COUNT(1)) AS "Contador"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitudes"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "Solicitudes"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :IdEmpresa)
WHERE
	"Solicitudes"."Estado" IN ('ING', 'PEN')
AND "Solicitudes"."IdEmpresa" = :IdEmpresa
AND "Solicitudes"."TipoSolicitud" IN ('APO','RES')
AND ("Solicitudes"."IdCliente" = :IdCliente)
AND (:FiltraIngresoAutomatico = 'N' OR "IngresoAutomatico" = :IngresoAutomatico)
AND "Solicitudes"."CalculoPorMontoCuota" = 'T'
GROUP BY
"Solicitudes"."IdPortafolio"
,"SerieFondo"."IdInstrumento"