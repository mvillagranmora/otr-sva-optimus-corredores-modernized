SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden";

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden"("IdTipoFormaCustodiaOrden", "Codigo", "Nombre" , "IdEstado" ) values ('NOM',1,'NOMINATIVA','ACT');							
insert into "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" ("IdTipoFormaCustodiaOrden", "Codigo", "Nombre" , "IdEstado" ) values ('CUS',2,'CUSTODIA FISICA','ACT');										
commit;


 

 





 






 

 
 

