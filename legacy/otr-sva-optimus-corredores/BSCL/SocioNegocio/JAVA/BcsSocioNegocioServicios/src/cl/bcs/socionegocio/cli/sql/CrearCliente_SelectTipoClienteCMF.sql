SELECT 
	"IdDetalleIndicador" AS "IdDetalleIndicador",
	"DetalleIndicador" AS "DetalleIndicador" 
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoClienteCmf"
			WHERE "IdDetalleIndicador" IN (:IdDetalleIndicador)