SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.TipoCanalDistribucion";
/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/

insert into "BSCL.Orden.BD::ORD.TipoCanalDistribucion" select 'MAN','MANUAL','ACT' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoCanalDistribucion" select 'RUA','RUTEO AGENTE','ACT' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoCanalDistribucion" select 'RUR','RUTEO RETAIL','ACT' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoCanalDistribucion" select 'RUT','RUTEO TRADER','ACT' from dummy;

 

 
 

