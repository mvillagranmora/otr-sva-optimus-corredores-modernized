SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.TipoOrden";
/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/


insert into "BSCL.Orden.BD::ORD.TipoOrden"("IdTipoOrden","Codigo","Nombre", "IdEstado") 
values ('OE',1,'ORDEN ESCRITA','ACT');
insert into "BSCL.Orden.BD::ORD.TipoOrden"("IdTipoOrden","Codigo","Nombre", "IdEstado") 
values ('OV',2,'ORDEN VERBAL','ACT');
insert into "BSCL.Orden.BD::ORD.TipoOrden"("IdTipoOrden","Codigo","Nombre", "IdEstado") 
values ('OD',3,'OPERADOR DIRECTO','ACT');
insert into "BSCL.Orden.BD::ORD.TipoOrden"("IdTipoOrden","Codigo","Nombre", "IdEstado") 
values ('OR',4,'SISTEMA DE RUTEO','ACT');
insert into "BSCL.Orden.BD::ORD.TipoOrden"("IdTipoOrden","Codigo","Nombre", "IdEstado") 
values ('OM',5,'ORDEN POR OTRO MEDIO','ACT');
insert into "BSCL.Orden.BD::ORD.TipoOrden"("IdTipoOrden","Codigo","Nombre", "IdEstado") 
values ('TM',6,'CUALQUIER MEDIO','ACT');


commit;



 

 
 

