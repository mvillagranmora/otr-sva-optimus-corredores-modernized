SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ObtenerInformacionFinanciera";

CREATE PROCEDURE
-- 
--  Proposito: Recupera la información Financiera
--  Autor: <author> - BCS
--  Fecha Creacion: 17/08/2016
--  Fecha Modificacion: 17/08/2016
"BSCL.Normativa.BD.Servicios::CAR.ObtenerInformacionFinanciera" (
	 IN p_IdModulo					NVARCHAR(40) , -- Obligatorio
	 IN p_IdUsuario					BIGINT       , -- Obligatorio
	 IN p_IdEmpresa					BIGINT       , -- Obligatorio
	 IN p_Fecha						DATE
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
--READS SQL DATA 
AS
BEGIN 
	DECLARE v_IdMonedaSistema NVARCHAR(3);
	CREATE LOCAL TEMPORARY TABLE #cartola_demo_data (
	            --Informacion Basica Indicadores
	            "ValorUF" 					DECIMAL(28,6),
	            "ValorUSD" 					DECIMAL(28,6),
	            "ValorEUR" 					DECIMAL(28,6)
	);


	SELECT "Valor" INTO v_IdMonedaSistema
	  FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
	 WHERE "CodigoParametro" = 'PLA.MonedaSistema'
	   AND "IdEmpresa" = :p_IdEmpresa;


	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
	SELECT 0,1,:p_Fecha,'UF',:v_IdMonedaSistema,0,1,'CARTOLA' FROM DUMMY UNION ALL
	SELECT 0,2,:p_Fecha,'USD',:v_IdMonedaSistema,0,1,'CARTOLA' FROM DUMMY UNION ALL
	SELECT 0,3,:p_Fecha,'EUR',:v_IdMonedaSistema,0,1,'CARTOLA' FROM DUMMY;

	CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => 0
	);
	
	
	insert into #cartola_demo_data
	SELECT
		COALESCE(MAX("ValorUF"),0) AS "ValorUF",
		COALESCE(MAX("ValorUSD"),0) AS "ValorUSD",
		COALESCE(MAX("ValorEUR"),0) AS "ValorEUR"
	FROM
	(
		SELECT 	CASE WHEN "IdMonedaOrigen"='UF' THEN "MontoDestino" ELSE null END AS "ValorUF",
				CASE WHEN "IdMonedaOrigen"='USD' THEN "MontoDestino" ELSE null END AS "ValorUSD",
				CASE WHEN "IdMonedaOrigen"='EUR' THEN "MontoDestino" ELSE null END AS "ValorEUR"
		  FROM (SELECT "IdMonedaOrigen", "MontoDestino" FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida")
	);
	

	select * from 	#cartola_demo_data;
	drop table #cartola_demo_data;
END;