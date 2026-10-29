UPDATE ind
SET ind."ValorIndicador" = CASE WHEN :UsuarioClientes = 'S' THEN 'N' ELSE 'S' END
FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" usu
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli ON usu."IdPersona" = cli."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ind ON cli."IdCliente" = ind."IdEntidad"
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" tipo ON ind."IdTipoIndicador" = tipo."IdTipoIndicador"
WHERE "CodigoTipoIndicador" = 'NPA' AND "IdUsuario" = :IdUsuario;