UPDATE "BSCL.Custodia.BD::GTA.Garantia"
SET 
	"IdEstado"= COALESCE(:IdEstado,"IdEstado")
WHERE "IdGarantia" = :IdGarantia;