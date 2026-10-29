SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoEnTransitoLite";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoEnTransitoLite" 

AS
SELECT * FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoEnTransitoLite" TO BCS_DESARROLLADOR WITH GRANT OPTION;
