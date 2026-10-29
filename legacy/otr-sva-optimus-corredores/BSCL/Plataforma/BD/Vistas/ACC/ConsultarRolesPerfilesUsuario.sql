SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"  
AS
SELECT
    SubC."IdUsuario",
    SubC."CodigoUsuario",
    SubC."IdEmpresa",
    SubC."IdRol",
    SubC."NombreRol",
    SubC."IdEstadoRol",
    SubC."IdPerfil",
    SubC."NombrePerfil",
    SubC."IdEstadoPerfil",
    SubC."EstadoPerfil",
    SubC."EsAutorizado"
FROM (
    SELECT
        Usu."IdUsuario",
        Usu."CodigoUsuario",
        Rol."IdEmpresa",
        Rol."IdRol",
        Rol."Nombre" AS "NombreRol",
        Rol."IdEstado" AS "IdEstadoRol",
        Per."IdPerfil",
        Per."Nombre" AS "NombrePerfil",
        Per."IdEstado" AS "IdEstadoPerfil",
        EPL."Descripcion" AS "EstadoPerfil",
        RolUsu."EsAutorizado",
        ROW_NUMBER() OVER (PARTITION BY Usu."IdUsuario", Per."Nombre" ORDER BY Rol."Nombre") AS rn		---(PARA ALMCENAR LAS VECES QUE SE REPITE EL NOMRBRE DEL PERFIL)
    FROM "BSCL.Plataforma.BD::ACC.Usuario" Usu
    INNER JOIN "BSCL.Plataforma.BD::ACC.RolUsuario" RolUsu ON RolUsu."IdUsuario" = Usu."IdUsuario"
    INNER JOIN "BSCL.Plataforma.BD::ACC.Rol" Rol ON Rol."IdRol" = RolUsu."IdRol"
    LEFT JOIN "BSCL.Plataforma.BD::ACC.RolPerfil" RolPer ON RolPer."IdRol" = Rol."IdRol"
    LEFT JOIN "BSCL.Plataforma.BD::ACC.Perfil" Per ON Per."IdPerfil" = RolPer."IdPerfil" AND Per."Nombre" IS NOT NULL
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla" EPL ON EPL."IdEstado" = Per."IdEstado" AND EPL."NombreEntidad" = 'PERFIL'
    WHERE Per."Nombre" IS NOT NULL -- EXCLUIR LOS PERFILES NULL
) SubC
WHERE SubC.rn = 1		--PARA OBTENER LOS NOMBRES DE PERFIL SIN TENER REPLICAS de MISMO PERFIL DIFERENTE ROL
ORDER BY SubC."NombreRol";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 