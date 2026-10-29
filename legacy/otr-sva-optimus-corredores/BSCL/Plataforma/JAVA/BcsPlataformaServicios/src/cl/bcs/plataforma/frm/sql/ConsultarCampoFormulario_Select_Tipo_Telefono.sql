SELECT
	"IdTipoTelefono"
	,"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoTelefono"
WHERE "IdTipoTelefono" in (:IdsTipoTelefono);