SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarClienteContratoPortafolioSAAPV";
GO
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarClienteContratoPortafolioSAAPV"  AS 

SELECT  POR."IdPortafolio"
	,POR."CodigoPortafolio"
	,PTI."NombrePlanTipoInversion"
	,DOC."DocumentoIdentidad" AS "RutCliente"
	,PER."Nombres"||' '||PER."ApellidoPaterno"||' '||PER."ApellidoMaterno" AS "NombrePersona"
	,PER."IdPersona"
	,POR."IdCliente"
	,CON."IdContratoSAAPV"
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
	,PRD."NombreProductoAgrupador" AS "Producto"
	,CON."IdEstado" AS "Estado"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON (CLI."IdCliente" = POR."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
	ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
 	ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
LEFT JOIN "BSCL.APV.BD.Vistas::AFI.ConsultarContratoPortafolioSAAPVPlanAbierto" AS PORCON
	ON (POR."IdPortafolio" = PORCON."IdPortafolio")
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON
	ON (CON."IdContratoSAAPV" = PORCON."IdContratoSAAPV" AND
		CON."IdCliente" = POR."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERJUR
	ON (CON."IdEmpleador" = PERJUR."IdPersona" AND
		PERJUR."Tipo" = 'J')
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
	ON (MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV")
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
	ON (REG."IdRegimenTributario" = MAT."IdRegimenTributario")
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS PRD
	ON (PRD."IdProductoAgrupador" = MAT."IdProductoAgrupador")
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PTI
	ON (PTI."IdPlanTipoInversion" = MAT."IdPlanTipoInversion")
LEFT JOIN "BSCL.APV.BD::SCS.MedioRecaudacion" AS MEDREC
	ON (CON."IdMedioRecaudacionFormaPago" = MEDREC."IdMedioRecaudacion")
LEFT JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS ENTREC
	ON (CAST(ENTREC."Codigo" AS NVARCHAR) = CASE WHEN CON."Institucion" = '' THEN '0' ELSE CON."Institucion" END)
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
WHERE POR."Proposito" = 'PROPOSITOAPV'
AND PTI."IdPlanTipoInversion" = 1
AND DOC."IdDocumentoIdentidad" IS NOT NULL
AND CLI."IdCliente" IS NOT NULL
AND PER."IdPersona" IS NOT NULL
AND POR."IdEstado" <> 'INA';
GO
GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarClienteContratoPortafolioSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO