SET SCHEMA "BSCL";
--CALL "BSCL.APV.BD.Servicios::ConsultarGestionContratosSAAPV"('813',?);
DROP PROCEDURE "BSCL.APV.BD.Servicios::ConsultarGestionContratosSAAPV";

CREATE PROCEDURE "BSCL.APV.BD.Servicios::ConsultarGestionContratosSAAPV"(
IN p_FolioContratoSAAPV NVARCHAR(50),
OUT P_CONTRATOS TABLE ("IdRelacionado"								BIGINT
					 , "IdContratoSAAPV"							BIGINT  		
					 , "FolioContratoSAAPV"							NVARCHAR(50)	
					 , "IdPlan"										BIGINT  		
					 , "NombrePlan"									NVARCHAR(100)	
					 , "FechaSuscripcion"							DATE			
					 , "FechaCreacion"								DATE			
					 , "FechaAprobacion"							DATE			
					 , "FechaNotificacion"							DATE			
					 , "FechaRecepcion"								DATE			
					 , "Folio"										NVARCHAR(50)	
					 , "IdEnrolamiento"								BIGINT			
					 , "IdPortafolio"								BIGINT			
					 , "CodigoPortafolio"							NVARCHAR(255)	
					 , "IdPersona"									BIGINT			
					 , "RutCliente"									NVARCHAR(40)	
					 , "DocumentoIdentidadCliente"					NVARCHAR(40)	
					 , "NombreCLiente"		 						NVARCHAR(100)	
					 , "ApellidoPaternoCliente"						NVARCHAR(100)	
					 , "ApellidoMaternoCliente"						NVARCHAR(100)	
					 , "NombrePersona"								NVARCHAR(100)	
					 , "IdEmpleadoEjecutivo"						BIGINT			
					 , "NombrePersonaEjecutivo"						NVARCHAR(100)	
					 , "Tipo"										NVARCHAR(1)		
					 , "IdEmpleador"								BIGINT			
					 , "RutEmpleador"								NVARCHAR(40)	
					 , "DocumentoIdentidadEmpleador"				NVARCHAR(40)		
					 , "NombreEmpleador"							NVARCHAR(100)	
					 , "IdCliente"									BIGINT			
					 , "FechaNacimientoCliente"						DATE			
					 , "EstadoCivil"								NVARCHAR(2)			
					 , "Genero"										NVARCHAR(1)		
					 , "IdPaisResidenciaCliente"					BIGINT			
					 , "IdCiudadCliente"							BIGINT			
					 , "NombreCiudad"								NVARCHAR(255)	
					 , "IdComunaCliente"							BIGINT			
					 , "NombreComuna"								NVARCHAR(255)	
			   		 , "DireccionCliente"							NVARCHAR(100)	
			   		 , "NumeroDirecionCliente"						NVARCHAR(10)	
					 , "IdTipoCargoPersona"							BIGINT			
					 , "Cargo"										NVARCHAR(100)	
					 , "IdCategoriaInversionista"					NVARCHAR(40)	
					 , "IdPerfilComercialBaseOrigen"				NVARCHAR(100)	
					 , "TelefonoFijo"								NVARCHAR(100)	
					 , "TelefonoMovil"								NVARCHAR(100)	
					 , "TelefonoLaboral"							NVARCHAR(100)	
					 , "EmailCliente"								NVARCHAR(100)	
					 , "DireccionComercialEmpleador"				NVARCHAR(100)		
					 , "NumeroDirecionEmpleador"					NVARCHAR(100)	
					 , "IdComunaEmpleador"							BIGINT			
					 , "NombreCiudadEmpleador"						NVARCHAR(100)	
					 , "TelefonoEmpleador"							NVARCHAR(100)	
					 , "NombreEncargado"							NVARCHAR(100)	
					 , "EmailEncargado"								NVARCHAR(100)	
					 , "TelefonoEncargado"							NVARCHAR(100)	
					 , "IdMedioRecaudacionTrabajador"				BIGINT			
					 , "IdEntidadRecaudadora"						BIGINT			
					 , "CodigoPrevision"							BIGINT				
					 , "Pensionado"									NVARCHAR(2)		
					 , "Trabajador"									NVARCHAR(20)	
					 , "RegimenTributario"							NVARCHAR(20)	
					 , "OrigenAhorro"								NVARCHAR(10)	
				     , "MontoPesos"									DECIMAL(26,8)	
					 , "MontoUF"									DECIMAL(26,8)	
					 , "PorcentajeRegimenImponible"					DECIMAL(26,8)	
					 , "Pago"										NVARCHAR(100)	
					 , "PeriodoInicio"								NVARCHAR(7)		
					 , "PeriodoTermino"								NVARCHAR(7)		
					 , "FormaDePago"								NVARCHAR(100)	
					 , "IdMedioRecaudacionFormaPago"				BIGINT			
					 , "CodigoMedioRecaudacion"						NVARCHAR(100)	
					 , "NombreMedioRecaudacion"						NVARCHAR(100)	
					 , "DiaDescuento"								BIGINT			
					 , "FormaDePagoIndirecta"						NVARCHAR(100)	
					 , "CodigoInstitucion"							NVARCHAR(100)	
					 , "Institucion"								NVARCHAR(100)	
					 , "ComisionTransferencia"						DECIMAL(26,8)	
					 , "CodigoTipoSolicitud"						NVARCHAR(3)
					 , "NombreEntidadOrigen"						NVARCHAR(100)	
					 , "RutEntidadOrigen"							NVARCHAR(40)	
					 , "TipoDeAhorro"								NVARCHAR(10)  	
					 , "FondoDeOrigen"								NVARCHAR(10)  	
					 , "TipoTraspaso"								NVARCHAR(20)  	
					 , "MontoTraspaso"								DECIMAL(26,8)	
					 , "UFTraspaso"									DECIMAL(26,8)	
					 , "PorcentajeTraspaso"							DECIMAL(26,8)	
					 , "IdEstadoEnvioNotificacion"					NVARCHAR(3)  		
					 , "IdEstadoSolicitud"							NVARCHAR(3)  	
					 , "IdEstado"									NVARCHAR(3)
					 , "IdEstadoEventoAutorizacion"					NVARCHAR(3)  	
					 , "EstadoNotificacion"							NVARCHAR(100)	
					 , "EstadoSolicitud" 							NVARCHAR(100)	
					 , "Estado"										NVARCHAR(100)	
					 , "EstadoEventoAutorizacion"					NVARCHAR(100)	
					 , "EstadoNuevo"								NVARCHAR(100)
					 , "IdEstadoNuevo"								NVARCHAR(3)
					 , "IdCargaMasiva"								BIGINT				
					 , "IdEstadoPortafolio"							NVARCHAR(3)  	
					 , "IdMatrizAgrupadorAPV"						BIGINT			
					 , "Proposito"									NVARCHAR(100)  	
					 , "IdProductoAgrupador"						NVARCHAR(100)  	
					 , "CodigoAgrupador"							NVARCHAR(100)  	
					 , "Producto"									NVARCHAR(100)  	
					 , "IdEmpresa"									BIGINT			
					 , "SocCliente"									BIGINT			
					 , "SocEmpleador"								BIGINT																
					)
)					
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
   registros       		integer := 1;
   v_IdRelacionado 		BIGINT:=0;
   v_CountRelacionado 	BIGINT := 0;
BEGIN	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
	BEGIN					
		DROP TABLE #ContratosOrdenados;
		DROP TABLE #Contratos; 
	END;
	
	CREATE LOCAL TEMPORARY TABLE #Contratos(
		"IdRelacionado"		 BIGINT,
		"IdContratoSAAPV" 	 BIGINT,
		"FolioContratoSAAPV" NVARCHAR(50),
		"Folio" 			 NVARCHAR(50),
		"Estado"	 		 NVARCHAR(3)
	);
	CREATE LOCAL TEMPORARY TABLE #ContratosOrdenados(
	   "IdRelacionado"								BIGINT
	 , "IdContratoSAAPV"							BIGINT  		
	 , "FolioContratoSAAPV"							NVARCHAR(50)	
	 , "IdPlan"										BIGINT  		
	 , "NombrePlan"									NVARCHAR(100)	
	 , "FechaSuscripcion"							DATE			
	 , "FechaCreacion"								DATE			
	 , "FechaAprobacion"							DATE			
	 , "FechaNotificacion"							DATE			
	 , "FechaRecepcion"								DATE			
	 , "Folio"										NVARCHAR(50)	
	 , "IdEnrolamiento"								BIGINT			
	 , "IdPortafolio"								BIGINT			
	 , "CodigoPortafolio"							NVARCHAR(255)	
	 , "IdPersona"									BIGINT			
	 , "RutCliente"									NVARCHAR(40)	
	 , "DocumentoIdentidadCliente"					NVARCHAR(40)	
	 , "NombreCLiente"		 						NVARCHAR(100)	
	 , "ApellidoPaternoCliente"						NVARCHAR(100)	
	 , "ApellidoMaternoCliente"						NVARCHAR(100)	
	 , "NombrePersona"								NVARCHAR(100)	
	 , "IdEmpleadoEjecutivo"						BIGINT			
	 , "NombrePersonaEjecutivo"						NVARCHAR(100)	
	 , "Tipo"										NVARCHAR(1)		
	 , "IdEmpleador"								BIGINT			
	 , "RutEmpleador"								NVARCHAR(40)	
	 , "DocumentoIdentidadEmpleador"				NVARCHAR(40)		
	 , "NombreEmpleador"							NVARCHAR(100)	
	 , "IdCliente"									BIGINT			
	 , "FechaNacimientoCliente"						DATE			
	 , "EstadoCivil"								NVARCHAR(2)			
	 , "Genero"										NVARCHAR(1)		
	 , "IdPaisResidenciaCliente"					BIGINT			
	 , "IdCiudadCliente"							BIGINT			
	 , "NombreCiudad"								NVARCHAR(255)	
	 , "IdComunaCliente"							BIGINT			
	 , "NombreComuna"								NVARCHAR(255)	
	 , "DireccionCliente"							NVARCHAR(100)	
	 , "NumeroDirecionCliente"						NVARCHAR(10)	
	 , "IdTipoCargoPersona"							BIGINT			
	 , "Cargo"										NVARCHAR(100)	
	 , "IdCategoriaInversionista"					NVARCHAR(40)	
	 , "IdPerfilComercialBaseOrigen"				NVARCHAR(100)	
	 , "TelefonoFijo"								NVARCHAR(100)	
	 , "TelefonoMovil"								NVARCHAR(100)	
	 , "TelefonoLaboral"							NVARCHAR(100)	
	 , "EmailCliente"								NVARCHAR(100)	
	 , "DireccionComercialEmpleador"				NVARCHAR(100)		
	 , "NumeroDirecionEmpleador"					NVARCHAR(100)	
	 , "IdComunaEmpleador"							BIGINT			
	 , "NombreCiudadEmpleador"						NVARCHAR(100)	
	 , "TelefonoEmpleador"							NVARCHAR(100)	
	 , "NombreEncargado"							NVARCHAR(100)	
	 , "EmailEncargado"								NVARCHAR(100)	
	 , "TelefonoEncargado"							NVARCHAR(100)	
	 , "IdMedioRecaudacionTrabajador"				BIGINT			
	 , "IdEntidadRecaudadora"						BIGINT			
	 , "CodigoPrevision"							BIGINT				
	 , "Pensionado"									NVARCHAR(2)		
	 , "Trabajador"									NVARCHAR(20)	
	 , "RegimenTributario"							NVARCHAR(20)	
	 , "OrigenAhorro"								NVARCHAR(10)	
     , "MontoPesos"									DECIMAL(26,8)	
	 , "MontoUF"									DECIMAL(26,8)	
	 , "PorcentajeRegimenImponible"					DECIMAL(26,8)	
	 , "Pago"										NVARCHAR(100)	
	 , "PeriodoInicio"								NVARCHAR(7)		
	 , "PeriodoTermino"								NVARCHAR(7)		
	 , "FormaDePago"								NVARCHAR(100)	
	 , "IdMedioRecaudacionFormaPago"				BIGINT			
	 , "CodigoMedioRecaudacion"						NVARCHAR(100)	
	 , "NombreMedioRecaudacion"						NVARCHAR(100)	
	 , "DiaDescuento"								BIGINT			
	 , "FormaDePagoIndirecta"						NVARCHAR(100)	
	 , "CodigoInstitucion"							NVARCHAR(100)	
	 , "Institucion"								NVARCHAR(100)	
	 , "ComisionTransferencia"						DECIMAL(26,8)
	 , "CodigoTipoSolicitud"						NVARCHAR(3)
	 , "NombreEntidadOrigen"						NVARCHAR(100)	
	 , "RutEntidadOrigen"							NVARCHAR(40)	
	 , "TipoDeAhorro"								NVARCHAR(10)  	
	 , "FondoDeOrigen"								NVARCHAR(10)  	
	 , "TipoTraspaso"								NVARCHAR(20)  	
	 , "MontoTraspaso"								DECIMAL(26,8)	
	 , "UFTraspaso"									DECIMAL(26,8)	
	 , "PorcentajeTraspaso"							DECIMAL(26,8)	
	 , "IdEstadoEnvioNotificacion"					NVARCHAR(3)  		
	 , "IdEstadoSolicitud"							NVARCHAR(3)  	
	 , "IdEstado"									NVARCHAR(3)  	
	 , "IdEstadoEventoAutorizacion"					NVARCHAR(3)  	
	 , "EstadoNotificacion"							NVARCHAR(100)	
	 , "EstadoSolicitud" 							NVARCHAR(100)	
	 , "Estado"										NVARCHAR(100)	
	 , "EstadoEventoAutorizacion"					NVARCHAR(100)	
	 , "EstadoNuevo"								NVARCHAR(100)	
	 , "IdEstadoNuevo"								NVARCHAR(3)
	 , "IdCargaMasiva"								BIGINT				
	 , "IdEstadoPortafolio"							NVARCHAR(3)  	
	 , "IdMatrizAgrupadorAPV"						BIGINT			
	 , "Proposito"									NVARCHAR(100)  	
	 , "IdProductoAgrupador"						NVARCHAR(100)  	
	 , "CodigoAgrupador"							NVARCHAR(100)  	
	 , "Producto"									NVARCHAR(100)  	
	 , "IdEmpresa"									BIGINT			
	 , "SocCliente"									BIGINT			
	 , "SocEmpleador"								BIGINT																					
	);
	
	INSERT INTO #Contratos
	SELECT DISTINCT row_number() over (order by A."FolioContratoSAAPV") AS "IdRelacionado"
	     , A."IdContratoSAAPV" 
	     , A."FolioContratoSAAPV" 
	     , CASE WHEN A."Folio" = '0' THEN
	     		''
	       ELSE
	       		A."Folio"
	       END AS "Folio"
	     , 'N'
	FROM "BSCL.APV.BD::SCS.ContratoSAAPV" A
	WHERE  (A."Folio" = '' OR A."Folio" = '0')
	ORDER BY "FolioContratoSAAPV" ASC;

	SELECT count(*) INTO registros 
	FROM #Contratos
	WHERE "Estado" = 'N';
	
	WHILE :registros > 0 DO

		UPDATE #Contratos 
		SET "Estado" = 'S'
		WHERE "Estado" = 'N';
		
		INSERT INTO #Contratos
		SELECT DISTINCT A."IdRelacionado"
			 , B."IdContratoSAAPV"
			 , B."FolioContratoSAAPV"
			 , B."Folio"
			 , 'N'
		FROM #Contratos A
		LEFT OUTER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" B
					 ON B."Folio" = A."FolioContratoSAAPV"			 
		WHERE A."Estado" = 'S'
		AND B."FolioContratoSAAPV" != ''
		AND B."Folio" != ''
		ORDER BY B."FolioContratoSAAPV" ASC;
		
     	UPDATE #Contratos 
		SET "Estado" = 'X'
		WHERE "Estado" = 'S';
     	
      	SELECT count(*) INTO registros 
      	FROM #Contratos  WHERE "Estado" = 'N';

	END WHILE;
	
	
	INSERT INTO #ContratosOrdenados
	SELECT ContratoOrdenado."IdRelacionado"
		 , Contrato."IdContratoSAAPV"							
		 , Contrato."FolioContratoSAAPV"							
		 , "IdPlan"										
		 , "NombrePlan"									
		 , "FechaSuscripcion"							
		 , "FechaCreacion"								
		 , "FechaAprobacion"							
		 , "FechaNotificacion"							
		 , "FechaRecepcion"								
		 , Contrato."Folio"										
		 , "IdEnrolamiento"								
		 , "IdPortafolio"								
		 , "CodigoPortafolio"							
		 , "IdPersona"									
		 , "RutCliente"									
		 , "DocumentoIdentidadCliente"					
		 , "NombreCLiente"		 						
		 , "ApellidoPaternoCliente"						
		 , "ApellidoMaternoCliente"						
		 , "NombrePersona"								
		 , "IdEmpleadoEjecutivo"						
		 , "NombrePersonaEjecutivo"						
		 , "Tipo"										
		 , "IdEmpleador"								
		 , "RutEmpleador"								
		 , "DocumentoIdentidadEmpleador"				
		 , "NombreEmpleador"							
		 , "IdCliente"									
		 , "FechaNacimientoCliente"						
		 , "EstadoCivil"								
		 , "Genero"										
		 , "IdPaisResidenciaCliente"					
		 , "IdCiudadCliente"							
		 , "NombreCiudad"								
		 , "IdComunaCliente"							
		 , "NombreComuna"								
   		 , "DireccionCliente"							
   		 , "NumeroDirecionCliente"						
		 , "IdTipoCargoPersona"							
		 , "Cargo"										
		 , "IdCategoriaInversionista"					
		 , "IdPerfilComercialBaseOrigen"				
		 , "TelefonoFijo"								
		 , "TelefonoMovil"								
		 , "TelefonoLaboral"							
		 , "EmailCliente"								
		 , "DireccionComercialEmpleador"				
		 , "NumeroDirecionEmpleador"					
		 , "IdComunaEmpleador"							
		 , "NombreCiudadEmpleador"						
		 , "TelefonoEmpleador"							
		 , "NombreEncargado"							
		 , "EmailEncargado"								
		 , "TelefonoEncargado"							
		 , "IdMedioRecaudacionTrabajador"				
		 , "IdEntidadRecaudadora"						
		 , "CodigoPrevision"							
		 , "Pensionado"									
		 , "Trabajador"									
		 , "RegimenTributario"							
		 , "OrigenAhorro"								
	     , "MontoPesos"									
		 , "MontoUF"									
		 , "PorcentajeRegimenImponible"					
		 , "Pago"										
		 , "PeriodoInicio"								
		 , "PeriodoTermino"								
		 , "FormaDePago"								
		 , "IdMedioRecaudacionFormaPago"				
		 , "CodigoMedioRecaudacion"						
		 , "NombreMedioRecaudacion"						
		 , "DiaDescuento"								
		 , "FormaDePagoIndirecta"						
		 , "CodigoInstitucion"							
		 , "Institucion"								
		 , "ComisionTransferencia"
		 , "CodigoTipoSolicitud"
		 , "NombreEntidadOrigen"						
		 , "RutEntidadOrigen"							
		 , "TipoDeAhorro"								
		 , "FondoDeOrigen"								
		 , "TipoTraspaso"								
		 , "MontoTraspaso"								
		 , "UFTraspaso"									
		 , "PorcentajeTraspaso"							
		 , "IdEstadoEnvioNotificacion"					
		 , "IdEstadoSolicitud"							
		 , "IdEstado"									
		 , "IdEstadoEventoAutorizacion"					
		 , "EstadoNotificacion"							
		 , "EstadoSolicitud" 							
		 , Contrato."Estado"										
		 , "EstadoEventoAutorizacion"					
		 , "EstadoNuevo"
		 , "IdEstadoNuevo"
		 , "IdCargaMasiva"								
		 , "IdEstadoPortafolio"							
		 , "IdMatrizAgrupadorAPV"						
		 , "Proposito"									
		 , "IdProductoAgrupador"						
		 , "CodigoAgrupador"							
		 , "Producto"									
		 , "IdEmpresa"									
		 , "SocCliente"									
		 , "SocEmpleador"							
	  FROM "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" AS Contrato
	  INNER JOIN #Contratos AS ContratoOrdenado
	  		  ON ContratoOrdenado."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
	  UNION ALL
	  SELECT 0 AS "IdRelacionado"
		 , "IdContratoSAAPV"							
		 , "FolioContratoSAAPV"							
		 , "IdPlan"										
		 , "NombrePlan"									
		 , "FechaSuscripcion"							
		 , "FechaCreacion"								
		 , "FechaAprobacion"							
		 , "FechaNotificacion"							
		 , "FechaRecepcion"								
		 , "Folio"										
		 , "IdEnrolamiento"								
		 , "IdPortafolio"								
		 , "CodigoPortafolio"							
		 , "IdPersona"									
		 , "RutCliente"									
		 , "DocumentoIdentidadCliente"					
		 , "NombreCLiente"		 						
		 , "ApellidoPaternoCliente"						
		 , "ApellidoMaternoCliente"						
		 , "NombrePersona"								
		 , "IdEmpleadoEjecutivo"						
		 , "NombrePersonaEjecutivo"						
		 , "Tipo"										
		 , "IdEmpleador"								
		 , "RutEmpleador"								
		 , "DocumentoIdentidadEmpleador"				
		 , "NombreEmpleador"							
		 , "IdCliente"									
		 , "FechaNacimientoCliente"						
		 , "EstadoCivil"								
		 , "Genero"										
		 , "IdPaisResidenciaCliente"					
		 , "IdCiudadCliente"							
		 , "NombreCiudad"								
		 , "IdComunaCliente"							
		 , "NombreComuna"								
   		 , "DireccionCliente"							
   		 , "NumeroDirecionCliente"						
		 , "IdTipoCargoPersona"							
		 , "Cargo"										
		 , "IdCategoriaInversionista"					
		 , "IdPerfilComercialBaseOrigen"				
		 , "TelefonoFijo"								
		 , "TelefonoMovil"								
		 , "TelefonoLaboral"							
		 , "EmailCliente"								
		 , "DireccionComercialEmpleador"				
		 , "NumeroDirecionEmpleador"					
		 , "IdComunaEmpleador"							
		 , "NombreCiudadEmpleador"						
		 , "TelefonoEmpleador"							
		 , "NombreEncargado"							
		 , "EmailEncargado"								
		 , "TelefonoEncargado"							
		 , "IdMedioRecaudacionTrabajador"				
		 , "IdEntidadRecaudadora"						
		 , "CodigoPrevision"							
		 , "Pensionado"									
		 , "Trabajador"									
		 , "RegimenTributario"							
		 , "OrigenAhorro"								
	     , "MontoPesos"									
		 , "MontoUF"									
		 , "PorcentajeRegimenImponible"					
		 , "Pago"										
		 , "PeriodoInicio"								
		 , "PeriodoTermino"								
		 , "FormaDePago"								
		 , "IdMedioRecaudacionFormaPago"				
		 , "CodigoMedioRecaudacion"						
		 , "NombreMedioRecaudacion"						
		 , "DiaDescuento"								
		 , "FormaDePagoIndirecta"						
		 , "CodigoInstitucion"							
		 , "Institucion"								
		 , "ComisionTransferencia"	
		 , "CodigoTipoSolicitud"
		 , "NombreEntidadOrigen"						
		 , "RutEntidadOrigen"							
		 , "TipoDeAhorro"								
		 , "FondoDeOrigen"								
		 , "TipoTraspaso"								
		 , "MontoTraspaso"								
		 , "UFTraspaso"									
		 , "PorcentajeTraspaso"							
		 , "IdEstadoEnvioNotificacion"					
		 , "IdEstadoSolicitud"							
		 , "IdEstado"									
		 , "IdEstadoEventoAutorizacion"					
		 , "EstadoNotificacion"							
		 , "EstadoSolicitud" 							
		 , "Estado"										
		 , "EstadoEventoAutorizacion"					
		 , "EstadoNuevo"
		 , "IdEstadoNuevo"
		 , "IdCargaMasiva"								
		 , "IdEstadoPortafolio"							
		 , "IdMatrizAgrupadorAPV"						
		 , "Proposito"									
		 , "IdProductoAgrupador"						
		 , "CodigoAgrupador"							
		 , "Producto"									
		 , "IdEmpresa"									
		 , "SocCliente"									
		 , "SocEmpleador"							
	  FROM "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV"
	  WHERE "FolioContratoSAAPV" NOT IN (SELECT "FolioContratoSAAPV" 
									   	 FROM #Contratos)
	  ORDER BY "IdRelacionado", "IdContratoSAAPV";

	IF(p_FolioContratoSAAPV IS NULL)THEN
		v_IdRelacionado = -2;
	ELSE
		SELECT COUNT("IdRelacionado") INTO v_CountRelacionado 
		FROM #ContratosOrdenados  
		WHERE "FolioContratoSAAPV" = :p_FolioContratoSAAPV;
		
		IF(v_CountRelacionado > 0)THEN
			SELECT TOP 1 "IdRelacionado" INTO v_IdRelacionado 
			FROM #ContratosOrdenados  
			WHERE "FolioContratoSAAPV" = :p_FolioContratoSAAPV;
		ELSE
			v_IdRelacionado = -1;
		END IF;
		
	END IF;

	P_CONTRATOS = SELECT "IdRelacionado" 
	  	 , "IdContratoSAAPV"							
		 , "FolioContratoSAAPV"							
		 , "IdPlan"										
		 , "NombrePlan"									
		 , "FechaSuscripcion"							
		 , "FechaCreacion"								
		 , "FechaAprobacion"							
		 , "FechaNotificacion"							
		 , "FechaRecepcion"								
		 , "Folio"										
		 , "IdEnrolamiento"								
		 , "IdPortafolio"								
		 , "CodigoPortafolio"							
		 , "IdPersona"									
		 , "RutCliente"									
		 , "DocumentoIdentidadCliente"					
		 , "NombreCLiente"		 						
		 , "ApellidoPaternoCliente"						
		 , "ApellidoMaternoCliente"						
		 , "NombrePersona"								
		 , "IdEmpleadoEjecutivo"						
		 , "NombrePersonaEjecutivo"						
		 , "Tipo"										
		 , "IdEmpleador"								
		 , "RutEmpleador"								
		 , "DocumentoIdentidadEmpleador"				
		 , "NombreEmpleador"							
		 , "IdCliente"									
		 , "FechaNacimientoCliente"						
		 , "EstadoCivil"								
		 , "Genero"										
		 , "IdPaisResidenciaCliente"					
		 , "IdCiudadCliente"							
		 , "NombreCiudad"								
		 , "IdComunaCliente"							
		 , "NombreComuna"								
   		 , "DireccionCliente"							
   		 , "NumeroDirecionCliente"						
		 , "IdTipoCargoPersona"							
		 , "Cargo"										
		 , "IdCategoriaInversionista"					
		 , "IdPerfilComercialBaseOrigen"				
		 , "TelefonoFijo"								
		 , "TelefonoMovil"								
		 , "TelefonoLaboral"							
		 , "EmailCliente"								
		 , "DireccionComercialEmpleador"				
		 , "NumeroDirecionEmpleador"					
		 , "IdComunaEmpleador"							
		 , "NombreCiudadEmpleador"						
		 , "TelefonoEmpleador"							
		 , "NombreEncargado"							
		 , "EmailEncargado"								
		 , "TelefonoEncargado"							
		 , "IdMedioRecaudacionTrabajador"				
		 , "IdEntidadRecaudadora"						
		 , "CodigoPrevision"							
		 , "Pensionado"									
		 , "Trabajador"									
		 , "RegimenTributario"							
		 , "OrigenAhorro"								
	     , "MontoPesos"									
		 , "MontoUF"									
		 , "PorcentajeRegimenImponible"					
		 , "Pago"										
		 , "PeriodoInicio"								
		 , "PeriodoTermino"								
		 , "FormaDePago"								
		 , "IdMedioRecaudacionFormaPago"				
		 , "CodigoMedioRecaudacion"						
		 , "NombreMedioRecaudacion"						
		 , "DiaDescuento"								
		 , "FormaDePagoIndirecta"						
		 , "CodigoInstitucion"							
		 , "Institucion"								
		 , "ComisionTransferencia"	
		 , "CodigoTipoSolicitud"
		 , "NombreEntidadOrigen"						
		 , "RutEntidadOrigen"							
		 , "TipoDeAhorro"								
		 , "FondoDeOrigen"								
		 , "TipoTraspaso"								
		 , "MontoTraspaso"								
		 , "UFTraspaso"									
		 , "PorcentajeTraspaso"							
		 , "IdEstadoEnvioNotificacion"					
		 , "IdEstadoSolicitud"							
		 , "IdEstado"									
		 , "IdEstadoEventoAutorizacion"					
		 , "EstadoNotificacion"							
		 , "EstadoSolicitud" 							
		 , "Estado"										
		 , "EstadoEventoAutorizacion"					
		 , "EstadoNuevo"
		 , "IdEstadoNuevo"
		 , "IdCargaMasiva"								
		 , "IdEstadoPortafolio"							
		 , "IdMatrizAgrupadorAPV"						
		 , "Proposito"									
		 , "IdProductoAgrupador"						
		 , "CodigoAgrupador"							
		 , "Producto"									
		 , "IdEmpresa"									
		 , "SocCliente"									
		 , "SocEmpleador"
	  FROM #ContratosOrdenados
	  WHERE "IdRelacionado" = CASE WHEN :v_IdRelacionado = -2 THEN "IdRelacionado" ELSE :v_IdRelacionado END
	  ORDER BY "IdContratoSAAPV";

	SIGNAL SALIDA_CONTROLADA;
END;