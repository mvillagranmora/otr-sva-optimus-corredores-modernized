UPDATE
	T1
SET
	T1."ID" = T2."NewID",
	T1."EjecutadoAjustarRescates" = 'S'
FROM "BSCL.Normativa.BD::TRI.DatosDJ1871" T1
INNER JOIN 
(
	SELECT
		ROW_NUMBER () OVER (ORDER BY "ID") AS "NewID"
	   ,"ID"
	   ,"Numero"
	FROM
		"BSCL.Normativa.BD::TRI.DatosDJ1871"
	ORDER BY
	   	"ID"
	   ,"Numero"
) T2 ON T2."Numero" = T1."Numero"
WHERE
	T1."ID" != T2."NewID";