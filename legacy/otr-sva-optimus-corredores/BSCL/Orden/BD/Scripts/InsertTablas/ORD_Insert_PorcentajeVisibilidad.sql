SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad";

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"("IdPorcentajeVisibilidad" , "Codigo", "Nombre" , "IdEstado" ,"Porcentaje") values (1,'100','100 %','ACT',100);	
insert into  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"("IdPorcentajeVisibilidad" , "Codigo", "Nombre" , "IdEstado" ,"Porcentaje") values (2,'50','50 %','ACT',75);										
insert into  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"("IdPorcentajeVisibilidad" , "Codigo", "Nombre" , "IdEstado" ,"Porcentaje") values (3,'25','25 %','ACT',50);							
insert into  "BSCL.Orden.BD::ORD.PorcentajeVisibilidad"("IdPorcentajeVisibilidad" , "Codigo", "Nombre" , "IdEstado" ,"Porcentaje") values (4,'10','10 %','ACT',25);										

commit;


 
 

