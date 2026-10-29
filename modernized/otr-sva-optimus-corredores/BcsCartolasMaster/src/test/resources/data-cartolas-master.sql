-- Datos de prueba para BcsCartolasMaster (H2 in-memory)

-- Configuración empresa 1
INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (1, 1, 'Thread pool para generación batch', 'PROCESO', 'threadPoolSize', '4');

INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (2, 1, 'Ruta raíz de archivos de salida', 'PROCESO', 'rutaRaizArchivos', '/tmp/cartolas');

INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (3, 1, 'Incluir cartera RV', 'REPORTE', 'PRV_incCartera', 'S');

INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (4, 1, 'Incluir cartera RF', 'REPORTE', 'IRF_incCartera', 'S');

INSERT INTO car_config (id_config, id_empresa, descripcion, tipo, nombre, valor)
VALUES (5, 2, 'Thread pool empresa 2', 'PROCESO', 'threadPoolSize', '2');

-- Parámetros nómina 100
INSERT INTO car_parametro (id_parametro, id_nomina, nombre, valor)
VALUES (1, 100, 'PRV_incCartera', 'true');

INSERT INTO car_parametro (id_parametro, id_nomina, nombre, valor)
VALUES (2, 100, 'IRF_incCartera', 'true');

INSERT INTO car_parametro (id_parametro, id_nomina, nombre, valor)
VALUES (3, 100, 'FM_incCartera', 'false');

INSERT INTO car_parametro (id_parametro, id_nomina, nombre, valor)
VALUES (4, 100, 'FORMATO_PAGINACION', '{0}/{1}');

-- Parámetros nómina 200
INSERT INTO car_parametro (id_parametro, id_nomina, nombre, valor)
VALUES (5, 200, 'PRV_incCartera', 'false');
