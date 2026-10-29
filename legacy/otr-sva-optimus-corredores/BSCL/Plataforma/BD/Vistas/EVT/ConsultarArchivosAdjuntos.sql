SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarArchivosAdjuntos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarArchivosAdjuntos"  
AS
select 
"IdArchivoAdjunto",
"IdEmail",
"Nombre",
"Archivo"
 from "BSCL.Plataforma.BD::EVT.ArchivoAdjunto";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarArchivosAdjuntos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 