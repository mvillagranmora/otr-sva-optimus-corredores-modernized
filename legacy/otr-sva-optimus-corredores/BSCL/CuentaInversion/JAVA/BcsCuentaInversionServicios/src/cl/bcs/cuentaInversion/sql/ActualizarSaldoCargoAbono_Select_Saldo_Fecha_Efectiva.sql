WITH 
MovimientoAnterior as (
                 SELECT MOV."IdCuentaInversion", MOV."Saldo" as "SaldoEfectivoAnterior",FMAX."RankEfectivoAnterior"
                   FROM "BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso" as MOV,                
                        (SELECT "IdCuentaInversion", MAX("RankFechaEfectiva") as "RankEfectivoAnterior"
                          FROM "BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso"
                          WHERE "FechaEfectivaMovimiento" < :p_FechaMovimiento
                          GROUP BY "IdCuentaInversion"
                         ) FMAX
                  WHERE FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
                    AND FMAX."RankEfectivoAnterior" = MOV."RankFechaEfectiva"
              )
SELECT CBM."IdCargoAbonoMovimiento",CBM."IdCuentaInversion",CBM."RankFechaEfectiva",MOVANT."RankEfectivoAnterior",MOVANT."SaldoEfectivoAnterior",CBM."Monto"
      ,COALESCE(MOVANT."SaldoEfectivoAnterior",0) + 
       COALESCE(SUM(CBM."Monto") over (PARTITION BY CBM."IdCuentaInversion" 
                                                      ORDER BY CBM."FechaEfectivaMovimiento", CBM."FechaCertificacion", CBM."IdCargoAbonoMovimiento" asc 
                                                       ROWS BETWEEN unbounded preceding and current row),0) as "SaldoEfectivo"
FROM "BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso" as CBM
LEFT JOIN MovimientoAnterior as MOVANT ON (MOVANT."IdCuentaInversion" = CBM."IdCuentaInversion")
WHERE CBM."FechaEfectivaMovimiento" >= :p_FechaMovimiento;