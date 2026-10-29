SELECT
	COALESCE(TO_VARCHAR(MAX("FechaRespaldo"), 'YYYY-MM-DD'), '0001-01-01') AS "FechaUltimoRespaldo"
FROM
	"BSCL.Custodia.BD::HIS.Cartera"
WHERE "FechaRespaldo" <= :p_Fecha;