SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarLogCambioFatca";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarLogCambioFatca" AS
	SELECT  DISTINCT
		CAST(LOG."FechaRegistro" AS SECONDDATE) as "FechaRegistro",
		LOG."CodigoUsuario",
		LOG."IdRegistro",
		DET."ValorAntiguo",
		DET."ValorNuevo",
		DET."CampoRegistro"
	FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog" LOG 
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle" DET ON DET."IdLog"=LOG."IdLog"
	WHERE LOG."IdTipoLog"='MODIFICACION' AND LOG."IdTipoRegistro"='FATCA' AND DET."CampoRegistro" NOT IN ('DeclaracionCRS')
WITH READ ONLY; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarLogCambioFatca" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
