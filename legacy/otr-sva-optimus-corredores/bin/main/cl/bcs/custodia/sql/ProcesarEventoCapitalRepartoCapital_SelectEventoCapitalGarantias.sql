SELECT "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL AS "IdEventoCapitalCliente", "IdEventoCapital", "IdTipoEventoCapital", "Secuencia", 'CNT' AS "TipoSocioNegocio", EventClie."IdClasificacionTipoInstrumento", 
        EventClie."IdTipoInstrumento", EventClie."IdInstrumento", "Nemo",CAST(0 AS BIGINT) AS "IdInstrumentoNuevo", '' AS "NemoNuevo", "FechaCierre", "FechaPago", 
        (Sum("CantidadDerecho") * -1) AS "CantidadDerecho", "TipoCartera", "IdConceptoGarantia", "IdTipoOperacionGarantia", CAST(0 AS DECIMAL(26,8)) AS "CantidadNueva", 
        CAST(0 AS DECIMAL(26,8)) AS "MontoCapital", CAST(0 AS DECIMAL(26,8)) AS "MontoInteres", (Sum("CantidadDerecho") * -1) AS "Total",
        "IdMoneda", "SecuenciaCupon", EventClie."IdEstado", "IdBoveda", "IdBolsa", 
        MAX("NumeroDividendo") AS "NumeroDividendo" , '' AS "IdTipoCuentaCustodio", '' AS "IdTipoReajuste", '' AS "CodigoCorredorContraparte", '' AS "TipoOpera cion",
        MAX("NumeroCuentaBoveda") AS "NumeroCuentaBoveda", CAST(0 AS DECIMAL(26,8)) AS "MontoRetencion", CAST(0 AS BIGINT) AS "IdCorredorContraparte", 'N' AS "AfectoaClearing",
        MAX(SocNeg."IdSocioDeNegocio") AS "IdSocioNegocio", MAX(Portafolio."IdPortafolio") AS "IdPortafolio", Max(DocInd."DocumentoIdentidad") AS "DocumentoIdentidad", 
        MAX(Persona."RazonSocial") AS "NombreCliente", MAX(DocInd."IdTipoDocumentoIdentidad") AS "IdTipoDocumentoIdentidad", 
        MAX(ConEjecPort."IdEmpleadoEjecutivoPortafolio") AS "IdEmpleadoEjecutivoPortafolio", MAX(ConEjecPort."CodigoSucursal") AS "CodigoSucursal",
        MAX(ConEjecPort."CodigoEmpleado") AS "CodigoUsuarioAgente", MAX(ConEjecPort."NombreCompletoEjecutivo") AS "NombreAgente"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventClie
   INNER JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" AS ConCus
     ON ConCus."IdConcepto" = EventClie."IdConceptoGarantia"
   INNER JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" AS Contraparte
     ON Contraparte."IdContraparte" = ConCus."IdContraparte"
   INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
     ON Persona."IdPersona" = Contraparte."IdPersona"
   INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocInd
     ON DocInd."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
   INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocNeg
     ON SocNeg."IdPersona" = Persona."IdPersona"
   INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
    ON Portafolio."IdCliente" = SocNeg."IdCliente"
   INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" AS ConEjecPort
    ON ConEjecPort."IdPortafolio" = Portafolio."IdPortafolio"
   AND ConEjecPort."EsEjecutivoVigente" = 'S'
WHERE "IdEventoCapital" = :IdEventoCapital
   AND "TipoCartera" = 'GAR'
   AND EventClie."IdEstado" = 'PRO'
   AND "TipoSocioNegocio" = 'CLI'
GROUP BY "TipoCartera", "IdConceptoGarantia", "IdTipoOperacionGarantia", "IdBoveda", "IdEventoCapital", "IdTipoEventoCapital", "Secuencia", 
          EventClie."IdClasificacionTipoInstrumento", EventClie."IdTipoInstrumento", EventClie."IdInstrumento", "Nemo", "FechaCierre", 
          "FechaPago", "IdMoneda", "SecuenciaCupon", EventClie."IdEstado", "IdBolsa" ;