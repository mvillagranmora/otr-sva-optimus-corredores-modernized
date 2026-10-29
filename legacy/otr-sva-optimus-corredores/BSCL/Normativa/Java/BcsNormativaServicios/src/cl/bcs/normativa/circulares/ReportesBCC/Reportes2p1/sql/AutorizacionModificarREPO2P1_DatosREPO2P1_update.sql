UPDATE DAT 
SET  DAT."TipoRegistro"								= CASE WHEN SOL."TipoRegistro" 							IS NOT NULL	THEN SOL."TipoRegistro" 							ELSE DAT."TipoRegistro" 		 					END 
	,DAT."EventoReporte"							= CASE WHEN SOL."EventoReporte"							IS NOT NULL	THEN SOL."EventoReporte" 							ELSE DAT."EventoReporte" 		 					END
	,DAT."TipoOperacion"							= CASE WHEN SOL."TipoOperacion"							IS NOT NULL THEN SOL."TipoOperacion" 							ELSE DAT."TipoOperacion" 		 					END
	,DAT."FechaNegociacion"							= CASE WHEN SOL."FechaNegociacion"						IS NOT NULL	THEN SOL."FechaNegociacion"							ELSE DAT."FechaNegociacion"		 					END
	,DAT."HoraNegociacion"							= CASE WHEN SOL."HoraNegociacion"						IS NOT NULL THEN SOL."HoraNegociacion"							ELSE DAT."HoraNegociacion" 		 					END
	,DAT."FechaValutaMonedaRecibida"				= CASE WHEN SOL."FechaValutaMonedaRecibida"				IS NOT NULL THEN SOL."FechaValutaMonedaRecibida" 				ELSE DAT."FechaValutaMonedaRecibida" 		 		END
	,DAT."FechaValutaMonedaEntregada"				= CASE WHEN SOL."FechaValutaMonedaEntregada"			IS NOT NULL	THEN SOL."FechaValutaMonedaEntregada" 				ELSE DAT."FechaValutaMonedaEntregada" 		 		END
	,DAT."Identificador"							= CASE WHEN SOL."Identificador"							IS NOT NULL	THEN SOL."Identificador" 							ELSE DAT."Identificador" 	 						END
	,DAT."RUTContraparte1"							= CASE WHEN SOL."RUTContraparte1"						IS NOT NULL THEN SOL."RUTContraparte1"							ELSE DAT."RUTContraparte1" 							END
	,DAT."LEIContraparte1"							= CASE WHEN SOL."LEIContraparte1"						IS NOT NULL THEN SOL."LEIContraparte1" 							ELSE DAT."LEIContraparte1"	 						END
	,DAT."NombreContraparte1"						= CASE WHEN SOL."NombreContraparte1"					IS NOT NULL	THEN SOL."NombreContraparte1" 						ELSE DAT."NombreContraparte1" 	 					END
	,DAT."RUTContraparte2"							= CASE WHEN SOL."RUTContraparte2"						IS NOT NULL	THEN SOL."RUTContraparte2" 							ELSE DAT."RUTContraparte2" 			 				END
	,DAT."LEIContraparte2"							= CASE WHEN SOL."LEIContraparte2"						IS NOT NULL	THEN SOL."LEIContraparte2" 							ELSE DAT."LEIContraparte2" 		 					END
	,DAT."NombreContraparte2"						= CASE WHEN SOL."NombreContraparte2"					IS NOT NULL THEN SOL."NombreContraparte2" 						ELSE DAT."NombreContraparte2" 						END
	,DAT."PaisContraparte2"							= CASE WHEN SOL."PaisContraparte2"						IS NOT NULL	THEN SOL."PaisContraparte2"							ELSE DAT."PaisContraparte2"		 					END
	,DAT."RUTIntermediario"							= CASE WHEN SOL."RUTIntermediario"						IS NOT NULL	THEN SOL."RUTIntermediario" 						ELSE DAT."RUTIntermediario" 		 				END
	,DAT."PlataformaNegociacion"					= CASE WHEN SOL."PlataformaNegociacion"					IS NOT NULL THEN SOL."PlataformaNegociacion" 					ELSE DAT."PlataformaNegociacion" 		 			END
	,DAT."CodigoOperacionCambio"					= CASE WHEN SOL."CodigoOperacionCambio"					IS NOT NULL	THEN SOL."CodigoOperacionCambio"					ELSE DAT."CodigoOperacionCambio"		 			END
	,DAT."PaisMonedaExtranjera"						= CASE WHEN SOL."PaisMonedaExtranjera"					IS NOT NULL	THEN SOL."PaisMonedaExtranjera" 					ELSE DAT."PaisMonedaExtranjera" 		 			END
	,DAT."MonedaCompradaRecibida"					= CASE WHEN SOL."MonedaCompradaRecibida"				IS NOT NULL THEN SOL."MonedaCompradaRecibida" 					ELSE DAT."MonedaCompradaRecibida" 		 			END
	,DAT."MontoMonedaCompradaRecibida"				= CASE WHEN SOL."MontoMonedaCompradaRecibida"			IS NOT NULL	THEN SOL."MontoMonedaCompradaRecibida"				ELSE DAT."MontoMonedaCompradaRecibida"		 		END
	,DAT."MonedaVendidaEntregada"					= CASE WHEN SOL."MonedaVendidaEntregada"				IS NOT NULL	THEN SOL."MonedaVendidaEntregada" 					ELSE DAT."MonedaVendidaEntregada" 		 			END
	,DAT."MontoMonedaVendidaEntregada"				= CASE WHEN SOL."MontoMonedaVendidaEntregada"			IS NOT NULL THEN SOL."MontoMonedaVendidaEntregada" 				ELSE DAT."MontoMonedaVendidaEntregada" 		 		END
	,DAT."ConvencionTipoCambio"						= CASE WHEN SOL."ConvencionTipoCambio"					IS NOT NULL	THEN SOL."ConvencionTipoCambio"						ELSE DAT."ConvencionTipoCambio"		 				END
	,DAT."TipoCambio"								= CASE WHEN SOL."TipoCambio"							IS NOT NULL	THEN SOL."TipoCambio" 								ELSE DAT."TipoCambio" 		 						END
	,DAT."AfectaDerivados"							= CASE WHEN SOL."AfectaDerivados"						IS NOT NULL THEN SOL."AfectaDerivados" 							ELSE DAT."AfectaDerivados" 		 					END
	,DAT."IdentificadorContratoDerivados"			= CASE WHEN SOL."IdentificadorContratoDerivados"		IS NOT NULL	THEN SOL."IdentificadorContratoDerivados"			ELSE DAT."IdentificadorContratoDerivados"		 	END
	,DAT."FechaSuscripcionContrato"					= CASE WHEN SOL."FechaSuscripcionContrato"				IS NOT NULL	THEN SOL."FechaSuscripcionContrato" 				ELSE DAT."FechaSuscripcionContrato" 		 		END
	,DAT."HoraSuscripcionContrato"					= CASE WHEN SOL."HoraSuscripcionContrato"				IS NOT NULL THEN SOL."HoraSuscripcionContrato" 					ELSE DAT."HoraSuscripcionContrato" 		 			END
	,DAT."OperacionEmpresaRelacionada"				= CASE WHEN SOL."OperacionEmpresaRelacionada"			IS NOT NULL	THEN SOL."OperacionEmpresaRelacionada"				ELSE DAT."OperacionEmpresaRelacionada"		 		END
	,DAT."NumeroInscripcionObligacionExterior"		= CASE WHEN SOL."NumeroInscripcionObligacionExterior"	IS NOT NULL	THEN SOL."NumeroInscripcionObligacionExterior" 		ELSE DAT."NumeroInscripcionObligacionExterior" 		END
	,DAT."NombreFinancistaAporante"					= CASE WHEN SOL."NombreFinancistaAporante"				IS NOT NULL THEN SOL."NombreFinancistaAporante" 				ELSE DAT."NombreFinancistaAporante" 		 		END
	,DAT."LEIFinancistaAporante"					= CASE WHEN SOL."LEIFinancistaAporante"					IS NOT NULL	THEN SOL."LEIFinancistaAporante"					ELSE DAT."LEIFinancistaAporante"		 			END
	,DAT."NombreReceptorInversion"					= CASE WHEN SOL."NombreReceptorInversion"				IS NOT NULL	THEN SOL."NombreReceptorInversion"					ELSE DAT."NombreReceptorInversion" 	 				END
	,DAT."LEIReceptorInversion"						= CASE WHEN SOL."LEIReceptorInversion"					IS NOT NULL THEN SOL."LEIReceptorInversion" 					ELSE DAT."LEIReceptorInversion" 		 			END
	,DAT."ModalidadInversionCustodio"				= CASE WHEN SOL."ModalidadInversionCustodio"			IS NOT NULL	THEN SOL."ModalidadInversionCustodio"				ELSE DAT."ModalidadInversionCustodio" 	 			END
FROM "BSCL.Normativa.BD::NOR.DatosREPO2P1" DAT
INNER JOIN 
(
	SELECT  SOL."IdSolicitudCambio",SOL."IdEntidadOrigen"
			,MAX(CASE WHEN SOL."CampoModificado"='TipoRegistro'	 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "TipoRegistro"
			,MAX(CASE WHEN SOL."CampoModificado"='EventoReporte' 							THEN SOL."ValorNuevo" ELSE NULL	END)  AS "EventoReporte"
			,MAX(CASE WHEN SOL."CampoModificado"='TipoOperacion' 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "TipoOperacion"
			,MAX(CASE WHEN SOL."CampoModificado"='FechaNegociacion' 						THEN SOL."ValorNuevo" ELSE NULL END)  AS "FechaNegociacion"
			,MAX(CASE WHEN SOL."CampoModificado"='HoraNegociacion' 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "HoraNegociacion"
			,MAX(CASE WHEN SOL."CampoModificado"='FechaValutaMonedaRecibida' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "FechaValutaMonedaRecibida"
			,MAX(CASE WHEN SOL."CampoModificado"='FechaValutaMonedaEntregada' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "FechaValutaMonedaEntregada"
			,MAX(CASE WHEN SOL."CampoModificado"='Identificador' 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "Identificador"  
			,MAX(CASE WHEN SOL."CampoModificado"='RUTContraparte1' 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "RUTContraparte1"
			,MAX(CASE WHEN SOL."CampoModificado"='LEIContraparte1' 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "LEIContraparte1"
			,MAX(CASE WHEN SOL."CampoModificado"='NombreContraparte1' 						THEN SOL."ValorNuevo" ELSE NULL END)  AS "NombreContraparte1"
			,MAX(CASE WHEN SOL."CampoModificado"='RUTContraparte2' 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "RUTContraparte2" 
			,MAX(CASE WHEN SOL."CampoModificado"='LEIContraparte2' 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "LEIContraparte2"
			,MAX(CASE WHEN SOL."CampoModificado"='NombreContraparte2' 						THEN SOL."ValorNuevo" ELSE NULL END)  AS "NombreContraparte2"
			,MAX(CASE WHEN SOL."CampoModificado"='PaisContraparte2' 						THEN SOL."ValorNuevo" ELSE NULL END)  AS "PaisContraparte2"
			,MAX(CASE WHEN SOL."CampoModificado"='RUTIntermediario' 						THEN SOL."ValorNuevo" ELSE NULL END)  AS "RUTIntermediario"
			,MAX(CASE WHEN SOL."CampoModificado"='PlataformaNegociacion' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "PlataformaNegociacion"
			,MAX(CASE WHEN SOL."CampoModificado"='CodigoOperacionCambio' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "CodigoOperacionCambio"  
			,MAX(CASE WHEN SOL."CampoModificado"='PaisMonedaExtranjera' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "PaisMonedaExtranjera"
			,MAX(CASE WHEN SOL."CampoModificado"='MonedaCompradaRecibida' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "MonedaCompradaRecibida"
			,MAX(CASE WHEN SOL."CampoModificado"='MontoMonedaCompradaRecibida' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "MontoMonedaCompradaRecibida"
			,MAX(CASE WHEN SOL."CampoModificado"='MonedaVendidaEntregada' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "MonedaVendidaEntregada" 
			,MAX(CASE WHEN SOL."CampoModificado"='MontoMonedaVendidaEntregada' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "MontoMonedaVendidaEntregada"
			,MAX(CASE WHEN SOL."CampoModificado"='ConvencionTipoCambio' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "ConvencionTipoCambio"
			,MAX(CASE WHEN SOL."CampoModificado"='TipoCambio' 								THEN SOL."ValorNuevo" ELSE NULL END)  AS "TipoCambio"
			,MAX(CASE WHEN SOL."CampoModificado"='AfectaDerivados' 							THEN SOL."ValorNuevo" ELSE NULL END)  AS "AfectaDerivados"
			,MAX(CASE WHEN SOL."CampoModificado"='IdentificadorContratoDerivados' 			THEN SOL."ValorNuevo" ELSE NULL END)  AS "IdentificadorContratoDerivados"
			,MAX(CASE WHEN SOL."CampoModificado"='FechaSuscripcionContrato' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "FechaSuscripcionContrato"  
			,MAX(CASE WHEN SOL."CampoModificado"='HoraSuscripcionContrato' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "HoraSuscripcionContrato"
			,MAX(CASE WHEN SOL."CampoModificado"='OperacionEmpresaRelacionada' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "OperacionEmpresaRelacionada"
			,MAX(CASE WHEN SOL."CampoModificado"='NumeroInscripcionObligacionExterior' 		THEN SOL."ValorNuevo" ELSE NULL END)  AS "NumeroInscripcionObligacionExterior"
			,MAX(CASE WHEN SOL."CampoModificado"='NombreFinancistaAporante' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "NombreFinancistaAporante" 
			,MAX(CASE WHEN SOL."CampoModificado"='LEIFinancistaAporante' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "LEIFinancistaAporante" 
			,MAX(CASE WHEN SOL."CampoModificado"='NombreReceptorInversion' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "NombreReceptorInversion" 
			,MAX(CASE WHEN SOL."CampoModificado"='LEIReceptorInversion' 					THEN SOL."ValorNuevo" ELSE NULL END)  AS "LEIReceptorInversion" 
			,MAX(CASE WHEN SOL."CampoModificado"='ModalidadInversionCustodio' 				THEN SOL."ValorNuevo" ELSE NULL END)  AS "ModalidadInversionCustodio" 
		FROM "BSCL.Normativa.BD::NOR.DatosREPO2P1" DAT
	INNER JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON (DAT."ID"=cast (SOL."IdEntidadOrigen" as int))
	WHERE DAT."EstadoSICAM"=''
	AND  SOL."IdSolicitudCambio" IN (:IdSolicitudCambio)
	GROUP BY SOL."IdSolicitudCambio",SOL."IdEntidadOrigen"
) AS SOL  ON (DAT."ID"=cast (SOL."IdEntidadOrigen" as int));

