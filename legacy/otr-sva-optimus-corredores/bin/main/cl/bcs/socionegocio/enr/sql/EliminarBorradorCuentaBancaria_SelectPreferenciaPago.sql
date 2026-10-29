SELECT
 CAST(0 AS BIGINT) AS "IdInterno",
 "IdPreferenciaPagoPortafolio" ,
 "IdEnrolamiento",
 "IdPreferenciaPago",
 "IdPersonaRelacionada",
 "IdCuentaBancaria",
 "IdEmpresa",
 "IdTarjetaBancaria",
 "PorDefecto"
FROM "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio"
WHERE "IdCuentaBancaria" IN (:idsCuentasBancarias)