SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='Numero';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='Observaciones';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='FuenteDatos';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='VboUsuario';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='RutInstitucion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"=''
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='ID';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='IdDocumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE_FILTER', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='FechaOperacion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='TipoCartera';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='EjecutadoComplementoIPC';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID_CLIENTE', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"=''
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='IdCliente';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='RUT_CLIENTE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='RutCliente';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INVERSIONES EFECTUADAS CON ANTERIORIDAD AL 01.08.98'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='SalAhorroNetoEjerNEGT1';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INVERSIONES EFECTUADAS CON ANTERIORIDAD AL 01.08.98'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='SalArrastreEjerSIGT1';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. INVERSIONES EFECTUADAS A CONTAR DEL 01.08.98 HASTA EL 31.12.2014'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='SalArrastreEjerSigNegT2';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO RETIRO INV. 7 BIS HASTA 31.12.2014'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='MtoRetiro57BisT2';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='MtoRenRetFFMMPOS';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='NumeroCertificado';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. INVERSIONES EFECTUADAS A CONTAR DEL 01.08.98 HASTA EL 31.12.2014'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='SalAhorroNetoEjerT2';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='SalAhorroNetoEjeNegT3';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='SalArrastreEjeSigNegT3';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='MtoRetiroAnoT3';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='MtoRenRetFFMMNEG';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='MtoRenRetOTROSPOS';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016'
WHERE "Esquema"='DJ1944' AND "Version"='2024' AND "Nombre"='MtoRenRetOTROSNEG';

COMMIT;