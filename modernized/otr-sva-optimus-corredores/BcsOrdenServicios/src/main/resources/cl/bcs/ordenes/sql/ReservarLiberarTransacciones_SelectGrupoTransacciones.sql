SELECT
	TRA."IdTransaccion",
	TRA."IdGrupoReserva"	
FROM "BSCL.Orden.BD::ORD.Transaccion" AS TRA
WHERE TRA."IdEmpresa" = :IdEmpresa
AND TRA."IdGrupoReserva" LIKE (:IdsGrupoReserva || '%');