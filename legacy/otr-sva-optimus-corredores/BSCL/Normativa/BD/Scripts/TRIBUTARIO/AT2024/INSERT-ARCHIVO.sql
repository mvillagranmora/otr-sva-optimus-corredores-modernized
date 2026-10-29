SET SCHEMA "BSCL";

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Archivo"
("IdArchivo", "IdEmpresa", "CodigoArchivo", "TieneSeparador", "SeparadorColumnas", "TieneCabecera", "IdModulo", "CodigoArchivoDetalle", "CodigoArchivoPie")
SELECT 99, 1, 'ARCHIVO_CALIFICACION_TRIBUTARIA', 'S', ';', 'N', '', '', ''
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "IdArchivo"=99 
AND "CodigoArchivo"= 'ARCHIVO_CALIFICACION_TRIBUTARIA');

COMMIT;
