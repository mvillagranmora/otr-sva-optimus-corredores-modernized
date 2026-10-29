SELECT
	Portafolio."IdPortafolio"
FROM
	"BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
INNER JOIN
	"BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupadorAPV ON MatrizAgrupadorAPV."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE
	Portafolio."IdCliente" = :p_IdCliente
AND Portafolio."IdMatrizAgrupadorAPV" = :p_IdMatrizAgrupadorAPV
AND MatrizAgrupadorAPV."IdRegimenTributario" = :p_IdRegimenTributario;