SELECT CE."IdPersona", CE."IdDireccionElectronica"
FROM "BSCL.Plataforma.BD::ACC.Usuario" U
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad"  
WHERE "IdUsuario" = :IdUsuario; 