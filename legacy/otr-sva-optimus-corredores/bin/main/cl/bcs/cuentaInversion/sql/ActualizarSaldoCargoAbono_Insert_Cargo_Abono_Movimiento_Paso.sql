INSERT INTO "BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso"
("IdTrabajo","IdCuentaInversion","IdCargoAbonoMovimiento","IdTipoMovimiento","Monto","Saldo","SaldoPorFechaMovimiento",
"FechaSistema","RankFechaMovimiento","FechaEfectivaMovimiento","RankFechaEfectiva","FechaCertificacion")
VALUES (
:IdTrabajo
,:IdCuentaInversion
,:IdCargoAbonoMovimiento
,:IdTipoMovimiento
,:Monto               
,:Saldo
,:SaldoPorFechaMovimiento
,:FechaSistema
,:RankFechaMovimiento
,:FechaEfectivaMovimiento
,:RankFechaEfectiva
,:FechaCertificacion);
