SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumentoAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumentoAnalitica"  
AS
SELECT 
	"IdClasificacionTipoInstrumento", 
	"Codigo",
	"Descripcion", 
	"IdEstado", 
	"Nombre"
FROM  "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumentoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;