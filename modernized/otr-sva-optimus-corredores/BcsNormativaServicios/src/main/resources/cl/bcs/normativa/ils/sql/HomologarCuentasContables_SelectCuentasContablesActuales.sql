SELECT "CuentaOptimus","CuentaContabilidad","CodigoContabilidad","CuentaProveedor","CuentaEEFF",
"CentroCosto",TRIM("NombreCuenta") AS "NombreCuenta","DetalleCuenta"
FROM "BSCL.Normativa.BD::ILS.HomologaCuenta";

