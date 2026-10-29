SELECT
	"IdConfiguracionItem",
	"IdItem",
	"IdProcesoFuncion",
	"CuentaContable",
	"CuentaOptimus",
	"SaldoDebeHaber",
	"Condicion",
	"Monto",
	"Operacion",
	"Factor",
	"MayorACero",
	"MenorACero",
	"PermiteCero",
	"Estado",
	"TipoItem"
FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" 
WHERE "IdItem" = :IdItem;