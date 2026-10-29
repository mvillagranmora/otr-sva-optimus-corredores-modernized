-- HANA-SOURCE: cl/bcs/cuentaInversion/sql/AnularCargoAbono_Delete_CargoAbono.sql
-- H2 needs an inferable type for the positional parameters used in a select list: they are CAST (IdInterno BIGINT, Resultado text).
-- Everything else is the HANA statement unchanged.
DELETE FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" A
WHERE A."IdInterno" IN (
	SELECT "IdInterno"
	FROM (SELECT CAST(? AS BIGINT) AS "IdInterno", CAST(? AS VARCHAR(10)) AS "Resultado" FROM DUMMY)
	WHERE
	CASE
		WHEN ? = 'N' AND ? = 'S' AND "Resultado" = 'RAUT' THEN "Resultado"
		WHEN ? = 'N' AND ? = 'N' AND "Resultado" = 'RAUT' THEN 'OK'
		ELSE "Resultado"
	END IN ('RAUT','RECH')
)
