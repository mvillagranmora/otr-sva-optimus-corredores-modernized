SET SCHEMA "BSCL";
DROP FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor";


CREATE FUNCTION 
"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" (
	p_IdEmisor				NVARCHAR(40),
	p_IdClaseInstrumento	NVARCHAR(3),
	p_TipoInstrumento		NVARCHAR(3),
	p_Nemotecnico			NVARCHAR(40),
	p_IdTipoReajuste		NVARCHAR(3)
) 
RETURNS p_out_Clasificacion NVARCHAR(2) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
AS

BEGIN
	DECLARE v_Clasificacion NVARCHAR(2);
	v_Clasificacion := '';
  
	IF COALESCE(:p_IdClaseInstrumento,'') = 'IF'  THEN
		p_out_Clasificacion :=	CASE
								WHEN p_Nemotecnico = 'PDBC' THEN '1'  
								
								
								WHEN instr(p_Nemotecnico,'PDBC')> 0 AND p_IdEmisor = 'CENTRAL' THEN '1' 
	
	
	
	
								WHEN p_Nemotecnico = 'PRBC' THEN '2'  
								WHEN p_IdEmisor = 'TESORERIA' THEN '13'  
								WHEN p_Nemotecnico = 'PDT' THEN '12'  
								WHEN p_Nemotecnico = 'PAGARE NR' AND p_IdEmisor = 'CENTRAL'  THEN '1'  
								WHEN p_Nemotecnico = 'PAGARE NR' AND p_IdEmisor <> 'CENTRAL' THEN '15'  
								WHEN p_Nemotecnico = 'PAGARE R'  AND p_IdEmisor = 'CENTRAL'  THEN '2'  
								WHEN p_Nemotecnico = 'PAGARE R'  AND p_IdEmisor <> 'CENTRAL' AND p_IdTipoReajuste = 'USD' THEN '16'  
								WHEN p_Nemotecnico = 'PAGARE R'  AND p_IdEmisor <> 'CENTRAL' AND p_IdTipoReajuste = 'UF' THEN '17'  
								WHEN p_Nemotecnico = 'PAGARE R'  AND p_IdTipoReajuste = 'ICP' THEN '22'
								ELSE '21'
						END;
	END IF; --IF
	
	IF COALESCE(:p_IdClaseInstrumento,'') = 'RF' THEN 
			IF COALESCE(:p_IdEmisor,'') = 'CENTRAL' OR  COALESCE(:p_IdEmisor,'') = 'TESORERIA' THEN --IN ('BT','CE')	
				p_out_Clasificacion :=	
						CASE p_TipoInstrumento
								WHEN 'PPBC' THEN '3'  
								WHEN 'PCC'  THEN '4'
								WHEN 'PCD'  THEN
									CASE p_IdTipoReajuste    
										WHEN 'UF'   THEN '25'    
										WHEN 'USD'  THEN '26'    
										ELSE '25'    
									END     
								WHEN 'PTF'  THEN '6'  
								WHEN 'PDP'  THEN '7' 
								WHEN 'PPS'  THEN '8'								  
								WHEN 'PRC'  THEN '9'  --PRC;PRC;PAGARE REAJUSTABLE BANCO CENTRAL;PAGARE REAJUSTABLE BANCO CENTRAL;VIG;RF								
								WHEN 'CAR'  THEN '10'  								
								WHEN 'PRD'  THEN '11' --PRD;PRD;PAGARÉS REAJUSTABLES DEL BANCO CENTRAL DE CHILE EN DÓLARES;PAGARÉS REAJUSTABLES DEL BANCO CENTRAL DE CHILE EN DÓLARES;VIG;RF								
								WHEN 'PRT'  THEN '13'  								
								WHEN 'BCX'  THEN '19' --BCX;BCX;BONOS BANCO CENTRAL EN DÓLARES;BONOS BANCO CENTRAL EN DÓLARES;VIG;RF 	
								WHEN 'PEP'  THEN '24'  	
								WHEN 'BR'   THEN '27' --BR;BR;BONO DE RECONOCIMIENTO;BONO DE RECONOCIMIENTO;VIG;RF	 
								WHEN 'ZER'  THEN '28' --ZER;ZERO;CUPONES DE EMISION REAJUSTABLES EN DOLARES;CUPONES DE EMISION REAJUSTABLES EN DOLARES;VIG;RF	 
								WHEN 'BCD'  THEN '29' --BCD;BCD;BANCO CENTRAL BONOS BULLET EN DOLARES;BANCO CENTRAL BONOS BULLET EN DOLARES;VIG;RF	 
								WHEN 'BCP'  THEN '30' --BCP;BCP;BANCO CENTRAL BONOS BULLET EN PESOS;BANCO CENTRAL BONOS BULLET EN PESOS;VIG;RF	
								WHEN 'BCU'  THEN '31' --BCU;BCU;BANCO CENTRAL BONOS BULLET EN UF;BANCO CENTRAL BONOS BULLET EN UF;VIG;RF	
								WHEN 'CER' THEN '33'   --CER;CERO;CUPONES DE EMISION REAJUSTABLES EN UF;CUPONES DE EMISION REAJUSTABLES EN UF;VIG;RF 
								ELSE '14'  
							END;
			ELSE 
				IF  COALESCE(:p_IdEmisor,'') = 'BTU' OR  COALESCE(:p_IdEmisor,'') = 'BTP' THEN
		
					p_out_Clasificacion := '14';
							
				ELSE 
					IF COALESCE(:p_TipoInstrumento,'') = 'LTH'  THEN
			
						p_out_Clasificacion :=	
									CASE p_IdEmisor  
											WHEN 'MVU' THEN '23'  
									ELSE '18'  
									END;
					ELSE 
						IF COALESCE(:p_TipoInstrumento,'') IN ('BEM', 'BBC') THEN
				
							p_out_Clasificacion :=
									CASE  
										WHEN LOCATE(LEFT(p_IdEmisor,2) || LEFT(p_Nemotecnico,2), 'BB') >= 1 AND COALESCE(:p_TipoInstrumento,'') = 'BBC' THEN '20'
	--									WHEN 'BB' IN (LEFT(p_IdEmisor,2), LEFT(p_Nemotecnico,2)) THEN '20'    --Otros títulos entidades financieras
										WHEN p_IdEmisor = 'FFCC' THEN '22'  
										ELSE '34'    -- Acá caerán los BONOS EMPRESA
									END;  
						ELSE   
							p_out_Clasificacion := '21';
						END IF;   
					END IF;  
				END IF;
				
			END IF;		      
	END IF; --RF
	
	
	p_out_Clasificacion := IFNULL(p_out_Clasificacion,'21');  
END
