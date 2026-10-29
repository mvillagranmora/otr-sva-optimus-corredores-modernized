SELECT
	"cue"."IdCuentaBancaria" AS "IdCuentaBancaria",
	"p"."IdPersona" AS "IdPersona",
	CASE WHEN LENGTH("NOM"."NombreCompleto") > 100 THEN LEFT("NOM"."NombreCompleto", 100) ELSE "NOM"."NombreCompleto" END AS "NombreCompleto",
	"di"."DocumentoIdentidad" AS "DocumentoIdentidad",
	"di"."IdTipoDocumentoIdentidad" AS "TipoDocumentoIdentidad",
	COALESCE("cue"."NumeroCuentaBancaria", '') AS "NumeroCuentaBancaria",
	COALESCE("ban"."NombreBanco", '') AS "NombreBanco",
	COALESCE("mone"."Nombre", '') AS "NombreMoneda",
	COALESCE("ticue"."NombreTipoCuentaBancaria", '') AS "NombreTipoCuentaBancaria",
	COALESCE("cue"."EsPac", '') AS "EsPac"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" "cue"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" "p"
	ON ("p"."IdPersona" = "cue"."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" "NOM"
	ON ("p"."IdPersona" = "NOM"."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "di"
	ON ("p"."IdDocumentoIdentidad" = "di"."IdDocumentoIdentidad")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" "ban"
	ON ("ban"."IdBanco" = "cue"."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" "mone"
	ON ("cue"."IdMoneda" = "mone"."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" "ticue"
	ON ("ticue"."IdTipoCuentaBancaria" = "cue"."IdTipoCuentaBancaria")
WHERE "cue"."IdCuentaBancaria" IN (:IdCuentaBancaria)
