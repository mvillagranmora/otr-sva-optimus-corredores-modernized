SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias";
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" 
AS 

	SELECT 
		"CTA"."IdCuentaBancaria",
		"CTA"."IdBanco",
		"BAN"."NombreBanco",
		"BAN"."IdPaisOperacion",   -- Ej: Santander(Chile)  Santander(Brasil)  >  Pais operación Chile y Brasil (1 registro por cada uno)
		"PAI"."NombrePais",
		'['|| "BAN"."NombreBanco" ||']'||  ' N°' || "CTA"."NumeroCuentaBancaria" AS "NombreCuentaBancariaFull",		
		"CTA"."IdTipoCuentaBancaria",
		"TIP"."NombreTipoCuentaBancaria",
		"CTA"."IdMoneda",
		"MON"."Nombre" AS "NombreMoneda",
		"CTA"."NumeroCuentaBancaria",
		"CTA"."IdPersona" as "IdPersona",
		"CTA"."FechaIngreso",
		"CTA"."FechaCreacion",
		"CTA"."FechaModificacion",
		CASE "CTA"."EsPac" WHEN '' THEN 'N' ELSE "CTA"."EsPac" END AS "EsPac",
		CASE WHEN "CTA"."Predeterminado"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Predeterminado",
		CASE WHEN "CTA"."EsFatca"='S' THEN 'Si' ELSE 'No' END AS "EsFatca",
		--"CTA"."IdEstado",
		"BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('CUENTABANCARIA',"CTA"."IdEstado","CTA"."IdEstadoAutorizacion") AS "IdEstado",
		"EST"."Nombre" as "NombreEstado",
		"CTA"."IdEstadoAutorizacion",
		"CTA"."IdPlaza",
		"PLA"."NombrePlaza",
		"BAN"."IdTipoBanco",
		"CTA"."IdTipoIdentificadorCuenta",
		"CTA"."IdentificadorCuenta",
		"CTA"."IdPersonaRelacionada",
		"CTA"."CodigoCuentaInterbancario",
		"CTA"."CodigoAba",
		"CTA"."OtroCodigoBancoExtranjero",
		"CTA"."CodigoIban",
		"CTA"."OtroCodigoCuentaExtranjero",
		"CLI"."NombreCompleto" AS "NombrePersonaRelacionada",
		"BAN"."CodigoBanco",
		"CLIE"."IdCliente"
		
		
	FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "CTA"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS "TIP" on "TIP"."IdTipoCuentaBancaria" = "CTA"."IdTipoCuentaBancaria"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "BAN" ON "CTA"."IdBanco" = "BAN"."IdBanco"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "PAI" ON "BAN"."IdPaisOperacion" = "PAI"."IdPais"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" AS "ESTSOC" ON "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('CUENTABANCARIA',"CTA"."IdEstado","CTA"."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "NombreEntidad" = 'CUENTABANCARIA'
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "ESTSOC"."IdEstado" = "EST"."IdEstado"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "MON" ON ("MON"."IdMoneda" = "CTA"."IdMoneda")
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI" ON "CTA"."IdPersonaRelacionada" = "CLI"."IdPersona"	
	LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Plaza" AS "PLA" ON ("PLA"."IdPlaza" = "CTA"."IdPlaza")
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLIE" ON "CTA"."IdPersona" = "CLIE"."IdPersona"
	
	WHERE "CTA"."Borrado"='N'
	WITH READ ONLY; 
GO 	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

