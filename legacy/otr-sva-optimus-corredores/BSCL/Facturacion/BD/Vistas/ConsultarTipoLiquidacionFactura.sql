SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFactura"; 
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFactura" AS
SELECT
	  "IdTipoLiquidacionComisionFactura",
	  "TipoLiquidacion",
	  "IdEstado",
	  "TipoPorDefecto"
FROM "BSCL.Facturacion.BD::FAC.TipoLiquidacionComisionFactura";
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFactura" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 