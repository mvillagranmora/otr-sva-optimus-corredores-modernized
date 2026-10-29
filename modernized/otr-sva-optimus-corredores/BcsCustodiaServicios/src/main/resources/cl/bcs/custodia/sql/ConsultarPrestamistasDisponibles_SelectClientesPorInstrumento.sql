SELECT "IdCliente"
FROM "BSCL.Custodia.BD::CUS.Cartera" 
WHERE "IdEstado" = 'VIG'
	AND "IdTipoOperacion" <> 'EMPRES'
	AND "IdInstrumento" = :p_IdInstrumento
	AND "IdEmpresa" = :p_IdEmpresa
GROUP BY "IdCliente";