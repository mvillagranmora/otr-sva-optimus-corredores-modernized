SELECT
	 CON."IdContratoSAAPV"
	,CON."FolioContratoSAAPV"
	,CON."Folio"
	,CON."NombrePlan"
	,CON."DocumentoIdentidadEmpleador" AS "RutEmpleador"
	,PERJUR."RazonSocial" AS "NombreEmpleador"
	,CON."NombreEncargado"
	,CON."FechaSuscripcion"
	,CON."FechaCreacion"
	,CON."FechaAprobacion"
	,CON."FechaNotificacion"
	,CON."FechaRecepcion"
	,PORCON."OrigenAhorro"
	,COALESCE(PORCON."MontoPesos",0) AS "MontoPesos"
	,COALESCE(PORCON."MontoUF",0) AS "MontoUF"
	,COALESCE(PORCON."PorcentajeRegimenImponible",0) AS "PorcentajeRegimenImponible"
	,PORCON."Pago"
	,PORCON."PeriodoInicio"
	,PORCON."PeriodoTermino"
	,REG."Codigo" AS "RegimenTributario"
	,CON."FormaDePago"
	,MEDREC."NombreMedioRecaudacion"
	,CON."DiaDescuento"
	,ENTREC."Nombre" AS "Institucion"
	,CON."ComisionTransferencia"
	,CON."CodigoTipoSolicitud"
	,CON."RutEntidadOrigen"
	,CON."NombreEntidadOrigen"
	,CON."TipoDeAhorro"
	,CON."FondoDeOrigen"
	,CON."TipoTraspaso"
	,CON."MontoTraspaso"
	,CON."UFTraspaso"
	,CON."PorcentajeTraspaso"
	,ESTNOT."Nombre" AS "EstadoNotificacion"
	,EVTAUT."IdEstado" AS "IdEstadoEvtAut"
	,ESTAPV."Nombre" AS "NombreEstadoAPV"
	,ESTPLA."Nombre" AS "EstadoPAC"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PORCON
	ON (CON."IdContratoSAAPV" = PORCON."IdContratoSAAPV")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
	ON (PORCON."IdPortafolio" = POR."IdPortafolio" AND
		CON."IdCliente" = POR."IdCliente" AND
		POR."Proposito" = 'PROPOSITOAPV')
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERJUR
	ON (CON."IdEmpleador" = PERJUR."IdPersona" AND
		PERJUR."Tipo" = 'J')
--LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MAT
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
	ON (MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV" AND
		MAT."IdEstado" = 'VIG')
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
	ON (REG."IdRegimenTributario" = MAT."IdRegimenTributario" AND
		REG."IdEstado" = 'VIG')
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS PRD
	ON (PRD."IdProductoAgrupador" = MAT."IdProductoAgrupador" AND
		PRD."IdEstado" = 'VIG')
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PTI
	ON (PTI."IdPlanTipoInversion" = MAT."IdPlanTipoInversion" AND
		PTI."IdEstado" = 'VIG')
LEFT JOIN "BSCL.APV.BD::SCS.MedioRecaudacion" AS MEDREC
	ON (CON."IdMedioRecaudacionFormaPago" = MEDREC."IdMedioRecaudacion")
LEFT JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS ENTREC
	ON (ENTREC."Codigo" = CASE WHEN CON."Institucion" = '' THEN 0 ELSE CON."Institucion" END)
LEFT JOIN "BSCL.APV.BD::APV.Estado" AS ESTNOT
	ON (CON."IdEstadoEnvioNotificacion" = ESTNOT."Codigo")
LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS EVTAUT
	ON (EVTAUT."NumeroOperacionConcepto" = CON."FolioContratoSAAPV" AND
		EVTAUT."IdCliente" = CON."IdCliente" AND
		EVTAUT."IdModulo" = 'APV' AND
		EVTAUT."FechaHoraRegistro" = (SELECT MAX("FechaHoraRegistro")
									  FROM "BSCL.Plataforma.BD::AUT.Evento"
									  WHERE "IdCliente" = CON."IdCliente"
									    AND "NumeroOperacionConcepto" = CON."FolioContratoSAAPV"
									    AND "IdModulo" = 'APV'))
LEFT JOIN "BSCL.APV.BD::APV.Estado" AS ESTAPV
	ON (ESTAPV."Codigo" = CON."IdEstado")
LEFT JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS MPAC
	ON (cast(MPAC."FolioSAAPV" as varchar(200)) =  CON."FolioContratoSAAPV")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTPLA
	ON (ESTPLA."IdEstado" = MPAC."Estado")
WHERE CON."IdEmpresa" = :IdEmpresa
  AND CON."IdPlan" = :IdPlan
  AND (:FiltroIdPersona = 0 OR CON."IdPersona" IN (:IdPersona))
  AND (:FiltroIdPortafolio = 0 OR PORCON."IdPortafolio" IN (:IdPortafolio))
  AND CON."IdEstado" IN ('VIG')
  AND POR."IdEstado" <> 'INA'
;
