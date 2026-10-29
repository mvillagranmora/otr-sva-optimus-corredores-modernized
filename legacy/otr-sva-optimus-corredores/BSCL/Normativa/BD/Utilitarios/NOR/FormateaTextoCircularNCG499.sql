set schema "BSCL";

DROP function "BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499";

CREATE function "BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499" (p_cadena NVARCHAR(1000))
RETURNS p_out_cadena NVARCHAR(1000)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
AS
BEGIN
    -- Expresión regular para buscar y eliminar caracteres especiales
    DECLARE lv_expresion NVARCHAR(100) = '[^a-zA-Z0-9#]';
    
   p_cadena =
            REPLACE(
                REPLACE ( 
                    REPLACE( 
                        REPLACE( 
                            REPLACE( 
                                REPLACE ( 
                                    REPLACE ( 
                                        REPLACE( 
                                            REPLACE( 
                                                REPLACE( 
                                                    REPLACE(
                                                        REPLACE(
                                                            p_cadena
                                                        ,'Á','A')
                                                    ,'É','E') 
                                                ,'Í','I') 
                                            ,'Ó','O') 
                                        ,'Ú','U') 
                                    ,'Ñ','#')   
                                ,'á','a') 
                            ,'é','e')
                        ,'í','i') 
                    ,'ó','o') 
                ,'ú','u')  
            ,'ñ','#'); 
           
            
     p_cadena = REPLACE ( 
                REPLACE( 
                    REPLACE( 
                        REPLACE( 
                            REPLACE ( 
                                REPLACE ( 
                                    REPLACE( 
                                        REPLACE( 
                                            REPLACE( 
                                                REPLACE(
                                                    p_cadena
                                                ,'Ä','A')
                                            ,'Ë','E') 
                                        ,'Ï','I') 
                                    ,'Ö','O') 
                                ,'Ü','U') 
                            ,'ä','a') 
                        ,'ë','e')
                    ,'ï','i') 
                ,'ö','o') 
            ,'ü','u'); 
   
    -- Aplicar la expresión regular a la cadena
    p_out_cadena = REPLACE_REGEXPR(lv_expresion IN  p_cadena WITH ' ');
END;