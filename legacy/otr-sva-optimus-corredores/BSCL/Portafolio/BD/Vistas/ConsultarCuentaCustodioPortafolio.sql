SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio"; 

CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS 

	SELECT  
		CCP."IdCuentaCustodioPortafolio",
	  	CCP."IdPortafolio",
	   	CCP."IdBoveda",
	   	BOV."Nombre" as "NombreBoveda",
		BOV."Nombre" || ' (' || CCP."Codigo" || ')' as "NombreBovedaCodigo",
	   	CCP."Codigo",
	   	CCP."FechaIngreso",
	   	CCP."FechaCreacion",
		CCP."FechaModificacion",
		CCP."IdUsuarioCreador",
		CCP."IdEstado",
		ESTCCP."Nombre" as "NombreEstado",
		CCP."IdEstadoAutorizacion",
		ESTCCPAUT."Nombre" as "NombreEstadoAutorizacion",
		
		CCP."LiquidaVencimientos",
		CCP."LiquidaCupones",
		CCP."LiquidaPrepagos",
		CCP."LiquidaDividendos",
		
		CCP."IdModeloCustodia",		
		MODELO."NombreModeloCustodia",
		CCP."IdTipoCustodia",
		TIPOCUSTODIA."NombreTipoCustodia",
		CCP."IdTipoCuentaCustodia",
		TIPOCUENTA."NombreTipoCuentaCustodia",
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
            END  as "NombreCompletoBancoLiquidador",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CCP."LiquidaVencimientos") AS "LiquidaVencimientosUI",	
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CCP."LiquidaCupones") AS "LiquidaCuponesUI",	
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CCP."LiquidaPrepagos") AS "LiquidaPrepagosUI",	
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CCP."LiquidaDividendos") AS "LiquidaDividendosUI"
		
	FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" as CCP
	INNER JOIN "BSCL.Plataforma.BD::PLA.Boveda" as BOV ON (BOV."IdBoveda" = CCP."IdBoveda")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTCCP ON (ESTCCP."IdEstado" = CCP."IdEstado")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTCCPAUT ON (ESTCCPAUT."IdEstado" = CCP."IdEstadoAutorizacion")
	LEFT JOIN "BSCL.Portafolio.BD::POR.ModeloCustodia" MODELO on MODELO."IdModeloCustodia" = CCP."IdModeloCustodia"
	LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" TIPOCUSTODIA on TIPOCUSTODIA."IdTipoCustodia" = CCP."IdTipoCustodia"
	LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCuentaCustodia" TIPOCUENTA on TIPOCUENTA."IdTipoCuentaCustodia" = CCP."IdTipoCuentaCustodia"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" BANCOCUSTODIO on BANCOCUSTODIO."IdPersona" = CCP."IdPersonaBancoCustodio"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" BANCOLIQ on BANCOLIQ."IdPersona" = CCP."IdPersonaBancoLiquidador"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERLIQ ON PERLIQ."IdPersona" = BANCOCUSTODIO."IdPersona"
    LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERCUS ON PERCUS."IdPersona" = BANCOLIQ."IdPersona";
    

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
