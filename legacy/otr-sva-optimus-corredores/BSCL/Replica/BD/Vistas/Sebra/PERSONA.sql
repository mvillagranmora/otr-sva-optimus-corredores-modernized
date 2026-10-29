SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.PERSONA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.PERSONA" AS

SELECT 
	CAST(
		CASE 
			WHEN instr(IDENT."DocumentoIdentidad" , '-') = 0 THEN -1 
			ELSE trim(LEFT(IDENT."DocumentoIdentidad"  , instr(IDENT."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***
			
	coalesce (PROF."IdProfesion" , 0) AS "PRO_ID", --int] NOT NULL,
	
	COALESCE( CAST(PAIS."CodigoPais" AS VARCHAR(2)), 'CL') AS "NAC_ID",--char](2) NOT NULL,
	CAST(COALESCE(HOM_SEG."ValorDestino",'DESCONOCID') AS VARCHAR(10)) AS "PAR_SEG_ID",--varchar](10) NOT NULL,
	CAST(CASE WHEN PER."IdEstado" = 'ACT' then '07' ELSE '08' END AS VARCHAR(2))  AS "EST_ID",--varchar](2) NOT NULL********** per.est_id = '08' BLO | DESQ
	CAST('RUT' AS VARCHAR(10)) AS "PER_TIP_DOC", --varchar](10) NOT NULL,
	CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad") AS VARCHAR(20)) AS "PER_NUM_DOC", --varchar](20) NOT NULL,******
	to_number(left(IDENT."DocumentoIdentidad",length(IDENT."DocumentoIdentidad")-2)) AS "PER_RUT",--int] NOT NULL,
	CAST(right(IDENT."DocumentoIdentidad",1) AS VARCHAR(1)) AS "PER_DV",--char](1) NOT NULL,
	"BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad") AS "PER_RUTDV", --varchar](11) NOT NULL,**********   
	CAST(CASE WHEN PER."Tipo" = 'N' then PER."ApellidoPaterno" ELSE  PER."RazonSocial"  END AS VARCHAR(60)) AS "PER_PATERNO", --varchar](60) NOT NULL,***********
	CAST(CASE WHEN PER."Tipo" = 'N' then PER."ApellidoMaterno" ELSE  PER."RazonSocial"  END AS VARCHAR(50)) AS "PER_MATERNO", --varchar](50) NOT NULL,***********
	CAST(CASE WHEN PER."Tipo" = 'N' then PER."Nombres" ELSE  '' END AS VARCHAR(30)) AS "PER_NOMBRES", --varchar](30) NOT NULL,************
	CAST(COALESCE( CASE WHEN PER."FechaNacimiento" < '1900-01-01' THEN '1900-01-01' ELSE PER."FechaNacimiento" END, '1900-01-01') AS DATETIME) AS "PER_FEC_NACI", --datetime] NOT NULL,*************
	CAST(COALESCE(PER."Sexo",'M') AS VARCHAR(1)) AS "PER_SEXO", --char](1) NOT NULL,******************
	CAST(CASE WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'CA' then 'ECIVICASAD'
	 WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'DI' then 'ECIVIDIVOR'
	 WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'SE' then 'ECIVISEPAR'
	 WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'SO' then 'ECIVISOLTE'
	 WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'VI' then 'ECIVIVIUDO'
	 ELSE 'DESCONOCID' END AS VARCHAR(10)) AS "PAR_EST_CIVIL", --varchar](10) NOT NULL,****************  
	CAST(CASE WHEN PER."Tipo" = 'N' AND PER."RegimenConyugal" = 'PG' then 'RCONYPARTG'
	 WHEN PER."Tipo" = 'N' AND PER."RegimenConyugal" = 'SB' then 'RCONYSEPBI'
	 WHEN PER."Tipo" = 'N' AND PER."RegimenConyugal" = 'SC' then 'RCONYSOCCO'
	 WHEN PER."Tipo" = 'N' AND PER."RegimenConyugal" = 'CM' then 'RCONYCOMUN'
	 WHEN PER."Tipo" = 'N' AND PER."RegimenConyugal" = 'SR' then 'RCONYSINRE'
	 ELSE 'DESCONOCID' END AS VARCHAR(10)) AS "PAR_REG_CONYUGAL", --varchar](10) NOT NULL
	CAST(CASE WHEN PER."Tipo" = 'N' then 'NAT' ELSE  'JUR' END AS VARCHAR(3)) AS "PER_TIP_PER", --char](3) NOT NULL,****************
	CASE WHEN PER."FechaIngreso" < '1900-01-01' THEN '1900-01-01' ELSE PER."FechaIngreso" END AS "PER_FEC_INGRESO",--datetime] NOT NULL,************
	CASE WHEN PER."FechaModificacion" < '1900-01-01' THEN '1900-01-01' ELSE PER."FechaModificacion" END AS "PER_FEC_MODIFICA",--datetime] NOT NULL,
	CAST(CASE WHEN PER."Tipo" = 'N' then PER."ApellidoPaterno"||' '||PER."ApellidoMaterno"||' '||PER."Nombres" ELSE  PER."RazonSocial" END AS VARCHAR(80)) AS "PER_RAZ_SOCIAL", --varchar](80) NOT NULL,****************
	LEFT(PER."ComentarioBloqueo", 250) AS "PER_OBS_BLOQUEO", --varchar](250) NOT NULL,
	LEFT(PER."Cargo", 30) AS "PER_CARGO", --varchar](30) NOT NULL,
	CAST(PER."Empleador" AS VARCHAR(100)) AS "PER_EMPLEADOR", --varchar](100) NOT NULL,***************
	coalesce (CAST(PAIS."CodigoPais" AS VARCHAR(2)),'CL') AS "PER_PAIS_ISO", --char](2) NOT NULL,************
	0 AS "PER_COM_ID", --int] NOT NULL,
	CAST('' AS VARCHAR(3)) AS "PER_RESIDENCIA",--char](3) NOT NULL,
	CAST(COALESCE (PROF."Nombre" ,'') AS VARCHAR(30)) AS "PER_ACTIVIDAD", --varchar](30) NOT NULL,
	CAST('' AS VARCHAR(4)) AS "PER_SUC_ID", --char](4) NOT NULL,
	CURRENT_TIMESTAMP AS "PER_FECHA_KYC", --datetime] NOT NULL,
	coalesce (CONYUGE."Nombres", '') AS "PER_NOM_CONYUGUE",--varchar](100) NOT NULL,
	coalesce (PROF."IdProfesion" , 0) AS "PER_ACTIVIDAD_ID",--int] NOT NULL,
	CAST('' AS VARCHAR(10)) AS "PER_SITUAC_LAB",--varchar](10) NOT NULL,en seb
	coalesce (EMPLE_DIR_FIS."Calle", '') AS "PER_DIR_EMPLEADOR",--varchar](100) NOT NULL,*********  S/I optimus
	coalesce (EMPLE_TEL."Numero", '') AS "PER_TEL_EMPLEADOR",--varchar](20) NOT NULL,*********** S/I optimus
	CAST('' AS VARCHAR(3)) AS "PER_ING_ULTANIO_MONEDA",--char](3) NOT NULL,
	0.0 AS "PER_ING_ULTANIO_MONTO",--decimal](24, 4) NOT NULL,
	0 AS "PER_ING_ULTANIO_ANIO",--smallint] NOT NULL,
	CAST(COALESCE(PER_PEP."ValorIndicador",'') AS VARCHAR(1)) AS "PER_IND_PEP",--char](1) NOT NULL,
	CAST('' AS VARCHAR(4)) AS "PER_MOTIVO_BLOQUEO",--char](4) NOT NULL,
    CASE WHEN PER."FechaBloqueo" < '1900-01-01' THEN '1900-01-01' ELSE PER."FechaBloqueo" END AS "PER_FEC_BLOQUEO",--datetime] NOT NULL,
    CASE WHEN PER."FechaDesbloqueo" < '1900-01-01' THEN '1900-01-01' ELSE PER."FechaDesbloqueo" END AS "PER_FEC_DESBLOQUEO",--datetime] NOT NULL,
	CAST('' AS VARCHAR(20)) AS "PER_TAXID",--varchar](20) NOT NULL,
	CAST('' AS VARCHAR(10)) AS "COD_BANCO",--char](10) NOT NULL,
	CAST('' AS VARCHAR(10)) AS "PER_FEC_FALLECIMIENTO",--varchar](10) NOT NULL,
	CAST('N' AS VARCHAR(1)) AS "PER_NO_VALIDA_KYC",--char](1) NOT NULL,
	CAST('' AS VARCHAR(60)) AS "PER_NOMBRE_EJECUTIVO",--varchar](60) NOT NULL,
	CAST('' AS VARCHAR(60)) AS "PER_NOMBRE_SUCURSAL",--varchar](60) NOT NULL,
	CAST('' AS VARCHAR(50)) AS "PER_TIPO_BLOQUEO",--varchar](50) NOT NULL,
	CAST('' AS VARCHAR(50)) AS "PER_COD_CLI_ALT",--varchar](50) NOT NULL,
	CAST('' AS VARCHAR(3)) AS "PER_ORIGEN_INGRESO",--char](3) NOT NULL,
	COALESCE (LEFT(PAIS_RESI."CodigoPais", 3),'CL') AS "PER_PAIS_RESI",--char](3) NOT NULL,
	CAST('' AS VARCHAR(10)) AS "PER_RELA_CORR",--char](10) NOT NULL,
	CAST('' AS VARCHAR(1)) AS "PER_PARTI_SOC",--char](1) NOT NULL,
	CAST('' AS VARCHAR(20)) AS "PER_SOC_RUT",--varchar](20) NOT NULL,
	CAST('' AS VARCHAR(80)) AS "PER_SOC_RAZ_SOCIAL",--varchar](80) NOT NULL,
	0 AS "PER_SOC_GIRO",--int] NOT NULL,
	0.00 AS "PER_SOC_PORC_PARTI",--numeric](5, 2) NOT NULL,
	coalesce (EMPLE_RUT."DocumentoIdentidad", '') AS "PER_EMP_RUT",--varchar](20) NOT NULL,
	coalesce (EMPLE_RUT."DocumentoIdentidad", '-') AS "PER_EMP_RUTL",--varchar](20) NOT NULL,
	coalesce (EMPLE_FANTASIA."DescripcionNombreFantasia", '') AS "PER_EMP_NOM_FANTASIA",--varchar](80) NOT NULL,
	coalesce (left(CAST(EMPLE_GIRO."IdGiro" AS VARCHAR(3)),2), '') AS "PER_EMP_GIRO",--varchar](2) NOT NULL, preguntar!
	CAST('' AS VARCHAR(50)) AS "PER_GLEI",--varchar](50) NOT NULL,
	CAST('' AS VARCHAR(3)) AS "PER_PAIS_ORI_FONDOS",--varchar](3) NOT NULL,
	CAST('' AS VARCHAR(80)) AS "PER_PRO_ORI_FONDOS",--varchar](80) NOT NULL,
	CAST('' AS VARCHAR(10)) AS "PER_TIP_CLI", --varchar](10) NOT NULL,
	CAST('' AS VARCHAR(1)) AS "PER_SUS_CON_INT", --[char](1) NOT NULL,
	CAST('' AS VARCHAR(1)) AS "PER_MAN_INFO_PRIV", --[char](1) NOT NULL,
	coalesce (PER_RELA_AR100."ValorIndicador", '') AS "PER_RELA_CORR_ART100", --char](1) NOT NULL
	CAST('' AS VARCHAR(10)) AS "PER_CLASIF_CLI",--varchar](10) NOT NULL,
	CAST('' AS VARCHAR(10)) AS "PAR_ENT_UAF",--varchar](10) NOT NULL,
	CAST('' AS VARCHAR(150)) AS "NATURALEZA_ENT_UAF",--varchar](150) NOT NULL,
	COALESCE(PER_FOLIOUAF57."ValorIndicador",'0') AS "FOLIO_ENT_UAF", --int] NOT NULL,
	CASE WHEN PER_UAF57."ValorIndicador" ='S' THEN  COALESCE( CAST(PAISNACIONALIDAD."CodigoPais" AS VARCHAR(2)),'CL') 
	ELSE CAST('' AS VARCHAR(3)) END AS "NACIONALIDAD_ENT_UAF",--char](2) NOT NULL,
	CAST('' AS VARCHAR(11)) AS "RUT_ENT_UAF",--char](11) NOT NULL,
	CASE WHEN PER_UAF57."ValorIndicador" ='S'THEN CAST (PER."RazonSocial" AS VARCHAR(250))
	ELSE CAST('' AS VARCHAR(200)) END AS "NOMBRE_ENT_UAF",--varchar](200) NOT NULL,
	CAST('' AS VARCHAR(200)) AS "RELACION_ENT_UAF",--, --varchar](200) NOT NULL
	CAST('' AS VARCHAR(10)) AS "TIPO_NAC_UAF", --] [varchar](10) NOT NULL,
	CAST('' AS VARCHAR(10)) AS "TIPO_DECLARACION_UAF",
	'1900-01-01' as "PER_FECHA_FW",
	PER."IdPersona",
	PER."CodigoPropositoTransaccion" as "PropositoTransaccion",
	PER."CodigoRangoNivelVenta" AS "RangoNivelVenta",
	PER."CodigoRangoNivelVenta" AS "RangoRenta"
FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND  IDENT."IdTipoDocumentoIdentidad" = 'R'
and IDENT."DocumentoIdentidad" != '15.829.477-K'
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" SEG ON CLI."IdSegmentoCliente" = SEG."IdSegmentoCliente"
LEFT JOIN "BSCL.Replica.BD::PLA.Homologacion" HOM_SEG ON HOM_SEG."TipoValor" = 'SegmentoCliente' AND SEG."NombreSegmentoCliente" = HOM_SEG."ValorOrigen"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON PER."IdPaisNacimiento" = PAIS."IdPais"
LEFT JOIN "BSCL.Plataforma.BD::REG.Profesion" AS PROF ON PER."IdProfesion" =  PROF."IdProfesion"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS CONYUGE ON PER."IdPersonaConyuge" = CONYUGE."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS_RESI ON PER."IdPaisResidencia" = PAIS_RESI."IdPais"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS EMPLEADOR  ON PER."IdPersonaEmpleador" = EMPLEADOR."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS EMPLE_DIR_FIS ON EMPLEADOR."IdDireccionFisicaPrincipal" = EMPLE_DIR_FIS."IdDireccionFisica" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS EMPLE_TEL on EMPLEADOR."IdTelefonoPrincipal" = EMPLE_TEL."IdTelefono"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS EMPLE_RUT on EMPLEADOR."IdDocumentoIdentidad" = EMPLE_RUT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" as EMPLE_FANTASIA on EMPLEADOR."IdNombreFantasiaPrincipal"  = EMPLE_FANTASIA. "IdNombreFantasia"
LEFT JOIN "BSCL.Plataforma.BD::REG.Giro" as EMPLE_GIRO on EMPLEADOR."IdGiroPrincipal"  = EMPLE_GIRO. "IdGiro"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" as TINDICADOR_RELCORRPER  on TINDICADOR_RELCORRPER."CodigoTipoIndicador" = 'RELCORRPER' 
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS PER_RELA_AR100  on PER_RELA_AR100."IdTipoIndicador" =   TINDICADOR_RELCORRPER."IdTipoIndicador"  and  PER."IdPersona" = PER_RELA_AR100."IdEntidad"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" as TINDICADOR_PEP  on TINDICADOR_PEP."CodigoTipoIndicador" = 'PEP'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS PER_PEP  on PER_PEP."IdTipoIndicador" =   TINDICADOR_PEP."IdTipoIndicador"  and  PER."IdPersona" = PER_PEP."IdEntidad"
--LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" as TINDICADOR_KYC  on TINDICADOR_KYC."CodigoTipoIndicador" = 'KYC'
--LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS PER_KYC  on PER_KYC."IdTipoIndicador" =   TINDICADOR_KYC."IdTipoIndicador"  and  PER."IdPersona" = PER_KYC."IdEntidad"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TINDICADOR_FOLIOUAF57 ON TINDICADOR_FOLIOUAF57."CodigoTipoIndicador" = 'FOLIOUAF57'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS PER_FOLIOUAF57 ON PER_PEP."IdTipoIndicador" = TINDICADOR_FOLIOUAF57."IdTipoIndicador" AND PER."IdPersona" = PER_FOLIOUAF57."IdEntidad"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TINDICADOR_UAF57 ON TINDICADOR_UAF57."CodigoTipoIndicador" = 'UAF57'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS PER_UAF57 ON PER_PEP."IdTipoIndicador" = TINDICADOR_UAF57."IdTipoIndicador" AND PER."IdPersona" = PER_UAF57."IdEntidad"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAISNACIONALIDAD ON PER."IdPaisNacionalidadPrincipal" = PAISNACIONALIDAD."IdPais"
WHERE IDENT."DocumentoIdentidad"  like '%-_' AND IDENT."DocumentoIdentidad" not like '%.%' AND PER."Borrado" = 'N'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.PERSONA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;