SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.LibroOrdenesDefinicion";

/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into  "BSCL.Orden.BD::ORD.LibroOrdenesDefinicion"("IdLibroOrdenesDefinicion","IdLibroOrdenes", "IdCanal" , "IdOperador","IdSegmentoCliente","IdEmpresa" ) values (1,1,'CLI',1,1,1);	
insert into  "BSCL.Orden.BD::ORD.LibroOrdenesDefinicion"("IdLibroOrdenesDefinicion","IdLibroOrdenes", "IdCanal" , "IdOperador","IdSegmentoCliente","IdEmpresa" ) values (2,1,'AGE',1,1,1);	
insert into  "BSCL.Orden.BD::ORD.LibroOrdenesDefinicion"("IdLibroOrdenesDefinicion","IdLibroOrdenes", "IdCanal" , "IdOperador","IdSegmentoCliente","IdEmpresa" ) values (3,1,'TRA',1,1,1);										
commit;



 






 

 
 

