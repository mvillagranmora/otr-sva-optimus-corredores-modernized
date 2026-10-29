SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarAFP"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarAFP" As   

SELECT "IdAFP", "NombreAFP"
FROM "BSCL.Facturacion.BD::FAC.AFP";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarAFP" TO BCS_DESARROLLADOR WITH GRANT OPTION; 