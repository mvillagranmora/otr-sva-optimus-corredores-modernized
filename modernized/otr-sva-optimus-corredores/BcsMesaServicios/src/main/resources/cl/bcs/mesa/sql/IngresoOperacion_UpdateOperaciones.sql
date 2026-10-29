UPDATE  "BSCL.Mesa.BD::MES.Operacion" 
SET     "FechaOperacion" = :FechaOperacion
        ,"FolioOperacion" = :FolioOperacion
        ,"IdTipoProducto" = :IdTipoProducto
        ,"IdTipoOperacion" = :IdTipoOperacion
        ,"IdOperacionRelacionada" = :IdOperacionRelacionada                    
        -- Campos Forward----------------------------
        , "IdInstrumento"           = :IdInstrumento
        , "IdTipoCanalIngreso"      = :IdTipoCanalIngreso
        , "IdMonedaPrincipal"       = :IdMonedaPrincipal
        , "IdMonedaSecundaria"      = :IdMonedaSecundaria
        , "IdModalidadLiquidacion"  = :IdModalidadLiquidacion
        , "IdReferenciaMercado"     = :IdReferenciaMercado
        , "IdPlaza"                 = :IdPlaza
        ----------------------------------------------
        ,"Intermesa" = :Intermesa          --"Intermesa"
        ,"IdMesaOrigen" = :IdMesaOrigen            --"IdMesaOrigen"
        ,"IdMesaDestino" = :IdMesaDestino          --"IdMesaDestino"
        ,"IdCliente" = :IdCliente
        ,"IdPortafolio" = :IdPortafolio
        ,"IdClienteCorredor" = :IdClienteCorredor
        ,"IdPortafolioCorredor" = :IdPortafolioCorredor
        -- Campo Forward------------------------------
        , "MontoOperacion"          = :MontoOperacion
        ----------------------------------------------
        ,"MontoOperacionInicial" = :MontoOperacionInicial
        ,"MontoOperacionFinal" = :MontoOperacionFinal
        ,"IdTipoReajuste" = :IdTipoReajuste
        ,"IdMoneda" = :IdMoneda
        ,"Tasa" = :Tasa
        --Campos Conversion---------------------------
        ,"MontoOperacionMoneda"         = :MontoOperacionMoneda        
        ,"MontoOperacionInicialMoneda"  = :MontoOperacionInicialMoneda
        ,"MontoOperacionFinalMoneda"    = :MontoOperacionFinalMoneda   
        ,"IdMonedaLiquidacion"          = :IdMonedaLiquidacion
        --Campos Forward------------------------------
        , "TCCierre"                = :TCCierre
        , "ParidadCierre"           = :ParidadCierre
        , "TCCosto"                 = :TCCosto             
        , "PuntosTasasForward"      = :PuntosTasasForward  
        , "PuntosForward"           = :PuntosForward           
        , "TCForward"               = :TCForward               
        , "TasasMonedaPrincipal"    = :TasasMonedaPrincipal    
        , "TasasMonedaSecundaria"   = :TasasMonedaSecundaria   
        , "MontoDiferido"           = :MontoDiferido           
        , "MontoMargen"             = :MontoMargen                     
        , "IdFixing"                = :IdFixing                
        , "FechaFixing"             = :FechaFixing         
        , "HoraFixing"              = :HoraFixing          
        , "IdSpotAsociado"          = :IdSpotAsociado      
        , "IdClasificacionCartera"  = :IdClasificacionCartera
        -----------------------------------------------
        ,"AfectaCustodia" = :AfectaCustodia
        ,"PlazoEnDias" = :PlazoEnDias
        ,"FechaVencimiento" = :FechaVencimiento
        ,"FechaLiquidacion" = :FechaLiquidacion
        ,"IdCondicionLiquidacion" = :IdCondicionLiquidacion
        ,"FechaCondicionLiquidacion" = :FechaCondicionLiquidacion
        ,"IdAgente" = :IdAgente
        ,"TasaAgente" = :TasaAgente                    
        ,"IdEstado" = :IdEstado
        ,"FechaCreacion" = :FechaCreacion
        --,"IdUsuarioCreacion" = "IdUsuarioCreacion"
        ,"Glosa" = :Glosa
        ,"IdEmpresa" = :IdEmpresa
        ,"IdEmisor" = :IdEmisor
        ,"TipoDocumento" = :TipoDocumento
        ,"IdContacto"	= :IdContacto
        ,"ValorizacionManual" = :ValorizacionManual
WHERE "IdOperacion" IN (:IdOperacion);