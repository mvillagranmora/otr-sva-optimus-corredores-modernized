SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='Observaciones';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='Nemo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE_FILTER', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PeriodoReporte';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='Nombres';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='Apellidos';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='FechaNacimiento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='FechaNacimientoControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. SALDO DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='SaldoActualizado';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DECIMAL', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. PAGOS A LA CUENTA FINANCIERA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='MontoPagoActualizado';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='NombreControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='ApellidosControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='IdDocumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='FuenteDatos';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"=''
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='ID';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='RazonSocial';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TaxID';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='Domicilio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TaxIDControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='DomicilioControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='VboUsuario';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='IdCliente';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='IdTipoInstrumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PaisTaxID';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='NumeroCuenta';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. PAGOS A LA CUENTA FINANCIERA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='DivisaPagoCuentaFinanciera';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='IdClasificacionTipoInstrumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PaisResidencia';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TipoPersona';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TipoJuridico';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PaisDomicilio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TipoDomicilio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PaisResidenciaControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PaisDomicilioControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TipoDomicilioControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='3. SALDO DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='DivisaSaldo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. INFORMACIoN ADICIONAL DE LA INSTITUCIoN FINANCIERA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PaisResidenciaDeclarante';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. INFORMACIoN ADICIONAL DE LA INSTITUCIoN FINANCIERA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TipoInstitucion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. INFORMACIoN ADICIONAL DE LA INSTITUCIoN FINANCIERA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PoseeCuentasFinanciera';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='CuentaNoDocumentada';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. ANTECEDENTES DEL TITULAR DE LA CUENTA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='CuentaCerrada';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. REGISTRO DE CONTROLADORES DE ENTIDAD NO FINANCIERA PASIVA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='PaisTaxIDControlador';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='5. PAGOS A LA CUENTA FINANCIERA'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TipoPagoCuentaFinanciera';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DATOS DE CONTROL'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TipoRegistro';

COMMIT;
