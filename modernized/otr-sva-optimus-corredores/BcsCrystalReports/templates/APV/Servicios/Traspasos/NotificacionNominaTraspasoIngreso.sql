SET SCHEMA "BSCL"; 
 
DROP PROCEDURE "BSCL.APV.CrystalReports.Servicios::Traspasos.NotificacionNominaTraspasoIngreso"; 
 
CREATE PROCEDURE
--
--  Proposito: Servicio para generar notificacion de contratosSAAPV para el Cliente
--  Autor: EORTIZ - PRONETIT
--  Fecha Creacion: 06/10/2017

--
"BSCL.APV.CrystalReports.Servicios::Traspasos.NotificacionNominaTraspasoIngreso" (
	-- Parametros entrada comunes en todos los servicios privados
	IN 	p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN 	p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	IN	p_Fecha_I				DATE,
	IN  p_Fecha_F				DATE
	-- Parametros entrada del servicio privado
	--IN p_SolicitudTraspasos	TABLE ("IdContratoSAAPV" BIGINT, "FolioContratoSAAPV" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

					
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	DECLARE v_firma TABLE("FirmaCorredora" BLOB);
	
	v_logo = select TOP 1
	"Binario" AS "LogoCorredora"
	from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
	
	v_firma = select TOP 1
	"Binario" AS "FirmaCorredora"
	from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;
	
	SELECT 
		CE."Nombre"		 			AS "NombreEmpresa"
		,CE."RazonSocial" 			AS "RazonSocialEmpresa"
		,CT."IdContratoSAAPV"		AS "IdContratoSAAPV"
		,CT."FolioContratoSAAPV"	AS "FolioSAAPV"
		,CT."RazonSocial" 			AS "RazonSocialTraspaso"
		,CT."NombreEntidad"			AS "NombreEntidad"
		,CT."Descripcion" 			AS "Causal"
		, COALESCE(Persona."Nombres",'')			AS "NombreCliente"
		, COALESCE(Persona."ApellidoPaterno",'')	AS "ApellidoPaternoCliente"
		, COALESCE(Persona."ApellidoMaterno",'')	AS "ApellidoMaternoCliente"
		,CT."DocumentoIdentidadDefecto" 	 		AS "RUTCliente"
		,CT."NombreCompleto"						AS "NombreCompleto"
		,CDT."Codigo"								AS "TipoAhorro"
		,:p_Fecha_I									AS "FechaInicioBusqueda"
		,:p_Fecha_F									AS "FechaFinalBusqueda"
		--, COALESCE(PortafolioContrato."MontoPesos",0) 						AS "MontoPesos"
		,L."LogoCorredora"
		,F."FirmaCorredora"
		FROM "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV" AS CT
		LEFT JOIN "BSCL.APV.BD.Vistas::TRA.ConsultarDetalleTraspasosAPV" CDT
			ON CDT."IdTraspaso" = CT."IdTraspaso"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS CE	
			ON CT."IdEmpresa" = CE."IdEmpresa" 	
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
			ON Persona."IdPersona" = CT."IdPersonaCli"		
		CROSS JOIN :v_logo AS L
		CROSS JOIN :v_firma AS F
		WHERE 	CT."IdEmpresa" 	= :p_IdEmpresa
			AND CT."IdEstado"	= 'PDI'
			AND CT."TipoMovimiento" = 'INGRESO'
			AND CT."FechaIngreso" BETWEEN :p_Fecha_I AND :p_Fecha_F;
		--------------------
		--FIN SP 
		--------------------
END;