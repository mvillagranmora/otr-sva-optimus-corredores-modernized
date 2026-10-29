SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroTipoCliente"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroTipoCliente"  
AS

SELECT 
	DEF."IdEmpresa",DEF."CodigoDefinicionCobro",DEF."IdTipoCliente","Nombre" as "NombreTipoCliente"
	
FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobroTipoCliente" DEF
INNER JOIN "BSCL.SocioNegocio.BD::CLI.TipoCliente" CLI ON CLI."IdTipoCliente" = DEF."IdTipoCliente"
;


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroTipoCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
