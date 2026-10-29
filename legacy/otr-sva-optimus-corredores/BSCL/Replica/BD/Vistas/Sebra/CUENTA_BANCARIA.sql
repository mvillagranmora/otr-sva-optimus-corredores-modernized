SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.CUENTA_BANCARIA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.CUENTA_BANCARIA" AS

SELECT
	COALESCE (CAST (CTA."IdCuentaBancaria" AS INT), 0) AS "CBA_ID", --INT NOT NULL

	COALESCE (CAST (
		CASE
			WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1
			ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) , -1) AS "PER_ID",--INT NOT NULL

	COALESCE (CAST ("IdMoneda" AS VARCHAR(3)), '') AS "MON_ID", --CHAR(3) NOT NULL
	COALESCE (CAST (ban."CodigoBanco" AS VARCHAR(4)), '') AS "BAN_ID", --VARCHAR(4) NOT NULL
	
	CASE CTA."IdTipoCuentaBancaria" WHEN 'CUCO' THEN 'TCUENCORRE'
	WHEN 'CUVI' THEN 'TCUENVISTA'
	WHEN 'CUAH' THEN 'TCUENAHORR' ELSE 'XXX' END AS "PAR_TCU_ID",
	
	CAST(COALESCE(PAIS."CodigoPais", '00') AS VARCHAR(2)) AS "PAI_ISO", --char](2) NOT NULL,

  	CAST(
		CASE WHEN CTA."IdMoneda" = 'USD' AND (CTA."CodigoAba" <> '' OR CTA."OtroCodigoBancoExtranjero" <> '' OR CTA."OtroCodigoCuentaExtranjero" <> '') THEN
			case when (LTRIM(CTA."OtroCodigoCuentaExtranjero") <> '') THEN concat(CTA."OtroCodigoCuentaExtranjero", '/') else '' END 
			||  concat(CTA."CodigoAba", '/') || CTA."OtroCodigoBancoExtranjero"
			|| (case when (LTRIM(CTA."OtroCodigoBancoExtranjero") = '' or CTA."OtroCodigoBancoExtranjero" is null) then CTA."OtroCodigoCuentaExtranjero"  else '' end)  
		ELSE '' END 
	AS VARCHAR(30)) AS "CBA_SUCURSAL", --varchar](30) NOT NULL,
	

	COALESCE (CAST (CTA."NumeroCuentaBancaria" AS VARCHAR(20)), '') AS "CBA_CUENTA", --VARCHAR(20) NOT NULL

	COALESCE (CAST (
		CASE WHEN PER."Tipo" = 'N' THEN PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" || ' ' || PER."Nombres" ELSE PER."RazonSocial" END
	AS VARCHAR(50)), '') AS "CBA_BENEFICIARIO", --VARCHAR(50) NOT NULL

	COALESCE (CAST (CTA."Predeterminado" AS VARCHAR(1)), '') AS "CBA_IND_PPAL", --CHAR(1) NOT NULL
	CAST('P' AS VARCHAR(1)) AS "CBA_IND_BENEFICIARIO", --char](1) NOT NULL,
	CAST('' AS VARCHAR(10)) AS "CTA_BEN_TIP_DOC", --varchar](10) NOT NULL,
	CAST('' AS VARCHAR(20)) AS "CTA_BEN_NUM_DOC", --varchar](20) NOT NULL,
	CAST('N' AS VARCHAR(1)) AS "CBA_IND_DEFAULT", --char](1) NOT NULL,
	CAST('0' AS VARCHAR(1)) AS "CBA_ORD_PERMANENTE", --char](1) NOT NULL,
	CAST('' AS VARCHAR(20)) AS "CBA_GRUPO", --varchar](20) NOT NULL,
	CAST('N' AS VARCHAR(1)) AS "CBA_DEPOSITOENESTACTA" --char](1) NOT NULL,
FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CTA ON PER."IdPersona" = CTA."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON (ban."IdBanco"  = CTA."IdBanco" )
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON BAN."IdPaisOperacion" = PAIS."IdPais" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE CTA."Borrado" = 'N'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.CUENTA_BANCARIA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
