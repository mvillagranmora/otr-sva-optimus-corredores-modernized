SET SCHEMA "BSCL";
CREATE COLUMN TABLE "BSCL.Contabilidad.BD::GBL.BloqueoCentralizaciones" (
 	"IdBloqueoCentralizacion" NVARCHAR(20),
 	"IdTipoCentralizacion" BIGINT CS_FIXED NOT NULL,
 	"FechaCentralizacion" NVARCHAR(15) NOT NULL,
 	PRIMARY KEY ("IdBloqueoCentralizacion")
);
COMMIT;