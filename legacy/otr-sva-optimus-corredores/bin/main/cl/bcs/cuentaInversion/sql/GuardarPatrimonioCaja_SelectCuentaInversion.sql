SELECT
"IdPortafolio"
,"IdMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion"
WHERE "IdEstado" = 'ACT' AND "IdMoneda" = :IdMoneda;