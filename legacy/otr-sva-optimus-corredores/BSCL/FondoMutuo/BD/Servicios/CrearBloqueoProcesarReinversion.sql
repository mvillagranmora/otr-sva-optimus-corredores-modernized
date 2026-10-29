SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearBloqueoProcesarReinversion";

CREATE PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearBloqueoProcesarReinversion" (
	IN p_BloqueoReinversion TABLE (
								"IdBloqueaReinversion"   NVARCHAR(100),
								"IdPortafolio"						BIGINT,
								"IdSerieFondo"						NVARCHAR(40),
								"IdRegimenTributario"				NVARCHAR(40),
								"IdCliente"							BIGINT,
								"NumeroCuotas"  					DECIMAL(26,8),
								"Monto" 							DECIMAL(26,8),
								"FechaSistema"      				DATE,
								"IdGrupoRegimen"					NVARCHAR(40)
	)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN AUTONOMOUS TRANSACTION
		INSERT INTO "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion"
		("IdBloqueaReinversion","IdPortafolio","IdRegimenTributario","IdSerieFondo","Monto","NumeroCuotas","FechaSistema","IdCliente","IdGrupoRegimen")
		SELECT "IdBloqueaReinversion",
				"IdPortafolio",
				"IdRegimenTributario",
				"IdSerieFondo",
				"Monto",
				"NumeroCuotas",
				"FechaSistema",
				"IdCliente",
				"IdGrupoRegimen"
		FROM :p_BloqueoReinversion;
	END;
END;