SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDestinatarios"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDestinatarios"  
AS
select 
dest."IdDestinatario",
dest."IdEmail",
dest."TipoDestinatario",
dest."Destinatario"
from "BSCL.Plataforma.BD::EVT.Destinatario" dest
left join "BSCL.Plataforma.BD::EVT.Mail" mail ON dest."IdEmail" = mail."IdEmail";   

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDestinatarios" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 