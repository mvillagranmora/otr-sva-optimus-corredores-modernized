SELECT META."CuentaOptimus",HOM."CuentaContabilidad",HOM."NombreCuenta",HOM."DetalleCuenta",META."IdTipoDatoEvento",META."Condicion",META."Valor"
FROM "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarMetadataCuentaContable" META
INNER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" HOM ON (HOM."CuentaOptimus" = META."CuentaOptimus")
WHERE META."IdTipoDatoEvento" IN (:IdTipoDatoEvento)