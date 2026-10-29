UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET
	"MontoImpuestoExtranjero" = :MontoImpuestoExtranjero,
	"MontoGastoTransferencia" = :MontoGastoTransferencia,
	"Total"					  = :Total
WHERE "IdEventoCapitalCliente" = :IdEventoCapitalClienteCnt;