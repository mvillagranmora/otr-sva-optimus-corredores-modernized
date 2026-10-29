SELECT DISTINCT "IdPortafolio" FROM  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Porta
INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" Matriz
	ON Porta."IdMatrizAgrupadorAPV" = Matriz."IdMatrizAgrupadorAPV" 
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" Regimen
	ON Regimen."IdRegimenTributario" = Matriz."IdRegimenTributario"
	AND Regimen."Codigo" = 'APV-A'
WHERE Porta."IdPortafolio" IN (:IdPortafolio);