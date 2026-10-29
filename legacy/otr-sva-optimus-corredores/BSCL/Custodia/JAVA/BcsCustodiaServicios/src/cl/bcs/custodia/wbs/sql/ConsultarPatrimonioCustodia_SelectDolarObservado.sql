SELECT
DEF."IdParametroFinanciero"
,PAR."Fecha"
,PAR."Valor"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DEF
INNER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON PAR."IdParametroFinanciero" = DEF."IdParametroFinanciero"
WHERE DEF."IdParametroFinanciero" = :IdParametroFinanciero AND PAR."Fecha" = :FechaConsulta;