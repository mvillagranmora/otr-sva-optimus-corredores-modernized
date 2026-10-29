SELECT C."IdCentralizacion"
FROM "BSCL.Contabilidad.BD::ITZ.Centralizacion" C
WHERE C."IdTipoCentralizacion" = :IdTipoCentralizacion AND C."Estado" = 'V' AND C."FechaContable" = :FechaContable AND C."IdEmpresa" = :IdEmpresa