SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.TipoCustodiaOrden";

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into  "BSCL.Orden.BD::ORD.TipoCustodiaOrden"("IdTipoCustodiaOrden" , "Codigo", "Nombre" , "IdEstado" ) values ('DCV',1,'EN DCV','ACT');							
insert into  "BSCL.Orden.BD::ORD.TipoCustodiaOrden" ("IdTipoCustodiaOrden" , "Codigo", "Nombre" , "IdEstado" ) values ('OTR',2,'FUERA DCV','ACT');										
commit;


 

 





 






 

 
 

