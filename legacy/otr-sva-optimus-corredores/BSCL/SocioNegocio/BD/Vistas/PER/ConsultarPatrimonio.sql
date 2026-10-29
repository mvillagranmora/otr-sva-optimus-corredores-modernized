SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPatrimonio"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPatrimonio"  AS 
SELECT 
	"IdRangoPatrimonio",
	"DescripcionRango",
	"Minimo",
	"Maximo"
FROM "BSCL.SocioNegocio.BD::PER.RangoPatrimonio";
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPatrimonio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 