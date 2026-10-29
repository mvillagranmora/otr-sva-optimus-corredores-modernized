SELECT 
	"DescripcionGrupoActivos",
	"IdTipoGrupoActivos",
	"IdEstado",
	"PorDefecto"
FROM "BSCL.Plataforma.BD::COM.GrupoActivos"
WHERE "IdGrupoActivos" = :IdGrupoActivos
