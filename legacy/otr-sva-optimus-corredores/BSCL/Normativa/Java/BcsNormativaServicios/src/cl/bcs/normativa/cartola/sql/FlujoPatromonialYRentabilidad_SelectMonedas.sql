SELECT DISTINCT "IdMoneda",
	"IdCliente"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion"
WHERE "IdCuentaInversion" IN (:idsCuentaInversion);