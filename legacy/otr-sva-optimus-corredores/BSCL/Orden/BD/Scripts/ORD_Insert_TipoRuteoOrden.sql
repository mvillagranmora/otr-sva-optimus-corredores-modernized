SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from "BSCL.Orden.BD::ORD.TipoRuteoOrden";

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into "BSCL.Orden.BD::ORD.TipoRuteoOrden" ("IdTipoRuteoOrden", "Codigo", "Nombre" , "IdEstado" ) values ('RM',1,'RUTEO MANUAL','ACT');							
insert into "BSCL.Orden.BD::ORD.TipoRuteoOrden" ("IdTipoRuteoOrden", "Codigo", "Nombre" , "IdEstado" ) values ('RA',2,'RUTEO AUTOMATICO','ACT' );										
commit;



 
 
 

 

 

 





 






 

 
 

