
--* ------------------------------------------------------------------------
--* Nombre .....: 	ConsultarOperacionesPendientes
--* Modulo .....: 	Custodia
--* F. Creacion : 	Enero 2020
--* Proposito ..: 	utilizada en SQL de servicio No. 700.146 para validar transferencias DVP
-- 					De cambiar condiciones de query en store procedure de transferencia de titulo
-- 					(BSCL.Custodia.BD.Servicios::OPE.ConsultaArchivoOperacionesPendientes) debe
-- 					considerarse en esta vista para mantener la consistencia
--* ------------------------------------------------------------------------

SET SCHEMA "BSCL"; 
--* ------------------------------------------------------------------------
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesPendientes";
--* ------------------------------------------------------------------------
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesPendientes" AS

      SELECT    -- FD (FACTURAS)
      			ROW_NUMBER() OVER (ORDER BY  DCOMP."IdMovimientoComprobante") 	AS "IdInterno"                          
                ,DCOMP."IdMovimientoComprobante"                				AS "FolioOperacion"                 
                ,DCOMP."IdTipoComprobante"                      				AS "IdTipoOperacion"                    
                ,CASE WHEN DCOMP."IdTipoProducto" = 'INSI'
                      THEN DCOMP."NemotecnicoSubyacente" 
                      ELSE DCOMP."NemotecnicoInstrumento"  END  				AS "Nemo"                               
                ,DCOMP."IdPortafolio"                           				AS "IdPortafolio"                       
                ,DCOMP."IdCliente"                              				AS "IdCliente"                          
                ,DCOMP."Monto"                                  				AS "Monto"                              
                ,'FD'                                           				AS "Categoria" 
        FROM    "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante"    AS DCOMP
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"              AS TOPER 
                ON (DCOMP."IdTipoOperacion" = TOPER."IdTipoOperacion" AND DCOMP."IdTipoInstrumento" = TOPER."IdTipoInstrumento")                
                INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"                   AS CONCE
                ON (DCOMP."IdConceptoMovimiento" = CONCE."IdConcepto")                      
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"               AS PRODU
                ON (DCOMP."CodigoTipoProducto" = PRODU."IdTipoProducto")
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"          AS IndCli
				ON  DCOMP."IdCliente" = IndCli."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'				
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"                   AS Cli 
				ON Cli."IdCliente" =  IndCli."IdCliente"
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS Contra
				ON Contra."IdPersona" = Cli."IdPersona"
        WHERE   DCOMP."IdEstadoComprobante" IN ('FAC', 'ENV')
                AND DCOMP."IdTipoProducto" IN ('INIF', 'INRF', 'INAC', 'INSI')
	UNION
        SELECT  -- GARANTIA                    
				 ROW_NUMBER() OVER (ORDER BY  GARAN."FolioComprobante") AS "IdInterno"
				,GARAN."FolioComprobante"                     		AS "FolioOperacion"
                ,CASE WHEN GARAN."IdEstado" = 'LIQ' 
                	  THEN 'RETGTA'
                 	  ELSE 'INGGTA' END								AS "IdTipoOperacion"                        
                ,GARAN."Nemotecnico"                                AS "Nemo"                                   
                ,GARAN."IdPortafolio"                               AS "IdPortafolio"           
                ,GARAN."IdCliente"                                  AS "IdCliente"                                          
                ,GARAN."MontoGarantia"                              AS "Monto"                                  
                ,'GT'                                               AS "Categoria"
        FROM    "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia"                       AS GARAN
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"          AS TOPER 
                ON ( CASE WHEN GARAN."IdEstado" = 'LIQ' 
                	  THEN 'RETGTA' ELSE 'INGGTA' END = TOPER."IdTipoOperacion" )
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 				AS Cli 
                ON (Cli."IdEmpresa" = GARAN."IdEmpresa" and Cli."IdCliente" = GARAN."IdCliente")
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"       PRODU
                ON ('CUST' = PRODU."IdTipoProducto")                
                LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 								AS Concepto 
                ON (GARAN."IdConcepto" = Concepto."IdConcepto")
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
				ON IndCli."IdCliente" =  GARAN."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS Contra
				ON Contra."IdPersona" = Cli."IdPersona"
        WHERE   GARAN."IdClasificacionTipoInstrumento" IN ('IF', 'RF', 'RV')
                AND (GARAN."IdEstado" IN ('VIG','LIQ' ))
		GROUP BY
				GARAN."FolioComprobante",TOPER."Nombre",GARAN."IdTipoInstrumento",GARAN."Nemotecnico",
				GARAN."IdClasificacionTipoInstrumento",GARAN."IdCondicionLiquidacion",GARAN."FechaIngresoRegistro",
				GARAN."FechaVencimientoOperacion",Cli."DocumentoIdentidad",GARAN."IdPortafolio",GARAN."IdCliente",
				GARAN."PrecioGarantia",GARAN."TasaGarantia",GARAN."MontoGarantia",GARAN."IdEstado",GARAN."IdAgente",
				GARAN."IdMonedaInstrumento",PRODU."Nombre",GARAN."IdConcepto",Concepto."Nombre",GARAN."IdEmisor",
				GARAN."EsFungible",GARAN."NemoSVS",GARAN."IdTipoReajuste",IndCli."ValorIndicador",CLI."NombreCompleto", --[CELCONDOR-659]
				CLI."IdCliente",Contra."IdContraparte"
	UNION
        SELECT  -- MOV TITULO                     
				 ROW_NUMBER() OVER (ORDER BY  GARAN."FolioComprobante") AS "IdInterno"
				,GARAN."FolioComprobante"                     		AS "FolioOperacion"
                ,CASE WHEN GARAN."IdEstado" = 'LIQ' 
                	  THEN 'RETGTA'
                 	  ELSE 'INGGTA' END								AS "IdTipoOperacion"                        
                ,GARAN."Nemotecnico"                                AS "Nemo"                                   
                ,GARAN."IdPortafolio"                               AS "IdPortafolio"
                ,GARAN."IdCliente"                                  AS "IdCliente"
                ,GARAN."MontoGarantia"                              AS "Monto"                                  
                ,'GT'                                               AS "Categoria"  
         FROM    "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia"                      AS GARAN
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"          AS TOPER 
                ON ( CASE WHEN GARAN."IdEstado" = 'LIQ' 
                	  THEN 'RETGTA' ELSE 'INGGTA' END = TOPER."IdTipoOperacion" )
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 				AS Cli 
                ON (Cli."IdEmpresa" = GARAN."IdEmpresa" and Cli."IdCliente" = GARAN."IdCliente")
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" PRODU
                ON ('CUST' = PRODU."IdTipoProducto")
                LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 								AS Concepto 
                ON (GARAN."IdConcepto" = Concepto."IdConcepto")
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
				ON IndCli."IdCliente" =  GARAN."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS Contra
				ON Contra."IdPersona" = Cli."IdPersona"
        WHERE   GARAN."IdClasificacionTipoInstrumento" IN ('IF', 'RF', 'RV')
                AND (GARAN."IdEstado" IN ('VIG','LIQ' ))
		GROUP BY
				GARAN."FolioComprobante",TOPER."Nombre",GARAN."IdTipoInstrumento",GARAN."Nemotecnico",
				GARAN."IdClasificacionTipoInstrumento",GARAN."IdCondicionLiquidacion",GARAN."FechaIngresoRegistro",
				GARAN."FechaVencimientoOperacion",Cli."DocumentoIdentidad",GARAN."IdPortafolio",GARAN."IdCliente",
				GARAN."PrecioGarantia",GARAN."TasaGarantia",GARAN."MontoGarantia",GARAN."IdEstado",GARAN."IdAgente",
				GARAN."IdMonedaInstrumento",PRODU."Nombre",GARAN."IdConcepto",Concepto."Nombre",GARAN."IdEmisor",
				GARAN."EsFungible",GARAN."NemoSVS",GARAN."IdTipoReajuste",IndCli."ValorIndicador",CLI."NombreCompleto", --[CELCONDOR-659]
				CLI."IdCliente",Contra."IdContraparte"
	UNION
		SELECT  -- MOV TRANSITO
				 ROW_NUMBER() OVER (ORDER BY  TRANSITO."FolioComprobante") 	AS "IdInterno"
				,TRANSITO."FolioComprobante"                     			AS "FolioOperacion"
                ,TRANSITO."IdTipoOperacion"									AS "IdTipoOperacion"                        
                ,TRANSITO."Nemotecnico"                               		AS "Nemo"                                   
                ,TRANSITO."IdPortafolio"                               		AS "IdPortafolio"
                ,TRANSITO."IdCliente"                                 		AS "IdCliente"
                ,TRANSITO."MontoCompra"										AS "Monto"                            
                ,'TR'                                               		AS "Categoria"  
			FROM	"BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"      	AS TRANSITO
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"	AS TOPER 
					ON ( TRANSITO."IdTipoOperacion" = TOPER."IdTipoOperacion" 
						AND TRANSITO."IdClasificacionTipoInstrumento" 	= TOPER."IdClasificacionTipoInstrumento"
			        	AND TRANSITO."IdTipoInstrumento" 				= TOPER."IdTipoInstrumento")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 		AS CLI 
			        ON (CLI."IdEmpresa" = TRANSITO."IdEmpresa" and CLI."IdCliente" = TRANSITO."IdCliente")
			    INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"       PRODU
			        ON ('CUST' = PRODU."IdTipoProducto")
			  	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 						AS CONCEPTO 
			        ON (TRANSITO."IdConcepto" = CONCEPTO."IdConcepto")
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      	AS INDCLI
					ON IndCli."IdCliente" =  TRANSITO."IdCliente" 	AND INDCLI."CodigoTipoIndicador"  = 'RELCORR'
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS CONTRA
					ON CONTRA."IdPersona" = CLI."IdPersona"
			WHERE 	TRANSITO."IdEstado" = 'PEN' 
			 		AND TRANSITO."IdBoveda" = 'DCV' AND TRANSITO."IdTipoOperacion" IN ('RETTIT','INGTIT')
	UNION				
        SELECT  -- MESA RF
        		ROW_NUMBER() OVER (ORDER BY  oper."IdOperacion")    AS "IdInterno"                          
				--1 									AS "IdInterno"
                ,OPER."FolioOperacion"  								AS "FolioOperacion"
                ,OPER."IdTipoOperacion" 								AS "IdTipoOperacion"
                ,CAR."Nemotecnico" 										AS "Nemo"                                    
                ,OPER."IdPortafolio" 									AS "IdPortafolio"
                ,OPER."IdCliente" 										AS "IdCliente"                
                ,DET."IngMontoValorizado" 								AS "Monto"                                    
                ,'MA'    										AS "Categoria"              
        FROM    "BSCL.Mesa.BD::MES.Operacion" 													AS OPER
                LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" 										AS DET
                ON OPER."IdOperacion" = DET."IdOperacion"
                LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 										AS CAR
                ON CAR."FolioCartera" = DET."FolioCarteraCustodia"              
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 					AS Cli 
                ON (Cli."IdEmpresa" = OPER."IdEmpresa" AND Cli."IdCliente" = OPER."IdCliente")
                LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 								AS Ins 
                ON (DET."IdInstrumento" = Ins."IdInstrumento")
                LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" 								AS T_Ope
                ON OPER."IdTipoProducto" = T_Ope."IdTipoProducto" AND OPER."IdTipoOperacion" = T_Ope."IdTipoOperacion"
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" 				AS PRODU
                ON (OPER."IdTipoProducto" = PRODU."IdTipoProducto")
                LEFT JOIN "BSCL.Mesa.BD::MES.ConceptoMesa" 								AS CONCEPTO_MES
                ON OPER."IdTipoOperacion" = CONCEPTO_MES."IdTipoOperacion" AND 
                OPER."IdTipoProducto" = CONCEPTO_MES."IdTipoProducto" AND 
                CONCEPTO_MES."NombreEntidad" = 'INGRESO'
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" 					AS CONCEPTO_PLA
                ON CONCEPTO_MES."IdConcepto" = CONCEPTO_PLA."IdConcepto"                 
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      	AS IndCli
				ON IndCli."IdCliente" =  OPER."IdCliente" 	AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS Contra
				ON Contra."IdPersona" = Cli."IdPersona"				
        WHERE   OPER."IdTipoOperacion" IN ('COMTER', 'VENTER', 'COMPAC', 'VENPAC') AND OPER."IdEstado" IN ('VIG', 'ANT', 'VCD');