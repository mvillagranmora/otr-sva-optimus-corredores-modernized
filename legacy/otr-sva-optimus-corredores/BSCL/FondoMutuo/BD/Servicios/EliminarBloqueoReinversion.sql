SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::EliminarBloqueoReinversion";

CREATE PROCEDURE "BSCL.FondoMutuo.BD.Servicios::EliminarBloqueoReinversion" (
	IN p_BloqueoReinversion TABLE (
							"IdBloqueaReinversion"   NVARCHAR(100)
	)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN AUTONOMOUS TRANSACTION
		DELETE FROM "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion"
		WHERE "IdBloqueaReinversion" IN (SELECT "IdBloqueaReinversion" FROM :p_BloqueoReinversion);

	END;
END;