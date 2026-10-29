SELECT  
	 BAN."CodigoBanco"
	,BAN."IdBanco"
	,CTA."IdCuentaBancaria"
	,CTA."NumeroCuentaBancaria" 
FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SON"
INNER JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "CTA"  ON "CTA"."IdPersona" = "SON"."IdPersona"
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS "TIP" on "TIP"."IdTipoCuentaBancaria" = "CTA"."IdTipoCuentaBancaria"
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "BAN" ON ("CTA"."IdBanco" = "BAN"."IdBanco" ) 
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "PAI" ON "BAN"."IdPaisOperacion" = "PAI"."IdPais"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" AS "ESTSOC" ON "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('CUENTABANCARIA',"CTA"."IdEstado","CTA"."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "NombreEntidad" = 'CUENTABANCARIA'
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "ESTSOC"."IdEstado" = "EST"."IdEstado"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "MON" ON ("MON"."IdMoneda" = "CTA"."IdMoneda")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI" ON "CTA"."IdPersonaRelacionada" = "CLI"."IdPersona" 	
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Plaza" AS "PLA" ON ("PLA"."IdPlaza" = "CTA"."IdPlaza")

WHERE "CTA"."Borrado"='N' AND "CTA"."IdBanco" = :IdBanco AND "SON"."IdCliente" = :IdCliente;
