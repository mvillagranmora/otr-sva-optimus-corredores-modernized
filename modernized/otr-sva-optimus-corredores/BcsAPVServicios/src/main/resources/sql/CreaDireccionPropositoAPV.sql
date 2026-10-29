INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionFisica"
(SELECT "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica".NEXTVAL,
        "Calle",
        "Numero", 
        "Departamento", 
        "Sector",
        "CodigoPostal", 
        "IdCiudad",
        'PREV' AS "CodigoProposito", 
        "Comentario", 
        "IdEstado", 
        "FechaIngreso",
        "FechaCreacion", 
        "FechaModificacion", 
        "IdPersona",
        "IdEstadoAutorizacion", 
        "IdCarga", 
        "IdOrigen", 
        "Borrado", 
        "IdUsuarioCreador", 
        "IdDivisionPais", 
        "Predeterminado", 
        "CodigoCompuesto"
    FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
        WHERE "IdDireccionFisica" = :IdDireccionFisica
        AND "IdEstado" = 'ACT')
