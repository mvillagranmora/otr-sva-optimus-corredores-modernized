SELECT 
	"IdValorDefectoSistema", 
	"Nombre", 
	"Valor", 
	"IdEmpresa", 
	"IdUsuarioCreador", 
	TO_NVARCHAR("FechaCreacion") AS "FechaCreacion", 
	TO_NVARCHAR("FechaModificacion") AS "FechaModificacion", 
	"IdEstado",
	"TipoEntidad",
	"IdTipoCanalIngreso"
FROM "BSCL.Plataforma.BD::PLA.ValorDefectoSistema" 
WHERE "IdEmpresa" = :IdEmpresa 
AND (:TipoEntidad IS NULL OR :TipoEntidad = '' OR "TipoEntidad" = :TipoEntidad) 
AND (:IdTipoCanalIngreso IS NULL OR :IdTipoCanalIngreso = '' OR "IdTipoCanalIngreso" = :IdTipoCanalIngreso);