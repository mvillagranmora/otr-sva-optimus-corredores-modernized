SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.ConsultaCarteraPropiaValorizada";
GO
CREATE PROCEDURE
"BSCL.Mesa.CrystalReports.Servicios::MRF.ConsultaCarteraPropiaValorizada" ( 
     IN p_IdInforme							NVARCHAR(60) 	
    ,IN p_IdEmpresa          	          	BIGINT   
    ,IN p_IdCliente                         BIGINT   
    ,IN p_IdSocioNegocio                    BIGINT
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN
		DECLARE v_count         INT;

                v_Datos = SELECT
                    CASE 
                    	WHEN CARV."IdClasificacionTipoInstrumento" = 'IF' THEN
                    		CARV."ValorRescate"
                    	ELSE 
                    		CARV."Cantidad"
                    END AS "Cantidad" ,
                    MON."CantidadDecimales",
                    CARV."CodigoIFRS" ,
                    CARV."Diferenciador" ,
                    CARV."Duration" ,
                    CARV."EsFungible" ,
                    TO_VARCHAR(CARV."FechaValorizacionActual", 'DD-MM-YYYY') AS "FechaHoy",
                    TO_VARCHAR(CARV."FechaOperacion" , 'DD-MM-YYYY') AS "FechaOperacion",
                    DAYS_BETWEEN (TO_DATE(CARV."FechaValorizacionActual", 'YYYY-MM-DD'),TO_DATE (CARV."FechaVencimientoInstrumento", 'YYYY-MM-DD')) as "FechaPlazoEfectiva",
                    TO_VARCHAR(CARV."FechaValorizacionProximo" , 'DD-MM-YYYY') AS "FechaProximo" ,
                    TO_VARCHAR(CARV."FechaVencimientoInstrumento" , 'DD-MM-YYYY') AS "FechaVencimientoInstrumento" ,
                    TO_VARCHAR(CARV."FechaLiquidacion" , 'DD-MM-YYYY') AS "FechaLiquidacion" ,
                    CARV."FolioCartera" ,
                    CARV."IdCartera" ,
                    CARV."Emisor" AS "IdEmisor",
                    CARV."IdMonedaInstrumento" ,
                    "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(CARV."IdClasificacionTipoInstrumento",CARV."IdTipoInstrumento",CARV."Nemotecnico",CARV."Emisor") AS "IdTipoEmisor" ,
                    CARV."IdTipoReajuste" ,
                    (COALESCE(CARV."MontoProximo", 0) - COALESCE(CARV."MontoActual", 0)) - COALESCE(CARV."MontoReajuste", 0)  AS "Interes",
                    CARV."MontoCompra" AS "MontoCompra",
                    CARV."TasaCompra" ,
                    CARV."MontoActual" AS "MontoValorActual",
                    CARV."MontoProximo" AS "MontoValorProximo",
                    CARV."Nemotecnico" ,
                    DAYS_BETWEEN(TO_DATE(CARV."FechaValorizacionProximo", 'YYYY-MM-DD'),TO_DATE (CARV."FechaVencimientoInstrumento", 'YYYY-MM-DD')) AS "PlazoEnDiasOperacion",
                    CARV."PrecioActual" ,
                    MON."PrecioDecimales" ,
                    CARV."PrecioProximo" ,
                    CARV."MontoReajuste" AS "Reajuste" ,
                    CARV."TasaActual" ,
                    MON."TasaDecimales" ,
                    CARV."TasaProximo" AS "TasaProxima",
                    CARV."prmTipoValorizacion" AS "TipoValorizacion",
                    CARV."IdClasificacionTipoInstrumento"
                FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CARV
                LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."FolioCartera" = CARV."FolioCartera" AND CAR."IdEstado" = 'VIG'
                LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" GAR ON CARV."FolioCartera" = GAR."FolioGarantia" AND GAR."IdEstado" = 'VIG'
                LEFT JOIN 
                    "BSCL.Plataforma.BD::PLA.MonedaFormato" MON ON MON."IdUsoMonedaFormato"  = 'GEN' 
                                                                AND MON."IdMoneda" = CARV."IdTipoReajuste"
                                                                AND MON."IdTipoOperacion" = ''
                                                                AND MON."IdTipoInstrumento" = ''
                WHERE 
                    "IdInforme" = :p_IdInforme
                ORDER BY CARV."FolioCartera";
    
        SELECT COUNT(*) INTO v_count FROM :v_Datos;

        IF (v_count > 0) THEN 
            SELECT * FROM :v_Datos;
        ELSE
        SELECT
            0  as "IdCartera"                                  
            , 0  as "FolioCartera"		
            ,''  as "Nemotecnico"	
            ,''  as "CodigoIFRS"				                 
            ,''  as "IdEmisor"					                 
            ,''  as "EsFungible"                                 
            ,'01-01-0001'  as "FechaVencimientoInstrumento"      
            ,'01-01-0001'  as "FechaLiquidacion"         
            ,0  as "FechaPlazoEfectiva" 		                 
            ,0  as "PlazoEnDiasOperacion"                        
            ,'01-01-0001'  as "FechaOperacion"                   
            ,0  as "Duration"	 				                 	
            ,0  as "Cantidad"			                         
            ,0  as "MontoCompra"		  		                 
            ,0  as "TasaCompra"		  		                 
            ,''  as "IdMonedaInstrumento" 		                 
            ,0  as "Interes"  	     		                     
            ,0  as "Reajuste"           		                 
            ,0  as "PrecioActual"				                 
            ,0  as "TasaActual"		                             
            ,0  as "MontoValorActual"   		                 
            ,0  as "PrecioProximo"	  		  	                 		
            ,0  as "TasaProxima"		  		                 
            ,0  as "MontoValorProximo"  		                 
            ,''  as "IdEstado"			  		                 
            ,''  as "Diferenciador"	  			                 
            ,''  as "TipoValorizacion"   		                 
            ,''  as "IdTipoEmisor"
            ,''  AS "IdTipoReajuste"
            ,0   AS "CantidadDecimales"
            ,0  AS "PrecioDecimales"
            ,0  AS "TasaDecimales"
            ,'' AS "FechaHoy"
            ,'' AS "FechaProximo"
            ,'' AS "IdClasificacionTipoInstrumento"
            FROM DUMMY;
    
        END IF;

	END;
END;