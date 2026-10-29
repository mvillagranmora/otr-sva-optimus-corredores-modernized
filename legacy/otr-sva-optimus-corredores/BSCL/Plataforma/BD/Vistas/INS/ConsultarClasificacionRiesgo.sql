SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo";
GO 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo"
AS

SELECT   
		CR."IdTipoClasificacionRiesgo",
		CR."IdClasificacionRiesgo",
		CR."ClasificacionRiesgo",
		CR."Nivel",
		CR."IdEstado" AS "IdEstadoClasificacionRiesgo",
		CR."IdEntidadClasificadoraRiesgo",
		TCR."Codigo",
		TCR."Nombre",
		TCR."IdEstado" AS "IdEstadoTipoClasificacionRiesgo",
		TCR."IdClasificacionTipoInstrumento"
FROM   "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" AS CR
INNER JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" AS TCR ON CR."IdTipoClasificacionRiesgo" = TCR."IdTipoClasificacionRiesgo"

GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

