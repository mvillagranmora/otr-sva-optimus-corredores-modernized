SELECT FA."IdAnexo"
	 , FA."IdEstado"
	 , FA."IdPersonaAgente"
	 , FA."IdTipoCanalIngreso"
	 , FA."FechaFirma"
	 , FA."EsAutorizado"
FROM "BSCL.Plataforma.BD::DOC.FirmaAnexo" FA
	 LEFT JOIN "BSCL.Plataforma.BD::DOC.SolicitudFirmaAnexo" SFA ON (SFA."IdSolicitudFirmaAnexo" = FA."IdSolicitudFirmaAnexo")
WHERE SFA."IdPersonaFirmante" = :IdPersona;