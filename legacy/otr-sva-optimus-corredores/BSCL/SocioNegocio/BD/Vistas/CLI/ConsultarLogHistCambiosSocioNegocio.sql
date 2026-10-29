SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogHistCambiosSocioNegocio";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogHistCambiosSocioNegocio" AS
	--Log Clientes
	SELECT 
		"IdLog",
		"FechaRegistro",
		"CodigoUsuario",
		"IdPersona",
		"IdCliente",
		"ValorAntiguo",
		"ValorNuevo",
		"CampoRegistro"
	FROM (
		SELECT
			"LOG"."IdLog",
			CAST("LOG"."FechaRegistro" AS SECONDDATE) AS "FechaRegistro",
			"LOG"."CodigoUsuario",
			"CLI"."IdPersona",
			"LOG"."IdCliente",
			"DET"."ValorAntiguo",
			"DET"."ValorNuevo",
			"DET"."CampoRegistro"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog"		        AS "LOG"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle"  AS "DET"  ON ("DET"."IdLog" = "LOG"."IdLog")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"	                AS "CLI"  ON ("CLI"."IdCliente" = "LOG"."IdCliente")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.CampoLog" 					AS "CLOG" ON ("CLOG"."CodigoCampo" = "DET"."CampoRegistro" AND PER."Tipo" = CLOG."Tipo")
		WHERE "CLOG"."AfectaFechaModificacion" = 'N' AND NOT "LOG"."IdCliente" = 0 AND "DET"."ValorAntiguo" != "DET"."ValorNuevo"
	)
	UNION ALL
	--Log Personas
	SELECT 
	"IdLog",
	"FechaRegistro",
	"CodigoUsuario",
	"IdPersona",
	"IdCliente",
	"ValorAntiguo",
	"ValorNuevo",
	"CampoRegistro"
	FROM (
		SELECT 
			"IdLog",
			"FechaRegistro",
			"CodigoUsuario",
			"IdPersona",
			"IdCliente",
			"ValorAntiguo",
			"ValorNuevo",
			"CampoRegistro"
		FROM (
			SELECT
				"LOG"."IdLog",
				CAST("LOG"."FechaRegistro" AS SECONDDATE) AS "FechaRegistro",
				"LOG"."CodigoUsuario",
				"LOG"."IdPersona",
				"LOG"."IdCliente",
				"DET"."ValorAntiguo",
				"DET"."ValorNuevo",
				"DET"."CampoRegistro"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog" 			   AS "LOG"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle" AS "DET" ON ("DET"."IdLog" = "LOG"."IdLog")
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"				   AS "CLI" ON ("CLI"."IdCliente" = "LOG"."IdCliente")
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = LOG."IdPersona")
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.CampoLog" 				   AS "CLOG" ON ("CLOG"."CodigoCampo" = "DET"."CampoRegistro" AND PER."Tipo" = CLOG."Tipo")
			WHERE "CLOG"."AfectaFechaModificacion" = 'S' AND NOT "LOG"."IdPersona" = 0 AND "DET"."ValorAntiguo" != "DET"."ValorNuevo"
		)
	)
	ORDER BY "IdLog" DESC;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogHistCambiosSocioNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 