SELECT COALESCE(EMP."IdPersona",0) AS "IdPersonaRemitente" 
FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP ON (EMP."IdEmpleado" = USU."IdEntidad")
WHERE USU."IdUsuario" = :IdUsuario;