SELECT
CI."IdCuentaInversion"
,CI."Codigo"
,CI."IdCliente"
,CI."IdPortafolio"
,CI."IdMoneda"
,CI."CuentaInversionPrincipal"
FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI
WHERE (:FiltroMoneda = 1 OR CI."IdMoneda" IN (:IdMoneda)) ;