SELECT 	
	 G."IdCliente"	
	,G."IdPortafolio"
	,G."IdConcepto"
	,G."IdTipoInstrumentoGarantizado"   
	,G."IdInstrumentoGarantizado"         
	,G."IdContraparteGarantia"
	,G."IdOperacionOrigenGarantia"
	,SUM(G."ExigibilidadEntidad") AS "MontoExigibilidadEntidad"
	,SUM(G."ExigibilidadCorredora") AS "MontoExigibilidadCorredora"
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaOperaciones" G
	WHERE   G."IdCliente" 					= (CASE WHEN COALESCE(:p_IdCliente,0) <> 0 THEN :p_IdCliente ELSE G."IdCliente" END)
	AND G."IdPortafolio" 					= (CASE WHEN COALESCE(:p_IdPortafolio,0) <> 0 THEN :p_IdPortafolio ELSE G."IdPortafolio" END)
	AND G."IdTipoInstrumentoGarantizado"	= (CASE WHEN COALESCE(:p_IdTipoInstrumentoGarantizado,'') <> '' THEN :p_IdTipoInstrumentoGarantizado ELSE G."IdTipoInstrumentoGarantizado" END)
	AND G."IdInstrumentoGarantizado" 		= (CASE WHEN COALESCE(:p_IdInstrumentoCarteraGarantizada,0) <> 0 THEN :p_IdInstrumentoCarteraGarantizada ELSE G."IdInstrumentoGarantizado" END)
	AND G."IdContraparteGarantia" 		= (CASE WHEN COALESCE(:p_IdContraparteGarantia,0) <> 0 THEN :p_IdContraparteGarantia ELSE G."IdContraparteGarantia" END)
	AND G."IdTipoInstrumentoGarantizado" <> 'FWD'
	GROUP BY G."IdCliente", G."IdPortafolio", G."IdConcepto", G."IdTipoInstrumentoGarantizado",
	     G."IdInstrumentoGarantizado", G."IdContraparteGarantia", G."IdOperacionOrigenGarantia"