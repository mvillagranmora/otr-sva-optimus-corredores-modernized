SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreFantasiaAnalitica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreFantasiaAnalitica" AS
SELECT 
 	 	"IdNombreFantasia"
 	   	,CAST("DescripcionNombreFantasia" AS NVARCHAR(200)) AS "DescripcionNombreFantasia"
		,CASE WHEN "IdPersona" = 0 THEN NULL ELSE "IdPersona" END AS "IdPersona"
		,CASE WHEN IFNULL("FechaCreacion",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
           ELSE  
               CASE WHEN  "FechaCreacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
               	ELSE LEFT(TO_CHAR(ADD_SECONDS ("FechaCreacion", CONFIGURACION."Valor")),19)
               END 
 		END AS "FechaCreacion"
		,CASE WHEN "IdEstado" = '' THEN NULL ELSE "IdEstado" END AS "IdEstado"
from "BSCL.SocioNegocio.BD::PER.NombreFantasia" 
CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
		where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreFantasiaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 