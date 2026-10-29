SELECT
        ECC."IdEventoCapitalClienteFormaDePago"
        ,ECC."IdEventoCapitalCliente"
        ,EC."IdEventoCapital" 
        ,ECFP."IdMedioDePago"
        ,ECFP."IdModalidadPago"             
        ,ECC."IdMoneda"
        
        ,CASE :p_LocalizacionSistema
            WHEN 'PE' THEN
                CASE ECC."IdBoveda"
                    WHEN 'CAV' THEN
                        CAST(ECC."MontoBeneficioArchivo" - ECC."MontoRetencionArchivo" AS DECIMAL(26,8))
                    ELSE
                        CAST(ECC."Total" AS DECIMAL(26,8))
                END
            ELSE
                CAST(ECC."Total" AS DECIMAL(26,8))                  
        END AS "Monto"  
        
        ,ECFP."IdCuentaBancaria"
        ,ECFP."IdBanco"
        ,ECFP."NombreCuentaBancaria"
        ,ECFP."NumeroCuentaBancaria"
        ,ECFP."IdTipoCuentaBancaria"
        ,ECFP."NombreTipoCuentaBancaria"
        ,ECFP."IdTipoTarjetaBancaria"
        ,ECFP."NumeroTarjetaBancaria"
        ,ECFP."FechaVctoDocumento"
        
    FROM "BSCL.Custodia.BD::CUS.EventoCapital"                                  AS EC
    LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"                      AS ECC  ON (ECC."IdEventoCapital" = EC."IdEventoCapital" AND ECC."IdTipoEventoCapital" = EC."IdTipoEventoCapital" ) 
    LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"           AS ECFP ON (ECFP."IdEventoCapitalCliente" = ECC."IdEventoCapitalCliente")
    
    
    WHERE EC."IdEventoCapital"      IN (:p_IdEventoCapital)
    AND   EC."IdTipoEventoCapital"  IN (:p_IdTipoEventoCapital)              
    --AND   ECC."TipoSocioNegocio"    = 'CLI'   -- Cliente
    AND   ECC."Total" > 0;
    