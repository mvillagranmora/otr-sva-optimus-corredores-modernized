SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCRSPaisResidencia";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCRSPaisResidencia" AS
SELECT 
	PRF."IdPaisResidenciaFiscal" AS TABLE_KEY, 
	DOC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
	PRF."IdPersona" AS "IDPERSONA",
	PRF."IdPaisResidenciaFiscal" AS "IDPAISRESIDENCIAFISCAL",
	PRF."IdPais" AS "IDPAIS",
	PAIS."NombrePais" AS "NOMBREPAIS",
	PRF."FechaInicioTributacionPais" AS "FECHAINICIOTRIBUTACION",
	PRF."IdEstado" AS "IDESTADO",
	EST."Nombre" AS "NOMBREESTADO"
FROM "BSCL.SocioNegocio.BD::PER.PaisResidenciaFiscal" AS PRF
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON PAIS."IdPais" = PRF."IdPais"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = PRF."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = PRF."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as EST on PRF."IdEstado" = EST."IdEstado" 
WHERE PRF."IdEstado"='ACT'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCRSPaisResidencia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;