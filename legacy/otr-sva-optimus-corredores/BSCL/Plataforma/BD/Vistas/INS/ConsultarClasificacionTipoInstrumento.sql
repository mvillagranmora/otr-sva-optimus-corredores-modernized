SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento"  
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
WHERE cla."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
