SET SCHEMA "BSCL"; 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.ConceptoOrden"; 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 1,'CMACNA','COMACN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 2,'CMACEX','COMACX'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 3,'CMCFIN','COMCFI'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 4,'CMCFMN','COMCFM'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 5,'CMCFEX','COMCFX'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 6,'CMETFN','COMETN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 7,'CMETFX','COMETX'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 8,'CMDVFU','COMFUT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 9,'CMINFI','COMIIF'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 10,'CMACMI','COMMIL'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 11,'CMMONE','COMMON'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 12,'CMOSAC','COMOSA'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 13,'CMINRF','COMRF'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 14,'CMSIMU','COMSIM'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 15,'VNACNA','VENACN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 16,'VNACEX','VENACX'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 17,'VNCFIN','VENCFI'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 18,'VNCFMN','VENCFM'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 19,'VNCFEX','VENCFX'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 20,'VNETFN','VENETN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 21,'VNETFX','VENETX'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 22,'VNDVFU','VENFUT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 23,'VNINFI','VENIIF'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 24,'VNACMI','VENMIL'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 25,'VNMONE','VENMON'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 26,'VNOSAC','VENOSA'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 27,'VNINRF','VENRF'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden"  ( "IdConceptoOrden","IdConcepto","IdTipoOperacion") VALUES ( 28,'VNSIMU','VENSIM'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"; 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 69,'MAN','AGE','ACN','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 69,'MAN','TRA','ACN','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','ACN','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','AGE','ACN','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'RUA','AGE','ACN','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','AGE','SIM','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','SIM','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','BCP','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','ACX','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','BCU','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','PAG','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','BEM','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','LTH','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','PRC','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','CER','BCS','N','N',0E-8); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"  ( "IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden") VALUES ( 1,'MAN','TRA','ZER','BCS','N','N',0E-8); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.ConfiguracionHorarioDistribucion"; 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionHorarioDistribucion"  ( "IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","IdCondicionDeLiquidacion","ValidaHorario","AplazaOrden","HorarioPreMarketInicio","HorarioPreMarketFin","HorarioMarketInicio","HorarioMarketFin") VALUES ( 'MAN','AGE','ACN','BCS','T0','S','S','12:00:00','12:30:00','12:30:00','19:00:00'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionHorarioDistribucion"  ( "IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","IdCondicionDeLiquidacion","ValidaHorario","AplazaOrden","HorarioPreMarketInicio","HorarioPreMarketFin","HorarioMarketInicio","HorarioMarketFin") VALUES ( 'RUA','AGE','ACN','BCS','T0','S','S','12:00:00','12:30:00','12:30:00','20:00:00'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"; 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'BovedaSimultanea','Boveda de instrumentos simultaneables','DCV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'ListasAyBSimultanea','Permite ingresar ordenes de simultaneas con instrumentos de lista A y B solamente','S'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'TemplateOrden','Id del template utilizado para generar los comprobantes de ordenes','57EA8A20B9140F76E10000007F000002'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'CodigoOperadorRuteoAgente','Codigo operador utilizado para mensajes fix ruteo agente','450'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'CodigoOperadorRuteoRetail','Codigo operador utilizado para mensajes fix ruteo retail','450'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'DiferenciaMontoAsignacion','Es el monto que puede tener de diferencia el monto asignado a una transaccion, y no ser considerado error de cuadratura','200'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'MonedaSimultanea','Moneda de Operaciones Simultaneas','CLP'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'RuteoUsaFechaSistema','Indica si al enviar orden a ruteo usa la fecha de sistema o la fecha de la base de datos','N'); 
   INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"  ( "IdEmpresa","CodigoParametro","Descripcion","Valor") VALUES ( 1,'PorcentajeDivergenciaPrecio','Porcentaje que puede diverger el precio de la ordden','20'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.EstadoOrden"; 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 1,'ING','TRANSACCION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 2,'ANU','TRANSACCION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 3,'ASP','TRANSACCION.ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 4,'ASC','TRANSACCION.ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 5,'SAS','TRANSACCION.ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 6,'ING','ORDEN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 25,'ACT','CORREDORCORRESPONSAL'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 8,'RZD','ORDEN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 9,'OFE','ORDEN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 27,'ENR','ORDEN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 11,'ANU','ORDEN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 12,'ASP','ORDEN.ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 13,'ASC','ORDEN.ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 14,'SAS','ORDEN.ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 15,'NEF','ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 16,'ANU','ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 17,'EAF','ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 18,'REF','ASIGNACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 19,'VCD','ORDEN.VIGENCIA'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 20,'VIG','ORDEN.VIGENCIA'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 21,'ACT','LIBRO.ORDENES'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 22,'INA','LIBRO.ORDENES'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 23,'ACT','GRUPOPORTAFOLIO'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 24,'INA','GRUPOPORTAFOLIO'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 7,'PDA','ORDEN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 26,'INA','CORREDORCORRESPONSAL'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 10,'NAU','ORDEN'); 
   INSERT INTO "BSCL.Orden.BD::ORD.EstadoOrden"  ( "IdEstadoOrden","IdEstado","NombreEntidad") VALUES ( 28,'ANP','ORDEN'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.Grupo"; 
   INSERT INTO "BSCL.Orden.BD::ORD.Grupo"  ( "IdGrupo","IdEmpresa","Nombre","IdEstado","Codigo") VALUES ( 14,1,'GRUPO PRUEBA','ACT','PRUEBA_RENZO'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.GrupoPortafolio"; 
   INSERT INTO "BSCL.Orden.BD::ORD.GrupoPortafolio"  ( "IdGrupoPortafolio","IdEmpresa","IdGrupo","IdPortafolio","Porcentaje") VALUES ( 489,1,14,343710,20.00000000); 
   INSERT INTO "BSCL.Orden.BD::ORD.GrupoPortafolio"  ( "IdGrupoPortafolio","IdEmpresa","IdGrupo","IdPortafolio","Porcentaje") VALUES ( 490,1,14,1450060,50.00000000); 
   INSERT INTO "BSCL.Orden.BD::ORD.GrupoPortafolio"  ( "IdGrupoPortafolio","IdEmpresa","IdGrupo","IdPortafolio","Porcentaje") VALUES ( 491,1,14,1450057,30.00000000); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.LibroOrdenes"; 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenes"  ( "IdLibroOrdenes","IdEmpresa","NombreLibroOrdenes","IdTipoLibroOrden","IdEstado","AsignacionAutomatica","IdTipoAsignacion","Prioridad","LibroPorDefecto","FechaCreacion","FechaModificacion","UltimaFechaDeEjecucion","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo","IdTipoDesbloqueo","FechaDesbloqueo","ComentarioDesbloqueo","Borrado") VALUES ( 3,1,'LIBRO_PRUEBA','NORMAL','INA','SI','ASIGNACION_CRONOLOGICA',3,null,'2016-04-22','2016-04-22','0001-01-01 00:00:00','','0001-01-01 00:00:00','','','0001-01-01 00:00:00','','S'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenes"  ( "IdLibroOrdenes","IdEmpresa","NombreLibroOrdenes","IdTipoLibroOrden","IdEstado","AsignacionAutomatica","IdTipoAsignacion","Prioridad","LibroPorDefecto","FechaCreacion","FechaModificacion","UltimaFechaDeEjecucion","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo","IdTipoDesbloqueo","FechaDesbloqueo","ComentarioDesbloqueo","Borrado") VALUES ( 4,1,'TEST OPA','OPA','ACT','NO','',1,null,'2016-05-24','2016-06-28','2016-05-30 20:45:52','','0001-01-01 00:00:00','','','0001-01-01 00:00:00','','N'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenes"  ( "IdLibroOrdenes","IdEmpresa","NombreLibroOrdenes","IdTipoLibroOrden","IdEstado","AsignacionAutomatica","IdTipoAsignacion","Prioridad","LibroPorDefecto","FechaCreacion","FechaModificacion","UltimaFechaDeEjecucion","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo","IdTipoDesbloqueo","FechaDesbloqueo","ComentarioDesbloqueo","Borrado") VALUES ( 2,1,'INSTITUCIONAL','NORMAL','ACT','NO','ASIGNACION_CRONOLOGICA',1,'S','2016-03-18','2016-10-17','2016-05-30 21:25:02','','0001-01-01 00:00:00','','','0001-01-01 00:00:00','','N'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenes"  ( "IdLibroOrdenes","IdEmpresa","NombreLibroOrdenes","IdTipoLibroOrden","IdEstado","AsignacionAutomatica","IdTipoAsignacion","Prioridad","LibroPorDefecto","FechaCreacion","FechaModificacion","UltimaFechaDeEjecucion","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo","IdTipoDesbloqueo","FechaDesbloqueo","ComentarioDesbloqueo","Borrado") VALUES ( 1,1,'RETAIL','NORMAL','ACT','NO','',1,null,'2016-03-18','2016-11-17','2016-05-16 12:56:22','','2016-05-02 20:37:53','Libro Ordenes Activo','','2016-04-21 18:50:59','Libro Ordenes Inactivo','N'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"; 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"  ( "IdMarcaNegociacion","IdEmpresa","IdLibroOrdenes","MarcaNegociacion","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 7,1,14,'CUENTA PROPIA','ACT','2016-03-10','2016-03-10'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"  ( "IdMarcaNegociacion","IdEmpresa","IdLibroOrdenes","MarcaNegociacion","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 8,1,15,'OP EXTERNO 1','INA','2016-03-10','2016-06-09'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"  ( "IdMarcaNegociacion","IdEmpresa","IdLibroOrdenes","MarcaNegociacion","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 9,1,16,'OP EXTERNO 44','ACT','2016-03-10','2016-03-10'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"  ( "IdMarcaNegociacion","IdEmpresa","IdLibroOrdenes","MarcaNegociacion","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 10,1,16,'ASD','INA','2016-03-15','2016-03-15'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"  ( "IdMarcaNegociacion","IdEmpresa","IdLibroOrdenes","MarcaNegociacion","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 11,1,15,'ASDF','ACT','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"  ( "IdMarcaNegociacion","IdEmpresa","IdLibroOrdenes","MarcaNegociacion","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 12,1,2,'MARCA1','INA','2016-03-18','2016-03-18'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"  ( "IdMarcaNegociacion","IdEmpresa","IdLibroOrdenes","MarcaNegociacion","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 13,1,2,'MARCA02','INA','2016-03-18','2016-03-18'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.LibroOrdenesOperador"; 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesOperador"  ( "IdLibroOrdenesOperador","IdEmpresa","IdLibroOrdenes","CodigoOperador","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 42,1,2,'001','ACT','2016-11-17','2016-11-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesOperador"  ( "IdLibroOrdenesOperador","IdEmpresa","IdLibroOrdenes","CodigoOperador","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 43,1,1,'001','ACT','2016-11-17','2016-11-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesOperador"  ( "IdLibroOrdenesOperador","IdEmpresa","IdLibroOrdenes","CodigoOperador","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 45,1,2,'333','ACT','2016-11-17','2016-11-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesOperador"  ( "IdLibroOrdenesOperador","IdEmpresa","IdLibroOrdenes","CodigoOperador","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 44,1,1,'333','INA','2016-11-17','2016-11-17'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"; 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 29,1,16,3,'ACT','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 30,1,14,3,'ACT','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 32,1,15,2,'ACT','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 28,1,15,1,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 33,1,15,1,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 34,1,16,1,'ACT','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 35,1,15,1,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 36,1,15,1,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 31,1,14,2,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 38,1,14,2,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 37,1,14,1,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 39,1,1,1,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 40,1,1,1,'INA','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 41,1,15,1,'ACT','2016-03-17','2016-03-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 49,1,4,2,'INA','2016-05-24','2016-06-28'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 43,1,1,2,'ACT','2016-04-20','2016-04-20'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 42,1,2,1,'INA','2016-03-18','2016-05-17'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 45,1,1,1,'INA','2016-05-17','2016-06-13'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 47,1,4,3,'ACT','2016-05-24','2016-05-24'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 48,1,4,1,'INA','2016-05-24','2016-06-13'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 44,1,2,3,'INA','2016-05-17','2016-06-28'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 50,1,2,1,'ACT','2016-06-13','2016-06-13'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 52,1,2,3,'ACT','2016-06-28','2016-06-28'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 53,1,4,1,'ACT','2016-10-18','2016-10-18'); 
   INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"  ( "IdLibroOrdenesSegmento","IdEmpresa","IdLibroOrdenes","IdSegmentoCliente","IdEstado","FechaCreacion","FechaModificacion") VALUES ( 54,1,4,2,'ACT','2016-10-18','2016-10-18'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"; 
   INSERT INTO "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"  ( "IdPorcentajeVisibilidad","Codigo","Nombre","IdEstado","Porcentaje") VALUES ( 1,'100','100 %','ACT',100); 
   INSERT INTO "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"  ( "IdPorcentajeVisibilidad","Codigo","Nombre","IdEstado","Porcentaje") VALUES ( 2,'50','50 %','ACT',50); 
   INSERT INTO "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"  ( "IdPorcentajeVisibilidad","Codigo","Nombre","IdEstado","Porcentaje") VALUES ( 3,'25','25 %','ACT',25); 
   INSERT INTO "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"  ( "IdPorcentajeVisibilidad","Codigo","Nombre","IdEstado","Porcentaje") VALUES ( 4,'10','10 %','ACT',10); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.SistemaBursatil"; 
   INSERT INTO "BSCL.Orden.BD::ORD.SistemaBursatil"  ( "IdSistemaBursatil","NombreSistemaBursatil") VALUES ( 'BCSTELEPREGON','BCS TELEPREGON'); 
   INSERT INTO "BSCL.Orden.BD::ORD.SistemaBursatil"  ( "IdSistemaBursatil","NombreSistemaBursatil") VALUES ( 'BCSREMATEHOLANDES','BCS REMATE HOLANDES'); 
   INSERT INTO "BSCL.Orden.BD::ORD.SistemaBursatil"  ( "IdSistemaBursatil","NombreSistemaBursatil") VALUES ( 'BCSREMATEELECTRONICO','BCS REMATE ELECTRONICO'); 
   INSERT INTO "BSCL.Orden.BD::ORD.SistemaBursatil"  ( "IdSistemaBursatil","NombreSistemaBursatil") VALUES ( 'MANUAL','INGRESO MANUAL'); 
   INSERT INTO "BSCL.Orden.BD::ORD.SistemaBursatil"  ( "IdSistemaBursatil","NombreSistemaBursatil") VALUES ( 'BCSTELERENTA','BCS TELERENTA'); 
   INSERT INTO "BSCL.Orden.BD::ORD.SistemaBursatil"  ( "IdSistemaBursatil","NombreSistemaBursatil") VALUES ( 'BCSREMATESERIALIZADO','BCS REMATE SERIALIZADO'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoAsignacion"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacion"  ( "IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") VALUES ( 'ASIGNACION_POSTRERA','UI_ASIGNACION_POSTRERA','ACT','N'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacion"  ( "IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") VALUES ( 'ASIGNACION_PROPORCIONAL','UI_ASIGNACION_PROPORCIONAL','ACT','S'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacion"  ( "IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") VALUES ( 'ASIGNACION_CRONOLOGICA','UI_ASIGNACION_CRONOLOGICA','ACT','S'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacion"  ( "IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") VALUES ( 'ASIGNACION_PRECIO_PROMEDIO','UI_ASIGNACION_PRECIO_PROMEDIO','ACT','N'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacion"  ( "IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") VALUES ( 'ASIGNACION_MANUAL','UI_ASIGNACION_MANUAL','ACT','N'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 1,'ASIGNACION_MANUAL','S','RV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 2,'ASIGNACION_MANUAL','S','DV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 3,'ASIGNACION_CRONOLOGICA','S','RV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 4,'ASIGNACION_CRONOLOGICA','S','DV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 5,'ASIGNACION_PROPORCIONAL','S','RV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 6,'ASIGNACION_PRECIO_PROMEDIO','N','RV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 7,'ASIGNACION_POSTRERA','N','RV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 8,'ASIGNACION_POSTRERA','N','RV'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 9,'ASIGNACION_POSTRERA','N','RF'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 10,'ASIGNACION_POSTRERA','N','IF'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento"  ( "IdTipoAsignacionClasificacionTipoInstrumento","IdTipoAsignacion","EsContraOrden","IdClasificacionTipoInstrumento") VALUES ( 11,'ASIGNACION_POSTRERA','N','DV'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoCanalDistribucion"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoCanalDistribucion"  ( "IdTipoCanalDistribucion","Nombre","IdEstado") VALUES ( 'MAN','MANUAL','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoCanalDistribucion"  ( "IdTipoCanalDistribucion","Nombre","IdEstado") VALUES ( 'RUA','RUTEO AGENTE','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoCanalDistribucion"  ( "IdTipoCanalDistribucion","Nombre","IdEstado") VALUES ( 'RUR','RUTEO RETAIL','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoCanalDistribucion"  ( "IdTipoCanalDistribucion","Nombre","IdEstado") VALUES ( 'RUT','RUTEO TRADER','ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoCustodiaOrden"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoCustodiaOrden"  ( "IdTipoCustodiaOrden","Codigo","Nombre","IdEstado") VALUES ( 'DCV','1','EN DCV','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoCustodiaOrden"  ( "IdTipoCustodiaOrden","Codigo","Nombre","IdEstado") VALUES ( 'OTR','2','FUERA DCV','ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoEventoBitacora"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoEventoBitacora"  ( "IdTipoEventoBitacora","DescripcionTipoEventoBitacora") VALUES ( 'ENVIADA_NEGOCIACION','ENVIADA A NEGOCIACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoEventoBitacora"  ( "IdTipoEventoBitacora","DescripcionTipoEventoBitacora") VALUES ( 'EN_NEGOCIACION','EN NEGOCIACION'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoEventoBitacora"  ( "IdTipoEventoBitacora","DescripcionTipoEventoBitacora") VALUES ( 'EJECUTADA_PARCIALMENTE','EJECUTADA PARCIALMENTE'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoEventoBitacora"  ( "IdTipoEventoBitacora","DescripcionTipoEventoBitacora") VALUES ( 'EJECUTADA_COMPLETAMENTE','EJECUTADA COMPLETAMENTE'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoEventoBitacora"  ( "IdTipoEventoBitacora","DescripcionTipoEventoBitacora") VALUES ( 'ENVIADA_ANULAR','ENVIADA A ANULAR'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoEventoBitacora"  ( "IdTipoEventoBitacora","DescripcionTipoEventoBitacora") VALUES ( 'ORDEN_ANULADA','ORDEN ANULADA'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoEventoBitacora"  ( "IdTipoEventoBitacora","DescripcionTipoEventoBitacora") VALUES ( 'ORDEN_RECHAZADA','ORDEN RECHAZADA'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoEventoBitacora"  ( "IdTipoEventoBitacora","DescripcionTipoEventoBitacora") VALUES ( 'ORDEN_ANULADA_PARCIALMENTE','ORDEN ANULADA PARCIALMENTE'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden"  ( "IdTipoFormaCustodiaOrden","Codigo","Nombre","IdEstado") VALUES ( 'NOM','1','NOMINATIVA','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden"  ( "IdTipoFormaCustodiaOrden","Codigo","Nombre","IdEstado") VALUES ( 'CUS','2','CUSTODIA','ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoLibroOrden"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoLibroOrden"  ( "IdTipoLibroOrden","NombreTipoLibroOrden") VALUES ( 'NORMAL','NORMAL'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoLibroOrden"  ( "IdTipoLibroOrden","NombreTipoLibroOrden") VALUES ( 'OPA','OPA'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoLibroOrden"  ( "IdTipoLibroOrden","NombreTipoLibroOrden") VALUES ( 'REMATE','REMATE'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoLibroOrden"  ( "IdTipoLibroOrden","NombreTipoLibroOrden") VALUES ( 'COLOCA','COLOCACION'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoPrecioOrden"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoPrecioOrden"  ( "IdTipoPrecioOrden","Codigo","Nombre","IdEstado") VALUES ( 'L','1','LIMITE','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoPrecioOrden"  ( "IdTipoPrecioOrden","Codigo","Nombre","IdEstado") VALUES ( 'M','2','MERCADO','ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.TipoVigenciaOrden"; 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoVigenciaOrden"  ( "IdTipoVigenciaOrden","Codigo","Nombre","IdEstado") VALUES ( 'OP','1','PERMANENTE','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoVigenciaOrden"  ( "IdTipoVigenciaOrden","Codigo","Nombre","IdEstado") VALUES ( 'SH','2','SOLO HOY','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoVigenciaOrden"  ( "IdTipoVigenciaOrden","Codigo","Nombre","IdEstado") VALUES ( 'SE','3','SEMANAL','ACT'); 
   INSERT INTO "BSCL.Orden.BD::ORD.TipoVigenciaOrden"  ( "IdTipoVigenciaOrden","Codigo","Nombre","IdEstado") VALUES ( 'FF','4','FECHA FIJA','ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Orden.BD::ORD.ValorPorDefectoOrden"; 
   INSERT INTO "BSCL.Orden.BD::ORD.ValorPorDefectoOrden"  ( "IdInterno","IdTipoPrecio","IdPorcentajeVisibilidad","IdFormaCustodia") VALUES ( 0,'M',1,'CUS'); 
 COMMIT; 
 END; 
 
 
