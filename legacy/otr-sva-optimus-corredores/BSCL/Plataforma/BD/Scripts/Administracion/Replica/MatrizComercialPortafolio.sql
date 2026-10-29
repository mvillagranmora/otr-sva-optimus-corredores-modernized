INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT IdPortafolio,'Plataforma_MatrizComercialPortafolios', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaPlataforma_MatrizComercialPortafolios";



select count(1) from
"BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialPortafoliosAnalitica" bpbvccmcpa 