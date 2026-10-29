WITH Solicitudes AS (
SELECT RecCli."IdRecaudacion", RecCli."IdRecaudacionCliente",SolApor."IdSolicitudAporteRescate" FROM "BSCL.APV.BD::REC.Cliente" AS RecCli
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolApor
	ON (SolApor."IdOrigenRecaCliente" = RecCli."IdRecaudacionCliente" AND SolApor."Estado" != 'ANU')
WHERE RecCli."IdRecaudacionCliente" IN (:IdRecaudacionCliente)
)

SELECT Solicitudes."IdRecaudacion", Solicitudes."IdRecaudacionCliente", count(Solicitudes."IdSolicitudAporteRescate") AS "Cantidad"
FROM Solicitudes
GROUP BY Solicitudes."IdRecaudacion", Solicitudes."IdRecaudacionCliente";