SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.LibroOrdenes";
/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into  "BSCL.Orden.BD::ORD.LibroOrdenes"("IdLibroOrdenes" ,"NombreLibroOrdenes", "EstadoLibroOrdenes","IdEmpresa") values (1,'CLIENTE','ACT',1);	
commit;






 

 
 

