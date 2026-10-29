SET SCHEMA "BSCL"; 

select * from "BSCL.Orden.BD::ORD.TipoEventoBitacora"

insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ENVIADA_NEGOCIACION','ENVIADA A NEGOCIACION' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'EN_NEGOCIACION','EN NEGOCIACION' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'EJECUTADA_PARCIALMENTE','EJECUTADA PARCIALMENTE' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'EJECUTADA_COMPLETAMENTE','EJECUTADA COMPLETAMENTE' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ENVIADA_ANULAR','ENVIADA A ANULAR' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ORDEN_ANULADA','ORDEN ANULADA' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ORDEN_RECHAZADA','ORDEN RECHAZADA' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ORDEN_ANULADA_PARCIALMENTE','ORDEN ANULADA PARCIALMENTE' from dummy;
