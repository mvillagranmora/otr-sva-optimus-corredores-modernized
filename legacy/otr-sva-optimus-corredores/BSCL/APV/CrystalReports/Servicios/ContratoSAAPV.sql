SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.APV.CrystalReports.Servicios::ContratoSAAPV"; 
GO

CREATE PROCEDURE
--
--  Proposito: Servicio para generar el contratosSAAPV
--  Autor: EORTIZ - PRONETIT
--  Fecha Creacion: 14/02/2017
--	MODIFICACION
--  Autor: HMULLER - PRONETIT
--  Fecha Modificacion: 20/04/2019
--
"BSCL.APV.CrystalReports.Servicios::ContratoSAAPV" (
	-- Parametros entrada comunes en todos los servicios privados
	IN 	p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN 	p_Solicitud				NVARCHAR(1),	--ID del Modulo que lo invoca
	-- Parametros entrada del servicio privado
	IN p_IdContrato				BIGINT,
	IN p_Seccion				NVARCHAR(3)
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

	IF :p_Seccion='REP' THEN
		SELECT
				:p_Solicitud  																																							AS "TipoSolicitud"
				 , Contrato."IdContratoSAAPV"																																			AS "IdContratoSAAPV" 
				 , Contrato."NombrePlan"																																				AS "NombrePlanAhorro"
				 , TO_NVARCHAR(Contrato."FechaSuscripcion", 'DD-MM-YYYY')																												AS "FechaSuscripcion"
				 , Contrato."FolioContratoSAAPV"																																		AS "FolioContrato"
				 , Contrato."DocumentoIdentidadCliente"																																	AS "DocumentoIdentidadCliente"
				 , COALESCE(Persona."Nombres",'')																																		AS "NombreCliente"
				 , COALESCE(Persona."ApellidoPaterno",'')																																AS "ApellidoPaternoCliente"
				 , COALESCE(Persona."ApellidoMaterno",'')																																AS "ApellidoMaternoCliente"
				 , COALESCE(TO_NVARCHAR(Persona."FechaNacimiento", 'DD-MM-YYYY'),'')																									AS "FechaNacimientoCliente"
				 , PaisNacimiento."NombrePais"																																			AS "Nacionalidad"
				 , COALESCE(TipoEstadoCivil."Nombre",'')																																AS "EstadoCivil"
				 , COALESCE(MAP(Persona."Sexo",'M','MASCULINO','FEMENINO'),'')																											AS "Genero"
				 , COALESCE(Pais."NombrePais",'')																																		AS "Pais"
				 , COALESCE(Ciudad."NombreCiudad",'')																																	AS "Ciudad"
				 , COALESCE(ComunaCliente."NombreDivisionPais",'')																														AS "ComunaCliente"	
				 , COALESCE(CDI."NombreClasificacionDivisionPais1",'')   																												AS "RegionCliente"
				 , COALESCE(CDIE."NombreClasificacionDivisionPais1",'')  																												AS "RegionEmpleador"
				 , COALESCE(CDIE."NombreDivisionPais",'')      																															AS "CiudadEmpleador" 
				 , CASE 
		   				WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."Calle"
						WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."Calle"
						WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."Calle"
						WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."Calle"
						ELSE ''
				   END	 																																								AS "DireccionCliente"
		   		 , CASE 
		   				WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."Numero"
						WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."Numero"
						WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."Numero"
						WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."Numero"
						ELSE ''
				   END																																									AS "NumeroDireccionCliente"
				 , COALESCE(Profesion."Nombre",'')																																		AS "CargoPersona"
				 , COALESCE(Inversionista."IdCategoriaInversionista",'')																											    AS "CategoriaInversionista"
				 , COALESCE(PerfilComercial."IdPerfilComercialBaseOrigen",'')																											AS "PerfilComercial"
				 , CASE WHEN TelefonoFijo."CodigoProposito" = 'PREV' THEN TelefonoFijo."Numero"
				 		WHEN TelefonoFijo."CodigoProposito" = 'PART' THEN TelefonoFijo."Numero"
				 		WHEN TelefonoFijo."CodigoProposito" = 'LABO' THEN TelefonoFijo."Numero"
				 		WHEN TelefonoFijo."CodigoProposito" = 'COME' THEN TelefonoFijo."Numero"
				 		ELSE ''
				   END 																																									AS "TelefonoCliente"
				 , CASE WHEN TelefonoMovil."CodigoProposito" = 'PREV' THEN TelefonoMovil."Numero"
				 		WHEN TelefonoMovil."CodigoProposito" = 'PART' THEN TelefonoMovil."Numero"
				 		WHEN TelefonoMovil."CodigoProposito" = 'LABO' THEN TelefonoMovil."Numero"
				 		WHEN TelefonoMovil."CodigoProposito" = 'COME' THEN TelefonoMovil."Numero"
				 		ELSE ''
				   END 																																									AS "TelefonoCelularCliente"
				 , CASE WHEN TelefonoOtro."CodigoProposito" = 'PREV' THEN TelefonoOtro."Numero"
				 		WHEN TelefonoOtro."CodigoProposito" = 'PART' THEN TelefonoOtro."Numero"
				 		WHEN TelefonoOtro."CodigoProposito" = 'LABO' THEN TelefonoOtro."Numero"
				 		WHEN TelefonoOtro."CodigoProposito" = 'COME' THEN TelefonoOtro."Numero"
				 		ELSE ''
				   END 																																									AS "TelefonoComercialCliente"																																	
				 , Contrato."EmailCliente"																																				AS "EmailCliente"
				 , COALESCE(Empleador."RazonSocial",'')																																	AS "NombreEmpleador"
				 , Contrato."DocumentoIdentidadEmpleador"																																AS "DocumentoIdentidadEmpleador"
				 , COALESCE(TelefonoEmpleador."Numero",'')																																AS "TelefonoEmpleador"
				 , COALESCE(DireccionFisicaEmpleador."Calle",'')																														AS "DireccionComercialEmpleador"
				 , COALESCE(DireccionFisicaEmpleador."Numero",'')																														AS "NumeroDireccionEmpleador"
				 , COALESCE(ComunaEmpleador."NombreDivisionPais",'')																													AS "ComunaEmpleador"
				 , Contrato."NombreEncargado"																																			AS "NombreEncargado"
				 , Contrato."EmailEncargado"																																			AS "EmailEncargado"
				 , Contrato."TelefonoEncargado"																																			AS "TelefonoEncargado"				
				 , MAP(Contrato."IdMedioRecaudacionTrabajador",1,'IPS-EX INP','AFP')																									AS "MedioRecaudacion"
				 , COALESCE(EntidadRecaudadora."Nombre",'')																																AS "EntidadRecaudadora"
				 , Contrato."CodigoPrevision"																																			AS "CodigoPrevision"
				 , Contrato."Pensionado"																																				AS "Pensionado"
				 , Contrato."Trabajador"																																				AS "Trabajador"
				 , Contrato."FormaDePago"																																				AS "FormaDePago"
				 , MAP(Contrato."FormaDePago",'DIRECTA',MAP(Contrato."IdMedioRecaudacionFormaPago",1,'DIRECTAMENTE AL TRABAJADOR',2,'A TRAVES DEL EMPLEADOR','PAC'),NULL)				AS "Recaudador"
				 , MAP(Contrato."FormaDePago",'DIRECTA',NULL,MAP(Contrato."IdMedioRecaudacionTrabajador",1,'IPS-EX INP','AFP'))															AS "Recaudacion"
				 , MAP(Contrato."FormaDePago",'DIRECTA',NULL,EntidadRecaudadora."Nombre")																								AS "Institucion"
				 , Contrato."DiaDescuento"																																				AS "DiaDescuento"
				 , Contrato."ComisionTransferencia"																																		AS "ComisionTransferencia"
				 , Contrato."NombreEntidadOrigen"																																		AS "NombreEntidadOrigen"
				 , Contrato."RutEntidadOrigen"																																			AS "RutEntidadOrigen"
				 , Contrato."TipoDeAhorro"                                              																								AS "TipoDeAhorro"
				 , Contrato."FondoDeOrigen"                                             																								AS "FondoDeOrigen"
				 , Contrato."TipoTraspaso"                                              																								AS "TipoTraspaso"
				 , Contrato."MontoTraspaso"                                             																								AS "MontoTraspaso"
				 , Contrato."UFTraspaso"                                                																								AS "UFTraspaso"
				 , Contrato."PorcentajeTraspaso"																																		AS "PorcentajeTraspaso"
				 , COALESCE(MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."IdPortafolio"),0)																		AS "IdPortafolio"
				 , COALESCE(MAP(PortafolioContrato."IdPortafolio",NULL,NULL,MatrizAgrupador."Producto"),'')																				AS "Producto"
				 , CASE WHEN (COALESCE(DIS."MontoPesos",0) = 0) THEN COALESCE(PortafolioContrato."MontoPesos",0)
				   ELSE COALESCE(DIS."MontoPesos",0)
				   END 																																									AS "MontoPesos"
				 , COALESCE(PortafolioContrato."MontoUF",0)																																AS "MontoUF"
		         , COALESCE(PortafolioContrato."PorcentajeRegimenImponible",0)																											AS "Porcentaje"
				 , COALESCE(MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."Pago"),'')																				AS "Pago"
				 , COALESCE(MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."PeriodoInicio"),'')																		AS "PeriodoInicio"
				 , COALESCE(MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."PeriodoTermino"),'')																	AS "PeriodoTermino"
				 , COALESCE(MAP(PortafolioContrato."IdPortafolio",NULL,NULL,MatrizAgrupador."Codigo"),'')
				 , COALESCE(Portafolio."IdMatrizAgrupadorAPV",0)																														AS "IdMatrizAgrupadorAPV"
				 , COALESCE(MatrizAgrupador."CodigoAgrupador",'')																														AS "CodigoAgrupador"
				 , COALESCE(Portafolio."Proposito",'')
				 , COALESCE(DIS."FolioContratoSAAPV",0)     																															AS "FolioContratoSAAPVDist"
				 , COALESCE(DIS."IdInstrumento",0)																																		AS "IdInstrumento"
				 , COALESCE(DIS."RazonSocial",'')																																		AS "RazonSocial"
				 , COALESCE(DIS."Nemotecnico",'')																																		AS "Nemotecnico"
				 , COALESCE(DIS."MontoPesos",0)																																			AS "MontoPesosDis"
				 , COALESCE(DIS."Porcentaje",0)																																			AS "PorcentajeDist"
				 , L."LogoCorredora"																																					AS "LogoCorredora"
		FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
						 ON Persona."IdPersona" = Contrato."IdPersona"
						AND Persona."Tipo" = 'N'
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Empleador
						 ON Empleador."IdPersona" = Contrato."IdEmpleador"
						AND Empleador."Tipo" = 'J'
			LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.TipoEstadoCivil" AS TipoEstadoCivil
						 ON TipoEstadoCivil."IdTipoEstadoCivil" = Persona."EstadoCivil"
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaPREV
			 			 ON DireccionFisicaPREV."IdPersona" = Contrato."IdPersona"
						AND DireccionFisicaPREV."CodigoProposito" = 'PREV'
						AND DireccionFisicaPREV."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Contrato."IdPersona" AND "CodigoProposito" = 'PREV')
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaPART
			 			 ON DireccionFisicaPART."IdPersona" = Contrato."IdPersona"
						AND DireccionFisicaPART."CodigoProposito" = 'PART'
						AND DireccionFisicaPART."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Contrato."IdPersona" AND "CodigoProposito" = 'PART')
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaLABO
			 			 ON DireccionFisicaLABO."IdPersona" = Contrato."IdPersona"
						AND DireccionFisicaLABO."CodigoProposito" = 'LABO'
						AND DireccionFisicaLABO."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Contrato."IdPersona" AND "CodigoProposito" = 'LABO')
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaCOME
			 			 ON DireccionFisicaCOME."IdPersona" = Contrato."IdPersona"
						AND DireccionFisicaCOME."CodigoProposito" = 'COME'
						AND DireccionFisicaCOME."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Contrato."IdPersona" AND "CodigoProposito" = 'COME')
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaEmpleador
						 ON DireccionFisicaEmpleador."IdPersona" = Empleador."IdPersona"
			LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais
						 ON Pais."IdPais" = Persona."IdPaisResidencia"
			LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PaisNacimiento
				 		 ON PaisNacimiento."IdPais" = Persona."IdPaisNacionalidadPrincipal"
			LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad
						 ON Ciudad."IdCiudad" = Contrato."IdCiudadCliente"
			LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS ComunaCliente
						 ON ComunaCliente."IdDivisionPais" = Contrato."IdComunaCliente"
			/**/
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" AS CDI
		   				 ON Contrato."IdComunaCliente" =  CDI."IdDivisionPais"
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" AS CDIE
		   				 ON Contrato."IdComunaEmpleador" =  CDIE."IdDivisionPais"
			/**/
			LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS ComunaEmpleador
						 ON ComunaEmpleador."IdDivisionPais" = DireccionFisicaEmpleador."IdDivisionPais"
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Inversionista
						 ON Inversionista."IdPersona" = Persona."IdPersona"
			LEFT OUTER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
						 ON PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
			LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
						 ON Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
						AND Portafolio."IdCliente" = Contrato."IdCliente"
		    			AND Portafolio."Proposito" = 'PROPOSITOAPV'
			LEFT OUTER JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora
						 ON EntidadRecaudadora."Codigo" = Contrato."CodigoPrevision"
			LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
		    			 ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		    LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoFijo
		    			 ON TelefonoFijo."IdPersona" = Contrato."IdPersona"
		    			AND TelefonoFijo."Borrado" = 'N'
		    			AND TelefonoFijo."IdTipoTelefono" = 'FIJO'
		    			AND TelefonoFijo."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Contrato."IdPersona" AND "IdTipoTelefono" = 'FIJO' AND "Borrado" = 'N')
		    LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoMovil
		    			 ON TelefonoMovil."IdPersona" = Contrato."IdPersona"
		    			AND TelefonoMovil."Borrado" = 'N'
		    			AND TelefonoMovil."IdTipoTelefono" = 'MOVIL'
		    			AND TelefonoMovil."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Contrato."IdPersona" AND "IdTipoTelefono" = 'MOVIL' AND "Borrado" = 'N')
		    LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoOtro
		    			 ON TelefonoOtro."IdPersona" = Contrato."IdPersona"
		    			AND TelefonoOtro."Borrado" = 'N'
		    			AND TelefonoOtro."IdTipoTelefono" = 'OTRO'
		    			AND TelefonoOtro."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Contrato."IdPersona" AND "IdTipoTelefono" = 'OTRO' AND "Borrado" = 'N')
		    LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoEmpleador
		    			 ON TelefonoEmpleador."IdPersona" = Empleador."IdPersona"
		    			AND TelefonoEmpleador."Borrado" = 'N'
			LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PerfilComercial
						 ON PerfilComercial."IdCliente" = Contrato."IdCliente"
						AND PerfilComercial."Proposito" = 'PROPGENERALES'
						AND PerfilComercial."IdEstado" <> 'INA'
			LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Profesion" AS Profesion
						 ON Profesion."IdProfesion" = Contrato."IdTipoCargoPersona"
			 LEFT OUTER JOIN "BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion" AS DIS
		   				 ON Contrato."IdContratoSAAPV" = DIS."IdContratoSAAPV"
			CROSS JOIN :v_logo AS L
				WHERE 	Contrato."IdContratoSAAPV"	= :p_IdContrato
					AND 	Contrato."IdEmpresa" 	= :p_IdEmpresa
					AND Portafolio."IdEstado" <> 'INA';
	END IF;			
					
	IF :p_Seccion='ENT' THEN
				SELECT 	
						EntidadRecaudadora."IdEntidadRecaudadora"								AS "IdEntidadRecaudadora"
					   ,EntidadRecaudadora."IdPersona"											AS "IdPersona"
					   ,EntidadRecaudadora."Codigo"												AS "Codigo"
					   ,TipoEntidadRecaudadora."Nombre" || ' ' || EntidadRecaudadora."Nombre" 	AS "Nombre"
					   ,EntidadRecaudadora."Estado"												AS "Estado"
					   ,EntidadRecaudadora."IdTipoEntidadRecaudadora"							AS "IdTipoEntidadRecaudadora"
					   ,TipoEntidadRecaudadora."Nombre"											AS "TipoEntidad"
					   ,EntidadRecaudadora."Nombre" 											AS "NombreEntidad"
					FROM
						"BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora
					INNER JOIN
						"BSCL.APV.BD::SCS.TipoEntidadRecaudadora" AS TipoEntidadRecaudadora
					 ON TipoEntidadRecaudadora."IdTipoEntidadRecaudadora" = EntidadRecaudadora."IdTipoEntidadRecaudadora";		
	END IF;		
END;
GO