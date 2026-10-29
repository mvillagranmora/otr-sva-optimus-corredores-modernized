WITH empleados_ejecutivos AS (
	SELECT  DISTINCT "IdEmpleadoEjecutivo" , CASE WHEN CURRENT_UTCTIMESTAMP BETWEEN "FechaInicioReemplazo" AND "FechaFinReemplazo" THEN "IdEmpleadoEjecutivoReemplaza" ELSE NULL END AS "IdEmpleadoEjecutivoReemplaza", "CodigoEmpleado", "NombreCompleto", "FechaInicioReemplazo", "FechaFinReemplazo", "IdEstado" FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"
), empleados_reemplazo AS (
	SELECT  DISTINCT "IdEmpleadoEjecutivo" , "IdEmpleadoEjecutivoReemplaza", "CodigoEmpleado", "NombreCompleto", "FechaInicioReemplazo", "FechaFinReemplazo", "IdEstado" FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"
)
SELECT
		DI."IdTipoDocumentoIdentidad" as "TipoDocumentoIdentidad",
		DI."DocumentoIdentidad",
		COALESCE(DIA."DocumentoIdentidad", '') As "DocumentoIdentidadRutCavali",
		CASE per."Tipo" WHEN 'N' THEN per."Nombres" || ' ' || per."ApellidoPaterno" || ' ' || per."ApellidoMaterno" ELSE per."RazonSocial" END AS "NombreCompleto",
		COALESCE(per."FechaNacimiento",TO_DATE('0001-01-01')) AS "FechaNacimiento",
		cli."FechaCreacion",
		COALESCE(cli."FechaModificacion",TO_DATE('0001-01-01')) AS "FechaModificacionCliete",
		COALESCE(per."FechaModificacion",TO_DATE('0001-01-01')) AS "FechaModificacionPersona",
		COALESCE(port."FechaModificacion",TO_DATE('0001-01-01')) AS "FechaModificacionPortafolio",
		CASE WHEN (per."FechaDefuncion" != '0001-01-01' AND per."FechaDefuncion" != NULL) THEN TO_DATE(per."FechaDefuncion") ELSE (CASE WHEN cli."IdTipoBloqueo" = 'BMUERT' OR per."IdTipoBloqueo" = 'BMUERT' THEN TO_DATE(per."FechaModificacion") ELSE TO_DATE('0001-01-01') END) END AS "FechaDefuncion",
		cli."TipoEnrolamiento",
		cli."IdTipoCliente",
		cli."IdCliente",
		port."IdPortafolio",
		tipocli."Nombre" as "NombreTipoCliente",
		per."Tipo" as "IdTipoPersona",
		tipoper."Descripcion" as "NombreTipoPersona",
		cli."IdSegmentoCliente",
		se."NombreSegmentoCliente",
		se."DescripcionSegmento" as "DescripcionSegmentoCliente", 
		port."IdEmpleadoEjecutivo",
		CASE WHEN COALESCE(empReemplazo."CodigoEmpleado",'')='' THEN emp."CodigoEmpleado" ELSE empReemplazo."CodigoEmpleado" END AS "CodigoEmpleadoEjecutivo",
		CASE WHEN COALESCE(empReemplazo."NombreCompleto",'')='' THEN emp."NombreCompleto" ELSE empReemplazo."NombreCompleto" END AS "NombreEmpleadoEjecutivo",
		cli."IdCategoriaInversionista",
		cci."NombreCategoriaInversionista",
		per."IdEstado" AS "IdEstadoPersona",
		cli."IdEstado" AS "IdEstadoCliente",
		CASE WHEN per."IdEstado" = 'BLO' THEN per."IdEstado" ELSE (CASE WHEN cli."IdEstado" = 'BLO' THEN cli."IdEstado" ELSE port."IdEstado" END) END AS "IdEstadoPortafolio",
		estadoPer."Nombre" AS "NombreEstadoPersona",
		estadoCli."Nombre" AS "NombreEstadoCliente",
		CASE WHEN per."IdEstado" = 'BLO' THEN estadoPer."Nombre" ELSE (CASE WHEN cli."IdEstado" = 'BLO' THEN estadoCli."Nombre" ELSE estadoPort."Nombre" END) END AS "NombreEstadoPortafolio",
		port."CodigoPortafolio",
		port."DescripcionPortafolio",
		port."Proposito" as "PropositoPortafolio",
		per."IdTipoBloqueo" AS "IdTipoBloqueoPersona",
		CASE WHEN per."IdEstado" = 'BLO' THEN per."IdTipoBloqueo" ELSE cli."IdTipoBloqueo" END AS "IdTipoBloqueoCliente",
		CASE WHEN per."IdEstado" = 'BLO' AND port."IdTipoBloqueo" IS NULL THEN per."IdTipoBloqueo" ELSE (CASE WHEN cli."IdEstado" = 'BLO' THEN cli."IdTipoBloqueo" ELSE port."IdTipoBloqueo" END) END AS "IdTipoBloqueoPortafolio",
		tipoBloqPersona."NombreTipoBloqueo" AS "NombreTipoBloqueoPersona",
		CASE WHEN per."IdEstado" = 'BLO' AND tipoBloqCliente."NombreTipoBloqueo" IS NULL THEN tipoBloqPersona."NombreTipoBloqueo" ELSE tipoBloqCliente."NombreTipoBloqueo" END AS "NombreTipoBloqueoCliente",
		CASE WHEN per."IdEstado" = 'BLO' AND tipoBloqPort."NombreTipoBloqueo" IS NULL THEN tipoBloqPersona."NombreTipoBloqueo" ELSE (CASE WHEN cli."IdEstado" = 'BLO' THEN tipoBloqCliente."NombreTipoBloqueo" ELSE tipoBloqPort."NombreTipoBloqueo" END) END AS "NombreTipoBloqueoPortafolio",
		port."IdPerfilComercialBaseOrigen" as "IdPerfilComercial",
		port."IdPerfilProductos",
		port."IdPerfilCobros",
		port."IdPerfilLimites",
		area."Descripcion" AS "AreaCliente",
        --peru
		cli."CodigoAdicionalHolding",
		CASE 
		  WHEN cli."EsClienteFinal" = 'S' THEN 'Si' 
		ELSE 'No' 
		END AS "ClienteFinal",
		ConsBrokEx."NombreCompleto" AS "NombreCompletoBroker",
		per."IdPersona",
		per."IdTipoCanalIngresoCorredor"
FROM "BSCL.SocioNegocio.BD::GBL.ConsultaAvanzadaClientes_IdPortafolio" AS port1
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" port ON port1."IdPortafolio" = port."IdPortafolio" AND port."Borrado" = 'N'
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS area ON port."AreaCliente" = area."Codigo"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli ON port."IdCliente" = cli."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" se ON cli."IdSegmentoCliente" = se."IdSegmentoCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista" cci ON cli."IdCategoriaInversionista" = cci."IdCategoriaInversionista" AND cli."IdEmpresa" = cci."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" per ON per."IdPersona" = cli."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" tipoper on tipoper."IdTipoPersona" = per."Tipo"
LEFT JOIN empleados_ejecutivos emp on port."IdEmpleadoEjecutivo" = emp."IdEmpleadoEjecutivo" AND emp."IdEstado" = 'ACT'
LEFT JOIN empleados_reemplazo empReemplazo on emp."IdEmpleadoEjecutivoReemplaza" = empReemplazo."IdEmpleadoEjecutivo" AND empReemplazo."IdEstado" = 'ACT' AND CURRENT_UTCTIMESTAMP BETWEEN emp."FechaInicioReemplazo" AND emp."FechaFinReemplazo"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" tipoBloqPersona on tipoBloqPersona."IdTipoBloqueo" = per."IdTipoBloqueo" AND tipoBloqPersona."IdTipoEntidad" = 'PERSONA'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" tipoBloqCliente on tipoBloqCliente."IdTipoBloqueo" = cli."IdTipoBloqueo" AND tipoBloqCliente."IdTipoEntidad" = 'CLIENTE'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" tipoBloqPort on tipoBloqPort."IdTipoBloqueo" = port."IdTipoBloqueo" AND tipoBloqPort."IdTipoEntidad" = 'PORTAFOLIO'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" estadoCli on estadoCli."IdEstado" = cli."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" estadoPer on estadoPer."IdEstado" = per."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" estadoPort on estadoPort."IdEstado" = port."IdEstado"					
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoCliente" tipocli on tipocli."IdTipoCliente" = cli."IdTipoCliente"					
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = per."IdPersona" AND DI."Predeterminado" = 'S'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DIA ON per."IdDocumentoIdentidadAdicional" = DIA."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBrokerExtranjero" AS ConsBrokEx ON ConsBrokEx."IdPersona" = port."IdPersonaBrokerExtranjero"
CROSS JOIN "BSCL.SocioNegocio.BD::GBL.ConsultaAvanzadaClientes_Filtros" filtros
WHERE cli."IdCliente" IS NOT NULL AND per."IdPersona" IS NOT NULL AND tipoper."IdTipoPersona" IS NOT NULL AND

--filtrar por los siguientes parametros
(
	(filtros."FiltroNombre" = 1 OR (CASE per."Tipo" WHEN 'N' THEN per."Nombres" || ' ' || per."ApellidoPaterno" || ' ' || per."ApellidoMaterno" ELSE per."RazonSocial" END) like '%' || REPLACE(filtros."Nombre",' ', '%') || '%')
	AND (filtros."FiltroClienteFinal" = 1 OR cli."EsClienteFinal" = filtros."ClienteFinal")
	AND (filtros."FiltroTipoDocumentoIdentidad" = 1 OR DI."IdTipoDocumentoIdentidad" like '%' || filtros."TipoDocumentoIdentidad")
	AND tipocli."IdEmpresa" = filtros."IdEmpresa"
	AND (filtros."FiltroAliasPortafolio" = 1 OR port."Alias" like '%' || filtros."AliasPortafolio" || '%')
	AND (filtros."FiltroIdTipoPersona" = 1 OR per."Tipo" = filtros."IdTipoPersona")
	AND (filtros."FiltroDocumentoIdentidad" = 1 OR DI."DocumentoIdentidad" = filtros."DocumentoIdentidad")
	AND (filtros."FiltroIdTipoCliente" = 1 OR cli."IdTipoCliente" = filtros."IdTipoCliente")
	AND (filtros."FiltroIdPersonaBrokerExtranjero" = 1 OR port."IdPersonaBrokerExtranjero" = filtros."IdPersonaBrokerExtranjero") 
	AND (filtros."FiltroIdSegmentoCliente" = 1 OR cli."IdSegmentoCliente" = filtros."IdSegmentoCliente")
	AND (filtros."FiltroIdEstadoCliente" = 1 OR cli."IdEstado" = filtros."IdEstadoCliente")
	AND (filtros."FiltroIdCategoriaInversionista" = 1 OR cli."IdCategoriaInversionista" = filtros."IdCategoriaInversionista")
	AND (filtros."FiltroIdTipoBloqueoCliente" = 1 OR cli."IdTipoBloqueo" = filtros."IdTipoBloqueoCliente" OR per."IdTipoBloqueo" = filtros."IdTipoBloqueoCliente")
	AND (filtros."FiltroIdTipoBloqueoPortafolio" = 1 OR port."IdTipoBloqueo" = filtros."IdTipoBloqueoPortafolio")
	AND (filtros."FiltroIdPropositoPortafolio" = 1 OR port."Proposito" = filtros."IdPropositoPortafolio")
	AND (filtros."FiltroIdEstadoPortafolio" = 1 OR port."IdEstado" = filtros."IdEstadoPortafolio")
	AND (filtros."FiltroIdPerfilComercial" = 1 OR port."IdPerfilComercialBaseOrigen" = filtros."IdPerfilComercial")
	AND (filtros."FiltroIdPerfilProductos" = 1 OR port."IdPerfilProductos" = filtros."IdPerfilProductos")
	AND (filtros."FiltroIdPerfilCobros" = 1 OR port."IdPerfilCobros" = filtros."IdPerfilCobros")
	AND (filtros."FiltroIdEmpleadoFiltro" = 1 OR port."IdEmpleadoEjecutivo" = filtros."IdEmpleadoFiltro")
	AND (filtros."FiltroFechaCreacionDesde" = 1 OR cli."FechaCreacion" >= filtros."FechaCreacionDesde")
	AND (filtros."FiltroFechaCreacionHasta" = 1 OR cli."FechaCreacion" <= filtros."FechaCreacionHasta")
	AND (filtros."FiltroFechaDefuncionDesde" = 1 OR per."FechaDefuncion" >= filtros."FechaDefuncionDesde")
	AND (filtros."FiltroFechaDefuncionHasta" = 1 OR per."FechaDefuncion" <= filtros."FechaDefuncionHasta")
	AND (filtros."FiltroFechaNacimientoDesde" = 1 OR per."FechaNacimiento" >= filtros."FechaNacimientoDesde") 
	AND (filtros."FiltroFechaNacimientoHasta" = 1 OR per."FechaNacimiento" <= filtros."FechaNacimientoHasta")
	AND (filtros."FiltroTipoEnrolamiento" = 1 OR cli."TipoEnrolamiento" = filtros."TipoEnrolamiento")
)
ORDER BY "NombreCompleto","CodigoPortafolio";