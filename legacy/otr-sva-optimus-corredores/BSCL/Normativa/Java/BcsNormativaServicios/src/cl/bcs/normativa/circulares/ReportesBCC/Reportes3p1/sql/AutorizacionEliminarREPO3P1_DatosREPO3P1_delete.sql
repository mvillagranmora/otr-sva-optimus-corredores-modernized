DELETE FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" DAT
WHERE DAT."EstadoSICAM"=''

AND
      (
		"ID" IN (SELECT cast ("IdEntidadOrigen" as int) from BSCL."BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL WHERE SOL."IdTipoEntidad"='ID' AND SOL."IdSolicitudCambio" IN (:IdSolicitudCambio) )
	  )
	OR
	  (
		"IdDocumento" IN (SELECT cast ("IdEntidadOrigen" as int) from BSCL."BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL WHERE SOL."IdTipoEntidad"='IdDocumento' AND SOL."IdSolicitudCambio" IN (:IdSolicitudCambio) )
	  );
	
