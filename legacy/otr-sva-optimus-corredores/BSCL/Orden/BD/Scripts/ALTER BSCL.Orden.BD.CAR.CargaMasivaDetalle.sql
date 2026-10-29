SET SCHEMA "BSCL";

/*==============================================*/
/* CREATE:  se agrega nueva referencia          */
/*==============================================*/
SET SCHEMA BSCL;

ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" ADD (
	"TipoInstruccion" NVARCHAR(2),
	"Emisor" NVARCHAR(40) DEFAULT '',
	"FechaVencimiento" NVARCHAR(10) DEFAULT '0001-01-01',
	"ValorRescate"  DECIMAL(26,8) DEFAULT 0,
	"FechaTrade" NVARCHAR(10) DEFAULT '0001-01-01',
	"Intermediario" NVARCHAR(40) DEFAULT '',
	"FolioFactura" NVARCHAR(40),
	"MontoLiquidacion" DECIMAL(26,8) DEFAULT 0,
	"IdTipoReajuste" NVARCHAR(3) DEFAULT '',
	"TotalCobros" DECIMAL(17,6) CS_FIXED DEFAULT 0
); 

ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" ALTER ("TipoOperacion" NVARCHAR(5));

/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "MontoLiquidacion";
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "Emisor";
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "FechaVencimiento";
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "ValorRescate";
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "FechaTrade";
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "Intermediario";
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "FolioFactura";
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "MontoLiquidacion";

ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "IdTipoReajuste";
ALTER TABLE "BSCL"."BSCL.Orden.BD::CAR.CargaMasivaDetalle" DROP COLUMN "TotalCobros";

*/