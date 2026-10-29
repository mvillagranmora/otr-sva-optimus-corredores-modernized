SELECT 
	  PCSAAPV."IdContratoSAAPV"
	, PCSAAPV."IdPortafolio"
	, MATAGR."Codigo" AS "Regimen"
FROM "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PCSAAPV
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = PCSAAPV."IdPortafolio"
INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS MATAGR ON MATAGR."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV" AND MATAGR."IdPropositoPortafolio" = POR."Proposito"
WHERE PCSAAPV."IdContratoSAAPV" IN (:IdContratoSAAPV) AND PCSAAPV."IdEstado" = 'VIG';