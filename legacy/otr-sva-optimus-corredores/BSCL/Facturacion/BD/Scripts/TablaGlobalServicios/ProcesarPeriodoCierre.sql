SET SCHEMA "BSCL"; 
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.ProcesarPeriodoCierre_IN_PeriodoCierre" cascade;
GO






CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ProcesarPeriodoCierre_IN_PeriodoCierre"
    ( "IdPeriodoCierre"  BIGINT,
      "Periodo"    NVARCHAR(7),
      "IdEstadoPeriodo" NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;
GO 
    
