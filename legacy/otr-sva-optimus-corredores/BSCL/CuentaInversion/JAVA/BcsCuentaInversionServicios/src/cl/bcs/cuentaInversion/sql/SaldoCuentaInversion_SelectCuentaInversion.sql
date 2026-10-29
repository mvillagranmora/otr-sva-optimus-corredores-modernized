SELECT
CI."IdCuentaInversion"
,CI."Codigo"
,CI."IdCliente"
,CI."IdPortafolio"
,CI."IdMoneda"
,CI."CuentaInversionPrincipal"
FROM "BSCL.CuentaInversion.BD::GBL.SaldoCuentaInversion_Proceso_Paso" PA
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON PA."IdPortafolio" = CI."IdPortafolio"
WHERE CI."IdPortafolio" IS NOT NULL AND (:FiltroMoneda = 1 OR CI."IdMoneda" IN (:IdMoneda) )
AND PA."IdTrabajo" = :IdTrabajo;
