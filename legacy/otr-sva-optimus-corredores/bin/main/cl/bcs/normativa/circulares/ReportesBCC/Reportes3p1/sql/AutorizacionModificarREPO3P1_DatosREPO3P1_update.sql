UPDATE DAT 
SET  DAT."TipoRegistro"				= CASE WHEN SOL."TipoRegistro" 			IS NOT NULL	THEN SOL."TipoRegistro" 			ELSE DAT."TipoRegistro" 		 END 
	,DAT."RUTReportante"			= CASE WHEN SOL."RUTReportante"			IS NOT NULL	THEN SOL."RUTReportante" 			ELSE DAT."RUTReportante" 		 END
	,DAT."FechaOperacion"			= CASE WHEN SOL."FechaOperacion"		IS NOT NULL THEN SOL."FechaOperacion" 			ELSE DAT."FechaOperacion" 		 END
	,DAT."HoraOperacion"			= CASE WHEN SOL."HoraOperacion"			IS NOT NULL	THEN SOL."HoraOperacion" 			ELSE DAT."HoraOperacion" 		 END
	,DAT."TipoOperacion"			= CASE WHEN SOL."TipoOperacion"			IS NOT NULL THEN SOL."TipoOperacion"			ELSE DAT."TipoOperacion" 		 END
	,DAT."EventoReporte"			= CASE WHEN SOL."EventoReporte"			IS NOT NULL THEN SOL."EventoReporte" 			ELSE DAT."EventoReporte" 		 END
	,DAT."LEIContraparte"			= CASE WHEN SOL."LEIContraparte"		IS NOT NULL	THEN SOL."LEIContraparte" 			ELSE DAT."LEIContraparte" 		 END
	,DAT."NombreContraparte"		= CASE WHEN SOL."NombreContraparte"		IS NOT NULL	THEN SOL."NombreContraparte" 		ELSE DAT."NombreContraparte" 	 END
	,DAT."PlataformaNegociacion"	= CASE WHEN SOL."PlataformaNegociacion"	IS NOT NULL THEN SOL."PlataformaNegociacion"	ELSE DAT."PlataformaNegociacion" END
	,DAT."MontoMonedaComprada"		= CASE WHEN SOL."MontoMonedaComprada"	IS NOT NULL THEN SOL."MontoMonedaComprada" 		ELSE DAT."MontoMonedaComprada"	 END
	,DAT."MontoMonedaVendida"		= CASE WHEN SOL."MontoMonedaVendida"	IS NOT NULL	THEN SOL."MontoMonedaVendida" 		ELSE DAT."MontoMonedaVendida" 	 END
	,DAT."TipoCambio"				= CASE WHEN SOL."TipoCambio"			IS NOT NULL	THEN SOL."TipoCambio" 				ELSE DAT."TipoCambio" 			 END
FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" DAT
INNER JOIN 
(
	SELECT  SOL."IdSolicitudCambio",SOL."IdEntidadOrigen"
			,MAX(CASE WHEN SOL."CampoModificado"='TipoRegistro'	 			THEN SOL."ValorNuevo" ELSE NULL END)  AS "TipoRegistro"
			,MAX(CASE WHEN SOL."CampoModificado"='RUTReportante' 			THEN SOL."ValorNuevo" ELSE NULL	END)  AS "RUTReportante"
			,MAX(CASE WHEN SOL."CampoModificado"='FechaOperacion' 			THEN SOL."ValorNuevo" ELSE NULL END)  AS "FechaOperacion"
			,MAX(CASE WHEN SOL."CampoModificado"='HoraOperacion' 			THEN SOL."ValorNuevo" ELSE NULL END)  AS "HoraOperacion"
			,MAX(CASE WHEN SOL."CampoModificado"='TipoOperacion' 			THEN SOL."ValorNuevo" ELSE NULL END)  AS "TipoOperacion"
			,MAX(CASE WHEN SOL."CampoModificado"='EventoReporte' 			THEN SOL."ValorNuevo" ELSE NULL END)  AS "EventoReporte"
			,MAX(CASE WHEN SOL."CampoModificado"='LEIContraparte' 			THEN SOL."ValorNuevo" ELSE NULL END)  AS "LEIContraparte"
			,MAX(CASE WHEN SOL."CampoModificado"='NombreContraparte' 		THEN SOL."ValorNuevo" ELSE NULL END)  AS "NombreContraparte"  
			,MAX(CASE WHEN SOL."CampoModificado"='PlataformaNegociacion' 	THEN SOL."ValorNuevo" ELSE NULL END)  AS "PlataformaNegociacion"
			,MAX(CASE WHEN SOL."CampoModificado"='MontoMonedaComprada' 		THEN SOL."ValorNuevo" ELSE NULL END)  AS "MontoMonedaComprada"
			,MAX(CASE WHEN SOL."CampoModificado"='MontoMonedaVendida' 		THEN SOL."ValorNuevo" ELSE NULL END)  AS "MontoMonedaVendida"
			,MAX(CASE WHEN SOL."CampoModificado"='TipoCambio' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "TipoCambio" 
		FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" DAT
	INNER JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON (DAT."ID"=cast (SOL."IdEntidadOrigen" as int))
	WHERE DAT."EstadoSICAM"=''
	AND  SOL."IdSolicitudCambio" IN (:IdSolicitudCambio)
	GROUP BY SOL."IdSolicitudCambio",SOL."IdEntidadOrigen"
) AS SOL  ON (DAT."ID"=cast (SOL."IdEntidadOrigen" as int));