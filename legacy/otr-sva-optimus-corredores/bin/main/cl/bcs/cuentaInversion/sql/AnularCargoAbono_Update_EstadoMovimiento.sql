UPDATE CAM
SET CAM."IdEstado" = 'ANU'
FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" as CA
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CAM
ON CAM."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" AND CAM."IdEmpresa" = :p_IdEmpresa;
