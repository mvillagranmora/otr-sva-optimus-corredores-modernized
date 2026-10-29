SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::COM.CalcularCobros";
CREATE PROCEDURE 
-- 
--  Proposito: Calcular cobros
--  Autor: TEAM ORDENES
--  Fecha Creacion: 31/08/2015
--  
"BSCL.Plataforma.BD.Servicios::COM.CalcularCobros"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa            BIGINT,
    
 	IN p_Cobros		 		TABLE(
								"IdOperacion"				BIGINT,
								"IdCobro" 					NVARCHAR(10),
								"ValorCobroPerfil"			DECIMAL(26,8),
								"MontoMinimo" 				DECIMAL(26,8),
								"IdMoneda" 					NVARCHAR(3),
								"FormaCobro" 				NVARCHAR(1),
								"CodigoCobroClasificacion" 	NVARCHAR(40),
								"Cantidad" 					DECIMAL(26,8),
    							"MontoOperacion"			DECIMAL(26,8),
    							"ValorRescate"				DECIMAL(26,8),
    							"DiasPlazo"					BIGINT,
    							"PermiteCambiarValor"		NVARCHAR(1)
								),
    IN p_CobrosModificados	TABLE(
    							"IdOperacion" 			BIGINT,
    							"IdCobro" 				NVARCHAR(40),
    							"ValorCobroAplicado"	DECIMAL(26,8)
    							),
    --VARIABLES DE SALIDA OBLIGATORIAS
    OUT p_out_Cobros          TABLE (
    
    							"IdOperacion" 				BIGINT,
    							"IdCobro" 					NVARCHAR(10),
								"ValorCobroPerfil"			DECIMAL(26,8),
								"MontoMinimo" 				DECIMAL(26,8),
								"IdMoneda" 					NVARCHAR(3),
								"FormaCobro" 				NVARCHAR(1),
								"CodigoCobroClasificacion" 	NVARCHAR(40),
								"Cantidad" 					DECIMAL(26,8),
    							"MontoOperacion"			DECIMAL(26,8),
    							"ValorRescate"				DECIMAL(26,8),
    							"DiasPlazo"					BIGINT,
    							"PermiteCambiarValor"		NVARCHAR(1),
    							"ValorCobroAplicado" 		DECIMAL(26,8), 
    							"MontoCobroCalculado" 		DECIMAL(26,8)
    							)    
	) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	
	v_cobros =	select  pcob."IdOperacion",
						pcob."IdCobro",
					 	pcob."FormaCobro",
					 	pcob."IdMoneda",
					 	pcob."ValorCobroPerfil",
					 	pcob."MontoMinimo",
					 	pcob."CodigoCobroClasificacion",
					 	pcob."Cantidad",
    					pcob."MontoOperacion",
    					pcob."ValorRescate",
    					pcob."DiasPlazo",
    					pcob."PermiteCambiarValor",
    					COALESCE(cob_mod."ValorCobroAplicado",pcob."ValorCobroPerfil",0) as "ValorCobroAplicado"
	            from :p_Cobros pcob
	            LEFT JOIN :p_CobrosModificados cob_mod on	pcob."IdOperacion" = cob_mod."IdOperacion" AND 
	            											pcob."IdCobro" = cob_mod."IdCobro";
	
	v_neto_cobros =	select 	cob."IdOperacion",
							cob."IdCobro",
							cob."ValorCobroPerfil",
							cob."MontoMinimo",
							cob."IdMoneda",
							cob."FormaCobro",
							cob."CodigoCobroClasificacion",
							cob."Cantidad",
    						cob."MontoOperacion",
    						cob."ValorRescate",
    						cob."DiasPlazo",
    						cob."PermiteCambiarValor",
    						cob."ValorCobroAplicado",						   				  					   
                   			case cob."IdCobro"
                   				WHEN 'COVA' THEN
                   					CASE 
                   						WHEN ( cob."ValorCobroAplicado" * cob."MontoOperacion" / 100.0) > cob."MontoMinimo" then ( cob."ValorCobroAplicado" * cob."MontoOperacion" / 100.0)
	                   					ELSE cob."MontoMinimo"
                   					END
                   				WHEN 'CCOR' THEN
                   					CASE 
                   						WHEN ( cob."ValorCobroAplicado" * cob."MontoOperacion" / 100.0) > cob."MontoMinimo" then ( cob."ValorCobroAplicado" * cob."MontoOperacion" / 100.0)
	                   					ELSE cob."MontoMinimo"
                   					END
                   				WHEN 'COSI' THEN
                   					CASE 
                   						WHEN (cob."ValorCobroAplicado" * cob."MontoOperacion" / 100.0)*(cob."DiasPlazo"/30.0) > cob."MontoMinimo" THEN (cob."ValorCobroAplicado" * cob."MontoOperacion" / 100.0)*(cob."DiasPlazo"/30.0)
                   						ELSE cob."MontoMinimo"
                   					END
                   				WHEN 'COFI' then ( cob."ValorCobroAplicado" )
                   				WHEN 'DEVA' THEN
                   					CASE 
                   						WHEN ( cob."ValorCobroAplicado" * cob."MontoOperacion" / 100.0) > cob."MontoMinimo" then ( cob."ValorCobroAplicado" * cob."MontoOperacion" / 100.0)
	                   					ELSE cob."MontoMinimo"
                   					END
                	 			WHEN 'DEFI' then ( cob."ValorCobroAplicado" )
                	 			WHEN 'GAFI' then ( cob."ValorCobroAplicado" )
                	 			WHEN 'GFFA' then ( cob."ValorCobroAplicado" )
                	 			WHEN 'GFOD' then ( cob."ValorCobroAplicado" )
                	  			else 0.0
                   			end as "MontoCobroCalculado"
             		from :v_cobros as cob
            		where cob."CodigoCobroClasificacion" in ('COMISION','DERECHO','GASTO');    
	
	v_impuestos =	select	cob."IdOperacion",
							cob."IdCobro",
							cob."ValorCobroPerfil",
							cob."MontoMinimo",
							cob."IdMoneda",
							cob."FormaCobro",
							cob."CodigoCobroClasificacion",
							cob."Cantidad",
    						cob."MontoOperacion",
    						cob."ValorRescate",
    						cob."DiasPlazo",
    						cob."PermiteCambiarValor",
						   	cob."ValorCobroAplicado", 
		                   	case cob."FormaCobro"
		                		when 'V' then ( cob."ValorCobroAplicado" * SUM(COALESCE(neto."MontoCobroCalculado",0)) /100.0 ) 
		                	  	else 0.0
		                   	end as "MontoCobroCalculado"
		     		from :v_cobros as cob
		     		LEFT JOIN :v_neto_cobros as neto on cob."IdOperacion" = neto."IdOperacion"
		     		where cob."CodigoCobroClasificacion" in ('IMPUESTO')
		            GROUP BY cob."IdOperacion",
							cob."IdCobro",
							cob."ValorCobroPerfil",
							cob."MontoMinimo",
							cob."IdMoneda",
							cob."FormaCobro",
							cob."CodigoCobroClasificacion",
							cob."Cantidad",
    						cob."MontoOperacion",
    						cob."ValorRescate",
    						cob."DiasPlazo",
    						cob."PermiteCambiarValor",
    						cob."ValorCobroAplicado";
    						
	--select 'v_cobros',* from :v_cobros;
	--select 'v_neto_cobros',* from :v_neto_cobros;
	
	
	
	p_out_Cobros = select * from :v_neto_cobros
					union
					select * from :v_impuestos;
	
	
	
END;
