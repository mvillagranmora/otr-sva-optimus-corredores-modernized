SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FMCarteraRFIF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FMCarteraRFIF" AS

   Select *
        FROM ( 
				SELECT 
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHA", -- CHAR(10)
		            "FolioCartera"  AS "SEC_CAR_INVER", -- DECIMAL
		            "FolioCartera"  AS "SEC_CAR_CORR", -- DECIMAL
		            "FolioCartera"  AS "SEC_CAR_CLI", -- VARCHAR(100)
		            1   AS "LIM_COMPROM", -- DECIMAL
		            0  AS "INTERMEDIADO", -- INT
		            DAYS_BETWEEN(fecPro."Valor",TO_DATE(Rf."FechaVencimientoInstrumento")) AS "DIASVCTO",--7      AS "DIASVCTO", -- DECIMAL
		            Rf."Cantidad" AS "NOMINAL",--8    
		            Rf."INGMontoATasaCompraActual" AS "MTOCOMPRAMONPAPEL", --9 --23-02-2011    
		            COALESCE ( "TasaCompra" , 0 )  AS "TIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( RF."INGMontoInteresCorridoCompraActual" , 0 )  AS "INTACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJACUMTIRCOMPRA", -- DECIMAL
		            Rf."INGMontoATasaCompraActual"  AS "MTOVPTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRAMAN", -- DECIMAL
		            Rf."INGTasaMercadoActual"  AS "TIRMCDO", -- DECIMAL
		            RF."INGMontoMercadoActualMonedaInstrumento"  AS "MTOVPTIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TASAMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDOBCS", -- DECIMAL
		            CASE 
			            WHEN RF."INGFechaPagoCorteCupon" IS NULL THEN ''
			            WHEN RF."INGFechaPagoCorteCupon" = '0001-01-01' THEN '' 
		           		WHEN RF."INGFechaPagoCorteCupon" = '1900-01-01' THEN '' 
		           		ELSE TO_VARCHAR(RF."INGFechaPagoCorteCupon", 'YYYY-MM-DD')
		           	END AS "FECPROXCUPON", -- CHAR(10)
		            Rf."INGMontoCorteCupon"  AS "VALPROXCUPON", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVALORVCTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOMONVALORVCTO", -- DECIMAL
		            CASE 
			  			WHEN Rf."IdClasificacionTipoInstrumento" = 'RF' THEN
			  				COALESCE(((Rf."INGCantidad" * Rf."MontoCompra") / NULLIF(Rf."Cantidad",0)),0)
			  			WHEN Rf."IdClasificacionTipoInstrumento" = 'RV' THEN
			  				(Rf."INGCantidad" * Rf."PrecioCompra")
			  			WHEN Rf."IdClasificacionTipoInstrumento" = 'IF' THEN
			  				COALESCE(((Rf."INGValorRescate" * Rf."MontoCompra") / NULLIF(Rf."ValorRescate",0)), 0)
			  		  END AS "MTOCOMPRAMONPESOS",
		            COALESCE (RF."INGNumeroCupon" , 0 )  AS "NUM_CUPON", -- INT
		            CASE WHEN INS."FrecuenciaCupon">12 THEN INS."FrecuenciaCupon"/30 ELSE COALESCE ( INS."FrecuenciaCupon" , 0 ) END  AS "FRECUENCIA_CUPON", -- INT
		            COALESCE ( INS."Tera" , 0 )  AS "TASATERA", -- DECIMAL
		            COALESCE ( RF."INGMontoCapitalCorteCupon" , 0 )  AS "INTERESDEVENGADO", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECHATASABCS", -- CHAR(10)
		            COALESCE (RF."INGMontoCapitalCorteCupon" , 0 )  AS "AMORTCUPON", -- DECIMAL
		            COALESCE (RF."INGMontoInteresCorteCupon" , 0 )  AS "INTECUPON", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "TIPOPRODUCTO", -- CHAR(4)
		            COALESCE(fecPro."Valor"||fecPro."Valor"||'LI'||fecPro."Valor"||' '||TO_VARCHAR(CURRENT_TIMESTAMP, 'HH24:MI:SS.FF3'),'')  AS "LLAVEREPORTE",
		            'SYSTEM'  AS "USUARIOLISTADO", -- CHAR(15)
		            CASE DOC."DocumentoIdentidad" 
			           	WHEN '76727730-K' THEN 'MALMCV'
			           	WHEN '99578880-2' THEN 'MALMSE'
			           	WHEN '77330030-5' THEN 'MALMCS'
			           	WHEN '96781620-5' THEN 'MALMMI'
		           	ELSE  'MALMMV' END AS "NOMBRELISTADO", -- CHAR(10)
		            CAST(Rf."FechaOperacion" AS VARCHAR(10))   AS "FECHACREACIONLST", -- DATETIME
		            '00:00:00.00'  AS "HORACREACIONLST", -- CHAR(11)
		            CAST(Rf."FechaOperacion" AS VARCHAR(7))   AS "CODPERLST", -- CHAR(7)
		            CAST(Rf."FechaOperacion" AS VARCHAR(10))  AS "FECHAANTERIORLST", -- CHAR(10)
		            CAST(Rf."FechaOperacion" AS VARCHAR(10))  AS "FECHAVALORIZACIONLST", -- CHAR(10)
		            ''  AS "CODTASALST", -- CHAR(2)
		            ''  AS "CODREAJLST", -- CHAR(3)
		            'LI'  AS "TIPOCARTERALST", -- CHAR(2)
		            0  AS "TIPOAGRUPLST", -- INT
		            "FolioCartera"  AS "NUMOPERACION", -- DECIMAL
		            Rf."FechaOperacion"  AS "FECHAINVERSION", -- CHAR(10)
		            Rf."FechaOperacion"  AS "FECHAINICIOPACTO", -- CHAR(10)
		            COALESCE(DOC."DocumentoIdentidad",CAST('' AS VARCHAR(10)))  AS "RUTCLIENTE", -- CHAR(11)
		            COALESCE(POR."CodigoPortafolio",CAST('' AS VARCHAR(10)))  AS "CODCLIENTE", -- DECIMAL
		            ''  AS "NOMBRECLIENTE", -- VARCHAR(130)
		            ''  AS "CUENTACLIENTE", -- VARCHAR(100)
		            CASE WHEN Rf."Nemotecnico" IN ('PAGARE NR', 'PAGARE R') THEN 'DP'
		            	 WHEN Rf."IdEmisor" = 'CENTRAL' THEN 'CE' 
		                 WHEN TIN."IdTipoInstrumento" IN ( 'BTU','BTP') THEN 'CE'
		                 ELSE 'BE' END  AS "TIPOFAMILIA", -- CHAR(3)
		            Rf."Nemotecnico" AS "NEMOTECNICO", -- CHAR(16)
		            Rf."FechaVencimientoInstrumento"  AS "FECHAVCTOPAPEL", -- CHAR(10)
		            Rf."FechaVencimientoInstrumento"  AS "FECHAVCTOPACTO", -- CHAR(10)
		            Rf."IdTipoReajuste"  AS "TIPOREAJPAPEL", -- CHAR(3)
		            Rf."IdTipoReajuste"  AS "TIPOREAJPACTO", -- CHAR(3)
		            Rf."TasaCompra"  AS "TASACOMPRA", -- DECIMAL
		            Rf."TasaCompra" AS "TASAPACTO", -- DECIMAL
		            "IdBoveda"  AS "CODBOVEDA", -- CHAR(3)    
		            ''  AS "CODRESERVA", -- CHAR(4) 
		            COALESCE(GAR."Cantidad",0)  AS "CANGARANTIA", -- DECIMAL
		            "PrecioCompra" AS "PRECIOPROMCOMPRA", -- DECIMAL
		            'CT'  AS "TIPOCOMPROM", -- CHAR(2)
		            RF."IdClasificacionTipoInstrumento"  AS "MERCADO", -- CHAR(2)
		            'N'  AS "INDFUNGIBILIDAD", -- CHAR(1)
		            COALESCE(DOC_EMI."DocumentoIdentidad",CAST('' AS VARCHAR(10)))AS "RUTEMISOR", -- CHAR(11)
		            Rf."Cantidad"  AS "CANTIDAD", -- DECIMAL
		            ''  AS "CLASRIESGO", -- CHAR(10)
		            Rf."IdEmisor"  AS "EMISOR", -- CHAR(40)
		            "TipoCarteraCorredor"  AS "TIPOCARTERA", -- CHAR(3)
		            CASE WHEN Rf."IdClasificacionTipoInstrumento" = 'RF' THEN COALESCE(CAST(INS."FechaEmision" AS VARCHAR(10)),'')  
		            	ELSE Rf."FechaOperacion" END 
		            AS "FECHAEMISIONPAPEL", -- CHAR(10)
		            Rf."IdEmisor"  AS "CODEMISOR", -- CHAR(10)
		            Rf."INGDurationCompraActual" AS "DURATION", -- DECIMAL
		            ' '  AS "CODFONDOLST", -- CHAR(10)
		            '96564330-3'  AS "RUTCORRLST", -- CHAR(11)
		            1  AS "CUENTACORRLST", -- DECIMAL
		            0  AS "TIRMCDOAYER", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONPACTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXPACTO", -- DECIMAL
		            0  AS "CODTIPOCARTERA", -- TINYINT
		            0  AS "CODCATIFRSID", -- TINYINT
		            0   AS "INDLIQUIDEZ", -- TINYINT
		            ''  AS "VTOCOMPADJ", -- CHAR(10)
		            0  AS "FACTOR", -- DECIMAL
		            1  AS "CODIGOINTERMEDIADA", -- TINYINT
		            0  AS "CODOFI", -- CHAR(7)
		            0  AS "CODSERIE", -- CHAR(10)
		            0  AS "TIRMED", -- DECIMAL
		            ''  AS "FECVCTOINSTRUMENTO", -- CHAR(10)
		            0 AS "NUMCUPONINSTRUMENTO", -- DECIMAL
		            ''  AS "FAMILIAINSTRUMENTO", -- CHAR(2)
		            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "TIPOCALCULO", -- CHAR(1)
		            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "INDBOLSON", -- CHAR(2)
		            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "ORINEMO" -- CHAR(1)
		            
		        FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" AS Rf
		        		LEFT JOIN BSCL."BSCL.Custodia.BD::GTA.Garantia" AS GAR ON Rf."IdCartera" = GAR."IdCarteraOrigen"
		    			LEFT JOIN BSCL."BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = Rf."Nemotecnico" )
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN ON TIN."IdTipoInstrumento" = INS."IdTipoInstrumento"
						LEFT JOIN BSCL."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON Rf."IdCliente" = CLI."IdCliente"
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
						LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = Rf."IdEmisor"
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER_EMI ON PER_EMI."IdPersona" = EMI."IdPersona"
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC_EMI ON DOC_EMI."IdDocumentoIdentidad" = PER_EMI."IdDocumentoIdentidad"
						LEFT JOIN BSCL."BSCL.Portafolio.BD::POR.Portafolio" AS POR ON Rf."IdPortafolio" = POR."IdPortafolio"
					 ,"BSCL.Plataforma.BD::PLA.Configuracion" AS fecPro                                                
			   WHERE "CodigoParametro" = 'PLA.FechaSistema' 
				AND RF."IdTipoInstrumento" != 'PAC'
				AND RF."IdClasificacionTipoInstrumento" IN ('RF','IF')
				AND Rf."IdEstado" = 'VIG'
			   )       				 
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FMCarteraRFIF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    