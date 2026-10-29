SELECT
	"IdDetalleIndicador"		AS "IdDetalleIndicador"
	, "CodigoTipoIndicador"		AS "CodigoTipoIndicador"
	, "Codigo"					AS "CodigoValor"
	, "DetalleIndicador"		AS "DetalleValor"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" VistaDetalleTipoIndicador
WHERE VistaDetalleTipoIndicador."CodigoTipoIndicador" IN (:TipoIndicadorCodigos)
	AND VistaDetalleTipoIndicador."IdEmpresa" = :IdEmpresa;