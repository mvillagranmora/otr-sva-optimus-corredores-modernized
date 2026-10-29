SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoMensaje";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoMensaje" AS
SELECT
	"IdTipoMensaje" AS "IdTipoMensaje",
	"NombreTipoMensaje" AS "NombreTipoMensaje"
FROM "BSCL.Plataforma.BD::EVT.TipoMensaje";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoMensaje" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
 
