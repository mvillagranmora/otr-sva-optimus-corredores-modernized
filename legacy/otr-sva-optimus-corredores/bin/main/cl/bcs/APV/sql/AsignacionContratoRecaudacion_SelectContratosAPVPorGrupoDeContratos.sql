SELECT
   Contrato."IdCliente" AS "IdCliente"
   ,Contrato."IdContratoSAAPV" AS "IdContratoSAAPV"
   ,PortafolioContrato."OrigenAhorro" AS "TipoContrato"
	,MatrizAgrupador."Codigo" AS "Regimen"
   FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
		LEFT OUTER JOIN "BSCL.APV.BD::APV.Estado" AS Estado 
			ON  Estado."Codigo" = Contrato."IdEstado"
			LEFT OUTER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
				ON  PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
					LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				 		ON Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
							AND Portafolio."IdCliente" = Contrato."IdCliente"
    							AND Portafolio."Proposito" = 'PROPOSITOAPV'
									LEFT OUTER JOIN (SELECT MAT."IdMatrizAgrupadorAPV"
															 , REG."Codigo"
														FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
														INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
																ON REG."IdRegimenTributario" = MAT."IdRegimenTributario"
														INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
																ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
															   AND ProductoAgrupador."IdEstado" = 'VIG'
														INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion
																ON PlanTipoInversion."IdPlanTipoInversion" = MAT."IdPlanTipoInversion"
															   AND PlanTipoInversion."IdEstado" = 'VIG'
														WHERE MAT."IdEstado" = 'VIG'	
														ORDER BY MAT."IdMatrizAgrupadorAPV"
										) AS MatrizAgrupador
    			 						ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"	
										WHERE CONTRATO."IdContratoSAAPV" IN (:IdContratoSAAPV)
										AND Portafolio."IdEstado" <> 'INA'