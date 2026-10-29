SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroPortafolio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroPortafolio"  
AS

SELECT 
	DEF."IdEmpresa",DEF."CodigoDefinicionCobro",DEF."IdPortafolio",POR."IdCliente","DocumentoIdentidad","NombreCompleto","CodigoPortafolio"
	
FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobroPortafolio" DEF
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON DEF."IdPortafolio" = POR."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON POR."IdCliente" = CLI."IdCliente"

;


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
