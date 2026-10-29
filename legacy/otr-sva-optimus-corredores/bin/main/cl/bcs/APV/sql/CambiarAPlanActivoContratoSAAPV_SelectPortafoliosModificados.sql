SELECT
	 CON."IdContratoSAAPV"
	,MAT."CodigoAgrupador"
	,MAT."IdMatrizAgrupadorAPV"
	,PORCON."OrigenAhorro"
	,PORCON."IdPortafolio"
	,MAT."IdPropositoPortafolio"
	,PORCON."PeriodoInicio"
	,PORCON."PeriodoTermino"
	,COALESCE(PORCON."MontoPesos",0) AS "MontoPesos"
	,COALESCE(PORCON."MontoUF",0) AS "MontoUF"
	,COALESCE(PORCON."PorcentajeRegimenImponible",0) AS "PorcentajeRegimenImponible"
	,PORCON."Pago"
	,PRD."NombreProductoAgrupador"
	,REG."Codigo"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" CON
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" PORCON
	ON (CON."IdContratoSAAPV" = PORCON."IdContratoSAAPV")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
	ON (PORCON."IdPortafolio" = POR."IdPortafolio" AND
		CON."IdCliente" = POR."IdCliente" AND
		POR."Proposito" = 'PROPOSITOAPV')
--LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MAT
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
	ON (MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV" AND
		MAT."IdEstado" = 'VIG')
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
	ON (REG."IdRegimenTributario" = MAT."IdRegimenTributario" AND
		REG."IdEstado" = 'VIG')
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS PRD
	ON (PRD."IdProductoAgrupador" = MAT."IdProductoAgrupador" AND
		PRD."IdEstado" = 'VIG')
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PTI
	ON (PTI."IdPlanTipoInversion" = MAT."IdPlanTipoInversion" AND
		PTI."IdEstado" = 'VIG')
WHERE CON."IdEmpresa" = :IdEmpresa
  AND CON."IdPlan" = :IdPlan
  AND CON."IdContratoSAAPV" IN (:IdContratoSAAPV)
  AND CON."IdEstado" IN ('VIG','PDI')
  AND POR."IdEstado" <> 'INA'
;