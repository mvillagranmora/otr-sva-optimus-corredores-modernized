INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Mesa_MovimientosSpot', 'I', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Replica.BD.Vistas::ReplicaMesa_MovimientosSpot"


select  1 from dummy;CREATE TABLE PEPITO(A INT) -- from "BSCL.Orden.BD.Vistas::ConsultarOrden"  limit 15000

select  1 from dummy;CREATE TABLE PEPITO(A INT);DROP TABLE PEPITO;-- from "BSCL.Orden.BD.Vistas::ConsultarOrden"  limit 15000