--*
--*
--* ---------------------------------------------------------------------------------------------------------
--* VOLCADO DE SURA PRO A SURA PRE
--* ---------------------------------------------------------------------------------------------------------
--*
--*
SET SCHEMA BSCL;

UPDATE  "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" SET "Valor" = 'https://masterdte.bcs.cl:1080' WHERE "CodigoParametro" = 'DTE_URL';
UPDATE  "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" SET "Valor" = 'yJ39hEbP@l402' WHERE "CodigoParametro" = 'DTE_PASS';
UPDATE  "BSCL.Plataforma.BD::PLA.Empresa" SET "Nombre" = 'PRE SURA' WHERE "Codigo" = 'CC001';
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" = 'QA' WHERE "CodigoParametro" ='EVT.TipoEnvioCorreo'  ;      
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" = 'PRE' WHERE "CodigoParametro" ='PLA.Ambiente'      ;  

UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario" SET "CodigoUsuario"='PERSHINGMB_PREGTD' WHERE "CodigoUsuario"='PERSHINGMB_GTD';
UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario" SET "CodigoUsuario"='CANALWEBMB_PREGTD' WHERE "CodigoUsuario"='CANALWEBMB_GTD';
UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario" SET "CodigoUsuario"='CANALNUEVAWEB_PREGTD' WHERE "CodigoUsuario"='CANALNUEVAWEB_GTD';
UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario" SET "CodigoUsuario"='BIZNETMB_PREGTD' WHERE "CodigoUsuario"='BIZNETMB_GTD';
UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario" SET "IdEstado"='ACT',"Clave" = 'E0BC614E4FD035A488619799853B075143DEEA596C477B8DC077E309C0FE42E9' WHERE "CodigoUsuario"='CARGABSX';
UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario" SET "IdEstado"='ACT',"Clave" = 'E0BC614E4FD035A488619799853B075143DEEA596C477B8DC077E309C0FE42E9' WHERE "CodigoUsuario"='SISTEMA';
UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario" set "FechaBloqueoTemporal" = '0001-01-01 00:00:00';


TRUNCATE TABLE bscl."BSCL.Plataforma.BD::PLA.AuditoriaWebServices";
TRUNCATE TABLE bscl."BSCL.Orden.BD::ORD.ControlMercado";
TRUNCATE TABLE bscl."BSCL.CuentaInversion.BD::CTA.SaldoFechaEfectiva";
TRUNCATE TABLE bscl."BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento";
truncate TABLE bscl."BSCL.SocioNegocio.BD::CLI.Anotacion";

--AGREGAR ROL SEGURIDAD A BIZNET PRE

COMMIT;
