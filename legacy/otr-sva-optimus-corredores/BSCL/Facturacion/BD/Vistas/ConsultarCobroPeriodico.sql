SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarCobroPeriodico";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarCobroPeriodico" AS

SELECT 	CobroPer."IdCobroPeriodico"
		,CobroPer."IdCobro"
		,Cobro."NombreCobro"
		,CobroPer."IdContratoCobroPeriodico"
		,CobroPer."MontoMinimoACobrar"
		,CobroPer."MontoMaximoACobrar"
		,CASE 	WHEN CobroPer."TipoCobroPeriodico" = 'V' THEN 'Variable'
				WHEN CobroPer."TipoCobroPeriodico" = 'F' THEN 'Fijo'
		 		ELSE ''
		 END AS "TipoCobroPeriodico"
		,CobroPer."PorRango"
FROM "BSCL.Facturacion.BD::FAC.CobroPeriodico" AS CobroPer
	INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro" Cobro
		ON CobroPer."IdCobro" = Cobro."IdCobro"
    
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarCobroPeriodico" TO BCS_DESARROLLADOR WITH GRANT OPTION;                                       
GO

