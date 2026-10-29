SELECT USU."CodigoUsuario"
      ,EMP."CodigoYNombreEmpleado" AS "NombreUsuarioCreacion"
FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP
    ON EMP."IdEmpleado" = USU."IdEntidad"
WHERE USU."IdUsuario" = :IdUsuario AND USU."TipoEntidad" = 'EMP';