SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante"; 
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" As

SELECT TipCmbte."IdTipoComprobante", 
       TipCmbte."Codigo", 
       TipCmbte."Nombre", 
       TipCmbte."IdEstadoFacturacion", 
       TipCmbte."FechaIngresoRegistro", 
       EstPla."Nombre" AS "NombreEstado",
       TipCmbte."AfectoImpuesto",
       TipCmbte."UsarEnSpot",
       TipCmbte."UsarEnFacturaAPartirDeOtra"
FROM (("BSCL.Facturacion.BD::FAC.TipoComprobante" AS TipCmbte 
    LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS EstFac
      ON (EstFac."IdEstado" = TipCmbte."IdEstadoFacturacion"))  
    LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstPla 
      ON (EstPla."IdEstado" = EstFac."IdEstado"))  
WHERE Upper(EstFac."NombreEntidad") = Upper('FAC.TipoComprobante');
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
