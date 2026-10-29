SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoInstrumento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoInstrumento"  
AS
SELECT 
"IdTipoInstrumento",
"Nombre", 
"IdEstado" 
FROM "BSCL.Plataforma.BD::INS.TipoInstrumento";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;