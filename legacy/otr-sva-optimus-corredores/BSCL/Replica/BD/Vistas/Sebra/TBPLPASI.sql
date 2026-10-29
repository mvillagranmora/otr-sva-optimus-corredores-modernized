SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLPASI"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLPASI" AS

	Select 
			CONF."CodigoParametro",
            CAST('CO' AS VARCHAR(2))  AS "COD_SIS", -- CHAR(2)
            CAST('' AS VARCHAR(15))  AS "CASILLA_CORR", -- CHAR(15)
            UPPER (CAST(EMP."DireccionFisicaPrincipalNombreCiudad" AS VARCHAR(20)))  AS "CIUDAD_CORR", -- CHAR(20)
            COALESCE ( COR."CodigoCorredora" , 0 )  AS "COD_CORR", -- DECIMAL
            COALESCE (CAST(EMP."DireccionFisicaPrincipalNombreDivisionPais" AS VARCHAR(20)),'')  AS "COMUNA_CORR", -- CHAR(20)
            COALESCE (CAST(EMP."DireccionFisicaPrincipalCalle" || ' ' || EMP."DireccionFisicaPrincipalNumero" AS VARCHAR(40)),'')  AS "DIRECCION_CORR", -- CHAR(40)
            COALESCE (CAST('' AS VARCHAR(20)),'')  AS "FAX_CORR", -- CHAR(20)
            COALESCE (CAST(CONF."Valor" AS VARCHAR(10)),'')  AS "FEC_PRO", -- CHAR(10)
            COALESCE (CAST(EMP."TelefonoPrincipal" AS VARCHAR(20)),'')  AS "FONO_CORR", -- CHAR(20)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_CORR_DUAL", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_SIS_CON", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_SIS_CTA_PRO", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_SIS_CTA_TER", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_SIS_FIN", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_SIS_MON", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_SIS_ORDEN", -- CHAR(1)
            COALESCE (CAST(cor."NombreCorto" AS VARCHAR(10)),'')  AS "NEMO_CORR", -- CHAR(10)
            COALESCE (CAST(EMP."RazonSocial" AS VARCHAR(50)),'')  AS "NOMBRE_CORR", -- CHAR(50)
           COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(EMP."DocumentoIdentidad"),'0-0') AS "RUT_CORR", -- CHAR(11)
            COALESCE ( 0 , 0 )  AS "SEC_RUT_CORR", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(400)),'')  AS "ZGLOSA" -- CHAR(400)

        FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
        LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON EMP."IdEmpresa" = CONF."IdEmpresa" AND "CodigoParametro" = 'PLA.FechaSistema'
        LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" cor on cor."IdPersona" = EMP."IdPersona" AND "IdBolsa"  = 'BCS' and cor."IdEstado"  = 'ACT'
        WHERE EMP."IdEstado" = 'ACT'	


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLPASI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
