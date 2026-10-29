SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioUI"; 
GO
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioUI" AS 

	SELECT  
		CCP."IdCuentaCustodioPortafolio",
	  	CCP."IdPortafolio",
	   	CCP."IdBoveda",
	   	CCP."Codigo",
		CCP."LiquidaVencimientos",
		CCP."LiquidaCupones",
		CCP."LiquidaPrepagos",
		CCP."LiquidaDividendos",
		CCP."IdModeloCustodia",		
		CCP."IdTipoCustodia",
		CCP."IdTipoCuentaCustodia",
		CCP."IdPersonaBancoCustodio",
		BANCOCUSTODIO."Codigo" AS "CodigoBancoCustodio",
		CASE PERCUS."Tipo" 
            WHEN 'N' THEN PERCUS."Nombres" ||' '|| PERCUS."ApellidoPaterno" || ' ' ||PERCUS."ApellidoMaterno" 
            WHEN 'M' THEN PERCUS."Nombres" ||' '|| PERCUS."ApellidoPaterno" || ' ' ||PERCUS."ApellidoMaterno" 
            ELSE PERCUS."RazonSocial"
            END as "NombreCompletoBancoCustodio",
		CCP."IdPersonaBancoLiquidador",
		BANCOLIQ."Codigo" AS "CodigoBancoLiquidador",
		CASE PERLIQ."Tipo" 
            WHEN 'N' THEN PERLIQ."Nombres" ||' '|| PERLIQ."ApellidoPaterno" || ' ' ||PERLIQ."ApellidoMaterno" 
            WHEN 'M' THEN PERLIQ."Nombres" ||' '|| PERLIQ."ApellidoPaterno" || ' ' ||PERLIQ."ApellidoMaterno" 
            ELSE PERLIQ."RazonSocial"
            END  as "NombreCompletoBancoLiquidador"
		
	FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" as CCP
	LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" BANCOCUSTODIO on BANCOCUSTODIO."IdPersona" = CCP."IdPersonaBancoCustodio"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" BANCOLIQ on BANCOLIQ."IdPersona" = CCP."IdPersonaBancoLiquidador"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERLIQ ON PERLIQ."IdPersona" = BANCOCUSTODIO."IdPersona"
    LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERCUS ON PERCUS."IdPersona" = BANCOLIQ."IdPersona";
    
GO 	
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
GO
