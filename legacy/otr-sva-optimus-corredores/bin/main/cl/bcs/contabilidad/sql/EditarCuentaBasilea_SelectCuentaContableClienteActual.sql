SELECT
	"CuentaContable" ,
	"NombreCuentaContable" 
FROM
	"BSCL.Contabilidad.BD::ITZ.InterfazCuentasBasilea"
WHERE UPPER("Concepto") = :IdConcepto AND UPPER("TipoCuentaContable") = :TipoCuentaContable;