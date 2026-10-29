SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFacturaPortafolio"; 
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFacturaPortafolio" AS

SELECT LIQ."IdTipoLiquidacionComisionFactura", LIQ."TipoLiquidacion", LIQ."IdEstado", LIQ."TipoPorDefecto",  POR."IdPortafolio"  
FROM "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFactura" 	AS LIQ 
INNER JOIN  "BSCL.Orden.BD.Vistas::ConsultarInformacionPortafolioOrdenes"		AS POR 
ON LIQ."IdTipoLiquidacionComisionFactura" = CASE WHEN POR."ModificaTipoLiquidacion" IN ('MODPREFCONT', 'MODPREFCONV') THEN 'MEN' END  

UNION ALL

SELECT LIQ."IdTipoLiquidacionComisionFactura", LIQ."TipoLiquidacion", LIQ."IdEstado", LIQ."TipoPorDefecto",  POR."IdPortafolio"  
FROM "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFactura" 			AS LIQ 
LEFT  JOIN  "BSCL.Orden.BD.Vistas::ConsultarInformacionPortafolioOrdenes"	AS POR 
ON LIQ."IdTipoLiquidacionComisionFactura" = 'DIR' 

GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFacturaPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 