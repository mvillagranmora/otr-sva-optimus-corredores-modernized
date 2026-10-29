SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FMMovtosSpot"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FMMovtosSpot" AS

Select 
            COALESCE (CAST (SPT."IdOperacionSpot" AS INT), 0)  AS "FOLIOTRANSACCION", --INT
            COALESCE (CAST (SPT."IdOperacionSpot" AS INT), 0)   AS "FOLIOOPERACION", --INT
            TO_VARCHAR (CASE WHEN SPT."FechaOperacion" < '1900-01-01' THEN '1900-01-01' 
            ELSE TO_TIMESTAMP(UTCTOLOCAL (TO_TIMESTAMP(SPT."FechaOperacion", 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago','platform'))  END ,'YYYY-MM-DD HH24:MI:SS.FF3') AS "FECHAOPERACION", -- DATETIME
            
            ''  AS "CODFONDO", -- CHAR(10)    
            COALESCE ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(DOC."DocumentoIdentidad" AS VARCHAR(11))), '0-0')  AS "RUTCLIENTE", -- CHAR(11)
            COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)), '-1') AS "CUENTACLIENTE", -- VARCHAR(100)
            0  AS "CODLIBRO", -- INT  
            0	 AS "CODCARTERA", -- INT  
            0  AS "CODSUBCARTERA", -- INT  
            0  AS "CODCARTFINANCIERA", -- INT  
            0  AS "CODDEPTO", -- INT  
            
            1 AS "CODPRODUCTO", -- INT   
            
			COALESCE (CASE WHEN INS."Nemotecnico" = 'INTERB' THEN 1
            			   ELSE 2 END , 0 )  AS "CODSUBPRODUCTO", -- INT
            
            COALESCE (CASE WHEN SPT."IdTipoOperacion" = 'VENSPT' THEN 2 ELSE 1 END , 0 )  AS "CODTIPOMOVTO", -- INT
            
            0  AS "CODENTIDAD", -- INT  
            0  AS "CODSUCURSAL", -- INT 
            
            COALESCE (MON."CodigoNumerico" , 0 )  AS "CODMONPRINC", -- INT
            COALESCE (MONS."CodigoNumerico" , 0 )  AS "CODMONSECU", -- INT
            COALESCE (SPT."MontoMonedaPrincipal" , 0 )  AS "MTOMONPRINC", -- DECIMAL
            
            COALESCE (SPT."ParidadCierre" , 0 )  AS "PARIDADCIERREMONPRINC", -- DECIMAL   
            0									 AS "PARIDADCIERREMONSECU", -- DECIMAL    
            
            COALESCE (SPT."TipoCambioCierre" , 0 )  AS "TCCIERRE", -- DECIMAL
            COALESCE (SPT."MontoMonedaSecundaria" , 0 )  AS "MTOMONSECU", -- DECIMAL
            
            COALESCE ( 0 , 0 )  AS "PARIDADTRANSFMONPRINC", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADTRANSFMONSECU", -- DECIMAL
            
            COALESCE (SPT."TipoCambioCosto" , 0 )  AS "TCTRANSF", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADHOYMONPRINC", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADHOYMONSECU", -- DECIMAL
            
            COALESCE (SPT."TipoCambioCosto" , 0 )  AS "TCHOY", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "CODTCREFERENCIA", -- INT
            COALESCE (SPT."TipoCambioCosto" , 0 )  AS "TCREFERENCIA", -- DECIMAL
            
            COALESCE ( 0 , 0 )  AS "CODPLAZA", -- INT
            
            COALESCE ( 0 , 0 )  AS "CODCURVAMONPRINC", -- INT
            COALESCE ( 0 , 0 )  AS "CODCURVAMONSECU", -- INT
            
            COALESCE (SPT."MontoMonedaPrincipal" , 0 )  AS "MTOTRANSFERENCIA", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MTOCOMISION", -- DECIMAL
            
            COALESCE (SPT."MontoRentabilidad" , 0 )  AS "MTORENTABILIDAD", -- DECIMAL
            ''  AS "INDVIENENVAMOS", -- CHAR(1)  
            COALESCE (CAST(SPT."CodigoComercio" AS VARCHAR(6)),'')  AS "CODCOMERCIO", -- CHAR(6)
            ''  AS "CODOMA", -- CHAR(6)
            COALESCE (CAST(SPT."Observacion" AS VARCHAR(100)),'')  AS "OBSERVACIONES", -- VARCHAR(100)
            
            COALESCE(SPT."IdTipoCanalIngreso" , '')	 AS "ORIGEN", -- VARCHAR(20)
            
            
            COALESCE (CAST(SPT."FolioInterfaz" AS VARCHAR(30)),'')  AS "FOLIOINTERFAZ", -- VARCHAR(30)
            ''  AS "AGENTECOMERCIAL", -- VARCHAR(15)
            
            COALESCE (CAST(CASE WHEN TES."IdEstado" = 'LIQ' THEN 'L'            
            					WHEN SPT."Estado" = 'VIG' THEN 'V'            					
            					WHEN SPT."Estado" = 'MOD' THEN 'M'
            					WHEN SPT."Estado" = 'ANU' THEN 'A'
            					ELSE '' END AS VARCHAR(1)),'')  AS "ESTADO", -- CHAR(1)
            					
            COALESCE (CAST (CASE WHEN SPT."Estado" = 'VIG' THEN 'V' ELSE 'P' END AS CHAR(1)), 'P')  AS "ESTADOLIMITE", -- CHAR(1)  
            COALESCE (CAST (CASE WHEN SPT."Estado" = 'VIG' THEN 'V' ELSE 'P' END AS CHAR(1)), 'P')  AS "ESTADOCONFIRMADOR", --CHAR(1)
            COALESCE (CAST (CASE WHEN SPT."Estado" = 'VIG' THEN 'V' ELSE 'P' END AS CHAR(1)), 'P') AS "ESTADOCTLFINANC", --CHAR(1)
            
            
            ''  AS "MARCA1", -- VARCHAR(50)  
            ''  AS "MARCA2", -- VARCHAR(50)  
            ''  AS "MARCA3", -- VARCHAR(50)  
            ''  AS "MARCA4", -- VARCHAR(50)  
            ''  AS "MARCA5", -- VARCHAR(50)  
            
            '0'  AS "TIPOPAGO", -- CHAR(2)
            
            COALESCE (CAST(USU."CodigoUsuario" AS VARCHAR(15)),'')  AS "USUARIOCREADOR", -- VARCHAR(15)
            
            LEFT(TO_VARCHAR (CASE WHEN SPT."FechaIngresoRegistro"  < '1900-01-01' THEN '1900-01-01' ELSE SPT."FechaIngresoRegistro"  END ,'YYYY-MM-DD HH:MI:SS.FF3'),10) AS "FECHACREACION", -- DATETIME
            
            COALESCE (CAST('' AS VARCHAR(16)),'')  AS "TERMINAL", -- CHAR(16)  x 
            
            COALESCE ( 0 , 0 )  AS "CODEVENTOTRANS", -- INT
            COALESCE (SPT."PuntaBid" , 0 )  AS "TCREFCOMPRA", -- DECIMAL
            COALESCE (SPT."PuntaAsk" , 0 )  AS "TCREFVENTA", -- DECIMAL
            
            COALESCE ( 0 , 0 )  AS "CODSUBLIBRO", -- INT  x 
            COALESCE ( 0 , 0 )  AS "CODLIBRO1", -- INT  x 
            COALESCE ( 0 , 0 )  AS "CODSUBLIBRO1", -- INT  x
            COALESCE ( 0 , 0 )  AS "CODSUBSUBLIBRO", -- INT  x 
            
            COALESCE ( 0 , 0 )  AS "MTOARBITRAJEMMD", -- DECIMAL
            
            COALESCE (SPT."TipoCambioCosto" , 0 )  AS "TCCOSTOMMD", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "TCCIERREMMD", -- DECIMAL
            COALESCE (SPT."ParidadCosto" , 0 )  AS "PARIDADCOSTOMMD", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADCIERREMMD", -- DECIMAL
            COALESCE (SPT."MontoEquivalente" , 0 )  AS "MTOEQUIVALENTEMMD", -- DECIMAL
            COALESCE (SPT."Margen" , 0 )  AS "MTOMARGENMMD", -- DECIMAL
            
            COALESCE (CASE WHEN INS."Nemotecnico" = 'INTERB' THEN 1
            			   ELSE 2 END , 0 )  AS "CODTIPOPOSICION", -- INT
            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "CODAGENTE", -- CHAR(4)
            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "TIPOCOMPPAGO", -- CHAR(10)
            
            COALESCE ( 0 , 0 )  AS "CODCONCEPTOSII", -- INT
            COALESCE ( 0 , 0 )  AS "CODMONCOSTO", -- INT
            COALESCE ( 0 , 0 )  AS "COSTOSPOT", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "TCTRANSFCRM", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MTORENTABILIDADCRM", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "MERCADODIVISAS", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "ESTADOCAJA", -- CHAR(1)
            COALESCE (CAST (SPT."FolioPlanilla" AS NUMERIC), 0 )  AS "FOLIOPLANILLA", -- NUMERIC
            COALESCE (CAST('' AS VARCHAR(3)),'')  AS "TIPOGASTO", -- CHAR(3)
            COALESCE ( 0 , 0 )  AS "MONTOGASTO", -- DECIMAL
            COALESCE (SPT."MontoMonedaSecundaria" , 0 )  AS "MTOMONSECUINI", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MTOTRANSFERENCIAINI", -- DECIMAL
            COALESCE (SPT."TipoCambioCierre" , 0 )  AS "TCCIERREINI", -- DECIMAL
            COALESCE (SPT."Margen" , 0 )  AS "SPREAD", -- DECIMAL
            COALESCE (SPT."TipoCambioCosto" , 0 )  AS "TCSPOT", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRECIOLIQUIDACION", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MTOLIQUIDACION", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MOVTOLIQUIDACION", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MOVTOCAJA", -- DECIMAL
			COALESCE (CASE SPT."AfectoADerivado" WHEN 'S' THEN 1 ELSE 0 END, 0) AS "AFECTOADERIVADOS", --BIGINT
			COALESCE (CASE SPT."DolarObservado" WHEN 'S' THEN 1 ELSE 0 END, 0) AS "DOLAROBSERVADO"		--BIGINT

        FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON SPT."IdCliente" = CLI."IdCliente"
        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND spt."IdPortafolio"  = por."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON ON MON."IdMoneda" = SPT."IdMonedaPrincipal"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONS ON MONS."IdMoneda" = SPT."IdMonedaSecundaria"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON SPT."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON SPT."IdUsuario" = USU."IdUsuario"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTE ON SPT."IdOperacionSpot" = MVTE."IdTransaccion" AND MVTE."IdEstadoFacturacion" = 'FAC'  AND MVTE."IdMovimientoComprobante"  = SPT."IdComprobanteFacturacion"
			AND ( ("NemotecnicoInstrumento" = 'ARBINT' AND MVTE."IdTipoOperacion" = 'COMSPT') OR ("NemotecnicoInstrumento" IN ('DISTRI','INTERB') AND MVTE."IdTipoOperacion" IN ('COMSPT','VENSPT') ) )
		LEFT JOIN 
		(
			SELECT TES."IdComprobanteOrigen",CMBTE."IdEstado"
			FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" TES
			LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" CMBTEDETOR ON CMBTEDETOR."IdIngresoEgreso" = TES."IdIngresoEgreso" and TES."IdTipoMovimiento"  = 'MOVING' 
			LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CMBTEDET ON CMBTEDETOR."IdComprobanteDetalle" = CMBTEDET."IdComprobanteDetalle"
			LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" CMBTE ON CMBTEDET."IdComprobante" = CMBTE."IdComprobante"
			WHERE CMBTE."IdEstado" = 'LIQ'
		) as TES ON TES."IdComprobanteOrigen"  = MVTE."IdComprobante"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FMMovtosSpot" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
