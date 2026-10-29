INSERT INTO "BSCL.Orden.BD::ORD.EventoBitacora"
	( "IdEventoBitacora"
	, "NombreEntidad"
	, "IdEntidad"
	, "IdOrden"
	, "IdAsignacion"
	, "IdTransaccion"
	, "MensajeEventoBitacora"
	, "FechaEventoBitacora"
	, "FechaIngresoRegistro"
	, "IdTipoEventoBitacora")
VALUES
	( :IdEventoBitacora
	, :NombreEntidad
	, :IdEntidad
	, :IdOrden
	, :IdAsignacion
	, :IdTransaccion
	, Upper(:MensajeEventoBitacora)
	, :FechaEventoBitacora
	, CURRENT_TIMESTAMP
	, :IdTipoEventoBitacora);