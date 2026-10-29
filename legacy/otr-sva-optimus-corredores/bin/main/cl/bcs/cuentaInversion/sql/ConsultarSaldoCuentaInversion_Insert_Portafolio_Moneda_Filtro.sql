INSERT INTO "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso"
("IdTrabajo","IdEjecutivo","IdPortafolio","IdMoneda")
SELECT :IdTrabajo,null AS "IdEjecutivo", c."IdPortafolio", cc."IdMoneda" 
FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" c
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" cc ON cc."IdPortafolio" = c."IdPortafolio" AND cc."IdMoneda" = 'CLP'
WHERE c."IdMoneda" = 'USD' OR c."IdMoneda" = 'EUR';