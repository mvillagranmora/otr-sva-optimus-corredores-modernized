UPDATE "BSCL.Orden.BD::ORD.Transaccion"
SET 
	"Reservado" 		= :Reservado,
	"IdUsuarioReserva" 	= :IdUsuarioReserva,
	"MotivoReserva" 	= :MotivoReserva,

	"IdGrupoReserva" 	= :IdGrupoReserva
WHERE "IdTransaccion" = :IdTransaccion;
