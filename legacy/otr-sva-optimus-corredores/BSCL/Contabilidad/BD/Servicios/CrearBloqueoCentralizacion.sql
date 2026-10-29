SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::CrearBloqueoCentralizacion";
GO
CREATE PROCEDURE "BSCL.Contabilidad.BD.Servicios::CrearBloqueoCentralizacion" (
	IN p_BloqueoCentralizacion TABLE (
								"IdBloqueoCentralizacion"	NVARCHAR(20),
								"IdTipoCentralizacion"		BIGINT,
								"FechaCentralizacion"      	NVARCHAR(15)
	)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN AUTONOMOUS TRANSACTION
		INSERT INTO "BSCL.Contabilidad.BD::GBL.BloqueoCentralizaciones"
		("IdBloqueoCentralizacion","IdTipoCentralizacion","FechaCentralizacion")
		SELECT "IdBloqueoCentralizacion","IdTipoCentralizacion","FechaCentralizacion"
		FROM :p_BloqueoCentralizacion;
	END;
END;
GO