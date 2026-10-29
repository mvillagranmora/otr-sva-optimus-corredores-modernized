SELECT IDENT."DocumentoIdentidad",
  port."CodigoPortafolio",
  pernom."NombreCompleto",
  SolicitudAporteRescate."IdCliente",
  SolicitudAporteRescate."IdPortafolio" 
  FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporteRescate
  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" port ON SolicitudAporteRescate."IdPortafolio" = PORT."IdPortafolio"
  LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on port."IdCliente" = cli."IdCliente"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON PER."IdPersona" = cli."IdPersona"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND IDENT."Predeterminado" ='S'
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" pernom on pernom."IdPersona" = per."IdPersona"
  LEFT JOIN "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS Interfaz ON (Interfaz."DocumentoIdentidad" = IDENT."DocumentoIdentidad" AND Interfaz."CodigoPortafolio" = port."CodigoPortafolio")
  WHERE SolicitudAporteRescate."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) AND COALESCE(Interfaz."IdInterfazAgf",0) = 0
  GROUP BY 
  IDENT."DocumentoIdentidad"
  ,port."CodigoPortafolio"
  ,pernom."NombreCompleto"
  ,SolicitudAporteRescate."IdCliente"
  ,SolicitudAporteRescate."IdPortafolio";