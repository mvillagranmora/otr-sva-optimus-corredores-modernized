SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles"  
AS
select PER."IdPerfil",
	   PER."IdEmpresa",
	   PER."Nombre",
	   PER."IdEstado",
	   EPL."Descripcion" as "EstadoPerfil"
from "BSCL.Plataforma.BD::ACC.Perfil" as PER
left JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla" EPL on EPL."IdEstado" = PER."IdEstado" and EPL."NombreEntidad" = 'PERFIL';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

