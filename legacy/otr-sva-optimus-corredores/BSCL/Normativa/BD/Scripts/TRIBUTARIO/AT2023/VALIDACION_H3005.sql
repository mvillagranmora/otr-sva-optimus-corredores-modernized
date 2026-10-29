SET SCHEMA "BSCL";

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1891', '2022', 'H3005', 'NORMA', 'S', 'Si el Monto de Adquisicion es mayor a 0. Entonces el tipo de Transaccion debe ser Acciones y el Monto de Venta debe ser mayor a 0', 'E', 'Si montoAdquisicion > 0, entonces "01".equals(tipoTransaccion) && montoVenta > 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1891', '2023', 'H3005', 'NORMA', 'S', 'Si el Monto de Adquisicion es mayor a 0. Entonces el tipo de Transaccion debe ser Acciones y el Monto de Venta debe ser mayor a 0', 'E', 'Si montoAdquisicion > 0, entonces "01".equals(tipoTransaccion) && montoVenta > 0');