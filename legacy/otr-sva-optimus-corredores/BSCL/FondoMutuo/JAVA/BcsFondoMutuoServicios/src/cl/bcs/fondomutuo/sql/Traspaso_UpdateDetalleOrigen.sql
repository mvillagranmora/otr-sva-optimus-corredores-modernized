UPDATE "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen"
SET
	 "IdSerieFondo"					= :IdSerieFondo
	,"Cantidad"						= :Cantidad
	,"IdEstado"						= :IdEstado
	,"FechaModificacion" 			= :FechaModificacion
    ,"FechaModificacionRegistro" 	=  CURRENT_UTCTIMESTAMP
    ,"IdRegimenTributario"			= :IdRegimenTributario
WHERE   "IdDetalleTraspaso" =:IdDetalleTraspaso