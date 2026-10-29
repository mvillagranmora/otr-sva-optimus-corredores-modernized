SELECT Portafolio."IdPortafolio"
FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
LEFT JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS Matriz ON Matriz."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV" AND Portafolio."Proposito" = Matriz."IdPropositoPortafolio"
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" Contrato ON Contrato."IdPortafolio" = Portafolio."IdPortafolio"
WHERE Portafolio."IdCliente" = :IdCliente
AND Portafolio."Proposito" = :Proposito
AND MAtriz."IdMatrizAgrupadorAPV" NOT NULL
AND Matriz."NombrePlan" <> :NombrePlan
AND Contrato."IdEstado" = 'VIG'