WITH CTE_PERSONA AS (
		SELECT
			Persona."IdPersona"
			, Persona."NombreCompleto"
			, Persona."Nombres"
			, Persona."ApellidoMaterno"
			, Persona."ApellidoPaterno"
		    , Persona."RazonSocial"
			, Persona."DocumentoIdentidad"
			, CASE
				WHEN Persona."TipoDocumentoIdentidad" = 'R' THEN COALESCE(SUBSTRING(Persona."DocumentoIdentidad", LOCATE(Persona."DocumentoIdentidad", '-') + 1, LENGTH(Persona."DocumentoIdentidad")), '')
				ELSE ''
			  END AS "DigitoVerificador"
			, Persona."IdPersonaConyuge"
			, Persona."EstadoCivil"
			, Persona."Tipo"
			, Persona."FechaIngreso"
			, Persona."FechaNacimiento"
			, Persona."Sexo"
			, Persona."RegimenConyugal"
			, Persona."IdEstado"
			, Persona."IdGiroPrincipal"
			, Persona."IdDireccionFisicaPrincipal"
			, Persona."IdDireccionElectronicaPrincipal"
			, Persona."IdTelefonoPrincipal"
			, Persona."IdPaisNacionalidadPrincipal"
			, Persona."IdPaisNacimiento"
			, Persona."IdPaisResidencia"
			, Persona."IdSituacionLaboral"
			, Persona."GlobalLegalEntity"
			, Persona."IdNombreFantasiaPrincipal"
			, Persona."CodigoTipoCargoEmpleado"
			, Profesion."Nombre" AS "Profesion"
			, Renta."Descripcion" AS "RangoRenta"
			, Venta."Descripcion" AS "RangoNivelVenta"
			, PropositoTransaccion."Descripcion" AS "PropositoTransaccion"
		FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" Persona
			LEFT JOIN "BSCL.Plataforma.BD::REG.Profesion" Profesion ON Profesion."IdProfesion" = Persona."IdProfesion" 
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.RangoSeleccion" Renta ON Renta."CodigoRangoSeleccion" = Persona."CodigoRangoRenta"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.RangoSeleccion" Venta ON Venta."CodigoRangoSeleccion" = Persona."CodigoRangoNivelVenta"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.RangoSeleccion" PropositoTransaccion ON PropositoTransaccion."CodigoRangoSeleccion" = Persona."CodigoPropositoTransaccion"
		WHERE Persona."IdPersona" IN (:PersonaIds)
	),
	CTE_TELEFONO AS (
		SELECT
			ROW_NUMBER() OVER (
				PARTITION BY Telefono."IdPersona", Telefono."IdTipoTelefono"
				ORDER BY Telefono."IdTelefono"
			)									AS "OrdenRegistro"
			, Telefono."IdPersona"				AS "IdPersona"
			, Telefono."IdTipoTelefono"			AS "IdTipoTelefono"
			, Telefono."Numero"					AS "Numero"
		FROM "BSCL.SocioNegocio.BD::PER.Telefono" Telefono
			INNER JOIN CTE_PERSONA Persona ON (Persona."IdPersona" = Telefono."IdPersona")
		WHERE Telefono."IdTipoTelefono" IN ('FIJO', 'MOVIL')
	),
	CTE_IND_MERCANTIL AS (
 		select 
 			INDPER."IdPersona" 
 			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'BCOCARGO1_1'  then INDPER."NombreValorIndicador" else '' end), '') as  "BCOCARGO1_1"  
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'NCTACARGO1'  then  INDPER."ValorIndicador" else '' end), '') as  "NCTACARGO1" 
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'TCTACARGO1'  then  INDPER."NombreValorIndicador" else '' end), '') as  "TCTACARGO1" 
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'BCOCARGO2_2'  then INDPER."NombreValorIndicador" else '' end), '') as  "BCOCARGO2_2"
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'NCTACARGO2'  then  INDPER."ValorIndicador" else '' end), '') as  "NCTACARGO2" 
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'TCTACARGO2'  then  INDPER."NombreValorIndicador" else '' end), '') as  "TCTACARGO2" 
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'BCOCARGO3_3' then  INDPER."NombreValorIndicador" else '' end), '') as  "BCOCARGO3_3"
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'NCTACARGO3'  then  INDPER."ValorIndicador" else '' end), '') as  "NCTACARGO3" 
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'TCTACARGO3'  then  INDPER."NombreValorIndicador" else '' end), '') as  "TCTACARGO3" 
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'BCOCARGO4_4' then  INDPER."NombreValorIndicador" else '' end), '') as  "BCOCARGO4_4"
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'NCTACARGO4'  then  INDPER."ValorIndicador" else '' end), '') as  "NCTACARGO4" 
			,COALESCE(MAX(case when INDPER."CodigoTipoIndicador" = 'TCTACARGO4'  then  INDPER."NombreValorIndicador" else '' end), '') as  "TCTACARGO4" 
		from "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" INDPER
		WHERE INDPER."GrupoIndicador" = 'CUENTA PARA CARGAR MANDATO MERCANTIL'
		AND INDPER."IdPersona" IN (:PersonaIds)
		GROUP BY INDPER."IdPersona"
	),
	CTE_DIRECCION_FISICA AS (
		SELECT
			ROW_NUMBER() OVER (
				PARTITION BY DireccionFisica."IdPersona", DireccionFisica."CodigoProposito"
				ORDER BY DireccionFisica."IdDireccionFisica", DireccionFisica."Predeterminado" DESC
			)																							AS "OrdenRegistro"
			, DireccionFisica."IdPersona"																AS "IdPersona"
			, DireccionFisica."CodigoProposito"															AS "CodigoProposito"
			, DireccionFisica."Calle" || COALESCE(' ' || COALESCE(DireccionFisica."Numero", ''), '')	AS "Direccion"
			, DireccionFisica."NombreCiudad" 															AS "DireccionNombreCiudad"
			, DireccionFisica."NombreDivisionPais"														AS "DireccionNombreComuna"
			, PaisRegionDireccionFisica."NombreClasificacionDivisionPais1"								AS "DireccionNombreRegion"
			, PaisRegionDireccionFisica."NombrePais"													AS "DireccionNombrePais"
		FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DireccionFisica
			INNER JOIN CTE_PERSONA Persona ON (Persona."IdPersona" = DireccionFisica."IdPersona")
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" PaisRegionDireccionFisica ON (DireccionFisica."CodigoDivisionPais" = PaisRegionDireccionFisica."CodigoDivisionPais")
		WHERE DireccionFisica."IdEstado" = 'ACT'
	),
	CTE_DIRECCION_ELECTRONICA AS (
		SELECT
			ROW_NUMBER() OVER (
				PARTITION BY DireccionElectronica."IdPersona", DireccionElectronica."CodigoProposito"
				ORDER BY DireccionElectronica."IdDireccionElectronica", DireccionElectronica."Predeterminado" DESC
			)																	AS "OrdenRegistro"
			, DireccionElectronica."IdPersona"									AS "IdPersona"
			, DireccionElectronica."CodigoProposito"							AS "CodigoProposito"
			, DireccionElectronica."Direccion"									AS "Direccion"
		FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DireccionElectronica
			INNER JOIN CTE_PERSONA Persona ON (Persona."IdPersona" = DireccionElectronica."IdPersona")
		WHERE DireccionElectronica."IdEstado" = 'ACT'
	),
	CTE_INDICADORES_PERSONA AS (
		SELECT
			VistaIndicadoresPersona."IdPersona"
			 , COALESCE(MAX(CASE WHEN VistaIndicadoresPersona."CodigoTipoIndicador" = 'NCG380PER' THEN VistaIndicadoresPersona."CodigoValorIndicador" END), '') AS "IndicadorNCG380"
			 , COALESCE(MAX(CASE WHEN VistaIndicadoresPersona."CodigoTipoIndicador" = 'KYC' THEN VistaIndicadoresPersona."ValorIndicador" END), '') AS "IndicadorKnowYourCustomer"
		FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" VistaIndicadoresPersona
			 INNER JOIN CTE_PERSONA CTEPersona ON (CTEPersona."IdPersona" = VistaIndicadoresPersona."IdPersona")
		GROUP BY VistaIndicadoresPersona."IdPersona"
	),
	CTE_REPRESENTANTE AS (
		SELECT
			  RelacionPersona."IdPersonaDestino"
			 ,RelacionPersona."Calle"
			 ,RelacionPersona."Numero"
			 ,RelacionPersona."Ciudad"
			 ,RelacionPersona."Telefono"
			 ,RelacionPersona."Email"
		FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" RelacionPersona
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PersonaOrigen ON  (PersonaOrigen."IdPersona" = RelacionPersona."IdPersonaOrigen")
		WHERE (RelacionPersona."IdTipoRelacion"='REP' OR RelacionPersona."IdTipoRelacion"='APO') AND PersonaOrigen."DocumentoIdentidad" = :DocumentoIdentidad  -- Apoderado o Representante Legal trae los datos de la tabla relacionPersona		
	),
	CTE_AUTORIZADO AS (
		SELECT "IdPersona", "IdPersonaDestino", "Email" 	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" A
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" B ON (b."IdPersonaOrigen" = A."IdPersona" )
		WHERE A."DocumentoIdentidad" =   :DocumentoIdentidad AND B."IdTipoRelacion" = 'AUTZDO'
	)
	
SELECT
	  CTEPersona."IdPersona"															AS "IdPersona"
	, COALESCE(CTEPersona."NombreCompleto", '')											AS "NombreCompleto"
	, COALESCE(CTEPersona."Nombres", '')												AS "Nombres"
	, COALESCE(CTEPersona."ApellidoMaterno", '')										AS "ApellidoMaterno"
	, COALESCE(CTEPersona."ApellidoPaterno", '')										AS "ApellidoPaterno"
	, COALESCE(CTEPersona."RazonSocial", '')											AS "RazonSocial"
	, COALESCE(CTEPersona."DocumentoIdentidad", '')										AS "DocumentoIdentidad"
	, COALESCE(CTEPersona."DigitoVerificador", '')										AS "DigitoVerificador"
	, COALESCE(CTEPersona."Profesion", '')												AS "Profesion"
	, COALESCE(GiroPrincipal."Nombre", '')												AS "NombreGiro"
	, COALESCE(Representante."Email",COALESCE(DireccionElectronica."Direccion", '')) 	AS "DireccionElectronicaPrincipal"
	, COALESCE(DireccionElectronicaLaboral."Direccion", '')								AS "DireccionElectronicaLaboral"
	, COALESCE(DireccionElectronicaComercial."Direccion", '')							AS "DireccionElectronicaComercialPersona"
	, COALESCE(CTEPersona."IdPersonaConyuge", '0')										AS "IdPersonaConyuge"
	, COALESCE(Representante."Calle",COALESCE(DireccionFisica."Calle", ''))				AS "DireccionFisicaCalle"
	, COALESCE(Representante."Numero",COALESCE(DireccionFisica."Numero", ''))			AS "DireccionFisicaNumero"
	, COALESCE(Representante."Ciudad",COALESCE(DireccionFisica."NombreCiudad", ''))		AS "DireccionFisicaNombreCiudad"
	, COALESCE(DireccionFisica."NombreDivisionPais", '')								AS "DireccionFisicaNombreComuna"
	, COALESCE(RegionDireccionFisica."NombreClasificacionDivisionPais1", '')			AS "DireccionFisicaNombreRegion"
	, COALESCE(DireccionFisica."NombrePais", '')										AS "DireccionFisicaNombrePais"
	, COALESCE(DireccionFisicaComercial."Direccion", '')								AS "DireccionFisicaComercial"
	, COALESCE(DireccionFisicaComercial."DireccionNombreCiudad", '')					AS "DireccionFisicaComercialNombreCiudad"
	, COALESCE(DireccionFisicaComercial."DireccionNombreComuna", '')					AS "DireccionFisicaComercialNombreComuna"
	, COALESCE(DireccionFisicaComercial."DireccionNombreRegion", '')					AS "DireccionFisicaComercialNombreRegion"
	, COALESCE(DireccionFisicaComercial."DireccionNombrePais", '')						AS "DireccionFisicaComercialNombrePais"
	, COALESCE(DireccionFisicaLaboral."Direccion", '')									AS "DireccionFisicaLaboral"
	, COALESCE(DireccionFisicaLaboral."DireccionNombreCiudad", '')						AS "DireccionFisicaLaboralNombreCiudad"
	, COALESCE(DireccionFisicaLaboral."DireccionNombreComuna", '')						AS "DireccionFisicaLaboralNombreComuna"
	, COALESCE(DireccionFisicaLaboral."DireccionNombreRegion", '')						AS "DireccionFisicaLaboralNombreRegion"
	, COALESCE(DireccionFisicaLaboral."DireccionNombrePais", '')						AS "DireccionFisicaLaboralNombrePais"
	, COALESCE(PaisNacionalidadPrincipal."NombrePais", '')								AS "NombrePaisNacionalidadPrincipal"
	, COALESCE(PaisNacimiento."NombrePais", '')											AS "NombrePaisNacimiento"
	, COALESCE(PaisResidencia."NombrePais", '')											AS "NombrePaisResidencia"
	, COALESCE(TelefonoPrincipal."IdTipoTelefono", '')									AS "TipoTelefonoPrincipal"
	, COALESCE(TelefonoPrincipal."Numero", '')											AS "NumeroTelefonoPrincipal"
	, COALESCE(TelefonoFijo."Numero", '')												AS "NumeroTelefonoFijo"
	, COALESCE(Representante."Telefono",COALESCE(TelefonoMovil."Numero", ''))			AS "NumeroTelefonoMovil"
	, COALESCE(TipoEstadoCivil."Nombre", '')											AS "EstadoCivil"
	, COALESCE(CTEPersona."EstadoCivil", '')											AS "CodigoEstadoCivil"
	, COALESCE(CTEPersona."Tipo", '')													AS "TipoPersona"
	, COALESCE(TO_NVARCHAR(CTEPersona."FechaIngreso", 'YYYY-MM-DD'), '')				AS "FechaIngreso"
	, COALESCE(TO_NVARCHAR(CTEPersona."FechaIngreso", 'HH:MM:SS'), '')					AS "HoraIngreso"
	, COALESCE(TO_NVARCHAR(CTEPersona."FechaNacimiento", 'YYYY-MM-DD'), '')				AS "FechaNacimiento"
	, COALESCE(Genero."Descripcion", '')												AS "Sexo"
	, COALESCE(CTEPersona."Sexo", '')													AS "SexoCorto"
	, COALESCE(CTEPersona."RegimenConyugal", '')										AS "RegimenConyugal"
	, COALESCE(TipoRegimenConyugal."Nombre", '')										AS "NombreRegimenConyugal"
	, COALESCE(CTEPersona."IdEstado", '')												AS "IdEstado"
	, COALESCE(PaisDireccionFisica."NombrePais", '')									AS "NombrePaisDireccionFisica"
	, COALESCE(PaisResidencia."NombrePais", '')											AS "NombrePaisResidencia"
	, COALESCE(CTEIndicadoresPersona."IndicadorNCG380", '')								AS "IndicadorNCG380"
	, COALESCE(CTEIndicadoresPersona."IndicadorKnowYourCustomer", '')					AS "IndicadorKnowYourCustomer"
	, COALESCE(SituacionLaboral."SituacionLaboral", '')									AS "IdSituacionLaboral"
	, COALESCE(NombreFantasia."DescripcionNombreFantasia", '')							AS "NombreFantasia"
	, COALESCE(CTEPersona."GlobalLegalEntity", '')										AS "GlobalLegalEntity"
	, COALESCE(Cargo."NombreTipoCargoPersona", '')										AS "Cargo" 	
 	, COALESCE(DireccionComercial."Email", DireccionElectronicaComercial."Direccion", '')	AS "DireccionElectronicaComercial"
	, COALESCE(CTEPersona."RangoRenta", '')										        AS "RangoRenta"
	, COALESCE(CTEPersona."RangoNivelVenta", '')										AS "RangoNivelVenta"
	, COALESCE(CTEPersona."PropositoTransaccion", '')								    AS "PropositoTransaccion"
	, COALESCE(IndicadorMercantilPersona."BCOCARGO1_1", '' )							AS "BCOCARGO1_1"
	, COALESCE(IndicadorMercantilPersona."NCTACARGO1" , '' )							AS "NCTACARGO1" 
	, COALESCE(IndicadorMercantilPersona."TCTACARGO1" , '' )							AS "TCTACARGO1" 
	, COALESCE(IndicadorMercantilPersona."BCOCARGO2_2", '' )							AS "BCOCARGO2_2"
	, COALESCE(IndicadorMercantilPersona."NCTACARGO2" , '' )							AS "NCTACARGO2" 
	, COALESCE(IndicadorMercantilPersona."TCTACARGO2" , '' )							AS "TCTACARGO2" 
	, COALESCE(IndicadorMercantilPersona."BCOCARGO3_3", '' )							AS "BCOCARGO3_3"
	, COALESCE(IndicadorMercantilPersona."NCTACARGO3" , '' )							AS "NCTACARGO3" 
	, COALESCE(IndicadorMercantilPersona."TCTACARGO3" , '' )							AS "TCTACARGO3" 
	, COALESCE(IndicadorMercantilPersona."BCOCARGO4_4", '' )							AS "BCOCARGO4_4"
	, COALESCE(IndicadorMercantilPersona."NCTACARGO4" , '' )							AS "NCTACARGO4" 
	, COALESCE(IndicadorMercantilPersona."TCTACARGO4" , '' )							AS "TCTACARGO4" 	
FROM CTE_PERSONA CTEPersona
	LEFT JOIN "BSCL.Plataforma.BD::REG.Giro" GiroPrincipal ON (CTEPersona."IdGiroPrincipal" = GiroPrincipal."IdGiro")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DireccionFisica ON (CTEPersona."IdDireccionFisicaPrincipal" = DireccionFisica."IdDireccionFisica")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" RegionDireccionFisica ON (DireccionFisica."CodigoDivisionPais" = RegionDireccionFisica."CodigoDivisionPais")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DireccionElectronica ON (CTEPersona."IdDireccionElectronicaPrincipal" = DireccionElectronica."IdDireccionElectronica")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TelefonoPrincipal ON (CTEPersona."IdTelefonoPrincipal" = TelefonoPrincipal."IdTelefono")
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PaisNacionalidadPrincipal ON (PaisNacionalidadPrincipal."IdPais" = CTEPersona."IdPaisNacionalidadPrincipal")
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PaisNacimiento ON (PaisNacimiento."IdPais" = CTEPersona."IdPaisNacimiento")
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PaisDireccionFisica ON (PaisDireccionFisica."IdPais" = DireccionFisica."IdPais")
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PaisResidencia ON (PaisResidencia."IdPais" = CTEPersona."IdPaisResidencia")
	LEFT JOIN "BSCL.Plataforma.BD::REG.TipoEstadoCivil" TipoEstadoCivil ON (TipoEstadoCivil."IdTipoEstadoCivil" = CTEPersona."EstadoCivil")
	LEFT JOIN "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" TipoRegimenConyugal ON (TipoRegimenConyugal."IdTipoRegimenConyugal" = CTEPersona."RegimenConyugal")
	LEFT JOIN CTE_TELEFONO TelefonoFijo ON (TelefonoFijo."IdPersona" = CTEPersona."IdPersona" AND TelefonoFijo."IdTipoTelefono" = 'FIJO' AND TelefonoFijo."OrdenRegistro" = 1)
	LEFT JOIN CTE_TELEFONO TelefonoMovil ON (TelefonoMovil."IdPersona" = CTEPersona."IdPersona" AND TelefonoMovil."IdTipoTelefono" = 'MOVIL' AND TelefonoMovil."OrdenRegistro" = 1)
	LEFT JOIN CTE_INDICADORES_PERSONA CTEIndicadoresPersona ON (CTEIndicadoresPersona."IdPersona" = CTEPersona."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.SituacionLaboral" SituacionLaboral ON (SituacionLaboral."CodigoSituacionLaboral" = CTEPersona."IdSituacionLaboral")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" NombreFantasia ON (NombreFantasia."IdNombreFantasia" = CTEPersona."IdNombreFantasiaPrincipal")
	LEFT JOIN CTE_REPRESENTANTE Representante ON ( Representante."IdPersonaDestino" = CTEPersona."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" Cargo ON Cargo."CodigoTipoCargoPersona" = CTEPersona."CodigoTipoCargoEmpleado"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.TipoGenero" Genero ON Genero."IdTipoGenero" = CTEPersona."Sexo"
	LEFT JOIN CTE_AUTORIZADO  DireccionComercial ON ( DireccionComercial."IdPersonaDestino" = CTEPersona."IdPersona")
	LEFT JOIN CTE_DIRECCION_FISICA  DireccionFisicaComercial ON (DireccionFisicaComercial."IdPersona" = CTEPersona."IdPersona" AND DireccionFisicaComercial."CodigoProposito" = 'COME' AND DireccionFisicaComercial."OrdenRegistro" = 1)
	LEFT JOIN CTE_DIRECCION_FISICA  DireccionFisicaLaboral ON (DireccionFisicaLaboral."IdPersona" = CTEPersona."IdPersona" AND DireccionFisicaLaboral."CodigoProposito" = 'LABO' AND DireccionFisicaLaboral."OrdenRegistro" = 1)
	LEFT JOIN CTE_DIRECCION_ELECTRONICA  DireccionElectronicaComercial ON (DireccionElectronicaComercial."IdPersona" = CTEPersona."IdPersona" AND DireccionElectronicaComercial."CodigoProposito" = 'COME' AND DireccionElectronicaComercial."OrdenRegistro" = 1)
	LEFT JOIN CTE_DIRECCION_ELECTRONICA  DireccionElectronicaLaboral ON (DireccionElectronicaLaboral."IdPersona" = CTEPersona."IdPersona" AND DireccionElectronicaLaboral."CodigoProposito" = 'LABO' AND DireccionElectronicaLaboral."OrdenRegistro" = 1)
	LEFT JOIN CTE_IND_MERCANTIL IndicadorMercantilPersona ON   IndicadorMercantilPersona."IdPersona" = CTEPersona."IdPersona";
