SET SCHEMA "BSCL";
 GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegalCorredor";
 GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegalCorredor"
AS
SELECT  
	"IdPersonaOrigen" 			AS "IdPersonaRepresentado"
	
	,"IdPersonaDestino" 		AS "IdPersonaRepresentante"
	,"NombreCompletoDestino"	AS "NombreCompletoRepresentante"
	,"IdEstado" 				AS "IdEstadoRelacion"
	FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" Relacion 
WHERE "IdTipoRelacion" IN ('REPAD','REP');

GO	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegalCorredor" TO  BCS_DESARROLLADOR WITH GRANT OPTION;