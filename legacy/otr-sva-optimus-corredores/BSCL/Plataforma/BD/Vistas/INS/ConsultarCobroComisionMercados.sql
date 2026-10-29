SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCobroComisionMercados"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCobroComisionMercados"  
AS
SELECT 
	cla."IdClasificacionTipoInstrumento" , 
	cla."Codigo" , 
	cla."Nombre" , 
	cla."Descripcion" , 
	cla."IdEstado" , 
	tipoins."IdTipoInstrumento" 
FROM  "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS cla 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON  tipoins."IdClasificacionTipoInstrumento" = cla."IdClasificacionTipoInstrumento"
WHERE cla."IdEstado" = 'ACT'
AND cla."IdClasificacionTipoInstrumento" IN ('RV','RF','FM','DV')
UNION ALL
SELECT 
	'CI' AS "IdClasificacionTipoInstrumento"
	,'CI'  AS "Codigo"
	,'CUENTA INVERSION' AS "Nombre"
	,'CUENTA INVERSION' AS "Descripcion"
	,'ACT' AS "IdEstado"
	,NULL AS "IdTipoInstrumento"
FROM DUMMY;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarCobroComisionMercados" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 