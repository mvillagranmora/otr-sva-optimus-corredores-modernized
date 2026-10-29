SET SCHEMA BSCL;

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='Numero';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='5. DATOS DE CONTROL'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='Observaciones';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='5. DATOS DE CONTROL'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='VboUsuario';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='5. DATOS DE CONTROL'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='FuenteDatos';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='RUT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='TaxID';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='RUT_CLIENTE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='RutCliente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DATE_FILTER', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='FechaOperacion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='RUT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='RutEmisor';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='NemotecnicoSVS';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='NumeroCuentaDCV';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='5. DATOS DE CONTROL'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='IdDocumento';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='1. Transacciones informadas'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='IdOperacion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID_CLIENTE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='IdCliente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID_PORTAFOLIO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='IdPortafolio';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"=''
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='ID';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='PorcentajePropiedadDirectaIndirecta';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='ParticipacionDirectaIndirectaControl';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='ValorAdquisicionEnPesos';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='ValorEnajenacion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='MayorValorEnajenacion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='MayorValorEnajenacionEnPesos';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='Moneda';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='ValorEmision';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='ValorColocacion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='ValorAdquisicion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='ValorEnajenacionEnPesos';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='CodigoPaisConstitucion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos de las Operaciones'
WHERE "Esquema"='DJ1919' AND "Version"='2024' AND "Nombre"='CodigoPaisResidencia';

COMMIT;