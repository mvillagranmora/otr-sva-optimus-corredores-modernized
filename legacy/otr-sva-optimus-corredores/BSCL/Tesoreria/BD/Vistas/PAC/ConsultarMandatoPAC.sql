SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPAC";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPAC" AS

SELECT
	 PAC."IdMandatoPAC"
	,PAC."IdPortafolio"
	,PAC."IdCliente"
	,PAC."FolioCSC"
	,PAC."CuentaCorriente"
	,COALESCE (CSAAPV."FolioContratoSAAPV",'0') AS "FolioSAAPV"
	,PAC."Receptor"
	,PAC."Fecha"
	,PAC."NumeroMandato"
	,PAC."Firma" 
	,COALESCE (ES."Nombre", PAC."Estado") 							AS "Estado"
	,PAC."Estado" AS "IdEstado"
	,CASE 
		WHEN PAC."FolioSAAPV" != '0'
			THEN CASE WHEN PAC."Estado" = 'PAU' THEN  ESSA."Nombre" ELSE ESS."Nombre" END
			ELSE CASE WHEN PAC."Estado" = 'PAU' THEN  ESCA."Nombre" ELSE ESC."Nombre" END
		END															AS "EstadoContrato" 
	,PAC."IdEmpresa"
	,PAC."Canal"
	,PAC."EstadoCargo" 
	,PAC."IdentServicio" 											AS "FolioPacMandatoAgrupador"
	,CC."NombreCompleto"
	,CC."DocumentoIdentidad"
	,CBP."NombrePlaza"
	,SN."IdPersona"
	,CBP."NombreMoneda"
	,CBP."NombreTipoCuentaBancaria"
	,CBP."NombreBanco"
	,CBP."NumeroCuentaBancaria"
	,CBP."IdCuentaBancaria"
	,CBP."NombreCuentaBancariaFull"
	,SN."IdSocioDeNegocio"
	,COALESCE (CSC."MontoPesos"
	,COALESCE(MONTOAPV."MontoPesos",0)) 							AS "MontoPesos"
	,COALESCE (CSC."MontoUF"
	,COALESCE(MONTOAPV."MontoUF",0)) 								AS "MontoUF"
	,(  COALESCE(MONTOPACTOTAL.		"TotalMontoPesos",0) 	
	 +  COALESCE(MONTOPACCSCTOTAL.	"TotalMontoPesos",0)) 			AS "TotalMontoPesos"
	,(	COALESCE(MONTOPACTOTAL.		"TotalMontoUF",0)   
	 +	COALESCE(MONTOPACCSCTOTAL.	"TotalMontoUF",0)) 				AS "TotalMontoUF"
	,CP."IdEstado" AS "EstadoPortafolio"
	,CC."IdEstado" AS "EstadoCliente"
	,CC."IdDireccionFisicaPrincipal"
	,CC."IdTelefonoPrincipal"
	,COALESCE(CSC."DiaDescuento",CSAAPV."DiaDescuento") 			AS "DiaDescuento"
	,PAC."DiaDescuentoPorAutorizar"
	,PAC."EstadoContratoPorAutorizar"
	,PAC."EstadoPorAutorizar"
	FROM 	"BSCL.Tesoreria.BD::PAC.MandatoPAC" 					AS PAC 	---------------------
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS CC 		ON (CC."IdCliente" 				= PAC."IdCliente") 
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 			AS SN 		ON (SN."IdCliente" 				= PAC."IdCliente") 
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CBP ON (CBP."IdCuentaBancaria" = PAC."IdCuentaBancaria")  
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					AS CP 		ON (CP."IdPortafolio"			= PAC."IdPortafolio") 
	LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" 	AS CSC 		ON (PAC."FolioCSC" 				= CSC."FolioCSC" AND CSC."TipoPago"= 'PAC')  
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" 						AS ES 		ON (ES."IdEstado" 				= PAC."Estado")
	LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 						AS CSAAPV 	ON (PAC."FolioSAAPV" 			= CSAAPV."FolioContratoSAAPV" AND CSAAPV."IdCliente" = PAC."IdCliente" AND PAC."FolioCSC" = '0')
	LEFT JOIN  "BSCL.Plataforma.BD::PLA.Estado"						AS ESC		ON (ESC."IdEstado" 				= CSC."Estado")
	LEFT JOIN  "BSCL.APV.BD::APV.Estado"							AS ESS		ON (ESS."Codigo" 				= CSAAPV."IdEstado" AND ESS."NombreEntidad" = 'CONTRATO')
	LEFT JOIN  "BSCL.APV.BD::APV.Estado"							AS ESSA		ON (ESSA."Codigo" 				= PAC."EstadoContratoPorAutorizar" AND ESSA."NombreEntidad" = 'CONTRATO')
	LEFT JOIN  "BSCL.Plataforma.BD::PLA.Estado"						AS ESCA		ON (ESCA."IdEstado" 			= PAC."EstadoContratoPorAutorizar")

	----------------
	LEFT JOIN  (
		SELECT 
			 SUM (por."MontoPesos") 		AS "MontoPesos" 
			,SUM (por."MontoUF")			AS "MontoUF"
			,SAP."FolioContratoSAAPV" 
		
		FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" 				AS PAC 	-------------
		INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 			AS SAP ON (cast (PAC."FolioSAAPV" AS VARCHAR) = SAP."FolioContratoSAAPV") 
		INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" 	AS por ON (por."IdContratoSAAPV" = SAP."IdContratoSAAPV") 
		
		WHERE por."IdEstado" = 'VIG'
	      AND SAP."IdEstado" = 'VIG'
		
	      GROUP BY SAP."FolioContratoSAAPV") AS MONTOAPV 
	ON (MONTOAPV."FolioContratoSAAPV" = cast (PAC."FolioSAAPV" as varchar)) 
	----------------
	LEFT JOIN (
		SELECT 
			 SUM (por."MontoPesos") AS "TotalMontoPesos"
			,SUM (por."MontoUF") 	AS "TotalMontoUF"
			,PAC."IdentServicio" 	AS "FolioPacMandatoAgrupador" 
		
		FROM "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS POR 	--------------	
		INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 	AS SAP ON (POR."IdContratoSAAPV" = SAP."IdContratoSAAPV") 
		INNER JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" 	AS PAC ON TO_NVARCHAR(PAC."FolioSAAPV") = SAP."FolioContratoSAAPV" 
		
		GROUP BY PAC."IdentServicio") AS MONTOPACTOTAL 
	ON MONTOPACTOTAL."FolioPacMandatoAgrupador" = PAC."IdentServicio" 
	----------------
	LEFT JOIN (
		SELECT 
			 SUM (CSC."MontoPesos") AS "TotalMontoPesos" 
			,SUM (CSC."MontoUF") 	AS "TotalMontoUF"
			,PAC."IdentServicio" 	AS "FolioPacMandatoAgrupador" 
		
		FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" 	AS CSC -------------		
		INNER JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" 				AS PAC ON (PAC."FolioCSC") = CSC."FolioCSC" 
		
		WHERE CSC."Estado" = 'ING'
		GROUP BY PAC."IdentServicio") AS MONTOPACCSCTOTAL 
	ON MONTOPACCSCTOTAL."FolioPacMandatoAgrupador" = PAC."IdentServicio";


GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPAC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;