UPDATE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
	SET "IdComprobanteTesoreria" = :IdComprobanteTesoreria
WHERE "IdCargoAbonoMovimiento" = :IdCargoAbonoMovimiento AND "IdComprobanteTesoreria" = 0;

--SELECT * FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" WHERE "IdConcepto" IN ('CALFVE','CALFDI','ABLFVE','ABLFDI','ABEJ','CAEJ','ABSO','CASO')