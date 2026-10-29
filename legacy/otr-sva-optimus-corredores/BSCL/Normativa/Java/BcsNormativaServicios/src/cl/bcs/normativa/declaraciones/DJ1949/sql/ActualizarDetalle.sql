UPDATE TRI
            SET  
                --TRI."RutSociedad" = (TRIM(TRI."RutSociedad")),
                --TRI."RutCliente" = (TRIM(TRI."RutCliente")),
                TRI."MontoActualizado"                        = TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)
                ,TRI."MtoDivReaAfecGCCCIDPC_C6"               = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_08",0)) END
                ,TRI."MtoDivReaAfecGCCCIDPCAL311216_C7"       = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_09",0)) END
                ,TRI."MtoDivReaAfecGCCDIDPCVol_C8"            = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_10",0)) END
                ,TRI."MtoDivReaAfecGCSinDer_C9"               = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_11",:p_Afecto100)) END 
                ,TRI."MtoDivReaREXTribCumpOtrasRentas_C11"    = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_13",0)) END
                ,TRI."MtoDivReaREXTribCumpRentasRap_C10"      = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_12",0)) END
                ,TRI."MtoDivReaREXTribCumpExcDist_C12"        = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_14",0)) END
                ,TRI."MtoDivReaREXTribCumpUtiISFUT_C13"       = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_15",0)) ELSE 0  END 
                ,TRI."MtoDivReaREXTribCumpRentasISFUT_C14"    = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_16",0)) ELSE 0  END  
                ,TRI."MtoDivReaREXRentExIGCAfecIAart11_C15"   = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_17",0)) END
                ,TRI."MtoDivReaREXRentExIGCIA_C16"            = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_18",0)) END
                ,TRI."MtoDivReaREXNoConstRenta_C17"           = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_19",0)) END 
                ,TRI."CIGCDesde01012017RentAfecNoRestSDD_C18" = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_20",0)) END
                ,TRI."CIGCDesde01012017RentAfecNoRestCDD_C19" = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_21",0)) END
                ,TRI."CIGCDesde01012017RentAfecNoRestSDD_C20" = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_22",0)) END
                ,TRI."CIGCDesde01012017RentAfecNoRestCDD_C21" = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_23",0)) END
                ,TRI."CIGCDesde01012017RentAfecRestSDD_C22"   = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_24",0)) END
                ,TRI."CIGCDesde01012017RentAfecRestCDD_C23"   = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_25",0)) END
                ,TRI."CIGCDesde01012017RentExenRestSDD_C24"   = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_26",0)) END
                ,TRI."CIGCDesde01012017RentExenRestCDD_C25"   = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_27",0)) END
                ,TRI."CIGCDesde01012017CredIPE_C26"           = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_28",0)) END 
                ,TRI."CIGCHasta31122016RentAfecSDD_C27"       = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_29",0)) END
                ,TRI."CIGCHasta31122016RentAfecCDD_C28"       = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_30",0)) END
                ,TRI."CIGCHasta31122016RentExenSDD_C29"       = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_31",0)) END
                ,TRI."CIGCHasta31122016RentExenCDD_C30"       = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_32",0)) END
                ,TRI."CIGCHasta31122016CredIPE_C31"           = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_33",0)) END
                ,TRI."CIGCImptoTasaAdicional_C32"             = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_34",0)) END
                ,TRI."TasaEfectCredFUT"                       = (COALESCE(CAL."FACTOR_35",0))
                ,TRI."TasaEfectCredFUNT"                      = (COALESCE(CAL."FACTOR_36",0))
                ,TRI."DevCapArt17N7LIR_C33"                   = CASE WHEN TRI."AcogidoISFUT" = 'S' AND CAL."AcogidoISFUT" = 'S' THEN 0 ELSE ((TRI."Monto" * ROUND((COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)), 3)) * COALESCE(CAL."FACTOR_37",0)) END
                ,TRI."OrigenCalificacion"                          = CAL.IDORIGEN
                --,TRI."UsrUltMod"                          = ''
                --,TRI."FecUltMod"                          = CURRENT_UTCTIMESTAMP
--SELECT SUBSTRING(COALESCE(TRI."FecMovto",'1900-01-01'),0,7),IPCACTR."Valor",TRI.*
        FROM "BSCL.Normativa.BD::TRI.DatosDJ1949" AS TRI
        INNER JOIN "BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"=TRI."IdDocumento")
        LEFT JOIN "BSCL.Normativa.BD::TRI.TBSICAVC" AS CAL
                ON (CAL."EJERCICIO" = DOC."Version"
                AND CAL."MERCADO" = 'AC'
                AND TRIM(CAL."NEMO") = TRIM(TRI."Nemo")
                AND CAL."FEC_PAGO" = TRI."FecMovto"
                AND CAL."SEC_EVE" = TRI."SecEvento"
                AND CAL.IDORIGEN = 'BCS')
        LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR  ON (SUBSTRING(ADD_MONTHS (TO_DATE (COALESCE(TRI."FecMovto",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")  
		AND IPCINVR."IdParametroFinanciero" = 'IPC'
 		LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR  ON (SUBSTRING(COALESCE(TRI."FecMovto",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")  		
		AND IPCACTR."IdParametroFinanciero" = 'IPC'						
		WHERE TRI."SecEvento" > 0
        AND	DOC."Version" = CAST(:p_Version AS NVARCHAR(4));   