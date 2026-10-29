UPDATE "BSCL.Orden.BD::ORD.ConfiguracionShortName" 
SET
	"ShortName"= :p_NewShortName,
	"AsignacionAuto"= :AsignacionAuto,
	"MovimientoFacturarAuto"= :MovimientoFacturarAuto,
	"ComprobanteAuto"= :ComprobanteAuto,
	"FechaHoraModificacion"= :p_FechaSistema,
	"UsuarioModificador"= :p_IdUsuario
	
WHERE "IdTipoInstrumento"=:IdTipoInstrumento
AND "ShortName"= :p_OldShortName
