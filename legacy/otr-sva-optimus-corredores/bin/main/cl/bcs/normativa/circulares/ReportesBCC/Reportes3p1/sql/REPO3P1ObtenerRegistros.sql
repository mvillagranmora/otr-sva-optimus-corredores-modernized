SELECT 	"ID",
		"IdDocumento",
		"EstadoSICAM",
		 "TipoRegistro",
		 "RUTReportante", 
		 TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",  
		 "HoraOperacion", 
		 "TipoOperacion", 
		 "EventoReporte", 
		 "Identificador",
		 "RUTContraparte", 
		 "LEIContraparte", 
		 "NombreContraparte", 
		 "PlataformaNegociacion", 
		 "MontoMonedaComprada", 
		 "MontoMonedaVendida", 
		 "TipoCambio"
FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" REPO
WHERE REPO."ID" = :p_ID
AND REPO."EstadoSICAM" = 'ENV';
