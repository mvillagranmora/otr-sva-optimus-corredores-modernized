SELECT 
	CASE
		WHEN LENGTH (CODCORR."CodigoCorredora") = 2 THEN '0'||CODCORR."CodigoCorredora"
		WHEN LENGTH (CODCORR."CodigoCorredora") = 3 THEN CODCORR."CodigoCorredora"
		ELSE ''
	END "CodigoCorredora" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" CODCORR
WHERE "IdEmpresa" = :IdEmpresa AND "IdBolsa" = 'BCS';