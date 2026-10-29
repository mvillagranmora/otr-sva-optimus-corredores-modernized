SET SCHEMA "BSCL"; 
GO
DROP TABLE "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas";

GO

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"(
    "IdInternoProceso"  BIGINT,
    "IdInterno"         BIGINT,
    "IdSerieFondo"      NVARCHAR(40),
    "FechaEmision"      DATE,
    "FechaInicial"      DATE,
    "FechaInicialEsHabil" NVARCHAR(1),
    "FechaFinal"        DATE,
    "FechaFinalEsHabil" NVARCHAR(1),
    "CantidadDiasValuta" BIGINT,
    "IdPaisLocal"       BIGINT,
    "IdPaisEmisor"      BIGINT,
    "IdPaisFondo"       BIGINT,
    "IdPaisMoneda"      BIGINT,
    "IdConfiguracionFechasFondoMutuo"   BIGINT,
    "IdTipoFecha"       NVARCHAR(6),
    "EsHabil"           NVARCHAR(1),
    "IdTipoFechaOrigen" NVARCHAR(6),
    "DebeSerHabilFechaOrigen" NVARCHAR(1)
) ON COMMIT DELETE ROWS;

