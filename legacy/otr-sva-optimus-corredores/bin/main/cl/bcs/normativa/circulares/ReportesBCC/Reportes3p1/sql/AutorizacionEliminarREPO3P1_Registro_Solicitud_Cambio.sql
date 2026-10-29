SELECT	
					SOL."IdSolicitudCambio"  AS "IdSolicitudCambio",	
					SOL."IdSecuenciaCambio"  AS	"IdSecuenciaCambio", --- Se crea interno
					'ID'	AS "IdTipoEntidad",	
					REPO.ID AS "IdEntidadOrigen",
					SOL."CampoModificado"  as "CampoModificado",
					SOL."TipoDato"  AS "TipoDato",
					TO_NVARCHAR(REPO."Identificador") AS "ValorAntiguo",
					'' AS "ValorNuevo",	
					SOL."FechaSolicitud" AS "FechaSolicitud",
					SOL."FechaRevision" AS "FechaRevision",
					SOL."IdEstadoAutorizacion"  AS "IdEstadoAutorizacion",
					REPO."Identificador" AS "Identificador",
					COALESCE(E."IdUsuario", 0) AS "IdUsuarioAtendido"
				FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" REPO
				INNER JOIN "BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"= REPO."IdDocumento" AND DOC."Esquema"='REPO3P1')
				INNER JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON (REPO."ID" = cast (SOL."IdEntidadOrigen" as int))	
				LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS E ON E."NumeroOperacionConcepto" = TO_NVARCHAR(SOL."IdSolicitudCambio")
				WHERE REPO."EstadoSICAM"='' 
				AND  SOL."IdSolicitudCambio" IN (:IdSolicitudCambio);