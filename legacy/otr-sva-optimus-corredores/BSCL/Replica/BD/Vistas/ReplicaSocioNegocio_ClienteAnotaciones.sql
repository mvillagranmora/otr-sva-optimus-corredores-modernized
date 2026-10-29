SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteAnotaciones";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteAnotaciones" AS
SELECT
	ANOT."IdAnotacion" AS TABLE_KEY,
	ANOT."IdCliente" AS "IDCLIENTE",
	DOC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
	CLI."IdPersona" AS "IDPERSONA",
	ANOT."IdAnotacion" AS "IDANOTACION",
	MOT."IdAtributoAnotacion" AS "IDMOTIVO",
	MOT."NombreAtributoAnotacion" AS "NOMBREMOTIVO",
	REL."IdAtributoAnotacion" AS "IDRELEVANCIA",
	REL."NombreAtributoAnotacion" AS "NOMBRERELEVANCIA",
	AMB."IdAtributoAnotacion" AS "IDAMBITO",
	AMB."NombreAtributoAnotacion" AS "NOMBREAMBITO",
	ANOT."FechaAnotacion" AS "FECHAANOTACION",
	ANOT."Titulo" AS "TITULO",
	ANOT."Detalle" AS "DETALLE",
	CASE COALESCE(ANOT."NombreArchivo",'') WHEN '' THEN 'N' ELSE 'S' END AS "EXISTEARCHIVO",
	ANOT."NombreArchivo" AS "NOMBREARCHIVO",
	ANOT."ValidoDesde" AS "VALIDODESDE",
	ANOT."ValidoHasta" AS "VALIDOHASTA",
	ANOT."Borrado" AS "BORRADO"
FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" AS ANOT
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" AS MOT ON ANOT."IdMotivoAnotacion" = MOT."IdAtributoAnotacion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" AS REL ON ANOT."IdRelevanciaAnotacion" = REL."IdAtributoAnotacion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" AS AMB ON ANOT."IdAmbitoAnotacion" = AMB."IdAtributoAnotacion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = ANOT."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteAnotaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;