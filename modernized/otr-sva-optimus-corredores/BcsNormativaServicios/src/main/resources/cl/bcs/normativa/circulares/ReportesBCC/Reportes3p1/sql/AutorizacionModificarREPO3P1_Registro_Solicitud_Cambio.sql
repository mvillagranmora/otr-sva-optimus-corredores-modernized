SELECT	
					SOL."IdSolicitudCambio"  AS "IdSolicitudCambio",	
					SOL."IdSecuenciaCambio"  AS	"IdSecuenciaCambio",
					'ID'	AS "IdTipoEntidad",	
					REPO.ID AS "IdEntidadOrigen",
					SOL."CampoModificado"  as "CampoModificado",
					SOL."TipoDato"  AS "TipoDato",
					(CASE SOL."CampoModificado"
											WHEN 'TipoCambio'				THEN IFNULL(CAST (REPO."TipoCambio" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaComprada'		THEN IFNULL(CAST (REPO."MontoMonedaComprada" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaVendida'		THEN IFNULL(CAST (REPO."MontoMonedaVendida" AS NVARCHAR(255)),'')
											WHEN 'PlataformaNegociacion'	THEN IFNULL(CAST (REPO."PlataformaNegociacion" AS NVARCHAR(255)),'')
											WHEN 'NombreContraparte'		THEN IFNULL(CAST (REPO."NombreContraparte" AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte'			THEN IFNULL(CAST (REPO."LEIContraparte" AS NVARCHAR(255)),'')
											WHEN 'EventoReporte'			THEN IFNULL(CAST (REPO."EventoReporte" AS NVARCHAR(255)),'')
											WHEN 'TipoOperacion'			THEN IFNULL(CAST (REPO."TipoOperacion" AS NVARCHAR(255)),'')
											WHEN 'HoraOperacion'			THEN IFNULL(CAST (REPO."HoraOperacion" AS NVARCHAR(255)),'')
											WHEN 'FechaOperacion'			THEN IFNULL(CAST (REPO."FechaOperacion" AS NVARCHAR(255)),'')
											WHEN 'RUTReportante'			THEN IFNULL(CAST (REPO."RUTReportante" AS NVARCHAR(255)),'')
											WHEN 'TipoRegistro'				THEN IFNULL(CAST (REPO."TipoRegistro" AS NVARCHAR(255)),'')
					END) AS "ValorAntiguo",
					(CASE SOL."CampoModificado"
											WHEN 'TipoCambio'	 							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaComprada' 						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaVendida' 						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')	
											WHEN 'PlataformaNegociacion'					THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'NombreContraparte'						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'EventoReporte'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'TipoOperacion'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'HoraOperacion'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'FechaOperacion'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'RUTReportante'							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'TipoRegistro'								THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
					END) AS "ValorNuevo",	
					SOL."FechaSolicitud" AS "FechaSolicitud",
					SOL."FechaRevision" AS "FechaRevision",
					SOL."IdEstadoAutorizacion"  AS "IdEstadoAutorizacion",
					REPO."Identificador"  AS "Identificador",
					COALESCE(E."IdUsuario", 0) AS "IdUsuarioAtendido"
				FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" REPO
				INNER JOIN 	"BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"= REPO."IdDocumento" AND DOC."Esquema"='REPO3P1')
				INNER JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON (REPO."ID" = cast (SOL."IdEntidadOrigen" as int)) AND SOL."ValorAntiguo" <> SOL."ValorNuevo"
				LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS E ON E."NumeroOperacionConcepto" = TO_NVARCHAR(SOL."IdSolicitudCambio")
				WHERE REPO."EstadoSICAM"=''
				AND  SOL."IdSolicitudCambio" IN (:IdSolicitudCambio);