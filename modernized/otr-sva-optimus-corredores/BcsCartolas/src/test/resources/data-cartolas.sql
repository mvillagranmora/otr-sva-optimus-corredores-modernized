-- Datos de prueba para BcsCartolas

-- Configuraciones empresa 1
INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (1, 1, 'Tamaño lote carga', 'INT', 'CallerBatchSize',           '100');
INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (2, 1, 'Tamaño lote inserción', 'INT', 'InsertBatchSize',       '1000');
INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (3, 1, 'Ruta raíz archivos', 'TEXTO', 'RutaRaizArchivos',       '/data/cartolas');
INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (4, 1, 'Formato nombre cartola', 'TEXTO', 'FormatoNombreArchivoCartola', '{DocumentoCliente}_{YYYYMMDD}');

-- Configuración empresa 2
INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (5, 2, 'Ruta raíz archivos empresa 2', 'TEXTO', 'RutaRaizArchivos', '/data/empresa2');

-- Nóminas
INSERT INTO car_nomina (id_nomina, id_empresa, id_usuario, id_plantilla, periodo,
                        tipo_proceso, estado_proceso, certificada)
VALUES (10, 1, 1, 1, '2024-03', 'NORMAL', 'INGRESADO', 'NO');

INSERT INTO car_nomina (id_nomina, id_empresa, id_usuario, id_plantilla, periodo,
                        tipo_proceso, estado_proceso, certificada)
VALUES (20, 1, 1, 1, '2024-03', 'PROGRAMADO', 'INGRESADO', 'NO');

-- Parámetros nómina 10
INSERT INTO car_parametro (id_parametro, id_nomina, nombre, valor)
VALUES (1, 10, 'Habilitado', 'true');
INSERT INTO car_parametro (id_parametro, id_nomina, nombre, valor)
VALUES (2, 10, 'DiaEjecucion', '5');
INSERT INTO car_parametro (id_parametro, id_nomina, nombre, valor)
VALUES (3, 10, 'HoraEjecucion', '08:00');

-- Cartolas nómina 10
INSERT INTO car_cartola (id_cartola, id_nomina, id_cliente, id_portafolio,
                         reprocesar, notificar, estado_proceso)
VALUES (100, 10, 1001, 2001, 'NO', 'NO', 'INGRESADO');
INSERT INTO car_cartola (id_cartola, id_nomina, id_cliente, id_portafolio,
                         reprocesar, notificar, estado_proceso)
VALUES (101, 10, 1002, 2002, 'SI', 'NO', 'ERROR');

-- Datos cartola 100
INSERT INTO car_datos (id_datos, id_cartola, nombre, formato, valor)
VALUES (1, 100, 'DatosPortafolio', 'csv', 'Col1,Col2\nval1,val2');

-- Trazas nómina 10
INSERT INTO car_traza (id_traza, id_nomina, mensaje)
VALUES (1, 10, 'Proceso iniciado');

-- Plantilla empresa 1
INSERT INTO car_plantilla (id_plantilla, id_empresa, nombre, vigente_desde, vigente)
VALUES (1, 1, 'CartolaMensual', '2024-01-01', 'SI');

-- Recursos plantilla 1
INSERT INTO car_recurso (id_recurso, id_plantilla, editable, grupo, nombre, tipo, valor)
VALUES (1, 1, 'NO', 'Reporte', 'CartolaMensual', 'jasper', 'BASE64_JASPER_PLACEHOLDER');
INSERT INTO car_recurso (id_recurso, id_plantilla, editable, grupo, nombre, tipo, valor)
VALUES (2, 1, 'SI', 'Config', 'MostrarGraficos', 'OPCION', 'SI');
INSERT INTO car_recurso (id_recurso, id_plantilla, editable, grupo, nombre, tipo, valor)
VALUES (3, 1, 'NO', 'Config', 'MaxRegistros', 'NUMERO', '100');

-- Clientes
INSERT INTO vw_cliente (id_cliente, razon_social, nombres, apellido_paterno, apellido_materno,
                        documento_identidad, email)
VALUES (1001, '', 'Juan', 'Pérez', 'González', '12345678-9', 'juan.perez@example.com');
INSERT INTO vw_cliente (id_cliente, razon_social, nombres, apellido_paterno, apellido_materno,
                        documento_identidad, email)
VALUES (1002, 'Empresa ABC S.A.', '', '', '', '76543210-K', 'contacto@abc.cl');

-- Portafolios
INSERT INTO por_portafolio (id_portafolio, codigo_portafolio)
VALUES (2001, 'POR-2001-A');
INSERT INTO por_portafolio (id_portafolio, codigo_portafolio)
VALUES (2002, 'POR-2002-B');
