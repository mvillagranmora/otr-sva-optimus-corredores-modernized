SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFMCarteraRFIF"; 

--SELECT FECHA, SEC_CAR_INVER, SEC_CAR_CORR, SEC_CAR_CLI, LIM_COMPROM, INTERMEDIADO, DIASVCTO, NOMINAL, MTOCOMPRAMONPAPEL, TIRCOMPRA, INTDIATIRCOMPRA, REAJDIATIRCOMPRA, INTACUMTIRCOMPRA, REAJACUMTIRCOMPRA, MTOVPTIRCOMPRA, MTOVPTIRCOMPRAMAN, TIRMCDO, MTOVPTIRMCDO, MENVALORMCDO, MAYVALORMCDO, TASAMCDOBCS, MTOVPTIRMCDOBCS, MENVALORMCDOBCS, MAYVALORMCDOBCS, FECPROXCUPON, VALPROXCUPON, MTOVALORVCTO, MTOMONVALORVCTO, MTOCOMPRAMONPESOS, NUM_CUPON, FRECUENCIA_CUPON, TASATERA, INTERESDEVENGADO, FECHATASABCS, AMORTCUPON, INTECUPON, TIPOPRODUCTO, LLAVEREPORTE, USUARIOLISTADO, NOMBRELISTADO, FECHACREACIONLST, HORACREACIONLST, CODPERLST, FECHAANTERIORLST, FECHAVALORIZACIONLST, CODTASALST, CODREAJLST, TIPOCARTERALST, TIPOAGRUPLST, NUMOPERACION, FECHAINVERSION, FECHAINICIOPACTO, RUTCLIENTE, CODCLIENTE, NOMBRECLIENTE, CUENTACLIENTE, TIPOFAMILIA, NEMOTECNICO, FECHAVCTOPAPEL, FECHAVCTOPACTO, TIPOREAJPAPEL, TIPOREAJPACTO, TASACOMPRA, TASAPACTO, CODBOVEDA, CODRESERVA, CANGARANTIA, PRECIOPROMCOMPRA, TIPOCOMPROM, MERCADO, INDFUNGIBILIDAD, RUTEMISOR, CANTIDAD, CLASRIESGO, EMISOR, TIPOCARTERA, FECHAEMISIONPAPEL, CODEMISOR, DURATION, CODFONDOLST, RUTCORRLST, CUENTACORRLST, TIRMCDOAYER, DURATIONMCDO, DURATIONPACTO, CONVEXCOMPRA, CONVEXMCDO, CONVEXPACTO, CODTIPOCARTERA, CODCATIFRSID, INDLIQUIDEZ, VTOCOMPADJ, FACTOR, CODIGOINTERMEDIADA, CODOFI, CODSERIE, TIRMED, FECVCTOINSTRUMENTO, NUMCUPONINSTRUMENTO, FAMILIAINSTRUMENTO, TIPOCALCULO, INDBOLSON, ORINEMO  FROM BSCL."BSCL.Replica.BD.Vistas::ReplicaFMCarteraRFIF"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFMCarteraRFIF" AS

   Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION, ''  AS TABLE_KEY, tb.* 
        FROM ( 
        	SELECT 
        			'CIRF' as tipo,
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHA", -- CHAR(10)
		            SEC_CAR_INVER  AS "SEC_CAR_INVER", -- DECIMAL
		            SEC_CAR_INVER  AS "SEC_CAR_CORR", -- DECIMAL
		            SEC_CAR_INVER  AS "SEC_CAR_CLI", -- VARCHAR(100)
		            0   AS "LIM_COMPROM", -- DECIMAL
		            0  AS "INTERMEDIADO", -- INT
		            DAYS_BETWEEN(fecPro."Valor",FEC_VCTO) AS "DIASVCTO",--7      AS "DIASVCTO", -- DECIMAL
		            (   Case    When 'CPICAR'='CPICAR' Or 'CPICAR'='CPLCFD' Then    
		                                    Rf.Can_Actual + Rf.Can_Com_Plazo + Rf.Can_Garantia -- CVF agrega garantia    
		                                Else   
		                                    Rf.Can_Actual + Rf.Can_Tramite_Com --23-02-2011    
		                        End) AS "NOMINAL",--8    
		                        
		            (   Case    When 'CPICAR' ='CPICAR' Or 'CPICAR' ='CPLCFD' Then    
		                            (((Rf.Can_Actual + Rf.Can_Com_Plazo + Rf.Can_Garantia) / (Rf.Can_Actual+Rf.Can_Tramite_Com+Rf.Can_Garantia+Rf.Can_Com_plazo)) * Rf.Monto_Ini_Ajus)    
		                        Else   
		                            (((Rf.Can_Actual+Rf.Can_Tramite_Com) / (Rf.Can_Actual+Rf.Can_Tramite_Com+Rf.Can_Garantia+Rf.Can_Com_plazo)) * Rf.Monto_Ini_Ajus)    
		            End) AS "MTOCOMPRAMONPAPEL", --9 --23-02-2011    
		            COALESCE ( TIR_PROM_COMP , 0 )  AS "TIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRAMAN", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TASAMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDOBCS", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECPROXCUPON", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "VALPROXCUPON", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVALORVCTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOMONVALORVCTO", -- DECIMAL
		             (   Case    When 'CPICAR'='CPICAR' Or 'CPICAR' ='CPLCFD' Then    
		                                    (((Rf.Can_Actual + Rf.Can_Com_Plazo + Rf.Can_Garantia) / (Rf.Can_Actual+Rf.Can_Tramite_Com+Rf.Can_Garantia+Rf.Can_Com_plazo)) * Rf.Monto_Ini_Ajus)    
		                                Else   
		                                    (((Rf.Can_Actual+Rf.Can_Tramite_Com) / (Rf.Can_Actual+Rf.Can_Tramite_Com+Rf.Can_Garantia+Rf.Can_Com_plazo)) * Rf.Monto_Ini_Ajus)    
		                        End)  AS "MTOCOMPRAMONPESOS", -- DECIMAL
		            COALESCE ( "NUMERO_CUPON" , 0 )  AS "NUM_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "FRECUENCIA_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "TASATERA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTERESDEVENGADO", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECHATASABCS", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "AMORTCUPON", -- DECIMAL
		            COALESCE ( "MontoInteresCorteCupon" , 0 )  AS "INTECUPON", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "TIPOPRODUCTO", -- CHAR(4)
		           '2022-08-222022-08-23XXX2022-08-22 18:57:20.580'  AS "LLAVEREPORTE", -- CHAR(50)
		            'SYSTEM'  AS "USUARIOLISTADO", -- CHAR(15)
		           'CPLVPC72  '  AS "NOMBRELISTADO", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))   AS "FECHACREACIONLST", -- DATETIME
		            '00:00:00.00'  AS "HORACREACIONLST", -- CHAR(11)
		            CAST(fecPro."Valor" AS VARCHAR(7))   AS "CODPERLST", -- CHAR(7)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAANTERIORLST", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAVALORIZACIONLST", -- CHAR(10)
		            'C'  AS "CODTASALST", -- CHAR(2)
		            ''  AS "CODREAJLST", -- CHAR(3)
		            'PT'  AS "TIPOCARTERALST", -- CHAR(2)
		            0  AS "TIPOAGRUPLST", -- INT
		            Rf.Sec_Car_Inver  AS "NUMOPERACION", -- DECIMAL
		            Rf.Fec_Inver  AS "FECHAINVERSION", -- CHAR(10)
		            ''  AS "FECHAINICIOPACTO", -- CHAR(10)
		            Rf.Rut_Cli  AS "RUTCLIENTE", -- CHAR(11)
		            Rf.Sec_Rut_Cli  AS "CODCLIENTE", -- DECIMAL
		            ''  AS "NOMBRECLIENTE", -- VARCHAR(130)
		            ''  AS "CUENTACLIENTE", -- VARCHAR(100)
		            Rf.Tipo_Fam  AS "TIPOFAMILIA", -- CHAR(3)
		             Rf.Nemo  AS "NEMOTECNICO", -- CHAR(16)
		            Rf.Fec_Vcto  AS "FECHAVCTOPAPEL", -- CHAR(10)
		            Rf.Fec_Vcto_com  AS "FECHAVCTOPACTO", -- CHAR(10)
		            Rf.Tipo_Reaj  AS "TIPOREAJPAPEL", -- CHAR(3)
		            ''  AS "TIPOREAJPACTO", -- CHAR(3)
		            Rf.Tir_Prom_Comp  AS "TASACOMPRA", -- DECIMAL
		            0 AS "TASAPACTO", -- DECIMAL
		            Rf.Cod_Boveda  AS "CODBOVEDA", -- CHAR(3)
		            Rf.Cod_Reserva  AS "CODRESERVA", -- CHAR(4)
		            Rf.Can_Garantia  AS "CANGARANTIA", -- DECIMAL
		            Rf.Precio_Prom_Comp  AS "PRECIOPROMCOMPRA", -- DECIMAL
		           'CT'  AS "TIPOCOMPROM", -- CHAR(2)
		            'RF'  AS "MERCADO", -- CHAR(2)
		            'N'  AS "INDFUNGIBILIDAD", -- CHAR(1)
		            ''  AS "RUTEMISOR", -- CHAR(11)
		            Rf.Can_Actual  AS "CANTIDAD", -- DECIMAL
		           ''  AS "CLASRIESGO", -- CHAR(10)
		           Rf.Emisor  AS "EMISOR", -- CHAR(40)
		            Rf.Tipo_Cartera  AS "TIPOCARTERA", -- CHAR(3)
		            COALESCE(CAST(INS."FechaEmision" AS VARCHAR(10)),'')  AS "FECHAEMISIONPAPEL", -- CHAR(10)
		           Rf.Emisor  AS "CODEMISOR", -- CHAR(10)
		           0  AS "DURATION", -- DECIMAL--- pendiente
		            'CPRVSA    '  AS "CODFONDOLST", -- CHAR(10)
		            ''  AS "RUTCORRLST", -- CHAR(11)
		            0  AS "CUENTACORRLST", -- DECIMAL
		            0  AS "TIRMCDOAYER", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONPACTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXPACTO", -- DECIMAL
		            0  AS "CODTIPOCARTERA", -- TINYINT
		            Rf.CODCATIFRSID  AS "CODCATIFRSID", -- TINYINT
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
		        FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRF" AS Rf
		    			LEFT JOIN BSCL."BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = Rf.Nemo )
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN ON TIN."IdTipoInstrumento" = INS."IdTipoInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = EMI."IdPersona"
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
						,
						 "BSCL.Plataforma.BD::PLA.Configuracion" AS fecPro WHERE "CodigoParametro" = 'PLA.FechaSistema'
		        UNION ALL 
		   	  Select
		   	  		'CIIF' as tipo,
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHA", -- CHAR(10)
		            SEC_CAR_INVER  AS "SEC_CAR_INVER", -- DECIMAL
		            SEC_CAR_INVER  AS "SEC_CAR_CORR", -- DECIMAL
		            SEC_CAR_INVER  AS "SEC_CAR_CLI", -- VARCHAR(100)
		            0   AS "LIM_COMPROM", -- DECIMAL
		            0  AS "INTERMEDIADO", -- INT
		            DAYS_BETWEEN(fecPro."Valor",FEC_VCTO) AS "DIASVCTO",--7      AS "DIASVCTO", -- DECIMAL
		            CASE   
					      WHEN Rf.IND_FUNGIBILIDAD =  'S' then Rf.VAL_RESC  
					         ELSE ((Rf.VAL_RESC)*(Rf.Can_Actual + Rf.Can_Com_plazo + Rf.Can_Garantia)) 
					     END AS "NOMINAL",--8    
					 (Case When 'CPICAR'='CPICAR' Or 'CPICAR' ='CPLCFD' Then    
					     (CASE   
					      WHEN Rf.IND_FUNGIBILIDAD =  'S' then Rf.monto_ini_ajus
					         ELSE
					         ((Rf.Can_Actual + Rf.Can_Com_plazo + Rf.Can_Garantia) * Rf.monto_ini_ajus)
					     END)    
					    Else   
					     (CASE   
					      WHEN Rf.IND_FUNGIBILIDAD =  'S' then Rf.monto_ini_ajus    
					      ELSE((Rf.Can_Actual + Rf.Can_Tramite_Com) * Rf.monto_ini_ajus)    
					     END)    
					    End) AS "MTOCOMPRAMONPAPEL", --9 --23-02-2011    
		            COALESCE ( Rf.Tasa_Prom_Comp , 0 )  AS "TIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRAMAN", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TASAMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDOBCS", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECPROXCUPON", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "VALPROXCUPON", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVALORVCTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOMONVALORVCTO", -- DECIMAL
		             (Case When 'CPICAR'='CPICAR' Or 'CPICAR' ='CPLCFD'  Then    
						     (CASE
						      WHEN Rf.IND_FUNGIBILIDAD = 'S' and Rf.VAL_RESC  > 0 THEN ((Rf.val_resc * Rf.monto_ini_ajus)/( Rf.val_resc + Rf.val_resc_plazo))    
						      WHEN Rf.IND_FUNGIBILIDAD = 'S' THEN Rf.monto_ini_ajus
						      ELSE ((((Rf.Can_Actual + Rf.Can_Com_plazo + Rf.Can_Garantia ) / (Rf.Can_Actual + Rf.Can_Tramite_Com+ Rf.Can_Garantia+ Rf.Can_Com_plazo)) * Rf.Monto_Ini_Ajus) * (Rf.Can_Actual + Rf.Can_Tramite_Com+ Rf.Can_Garantia+ Rf.Can_Com_plazo))
						     END)    
						    Else   
						    (CASE
						      WHEN Rf.IND_FUNGIBILIDAD = 'S' and Rf.VAL_RESC  > 0 THEN ((Rf.val_resc * Rf.monto_ini_ajus)/( Rf.val_resc + Rf.val_resc_plazo))    
						      WHEN Rf.IND_FUNGIBILIDAD = 'S' THEN Rf.monto_ini_ajus
						      ELSE ((((Rf.Can_Actual+ Rf.Can_Tramite_Com) / (Rf.Can_Actual + Rf.Can_Tramite_Com+ Rf.Can_Garantia+ Rf.Can_Com_plazo)) * Rf.Monto_Ini_Ajus) * (Rf.Can_Actual + Rf.Can_Tramite_Com+ Rf.Can_Garantia+ Rf.Can_Com_plazo))
						    END)    
						    End)   AS "MTOCOMPRAMONPESOS", -- DECIMAL
		            COALESCE (0 , 0 )  AS "NUM_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "FRECUENCIA_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "TASATERA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTERESDEVENGADO", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECHATASABCS", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "AMORTCUPON", -- DECIMAL
		            COALESCE (0, 0 )  AS "INTECUPON", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "TIPOPRODUCTO", -- CHAR(4)
		           '2022-08-222022-08-23XXX2022-08-22 18:57:20.580'  AS "LLAVEREPORTE", -- CHAR(50)
		            'SYSTEM'  AS "USUARIOLISTADO", -- CHAR(15)
		           'CPLVPC72  '  AS "NOMBRELISTADO", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))   AS "FECHACREACIONLST", -- DATETIME
		            '00:00:00.00'  AS "HORACREACIONLST", -- CHAR(11)
		            CAST(fecPro."Valor" AS VARCHAR(7))   AS "CODPERLST", -- CHAR(7)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAANTERIORLST", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAVALORIZACIONLST", -- CHAR(10)
		            'C'  AS "CODTASALST", -- CHAR(2)
		            ''  AS "CODREAJLST", -- CHAR(3)
		            'PT'  AS "TIPOCARTERALST", -- CHAR(2)
		            0  AS "TIPOAGRUPLST", -- INT
		            Rf.Sec_Car_Inver  AS "NUMOPERACION", -- DECIMAL
		            Rf.Fec_Inver  AS "FECHAINVERSION", -- CHAR(10)
		            ''  AS "FECHAINICIOPACTO", -- CHAR(10)
		            Rf.Rut_Cli  AS "RUTCLIENTE", -- CHAR(11)
		            Rf.Sec_Rut_Cli  AS "CODCLIENTE", -- DECIMAL
		            ''  AS "NOMBRECLIENTE", -- VARCHAR(130)
		            ''  AS "CUENTACLIENTE", -- VARCHAR(100)
		            Rf.Tipo_Fam  AS "TIPOFAMILIA", -- CHAR(3)
		             Rf.Nemo  AS "NEMOTECNICO", -- CHAR(16)
		            Rf.Fec_Vcto  AS "FECHAVCTOPAPEL", -- CHAR(10)
		            Rf.Fec_Vcto_com  AS "FECHAVCTOPACTO", -- CHAR(10)
		            Rf.Tipo_Reaj  AS "TIPOREAJPAPEL", -- CHAR(3)
		            ''  AS "TIPOREAJPACTO", -- CHAR(3)
		            Rf.TASA_PROM_COMP  AS "TASACOMPRA", -- DECIMAL
		            0 AS "TASAPACTO", -- DECIMAL
		            Rf.Cod_Boveda  AS "CODBOVEDA", -- CHAR(3)
		            Rf.Cod_Reserva  AS "CODRESERVA", -- CHAR(4)
		            Rf.Can_Garantia  AS "CANGARANTIA", -- DECIMAL
		            Rf.Precio_Prom_Comp  AS "PRECIOPROMCOMPRA", -- DECIMAL
		           'CT'  AS "TIPOCOMPROM", -- CHAR(2)
		            'IF'  AS "MERCADO", -- CHAR(2)
		            rf.Ind_Fungibilidad   AS "INDFUNGIBILIDAD", -- CHAR(1)
		            ''  AS "RUTEMISOR", -- CHAR(11)
		            Rf.Can_Actual  AS "CANTIDAD", -- DECIMAL
		           ''  AS "CLASRIESGO", -- CHAR(10)
		           Rf.Emisor  AS "EMISOR", -- CHAR(40)
		            Rf.Tipo_Cartera  AS "TIPOCARTERA", -- CHAR(3)
		            COALESCE(CAST(INS."FechaEmision" AS VARCHAR(10)),'')   AS "FECHAEMISIONPAPEL", -- CHAR(10)-- doc."DocumentoIdentidad"
		           Rf.Emisor  AS "CODEMISOR", -- CHAR(10)
		           0  AS "DURATION", -- DECIMAL--- pendiente
		            'CPRVSA    '  AS "CODFONDOLST", -- CHAR(10)
		            ''  AS "RUTCORRLST", -- CHAR(11)
		            0  AS "CUENTACORRLST", -- DECIMAL
		            0  AS "TIRMCDOAYER", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONPACTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXPACTO", -- DECIMAL
		            0  AS "CODTIPOCARTERA", -- TINYINT
		            Rf.CODCATIFRSID  AS "CODCATIFRSID", -- TINYINT
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
		        FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF" AS Rf
		    			LEFT JOIN BSCL."BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = Rf.Nemo )
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN ON TIN."IdTipoInstrumento" = INS."IdTipoInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = EMI."IdPersona"
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
						,
						 "BSCL.Plataforma.BD::PLA.Configuracion" AS fecPro 
				WHERE "CodigoParametro" = 'PLA.FechaSistema'
				and   
				(Rf.Can_Actual + Rf.Can_Com_plazo + Rf.Can_Garantia <> 0)    
		      And (Rf.Can_Actual <> 0 or Rf.Can_Com_plazo <> 0 or Rf.Can_Garantia <> 0) 
		        UNION ALL 
		        Select 
		        	'PACTO RF' as tipo,
		        	CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHA", -- CHAR(10)
		            Det.Sec_Comprom  AS "SEC_CAR_INVER", -- DECIMAL
		            Det.Sec_Car_Corr  AS "SEC_CAR_CORR", -- DECIMAL
		            Det.Sec_Car_Cli  AS "SEC_CAR_CLI", -- VARCHAR(100)
		            Det.Lin_Comprom   AS "LIM_COMPROM", -- DECIMAL
		            0  AS "INTERMEDIADO", -- INT
		            DAYS_BETWEEN(fecPro."Valor",FEC_VCTO) AS "DIASVCTO",--7      AS "DIASVCTO", -- DECIMAL
		           Det.Cantidad AS "NOMINAL",--8    
				   Monto_Ini_Cap AS "MTOCOMPRAMONPAPEL", --9 --23-02-2011    
		            COALESCE (0 , 0 )  AS "TIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRAMAN", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TASAMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDOBCS", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECPROXCUPON", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "VALPROXCUPON", -- DECIMAL
		            COALESCE ( Det.Monto_Comprom , 0 )  AS "MTOVALORVCTO", -- DECIMAL
		            COALESCE ( Det.Cant_Pc_Comprom , 0 )  AS "MTOMONVALORVCTO", -- DECIMAL
		            Monto_Ini_Cap   AS "MTOCOMPRAMONPESOS", -- DECIMAL
		            COALESCE (0 , 0 )  AS "NUM_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "FRECUENCIA_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "TASATERA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTERESDEVENGADO", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECHATASABCS", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "AMORTCUPON", -- DECIMAL
		            COALESCE (0, 0 )  AS "INTECUPON", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "TIPOPRODUCTO", -- CHAR(4)
		           '2022-08-222022-08-23XXX2022-08-22 18:57:20.580'  AS "LLAVEREPORTE", -- CHAR(50)
		            'SYSTEM'  AS "USUARIOLISTADO", -- CHAR(15)
		           'CPLVPC72  '  AS "NOMBRELISTADO", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))   AS "FECHACREACIONLST", -- DATETIME
		            '00:00:00.00'  AS "HORACREACIONLST", -- CHAR(11)
		            CAST(fecPro."Valor" AS VARCHAR(7))   AS "CODPERLST", -- CHAR(7)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAANTERIORLST", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAVALORIZACIONLST", -- CHAR(10)
		            'C'  AS "CODTASALST", -- CHAR(2)
		            ''  AS "CODREAJLST", -- CHAR(3)
		            'PT'  AS "TIPOCARTERALST", -- CHAR(2)
		            0  AS "TIPOAGRUPLST", -- INT
		            Det.Sec_Car_Corr  AS "NUMOPERACION", -- DECIMAL
		            Cab.Fec_Operac  AS "FECHAINVERSION", -- CHAR(10)
		            Cab.Fec_Operac  AS "FECHAINICIOPACTO", -- CHAR(10)
		            Cab.Rut_Cli  AS "RUTCLIENTE", -- CHAR(11)
		            Cab.Sec_Rut_Cli  AS "CODCLIENTE", -- DECIMAL
		            ''  AS "NOMBRECLIENTE", -- VARCHAR(130)
		            ''  AS "CUENTACLIENTE", -- VARCHAR(100)
		            Det.Tipo_Fam  AS "TIPOFAMILIA", -- CHAR(3)
		             Det.Nemo  AS "NEMOTECNICO", -- CHAR(16)
		            Det.Fec_Vcto  AS "FECHAVCTOPAPEL", -- CHAR(10)
		            Cab.Fec_Vcto_com  AS "FECHAVCTOPACTO", -- CHAR(10)
		            Det.Tipo_Reaj  AS "TIPOREAJPAPEL", -- CHAR(3)
		            Cab.Tipo_Reaj_Comp  AS "TIPOREAJPACTO", -- CHAR(3)
		            Det.Tir  AS "TASACOMPRA", -- DECIMAL
		            Cab.Tasa_Comprom AS "TASAPACTO", -- DECIMAL
		            Det.Cod_Boveda  AS "CODBOVEDA", -- CHAR(3)
		            '0'  AS "CODRESERVA", -- CHAR(4)
		            0 AS "CANGARANTIA", -- DECIMAL
		            Det.Precio AS "PRECIOPROMCOMPRA", -- DECIMAL
		            Cab.Tipo_Comprom  AS "TIPOCOMPROM", -- CHAR(2)
		            'RF'  AS "MERCADO", -- CHAR(2)
		            'N'  AS "INDFUNGIBILIDAD", -- CHAR(1)
		            ''  AS "RUTEMISOR", -- CHAR(11)
		            Det.Cantidad  AS "CANTIDAD", -- DECIMAL
		           ''  AS "CLASRIESGO", -- CHAR(10)
		          Det.Emisor  AS "EMISOR", -- CHAR(40)
		            Det.Tipo_Cartera  AS "TIPOCARTERA", -- CHAR(3)
		            COALESCE(CAST(INS."FechaEmision" AS VARCHAR(10)),'')   AS "FECHAEMISIONPAPEL", -- CHAR(10)--doc."DocumentoIdentidad" 
		           Det.Emisor  AS "CODEMISOR", -- CHAR(10)
		           0  AS "DURATION", -- DECIMAL--- pendiente
		            'CPRVSA    '  AS "CODFONDOLST", -- CHAR(10)
		            ''  AS "RUTCORRLST", -- CHAR(11)
		            0  AS "CUENTACORRLST", -- DECIMAL
		            0  AS "TIRMCDOAYER", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONPACTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXPACTO", -- DECIMAL
		            0  AS "CODTIPOCARTERA", -- TINYINT
		            0 AS "CODCATIFRSID", -- TINYINT
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
		    From  "BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC"  Cab     
		    	Inner Join "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC" Det On (Cab.Sec_Comprom = Det.Sec_Comprom And Det.Mercado ='RF'  
		                                               And Det.Cod_Per=Cab.Cod_per And Det.Cantidad > 0  )
					LEFT JOIN BSCL."BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = Det.Nemo )
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN ON TIN."IdTipoInstrumento" = INS."IdTipoInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = EMI."IdPersona"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad",
					 "BSCL.Plataforma.BD::PLA.Configuracion" AS fecPro                                                
			   Where 
			   "CodigoParametro" = 'PLA.FechaSistema' 
			   AND Cab.Cod_per= LEFT(fecPro."Valor",7)              
			   And Det.Fec_Liquid > fecPro."Valor"      
			   And Cab.Tipo_Comprom In ('CP','VP')    
			   And Cab.Estado In ('E','P')        
		      UNION all
		 	Select 
		 			'PACTO IF' as tipo,
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHA", -- CHAR(10)
		            Det.Sec_Comprom  AS "SEC_CAR_INVER", -- DECIMAL
		            Det.Sec_Car_Corr  AS "SEC_CAR_CORR", -- DECIMAL
		            Det.Sec_Car_Cli  AS "SEC_CAR_CLI", -- VARCHAR(100)
		            Det.Lin_Comprom   AS "LIM_COMPROM", -- DECIMAL
		            0  AS "INTERMEDIADO", -- INT
		            DAYS_BETWEEN(fecPro."Valor",FEC_VCTO) AS "DIASVCTO",--7      AS "DIASVCTO", -- DECIMAL
		           (Det.Val_resc*Det.Cantidad) AS "NOMINAL",--8    
				   Monto_Ini_Cap*Det.Cantidad AS "MTOCOMPRAMONPAPEL", --9 --23-02-2011    
		            COALESCE (0 , 0 )  AS "TIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRAMAN", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TASAMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDOBCS", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECPROXCUPON", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "VALPROXCUPON", -- DECIMAL
		            COALESCE ( Det.Monto_Comprom*Det.Cantidad , 0 )  AS "MTOVALORVCTO", -- DECIMAL
		            COALESCE (  (Det.Cant_Pc_Comprom*Det.Cantidad) , 0 )  AS "MTOMONVALORVCTO", -- DECIMAL
		            (Monto_Ini_Cap*Det.Cantidad)   AS "MTOCOMPRAMONPESOS", -- DECIMAL          
		            COALESCE (0 , 0 )  AS "NUM_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "FRECUENCIA_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "TASATERA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTERESDEVENGADO", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECHATASABCS", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "AMORTCUPON", -- DECIMAL
		            COALESCE (0, 0 )  AS "INTECUPON", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "TIPOPRODUCTO", -- CHAR(4)
		           '2022-08-222022-08-23XXX2022-08-22 18:57:20.580'  AS "LLAVEREPORTE", -- CHAR(50)
		            'SYSTEM'  AS "USUARIOLISTADO", -- CHAR(15)
		           'CPLVPC72  '  AS "NOMBRELISTADO", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))   AS "FECHACREACIONLST", -- DATETIME
		            '00:00:00.00'  AS "HORACREACIONLST", -- CHAR(11)
		            CAST(fecPro."Valor" AS VARCHAR(7))   AS "CODPERLST", -- CHAR(7)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAANTERIORLST", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAVALORIZACIONLST", -- CHAR(10)
		            'C'  AS "CODTASALST", -- CHAR(2)
		            ''  AS "CODREAJLST", -- CHAR(3)
		            'PT'  AS "TIPOCARTERALST", -- CHAR(2)
		            0  AS "TIPOAGRUPLST", -- INT
		            Det.Sec_Car_Corr  AS "NUMOPERACION", -- DECIMAL
		            Cab.Fec_Operac  AS "FECHAINVERSION", -- CHAR(10)
		            Cab.Fec_Operac  AS "FECHAINICIOPACTO", -- CHAR(10)
		            Cab.Rut_Cli  AS "RUTCLIENTE", -- CHAR(11)
		            Cab.Sec_Rut_Cli  AS "CODCLIENTE", -- DECIMAL
		            ''  AS "NOMBRECLIENTE", -- VARCHAR(130)
		            ''  AS "CUENTACLIENTE", -- VARCHAR(100)
		            Det.Tipo_Fam  AS "TIPOFAMILIA", -- CHAR(3)
		             Det.Nemo  AS "NEMOTECNICO", -- CHAR(16)
		            Det.Fec_Vcto  AS "FECHAVCTOPAPEL", -- CHAR(10)
		            Cab.Fec_Vcto_com  AS "FECHAVCTOPACTO", -- CHAR(10)
		            Det.Tipo_Reaj  AS "TIPOREAJPAPEL", -- CHAR(3)
		            Cab.Tipo_Reaj_Comp  AS "TIPOREAJPACTO", -- CHAR(3)
		            Det.Tir  AS "TASACOMPRA", -- DECIMAL
		            Cab.Tasa_Comprom AS "TASAPACTO", -- DECIMAL
		            Det.Cod_Boveda  AS "CODBOVEDA", -- CHAR(3)
		            '0'  AS "CODRESERVA", -- CHAR(4)
		            0 AS "CANGARANTIA", -- DECIMAL
		            Det.Precio AS "PRECIOPROMCOMPRA", -- DECIMAL
		            Cab.Tipo_Comprom  AS "TIPOCOMPROM", -- CHAR(2)
		            'IF'  AS "MERCADO", -- CHAR(2)
		             (Case When Det.Ind_Fungibilidad = '' Then 'N' Else Det.Ind_Fungibilidad End)  AS "INDFUNGIBILIDAD", -- CHAR(1)
		            ''  AS "RUTEMISOR", -- CHAR(11)
		            Det.Cantidad  AS "CANTIDAD", -- DECIMAL
		           ''  AS "CLASRIESGO", -- CHAR(10)
		          Det.Emisor  AS "EMISOR", -- CHAR(40)
		            Det.Tipo_Cartera  AS "TIPOCARTERA", -- CHAR(3)
		             COALESCE(CAST(INS."FechaEmision" AS VARCHAR(10)),'')  AS "FECHAEMISIONPAPEL", -- CHAR(10) doc."DocumentoIdentidad" 
		           Det.Emisor  AS "CODEMISOR", -- CHAR(10)
		           0  AS "DURATION", -- DECIMAL--- pendiente
		            'CPRVSA    '  AS "CODFONDOLST", -- CHAR(10)
		            ''  AS "RUTCORRLST", -- CHAR(11)
		            0  AS "CUENTACORRLST", -- DECIMAL
		            0  AS "TIRMCDOAYER", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONPACTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXPACTO", -- DECIMAL
		            0  AS "CODTIPOCARTERA", -- TINYINT
		            0 AS "CODCATIFRSID", -- TINYINT
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
		    From  "BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC"  Cab     
		    	Inner Join "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC" Det On (Cab.Sec_Comprom = Det.Sec_Comprom And Det.Mercado ='IF'  
		                                               And Det.Cod_Per=Cab.Cod_per And Det.Cantidad > 0  )
					LEFT JOIN BSCL."BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = Det.Nemo )
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN ON TIN."IdTipoInstrumento" = INS."IdTipoInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = EMI."IdPersona"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad",
					 "BSCL.Plataforma.BD::PLA.Configuracion" AS fecPro                                                
			   Where 
			   "CodigoParametro" = 'PLA.FechaSistema' 
			   AND Cab.Cod_per= LEFT(fecPro."Valor",7)              
			   And Det.Fec_Liquid > fecPro."Valor"      
			   And Cab.Tipo_Comprom In ('CP','VP')    
			   And Cab.Estado In ('E','P')        
      		) AS tb
			   	left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FMCarteraRFIF' --AND WST."Key" = DUM."Key"
        				 
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFMCarteraRFIF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
