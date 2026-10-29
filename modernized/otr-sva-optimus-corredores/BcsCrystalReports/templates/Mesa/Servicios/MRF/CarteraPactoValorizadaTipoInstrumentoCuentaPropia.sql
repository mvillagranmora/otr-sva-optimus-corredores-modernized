SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPactoValorizadaTipoInstrumentoCuentaPropia";

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPactoValorizadaTipoInstrumentoCuentaPropia" ( 
    --VARIABLES OBLIGATORIAS
	IN p_IdInforme							NVARCHAR(100)
	,IN p_IdEmpresa          	          	BIGINT
    
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_count 			INT;
	DECLARE v_FechaValorizacion	NVARCHAR(10);
	DECLARE v_IdCliente     	INT;
	
	BEGIN
	v_FechaValorizacion = SUBSTRING(:p_IdInforme, LENGTH(:p_IdInforme) - 28,10);
	
	v_Datos = 	SELECT 
					CARV."IdTipoInstrumento",
					CARV."FechaVencimientoOperacion",
					CASE WHEN CARV."Diferenciador" = 'PD' THEN CARV."Diferenciador"
						 ELSE 'PC'
					END AS "Diferenciador",
					CARV."MontoProximo" AS "MontoActual",
					'CLP' AS "IdMonedaInstrumento"
				FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" AS CARV
				WHERE "IdInforme" = :p_IdInforme;

	SELECT COUNT(*) INTO v_count FROM :v_Datos;
	
	IF (v_count != 0) THEN 
		v_Salida =SELECT 
			 	 T."IdTipoInstrumento"
			 	,T."Nombre" AS "NombreTipoInstrumento"
			 	,'CLP' AS "IdMonedaInstrumento"
			 	
			 	--Cartera Libre
				,SUM(CASE WHEN DAYS_BETWEEN (TO_DATE (:v_FechaValorizacion, 'YYYY-MM-DD'),TO_DATE (C."FechaVencimientoOperacion", 'YYYY-MM-DD')) >7 AND C."Diferenciador"='PD' 
					 THEN COALESCE(C."MontoActual",0) ELSE 0 END) AS "MontoValorActualMayorCD"
				,SUM(CASE WHEN DAYS_BETWEEN (TO_DATE (:v_FechaValorizacion, 'YYYY-MM-DD'),TO_DATE (C."FechaVencimientoOperacion", 'YYYY-MM-DD')) <=7  AND C."Diferenciador"='PD'
					 THEN COALESCE(C."MontoActual",0) ELSE 0 END) AS "MontoValorActualMenorCD"
				--Cartera Pacteada
				,SUM(CASE WHEN DAYS_BETWEEN (TO_DATE (:v_FechaValorizacion, 'YYYY-MM-DD'),TO_DATE (C."FechaVencimientoOperacion", 'YYYY-MM-DD')) >7 AND C."Diferenciador"='PC'
					 THEN COALESCE(C."MontoActual",0) ELSE 0 END) AS "MontoValorActualMayorCP"
				,SUM(CASE WHEN DAYS_BETWEEN (TO_DATE (:v_FechaValorizacion, 'YYYY-MM-DD'),TO_DATE (C."FechaVencimientoOperacion", 'YYYY-MM-DD')) <=7 AND C."Diferenciador"='PC'
					 THEN COALESCE(C."MontoActual",0) ELSE 0 END) AS "MontoValorActualMenorCP"
				
			 FROM "BSCL.Plataforma.BD::INS.TipoInstrumento" AS T 
			 LEFT JOIN  :v_Datos AS C ON (C."IdTipoInstrumento"=T."IdTipoInstrumento")
			 WHERE T."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
			 GROUP BY  T."IdTipoInstrumento",T."Nombre",C."IdMonedaInstrumento";
	ELSE
	 
		v_Salida =SELECT 
			 	 T."IdTipoInstrumento"
			 	,T."Nombre" AS "NombreTipoInstrumento"
			 	,'' AS "IdMonedaInstrumento"
			 	--Cartera Libre
				,0 AS  "MontoValorActualMayorCD"
				,0 AS "MontoValorActualMenorCD"
				--Cartera Pacteada
				,0 AS "MontoValorActualMayorCP"
				,0 AS "MontoValorActualMenorCP"
				
			 FROM "BSCL.Plataforma.BD::INS.TipoInstrumento" AS T 
			 WHERE T."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
			 GROUP BY  T."IdTipoInstrumento",T."Nombre"; 
	END IF;
	
	
	SELECT * FROM :v_Salida;
		
	END;
END;	