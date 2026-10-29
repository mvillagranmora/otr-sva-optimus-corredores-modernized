		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.CrystalReports.Servicios::FMRepComprobanteExeSpot";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.CrystalReports.Servicios::FMRepComprobanteExeSpot" ( 
	--Entrada
	IN p_IdOperacion BIGINT
)
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN
	
	v_Limites = SELECT 
		 LIM."Nombre" AS "NombreLimite"
		,TL."Nombre" AS "TipoLimite"
		,CEL."MontoExcedido" AS "MontoExcedido"
	FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEvaluacionLimite" CEL
	INNER JOIN "BSCL.Plataforma.BD::LIM.Limite" LIM
	ON CEL."IdLimite" = LIM."IdLimite"
	INNER JOIN "BSCL.Plataforma.BD::LIM.TipoLimite" TL
	ON CEL."IdTipoLimite" = TL."IdTipoLimite"
	WHERE CEL."IdOperacion" = :p_IdOperacion;
		
	 
	BEGIN
			
		SELECT * FROM :v_Limites;
				
	END;	
					
END;
