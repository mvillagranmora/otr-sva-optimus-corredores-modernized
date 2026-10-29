SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgoTipoInstrumento";
GO 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgoTipoInstrumento"
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
		TCR."IdClasificacionTipoInstrumento",		
		TICR."IdTipoInstrumento"
		
FROM   "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" AS CR
INNER JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" AS TCR ON CR."IdTipoClasificacionRiesgo" = TCR."IdTipoClasificacionRiesgo"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON (TICR."IdTipoClasificacionRiesgo" = CR."IdTipoClasificacionRiesgo" AND TICR."IdEntidadClasificadoraRiesgo" = CR."IdEntidadClasificadoraRiesgo") 


GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgoTipoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

			
			