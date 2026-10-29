SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.EstadoOrden";
/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (1,'ING','TRANSACCION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (2,'ANU','TRANSACCION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (3,'ASP','TRANSACCION.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (4,'ASC','TRANSACCION.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (5,'SAS','TRANSACCION.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (6,'ING','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (7,'CNF','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (8,'RZD','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (9,'OFE','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (10,'CAN','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (11,'ANU','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (12,'ASP','ORDEN.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (13,'ASC','ORDEN.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (14,'SAS','ORDEN.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (15,'NEF','ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (16,'ANU','ASIGNACION');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (17,'EAF','ASIGNACION');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (18,'REF','ASIGNACION');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (19,'VCD','ORDEN.VIGENCIA');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (20,'VIG','ORDEN.VIGENCIA');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (21,'ACT','LIBRO.ORDENES');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (22,'INA','LIBRO.ORDENES');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (23,'ACT','GRUPOPORTAFOLIO');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (24,'INA','GRUPOPORTAFOLIO');

commit;

