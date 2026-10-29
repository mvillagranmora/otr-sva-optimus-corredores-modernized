SELECT	
					SOL."IdSolicitudCambio"  AS "IdSolicitudCambio",	
					SOL."IdSecuenciaCambio"  AS	"IdSecuenciaCambio",
					'ID'	AS "IdTipoEntidad",	
					REPO.ID AS "IdEntidadOrigen",
					SOL."CampoModificado"  as "CampoModificado",
					SOL."TipoDato"  AS "TipoDato",
					(CASE SOL."CampoModificado"
											WHEN 'TipoRegistro'								THEN IFNULL(CAST (REPO."TipoRegistro" AS NVARCHAR(255)),'')
											WHEN 'RUTEntidadMCF'							THEN IFNULL(CAST (REPO."RUTEntidadMCF" AS NVARCHAR(255)),'')
											WHEN 'Fecha'									THEN IFNULL(CAST (REPO."Fecha" AS NVARCHAR(255)),'')
											WHEN 'SaldoCuentaCambioConversion'				THEN IFNULL(CAST (REPO."SaldoCuentaCambioConversion" AS NVARCHAR(255)),'')
											WHEN 'SaldoMonedaOriginal'						THEN IFNULL(CAST (REPO."SaldoMonedaOriginal" AS NVARCHAR(255)),'')
											WHEN 'MonedaSaldo'								THEN IFNULL(CAST (REPO."MonedaSaldo" AS NVARCHAR(255)),'')
											WHEN 'Paridad'									THEN IFNULL(CAST (REPO."Paridad" AS NVARCHAR(255)),'')
					END) AS "ValorAntiguo",
					(CASE SOL."CampoModificado"
											WHEN 'TipoRegistro'	 							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'RUTEntidadMCF' 							THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'Fecha' 									THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')	
											WHEN 'SaldoCuentaCambioConversion'				THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'SaldoMonedaOriginal'						THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'MonedaSaldo'								THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
											WHEN 'Paridad'									THEN IFNULL(CAST (SOL."ValorNuevo" AS NVARCHAR(255)),'')
					END) AS "ValorNuevo",	
					SOL."FechaSolicitud" AS "FechaSolicitud",
					SOL."FechaRevision" AS "FechaRevision",
					SOL."IdEstadoAutorizacion"  AS "IdEstadoAutorizacion",
					TO_NVARCHAR(REPO."IdCargoAbonoMovimiento")  AS "IdCargoAbonoMovimiento",
					COALESCE(E."IdUsuario", 0) AS "IdUsuarioAtendido"
				FROM "BSCL.Normativa.BD::NOR.DatosREPO2P2" REPO
				INNER JOIN 	"BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"= REPO."IdDocumento" AND DOC."Esquema"='REPO2P2')
				INNER JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON (REPO."ID" = cast (SOL."IdEntidadOrigen" as int)) AND SOL."ValorAntiguo" <> SOL."ValorNuevo"
				LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS E ON E."NumeroOperacionConcepto" = TO_NVARCHAR(SOL."IdSolicitudCambio")
				WHERE REPO."EstadoSICAM"=''
				AND  SOL."IdSolicitudCambio" IN (:IdSolicitudCambio);