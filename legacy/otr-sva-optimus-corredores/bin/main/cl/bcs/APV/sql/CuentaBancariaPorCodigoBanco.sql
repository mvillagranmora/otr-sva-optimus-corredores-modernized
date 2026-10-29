SELECT 
	TOP 1 "NumeroCuentaBancaria" 
FROM  "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS CB
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" AS CCB
	ON CB."IdBanco" = CCB."IdBanco"
	AND CB."CodigoBanco" = CCB."CodigoBanco"
WHERE CB."CodigoBanco" = :p_CodigoBanco
AND  CCB."IdMoneda" = 'CLP'