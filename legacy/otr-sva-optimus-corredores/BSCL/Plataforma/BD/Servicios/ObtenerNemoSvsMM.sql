SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ObtenerNemoSvsMM";

CREATE PROCEDURE
-- 
--  Proposito: Obtener nemo svs para instrumentos de intermediacion financiera
--  Autor: asdfasdfadf
--  Fecha Creacion: 06/08/2015
--  
"BSCL.Plataforma.BD.Servicios::ObtenerNemoSvsMM" (
	IN p_in	TABLE(
				"IdInterno"			BIGINT,
				"IdEmisor"			NVARCHAR(40),
				"Fecha"				NVARCHAR(10),
				"Plazo"				BIGINT,
				"Nemotecnico"		NVARCHAR(40),
				"IdTipoReajuste"	NVARCHAR(3),
				"PagaderoEn"		NVARCHAR(3)
			),
	OUT p_out_NemoSvs TABLE (
					"IdInterno"		BIGINT,
					"NemoSVS"		NVARCHAR(40)
					)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
		
	--------------------
	--LOGICA 
	--------------------

	v_fechasVencimiento = SELECT "IdInterno", ADD_DAYS (TO_DATE ("Fecha", 'YYYY-MM-DD'), "Plazo") as "FechaVencimiento" FROM :p_in;
	v_f1 = SELECT pp."IdInterno", UCASE(pp."IdEmisor") as "IdEmisor",UCASE(pp."IdTipoReajuste") as "IdTipoReajuste",UCASE(pp."Nemotecnico") as "Nemotecnico","PagaderoEn",
			DAYOFMONTH ("FechaVencimiento") as "FechaVencimientoDay",MONTH ("FechaVencimiento") as "FechaVencimientoMonth", RIGHT (YEAR ("FechaVencimiento"),2) as "FechaVencimientoYear"
			FROM :p_in pp
			INNER JOIN :v_fechasVencimiento vec on pp."IdInterno" = vec."IdInterno";
	
	
	v_f2 = SELECT f1."IdInterno", f1."IdEmisor",f1."IdTipoReajuste",f1."Nemotecnico",
					CASE WHEN LENGTH (f1."FechaVencimientoDay") = 1 THEN CONCAT ('0', f1."FechaVencimientoDay") ELSE '' || f1."FechaVencimientoDay" END AS "FechaVencimientoDay"  ,
					CASE WHEN LENGTH (f1."FechaVencimientoMonth") = 1 THEN CONCAT ('0', f1."FechaVencimientoMonth") ELSE '' || f1."FechaVencimientoMonth" END AS "FechaVencimientoMonth"  ,
					f1."FechaVencimientoYear" ,
					CASE emi."TipoEntidad"
						WHEN 1 THEN 'B' --BANCOS
 						WHEN 2 THEN 'F' --FINANCIERAS
 						WHEN 3 THEN 'S' --SOCIEDADES
 						WHEN 4 THEN 'O' --OTRAS
 						WHEN 5 THEN 'T' --TESORERIAS
 						ELSE ''
	 						
	 				END as "CodigoTipoEntidad",
					CASE f1."IdEmisor"
						WHEN 'CENTRAL' THEN
							CASE f1."Nemotecnico"
								WHEN 'PAGARE R' THEN 'PRBC'
				 				WHEN 'PRBC' THEN 'PRBC'
	 							WHEN 'PAGARE NR' THEN 'PDBC'
				 				WHEN 'PDBC' THEN 'PDBC'
				 				ELSE ''
				 			END
						ELSE
							CASE LENGTH(emi."CodigoRegulador") WHEN 4 THEN emi."CodigoRegulador" WHEN 3 THEN emi."CodigoRegulador" || '-' ELSE emi."CodigoRegulador" END
								
	 						
	 					END AS "CodigoEmisor",
	 				CASE f1."IdTipoReajuste"
	 					WHEN 'CLP' THEN 'N'
	 					WHEN 'UF' THEN 'U'
		 				WHEN 'USD' THEN CASE f1."PagaderoEn" WHEN 'USD' THEN '*' ELSE 'D' END
		 				WHEN 'EUR' THEN CASE f1."PagaderoEn" WHEN 'EUR' THEN 'R' ELSE 'E' END
		 				WHEN 'IVP' THEN 'I'
		 				WHEN 'ICP' THEN 'O'
		 				ELSE 'O'
		 			END AS "CodigoTipoReajuste"
			FROM :v_f1 f1
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi on f1."IdEmisor" = emi."IdEmisor";
	
	p_out_NemoSvs = SELECT "IdInterno", 
					CONCAT("CodigoTipoEntidad",CONCAT("CodigoTipoReajuste",CONCAT("CodigoEmisor",CONCAT("FechaVencimientoDay",CONCAT("FechaVencimientoMonth","FechaVencimientoYear"))))) as "NemoSVS"
					FROM :v_f2;	
 	
 	 
END;
