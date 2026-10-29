SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroCorredoraCorresponsal"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroCorredoraCorresponsal"  
AS

SELECT 
	DEF."IdEmpresa","CodigoDefinicionCobro","IdCorredoraCorresponsal","IdPais","NombrePais","CodigoCorredor"
	
FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobroCorredoraCorresponsal" DEF
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarCorredorCorresponsal" COR ON DEF."IdCorredoraCorresponsal" = COR."IdCorredorCorresponsal"
;

GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroCorredoraCorresponsal" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO