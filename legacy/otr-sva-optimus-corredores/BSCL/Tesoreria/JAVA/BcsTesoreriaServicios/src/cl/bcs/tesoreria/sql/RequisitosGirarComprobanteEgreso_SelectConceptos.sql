SELECT 
	CASE 
		 WHEN (Count(Distinct "COM"."IdConcepto") > 1) THEN 'MULCPT'
		 ELSE MAX("COM"."IdConcepto")
	END AS "p_out_IdConcepto",						
	CASE 
		 WHEN (Count(Distinct "COM"."IdConcepto") > 1) THEN 'MULTIPLE CONCEPTOS'
		 ELSE MAX("CON"."Nombre")
	END AS "p_out_NombreConcepto"
				FROM  #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar AS GIR				
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM  ON (COM."IdComprobante" = GIR."IdComprobante")
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" AS CON ON (CON."IdConcepto" = COM."IdConcepto");