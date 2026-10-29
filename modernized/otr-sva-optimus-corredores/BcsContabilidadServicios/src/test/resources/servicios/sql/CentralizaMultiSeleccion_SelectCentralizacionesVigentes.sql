SELECT DISTINCT C."IdTipoCentralizacion",CT."Nombre"
FROM "BSCL.Contabilidad.BD::ITZ.Centralizacion" C
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" CT ON (C."IdTipoCentralizacion" = CT."IdTipoCentralizacion")
WHERE C."IdTipoCentralizacion" IN (:IdTipoCentralizacion) AND C."Estado" = 'V' AND C."FechaContable" = :FechaContable AND C."IdEmpresa" = :IdEmpresa