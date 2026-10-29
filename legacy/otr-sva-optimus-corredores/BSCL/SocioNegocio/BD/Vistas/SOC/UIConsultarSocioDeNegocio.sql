SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio";
GO

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio"  AS
 SELECT 
 		SOC."IdSocioDeNegocio" 					as "IdSocioDeNegocio"
 		,SOC."IdEmpresa" 							as "IdEmpresa"
 		,SOC."IdPersona"							as "IdPersona"
 		,SOC."IdCliente"							as "IdCliente"
 		,SOC."IdEmpleado"							as "IdEmpleado"
 		,SOC."IdProveedor"							as "IdProveedor"
 		--,SOC."IdOperador"							as "IdOperador"
 		,SOC."IdContraparte"						as "IdContraparte"
        ,PER."FechaIngreso" 						as "FechaIngreso"
        ,PER."FechaCreacion"						as "FechaCreacion" 
        ,PER."FechaModificacion"  					as "FechaModificacion"
        ,TIDEN."IdTipoDocumentoIdentidad"			as "IdTipoDocumentoIdentidadDefecto"
        ,TIDEN."NombreTipoDocumentoIdentidad"		as "NombreTipoDocumentoIdentidadDefecto"
        ,TIDEN."NombreAbrTipoDocumentoIdentidad"	as "NombreAbrTipoDocumentoIdentidadDefecto"
        ,PER."DocumentoIdentidad"					as "DocumentoIdentidadDefecto" 	
        ,PER."Tipo"									as "CodigoTipoPersona"
        ,TPER."Descripcion"							as "TipoPersona"
        ,PER."NombreCompleto"						as "NombreCompleto"
        ,PER."RazonSocial" 							
        ,PER."Alias"
        ,PER."Nombres" 
        ,PER."ApellidoPaterno"         	
        ,PER."ApellidoMaterno"
        ,PER."FechaNacimiento"
        ,PER."IdPaisResidencia"
        ,PER."EstadoCivil"
        ,PAIS."CodigoPais" 							as "CodigoPaisResidencia"
        ,PAIS."NombrePais"							as "NombrePaisResidencia"
        ,CASE WHEN ( SOC."IdCliente" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END AS "EsCliente"
        ,CASE WHEN ( SOC."IdEmpleado" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END AS "EsEmpleado"
        --,CASE WHEN ( SOC."IdOperador" > 0 ) THEN 'UI_SI'  ELSE 'UI_NO'  END   AS "EsOperador"
        ,CASE WHEN ( SOC."IdProveedor" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END  AS "EsProveedor"
        ,CASE WHEN ( SOC."IdContraparte" > 0 ) THEN 'UI_SI' ELSE 'UI_NO' END  AS "EsContraparte"   
        -- ESTADOS
		-- IDS ESTADOS
		,PER."IdEstado"   as "EstadoPersona"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdCliente" > 0) THEN PER."IdEstado" ELSE CLI."IdEstadoTabla" END   as "EstadoCliente"  
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdEmpleado" > 0) THEN PER."IdEstado" ELSE EMP."IdEstado" END   as "EstadoEmpleado"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdProveedor" > 0 ) THEN PER."IdEstado" ELSE PRO."IdEstado" END   as "EstadoProveedor"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdContraparte" > 0) THEN PER."IdEstado" ELSE CNT."IdEstado" END   as "EstadoContraparte"	
		-- NOMBRES ESTADOS
		,ESTPER."Nombre"   as "NombreEstadoPersona"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdCliente" > 0) THEN ESTPER."Nombre" ELSE ESTCLI."Nombre" END   as "NombreEstadoCliente"  
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdEmpleado" > 0) THEN ESTPER."Nombre" ELSE ESTEMP."Nombre" END   as "NombreEstadoEmpleado"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdProveedor" > 0 ) THEN ESTPER."Nombre" ELSE ESTPRO."Nombre" END   as "NombreEstadoProveedor"
		,CASE WHEN ( PER."IdEstado" <> 'ACT' AND SOC."IdContraparte" > 0) THEN ESTPER."Nombre" ELSE ESTCNT."Nombre" END   as "NombreEstadoContraparte",
	    PER."FechaBloqueo",
 		PER."ComentarioBloqueo",
 		CLI."IdPersonaEjecutivo",
 		CLI."IdSegmentoCliente", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
 		CLI."NombreSegmentoCliente", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
		CLI."DescripcionSegmento", -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
		CLI."IdCategoriaInversionista",
		CLI."EsClienteFinal",
		EJECUTIVO."NombreCompleto" as "NombreCompletoEjecutivo", -- 09/05/2016 se requiere nombre del ejecutivo en el combo.
		CASE 
			WHEN EMPR."IdEmpresa" is null then 'UI_NO' else 'UI_SI' end 	as "EsCuentaPropia"	-- Se agrega indicador de cuenta propia

 FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON SOC."IdPersona" = PER."IdPersona"
 INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as TIDEN ON  PER."TipoDocumentoIdentidad" = TIDEN."IdTipoDocumentoIdentidad" 
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" as TPER ON TPER."IdTipoPersona" = PER."Tipo"
 LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as PAIS ON PAIS."IdPais" = PER."IdPaisResidencia"
 --LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdPersona" = PER."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI ON CLI."IdPersona" = PER."IdPersona" -- se pidio columna Segmento Cliente en Socio Negocio - mmachmar 09/05/2016
 LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON EMP."IdPersona" = PER."IdPersona"
 --LEFT JOIN "BSCL.SocioNegocio.BD::OPE.Operador" as OPE ON OPE."IdPersona" = PER."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" as PRO ON PRO."IdPersona" = PER."IdPersona" 
 LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" as CNT ON CNT."IdPersona" = PER."IdPersona"  
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" EJECUTIVO ON (EJECUTIVO."IdPersona" = CLI."IdPersonaEjecutivo")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTPER ON (ESTPER."IdEstado" = PER."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCLI ON (ESTCLI."IdEstado" = CLI."IdEstadoTabla")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTEMP ON (ESTEMP."IdEstado" = EMP."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTPRO ON (ESTPRO."IdEstado" = PRO."IdEstado")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCNT ON (ESTCNT."IdEstado" = CNT."IdEstado") 

 LEFT JOIN  "BSCL.Plataforma.BD::PLA.Empresa"AS EMPR  ON (EMPR."IdSocioDeNegocio" = SOC."IdSocioDeNegocio")
 --WHERE PER."IdCarga"=0 -- TEMPORAL!
;
--WITH READ ONLY; 
GO

GRANT SELECT ON  "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
