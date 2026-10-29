SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMultifondos"; 
GO
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMultifondos" AS
SELECT "IdMultifondo", "NombreMultifondo" 
FROM "BSCL.Facturacion.BD::FAC.Multifondos"

WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMultifondos" TO BCS_DESARROLLADOR WITH GRANT OPTION;