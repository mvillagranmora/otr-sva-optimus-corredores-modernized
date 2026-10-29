UPDATE "BSCL.Mesa.BD::MES.Operacion"
SET "TipoInformeGenerado" = CASE WHEN TRIM("TipoInformeGenerado")='' THEN  :TipoInformeGenerado ELSE 'TODOS' END 
WHERE "IdEstado" IN ('VIG','ANT','LIQ')
AND "IdOperacion"= :IdOperacion
AND "IdEmpresa"= :IdEmpresa;