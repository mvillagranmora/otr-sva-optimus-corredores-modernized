INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Plataforma_PrecioInstrumento', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PrecioInstrumento" 

SELECT * FROM  "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PrecioInstrumento" 

