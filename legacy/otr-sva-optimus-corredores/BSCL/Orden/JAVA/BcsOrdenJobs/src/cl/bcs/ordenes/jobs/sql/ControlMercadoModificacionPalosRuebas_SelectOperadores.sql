SELECT "CodigoOperador","CodigoOperadorAsociado","Fondo","IdPortafolio" 
FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador"
WHERE "IdEstado" = 'ACT'
AND "IdEmpresa"=:IdEmpresa
AND "CodigoOperador" IN (:Operadores)
UNION ALL
SELECT TOP 1 Cod."CodigoCorredoraFix" as "CodigoOperador", '' as "CodigoOperadorAsociado", '' AS "Fondo", 0 as "IdPortafolio" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" Corr
INNER JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" Cod 
ON Corr."IdCorredora"=Cod."IdCorredora" AND Corr."IdBolsa"=Cod."IdBolsa" AND Corr."IdBolsa"='BCS'
WHERE Cod."CodigoCorredoraFix" IN (:Operadores);