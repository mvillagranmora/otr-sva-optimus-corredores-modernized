SELECT "IdCargoAbonoMovimiento", *
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos"
WHERE "IdComprobante" = :IdComprobante
	AND "IdEstado" = 'CRT'
	AND "Monto" = :Monto;