SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarFoliosOrdenes";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarFoliosOrdenes"

AS 

SELECT
	"Folio",
	TO_NVARCHAR("FechaAceptacion",'YYYY-MM-DD') AS "FechaAceptacion"
FROM 
	"BSCL.Orden.BD::ORD.Orden";
GO	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::EJORD.ConsultarFoliosOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION;