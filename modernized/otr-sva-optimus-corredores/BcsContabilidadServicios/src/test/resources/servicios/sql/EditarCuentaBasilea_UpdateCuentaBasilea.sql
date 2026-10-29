UPDATE "BSCL.Contabilidad.BD::ITZ.InterfazCuentasBasilea"
SET "CuentaContable" = :CuentaContable,
	"NombreCuentaContable" = :NombreCuentaContable,
	"IdUsuarioModifica"  = :IdUsuario,
	"FechaModificacionRegistro" = :FechaSistema,
	"FechaModificacionSistema" = CURRENT_UTCTIMESTAMP
WHERE UPPER("Concepto") = :Concepto AND UPPER("TipoCuentaContable") = :TipoCuentaContable;