SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCOMSSD"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCOMSSD" AS

	Select 
            COALESCE (PCOM."IdEmpresa" , 0 )  AS "COD_EMPRESA", -- DECIMAL
            COALESCE (PCOM."IdPreComprobante" , 0 )  AS "SEC_CMBTE", -- DECIMAL
            'N'  AS "VIGENCIA", -- CHAR(1)
            COALESCE (CAST(PCOM."FechaContable" AS VARCHAR(10)),'')  AS "FEC_CMBTE", -- CHAR(10)
            COALESCE (PCOM."FolioContabilidad" , 0 )  AS "FOLIO_VOUCHER", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "CAN_LIN", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "COD_CMBTE", -- CHAR(4) -- ??
            'I'  	 AS "ESTADO", -- CHAR(1)
            0 		 AS "FOLIO_COMP_ADJ", -- DECIMAL
            COALESCE (CAST(PCOM."Glosa" AS VARCHAR(40)),'')  AS "GLOSA_CMBTE", -- CHAR(40)
            ''  AS "ID_ASOC", -- CHAR(15)
            COALESCE (CAST('CT' AS VARCHAR(2)),'')  AS "SIST_SUBSID", -- CHAR(2)
            COALESCE (CAST('T' AS VARCHAR(1)),'')  AS "TIPO_CMBTE", -- CHAR(1)
            ''  AS "TIPO_CMBTE_ADJ", -- CHAR(2)
            COALESCE (PCOM."CodigoUsuario", '0' )  AS "USUARIO" -- DECIMAL

        FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" PCOM 	


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCOMSSD" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
