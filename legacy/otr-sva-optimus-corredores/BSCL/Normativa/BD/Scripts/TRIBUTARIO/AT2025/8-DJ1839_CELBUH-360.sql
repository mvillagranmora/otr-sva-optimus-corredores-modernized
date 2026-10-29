SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='4. Control'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='VboUsuario';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='4. Control'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='Observaciones';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='4. Control'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='FuenteDatos';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE_FILTER', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. Acuerdo marco'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='AcuerdoMarcoFechaSuscripcion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='TaxID';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='TaxIDB';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='RUT_CLIENTE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='RutCliente';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE_FILTER', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. Contrato / Confirmacion'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='ContratoFechaSuscripcion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='RUT_CLIENTE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='RutClienteB';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. Acuerdo marco'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='AcuerdoMarcoTipo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. Acuerdo marco'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='AcuerdoMarcoNumero';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. Contrato / Confirmacion'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='ContratoNumero';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. Contrato / Confirmacion'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='ContratoTipo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. Contrato / Confirmacion'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='ContratoNombreInstrumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='4. Control'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='IdDocumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID_CLIENTE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='IdCliente';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID_PORTAFOLIO', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='IdPortafolio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='CodigoPaisA';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='CodigoPaisB';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Datos principales'
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='Numero';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"=''
WHERE "Esquema"='DJ1839' AND "Version"='2024' AND "Nombre"='ID';

COMMIT;