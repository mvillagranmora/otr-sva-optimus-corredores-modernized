SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.DIRECCION"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.DIRECCION" AS

SELECT 
		DF."IdDireccionFisica" AS "DIR_ID",--int] NOT NULL,
		COALESCE( homCOM."ValorOrigen" ,  DP."Codigo" )	 AS "COM_ID",--int] NOT NULL, ***
		COALESCE( hr."ValorOrigen", DF."CodigoProposito" ) AS "PAR_TDI_ID",--varchar](10) NOT NULL,***
		CAST(
			CASE 
				WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***
		CAST(DF."Calle" AS VARCHAR(50)) AS "DIR_NOMBRE",--varchar](50) NOT NULL,***
		CAST(DF."Numero"  AS VARCHAR(15)) AS "DIR_NUMERO",--varchar](15) NOT NULL,***
		CAST(DF."Departamento" AS VARCHAR(50))  AS "DIR_COMPLEMENTO",--varchar](50) NOT NULL,***
		CAST(DF."CodigoPostal" AS VARCHAR(11)) AS "DIR_COD_POSTAL",--varchar](11) NOT NULL,
		CAST((DF."Calle" || ' ' || DF."Numero" || ' ' || DF."Departamento") AS VARCHAR(80)) AS "DIR_PRESENTACION",--varchar](80) NOT NULL,***
		CAST(CASE WHEN COALESCE(DF."IdEstado",'N') = 'INA' THEN 'S' ELSE 'N' END AS VARCHAR(1)) AS "DIR_INVALIDA",--char](1) NOT NULL,
		CASE WHEN DF."FechaModificacion" < '1900-01-01' THEN '1900-01-01' ELSE DF."FechaModificacion" END AS "DIR_FEC_MODIFICA",--datetime] NOT NULL,
		CAST(' ' AS VARCHAR(30)) AS "DIR_BARRIO",--varchar](30) NOT NULL,
		CAST(' ' AS VARCHAR(50)) AS "DIR_CONTACTO",--varchar](50) NOT NULL,
		CAST(' ' AS VARCHAR(1)) AS "DIR_MODIFICABLE",--char](1) NOT NULL,
		CAST(COALESCE(DF."Predeterminado",' ') AS VARCHAR(1)) AS "DIR_IND_PRINCIPAL"--char](1) NOT NULL,
	FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF
	INNER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DP ON DF."IdCiudad" = DP."IdCiudad" AND DF."IdDivisionPais" = DP."IdDivisionPais"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON PER."IdPersona" = DF."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	LEFT JOIN  "BSCL.Homologacion.BD::HOM.Homologacion" hr ON (hr."TipoValor" = 'TipoDireccion' and hr."ValorDestino" = DF."CodigoProposito"  AND left(hr."ValorOrigen",6) ='TDIREF'  )
	LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS homCOM ON (homCOM."TipoValor" = 'COMUNA_SEBRA' AND homCOM."ValorDestino" = DP."IdDivisionPais" )
	WHERE DF."Borrado" <> 'S' AND DOC."DocumentoIdentidad" not like '%.%'

	UNION 
	
	SELECT
		DF."IdDireccionElectronica"  * -1 AS "DIR_ID",--int] NOT NULL,
		0 AS "COM_ID",--int] NOT NULL, ***
		COALESCE( hr."ValorOrigen", DF."CodigoProposito" ) AS "PAR_TDI_ID",--varchar](10) NOT NULL,***
		CAST(
			CASE 
				WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***	
		CAST(DF."Direccion" AS VARCHAR(50)) AS "DIR_NOMBRE",--varchar](50) NOT NULL,***
		CAST(' ' AS VARCHAR(15)) AS "DIR_NUMERO",--varchar](15) NOT NULL,***
		CAST(' ' AS VARCHAR(50))  AS "DIR_COMPLEMENTO",--varchar](50) NOT NULL,***
		CAST(' ' AS VARCHAR(11)) AS "DIR_COD_POSTAL",--varchar](11) NOT NULL,
		CAST(DF."Direccion" AS VARCHAR(80)) AS "DIR_PRESENTACION",--varchar](80) NOT NULL,***
		CAST(CASE WHEN COALESCE(DF."IdEstado",'N') = 'INA' THEN 'S' ELSE 'N' END AS VARCHAR(1)) AS "DIR_INVALIDA",--char](1) NOT NULL,
		CASE WHEN DF."FechaModificacion" < '1900-01-01' THEN '1900-01-01' ELSE DF."FechaModificacion" END AS "DIR_FEC_MODIFICA",--datetime] NOT NULL,
		CAST(' ' AS VARCHAR(30)) AS "DIR_BARRIO",--varchar](30) NOT NULL,
		CAST(' ' AS VARCHAR(50)) AS "DIR_CONTACTO",--varchar](50) NOT NULL,
		CAST(' ' AS VARCHAR(1)) AS "DIR_MODIFICABLE",--char](1) NOT NULL,
		CAST(COALESCE(DF."Predeterminado",' ') AS VARCHAR(1)) AS "DIR_IND_PRINCIPAL"--char](1) NOT NULL,
	FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DF
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON PER."IdPersona" = DF."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON PER."IdPersona" = EMP."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	LEFT JOIN  "BSCL.Homologacion.BD::HOM.Homologacion" hr ON (hr."TipoValor" = 'TipoDireccion' and hr."ValorDestino" = DF."CodigoProposito"  AND left(hr."ValorOrigen",6) ='TDIREE' AND hr."ValorOrigen" != 'TDIREEFUNC' )
	WHERE (EMP."IdDireccionElectronica" IS NULL OR DF."IdDireccionElectronica" != EMP."IdDireccionElectronica")
	AND DF."Borrado" <> 'S' AND DOC."DocumentoIdentidad" not like '%.%'
	UNION 
	
	--DIRECCIONES DE EMPLEADO
	SELECT
		DF."IdDireccionElectronica"  * 1000 AS "DIR_ID",--int] NOT NULL,
		0 AS "COM_ID",--int] NOT NULL, ***
		'TDIREEFUNC' AS "PAR_TDI_ID",--varchar](10) NOT NULL,***
		CAST(
			CASE 
				WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***	
		CAST(DF."Direccion" AS VARCHAR(50)) AS "DIR_NOMBRE",--varchar](50) NOT NULL,***
		CAST(' ' AS VARCHAR(15)) AS "DIR_NUMERO",--varchar](15) NOT NULL,***
		CAST(' ' AS VARCHAR(50))  AS "DIR_COMPLEMENTO",--varchar](50) NOT NULL,***
		CAST(' ' AS VARCHAR(11)) AS "DIR_COD_POSTAL",--varchar](11) NOT NULL,
		CAST(DF."Direccion" AS VARCHAR(80)) AS "DIR_PRESENTACION",--varchar](80) NOT NULL,***
		CAST(CASE WHEN COALESCE(DF."IdEstado",'N') = 'INA' THEN 'S' ELSE 'N' END AS VARCHAR(1)) AS "DIR_INVALIDA",--char](1) NOT NULL,
		CASE WHEN DF."FechaModificacion" < '1900-01-01' THEN '1900-01-01' ELSE DF."FechaModificacion" END AS "DIR_FEC_MODIFICA",--datetime] NOT NULL,
		CAST(' ' AS VARCHAR(30)) AS "DIR_BARRIO",--varchar](30) NOT NULL,
		CAST(' ' AS VARCHAR(50)) AS "DIR_CONTACTO",--varchar](50) NOT NULL,
		CAST(' ' AS VARCHAR(1)) AS "DIR_MODIFICABLE",--char](1) NOT NULL,
		CAST(COALESCE(DF."Predeterminado",' ') AS VARCHAR(1)) AS "DIR_IND_PRINCIPAL"--char](1) NOT NULL,
	FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DF
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON PER."IdPersona" = DF."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON PER."IdPersona" = EMP."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	WHERE (EMP."IdDireccionElectronica" IS NOT NULL and DF."IdDireccionElectronica" = EMP."IdDireccionElectronica")
	AND DF."Borrado" <> 'S' AND  DOC."DocumentoIdentidad" not like '%.%'
	;
GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.DIRECCION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
