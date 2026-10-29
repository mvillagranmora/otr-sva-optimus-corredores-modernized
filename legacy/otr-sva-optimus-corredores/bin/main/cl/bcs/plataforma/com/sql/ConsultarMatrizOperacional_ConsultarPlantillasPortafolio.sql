SELECT "IdPortafolio","IdPerfilCobros", "IdPerfilCobrosExtra", "IdPerfilProductos","IdPerfilProductosExtra","IdPerfilCobrosPeru","IdPerfilCobrosPeruExtra"
FROM "BSCL.Portafolio.BD::POR.Portafolio" 
WHERE "IdPortafolio" = CAST(:IdEntidad as BIGINT);
