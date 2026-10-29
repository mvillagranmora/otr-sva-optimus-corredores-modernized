SELECT 
	CAST("IdInstrumento" AS NVARCHAR(255)) AS IdInstrumento,
	"IdEstado"
FROM "BSCL.Plataforma.BD::COM.DetalleGrupoActivos"
WHERE "IdDetalleGrupoActivos" = :IdDetalleGrupoActivos
