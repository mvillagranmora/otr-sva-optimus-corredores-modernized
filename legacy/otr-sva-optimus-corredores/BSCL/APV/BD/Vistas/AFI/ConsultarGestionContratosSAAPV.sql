SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV";
 
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV"  AS 
	SELECT  
		   Contrato."IdContratoSAAPV"
         , Contrato."FolioContratoSAAPV"
		 , Contrato."IdPlan"
		 , PlanTipoInversion."NombrePlanTipoInversion" AS "NombrePlan"
		 , Contrato."FechaSuscripcion"
		 , Contrato."FechaCreacion"
	     , CASE WHEN EventoAutorizacion."IdEstado" = 'AUT' THEN EventoAutorizacion."FechaHoraCambioEstado" end AS "FechaAprobacion" 
		 , Contrato."FechaNotificacion"
		 , Contrato."FechaRecepcion"
		 , Contrato."Folio"
		 , Contrato."IdEnrolamiento"
		 , PortafolioContrato."IdPortafolio"
		 , Portafolio."CodigoPortafolio"
		 , Contrato."IdPersona"	
		 , Contrato."DocumentoIdentidadCliente"																									AS "RutCliente"
		 , Contrato."DocumentoIdentidadCliente"																									AS "DocumentoIdentidadCliente"
		 , Persona."Nombres"																													AS "NombreCLiente"		 
		 , Persona."ApellidoPaterno"																											AS "ApellidoPaternoCliente"
		 , Persona."ApellidoMaterno"																											AS "ApellidoMaternoCliente"
		 , Persona."Nombres" || ' ' || Persona."ApellidoPaterno" || ' ' || Persona."ApellidoMaterno" 											AS "NombrePersona"
		 , Empleado."IdEmpleado"																												AS "IdEmpleadoEjecutivo"
		 , PersonaEjecutivo."Nombres" || ' ' || PersonaEjecutivo."ApellidoPaterno" || ' ' || PersonaEjecutivo."ApellidoMaterno" 				AS "NombrePersonaEjecutivo"
		 , Persona."Tipo"
		 , Contrato."IdEmpleador"
		 , Contrato."DocumentoIdentidadEmpleador"																								AS "RutEmpleador"
		 , Contrato."DocumentoIdentidadEmpleador"					
		 , PersonaJuridica."RazonSocial"																										AS "NombreEmpleador"
		 , Inversionista."IdCliente"
		 , Persona."FechaNacimiento"																											AS "FechaNacimientoCliente"
		 , PaisNacimiento."NombrePais"																											AS "Nacionalidad"
		 , Persona."EstadoCivil"																												AS "EstadoCivil"	
		 , Persona."Sexo" 																														AS "Genero"	
		 , Persona."IdPaisResidencia"																											AS "IdPaisResidenciaCliente"
		 , DireccionFisica."IdCiudad"                                                                                                           AS "IdCiudadCliente"
		 , Ciudad."NombreCiudad"
		 , DireccionFisica."IdDivisionPais" 															                                        AS "IdComunaCliente"
		 , Comuna."NombreDivisionPais"																											AS "NombreComuna"
   		 , DireccionFisica."Calle"     																									        AS "DireccionCliente"
   		 , DireccionFisica."Numero"   																									        AS "NumeroDirecionCliente"
		 , Profesion."IdProfesion"																												AS "IdTipoCargoPersona"
		 , Profesion."Nombre"																													AS "Cargo"
		 , COALESCE(Inversionista."IdCategoriaInversionista",Contrato."IdCategoriaInversionista")												AS "IdCategoriaInversionista"
		 , COALESCE(Portafolio."IdPerfilComercialBaseOrigen",Contrato."IdPerfilComercial")														AS "IdPerfilComercialBaseOrigen"
		 , TelefonoFijo."Numero" AS "TelefonoFijo"
		 , TelefonoMovil."Numero" AS "TelefonoMovil"
		 , TelefonoOtro."Numero" AS "TelefonoLaboral"
		 , Contrato."EmailCliente"
		 , DireccionFisicaEmpleador."Calle"																										AS "DireccionComercialEmpleador"
		 , DireccionFisicaEmpleador."Numero"																									AS "NumeroDirecionEmpleador"
		 , Contrato."IdComunaEmpleador"																											AS "IdComunaEmpleador"
		 , CiudadEmpleador."NombreCiudad"																										AS "NombreCiudadEmpleador"
		 , Contrato."TelefonoEmpleador"
		 , NombreEncargado."ValorIndicador"																										AS "NombreEncargado"
		 , EmailEncargado."ValorIndicador"																										AS "EmailEncargado"
		 , TelefonoEncargado."ValorIndicador"																									AS "TelefonoEncargado"
		 , Contrato."IdMedioRecaudacionTrabajador"
		 , Contrato."IdEntidadRecaudadora"
		 , Contrato."CodigoPrevision"
		 , Contrato."Pensionado"
		 , Contrato."Trabajador"
		 , MatrizAgrupador."Codigo"																												AS "RegimenTributario"
		 , PortafolioContrato."OrigenAhorro"
	     , COALESCE(PortafolioContrato."MontoPesos",0)																							AS "MontoPesos"
		 , COALESCE(PortafolioContrato."MontoUF"	,0)																							AS "MontoUF"
		 , COALESCE(PortafolioContrato."PorcentajeRegimenImponible",0)																			AS "PorcentajeRegimenImponible"
		 , PortafolioContrato."Pago"
		 , PortafolioContrato."PeriodoInicio"
		 , PortafolioContrato."PeriodoTermino"
		 , Contrato."FormaDePago"
		 , MAP(Contrato."FormaDePago",'DIRECTA',Contrato."IdMedioRecaudacionFormaPago",NULL)													AS "IdMedioRecaudacionFormaPago"
		 , MAP(Contrato."FormaDePago",'DIRECTA',MedioRecaudacion."CodigoMedioRecaudacion",NULL)													AS "CodigoMedioRecaudacion"
		 , MAP(Contrato."FormaDePago",'DIRECTA',MedioRecaudacion."NombreMedioRecaudacion",NULL)													AS "NombreMedioRecaudacion"
		 , MAP(Contrato."FormaDePago",'DIRECTA',MAP(MedioRecaudacion."NombreMedioRecaudacion",'PAC',Contrato."DiaDescuento",NULL),NULL)			AS "DiaDescuento"
		 , MAP(Contrato."FormaDePago",'INDIRECTA',MAP(Contrato."IdMedioRecaudacionTrabajador",1,'IPS-EX INP','AFP'),NULL)						AS "FormaDePagoIndirecta"
		 , MAP(Contrato."FormaDePago",'INDIRECTA',Contrato."Institucion",NULL)																	AS "CodigoInstitucion"
		 , MAP(Contrato."FormaDePago",'INDIRECTA',EntidadRecaudadora."Nombre",NULL)																AS "Institucion"
		 , MAP(Contrato."FormaDePago",'INDIRECTA',Contrato."ComisionTransferencia",NULL)														AS "ComisionTransferencia"
		 , Contrato."CodigoTipoSolicitud"
		 , Contrato."NombreEntidadOrigen"
		 , Contrato."RutEntidadOrigen"
		 , Contrato."TipoDeAhorro"
		 , Contrato."FondoDeOrigen"
		 , Contrato."TipoTraspaso"
		 , Contrato."MontoTraspaso"
		 , Contrato."UFTraspaso"
		 , Contrato."PorcentajeTraspaso"
		 , Contrato."IdEstadoEnvioNotificacion"
		 , Contrato."IdEstadoSolicitud"
		 , Contrato."IdEstado"
		 , EventoAutorizacion."IdEstado"																										AS "IdEstadoEventoAutorizacion"
		 , MAP(EventoAutorizacion."IdEstado",'VCD','PDA','VIG','PDA','RZD','RZD',Contrato."IdEstado")											AS "IdEstadoNuevo"
		 , EstadoNotificacion."Nombre"																											AS "EstadoNotificacion"
		 , EstadoSolicitud."Nombre" 																											AS "EstadoSolicitud" 
		 , Estado."Nombre"																														AS "Estado"
		 , MAP(EventoAutorizacion."IdEstado",'VCD','VENCIDO','VIG','VIGENTE','RZD','RECHAZADO','AUT','AUTORIZADO',NULL)							AS "EstadoEventoAutorizacion"
		 , MAP(EventoAutorizacion."IdEstado",'VCD','PENDIENTE DE APROBACION','VIG','PENDIENTE DE APROBACION','RZD','RECHAZADO',Estado."Nombre")	AS "EstadoNuevo"
		 , Contrato."IdCargaMasiva"
		 , Portafolio."IdEstado"																												AS "IdEstadoPortafolio"
		 , Portafolio."IdMatrizAgrupadorAPV"
		 , Portafolio."Proposito"
		 , MatrizAgrupador."IdProductoAgrupador"
		 , MatrizAgrupador."CodigoAgrupador"
		 , MatrizAgrupador."Producto"
		 , Contrato."IdEmpresa"
		 , SocCliente."IdSocioDeNegocio"																										AS "SocCliente"
		 , SocEmpleador."IdSocioDeNegocio"																									    AS "SocEmpleador"
		 , ES."Nombre" 																											    			AS "EstadoPAC"
		 , Contrato."Canal"																														AS "Canal"
		 , Contrato."DescripcionGlosa"																											AS "Observacion"
		 , Glosa."Descripcion"																													AS "Motivo"
	FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
	LEFT JOIN "BSCL.APV.BD::APV.Glosa" AS Glosa
				ON Glosa."IdGlosa" = Contrato."IdGlosa"
	LEFT OUTER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
				 ON PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
	LEFT JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS MPAC 
				ON cast(MPAC."FolioSAAPV" as varchar(200)) =  Contrato."FolioContratoSAAPV"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ES 
				ON (ES."IdEstado" = MPAC."Estado")
    LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
    			 ON Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
    			AND Portafolio."IdCliente" = Contrato."IdCliente"
    			AND Portafolio."Proposito" = 'PROPOSITOAPV'
	LEFT OUTER JOIN (
					SELECT MAT."IdMatrizAgrupadorAPV"
						 , MAT."IdRegimenTributario"
						 , PlanTipoInversion."IdPlanTipoInversion"
						 , PlanTipoInversion."NombrePlanTipoInversion" AS "NombrePlan"
						 , MAT."IdPropositoPortafolio"
						 , ProductoAgrupador."IdProductoAgrupador"
						 , ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
						 , MAT."NombreAgrupador"
						 , MAT."CodigoAgrupador"
						 , REG."Codigo"
						 , MAT."IdProductoAgrupador" || ' / ' || REG."Codigo"  AS "NombreAgrupadorCodigo"
						 , MAT."IdEstado"
						 , MAT."EsADC"
					FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
					INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
							ON REG."IdRegimenTributario" = MAT."IdRegimenTributario"
						   --AND REG."IdEstado" = 'VIG'
					INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
							ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
						   AND ProductoAgrupador."IdEstado" = 'VIG'
					INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion
							ON PlanTipoInversion."IdPlanTipoInversion" = MAT."IdPlanTipoInversion"
						   AND PlanTipoInversion."IdEstado" = 'VIG'
					WHERE MAT."IdEstado" = 'VIG'	
					ORDER BY MAT."IdMatrizAgrupadorAPV"
	) AS MatrizAgrupador
    			 ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"	
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
				 ON Persona."IdPersona" = Contrato."IdPersona"
				AND Persona."Tipo" = 'N'
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PaisNacimiento
				 ON PaisNacimiento."IdPais" = Persona."IdPaisNacionalidadPrincipal"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PersonaJuridica
				 ON PersonaJuridica."IdPersona" = Contrato."IdEmpleador"
		   		AND PersonaJuridica."Tipo" = 'J'
    LEFT OUTER JOIN (
					SELECT MAX(COALESCE(DirPrev."Id", DirPart."Id", DirLabo."Id", DirCome."Id")) AS "IdDireccionFisica",
 		                   COALESCE(DirPrev."IdPersona", DirPart."IdPersona", DirLabo."IdPersona", DirCome."IdPersona") AS "IdPersona"
					      FROM 
						   (SELECT MAX("IdDireccionFisica") AS "Id" ,"IdPersona"
					        FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
							WHERE "CodigoProposito" = 'PREV' 
							GROUP BY "IdPersona"
						  ) AS DirPrev
					  
					       FULL JOIN 
					       
						   (SELECT MAX("IdDireccionFisica") AS "Id" ,"IdPersona"
					        FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
							WHERE "CodigoProposito" = 'PART' 
							GROUP BY "IdPersona"
						  )AS  DirPart
						   ON DirPrev."IdPersona" = DirPart."IdPersona"
					   
					       FULL JOIN 
					       
						   (SELECT MAX("IdDireccionFisica") AS "Id" ,"IdPersona"
					        FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
							WHERE "CodigoProposito" = 'LABO' 
							GROUP BY "IdPersona"
						  )AS  DirLabo
						   ON DirPart."IdPersona" = DirLabo."IdPersona"
					   
					       FULL JOIN 
					       
						   (SELECT MAX("IdDireccionFisica") AS "Id" ,"IdPersona"
					        FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
							WHERE "CodigoProposito" = 'COME' 
							GROUP BY "IdPersona"
						  )AS  DirCome
						   ON DirLabo."IdPersona" = DirCome."IdPersona"
				GROUP BY COALESCE(DirPrev."IdPersona", DirPart."IdPersona", DirLabo."IdPersona", DirCome."IdPersona")
   				) AS DireccionFisicaGROUP
    			ON DireccionFisicaGROUP."IdPersona" = Contrato."IdPersona"
    		
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisica
				ON DireccionFisica."IdDireccionFisica" = DireccionFisicaGROUP."IdDireccionFisica"	    		
    LEFT OUTER JOIN (SELECT * FROM (
					SELECT MAX("IdDireccionFisica") AS "IdDireccionFisicaPRED", "IdPersona" AS "IdPersonaPRED"
						FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" 
						WHERE "IdEstado" = 'ACT' AND "Predeterminado" = 'S'
					    GROUP BY "IdPersona"
				    )AS DF_PRED
			    	FULL JOIN 
					(
						SELECT MAX("IdDireccionFisica") AS "IdDireccionFisicaOTRO", "IdPersona" AS "IdPersonaOTRO"
						FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
						WHERE "IdEstado" = 'ACT' AND "Predeterminado" != 'S'
					    GROUP BY "IdPersona"
				    )AS DF_OTRO
			    	ON DF_PRED."IdPersonaPRED" = DF_OTRO."IdPersonaOTRO") AS DFEmpleador
			    ON COALESCE(DFEmpleador."IdPersonaPRED",DFEmpleador."IdPersonaOTRO") = Contrato."IdEmpleador"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DireccionFisicaEmpleador
    			 ON COALESCE(DFEmpleador."IdDireccionFisicaPRED",DFEmpleador."IdDireccionFisicaOTRO") = DireccionFisicaEmpleador."IdDireccionFisica"
    LEFT OUTER JOIN (
					SELECT  MAX(TelFijo."Id") AS "IdTelFijo" ,
					        MAX(TelMovil."Id") AS "IdTelMovil",
					        MAX(TelOtro."Id") AS "IdTelOtro",
						   COALESCE(TelFijo."IdPersona",TelMovil."IdPersona",TelOtro."IdPersona") AS "IdPersona" 
					      FROM 
						   (SELECT MAX("IdTelefono") AS "Id" ,"IdPersona"
					        FROM "BSCL.SocioNegocio.BD::PER.Telefono" 
							WHERE "IdTipoTelefono" = 'FIJO' 
							AND "Borrado" = 'N'
							GROUP BY "IdPersona"
						  ) AS TelFijo
					  
					       FULL JOIN 
					       
						   (SELECT MAX("IdTelefono") AS "Id" ,"IdPersona"
					        FROM "BSCL.SocioNegocio.BD::PER.Telefono" 
							WHERE "IdTipoTelefono" = 'MOVIL' 
							AND "Borrado" = 'N'
							GROUP BY "IdPersona"
						  )AS  TelMovil
						   ON TelFijo."IdPersona" = TelMovil."IdPersona"
					   
					       FULL JOIN 
					       
						   (SELECT MAX("IdTelefono") AS "Id" ,"IdPersona"
					        FROM "BSCL.SocioNegocio.BD::PER.Telefono" 
							WHERE "IdTipoTelefono" = 'OTRO' 
							AND "Borrado" = 'N'
							GROUP BY "IdPersona"
						  )AS  TelOtro
						   ON TelMovil."IdPersona" = TelOtro."IdPersona"
				   GROUP BY COALESCE(TelFijo."IdPersona",TelMovil."IdPersona",TelOtro."IdPersona")
    ) TelefonoFiltrado
    		 ON TelefonoFiltrado."IdPersona" = Contrato."IdPersona"
    LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoFijo
			 ON TelefonoFijo."IdTelefono" = TelefonoFiltrado."IdTelFijo"
    LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoMovil
			 ON TelefonoMovil."IdTelefono" = TelefonoFiltrado."IdTelMovil"
    LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoOtro
			 ON TelefonoOtro."IdTelefono" = TelefonoFiltrado."IdTelOtro"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
				 ON DocumentoIdentidad."IdPersona" = Contrato."IdPersona" 
		   		AND DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidadEmpleador
				 ON DocumentoIdentidadEmpleador."IdPersona" = Contrato."IdEmpleador" 
		   		AND DocumentoIdentidadEmpleador."IdDocumentoIdentidad" = PersonaJuridica."IdDocumentoIdentidad"
	LEFT OUTER JOIN "BSCL.APV.BD::SCS.MedioRecaudacion" AS MedioRecaudacion
				 ON MedioRecaudacion."IdMedioRecaudacion" = Contrato."IdMedioRecaudacionFormaPago"
	LEFT OUTER JOIN "BSCL.APV.BD::APV.Estado" AS EstadoSolicitud 
		     	 ON EstadoSolicitud."Codigo" = Contrato."IdEstadoSolicitud"
	LEFT OUTER JOIN "BSCL.APV.BD::APV.Estado" AS EstadoNotificacion 
		         ON EstadoNotificacion."Codigo" = Contrato."IdEstadoEnvioNotificacion"
	LEFT OUTER JOIN "BSCL.APV.BD::APV.Estado" AS Estado 
		         ON Estado."Codigo" = Contrato."IdEstado"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SocCliente
				 ON SocCliente."IdPersona" = Persona."IdPersona"
				AND SocCliente."IdCliente" = Contrato."IdCliente"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SocEmpleador
				 ON SocEmpleador."IdPersona" = PersonaJuridica."IdPersona"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Inversionista
				 ON Inversionista."IdPersona" = Persona."IdPersona"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PersonaEjecutivo
				 ON Inversionista."IdPersonaEjecutivo" = PersonaEjecutivo."IdPersona"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS Empleado
				 ON Empleado."IdPersona" = Inversionista."IdPersonaEjecutivo"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Profesion" AS Profesion
				 ON Profesion."IdProfesion" = Persona."IdProfesion"
	LEFT OUTER JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora
				 ON EntidadRecaudadora."Codigo" = CASE WHEN Contrato."Institucion" = '' THEN 0 ELSE Contrato."Institucion" END 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS Comuna
				 ON Comuna."IdDivisionPais" = DireccionFisica."IdDivisionPais"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad
				 ON Ciudad."IdCiudad" = Contrato."IdCiudadCliente"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CiudadEmpleador
				 ON (CiudadEmpleador."IdCiudad" = DireccionFisicaEmpleador."IdCiudad")
    LEFT OUTER JOIN (			 
				 		SELECT MAX("IdEvento") AS "IdEvento", "IdCliente", "NumeroOperacionConcepto"
				 	    FROM "BSCL.Plataforma.BD::AUT.Evento" 
			            WHERE "IdModulo" = 'APV'		
		                GROUP BY "IdCliente", "NumeroOperacionConcepto"
			 ) AS UltimoEventoAutorizacion
			 ON  UltimoEventoAutorizacion."IdCliente" = Contrato."IdCliente" 
		 	 AND UltimoEventoAutorizacion."NumeroOperacionConcepto" = Contrato."FolioContratoSAAPV"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::AUT.Evento" AS EventoAutorizacion
				ON EventoAutorizacion."IdEvento" = UltimoEventoAutorizacion."IdEvento" 
	INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" PlanTipoInversion
		ON (Contrato."IdPlan" = PlanTipoInversion."IdPlanTipoInversion" AND
			PlanTipoInversion."IdEstado" = 'VIG')
	LEFT OUTER JOIN (SELECT Indicador."IdEntidad" AS "IdPersona"
	                        ,CAST(TipoIndicador."CodigoTipoIndicador" AS NVARCHAR(40))	AS "CodigoTipoIndicador"
	                        ,CAST(IFNULL(Indicador."ValorIndicador",TipoIndicador."ValorIndicadorDefecto") AS NVARCHAR(255)) AS "ValorIndicador"
	                 FROM "BSCL.Plataforma.BD::IND.Indicador" AS Indicador
	                 	INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TipoIndicador
	                 	   ON Indicador."IdTipoEntidad" = TipoIndicador."IdTipoEntidad"
	                 	  AND Indicador."IdTipoIndicador" = TipoIndicador."IdTipoIndicador"
	                 	  AND TipoIndicador."IdTipoEntidad" = 'PERSONA'
	                 	  AND TipoIndicador."GrupoIndicador" = 'DATOS EMPLEADOR'
	                 	  AND TipoIndicador."IdEstado" != 'INA'
	                 ORDER BY Indicador."IdEntidad"
	                ) AS NombreEncargado
	    ON  NombreEncargado."IdPersona" = Contrato."IdEmpleador"
       AND NombreEncargado."CodigoTipoIndicador" = 'NOMBRE_ENCARGADO_RRHH'
       AND NombreEncargado."ValorIndicador" IS NOT NULL
       AND NombreEncargado."ValorIndicador" != ''    
    LEFT OUTER JOIN (SELECT Indicador."IdEntidad" AS "IdPersona"
                           ,CAST(TipoIndicador."CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador"
                           ,CAST(IFNULL(Indicador."ValorIndicador",TipoIndicador."ValorIndicadorDefecto") AS NVARCHAR(255)) AS "ValorIndicador"
                     FROM "BSCL.Plataforma.BD::IND.Indicador" AS Indicador
                       INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TipoIndicador
                          ON Indicador."IdTipoEntidad" = TipoIndicador."IdTipoEntidad"
                         AND Indicador."IdTipoIndicador" = TipoIndicador."IdTipoIndicador"
                         AND TipoIndicador."IdTipoEntidad" = 'PERSONA'
                         AND TipoIndicador."GrupoIndicador" = 'DATOS EMPLEADOR'
                         AND TipoIndicador."IdEstado" != 'INA'
                     ORDER BY Indicador."IdEntidad"
                    ) AS EmailEncargado
       ON  EmailEncargado."IdPersona" = Contrato."IdEmpleador"
      AND EmailEncargado."CodigoTipoIndicador" = 'MAIL_ENCARGADO_RRHH'
      AND EmailEncargado."ValorIndicador" IS NOT NULL
      AND EmailEncargado."ValorIndicador" != ''       
    LEFT OUTER JOIN (SELECT Indicador."IdEntidad"   AS "IdPersona"
                            ,CAST(TipoIndicador."CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador"
                            ,CAST(IFNULL(Indicador."ValorIndicador",TipoIndicador."ValorIndicadorDefecto") AS NVARCHAR(255)) AS "ValorIndicador"
                     FROM "BSCL.Plataforma.BD::IND.Indicador" AS Indicador
                       INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TipoIndicador
                          ON  Indicador."IdTipoEntidad" = TipoIndicador."IdTipoEntidad"
                         AND Indicador."IdTipoIndicador" = TipoIndicador."IdTipoIndicador"
                         AND TipoIndicador."IdTipoEntidad" = 'PERSONA'
	                     AND TipoIndicador."GrupoIndicador" = 'DATOS EMPLEADOR'
	                     AND TipoIndicador."IdEstado" != 'INA'
	                 ORDER BY Indicador."IdEntidad"
                    ) AS TelefonoEncargado
        ON  TelefonoEncargado."IdPersona" = Contrato."IdEmpleador"
       AND TelefonoEncargado."CodigoTipoIndicador" = 'TELEFONO_ENCARGADO_RRHH'
       AND TelefonoEncargado."ValorIndicador" IS NOT NULL
       AND TelefonoEncargado."ValorIndicador" != ''		
    ORDER BY Contrato."IdContratoSAAPV" DESC;
	
GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;