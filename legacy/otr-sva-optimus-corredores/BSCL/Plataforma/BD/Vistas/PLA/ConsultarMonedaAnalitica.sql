SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMonedaAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMonedaAnalitica" AS
SELECT
	"IdMoneda",
	"Codigo",
	"CodigoNumerico",
	"Nombre",
	"Simbolo"
FROM "BSCL.Plataforma.BD::PLA.Moneda";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMonedaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;