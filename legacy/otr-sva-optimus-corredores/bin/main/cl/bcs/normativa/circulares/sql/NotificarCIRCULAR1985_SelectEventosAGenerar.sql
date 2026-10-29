SELECT 
     "CC"."IdPersona"
    ,COALESCE("CPDE"."IdDireccionElectronica",0) AS "IdDireccionElectronica"
    ,"CEO"."Secuencia"
    ,"CEO"."IdPortafolio"
    ,"CORREDORA"."RazonSocial"
    ,"CC"."NombreCompleto"
    ,"CC"."DocumentoIdentidad"
    ,TO_VARCHAR(TO_DATE(:p_FechaSistema), 'DD-MM-YYYY') AS "FechaEjecucion"
    ,COALESCE("POR"."CodigoPortafolio",'') AS "CodigoPortafolio"
    ,COALESCE("CPDF"."NombreLargo",'-') AS "DireccionFisica"
    ,EMP."NombreCompleto" AS "NombreAgente"
    ,TERE."Numero" || '  ' || TERE."Anexo" AS "TelefonoAgente"
FROM (
    SELECT
         "CEO"."IdPortafolio"
        ,"CEO"."IdCliente"
        ,MAX("CEO"."IdOrden") AS "Secuencia"
        ,"CEO"."IdEmpresa"
    FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" AS "CEO"
    WHERE TO_DATE(COALESCE("CEO"."FechaReferencia", "CEO"."FechaEjecucion")) = TO_DATE(:p_FechaSistema)
    GROUP BY "CEO"."IdPortafolio", "CEO"."IdCliente", "CEO"."IdEmpresa"
) AS "CEO"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CC"
    ON "CC"."IdCliente" = "CEO"."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS "CPDE"
    ON "CPDE"."IdDireccionElectronica" = "CC"."IdDireccionElectronicaPrincipal"
    AND "CPDE"."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales" AS "CORREDORA"
    ON "CORREDORA"."IdEmpresa" = "CEO"."IdEmpresa"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "POR"
    ON "CEO"."IdPortafolio" = "POR"."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "CPDF"
    ON "CPDF"."IdDireccionFisica" = "CC"."IdDireccionFisicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP
    ON "CC"."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TERE
    ON EMP."IdTelefonoPrincipal" = TERE."IdTelefono"