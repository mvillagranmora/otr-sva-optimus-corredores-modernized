INSERT  INTO "BSCL.Mesa.BD::MES.Operacion" ( 
        "IdOperacion"
        , "FechaOperacion"  --"FechaOperacion"
        , "FolioOperacion"  --"FolioOperacion"
        , "IdTipoProducto"  --"IdTipoProducto"
        , "IdTipoOperacion" --"IdTipoOperacion"
        , "IdOperacionRelacionada"
        -- Campos Forward----------------------------
        , "IdInstrumento"           
        , "IdMonedaPrincipal"   
        , "IdMonedaSecundaria"  
        , "IdModalidadLiquidacion"
        , "IdReferenciaMercado"  
        , "IdPlaza"         
        ----------------------------------------------
        , "Intermesa"
        , "IdMesaOrigen"
        , "IdMesaDestino"
        , "IdCliente"           --"IdCliente"
        , "IdPortafolio"        --"IdPortafolio"
        , "IdClienteCorredor"   --"IdClienteCorredor"
        , "IdPortafolioCorredor"--"IdPortafolioCorredor"
        -- Campo Forward------------------------------
        , "MontoOperacion"
        ----------------------------------------------
        , "MontoOperacionInicial"   --"MontoOperacionInicial"
        , "MontoOperacionFinal" --"MontoOperacionFinal"
        , "IdTipoReajuste"      --"IdTipoReajuste"
        , "IdMoneda"
        , "Tasa"                    --"Tasa"
        --Campos Conversion---------------------------
        , "MontoOperacionMoneda"        
        , "MontoOperacionInicialMoneda"
        , "MontoOperacionFinalMoneda"   
        , "IdMonedaLiquidacion"     

        --Campos Forward------------------------------
        , "TCCierre"                
        , "ParidadCierre"           
        , "TCCosto"             
        , "PuntosTasasForward"  
        , "PuntosForward"           
        , "TCForward"               
        , "TasasMonedaPrincipal"    
        , "TasasMonedaSecundaria"   
        , "MontoDiferido"           
        , "MontoMargen"                     
        , "IdFixing"                
        , "FechaFixing"         
        , "HoraFixing"          
        , "IdSpotAsociado"      
        , "IdClasificacionCartera"
        -----------------------------------------------
        , "AfectaCustodia"
        , "PlazoEnDias"         --"PlazoEnDias"
        , "FechaVencimiento"        --"FechaVencimiento"
        , "FechaLiquidacion"        --"FechaLiquidacion"
        , "IdCondicionLiquidacion"  --"IdCondicionLiquidacion"
        , "FechaCondicionLiquidacion"   --"FechaCondicionLiquidacion"
        , "IdAgente"                --"IdAgente"
        , "TasaAgente"
        , "IdEstado"
        , "FechaCreacion"
        , "IdUsuarioCreacion"       --"IdUsuarioCreacion"
        , "IdTipoCanalIngreso"  
        , "Glosa"
        -----------------------------------------------
        , "IdEmpresa"                   
        , "IdUsuarioEdicion"
        , "FechaEdicion"
        , "IdEmisor"
        , "TipoDocumento"
        , "IdContacto"
        ,"TasaAnualizada"
        ,"CostoFondo"
        ,"UtilidadPerdida"
        ,"UtilidadFinal"
        ,"PagaConCtaInv"
        ,"ValorizacionManual"
        ,"IdMonedaCompensacion"
        ,"IdEmpleadoDigitador"
)
VALUES  (:IdOperacion
        , :FechaOperacion  --"FechaOperacion"
        , :FolioOperacion  --"FolioOperacion"
        , :IdTipoProducto  --"IdTipoProducto"
        , :IdTipoOperacion --"IdTipoOperacion"
        , :IdOperacionRelacionada                  
        -- Campos Forward----------------------------
        , :IdInstrumento           
        , :IdMonedaPrincipal   
        , :IdMonedaSecundaria  
        , :IdModalidadLiquidacion
        , :IdReferenciaMercado  
        , :IdPlaza
        ----------------------------------------------
        , :Intermesa           --"Intermesa"
        , :IdMesaOrigen            --"IdMesaOrigen"
        , :IdMesaDestino           --"IdMesaDestino"
        , :IdCliente           --"IdCliente"
        , :IdPortafolio        --"IdPortafolio"
        , :IdClienteCorredor   --"IdClienteCorredor"
        , :IdPortafolioCorredor--"IdPortafolioCorredor"
        -- Campo Forward------------------------------
        , :MontoOperacion
        ----------------------------------------------
        , :MontoOperacionInicial   --"MontoOperacionInicial"
        , :MontoOperacionFinal --"MontoOperacionFinal"
        , :IdTipoReajuste      --"IdTipoReajuste"
        , :IdMoneda
        , :Tasa                    --"Tasa"
        --Campos Conversion---------------------------
        , :MontoOperacionMoneda        
        , :MontoOperacionInicialMoneda
        , :MontoOperacionFinalMoneda
        , :IdMonedaLiquidacion     
        --Campos Forward------------------------------
        , :TCCierre                
        , :ParidadCierre           
        , :TCCosto             
        , :PuntosTasasForward  
        , :PuntosForward           
        , :TCForward               
        , :TasasMonedaPrincipal    
        , :TasasMonedaSecundaria   
        , :MontoDiferido           
        , :MontoMargen                     
        , :IdFixing                
        , :FechaFixing         
        , :HoraFixing          
        , :IdSpotAsociado      
        , :IdClasificacionCartera
        -----------------------------------------------
        , :AfectaCustodia
        , :PlazoEnDias         --"PlazoEnDias"
        , :FechaVencimiento        --"FechaVencimiento"
        , :FechaLiquidacion        --"FechaLiquidacion"
        , :IdCondicionLiquidacion  --"IdCondicionLiquidacion"
        , :FechaCondicionLiquidacion   --"FechaCondicionLiquidacion"
        , :IdAgente                --"IdAgente"
        , :TasaAgente
        , :IdEstado
        , :FechaCreacion
        , :IdUsuarioCreacion       --"IdUsuarioCreacion"
        , :IdTipoCanalIngreso  
        , :Glosa
        -----------------------------------------------
        , :IdEmpresa       
        , :IdUsuarioCreacion
        , :FechaCreacion
        , :IdEmisor
        , :TipoDocumento
        , :IdContacto
        ,:TasaAnualizada
	,:CostoFondo
	,:UtilidadPerdida
	,:UtilidadFinal
	,COALESCE(:PagaConCtaInv,'N')
	,:ValorizacionManual
	,COALESCE(:IdMonedaCompensacion,'')
	,:IdEmpleadoDigitador
        );
        
        