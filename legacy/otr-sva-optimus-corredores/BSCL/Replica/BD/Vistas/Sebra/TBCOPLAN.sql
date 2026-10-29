SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCOPLAN"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCOPLAN" AS

		Select 
            0  AS "COD_EMPRESA", -- DECIMAL
            cast(LEFT(conf."Valor",4) as decimal(4,0))  AS "EJERCICIO", -- DECIMAL
            RPAD("CuentaOptimus",12)  AS "CTA_CONTAB", -- CHAR(12)
            ''  AS "COD_CTA_MEF", -- CHAR(6)
            ''  AS "ALIAS_CTA", -- CHAR(10)
            'N'  AS "IND_CENTRO", -- CHAR(1)
            "Imputable"  AS "IND_IMPUTAB", -- CHAR(1)
            'SSSSSSSSSSSS'  AS "IND_VIGENCIA", -- CHAR(12)
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_001", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_002", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_003", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_004", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_005", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_006", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_007", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_008", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_009", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_010", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_011", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_DEBE_PER_012", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_001", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_002", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_003", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_004", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_005", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_006", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_007", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_008", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_009", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_010", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_011", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_ECONOMICO_012", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_001", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_002", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_003", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_004", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_005", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_006", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_007", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_008", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_009", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_010", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_011", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_HABER_PER_012", -- DECIMAL
            LEFT("NombreCuenta",50)  AS "NOMBRE_CTA", -- CHAR(50)
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_001", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_002", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_003", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_004", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_005", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_006", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_007", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_008", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_009", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_010", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_011", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRESUP_PER_012", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "SEC_CTA_MEF", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "TIPO_CORR_MON", -- CHAR(2)
            COALESCE (CAST('' AS VARCHAR(6)),'')  AS "COD_FLUJO_EFEC", -- CHAR(6)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_CTA_CONCIL", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(6)),'')  AS "COD_FLUJO_ADIC" -- CHAR(6)
			
        from "BSCL.Contabilidad.BD::ITZ.HomologaCuenta"  cuenta
        left join "BSCL.Plataforma.BD::PLA.Configuracion" conf on conf."CodigoParametro" = 'PLA.FechaSistema'
        WHERE LENGTH("CuentaOptimus") <= 12


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCOPLAN" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
