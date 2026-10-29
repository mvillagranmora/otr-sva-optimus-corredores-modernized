SET SCHEMA
    "BSCL";

GO
    DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarApoderadosCorredora";

GO
    CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarApoderadosCorredora" AS
SELECT
    EMP."IdEmpresa",
    COALESCE(CR."NombreCompletoDestino", '-') AS "NombreCompleto",
    COALESCE(CR."DocumentoIdentidadDestino", '-') AS "DocumentoIdentidad",
    COALESCE(DIR."Calle", '-') || ' ' || COALESCE(DIR."Numero", '-') || ' ' || COALESCE(DIR."Departamento", '-') AS "Direccion",
    COALESCE(TEL."Numero", '-') || ' ' || COALESCE(TEL."Anexo", '-') AS "Numero",
    COALESCE(DIR."NombreCiudad", '-') AS "Ciudad"
FROM
    "BSCL.Plataforma.BD::PLA.Empresa" EMP
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" CR ON SOC."IdPersona" = CR."IdPersonaOrigen"
    AND CR."IdTipoRelacion" = 'APO'
    AND CR."IdEstado" = 'ACT'
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON SOC."IdPersona" = PER."IdPersona"
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL ON PER."IdTelefonoPrincipal" = TEL."IdTelefono"
    AND PER."IdPersona" = TEL."IdPersona" 
ORDER BY
    "FechaInicioRelacion" DESC,
    "IdRelacionPersona" ASC;

