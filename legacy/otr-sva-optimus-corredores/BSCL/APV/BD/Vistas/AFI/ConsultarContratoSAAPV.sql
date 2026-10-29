SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarContratoSAAPV";
GO
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarContratoSAAPV"  AS 

SELECT Contrato."IdContratoSAAPV"																																			AS "IdContratoSAAPV" 
		 --, Contrato."NombrePlan"																																				AS "NombrePlanAhorro"
		 , PlanTipoInversion."NombrePlanTipoInversion" AS "NombrePlanAhorro"
		 , TO_NVARCHAR(Contrato."FechaSuscripcion", 'DD-MM-YYYY')																												AS "FechaSuscripcion"
		 , Contrato."FolioContratoSAAPV"																																		AS "FolioContrato"
		 , Contrato."DocumentoIdentidadCliente"																																	AS "DocumentoIdentidadCliente"
		 , Persona."Nombres"																																					AS "NombreCliente"
		 , Persona."ApellidoPaterno"																																			AS "ApellidoPaternoCliente"
		 , Persona."ApellidoMaterno"																																			AS "ApellidoMaternoCliente"
		 , Inversionista."IdPersonaEjecutivo"																																	AS "IdEmpleadoEjecutivo"
		 , PersonaEjecutivo."Nombres" || ' ' || PersonaEjecutivo."ApellidoPaterno" || ' ' || PersonaEjecutivo."ApellidoMaterno" 												AS "NombrePersonaEjecutivo"
		 , TO_NVARCHAR(Persona."FechaNacimiento", 'DD-MM-YYYY')																													AS "FechaNacimientoCliente"
		 , PaisNacimiento."NombrePais"																																			AS "Nacionalidad"
		 , TipoEstadoCivil."Nombre"																																				AS "EstadoCivil"
		 , MAP(Persona."Sexo",'M','MASCULINO','FEMENINO')																														AS "Genero"
		 , Pais."NombrePais"																																					AS "Pais"
		 , Ciudad."NombreCiudad"																																				AS "Ciudad"
		 , ComunaCliente."NombreDivisionPais"																																	AS "ComunaCliente"
   		 , CASE 
   				WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."Calle"
				WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."Calle"
				WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."Calle"
				WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."Calle"
				ELSE NULL
		   END																																									AS "DireccionCliente"
   		 , CASE 
   				WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."Numero"
				WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."Numero"
				WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."Numero"
				WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."Numero"
				ELSE NULL
		   END																																									AS "NumeroDireccionCliente"
		 , Profesion."Nombre"																																					AS "CargoPersona"
		 , COALESCE(Categoria."NombreCategoriaInversionista",'')																												AS "NombreCategoriaInversionista"
		 , COALESCE(PerfilComercial."DescripcionPerfilComercialBase",'')																										AS "NombrePerfilComercial"
		 , Contrato."TelefonoCliente"																																				AS "TelefonoFijo"
		 , Contrato."TelefonoCelularCliente"																																		AS "TelefonoMovil"
		 , Contrato."TelefonoComercialCliente"																																	AS "TelefonoLaboral"
		 , Contrato."EmailCliente"																																				AS "EmailCliente"
		 , Empleador."RazonSocial"																																				AS "NombreEmpleador"
		 , Contrato."DocumentoIdentidadEmpleador"																																AS "DocumentoIdentidadEmpleador"
		 , TelefonoEmpleador."Numero"																																			AS "TelefonoEmpleador"
		 , DireccionFisicaEmpleador."Calle"																																		AS "DireccionComercialEmpleador"
		 , DireccionFisicaEmpleador."Numero"																																	AS "NumeroDireccionEmpleador"
		 , ComunaEmpleador."NombreDivisionPais"																																	AS "ComunaEmpleador"
		 , CiudadEmpleador."NombreCiudad"																																		AS "CiudadEmpleador"
		 , Contrato."NombreEncargado"																																			AS "NombreEncargado"
		 , Contrato."EmailEncargado"																																			AS "EmailEncargado"
		 , Contrato."TelefonoEncargado"																																			AS "TelefonoEncargado"				
		 , MAP(Contrato."IdMedioRecaudacionTrabajador",1,'IPS-EX INP','AFP')																									AS "MedioRecaudacion"
		 , EntidadRecaudadora."Nombre"																																			AS "EntidadRecaudadora"
		 , Contrato."CodigoPrevision"																																			AS "CodigoPrevision"
		 , Contrato."Pensionado"																																				AS "Pensionado"
		 , Contrato."Trabajador"																																				AS "Trabajador"
		 , Contrato."FormaDePago"																																				AS "FormaDePago"
		 , MAP(Contrato."FormaDePago",'DIRECTA',MAP(Contrato."IdMedioRecaudacionFormaPago",1,'DIRECTAMENTE AL TRABAJADOR',2,'A TRAVES DEL EMPLEADOR','PAC'),NULL)				AS "Recaudador"
		 , MAP(Contrato."FormaDePago",'DIRECTA',MAP(Contrato."IdMedioRecaudacionFormaPago",3,Contrato."DiaDescuento",NULL),NULL)												AS "DiaDescuento"
		 , MAP(Contrato."FormaDePago",'INDIRECTA',MAP(Contrato."IdMedioRecaudacionTrabajador",1,'IPS-EX INP','AFP'),NULL)														AS "Recaudacion"
		 , MAP(Contrato."FormaDePago",'INDIRECTA',EntidadRecaudadora."Nombre",NULL)																								AS "Institucion"
		 , MAP(Contrato."FormaDePago",'INDIRECTA',Contrato."ComisionTransferencia",NULL)																						AS "ComisionTransferencia"
		 , Contrato."NombreEntidadOrigen"																																		AS "NombreEntidadOrigen"
		 , Contrato."RutEntidadOrigen"																																			AS "RutEntidadOrigen"
		 , Contrato."TipoDeAhorro"                                              																								AS "TipoDeAhorro"
		 , Contrato."FondoDeOrigen"                                             																								AS "FondoDeOrigen"
		 , Contrato."TipoTraspaso"                                              																								AS "TipoTraspaso"
		 , Contrato."MontoTraspaso"                                             																								AS "MontoTraspaso"
		 , Contrato."UFTraspaso"                                                																								AS "UFTraspaso"
		 , Contrato."PorcentajeTraspaso"																																		AS "PorcentajeTraspaso"
		 , MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."IdPortafolio")																					AS "IdPortafolio"
		 , MAP(PortafolioContrato."IdPortafolio",NULL,NULL,MatrizAgrupador."Producto")																							AS "Producto"
		 , COALESCE(PortafolioContrato."MontoPesos",0) 																															AS "MontoPesos"
		 , COALESCE(PortafolioContrato."MontoUF",0)																																AS "MontoUF"
         , COALESCE(PortafolioContrato."PorcentajeRegimenImponible",0)																											AS "Porcentaje"
		 , MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."Pago")																							AS "Pago"
		 , MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."PeriodoInicio")																					AS "PeriodoInicio"
		 , MAP(PortafolioContrato."IdPortafolio",NULL,NULL,PortafolioContrato."PeriodoTermino")																					AS "PeriodoTermino"
		 , MAP(PortafolioContrato."IdPortafolio",NULL,NULL,MatrizAgrupador."Codigo")																							AS "RegimenTributario"
		 , Portafolio."IdMatrizAgrupadorAPV"																																	AS "IdMatrizAgrupadorAPV"
		 , MatrizAgrupador."CodigoAgrupador"																																	AS "CodigoAgrupador"
		 , Portafolio."Proposito"																																				AS "Proposito"
		 , Contrato."CodigoTipoSolicitud"																																		AS "CodigoTipoSolicitud"
		 , Traspaso."TipoMovimiento"																																			AS "TipoMovimiento"
		 , COALESCE(Inversionista."IdCategoriaInversionista",Contrato."IdCategoriaInversionista")																				AS "CategoriaInversionista"
		 , COALESCE(Portafolio."IdPerfilComercialBaseOrigen",Contrato."IdPerfilComercial")																						AS "PerfilComercial"
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
				AND DireccionFisicaEmpleador."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Empleador."IdPersona")	
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS ComunaCliente
				 ON ComunaCliente."IdDivisionPais" = CASE 
														 WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."IdDivisionPais"
														 WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."IdDivisionPais"
														 WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."IdDivisionPais"
														 WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."IdDivisionPais"
														 ELSE NULL
													 END
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS ComunaEmpleador
				 ON ComunaEmpleador."IdDivisionPais" = DireccionFisicaEmpleador."IdDivisionPais"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad
				 ON Ciudad."IdCiudad" = CASE 
											WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."IdCiudad"
											WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."IdCiudad"
											WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."IdCiudad"
											WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."IdCiudad"
											ELSE NULL
										END
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CiudadEmpleador
				 ON CiudadEmpleador."IdCiudad" = DireccionFisicaEmpleador."IdCiudad"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais
				 ON Pais."IdPais" = Ciudad."IdPais"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PaisNacimiento
				 ON PaisNacimiento."IdPais" = Persona."IdPaisNacionalidadPrincipal"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Inversionista
				 ON Inversionista."IdPersona" = Persona."IdPersona"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PersonaEjecutivo
				 ON Inversionista."IdPersonaEjecutivo" = PersonaEjecutivo."IdPersona"					 
	LEFT OUTER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
				 ON PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
	LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				 ON Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
				AND Portafolio."IdCliente" = Contrato."IdCliente"
    			AND Portafolio."Proposito" = 'PROPOSITOAPV'
	LEFT OUTER JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora
				 ON EntidadRecaudadora."Codigo" = Contrato."CodigoPrevision"
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
    LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoEmpleador
    			 ON TelefonoEmpleador."IdPersona" = Empleador."IdPersona"
    			AND TelefonoEmpleador."Borrado" = 'N'
    			AND TelefonoEmpleador."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Contrato."IdPersona" AND "CodigoProposito" = 'LABO' AND "Borrado" = 'N')
    			AND TelefonoEmpleador."CodigoProposito" = 'LABO'
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Profesion" AS Profesion
				 ON Profesion."IdProfesion" = Persona."IdProfesion"
	LEFT OUTER JOIN "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
				 ON Traspaso."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
	INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" PlanTipoInversion
		ON (Contrato."IdPlan" = PlanTipoInversion."IdPlanTipoInversion" AND
			PlanTipoInversion."IdEstado" = 'VIG')
	LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" Categoria ON Categoria."IdCategoriaInversionista" = COALESCE(Inversionista."IdCategoriaInversionista",Contrato."IdCategoriaInversionista") AND Categoria."IdEmpresa" = Contrato."IdEmpresa"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.PerfilComercialBase" PerfilComercial ON PerfilComercial."IdPerfilComercialBase" = COALESCE(Portafolio."IdPerfilComercialBaseOrigen",Contrato."IdPerfilComercial") AND PerfilComercial."IdEmpresa" = Contrato."IdEmpresa";
GO
GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarContratoSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO