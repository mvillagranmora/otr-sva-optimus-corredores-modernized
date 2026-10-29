SELECT "CuentaOptimus","CuentaContabilidad","CodigoContabilidad","CuentaProveedor","CuentaEEFF",
"CentroCosto",TRIM("NombreCuenta") AS "NombreCuenta","DetalleCuenta","Imputable"
FROM "BSCL.Contabilidad.BD::ITZ.HomologaCuenta";