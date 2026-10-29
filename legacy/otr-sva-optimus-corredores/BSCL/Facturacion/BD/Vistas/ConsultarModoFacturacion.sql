SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarModoFacturacion"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarModoFacturacion" AS
 
SELECT 
	MF."IdModoFacturacion",
	MF."Nombre"
FROM "BSCL.Facturacion.BD::FAC.ModoFacturacion" as MF;

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarModoFacturacion" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
