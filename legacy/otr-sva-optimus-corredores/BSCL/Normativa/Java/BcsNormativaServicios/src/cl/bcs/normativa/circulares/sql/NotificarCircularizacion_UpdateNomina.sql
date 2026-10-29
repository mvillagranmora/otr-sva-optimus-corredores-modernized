UPDATE "BSCL.Normativa.BD::CIR.Nomina"
SET "IdEvento" = :IdEvento,
	"IdEstadoNotificacion"='ENV',
	 "FechaModificacion"=CURRENT_UTCTIMESTAMP
WHERE "IdNomina" = :IdSecuencia;