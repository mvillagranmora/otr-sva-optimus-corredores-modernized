SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from "BSCL.Orden.BD::ORD.TipoVigenciaOrden";

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into "BSCL.Orden.BD::ORD.TipoVigenciaOrden" ("IdTipoVigenciaOrden", "Codigo", "Nombre" , "IdEstado" ) values ('OP',1,'PERMANENTE','ACT');							
insert into "BSCL.Orden.BD::ORD.TipoVigenciaOrden" ("IdTipoVigenciaOrden", "Codigo", "Nombre" , "IdEstado" ) values ('SH',2,'SOLO HOY','ACT');							
insert into "BSCL.Orden.BD::ORD.TipoVigenciaOrden" ("IdTipoVigenciaOrden", "Codigo", "Nombre" , "IdEstado" ) values ('SE',3,'SEMANAL','ACT');							
insert into "BSCL.Orden.BD::ORD.TipoVigenciaOrden" ("IdTipoVigenciaOrden", "Codigo", "Nombre" , "IdEstado" ) values ('FF',4,'FECHA FIJA','ACT');							
commit;

 
 

 

 

 





 






 

 
 

