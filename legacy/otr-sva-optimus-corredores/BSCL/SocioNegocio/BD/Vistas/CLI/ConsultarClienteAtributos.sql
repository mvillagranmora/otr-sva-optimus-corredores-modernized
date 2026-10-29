SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAtributos"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAtributos"  AS 

SELECT 1 FROM Dummy;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAtributos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 