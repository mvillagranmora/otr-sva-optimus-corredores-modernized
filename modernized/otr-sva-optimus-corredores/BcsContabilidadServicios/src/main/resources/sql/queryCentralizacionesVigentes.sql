SELECT DISTINCT
    C."IdTipoCentralizacion"    AS "IdTipoCentralizacion",
    CT."Nombre"                 AS "Nombre"
FROM "BSCL.Contabilidad.BD::ITZ.Centralizacion" C
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" CT
    ON C."IdTipoCentralizacion" = CT."IdTipoCentralizacion"
WHERE C."IdTipoCentralizacion" IN (:IdTipoCentralizacion)
  AND C."Estado"       = 'V'
  AND C."FechaContable" = :FechaContable
  AND C."IdEmpresa"    = :IdEmpresa
