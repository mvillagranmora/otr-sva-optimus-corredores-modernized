
/****** Eliminacion de Esquemas de las DJs SII ********/

SET SCHEMA "BSCL";

--SELECT * FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII'

DELETE 
FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

DELETE
FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

DELETE 
FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

DELETE 
FROM "BSCL"."BSCL.Normativa.BD::DOC.Clasificador" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

DELETE  
FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

DELETE  
FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

DELETE  
FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');


DELETE 
FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

DELETE  
FROM "BSCL"."BSCL.Normativa.BD::DOC.Parametro" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

DELETE  
FROM "BSCL"."BSCL.Normativa.BD::TRI.Complemento" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');

/* tablas propias de la declaracion*/
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1820";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1823";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1829";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1839";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1848";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1849";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1862";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1870";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1871";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1885";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1889";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1890";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1891";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1894";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1899";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1914";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1919";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1922";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DatosDJ1932";

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36";
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43";


DELETE FROM BSCL."BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoDocumento"='DECLACORR';

/*tablas iniciales*/

DELETE 
FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" D
WHERE D."Esquema" IN (SELECT "Esquema" FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII');



DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "IdEntidadReguladora" ='SII';
