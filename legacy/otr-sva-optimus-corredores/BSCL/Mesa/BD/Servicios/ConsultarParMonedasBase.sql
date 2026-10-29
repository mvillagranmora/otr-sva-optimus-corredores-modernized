SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarParMonedasBase";

-- 
--  Proposito : Consulta todas las posibles combinaciones de monedas por producto 
--  Autor ....: MSANDOVAL - BCS
--  Fecha ....: 25-02-2017
-- 
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarParMonedasBase" (
    --* Salidas del SP
    OUT p_out_Monedas TABLE (	
			"IdMoneda" NVARCHAR(3)
			,"Nombre" NVARCHAR(100) 
			,"NombreLargo" NVARCHAR(150) 
			,"IdTipoInstrumento" NVARCHAR(5)
			,"Nemotecnico" NVARCHAR(40)
			,"OrdenEnPar" BIGINT
			)

	) 
	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA WITH RESULT VIEW "BSCL.Mesa.BD.Vistas::ConsultarParMonedasBase"  

	
AS
BEGIN 
	DECLARE	v_NemoLocal NVARCHAR(5);

	--* Todas las monedas activas
	--* -----------------------------
	t_Monedas = 
		SELECT	MON."IdMoneda", MON."Nombre", MON."NombreLargo",
				INS."IdTipoInstrumento", INS."Nemotecnico" 
		FROM	"BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" MON,
				"BSCL.Plataforma.BD::INS.Instrumento" INS
		WHERE	MON."IdEstado" = 'ACT'
				AND INS."IdTipoInstrumento" IN ( 'SPT', 'FWD' );	


	--* Nemo de la moneda Local
	--* -----------------------------
	SELECT 	TOP 1 "Valor" 
	INTO	v_NemoLocal
	FROM 	"BSCL.Plataforma.BD::PLA.Configuracion" 
	WHERE 	"CodigoParametro" = 'PLA.MonedaSistema'; --AND "IdEmpresa" = p_IdEmpresa;

	--* Configuración x Producto
	--* -----------------------------
	t_Config = 
		SELECT	"IdTipoProducto", "IdTipoInstrumento", "Nemotecnico",
				"IdTipoMoneda", 
				CASE WHEN "IdNemoMoneda" = '_LOCAL' THEN v_NemoLocal ELSE "IdNemoMoneda" END AS "IdNemoMoneda", 
				"IdAccion"
		FROM	"BSCL.Mesa.BD::MES.PlantillaMonedasxProducto" CFG;


	--* ----------------------------------------------------------------------------------------------
	--* PRIMERA MONEDA
	--* ----------------------------------------------------------------------------------------------
	p_out_Monedas = 
		SELECT	MON."IdMoneda", MON."Nombre", MON."NombreLargo", 
				MON."IdTipoInstrumento", MON."Nemotecnico", 1 AS "OrdenEnPar"
		FROM	:t_Monedas MON
				INNER JOIN :t_Config CFG 
				ON	MON."IdMoneda" = CFG."IdNemoMoneda" 
					AND CFG."IdTipoInstrumento" = MON."IdTipoInstrumento"
					AND CFG."Nemotecnico" = MON."Nemotecnico"
					AND CFG."IdAccion" = 'SOLO'
					AND CFG."IdTipoMoneda" = 1;
							
	p_out_Monedas = 
		SELECT	*
		FROM	:p_out_Monedas
		---------
		UNION
		---------	
		SELECT	MON."IdMoneda", MON."Nombre", MON."NombreLargo", 
				MON."IdTipoInstrumento", MON."Nemotecnico", 1 AS "OrdenEnPar" 
		FROM	:t_Monedas MON
				LEFT JOIN :t_Config CFG 
				ON	MON."IdMoneda" = CFG."IdNemoMoneda" 
					AND CFG."IdTipoInstrumento" = MON."IdTipoInstrumento"
					AND CFG."Nemotecnico" = MON."Nemotecnico"
					AND CFG."IdAccion" = 'EXCL'
					AND CFG."IdTipoMoneda" = 1	
		WHERE	COALESCE( CFG."IdNemoMoneda", '_NO_' ) = '_NO_' 
				AND CONCAT(MON."IdTipoInstrumento",MON."Nemotecnico") NOT IN 
				( SELECT DISTINCT CONCAT("IdTipoInstrumento","Nemotecnico") FROM :p_out_Monedas );


	--* ----------------------------------------------------------------------------------------------		
	--* SEGUNDA MONEDA...
	--* ----------------------------------------------------------------------------------------------
	p_out_Monedas = 
		SELECT	*
		FROM	:p_out_Monedas
		---------
		UNION
		---------	
		SELECT	MON."IdMoneda", MON."Nombre", MON."NombreLargo", 
				MON."IdTipoInstrumento", MON."Nemotecnico", 2 AS "OrdenEnPar"
		FROM	:t_Monedas MON
				INNER JOIN :t_Config CFG 
				ON	MON."IdMoneda" = CFG."IdNemoMoneda" 
					AND CFG."IdTipoInstrumento" = MON."IdTipoInstrumento"
					AND CFG."Nemotecnico" = MON."Nemotecnico"
					AND CFG."IdAccion" = 'SOLO'
					AND CFG."IdTipoMoneda" = 2;
							
	p_out_Monedas = 
		SELECT	*
		FROM	:p_out_Monedas
		---------
		UNION
		---------	
		SELECT	MON."IdMoneda", MON."Nombre", MON."NombreLargo", 
				MON."IdTipoInstrumento", MON."Nemotecnico", 2 AS "OrdenEnPar" 
		FROM	:t_Monedas MON
				LEFT JOIN :t_Config CFG 
				ON	MON."IdMoneda" = CFG."IdNemoMoneda" 
					AND CFG."IdTipoInstrumento" = MON."IdTipoInstrumento"
					AND CFG."Nemotecnico" = MON."Nemotecnico"
					AND CFG."IdAccion" = 'EXCL'
					AND CFG."IdTipoMoneda" = 2	
		WHERE	COALESCE( CFG."IdNemoMoneda", '_NO_' ) = '_NO_' 
				AND CONCAT(MON."IdTipoInstrumento",MON."Nemotecnico") NOT IN 
				( 	SELECT DISTINCT CONCAT("IdTipoInstrumento","Nemotecnico") 
					FROM :p_out_Monedas WHERE "OrdenEnPar" = 2 	);



	--* ----------------------------------------------------------------------------------------------		
	--* FINALMENTE SE AGREGAN TODAS LAS MONEDAS (SIN PRODUCTO NI INSTRUMENTO ASOCIADO)
	--* ----------------------------------------------------------------------------------------------
	p_out_Monedas = 
		SELECT	DISTINCT 
				MON."IdMoneda", MON."Nombre", MON."NombreLargo", 
				MON."IdTipoInstrumento", 
				'' AS "Nemotecnico", 
				MON."OrdenEnPar"
		FROM	:p_out_Monedas MON
		---------
		UNION
		---------		
		SELECT	*
		FROM	:p_out_Monedas;

			
END;