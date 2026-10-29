INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT TABLE_KEY,'FondoMutuo_Traspasos', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Traspasos"

SELECT "TipoTraspaso",count(1) FROM BSCL."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica"
GROUP BY "TipoTraspaso"

SELECT "TipoTraspaso","IdSolicitudAporteRescate",count(1) FROM BSCL."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica"
GROUP BY "TipoTraspaso","IdSolicitudAporteRescate"
HAVING count(1) > 1

SELECT * FROM BSCL."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica"
WHERE "IdSolicitudAporteRescate" IN (11343720,11343721,11343719)

SELECT * FROM BSCL."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica"

SELECT COUNT(1) FROM BSCL."BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Traspasos" WHERE IDSTACK IS NOT NULL
SELECT * FROM BSCL."BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Traspasos" WHERE IDSTACK IS NULL