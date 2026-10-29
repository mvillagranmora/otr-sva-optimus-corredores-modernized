SELECT
	"IdTipoCliente"
FROM
	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
WHERE
	"IdCliente" = :p_IdEntidadBase;