SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarCuentaBancaria";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarCuentaBancaria" AS
SELECT 
	"CTA"."IdCuentaBancaria",
	"CTA"."IdBanco",
	"BAN"."NombreBanco",
	"BAN"."IdPaisOperacion",
	"PAI"."NombrePais",
	'['|| "BAN"."NombreBanco" ||']'||  ' N°' || "CTA"."NumeroCuentaBancaria" AS "NombreCuentaBancariaFull",		
	"CTA"."IdTipoCuentaBancaria",
	"TIP"."NombreTipoCuentaBancaria",
	"CTA"."IdMoneda",
	"MON"."Nombre" AS "NombreMoneda",
	"CTA"."NumeroCuentaBancaria",
	"CTA"."IdEnrolamiento" as "IdEnrolamiento",
	CASE "CTA"."EsPac" WHEN '' THEN 'N' ELSE "CTA"."EsPac" END AS "EsPac",
	CASE WHEN "CTA"."Predeterminado"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Predeterminado",
	"CTA"."IdPlaza",
	"PLA"."NombrePlaza",
	"BAN"."IdTipoBanco",
	"CTA"."IdTipoIdentificadorCuenta",
	"CTA"."IdentificadorCuenta",
	"CTA"."CodigoCuentaInterbancario",
	"CTA"."CodigoAba",
	"CTA"."OtroCodigoBancoExtranjero",
	"CTA"."CodigoIban",
	"CTA"."OtroCodigoCuentaExtranjero",
	"BAN"."CodigoBanco"
FROM "BSCL.SocioNegocio.BD::ENR.CuentaBancaria" AS "CTA"
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS "TIP" on "TIP"."IdTipoCuentaBancaria" = "CTA"."IdTipoCuentaBancaria"
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "BAN" ON "CTA"."IdBanco" = "BAN"."IdBanco"
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "PAI" ON "BAN"."IdPaisOperacion" = "PAI"."IdPais"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "MON" ON ("MON"."IdMoneda" = "CTA"."IdMoneda")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Plaza" AS "PLA" ON ("PLA"."IdPlaza" = "CTA"."IdPlaza");

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarCuentaBancaria" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
