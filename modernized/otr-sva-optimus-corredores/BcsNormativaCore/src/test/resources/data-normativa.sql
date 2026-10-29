-- Test fixtures for BcsNormativaCore

-- Esquema
INSERT INTO ESQUEMA (Esquema, Version, Modulo, Periodicidad, Objetivo, CodigoVersionSII)
VALUES ('DJ1937', 'v1', 'TRI', 'MENSUAL', 'Declaracion jurada de ingresos', 1937);

-- Campos del esquema DJ1937-v1
INSERT INTO CAMPO (Esquema, Version, Nombre, Titulo, TipoDato, Obligatorio, Comun, Clave, Exportable, Orden, OrdenCargaExterna)
VALUES ('DJ1937', 'v1', 'Periodo', 'Periodo', 'DATE_FILTER', 'S', 'S', 'N', 'S', 1, 1);

INSERT INTO CAMPO (Esquema, Version, Nombre, Titulo, TipoDato, Obligatorio, Comun, Clave, Exportable, Orden, OrdenCargaExterna)
VALUES ('DJ1937', 'v1', 'RutCliente', 'RUT Cliente', 'RUT_CLIENTE', 'S', 'N', 'S', 'S', 2, 2);

INSERT INTO CAMPO (Esquema, Version, Nombre, Titulo, TipoDato, Obligatorio, Comun, Clave, Exportable, Orden, OrdenCargaExterna)
VALUES ('DJ1937', 'v1', 'Monto', 'Monto', 'AMOUNT', 'S', 'N', 'N', 'S', 3, 3);

-- Documento
INSERT INTO DOCUMENTO (IdDocumento, IdTipoDocumento, Esquema, Version, IdEmpresa,
    IdEstadoDocumento, FechaCreacion, EnProceso)
VALUES (100, 'DJ', 'DJ1937', 'v1', 1001, 'BOR', CURRENT_DATE, 'N');

-- Validaciones
INSERT INTO VALIDACION (Esquema, Version, Codigo, Tipo, Activa, Descripcion, Severidad, Formula)
VALUES ('DJ1937', 'v1', 'V001', 'CAMPO', 'S', 'Monto mayor que cero', 'ERROR', 'Monto > 0');

INSERT INTO VALIDACION (Esquema, Version, Codigo, Tipo, Activa, Descripcion, Severidad, Formula)
VALUES ('DJ1937', 'v1', 'V002', 'CAMPO', 'N', 'Validacion inactiva', 'ADVERTENCIA', 'true');

-- Empresa
INSERT INTO EMPRESA (IdEmpresa, Nombre, RazonSocial, Rut, Codigo, EsCorredora)
VALUES (1001, 'Corredora Test SA', 'Corredora Test Sociedad Anonima', '12345678-9', 'CT001', 'S');

-- Corredora empresa
INSERT INTO CORREDORA_EMPRESA (IdEmpresa, IdBolsa, CodigoCorredora)
VALUES (1001, 'BCS', 'CT-BCS-001');

-- Configuracion
INSERT INTO CONFIGURACION (IdEmpresa, CodigoParametro, Valor)
VALUES (1001, 'RUTA_ARCHIVOS', '/data/normativa/dj1937');

-- Parametro
INSERT INTO PARAMETRO (Esquema, Version, Codigo, Valor)
VALUES ('DJ1937', 'v1', 'FORMATO_FECHA', 'dd/MM/yyyy');

-- Cuadratura
INSERT INTO CUADRATURA (Esquema, Version, Tipo, Descripcion)
VALUES ('DJ1937', 'v1', 'RENTA', 'Cuadratura de rentas declaradas');

-- Campos cuadratura
INSERT INTO CAMPO_CUADRATURA (Esquema, Version, NombreCuadratura, NombreCampo, Orden)
VALUES ('DJ1937', 'v1', 'RENTA', 'Monto', 1);

-- Clasificador
INSERT INTO CLASIFICADOR (Esquema, Version, Tipo, Descripcion)
VALUES ('DJ1937', 'v1', 'TIPO_RENTA', 'Tipo de renta declarada');
