UPDATE "BSCL.FondoMutuo.BD::TRA.Traspaso"
SET
 	 "IdEmisor"					=:IdEmisor
	,"IdMotivoTraspaso"			=:IdMotivoTraspaso
	,"IdTipoMovimientoTraspaso"	=:IdTipoMovimientoTraspaso
    ,"IdTipoTraspaso" 			=:IdTipoTraspaso
    ,"Glosa"  					=:Glosa
    ,"FechaHoraSuscripcion"		=:FechaHoraSuscripcion
    ,"IdDivisionPais"			=:IdDivisionPais
    ,"FolioTraspaso"			=:FolioTraspaso
    ,"IdEstado"					=:IdEstado
    ,"Monto"					=:Monto
    ,"FechaModificacion"		=:FechaModificacion
    ,"FechaModificacionRegistro"=CURRENT_UTCTIMESTAMP
    ,"FolioMLT"					=:FolioMLT
    ,"FolioCSC"					=:FolioCSC
    ,"IdEmpleadoEjecutivo"		=:IdEmpleadoEjecutivo
    ,"IdCliente"				=:IdCliente
    ,"IdPortafolio"				=:IdPortafolio
    ,"IdEntidadRecaudadora"		=:IdEntidadRecaudadora
WHERE  "IdTraspaso"= :IdTraspaso