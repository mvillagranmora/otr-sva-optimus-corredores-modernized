UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "IdEstado" = 'LIQ',
	"EGRIdSpotAsociado" = :IdOperacionSpot
WHERE "IdCartera" IN (:IdCartera)
  AND "IdTipoProducto" = 'MEFW';
