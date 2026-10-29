SELECT
COALESCE("Valor", '0') AS "Valor"
FROM "BSCL.Plataforma.BD::PLA.Configuracion"
WHERE "CodigoParametro"='INS.Art104';