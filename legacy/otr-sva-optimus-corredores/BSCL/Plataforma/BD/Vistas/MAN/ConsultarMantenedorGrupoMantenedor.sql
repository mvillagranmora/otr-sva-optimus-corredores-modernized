SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MAN.ConsultarMantenedorGrupoMantenedor"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MAN.ConsultarMantenedorGrupoMantenedor"  
AS
Select GRUMAN."IdGrupoMantenedor", 
       GRUMAN."NombreGrupoMantenedor", 
       GRUMAN."IdEstado" As EstadoGrupoMantenedor,
       MAN."IdMantenedor", 
       MAN."NombreMantenedor", 
       MAN."TablaMantenedor", 
       MAN."IdEstado" As EstadoMantenedor
from "BSCL.Plataforma.BD::MAN.GrupoMantenedor" As GRUMAN
  Inner Join "BSCL.Plataforma.BD::MAN.Mantenedor" As MAN
     On MAN."IdGrupoMantenedor" = GRUMAN."IdGrupoMantenedor";
     
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MAN.ConsultarMantenedorGrupoMantenedor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
     
     
     