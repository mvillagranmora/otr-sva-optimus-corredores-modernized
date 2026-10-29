SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.TipoCanalDistribucion";
delete from  "BSCL.Orden.BD::ORD.TipoCanalDistribucionBolsa";
/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
INSERT INTO "BSCL.Orden.BD::ORD.TipoCanalDistribucion" ( "IdTipoCanalDistribucion","Codigo","Nombre","IdEstado" )  
VALUES('RUM','RUTEO MANUAL','RUTEO MANUAL','ACT');
INSERT INTO "BSCL.Orden.BD::ORD.TipoCanalDistribucion" ( "IdTipoCanalDistribucion","Codigo","Nombre","IdEstado" )  
VALUES('RUA','RUTEO AUTOMATICO','RUTEO AUTOMATICO','ACT');
INSERT INTO "BSCL.Orden.BD::ORD.TipoCanalDistribucion" ( "IdTipoCanalDistribucion","Codigo","Nombre","IdEstado" )  
VALUES('TRA','TRADER','TRADER','ACT');


INSERT INTO "BSCL.Orden.BD::ORD.TipoCanalDistribucionBolsa" ( "IdTipoCanalDistribucion","IdBolsa" )  
VALUES('TRA','BCS');
commit;



 

 
 

