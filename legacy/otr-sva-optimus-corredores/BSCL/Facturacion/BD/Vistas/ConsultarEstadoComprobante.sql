SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarEstadoComprobante"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarEstadoComprobante" As   

SELECT "EstadoFacturacion_$0"."IdEstadoFacturacion" , 
       "EstadoFacturacion_$0"."IdEstado", 
       "EstadoFacturacion_$0"."NombreEntidad", 
       "Estado_$1"."Nombre" AS "NombreEstado" 
FROM ("BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS "EstadoFacturacion_$0" 
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$1" 
     ON ("Estado_$1"."IdEstado" = "EstadoFacturacion_$0"."IdEstado"))  
WHERE ("EstadoFacturacion_$0"."NombreEntidad" = 'FAC.Comprobante');

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarEstadoComprobante" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
