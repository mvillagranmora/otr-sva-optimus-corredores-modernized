SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEstadoConfirmacion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEstadoConfirmacion"  
AS
SELECT "CodigoConfirmacion", "EstadoConfirmacion" 
FROM 
(
select 
 'T' as "CodigoConfirmacion",
 'TODOS' as "EstadoConfirmacion"
 FROM DUMMY
union all 
select 
'PEN' as "CodigoConfirmacion",
'PENDIENTE' as "EstadoConfirmacion"
 from dummy
union all 
select 
 'RZD' as "CodigoConfirmacion",
 'CONFIRMADO' as "EstadoConfirmacion"
from dummy
union all 
select 
 'APD' as "CodigoConfirmacion",
 'RECHAZADO' as "EstadoConfirmacion"
from dummy
union all 
select 
 'VCD' as "CodigoConfirmacion",
 'VENCIDO'  as "EstadoConfirmacion"
from dummy);


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEstadoConfirmacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


