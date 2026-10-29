SET SCHEMA "BSCL"; 
/*==============================================================*/
/* delete from :                                                */
/*==============================================================*/

delete from  "BSCL.Orden.BD::ORD.TipoLibroOrden";
/*==============================================================*/
/* insert :                                                     */
/*==============================================================*/
-- INSERT Tipo Libro Ordenes
insert into "BSCL.Orden.BD::ORD.TipoLibroOrden"("IdTipoLibroOrden","NombreTipoLibroOrden") values ('NORMAL','Normal');
insert into "BSCL.Orden.BD::ORD.TipoLibroOrden"("IdTipoLibroOrden","NombreTipoLibroOrden") values ('OPA','OPA');
insert into "BSCL.Orden.BD::ORD.TipoLibroOrden"("IdTipoLibroOrden","NombreTipoLibroOrden") values ('REMATE','Remate');
insert into "BSCL.Orden.BD::ORD.TipoLibroOrden"("IdTipoLibroOrden","NombreTipoLibroOrden") values ('COLOCA','Colocación');

commit;
