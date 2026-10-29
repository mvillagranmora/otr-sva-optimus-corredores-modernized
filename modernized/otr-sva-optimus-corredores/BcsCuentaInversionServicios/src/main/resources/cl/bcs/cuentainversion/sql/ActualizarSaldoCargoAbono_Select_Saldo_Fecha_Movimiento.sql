WITH 
MovimientoAnterior as (
           SELECT MOV."IdCuentaInversion", MOV."SaldoPorFechaMovimiento" as "SaldoMovimientoAnterior",FMAX."RankMovimientoAnterior"
           FROM "BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso" as MOV,                
                (SELECT "IdCuentaInversion", MAX("RankFechaMovimiento") as "RankMovimientoAnterior"
                    FROM "BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso"
                         WHERE "FechaSistema" < :p_FechaMovimiento
                         GROUP BY "IdCuentaInversion"
                 ) FMAX
                 WHERE FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
                 AND FMAX."RankMovimientoAnterior" = MOV."RankFechaMovimiento"
         )       
SELECT  CBM."IdCargoAbonoMovimiento",CBM."IdCuentaInversion",CBM."RankFechaMovimiento",MOVANT."RankMovimientoAnterior",MOVANT."SaldoMovimientoAnterior",CBM."Monto"
        ,COALESCE(MOVANT."SaldoMovimientoAnterior",0) + 
        COALESCE(SUM(CBM."Monto") over (PARTITION BY CBM."IdCuentaInversion" 
        					
                                                  
                                                  ORDER BY CBM."FechaSistema", CBM."IdCargoAbonoMovimiento" asc 
                                                  
                                                  ROWS BETWEEN unbounded preceding and current row),0) as "SaldoMovimiento"
FROM "BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso" as CBM
     LEFT JOIN MovimientoAnterior as MOVANT ON (MOVANT."IdCuentaInversion" = CBM."IdCuentaInversion")
WHERE CBM."FechaSistema" >= :p_FechaMovimiento;