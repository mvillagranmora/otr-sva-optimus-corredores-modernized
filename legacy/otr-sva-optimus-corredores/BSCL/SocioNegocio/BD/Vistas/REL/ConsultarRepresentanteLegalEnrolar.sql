SET SCHEMA "BSCL";
 GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegalEnrolar";
 GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegalEnrolar"
AS
SELECT  
	 ENR."IdEnrolamiento" 		AS "IdPersonaRepresentado"	
	,REL."IdPersonaDestino" 	AS "IdPersonaRepresentante"
	,PER."NombreCompleto"		AS "NombreCompletoRepresentante"
	,'ACT'						AS "IdEstadoRelacion"
	FROM  "BSCL.SocioNegocio.BD::ENR.Enrolamiento"	ENR
	INNER JOIN "BSCL.SocioNegocio.BD::ENR.RelacionPersona" REL ON (REL."IdEnrolamientoOrigen" = ENR."IdEnrolamiento")
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = REL."IdPersonaDestino")
WHERE REL."IdTipoRelacion" IN ('REPAD','REP') ;

GO	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegalEnrolar" TO  BCS_DESARROLLADOR WITH GRANT OPTION;