INSERT INTO #ActualizarSaldoCargoAbono_CuentaInversion(
	"IdCuentaInversion"
) 
SELECT "IdCuentaInversion"
FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion"
WHERE "IdEstado" = 'ACT';