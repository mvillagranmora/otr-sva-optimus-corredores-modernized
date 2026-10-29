UPDATE CAM
SET CAM."SaldoLibroInventario" = (CAM."SaldoLibroInventario" + LIS."MontoCalce")
FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" as CA			 		  
INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarLibroInventarioDeSaldos" as LIS
	ON (LIS."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" OR LIS."IdCargoAbonoMovimientoCalce" = CA."IdCargoAbonoMovimiento")
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CAM
	ON CAM."IdCargoAbonoMovimiento" = CASE WHEN LIS."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" THEN LIS."IdCargoAbonoMovimientoCalce" 
									   WHEN LIS."IdCargoAbonoMovimientoCalce" = CA."IdCargoAbonoMovimiento" THEN LIS."IdCargoAbonoMovimiento" END
	AND CAM."IdEstado" <> 'ANU';