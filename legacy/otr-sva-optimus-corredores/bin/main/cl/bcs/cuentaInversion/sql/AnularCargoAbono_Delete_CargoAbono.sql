DELETE FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" A 
WHERE A."IdInterno" IN (
	SELECT "IdInterno"
	FROM (SELECT ? AS "IdInterno", ? AS "Resultado" FROM DUMMY) 
	WHERE 
	CASE 
		WHEN ? = 'N' AND ? = 'S' AND "Resultado" = 'RAUT' THEN "Resultado"
		WHEN ? = 'N' AND ? = 'N' AND "Resultado" = 'RAUT' THEN 'OK'
		ELSE "Resultado"
	END IN ('RAUT','RECH')
)