SELECT 
	TIPO_FEC as "tipoFecha",
	DIA as "fecha",
	NUM_REMATE AS "numRemate"
FROM TBUTFECH
WHERE DIA >= :p_fechaSistema  AND TIPO_FEC = 'F';
