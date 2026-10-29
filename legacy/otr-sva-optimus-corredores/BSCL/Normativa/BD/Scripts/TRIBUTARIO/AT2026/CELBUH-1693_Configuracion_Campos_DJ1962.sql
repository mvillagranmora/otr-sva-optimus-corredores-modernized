SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "TipoDato"='DECIMAL', "Largo"=24, "CantidadDecimales"=6 
WHERE "Esquema"='DJ1962' AND "Version" IN ('2025','2026') AND "Nombre"='CantidadCuotasParticipe';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Clave"='S' WHERE "Esquema"='DJ1962' AND "Version" IN ('2025','2026')AND "Nombre"='FolioCartera';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Clave"='N' WHERE "Esquema"='DJ1962' AND "Version" IN ('2025','2026')AND "Nombre"='RutFondoC';

COMMIT;

