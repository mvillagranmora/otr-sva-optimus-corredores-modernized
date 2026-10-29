UPDATE DAT 
SET  DAT."TipoRegistro"						= CASE WHEN SOL."TipoRegistro" 					IS NOT NULL	THEN SOL."TipoRegistro" 					ELSE DAT."TipoRegistro" 		 END 
	,DAT."RUTEntidadMCF"					= CASE WHEN SOL."RUTEntidadMCF"					IS NOT NULL	THEN SOL."RUTEntidadMCF" 					ELSE DAT."RUTEntidadMCF" 		 END
	,DAT."Fecha"							= CASE WHEN SOL."Fecha"							IS NOT NULL THEN SOL."Fecha" 							ELSE DAT."Fecha" 		 		 END
	,DAT."SaldoCuentaCambioConversion"		= CASE WHEN SOL."SaldoCuentaCambioConversion"	IS NOT NULL	THEN SOL."SaldoCuentaCambioConversion" 		ELSE DAT."SaldoCuentaCambioConversion" 		 END
	,DAT."SaldoMonedaOriginal"				= CASE WHEN SOL."SaldoMonedaOriginal"			IS NOT NULL THEN SOL."SaldoMonedaOriginal"				ELSE DAT."SaldoMonedaOriginal" 	 END
	,DAT."MonedaSaldo"						= CASE WHEN SOL."MonedaSaldo"					IS NOT NULL THEN SOL."MonedaSaldo" 						ELSE DAT."MonedaSaldo" 		 	 END
	,DAT."Paridad"							= CASE WHEN SOL."Paridad"						IS NOT NULL	THEN SOL."Paridad" 							ELSE DAT."Paridad" 				 END
	
FROM "BSCL.Normativa.BD::NOR.DatosREPO2P2" DAT
INNER JOIN 
(
	SELECT  SOL."IdSolicitudCambio",SOL."IdEntidadOrigen"
			,MAX(CASE WHEN SOL."CampoModificado"='TipoRegistro'	 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "TipoRegistro"
			,MAX(CASE WHEN SOL."CampoModificado"='RUTEntidadMCF' 				THEN SOL."ValorNuevo" ELSE NULL	END)  AS "RUTEntidadMCF"
			,MAX(CASE WHEN SOL."CampoModificado"='Fecha' 						THEN SOL."ValorNuevo" ELSE NULL END)  AS "Fecha"
			,MAX(CASE WHEN SOL."CampoModificado"='SaldoCuentaCambioConversion' 	THEN SOL."ValorNuevo" ELSE NULL END)  AS "SaldoCuentaCambioConversion"
			,MAX(CASE WHEN SOL."CampoModificado"='SaldoMonedaOriginal' 			THEN SOL."ValorNuevo" ELSE NULL END)  AS "SaldoMonedaOriginal"
			,MAX(CASE WHEN SOL."CampoModificado"='MonedaSaldo' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "MonedaSaldo"
			,MAX(CASE WHEN SOL."CampoModificado"='Paridad' 						THEN SOL."ValorNuevo" ELSE NULL END)  AS "Paridad"
			
		FROM "BSCL.Normativa.BD::NOR.DatosREPO2P2" DAT
	INNER JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON (DAT."ID"=cast (SOL."IdEntidadOrigen" as int))
	WHERE DAT."EstadoSICAM"=''
	AND  SOL."IdSolicitudCambio" IN (:IdSolicitudCambio)
	GROUP BY SOL."IdSolicitudCambio",SOL."IdEntidadOrigen"
) AS SOL  ON (DAT."ID"=cast (SOL."IdEntidadOrigen" as int));