CREATE LOCAL TEMPORARY TABLE #p_BloqueoSolicitudAporteRescate (
    "IdBloqueoSolicitudAporteRescate"   NVARCHAR(120),
    "IdPortafolio"						BIGINT,
    "IdSerieFondo"						NVARCHAR(40),
    "IdRegimenTributario"				NVARCHAR(40),
    "TipoSolicitud"      				NVARCHAR(3),
    "Monto"  							DECIMAL(26,8),
    "NumeroCuotas"  					DECIMAL(26,8),
    "FechaSistema"      				DATE
);