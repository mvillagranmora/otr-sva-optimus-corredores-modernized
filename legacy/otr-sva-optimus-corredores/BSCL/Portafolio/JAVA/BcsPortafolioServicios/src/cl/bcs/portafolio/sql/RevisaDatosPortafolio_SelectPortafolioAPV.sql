SELECT "IdPortafolio"
FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
LEFT JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS Matriz ON Matriz."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV" AND Portafolio."Proposito" = Matriz."IdPropositoPortafolio"
WHERE Portafolio."IdCliente" = :IdCliente
AND Portafolio."Proposito" = :Proposito
AND Matriz."Codigo" = :CodigoRegimen
AND Matriz."NombrePlan" = :NombrePlan
AND Matriz."CodigoAgrupador" = :CodigoAgrupador;