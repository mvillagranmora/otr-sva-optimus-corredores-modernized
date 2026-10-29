SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroRango"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroRango"  
AS

SELECT 
	"IdEmpresa","CodigoDefinicionCobro","Hasta","Valor"
	
FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobroRango" DEFRANGO
;


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroRango" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
