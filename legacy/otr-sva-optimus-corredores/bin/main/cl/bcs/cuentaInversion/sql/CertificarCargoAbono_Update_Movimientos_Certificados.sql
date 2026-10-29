UPDATE CBM
SET CBM."IdUsuarioCertificador"      = :p_IdUsuario,
    CBM."FechaEfectivaMovimiento"    = :p_FechaCalculada,
    CBM."FechaCertificacion"         = :p_FechaHoraSistema,
    CBM."SaldoLibroInventario"       = :p_Monto,
    CBM."IdEstado"                   = 'CRT'     
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CBM
WHERE CBM."IdCargoAbonoMovimiento" = :p_IdCargoAbonoMovimiento
AND CBM."IdEmpresa" = :p_IdEmpresa;