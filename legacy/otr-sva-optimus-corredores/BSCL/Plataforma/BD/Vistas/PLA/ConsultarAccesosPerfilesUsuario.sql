SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarAccesosPerfilesUsuario"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarAccesosPerfilesUsuario"  
AS
WITH TBUsuarioRolPerfil AS (
  SELECT "IdEmpresa", "IdUsuario", "CodigoUsuario", "IdPerfil"  
  FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" 
  WHERE "IdEstadoRol" = 'ACT'
  AND "IdEstadoPerfil"  = 'ACT'
  GROUP BY "IdEmpresa", "IdUsuario", "CodigoUsuario", "IdPerfil" 
)
SELECT URP."IdUsuario", URP."CodigoUsuario", URP."IdPerfil", CAP."IdElementoHtml", CAP."IdEstadoPerfilAcceso"
FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil" AS CAP
INNER JOIN TBUsuarioRolPerfil AS URP ON URP."IdPerfil" = CAP."IdPerfil" AND URP."IdEmpresa" = CAP."IdEmpresaPerfil"
WHERE CAP."IdEstadoPerfilAcceso" IS NOT NULL
ORDER BY CAP."IdEstadoPerfilAcceso" DESC;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarAccesosPerfilesUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
