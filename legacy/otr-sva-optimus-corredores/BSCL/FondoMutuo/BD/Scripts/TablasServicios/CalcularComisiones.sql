SET SCHEMA "BSCL"; 
GO
DROP TABLE "BSCL.FondoMutuo.BD::GBL.CalcularComisiones_Proceso_ComisionesPorSerie";
GO
DROP TABLE "BSCL.FondoMutuo.BD::GBL.CalcularComisiones_Proceso_CarterasAfectasComision";
GO

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.FondoMutuo.BD::GBL.CalcularComisiones_Proceso_ComisionesPorSerie"(
    "IdTrabajo"             BIGINT,
    "IdInterno"             BIGINT,
    "IdInstrumento"         BIGINT,
    "IdSerieFondo"          NVARCHAR(40),
    "MontoComision"         DECIMAL(26,8),
    "MontoComisionLocal"    DECIMAL(26,8),
    "IdCliente"             BIGINT,
    "IdPortafolio"          BIGINT,
    "TipoSolicitud"         NVARCHAR(3)
) ON COMMIT DELETE ROWS;

GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.FondoMutuo.BD::GBL.CalcularComisiones_Proceso_CarterasAfectasComision"(
    "IdTrabajo"             BIGINT,
    "IdInterno"             BIGINT,
    "IdInstrumento"         BIGINT,
    "IdSerieFondo"          NVARCHAR(40),
    "MontoComision"         DECIMAL(26,8),
    "IdCliente"             BIGINT,
    "IdPortafolio"          BIGINT,
    "FechaIngresoCartera"   DATE) ON COMMIT DELETE ROWS;


