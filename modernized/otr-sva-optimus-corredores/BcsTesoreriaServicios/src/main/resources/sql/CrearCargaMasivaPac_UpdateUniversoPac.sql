UPDATE PAC
SET PAC."EstadoCargo" = 'ACT'    
FROM  "BSCL.Tesoreria.BD::PAC.MandatoPAC" PAC
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV"  AS SAP 
		ON (PAC."FolioSAAPV" 	= SAP."FolioContratoSAAPV"
			AND "SAP"."IdEstado"	   ='VIG')
LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" 	AS CSC 
		ON (PAC."FolioCSC" 	= CSC."FolioCSC"
			AND "CSC"."Estado"	   		= 'ING'
			AND "CSC"."TipoPago" 		= 'PAC')
WHERE PAC."IdentServicio" = :IdentServicio AND 
	PAC."Estado" = 'ING' AND 
	PAC."EstadoCargo" = 'INA' AND
	(COALESCE("SAP"."IdEstado",'') = 'VIG' OR
	COALESCE("CSC"."Estado",'') = 'ING');