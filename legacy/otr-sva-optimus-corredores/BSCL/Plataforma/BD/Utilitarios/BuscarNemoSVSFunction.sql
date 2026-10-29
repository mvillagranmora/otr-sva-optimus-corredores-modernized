SET SCHEMA "BSCL";
DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::BuscarNemoSVSFunction";
				  
																			 
CREATE FUNCTION 
-- 
--  Proposito: Obtiene la fecha UTC
--  Autor: EAR - BCS
--  Fecha Creacion: 01/08/2015
--  
"BSCL.Plataforma.BD.Utilitarios::BuscarNemoSVSFunction" ( 
  IN p_IdEmisor VARCHAR(40),
  IN p_Fecha VARCHAR(10),
  IN p_Plazo BIGINT,
  IN p_Nemotecnico VARCHAR(40),
  IN p_IdTipoReajuste VARCHAR(3),
  IN p_PagaderoEn VARCHAR(3),
  IN p_FechaVencimiento DATE
  ) 
	RETURNS resultado VARCHAR(40)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
            select IFNULL(CASE emi."TipoEntidad"
									WHEN 1 THEN 'B' --BANCOS
			 						WHEN 2 THEN 'F' --FINANCIERAS
			 						WHEN 3 THEN 'S' --SOCIEDADES
			 						WHEN 4 THEN 'O' --OTRAS
			 						WHEN 5 THEN 'T' --TESORERIAS
			 						ELSE ''
				 				END  
			             ||
			             CASE p_IdTipoReajuste
				 					WHEN 'CLP' THEN 'N'
				 					WHEN 'UF' THEN 'U'
					 				WHEN 'USD' THEN CASE p_PagaderoEn WHEN 'USD' THEN '*' ELSE 'D' END
					 				WHEN 'EUR' THEN CASE p_PagaderoEn WHEN 'EUR' THEN 'R' ELSE 'E' END
					 				WHEN 'IVP' THEN 'I'
					 				WHEN 'ICP' THEN 'O'
					 				ELSE 'O'
					 			END 
					     ||
			             CASE p_IdEmisor
									WHEN 'CENTRAL' THEN
										CASE p_Nemotecnico
											WHEN 'PAGARE R' THEN 'PRBC'
							 				WHEN 'PRBC' THEN 'PRBC'
				 							WHEN 'PAGARE NR' THEN 'PDBC'
							 				WHEN 'PDBC' THEN 'PDBC'
							 				ELSE ''
							 			END
									ELSE
										CASE LENGTH(emi."CodigoRegulador") WHEN 4 THEN emi."CodigoRegulador" WHEN 3 THEN emi."CodigoRegulador" || '-' ELSE emi."CodigoRegulador" END
				 					END 
			             || CASE WHEN LENGTH (DAYOFMONTH(p_FechaVencimiento)) = 1 THEN CONCAT ('0',DAYOFMONTH(p_FechaVencimiento)) ELSE '' || DAYOFMONTH(p_FechaVencimiento) END
			             || CASE WHEN LENGTH (MONTH(p_FechaVencimiento)) = 1 THEN CONCAT ('0', MONTH(p_FechaVencimiento)) ELSE '' || MONTH(p_FechaVencimiento) END 
			             || RIGHT (YEAR(p_FechaVencimiento),2),'') as "NemoSVS" INTO resultado
			             FROM DUMMY AS P
						 left join  "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi on  emi."IdEmisor" = p_IdEmisor;
END;