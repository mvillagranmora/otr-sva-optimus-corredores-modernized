

--SELECT * FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "CodigoParametro" IN ('CodigoEmpresaDJInternet','CodigoEmpresaDJMagnetico')

--- ACTUALIZA CODIGO VERSION ENVIADO POR SII AT2022
UPDATE BSCL."BSCL.Normativa.BD::DOC.Esquema" SET  "CodigoVersionSII"=22
WHERE "IdEntidadReguladora"='SII' AND LEFT("Version",4)='2021';

--- ACTUALIZA CODIGO DE EMPRESA ENVIADO POR SII

DELETE FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "CodigoParametro" IN ('CodigoEmpresaDJInternet','CodigoEmpresaDJMagnetico');

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1, 'CodigoEmpresaDJInternet', 'Codigo Empresa para DJ por Internet', '36', '', 'N', 'Declaraciones Juradas', '36', '​Numerico, (91/36) ', 'N', ' ', '', CURRENT_UTCTIMESTAMP, ' ', CURRENT_UTCTIMESTAMP, ' '
FROM  DUMMY;

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1, 'CodigoEmpresaDJMagnetico', 'Codigo Empresa para DJ por medio Magnetico', '80', '', 'N', 'Declaraciones Juradas', '80', '​Numerico, (91/36)', 'N', ' ', '', CURRENT_UTCTIMESTAMP, ' ', CURRENT_UTCTIMESTAMP, ' '
FROM  DUMMY;
