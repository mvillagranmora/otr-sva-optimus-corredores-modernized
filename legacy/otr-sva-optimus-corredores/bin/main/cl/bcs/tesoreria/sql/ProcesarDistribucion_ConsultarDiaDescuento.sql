SELECT TOP 1
   (COALESCE(COALESCE(SAP."DiaDescuento",CSC."DiaDescuento"),5)) AS "DiaDescuento" 
FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC"  		AS PAC
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV"  AS SAP ON (PAC."FolioSAAPV" 	= SAP."FolioContratoSAAPV"
															AND "SAP"."IdEstado"	   NOT IN('ANU','RVC'))
LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" 	AS CSC ON (PAC."FolioCSC" 	= CSC."FolioCSC"
																				AND "CSC"."Estado"	   NOT IN('ANU','RVC')
																				AND "CSC"."TipoPago" 		= 'PAC')
WHERE PAC."EstadoCargo" = 'ACT';