SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='N' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" IN ('DJ1862','DJ1871','DJ1874','DJ1894','DJ1899','DJ1914');

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "TipoDiccionario"='CodigoPais1862' WHERE "Version" IN ('2024','2025','2026') AND "Esquema"='DJ1862' AND "Nombre"='CodigoPaisContraparte';

UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S',"Editable"='N' WHERE "Esquema"='DJ1862' AND "Version" IN ('2024','2025','2026') AND "Grupo" = '4. DATOS DE CONTROL';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='S',"Editable"='S' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" = ('DJ1862') 
AND "Nombre" IN ( 'CodigoModalidadOperacion','MontoEnPesos','CodigoConcepto','FechaOperacion','CodigoMoneda','PaisOtorganteIdentificador','CodigoPaisContraparte');


UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S',"Editable"='N' WHERE "Esquema"='DJ1871' AND "Version" IN ('2024','2025','2026') AND "Grupo" = '4. DATOS DE CONTROL';

UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S',"Editable"='N' WHERE "Esquema"='DJ1871' AND "Version" IN ('2024','2025','2026') AND "Nombre"='IdCliente';


UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='S',"Editable"='S' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" = ('DJ1871') 
AND "Nombre" IN ('AnoAhorro','RutCliente','FechaOperacion');


UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S',"Editable"='N' WHERE "Esquema"='DJ1874' AND "Version" IN ('2024','2025','2026') AND "Grupo" = '2. DATOS DE CONTROL';

UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S',"Editable"='N' 
WHERE "Esquema"='DJ1874' AND "Version" IN ('2024','2025','2026') AND "Grupo" IN ('3. DATOS VALORIZACION','4. RESULTADO VALORIZACION') AND "Nombre" != 'Nemotecnico';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Editable"='N' WHERE "Grupo"='2. DATOS DE CONTROL' 
AND "Version" IN ('2024','2025','2026') AND "Esquema" IN ('DJ1874');

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Editable"='S' WHERE "Grupo"!='2. DATOS DE CONTROL' 
AND "Version" IN ('2024','2025','2026') AND "Esquema" IN ('DJ1874');


UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='S',"Editable"='S' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" = ('DJ1874') 
AND "Nombre" IN ('RutEmisor','RutInversionista','TipoContribuyente','FechaOperacion','MonedaUnidadReajuste','NumeroCuentaDCV','NemotecnicoSVS');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1874TIPOCONTRIBUYENTE', '1', 0, 'Domiciliado en Chile', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1874TIPOCONTRIBUYENTE'
      AND "Codigo" = '1'
);
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1874TIPOCONTRIBUYENTE', '2', 0, 'Domiciliado en el extranjero', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1874TIPOCONTRIBUYENTE'
      AND "Codigo" = '2'
);

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "TipoDiccionario"='DJ1874TIPOCONTRIBUYENTE' WHERE "Version" IN ('2024','2025','2026') AND "Esquema"='DJ1874' AND "Nombre"='TipoContribuyente';



UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S',"Editable"='N' WHERE "Esquema"='DJ1899' AND "Version" IN ('2024','2025','2026') AND "Grupo" = '8. DATOS DE CONTROL';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='S',"Editable"='S' WHERE "Esquema"='DJ1899' AND "Version" IN ('2024','2025','2026') AND "Nombre" IN ('RutCliente','ActivoPensionado','FechaOperacion');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1899ACTIVOPENSIONADO', '1', 0, 'Activo', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1899ACTIVOPENSIONADO'
      AND "Codigo" = '1'
);

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1899ACTIVOPENSIONADO', '2', 0, 'Pensionado', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1899ACTIVOPENSIONADO'
      AND "Codigo" = '2'
);

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "TipoDiccionario"='DJ1899ACTIVOPENSIONADO' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" IN ('DJ1899') AND "Nombre"='ActivoPensionado';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S' WHERE "Nombre"='IdCliente' AND "Version" IN ('2024','2025','2026') AND "Esquema" IN ('DJ1899');

SELECT * FROM "BSCL.Plataforma.BD::PLA.Empresa"

SELECT "Esquema", "Version", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "Modulo", "HabilitadoMantenedor", "TieneCertificado"
FROM BSCL."BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema" LIKE '%REPO%';


SELECT * FROM "BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo" LIKE '%REP%'

UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S',"Editable"='N' WHERE "Esquema"='DJ1914' AND "Version" IN ('2024','2025','2026') AND "Grupo" = '5. DATOS DE CONTROL';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='S',"Editable"='S' WHERE "Esquema"='DJ1914' AND "Version" IN ('2024','2025','2026') AND 
"Nombre" IN ('RutCliente','TipoInstrumento','NumeroIdentificadorInversion','RetiroInversion54BISMontoNominal');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1914TIPOINSTRUMENTO', '1', 0, 'Deposito a Plazo', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1914TIPOINSTRUMENTO'
      AND "Codigo" = '1'
);

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1914TIPOINSTRUMENTO', '2', 0, 'Fondos Mutuos', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1914TIPOINSTRUMENTO'
      AND "Codigo" = '2'
);

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1914TIPOINSTRUMENTO', '3', 0, 'Cuentas de Ahorro', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1914TIPOINSTRUMENTO'
      AND "Codigo" = '3'
);

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1914TIPOINSTRUMENTO', '4', 0, 'Cuentas de Ahorro Voluntario', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1914TIPOINSTRUMENTO'
      AND "Codigo" = '4'
);

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1914TIPOINSTRUMENTO', '5', 0, 'Otros Instrumentos', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1914TIPOINSTRUMENTO'
      AND "Codigo" = '5'
);

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "TipoDiccionario"='DJ1914TIPOINSTRUMENTO' WHERE "Version" IN ('2024','2025','2026') 
AND "Esquema" IN ('DJ1914') AND "Nombre"='TipoInstrumento';




UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S',"Editable"='N' WHERE "Esquema"='DJ1894' AND "Version" IN ('2024','2025','2026') AND "Grupo" = '5. DATOS DE CONTROL';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='S',"Editable"='S' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" = 'DJ1894' AND "Nombre" 
IN ('RutAdministradoraFondoReceptor','RutEmisor','RutFondo','RunFondo','TipoOperacion','TipoDeclarante');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1894TIPODECLARANTE', '1', 0, 'Administradora de Fondos Mutuos', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1894TIPODECLARANTE'
      AND "Codigo" = '1'
);

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'DJ1894TIPODECLARANTE', '2', 0, 'Institucion Intermediaria', 'S', '', ''
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::DOC.Diccionario"
    WHERE "Tipo" = 'DJ1894TIPODECLARANTE'
      AND "Codigo" = '2'
);

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "TipoDiccionario"='DJ1894TIPODECLARANTE' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" IN ('DJ1894') AND "Nombre" = 'TipoDeclarante';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "TipoDiccionario"='TipoOperacionDJ1894' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" IN ('DJ1894') AND "Nombre" = 'TipoOperacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Editable"='S' WHERE "Version" IN ('2024','2025','2026') AND "Esquema" LIKE '%DJ1894%'

UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "Grupo"='1. DATOS DE LOS INFORMADOS',"OcultoEnMantenedor"='N',"Obligatorio"='S',"Editable"='S' 
WHERE "Esquema"='DJ1894' AND "Version"='2024' AND "Nombre"='FechaOperacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Editable"='S' WHERE "Grupo"!='2. DATOS DE CONTROL' 
AND "Version" IN ('2024','2025','2026') AND "Esquema" IN ('DJ1874');

UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "Grupo"='1. DATOS DE LOS INFORMADOS',"OcultoEnMantenedor"='N',"Obligatorio"='S',"Editable"='S' 
WHERE "Esquema"='DJ1874' AND "Version" IN ('2024','2025','2026') AND "Nombre"='FechaAdquisicion';



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
    ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT","RegistraUso","ValidaAccesoServicio")
SELECT
    (SELECT IFNULL(MAX("IdServicio"), 0) + 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio") AS "IdServicio",
    'BSCL.Normativa.BD.Servicios::TRI.DJ1946.GrabarDetalle',
    0,
    'P',
    '',
    '',
    '',
    'S',
    'N'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio"
    WHERE "RutaServicio" = 'BSCL.Normativa.BD.Servicios::TRI.DJ1946.GrabarDetalle'
);



COMMIT;