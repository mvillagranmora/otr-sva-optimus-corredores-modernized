SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ActualizarInstrumentoCambioNemo_IN_Instrumento" cascade;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ActualizarInstrumentoCambioNemo_IN_Instrumento"
    ( "IdEmpresa"          BIGINT,
      "IdClasificacionTipoInstrumento" NVARCHAR(3),
      "IdTipoInstrumento"  NVARCHAR(3),
      "IdInstrumento"      BIGINT,
      "IdInstrumentoNuevo" BIGINT
    ) ON COMMIT DELETE ROWS;
