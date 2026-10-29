SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.SistemaBursatil";
/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" select 'BCSTELEPREGON','BCS TELEPREGON' from dummy;
insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" select 'BCSREMATEHOLANDES','BCS REMATE HOLANDES' from dummy;
insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" select 'BCSREMATEELECTRONICO','BCS REMATE ELECTRONICO' from dummy;
insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" select 'MANUAL','INGRESO MANUAL' from dummy;
insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" select 'BCSTELERENTA','BCS TELERENTA' from dummy;
insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" select 'BCSREMATESERIALIZADO','BCS REMATE SERIALIZADO' from dummy;

commit;

