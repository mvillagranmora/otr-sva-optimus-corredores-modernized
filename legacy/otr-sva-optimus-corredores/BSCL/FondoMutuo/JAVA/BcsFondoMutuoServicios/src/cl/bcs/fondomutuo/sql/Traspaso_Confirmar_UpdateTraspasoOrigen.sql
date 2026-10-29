UPDATE "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen"
SET
	--,"IdEstado"						= 'VIG'
	"IdSolicitudAporteRescate" 	= :IdSolicitudAporteRescate
WHERE   "IdDetalleTraspaso" IN (:IdDetalleTraspaso);