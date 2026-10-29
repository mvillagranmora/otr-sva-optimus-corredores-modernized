SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MAN.ConsultarGrupoMantenedorDetalle"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MAN.ConsultarGrupoMantenedorDetalle"
AS  
Select 
   GRUPOMANT."IdGrupoMantenedor",
   "NombreGrupoMantenedor",
   GRUPOMANT."IdEstado" As "IdEstadoGrupoMantenedor",
   "IdMantenedor",
   "CodigoMantenedor",
   "NombreMantenedor",
   "TablaMantenedor",
   MANT."IdEstado" As "IdEstadoMantenedor"
From "BSCL.Plataforma.BD::MAN.GrupoMantenedor" As GRUPOMANT
  Inner Join "BSCL.Plataforma.BD::MAN.Mantenedor" As MANT
     On MANT."IdGrupoMantenedor" = GRUPOMANT."IdGrupoMantenedor";  

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MAN.ConsultarGrupoMantenedorDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
