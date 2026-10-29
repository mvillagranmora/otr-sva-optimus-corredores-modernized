SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearBloqueoSolicitudAporteRescate";

CREATE PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearBloqueoSolicitudAporteRescate" (
	IN p_BloqueoSolicitudAporteRescate TABLE (
								"IdBloqueoSolicitudAporteRescate"   NVARCHAR(120),
								"IdPortafolio"						BIGINT,
								"IdSerieFondo"						NVARCHAR(40),
								"IdRegimenTributario"				NVARCHAR(40),
								"TipoSolicitud"      				NVARCHAR(3),
								"Monto"  							DECIMAL(26,8),
								"NumeroCuotas"  					DECIMAL(26,8),
								"FechaSistema"      				DATE
	)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN AUTONOMOUS TRANSACTION
		INSERT INTO "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate"
		("IdBloqueoSolicitudAporteRescate","IdPortafolio","IdSerieFondo","IdRegimenTributario","TipoSolicitud", "Monto", "NumeroCuotas","FechaSistema")
		SELECT "IdBloqueoSolicitudAporteRescate",
				"IdPortafolio",
				"IdSerieFondo",
				"IdRegimenTributario",
				"TipoSolicitud",
				"Monto",
				"NumeroCuotas",
				"FechaSistema"
		FROM :p_BloqueoSolicitudAporteRescate;
	END;
END;