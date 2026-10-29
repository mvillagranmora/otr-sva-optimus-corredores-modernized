SELECT
c1."IdCliente", 
cli."NombreCompleto" AS "Nombre", 
cli."DocumentoIdentidad",
port."IdPortafolio", 
port."Proposito",
port."CodigoPortafolio"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" c1 
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"c3 ON c3."IdCliente" = c1."IdCliente" AND c3."CodigoTipoIndicador" = 'CE' AND c3."ValorIndicador" = 'S'
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = c1."IdCliente" AND cli."IdEstado" = 'ACT'
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port ON port."IdCliente" = c1."IdCliente" AND port."IdEstado" = 'ACT'
LEFT JOIN (SELECT DISTINCT car."IdCliente", car."IdPortafolio"
	FROM "BSCL.Custodia.BD::CUS.Cartera" car 
	WHERE car."IdEmpresa" =:p_IdEmpresa 
	AND car."IdInstrumento" = :p_IdInstrumento 
	AND car."IdEstado" = 'VIG') car1 ON c1."IdCliente"=car1."IdCliente" AND car1."IdPortafolio"=port."IdPortafolio"
WHERE car1."IdCliente" IS NULL
AND c1."CodigoTipoIndicador" = 'CC' AND c1."ValorIndicador" = 'N'
AND c1."IdEmpresa" = :p_IdEmpresa 
AND c1."IdCliente" <> :p_IdClienteExcluido
AND c1."IdCliente" = CASE WHEN :p_IdClienteValidando > 0 THEN :p_IdClienteValidando ELSE c1."IdCliente" END 