SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteLogHistorialCambios";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteLogHistorialCambios" AS
--Log Clientes
SELECT 
	"IdDetalleLog" AS TABLE_KEY,
	"IdDetalleLog" AS "IDDETALLELOG",
	"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
	"IdPersona" AS "IDPERSONA",
	"IdCliente" AS "IDCLIENTE",
	"IdLog" AS "IDLOG",
	"FechaRegistro" AS "FECHAREGISTRO",
	"CodigoUsuario" AS "USUARIO",
	"ValorAntiguo" AS "VALORANTIGUO",
	"ValorNuevo" AS "VALORNUEVO",
	"CampoRegistro" AS "CAMPOMODIFICADO"
FROM (
	SELECT
		LOG."IdLog",
		DET."IdDetalleLog",
		CAST(LOG."FechaRegistro" AS SECONDDATE) AS "FechaRegistro",
		USU."CodigoUsuario",
		CLI."IdPersona",
		LOG."IdCliente",
		DOC."DocumentoIdentidad",
		"ValorAntiguo",
		"ValorNuevo",
		DET."CampoRegistro"
	FROM "BSCL.Plataforma.BD::PLA.Log" AS "LOG"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS DET  ON (DET."IdLog" = LOG."IdLog")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = LOG."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = LOG."IdUsuario")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.CampoLog" AS CLOG ON (CLOG."CodigoCampo" = DET."CampoRegistro" AND PER."Tipo" = CLOG."Tipo")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
	WHERE CLOG."AfectaFechaModificacion" = 'N' AND NOT LOG."IdCliente" = 0 AND DET."ValorAntiguo" != DET."ValorNuevo" 
)
UNION ALL
--Log Personas
SELECT 
	"IdDetalleLog" AS TABLE_KEY,
	"IdDetalleLog" AS "IDDETALLELOG",
	"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
	"IdPersona" AS "IDPERSONA",
	"IdCliente" AS "IDCLIENTE",
	"IdLog" AS "IDLOG",
	"FechaRegistro" AS "FECHAREGISTRO",
	"CodigoUsuario" AS "USUARIO",
	"ValorAntiguo" AS "VALORANTIGUO",
	"ValorNuevo" AS "VALORNUEVO",
	"CampoRegistro" AS "CAMPOMODIFICADO"
FROM (
	SELECT 
		"IdLog",
		"IdDetalleLog",
		"FechaRegistro",
		"CodigoUsuario",
		"IdPersona",
		"IdCliente",
		"DocumentoIdentidad",
		"ValorAntiguo",
		"ValorNuevo",
		"CampoRegistro"
	FROM (
		SELECT
			LOG."IdLog",
			DET."IdDetalleLog",
			CAST(LOG."FechaRegistro" AS SECONDDATE) AS "FechaRegistro",
			USU."CodigoUsuario",
			LOG."IdPersona",
			CLI."IdCliente",
			DOC."DocumentoIdentidad",
			DET."ValorAntiguo",
			DET."ValorNuevo",
			DET."CampoRegistro"
		FROM "BSCL.Plataforma.BD::PLA.Log" AS "LOG"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS DET ON (DET."IdLog" = LOG."IdLog")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = LOG."IdPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = LOG."IdPersona")
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = LOG."IdUsuario")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.CampoLog" AS CLOG ON (CLOG."CodigoCampo" = DET."CampoRegistro" AND PER."Tipo" = CLOG."Tipo")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = LOG."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
		WHERE CLOG."AfectaFechaModificacion" = 'S' AND NOT LOG."IdPersona" = 0 AND DET."ValorAntiguo" != DET."ValorNuevo" 
	)
)
ORDER BY "IDDETALLELOG" DESC
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteLogHistorialCambios" TO  BCS_DESARROLLADOR WITH GRANT OPTION;