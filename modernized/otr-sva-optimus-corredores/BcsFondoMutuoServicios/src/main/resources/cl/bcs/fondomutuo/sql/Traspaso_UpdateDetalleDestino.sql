UPDATE "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino"
SET 
	 "IdSerieFondo" 				= :IdSerieFondo
	,"Porcentaje"					= :Porcentaje
	,"IdEstado"						= :IdEstado
    ,"FechaModificacion"			= :FechaModificacion
    ,"FechaModificacionRegistro" 	= CURRENT_UTCTIMESTAMP
    ,"IdRegimenFondoMutuo"			= :IdRegimenFondoMutuo
    ,"RunFFMM" 						=:RunFFMM
    ,"NombreFFMM" 					=:NombreFFMM
WHERE  "IdDetalleTraspaso"=:IdDetalleTraspaso