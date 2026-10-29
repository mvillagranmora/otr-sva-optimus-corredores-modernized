INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )

SELECT table_key,'Plataforma_Instrumento', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL"."BSCL.Replica.BD.Vistas::ReplicaPlataforma_Instrumento"

SELECT * FROM "BSCL"."BSCL.Replica.BD.Vistas::ReplicaPlataforma_Instrumento" where IDSTACK is not null

select * from "BSCL.Plataforma.BD::PLA.Replica.WorkStack"