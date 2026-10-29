INSERT INTO #p_BloqueoSolicitudAporteRescate (
"IdBloqueoSolicitudAporteRescate",
"IdPortafolio",				
"IdSerieFondo",
"IdRegimenTributario",
"TipoSolicitud",
"Monto",
"NumeroCuotas",
"FechaSistema"
)
VALUES (
:IdBloqueoSolicitudAporteRescate,
:IdPortafolio,
:IdSerieFondo,
:IdRegimenTributario,
:TipoSolicitud,
:Monto,
:NumeroCuotas,
:FechaSistema
);