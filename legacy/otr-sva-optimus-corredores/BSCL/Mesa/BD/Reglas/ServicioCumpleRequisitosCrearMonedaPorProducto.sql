SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearMonedaPorProducto";

CREATE PROCEDURE
-- 
--  Proposito: Validar datos del cliente para ingresar Moneda por Producto.
--  Autor: BCS
--  Fecha Creacion: 01/07/2015  
--   .CRE::
"BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearMonedaPorProducto"(
     IN p_DatosMonedaPorProducto 			TABLE(	
     												"IdInstrumento"  BIGINT,
     												"IdEmpresa" BIGINT,
													"Nemotecnico"	NVARCHAR(6), 
													"IdTipoProducto" NVARCHAR(4), 
													"IdMonedaPrincipal"	NVARCHAR(3),
    												"IdMonedaSecundaria"	NVARCHAR(3),
    												"IdEstado" NVARCHAR(3)         
												),

	OUT p_out_SalidaDetalle     TABLE ("IdInstrumento" BIGINT, "Resultado" NVARCHAR(40), "Codigo" nvarchar(40),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40)),
	OUT p_out_Salida        	TABLE ("IdInstrumento" BIGINT, "Resultado" NVARCHAR(40))   --OK=Cumple todos los requisitos
																						--ADV=Advertencia
																						--AUT=Autorizacion							  	   
                                                   										--ERR=Error
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
READS SQL DATA AS
BEGIN

	v_matriz_validaciones =	SELECT 	mpp."IdInstrumento",
									mpp."Nemotecnico",
									mpp."IdMonedaPrincipal",
									mpp."IdMonedaSecundaria",
								1 as "EXISTE_INSTRUMENTO"
							FROM :p_DatosMonedaPorProducto mpp;
							
					
	v_TablaRequisitos =
		SELECT mpp."IdInstrumento",
			   CASE WHEN val."EXISTE_INSTRUMENTO" = 0 or count(tim."Nemotecnico") = 0 THEN 1 ELSE 4 END as "IdResultado", 
			   'YA_EXISTE_INSTRUMENTO' as "Codigo", 
			   CAST(mpp."IdInstrumento" AS NVARCHAR(100)) as "ValorParametro", 
			   'IdInstrumento' as "NombreParametro"
		FROM :p_DatosMonedaPorProducto mpp
		INNER JOIN :v_matriz_validaciones val 
		ON mpp."IdInstrumento" = val."IdInstrumento" 
		LEFT JOIN "BSCL.Mesa.BD::MES.InstrumentoMoneda" tim 
		ON mpp."Nemotecnico" = tim."Nemotecnico"
		AND mpp."IdMonedaPrincipal" = tim."IdMonedaPrincipal"
		AND mpp."IdMonedaSecundaria" = tim."IdMonedaSecundaria"
		GROUP BY mpp."IdInstrumento", val."EXISTE_INSTRUMENTO"
		;
	
	p_out_SalidaDetalle =	SELECT 
								tb."IdInstrumento",
								CASE "IdResultado"
									WHEN 1 THEN 'OK'
									WHEN 2 THEN 'ADV'
									WHEN 3 THEN 'AUT'
									WHEN 4 THEN 'ERR'
								END as "Resultado",
								tb."Codigo" ,
								tb."ValorParametro" ,
								tb."NombreParametro" 
							FROM :v_TablaRequisitos tb
							WHERE "IdResultado" != 1
							ORDER BY "IdInstrumento";
							
	p_out_Salida = 	SELECT "IdInstrumento",CASE MAX("IdResultado")
									WHEN 1 THEN 'OK'
									WHEN 2 THEN 'ADV'
									WHEN 3 THEN 'AUT'
									WHEN 4 THEN 'ERR'
								END AS "Resultado"
								
					FROM :v_TablaRequisitos 
					GROUP BY "IdInstrumento"
					ORDER BY "IdInstrumento";

	
END;
