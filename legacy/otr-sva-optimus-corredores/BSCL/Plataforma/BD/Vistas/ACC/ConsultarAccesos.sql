SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesos"  
AS
select 
       Acc."IdAcceso",
       Acc."NombreAcceso",
       Acc."TipoAcceso",
       Acc."Padre",
   	   Acc."Orden",
   	   Acc."IdServicio",
   	   Acc."IdElementoHtml",
       Acc."EsVisibleMenu"
from "BSCL.Plataforma.BD::ACC.Acceso" Acc;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
