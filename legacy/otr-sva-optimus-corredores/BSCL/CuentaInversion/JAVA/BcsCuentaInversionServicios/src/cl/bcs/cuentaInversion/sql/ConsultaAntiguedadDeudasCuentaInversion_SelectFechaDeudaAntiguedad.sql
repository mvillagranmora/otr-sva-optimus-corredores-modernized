SELECT MIN(FDEUDA."FechaSistema") AS "FechaSistema"
FROM "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" AS FDEUDA
WHERE FDEUDA."IdCliente" = :IdCliente 
  AND FDEUDA."IdPortafolio" IN (:IdPortafolio)
  AND FDEUDA."Saldo" < 0
  AND (FDEUDA."TipoSaldo" = :TipoSaldo OR '' = :TipoSaldo)
  AND (FDEUDA."IdMoneda"  = :IdMoneda OR '' = :IdMoneda);