SELECT P."IdPrograma"
FROM "BSCL.Plataforma.BD::BAT.Programa" P
INNER JOIN "BSCL.Plataforma.BD::BAT.ParametrosPrograma" PP 
	ON P."IdPrograma" = PP."IdPrograma" 
	AND PP."NombreParametro" = 'EsAgenda'
	AND PP."Valor" = 'S'
WHERE P."IdTipoPrograma" = 'NOTIFICARNCG365FFMMPAUTA'
	AND P."idEstado" = 'TER'
	AND TO_DATE(UTCTOLOCAL(p."fechaEjecucion")) = :FechaSistema;