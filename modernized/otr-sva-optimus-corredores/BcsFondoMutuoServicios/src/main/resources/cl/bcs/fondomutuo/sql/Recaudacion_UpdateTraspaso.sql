UPDATE "BSCL.FondoMutuo.BD::TRA.Traspaso"
SET
     "IdEstado"					='VIG'
    ,"FechaAceptacion"			=:FechaAceptacion
    ,"FechaAceptacionRegistro"	=CURRENT_UTCTIMESTAMP
WHERE  "IdTraspaso" IN (:IdTraspaso);