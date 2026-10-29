SET SCHEMA "BSCL"; 
 
DROP PROCEDURE "BSCL.APV.CrystalReports.Servicios::Traspasos.NotificacionCobranza"; 
 
CREATE PROCEDURE
--
--  Proposito: Servicio para generar notificacion de contratosSAAPV para el Cliente
--  Autor: EORTIZ - PRONETIT
--  Fecha Creacion: 06/10/2017

--
"BSCL.APV.CrystalReports.Servicios::Traspasos.NotificacionCobranza" (
	-- Parametros entrada comunes en todos los servicios privados
	IN 	p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN 	p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	IN	p_Fecha					DATE,
	-- Parametros entrada del servicio privado
	IN p_RUTCliente				NVARCHAR (10),
	IN p_IdContrato		BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

					
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	v_logo = select TOP 1
	"Binario" AS "LogoCorredora"
	from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
	
	SELECT 
		CE."Nombre"		 		AS "NombreEmpresa"
		,CE."RazonSocial" 		AS "RazonSocialEmpresa"
		,CT."IdContratoSAAPV"	AS "IdContratoSAAPV"
		,CT."FolioContratoSAAPV"	AS "FolioSAAPV"
		,CT."RazonSocial" 		AS "RazonSocialTraspaso"
		,CT."NombreEntidad"
		,CT."Descripcion" 		AS "Causal"
		, COALESCE(Persona."Nombres",'')	AS "NombreCliente"
		, COALESCE(Persona."ApellidoPaterno",'')	AS "ApellidoPaternoCliente"
		, COALESCE(Persona."ApellidoMaterno",'')	AS "ApellidoMaternoCliente"
		,CT."DocumentoIdentidadDefecto" 	 		AS "RUTCliente"
		,CT."NombreCompleto"
		,CT."NombreCompleto"
		,:p_Fecha				AS "FechaOptimus"
		--, COALESCE(PortafolioContrato."MontoPesos",0) 						AS "MontoPesos"
		,L."LogoCorredora"
		FROM "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV" AS CT
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS CE	
			ON CT."IdEmpresa" = CE."IdEmpresa" 	
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
			ON Persona."IdPersona" = CT."IdPersonaCli"
		CROSS JOIN :v_logo AS L
		WHERE 	--Contrato."DocumentoIdentidadCliente" = :p_RUTCliente;
			CT."IdContratoSAAPV"	= :p_IdContrato
			AND 	CT."IdEmpresa" 	= :p_IdEmpresa
			AND 	CT."IdEstado"	= 'RZD';
		--------------------
		--FIN SP 
		--------------------
END;