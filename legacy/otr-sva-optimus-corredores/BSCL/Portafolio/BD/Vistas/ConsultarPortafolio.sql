SET SCHEMA "BSCL";

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio";

CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS 
SELECT
	"PORT"."IdPortafolio", 
	"PORT"."CodigoPortafolio", 	
	"PORT"."DescripcionPortafolio",
	"MAT"."NombreAgrupador" AS "NombreAgrupador",
	"PORT"."Proposito", 
	"PORT"."IdCliente", 
	"PORT"."IdEstado",
	"EST"."Nombre" as "Estado", 
	"PORT"."FechaBloqueo", 
	"PORT"."ComentarioBloqueo", 
	"PORT"."IdTipoBloqueo",
	"BLOQ"."NombreTipoBloqueo",
	"IdPerfilComercialBaseOrigen",
	"PORT"."IdPerfilProductos",
	"PORT"."IdPersonaBrokerExtranjero",
	"PORT"."IdPerfilProductosExtra",
	"PORT"."IdPerfilCobros",
	"PORT"."IdPerfilCobrosExtra",
	"PORT"."IdPerfilCobrosPeru",
	"PORT"."IdPerfilCobrosPeruExtra",
	"PORT"."IdPerfilLimites",
	"PORT"."IdPerfilLimitesExtra",
	"PORT"."IdPerfilCondicionesOperativas",
	"PORT"."IdPerfilCondicionesOperativasExtra",
	"PORT"."IdEstadoAutorizacion",
	"PORT"."FechaIngreso",
	"PORT"."FechaCreacion",
	"PORT"."FechaModificacion",
	"PORT"."IdDireccionEnvioCorrespondencia",
	"PORT"."IdTelefonoContacto",
	"PORT"."IdCarga",
	"PORT"."IdEmpleadoEjecutivo",
	"PORT"."EsEjecutivoExclusivo",
	"PORT"."CodigoPortafolio" ||' '||"PORT"."Proposito" as "CodigoPropositoPortafolio",
	"PORT"."NumeroFicha",
	"PORT"."IdNombreFantasia",
	"PORT"."Alias",
	"PORT"."FechaUltimaCartola",
	"PORT"."IdUltimaCartola",
	COALESCE("ARE"."Codigo", '') AS "IdAreaCliente",
	COALESCE("ARE"."Descripcion", '') AS "AreaCliente",
	case when EMP."IdEmpresa" is null then 'N' else 'S' end as "EsCuentaPropia",
	EMP."IdEmpresa" as "IdEmpresaCuentaPropia",
	"PORT"."IdMatrizAgrupadorAPV",
	CASE WHEN "PORT"."IdMatrizAgrupadorAPV" = 0 THEN 'N' ELSE 'S' END AS "TienePropositoAPV",
	MAP("MAT"."IdProductoAgrupador",NULL,9999,"MAT"."IdProductoAgrupador") AS "IdProductoAgrupador",
	RT."IdRegimenTributario",
	(CASE WHEN COALESCE(SOC."IdContraparte",0) = 0 THEN 'N' ELSE 'S' END) AS "EsContraparte",
	CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador",
	CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("MAT"."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC",
	"ESTAUT"."IdEstado" as "IdEstadoPortafolioAutorizacion",
	"ESTAUT"."Nombre" as "EstadoPortafolioAutorizacion",
	"PER"."IdPersona",
	"PER"."IdEstado" AS "IdEstadoPersona",
	RT."CodigoRegimenTipoAhorro",
	"PORT"."ExcluyeLimpiezaLineaOperacional",
	"PORT"."IdEmpleadoEjecutivoComercial",	
	"PORT"."FechaBloqueoTemporal",
	"PORT"."FechaDesbloqueoTemporal",
	"PORT"."IdTipoDesbloqueo",
	"PORT"."ComentarioDesbloqueo"

	
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI" ON "CLI"."IdCliente" = "PORT"."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" ON "CLI"."IdPersona" = "PER"."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.EstadoPortafolio" AS "ESTPORT" ON "PORT"."IdEstado" = "ESTPORT"."IdEstado" AND "ESTPORT"."NombreEntidad" = 'PORTAFOLIO'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "EST"."IdEstado" = (CASE WHEN "PORT"."IdEstado" IN ('ACT','VIG') THEN "PORT"."IdEstado" ELSE 'BLO' END)
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTAUT" ON "ESTAUT"."IdEstado" = (CASE WHEN "PORT"."IdEstadoAutorizacion" = 'PEN' THEN 'PDA' ELSE "PORT"."IdEstado" END)
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on SOC."IdCliente" = "PORT"."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" "MAT" ON "MAT"."IdMatrizAgrupadorAPV" = "PORT"."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS "ARE" ON "PORT"."AreaCliente" = "ARE"."Codigo"
LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" RT ON RT."IdRegimenTributario" = "MAT"."IdRegimenTributario"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = "PORT"."CodigoPortafolio" AND  "PORT"."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" AS "BLOQ" ON "BLOQ"."IdTipoBloqueo" = "PORT"."IdTipoBloqueo" AND "BLOQ"."IdTipoEntidad" = 'PORTAFOLIO'
WHERE "PORT"."Borrado" = 'N';
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
