upsert "BSCL.Normativa.BD::ILS.HomologaCuenta"
(
	"CuentaOptimus",
	"CuentaContabilidad",
	"CodigoContabilidad",
	"CuentaProveedor",
	"CuentaEEFF",
	"CentroCosto",
	"NombreCuenta",
	"DetalleCuenta"
 )
    
SELECT
	:CuentaOptimus,  
	:CuentaContabilidad, 
	:CodigoContabilidad,
	:CuentaProveedor, 
	:CuentaEEFF, 
	:CentroCosto,
	:NombreCuenta,
	:DetalleCuenta
FROM DUMMY;
