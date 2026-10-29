SELECT "IdEstado","IdCargoAbonoMovimiento","IdOperacion", *
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos"
WHERE "IdOperacion"IN (:IdOperacion)
	AND "IdConcepto"='ANTFWD'
	AND "IdEstado"='CRT';