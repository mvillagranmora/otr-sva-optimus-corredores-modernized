SELECT 
    CCP."IdCuentaCustodioPortafolio" AS "IdCuentaCustodioPortafolio",
    CCP."IdPortafolio" AS "IdPortafolio",
    CASE 
        WHEN P."Proposito" = 'PROPOSITOAPV' THEN 'S'
        ELSE 'N'
    END AS "EsAPV"
FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" CCP
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" P 
    ON CCP."IdPortafolio" = P."IdPortafolio"
WHERE CCP."IdCuentaCustodioPortafolio" = :IdCuentaCustodioPortafolio
