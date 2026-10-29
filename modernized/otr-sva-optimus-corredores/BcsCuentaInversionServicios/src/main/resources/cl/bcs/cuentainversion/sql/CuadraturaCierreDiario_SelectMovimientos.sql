SELECT
"IdCargoAbonoMovimiento"
,"NombreCliente"
,"Proposito"
,"NombreConcepto"
,"IdMoneda"
,"Monto"
,"IdEstado"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos"
WHERE "FechaSistema" = :FechaLiquidacion