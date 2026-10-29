UPDATE "BSCL.Normativa.BD::ILS.ItemConfiguracion"
SET "SaldoDebeHaber" = :SaldoDebeHaber,
	"Condicion" = :Condicion,
	"Monto" = :Monto,
	"Operacion" = :Operacion,
	"Factor" = :Factor
WHERE "IdConfiguracionItem" = :IdConfiguracionItem;