
--- ACTUALIZA FACTOR POR SII AT2023


DELETE FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "CodigoParametro"  IN ('P23');

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
VALUES(1, 'P23', 'Factor maximo actualizado de Enero a Diciembre AT-1', '1.125', 'DJ1889', 'N', 'Validacion DJ1889', '1', 'Numerico, (10000)', 'N', '', 'SISTEMA', '2020-05-03 12:00:00.000', 'CFLORES', '2023-01-25 23:05:04.000', ' ');
