---------------------------------------------------------------
-- VISTA PARA bcs-combobox-cliente-portafolio.
-- NO UTILIZAR EN EL NEGOCIO, PARA ESTO HACER USO DE LA VISTA  
-- "BSCL.Portafolio.BD.Vistas::POR.ConsultarPortafolio"
---------------------------------------------------------------


SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"; 

CREATE VIEW "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"  
AS
WITH PERSONA AS (
	SELECT "IdDocumentoIdentidad","Tipo","Nombres","IdPersona","Borrado","IdDocumentoIdentidadAdicional",
	CASE "PER"."Tipo" 
			WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
			ELSE "PER"."RazonSocial"
		END  as "NombreCompleto"
		FROM "BSCL.SocioNegocio.BD::PER.Persona" "PER"
)
SELECT
	"IDENT"."DocumentoIdentidad" ||
	' (' || port."CodigoPortafolio" || ')' ||
	UPPER(
		CASE WHEN TRIM(COALESCE("PER"."NombreCompleto", '')) != '' THEN ' ' || TRIM("PER"."NombreCompleto") ELSE '' END ||
        CASE WHEN TRIM(COALESCE(ProductoAgrupador."NombreProductoAgrupador", '')) != '' THEN ' - ' || TRIM(ProductoAgrupador."NombreProductoAgrupador") ELSE '' END ||
        CASE WHEN TRIM(COALESCE(port."Alias", '')) != '' THEN ' - ' || TRIM(port."Alias") ELSE '' END ||
        CASE WHEN TRIM(COALESCE(fan."DescripcionNombreFantasia", '')) != '' THEN ' - ' || TRIM(fan."DescripcionNombreFantasia") ELSE '' END
	) AS "NombreCompletoPortafolio",
	LPAD(port."CodigoPortafolio",3,' ') AS "CodigoPortafolioPad",
	port."IdPortafolio", 
	port."CodigoPortafolio", 	
	port."DescripcionPortafolio",
	port."Proposito", 
	port."IdCliente", 
	"EST"."Nombre" as "Estado", 
	port."FechaBloqueo", 
	port."ComentarioBloqueo", 
	port."IdTipoBloqueo",
	"BLOQ"."NombreTipoBloqueo",
	port."IdPerfilComercialBaseOrigen",
	port."IdPerfilProductos",
	port."IdPerfilProductosExtra",
	port."IdPerfilCobros",
	port."IdPerfilCobrosExtra",
	port."IdPerfilLimites",
	port."IdPerfilLimitesExtra",
	port."IdPerfilCondicionesOperativas",
	port."IdPerfilCondicionesOperativasExtra",
	port."IdEstadoAutorizacion",
	port."IdEstado",
	port."FechaIngreso",
	port."FechaCreacion",
	port."FechaModificacion",
	port."IdDireccionEnvioCorrespondencia",
	port."IdTelefonoContacto",
	port."IdCarga",
	port."IdEmpleadoEjecutivo",
	port."EsEjecutivoExclusivo",
	"PORT"."CodigoPortafolio" ||' '||"PORT"."Proposito" as "CodigoPropositoPortafolio",
	port."NumeroFicha",
	port."IdNombreFantasia",
	port."Alias",
	case when EMP."IdEmpresa" is null then 'N' else 'S' end as "EsCuentaPropia",
	EMP."IdEmpresa" as "IdEmpresaCuentaPropia",
	"PER"."NombreCompleto",
	"IDENT"."DocumentoIdentidad"        AS "DocumentoIdentidad",
	cli."IdEmpresa",
	cli."IdPersona",
	cli."IdPersonaEjecutivo",
	cli."IdCategoriaInversionista",
	cli."IdTipoCliente",
	cli."IdEstado" as "IdEstadoCliente",
	"PER"."Tipo" as "TipoPersona",
	cli."TipoEnrolamiento",
	"SOC"."IdSocioDeNegocio",
	cli."FechaBloqueo" as "FechaBloqueoCliente",
	cli."ComentarioBloqueo" as "ComentarioBloqueoCliente", 
	cli."IdTipoBloqueo" as "IdTipoBloqueoCliente",
	"TIPBLO"."NombreTipoBloqueo" as "NombreTipoBloqueoCliente",
	CASE WHEN ((SUBSTRING("IDENT"."DocumentoIdentidad",3,11) = "IDENTADICIONAL"."DocumentoIdentidad") AND "PER"."Tipo" = 'J' AND "IDENT"."IdTipoDocumentoIdentidad" = 'IUP' AND "IDENTADICIONAL"."IdTipoDocumentoIdentidad" = 'RUT') THEN 'S' ELSE 'N' END AS "EsRutInterno"
	,"IDENTADICIONAL"."DocumentoIdentidad" AS "DocumentoIdentidadAdicional"
    ,"IDENTADICIONAL"."IdTipoDocumentoIdentidad" AS "TipoDocumentoIdentidadAdicional"
    ,(CASE WHEN COALESCE(SOC."IdContraparte",0) = 0 THEN 'N' ELSE 'S' END) AS "EsContraparte"
    ,CASE WHEN COALESCE(ind."ValorIndicador",'0') = CAST(port."IdPortafolio" AS NVARCHAR) THEN 'S' ELSE 'N' END AS "PortafolioDerivados"
    ,CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("MAT"."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
    ,CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
	,COALESCE(INDCUMP."ValorIndicador",'N') AS "BloqueadoPorCumplimiento"
	,COALESCE("ARE"."Codigo", '') AS "Codigo"
	,port."IdEmpleadoEjecutivoComercial"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS port
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on port."IdCliente" = cli."IdCliente"
LEFT JOIN PERSONA AS "PER" ON "PER"."IdPersona" = cli."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENTADICIONAL" ON "PER"."IdDocumentoIdentidadAdicional" = "IDENTADICIONAL"."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" "MAT" ON "MAT"."IdMatrizAgrupadorAPV" = "PORT"."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = "PORT"."CodigoPortafolio" AND  "PORT"."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS "ARE" ON "PORT"."AreaCliente" = "ARE"."Codigo"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" AS "BLOQ" ON "BLOQ"."IdTipoBloqueo" = "PORT"."IdTipoBloqueo" AND "BLOQ"."IdTipoEntidad" = 'PORTAFOLIO'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "EST"."IdEstado" = (CASE WHEN "PORT"."IdEstado" IN ('ACT','VIG') THEN "PORT"."IdEstado" ELSE 'BLO' END)
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on SOC."IdCliente" = "PORT"."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" fan on port."IdNombreFantasia" = fan."IdNombreFantasia"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" AS "TIPBLO" ON ("TIPBLO"."IdTipoBloqueo" = "CLI"."IdTipoBloqueo" AND "TIPBLO"."IdEmpresa" = "CLI"."IdEmpresa" AND "TIPBLO"."IdTipoEntidad" = 'CLIENTE')	
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
		ON ProductoAgrupador."IdProductoAgrupador" = MAP("MAT"."IdProductoAgrupador",NULL,9999,"MAT"."IdProductoAgrupador")
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" tInd ON  tInd."IdTipoEntidad"='CLIENTE' AND tInd."GrupoIndicador"='GENERALES' AND tInd."CodigoTipoIndicador"='PORTAFOLIO_DERIVADOS'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ind ON ind."IdTipoIndicador" = tInd."IdTipoIndicador" AND ind."IdTipoEntidad"='CLIENTE' AND ind."IdEntidad" = port."IdCliente"
  LEFT JOIN 
  ( 
  	SELECT IND."IdEntidad", IND."ValorIndicador"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND ON  (IND."IdTipoIndicador" = TIND."IdTipoIndicador" AND IND."IdTipoEntidad" = TIND."IdTipoEntidad")
	WHERE TIND."IdTipoEntidad" 		= 'CLIENTE'
	AND TIND."CodigoTipoIndicador" 	= 'BCCUM' -- Bloqueo por cumplimiento
	AND TIND."IdEstado" 			= 'ACT'
  ) AS INDCUMP ON (INDCUMP."IdEntidad" = cli."IdCliente")
  WHERE "PORT"."Borrado" = 'N' AND "IDENT"."Borrado"='N' AND "PER"."Borrado"='N' AND CLI."Borrado"='N' ;

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

