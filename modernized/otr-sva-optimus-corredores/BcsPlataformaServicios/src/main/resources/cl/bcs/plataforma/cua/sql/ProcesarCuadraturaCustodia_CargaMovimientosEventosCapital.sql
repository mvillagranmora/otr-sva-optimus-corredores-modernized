INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCustodiaRF" ("Fecha","DocumentoIdentidad","TipoDocumentoIdentidad","IdPortafolio","CodigoPortafolio","IdCliente","Nemotecnico","IdInstrumento","IdTipoMovimiento","MontoOperacionFinal")
SELECT ec."FechaOperacion",ec."DocumentoIdentidad",CLI."TipoDocumentoIdentidad",ec."IdPortafolio",Port."CodigoPortafolio", Port."IdCliente"
,ec."Nemotecnico",car."IdInstrumento",'CORCUP',ec."MontoCompra" 
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaPagoCorteCupon" AS ec	
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = ec."IdPortafolio" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON ec."IdCliente" = CLI."IdCliente" 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" car ON ec."FolioCartera"=car."FolioCartera" 
LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" carH ON ec."FolioCartera"=carH."FolioCartera"
WHERE ec."IdClasificacionTipoInstrumento"  IN ('RF','IF')  
AND ec."FechaOperacion" >:p_FechaInicio  AND ec."FechaOperacion"<= :p_FechaFin