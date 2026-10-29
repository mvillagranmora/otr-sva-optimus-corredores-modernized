SET SCHEMA "BSCL";

UPSERT "BSCL.Normativa.BD::NOR.Configuracion"
VALUES(1, 'EXCLUIR_INSTRUMENTOS', 'Parámetro para excluir instrumentos en Circular NCG 499', 'DINERO;', '', 'S', 'Circulares', '', 'NEMO0;NEMO1', 'S', '', 'BUHO', '2024-02-22 16:00:00.000', '', '2024-02-22 16:00:00.000', '')
WHERE "IdEmpresa"=1 AND "CodigoParametro"='EXCLUIR_INSTRUMENTOS';

COMMIT;