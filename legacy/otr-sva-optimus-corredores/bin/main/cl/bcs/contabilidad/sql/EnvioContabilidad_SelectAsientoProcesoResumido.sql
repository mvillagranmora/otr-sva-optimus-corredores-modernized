SELECT
A."IdPreComprobante"
,A."MontoDebe"
,A."MontoHaber"
,A."Glosa"
,substring(C."NombreCuenta", 1, 50) AS "GlosaDetalle"
,CASE WHEN IFNULL(C."CentroCosto", '0') = '0' THEN '' ELSE C."CentroCosto" END AS "CentroCosto"
,C."NombreCuenta"
,C."CodigoContabilidad"
,C."CuentaContabilidad"
,A."FechaContable"
,A."IdCuentaContable"
,P."IdTipoContabilidad"
FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS A
INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS P ON P."IdPreComprobante" = A."IdPreComprobante"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" C ON A."IdCuentaContable" = C."CuentaOptimus"
WHERE A."IdPreComprobante" IN (:IdPrecomprobante)
AND (A."MontoDebe" > 0
OR A."MontoHaber" > 0);