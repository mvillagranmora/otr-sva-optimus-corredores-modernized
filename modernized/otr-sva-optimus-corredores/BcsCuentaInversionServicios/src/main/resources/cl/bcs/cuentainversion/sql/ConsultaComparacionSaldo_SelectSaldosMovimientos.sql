WITH SaldoCtaInversion AS (SELECT SaldosFinal."IdCuentaInversion",
                                  Max(SaldosFinal."IdCargoAbonoMovimiento") AS "IdCargoAbonoMovimiento"
                           FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS SaldosFinal
                             INNER JOIN "BSCL.CuentaInversion.BD::CTA.Saldo_Cuenta_Proceso" AS SaldoCuentaProc
                               ON SaldoCuentaProc."IdCuentaInversion" = SaldosFinal."IdCuentaInversion"
                             INNER JOIN (SELECT "IdCuentaInversion",
                                                Max("IdCargoAbonoMovimiento") AS "IdCargoAbonoMovimiento",
                                                Max("FechaSistema") AS "FechaSistema"
                                         FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS ConsCarAbo 
                                         GROUP BY "IdCuentaInversion") AS SaldosFecha
                                ON SaldosFinal."IdCargoAbonoMovimiento" = SaldosFecha."IdCargoAbonoMovimiento"
                               AND SaldosFinal."FechaSistema" = SaldosFecha."FechaSistema" 
                               AND SaldosFecha."IdCuentaInversion" = SaldosFinal."IdCuentaInversion"
                           WHERE "IdEstado" = 'CRT'    
                           GROUP BY SaldosFinal."IdCuentaInversion") 
SELECT ConsCarAbon."IdCuentaInversion",
       ConsCarAbon."IdMoneda",
       ConsCarAbon."FechaEfectivaMovimiento",
       ConsCarAbon."CodigoCuentaInversion",
       ConsCarAbon."NombreCuentaInversion",
       ConsCarAbon."CodigoNombreMoneda",
       ConsCarAbon."DocumentoIdentidad",
       ConsCarAbon."NombreCliente",
       ConsCarAbon."CodigoPortafolio",
       ConsCarAbon."IdCargoAbonoMovimiento",
       ConsCarAbon."Proposito",
       ConsCarAbon."Saldo",
       ConsCarAbon."SaldoPorFechaMovimiento" AS "SaldoMovimiento",
       SaldoCuentaProc."SaldoAnterior" AS "SaldoCuentaInversion"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS ConsCarAbon
  INNER JOIN SaldoCtaInversion AS SaldoMovimiento
     ON SaldoMovimiento."IdCuentaInversion" = ConsCarAbon."IdCuentaInversion"
    AND SaldoMovimiento."IdCargoAbonoMovimiento" =  ConsCarAbon."IdCargoAbonoMovimiento"
  INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS ConsCartMvto
    ON ConsCartMvto."IdCuentaInversion" = SaldoMovimiento."IdCuentaInversion"
   AND ConsCartMvto."IdCargoAbonoMovimiento" = SaldoMovimiento."IdCargoAbonoMovimiento"
  INNER JOIN "BSCL.CuentaInversion.BD::CTA.Saldo_Cuenta_Proceso" AS SaldoCuentaProc
    ON SaldoCuentaProc."IdCuentaInversion" = ConsCarAbon."IdCuentaInversion"
   AND SaldoCuentaProc."SaldoAnterior" <> ConsCarAbon."SaldoPorFechaMovimiento"
WHERE ConsCarAbon."FechaEfectivaMovimiento" < :FechaOperacionDesde 