WITH CTE_CLIENTE AS (
		SELECT
			Cliente."IdPersona"								AS "IdPersona"
			, Cliente."IdCliente"							AS "IdCliente"
			, Cliente."IdSucursalPreferencia"				AS "IdSucursalPreferencia"
			, Cliente."IdPersonaEjecutivo"					AS "IdPersonaEjecutivo"
			, Cliente."IdEmpresa"							AS "IdEmpresa"
		FROM "BSCL.SocioNegocio.BD::CLI.Cliente" Cliente
    	WHERE Cliente."IdPersona" = :IdPersona
			AND Cliente."IdEmpresa" = :IdEmpresa
	),
     CTE_PORTAFOLIO AS (
		SELECT
			ROW_NUMBER() OVER (
				PARTITION BY Portafolio."IdCliente"
				ORDER BY Portafolio."IdPortafolio" ASC
			)																																						AS "OrdenRegistro"
			, Portafolio."IdPortafolio"																																AS "IdPortafolio"
			, Portafolio."IdCliente"																																AS "IdCliente"
			, Portafolio."CodigoPortafolio"																															AS "CodigoPortafolio"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresPortafolio."CodigoTipoIndicador" = 'ORDMODALIDAD' THEN VistaIndicadoresPortafolio."ValorIndicador" END), '{}')	AS "EstadoModalidadPorCodigoJSONString"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresPortafolio."CodigoTipoIndicador" = 'NOT_EMAIL' THEN VistaIndicadoresPortafolio."ValorIndicador" END), '')		AS "IdDireccionElectronicaNotificacionPorDefecto"
		FROM "BSCL.Portafolio.BD::POR.Portafolio" Portafolio
		    INNER JOIN CTE_CLIENTE CTECliente ON (CTECliente."IdCliente" = Portafolio."IdCliente")
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" VistaIndicadoresPortafolio ON (VistaIndicadoresPortafolio."IdPortafolio" = Portafolio."IdPortafolio")
		WHERE Portafolio."IdEstado" = 'ACT'
		GROUP BY Portafolio."IdPortafolio", Portafolio."IdCliente", Portafolio."CodigoPortafolio"
	),
	CTE_INDICADORES_CLIENTE AS (
		SELECT
			VistaIndicadoresCliente."IdCliente"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresCliente."CodigoTipoIndicador" = 'ANTLEGAL' THEN VistaIndicadoresCliente."ValorIndicador" END), '') 		AS "IndicadorAntecedentesLegales"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresCliente."CodigoTipoIndicador" = 'INVCALIF' THEN VistaIndicadoresCliente."ValorIndicador" END), '') 		AS "IndicadorInversionistaCalificado"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresCliente."CodigoTipoIndicador" = 'PEP' THEN VistaIndicadoresCliente."ValorIndicador" END), '') 				AS "IndicadorPersonaPoliticamenteExpuesta"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresCliente."CodigoTipoIndicador" = 'CATINV' THEN VistaIndicadoresCliente."ValorIndicador" END), '') 			AS "IndicadorCategoriaInversionista"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresCliente."CodigoTipoIndicador" = 'DCRSFAT' THEN VistaIndicadoresCliente."ValorIndicador" END), '') 			AS "IndicadorClienteFATCA"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresCliente."CodigoTipoIndicador" = 'INFPRV' THEN VistaIndicadoresCliente."ValorIndicador" END), '') 			AS "IndicadorClienteInfomacionPrivilegiada"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresCliente."CodigoTipoIndicador" = 'NCG380' THEN VistaIndicadoresCliente."CodigoValorIndicador" END), '') 	AS "IndicadorNGC380"
		FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" VistaIndicadoresCliente
		INNER JOIN CTE_CLIENTE CTECliente ON (CTECliente."IdCliente" = VistaIndicadoresCliente."IdCliente" AND CTECliente."IdEmpresa" = VistaIndicadoresCliente."IdEmpresa")
		GROUP BY VistaIndicadoresCliente."IdCliente"
	),
	CTE_INDICADORES_PORTAFOLIO AS (
		SELECT
			VistaIndicadoresPortafolio."IdCliente",
			VistaIndicadoresPortafolio."IdPortafolio"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresPortafolio."CodigoTipoIndicador" = 'ADMCART' THEN VistaIndicadoresPortafolio."ValorIndicador" END), '') 		AS "IndicadorAdministracionCartera"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresPortafolio."CodigoTipoIndicador" = 'TES_NIE' THEN VistaIndicadoresPortafolio."ValorIndicador" END), '') 		AS "IndicadorNeteaAbonosCargos"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresPortafolio."CodigoTipoIndicador" = 'ORDOCUSTRF' THEN VistaIndicadoresPortafolio."ValorIndicador" END), '') 	AS "IndicadorCustodiaRF"
			, COALESCE(MAX(CASE WHEN VistaIndicadoresPortafolio."CodigoTipoIndicador" = 'ORDOCUSTRV' THEN VistaIndicadoresPortafolio."ValorIndicador" END), '') 	AS "IndicadorCustodiaRV"
		FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" VistaIndicadoresPortafolio
		INNER JOIN CTE_PORTAFOLIO CTEPortafolio ON (CTEPortafolio."IdPortafolio" = VistaIndicadoresPortafolio."IdPortafolio" AND CTEPortafolio."OrdenRegistro" = 1)
		GROUP BY VistaIndicadoresPortafolio."IdCliente", VistaIndicadoresPortafolio."IdPortafolio"
	)
SELECT
	CTECliente."IdPersona"													AS "IdPersona"
	, CTECliente."IdCliente"												AS "IdCliente"
	, VistaEjecutivo."CodigoEmpleado"										AS "CodigoEjecutivo"
	, DatosEjecutivo."NombreCompleto"										AS "NombreEjecutivo"
	, DatosEjecutivo."ApellidoPaterno" 										AS "ApellidoPaternoEjecutivo"
	, DatosEjecutivo."ApellidoMaterno" 										AS "ApellidoMaternoEjecutivo"
	, DatosEjecutivo."DocumentoIdentidad"									AS "DocumentoIdentidadEjecutivo"
	, CTEPortafolio."IdPortafolio"											AS "IdPortafolio"
	, CTEPortafolio."CodigoPortafolio"										AS "CodigoPortafolio"
	, CTEPortafolio."EstadoModalidadPorCodigoJSONString"					AS "EstadoModalidadPorCodigoJSONString"
	, CTEPortafolio."IdDireccionElectronicaNotificacionPorDefecto"			AS "IdDireccionElectronicaNotificacionPorDefecto"
	, IFNULL(DireccionElectronicaNotificacion."Direccion", '')				AS "DireccionElectronicaNotificacionPorDefecto"
	, CTECliente."IdSucursalPreferencia"									AS "IdSucursalPreferencia"
	, Sucursal."CodigoSucursal"												AS "CodigoSucursalPreferencia"
	, Sucursal."NombreSucursal"												AS "NombreSucursalPreferencia"
	, CTEIndicadoresCliente."IndicadorAntecedentesLegales"					AS "IndicadorAntecedentesLegales"
	, CTEIndicadoresCliente."IndicadorInversionistaCalificado"				AS "IndicadorInversionistaCalificado"
	, CTEIndicadoresCliente."IndicadorPersonaPoliticamenteExpuesta"			AS "IndicadorPersonaPoliticamenteExpuesta"
	, CTEIndicadoresCliente."IndicadorCategoriaInversionista"				AS "IndicadorCategoriaInversionista"
	, CTEIndicadoresCliente."IndicadorClienteFATCA"							AS "IndicadorClienteFATCA"
	, CTEIndicadoresCliente."IndicadorClienteInfomacionPrivilegiada"		AS "IndicadorClienteInfomacionPrivilegiada"
	, CTEIndicadoresCliente."IndicadorNGC380"								AS "IndicadorNGC380"
	, CTEIndicadoresPortafolio."IndicadorAdministracionCartera"				AS "IndicadorAdministracionCartera"
	, CTEIndicadoresPortafolio."IndicadorNeteaAbonosCargos"					AS "IndicadorNeteaAbonosCargos"
	, CTEIndicadoresPortafolio."IndicadorCustodiaRF"						AS "IndicadorCustodiaRF"
	, CTEIndicadoresPortafolio."IndicadorCustodiaRV"						AS "IndicadorCustodiaRV"
	, COALESCE((LTRIM(RTRIM(SUBSTRING(:DocumentoIdentidad,1,LENGTH(:DocumentoIdentidad)-2)))),'') 	AS "FolioContrato"
FROM CTE_CLIENTE CTECliente
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" VistaEjecutivo ON (VistaEjecutivo."IdPersonaEjecutivo" = CTECliente."IdPersonaEjecutivo")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" DatosEjecutivo ON (DatosEjecutivo."IdPersona" = CTECliente."IdPersonaEjecutivo")
	LEFT JOIN CTE_PORTAFOLIO CTEPortafolio ON (CTEPortafolio."IdCliente" = CTECliente."IdCliente" AND CTEPortafolio."OrdenRegistro" = 1)
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DireccionElectronicaNotificacion ON (TO_NVARCHAR(DireccionElectronicaNotificacion."IdDireccionElectronica") = CTEPortafolio."IdDireccionElectronicaNotificacionPorDefecto")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" Sucursal ON (Sucursal."IdSucursal" = CTECliente."IdSucursalPreferencia" AND Sucursal."IdEmpresa" = CTECliente."IdEmpresa")
	LEFT JOIN CTE_INDICADORES_CLIENTE CTEIndicadoresCliente ON (CTEIndicadoresCliente."IdCliente" = CTECliente."IdCliente")
	LEFT JOIN CTE_INDICADORES_PORTAFOLIO CTEIndicadoresPortafolio ON (CTEIndicadoresPortafolio."IdCliente" = CTECliente."IdCliente");