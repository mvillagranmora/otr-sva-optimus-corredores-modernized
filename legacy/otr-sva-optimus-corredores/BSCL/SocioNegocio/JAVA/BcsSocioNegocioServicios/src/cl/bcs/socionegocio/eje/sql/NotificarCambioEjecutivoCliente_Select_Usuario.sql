SELECT TOP 1
    CE."IdPersona" AS "IdPersona",
    CE."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronica"
FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" U ON CSN."IdEmpleado" = U."IdEntidad"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CSN."IdPersona" = CE."IdPersona"
WHERE U."IdUsuario" = :IdUsuario
