SELECT "FolioMod","FechaMod","Fecha","CodComNuevo","CodVenNuevo"
FROM "BSCL.Orden.BD::MME.MODIFICACION_PALOS_RUEBAS"
WHERE "FechaMod">=:FechaSistema;