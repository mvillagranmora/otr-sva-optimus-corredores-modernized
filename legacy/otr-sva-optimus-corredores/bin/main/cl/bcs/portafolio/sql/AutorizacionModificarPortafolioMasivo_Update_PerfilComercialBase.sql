UPDATE DOC
SET
    DOC."IdPerfilComercialBaseOrigen" = :ValorNuevo,
    DOC."IdPerfilProductos" = PCOM."IdPerfilProductos",
    DOC."FechaModificacion" = :FechaModificacion
FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
    INNER JOIN "BSCL.Plataforma.BD::COM.PerfilComercialBase" PCOM ON PCOM."IdPerfilComercialBase" = :ValorNuevo
WHERE DOC."IdPortafolio" = :IdPortafolio
