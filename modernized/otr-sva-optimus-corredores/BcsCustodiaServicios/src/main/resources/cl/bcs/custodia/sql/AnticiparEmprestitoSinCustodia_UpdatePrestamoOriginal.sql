UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "Cantidad" = :Cantidad,
	"INGCantidad" = :Cantidad,
	"INGMontoATasaCompraActual" = :MontoPrestamo
WHERE "FolioCartera" = :FolioPrestamo