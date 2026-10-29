SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoResultado";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoResultado" 
AS
		SELECT 
			 0 as "IdIndice" --NOR_ilsInformeResultadoindice.IdIndice				 AS IdIndice
			,'' as "NombreIndice" --NOR_ilsInformeResultadoindice.NombreIndice			 AS NombreIndice
			,0 as "IdResultadoConcepto" --NOR_ilsInformeResultadoConcepto.IdResultadoConcepto AS IdResultadoConcepto
			,0 as "IdResultadoIndice" --NOR_ilsInformeResultadoConcepto.IdResultadoIndice	 AS IdResultadoIndice
			,0 as "Monto" --NOR_ilsInformeResultadoConcepto.Monto				 AS Monto
			,'' as "NombreConcepto" --NOR_ilsInformeResultadoConcepto.NombreConcepto		 AS NombreConcepto
			,0 as "IdConcepto" --NOR_ilsInformeResultadoConcepto.IdConcepto			 AS IdConcepto
			,CASE 'N' --NOR_ilsInformeResultadoConcepto.Posicion			
				WHEN 'N' THEN				
					'Numerador'
				WHEN 'D' THEN
					'Denominador'
				else
					'N/A'
			END AS "Posicion"
			,'' as "Operador" --NOR_ilsInformeResultadoConcepto.Operador	AS Operador
			,'' as "Factor" --NOR_ilsInformeResultadoConcepto.Factor		AS Factor
			,0 as "MontoCalculado" --NOR_ilsInformeResultadoConcepto.MontoCalculado	* NOR_ilsInformeResultadoConcepto.Factor AS MontoCalculado
			,'' as "Estado" --NOR_ilsInformeResultadoConcepto.Estado		AS Estado
		FROM DUMMY;
		/*
			NOR_ilsInformeResultadoindice
		INNER JOIN
			NOR_ilsInformeResultadoConcepto
		ON 
			NOR_ilsInformeResultadoindice.IdResultadoIndice = NOR_ilsInformeResultadoConcepto.IdResultadoIndice and
			NOR_ilsInformeResultadoindice.IdTipoInforme = @pIdTipoInforme and 
			NOR_ilsInformeResultadoindice.FechaContable = @pFechaContable
		WHERE 
			NOR_ilsInformeResultadoConcepto.Estado = 'V'

		ORDER BY 
			NOR_ilsInformeResultadoindice.IdIndice,
			NOR_ilsInformeResultadoConcepto.IdConcepto	ASC		
	*/

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoResultado" TO BCS_DESARROLLADOR WITH GRANT OPTION;
