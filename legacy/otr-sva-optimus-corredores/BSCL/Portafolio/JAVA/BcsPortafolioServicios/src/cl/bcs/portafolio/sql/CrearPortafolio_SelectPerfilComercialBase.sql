SELECT 
	PCOM."IdPerfilProductos",
    PCOM."IdPerfilCobros",
    PCOM."IdPerfilCobrosPeru",
    PCOM."IdPerfilLimites",
    PCOM."IdPerfilCondicionesOperativas"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase" PCOM
WHERE 
    "IdPerfilComercialBase" = :p_IdPerfilComercialBase;