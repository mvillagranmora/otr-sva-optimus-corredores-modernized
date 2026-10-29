SELECT "IdCliente"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
WHERE "IdPersona" IN (SELECT "IdPersona" FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa")