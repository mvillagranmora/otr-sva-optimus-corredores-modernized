--  Actualiza estado de los movimientos de ingreso egreso
UPDATE IE    
   SET "IdEstado" 			= 'EMI', 
   	   "IdUsuarioEmision"	= :IdUsuario,
   	   "FechaHoraEmision"	= TO_DATE(:FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE
INNER JOIN "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso" IEG ON IE."IdIngresoEgreso" = IEG."IdIngresoEgreso";