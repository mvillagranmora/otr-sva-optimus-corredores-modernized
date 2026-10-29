INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdIngresoEgreso" AS varchar(200)),'Tesoreria_IngresoEgreso', 'A', 0,  CURRENT_TIMESTAMP  
FROM bscl."BSCL.Tesoreria.BD.Vistas::IngresoEgresoAnalitica"

SELECT COUNT(1) FROM "BSCL.Tesoreria.BD.Vistas::IngresoEgresoAnalitica"