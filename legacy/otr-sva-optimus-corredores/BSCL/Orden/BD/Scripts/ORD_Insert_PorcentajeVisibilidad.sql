SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad";

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"("IdPorcentajeVisibilidad" , "Codigo", "Nombre" , "IdEstado" ) values (1,'100','100 %','ACT');	
insert into  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"("IdPorcentajeVisibilidad" , "Codigo", "Nombre" , "IdEstado" ) values (2,'50','50 %','ACT');										
insert into  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"("IdPorcentajeVisibilidad" , "Codigo", "Nombre" , "IdEstado" ) values (3,'25','25 %','ACT');							
insert into  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"("IdPorcentajeVisibilidad" , "Codigo", "Nombre" , "IdEstado" ) values (4,'10','10 %','ACT');										

commit;







 

 
 

