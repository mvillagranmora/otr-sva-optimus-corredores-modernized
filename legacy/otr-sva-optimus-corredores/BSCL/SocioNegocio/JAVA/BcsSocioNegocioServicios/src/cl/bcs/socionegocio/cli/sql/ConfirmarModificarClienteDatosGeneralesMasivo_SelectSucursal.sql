SELECT 
	"IdSucursal",
	"NombreSucursal"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal"
		WHERE "IdSucursal" IN (:IdSucursal)