INSERT INTO "BSCL.Orden.BD::ORD.EventoBitacora"
 ("IdEventoBitacora","NombreEntidad","IdOrden","IdAsignacion","IdTransaccion","MensajeEventoBitacora","FechaEventoBitacora","FechaIngresoRegistro","IdTipoEventoBitacora")
 VALUES (
 "BSCL.Orden.BD.Secuencias::EventoBitacora".NEXTVAL,:NombreEntidad,:IdOrden,:IdAsignacion,:IdTransaccion,:MensajeEventoBitacora,:FechaEventoBitacora,CURRENT_UTCTIMESTAMP,:IdTipoEventoBitacora)
 ;
	