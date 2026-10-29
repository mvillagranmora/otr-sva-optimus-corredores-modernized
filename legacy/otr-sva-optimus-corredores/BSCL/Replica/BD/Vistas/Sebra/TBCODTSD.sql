SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCODTSD"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCODTSD" AS

        Select 
        	--MOV."IdMovimiento",
        	PCOM."IdPreComprobante",PCOM."IdPreComprobanteAsiento",
            COALESCE (PCOM."IdEmpresa" , 0 )  AS "COD_EMPRESA", -- DECIMAL
            COALESCE (PCOM."IdPreComprobante" , 0 )  AS "SEC_CMBTE", -- DECIMAL

			DENSE_RANK() OVER (PARTITION BY PCOM."IdPreComprobante" ORDER BY PCOM."IdPreComprobanteAsiento",EPCOM."IdEvento" )  AS "LIN_CMBTE", -- DECIMAL
		
			
            COALESCE (CAST(PCOM."FechaContable" AS VARCHAR(10)),'')  AS "FEC_CMBTE", -- CHAR(10)
            COALESCE ( PCOM."IdPreComprobante" , 0 )  AS "FOLIO_VOUCHER", -- DECIMAL
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "VIGENCIA", -- CHAR(1)
            COALESCE (CAST(PCOM."IdCuentaContable" AS VARCHAR(12)),'')  AS "CTA_CONTAB", -- CHAR(12)
            COALESCE (CAST(PCOM."Glosa" AS VARCHAR(120)),'')  AS "GLOSA_MVTO", -- CHAR(120)
            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "CENTRO", -- CHAR(4)
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "IND_CENTRAL", -- CHAR(1)
            COALESCE (PCOM."MontoDebe" , 0 )  AS "MONTO_DEBE", -- DECIMAL
            COALESCE (PCOM."MontoHaber", 0 )  AS "MONTO_HABER", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "SUBCENTRO", -- CHAR(4)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "TIPO_CMBTE", -- CHAR(1) -- ??
            CAST ('' AS CHAR(11)) AS RUT_CLI,
            '0' AS SEC_RUT_CLI,
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "TIPO_COMP_FAC", -- CHAR(2) -- ??
            COALESCE ( 0 , 0 )  AS "FOLIO_COMP_FAC", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "CONCEPTO_MVTO", -- CHAR(2)
            COALESCE ( 0 , 0 )  AS "NRO_DCTO", -- DECIMAL
            --COALESCE (CAST(COALESCE (CAST(CASE WHEN PER."Tipo" = 'N' THEN (PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" || ' ' || PER."Nombres")
            --								   ELSE PER."RazonSocial" END AS VARCHAR(255)),'') AS VARCHAR(23)),'')  AS "NOM_CLIENTE", -- CHAR(23)
			CAST('' AS VARCHAR(23)) AS NOM_CLIENTE,
            COALESCE ( 0 , 0 )  AS "CODEVENTOCTB", -- INT
            CAST('' AS VARCHAR(1))  AS "IND_GLOSA_PARAM" -- CHAR(1)

        FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" P 
        INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS PCOM ON P."IdPreComprobante" = PCOM."IdPreComprobante"
		LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS EPCOM ON EPCOM."IdPreComprobanteAsiento" = PCOM."IdPreComprobanteAsiento" 
		--LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS EVE ON EVE."IdEvento" = EPCOM."IdEvento"
		--LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS EVET ON EVET."IdTipoEvento" = EVE."IdTipoEvento" 
		--LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS MOV ON MOV."IdMovimiento" = EVE."IdMovimiento" 
				
		--LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON POR."IdPortafolio" = MOV."Cuenta"
		--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
		--LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
			


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCODTSD" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
