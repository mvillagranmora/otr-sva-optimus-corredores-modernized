SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLPERS"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLPERS" AS

	SELECT 
			PER."IdPersona",
            COALESCE (CAST('P' AS VARCHAR(1)),'')  AS "TIPO_REG", -- CHAR(1)
            COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(IDENT."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(100)),'0-0'   ) AS "RUT_PERSONA", -- CHAR(11)
            COALESCE ( 0 , 0 )  AS "SEC_RUT_PERSONA", -- DECIMAL
            COALESCE (CAST(CASE WHEN PER."Tipo" = 'N' THEN (PER."ApellidoPaterno" || ' ' ||PER."ApellidoMaterno" ||' '|| PER."Nombres")
            					ELSE PER."RazonSocial" END AS VARCHAR(50)) ,'')  AS "NOMBRE", -- CHAR(50)
            COALESCE (CAST('' AS VARCHAR(15)),'')  AS "CASILLA", -- CHAR(15)
            COALESCE (CAST('' AS VARCHAR(5)),'')  AS "CENTRO_COSTO", -- CHAR(5)
            COALESCE (CAST(COALESCE(CIU."COD_TRAD",'') AS VARCHAR(4)),'')  AS "CIUDAD", -- CHAR(4)
            COALESCE (CAST(COALESCE(COM."COD_TRAD",'') AS VARCHAR(4)),'')  AS "COMUNA", -- CHAR(4)
            COALESCE (CAST(DIRFIS."Calle" || ' ' || DIRFIS."Numero" AS VARCHAR(40)),'')  AS "DIRECCION", -- CHAR(40)
            COALESCE (CAST('' AS VARCHAR(20)),'')  AS "FAX", -- CHAR(20)
            COALESCE (CAST('' AS VARCHAR(40)),'')  AS "OBSERVACION", -- CHAR(40)
            COALESCE (CAST(COALESCE(PAIS."COD_TRAD", '') AS VARCHAR(4)),'')  AS "PAIS", -- CHAR(4)
            COALESCE (CAST(TEL."Numero" AS VARCHAR(20)),'')  AS "TELEFONO", -- CHAR(20)
            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "COD_BCO", -- CHAR(4)
            COALESCE (CAST('' AS VARCHAR(20)),'')  AS "CTA_BCO", -- CHAR(20)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_COMIS_ACC", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "TIPO_COMIS_ACC", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_COMIS_IRF", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "TIPO_COMIS_IRF", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_COMIS_IIF", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "TIPO_COMIS_IIF", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_COBRANZA", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "TIPO_MON_COMIS", -- CHAR(2)
            COALESCE ( 0 , 0 )  AS "MONTO_COMISION", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "TIPO_PROV", -- CHAR(2)
            COALESCE (CAST(DIRELE."Direccion" AS VARCHAR(50)),'')  AS "EMAIL" -- CHAR(50)
        FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
        LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DIRFIS ON PER."IdDireccionFisicaPrincipal" = DIRFIS."IdDireccionFisica"
        LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DIVPA ON DIVPA."IdDivisionPais" = DIRFIS."IdDivisionPais"
		LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" COM ON COM."TIPO_TRAD" = 'COMU' AND UPPER(TRIM(COM."GLOSA")) = UPPER(TRIM(DIVPA."NombreDivisionPais")) 
		LEFT JOIN (
			SELECT P."IdPais", SITRAD."COD_TRAD" FROM "BSCL.Plataforma.BD::REG.Pais" P
			LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" SITRAD ON SITRAD."TIPO_TRAD" = 'PAIS' AND UPPER(TRIM(SITRAD."GLOSA")) = UPPER(TRIM(P."NombrePais")) 
			WHERE SITRAD."COD_TRAD" IS NOT NULL
		)PAIS ON PAIS."IdPais" = DIVPA."IdPais"
		LEFT JOIN (
			SELECT C."IdCiudad", SITRAD."COD_TRAD" FROM "BSCL.Plataforma.BD::REG.Ciudad" C
			LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" SITRAD ON SITRAD."TIPO_TRAD" = 'CIUD' AND UPPER(TRIM(SITRAD."GLOSA")) = UPPER(TRIM(C."NombreCiudad")) 
			WHERE SITRAD."COD_TRAD" IS NOT NULL
		) CIU ON CIU."IdCiudad" = DIVPA."IdCiudad"
        LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DIRELE ON PER."IdDireccionElectronicaPrincipal" = DIRELE."IdDireccionElectronica"
        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL ON PER."IdTelefonoPrincipal" = TEL."IdTelefono"
		WHERE IDENT."DocumentoIdentidad" IS NOT null and IDENT."IdTipoDocumentoIdentidad"= 'R' and IDENT."DocumentoIdentidad" != '0-0'
		and PER."Borrado" = 'N'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLPERS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
