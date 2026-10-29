SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from "BSCL.Orden.BD::ORD.TipoPrecioOrden";

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into "BSCL.Orden.BD::ORD.TipoPrecioOrden" ("IdTipoPrecioOrden", "Codigo", "Nombre" , "IdEstado" ) values ('L',1,'LIMITE','ACT');							
insert into "BSCL.Orden.BD::ORD.TipoPrecioOrden" ("IdTipoPrecioOrden", "Codigo", "Nombre" , "IdEstado" ) values ('M',2,'MERCADO','ACT');										
commit;


 

 

 





 






 

 
 

