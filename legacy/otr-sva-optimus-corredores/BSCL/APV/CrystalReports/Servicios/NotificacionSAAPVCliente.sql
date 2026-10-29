SET SCHEMA "BSCL"; 
 
DROP PROCEDURE "BSCL.APV.CrystalReports.Servicios::NotificacionSAAPVCliente"; 
 
CREATE PROCEDURE
--
--  Proposito: Servicio para generar notificacion de contratosSAAPV para el Cliente
--  Autor: EORTIZ - PRONETIT
--  Fecha Creacion: 06/10/2017

--
"BSCL.APV.CrystalReports.Servicios::NotificacionSAAPVCliente" (
	-- Parametros entrada comunes en todos los servicios privados
	IN 	p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN 	p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	IN	p_Fecha					DATE,
	-- Parametros entrada del servicio privado
	IN p_RUTCliente				NVARCHAR (10)
	,	IN p_IdContrato		BIGINT
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
		:p_IdEmpresa 																					AS "IdEmpresa"
		,Contrato."IdContratoSAAPV"																		AS "IdContratoSAAPV"
		,Contrato."FolioContratoSAAPV"																	AS "FolioContratoSAAPV"
		--Empleador
		,Contrato."RazonSocialEmpleador"																AS "RazonSocialEmpleador"
		,Contrato."DocumentoIdentidadEmpleador"															AS "DocumentoIdentidadEmpleador"
		--Cliente
		,Contrato."DocumentoIdentidadCliente"															AS "DocumentoIdentidadCliente"
		,Contrato."FolioContratoSAAPV"																	AS "FolioContratoSAAPV"	
		,Contrato."NombreCliente"||' '||Contrato."ApellidoPaternoCliente"||' '||Contrato."ApellidoMaternoCliente" AS "NombreCompleto"	
		, MAP(PortafolioContrato."IdPortafolio",NULL,NULL,MatrizAgrupador."Codigo") 					AS "RegimenTributario"
		, MAP(PortafolioContrato."IdPortafolio",NULL,NULL,MatrizAgrupador."Producto")					AS "Producto"
		, COALESCE(PortafolioContrato."MontoPesos",0) 													AS "MontoPesos"
		, COALESCE(PortafolioContrato."MontoUF",0)														AS "MontoUF"
		, COALESCE(PortafolioContrato."PorcentajeRegimenImponible",0)									AS "Porcentaje"
		, MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."PeriodoInicio")	        AS "PeriodoInicio"
		, MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."PeriodoTermino")		    AS "PeriodoTermino"
		,'CLP' 																							AS "Moneda"
		,:p_Fecha																						AS "FechaOptimus"
		,L."LogoCorredora"
		FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
		LEFT OUTER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
						 ON PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
		LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
						 ON Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
						AND Portafolio."IdCliente" = Contrato."IdCliente"
		    			AND Portafolio."Proposito" = 'PROPOSITOAPV'
		LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
		    			 ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"		    
		CROSS JOIN :v_logo AS L
		WHERE 	Contrato."DocumentoIdentidadCliente" = :p_RUTCliente
			AND		Contrato."IdContratoSAAPV"		 = :p_IdContrato
			AND 	Contrato."IdEmpresa" 			 = :p_IdEmpresa
			AND		Portafolio."IdEstado" <> 'INA';
		--------------------
		--FIN SP 
		--------------------
END;