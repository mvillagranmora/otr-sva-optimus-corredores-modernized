SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterConsultarSocioDeNegocio";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterConsultarSocioDeNegocio"  AS
SELECT 
 		SOC."IdSocioDeNegocio" 							AS "IdSocioDeNegocio"
 		,SOC."IdEmpresa" 								AS "IdEmpresa"
 		,SOC."IdPersona"								AS "IdPersona"
 		,SOC."IdCliente"								AS "IdCliente"
 		,SOC."IdEmpleado"								AS "IdEmpleado"
 		,SOC."IdProveedor"								AS "IdProveedor"
 		--,SOC."IdOperador"								AS "IdOperador"
 		,SOC."IdContraparte"							AS "IdContraparte"
        ,PER."FechaIngreso" 							AS "FechaIngreso"
        ,PER."FechaCreacion"							AS "FechaCreacion" 
        ,PER."FechaModificacion"  						AS "FechaModificacion"
        ,TIDEN."IdTipoDocumentoIdentidad"				AS "IdTipoDocumentoIdentidadDefecto"
        ,TIDEN."NombreTipoDocumentoIdentidad"			AS "NombreTipoDocumentoIdentidadDefecto"
        ,TIDEN."NombreAbrTipoDocumentoIdentidad"		AS "NombreAbrTipoDocumentoIdentidadDefecto"
        ,TIDENADIC."IdTipoDocumentoIdentidad"			AS "IdTipoDocumentoIdentidadAdicional"
        ,TIDENADIC."NombreTipoDocumentoIdentidad"		AS "NombreTipoDocumentoIdentidadAdicional"
        ,TIDENADIC."NombreAbrTipoDocumentoIdentidad"	AS "NombreAbrTipoDocumentoIdentidadAdicional"
        ,PER."DocumentoIdentidad"						AS "DocumentoIdentidadDefecto" 	
        ,PER."DocumentoIdentidadAdicional"				AS "DocumentoIdentidadAdicional" 	
        ,PER."Tipo"										AS "CodigoTipoPersona"
        ,TPER."Descripcion"								AS "TipoPersona"
        ,PER."NombreCompleto"							AS "NombreCompleto"
        ,PER."RazonSocial" 							
        ,PER."Alias"
        ,PER."Nombres" 
        ,PER."ApellidoPaterno"         	
        ,PER."ApellidoMaterno"
        ,PER."FechaNacimiento"
        ,PER."IdPaisResidencia"
        ,PER."EstadoCivil"
        ,PAIS."CodigoPais" 								AS "CodigoPaisResidencia"
        ,PAIS."NombrePais"								AS "NombrePaisResidencia"
        ,'UI_SI'										AS "EsPersona"
        ,CASE WHEN ( SOC."IdCliente" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END 		AS "EsCliente"
        ,CASE WHEN ( SOC."IdEmpleado" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END 		AS "EsEmpleado"
        ,CASE WHEN ( SOC."IdProveedor" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END  	AS "EsProveedor"
        ,CASE WHEN ( SOC."IdContraparte" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END  	AS "EsContraparte"   
        -- ID ESTADOS
		,PER."IdEstado"   								AS "EstadoPersona"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdCliente" > 0) THEN PER."IdEstado" ELSE CLI."IdEstadoTabla" END  AS "EstadoCliente"  
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdEmpleado" > 0) THEN PER."IdEstado" ELSE EMP."IdEstado" END   	AS "EstadoEmpleado"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdProveedor" > 0 ) THEN PER."IdEstado" ELSE PRO."IdEstado" END   	AS "EstadoProveedor"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdContraparte" > 0) THEN PER."IdEstado" ELSE CNT."IdEstado" END   AS "EstadoContraparte"	
		-- NOMBRES ESTADOS
		,ESTPER."Nombre"   								AS "NombreEstadoPersona"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdCliente" > 0) THEN ESTPER."Nombre" ELSE ESTCLI."Nombre" END   	AS "NombreEstadoCliente"  
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdEmpleado" > 0) THEN ESTPER."Nombre" ELSE ESTEMP."Nombre" END   	AS "NombreEstadoEmpleado"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdProveedor" > 0 ) THEN ESTPER."Nombre" ELSE ESTPRO."Nombre" END  AS "NombreEstadoProveedor"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdContraparte" > 0) THEN ESTPER."Nombre" ELSE ESTCNT."Nombre" END AS "NombreEstadoContraparte",
	    PER."FechaBloqueo",
 		PER."ComentarioBloqueo",
 		PER."IdTipoBloqueo",
 		BLO."NombreTipoBloqueo",
 		CLI."IdPersonaEjecutivo",
 		CLI."IdSegmentoCliente", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
 		CLI."NombreSegmentoCliente", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
		CLI."DescripcionSegmento", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
		EJECUTIVO."NombreCompleto" 						AS "NombreCompletoEjecutivo", -- 09/05/2016 se requiere nombre del ejecutivo en el combo.
		CALCDOCID."ListaDocumentosIdentidad",		
		CASE WHEN (CLI."EsClienteFinal" = 'S' ) THEN 'UI_SI' ELSE 'UI_NO' END AS "EsClienteFinal",
		CLI."TipoEnrolamiento"
 FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON SOC."IdPersona" = PER."IdPersona"
 INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS TIDEN ON  PER."TipoDocumentoIdentidad" = TIDEN."IdTipoDocumentoIdentidad" 
 LEFT JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS TIDENADIC ON  PER."TipoDocumentoIdentidadAdicional" = TIDENADIC."IdTipoDocumentoIdentidad" --PROY PERU
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS TPER ON TPER."IdTipoPersona" = PER."Tipo"
 LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON PAIS."IdPais" = PER."IdPaisResidencia"
 --LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdPersona" = PER."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteUI" AS CLI ON CLI."IdCliente" = SOC."IdCliente" -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
 LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON EMP."IdPersona" = PER."IdPersona" AND EMP."Borrado"='N'
 --LEFT JOIN "BSCL.SocioNegocio.BD::OPE.Operador" as OPE ON OPE."IdPersona" = PER."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" AS PRO ON PRO."IdPersona" = PER."IdPersona" 
 LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" AS CNT ON CNT."IdPersona" = PER."IdPersona"  
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" EJECUTIVO ON (EJECUTIVO."IdPersona" = CLI."IdPersonaEjecutivo")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTPER ON (ESTPER."IdEstado" = PER."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCLI ON (ESTCLI."IdEstado" = CLI."IdEstadoTabla")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTEMP ON (ESTEMP."IdEstado" = EMP."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTPRO ON (ESTPRO."IdEstado" = PRO."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCNT ON (ESTCNT."IdEstado" = CNT."IdEstado") 
 LEFT JOIN 
	(select "IdPersona", STRING_AGG("DocumentoIdentidad", ' ')  AS "ListaDocumentosIdentidad"
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
	GROUP  BY "IdPersona") AS CALCDOCID
	ON CALCDOCID."IdPersona" = SOC."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" AS BLO
	ON PER."IdTipoBloqueo" = BLO."IdTipoBloqueo" AND BLO."IdTipoEntidad" = 'PERSONA'
;
--WITH READ ONLY; 
GRANT SELECT ON  "BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterConsultarSocioDeNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
