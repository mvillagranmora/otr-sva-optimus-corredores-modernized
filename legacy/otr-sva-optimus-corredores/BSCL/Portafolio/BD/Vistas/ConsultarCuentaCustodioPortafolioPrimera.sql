SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioPrimera"; 
GO
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioPrimera" AS 

	SELECT  "Posicion", "IdCuentaCustodioPortafolio", "IdPortafolio", "IdBoveda", "Codigo", "FechaIngreso", "FechaCreacion", "FechaModificacion", "IdUsuarioCreador", "IdEstado", "IdEstadoAutorizacion", 
			"IdModeloCustodia", "IdTipoCustodia", "IdTipoCuentaCustodia", "LiquidaDividendos", "LiquidaCupones", "LiquidaPrepagos", "IdPersonaBancoCustodio", "IdPersonaBancoLiquidador", "LiquidaVencimientos"
	FROM  
	(
		SELECT
			RANK() over (partition by "IdPortafolio", "IdBoveda" order by "IdCuentaCustodioPortafolio" desc) as "Posicion",
			"IdCuentaCustodioPortafolio", "IdPortafolio", "IdBoveda", "Codigo", "FechaIngreso", "FechaCreacion", "FechaModificacion", "IdUsuarioCreador", "IdEstado", "IdEstadoAutorizacion", 
			"IdModeloCustodia", "IdTipoCustodia", "IdTipoCuentaCustodia", "LiquidaDividendos", "LiquidaCupones", "LiquidaPrepagos", "IdPersonaBancoCustodio", "IdPersonaBancoLiquidador", "LiquidaVencimientos"
		FROM 
			"BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
		WHERE "IdEstado" = 'ACT'
	) data
	WHERE data."Posicion" = 1;
    
GO 	
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioPrimera" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
GO
