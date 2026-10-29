SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarSubyacenteLite";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarSubyacenteLite" 

AS
SELECT
	SUB.*,
	CAR."IdEmpresa"
FROM "BSCL.Custodia.BD::CUS.Subyacente" SUB
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
	ON (SUB."FolioCarteraRelacionada" = CAR."FolioCartera")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarSubyacenteLite" TO BCS_DESARROLLADOR WITH GRANT OPTION;
