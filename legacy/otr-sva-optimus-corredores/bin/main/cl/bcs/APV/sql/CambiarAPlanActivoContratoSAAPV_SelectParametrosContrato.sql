
SELECT
	 CON."IdContratoSAAPV"
	,CON."IdEmpresa"
	,CON."FolioContratoSAAPV"
	,CON."IdPlan"
	,PTI."NombrePlanTipoInversion" AS "NombrePlan"
	,CON."FechaSuscripcion"
	,CON."FechaCreacion"
	,CON."FechaAprobacion"
	,CON."FechaNotificacion"
	,CON."FechaRecepcion"
	,CON."Folio"
	,CON."IdPersona"
	,CON."IdEmpleador"
	,CON."IdCliente"
	,CLI."IdCategoriaInversionista"
	,POR."IdPerfilComercialBaseOrigen" AS "IdPerfilComercial"
	,PRFAPV."IdPerfilComercialActivo" AS "IdPerfilComercialAPV"
	,CON."NombreCliente"
	,CON."ApellidoPaternoCliente"
	,CON."ApellidoMaternoCliente"
	,CON."DocumentoIdentidadCliente"
	,CON."FechaNacimientoCliente"
	,CON."EstadoCivil"
	,CON."IdTipoGenero"
	,Persona."IdPaisResidencia"  AS "IdPaisResidenciaCliente"
	,CON."IdCiudadCliente"
	,CON."IdComunaCliente"
	,CON."DireccionCliente"
	,'0' AS "NumeroDireccionCliente"
	,CON."IdTipoCargoPersona"
	,CON."TelefonoCliente"
	,CON."TelefonoComercialCliente"
	,CON."TelefonoCelularCliente"
	,CON."EmailCliente"
	,Empleador."RazonSocial" AS "RazonSocialEmpleador"
	,CON."IdMedioRecaudacionTrabajador"
	,CON."IdEntidadRecaudadora"
	,DocEmpleador."DocumentoIdentidad" AS "DocumentoIdentidadEmpleador"
	,Empleador."IdPaisResidencia" AS "IdPaisEmpleador"
	,DirEmpleador."Calle"  AS "DireccionComercialEmpleador" 
	,'0' AS "NumeroDireccionEmpleador"
	,TO_BIGINT(0) AS "IdCiudadEmpleador"
	,CON."IdComunaEmpleador"
    ,TelEmpleador."Numero" AS "TelefonoEmpleador"
	,CON."NombreEncargado"
	,CON."EmailEncargado"
	,CON."TelefonoEncargado"
	,CON."CodigoPrevision"
	,CON."Pensionado"
	,CON."Trabajador"
	,CON."FormaDePago"
	,CON."IdMedioRecaudacionFormaPago"
	,CON."DiaDescuento"
	,CON."Institucion"
	,CON."ComisionTransferencia"
	,CON."CodigoTipoSolicitud"
	,TRA."TipoMovimiento"
	,TRA."RazonRetiro"
	,CON."NombreEntidadOrigen"
	,CON."RutEntidadOrigen"
	,TRA."IdPersona" AS "IdPersonaOrigen"
	,TRA."Descripcion"
	,CON."TipoDeAhorro"
	,CON."FondoDeOrigen"
	,CON."TipoTraspaso"
	,CON."MontoTraspaso"
	,CON."UFTraspaso"
	,CON."PorcentajeTraspaso"
	,CON."IdEstadoEnvioNotificacion"
	,CON."IdEstadoSolicitud"
	,CON."IdEstado"
	,CON."IdCargaMasiva"
	,EMP."IdEmpleado"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" CON
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
				 ON Persona."IdPersona" = CON."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Empleador
				 ON Empleador."IdPersona" = CON."IdEmpleador"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocEmpleador
				ON DocEmpleador."IdDocumentoIdentidad" =  Empleador."IdDocumentoIdentidad"				
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DirEmpleador
				ON DirEmpleador."IdDireccionFisica"  = Empleador."IdDireccionFisicaPrincipal" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelEmpleador
				ON TelEmpleador."IdTelefono"  = Empleador."IdTelefonoPrincipal" 
				
				INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" PTI
	ON (PTI."IdPlanTipoInversion" = 2)
INNER JOIN (
		SELECT PORCON."IdContratoSAAPV", POR."IdPerfilComercialBaseOrigen"
		FROM "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PORCON
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
			ON (PORCON."IdPortafolio" = POR."IdPortafolio" AND
				POR."Proposito" = 'PROPOSITOAPV' AND
				POR."IdEstado" <> 'INA')
		WHERE PORCON."IdContratoSAAPV" IN (:IdContratoSAAPV)
		GROUP BY PORCON."IdContratoSAAPV", POR."IdPerfilComercialBaseOrigen"
	) POR
	ON (CON."IdContratoSAAPV" = POR."IdContratoSAAPV")
LEFT JOIN "BSCL.APV.BD::TRA.Traspaso" AS TRA
	ON (CON."IdContratoSAAPV" = TRA."IdContratoSAAPV")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON (CON."IdCliente" = CLI."IdCliente" AND
		CLI."Borrado" = 'N')
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
	ON (EMP."IdPersona" = CLI."IdPersonaEjecutivo" AND
		EMP."Borrado" = 'N')
LEFT JOIN "BSCL.APV.BD::APV.PerfilComercialAPV" PRFAPV
	ON (POR."IdPerfilComercialBaseOrigen" = PRFAPV."IdPerfilComercialAbierto" AND
		CLI."IdCategoriaInversionista" = PRFAPV."IdCategoriaInversionista" AND
		PRFAPV."IdEmpresa" = :IdEmpresa AND
		PRFAPV."IdEstado" = 'VIG')
WHERE CON."IdEmpresa" = :IdEmpresa
  AND CON."IdPlan" = :IdPlan
  AND CON."IdContratoSAAPV" IN (:IdContratoSAAPV)
  AND CON."IdEstado" IN ('VIG')
;
