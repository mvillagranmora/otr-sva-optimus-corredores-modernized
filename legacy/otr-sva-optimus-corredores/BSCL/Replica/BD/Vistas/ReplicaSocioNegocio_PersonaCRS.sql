SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCRS";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCRS" AS
SELECT
	FATCA."IdPersona" AS TABLE_KEY,
	DOC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
	FATCA."IdPersona" AS "IDPERSONA",
	FATCA."DeclaracionCRS" AS "DECLARACIONCRS",
	PER."ResidenciaTributariaExtranjera" AS "RESIDENCIATRIBUTARIAEXTRANJERA"
FROM "BSCL.SocioNegocio.BD::PER.Fatca" AS FATCA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = FATCA."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = FATCA."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
WHERE FATCA."Borrado" = 'N'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCRS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
