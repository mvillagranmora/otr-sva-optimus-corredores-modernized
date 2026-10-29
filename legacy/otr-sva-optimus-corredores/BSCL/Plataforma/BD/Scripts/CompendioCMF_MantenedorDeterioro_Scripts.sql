
SET SCHEMA "BSCL";

DROP TABLE "BSCL.Plataforma.BD::MER.DeterioroEmisor";

CREATE COLUMN TABLE "BSCL.Plataforma.BD::MER.DeterioroEmisor" ("IdDeterioroEmisor" BIGINT CS_FIXED,
"Emisor" NVARCHAR(50) NOT NULL,
"FlagDeterioro" NVARCHAR(50) NOT NULL,
"PorcentajeDeterioro" DECIMAL(7,4) CS_FIXED NOT NULL,
"Fase" INTEGER CS_INT NOT NULL,
"IdEstado" NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
PRIMARY KEY ("IdDeterioroEmisor")) UNLOAD PRIORITY 5 AUTO MERGE;


---CREACION SECUENCIA


DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::MER.IdDeterioroEmisor"   CASCADE;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::MER.IdDeterioroEmisor"
START WITH 1
INCREMENT BY 1
MINVALUE 1;


-- SERVICIOS

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 55669900, 'BSCL.Plataforma.BD.Vistas::MER.ConsultarDeterioroEmisor', 0, 'Q', 'Consultar Deterioro Emisores', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=55669900);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 55669901, 'PLUGIN_PLA.jar@cl.bcs.plataforma.mer.CrearDeterioroEmisor', 0, 'J', 'Agrega Deterioro Emisor', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=55669901);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 55669902, 'PLUGIN_PLA.jar@cl.bcs.plataforma.mer.ModificarDeterioroEmisor', 0, 'J', 'Modificar Deterioro Emisor', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=55669902);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 55669903, 'PLUGIN_PLA.jar@cl.bcs.plataforma.mer.CargarArchivoDeterioroEmisor', 0, 'J', 'Agrega o modifica Deterioro Emisor', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=55669903);

--CARGA ARCHIVO
--Archivo
INSERT INTO "BSCL.Plataforma.BD::PLA.Archivo"
("IdArchivo", "IdEmpresa", "CodigoArchivo", "TieneSeparador", "SeparadorColumnas", "TieneCabecera", "IdModulo", "CodigoArchivoDetalle", "CodigoArchivoPie")
SELECT 170, 1, 'CARGA_DETERIORO_EMISORES', 'S', ';', 'N', '', '', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Archivo" WHERE "IdArchivo"=170 AND "CodigoArchivo" = 'CARGA_DETERIORO_EMISORES');

-- Formato
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"
("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula", "Ejemplo")
SELECT 4100, 'EMISOR', 'STRING', 1, 10, 'S', '', '', 'N', 0, '', 'AGUAS'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'EMISOR' AND "TipoDato" = 'STRING' AND "Posicion"=1 AND  "LargoCampo" = 10);


INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"
("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula", "Ejemplo")
SELECT 4101, 'FLAGDETERIORO', 'STRING', 2, 1, 'S', '^S$|^N$', 'El Flag de deterioro debe ser S o N', 'N', 0, '', 'S'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'FLAGDETERIORO' AND "TipoDato" = 'STRING' AND "Posicion"=2  AND "LargoCampo" = 1);


INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"
("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula", "Ejemplo")
SELECT 4102, 'PORCENTAJEDETERIORO', 'NUMBER', 3, 6, 'S', '^\d{1,2}(?:\.\d{0,4})?$', 'El Porcentaje debe ser mayor a 0 y menor a 100 con hasta 4 decimales', 'S', 4, '', '20.6'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'PORCENTAJEDETERIORO' AND "TipoDato" = 'NUMBER' AND "Posicion"=3 AND "LargoCampo" = 6);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"
("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula", "Ejemplo")
SELECT 4103, 'FASE', 'NUMBER', 4, 1, 'S', '^[1-3]$', 'La Fase debe debe ser un entero entre 1 y 3', 'N', 0, '', '2'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'FASE' AND "TipoDato" = 'NUMBER' AND "Posicion"=4 AND "LargoCampo" = 1);

-- ArchivoFormato

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato", "IdArchivo", "IdFormato")
SELECT 4100, 170, 4100
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 4100);


INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato", "IdArchivo", "IdFormato")
SELECT 4101, 170, 4101
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 4101);


INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato", "IdArchivo", "IdFormato")
SELECT 4102, 170, 4102
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 4102);


INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato", "IdArchivo", "IdFormato")
SELECT 4103, 170, 4103
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 4103);

--DICCIONARIO

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CARGA_DETERIORO_EMISOR_NO_VALIDO', 'CL', 'El Emisor ::Emisor:: no existe en el sistema', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_CARGA_DETERIORO_EMISOR_NO_VALIDO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_DETERIORO_EMISOR_EXISTE', 'CL', 'Ya se encuentra registrado el Emisor ::Emisor:: en el mantenedor', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_DETERIORO_EMISOR_EXISTE');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_DETERIORO_EMISOR_NO_EXISTE', 'CL', 'No se encuentra registrado el Emisor ::Emisor:: en el mantenedor', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_DETERIORO_EMISOR_NO_EXISTE');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_ELIMINAR_REGISTRO', 'CL', '�Desea eliminar el registro seleccionado?', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_ELIMINAR_REGISTRO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_MANTENEDOR_DETERIORO_EMISOR', 'CL', 'Mantenedor Deterioro Emisores', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_MANTENEDOR_DETERIORO_EMISOR');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CARGAR_DETERIORO_EMISOR', 'CL', 'Carga Deterioro Emisores', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_CARGAR_DETERIORO_EMISOR');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_EMISOR', 'CL', 'Emisor', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_EMISOR');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_PORCENTAJE', 'CL', 'Porcentaje', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_PORCENTAJE');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_FLAG', 'CL', 'Flag', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_FLAG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_FASE', 'CL', 'Fase', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_FASE');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_VALIDACION_PORCENTAJE_DETERIORO', 'CL', 'Porcentaje debe ser mayor a 0 y menor a 100, con hasta 4 decimales', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_VALIDACION_PORCENTAJE_DETERIORO');


INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_VALIDACION_FASE_DETERIORO', 'CL', 'Fase debe ser un entero entre 1 y 3', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_VALIDACION_FASE_DETERIORO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_VALIDACION_FLAG_DETERIORO', 'CL', 'Flag debe ser S o N', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_VALIDACION_FLAG_DETERIORO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CREAR_DETERIORO_EMISOR_EXITO', 'CL', 'Deterioro emisor creado exitosamente', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_CREAR_DETERIORO_EMISOR_EXITO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_MODIFICAR_DETERIORO_EMISOR_EXITO', 'CL', 'Deterioro emisor modificado exitosamente', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_MODIFICAR_DETERIORO_EMISOR_EXITO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CARGAR_ARCHIVO_DETERIORO_EMISOR_EXITO', 'CL', 'Archivo cargado exitosamente', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_CARGAR_ARCHIVO_DETERIORO_EMISOR_EXITO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_DETERIORO_EMISOR_SIN_REGISTROS', 'CL', 'No hay registros a procesar', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_DETERIORO_EMISOR_SIN_REGISTROS');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CARGA_DETERIORO_EMISOR_REPETIDO', 'CL', 'El archivo de carga contiene emisores repetidos', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_CARGA_DETERIORO_EMISOR_REPETIDO');



INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_PORCENTAJE_DETERIORO', 'CL', 'Porcentaje Deterioro', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_PORCENTAJE_DETERIORO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_DETERIORO', 'CL', 'Deterioro', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_DETERIORO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_FLAG_DETERIORO', 'CL', 'Flag Deterioro', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_FLAG_DETERIORO');

COMMIT;














