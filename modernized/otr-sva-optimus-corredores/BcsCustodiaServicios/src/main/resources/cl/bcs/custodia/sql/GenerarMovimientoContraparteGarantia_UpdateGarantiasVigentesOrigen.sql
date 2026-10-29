UPDATE "BSCL.Custodia.BD::GTA.Garantia"
SET 
	"IdEstado" = 'RPA'
WHERE "IdGarantia" = :IdGarantia AND "IdEstado" != 'LIQ';