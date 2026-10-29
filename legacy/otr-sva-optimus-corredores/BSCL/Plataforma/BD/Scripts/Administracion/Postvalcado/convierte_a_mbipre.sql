--nombre archivo original "Update de Usuario y Configuracion en volcado a Ambiente PRE.sql"
--para ejecutar toda vez que se haga volcado desde PRO a PRE MBI
SET SCHEMA BSCL;
SELECT * FROM "BSCL.Plataforma.BD::PLA.Empresa";
UPDATE "BSCL.Plataforma.BD::PLA.Empresa" SET "Nombre" = CONCAT('PRE ',"Nombre");

UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario"
SET "CodigoUsuario" = 'WEBSERVICES_PRE'
,"Clave" = 'FC626BFE5E58FE28F045030F5BAE0101CAD71CE3A7D40B93D163FAAC531056DC'
WHERE "CodigoUsuario" = 'WEBSERVICES';

UPDATE BSCL."BSCL.Plataforma.BD::PLA.Configuracion" 
SET "Valor" = 'PRE'
WHERE "CodigoParametro" = 'PLA.Ambiente';

UPDATE BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" 
SET "Valor"='https://masterdte.bcs.cl:1080' 
WHERE "IdEmpresa"=1 AND "CodigoParametro"='DTE_URL';
UPDATE BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" 
SET "Valor"='yJ39hEbP@l402' 
WHERE "IdEmpresa"=1 AND "CodigoParametro"='DTE_PASS';

UPDATE BSCL."BSCL.Plataforma.BD::PLA.Configuracion" 
SET "Valor" = 'QA'
WHERE lower("CodigoParametro") like 'evt.tipoenvio%';

UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario"
SET "IdEstado"='ACT',
"DebeCambiarClave" = 'N'
WHERE "IdUsuario"=288;

UPDATE BSCL."BSCL.Plataforma.BD::ACC.Usuario"
SET "IdEstado"='ACT',
"DebeCambiarClave" = 'N'
WHERE "IdUsuario"=291;

UPDATE BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"
SET "Nodo"='' WHERE "IdTipoPrograma" IN ('DTEENV','DTEACT','DTELIB');

