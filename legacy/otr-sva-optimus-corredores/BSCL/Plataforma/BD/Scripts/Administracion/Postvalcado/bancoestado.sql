--*
--*
--* ---------------------------------------------------------------------------------------------------------
--* VOLCADO DE BANCOESTADO PRO A BANCOESTADO PRE
--* ---------------------------------------------------------------------------------------------------------
--*
--*
SET SCHEMA BSCL;

UPDATE  "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" SET "Valor" = 'https://masterdte.bcs.cl:1080' WHERE "CodigoParametro" = 'DTE_URL';
UPDATE  "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" SET "Valor" = 'yJ39hEbP@l402' WHERE "CodigoParametro" = 'DTE_PASS';
UPDATE  "BSCL.Plataforma.BD::PLA.Empresa" SET "Nombre" = 'PRE BANCOESTADO CORREDORES DE BOLSA' WHERE "Codigo" = 'CC001';
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" = 'QA' WHERE "CodigoParametro" ='EVT.TipoEnvioCorreo'  ;      
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" = 'PRE' WHERE "CodigoParametro" ='PLA.Ambiente'      ;  

COMMIT;
