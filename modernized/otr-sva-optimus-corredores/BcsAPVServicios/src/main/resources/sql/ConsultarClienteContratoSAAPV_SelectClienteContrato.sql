SELECT
	 CON."IdPersona"
	,CON."DocumentoIdentidadCliente" AS "RutCliente"
	,CON."NombreCliente"||' '||CON."ApellidoPaternoCliente"||' '||CON."ApellidoMaternoCliente" AS "NombrePersona"
	,PORCON."IdPortafolio"
	,PRD."NombreProductoAgrupador" AS "Producto"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PORCON
	ON (CON."IdContratoSAAPV" = PORCON."IdContratoSAAPV" AND
		CON."IdEmpresa" = :IdEmpresa AND
		CON."IdPlan" = :IdPlan AND
		(:FiltroIdPersona = 0 OR CON."IdPersona" IN (:IdPersona)) AND
		CON."IdEstado" IN ('VIG'))
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
WHERE POR."IdEstado" <> 'INA';