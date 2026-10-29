WITH 
	carteras AS (
		SELECT "IdPortafolio", "IdCliente"
		  FROM "BSCL.Custodia.BD::CUS.Cartera" 
		 WHERE "IdEstado" = 'VIG'
		   AND "IdTipoOperacion" <> 'EMPRES'
		   AND "IdInstrumento" = :p_IdInstrumento
		   AND "IdEmpresa" = :p_IdEmpresa
		GROUP BY "IdPortafolio", "IdCliente"
	)
SELECT
ind."IdCliente", 
cli."NombreCompleto" AS "Nombre", 
cli."DocumentoIdentidad", 
CAR."IdPortafolio", 
port."Proposito",
port."CodigoPortafolio"
FROM carteras CAR 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = car."IdCliente"
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ind ON cli."IdCliente" = ind."IdCliente" AND ind."CodigoTipoIndicador" = 'CE' AND ind."ValorIndicador" = 'S' -- Contrato de Emprestitos
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ind2 ON cli."IdCliente" = ind2."IdCliente" AND ind2."CodigoTipoIndicador" = 'CC' AND ind2."ValorIndicador" = 'S' -- Contrato custodia
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port ON port."IdPortafolio" = CAR."IdPortafolio" AND port."IdEstado" = 'ACT'
WHERE cli."IdEstado" = 'ACT'
AND ind."IdEmpresa" = :p_IdEmpresa 
AND ind."IdCliente" <> :p_IdClienteExcluido
AND ind."IdCliente" = CASE WHEN :p_IdClienteValidando > 0 THEN :p_IdClienteValidando ELSE ind."IdCliente" END   