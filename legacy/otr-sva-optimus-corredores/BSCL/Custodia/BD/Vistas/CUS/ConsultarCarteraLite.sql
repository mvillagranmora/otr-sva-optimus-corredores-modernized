SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" 

AS
SELECT * FROM "BSCL.Custodia.BD::CUS.Cartera"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" TO BCS_DESARROLLADOR WITH GRANT OPTION;

