SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaLogCambios";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaLogCambios" AS
SELECT 
	DET."IdDetalleLog" AS TABLE_KEY,
	DET."IdDetalleLog" AS "IDDETALLELOG",
	LOG."IdRegistro" AS "IDPERSONA",
	LOG."IdLog" AS "IDLOG",
	DOC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
	CAST(LOG."FechaRegistro" AS SECONDDATE) AS "FECHAREGISTRO",
	USR."CodigoUsuario" AS "USUARIO",
	DET."ValorAntiguo" AS "VALORANTIGUO",
	DET."ValorNuevo" AS "VALORNUEVO",
	DET."CampoRegistro" AS "CAMPOMODIFICADO"
FROM "BSCL.Plataforma.BD::PLA.Log" AS LOG
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USR ON USR."IdUsuario" = LOG."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS DET ON DET."IdLog" = LOG."IdLog"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = LOG."IdRegistro" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
WHERE LOG."IdTipoLog" = 'MODIFICACION' AND LOG."IdTipoRegistro" = 'FATCA' AND DET."CampoRegistro" NOT IN ('DeclaracionCRS')
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaLogCambios" TO  BCS_DESARROLLADOR WITH GRANT OPTION;