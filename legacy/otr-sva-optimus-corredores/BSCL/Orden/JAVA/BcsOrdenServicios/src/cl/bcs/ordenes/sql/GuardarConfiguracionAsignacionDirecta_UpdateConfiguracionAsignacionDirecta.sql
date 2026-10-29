UPDATE "BSCL.Orden.BD::ORD.ConfiguracionAsignacionDirecta" 
SET
	"AsignacionAuto"= :AsignacionAuto,
	"MovimientoFacturarAuto"= :MovimientoFacturarAuto,
	"ComprobanteAuto"= :ComprobanteAuto,
	"FechaModificacion"= :p_FechaSistema,
	"IdUsuarioModifica"= :p_IdUsuario,
	"IdEstado"= 'ACT'
WHERE "IdTipoInstrumento"=:IdTipoInstrumento
AND 	"IdEmpresa"= :p_IdEmpresa
