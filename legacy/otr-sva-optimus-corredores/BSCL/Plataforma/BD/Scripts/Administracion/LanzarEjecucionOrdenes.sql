set schema bscl;


DELETE FROM BSCL."BSCL.Normativa.BD::EJORD.EjecucionOrdenes"
WHERE "FechaEjecucion" >= '2022-01-21 00:00:00' AND "FechaEjecucion" <= '2022-01-21 23:59:00';


call "BSCL.Normativa.BD.Servicios::EJORD.ProcesarEjecucionOrdenes"(1,0,0,'2022-01-21');

SELECT * FROM BSCL."BSCL.Normativa.BD::EJORD.EjecucionOrdenes"
where "IdOrden" = 131693
ORDER BY "FechaEjecucion" DESC;

commit;