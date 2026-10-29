UPDATE "BSCL.APV.BD::APV.OrigenDestinoTransicion"
SET  "OrigenCtaBco" 		= :OrigenCtaBco
	,"SocioNegocioCtaBco"	= :SocioNegocioCtaBco
	,"DestinoCtaBco"		= :DestinoCtaBco
	,"FechaModificacion"	= :FechaModificacion
	,"IdUsuarioModificador"	= :IdUsuarioModificador
	,"IdEstado"				= :IdEstado
WHERE "IdOrigenDestinoTransicion" = :IdOrigenDestinoTransicion;

