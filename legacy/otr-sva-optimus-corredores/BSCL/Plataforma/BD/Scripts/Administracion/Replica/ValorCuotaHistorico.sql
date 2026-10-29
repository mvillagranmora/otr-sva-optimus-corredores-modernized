INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdValorCuotaHistorico" AS varchar(200)),'FondoMutuo_ValorCuotaHistorico', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistoricoAnalitica"



 SELECT (DAYS_BETWEEN("FechaValorCuota",'1970-01-01') + "IdValorCuotaHistorico" + ROUND("ValorCuota",5) + CASE WHEN "CodigoSerie" is null then 0 else length("CodigoSerie") END + case when "EsUltimaCargaDiaria" = 'S' THEN 10 ELSE 13 END) AS HASH,* FROM BSCL."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistoricoAnalitica"
   WHERE (FLOOR("IdValorCuotaHistorico"/100) = 190797 OR 0 = 1)
   ORDER BY "IdValorCuotaHistorico"
   