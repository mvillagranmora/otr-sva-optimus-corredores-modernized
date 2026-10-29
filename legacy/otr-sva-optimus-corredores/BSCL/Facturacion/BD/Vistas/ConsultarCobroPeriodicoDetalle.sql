SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarCobroPeriodicoDetalle";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarCobroPeriodicoDetalle" AS

SELECT	 "IdCobroPeriodicoDetalle"
		,"IdCobroPeriodico"
		,"MontoCobroPeriodico"
		,"PorcentajeCobroPeriodico"
		,"MontoDesde"
		,"MontoHasta"
FROM "BSCL.Facturacion.BD::FAC.CobroPeriodicoDetalle" 
    
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarCobroPeriodicoDetalle" TO BCS_DESARROLLADOR WITH GRANT OPTION;                                       
GO

