SELECT
	"CuentaOptimus" AS "CuentaContable",
	"NombreCuenta" AS "NombreCuentaContable"
FROM
	"BSCL.Contabilidad.BD::ITZ.HomologaCuenta"
WHERE "CuentaOptimus" = :CuentaContabilidad;