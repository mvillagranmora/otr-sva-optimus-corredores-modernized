SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.ConsultarFactorPorPlazo";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.ConsultarFactorPorPlazo" 
	(IN p_IdEmpresa          		BIGINT,
 	 IN p_IdMoneda          		NVARCHAR(3),
 	 IN p_Plazo						BIGINT,
-- Parametros de Salida
	OUT p_out_Factor 				DECIMAL(26,8)
	)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL"
	 
AS
 BEGIN	
	 SELECT Canasta."Factor" INTO p_out_Factor
		FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" AS Canasta
		INNER JOIN 
			(
			Select 
			    CCM."IdCanasta",
				MIN(CFC."Hasta") as "MIN",
				MAX(CFC_MAYOR."Hasta") as "MAX"
			 from "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanastaMoneda" CCM
			 	LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" CFC
			 		ON 	CFC."IdEmpresa" = CCM."IdEmpresa" 
			 		AND CFC."IdCanasta" = CCM."IdCanasta" 
			 		AND (CFC."Hasta" >= :p_Plazo)
			 	LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" CFC_MAYOR
			 	 	ON 	CFC_MAYOR."IdEmpresa" = CCM."IdEmpresa" 
			 		AND CFC_MAYOR."IdCanasta" = CCM."IdCanasta" 
			WHERE CCM."IdEmpresa"	= :p_IdEmpresa 
				AND CCM."IdMoneda" 	= :p_IdMoneda
			GROUP BY CCM."IdCanasta"
			) AS FactorCorrespondiente
			ON COALESCE(FactorCorrespondiente."MIN",FactorCorrespondiente."MAX") = Canasta."Hasta"
			AND Canasta."IdEmpresa"	= :p_IdEmpresa  
			AND Canasta."IdCanasta"	= FactorCorrespondiente."IdCanasta";

 END;