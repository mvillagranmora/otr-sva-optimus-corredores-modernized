SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarTipoUtilidadesCalificacionTributaria";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarTipoUtilidadesCalificacionTributaria" (
"IdTipoUtilidadesCalificacionTributaria"	
,"Descripcion"
,"DescripcionCombobox"			
,"IdEstado"				
,"NombreEstado"				
)
AS 
SELECT 
	TIPO."IdTipoUtilidadesCalificacionTributaria"	
	,TIPO."Descripcion"
	,TIPO."IdTipoUtilidadesCalificacionTributaria" || '-' || TIPO."Descripcion" AS "DescripcionCombobox"			
	,TIPO."IdEstado"				
	,ESTADO."Nombre" AS "NombreEstado"	
FROM "BSCL.Normativa.BD::TRI.TipoUtilidadesCalificacionTributaria" TIPO
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ESTADO ON TIPO."IdEstado" = ESTADO."IdEstado"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarTipoUtilidadesCalificacionTributaria" TO bcs_desarrollador WITH GRANT OPTION;
