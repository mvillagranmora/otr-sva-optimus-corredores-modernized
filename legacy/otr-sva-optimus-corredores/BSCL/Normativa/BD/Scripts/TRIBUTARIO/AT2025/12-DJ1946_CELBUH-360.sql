SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"=''
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='ID';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='2. DATOS DE CONTROL'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='IdDocumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='1. Transacciones informadas'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorIdentificacionTributaria';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='1. Transacciones informadas'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='IntermediarioIdentificacionTributaria';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='Operacion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='TipoRenta';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='RentaBruta';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='TasaDeRetencion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='Impuesto';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='RentaLiquida';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='CreditoIDPC';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='OtrosCreditos';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='F50Codigo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='F50Folio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='MonedaPago';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='TipoCambioPago';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PagadorTipoJuridico';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PagadorRazonSocial';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PagadorNombre';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='RUT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PagadorRut';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PagadorDomicilio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PagadorCiudad';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PagadorRelacion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PagadorHolding';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorTipoJuridico';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorPaisResidencia';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorRazonSocial';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorNombre';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='NumPerIdenTri_C51';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='RUT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-2'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorRut';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorFechaNacimiento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorTipoDomicilio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorDomicilio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorCiudad';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='PerceptorPais';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='IntermediarioTipoJuridico';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='IntermediarioPaisResidencia';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='IntermediarioRazonSocial';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='IntermediarioNombre';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='NumInterIdenTri_C52';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='1. Transacciones informadas-3'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='IdCliente';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='Numero';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE_FILTER', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. Transacciones informadas-1'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='FechaPago';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='2. DATOS DE CONTROL'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='Observaciones';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='2. DATOS DE CONTROL'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='VboUsuario';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='2. DATOS DE CONTROL'
WHERE "Esquema"='DJ1946' AND "Version"='2024' AND "Nombre"='FuenteDatos';

COMMIT;