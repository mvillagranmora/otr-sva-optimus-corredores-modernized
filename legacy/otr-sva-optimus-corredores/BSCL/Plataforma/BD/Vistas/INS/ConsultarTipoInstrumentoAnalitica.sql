SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoAnalitica" AS
select 
 	 CAST("IdTipoInstrumento" AS NVARCHAR(3)) AS "IdTipoInstrumento"
	,CAST("Codigo" AS NVARCHAR(40)) AS "Codigo"
	,CAST("Nombre" AS NVARCHAR(100)) AS "Nombre"
	,CASE WHEN "IdEstado" = '' THEN NULL ELSE "IdEstado" END AS "IdEstado"
	,CASE WHEN "IdClasificacionTipoInstrumento" = '' THEN NULL ELSE "IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento"
from "BSCL.Plataforma.BD::INS.TipoInstrumento";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;