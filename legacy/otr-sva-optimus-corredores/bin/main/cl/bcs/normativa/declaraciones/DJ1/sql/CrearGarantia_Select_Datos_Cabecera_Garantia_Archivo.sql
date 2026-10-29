SELECT 
CAB."IdDocumento",
CAB."VboUsuario",
CAB."Observaciones",
CAB."KRutCtraparte1",
CAB."KIdContrato",
CAB."KFechaContrato",
CAB."HoraContrato",
CAB."KIdOpeEstruc",
CAB."kPeriodoComercial",
CAB."kProducto",
CAB."Rectifica"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" CAB
INNER JOIN "BSCL.Normativa.BD::GBL.CabeceraDJ1_Garantia" TMP
ON TMP."KRutCtraparte1" = CAB."KRutCtraparte1" 
AND TMP."KIdContrato" = CAB."KIdContrato"
AND TMP."KFechaContrato" =  CAB."KFechaContrato"
AND TMP."KIdOpeEstruc" =  CAB."KIdOpeEstruc"
AND TMP."KProducto" =  CAB."kProducto"
AND TMP."KPeriodoComercial" =  CAB."kPeriodoComercial";
