SELECT
	"RazonSocial",
	"Direccion",
	"RUT",
	"Telefono"
FROM "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales"
WHERE "IdEmpresa" = :IdEmpresa