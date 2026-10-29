SELECT
CTB_itzPreComprobanteAsiento."IdDefinicionAsiento"
,CTB_itzPreComprobanteAsiento."IdTipoContabilidad"
,CTB_itzPreComprobanteAsiento."IdCuentaContable"
,CTB_itzPreComprobanteAsiento."Ejercicio"
,CTB_itzPreComprobanteAsiento."Orden"
,CTB_itzPreComprobanteAsiento."DebeHaber"
,CTB_itzPreComprobanteAsiento."TipoEvento"
,CTB_itzPreComprobanteAsiento."Condicionado"
,CTB_itzPreComprobanteAsiento."OmiteCondicionCmbte"
,CTB_itzPreComprobanteAsiento."Estado"
,CTB_itzPreComprobanteAsiento."Formato"
,CTB_itzPreComprobanteAsiento."IdMetadataCuentaContable"
,CASE WHEN CTB_itzHomologaCuenta."CuentaOptimus" IS NULL THEN 'ERR' ELSE 'OK' END AS "Validacion"
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" CTB_itzPreComprobanteAsiento
LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTB_itzHomologaCuenta ON CTB_itzPreComprobanteAsiento."IdCuentaContable" = CTB_itzHomologaCuenta."CuentaOptimus"
WHERE CTB_itzPreComprobanteAsiento."IdTipoContabilidad" IN (:IdTipoContabilidad) AND CTB_itzPreComprobanteAsiento."Estado" = 'V'