SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='TEXT', "Grupo"='8. Control'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='Observaciones';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='ID', "Grupo"='8. Control'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='IdCartera';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='ID', "Grupo"='8. Control'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='FolioOperacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='N', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='TEXT', "Grupo"='8. Control'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='FuenteDatos';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='TEXT', "Grupo"='8. Control'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='VboUsuario';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='1. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoModoContrato';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='DATE_FILTER', "Grupo"='1. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='FechaOperacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='4. Activo Subyacente'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoActivoSubyacente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='ID_CLIENTE', "Grupo"=''
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='IdCliente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='ID_PORTAFOLIO', "Grupo"=''
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='IdPortafolio';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='DATE', "Grupo"='3. Contrato / ConfirmaciÃƒÂ³n'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='FechaSuscripcionContrato';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='TEXT', "Grupo"='1. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='TaxID';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='ID', "Grupo"='3. Contrato / ConfirmaciÃƒÂ³n'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='NumeroContrato';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='5. Segundo Activo Subyacente (SÃƒÂ³lo Swap)'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoSegundoActivoSubyacente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='DATE', "Grupo"='2. Acuerdo Marco'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='FechaSuscripcionAcuerdo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='ID', "Grupo"='2. Acuerdo Marco'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='NumeroAcuerdoMarco';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='3. Contrato / ConfirmaciÃƒÂ³n'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='Instrumento';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='4. Activo Subyacente'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='OtroActivoSubyacente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='5. Segundo Activo Subyacente (SÃƒÂ³lo Swap)'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='TipoSegundoActivoSubyacente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='DECIMAL', "Grupo"='5. Segundo Activo Subyacente (SÃƒÂ³lo Swap)'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='TasaSegundoActivoSubyacente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='AMOUNT', "Grupo"='7. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='MontoNocional';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='DATE', "Grupo"='7. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='FechaVencimiento';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='DECIMAL', "Grupo"='4. Activo Subyacente'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='TasaActivoSubyacente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='5. Segundo Activo Subyacente (SÃƒÂ³lo Swap)'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='OtroSegundoActivoSubyacente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='AMOUNT', "Grupo"='6. Precio futuro contratado (futuros, forwards y opciones)'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='PrecioFuturo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='6. Precio futuro contratado (futuros, forwards y opciones)'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoMonedaFuturo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='TEXT', "Grupo"='7. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='SegundoMontoNocional';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='N', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='ID', "Grupo"='8. Control'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='IdDocumento';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='N', "Obligatorio"='N', "OcultoEnMantenedor"='S', "TipoDato"='ID', "Grupo"=''
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='ID';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='RUT_CLIENTE', "Grupo"='1. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='RutCliente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='7. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoUnidad';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='2. Acuerdo Marco'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='TipoAcuerdoMarco';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='3. Contrato / ConfirmaciÃƒÂ³n'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='TipoEvento';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='3. Contrato / ConfirmaciÃƒÂ³n'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='TipoContrato';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='S', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='3. Contrato / ConfirmaciÃƒÂ³n'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='ModalidadCumplimiento';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='3. Contrato / ConfirmaciÃƒÂ³n'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoPosicionDeclarante';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='4. Activo Subyacente'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='TipoActivoSubyacente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='6. Precio futuro contratado (futuros, forwards y opciones)'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoPrecioFuturo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='7. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoSegundaUnidad';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='1. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoRelacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='S', "Obligatorio"='N', "OcultoEnMantenedor"='N', "TipoDato"='NEMO', "Grupo"='1. Datos de los contratos informados'
WHERE "Esquema"='DJ1820' AND "Version"='2024/01' AND "Nombre"='CodigoPaisResidencia';

COMMIT;