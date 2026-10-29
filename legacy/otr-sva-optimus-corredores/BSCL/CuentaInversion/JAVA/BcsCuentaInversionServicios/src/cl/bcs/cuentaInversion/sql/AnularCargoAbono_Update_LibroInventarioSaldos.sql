UPDATE LIS
	SET LIS."IdEstado" = 'ANU'
FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" as CA
INNER JOIN "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos" as LIS
	ON (LIS."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" OR LIS."IdCargoAbonoMovimientoCalce" = CA."IdCargoAbonoMovimiento");