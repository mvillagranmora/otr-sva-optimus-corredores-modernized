SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraDePactosValorizada_CuentaPropia";
GO
CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraDePactosValorizada_CuentaPropia" ( 
    --VARIABLES OBLIGATORIAS
   	IN p_IdInforme              			NVARCHAR(60)
   	,IN p_IdEmpresa							BIGINT     
   	,IN p_IdCliente							BIGINT
    ,IN p_IdSocioNegocio        			BIGINT	
    
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
    DECLARE v_count           INT;
   	DECLARE v_FechaValorizacion    NVARCHAR(10);
    BEGIN
  
    v_FechaValorizacion = SUBSTRING(:p_IdInforme, LENGTH(:p_IdInforme) - 28,10);
	    
    v_CarteraValorizada =  SELECT 
                              CAR."IdCartera", 				
                              CAR."IdOperacion",         
                              DOCID."DocumentoIdentidad" AS "RutCliente",
                              CAR."FolioOperacion",         
                              CASE WHEN IFNULL(PER."Nombres",'') = '' 
                                THEN PER."RazonSocial" 
                              ELSE 
                                CONCAT(CONCAT(CONCAT(PER."Nombres",' '),CONCAT(PER."ApellidoPaterno",' ') )  ,PER."ApellidoMaterno") 
                              END AS "NombreCliente",					
                              PORT."CodigoPortafolio",
                              PORT."Proposito" AS "PropositoPortafolio",				
                              CAR."Diferenciador",                
                              TO_VARCHAR(CAR."FechaOperacion", 'DD-MM-YYYY') AS "FechaOperacion",                
                              TO_VARCHAR(CAR."FechaVencimientoOperacion", 'DD-MM-YYYY') AS "FechaVencimientoOperacion",     
                              DAYS_BETWEEN (TO_DATE(:v_FechaValorizacion, 'YYYY-MM-DD'), CAR."FechaVencimientoOperacion") as "Dia",  
                              CAR."prmTipoValorizacion" AS "TipoValorizacion",   
							                COALESCE(TITE."CodigoTipoEmisor",'-') AS "CodigoTipoEmisor",
                              CASE WHEN CAR."prmTipoInforme" = 'CARTERA.PACTO' AND CAR."prmTipoValorizacion" = 'PAC' THEN COALESCE(OPE."IdTipoReajuste", CAR."IdTipoReajuste") ELSE CAR."IdTipoReajuste" END AS "IdTipoReajuste",                
                              CAR."CodigoIFRS",     
                              CAR."Duration",             
                              CAR."EsFungible",                    
                              CASE
                              	WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN 
                              		CAR."ValorRescate"
                              	ELSE CAR."Cantidad"
                              END AS "Cantidad",    
							                CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END AS "Instrumento",
                              CAR."Emisor",                        
                              TO_VARCHAR(CAR."FechaVencimientoInstrumento", 'DD-MM-YYYY') AS "FechaVencimientoInstrumento",
                              CAR."MontoActual"  AS "MontoAnterior",            
                              CAR."MontoProximo" AS "MontoPresente",            
                              CAR."MontoCompra" AS "MontoOperacion",  
                              CAR."MontoReajuste" AS "Reajuste",                
                              CASE WHEN CAR."prmTipoInforme" = 'CARTERA.PACTO' AND CAR."prmTipoValorizacion" = 'PAC'
                              THEN (COALESCE(CAR."MontoProximo", 0) - COALESCE(CAR."MontoActual", 0))
                              ELSE (COALESCE(CAR."MontoProximo", 0) - COALESCE(CAR."MontoActual", 0)) - COALESCE(CAR."MontoReajuste", 0) END AS "InteresDiario",            
                              COALESCE(CAR."MontoProximo", 0) - COALESCE(CAR."MontoCompra", 0) AS "InteresAcumulado",     
                              CAR."MontoPactado",
                              CAR."MontoPactado" - CAR."MontoCompra" AS "InteresVencimiento",        
                              CAR."TasaActual", 
                              MF."CantidadDecimales"
                            FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" AS CAR
                            INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CAR."IdCliente" 
                            INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
                            INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCID ON DOCID."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
                            INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON PORT."IdPortafolio" = CAR."IdPortafolio" 
							LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" TITE ON TITE."IdTipoInstrumento" = CAR."IdTipoInstrumento"
								AND CAR."IdTipoInstrumento" = TITE."IdTipoInstrumento"
								AND ( 
									TITE."Nemotecnico" != '' AND (TRIM(TITE."Nemotecnico") = CAR."Nemotecnico")
															 AND ((TITE."IdEmisor" != '' AND TITE."IdEmisor" = CAR."Emisor") OR (TITE."IdEmisor" = ''))					
									OR
									TITE."Nemotecnico" = '' AND ((TITE."IdEmisor" = CAR."Emisor") OR (TITE."IdEmisor" = ''))
								)
                            LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF ON MF."IdTipoInstrumento" = CAR."IdTipoInstrumento"
                              AND MF."IdMoneda" = CAR."IdMonedaInstrumento"
                              AND MF."IdUsoMonedaFormato" = 'GEN'
                              AND MF."IdTipoOperacion" = ''
                            LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = CAR."IdOperacion"
                            WHERE "IdInforme" = :p_IdInforme;
    
    SELECT COUNT(*) INTO v_count FROM :v_CarteraValorizada;

    IF (v_count > 0) THEN 
      SELECT
        "IdCartera", 				
        "IdOperacion",         
        "RutCliente",
        "CodigoPortafolio",
        "PropositoPortafolio",
        "FolioOperacion",
        "NombreCliente",					
        "Diferenciador",                
        "FechaOperacion",                
        "FechaVencimientoOperacion",     
        "Dia",  
        "TipoValorizacion",
        "CodigoTipoEmisor",                  
        "IdTipoReajuste",                
        "CodigoIFRS", 
        "Duration",                   
        "EsFungible",                    
        "Cantidad",                     
        "Instrumento",
        "Emisor",                        
        "FechaVencimientoInstrumento",
        "MontoAnterior",             
        "MontoPresente",            
        "MontoOperacion",              
        "Reajuste",                
        "InteresDiario",            
        "InteresAcumulado",     
        "InteresVencimiento",        
        "TasaActual", 
        COALESCE("CantidadDecimales", 0) AS "CantidadDecimales",
        "MontoPactado"
      FROM :v_CarteraValorizada;
    ELSE
      SELECT 
        0 AS "IdCartera"     				
        ,0   AS "IdOperacion"         
        ,''  AS "RutCliente"
        ,''  AS "CodigoPortafolio"
        ,''  AS "PropositoPortafolio"	  
        ,0   AS "FolioOperacion"
        ,''  AS "NombreCliente"					
        ,''  AS "Diferenciador"                
        ,'0001-01-01'  AS "FechaOperacion"                
        ,'0001-01-01'  AS "FechaVencimientoOperacion"     
        ,0  AS "Dia"             				
        ,'' AS "TipoValorizacion"              
        ,'' AS "CodigoTipoEmisor"                  
        ,'' AS "IdTipoReajuste"                
        ,'' AS "CodigoIFRS"
        ,0  AS "Duration"
        ,'' AS "EsFungible"
        ,0  AS "Cantidad"                      
        ,'' AS "Instrumento"                   
        ,'' AS "Emisor"                        
        ,'0001-01-01'  AS "FechaVencimientoInstrumento"   
        ,0  AS "MontoAnterior"                 
        ,0  AS "MontoPresente"                 
        ,0  AS "MontoOperacion"                
        ,0  AS "Reajuste"                      
        ,0  AS "InteresDiario"                 
        ,0  AS "InteresAcumulado"              
        ,0  AS "InteresVencimiento"            
        ,0  AS "TasaActual" 
        ,0  AS "CantidadDecimales"
        ,0  AS "MontoPactado"           
	    FROM DUMMY;
    
    END IF;
	
	END;
END