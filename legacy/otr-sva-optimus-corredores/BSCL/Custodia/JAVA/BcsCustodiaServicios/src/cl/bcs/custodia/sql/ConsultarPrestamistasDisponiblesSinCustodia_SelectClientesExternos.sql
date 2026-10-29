SELECT
cli."IdCliente", 
cli."NombreCompleto" AS "Nombre", 
cli."DocumentoIdentidad",
port."IdPortafolio", 
port."Proposito",
port."CodigoPortafolio"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ind1 ON cli."IdCliente" = ind1."IdCliente" AND ind1."CodigoTipoIndicador" = 'CC' AND ind1."ValorIndicador" = 'N'
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ind2 ON cli."IdCliente" = ind2."IdCliente" AND ind2."CodigoTipoIndicador" = 'CE' AND ind2."ValorIndicador" = 'S'
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port ON port."IdCliente" = cli."IdCliente" AND port."IdEstado" = 'ACT'
WHERE cli."IdEstado" = 'ACT'
AND cli."IdEmpresa" = :p_IdEmpresa 
AND cli."IdCliente" <> :p_IdClienteExcluido
AND cli."IdCliente" = CASE WHEN :p_IdClienteValidando > 0 THEN :p_IdClienteValidando ELSE ind1."IdCliente" END;