SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario"  
AS
SELECT
       Usu."IdUsuario",
       Usu."CodigoUsuario",
       Rol."IdEmpresa",
       Rol."IdRol",
       Rol."Nombre" as "NombreRol",
       Rol."IdEstado",
       EPL."Descripcion" AS "EstadoRol",
       RolUsu."EsAutorizado"            
       
FROM "BSCL.Plataforma.BD::ACC.Usuario" Usu
INNER JOIN "BSCL.Plataforma.BD::ACC.RolUsuario" RolUsu ON RolUsu."IdUsuario" = Usu."IdUsuario"
INNER JOIN "BSCL.Plataforma.BD::ACC.Rol" Rol ON Rol."IdRol" = RolUsu."IdRol"
left JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla" EPL on EPL."IdEstado" = Usu."IdEstado" and EPL."NombreEntidad" = 'ROL';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 