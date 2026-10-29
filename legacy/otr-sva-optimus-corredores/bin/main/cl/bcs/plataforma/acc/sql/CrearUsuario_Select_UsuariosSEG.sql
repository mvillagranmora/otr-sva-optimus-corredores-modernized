SELECT DIR."IdDireccionElectronica" AS "IdCorreo", DIR."IdPersona" AS "IdPersona" FROM "BSCL.Plataforma.BD::ACC.RolUsuario" ROLUS
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" US ON ROLUS."IdUsuario" = US."IdUsuario" AND ROLUS."IdRol" = (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'ACC.IdRolSeguridad')
INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON US."IdEntidad" = EMP."IdEmpleado"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON EMP."IdPersona" = PER."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DIR ON Per."IdPersona" = DIR."IdPersona" AND DIR."Predeterminado" = 'S'; 