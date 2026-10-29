SELECT "Table","Action", max("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2023-11-02' 
AND "Status" = 0
-- AND "Action" = 'D'
GROUP BY "Table","Action"

--"IdDireccionFisica" >= 1235000 AND "IdDireccionFisica" <= 1235999

SELECT * FROM 
"BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionFisica"
WHERE IDDIRECCIONFISICA IN (
1235529,
1235528,
1235527,
1235526,
1235525,
1235524,
1235523,
1235522,
1235521,
1235520
)


SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_CustodiaFechaLiquidacion"
where "TABLE_KEY" = '226626:CFRCLP-OSA'

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'SocioNegocio_PersonaDireccionElectronica', 'I', 0,  CURRENT_TIMESTAMP 
from "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionElectronica" 

select * from "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionElectronica" brbvrsnpde 