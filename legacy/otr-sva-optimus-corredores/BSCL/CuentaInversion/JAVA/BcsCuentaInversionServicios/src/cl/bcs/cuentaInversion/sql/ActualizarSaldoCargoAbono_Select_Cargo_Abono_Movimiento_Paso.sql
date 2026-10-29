SELECT  B."IdCuentaInversion"
       ,B."IdCargoAbonoMovimiento"
       ,B."IdTipoMovimiento"
       ,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"                
       ,B."Saldo"
       ,B."SaldoPorFechaMovimiento"
       ,B."FechaSistema"
       
       
       ,RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."IdCargoAbonoMovimiento") as "RankFechaMovimiento"
       
       
       ,B."FechaEfectivaMovimiento"
       ,RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaEfectivaMovimiento",B."FechaCertificacion",B."IdCargoAbonoMovimiento") as "RankFechaEfectiva"
       ,B."FechaCertificacion"
FROM #ActualizarSaldoCargoAbono_CuentaInversion AS A
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS B ON B."IdCuentaInversion" = A."IdCuentaInversion"
WHERE B."IdEstado" = 'CRT';