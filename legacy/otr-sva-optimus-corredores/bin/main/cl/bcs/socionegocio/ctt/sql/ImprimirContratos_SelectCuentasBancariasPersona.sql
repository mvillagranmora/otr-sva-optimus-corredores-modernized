WITH CTE_VCB AS (
	SELECT CB."IdPersona"						AS "IdPersona"
		, ROW_NUMBER() OVER (
			PARTITION  BY CB."IdPersona"
			ORDER BY CB."IdCuentaBancaria"
		)										AS "OrdenRegistro"
		, CB."IdMoneda"							AS "IdMoneda"
		, BAN."NombreBanco"						AS "NombreBanco"
		, PER."DocumentoIdentidad" 				AS "DocumentoIdentidadBanco"
		, MON."Nombre"							AS "NombreMoneda"
		, MON."Simbolo" 						AS "SimboloMoneda"
		, TCB."NombreTipoCuentaBancaria"		AS "NombreTipoCuentaBancaria"
		, CB."NumeroCuentaBancaria"				AS "NumeroCuentaBancaria"
		, PAI."CodigoPais"						AS "CodigoPais"
		, PAI."NombrePais" 						AS "NombrePais"
		, PLA."NombrePlaza" 					AS "Sucursal"
		, CB."EsFatca" 							AS "EsFatca"
	FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CB
		 INNER JOIN "BSCL.Plataforma.BD::BAN.Banco" BAN ON (BAN."IdBanco" = CB."IdBanco")
		 INNER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TCB ON (TCB."IdTipoCuentaBancaria" = CB."IdTipoCuentaBancaria")
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = BAN."IdPersona")
		 LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON ON (MON."IdMoneda" = CB."IdMoneda")
		 LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PAI ON (PAI."IdPais" = BAN."IdPaisOperacion")
		 LEFT JOIN "BSCL.Plataforma.BD::BAN.Plaza" PLA ON (PLA."IdPlaza" = CB."IdPlaza")
    WHERE CB."IdPersona" IN (:PersonaIds)
    	AND CB."IdEstado" = 'ACT'
    	AND CB."Borrado" = 'N'
)
SELECT VCB."IdPersona"					AS "IdPersona"
	 , VCB."OrdenRegistro"				AS "OrdenRegistro"
	 , VCB."NombreBanco"				AS "NombreBanco"
	 , VCB."DocumentoIdentidadBanco" 	AS "DocumentoIdentidadBanco"
	 , VCB."NombreTipoCuentaBancaria"	AS "NombreTipoCuentaBancaria"
	 , VCB."NumeroCuentaBancaria"		AS "NumeroCuentaBancaria"
	 , VCB."IdMoneda"					AS "IdMoneda"
	 , VCB."NombreMoneda"				AS "NombreMoneda"
	 , VCB."SimboloMoneda" 				AS "SimboloMoneda"
	 , VCB."CodigoPais"					AS "CodigoPais"
	 , VCB."NombrePais"					AS "NombrePais"
	 , VCB."Sucursal"					AS "Sucursal"
	 , COALESCE(VCB."EsFatca",'')		AS "EsFatca"
FROM CTE_VCB VCB
WHERE VCB."OrdenRegistro" <= 5;