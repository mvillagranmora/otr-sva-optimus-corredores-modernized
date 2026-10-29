SELECT	
					SOL."IdSolicitudCambio"  AS "IdSolicitudCambio",	
					SOL."IdSecuenciaCambio"  AS	"IdSecuenciaCambio",
					'ID'	AS "IdTipoEntidad",	
					REPO.ID AS "IdEntidadOrigen",
					SOL."CampoModificado"  as "CampoModificado",
					SOL."TipoDato"  AS "TipoDato",
					(CASE SOL."CampoModificado"
											WHEN 'TipoRegistro'								THEN IFNULL(CAST (REPO."TipoRegistro" AS NVARCHAR(255)),'')
											WHEN 'EventoReporte'							THEN IFNULL(CAST (REPO."EventoReporte" AS NVARCHAR(255)),'')
											WHEN 'TipoOperacion'							THEN IFNULL(CAST (REPO."TipoOperacion" AS NVARCHAR(255)),'')
											WHEN 'FechaNegociacion'							THEN IFNULL(CAST (REPO."FechaNegociacion" AS NVARCHAR(255)),'')
											WHEN 'HoraNegociacion'							THEN IFNULL(CAST (REPO."HoraNegociacion" AS NVARCHAR(255)),'')
											WHEN 'FechaValutaMonedaRecibida'				THEN IFNULL(CAST (REPO."FechaValutaMonedaRecibida" AS NVARCHAR(255)),'')
											WHEN 'FechaValutaMonedaEntregada'				THEN IFNULL(CAST (REPO."FechaValutaMonedaEntregada" AS NVARCHAR(255)),'')
											WHEN 'Identificador'							THEN IFNULL(CAST (REPO."Identificador" AS NVARCHAR(255)),'')
											WHEN 'RUTContraparte1'							THEN IFNULL(CAST (REPO."RUTContraparte1" AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte1'							THEN IFNULL(CAST (REPO."LEIContraparte1" AS NVARCHAR(255)),'')											
											WHEN 'NombreContraparte1'						THEN IFNULL(CAST (REPO."NombreContraparte1" AS NVARCHAR(255)),'')
											WHEN 'RUTContraparte2'							THEN IFNULL(CAST (REPO."RUTContraparte2" AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte2'							THEN IFNULL(CAST (REPO."LEIContraparte2" AS NVARCHAR(255)),'')
											WHEN 'NombreContraparte2'						THEN IFNULL(CAST (REPO."NombreContraparte2" AS NVARCHAR(255)),'')
											WHEN 'PaisContraparte2'							THEN IFNULL(CAST (REPO."PaisContraparte2" AS NVARCHAR(255)),'')	
											WHEN 'RUTIntermediario'							THEN IFNULL(CAST (REPO."RUTIntermediario" AS NVARCHAR(255)),'')
											WHEN 'PlataformaNegociacion'					THEN IFNULL(CAST (REPO."PlataformaNegociacion" AS NVARCHAR(255)),'')	
											WHEN 'CodigoOperacionCambio'					THEN IFNULL(CAST (REPO."CodigoOperacionCambio" AS NVARCHAR(255)),'')
											WHEN 'PaisMonedaExtranjera'						THEN IFNULL(CAST (REPO."PaisMonedaExtranjera" AS NVARCHAR(255)),'')
											WHEN 'MonedaCompradaRecibida'					THEN IFNULL(CAST (REPO."MonedaCompradaRecibida" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaCompradaRecibida'				THEN IFNULL(CAST (REPO."MontoMonedaCompradaRecibida" AS NVARCHAR(255)),'')
											WHEN 'MonedaVendidaEntregada'					THEN IFNULL(CAST (REPO."MonedaVendidaEntregada" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaVendidaEntregada'				THEN IFNULL(CAST (REPO."MontoMonedaVendidaEntregada" AS NVARCHAR(255)),'')																
											WHEN 'ConvencionTipoCambio'						THEN IFNULL(CAST (REPO."ConvencionTipoCambio" AS NVARCHAR(255)),'')	
											WHEN 'TipoCambio'								THEN IFNULL(CAST (REPO."TipoCambio" AS NVARCHAR(255)),'')
											WHEN 'AfectaDerivados'							THEN IFNULL(CAST (REPO."AfectaDerivados" AS NVARCHAR(255)),'')
											WHEN 'IdentificadorContratoDerivados'			THEN IFNULL(CAST (REPO."IdentificadorContratoDerivados" AS NVARCHAR(255)),'')											
											WHEN 'FechaSuscripcionContrato'					THEN IFNULL(CAST (REPO."FechaSuscripcionContrato" AS NVARCHAR(255)),'')
											WHEN 'HoraSuscripcionContrato'					THEN IFNULL(CAST (REPO."HoraSuscripcionContrato" AS NVARCHAR(255)),'')
											WHEN 'OperacionEmpresaRelacionada'				THEN IFNULL(CAST (REPO."OperacionEmpresaRelacionada" AS NVARCHAR(255)),'')
											WHEN 'NumeroInscripcionObligacionExterior'		THEN IFNULL(CAST (REPO."NumeroInscripcionObligacionExterior" AS NVARCHAR(255)),'')
											WHEN 'NombreFinancistaAporante'					THEN IFNULL(CAST (REPO."NombreFinancistaAporante" AS NVARCHAR(255)),'')
											WHEN 'LEIFinancistaAporante'					THEN IFNULL(CAST (REPO."LEIFinancistaAporante" AS NVARCHAR(255)),'')											
											WHEN 'NombreReceptorInversion'					THEN IFNULL(CAST (REPO."NombreReceptorInversion" AS NVARCHAR(255)),'')
											WHEN 'LEIReceptorInversion'						THEN IFNULL(CAST (REPO."LEIReceptorInversion" AS NVARCHAR(255)),'')
											WHEN 'ModalidadInversionCustodio'				THEN IFNULL(CAST (REPO."ModalidadInversionCustodio" AS NVARCHAR(255)),'')
					END) AS "ValorAntiguo",
					(CASE SOL."CampoModificado"
											WHEN 'TipoRegistro'	 							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'EventoReporte' 							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'TipoOperacion' 							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')	
											WHEN 'FechaNegociacion'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'HoraNegociacion'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'FechaValutaMonedaRecibida'				THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'FechaValutaMonedaEntregada'				THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'Identificador'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'RUTContraparte1'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte1'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')											
											WHEN 'NombreContraparte1'						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'RUTContraparte2'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte2'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'NombreContraparte2'						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'PaisContraparte2'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')	
											WHEN 'RUTIntermediario'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'PlataformaNegociacion'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')	
											WHEN 'CodigoOperacionCambio'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'PaisMonedaExtranjera'						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'MonedaCompradaRecibida'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaCompradaRecibida'				THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'MonedaVendidaEntregada'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaVendidaEntregada'				THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')																
											WHEN 'ConvencionTipoCambio'						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')	
											WHEN 'TipoCambio'								THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'AfectaDerivados'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'IdentificadorContratoDerivados'			THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'FechaSuscripcionContrato'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'HoraSuscripcionContrato'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'OperacionEmpresaRelacionada'				THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'NumeroInscripcionObligacionExterior'		THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'NombreFinancistaAporante'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'LEIFinancistaAporante'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'NombreReceptorInversion'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'LEIReceptorInversion'						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'ModalidadInversionCustodio'				THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
					END) AS "ValorNuevo",	
					SOL."FechaSolicitud" AS "FechaSolicitud",
					SOL."FechaRevision" AS "FechaRevision",
					SOL."IdEstadoAutorizacion"  AS "IdEstadoAutorizacion",
					REPO."Identificador" AS "Identificador",
					COALESCE(E."IdUsuario", 0) AS "IdUsuarioAtendido"
				FROM "BSCL.Normativa.BD::NOR.DatosREPO2P1" REPO
				INNER JOIN 	"BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"= REPO."IdDocumento" AND DOC."Esquema"='REPO2P1')
				INNER JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON (REPO."ID" = cast (SOL."IdEntidadOrigen" as int)) AND SOL."ValorAntiguo" <> SOL."ValorNuevo"
				LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS E ON E."NumeroOperacionConcepto" = TO_NVARCHAR(SOL."IdSolicitudCambio")
				WHERE REPO."EstadoSICAM"=''
				AND  SOL."IdSolicitudCambio" IN (:IdSolicitudCambio);
