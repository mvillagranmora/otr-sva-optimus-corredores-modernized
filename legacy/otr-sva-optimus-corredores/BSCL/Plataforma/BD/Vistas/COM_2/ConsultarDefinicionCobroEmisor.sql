SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroEmisor"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroEmisor"  
AS

SELECT 
	"IdEmpresa","CodigoDefinicionCobro","IdEmisor"
	
FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobroEmisor" DEFRANGO
;


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroEmisor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
