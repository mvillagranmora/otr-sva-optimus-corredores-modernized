SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE_FILTER', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='FecMovto';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"=''
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='ID';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='4. DATOS DE CONTROL'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='IdDocumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaAfecGCCCIDPC_C6';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaAfecGCCCIDPCAL311216_C7';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaAfecGCCDIDPCVol_C8';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaAfecGCSinDer_C9';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaREXTribCumpRentasRap_C10';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaREXTribCumpOtrasRentas_C11';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaREXTribCumpExcDist_C12';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaREXTribCumpUtiISFUT_C13';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaREXTribCumpRentasISFUT_C14';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaREXRentExIGCAfecIAart11_C15';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaREXRentExIGCIA_C16';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='MtoDivReaREXNoConstRenta_C17';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='TipoSociedad';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CantCuotas3112';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='Mercado';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017RentAfecNoRestSDD_C18';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017RentAfecNoRestCDD_C19';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017RentAfecNoRestSDD_C20';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017RentAfecNoRestCDD_C21';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017RentAfecRestSDD_C22';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017RentAfecRestCDD_C23';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017RentExenRestSDD_C24';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017RentExenRestCDD_C25';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCDesde01012017CredIPE_C26';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCHasta31122016RentAfecSDD_C27';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCHasta31122016RentAfecCDD_C28';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCHasta31122016RentExenSDD_C29';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCHasta31122016RentExenCDD_C30';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCHasta31122016CredIPE_C31';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='CIGCImptoTasaAdicional_C32';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='DevCapArt17N7LIR_C33';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='NumCertificado_C34';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='Monto';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='Cuenta';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS-2'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='SecEvento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='4. DATOS DE CONTROL'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='Observaciones';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='4. DATOS DE CONTROL'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='FuenteDatos';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='4. DATOS DE CONTROL'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='AcogidoISFUT';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='4. DATOS DE CONTROL'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='IdEventoCapitalCliente';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='4. DATOS DE CONTROL'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='OrigenCalificacion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='4. DATOS DE CONTROL'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='VboUsuario';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='Nemo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='RutSociedad';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1949' AND "Version"='2024' AND "Nombre"='RutCliente';

COMMIT;