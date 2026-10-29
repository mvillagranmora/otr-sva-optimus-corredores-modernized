SELECT 
    A."IdAutorizado" AS "IdAutorizado",
    A."IdPortafolio" AS "IdPortafolio",
    CASE 
        WHEN P."Proposito" = 'PROPOSITOAPV' THEN 'S'
        ELSE 'N'
    END AS "EsAPV"
FROM "BSCL.Portafolio.BD::POR.Autorizado" A
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" P 
    ON A."IdPortafolio" = P."IdPortafolio"
WHERE A."IdAutorizado" IN (:IdAutorizado)
