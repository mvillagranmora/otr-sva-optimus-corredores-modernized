UPDATE "BSCL.Plataforma.BD::ENC.DefinicionEncuesta"
SET 
	"Nombre" = COALESCE(:Nombre,"Nombre"), 
	"IdEstado" = COALESCE(:IdEstado,"IdEstado")
WHERE "IdDefinicionEncuesta" = :IdDefinicionEncuesta
