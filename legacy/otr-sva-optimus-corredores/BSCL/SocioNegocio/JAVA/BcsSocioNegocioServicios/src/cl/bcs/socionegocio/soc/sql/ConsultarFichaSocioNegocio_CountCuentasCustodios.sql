SELECT CCP."IdPortafolio"                      AS "IdEntidad"
     , COUNT(CCP."IdCuentaCustodioPortafolio") AS "Cantidad"
FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCP
WHERE CCP."IdEstado" = 'ACT'
  AND CCP."IdPortafolio" IN (:IdsEntidades)
GROUP BY CCP."IdPortafolio";