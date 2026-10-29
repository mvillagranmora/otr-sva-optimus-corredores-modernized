SELECT TOP 1
    CE."NombreCompleto"  AS "NombreEjecutivo",
    CPDE."Direccion"     AS "Direccion",
    CPT."Numero"         AS "Telefono",
    CE2."NombreCompleto" AS "JefeDirecto",
    CEJ."NombreSupervisor" AS "Supervisor",
    (SELECT COUNT(CAP."IdDetalleAsuntoPendiente")
     FROM "BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntosPendientes" CAP
     WHERE CAP."IdEstadoDetalle" = 'SNC'
       AND CAP."IdEmpleado" = :IdEmpleado
       AND CAP."IdEmpresa" = :IdEmpresa) AS "AsuntosPendientes",
    (SELECT COUNT(C."IdCliente")
     FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" P
     INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" C ON P."IdPersona" = C."IdPersona"
     WHERE C."IdEmpleadoEjecutivo" = :IdEmpleado
       AND C."IdEstado" = 'ACT'
       AND P."EsEnrolado" = 'S'
       AND C."IdEmpresa" = :IdEmpresa) AS "ClientesActivos",
    (SELECT COUNT(CO."IdOrden")
     FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" CO
     WHERE CO."IdEmpleadoPortafolio" = :IdEmpleado
       AND CO."IdEstadoOrden" = 'ING'
       AND CO."IdEmpresa" = :IdEmpresa) AS "OrdenesAccionesVigentes",
    (SELECT COUNT(CE3."IdEvento")
     FROM "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento" CE3
     WHERE CE3."IdUsuario" = :IdUsuario
       AND CE3."IdEstado" = 'VIG'
       AND CE3."IdEmpresa" = :IdEmpresa) AS "AutorizacionesPendientes",
    (SELECT COUNT(CP."IdPortafolio")
     FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
     WHERE CP."IdEmpleadoEjecutivo" = :IdEmpleado
       AND CP."IdEstado" = 'ACT') AS "PortafoliosActivos",
    (SELECT COUNT(CSAR."IdSolicitudAporteRescate")
     FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" CSAR
     WHERE CSAR."IdUsuario" = :IdUsuario
       AND CSAR."EstadoSolicitud" <> 'ANU'
       AND CSAR."IdEmpresa" = :IdEmpresa) AS "OrdenesFfmmVigentes"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CE."IdPersona" = CPDE."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CE."IdPersona" = CPT."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE2 ON CE."IdEmpleadoSupervisor" = CE2."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" CEJ ON CE."IdEmpleado" = CEJ."IdEmpleadoEjecutivo"
WHERE CE."IdEmpleado" = :IdEmpleado
ORDER BY "Direccion" DESC
