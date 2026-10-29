SELECT 
	CB."IdBanco", CCB."NumeroCuentaBancaria",* 
FROM  "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS CB
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" AS CCB
	ON CB."IdBanco" = CCB."IdBanco"
	AND CB."CodigoBanco" = CCB."CodigoBanco"
INNER JOIN "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" AS PPC
	ON PPC."IdConcepto" = 'APVPAC'
	AND PPC."IdTipoMovimiento" = 'MOVING'
	AND PPC."IdCuentaBanco" = CCB."IdCuentaBanco"
WHERE CB."CodigoBanco" = :p_CodigoBanco
AND  CCB."IdMoneda" = 'CLP';