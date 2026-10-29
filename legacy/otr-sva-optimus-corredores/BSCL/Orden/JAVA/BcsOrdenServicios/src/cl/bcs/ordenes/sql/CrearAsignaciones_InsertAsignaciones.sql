/********************************************************** 
Proposito	: INSERTAR ASIGNACIONES
autor		: RU
fecha		: 01/02/2017
***********************************************************/
INSERT INTO "BSCL.Orden.BD::ORD.Asignacion"(
"IdAsignacion","FechaAsignacion","FechaIngresoRegistro","CantidadAsignada","MontoAsignado","MontoAsignadoLocal","MontoAsignadoLiquidacion","TipoCambio",
"ValorRescateAsignado","IdEstadoAsignacion","IdTransaccion","IdOrden","EsAPlazo","IdLibroOrdenes","IdAsignacionRef","FolioCartera","Paridad","FechaTipoCambio"
,"FechaParidad","CodigoIFRS", "MontoAsignadoInteres", "IdAsignacionGrupo", "IdAsignacionMasiva", "IdTipoAsignacion","AplicaComisionAsignacionMasiva")
VALUES (:IdAsignacion,:FechaAsignacion,CURRENT_UTCTIMESTAMP,:CantidadAsignada,:MontoAsignado,:MontoAsignadoLocal,:MontoAsignadoLiquidacion,:TipoCambio,
:ValorRescateAsignado,:IdEstadoAsignacion,:IdTransaccion,:IdOrden,:EsAPlazo,:IdLibroOrdenes,:IdAsignacionRef,:FolioCartera,:Paridad,:FechaTipoCambio
,:FechaParidad,:CodigoIFRS,:MontoAsignadoInteres, :IdAsignacionGrupo, :IdAsignacionMasiva, :IdTipoAsignacion, :AplicaComisionAsignacionMasiva);