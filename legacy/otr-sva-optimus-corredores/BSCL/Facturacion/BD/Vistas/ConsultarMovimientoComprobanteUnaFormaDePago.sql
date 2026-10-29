SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteUnaFormaDePago"; 
 GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteUnaFormaDePago" As
 
SELECT
	DISTINCT
   "IdMovimientoComprobante",
   max("NombreMedioDePago")  OVER (PARTITION BY "IdMovimientoComprobante") AS "NombreMedioDePago"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" AS MvtoCmbteFPago
  INNER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS EstadoFacturacion 
     ON EstadoFacturacion."IdEstado" = MvtoCmbteFPago."IdEstado"
  INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla      
     ON EstadoPla."IdEstado" = EstadoFacturacion."IdEstado"
WHERE Upper(EstadoFacturacion."NombreEntidad") = Upper('FAC.MovimientoComprobanteFormaDePago');
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteUnaFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

GO
