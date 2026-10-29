UPDATE "BSCL.FondoMutuo.BD::TRA.Traspaso"
SET
     "IdEstado"					='ANU'
    ,"IdMotivoAnulacion"		=:IdMotivoAnulacion
    ,"FechaAnulacion"			=:FechaAnulacion
    ,"FechaAnulacionRegistro"	=CURRENT_UTCTIMESTAMP
WHERE  "IdTraspaso"= :IdTraspaso