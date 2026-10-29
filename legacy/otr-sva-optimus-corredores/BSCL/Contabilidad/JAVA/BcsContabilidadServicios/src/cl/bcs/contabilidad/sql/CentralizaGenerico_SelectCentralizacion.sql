SELECT 
"IdTipoCentralizacion"
,"IdCentralizacion"
FROM
(SELECT
row_number() OVER (partition by "IdTipoCentralizacion" order BY "IdCentralizacion" DESC) AS "Row"
,"IdTipoCentralizacion"
,"IdCentralizacion"
FROM "BSCL.Contabilidad.BD::ITZ.Centralizacion"
WHERE "IdTipoCentralizacion" IN (:IdTipoCentralizacion)
AND "FechaContable" = :FechaContable
AND "Estado" = 'V')
WHERE "Row" = 1;