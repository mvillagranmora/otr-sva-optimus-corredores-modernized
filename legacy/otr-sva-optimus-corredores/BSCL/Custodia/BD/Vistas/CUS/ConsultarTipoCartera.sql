
SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoCartera";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoCartera"
AS 
SELECT
	"IdTipoCartera",
	"TipoCartera",
	"EstadoTipoCartera"
FROM "BSCL.Custodia.BD::CUS.TipoCartera";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoCartera" TO BCS_DESARROLLADOR WITH GRANT OPTION; 