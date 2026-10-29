SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoPerfilComercial"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoPerfilComercial"  
AS

SELECT 
	EPC."IdEstadoPerfilComercial",
	E."IdEstado",
	E."Nombre",
	EPC."Entidad"
FROM 
	"BSCL.Plataforma.BD::COM.EstadoPerfilComercial" as EPC
	INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" as E
		on E."IdEstado" = EPC."IdEstado";	

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoPerfilComercial" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
