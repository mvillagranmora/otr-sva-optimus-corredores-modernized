SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/

SELECT * FROM "BSCL.Orden.BD::ORD.TipoAsignacion"
SELECT * FROM "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 

insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_POSTRERA','UI_ASIGNACION_POSTRERA','ACT','N');
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_PROPORCIONAL','UI_ASIGNACION_PROPORCIONAL','ACT','S');
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_CRONOLOGICA','UI_ASIGNACION_CRONOLOGICA','ACT','S');
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_PRECIO_PROMEDIO','UI_ASIGNACION_PRECIO_PROMEDIO','ACT','N');
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_MANUAL','UI_ASIGNACION_MANUAL','ACT','N');

insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (1,'ASIGNACION_MANUAL','S','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (2,'ASIGNACION_MANUAL','S','DV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (3,'ASIGNACION_CRONOLOGICA','S','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (4,'ASIGNACION_CRONOLOGICA','S','DV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (5,'ASIGNACION_PROPORCIONAL','S','RV');

insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (6,'ASIGNACION_PRECIO_PROMEDIO','N','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (7,'ASIGNACION_POSTRERA','N','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (8,'ASIGNACION_POSTRERA','N','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (9,'ASIGNACION_POSTRERA','N','RF');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (10,'ASIGNACION_POSTRERA','N','IF');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (11,'ASIGNACION_POSTRERA','N','DV');

commit;






 

 
 

