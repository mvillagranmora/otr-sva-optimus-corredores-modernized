upsert "BSCL.Contabilidad.BD::ITZ.HomologaCuenta"
(
	"CuentaOptimus",
	"CuentaContabilidad",
	"CodigoContabilidad",
	"CuentaProveedor",
	"CuentaEEFF",
	"CentroCosto",
	"NombreCuenta",
	"DetalleCuenta",
	"Imputable",
	"TipoCuenta"
 )
    
SELECT
	:CuentaOptimus,  
	:CuentaContabilidad, 
	:CodigoContabilidad,
	:CuentaProveedor, 
	:CuentaEEFF, 
	:CentroCosto,
	:NombreCuenta,
	:DetalleCuenta,
	:Imputable,
	:TipoCuenta
FROM DUMMY;
