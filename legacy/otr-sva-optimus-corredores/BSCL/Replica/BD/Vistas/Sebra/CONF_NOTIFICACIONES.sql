SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.CONF_NOTIFICACIONES"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.CONF_NOTIFICACIONES" AS

Select
		T.CODNOTIFICACION AS CODNOTIFICACION,	--VARCHAR(5)
		CAST(
		CASE 
			WHEN instr(IDENT."DocumentoIdentidad" , '-') = 0 THEN -1 
			ELSE trim(LEFT(IDENT."DocumentoIdentidad"  , instr(IDENT."DocumentoIdentidad" , '-')-1)) END AS INT) AS perid,	--INT
		TO_INT(POR."CodigoPortafolio") AS CUENTA,			--INT
		CASE WHEN IND."IdIndicador" IS NOT NULL THEN 1 ELSE 0 END AS ACTIVO,			--INT
		CASE WHEN IND."IdIndicador" IS NOT NULL THEN 1 ELSE 0 END AS EMAIL,				--INT
		CAST(coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad"), '') AS Varchar)  AS "RUT_CLI" -- TIPO :varchar(11)
		FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"  as POR ON CLI."IdCliente" = POR."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" and IDENT."Borrado" = 'N'
		LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" as TIND on TIND."CodigoTipoIndicador" = 'ORDMAILNTF'
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" as IND ON IND."IdEntidad" = POR."IdPortafolio" AND IND."IdTipoEntidad" = 'PORTAFOLIO' 
			and tind."IdTipoIndicador"  = ind."IdTipoIndicador"
		CROSS JOIN (
			SELECT 'EOACC' AS CODNOTIFICACION FROM DUMMY UNION
			SELECT 'EOAVC' AS CODNOTIFICACION FROM DUMMY UNION
			SELECT 'EOSIM' AS CODNOTIFICACION FROM DUMMY
		) AS T
		where "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
		and IDENT."DocumentoIdentidad" not like '%.%'
		and instr(IDENT."DocumentoIdentidad" , '-') != 0
		
		
		
		
		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.CONF_NOTIFICACIONES" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
