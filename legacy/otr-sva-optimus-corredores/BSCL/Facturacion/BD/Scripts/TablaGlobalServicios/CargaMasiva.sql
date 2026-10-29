SET SCHEMA "BSCL";
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.ProcesarCargaMasiva_Proceso" cascade;
GO

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ProcesarCargaMasiva_Proceso"
(
    "IdCargaMasiva" BIGINT,
	"IdTipoCargaMasiva" BIGINT,
	"CodigoArchivo" NVARCHAR(100),
	"IdEmpleadoDigitador" BIGINT,
	"IdBolsa" NVARCHAR(3),
	"IdBoveda" NVARCHAR(3)

)  ON COMMIT DELETE ROWS;
GO

COMMIT;
GO