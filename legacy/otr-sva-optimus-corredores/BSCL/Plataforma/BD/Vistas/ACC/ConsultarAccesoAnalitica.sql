SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesoAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesoAnalitica"  
AS
select 
       Acc."IdAcceso",
       Acc."NombreAcceso",
       Acc."TipoAcceso",
       Acc."Padre",
   	   Acc."Orden",
   	   Acc."IdServicio",
   	   Acc."IdElementoHtml",
       Acc."EsVisibleMenu",
       Acc."CanalAcceso"
	FROM "BSCL.Plataforma.BD::ACC.Acceso" Acc;
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
