SELECT 
	Portafolio."IdPortafolio"
	FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
	INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS Matriz
	ON Matriz."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"  
	AND Portafolio."Proposito" = Matriz."IdPropositoPortafolio"
	WHERE Portafolio."IdCliente" = :IdCliente
	AND Portafolio."Proposito" = :PropositoPortafolio
	AND Portafolio."IdEstado" = 'ACT'
	AND Matriz."IdPropositoPortafolio" = :PropositoPortafolio
	AND Matriz."NombrePlan" <> :NombrePlan
