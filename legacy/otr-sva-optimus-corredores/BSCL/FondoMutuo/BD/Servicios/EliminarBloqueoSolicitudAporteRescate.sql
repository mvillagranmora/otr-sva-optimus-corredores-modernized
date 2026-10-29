SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::EliminarBloqueoSolicitudAporteRescate";

CREATE PROCEDURE "BSCL.FondoMutuo.BD.Servicios::EliminarBloqueoSolicitudAporteRescate" (
	IN p_BloqueoSolicitudAporteRescate TABLE (
							"IdBloqueoSolicitudAporteRescate"   NVARCHAR(120)
	)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN AUTONOMOUS TRANSACTION
		DELETE FROM "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate"
		WHERE "IdBloqueoSolicitudAporteRescate" IN (SELECT "IdBloqueoSolicitudAporteRescate" FROM :p_BloqueoSolicitudAporteRescate);
	END;
END;