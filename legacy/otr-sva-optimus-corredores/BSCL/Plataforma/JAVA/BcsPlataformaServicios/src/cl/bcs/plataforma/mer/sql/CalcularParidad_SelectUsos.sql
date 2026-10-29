SELECT USO."IdUso", USO."NombreUso", COUNT(USO_PARAMETRO."IdUso") AS "Cantidad"
FROM "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio" USO
LEFT JOIN "BSCL.Plataforma.BD::MER.UsoParidadParametroFinanciero" USO_PARAMETRO ON (USO."IdUso" = USO_PARAMETRO."IdUso")
WHERE USO."IdUso" IN (:IdUso) AND USO."IdEmpresa" = :IdEmpresa
GROUP BY USO."IdUso", USO."NombreUso";