SET SCHEMA "BSCL";
DROP FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ValorDatoEventoValido";

CREATE FUNCTION 
-- 
--  Proposito: retronar si el valor de un dato es valido
--  Autor: Rabreu - Pronet IT
--  Fecha Creacion: 27-10-2016
--  
"BSCL.Contabilidad.BD.Utilitarios::ITZ.ValorDatoEventoValido" (

		 p_IdTipoDatoEvento	NVARCHAR(30)
		,p_ValorVarChar		NVARCHAR(60)
		,p_ValorDecimal		DECIMAL (18,4)
		,p_Tipo				NVARCHAR(1)
) 
RETURNS p_out_estado NVARCHAR(1) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
AS

BEGIN
	DECLARE v_CantidadDeDatosValidos	BIGINT;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_estado := 'N';
	END;
	
	v_CantidadDeDatosValidos:= 0;

	--+ Valida si existe una lista para validar
	SELECT 
		COUNT(1)
	INTO
		v_CantidadDeDatosValidos	
	FROM 
		"BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" AS CTB_itzEventoTipoDatoValor
	WHERE 
		"IdTipoDatoEvento" = :p_IdTipoDatoEvento
	AND ( 
			(:p_Tipo = 'V' AND "ValorVarchar" = :p_ValorVarChar) 
		OR  (:p_Tipo = 'D' AND "ValorDecimal" = :p_ValorDecimal) 
		)
	AND "Estado" = 'V';

	--+ Valida los datos solo si hay una lista que validar
	IF :v_CantidadDeDatosValidos > 0 THEN

		p_out_estado := 'N';
		
		--+ Busca si el dato es valido
		SELECT 
			CASE
				WHEN COUNT(1) > 0 THEN 'S'
				ELSE 'N'
			END
		INTO
			p_out_estado	
		FROM 
			"BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" AS CTB_itzEventoTipoDatoValor
		WHERE 
			"IdTipoDatoEvento" = :p_IdTipoDatoEvento
		AND ( 
				(:p_Tipo = 'V' AND "ValorVarchar" = :p_ValorVarChar) 
			OR  (:p_Tipo = 'D' AND "ValorDecimal" = :p_ValorDecimal) 
			)
		AND "Estado" = 'V';
	END IF;
END
