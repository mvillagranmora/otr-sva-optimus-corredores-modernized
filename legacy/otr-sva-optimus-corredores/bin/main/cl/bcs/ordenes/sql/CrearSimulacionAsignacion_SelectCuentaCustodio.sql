SELECT POR."IdPortafolio",POR."IdModeloCustodia",POR."IdCuentaCustodioPortafolio", (TIPOCUSTODIA."NombreTipoCustodia" || ' - ' || BOV."Nombre" || ' - ' || POR."Codigo" ) AS "CuentaCustodioPortafolio",POR."IdEstadoAutorizacion",POR."IdEstado",POR."IdBoveda"
FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" POR
    INNER JOIN "BSCL.Plataforma.BD::PLA.Boveda" as BOV ON (BOV."IdBoveda" = POR."IdBoveda")
    LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" TIPOCUSTODIA on TIPOCUSTODIA."IdTipoCustodia" = POR."IdTipoCustodia"
--    LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCuentaCustodia" TIPOCUENTA on TIPOCUENTA."IdTipoCuentaCustodia" = POR."IdTipoCuentaCustodia"
WHERE "IdPortafolio" IN (:idsPortafolio) 
AND POR."IdEstadoAutorizacion" != 'RZD'
AND POR."IdEstado" = 'ACT'
