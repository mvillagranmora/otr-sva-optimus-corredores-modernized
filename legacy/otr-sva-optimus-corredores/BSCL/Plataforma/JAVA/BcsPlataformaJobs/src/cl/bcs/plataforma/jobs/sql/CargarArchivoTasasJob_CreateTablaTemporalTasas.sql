CREATE LOCAL TEMPORARY TABLE #CargarArchivoTasas_p_DataTasas(	
"Nemo"			NVARCHAR(40),
"IdMoneda"		NVARCHAR(3),
"Tasa"			DECIMAL(26,8),
"TasaEstimada"	DECIMAL(26,8),
"Plazo"			BIGINT
);