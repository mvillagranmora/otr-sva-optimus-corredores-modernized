WITH DatosPaso AS (
    SELECT "IdTrabajo", "IdCuentaInversion" , "FechaSaldo"
    FROM "BSCL.CuentaInversion.BD::GBL.ConsultarUltimoMovimientoCuentaInversion_Paso"
    WHERE "IdTrabajo" = :p_IdTrabajo
),
MovimientosNumerados AS (
    SELECT 
        MOV."IdCuentaInversion",
        MOV."FechaEfectivaMovimiento",
        PASO."FechaSaldo",
        ROW_NUMBER() OVER (PARTITION BY MOV."IdCuentaInversion", PASO."FechaSaldo" ORDER BY MOV."FechaEfectivaMovimiento" DESC, MOV."IdCargoAbonoMovimiento" DESC) AS RowNum
    FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
    INNER JOIN DatosPaso PASO 
        ON MOV."IdCuentaInversion" = PASO."IdCuentaInversion"
        AND MOV."FechaEfectivaMovimiento" <= PASO."FechaSaldo"
    WHERE MOV."IdEstado" = 'CRT'
)
SELECT 
    PASO."IdCuentaInversion",
    PASO."FechaSaldo",
    COALESCE(MOV."FechaEfectivaMovimiento", '0001-01-01') AS "FechaUltimoMovimiento",
    (PASO."IdTrabajo" || PASO."IdCuentaInversion" || PASO."FechaSaldo") AS "KeyUltimoMovimiento"
FROM DatosPaso PASO
LEFT JOIN MovimientosNumerados MOV
    ON PASO."IdCuentaInversion" = MOV."IdCuentaInversion"
    AND PASO."FechaSaldo" = MOV."FechaSaldo"
    AND MOV.RowNum = 1;


