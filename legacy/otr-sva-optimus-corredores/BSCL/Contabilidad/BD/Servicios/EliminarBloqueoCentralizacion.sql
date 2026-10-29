SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::EliminarBloqueoCentralizacion";
GO
CREATE PROCEDURE "BSCL.Contabilidad.BD.Servicios::EliminarBloqueoCentralizacion"(
	IN p_BloqueoCentralizacion TABLE(
		"IdBloqueoCentralizacion" NVARCHAR(20),
		"IdTipoCentralizacion" BIGINT,
		"FechaCentralizacion" NVARCHAR(15)
	)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN AUTONOMOUS TRANSACTION
		DELETE FROM "BSCL.Contabilidad.BD::GBL.BloqueoCentralizaciones"
		WHERE EXISTS(
			SELECT 1
			FROM :p_BloqueoCentralizacion BC
			WHERE BC."IdTipoCentralizacion" = "BSCL.Contabilidad.BD::GBL.BloqueoCentralizaciones"."IdTipoCentralizacion"
			AND BC."FechaCentralizacion" = "BSCL.Contabilidad.BD::GBL.BloqueoCentralizaciones"."FechaCentralizacion"
			AND "BSCL.Contabilidad.BD::GBL.BloqueoCentralizaciones"."IdBloqueoCentralizacion" = BC."IdBloqueoCentralizacion"
			);
	END;
END;
GO