INSERT INTO "BSCL.SocioNegocio.BD::PRO.Proveedor"(
    "IdProveedor" ,
    "IdPersona" ,
    "IdEmpresa" ,
    "IdEstado" ,
    "IdAreaProveedor",
    "FechaIngreso" ,
    "FechaCreacion" ,
    "FechaModificacion",
    "EsEnrolado",
    "IdUsuarioCreador"  
 )VALUES(
     :IdProveedor,
     :IdPersona,
     :IdEmpresa,
     :IdEstado,
     :PRO_IdAreaProveedor,
     CURRENT_UTCTIMESTAMP,
     CURRENT_UTCTIMESTAMP,
     CURRENT_UTCTIMESTAMP,
     'S',
     :IdUsuario
 ); 