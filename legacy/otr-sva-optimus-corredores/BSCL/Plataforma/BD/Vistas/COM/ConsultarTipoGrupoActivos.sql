SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoGrupoActivos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoGrupoActivos"  AS

SELECT 
	*
FROM
	"BSCL.Plataforma.BD::COM.TipoGrupoActivos";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoGrupoActivos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
