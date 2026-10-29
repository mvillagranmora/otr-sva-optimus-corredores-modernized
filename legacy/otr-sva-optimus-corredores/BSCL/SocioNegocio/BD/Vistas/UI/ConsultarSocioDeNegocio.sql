---------------------------------------------------------------
-- VISTA PARA bcs-combobox-socio-negocio
-- NO UTILIZAR EN EL NEGOCIO
---------------------------------------------------------------


SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio";
GO 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio"  AS
 SELECT 
 		BASICOS."IdSocioDeNegocio" 					as "IdSocioDeNegocio"
 		,BASICOS."IdEmpresa" 						as "IdEmpresa"
 		,BASICOS."IdPersona"						as "IdPersona"
 		,BASICOS."IdCliente"						as "IdCliente"
 		,BASICOS."IdEmpleado"						as "IdEmpleado"
 		,BASICOS."IdProveedor"						as "IdProveedor"
 		,BASICOS."IdContraparte"					as "IdContraparte"
        ,BASICOS."FechaIngreso" 					as "FechaIngreso"
        ,BASICOS."FechaCreacion"					as "FechaCreacion" 
        ,BASICOS."FechaModificacion"  				as "FechaModificacion"
        ,TIDEN."IdTipoDocumentoIdentidad"			as "IdTipoDocumentoIdentidadDefecto"
        ,TIDEN."NombreTipoDocumentoIdentidad"		as "NombreTipoDocumentoIdentidadDefecto"
        ,TIDEN."NombreAbrTipoDocumentoIdentidad"	as "NombreAbrTipoDocumentoIdentidadDefecto"
        ,BASICOS."DocumentoIdentidad"				as "DocumentoIdentidadDefecto" 	
        ,BASICOS."Tipo"								as "CodigoTipoPersona"
        ,TPER."Descripcion"							as "TipoPersona"
        ,BASICOS."NombreCompleto"					as "NombreCompleto"
        ,BASICOS."EsNombreFantasia"
        ,BASICOS."RazonSocial" 							
        ,BASICOS."Alias"
        ,BASICOS."Nombres" 
        ,BASICOS."ApellidoPaterno"         	
        ,BASICOS."ApellidoMaterno"
        ,BASICOS."FechaNacimiento"
        ,BASICOS."IdPaisResidencia"
        ,BASICOS."EstadoCivil"
        ,PAIS."CodigoPais" 							as "CodigoPaisResidencia"
        ,PAIS."NombrePais"							as "NombrePaisResidencia"
        ,CASE WHEN ( BASICOS."IdCliente" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END AS "EsCliente"
        ,CASE WHEN ( BASICOS."IdEmpleado" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END AS "EsEmpleado"
        --,CASE WHEN ( SOC."IdOperador" > 0 ) THEN 'UI_SI'  ELSE 'UI_NO'  END   AS "EsOperador"
        ,CASE WHEN ( BASICOS."IdProveedor" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END  AS "EsProveedor"
        ,CASE WHEN ( BASICOS."IdContraparte" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END  AS "EsContraparte"   
        -- ESTADOS
		-- IDS ESTADOS
		,BASICOS."IdEstado"   as "EstadoPersona"
		,CASE WHEN ( BASICOS."IdEstado" <> 'ACT' AND BASICOS."IdCliente" > 0) THEN BASICOS."IdEstado" ELSE CLI."IdEstadoTabla" END   as "EstadoCliente"  
		,CASE WHEN ( BASICOS."IdEstado" <> 'ACT' AND BASICOS."IdEmpleado" > 0) THEN BASICOS."IdEstado" ELSE EMP."IdEstado" END   as "EstadoEmpleado"
		,CASE WHEN ( BASICOS."IdEstado" <> 'ACT' AND BASICOS."IdProveedor" > 0 ) THEN BASICOS."IdEstado" ELSE PRO."IdEstado" END   as "EstadoProveedor"
		,CASE WHEN ( BASICOS."IdEstado" <> 'ACT' AND BASICOS."IdContraparte" > 0) THEN BASICOS."IdEstado" ELSE CNT."IdEstado" END   as "EstadoContraparte"	
		-- NOMBRES ESTADOS
		,ESTPER."Nombre"   as "NombreEstadoPersona"
		,CASE WHEN ( BASICOS."IdEstado" <> 'ACT' AND BASICOS."IdCliente" > 0) THEN ESTPER."Nombre" ELSE ESTCLI."Nombre" END   as "NombreEstadoCliente"  
		,CASE WHEN ( BASICOS."IdEstado" <> 'ACT' AND BASICOS."IdEmpleado" > 0) THEN ESTPER."Nombre" ELSE ESTEMP."Nombre" END   as "NombreEstadoEmpleado"
		,CASE WHEN ( BASICOS."IdEstado" <> 'ACT' AND BASICOS."IdProveedor" > 0 ) THEN ESTPER."Nombre" ELSE ESTPRO."Nombre" END   as "NombreEstadoProveedor"
		,CASE WHEN ( BASICOS."IdEstado" <> 'ACT' AND BASICOS."IdContraparte" > 0) THEN ESTPER."Nombre" ELSE ESTCNT."Nombre" END   as "NombreEstadoContraparte",
	    BASICOS."FechaBloqueo",
 		BASICOS."ComentarioBloqueo",
 		--BASICOS."IdTipoBloqueo",
 		CLI."IdPersonaEjecutivo",
 		CLI."IdSegmentoCliente", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
 		CLI."NombreSegmentoCliente", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
		CLI."DescripcionSegmento", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
		CLI."IdCategoriaInversionista",
		EJECUTIVO."NombreCompleto" as "NombreCompletoEjecutivo", -- 09/05/2016 se requiere nombre del ejecutivo en el combo.
		CASE 
			WHEN EMPR."IdEmpresa" is null then 'UI_NO' else 'UI_SI' end 	as "EsCuentaPropia"	-- Se agrega indicador de cuenta propia
		
	
		,COALESCE(INDCUMP."ValorIndicador",'N') AS "BloqueadoPorCumplimiento"
			
			
 FROM
 (
 	SELECT PER."TipoDocumentoIdentidad",PER."Tipo",SOC."IdSocioDeNegocio",PER."IdPaisResidencia",PER."IdPersona",PER."IdEstado",SOC."IdEmpresa",SOC."IdCliente",
 			SOC."IdEmpleado",SOC."IdProveedor",SOC."IdContraparte",PER."FechaIngreso",PER."FechaBloqueo",PER."ComentarioBloqueo",PER."FechaCreacion",PER."FechaModificacion",
 			PER."DocumentoIdentidad",PER."NombreCompleto",PER."RazonSocial",PER."Alias",PER."Nombres",PER."ApellidoPaterno",PER."ApellidoMaterno",PER."FechaNacimiento",
 			PER."EstadoCivil", 'N' AS "EsNombreFantasia"
	 FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC
	 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON SOC."IdPersona" = PER."IdPersona"
 ) AS BASICOS
 
 INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as TIDEN ON  BASICOS."TipoDocumentoIdentidad" = TIDEN."IdTipoDocumentoIdentidad" 
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" as TPER ON TPER."IdTipoPersona" = BASICOS."Tipo"
 LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as PAIS ON PAIS."IdPais" = BASICOS."IdPaisResidencia"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI ON CLI."IdPersona" = BASICOS."IdPersona" -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
 LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON EMP."IdPersona" = BASICOS."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" as PRO ON PRO."IdPersona" = BASICOS."IdPersona" 
 LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" as CNT ON CNT."IdPersona" = BASICOS."IdPersona"  
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" EJECUTIVO ON (EJECUTIVO."IdPersona" = CLI."IdPersonaEjecutivo")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTPER ON (ESTPER."IdEstado" = BASICOS."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCLI ON (ESTCLI."IdEstado" = CLI."IdEstadoTabla")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTEMP ON (ESTEMP."IdEstado" = EMP."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTPRO ON (ESTPRO."IdEstado" = PRO."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCNT ON (ESTCNT."IdEstado" = CNT."IdEstado") 

 LEFT JOIN  "BSCL.Plataforma.BD::PLA.Empresa"AS EMPR  ON (EMPR."IdSocioDeNegocio" = BASICOS."IdSocioDeNegocio")
 
 

 LEFT JOIN 
 ( 
  	SELECT IND."IdEntidad", IND."ValorIndicador"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
	INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND ON  (IND."IdTipoIndicador" = TIND."IdTipoIndicador" AND IND."IdTipoEntidad" = TIND."IdTipoEntidad")
	WHERE TIND."IdTipoEntidad" 		= 'CLIENTE' 
	AND TIND."CodigoTipoIndicador" 	= 'BCCUM' -- Bloqueo por cumplimiento
	AND TIND."IdEstado" 			= 'ACT'
 ) AS INDCUMP ON (INDCUMP."IdEntidad" = BASICOS."IdCliente")
  
  

;
--WITH READ ONLY; 
GO 
GRANT SELECT ON  "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
