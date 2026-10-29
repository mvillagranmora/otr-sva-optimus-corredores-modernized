SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarTipoCustodiaPortafolio"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarTipoCustodiaPortafolio" AS 
	SELECT 
		tc."IdTipoCustodia",
		tc."NombreTipoCustodia",
		tc."ValidaFlujoCustodia",
		tc."CodigoBancoCustodioDefault",
		coalesce(bc."IdPersona",0) as "IdPersonaBancoCustodioDefault",
		tc."CodigoBancoLiquidadorDefault",
		coalesce(bl."IdPersona",0) as "IdPersonaBancoLiquidadorDefault"
	FROM "BSCL.Portafolio.BD::POR.TipoCustodia" tc
	left join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodio" bc
		on tc."CodigoBancoCustodioDefault" = bc."Codigo"
	left join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoLiquidador" bl
		on tc."CodigoBancoCustodioDefault" = bl."Codigo";
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarTipoCustodiaPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


