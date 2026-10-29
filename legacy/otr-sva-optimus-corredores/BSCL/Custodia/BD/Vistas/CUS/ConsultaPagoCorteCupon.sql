SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaPagoCorteCupon"; 

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaPagoCorteCupon"
AS 


-----------------------------------------------------------------------------------------------------------------------
-- COMPROBANTES:  FACTURA - FACTURA ELECTRONICA - NOTA DE CREDITO - NOTE DE CREDITO ELECTRONICA
-----------------------------------------------------------------------------------------------------------------------
SELECT 
		CA."FolioCartera"											AS "FolioCartera",
		CA."FechaOperacion"											AS "FechaOperacion",
		CA."FechaVencimientoOperacion"								AS "FechaVencimientoOperacion",
		CA."IdCliente"												AS "IdCliente",
		CA."IdPortafolio"											AS "IdPortafolio",
		CA."DocumentoIdentidad"										AS "DocumentoIdentidad",
		CA."RazonSocial"											AS "RazonSocial",
		CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento",
		CA."IdTipoInstrumento"										AS "IdTipoInstrumento",
		CA."RazonSocialInstrumento"									AS "RazonSocialInstrumento",
		CA."Nemotecnico"											AS "Nemotecnico",
		CA."IdEmisor"												AS "IdEmisor",
		CA."MontoCompra"											AS "MontoCompra",
		CA."INGMontoCapitalCorteCupon"								AS "MontoAmortizacion",
		CA."INGInteresesPagoCorteCupon"								AS "MontoIntereses",
		CA."INGFechaPagoCorteCupon"									AS "INGFechaPagoCorteCupon",
		CA."INGFechaCorteCupon"										AS "INGFechaCorteCupon", 
		CA."INGNumeroCupon"											AS "INGNumeroCupon", 
		CA."INGIdMonedaPagoCupon"									AS "INGIdMonedaPagoCupon", 
		CA."INGMontoCorteCupon"										AS "INGMontoCorteCupon",
		CA."IdTipoReajuste"											AS "IdTipoReajuste",
		CA."IdBoveda"												AS "IdBoveda",
		CA."IdTipoOperacion"										AS "IdTipoOperacion",
		CA."NombreTipoOperacion"									AS "NombreTipoOperacion",
		CA."IdCondicionLiquidacion"									AS "IdCondicionLiquidacion",
		CA."FechaLiquidacion"										AS "FechaLiquidacion",
		CA."IdMonedaSistema"										AS "IdMonedaSistema",
		CA."IdEstado"												AS "IdEstado"		
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"		CA
			
		WHERE 	CA."IdClasificacionTipoInstrumento" = 'RF'				AND 
				CA."IdEstado" 						= 'VIG'				AND
				CA."IdModulo" 						= 'CUS'		

	UNION ALL
	
	SELECT
	CA."FolioCustodia" AS "FolioCartera",
	CA."FechaOperacion" AS "FechaOperacion",
	CA."FechaVencimientoOperacion" AS "FechaVencimientoOperacion",
	CA."IdCliente" AS "IdCliente",
	CA."IdPortafolio" AS "IdPortafolio",
	DOC."DocumentoIdentidad" AS "DocumentoIdentidad",
	PER."RazonSocial" AS "RazonSocial",
	TIP."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
	TIP."IdTipoInstrumento" AS "IdTipoInstrumento",
	INS."RazonSocial" AS "RazonSocialInstrumento",
	INS."Nemotecnico" AS "Nemotecnico",
	CA."IdEmisor" AS "IdEmisor",
	CA."MontoOperacionMonedaInstrumento" AS "MontoCompra",
	CA."MontoCapitalCorteCupon" *-1 AS "MontoAmortizacion",
	CA."InteresesPagoCorteCupon" *-1 AS "MontoIntereses",
	CA."FechaPagoCorteCupon" AS "INGFechaPagoCorteCupon",
	CA."FechaCorteCupon" AS "INGFechaCorteCupon",
	CA."NumeroCupon" AS "INGNumeroCupon",
	CA."IdMonedaPagoCupon" AS "INGIdMonedaPagoCupon",
	CA."MontoCorteCupon"*-1 AS "INGMontoCorteCupon",
	CA."IdTipoReajuste" AS "IdTipoReajuste",
	CA."IdBoveda" AS "IdBoveda",
	CA."IdTipoOperacion" AS "IdTipoOperacion",
	OP."Nombre" AS "NombreTipoOperacion",
	CA."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",
	CA."FechaLiquidacion" AS "FechaLiquidacion",
	'CLP' AS "IdMonedaSistema",
	CA."IdEstado" AS "IdEstado"
FROM
	"BSCL.Custodia.BD::CUS.CarteraNegativa" CA
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CA."IdCliente" = CLI."IdCliente" 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona"  = PER."IdPersona" 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON CA."IdInstrumento" = INS."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIP ON INS."IdTipoInstrumento" = TIP."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OP ON CA."IdTipoOperacion" = OP."IdTipoOperacion" 
WHERE
	TIP."IdClasificacionTipoInstrumento" = 'RF'
	AND CA."IdEstado" = 'VIG'	

	UNION ALL
	
	SELECT
	CA."FolioCustodia" AS "FolioCartera",
	CA."FechaOperacion" AS "FechaOperacion",
	CA."FechaVencimientoOperacion" AS "FechaVencimientoOperacion",
	CA."IdCliente" AS "IdCliente",
	CA."IdPortafolio" AS "IdPortafolio",
	DOC."DocumentoIdentidad" AS "DocumentoIdentidad",
	PER."RazonSocial" AS "RazonSocial",
	TIP."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
	TIP."IdTipoInstrumento" AS "IdTipoInstrumento",
	INS."RazonSocial" AS "RazonSocialInstrumento",
	INS."Nemotecnico" AS "Nemotecnico",
	CA."IdEmisor" AS "IdEmisor",
	CA."MontoOperacionMonedaInstrumento" AS "MontoCompra",
	CA."MontoCapitalCorteCupon" *-1 AS "MontoAmortizacion",
	CA."InteresesPagoCorteCupon" *-1 AS "MontoIntereses",
	CA."FechaPagoCorteCupon" AS "INGFechaPagoCorteCupon",
	CA."FechaCorteCupon" AS "INGFechaCorteCupon",
	CA."NumeroCupon" AS "INGNumeroCupon",
	CA."IdMonedaPagoCupon" AS "INGIdMonedaPagoCupon",
	CA."MontoCorteCupon"*-1 AS "INGMontoCorteCupon",
	CA."IdTipoReajuste" AS "IdTipoReajuste",
	CA."IdBoveda" AS "IdBoveda",
	CA."IdTipoOperacion" AS "IdTipoOperacion",
	OP."Nombre" AS "NombreTipoOperacion",
	CA."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",
	CA."FechaLiquidacion" AS "FechaLiquidacion",
	'CLP' AS "IdMonedaSistema",
	CA."IdEstado" AS "IdEstado"
FROM
	"BSCL.Custodia.BD::HIS.CarteraNegativa" CA
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CA."IdCliente" = CLI."IdCliente" 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona"  = PER."IdPersona" 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON CA."IdInstrumento" = INS."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIP ON INS."IdTipoInstrumento" = TIP."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OP ON CA."IdTipoOperacion" = OP."IdTipoOperacion" 
WHERE
	TIP."IdClasificacionTipoInstrumento" = 'RF'
	AND CA."IdEstado" = 'VIG'	


	WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultaPagoCorteCupon"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

