SELECT  DISTINCT "DET"."IdentServicio"
				,"DET"."IdCargaMasivaDetallePac"
				,'C' 					AS "EstadoProcesamiento"
				,"DET"."MontoCargo" 	AS "MontoDeposito"
				,"DET"."Estado"
				,"DET"."TipoArchivo"
				,"DET"."EstadoCargo" 	AS "EstadoCargoBanco"
FROM  "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC" 					AS "DET"
INNER JOIN "BSCL.Tesoreria.BD::PAC.LogMandatoPAC"					AS "LPA"		ON ("DET"."IdentServicio"		= "LPA"."IdentServicio" AND
  																						"LPA"."EstadoProcesamiento"	= :EstadoProcesamiento  AND
  																						YEAR("LPA"."FechaSistema")  = YEAR(:fechaSistema)	AND
  																						MONTH("LPA"."FechaSistema") = MONTH(:fechaSistema)) 
INNER JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" 					AS "PAC"		ON ("PAC"."IdMandatoPAC"		= "LPA"."IdMandatoPAC")
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV"  						AS "SAP" ON (PAC."FolioSAAPV" 	= SAP."FolioContratoSAAPV"
																				AND "SAP"."DiaDescuento" = :DiaDescuento)
LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" 	AS "CSC" ON (PAC."FolioCSC" 	= CSC."FolioCSC"
																			AND "CSC"."TipoPago" 		= 'PAC'
																			AND "CSC"."DiaDescuento" = :DiaDescuento)
WHERE 		 "DET"."Estado" 			= 'ACT' 
		AND "DET"."TipoArchivo" 		= 'REN'
		AND "DET"."TipoRegistro" 		= 'D'
		AND (COALESCE("SAP"."DiaDescuento","CSC"."DiaDescuento") = :DiaDescuento);
