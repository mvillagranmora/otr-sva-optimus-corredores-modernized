SET SCHEMA "BSCL"; 
 
DROP PROCEDURE "BSCL.APV.CrystalReports.Servicios::NotificacionSAAPVEmpleador"; 
 
CREATE PROCEDURE
--
--  Proposito: Servicio para generar notificacion de contratosSAAPV para el Cliente
--  Autor: EORTIZ - PRONETIT
--  Fecha Creacion: 04/06/2018

--
"BSCL.APV.CrystalReports.Servicios::NotificacionSAAPVEmpleador" (
	-- Parametros entrada comunes en todos los servicios privados
	IN 	p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN 	p_IdModulo		NVARCHAR(3),	--ID del Modulo que lo invoca
	IN	p_Fecha			DATE,
	-- Parametros entrada del servicio privado
	IN p_IdEmpleador	NVARCHAR (10)
	/*IN p_RUTCliente	NVARCHAR (10)
	IN p_IdContrato		BIGINT eliminado por madnato fde R.Abreu*/
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
		:p_IdEmpresa 																					AS "IdEmpresa"
		,Contrato."IdContratoSAAPV"																		AS "IdContratoSAAPV"
		,Contrato."FolioContratoSAAPV"																	AS "FolioContratoSAAPV"
		--Empleador
		,Empleador."RazonSocial"																		AS "RazonSocialEmpleador"
		,Contrato."DocumentoIdentidadEmpleador"															AS "DocumentoIdentidadEmpleador"
		,COALESCE(DireccionFisicaEmpleador."Calle",'')													AS "DireccionComercialEmpleador"
		,COALESCE(DireccionFisicaEmpleador."Numero",'')			 										AS "NumeroDireccionEmpleador"
		,COALESCE(ComunaEmpleador."NombreDivisionPais",'')												AS "ComunaEmpleador"
		,COALESCE(CDIE."NombreDivisionPais",'')      													AS "CiudadEmpleador" 
		--CLIENTE
		,Contrato."DocumentoIdentidadCliente"															AS "DocumentoIdentidadCliente"
		--,Contrato."FolioContratoSAAPV"																	AS "FolioContratoSAAPV"	
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
		,F."FirmaCorredora"
		FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
		LEFT OUTER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
						 ON PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
		LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
						 ON Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
						AND Portafolio."IdCliente" = Contrato."IdCliente"
		    			AND Portafolio."Proposito" = 'PROPOSITOAPV'
		LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
		    			 ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"	
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Empleador
						 ON Empleador."IdPersona" = Contrato."IdEmpleador"
						AND Empleador."Tipo" = 'J'
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" AS CDIE
		   				 ON Contrato."IdComunaEmpleador" =  CDIE."IdDivisionPais"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaEmpleador
						 ON DireccionFisicaEmpleador."IdPersona" = Empleador."IdPersona"
						AND DireccionFisicaEmpleador."IdDireccionFisica" = Empleador."IdDireccionFisicaPrincipal"
	    LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS ComunaEmpleador
						 ON ComunaEmpleador."IdDivisionPais" = DireccionFisicaEmpleador."IdDivisionPais"
		CROSS JOIN :v_logo AS L
		CROSS JOIN :v_firma AS F
		WHERE  Contrato."IdEmpleador" = p_IdEmpleador
			/*Eliminado Por mandato de Rogers A.*/
			/*Contrato."DocumentoIdentidadCliente" = :p_RUTCliente
			AND		Contrato."IdContratoSAAPV"		 = :p_IdContrato*/
			AND 	Contrato."IdEmpresa" 			 = :p_IdEmpresa
			AND		Contrato."IdEstadoEnvioNotificacion" = 'NEV'
			AND		Portafolio."IdEstado" <> 'INA'
		ORDER BY Contrato."FolioContratoSAAPV";
		--------------------
		--FIN SP 
		--------------------
END;