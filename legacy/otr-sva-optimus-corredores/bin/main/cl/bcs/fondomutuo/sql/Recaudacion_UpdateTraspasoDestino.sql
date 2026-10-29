UPDATE "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino"
SET
	--,"IdEstado"						= 'VIG'
	"IdSolicitudAporteRescate" 	= :IdSolicitudAporteRescate
WHERE   "IdDetalleTraspaso" IN (:IdDetalleTraspaso);