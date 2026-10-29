SELECT "IdUso" AS "Valor" , 'Uso' AS "Tipo"
FROM "BSCL.Plataforma.BD::TAS.Uso"
WHERE "IdEmpresa" = :IdEmpresa
UNION
SELECT "Valor" AS "Valor", 'TipoCartera' AS "Tipo" 
FROM "BSCL.Plataforma.BD::TAS.ConfiguracionTasas"
WHERE "NombreEntidad" = 'TIPOCARTERATASAS' AND "IdEmpresa" = :IdEmpresa
UNION
SELECT "Codigo" AS "Valor" , 'TipoCriterio' AS "Tipo"
FROM "BSCL.Plataforma.BD::TAS.TipoCriterioTasa"
where "IdEmpresa" = :IdEmpresa
UNION
SELECT "IdTipoInstrumento" AS "Valor",'TipoInstrumento' AS "Tipo"
FROM "BSCL.Plataforma.BD::INS.TipoInstrumento"