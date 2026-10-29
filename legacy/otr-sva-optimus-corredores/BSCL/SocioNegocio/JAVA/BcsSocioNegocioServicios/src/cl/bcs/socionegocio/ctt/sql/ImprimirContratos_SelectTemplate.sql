
SELECT "IdEmpresa", "Binario","TipoBinario","EsEstatico","TipoPresentacion","Descripcion"
FROM "BSCL.Plataforma.BD::CTT.ArchivosContrato"
WHERE "CodigoArchivo" = :CodigoArchivo AND "IdEmpresa" = :IdEmpresa;
