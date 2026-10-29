 ---Validaciones de Valorizacion  
 UPDATE DJ
  SET
     DJ."ValIndicadorValorizado"			  	=	CASE WHEN MEN."Resultado" <> ''   	THEN 'NO' ELSE '' END
    ,DJ."ValResultado"			  				=	CASE WHEN MEN."Resultado" <> '' 	THEN 'FALTA :' ||  MEN."Resultado" ELSE '' END
FROM "BSCL.Normativa.BD::TRI.DatosDJ1874"   AS DJ
INNER JOIN 
(	
	SELECT RES."ID",RES."Resultado"
	FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1874.ValidaValorizar"  AS RES
    WHERE     RES."Version" = CAST(:p_Version AS NVARCHAR(4)) AND RES."IdEmpresa" = :p_IdEmpresa AND (RES."FuenteDatos"=:p_FuenteDatos OR :p_FuenteDatos ='AMBAS')   
	
) AS MEN ON (MEN."ID"=DJ."ID")	