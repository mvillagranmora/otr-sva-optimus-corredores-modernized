SELECT
	'D' AS "TipoRegistro" --Constante
	,'S80080' AS "CodigoAI" --Constante
	,CLIENTE."DocumentoIdentidad" AS "IdentificadorUnicoPersona"
	,'1' AS "TipoCodigoCliente" --Constante o no?
	,COALESCE(DOCUMENTO_RUT."DocumentoIdentidad",'') AS "CodigoCliente" -- Codigo Depositante
	,CASE WHEN COALESCE(INDICADOR_PR."ValorIndicador",'2') = '2' OR INDICADOR_PR."ValorIndicador" = '' THEN '2' ELSE '1' END AS "PrimeraRelacion"
	,CASE WHEN COALESCE(INDICADOR_SR."ValorIndicador",'2') = '2' OR INDICADOR_SR."ValorIndicador" = '' THEN '2' ELSE '1' END AS "SegundaRelacion"
	,CLIENTE."ApellidoPaterno" AS "ApellidoPaterno"
	,CLIENTE."ApellidoMaterno" AS "ApellidoMaterno"
	,CLIENTE."NombreCompleto" AS "Nombres"
	,CLIENTE."RazonSocial" AS "RazonSocial"
	,TO_CHAR(CLIENTE."FechaIngreso",'DDMMYYYY') AS "FechaInscripcionCliente"
	,COALESCE(DIRECCION_FISICA."NombreLargo",'') AS "Direccion"
	,CASE WHEN PAIS_RESIDENCIA."CodigoPais" = 'PE' THEN DIRECCION_FISICA."CodigoDivisionPais" ELSE '' END AS "Ubigeo"
	,COALESCE(HOMOLOGACION_PAIS_RESIDENCIA."ValorHomologacion",'000') AS "Pais"
	,COALESCE(TELEFONO."Numero",'') AS "Telefono"
	,CASE WHEN COALESCE(REPRESENTANTE_LEGAL."IdPersonaDestino",0) = 0 THEN '02'
		  ELSE CASE WHEN COALESCE(INDICADOR_PERSONA_RP."ValorIndicador",'') = '' OR INDICADOR_PERSONA_RP."ValorIndicador" = '2' THEN '01' 
		  	   ELSE '03' 
		  	   END
		  END AS "FlagIndicadorRepresentanteLegal"
	,COALESCE(CASE WHEN COALESCE(INDICADOR_TSR."ValorIndicador",'') != '' THEN CASE WHEN TO_BIGINT(INDICADOR_TSR."ValorIndicador") < 10 THEN '0' || INDICADOR_TSR."ValorIndicador" ELSE INDICADOR_TSR."ValorIndicador" END ELSE INDICADOR_TSR."ValorIndicador" END,'') AS "TipoPrimeraRelacion"
	,COALESCE(HOMOLOGACION_PAIS_NACIONALIDAD."ValorHomologacion",'000') AS "NacionalidadCliente"
	,COALESCE(DIRECCION_ELECTRONICA."Direccion",'') AS "DireccionElectronica"
	,COALESCE(PERSONA_RL."DocumentoIdentidad",'') AS "RepresentanteLegalIdentificadorUnicoPersona"
	,COALESCE(PERSONA_RL."RazonSocial",'') AS "RepresentanteLegalRazonSocial"
	,COALESCE(PERSONA_RL."ApellidoPaterno",'') AS "RepresentanteLegalApellidoPaterno"
	,COALESCE(PERSONA_RL."ApellidoMaterno",'') AS "RepresentanteLegalApellidoMaterno"
	,COALESCE(PERSONA_RL."RazonSocial",'') AS "RepresentanteLegalNombres"
	,COALESCE(DOCUMENTO_RUC."DocumentoIdentidad",'') AS "Ruc"
	,CASE WHEN CLIENTE."FechaNacimiento" = '0001-01-01' THEN '' ELSE COALESCE(TO_CHAR(CLIENTE."FechaNacimiento",'DDMMYYYY'),'') END AS "FechaNacimiento"
	,'D' AS "ActividadEconomica" 
	,CASE WHEN COALESCE(IND_PEP."ValorIndicador",'N') = 'N' THEN '3'
		  ELSE CASE WHEN COALESCE(IND_SEN_PEP."ValorIndicador",'N') = 'N' THEN '1'
		  	   ELSE '2'
		  	   END
	END AS "FlagClientePersonaExpuestaPoliticamente"
	,'' AS "NumeroCorrelativoBeneficiario" --Pendiente
	,'' AS "FechaFinal" --Pendiente
	,'' AS "TipoCliente" --Pendiente
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRECCION_FISICA ON (DIRECCION_FISICA."IdDireccionFisica" = CLIENTE."IdDireccionFisicaPrincipal")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELEFONO ON (TELEFONO."IdTelefono" = CLIENTE."IdTelefonoPrincipal")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIRECCION_ELECTRONICA ON (DIRECCION_ELECTRONICA."IdDireccionElectronica" = CLIENTE."IdDireccionElectronicaPrincipal")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_RUT ON (CLIENTE."IdPersona" = DOCUMENTO_RUT."IdPersona" AND DOCUMENTO_RUT."IdTipoDocumentoIdentidad" = 'RUT')
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_RUC ON (CLIENTE."IdPersona" = DOCUMENTO_RUC."IdPersona" AND DOCUMENTO_RUC."IdTipoDocumentoIdentidad" = 'RUC')
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PAIS_RESIDENCIA ON (PAIS_RESIDENCIA."IdPais" = CLIENTE."IdPaisResidencia")
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" HOMOLOGACION_PAIS_RESIDENCIA ON (HOMOLOGACION_PAIS_RESIDENCIA."IdCampo" = 'Pais' AND HOMOLOGACION_PAIS_RESIDENCIA."Valor" = PAIS_RESIDENCIA."CodigoPais")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PAIS_NACIONALIDAD ON (PAIS_NACIONALIDAD."IdPais" = CLIENTE."IdPaisNacionalidadPrincipal")
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" HOMOLOGACION_PAIS_NACIONALIDAD ON (HOMOLOGACION_PAIS_NACIONALIDAD."IdCampo" = 'Pais' AND HOMOLOGACION_PAIS_NACIONALIDAD."Valor" = PAIS_NACIONALIDAD."CodigoPais")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" INDICADOR_PR ON (INDICADOR_PR."IdPersona" = CLIENTE."IdPersona"
																							  AND INDICADOR_PR."IdEmpresa" = CLIENTE."IdEmpresa"
																							  AND INDICADOR_PR."CodigoTipoIndicador" = 'GENPRIMREL')
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" INDICADOR_SR ON (INDICADOR_SR."IdPersona" = CLIENTE."IdPersona"
																							  AND INDICADOR_SR."IdEmpresa" = CLIENTE."IdEmpresa"
																							  AND INDICADOR_SR."CodigoTipoIndicador" = 'GENSEGUREL')
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" INDICADOR_TSR ON (INDICADOR_TSR."IdPersona" = CLIENTE."IdPersona"
																							  AND INDICADOR_TSR."IdEmpresa" = CLIENTE."IdEmpresa"
																							  AND INDICADOR_TSR."CodigoTipoIndicador" = 'GENTIPPRIR')
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" REPRESENTANTE_LEGAL ON (REPRESENTANTE_LEGAL."IdPersonaOrigen" = CLIENTE."IdPersona" 
																							AND REPRESENTANTE_LEGAL."IdTipoRelacion" = 'REP' 
																							AND REPRESENTANTE_LEGAL."IdEstado" = 'ACT')
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" INDICADOR_PERSONA_RP ON (INDICADOR_PERSONA_RP."IdPersona" = REPRESENTANTE_LEGAL."IdPersonaDestino"
																								   AND INDICADOR_PERSONA_RP."IdEmpresa" = CLIENTE."IdEmpresa"
																								   AND INDICADOR_PERSONA_RP."CodigoTipoIndicador" = 'GENPRIMREL')
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA_RL ON (PERSONA_RL."IdPersona" = REPRESENTANTE_LEGAL."IdPersonaDestino")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND_PEP ON (IND_PEP."IdPersona" = CLIENTE."IdPersona"
																						 AND IND_PEP."IdEmpresa" = CLIENTE."IdEmpresa"
																						 AND IND_PEP."CodigoTipoIndicador" = 'PEP')
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND_SEN_PEP ON (IND_SEN_PEP."IdPersona" = CLIENTE."IdPersona"
																						 AND IND_SEN_PEP."IdEmpresa" = CLIENTE."IdEmpresa"
																						 AND IND_SEN_PEP."CodigoTipoIndicador" = 'SEN_PEP')
WHERE ((TO_CHAR(CLIENTE."FechaCreacion",'YYYY-MM-DD') = TO_CHAR(:FechaDesde,'YYYY-MM-DD')) OR (TO_CHAR(CLIENTE."FechaModificacion",'YYYY-MM-DD') = TO_CHAR(:FechaDesde,'YYYY-MM-DD')))
AND (:EsTodosClientes = 'S' OR CLIENTE."IdCliente" = :IdCliente)
AND CLIENTE."IdEmpresa" = :IdEmpresa;